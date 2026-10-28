/**
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

import com.microsoft.playwright.Page;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Port of tests/page/elementhandle-press.spec.ts. */
public class TestElementhandlePress {
  @PlaywrightTest
  @DisplayName("EH-062 should work")
  void shouldWork(Page page) {
    page.setContent("<input type='text' />");
    page.press("input", "h");
    assertEquals("h", page.evalOnSelector("input", "input => input.value"));
  }

  @PlaywrightTest
  @DisplayName("EH-063 should not select existing value")
  void shouldNotSelectExistingValue(Page page) {
    page.setContent("<input type='text' value='hello' />");
    page.press("input", "w");
    assertEquals("whello", page.evalOnSelector("input", "input => input.value"));
  }

  @PlaywrightTest
  @DisplayName("EH-064 should reset selection when not focused")
  void shouldResetSelectionWhenNotFocused(Page page) {
    page.setContent("<input type='text' value='hello' /><div tabIndex=2>text</div>");
    page.evalOnSelector("input", "input => {\n" +
      "    input.selectionStart = 2;\n" +
      "    input.selectionEnd = 4;\n" +
      "    document.querySelector('div').focus();\n" +
      "  }");
    page.press("input", "w");
    assertEquals("whello", page.evalOnSelector("input", "input => input.value"));
  }

  @PlaywrightTest
  @DisplayName("EH-065 should not modify selection when focused")
  void shouldNotModifySelectionWhenFocused(Page page) {
    page.setContent("<input type='text' value='hello' />");
    page.evalOnSelector("input", "input => {\n" +
      "    input.focus();\n" +
      "    input.selectionStart = 2;\n" +
      "    input.selectionEnd = 4;\n" +
      "  }");
    page.press("input", "w");
    assertEquals("hewo", page.evalOnSelector("input", "input => input.value"));
  }

  @PlaywrightTest
  @DisplayName("EH-066 should work with number input")
  @DisabledOnBrowser(value = BrowserName.WEBKIT, reason = "fail: Started failing after https://github.com/WebKit/WebKit/commit/c92a2aea185d63b5e9998608a9c0321a461c496c")
  void shouldWorkWithNumberInput(Page page) {
    page.setContent("<input type='number' value=2 />");
    page.press("input", "1");
    assertEquals("12", page.evalOnSelector("input", "input => input.value"));
  }
}
