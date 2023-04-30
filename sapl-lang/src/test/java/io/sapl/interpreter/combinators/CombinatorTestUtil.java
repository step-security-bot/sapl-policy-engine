package io.sapl.interpreter.combinators;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.sapl.api.pdp.AuthorizationDecision;
import io.sapl.api.pdp.AuthorizationSubscription;
import io.sapl.api.pdp.Decision;
import io.sapl.interpreter.DefaultSAPLInterpreter;
import io.sapl.interpreter.functions.AnnotationFunctionContext;
import io.sapl.interpreter.pip.AnnotationAttributeContext;
import lombok.experimental.UtilityClass;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@UtilityClass
public class CombinatorTestUtil {
	private static final DefaultSAPLInterpreter     INTERPRETER  = new DefaultSAPLInterpreter();
	private static final AnnotationAttributeContext ATTRIBUTE_CTX = new AnnotationAttributeContext();
	private static final AnnotationFunctionContext  FUNCTION_CTX  = new AnnotationFunctionContext();
	private static final Map<String, JsonNode>      VARIABLES    = new HashMap<>();

	public static void validateDecision(AuthorizationSubscription subscription, String policySet, Decision expected) {
		var decisions = evaluate(subscription, policySet).map(AuthorizationDecision::getDecision);
		StepVerifier.create(decisions).expectNext(expected).verifyComplete();
	}

	public static void validateResource(AuthorizationSubscription subscription, String policySet,
			Optional<JsonNode> expected) {
		var decisions = evaluate(subscription, policySet).map(AuthorizationDecision::getResource);
		StepVerifier.create(decisions).expectNext(expected).verifyComplete();
	}

	public static void validateObligations(AuthorizationSubscription subscription, String policySet,
			Optional<ArrayNode> expected) {
		var decisions = evaluate(subscription, policySet).map(AuthorizationDecision::getObligations);
		StepVerifier.create(decisions).expectNext(expected).verifyComplete();
	}

	public static void validateAdvice(AuthorizationSubscription subscription, String policySet,
			Optional<ArrayNode> expected) {
		var decisions = evaluate(subscription, policySet).map(AuthorizationDecision::getAdvice);
		StepVerifier.create(decisions).expectNext(expected).verifyComplete();
	}

	private Flux<AuthorizationDecision> evaluate(AuthorizationSubscription subscription, String policySet) {
		return INTERPRETER.evaluate(subscription, policySet, ATTRIBUTE_CTX, FUNCTION_CTX, VARIABLES);
	}

}
