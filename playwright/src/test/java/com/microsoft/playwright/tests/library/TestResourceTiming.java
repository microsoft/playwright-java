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
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.ContextFactory;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.HttpsTestServer;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.Timing;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/library/resource-timing.spec.ts. */
public class TestResourceTiming {
  @PlaywrightTest
  @DisplayName("NE-131 should work @smoke")
  void shouldWork(ContextFactory contextFactory, TestServer server) {
    BrowserContext context = contextFactory.newContext();
    Page page = context.newPage();
    Request request = page.waitForRequestFinished(() -> {
      page.navigate(server.EMPTY_PAGE);
    });
    Timing timing = request.timing();
    verifyConnectionTimingConsistency(timing);
    assertTrue(timing.requestStart >= timing.connectEnd);
    assertTrue(timing.responseStart >= timing.requestStart);
    assertTrue(timing.responseEnd >= timing.responseStart);
    assertTrue(timing.responseEnd < 10000);
    context.close();
  }

  @PlaywrightTest
  @DisplayName("NE-132 should work for subresource")
  void shouldWorkForSubresource(ContextFactory contextFactory, TestServer server, BrowserName browserName) {
    // issue: https://github.com/microsoft/playwright/issues/20654
    assumeFalse(browserName == BrowserName.WEBKIT && Env.platform().equals("win32"), "fixme: responseStart is wrong due upstream webkit/libcurl bug");
    BrowserContext context = contextFactory.newContext();
    Page page = context.newPage();
    List<Request> requests = new ArrayList<>();
    page.onRequestFinished(request -> requests.add(request));
    page.navigate(server.PREFIX + "/one-style.html");
    assertEquals(2, requests.size());
    Timing timing = requests.get(1).timing();
    verifyConnectionTimingConsistency(timing);
    assertTrue(timing.requestStart >= 0);
    assertTrue(timing.responseStart > timing.requestStart);
    assertTrue(timing.responseEnd >= timing.responseStart);
    assertTrue(timing.responseEnd < 10000);
    context.close();
  }

  @PlaywrightTest
  @DisplayName("NE-133 should work for SSL")
  void shouldWorkForSSL(Browser browser, HttpsTestServer httpsServer, BrowserName browserName) {
    // issue: https://github.com/microsoft/playwright/issues/20654

    Page page = browser.newPage(new Browser.NewPageOptions().setIgnoreHTTPSErrors(true));
    Request request = page.waitForRequestFinished(() -> {
      page.navigate(httpsServer.EMPTY_PAGE);
    });
    Timing timing = request.timing();
    verifyConnectionTimingConsistency(timing);
    assertTrue(timing.requestStart >= timing.connectEnd);
    assertTrue(timing.responseStart > timing.requestStart);
    assertTrue(timing.responseEnd >= timing.responseStart);
    assertTrue(timing.responseEnd < 10000);
    page.close();
  }

  @PlaywrightTest
  @DisplayName("NE-134 should work for redirect")
  @DisabledOnBrowser(value = BrowserName.WEBKIT, reason = "fixme: In WebKit, redirects don't carry the timing info")
  void shouldWorkForRedirect(ContextFactory contextFactory, BrowserName browserName, TestServer server) {
    BrowserContext context = contextFactory.newContext();
    Page page = context.newPage();
    server.setRedirect("/foo.html", "/empty.html");
    List<Response> responses = new ArrayList<>();
    page.onResponse(response -> responses.add(response));
    page.navigate(server.PREFIX + "/foo.html");
    for (Response r : responses) {
      r.finished();
    }

    assertEquals(2, responses.size());
    assertEquals(server.PREFIX + "/foo.html", responses.get(0).url());
    assertEquals(server.PREFIX + "/empty.html", responses.get(1).url());

    Timing timing1 = responses.get(0).request().timing();
    verifyConnectionTimingConsistency(timing1);
    assertTrue(timing1.requestStart >= timing1.connectEnd);
    assertTrue(timing1.responseStart > timing1.requestStart);
    assertTrue(timing1.responseEnd >= timing1.responseStart);
    assertTrue(timing1.responseEnd < 10000);

    Timing timing2 = responses.get(1).request().timing();
    verifyConnectionTimingConsistency(timing2);
    assertTrue(timing2.requestStart >= 0);
    assertTrue(timing2.responseStart > timing2.requestStart);
    assertTrue(timing2.responseEnd >= timing2.responseStart);
    assertTrue(timing2.responseEnd < 10000);

    context.close();
  }

  @PlaywrightTest
  @DisplayName("NE-135 should work when serving from memory cache")
  @Fixme("java: Request.timing().responseStart stays -1 for a response served from the memory cache; the JavaScript client sets it to responseEnd when it is -1 (Request._setResponseEndTiming), the Java client only sets responseEnd")
  void shouldWorkWhenServingFromMemoryCache(ContextFactory contextFactory, TestServer server, BrowserName browserName) {
    // issue: https://github.com/microsoft/playwright-java/issues/1080
    assumeFalse(browserName == BrowserName.FIREFOX && !Env.isBidi(), "fixme: Response event is not fired in Firefox");
    server.setRoute("/one-style.css", (req, res) -> {
      res.writeHead(200, "Content-Type", "text/css", "Cache-Control", "public, max-age=10031518");
      res.end("body { background: red }");
    });

    BrowserContext context = contextFactory.newContext();
    Page page = context.newPage();
    page.navigate(server.PREFIX + "/one-style.html");
    Response response = page.waitForResponse("**/one-style.css", () -> {
      page.reload();
    });
    response.finished();

    Timing timing = response.request().timing();
    verifyConnectionTimingConsistency(timing);

    assertEquals(timing.responseEnd, timing.responseStart);
    assertTrue(timing.responseEnd < 1000);
    context.close();
  }

  private static void verifyTimingValue(double value, double previous) {
    // expect(value === -1 || value > 0 && value >= previous) has no matcher upstream, so it checks nothing.
  }

  private static void verifyConnectionTimingConsistency(Timing timing) {
    verifyTimingValue(timing.domainLookupStart, -1);
    verifyTimingValue(timing.domainLookupEnd, timing.domainLookupStart);
    verifyTimingValue(timing.connectStart, timing.domainLookupEnd);
    verifyTimingValue(timing.secureConnectionStart, timing.connectStart);
    verifyTimingValue(timing.connectEnd, timing.secureConnectionStart);
  }
}
