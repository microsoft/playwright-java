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

import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.PlaywrightTest;

import static com.microsoft.playwright.fixtures.BrowserName.WEBKIT;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestElementHandleType {

  @PlaywrightTest
  void shouldWork(Page page) {
    page.setContent("<input type='text' />");
    page.type("input", "hello");
    assertEquals("hello", page.evalOnSelector("input", "input => input.value"));
  }

  @PlaywrightTest
  void shouldNotSelectExistingValue(Page page) {
    page.setContent("<input type='text' value='hello' />");
    page.type("input", "world");
    assertEquals("worldhello", page.evalOnSelector("input", "input => input.value"));
  }

  @PlaywrightTest
  void shouldResetSelectionWhenNotFocus(Page page) {
    page.setContent("<input type='text' value='hello' /><div tabIndex=2>text</div>");
    page.evalOnSelector("input", "input => {\n" +
      "    input.selectionStart = 2;\n" +
      "    input.selectionEnd = 4;\n" +
      "    document.querySelector('div').focus();\n" +
      "  }");
    page.type("input", "world");
    assertEquals("worldhello", page.evalOnSelector("input", "input => input.value"));
  }

  @PlaywrightTest
  void shouldNotModifySelectionWhenFocus(Page page) {
    page.setContent("<input type='text' value='hello' />");
    page.evalOnSelector("input", "input => {\n" +
      "    input.focus();\n" +
      "    input.selectionStart = 2;\n" +
      "    input.selectionEnd = 4;\n" +
      "  }");
    page.type("input", "world");
    assertEquals("heworldo", page.evalOnSelector("input", "input => input.value"));
  }

  @PlaywrightTest
  void shouldWorkWithNumberInput(Page page, BrowserName browserName) {
    page.setContent("<input type='number' value=2 />");
    page.type("input", "13");
    assertEquals(browserName == WEBKIT ? "13" : "132", page.evalOnSelector("input", "input => input.value"));
  }
}
