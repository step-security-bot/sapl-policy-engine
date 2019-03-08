/**
 * Copyright © 2017 Dominic Heutelbeck (dheutelbeck@ftk.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package io.sapl.grammar.sapl.impl;

import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.emf.ecore.EObject;

import com.fasterxml.jackson.databind.JsonNode;

import io.sapl.api.interpreter.PolicyEvaluationException;
import io.sapl.grammar.sapl.Step;
import io.sapl.interpreter.EvaluationContext;
import reactor.core.publisher.Flux;

public class BasicIdentifierImplCustom extends BasicIdentifierImpl {

	private static final String UNBOUND_VARIABLE = "Evaluation error. Variable '%s' is not defined.";

	@Override
	public Flux<Optional<JsonNode>> evaluate(EvaluationContext ctx, boolean isBody, Optional<JsonNode> relativeNode) {
		if (!ctx.getVariableCtx().exists(getIdentifier())) {
			return Flux.error(new PolicyEvaluationException(String.format(UNBOUND_VARIABLE, getIdentifier())));
		}

		try {
			final JsonNode resultBeforeSteps = ctx.getVariableCtx().get(getIdentifier());
			return evaluateStepsFilterSubtemplate(Optional.of(resultBeforeSteps), steps, ctx, isBody, relativeNode);
		} catch (PolicyEvaluationException e) {
			return Flux.error(e);
		}
	}

	@Override
	public int hash(Map<String, String> imports) {
		int hash = 17;
		hash = 37 * hash + Objects.hashCode(getClass().getTypeName());
		hash = 37 * hash + ((getFilter() == null) ? 0 : getFilter().hash(imports));
		hash = 37 * hash + Objects.hashCode(getIdentifier());
		for (Step step : getSteps()) {
			hash = 37 * hash + ((step == null) ? 0 : step.hash(imports));
		}
		hash = 37 * hash + ((getSubtemplate() == null) ? 0 : getSubtemplate().hash(imports));
		return hash;
	}

	@Override
	public boolean isEqualTo(EObject other, Map<String, String> otherImports, Map<String, String> imports) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}
		final BasicIdentifierImplCustom otherImpl = (BasicIdentifierImplCustom) other;
		if ((getFilter() == null) ? (getFilter() != otherImpl.getFilter())
				: !getFilter().isEqualTo(otherImpl.getFilter(), otherImports, imports)) {
			return false;
		}
		if ((getSubtemplate() == null) ? (getSubtemplate() != otherImpl.getSubtemplate())
				: !getSubtemplate().isEqualTo(otherImpl.getSubtemplate(), otherImports, imports)) {
			return false;
		}
		if (!Objects.equals(getIdentifier(), otherImpl.getIdentifier())) {
			return false;
		}
		if (getSteps().size() != otherImpl.getSteps().size()) {
			return false;
		}
		ListIterator<Step> left = getSteps().listIterator();
		ListIterator<Step> right = otherImpl.getSteps().listIterator();
		while (left.hasNext()) {
			Step lhs = left.next();
			Step rhs = right.next();
			if (!lhs.isEqualTo(rhs, otherImports, imports)) {
				return false;
			}
		}
		return true;
	}

}
