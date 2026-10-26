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
import com.microsoft.playwright.Response;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.WaitUntilState;
import org.junit.jupiter.api.DisplayName;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/frame-goto.spec.ts. */
public class TestFrameGoto {
  @PlaywrightTest
  @DisplayName("FR-020 should navigate subframes @smoke")
  void shouldNavigateSubframes(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/frames/one-frame.html");
    assertTrue(page.frames().get(0).url().contains("/frames/one-frame.html"));
    assertTrue(page.frames().get(1).url().contains("/frames/frame.html"));

    Response response = page.frames().get(1).navigate(server.EMPTY_PAGE);
    assertEquals(true, response.ok());
    assertEquals(page.frames().get(1), response.frame());
  }

  @PlaywrightTest
  @DisplayName("FR-022 should continue after client redirect")
  @DisabledOnBrowser(value = BrowserName.FIREFOX, reason = "fixme: script.js is requested before navigationCommitted arrives")
  @Fixme("java: the error message has no 'page.goto:' prefix and no call log")
  void shouldContinueAfterClientRedirect(Page page, TestServer server) {
    server.setRoute("/frames/script.js", (req, res) -> {});
    String url = server.PREFIX + "/frames/child-redirect.html";
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.navigate(url, new Page.NavigateOptions().setTimeout(5000).setWaitUntil(WaitUntilState.NETWORKIDLE)));
    assertTrue(error.getMessage().contains("page.goto: Timeout 5000ms exceeded."));
    assertTrue(error.getMessage().contains("navigating to \"" + url + "\", waiting until \"networkidle\""));
  }
}
