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
package io.sapl.interpreter

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import io.sapl.api.interpreter.PolicyEvaluationException
import io.sapl.api.pdp.Decision
import io.sapl.api.pdp.Request
import io.sapl.api.pdp.Response
import io.sapl.interpreter.functions.AnnotationFunctionContext
import io.sapl.interpreter.functions.FunctionContext
import io.sapl.interpreter.pip.AnnotationAttributeContext
import io.sapl.interpreter.pip.AttributeContext
import java.util.Collections
import java.util.HashMap
import java.util.Map
import java.util.Optional
import org.junit.Before
import org.junit.Test

import static org.hamcrest.CoreMatchers.equalTo
import static org.junit.Assert.assertThat
import io.sapl.functions.SelectionFunctionLibrary
import io.sapl.functions.FilterFunctionLibrary
import com.fasterxml.jackson.databind.node.ArrayNode

class SampleXACMLTest {

	 static final ObjectMapper MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
	 static final DefaultSAPLInterpreter INTERPRETER = new DefaultSAPLInterpreter();
	 static final AttributeContext ATTRIBUTE_CTX = new AnnotationAttributeContext();
	 static final FunctionContext FUNCTION_CTX = new AnnotationFunctionContext();
	 static final Map<String, JsonNode> SYSTEM_VARIABLES = Collections.unmodifiableMap(
		new HashMap<String, JsonNode>());
		
	 static Request request_example_two;

	@Before
	def void init() {
		FUNCTION_CTX.loadLibrary(new MockXACMLStringFunctionLibrary());
		FUNCTION_CTX.loadLibrary(new MockXACMLDateFunctionLibrary());
		FUNCTION_CTX.loadLibrary(new SelectionFunctionLibrary());
		FUNCTION_CTX.loadLibrary(new FilterFunctionLibrary());
		ATTRIBUTE_CTX.loadPolicyInformationPoint(new MockXACMLPatientProfilePIP());
		
		io.sapl.interpreter.SampleXACMLTest.request_example_two = MAPPER.readValue('''
			{
				"subject": {
					"id": "CN=Julius Hibbert",
					"role": "physician",
					"physician_id": "jh1234"
				},
				"resource": {
					"_type": "urn:example:med:schemas:record",
					"_content": {
						"patient": {
							"dob": "1992-03-21",
							"patient_number": "555555",
							"contact": {
								"email": "b.simpsons@example.com"
							}
						}
					},
					"_selector": "@.patient.dob"
				},
				"action": "read",
				"environment": {
					"current_date": "2010-01-11"
				}
			}
		''', Request)
	}
	
	def String policyExampleOne() {
		return '''
			policy "SimplePolicy1"
			/* Any subject with an e-mail name in the med.example.com 
			    domain can perform any action on any resource. */
			
			permit subject =~ "(?i).*@med\\.example\\.com"
		''';
	}

	@Test
	def void exampleOne() throws PolicyEvaluationException {
		val request_object = MAPPER.readValue('''
			{
				"subject": "bs@simpsons.com",
				"resource": "file://example/med/record/patient/BartSimpson",
				"action": "read"
			}
		''', Request)

		val expectedResponse = Response.notApplicable()

		assertThat("XACML example one not working as expected",
			INTERPRETER.evaluate(request_object, policyExampleOne(), ATTRIBUTE_CTX, FUNCTION_CTX, SYSTEM_VARIABLES),
			equalTo(expectedResponse));
	}
	
	@Test
	def void exampleOnePermit() throws PolicyEvaluationException {
		val request_object = MAPPER.readValue('''
			{
				"subject": "abc@Med.example.com",
				"resource": "file://example/med/record/patient/BartSimpson",
				"action": "read"
			}
		''', Request)

		val policyDefinition = '''
			policy "SimplePolicy1"
			/* Any subject with an e-mail name in the med.example.com 
			    domain can perform any action on any resource. */
			
			permit subject =~ "(?i).*@med\\.example\\.com"
		''';

		val expectedResponse = new Response(Decision.PERMIT, Optional.empty(), Optional.empty(), Optional.empty());

		assertThat("XACML example one not working as expected",
			INTERPRETER.evaluate(request_object, policyDefinition, ATTRIBUTE_CTX, FUNCTION_CTX, SYSTEM_VARIABLES),
			equalTo(expectedResponse));
	}
	
