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

package com.microsoft.playwright.tests.library;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/library/browsercontext-device.spec.ts. */
public class TestBrowsercontextDevice {
  @Nested
  class Device {
    @BeforeEach
    void skipFirefox(BrowserName browserName) {
      assumeFalse(browserName == BrowserName.FIREFOX && !Env.isBidi());
    }

    @PlaywrightTest
    @DisplayName("DV-003 should scroll to click")
    void shouldScrollToClick(Browser browser, TestServer server) {
      BrowserContext context = browser.newContext(new Browser.NewContextOptions()
        .setViewportSize(400, 400)
        .setDeviceScaleFactor(1)
        .setIsMobile(true));
      Page page = context.newPage();
      page.navigate(server.PREFIX + "/input/scrollable.html");
      ElementHandle element = page.querySelector("#button-91");
      element.click();
      assertEquals("clicked", element.textContent());
      context.close();
    }
  }
}
