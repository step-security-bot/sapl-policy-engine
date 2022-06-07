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
package io.sapl.interpreter.combinators;

/**
 * Enumeration of the algorithms supported by the SAPL policy engine to combine SAPL
 * documents (holding a policy set or a policy).
 */
public enum PolicyDocumentCombiningAlgorithm {

	DENY_OVERRIDES, PERMIT_OVERRIDES, ONLY_ONE_APPLICABLE, DENY_UNLESS_PERMIT, PERMIT_UNLESS_DENY

}