	def String policyExampleTwoRule1() {
		return '''
			policy "rule_1"
			/* A person may read any medical record in the
			    http://www.med.example.com/schemas/record.xsd namespace
			    for which he or she is the designated patient */
			   
			permit 
				resource._type == "urn:example:med:schemas:record" &
				string.starts_with(resource._selector, "@") &
				action == "read"
			where
				subject.role == "patient";
				subject.patient_number == resource._content.patient.patient_number;
		''';
	}
	
	@Test
	def void exampleTwoRule1() throws PolicyEvaluationException {
		val expectedResponse = Response.notApplicable()

		assertThat("XACML example two rule 1 not working as expected",
			INTERPRETER.evaluate(request_example_two, policyExampleTwoRule1(), ATTRIBUTE_CTX, FUNCTION_CTX, SYSTEM_VARIABLES),
			equalTo(expectedResponse));
	}
	
	@Test
	def void exampleTwoRule1Permit() throws PolicyEvaluationException {
		val request = MAPPER.readValue('''
			{
				"subject": {
					"id": "alice",
					"role": "patient",
					"patient_number": "555555"
				},
				"resource": {
					"_type": "urn:example:med:schemas:record",
					"_content": {
						"patient": {
							"dob": "1992-03-21",
							"patient_number": "555555",
							"contact": {
								"email": "b.simpsons@example.com"
							}
						}
					},
					"_selector": "@.patient.dob"
				},
				"action": "read",
				"environment": {
					"current_date": "2010-01-11"
				}
			}
		''', Request)
		
		val expectedResponse = new Response(Decision.PERMIT, Optional.empty, Optional.empty, Optional.empty)
		
		assertThat("XACML example two rule 1 not working as expected",
			INTERPRETER.evaluate(request, policyExampleTwoRule1(), ATTRIBUTE_CTX, FUNCTION_CTX, SYSTEM_VARIABLES),
			equalTo(expectedResponse));
	}
	
	def String policyExampleTwoRule2() {
		return '''
			policy "rule_2"
			/* A person may read any medical record in the
			    http://www.med.example.com/records.xsd namespace
			    for which he or she is the designated parent or guardian,
			    and for which the patient is under 16 years of age */
			   
			permit 
				resource._type == "urn:example:med:schemas:record" &
				string.starts_with(resource._selector, "@") &
				action == "read"
			where
				subject.role == "parent_guardian";
				subject.parent_guardian_id == resource._content.patient.patient_number.<patient.profile>.parentGuardian.id;
				date.diff("years", environment.current_date, resource._content.patient.dob) < 16;
		''';
	}
	
	@Test
	def void exampleTwoRule2() throws PolicyEvaluationException {
		val expectedResponse = Response.notApplicable()

		assertThat("XACML example two rule 2 not working as expected",
			INTERPRETER.evaluate(request_example_two, policyExampleTwoRule2(), ATTRIBUTE_CTX, FUNCTION_CTX, SYSTEM_VARIABLES),
			equalTo(expectedResponse));
	}
	
	@Test
	def void exampleTwoRule2Permit() throws PolicyEvaluationException {
		val request = MAPPER.readValue('''
			{
				"subject": {
					"id": "john",
					"role": "parent_guardian",
					"parent_guardian_id": "HS001"
				},
				"resource": {
					"_type": "urn:example:med:schemas:record",
					"_content": {
						"patient": {
							"dob": "1992-03-21",
							"patient_number": "555555",
							"contact": {
								"email": "b.simpsons@example.com"
							}
						}
					},
					"_selector": "@.patient.dob"
				},
				"action": "read",
				"environment": {
					"current_date": "2010-01-11"
				}
			}
		''', Request)
		
		val expectedResponse = new Response(Decision.PERMIT, Optional.empty, Optional.empty, Optional.empty)
		
		assertThat("XACML example two rule 2 not working as expected",
			INTERPRETER.evaluate(request, policyExampleTwoRule2(), ATTRIBUTE_CTX, FUNCTION_CTX, SYSTEM_VARIABLES),
			equalTo(expectedResponse));
	}
	
