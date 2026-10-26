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
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/page-event-network.spec.ts. */
public class TestPageEventNetwork {
  @PlaywrightTest
  @DisplayName("NE-084 Page.Events.Request @smoke")
  void pageEventsRequest(Page page, TestServer server) {
    List<Request> requests = new ArrayList<>();
    page.onRequest(request -> requests.add(request));
    page.navigate(server.EMPTY_PAGE);
    assertEquals(1, requests.size());
    assertEquals(server.EMPTY_PAGE, requests.get(0).url());
    assertEquals("document", requests.get(0).resourceType());
    assertEquals("GET", requests.get(0).method());
    assertNotNull(requests.get(0).response());
    assertEquals(true, requests.get(0).frame() == page.mainFrame());
    assertEquals(server.EMPTY_PAGE, requests.get(0).frame().url());
  }

  @PlaywrightTest
  @DisplayName("NE-085 Page.Events.Response @smoke")
  void pageEventsResponse(Page page, TestServer server) {
    List<Response> responses = new ArrayList<>();
    page.onResponse(response -> responses.add(response));
    page.navigate(server.EMPTY_PAGE);
    assertEquals(1, responses.size());
    assertEquals(server.EMPTY_PAGE, responses.get(0).url());
    assertEquals(200, responses.get(0).status());
    assertEquals(true, responses.get(0).ok());
    assertNotNull(responses.get(0).request());
  }

  @PlaywrightTest
  @DisplayName("NE-086 Page.Events.RequestFailed @smoke")
  void pageEventsRequestFailed(Page page, TestServer server, BrowserName browserName) {
    server.setRoute("/one-style.css", (req, res) -> {
      res.setHeader("Content-Type", "text/css");
      res.socket().destroy();
    });
    List<Request> failedRequests = new ArrayList<>();
    page.onRequestFailed(request -> failedRequests.add(request));
    page.navigate(server.PREFIX + "/one-style.html");
    assertEquals(1, failedRequests.size());
    assertTrue(failedRequests.get(0).url().contains("one-style.css"));
    assertNull(failedRequests.get(0).response());
    assertEquals("stylesheet", failedRequests.get(0).resourceType());
    if (browserName == BrowserName.CHROMIUM) {
      assertEquals("net::ERR_EMPTY_RESPONSE", failedRequests.get(0).failure());
    } else if (browserName == BrowserName.WEBKIT) {
      if (Env.platform().equals("linux") || "webkit-wsl".equals(Env.channel()))
        assertTrue(Pattern.compile("(Message Corrupt)|(Connection terminated unexpectedly)", Pattern.CASE_INSENSITIVE).matcher(failedRequests.get(0).failure()).find());
      else if (Env.platform().equals("darwin"))
        assertEquals("The network connection was lost.", failedRequests.get(0).failure());
      else if (Env.platform().equals("win32"))
        assertEquals("Server returned nothing (no headers, no data)", failedRequests.get(0).failure());
    } else {
      assertEquals("NS_ERROR_NET_RESET", failedRequests.get(0).failure());
    }
    assertNotNull(failedRequests.get(0).frame());
  }

  @PlaywrightTest
  @DisplayName("NE-087 Page.Events.RequestFinished @smoke")
  void pageEventsRequestFinished(Page page, TestServer server) {
    Response[] response = new Response[1];
    page.waitForRequestFinished(() -> {
      response[0] = page.navigate(server.EMPTY_PAGE);
    });
    Request request = response[0].request();
    assertEquals(server.EMPTY_PAGE, request.url());
    assertNotNull(request.response());
    assertEquals(true, request.frame() == page.mainFrame());
    assertEquals(server.EMPTY_PAGE, request.frame().url());
    assertNull(request.failure());
  }

  @PlaywrightTest
  @DisplayName("NE-088 should fire events in proper order")
  void shouldFireEventsInProperOrder(Page page, TestServer server) {
    List<String> events = new ArrayList<>();
    page.onRequest(request -> events.add("request"));
    page.onResponse(response -> events.add("response"));
    Response response = page.navigate(server.EMPTY_PAGE);
    assertNull(response.finished());
    events.add("requestfinished");
    assertEquals(asList("request", "response", "requestfinished"), events);
  }

  @PlaywrightTest
  @DisplayName("NE-089 should support redirects")
  void shouldSupportRedirects(Page page, TestServer server) {
    String FOO_URL = server.PREFIX + "/foo.html";
    Map<String, List<Object>> events = new LinkedHashMap<>();
    events.put(server.EMPTY_PAGE, new ArrayList<>());
    events.put(FOO_URL, new ArrayList<>());
    page.onRequest(request -> events.get(request.url()).add(request.method()));
    page.onResponse(response -> events.get(response.url()).add(response.status()));
    page.onRequestFinished(request -> events.get(request.url()).add("DONE"));
    page.onRequestFailed(request -> events.get(request.url()).add("FAIL"));

    server.setRedirect("/foo.html", "/empty.html");
    Response response = page.navigate(FOO_URL);
    response.finished();

    Map<String, List<Object>> expected = new LinkedHashMap<>();
    expected.put(FOO_URL, asList("GET", 302, "DONE"));
    expected.put(server.EMPTY_PAGE, asList("GET", 200, "DONE"));
    assertEquals(expected, events);
    Request redirectedFrom = response.request().redirectedFrom();
    assertTrue(redirectedFrom.url().contains("/foo.html"));
    assertNull(redirectedFrom.redirectedFrom());
    assertEquals(response.request(), redirectedFrom.redirectedTo());
  }

  @PlaywrightTest
  @DisplayName("NE-090 should resolve responses after a navigation")
  @DisabledOnBrowser(value = BrowserName.CHROMIUM, reason = "fixme")
  void shouldResolveResponsesAfterANavigation(Page page, TestServer server) throws Exception {
    CompletableFuture<TestServer.Response> responseFromServerPromise = new CompletableFuture<>();
    server.setRoute("/foo", (message, response) -> {
      responseFromServerPromise.complete(response);
    });
    page.navigate(server.EMPTY_PAGE);
    // start a long running request and wait until it hits the server
    Request request = page.waitForRequest(r -> true, () -> {
      page.evaluate("url => void fetch(url)", server.PREFIX + "/foo");
    });
    TestServer.Response responseFromServer = responseFromServerPromise.get();
    // navigate, which should cancel the request
    page.navigate(server.CROSS_PROCESS_PREFIX);
    // make sure we aren't stalling this request on the server
    responseFromServer.end("done");
    // the response should resolve to null, because the page navigated.
    // Java: request.response() keeps its result once the page has navigated, so it runs after the navigation.
    assertNull(request.response());
  }
}
