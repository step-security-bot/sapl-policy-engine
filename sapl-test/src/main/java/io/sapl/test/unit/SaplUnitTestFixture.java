/*
 * Copyright (C) 2017-2023 Dominic Heutelbeck (dominic@heutelbeck.com)
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
package io.sapl.test.unit;

import io.sapl.test.utils.DocumentHelper;
import io.sapl.interpreter.SAPLInterpreter;
import io.sapl.test.SaplTestException;
import io.sapl.test.SaplTestFixtureTemplate;
import io.sapl.test.coverage.api.CoverageAPIFactory;
import io.sapl.test.lang.TestSaplInterpreter;
import io.sapl.test.steps.GivenStep;
import io.sapl.test.steps.WhenStep;

public class SaplUnitTestFixture extends SaplTestFixtureTemplate {

    private static final String ERROR_MESSAGE_MISSING_SAPL_DOCUMENT_NAME = """
            Before constructing a test case you have to specify the filename where to find your SAPL policy!

            Probably you forgot to call ".setSaplDocumentName("")\"""";

    private final String saplDocumentName;

    /**
     * Fixture for constructing a unit test case
     *
     * @param saplDocumentName path relative to your classpath to the sapl document.
     *                         If your policies are located at the root of the
     *                         classpath or in the standard path {@code "policies/"}
     *                         in your {@code resources} folder you only have to
     *                         specify the name of the .sapl file. If your policies
     *                         are located at some special place you have to
     *                         configure a relative path like
     *                         {@code "yourSpecialDirectory/policies/myPolicy.sapl"}
     */
    public SaplUnitTestFixture(String saplDocumentName) {
        this.saplDocumentName = saplDocumentName;
    }

    @Override
    public GivenStep constructTestCaseWithMocks() {
        if (this.saplDocumentName == null || this.saplDocumentName.isEmpty()) {
            throw new SaplTestException(ERROR_MESSAGE_MISSING_SAPL_DOCUMENT_NAME);
        }

        return StepBuilder.newBuilderAtGivenStep(DocumentHelper.readSaplDocument(saplDocumentName, getSaplInterpreter()), this.attributeCtx, this.functionCtx,
                this.variables);
    }

    @Override
    public WhenStep constructTestCase() {
        if (this.saplDocumentName == null || this.saplDocumentName.isEmpty()) {
            throw new SaplTestException(ERROR_MESSAGE_MISSING_SAPL_DOCUMENT_NAME);
        }

        return StepBuilder.newBuilderAtWhenStep(DocumentHelper.readSaplDocument(saplDocumentName, getSaplInterpreter()), this.attributeCtx, this.functionCtx,
                this.variables);
    }

    private SAPLInterpreter getSaplInterpreter() {
        return new TestSaplInterpreter(CoverageAPIFactory.constructCoverageHitRecorder(resolveCoverageBaseDir()));
    }

}
