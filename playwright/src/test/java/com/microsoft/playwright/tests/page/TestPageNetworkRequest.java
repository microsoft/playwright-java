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

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.EnabledOnBrowser;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.HttpHeader;
import org.junit.jupiter.api.DisplayName;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.microsoft.playwright.fixtures.Utils.asset;
import static com.microsoft.playwright.fixtures.Utils.attachFrame;
import static com.microsoft.playwright.fixtures.Utils.browserMajorVersion;
import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/page/page-network-request.spec.ts. */
public class TestPageNetworkRequest {
  private static Map<String, String> adjustServerHeaders(Map<String, String> headers, BrowserName browserName) {
    // Java: the headers of the server request are read-only, so the copy loses the header.
    Map<String, String> result = new LinkedHashMap<>(headers);
    if (browserName == BrowserName.FIREFOX)
      result.remove("priority");
    return result;
  }

  @PlaywrightTest
  @DisplayName("NE-001 should work for main frame navigation request")
  void shouldWorkForMainFrameNavigationRequest(Page page, TestServer server) {
    List<Request> requests = new ArrayList<>();
    page.onRequest(request -> requests.add(request));
    page.navigate(server.EMPTY_PAGE);
    assertEquals(1, requests.size());
    assertEquals(page.mainFrame(), requests.get(0).frame());
  }

  @PlaywrightTest
  @DisplayName("NE-002 should work for subframe navigation request")
  void shouldWorkForSubframeNavigationRequest(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    List<Request> requests = new ArrayList<>();
    page.onRequest(request -> requests.add(request));
    attachFrame(page, "frame1", server.EMPTY_PAGE);
    assertEquals(1, requests.size());
    assertEquals(page.frames().get(1), requests.get(0).frame());
  }

  @PlaywrightTest
  @DisplayName("NE-003 should work for fetch requests @smoke")
  void shouldWorkForFetchRequests(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    List<Request> requests = new ArrayList<>();
    page.onRequest(request -> requests.add(request));
    page.evaluate("() => fetch('/digits/1.png')");
    assertEquals(1, requests.size());
    assertEquals(page.mainFrame(), requests.get(0).frame());
  }

  @PlaywrightTest
  @DisplayName("NE-004 should work for a redirect")
  void shouldWorkForARedirect(Page page, TestServer server) {
    server.setRedirect("/foo.html", "/empty.html");
    List<Request> requests = new ArrayList<>();
    page.onRequest(request -> requests.add(request));
    page.navigate(server.PREFIX + "/foo.html");

    assertEquals(2, requests.size());
    assertEquals(server.PREFIX + "/foo.html", requests.get(0).url());
    assertEquals(server.PREFIX + "/empty.html", requests.get(1).url());
  }

