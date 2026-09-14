/*
 * Copyright (c) Microsoft Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.playwright;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestJSpecifyAnnotations {
  @Test
  void shouldMarkEvaluateReturnAndArgumentNullable() throws NoSuchMethodException {
    Method evaluateWithoutArgument = Page.class.getMethod("evaluate", String.class);
    assertNotNull(evaluateWithoutArgument.getAnnotatedReturnType().getAnnotation(Nullable.class));

    Method evaluateWithArgument = Page.class.getMethod("evaluate", String.class, Object.class);
    assertNotNull(evaluateWithArgument.getAnnotatedReturnType().getAnnotation(Nullable.class));
    assertNotNull(evaluateWithArgument.getAnnotatedParameterTypes()[1].getAnnotation(Nullable.class));
  }
}
