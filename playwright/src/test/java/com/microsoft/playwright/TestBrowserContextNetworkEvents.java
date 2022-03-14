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

import com.sun.net.httpserver.Filter;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class TestBrowserContextNetworkEvents extends TestBase {
  @Test
  void BrowserContextEventsRequest() {
    List<String> requests = new ArrayList<>();
    context.onRequest(request -> requests.add(request.url()));
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<a target=_blank rel=noopener href='/one-style.html'>yo</a>");
    Page page1 = context.waitForPage(() -> page.click("a"));
    page1.waitForLoadState();
    // In firefox one-style.css is requested multiple times.
    if (isFirefox()) {
      assertEquals(asList(
        server.EMPTY_PAGE,
        server.PREFIX + "/one-style.html",
        server.PREFIX + "/one-style.css",
        server.PREFIX + "/one-style.css"), requests);
    } else {
      assertEquals(asList(
        server.EMPTY_PAGE,
        server.PREFIX + "/one-style.html",
        server.PREFIX + "/one-style.css"), requests);
    }
  }

  @Test
  void BrowserContextEventsResponse() {
    List<String> responses = new ArrayList<>();
    context.onResponse(response -> responses.add(response.url()));
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<a target=_blank rel=noopener href='/one-style.html'>yo</a>");
    Page page1 = context.waitForPage(() -> page.click("a"));
    page1.waitForLoadState();
    // In firefox one-style.css is requested multiple times.
    if (isFirefox()) {
      assertEquals(asList(
        server.EMPTY_PAGE,
        server.PREFIX + "/one-style.html",
        server.PREFIX + "/one-style.css",
        server.PREFIX + "/one-style.css"), responses);
    } else {
      assertEquals(asList(
        server.EMPTY_PAGE,
        server.PREFIX + "/one-style.html",
        server.PREFIX + "/one-style.css"), responses);
    }
  }

  @Test
  void BrowserContextEventsRequestFailed() {
    server.setRoute("/one-style.css", exchange -> exchange.getResponseBody().close());
    List<Request> failedRequests = new ArrayList<>();
    context.onRequestFailed(request -> failedRequests.add(request));
    page.navigate(server.PREFIX + "/one-style.html");
    // In firefox one-style.css is requested multiple times.
    if (isFirefox()) {
      assertTrue(failedRequests.size() > 0);
    } else {
      assertEquals(1, failedRequests.size());
    }
    assertTrue(failedRequests.get(0).url().contains("one-style.css"));
    assertNull(failedRequests.get(0).response());
    assertEquals("stylesheet", failedRequests.get(0).resourceType());
    assertNotNull(failedRequests.get(0).frame());
  }


  @Test
  void BrowserContextEventsRequestFinished() {
    Request[] requestRef = {null};
    context.onRequestFinished(r -> requestRef[0] = r);
    Response response = page.navigate(server.EMPTY_PAGE);
    Request request = response.request();
    assertEquals(server.EMPTY_PAGE, request.url());
    assertNotNull(request.response());
    assertEquals(request.frame(), page.mainFrame());
    assertEquals(server.EMPTY_PAGE, request.frame().url());
    assertNull(request.failure());
  }

  @Test
  void shouldFireEventsInProperOrder() {
    List<String> events = new ArrayList<>();
    context.onRequest(r -> events.add("request"));
    context.onResponse(r -> events.add("response"));
    context.onRequestFinished(r -> events.add("requestfinished"));
    Response response = page.navigate(server.EMPTY_PAGE);
    assertNull(response.finished());
    assertEquals(asList("request", "response", "requestfinished"), events);
  }
}
