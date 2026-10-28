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
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.HttpsTestServer;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.WaitUntilState;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/library/ignorehttpserrors.spec.ts. */
public class TestIgnorehttpserrors {
  @PlaywrightTest
  @DisplayName("NE-136 should work @smoke")
  void shouldWork(Browser browser, HttpsTestServer httpsServer) {
    PlaywrightException error = null;
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setIgnoreHTTPSErrors(true));
    Page page = context.newPage();
    Response response = null;
    try {
      response = page.navigate(httpsServer.EMPTY_PAGE);
    } catch (PlaywrightException e) {
      error = e;
    }
    assertNull(error);
    assertEquals(true, response.ok());
    context.close();
  }

  @PlaywrightTest
  @DisplayName("NE-137 should isolate contexts")
  void shouldIsolateContexts(Browser browser, HttpsTestServer httpsServer) {
    {
      PlaywrightException error = null;
      BrowserContext context = browser.newContext(new Browser.NewContextOptions().setIgnoreHTTPSErrors(true));
      Page page = context.newPage();
      Response response = null;
      try {
        response = page.navigate(httpsServer.EMPTY_PAGE);
      } catch (PlaywrightException e) {
        error = e;
      }
      assertNull(error);
      assertEquals(true, response.ok());
      context.close();
    }
    {
      PlaywrightException error = null;
      BrowserContext context = browser.newContext();
      Page page = context.newPage();
      try {
        page.navigate(httpsServer.EMPTY_PAGE);
      } catch (PlaywrightException e) {
        error = e;
      }
      assertNotNull(error);
      context.close();
    }
  }

  @PlaywrightTest
  @DisplayName("NE-138 should isolated contexts that share network process")
  void shouldIsolatedContextsThatShareNetworkProcess(Browser browser, HttpsTestServer httpsServer, BrowserName browserName) {
    // issue: https://github.com/microsoft/playwright/issues/35870
    assumeFalse(browserName == BrowserName.WEBKIT && Env.isLinux, "fixme: See https://bugs.webkit.org/show_bug.cgi?id=293148");
    {
      BrowserContext context = browser.newContext(new Browser.NewContextOptions().setIgnoreHTTPSErrors(true));
      Page page = context.newPage();
      Response response = page.navigate(httpsServer.EMPTY_PAGE);
      assertEquals(true, response.ok());
      // Closing the context will remove WebsiteDataStore and stop the network process
      // which will make the test pass.
      // await context.close();
    }
    {
      BrowserContext context = browser.newContext();
      Page page = context.newPage();
      PlaywrightException error = null;
      try {
        page.navigate(httpsServer.EMPTY_PAGE);
      } catch (PlaywrightException e) {
        error = e;
      }
      assertNotNull(error, "A TLS error expected, but the request succeeded.");
      context.close();
    }
  }

  @PlaywrightTest
  @DisplayName("NE-139 should work with mixed content")
  void shouldWorkWithMixedContent(Browser browser, TestServer server, HttpsTestServer httpsServer) {
    httpsServer.setRoute("/mixedcontent.html", (req, res) -> {
      res.end("<iframe src=" + server.EMPTY_PAGE + "></iframe>");
    });
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setIgnoreHTTPSErrors(true));
    Page page = context.newPage();
    page.navigate(httpsServer.PREFIX + "/mixedcontent.html", new Page.NavigateOptions().setWaitUntil(WaitUntilState.LOAD));
    assertEquals(2, page.frames().size());
    // Make sure blocked iframe has functional execution context
    // @see https://github.com/GoogleChrome/puppeteer/issues/2709
    assertEquals(3, page.frames().get(0).evaluate("1 + 2"));
    assertEquals(5, page.frames().get(1).evaluate("2 + 3"));
    context.close();
  }

  @PlaywrightTest
  @DisplayName("NE-140 should work with WebSocket")
  void shouldWorkWithWebSocket(Browser browser, HttpsTestServer httpsServer) {
    httpsServer.sendOnWebSocketConnection("incoming");
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setIgnoreHTTPSErrors(true));
    Page page = context.newPage();
    Object value = page.evaluate("endpoint => {\n" +
      "  let cb;\n" +
      "  const result = new Promise(f => cb = f);\n" +
      "  const ws = new WebSocket(endpoint);\n" +
      "  ws.addEventListener('message', data => { ws.close(); cb(data.data); });\n" +
      "  ws.addEventListener('error', error => cb('Error'));\n" +
      "  return result;\n" +
      "}", httpsServer.PREFIX.replaceFirst("https", "wss") + "/ws");
    assertEquals("incoming", value);
    context.close();
  }

  @PlaywrightTest
  @DisplayName("NE-141 should fail with WebSocket if not ignored")
  void shouldFailWithWebSocketIfNotIgnored(Browser browser, HttpsTestServer httpsServer) {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    Object value = page.evaluate("endpoint => {\n" +
      "  let cb;\n" +
      "  const result = new Promise(f => cb = f);\n" +
      "  const ws = new WebSocket(endpoint);\n" +
      "  ws.addEventListener('message', data => { ws.close(); cb(data.data); });\n" +
      "  ws.addEventListener('error', error => cb('Error'));\n" +
      "  return result;\n" +
      "}", httpsServer.PREFIX.replaceFirst("https", "wss") + "/ws");
    assertEquals("Error", value);
    context.close();
  }

  @PlaywrightTest
  @DisplayName("NE-142 serviceWorker should intercept document request")
  @DisabledOnBrowser(value = BrowserName.CHROMIUM, reason = "fixme: Failed to register a ServiceWorker: An SSL certificate error occurred when fetching the script.")
  void serviceWorkerShouldInterceptDocumentRequest(Browser browser, HttpsTestServer httpsServer, BrowserName browserName) {
    // issue: https://github.com/microsoft/playwright/issues/27768
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setIgnoreHTTPSErrors(true));
    Page page = context.newPage();
    context.route("**/*", route -> route.resume());
    httpsServer.setRoute("/sw.js", (req, res) -> {
      res.setHeader("Content-Type", "application/javascript");
      res.end("\n" +
        "      self.addEventListener('fetch', event => {\n" +
        "        event.respondWith(new Response('intercepted'));\n" +
        "      });\n" +
        "      self.addEventListener('activate', event => {\n" +
        "        event.waitUntil(clients.claim());\n" +
        "      });\n" +
        "    ");
    });
    page.navigate(httpsServer.EMPTY_PAGE);
    page.evaluate("async () => {\n" +
      "  const waitForControllerChange = new Promise(resolve => navigator.serviceWorker.oncontrollerchange = resolve);\n" +
      "  await navigator.serviceWorker.register('/sw.js');\n" +
      "  await waitForControllerChange;\n" +
      "}");
    page.reload();
    assertEquals("intercepted", page.textContent("body"));
    context.close();
  }
}
