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

package com.microsoft.playwright.tests.page;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.ConsoleMessage;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.EnabledOnBrowser;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import java.util.HashMap;
import java.util.Map;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.microsoft.playwright.fixtures.Utils.browserMajorVersion;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/page/interception.spec.ts. */
public class TestInterception {
  private static boolean truthy(String value) {
    return value != null && !value.isEmpty();
  }

  @PlaywrightTest
  @DisplayName("RO-151 should work with navigation @smoke")
  void shouldWorkWithNavigation(Page page, TestServer server) {
    Map<String, Request> requests = new HashMap<>();
    page.route("**/*", route -> {
      String url = route.request().url();
      requests.put(url.substring(url.lastIndexOf('/') + 1), route.request());
      route.resume();
    });
    server.setRedirect("/rrredirect", "/frames/one-frame.html");
    page.navigate(server.PREFIX + "/rrredirect");
    assertEquals(true, requests.get("rrredirect").isNavigationRequest());
    assertEquals(true, requests.get("frame.html").isNavigationRequest());
    assertEquals(false, requests.get("script.js").isNavigationRequest());
    assertEquals(false, requests.get("style.css").isNavigationRequest());
  }

  @PlaywrightTest
  @DisplayName("RO-152 should intercept after a service worker")
  void shouldInterceptAfterAServiceWorker(Page page, TestServer server, BrowserName browserName) {
    page.navigate(server.PREFIX + "/serviceworkers/fetchdummy/sw.html");
    page.evaluate("() => window['activationPromise']");

    // Sanity check.
    Object swResponse = page.evaluate("() => window['fetchDummy']('foo')");
    assertEquals("responseFromServiceWorker:foo", swResponse);

    page.route("**/foo", route -> {
      int slash = route.request().url().lastIndexOf('/');
      String name = route.request().url().substring(slash + 1);
      route.fulfill(new Route.FulfillOptions()
        .setStatus(200)
        .setContentType("text/css")
        .setBody("responseFromInterception:" + name));
    });

    // Page route is applied after service worker fetch event.
    Object swResponse2 = page.evaluate("() => window['fetchDummy']('foo')");
    assertEquals("responseFromServiceWorker:foo", swResponse2);

    // Page route is not applied to service worker initiated fetch.
    Object nonInterceptedResponse = page.evaluate("() => window['fetchDummy']('passthrough')");
    assertEquals("FAILURE: Not Found", nonInterceptedResponse);

    // Firefox/Juggler does not want to fetch the redirect for some reason.
    if (browserName != BrowserName.FIREFOX || Env.isBidi()) {
      // Page route is not applied to service worker initiated fetch with redirect.
      server.setRedirect("/serviceworkers/fetchdummy/passthrough", "/simple.json");
      Object redirectedResponse = page.evaluate("() => window['fetchDummy']('passthrough')");
      assertEquals("{\"foo\": \"bar\"}\n", redirectedResponse);
    }
  }

