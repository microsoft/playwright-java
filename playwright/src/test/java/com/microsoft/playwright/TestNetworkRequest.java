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

import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.options.HttpHeader;
import org.junit.jupiter.api.condition.OS;

import java.io.OutputStreamWriter;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static com.microsoft.playwright.Utils.*;
import static com.microsoft.playwright.fixtures.BrowserName.CHROMIUM;
import static com.microsoft.playwright.fixtures.BrowserName.FIREFOX;
import static com.microsoft.playwright.fixtures.BrowserName.WEBKIT;
import static org.junit.jupiter.api.Assertions.*;

public class TestNetworkRequest {
  @PlaywrightTest
  void shouldWorkForMainFrameNavigationRequest(Page page, Server server) {
    List<Request> requests = new ArrayList<>();
    page.onRequest(request -> requests.add(request));
    page.navigate(server.EMPTY_PAGE);
    assertEquals(1, requests.size());
    assertEquals(page.mainFrame(), requests.get(0).frame());
  }

  @PlaywrightTest
  void shouldWorkForSubframeNavigationRequest(Page page, Server server) {
    page.navigate(server.EMPTY_PAGE);
    List<Request> requests = new ArrayList<>();
    page.onRequest(request -> requests.add(request));
    attachFrame(page, "frame1", server.EMPTY_PAGE);
    assertEquals(1, requests.size());
    assertEquals(page.frames().get(1), requests.get(0).frame());
  }

  @PlaywrightTest
  void shouldWorkForFetchRequests(Page page, Server server) {
    page.navigate(server.EMPTY_PAGE);
    List<Request> requests = new ArrayList<>();
    page.onRequest(request -> requests.add(request));
    page.evaluate("() => fetch('/digits/1.png')");
    assertEquals(1, requests.size());
    assertEquals(page.mainFrame(), requests.get(0).frame());
  }

  @PlaywrightTest
  void shouldWorkForARedirect(Page page, Server server) {
    server.setRedirect("/foo.html", "/empty.html");
    List<Request> requests = new ArrayList<>();
    page.onRequest(request -> requests.add(request));
    page.navigate(server.PREFIX + "/foo.html");

    assertEquals(2, requests.size());
    assertEquals(server.PREFIX + "/foo.html", requests.get(0).url());
    assertEquals(server.PREFIX + "/empty.html", requests.get(1).url());
  }

  @PlaywrightTest
  void shouldWorkAllHeadersInsideRoute(Page page, Server server) {
    List<Request> requests = new ArrayList<>();
    page.route("**", route -> {
      assertTrue(route.request().allHeaders().get("accept").length() > 5);
      requests.add(route.request());
      route.resume();
    });
    page.navigate(server.PREFIX + "/empty.html");
    assertEquals(1, requests.size());
  }

  // https://github.com/microsoft/playwright/issues/3993
  @PlaywrightTest
  void shouldNotWorkForARedirectAndInterception(Page page, Server server) {
    server.setRedirect("/foo.html", "/empty.html");
    List<Request> requests = new ArrayList<>();
    page.route("**", route -> {
      requests.add(route.request());
      route.resume();
    });
    page.navigate(server.PREFIX + "/foo.html");

    assertEquals(server.PREFIX + "/empty.html", page.url());

    assertEquals(1, requests.size());
    assertEquals(server.PREFIX + "/foo.html", requests.get(0).url());
  }

  @PlaywrightTest
  void shouldReturnHeaders(Page page, Server server, BrowserName browserName) {
    Response response = page.navigate(server.EMPTY_PAGE);
    if (browserName == CHROMIUM)
      assertTrue(response.request().headers().get("user-agent").contains("Chrome"));
    else if (browserName == FIREFOX)
      assertTrue(response.request().headers().get("user-agent").contains("Firefox"));
    else if (browserName == WEBKIT)
      assertTrue(response.request().headers().get("user-agent").contains("WebKit"));
  }

