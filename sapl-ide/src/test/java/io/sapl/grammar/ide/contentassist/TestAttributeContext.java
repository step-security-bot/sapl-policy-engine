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
package io.sapl.grammar.ide.contentassist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;

import io.sapl.api.interpreter.Val;
import io.sapl.grammar.sapl.Arguments;
import io.sapl.interpreter.pip.AttributeContext;
import io.sapl.interpreter.pip.PolicyInformationPointDocumentation;
import reactor.core.publisher.Flux;

class TestAttributeContext implements AttributeContext {

    private final Map<String, Set<String>> availableLibraries;

    static final String TEMP_NOW_SCHEMA = """
            {
              "type": "object",
              "properties": {
            	"value": { "type": "number" },
            	"unit": { "type": "string"}
              }
            }
            """;

    static final String TEMP_MEAN_SCHEMA = """
            {
              "type": "object",
              "properties": {
            	"value": { "type": "number" },
            	"period": { "type": "number"}
              }
            }
            """;

    public TestAttributeContext() {
        availableLibraries = new HashMap<>();
        availableLibraries.put("clock", Set.of("now", "millis", "ticker"));
        availableLibraries.put("temperature", Set.of("now", "mean", "predicted"));

    }

    public Boolean isProvidedFunction(String function) {
        List<String> availableFunctions = new ArrayList<>();
        for (var lib : availableLibraries.entrySet()) {
            var key = lib.getKey();
            for (var value : lib.getValue()) {
                availableFunctions.add(key.concat(".").concat(value));
            }
        }
        return availableFunctions.contains(function);
    }

    @Override
    public Collection<String> providedFunctionsOfLibrary(String pipName) {
        return availableLibraries.getOrDefault(pipName, new HashSet<>());
    }

    @Override
    public Collection<String> getAvailableLibraries() {
        return availableLibraries.keySet();
    }

    @Override
    public Collection<PolicyInformationPointDocumentation> getDocumentation() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getAttributeCodeTemplates() {
        return List.of("clock.now", "clock.millis", "clock.ticker", "temperature.now()>", "temperature.mean(a1, a2)>",
                "temperature.predicted(a2)>");
    }

    @Override
    public List<String> getEnvironmentAttributeCodeTemplates() {
        return List.of("clock.now", "clock.millis", "clock.ticker", "temperature.now()>", "temperature.mean(a1, a2)>",
                "temperature.predicted(a2)>");
    }

    @Override
    public Collection<String> getAllFullyQualifiedFunctions() {
        return List.of("clock.now", "clock.millis", "clock.ticker", "temperature.now()>", "temperature.mean(a1, a2)>",
                "temperature.predicted(a2)>");
    }

    @Override
    public Flux<Val> evaluateAttribute(String attribute, Val value, Arguments arguments,
            Map<String, JsonNode> variables) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Flux<Val> evaluateEnvironmentAttribute(String attribute, Arguments arguments,
            Map<String, JsonNode> variables) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String> getDocumentedAttributeCodeTemplates() {
        return Map.of("clock.now", "documentation");
    }

    @Override
    public Map<String, String> getAttributeSchemas() {
        var schemas = new HashMap<String, String>();
        schemas.put("temperature.now", TEMP_NOW_SCHEMA);
        schemas.put("temperature.mean", TEMP_MEAN_SCHEMA);
        return schemas;
    }

}
