/*
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

import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.EnabledOnBrowser;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static com.microsoft.playwright.fixtures.Utils.attachFrame;
import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/page-event-request.spec.ts. */
public class TestPageEventRequest {
  @PlaywrightTest
  @DisplayName("NE-091 should fire for navigation requests")
  void shouldFireForNavigationRequests(Page page, TestServer server) {
    List<Request> requests = new ArrayList<>();
    page.onRequest(request -> requests.add(request));
    page.navigate(server.EMPTY_PAGE);
    assertEquals(1, requests.size());
  }

  @PlaywrightTest
  @DisplayName("NE-092 should fire for iframes")
  void shouldFireForIframes(Page page, TestServer server) {
    List<Request> requests = new ArrayList<>();
    page.onRequest(request -> requests.add(request));
    page.navigate(server.EMPTY_PAGE);
    attachFrame(page, "frame1", server.EMPTY_PAGE);
    assertEquals(2, requests.size());
  }

  @PlaywrightTest
  @DisplayName("NE-093 should fire for fetches")
  void shouldFireForFetches(Page page, TestServer server) {
    List<Request> requests = new ArrayList<>();
    page.onRequest(request -> requests.add(request));
    page.navigate(server.EMPTY_PAGE);
    page.evaluate("() => fetch('/empty.html')");
    assertEquals(2, requests.size());
  }

  @PlaywrightTest
  @DisplayName("NE-094 should fire for fetches with keepalive: true")
  void shouldFireForFetchesWithKeepaliveTrue(Page page, TestServer server) {
    List<Request> requests = new ArrayList<>();
    page.onRequest(request -> requests.add(request));
    page.navigate(server.EMPTY_PAGE);
    page.evaluate("() => fetch('/empty.html', { keepalive: true })");
    assertEquals(2, requests.size());
  }

  @PlaywrightTest
  @DisplayName("NE-099 should return response body when Cross-Origin-Opener-Policy is set")
  void shouldReturnResponseBodyWhenCrossOriginOpenerPolicyIsSet(Page page, TestServer server) {
    server.setRoute("/empty.html", (req, res) -> {
      res.setHeader("Cross-Origin-Opener-Policy", "same-origin");
      // Note: without 'onload', Firefox sometimes does not fire the load event
      // over the protocol. The reason is unclear.
      res.end("\n" +
        "      <div>Hello there!</div>\n" +
        "      <script>window.onload = () => console.log('onload')</script>\n" +
        "    ");
    });
    Response response = page.navigate(server.EMPTY_PAGE);
    assertEquals(server.EMPTY_PAGE, page.url());
    response.finished();
    assertNull(response.request().failure());
    assertTrue(response.text().contains("Hello there!"));
  }

  @PlaywrightTest
  @DisplayName("NE-100 should fire requestfailed when intercepting race")
  @EnabledOnBrowser(value = BrowserName.CHROMIUM, reason = "This test is specifically testing Chromium race")
  void shouldFireRequestfailedWhenInterceptingRace(Page page, TestServer server) throws Exception {
    CompletableFuture<Void> promise = new CompletableFuture<>();
    int[] counter = {0};
    Set<Request> failures = new HashSet<>();
    Set<Request> alive = new HashSet<>();
    page.onRequest(request -> {
      assertFalse(alive.contains(request));
      assertFalse(failures.contains(request));
      alive.add(request);
    });
    page.onRequestFailed(request -> {
      assertFalse(failures.contains(request));
      assertTrue(alive.contains(request));
      alive.remove(request);
      failures.add(request);
      if (++counter[0] == 10)
        promise.complete(null);
    });

    // Stall requests to make sure we don't get requestfinished.
    page.route("**", route -> {});

    page.setContent("\n" +
      "    <iframe src=\"" + server.EMPTY_PAGE + "\"></iframe>\n" +
      "    <iframe src=\"" + server.EMPTY_PAGE + "\"></iframe>\n" +
      "    <iframe src=\"" + server.EMPTY_PAGE + "\"></iframe>\n" +
      "    <iframe src=\"" + server.EMPTY_PAGE + "\"></iframe>\n" +
      "    <iframe src=\"" + server.EMPTY_PAGE + "\"></iframe>\n" +
      "    <iframe src=\"" + server.EMPTY_PAGE + "\"></iframe>\n" +
      "    <iframe src=\"" + server.EMPTY_PAGE + "\"></iframe>\n" +
      "    <iframe src=\"" + server.EMPTY_PAGE + "\"></iframe>\n" +
      "    <iframe src=\"" + server.EMPTY_PAGE + "\"></iframe>\n" +
      "    <iframe src=\"" + server.EMPTY_PAGE + "\"></iframe>\n" +
      "    <script>\n" +
      "      function abortAll() {\n" +
      "        const frames = document.querySelectorAll(\"iframe\");\n" +
      "        for (const frame of frames)\n" +
      "          frame.src = \"about:blank\";\n" +
      "      }\n" +
      "      abortAll();\n" +
      "    </script>\n" +
      "  ");

    // Java: awaiting a future that a page event completes is waitForCondition(), it dispatches the events.
    page.waitForCondition(() -> promise.isDone());
    promise.get();
  }

