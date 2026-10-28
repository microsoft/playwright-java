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

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.Fixture;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.ServiceWorkerPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/library/browsercontext-service-worker-policy.spec.ts. */
public class TestBrowsercontextServiceWorkerPolicy {
  @PlaywrightTest
  @DisplayName("WO-027 should allow service workers by default")
  void shouldAllowServiceWorkersByDefault(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/serviceworkers/empty/sw.html");
    // Java: the registration serializes to an empty object, which is truthy in JavaScript only.
    assertNotNull(page.evaluate("() => window['registrationPromise']"));
  }

  public static Stream<Arguments> modes() {
    return Stream.of(
      Arguments.of("literal", "new Response('<h1>offline</h1>', { headers: { 'Content-Type': 'text/html' } })"),
      Arguments.of("cache-only", "caches.match('/offline.html')"),
      Arguments.of("network-fallback", "fetch(event.request).catch(() => caches.match('/offline.html'))"),
      Arguments.of("preload-fallback", "(async () => {\n" +
        "    try {\n" +
        "      return await event.preloadResponse || await fetch(event.request);\n" +
        "    } catch {\n" +
        "      return caches.match('/offline.html');\n" +
        "    }\n" +
        "  })()"));
  }

  @Nested
  @ParameterizedClass
  @MethodSource("com.microsoft.playwright.tests.library.TestBrowsercontextServiceWorkerPolicy#modes")
  class ShouldAllowServiceWorkerNavigationWhileOffline {
    @Parameter(0)
    String mode;
    @Parameter(1)
    String workerResponse;

    @PlaywrightTest
    @DisplayName("WO-028 should allow ${mode} service worker navigation while offline")
    void shouldAllowModeServiceWorkerNavigationWhileOffline(BrowserContext context, Page page, TestServer server, BrowserName browserName) {
      // { annotation: { type: 'issue', description: 'https://github.com/microsoft/playwright/issues/42775' } }
      assumeFalse(browserName == BrowserName.FIREFOX && (mode.equals("network-fallback") || mode.equals("preload-fallback")), "fixme: Firefox does not apply offline emulation to service worker requests.");

      AtomicInteger networkRequests = new AtomicInteger();
      server.setRoute("/target.html", (request, response) -> {
        networkRequests.incrementAndGet();
        response.setHeader("Cache-Control", "no-store");
        response.end("online");
      });
      server.setRoute("/offline.html", (request, response) -> {
        response.setHeader("Content-Type", "text/html");
        response.end("<h1>offline</h1>");
      });
      server.setRoute("/sw.js", (request, response) -> {
        response.setHeader("Content-Type", "application/javascript");
        response.end("\n" +
          "        self.addEventListener('install', event => event.waitUntil((async () => {\n" +
          "          const cache = await caches.open('offline');\n" +
          "          await cache.add('/offline.html');\n" +
          "          await self.skipWaiting();\n" +
          "        })()));\n" +
          "        self.addEventListener('activate', event => event.waitUntil((async () => {\n" +
          "          " + (mode.equals("preload-fallback") ? "await self.registration.navigationPreload.enable();" : "") + "\n" +
          "          await self.clients.claim();\n" +
          "        })()));\n" +
          "        self.addEventListener('fetch', event => {\n" +
          "          if (event.request.mode === 'navigate')\n" +
          "            event.respondWith(" + workerResponse + ");\n" +
          "        });\n" +
          "      ");
      });

      page.navigate(server.EMPTY_PAGE);
      page.evaluate("async () => {\n" +
        "      await navigator.serviceWorker.register('/sw.js');\n" +
        "      await navigator.serviceWorker.ready;\n" +
        "    }");
      page.waitForFunction("() => !!navigator.serviceWorker.controller");
      context.setOffline(true);

      Response response = page.navigate(server.PREFIX + "/target.html");
      assertEquals(200, response.status());
      assertEquals(true, response.fromServiceWorker());
      assertThat(page.locator("h1")).hasText("offline");
      assertEquals(false, page.evaluate("() => navigator.onLine"));
      assertEquals(0, networkRequests.get());

      Page newPage = context.newPage();
      Response newResponse = newPage.navigate(server.PREFIX + "/target.html");
      assertEquals(200, newResponse.status());
      assertEquals(true, newResponse.fromServiceWorker());
      assertThat(newPage.locator("h1")).hasText("offline");
      assertEquals(false, newPage.evaluate("() => navigator.onLine"));
      assertEquals(0, networkRequests.get());

      context.setOffline(false);
      assertEquals("online", page.evaluate("() => fetch('/target.html').then(response => response.text())"));
      assertEquals(1, networkRequests.get());
    }
  }

  @Nested
  class Block {
    @Fixture
    Browser.NewContextOptions contextOptions() {
      return new Browser.NewContextOptions().setServiceWorkers(ServiceWorkerPolicy.BLOCK);
    }

    @PlaywrightTest
    @DisplayName("WO-029 blocks service worker registration")
    void blocksServiceWorkerRegistration(Page page, TestServer server) {
      page.waitForConsoleMessage(
        new Page.WaitForConsoleMessageOptions().setPredicate(evt -> evt.text().equals("Service Worker registration blocked by Playwright")),
        () -> page.navigate(server.PREFIX + "/serviceworkers/empty/sw.html"));
    }

    @PlaywrightTest
    @DisplayName("WO-030 should not throw error on about:blank")
    void shouldNotThrowErrorOnAboutBlank(Page page) {
      // { type: 'issue', description: 'https://github.com/microsoft/playwright/issues/32292' }
      List<String> errors = new CopyOnWriteArrayList<>();
      page.onPageError(error -> errors.add(error));
      page.navigate("about:blank");
      assertEquals(new ArrayList<String>(), errors);
    }

    @PlaywrightTest
    @DisplayName("WO-031 should not throw error in opaque sandboxed iframe")
    void shouldNotThrowErrorInOpaqueSandboxedIframe(Page page, TestServer server) {
      // { type: 'issue', description: 'https://github.com/microsoft/playwright/issues/43113' }
      List<String> errors = new CopyOnWriteArrayList<>();
      page.onPageError(error -> errors.add(error));
      page.navigate(server.EMPTY_PAGE);
      page.setContent("<iframe sandbox=\"allow-scripts\" srcdoc=\"<p>Preview</p>\"></iframe>");
      assertThat(page.frameLocator("iframe").locator("p")).hasText("Preview");
      assertEquals(new ArrayList<String>(), errors);
    }
  }
}