	def String policyExampleTwoRule3() {
		return '''
			policy "rule_3"
			/* A physician may write any medical element in a record 
			    for which he or she is the designated primary care 
			    physician, provided an email is sent to the patient */
			
			permit 
				subject.role == "physician" &
				string.starts_with(resource._selector, "@.medical") &
				action == "write"
			where
				subject.physician_id == resource._content.primaryCarePhysician.registrationID;
			obligation
				{
					"id" : "email",
					"mailto" : resource._content.patient.contact.email,
					"text" : "Your medical record has been accessed by:" + subject.id
				}
		''';
	}
	
	@Test
	def void exampleTwoRule3() throws PolicyEvaluationException {
		val expectedResponse = Response.notApplicable()

		assertThat("XACML example two rule 3 not working as expected",
			INTERPRETER.evaluate(request_example_two, policyExampleTwoRule3(), ATTRIBUTE_CTX, FUNCTION_CTX, SYSTEM_VARIABLES),
			equalTo(expectedResponse));
	}
	
	@Test
	def void exampleTwoRule3Permit() throws PolicyEvaluationException {
		val request = MAPPER.readValue('''
			{
				"subject": {
					"id": "CN=Julius Hibbert",
					"role": "physician",
					"physician_id": "jh1234"
				},
				"resource": {
					"_type": "urn:example:med:schemas:record",
					"_content": {
						"patient": {
							"dob": "1992-03-21",
							"patient_number": "555555",
							"contact": {
								"email": "b.simpsons@example.com"
							}
						},
						"primaryCarePhysician": {
							"registrationID": "jh1234"
						}
					},
					"_selector": "@.medical"
				},
				"action": "write",
				"environment": {
					"current_date": "2010-01-11"
				}
			}
		''', Request)
		
		val expectedObligation = MAPPER.readValue('''
			[
				{
					"id": "email",
					"mailto": "b.simpsons@example.com",
					"text": "Your medical record has been accessed by:CN=Julius Hibbert"
				}
			]
		''', ArrayNode)
		
		val expectedResponse = new Response(Decision.PERMIT, Optional.empty, Optional.of(expectedObligation), Optional.empty)
		
		assertThat("XACML example two rule 3 not working as expected",
			INTERPRETER.evaluate(request, policyExampleTwoRule3(), ATTRIBUTE_CTX, FUNCTION_CTX, SYSTEM_VARIABLES),
			equalTo(expectedResponse));
	}
	
	def String policyExampleTwoRule4() {
		return '''
			policy "rule_4"
			/* An Administrator shall not be permitted to read or write
			   medical elements of a patient record in the
			   http://www.med.example.com/records.xsd namespace. */
			
			deny
				subject.role == "administrator"
			where
				resource._type == "urn:example:med:schemas:record" &
				(action == "write" | action == "read") &
				(
					selection.match(resource._content, resource._selector, "@.medical") |
					selection.match(resource._content, "@.medical", resource._selector) 
				);
		''';
	}
	
	@Test
	def void exampleTwoRule4() throws PolicyEvaluationException {
		val request = request_example_two
		val expectedResponse = Response.notApplicable()

		assertThat("XACML example two rule 4 not working as expected",
			INTERPRETER.evaluate(request, policyExampleTwoRule4(), ATTRIBUTE_CTX, FUNCTION_CTX, SYSTEM_VARIABLES),
			equalTo(expectedResponse));
	}
	
	@Test
	def void exampleTwoRule4Deny() throws PolicyEvaluationException {
		val request = MAPPER.readValue('''
			{
				"subject": {
					"id": "admin",
					"role": "administrator",
					"admin_id": "admin_01"
				},
				"resource": {
					"_type": "urn:example:med:schemas:record",
					"_content": {
						"patient": {
							"dob": "1992-03-21",
							"patient_number": "555555",
							"contact": {
								"email": "b.simpsons@example.com"
							}
						},
						"medical": {
							"drug": "xyz"
						}
					},
					"_selector": "@..drug"
				},
				"action": "read",
				"environment": {
					"current_date": "2010-01-11"
				}
			}
		''', Request)
		
		val expectedResponse = Response.deny()
		
		assertThat("XACML example two rule 4 not working as expected",
			INTERPRETER.evaluate(request, policyExampleTwoRule4(), ATTRIBUTE_CTX, FUNCTION_CTX, SYSTEM_VARIABLES),
			equalTo(expectedResponse));
	}
}