  @PlaywrightTest
  @DisplayName("NE-101 main resource xhr should have type xhr")
  void mainResourceXhrShouldHaveTypeXhr(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    // Java: waitForEvent('request') without a predicate is waitForRequest with a predicate that accepts all.
    Request request = page.waitForRequest(r -> true, () -> {
      page.evaluate("() => {\n" +
        "  const x = new XMLHttpRequest();\n" +
        "  x.open('GET', location.href, false);\n" +
        "  x.send();\n" +
        "}");
    });
    assertEquals(false, request.isNavigationRequest());
    assertEquals("xhr", request.resourceType());
  }

  @PlaywrightTest
  @DisplayName("NE-102 should finish 204 request")
  void shouldFinish204Request(Page page, TestServer server) throws Exception {
    server.setRoute("/204", (req, res) -> {
      res.writeHead(204, "Content-type", "text/plain");
      res.end();
    });
    page.navigate(server.EMPTY_PAGE);
    // Java: Promise.race of two page events is a future that either handler completes.
    CompletableFuture<String> reqPromise = new CompletableFuture<>();
    page.onRequestFailed(r -> {
      if (r.url().endsWith("/204"))
        reqPromise.complete("requestfailed");
    });
    page.onRequestFinished(r -> {
      if (r.url().endsWith("/204"))
        reqPromise.complete("requestfinished");
    });
    try {
      page.evaluate("async url => { await fetch(url); }", server.PREFIX + "/204");
    } catch (PlaywrightException e) {
      // .catch(() => {})
    }
    page.waitForCondition(() -> reqPromise.isDone());
    assertEquals("requestfinished", reqPromise.get());
  }

  @PlaywrightTest
  @DisplayName("NE-103 should fail 204 navigation request")
  @Fixme(value = "driver: the driver 1.64.1 reports requestfinished for the 204 navigation request, the test expects requestfailed", browsers = BrowserName.CHROMIUM)
  void shouldFail204NavigationRequest(Page page, TestServer server) throws Exception {
    server.setRoute("/204", (req, res) -> {
      res.statusCode = 204;
      res.end();
    });
    page.navigate(server.EMPTY_PAGE);
    // Java: Promise.race of two page events is a future that either handler completes.
    CompletableFuture<String> reqPromise = new CompletableFuture<>();
    page.onRequestFailed(r -> {
      if (r.url().endsWith("/204"))
        reqPromise.complete("requestfailed");
    });
    page.onRequestFinished(r -> {
      if (r.url().endsWith("/204"))
        reqPromise.complete("requestfinished");
    });
    page.setContent("<a href=\"" + server.PREFIX + "/204\">link</a>");
    page.click("a", new Page.ClickOptions().setNoWaitAfter(true));
    page.waitForCondition(() -> reqPromise.isDone());
    assertEquals("requestfailed", reqPromise.get());
  }

  @PlaywrightTest
  @DisplayName("NE-104 <picture> resource should have type image")
  void pictureResourceShouldHaveTypeImage(Page page) {
    // Java: waitForEvent('request') without a predicate is waitForRequest with a predicate that accepts all.
    Request request = page.waitForRequest(r -> true, () -> {
      page.setContent("\n" +
        "      <picture>\n" +
        "        <source>\n" +
        "          <img src=\"https://www.wikipedia.org/portal/wikipedia.org/assets/img/Wikipedia-logo-v2@2x.png\">\n" +
        "        </source>\n" +
        "      </picture>\n" +
        "    ");
    });
    assertEquals("image", request.resourceType());
  }

