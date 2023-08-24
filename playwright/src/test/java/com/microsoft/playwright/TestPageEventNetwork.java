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

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class TestPageEventNetwork extends TestBase {
  @Test
  void PageEventsRequest() {
    List<Request> requests = new ArrayList<>();
    page.onRequest(request -> requests.add(request));
    page.navigate(server.EMPTY_PAGE);
    assertEquals(1, requests.size());
    assertEquals(server.EMPTY_PAGE, requests.get(0).url());
    assertEquals("document", requests.get(0).resourceType());
    assertEquals("GET", requests.get(0).method());
    assertNotNull(requests.get(0).response());
    assertEquals(page.mainFrame(), requests.get(0).frame());
    assertEquals(server.EMPTY_PAGE, requests.get(0).frame().url());
  }

  @Test
  void PageEventsResponse() {
    List<Response> responses = new ArrayList<>();
    page.onResponse(response -> responses.add(response));
    page.navigate(server.EMPTY_PAGE);
    assertEquals(1, responses.size());
    assertEquals(server.EMPTY_PAGE, responses.get(0).url());
    assertEquals(200, responses.get(0).status());
    assertTrue(responses.get(0).ok());
    assertNotNull(responses.get(0).request());
  }

  @Test
  void PageEventsRequestFailed() {
    server.setRoute("/one-style.css", exchange -> exchange.getResponseBody().close());
    List<Request> failedRequests = new ArrayList<>();
    page.onRequestFailed(request -> failedRequests.add(request));
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
    String error = failedRequests.get(0).failure();
    if (isChromium()) {
      assertEquals("net::ERR_EMPTY_RESPONSE", error);
    } else if (isWebKit()) {
      if (isMac)
        assertEquals("The network connection was lost.", error);
      else if (isWindows)
        assertEquals("Server returned nothing (no headers, no data)", error);
      else
        assertTrue("Message Corrupt".equals(error) || "Connection terminated unexpectedly".equals(error), error);
    } else {
      assertEquals("NS_ERROR_NET_RESET", error);
    }
    assertNotNull(failedRequests.get(0).frame());
  }

  @Test
  void PageEventsRequestFinished() {
    Request[] requestRef = {null};
    page.onRequestFinished(r -> requestRef[0] = r);
    Response response = page.navigate(server.EMPTY_PAGE);
    assertNotNull(response);
    assertNull(response.finished());
    Request request = requestRef[0];
    assertNotNull(request);
    assertEquals(response.request(), request);
    assertEquals(server.EMPTY_PAGE, request.url());
    assertNotNull(request.response());
    assertEquals(page.mainFrame(), request.frame());
    assertEquals(server.EMPTY_PAGE, request.frame().url());
    assertNull(request.failure());
  }

  @Test
  void PageWaitForRequestFinished() {
    Response[] responseRef = {null};
    Request request = page.waitForRequestFinished(() -> {
      responseRef[0] = page.navigate(server.EMPTY_PAGE);
    });
    assertNotNull(request);
    Response response = responseRef[0];
    assertNotNull(response);
    assertNull(response.finished());
    assertEquals(response.request(), request);
    assertEquals(server.EMPTY_PAGE, request.url());
    assertNotNull(request.response());
    assertEquals(page.mainFrame(), request.frame());
    assertEquals(server.EMPTY_PAGE, request.frame().url());
    assertNull(request.failure());
  }

  @Test
  void shouldFireEventsInProperOrder() {
    List<String> events = new ArrayList<>();
    page.onRequest(request -> events.add("request"));
    page.onResponse(response -> events.add("response"));
    page.onRequestFinished(r -> events.add("requestfinished"));
    Response response = page.navigate(server.EMPTY_PAGE);
    assertNull(response.finished());
    assertEquals(asList("request", "response", "requestfinished"), events);
  }

  @Test
  void shouldSupportRedirects() {
    List<String> events = new ArrayList<>();
    page.onRequest(request -> {
      events.add(request.method() + " " + request.url());
    });
    page.onResponse(response -> {
      events.add(response.status() + " " + response.url());
    });
    page.onRequestFinished(request -> {
      events.add("DONE " + request.url());
    });
    page.onRequestFailed(request -> {
      events.add("FAIL " + request.url());
    });
    server.setRedirect("/foo.html", "/empty.html");
    String FOO_URL = server.PREFIX + "/foo.html";
    Response response = page.navigate(FOO_URL);
    response.finished();
    assertEquals(asList(
      "GET " + FOO_URL,
      "302 " + FOO_URL,
      "DONE " + FOO_URL,
      "GET " + server.EMPTY_PAGE,
      "200 " + server.EMPTY_PAGE,
      "DONE " + server.EMPTY_PAGE), events);
    Request redirectedFrom = response.request().redirectedFrom();
    assertTrue(redirectedFrom.url().contains("/foo.html"));
    assertNull(redirectedFrom.redirectedFrom());
    assertEquals(response.request(), redirectedFrom.redirectedTo());
  }
}
