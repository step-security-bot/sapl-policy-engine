/*
 * Copyright (C) 2017-2024 Dominic Heutelbeck (dominic@heutelbeck.com)
 *
 * SPDX-License-Identifier: Apache-2.0
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
package io.sapl.interpreter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.sapl.api.interpreter.Trace;
import io.sapl.api.interpreter.Val;
import io.sapl.api.pdp.AuthorizationDecision;

class PolicySetDecisionTests {

    @Test
    void error() {
        var decision = PolicySetDecision.error("documentName", "error message");
        assertThat(decision.getTrace().get(Trace.DOCUMENT_TYPE).textValue()).isEqualTo("policy set");
        assertThat(decision.getTrace().get(Trace.POLICY_SET_NAME).textValue()).isEqualTo("documentName");
    }

    @Test
    void ofCombined() {
        var decision = PolicySetDecision
                .of(CombinedDecision.of(AuthorizationDecision.NOT_APPLICABLE, "algorithm name"), "documentName")
                .withTargetResult(Val.TRUE);
        assertThat(decision.getTrace().get(Trace.DOCUMENT_TYPE).textValue()).isEqualTo("policy set");
        assertThat(decision.getTrace().get(Trace.POLICY_SET_NAME).textValue()).isEqualTo("documentName");
        assertThat(decision.getTrace().get(Trace.TARGET).get(Trace.VALUE).asBoolean()).isTrue();
    }

    @Test
    void ofTargetError() {
        var decision = PolicySetDecision.ofTargetError("documentName", Val.error("error message"), "test");
        assertThat(decision.getTrace().get(Trace.TARGET).get(Trace.VALUE).textValue())
                .isEqualTo("|ERROR| error message");
        assertThat(decision.getTrace().get(Trace.POLICY_SET_NAME).textValue()).isEqualTo("documentName");
    }
}
