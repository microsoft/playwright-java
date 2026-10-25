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

import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/page-click-timeout-1.spec.ts. */
public class TestPageClickTimeout1 {
  @PlaywrightTest
  @DisplayName("CL-111 should timeout waiting for button to be enabled")
  @Fixme("java: the error message has no 'page.click: ' prefix (it starts with 'Error {' and the serialized error)")
  void shouldTimeoutWaitingForButtonToBeEnabled(Page page) {
    page.setContent("<button onclick=\"javascript:window.__CLICKED=true;\" disabled><span>Click target</span></button>");
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.click("text=Click target", new Page.ClickOptions().setTimeout(3000)));
    assertNull(page.evaluate("window.__CLICKED"));
    assertTrue(error.getMessage().contains("page.click: Timeout 3000ms exceeded."));
    assertTrue(error.getMessage().contains("element is not enabled"));
    assertTrue(error.getMessage().contains("retrying click action"));
  }

  @PlaywrightTest
  @DisplayName("CL-112 should timeout waiting for inert element to become non-inert")
  @Fixme("java: the error message has no 'page.click: ' prefix and the bundled driver 1.64.1 does not report 'element is inert' in the call log")
  void shouldTimeoutWaitingForInertElementToBecomeNonInert(Page page) {
    page.setContent("<div inert><button onclick=\"javascript:window.__CLICKED=true;\">Click target</button></div>");
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.click("text=Click target", new Page.ClickOptions().setTimeout(3000)));
    assertNull(page.evaluate("window.__CLICKED"));
    assertTrue(error.getMessage().contains("page.click: Timeout 3000ms exceeded."));
    assertTrue(error.getMessage().contains("element is inert"));
    assertTrue(error.getMessage().contains("retrying click action"));
  }

  @PlaywrightTest
  @DisplayName("CL-113 should timeout waiting for element outside of a modal dialog")
  @Fixme("java: the error message has no 'page.click: ' prefix and the bundled driver 1.64.1 does not report 'element is inert' in the call log")
  void shouldTimeoutWaitingForElementOutsideOfAModalDialog(Page page) {
    page.setContent("\n" +
      "    <button onclick=\"javascript:window.__CLICKED=true;\">Click target</button>\n" +
      "    <dialog id=dialog><button>Inside</button></dialog>\n" +
      "    <script>\n" +
      "      document.getElementById('dialog').showModal();\n" +
      "    </script>\n" +
      "  ");
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.click("text=Click target", new Page.ClickOptions().setTimeout(3000)));
    assertNull(page.evaluate("window.__CLICKED"));
    assertTrue(error.getMessage().contains("page.click: Timeout 3000ms exceeded."));
    assertTrue(error.getMessage().contains("element is inert"));
  }
}
