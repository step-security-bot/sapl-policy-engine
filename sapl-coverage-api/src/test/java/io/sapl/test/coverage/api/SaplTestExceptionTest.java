/*
 * Streaming Attribute Policy Language (SAPL) Engine
 * Copyright (C) 2017-2023 Dominic Heutelbeck (dominic@heutelbeck.com)
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
package io.sapl.test.coverage.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.Test;

class SaplTestExceptionTest {

    @Test
    void defaultConstructor() {
        var exception = new SaplTestException();
        assertThat(exception.getMessage()).isNull();
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void messageConstructor() {
        var exception = new SaplTestException("Test");
        assertThat(exception.getMessage()).isEqualTo("Test");
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void messageAndCauseConstructor() {
        var exception = new SaplTestException("Test", new IOException());
        assertThat(exception.getMessage()).isEqualTo("Test");
        assertThat(exception.getCause()).isInstanceOfAny(IOException.class);
    }

}
