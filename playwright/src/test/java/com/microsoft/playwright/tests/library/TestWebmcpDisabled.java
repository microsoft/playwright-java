/**
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

package com.microsoft.playwright.tests.library;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/library/webmcp-disabled.spec.ts. */
@DisabledOnBrowser(value = BrowserName.WEBKIT, reason = "WebKit does not implement WebMCP")
public class TestWebmcpDisabled {
  @BeforeEach
  void skipOnBidi() {
    assumeFalse(Env.isBidi(), "WebMCP is not implemented over BiDi");
  }

  @PlaywrightTest
  @DisplayName("WM-015 should throw when the browser was launched without WebMCP")
  void shouldThrowWhenTheBrowserWasLaunchedWithoutWebMCP(Page page, BrowserName browserName) {
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.webmcp().tools());
    assertTrue(error.getMessage().contains("WebMCP is not enabled."));
    assertTrue(error.getMessage().contains(browserName == BrowserName.FIREFOX ? "dom.modelcontext.enabled" : "--enable-features=WebMCP"));
  }
}