  // HTTP server in java does not preserve headers order.
  static List<HttpHeader> normalizeServerHeaders(Map<String,List<String>> headers) {
    List<HttpHeader> serverHeaders = new ArrayList<>();
    for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
      for (String value: entry.getValue()) {
        HttpHeader header = new HttpHeader();
        header.name = entry.getKey().toLowerCase();
        header.value = value;
        serverHeaders.add(header);
      }
    }
    Comparator<HttpHeader> comparator = Comparator.comparing(h -> h.name);
    serverHeaders.sort(comparator);
    return serverHeaders;
  }

  static List<HttpHeader> normalizeAllHeaders(Map<String, String> headers) {
    List<HttpHeader> serverHeaders = new ArrayList<>();
    for (Map.Entry<String, String> entry : headers.entrySet()) {
      HttpHeader header = new HttpHeader();
      header.name = entry.getKey().toLowerCase();
      header.value = entry.getValue();
      serverHeaders.add(header);
    }
    Comparator<HttpHeader> comparator = Comparator.comparing(h -> h.name);
    serverHeaders.sort(comparator);
    return serverHeaders;
  }

  static List<HttpHeader> adjustServerHeaders(BrowserName browserName, List<HttpHeader> headers) {
    if (browserName == FIREFOX) {
      for (Iterator<HttpHeader> it = headers.iterator(); it.hasNext(); ) {
        if ("priority".equals(it.next().name)) {
          it.remove();
        }
      }
    }
    return headers;
  }

  @PlaywrightTest
  @DisabledOnBrowser(value = WEBKIT, os = OS.WINDOWS, reason = "Flaky, see https://github.com/microsoft/playwright/issues/6690")
  void shouldGetTheSameHeadersAsTheServer(Page page, Server server, BrowserName browserName) throws ExecutionException, InterruptedException {
    Future<Server.Request> serverRequest = server.futureRequest("/empty.html");
    server.setRoute("/empty.html", exchange -> {
      exchange.sendResponseHeaders(200, 0);
      try (OutputStreamWriter writer = new OutputStreamWriter(exchange.getResponseBody())) {
        writer.write("done");
      }
    });
    Response response = page.navigate(server.PREFIX + "/empty.html");

    List<HttpHeader> expectedHeaders = adjustServerHeaders(browserName, normalizeServerHeaders(serverRequest.get().headers));
    List<HttpHeader> allHeaders = normalizeAllHeaders(response.request().allHeaders());
    assertJsonEquals(expectedHeaders, allHeaders);
  }

  @PlaywrightTest
  @DisabledOnBrowser(value = WEBKIT, reason = "fail")
  void shouldGetTheSameHeadersAsTheServerCORP(Page page, Server server, BrowserName browserName) throws ExecutionException, InterruptedException {
    page.navigate(server.PREFIX + "/empty.html");
    Future<Server.Request> serverRequest = server.futureRequest("/something");
    server.setRoute("/something", exchange -> {
      exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
      exchange.sendResponseHeaders(200, 0);
      try (OutputStreamWriter writer = new OutputStreamWriter(exchange.getResponseBody())) {
        writer.write("done");
      }
    });
    Response response = page.waitForResponse("**", () -> {
      Object text = page.evaluate("async url => {\n" +
          "  const data = await fetch(url);\n" +
          "  return data.text();\n" +
          "}", server.CROSS_PROCESS_PREFIX + "/something");
        assertEquals("done", text);
      });
    List<HttpHeader> expectedHeaders = adjustServerHeaders(browserName, normalizeServerHeaders(serverRequest.get().headers));
    List<HttpHeader> allHeaders = normalizeAllHeaders(response.request().allHeaders());
    assertJsonEquals(expectedHeaders, allHeaders);
  }

  @PlaywrightTest
  void shouldReturnPostData(Page page, Server server) {
    page.navigate(server.EMPTY_PAGE);
    server.setRoute("/post", exchange -> {
      exchange.sendResponseHeaders(200, 0);
      exchange.getResponseBody().close();
    });
    Request[] request = {null};
    page.onRequest(r -> request[0] = r);
    page.evaluate("() => fetch('./post', { method: 'POST', body: JSON.stringify({foo: 'bar'})})");
    assertNotNull(request[0]);
    assertEquals("{\"foo\":\"bar\"}", request[0].postData());
  }

  @PlaywrightTest
  void shouldWorkWithBinaryPostData(Page page, Server server) {
    page.navigate(server.EMPTY_PAGE);
    server.setRoute("/post", exchange -> {
      exchange.sendResponseHeaders(200, 0);
      exchange.getResponseBody().close();
    });
    Request[] request = {null};
    page.onRequest(r -> request[0] = r);
    page.evaluate("async () => {\n" +
      "  await fetch('./post', { method: 'POST', body: new Uint8Array(Array.from(Array(256).keys())) });\n" +
      "}");
    assertNotNull(request[0]);
    byte[] buffer = request[0].postDataBuffer();
    assertEquals(256, buffer.length);
    for (int i = 0; i < 256; ++i) {
      assertEquals((byte) i, buffer[i]);
    }
  }

  @PlaywrightTest
  void shouldWorkWithBinaryPostDataAndInterception(Page page, Server server) {
    page.navigate(server.EMPTY_PAGE);
    server.setRoute("/post", exchange -> {
      exchange.sendResponseHeaders(200, 0);
      exchange.getResponseBody().close();
    });
    Request[] request = {null};
    page.onRequest(r -> request[0] = r);
    page.route("/post", route -> route.resume());
    page.evaluate("async () => {\n" +
      "  await fetch('./post', { method: 'POST', body: new Uint8Array(Array.from(Array(256).keys())) });\n" +
      "}");
    assertNotNull(request[0]);
    byte[] buffer = request[0].postDataBuffer();
    assertEquals(256, buffer.length);
    for (int i = 0; i < 256; ++i) {
      assertEquals((byte) i, buffer[i]);
    }
  }

  @PlaywrightTest
  void shouldBeUndefinedWhenThereIsNoPostData(Page page, Server server) {
    Response response = page.navigate(server.EMPTY_PAGE);
    assertNull(response.request().postData());
  }

  void shouldParseTheJsonPostData() {
    // Not supported in Java.
  }

  void shouldParseTheDataIfContentTypeIsApplicationXWwwFormUrlencoded() {
    // Not supported in Java.
  }

  @PlaywrightTest
  void shouldReturnEventSource(Page page, Server server) {
    // 1. Setup server-sent events on server that immediately sends a message to the client.
    server.setRoute("/sse", exchange -> {
      exchange.getResponseHeaders().add("Content-Type", "text/event-stream");
      exchange.getResponseHeaders().add("Connection", "keep-alive");
      exchange.getResponseHeaders().add("Cache-Control", "no-cache");
      exchange.sendResponseHeaders(200, 0);
      try (OutputStreamWriter writer = new OutputStreamWriter(exchange.getResponseBody())) {
        writer.write("data: {\"foo\":\"bar\"}\n\n");
      }
    });
    // 2. Subscribe to page request events.
    page.navigate(server.EMPTY_PAGE);
    List<Request> requests = new ArrayList<>();
    page.onRequest(request -> requests.add(request));
    // 3. Connect to EventSource in browser and return first message.
    Object result = page.evaluate("() => {\n" +
      "  const eventSource = new EventSource('/sse');\n" +
      "  return new Promise(resolve => {\n" +
      "    eventSource.onmessage = e => resolve(JSON.parse(e.data));\n" +
      "  });\n" +
      "}");
    assertEquals(mapOf("foo", "bar"), result);
    assertEquals("eventsource", requests.get(0).resourceType());
  }

  @PlaywrightTest
  void shouldReturnNavigationBit(Page page, Server server) {
    Map<String, Request> requests = new HashMap<>();
    page.onRequest(request -> {
      String name = request.url();
      int lastSlash = name.lastIndexOf('/');
      if (lastSlash != -1) {
        name = name.substring(lastSlash + 1);
      }
      requests.put(name, request);
    });
    server.setRedirect("/rrredirect", "/frames/one-frame.html");
    page.navigate(server.PREFIX + "/rrredirect");
    assertTrue(requests.get("rrredirect").isNavigationRequest());
    assertTrue(requests.get("one-frame.html").isNavigationRequest());
    assertTrue(requests.get("frame.html").isNavigationRequest());
    assertFalse(requests.get("script.js").isNavigationRequest());
    assertFalse(requests.get("style.css").isNavigationRequest());
  }

  @PlaywrightTest
  void shouldReturnNavigationBitWhenNavigatingToImage(Page page, Server server) {
    List<Request> requests = new ArrayList<>();
    page.onRequest(request -> requests.add(request));
    page.navigate(server.PREFIX + "/pptr.png");
    assertTrue(requests.get(0).isNavigationRequest());
  }
}
