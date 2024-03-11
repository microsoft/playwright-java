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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestElementHandlePress extends TestBase {

  @Test
  void shouldWork() {
    page.setContent("<input type='text' />");
    page.press("input", "h");
    assertEquals("h", page.evalOnSelector("input", "input => input.value"));
  }

  @Test
  void shouldNotSelectExistingValue() {
    page.setContent("<input type='text' value='hello' />");
    page.press("input", "w");
    assertEquals("whello", page.evalOnSelector("input", "input => input.value"));
  }

  @Test
  void shouldResetSelectionWhenNotFocused() {
    page.setContent("<input type='text' value='hello' /><div tabIndex=2>text</div>");
    page.evalOnSelector("input", "input => {\n" +
      "    input.selectionStart = 2;\n" +
      "    input.selectionEnd = 4;\n" +
      "    document.querySelector('div').focus();\n" +
      "  }");
    page.press("input", "w");
    assertEquals("whello", page.evalOnSelector("input", "input => input.value"));
  }

  @Test
  void shouldNotModifySelectionWhenFocused() {
    page.setContent("<input type='text' value='hello' />");
    page.evalOnSelector("input", "input => {\n" +
      "    input.focus();\n" +
      "    input.selectionStart = 2;\n" +
      "    input.selectionEnd = 4;\n" +
      "  }");
    page.press("input", "w");
    assertEquals("hewo", page.evalOnSelector("input", "input => input.value"));
  }

  @Test
  void shouldWorkWithNumberInput() {
    page.setContent("<input type='number' value=2 />");
    page.press("input", "1");
    assertEquals(isWebKit() ? "1" : "12", page.evalOnSelector("input", "input => input.value"));
  }
}
