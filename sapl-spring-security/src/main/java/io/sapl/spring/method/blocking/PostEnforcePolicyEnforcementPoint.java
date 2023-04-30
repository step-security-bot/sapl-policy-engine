/*
 * Copyright © 2017-2022 Dominic Heutelbeck (dominic@heutelbeck.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.sapl.spring.method.blocking;

import java.util.Optional;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import io.sapl.api.pdp.AuthorizationDecision;
import io.sapl.api.pdp.Decision;
import io.sapl.api.pdp.PolicyDecisionPoint;
import io.sapl.spring.constraints.BlockingPostEnforceConstraintHandlerBundle;
import io.sapl.spring.constraints.ConstraintEnforcementService;
import io.sapl.spring.method.metadata.PostEnforceAttribute;
import io.sapl.spring.subscriptions.WebAuthorizationSubscriptionBuilderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Exceptions;

/**
 * Method post-invocation handling based on a SAPL policy decision point.
 */
@Slf4j
@RequiredArgsConstructor
public class PostEnforcePolicyEnforcementPoint {
	
	private final ObjectFactory<PolicyDecisionPoint> pdpFactory;
	private final ObjectFactory<ConstraintEnforcementService> constraintEnforcementServiceFactory;
	private final ObjectFactory<WebAuthorizationSubscriptionBuilderService> subscriptionBuilderFactory;

	private PolicyDecisionPoint pdp;
	private ConstraintEnforcementService constraintEnforcementService;
	private WebAuthorizationSubscriptionBuilderService subscriptionBuilder;

	/**
	 * Lazy loading of dependencies decouples security infrastructure from domain logic in
	 * initialization. This avoids beans to become not eligible for Bean post-processing.
	 */
	protected void lazyLoadDependencies() {
		if (pdp == null)
			pdp = pdpFactory.getObject();

		if (constraintEnforcementService == null)
			constraintEnforcementService = constraintEnforcementServiceFactory.getObject();

		if (subscriptionBuilder == null)
			subscriptionBuilder = subscriptionBuilderFactory.getObject();
	}
	
	@SuppressWarnings("unchecked") // is actually checked, warning is false positive
	public Object after(Authentication authentication, MethodInvocation methodInvocation,
			PostEnforceAttribute postEnforceAttribute, Object returnedObject) {
		lazyLoadDependencies();

		log.debug("Attribute        : {}", postEnforceAttribute);

		var isOptional                         = returnedObject instanceof Optional;
		var returnedObjectForAuthzSubscription = returnedObject;
		var returnType                         = methodInvocation.getMethod().getReturnType();

		if (isOptional) {
			var optObject = (Optional<Object>) returnedObject;
			if (optObject.isPresent()) {
				returnedObjectForAuthzSubscription = ((Optional<Object>) returnedObject).get();
				returnType                         = ((Optional<Object>) returnedObject).get().getClass();
			} else {
				returnedObjectForAuthzSubscription = null;
				returnType                         = postEnforceAttribute.getGenericsType();
			}
		}

		var authzSubscription = subscriptionBuilder.constructAuthorizationSubscriptionWithReturnObject(authentication,
				methodInvocation, postEnforceAttribute, returnedObjectForAuthzSubscription);
		log.debug("AuthzSubscription: {}", authzSubscription);

		var authzDecisions = pdp.decide(authzSubscription);
		if (authzDecisions == null) {
			throw new AccessDeniedException(
					String.format("Access Denied by PEP. PDP returned null. %s", postEnforceAttribute));
		}

		var authzDecision = authzDecisions.blockFirst();
		log.debug("AuthzDecision    : {}", authzDecision);

		if (authzDecision == null) {
			throw new AccessDeniedException(
					String.format("Access Denied by PEP. PDP did not return a decision. %s", postEnforceAttribute));
		}

		return enforceDecision(isOptional, returnedObjectForAuthzSubscription, returnType, authzDecision);
	}

	@SuppressWarnings("unchecked") // False positive. The type is checked beforehand
	private <T> Object enforceDecision(boolean isOptional, Object returnedObjectForAuthzSubscription,
			Class<T> returnType, AuthorizationDecision authzDecision) {
		BlockingPostEnforceConstraintHandlerBundle<T> constraintHandlerBundle = null;
		try {
			constraintHandlerBundle = constraintEnforcementService.blockingPostEnforceBundleFor(authzDecision,
					returnType);
		} catch (Throwable e) {
			Exceptions.throwIfFatal(e);
			throw new AccessDeniedException("Access Denied by PEP. Failed to construct bundle.", e);
		}

		if (constraintHandlerBundle == null) {
			throw new AccessDeniedException("Access Denied by PEP. No constraint handler bundle.");
		}

		try {
			constraintHandlerBundle.handleOnDecisionConstraints();

			var isNotPermit = authzDecision.getDecision() != Decision.PERMIT;
			if (isNotPermit)
				throw new AccessDeniedException("Access denied by PDP");

			var result = constraintEnforcementService.replaceResultIfResourceDefinitionIsPresentInDecision(
					authzDecision, (T) returnedObjectForAuthzSubscription, returnType);
			result = constraintHandlerBundle.handleAllOnNextConstraints((T) result);

			if (isOptional)
				return Optional.ofNullable(result);

			return result;
		} catch (Throwable e) {
			Throwable e1 = constraintHandlerBundle.handleAllOnErrorConstraints(e);
			Exceptions.throwIfFatal(e1);
			throw new AccessDeniedException("Access Denied by PEP. Failed to enforce decision", e1);
		}
	}

}
