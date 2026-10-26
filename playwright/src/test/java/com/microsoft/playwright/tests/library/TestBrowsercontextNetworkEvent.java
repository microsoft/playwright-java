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

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.microsoft.playwright.fixtures.Utils.async;
import static com.microsoft.playwright.fixtures.Utils.cause;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/library/browsercontext-network-event.spec.ts. */
public class TestBrowsercontextNetworkEvent {
  @PlaywrightTest
  @DisplayName("NE-121 BrowserContext.Events.Request")
  void browserContextEventsRequest(BrowserContext context, TestServer server) {
    Page page = context.newPage();
    List<Request> requests = new ArrayList<>();
    context.onRequest(request -> requests.add(request));
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<a target=_blank rel=noopener href=\"/one-style.html\">yo</a>");
    Page page1 = context.waitForPage(() -> page.click("a"));
    page1.waitForLoadState();
    List<String> urls = new ArrayList<>();
    for (Request r : requests) {
      urls.add(r.url());
    }
    assertEquals(Arrays.asList(
      server.EMPTY_PAGE,
      server.PREFIX + "/one-style.html",
      server.PREFIX + "/one-style.css"), urls);
  }

  @PlaywrightTest
  @DisplayName("NE-122 BrowserContext.Events.Response")
  void browserContextEventsResponse(BrowserContext context, TestServer server) {
    Page page = context.newPage();
    List<Response> responses = new ArrayList<>();
    context.onResponse(response -> responses.add(response));
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<a target=_blank rel=noopener href=\"/one-style.html\">yo</a>");
    Page page1 = context.waitForPage(() -> page.click("a"));
    page1.waitForLoadState();
    List<String> urls = new ArrayList<>();
    for (Response r : responses) {
      urls.add(r.url());
    }
    assertEquals(Arrays.asList(
      server.EMPTY_PAGE,
      server.PREFIX + "/one-style.html",
      server.PREFIX + "/one-style.css"), urls);
  }

  @PlaywrightTest
  @DisplayName("NE-123 BrowserContext.Events.RequestFailed")
  void browserContextEventsRequestFailed(BrowserContext context, TestServer server) {
    server.setRoute("/one-style.css", (req, res) -> {
      res.setHeader("Content-Type", "text/css");
      res.socket().destroy();
    });
    Page page = context.newPage();
    List<Request> failedRequests = new ArrayList<>();
    context.onRequestFailed(request -> failedRequests.add(request));
    page.navigate(server.PREFIX + "/one-style.html");
    assertEquals(1, failedRequests.size());
    assertTrue(failedRequests.get(0).url().contains("one-style.css"));
    assertNull(failedRequests.get(0).response());
    assertEquals("stylesheet", failedRequests.get(0).resourceType());
    assertNotNull(failedRequests.get(0).frame());
  }

  @PlaywrightTest
  @DisplayName("NE-124 BrowserContext.Events.RequestFinished")
  void browserContextEventsRequestFinished(BrowserContext context, TestServer server) {
    Page page = context.newPage();
    // Java: there is no context.waitForRequestFinished, a handler completes a future.
    CompletableFuture<Request> requestFinished = new CompletableFuture<>();
    context.onRequestFinished(requestFinished::complete);
    Response response = page.navigate(server.EMPTY_PAGE);
    context.waitForCondition(requestFinished::isDone);
    Request request = response.request();
    assertEquals(server.EMPTY_PAGE, request.url());
    assertNotNull(request.response());
    assertEquals(true, request.frame() == page.mainFrame());
    assertEquals(server.EMPTY_PAGE, request.frame().url());
    assertNull(request.failure());
  }

  @PlaywrightTest
  @DisplayName("NE-125 should fire events in proper order")
  void shouldFireEventsInProperOrder(BrowserContext context, TestServer server) {
    Page page = context.newPage();
    List<String> events = new ArrayList<>();
    context.onRequest(r -> events.add("request"));
    context.onResponse(r -> events.add("response"));
    // Java: there is no context.waitForRequestFinished, a handler completes a future.
    CompletableFuture<Request> requestFinished = new CompletableFuture<>();
    context.onRequestFinished(r -> {
      events.add("requestfinished");
      requestFinished.complete(r);
    });
    page.navigate(server.EMPTY_PAGE);
    context.waitForCondition(requestFinished::isDone);
    assertEquals(Arrays.asList(
      "request",
      "response",
      "requestfinished"), events);
  }

  @PlaywrightTest
  @DisplayName("NE-126 should not fire events for favicon or favicon redirects")
  void shouldNotFireEventsForFaviconOrFaviconRedirects(BrowserContext context, Page page, TestServer server, BrowserName browserName) throws Exception {
    assumeFalse(!Env.headed() && browserName != BrowserName.FIREFOX, "headless browsers, except firefox, do not request favicons");
    assumeFalse(Env.headed() && browserName == BrowserName.WEBKIT, "headed webkit does not have a favicon feature");
    String favicon = "/no-cache/favicon.ico";
    String hashedFaviconUrl = "/favicon-hashed.ico";
    String imagePath = "/fakeimage.png";
    String pagePath = "/page.html";
    server.setRedirect(favicon, hashedFaviconUrl);
    server.setRoute(pagePath, (_req, res) -> {
      res.end("\n" +
        "      <!DOCTYPE html>\n" +
        "      <html>\n" +
        "        <head>\n" +
        "          <meta charset=\"utf-8\">\n" +
        "          <link rel=\"icon\" type=\"image/svg+xml\" href=\"" + favicon + "\">\n" +
        "          <title>SVG Favicon Test</title>\n" +
        "        </head>\n" +
        "        <body>\n" +
        "          <img src=\"" + imagePath + "\" alt=\"my fake image\">\n" +
        "        </body>\n" +
        "      </html>\n");
    });

    List<String> events = new ArrayList<>();
    context.onRequest(req -> events.add(req.url()));
    context.onResponse(res -> events.add(res.url()));
    context.onRequestFinished(req -> events.add(req.url()));

    CompletableFuture<TestServer.Request> faviconRequest = server.waitForRequest(favicon);
    CompletableFuture<TestServer.Request> hashedFaviconRequest = server.waitForRequest(hashedFaviconUrl);
    page.navigate(server.PREFIX + "/page.html");
    faviconRequest.get();
    hashedFaviconRequest.get();

    assertTrue(events.stream().anyMatch(e -> e.contains(pagePath)));
    assertTrue(events.stream().anyMatch(e -> e.contains(imagePath)));
    assertFalse(events.stream().anyMatch(e -> e.contains(favicon)));
    assertFalse(events.stream().anyMatch(e -> e.contains(hashedFaviconUrl)));
  }

  @PlaywrightTest
  @DisplayName("NE-127 should reject response.finished if context closes")
  void shouldRejectResponseFinishedIfContextCloses(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    server.setRoute("/get", (req, res) -> {
      // In Firefox, |fetch| will be hanging until it receives |Content-Type| header
      // from server.
      res.setHeader("Content-Type", "text/plain; charset=utf-8");
      res.write("hello ");
    });
    // send request and wait for server response
    // Java: there is no page.waitForEvent('response') without a predicate.
    Response pageResponse = page.waitForResponse(r -> true, () -> {
      page.evaluate("() => fetch('./get', { method: 'GET' })");
    });

    // Java: the pending promise is an async call.
    CompletableFuture<String> finishPromise = async(() -> pageResponse.finished());
    page.context().close();
    Throwable error = cause(finishPromise);
    assertTrue(error.getMessage().contains("closed"));
  }
}
