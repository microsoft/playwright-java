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

import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.options.ServiceWorkerPolicy;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestBrowserContextServiceWorkerPolicy {
  @PlaywrightTest
  void shouldAllowServiceWorkersByDefault(Page page, Server server) {
    page.navigate(server.PREFIX + "/serviceworkers/empty/sw.html");
    assertNotNull(page.evaluate("() => window['registrationPromise']"));
  }

  @PlaywrightTest
  void blocksServiceWorkerRegistration(Browser browser, Server server) {
    try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setServiceWorkers(ServiceWorkerPolicy.BLOCK))) {
      Page page = context.newPage();
      ConsoleMessage message = page.waitForConsoleMessage(new Page.WaitForConsoleMessageOptions()
          .setPredicate(m -> "Service Worker registration blocked by Playwright".equals(m.text())),
        () -> page.navigate(server.PREFIX + "/serviceworkers/empty/sw.html"));
      assertNotNull(message);
    }
  }

}
