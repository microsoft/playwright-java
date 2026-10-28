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

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/elementhandle-select-text.spec.ts. */
public class TestElementhandleSelectText {
  @PlaywrightTest
  @DisplayName("EH-102 should select textarea")
  void shouldSelectTextarea(Page page, TestServer server, BrowserName browserName) {
    page.navigate(server.PREFIX + "/input/textarea.html");
    ElementHandle textarea = page.querySelector("textarea");
    textarea.evaluate("textarea => textarea.value = 'some value'");
    textarea.selectText();
    assertEquals("some value", page.evaluate("() => window.getSelection().toString()"));
  }

  @PlaywrightTest
  @DisplayName("EH-103 should select input")
  void shouldSelectInput(Page page, TestServer server, BrowserName browserName) {
    page.navigate(server.PREFIX + "/input/textarea.html");
    ElementHandle input = page.querySelector("input");
    input.evaluate("input => input.value = 'some value'");
    input.selectText();
    assertEquals("some value", page.evaluate("() => window.getSelection().toString()"));
  }

  @PlaywrightTest
  @DisplayName("EH-104 should select plain div")
  void shouldSelectPlainDiv(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/textarea.html");
    ElementHandle div = page.querySelector("div.plain");
    div.selectText();
    assertEquals("Plain div", page.evaluate("() => window.getSelection().toString()"));
  }

  @PlaywrightTest
  @DisplayName("EH-105 should timeout waiting for invisible element")
  void shouldTimeoutWaitingForInvisibleElement(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/textarea.html");
    ElementHandle textarea = page.querySelector("textarea");
    textarea.evaluate("e => e.style.display = 'none'");
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> textarea.selectText(new ElementHandle.SelectTextOptions().setTimeout(3000)));
    assertTrue(error.getMessage().contains("element is not visible"));
  }

}
