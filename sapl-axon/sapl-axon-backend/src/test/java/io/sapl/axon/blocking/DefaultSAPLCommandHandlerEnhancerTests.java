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
package io.sapl.axon.blocking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.axonframework.commandhandling.CommandMessageHandlingMember;
import org.axonframework.messaging.annotation.MessageHandlingMember;
import org.axonframework.queryhandling.annotation.QueryHandlingMember;
import org.junit.jupiter.api.Test;

import io.sapl.axon.commandhandling.CommandPolicyEnforcementPoint;

public class DefaultSAPLCommandHandlerEnhancerTests {

	final CommandMessageHandlingMember<?> command = mock(CommandMessageHandlingMember.class);
	QueryHandlingMember<?> query = mock(QueryHandlingMember.class);
	final MessageHandlingMember<?> original = mock(MessageHandlingMember.class);
	final CommandPolicyEnforcementPoint pep = mock(CommandPolicyEnforcementPoint.class);
	
	@Test
	void when_CommandMessageHandlingMemberType_then_wrapHandlerReturns_CommandMessageHandlingMember() {
		DefaultSAPLCommandHandlerEnhancer enhancer = new DefaultSAPLCommandHandlerEnhancer(pep);
		MessageHandlingMember<?> member = enhancer.wrapHandler(command);
		
		assertEquals(DefaultSAPLCommandHandlingMember.class, member.getClass());
	}

	@Test
	void when_OriginalHandlingMemberType_then_wrapHandlerReturns_Original() {
		DefaultSAPLCommandHandlerEnhancer enhancer = new DefaultSAPLCommandHandlerEnhancer(pep);
		MessageHandlingMember<?> member = enhancer.wrapHandler(original);
		
		assertEquals(original, member);
	}
}

 