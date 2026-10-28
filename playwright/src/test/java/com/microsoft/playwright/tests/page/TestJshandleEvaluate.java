/*
 * Copyright 2018 Google Inc. All rights reserved.
 * Modifications copyright (c) Microsoft Corporation.
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

package com.microsoft.playwright.tests.page;

import com.microsoft.playwright.JSHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Port of tests/page/jshandle-evaluate.spec.ts. */
public class TestJshandleEvaluate {
  @PlaywrightTest
  @DisplayName("JH-005 should work with function @smoke")
  void shouldWorkWithFunction(Page page) {
    JSHandle windowHandle = page.evaluateHandle("() => {\n" +
      "    window['foo'] = [1, 2];\n" +
      "    return window;\n" +
      "  }");
    assertEquals(Arrays.asList(1, 2), windowHandle.evaluate("w => w['foo']"));
  }

  @PlaywrightTest
  @DisplayName("JH-006 should work with expression")
  void shouldWorkWithExpression(Page page) {
    JSHandle windowHandle = page.evaluateHandle("() => {\n" +
      "    window['foo'] = [1, 2];\n" +
      "    return window;\n" +
      "  }");
    assertEquals(Arrays.asList(1, 2), windowHandle.evaluate("window.foo"));
  }
}
