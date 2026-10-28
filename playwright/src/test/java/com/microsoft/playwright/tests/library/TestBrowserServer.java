/*
 * Copyright (c) Microsoft Corporation.
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

package com.microsoft.playwright.tests.library;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.options.BindResult;
import org.junit.jupiter.api.DisplayName;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/library/browser-server.spec.ts. */
public class TestBrowserServer {
  // Java: the beforeEach sets process.env.PWTEST_SERVER_REGISTRY to a directory of the test, which cannot be done for
  // the driver process of the worker; the descriptor goes to the default registry of the driver.

  @PlaywrightTest
  @DisplayName("BR-008 should start and stop pipe server")
  void shouldStartAndStopPipeServer(BrowserType browserType, Browser browser) {
    BindResult serverInfo = browser.bind("default");
    assertTrue(Pattern.compile("browser-").matcher(serverInfo.endpoint).find());

    Browser browser2 = browserType.connect(serverInfo.endpoint);
    Page page = browser2.newPage();
    page.navigate("data:text/html,<h1>Hello via pipe</h1>");
    assertEquals("Hello via pipe", page.locator("h1").textContent());
    page.close();
    browser2.close();
    browser.unbind();
  }

  @PlaywrightTest
  @DisplayName("BR-010 should start ws server with host/port and produce well-formed endpoint")
  void shouldStartWsServerWithHostPortAndProduceWellFormedEndpoint(BrowserType browserType, Browser browser) {
    BindResult serverInfo = browser.bind("default", new Browser.BindOptions().setHost("localhost").setPort(0));
    assertTrue(Pattern.compile("^ws://(127\\.0\\.0\\.1|\\[::1\\]):\\d+/[a-f0-9]+$").matcher(serverInfo.endpoint).find());

    Browser browser2 = browserType.connect(serverInfo.endpoint);
    Page page = browser2.newPage();
    page.navigate("data:text/html,<h1>Hello via ws</h1>");
    assertEquals("Hello via ws", page.locator("h1").textContent());
    page.close();
    browser2.close();
    browser.unbind();
  }
}
