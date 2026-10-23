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

import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.PlaywrightTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * This test simply makes sure that all 4 main interfaces implement AutoCloseable. If it compiles, then it passes.
 */
public class TestAutoClose {
  @PlaywrightTest
  void shouldAllowUsingTryWithResources(BrowserName browserName) {
    try (Playwright playwright = Playwright.create();
         Browser browser = browserName.browserType(playwright).launch(Env.launchOptions());
         BrowserContext context = browser.newContext();
         Page page = context.newPage()) {
      assertEquals(2021, page.evaluate("() => 2021"));
    }
  }
}
