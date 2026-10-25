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

package com.microsoft.playwright.tests.page;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Port of tests/page/page-cache-storage.spec.ts. */
public class TestPageCacheStorage {
  @PlaywrightTest
  @DisplayName("ST-032 CacheStorage entry should survive page.reload()")
  @DisabledOnBrowser(value = BrowserName.WEBKIT, reason = "fail: Ephemeral CacheStorage is not persisted across reload in WebKit, consistent with Safari")
  void cacheStorageEntryShouldSurvivePageReload(Page page, TestServer server, BrowserName browserName) {
    // { annotation: { type: 'issue', description: 'https://github.com/microsoft/playwright/issues/41618' } }
    page.navigate(server.EMPTY_PAGE);
    page.evaluate("async () => {\n" +
      "    const cache = await caches.open('repro-cache');\n" +
      "    await cache.put('/meta', new Response('payload'));\n" +
      "  }");

    page.reload();

    Object after = page.evaluate("async () => {\n" +
      "    const cache = await caches.open('repro-cache');\n" +
      "    const resp = await cache.match('/meta');\n" +
      "    return resp ? await resp.text() : null;\n" +
      "  }");
    assertEquals("payload", after);
  }
}