  // Chromium: requestWillBeSentEvent.frameId is undefined for OPTIONS.
  // WebKit: no requestWillBeSent event in the protocol for OPTIONS (at least on Mac).
  // Firefox: OPTIONS request can be dispatched.
  @PlaywrightTest
  @DisplayName("NE-105 should not expose preflight OPTIONS request")
  void shouldNotExposePreflightOPTIONSRequest(Page page, TestServer server) {
    List<String> serverRequests = Collections.synchronizedList(new ArrayList<>());
    server.setRoute("/cors", (req, res) -> {
      serverRequests.add(req.method + " " + req.url);
      if (req.method.equals("OPTIONS")) {
        res.writeHead(204,
          "Access-Control-Allow-Origin", "*",
          "Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS",
          "Access-Control-Allow-Headers", "*");
        res.end();
        return;
      }
      res.writeHead(200, "Content-type", "text/plain", "Access-Control-Allow-Origin", "*");
      res.end("Hello there!");
    });
    List<String> clientRequests = new ArrayList<>();
    page.onRequest(request -> {
      clientRequests.add(request.method() + " " + request.url());
    });
    Object response;
    try {
      response = page.evaluate("async url => {\n" +
        "  const response = await fetch(url, {\n" +
        "    method: 'POST',\n" +
        "    body: '',\n" +
        "    headers: {\n" +
        "      'Content-Type': 'application/json',\n" +
        "      'X-Custom-Header': 'test-value'\n" +
        "    }\n" +
        "  });\n" +
        "  return await response.text();\n" +
        "}", server.CROSS_PROCESS_PREFIX + "/cors");
    } catch (PlaywrightException e) {
      response = null;
    }
    assertEquals("Hello there!", response);
    assertEquals(asList(
      "OPTIONS /cors",
      "POST /cors"), serverRequests);
    assertEquals(asList(
      "POST " + server.CROSS_PROCESS_PREFIX + "/cors"), clientRequests);
  }

  @PlaywrightTest
  @DisplayName("NE-106 should not expose preflight OPTIONS request with network interception")
  void shouldNotExposePreflightOPTIONSRequestWithNetworkInterception(Page page, TestServer server, BrowserName browserName) {
    List<String> serverRequests = Collections.synchronizedList(new ArrayList<>());
    server.setRoute("/cors", (req, res) -> {
      serverRequests.add(req.method + " " + req.url);
      if (req.method.equals("OPTIONS")) {
        res.writeHead(204,
          "Access-Control-Allow-Origin", "*",
          "Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS",
          "Access-Control-Allow-Headers", "*");
        res.end();
        return;
      }
      res.writeHead(200, "Content-type", "text/plain", "Access-Control-Allow-Origin", "*");
      res.end("Hello there!");
    });
    page.route("**/*", route -> route.resume());
    List<String> clientRequests = new ArrayList<>();
    page.onRequest(request -> {
      clientRequests.add(request.method() + " " + request.url());
    });
    Object response;
    try {
      response = page.evaluate("async url => {\n" +
        "  const response = await fetch(url, {\n" +
        "    method: 'POST',\n" +
        "    body: '',\n" +
        "    headers: {\n" +
        "      'Content-Type': 'application/json',\n" +
        "      'X-Custom-Header': 'test-value'\n" +
        "    }\n" +
        "  });\n" +
        "  return await response.text();\n" +
        "}", server.CROSS_PROCESS_PREFIX + "/cors");
    } catch (PlaywrightException e) {
      response = null;
    }
    assertEquals("Hello there!", response);
    // Java: expect.soft() is assertAll().
    List<String> expectedServerRequests = new ArrayList<>();
    if (!(browserName == BrowserName.CHROMIUM || Env.isBidi()))
      expectedServerRequests.add("OPTIONS /cors");
    expectedServerRequests.add("POST /cors");
    assertAll(
      () -> assertEquals(expectedServerRequests, serverRequests),
      () -> assertEquals(asList(
        "POST " + server.CROSS_PROCESS_PREFIX + "/cors"), clientRequests));
  }

  @PlaywrightTest
  @DisplayName("NE-107 should return last requests")
  void shouldReturnLastRequests(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/title.html");
    for (int i = 0; i < 200; ++i)
      server.setRoute("/fetch?" + i, (req, res) -> res.end("url:" + server.PREFIX + req.url));

    // #0 is the navigation request, so start with #1.
    for (int i = 0; i < 99; ++i)
      page.evaluate("url => fetch(url)", server.PREFIX + "/fetch?" + i);
    List<Request> first99Requests = new ArrayList<>(page.requests());
    first99Requests.remove(0);
    for (int i = 99; i < 199; ++i)
      page.evaluate("url => fetch(url)", server.PREFIX + "/fetch?" + i);
    List<Request> last100Requests = page.requests();
    List<Request> allRequests = new ArrayList<>(first99Requests);
    allRequests.addAll(last100Requests);

    // All 199 requests are fully functional.
    List<Map<String, Object>> received = new ArrayList<>();
    for (Request request : allRequests) {
      Response response = request.response();
      received.add(mapOf("text", response.text(), "url", request.url()));
    }
    List<Map<String, Object>> expected = new ArrayList<>();
    for (int i = 0; i < 199; ++i) {
      String url = server.PREFIX + "/fetch?" + i;
      expected.add(mapOf("url", url, "text", "url:" + url));
    }
    assertEquals(expected, received);
  }
}