  // https://github.com/microsoft/playwright/issues/3993
  @PlaywrightTest
  @DisplayName("NE-005 should not work for a redirect and interception")
  void shouldNotWorkForARedirectAndInterception(Page page, TestServer server) {
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
  @DisplayName("NE-006 should return headers")
  void shouldReturnHeaders(Page page, TestServer server, BrowserName browserName) {
    Response response = page.navigate(server.EMPTY_PAGE);
    if (browserName == BrowserName.CHROMIUM)
      assertTrue(response.request().headers().get("user-agent").contains("Chrome"));
    else if (browserName == BrowserName.FIREFOX)
      assertTrue(response.request().headers().get("user-agent").contains("Firefox"));
    else if (browserName == BrowserName.WEBKIT)
      assertTrue(response.request().headers().get("user-agent").contains("WebKit"));
  }

  @PlaywrightTest
  @DisplayName("NE-007 should get the same headers as the server")
  void shouldGetTheSameHeadersAsTheServer(Page page, TestServer server, BrowserName browserName) {
    assumeFalse(browserName == BrowserName.WEBKIT && Env.platform().equals("win32") && !"webkit-wsl".equals(Env.channel()),
      "fail: Curl does not show accept-encoding and accept-language");
    AtomicReference<TestServer.Request> serverRequest = new AtomicReference<>();
    server.setRoute("/empty.html", (request, response) -> {
      serverRequest.set(request);
      response.end("done");
    });
    Response response = page.navigate(server.PREFIX + "/empty.html");
    Map<String, String> headers = response.request().allHeaders();
    assertEquals(adjustServerHeaders(serverRequest.get().headers, browserName), headers);
  }

  @PlaywrightTest
  @DisplayName("NE-008 should not return allHeaders() until they are available")
  void shouldNotReturnAllHeadersUntilTheyAreAvailable(Page page, TestServer server, BrowserName browserName) throws Exception {
    assumeFalse(browserName == BrowserName.WEBKIT && Env.platform().equals("win32") && !"webkit-wsl".equals(Env.channel()),
      "fail: Curl does not show accept-encoding and accept-language");

    // Java: the handlers call allHeaders() inline, they run while page.navigate() is pending.
    AtomicReference<Map<String, String>> requestHeadersRef = new AtomicReference<>();
    page.onRequest(request -> requestHeadersRef.set(request.allHeaders()));
    AtomicReference<Map<String, String>> responseHeadersRef = new AtomicReference<>();
    page.onResponse(response -> responseHeadersRef.set(response.allHeaders()));

    AtomicReference<TestServer.Request> serverRequest = new AtomicReference<>();
    server.setRoute("/empty.html", (request, response) -> {
      serverRequest.set(request);
      response.writeHead(200, "foo", "bar");
      Thread.sleep(3000);
      response.end("done");
    });

    page.navigate(server.PREFIX + "/empty.html");
    Map<String, String> requestHeaders = requestHeadersRef.get();
    assertEquals(adjustServerHeaders(serverRequest.get().headers, browserName), requestHeaders);

    Map<String, String> responseHeaders = responseHeadersRef.get();
    assertEquals("bar", responseHeaders.get("foo"));
  }

  @PlaywrightTest
  @DisplayName("NE-009 should get the same headers as the server CORS")
  void shouldGetTheSameHeadersAsTheServerCORS(Page page, TestServer server, BrowserName browserName) {
    assumeFalse(browserName == BrowserName.WEBKIT && Env.platform().equals("win32") && !"webkit-wsl".equals(Env.channel()),
      "fail: Curl does not show accept-encoding and accept-language");

    page.navigate(server.PREFIX + "/empty.html");
    AtomicReference<TestServer.Request> serverRequest = new AtomicReference<>();
    server.setRoute("/something", (request, response) -> {
      serverRequest.set(request);
      response.writeHead(200, "Access-Control-Allow-Origin", "*");
      response.end("done");
    });
    String[] text = new String[1];
    // Java: waitForEvent('response') without a predicate is waitForResponse with a predicate that accepts all.
    Response response = page.waitForResponse(r -> true, () -> {
      text[0] = (String) page.evaluate("async url => {\n" +
        "  const data = await fetch(url);\n" +
        "  return data.text();\n" +
        "}", server.CROSS_PROCESS_PREFIX + "/something");
    });
    assertEquals("done", text[0]);
    Map<String, String> headers = response.request().allHeaders();
    assertEquals(adjustServerHeaders(serverRequest.get().headers, browserName), headers);
  }

  @PlaywrightTest
  @DisplayName("NE-010 should not get preflight CORS requests when intercepting")
  void shouldNotGetPreflightCORSRequestsWhenIntercepting(Page page, TestServer server, BrowserName browserName) {
    page.navigate(server.PREFIX + "/empty.html");

    List<String> requests = Collections.synchronizedList(new ArrayList<>());
    server.setRoute("/something", (request, response) -> {
      requests.add(request.method);
      if (request.method.equals("OPTIONS")) {
        response.writeHead(204,
          "Access-Control-Allow-Origin", "*",
          "Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE",
          "Access-Control-Allow-Headers", "*",
          "Cache-Control", "no-cache");
        response.end();
        return;
      }
      response.writeHead(200, "Access-Control-Allow-Origin", "*");
      response.end("done");
    });
    // First check the browser will send preflight request when interception is OFF.
    {
      Object text = page.evaluate("async url => {\n" +
        "  const data = await fetch(url, {\n" +
        "    method: 'DELETE',\n" +
        "    headers: { 'X-PINGOTHER': 'pingpong' }\n" +
        "  });\n" +
        "  return data.text();\n" +
        "}", server.CROSS_PROCESS_PREFIX + "/something");
      assertEquals("done", text);
      assertEquals(asList("OPTIONS", "DELETE"), requests);
    }

    // Now check the browser will NOT send preflight request when interception is ON.
    {
      requests.clear();
      List<String> routed = new ArrayList<>();
      page.route("**/something", route -> {
        routed.add(route.request().method());
        route.resume();
      });

      Object text = page.evaluate("async url => {\n" +
        "  const data = await fetch(url, {\n" +
        "    method: 'DELETE',\n" +
        "    headers: { 'X-PINGOTHER': 'pingpong' }\n" +
        "  });\n" +
        "  return data.text();\n" +
        "}", server.CROSS_PROCESS_PREFIX + "/something");
      assertEquals("done", text);
      // Check that there was no preflight (OPTIONS) request.
      assertEquals(asList("DELETE"), routed);
      if (browserName == BrowserName.FIREFOX && !Env.isBidi())
        assertEquals(asList("OPTIONS", "DELETE"), requests);
      else
        assertEquals(asList("DELETE"), requests);
    }
  }

  @PlaywrightTest
  @DisplayName("NE-011 should return postData")
  void shouldReturnPostData(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    server.setRoute("/post", (req, res) -> res.end());
    AtomicReference<Request> request = new AtomicReference<>();
    page.onRequest(r -> request.set(r));
    page.evaluate("() => fetch('./post', { method: 'POST', body: JSON.stringify({ foo: 'bar' }) })");
    assertNotNull(request.get());
    assertEquals("{\"foo\":\"bar\"}", request.get().postData());
  }

  @PlaywrightTest
  @DisplayName("NE-012 should work with binary post data")
  void shouldWorkWithBinaryPostData(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    server.setRoute("/post", (req, res) -> res.end());
    AtomicReference<Request> request = new AtomicReference<>();
    page.onRequest(r -> request.set(r));
    page.evaluate("async () => {\n" +
      "  await fetch('./post', { method: 'POST', body: new Uint8Array(Array.from(Array(256).keys())) });\n" +
      "}");
    assertNotNull(request.get());
    byte[] buffer = request.get().postDataBuffer();
    assertEquals(256, buffer.length);
    for (int i = 0; i < 256; ++i)
      // Java: bytes are signed.
      assertEquals(i, buffer[i] & 0xFF);
  }

  @PlaywrightTest
  @DisplayName("NE-013 should work with binary post data and interception")
  void shouldWorkWithBinaryPostDataAndInterception(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    server.setRoute("/post", (req, res) -> res.end());
    AtomicReference<Request> request = new AtomicReference<>();
    page.route("/post", route -> route.resume());
    page.onRequest(r -> request.set(r));
    page.evaluate("async () => {\n" +
      "  await fetch('./post', { method: 'POST', body: new Uint8Array(Array.from(Array(256).keys())) });\n" +
      "}");
    assertNotNull(request.get());
    byte[] buffer = request.get().postDataBuffer();
    assertEquals(256, buffer.length);
    for (int i = 0; i < 256; ++i)
      // Java: bytes are signed.
      assertEquals(i, buffer[i] & 0xFF);
  }

  @PlaywrightTest
  @DisplayName("NE-014 should override post data content type")
  void shouldOverridePostDataContentType(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    AtomicReference<TestServer.Request> request = new AtomicReference<>();
    server.setRoute("/post", (req, res) -> {
      request.set(req);
      res.end();
    });
    page.route("**/post", route -> {
      Request request1 = route.request();
      Map<String, String> headers = new HashMap<>(request1.headers());
      headers.put("content-type", "application/x-www-form-urlencoded; charset=UTF-8");
      route.resume(new Route.ResumeOptions()
        .setHeaders(headers)
        .setPostData(request1.postData()));
    });
    page.evaluate("async () => {\n" +
      "  await fetch('./post', { method: 'POST', body: 'foo=bar' });\n" +
      "}");
    assertNotNull(request.get());
    assertEquals("application/x-www-form-urlencoded; charset=UTF-8", request.get().headers.get("content-type"));
  }

  @PlaywrightTest
  @DisplayName("NE-015 should get |undefined| with postData() when there is no post data")
  void shouldGetUndefinedWithPostDataWhenThereIsNoPostData(Page page, TestServer server) {
    Response response = page.navigate(server.EMPTY_PAGE);
    assertNull(response.request().postData());
  }

  @PlaywrightTest
  @DisplayName("NE-020 should return multipart/form-data")
  @DisabledOnBrowser(value = BrowserName.WEBKIT, reason = "fixme: File content is missing in WebKit")
  void shouldReturnMultipartFormData(Page page, TestServer server, BrowserName browserName, Browser browser) throws Exception {
    assumeFalse(browserName == BrowserName.CHROMIUM && browserMajorVersion(browser) < 126, "Requires a recent enough protocol");

    page.navigate(server.EMPTY_PAGE);
    server.setRoute("/post", (req, res) -> res.end());
    page.route("**/*", route -> route.resume());
    Request request = page.waitForRequest("**/post", () -> {
      page.evaluate("async () => {\n" +
        "  const body = new FormData();\n" +
        "  body.set('name1', 'value1');\n" +
        "  body.set('file', new File(['file-value'], 'foo.txt'));\n" +
        "  body.set('name2', 'value2');\n" +
        "  body.append('name2', 'another-value2');\n" +
        "  await fetch('/post', { method: 'POST', body });\n" +
        "}");
    });
    String contentType = request.headerValue("Content-Type");
    Pattern re = Pattern.compile("^multipart/form-data; boundary=(.*)$");
    assertTrue(re.matcher(contentType).find());
    Matcher matcher = re.matcher(contentType);
    matcher.find();
    String b = matcher.group(1);
    String expected = "--" + b + "\r\nContent-Disposition: form-data; name=\"name1\"\r\n\r\nvalue1\r\n--" + b + "\r\nContent-Disposition: form-data; name=\"file\"; filename=\"foo.txt\"\r\nContent-Type: application/octet-stream\r\n\r\nfile-value\r\n--" + b + "\r\nContent-Disposition: form-data; name=\"name2\"\r\n\r\nvalue2\r\n--" + b + "\r\nContent-Disposition: form-data; name=\"name2\"\r\n\r\nanother-value2\r\n--" + b + "--\r\n";
    assertEquals(expected, new String(request.postDataBuffer(), StandardCharsets.UTF_8));
  }

  @PlaywrightTest
  @DisplayName("NE-021 should return event source")
  void shouldReturnEventSource(Page page, TestServer server) {
    Map<String, Object> SSE_MESSAGE = mapOf("foo", "bar");
    // 1. Setup server-sent events on server that immediately sends a message to the client.
    server.setRoute("/sse", (req, res) -> {
      res.writeHead(200,
        "Content-Type", "text/event-stream",
        "Connection", "keep-alive",
        "Cache-Control", "no-cache");
      res.write("data: " + "{\"foo\":\"bar\"}" + "\n\n");
    });
    // 2. Subscribe to page request events.
    page.navigate(server.EMPTY_PAGE);
    List<Request> requests = new ArrayList<>();
    page.onRequest(request -> requests.add(request));
    // 3. Connect to EventSource in browser and return first message.
    assertEquals(SSE_MESSAGE, page.evaluate("() => {\n" +
      "  const eventSource = new EventSource('/sse');\n" +
      "  return new Promise(resolve => {\n" +
      "    eventSource.onmessage = e => resolve(JSON.parse(e.data));\n" +
      "  });\n" +
      "}"));
    assertEquals("eventsource", requests.get(0).resourceType());
  }

  @PlaywrightTest
  @DisplayName("NE-022 should return navigation bit")
  void shouldReturnNavigationBit(Page page, TestServer server) {
    Map<String, Request> requests = new HashMap<>();
    page.onRequest(request -> requests.put(request.url().substring(request.url().lastIndexOf('/') + 1), request));
    server.setRedirect("/rrredirect", "/frames/one-frame.html");
    page.navigate(server.PREFIX + "/rrredirect");
    assertEquals(true, requests.get("rrredirect").isNavigationRequest());
    assertEquals(true, requests.get("one-frame.html").isNavigationRequest());
    assertEquals(true, requests.get("frame.html").isNavigationRequest());
    assertEquals(false, requests.get("script.js").isNavigationRequest());
    assertEquals(false, requests.get("style.css").isNavigationRequest());
  }

  @PlaywrightTest
  @DisplayName("NE-023 should return navigation bit when navigating to image")
  void shouldReturnNavigationBitWhenNavigatingToImage(Page page, TestServer server) {
    List<Request> requests = new ArrayList<>();
    page.onRequest(request -> requests.add(request));
    page.navigate(server.PREFIX + "/pptr.png");
    assertEquals(true, requests.get(0).isNavigationRequest());
  }

  private static HttpHeader header(String name, String value) {
    HttpHeader header = new HttpHeader();
    header.name = name;
    header.value = value;
    return header;
  }

  private static List<String> sortedByName(List<HttpHeader> headers) {
    // Java: HttpHeader has no equals(), the headers are compared as "name: value" strings.
    Collator collator = Collator.getInstance();
    List<HttpHeader> sorted = new ArrayList<>(headers);
    sorted.sort((a, b) -> collator.compare(a.name, b.name));
    List<String> result = new ArrayList<>();
    for (HttpHeader h : sorted)
      result.add(h.name + ": " + h.value);
    return result;
  }

  @PlaywrightTest
  @DisplayName("NE-024 should report raw headers")
  void shouldReportRawHeaders(Page page, TestServer server, BrowserName browserName) {
    List<HttpHeader> expectedHeaders = new ArrayList<>();
    server.setRoute("/headers", (req, res) -> {
      expectedHeaders.clear();
      for (int i = 0; i < req.rawHeaders.size(); i += 2)
        expectedHeaders.add(header(req.rawHeaders.get(i), req.rawHeaders.get(i + 1)));
      if (browserName == BrowserName.WEBKIT && Env.platform().equals("win32") && !"webkit-wsl".equals(Env.channel())) {
        expectedHeaders.removeIf(h -> h.name.toLowerCase().equals("accept-encoding"));
        // Convert "value": "en-US, en-US" => "en-US"
        for (int i = 0; i < expectedHeaders.size(); i++) {
          HttpHeader e = expectedHeaders.get(i);
          if (!e.name.toLowerCase().equals("accept-language"))
            continue;
          String[] values = e.value.split(",");
          for (int j = 0; j < values.length; j++)
            values[j] = values[j].trim();
          if (values.length == 1)
            continue;
          if (!values[0].equals(values[1]))
            continue;
          expectedHeaders.set(i, header(e.name, values[0]));
        }
      }
      if (browserName == BrowserName.FIREFOX)
        expectedHeaders.removeIf(h -> h.name.toLowerCase().equals("priority"));

      res.end();
    });
    page.navigate(server.EMPTY_PAGE);
    Request request = page.waitForRequest("**/*", () -> {
      page.evaluate("() => fetch('/headers', {\n" +
        "  headers: [\n" +
        "    ['header-a', 'value-a'],\n" +
        "    ['header-b', 'value-b'],\n" +
        "    ['header-a', 'value-a-1'],\n" +
        "    ['header-a', 'value-a-2'],\n" +
        "  ]\n" +
        "})");
    });
    List<HttpHeader> headers = request.headersArray();
    assertEquals(sortedByName(expectedHeaders), sortedByName(headers));
    assertEquals("value-a, value-a-1, value-a-2", request.headerValue("header-a"));
    assertNull(request.headerValue("not-there"));
  }

  @PlaywrightTest
  @DisplayName("NE-025 should report raw response headers in redirects")
  @DisabledOnBrowser(value = BrowserName.WEBKIT, reason = "WebKit won't give us raw headers for redirects")
  void shouldReportRawResponseHeadersInRedirects(Page page, TestServer server) {
    server.setExtraHeaders("/redirect/1.html", Collections.singletonMap("sec-test-header", "1.html"));
    server.setExtraHeaders("/redirect/2.html", Collections.singletonMap("sec-test-header", "2.html"));
    server.setExtraHeaders("/empty.html", Collections.singletonMap("sec-test-header", "empty.html"));
    server.setRedirect("/redirect/1.html", "/redirect/2.html");
    server.setRedirect("/redirect/2.html", "/empty.html");

    List<String> expectedUrls = new ArrayList<>();
    for (String s : asList("/redirect/1.html", "/redirect/2.html", "/empty.html"))
      expectedUrls.add(server.PREFIX + s);
    List<String> expectedHeaders = asList("1.html", "2.html", "empty.html");

    Response response = page.navigate(server.PREFIX + "/redirect/1.html");
    List<String> redirectChain = new ArrayList<>();
    List<String> headersChain = new ArrayList<>();
    for (Request req = response.request(); req != null; req = req.redirectedFrom()) {
      // Java: unshift is List.add(0, ...).
      redirectChain.add(0, req.url());
      Response res = req.response();
      Map<String, String> headers = res.allHeaders();
      headersChain.add(0, headers.get("sec-test-header"));
    }

    assertEquals(expectedUrls, redirectChain);
    assertEquals(expectedHeaders, headersChain);
  }

  @PlaywrightTest
  @DisplayName("NE-026 should report all cookies in one header")
  void shouldReportAllCookiesInOneHeader(Page page, TestServer server) {
    Map<String, String> expectedHeaders = new ConcurrentHashMap<>();
    server.setRoute("/headers", (req, res) -> {
      for (int i = 0; i < req.rawHeaders.size(); i += 2)
        expectedHeaders.put(req.rawHeaders.get(i), req.rawHeaders.get(i + 1));
      res.end();
    });

    page.navigate(server.EMPTY_PAGE);
    page.evaluate("() => {\n" +
      "  document.cookie = 'myCookie=myValue';\n" +
      "  document.cookie = 'myOtherCookie=myOtherValue';\n" +
      "}");
    Response response = page.navigate(server.EMPTY_PAGE);
    String cookie = response.request().allHeaders().get("cookie");
    assertEquals("myCookie=myValue; myOtherCookie=myOtherValue", cookie);
  }

  @PlaywrightTest
  @DisplayName("NE-027 should not allow to access frame on popup main request")
  @Fixme(value = "java: response.finished() throws 'Frame for this navigation request is not available, because the request was issued before the frame is created.' (racy in WebKit, which passes sometimes)")
  void shouldNotAllowToAccessFrameOnPopupMainRequest(Page page, TestServer server) throws Exception {
    page.setContent("<a target=_blank href=\"" + server.EMPTY_PAGE + "\">click me</a>");
    // Java: context.waitForEvent('request') and context.waitForEvent('page') are futures completed by the
    // context events, awaited with waitForCondition() that dispatches the events.
    CompletableFuture<Request> requestPromise = new CompletableFuture<>();
    page.context().onRequest(r -> requestPromise.complete(r));
    CompletableFuture<Page> popupPromise = new CompletableFuture<>();
    page.context().onPage(p -> popupPromise.complete(p));
    // Java: the click is not left running on another thread, the Java client dispatches events on the caller's thread.
    page.getByText("click me").click();
    page.context().waitForCondition(() -> requestPromise.isDone());
    Request request = requestPromise.get();

    assertEquals(true, request.isNavigationRequest());

    PlaywrightException error = assertThrows(PlaywrightException.class, () -> request.frame());
    assertTrue(error.getMessage().contains("Frame for this navigation request is not available"));

    Response response = request.response();
    response.finished();
    page.context().waitForCondition(() -> popupPromise.isDone());
    popupPromise.get();
  }

  @PlaywrightTest
  @DisplayName("NE-028 page.reload return 304 status code")
  void pageReloadReturn304StatusCode(Page page, TestServer server, BrowserName browserName) {
    assumeFalse(browserName == BrowserName.FIREFOX && !Env.isBidi(), "fixme: Does not send second request");
    int[] requestNumber = {0};
    server.setRoute("/test.html", (req, res) -> {
      ++requestNumber[0];
      Map<String, String> headers = new LinkedHashMap<>();
      headers.put("cf-cache-status", "DYNAMIC");
      headers.put("Content-Type", "text/html;charset=UTF-8");
      headers.put("Last-Modified", "Fri, 05 Jan 2024 01:56:20 GMT");
      headers.put("Vary", "Access-Control-Request-Headers");
      if (requestNumber[0] == 1)
        res.writeHead(200, headers);
      else
        res.writeHead(304, headers);
      res.write("<div>Test</div>");
      res.end();
    });
    Response response1 = page.navigate(server.PREFIX + "/test.html");
    assertEquals(200, response1.status());
    Response response2 = page.reload();
    assertEquals(2, requestNumber[0]);
    if (browserName == BrowserName.CHROMIUM) {
      assertEquals(200, response2.status());
      assertEquals("OK", response2.statusText());
      assertEquals("<div>Test</div>", response2.text());
    } else {
      assertEquals(304, response2.status());
      assertEquals("Not Modified", response2.statusText());
    }
  }

  @PlaywrightTest
  @DisplayName("NE-029 should handle mixed-content blocked requests")
  @EnabledOnBrowser(value = BrowserName.CHROMIUM, reason = "FF and WK actually succeed with the request, and block afterwards")
  void shouldHandleMixedContentBlockedRequests(Page page) {
    page.route("**/mixedcontent.html", route -> {
      route.fulfill(new Route.FulfillOptions()
        .setStatus(200)
        .setContentType("text/html")
        .setBody("\n" +
          "        <!doctype html>\n" +
          "        <meta charset=\"utf-8\">\n" +
          "        <style>\n" +
          "        @font-face {\n" +
          "          font-family: 'pwtest-iconfont';\n" +
          "          src: url('http://another.com/iconfont.woff2') format('woff2');\n" +
          "        }\n" +
          "        body {\n" +
          "          font-family: 'pwtest-iconfont';\n" +
          "        }\n" +
          "        </style>\n" +
          "        <span>+-</span>\n" +
          "    "));
    });
    page.route("**/iconfont.woff2", route -> {
      byte[] body;
      try {
        body = Files.readAllBytes(asset("webfont/iconfont2.woff"));
      } catch (java.io.IOException e) {
        throw new RuntimeException(e);
      }
      route.fulfill(new Route.FulfillOptions().setBodyBytes(body));
    });

    // Java: waitForEvent('requestfailed', predicate) is a future completed by the page event.
    CompletableFuture<Request> failed = new CompletableFuture<>();
    page.onRequestFailed(r -> {
      if (r.url().contains("iconfont.woff2"))
        failed.complete(r);
    });
    page.navigate("https://example.com/mixedcontent.html");
    page.waitForCondition(() -> failed.isDone());
    Request request = failed.join();
    Map<String, String> headers = request.allHeaders();
    assertNotNull(headers.get("origin"));
    assertEquals("mixed-content", request.failure());
  }
}