  @PlaywrightTest
  @DisplayName("RO-155 should throw on page.route with invalid glob")
  void shouldThrowOnPageRouteWithInvalidGlob(Page page) {
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.route("http://*/foo{", route -> route.resume()));
    assertTrue(error.getMessage().contains("unmatched '{'"));
  }

  @PlaywrightTest
  @DisplayName("RO-156 should intercept by glob")
  void shouldInterceptByGlob(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.route("http://localhos**?*oo", route -> {
      route.fulfill(new Route.FulfillOptions()
        .setStatus(200)
        .setBody("intercepted"));
    });
    Object result = page.evaluate("url => fetch(url).then(r => r.text())", server.PREFIX + "/?foo");
    assertEquals("intercepted", result);
  }

  @PlaywrightTest
  @DisplayName("RO-157 should intercept network activity from worker")
  void shouldInterceptNetworkActivityFromWorker(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    server.setRoute("/data_for_worker", (req, res) -> res.end("failed to intercept"));
    String url = server.PREFIX + "/data_for_worker";
    page.route(url, route -> {
      try {
        route.fulfill(new Route.FulfillOptions()
          .setStatus(200)
          .setBody("intercepted"));
      } catch (Exception e) {
      }
    });
    ConsoleMessage msg = page.waitForConsoleMessage(() -> {
      page.evaluate("url => new Worker(URL.createObjectURL(new Blob([`\n" +
        "      fetch(\"${url}\").then(response => response.text()).then(console.log);\n" +
        "    `], { type: 'application/javascript' })))", url);
    });
    assertEquals("intercepted", msg.text());
  }

  @PlaywrightTest
  @DisplayName("RO-158 should intercept worker requests when enabled after worker creation")
  void shouldInterceptWorkerRequestsWhenEnabledAfterWorkerCreation(Page page, TestServer server, BrowserName browserName, Browser browser) {
    assumeFalse(browserName == BrowserName.CHROMIUM && browserMajorVersion(browser) < 130, "fixed in Chromium 130");

    page.navigate(server.EMPTY_PAGE);
    server.setRoute("/data_for_worker", (req, res) -> res.end("failed to intercept"));
    String url = server.PREFIX + "/data_for_worker";
    page.waitForWorker(() -> {
      page.evaluate("url => {\n" +
        "  window.w = new Worker(URL.createObjectURL(new Blob([`\n" +
        "        onmessage = function(e) {\n" +
        "          fetch(\"${url}\").then(response => response.text()).then(console.log);\n" +
        "        };\n" +
        "      `], { type: 'application/javascript' })));\n" +
        "}", url);
    });
    // Install the route **after** the worker has been created.
    page.route(url, route -> {
      try {
        route.fulfill(new Route.FulfillOptions()
          .setStatus(200)
          .setBody("intercepted"));
      } catch (Exception e) {
      }
    });
    ConsoleMessage msg = page.waitForConsoleMessage(() -> {
      page.evaluate("() => window.w.postMessage('')");
    });
    assertEquals("intercepted", msg.text());
  }

  @PlaywrightTest
  @DisplayName("RO-159 should intercept network activity from worker 2")
  void shouldInterceptNetworkActivityFromWorker2(Page page, TestServer server) {
    String url = server.PREFIX + "/worker/worker.js";
    page.route(url, route -> {
      try {
        route.fulfill(new Route.FulfillOptions()
          .setStatus(200)
          .setBody("console.log(\"intercepted\");")
          .setContentType("application/javascript"));
      } catch (Exception e) {
      }
    });
    ConsoleMessage msg = page.waitForConsoleMessage(() -> {
      page.navigate(server.PREFIX + "/worker/worker.html");
    });
    assertEquals("intercepted", msg.text());
  }

  @PlaywrightTest
  @DisplayName("RO-162 should not break remote worker importScripts")
  void shouldNotBreakRemoteWorkerImportScripts(Page page, TestServer server) {
    page.route("**", route -> {
      route.resume();
    });
    page.navigate(server.PREFIX + "/worker/worker-http-import.html");
    page.waitForSelector("#status:has-text('finished')");
  }

  @PlaywrightTest
  @DisplayName("RO-163 should disable memory cache when intercepting")
  void shouldDisableMemoryCacheWhenIntercepting(Page page, TestServer server) {
    int[] interceted = {0};
    page.route("**/page.html", route -> {
      ++interceted[0];
      route.fulfill(new Route.FulfillOptions().setBody("success"));
    });
    page.navigate(server.PREFIX + "/page.html");
    assertTrue(page.locator("body").textContent().contains("success"));
    page.navigate(server.EMPTY_PAGE);
    assertThat(page).hasURL(server.EMPTY_PAGE);
    assertEquals(1, interceted[0]);
    page.goBack();
    assertThat(page).hasURL(server.PREFIX + "/page.html");
    assertEquals(2, interceted[0]);
  }

  @PlaywrightTest
  @DisplayName("RO-164 should intercept blob url requests")
  @EnabledOnBrowser(BrowserName.WEBKIT)
  void shouldInterceptBlobUrlRequests(Page page, TestServer server, BrowserName browserName) {
    page.navigate(server.EMPTY_PAGE);
    page.route("**/*", route -> {
      try {
        route.fulfill(new Route.FulfillOptions()
          .setStatus(200)
          .setBody("intercepted"));
      } catch (Exception e) {
      }
    });
    page.onConsoleMessage(msg -> {
      // console.log(msg.text())
    });
    Object response = page.evaluate("async () => {\n" +
      "  const blobUrl = URL.createObjectURL(new Blob(['failed to intercept'], { type: 'text/plain' }));\n" +
      "  return await fetch(blobUrl).then(response => response.text());\n" +
      "}");
    assertEquals("intercepted", response);
  }
}
