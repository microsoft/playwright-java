/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
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
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.fixtures.ContextFactory;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/library/browsercontext-dsf.spec.ts. */
public class TestBrowsercontextDsf {
  @PlaywrightTest
  @DisplayName("DV-009 should fetch lodpi assets @smoke")
  void shouldFetchLodpiAssets(ContextFactory contextFactory, TestServer server) {
    BrowserContext context = contextFactory.newContext(new Browser.NewContextOptions()
      .setDeviceScaleFactor(1));
    Page page = context.newPage();
    Request request = page.waitForRequest("**/image*", () -> page.navigate(server.PREFIX + "/highdpi.html"));
    assertTrue(request.url().contains("image1x"));
  }

  @PlaywrightTest
  @DisplayName("DV-010 should fetch hidpi assets")
  void shouldFetchHidpiAssets(ContextFactory contextFactory, TestServer server) {
    BrowserContext context = contextFactory.newContext(new Browser.NewContextOptions()
      .setDeviceScaleFactor(2));
    Page page = context.newPage();
    Request request = page.waitForRequest("**/image*", () -> page.navigate(server.PREFIX + "/highdpi.html"));
    assertTrue(request.url().contains("image2x"));
  }
}
