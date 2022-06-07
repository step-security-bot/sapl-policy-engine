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
package io.sapl.grammar.ide.contentassist;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.xtext.testing.TestCompletionConfiguration;
import org.junit.jupiter.api.Test;

public class IdeStepCompletionTests extends CompletionTests {

	@Test
	public void testCompletion_EmptyAttributeStepReturnsClockFunctions() {
		testCompletion((TestCompletionConfiguration it) -> {
			String policy = "policy \"test\" permit where subject.<";
			it.setModel(policy);
			it.setColumn(policy.length());
			it.setAssertCompletionList(completionList -> {
				var expected = List.of("clock.millis", "clock.now", "clock.ticker");
				assertProposalsSimple(expected, completionList);
			});
		});
	}

	@Test
	public void testCompletion_AttributeStepWithPrefixReturnsMatchingClockFunction() {
		testCompletion((TestCompletionConfiguration it) -> {
			String policy = "policy \"test\" permit where subject.<clock.n";
			it.setModel(policy);
			it.setColumn(policy.length());
			it.setAssertCompletionList(completionList -> {
				var expected = List.of("clock.now");
				assertProposalsSimple(expected, completionList);
			});
		});
	}

	@Test
	public void testCompletion_AttributeStepWithNoMatchingPrefixReturnsNoMatchingFunction() {
		testCompletion((TestCompletionConfiguration it) -> {
			String policy = "policy \"test\" permit where subject.<foo";
			it.setModel(policy);
			it.setColumn(policy.length());
			it.setAssertCompletionList(completionList -> {
				var expected = new ArrayList<String>();
				assertProposalsSimple(expected, completionList);
			});
		});
	}

	@Test
	public void testCompletion_HeadEmptyAttributeStepReturnsClockFunctions() {
		testCompletion((TestCompletionConfiguration it) -> {
			String policy = "policy \"test\" permit where subject.|<";
			it.setModel(policy);
			it.setColumn(policy.length());
			it.setAssertCompletionList(completionList -> {
				var expected = List.of("clock.millis", "clock.now", "clock.ticker");
				assertProposalsSimple(expected, completionList);
			});
		});
	}

	@Test
	public void testCompletion_HeadAttributeStepWithPrefixReturnsMatchingClockFunction() {
		testCompletion((TestCompletionConfiguration it) -> {
			String policy = "policy \"test\" permit where subject.|<clock.n";
			it.setModel(policy);
			it.setColumn(policy.length());
			it.setAssertCompletionList(completionList -> {
				var expected = List.of("clock.now");
				assertProposalsSimple(expected, completionList);
			});
		});
	}

	@Test
	public void testCompletion_HeadAttributeStepWithNoMatchingPrefixReturnsNoMatchingFunction() {
		testCompletion((TestCompletionConfiguration it) -> {
			String policy = "policy \"test\" permit where subject.|<";
			it.setModel(policy);
			it.setColumn(policy.length());
			it.setAssertCompletionList(completionList -> {
				var expected = new ArrayList<String>();
				assertProposalsSimple(expected, completionList);
			});
		});
	}

}
