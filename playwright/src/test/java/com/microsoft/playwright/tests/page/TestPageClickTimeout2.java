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
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/page-click-timeout-2.spec.ts. */
public class TestPageClickTimeout2 {
  @PlaywrightTest
  @DisplayName("CL-114 should timeout waiting for display:none to be gone")
  @Fixme("java: the error message has no 'page.click: ' prefix (it starts with 'Error {' and the serialized error)")
  void shouldTimeoutWaitingForDisplayNoneToBeGone(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/button.html");
    page.evalOnSelector("button", "b => b.style.display = 'none'");
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.click("button", new Page.ClickOptions().setTimeout(5000)));
    assertTrue(error.getMessage().contains("page.click: Timeout 5000ms exceeded."));
    assertTrue(error.getMessage().contains("waiting for element to be visible, enabled and stable"));
    assertTrue(error.getMessage().contains("element is not visible"));
    assertTrue(error.getMessage().contains("retrying click action"));
  }

  @PlaywrightTest
  @DisplayName("CL-115 should timeout waiting for visibility:hidden to be gone")
  @Fixme("java: the error message has no 'page.click: ' prefix (it starts with 'Error {' and the serialized error)")
  void shouldTimeoutWaitingForVisibilityHiddenToBeGone(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/button.html");
    page.evalOnSelector("button", "b => b.style.visibility = 'hidden'");
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.click("button", new Page.ClickOptions().setTimeout(5000)));
    assertTrue(error.getMessage().contains("page.click: Timeout 5000ms exceeded."));
    assertTrue(error.getMessage().contains("waiting for element to be visible, enabled and stable"));
    assertTrue(error.getMessage().contains("element is not visible"));
    assertTrue(error.getMessage().contains("retrying click action"));
  }
}
