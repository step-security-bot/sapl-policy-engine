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
package io.sapl.grammar.sapl.impl;

import static io.sapl.grammar.sapl.impl.util.OperatorUtil.arithmeticOperator;

import java.util.Map;

import io.sapl.api.interpreter.Trace;
import io.sapl.api.interpreter.Val;
import io.sapl.grammar.sapl.Less;
import reactor.core.publisher.Flux;

/**
 * Checks for a left value being less than a right value.
 * <p>
 * Grammar: {@code Comparison returns Expression: Prefixed (({Less.left=current}
 * '&lt;') right=Prefixed)? ;}
 */
public class LessImplCustom extends LessImpl {

    @Override
    public Flux<Val> evaluate() {
        return arithmeticOperator(this, this::lessThan);
    }

    private Val lessThan(Val left, Val right) {
        return Val.of(left.decimalValue().compareTo(right.decimalValue()) < 0).withTrace(Less.class,
                Map.of(Trace.LEFT, left, Trace.RIGHT, right));
    }

}
