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
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.HttpHeader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.condition.OS;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import static com.microsoft.playwright.fixtures.Utils.async;
import static com.microsoft.playwright.fixtures.Utils.asset;
import static com.microsoft.playwright.fixtures.Utils.browserMajorVersion;
import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/page/page-network-response.spec.ts. */
public class TestPageNetworkResponse {
  @PlaywrightTest
  @DisplayName("NE-030 should work @smoke")
  void shouldWork(Page page, TestServer server) {
    server.setRoute("/empty.html", (req, res) -> {
      res.setHeader("foo", "bar");
      res.setHeader("BaZ", "bAz");
      res.end();
    });
    Response response = page.navigate(server.EMPTY_PAGE);
    assertEquals("bar", response.allHeaders().get("foo"));
    assertEquals("bAz", response.allHeaders().get("baz"));
    assertNull(response.allHeaders().get("BaZ"));
  }

  @PlaywrightTest
  @DisplayName("NE-031 should return multiple header value")
  @DisabledOnBrowser(value = BrowserName.WEBKIT, os = OS.WINDOWS, reason = "libcurl does not support non-set-cookie multivalue headers")
  void shouldReturnMultipleHeaderValue(Page page, TestServer server) {
    server.setRoute("/headers", (req, res) -> {
      // Headers array is only supported since Node v14.14.0 so we write directly to the socket.
      // res.writeHead(200, ['name-a', 'v1','name-b', 'v4','Name-a', 'v2', 'name-A', 'v3']);
      TestServer.Connection conn = res.socket();
      conn.write("HTTP/1.1 200 OK\r\n");
      conn.write("Name-A: v1\r\n");
      conn.write("Name-a: v2\r\n");
      conn.write("name-A: v3\r\n");
      conn.write("\r\n");
      conn.uncork();
      conn.end();
    });
    Response response = page.navigate(server.PREFIX + "/headers");
    assertEquals(200, response.status());
    assertEquals("v1, v2, v3", response.headers().get("name-a"));
  }

  @PlaywrightTest
  @DisplayName("NE-032 should return text")
  void shouldReturnText(Page page, TestServer server) {
    Response response = page.navigate(server.PREFIX + "/simple.json");
    assertEquals("{\"foo\": \"bar\"}\n", response.text());
  }

  @PlaywrightTest
  @DisplayName("NE-033 should return uncompressed text")
  void shouldReturnUncompressedText(Page page, TestServer server) {
    server.enableGzip("/simple.json");
    Response response = page.navigate(server.PREFIX + "/simple.json");
    assertEquals("gzip", response.headers().get("content-encoding"));
    assertEquals("{\"foo\": \"bar\"}\n", response.text());
  }

  @PlaywrightTest
  @DisplayName("NE-034 should return uncompressed text for brotli encoding")
  void shouldReturnUncompressedTextForBrotliEncoding(Page page, TestServer server) {
    String text = "{\"foo\": \"bar\"}\n";
    // Java: the JDK has no brotli encoder, these are the bytes of zlib.brotliCompressSync(Buffer.from(text)).
    byte[] compressed = {11, 7, -128, 123, 34, 102, 111, 111, 34, 58, 32, 34, 98, 97, 114, 34, 125, 10, 3};
    server.setRoute("/brotli.json", (req, res) -> {
      res.writeHead(200,
        "Content-Type", "application/json",
        "Content-Encoding", "br");
      res.end(compressed);
    });
    Response response = page.navigate(server.PREFIX + "/brotli.json");
    assertEquals("br", response.headers().get("content-encoding"));
    assertEquals(text, response.text());
  }

  @PlaywrightTest
  @DisplayName("NE-035 should return text for identity encoding")
  void shouldReturnTextForIdentityEncoding(Page page, TestServer server) {
    String text = "<div>hello</div>";
    server.setRoute("/identity.html", (req, res) -> {
      res.writeHead(200,
        "Content-Type", "text/html; charset=utf-8",
        "Content-Encoding", "Identity");
      res.end(text);
    });
    Response response = page.navigate(server.PREFIX + "/identity.html");
    assertEquals("Identity", response.headers().get("content-encoding"));
    assertEquals(text, response.text());
  }

  @PlaywrightTest
  @DisplayName("NE-036 should throw when requesting body of redirected response")
  void shouldThrowWhenRequestingBodyOfRedirectedResponse(Page page, TestServer server) {
    server.setRedirect("/foo.html", "/empty.html");
    Response response = page.navigate(server.PREFIX + "/foo.html");
    Request redirectedFrom = response.request().redirectedFrom();
    assertNotNull(redirectedFrom);
    Response redirected = redirectedFrom.response();
    assertEquals(302, redirected.status());
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> redirected.text());
    assertTrue(error.getMessage().contains("Response body is unavailable for redirect responses"));
  }

  @PlaywrightTest
  @DisplayName("NE-037 should wait until response completes")
  void shouldWaitUntilResponseCompletes(Page page, TestServer server) throws Exception {
    page.navigate(server.EMPTY_PAGE);
    // Setup server to trap request.
    AtomicReference<TestServer.Response> serverResponse = new AtomicReference<>();
    server.setRoute("/get", (req, res) -> {
      serverResponse.set(res);
      // In Firefox, |fetch| will be hanging until it receives |Content-Type| header
      // from server.
      res.setHeader("Content-Type", "text/plain; charset=utf-8");
      res.write("hello ");
    });
    // Setup page to trap response.
    boolean[] requestFinished = {false};
    page.onRequestFinished(r -> requestFinished[0] = requestFinished[0] || r.url().contains("/get"));
    // send request and wait for server response
    CompletableFuture<TestServer.Request> serverRequest = server.waitForRequest("/get");
    // Java: waitForEvent('response') without a predicate is waitForResponse with a predicate that accepts all.
    Response pageResponse = page.waitForResponse(r -> true, () -> {
      page.evaluate("() => fetch('./get', { method: 'GET' })");
    });
    serverRequest.get();

    assertNotNull(serverResponse.get());
    assertNotNull(pageResponse);
    assertEquals(200, pageResponse.status());
    assertEquals(false, requestFinished[0]);

    CompletableFuture<String> responseText = async(() -> pageResponse.text());
    // Write part of the response and wait for it to be flushed.
    serverResponse.get().write("wor");
    // Finish response.
    serverResponse.get().end("ld!");
    assertEquals("hello world!", responseText.get());
  }

  @PlaywrightTest
  @DisplayName("NE-039 should return body")
  void shouldReturnBody(Page page, TestServer server) throws Exception {
    Response response = page.navigate(server.PREFIX + "/pptr.png");
    byte[] imageBuffer = Files.readAllBytes(asset("pptr.png"));
    byte[] responseBuffer = response.body();
    assertEquals(true, Arrays.equals(responseBuffer, imageBuffer));
  }

  @PlaywrightTest
  @DisplayName("NE-040 should return body with compression")
  void shouldReturnBodyWithCompression(Page page, TestServer server) throws Exception {
    server.enableGzip("/pptr.png");
    Response response = page.navigate(server.PREFIX + "/pptr.png");
    byte[] imageBuffer = Files.readAllBytes(asset("pptr.png"));
    byte[] responseBuffer = response.body();
    assertEquals(true, Arrays.equals(responseBuffer, imageBuffer));
  }

  @PlaywrightTest
  @DisplayName("NE-041 should return non-utf8 body even when content-type says utf8")
  @DisabledOnBrowser(value = BrowserName.WEBKIT, reason = "fixme: webkit encodes the response body")
  @Fixme(value = "java: page.evaluate() fails with 'Unexpected result: {}' for a Uint8Array result", browsers = {BrowserName.CHROMIUM, BrowserName.FIREFOX})
  void shouldReturnNonUtf8BodyEvenWhenContentTypeSaysUtf8(Page page, TestServer server, BrowserName browserName, Browser browser) {
    assumeFalse(browserName == BrowserName.CHROMIUM && browserMajorVersion(browser) < 151,
      "fixme: older chromium re-encodes the non-utf8 response body and lacks Response.bytes()");

    // Binary data with bytes that are invalid UTF-8.
    // Java: bytes are signed.
    byte[] bytes = {(byte) 0x80, (byte) 0x81, (byte) 0x82, (byte) 0xFF, (byte) 0xFE, 0x00, 0x01, 0x02};
    byte[] buffer = bytes.clone();
    server.setRoute("/binary-as-text", (req, res) -> {
      res.writeHead(200,
        "Content-Type", "text/plain;charset=UTF-8",
        "Content-Length", String.valueOf(buffer.length));
      res.end(buffer);
    });
    page.navigate(server.EMPTY_PAGE);
    Object[] bytesReceived = new Object[1];
    Response response = page.waitForResponse(server.PREFIX + "/binary-as-text", () -> {
      bytesReceived[0] = page.evaluate("url => fetch(url).then(r => r.bytes())", server.PREFIX + "/binary-as-text");
    });
    byte[] body = response.body();
    assertEquals(true, Arrays.equals(body, buffer));
    assertTrue(Arrays.equals(bytes, (byte[]) bytesReceived[0]));
  }

  @PlaywrightTest
  @DisplayName("NE-042 should return status text")
  void shouldReturnStatusText(Page page, TestServer server) {
    server.setRoute("/cool", (req, res) -> {
      res.writeHead(200, "cool!", Collections.<String, String>emptyMap());
      res.end();
    });
    Response response = page.navigate(server.PREFIX + "/cool");
    assertEquals("cool!", response.statusText());
  }

  @PlaywrightTest
  @DisplayName("NE-043 should report all headers")
  @DisabledOnBrowser(value = BrowserName.WEBKIT, os = OS.WINDOWS, reason = "libcurl does not support non-set-cookie multivalue headers")
  void shouldReportAllHeaders(Page page, TestServer server) {
    Map<String, List<String>> expectedHeaders = new LinkedHashMap<>();
    expectedHeaders.put("header-a", Arrays.asList("value-a", "value-a-1", "value-a-2"));
    expectedHeaders.put("header-b", Arrays.asList("value-b"));
    server.setRoute("/headers", (req, res) -> {
      res.writeHead(200);
      // Java: writeHead() takes no array values, set them one by one.
      for (Map.Entry<String, List<String>> header : expectedHeaders.entrySet())
        res.setHeader(header.getKey(), header.getValue());
      // Java: Node's writeHead() sends the headers at once, so end() adds no Content-Length.
      res.flushHeaders();
      res.end();
    });

    page.navigate(server.EMPTY_PAGE);
    Response response = page.waitForResponse("**/*", () -> {
      page.evaluate("() => fetch('/headers')");
    });
    List<HttpHeader> headers = response.headersArray();
    Map<String, List<String>> actualHeaders = new LinkedHashMap<>();
    for (HttpHeader header : headers) {
      if (!actualHeaders.containsKey(header.name))
        actualHeaders.put(header.name, new ArrayList<>());
      actualHeaders.get(header.name).add(header.value);
    }
    actualHeaders.remove("Keep-Alive");
    actualHeaders.remove("keep-alive");
    actualHeaders.remove("Connection");
    actualHeaders.remove("connection");
    actualHeaders.remove("Date");
    actualHeaders.remove("date");
    actualHeaders.remove("Transfer-Encoding");
    actualHeaders.remove("transfer-encoding");
    assertEquals(expectedHeaders, actualHeaders);
  }

  @PlaywrightTest
  @DisplayName("NE-044 should report multiple set-cookie headers")
  void shouldReportMultipleSetCookieHeaders(Page page, TestServer server) {
    server.setRoute("/headers", (req, res) -> {
      res.writeHead(200);
      // Java: writeHead() takes no array values, set them one by one.
      res.setHeader("Set-Cookie", Arrays.asList("a=b", "c=d"));
      res.write("\r\n");
      res.end();
    });

    page.navigate(server.EMPTY_PAGE);
    Response response = page.waitForResponse("**/*", () -> {
      page.evaluate("() => fetch('/headers')");
    });
    List<HttpHeader> headers = response.headersArray();
    List<String> cookies = new ArrayList<>();
    for (HttpHeader header : headers) {
      if (header.name.toLowerCase().equals("set-cookie"))
        cookies.add(header.value);
    }
    assertEquals(Arrays.asList("a=b", "c=d"), cookies);
    assertNull(response.headerValue("not-there"));
    assertEquals("a=b\nc=d", response.headerValue("set-cookie"));
    assertEquals(Arrays.asList("a=b", "c=d"), response.headerValues("set-cookie"));
  }

  @PlaywrightTest
  @DisplayName("NE-045 should behave the same way for headers and allHeaders")
  @DisabledOnBrowser(value = BrowserName.WEBKIT, os = OS.WINDOWS, reason = "libcurl does not support non-set-cookie multivalue headers")
  void shouldBehaveTheSameWayForHeadersAndAllHeaders(Page page, TestServer server, BrowserName browserName) {
    server.setRoute("/headers", (req, res) -> {
      Map<String, List<String>> headers = new LinkedHashMap<>();
      headers.put("Set-Cookie", Arrays.asList("a=b", "c=d"));
      headers.put("header-a", Arrays.asList("a=b", "c=d"));
      headers.put("Name-A", Arrays.asList("v1"));
      headers.put("name-b", Arrays.asList("v4"));
      headers.put("Name-a", Arrays.asList("v2"));
      headers.put("name-A", Arrays.asList("v3"));
      // Chromium does not report set-cookie headers immediately, so they are missing from .headers()
      if (browserName == BrowserName.CHROMIUM)
        headers.remove("Set-Cookie");

      // Java: TestServer.Response.setHeader() replaces a header of the same name in any case, but Node sends
      // 'Name-A', 'Name-a' and 'name-A' as three fields, so the response goes to the socket.
      TestServer.Connection conn = res.socket();
      conn.write("HTTP/1.1 200 OK\r\n");
      for (Map.Entry<String, List<String>> header : headers.entrySet()) {
        for (String value : header.getValue())
          conn.write(header.getKey() + ": " + value + "\r\n");
      }
      conn.write("Transfer-Encoding: chunked\r\n");
      conn.write("\r\n");
      conn.write("2\r\n\r\n\r\n");
      conn.write("0\r\n\r\n");
      conn.uncork();
      conn.end();
    });

    page.navigate(server.EMPTY_PAGE);
    Response response = page.waitForResponse("**/*", () -> {
      page.evaluate("() => fetch('/headers')");
    });
    Map<String, String> allHeaders = response.allHeaders();
    assertEquals(allHeaders, response.headers());
    assertEquals("a=b, c=d", allHeaders.get("header-a"));
    assertEquals("v1, v2, v3", allHeaders.get("name-a"));
    assertEquals("v4", allHeaders.get("name-b"));
  }

  @PlaywrightTest
  @DisplayName("NE-046 should provide a Response with a file URL")
  @DisabledOnBrowser(value = BrowserName.FIREFOX, reason = "Firefox does return null for file:// URLs")
  void shouldProvideAResponseWithAFileURL(Page page, BrowserName browserName) {
    assumeFalse("webkit-wsl".equals(Env.channel()));

    String fileurl = asset("frames/two-frames.html").toUri().toString();
    Response response = page.navigate(fileurl);
    if (browserName == BrowserName.CHROMIUM || (browserName == BrowserName.WEBKIT && Env.isWindows))
      assertEquals(200, response.status());
    else
      assertEquals(0, response.status());
    assertEquals(true, response.ok());
  }

  @PlaywrightTest
  @DisplayName("NE-047 should return set-cookie header after route.fulfill")
  @DisabledOnBrowser(value = {BrowserName.WEBKIT, BrowserName.CHROMIUM}, reason = "fail: https://github.com/microsoft/playwright/issues/11035")
  void shouldReturnSetCookieHeaderAfterRouteFulfill(Page page, TestServer server) {
    page.route("**/*", route -> {
      route.fulfill(new Route.FulfillOptions()
        .setStatus(200)
        .setHeaders(Collections.singletonMap("set-cookie", "a=b"))
        .setContentType("text/plain")
        .setBody(""));
    });
    Response response = page.navigate(server.EMPTY_PAGE);
    Map<String, String> headers = response.allHeaders();
    assertEquals("a=b", headers.get("set-cookie"));
  }

  @PlaywrightTest
  @DisplayName("NE-048 should return headers after route.fulfill")
  void shouldReturnHeadersAfterRouteFulfill(Page page, TestServer server) {
    page.route("**/*", route -> {
      Map<String, String> headers = new LinkedHashMap<>();
      headers.put("foo", "bar");
      headers.put("content-language", "en");
      route.fulfill(new Route.FulfillOptions()
        .setStatus(200)
        .setHeaders(headers)
        .setContentType("text/plain")
        .setBody("done"));
    });
    Response response = page.navigate(server.EMPTY_PAGE);
    assertEquals(mapOf(
      "foo", "bar",
      "content-type", "text/plain",
      "content-length", "4",
      "content-language", "en"), response.allHeaders());
  }

  @PlaywrightTest
  @DisplayName("NE-049 should report if request was fromServiceWorker")
  void shouldReportIfRequestWasFromServiceWorker(Page page, TestServer server) {
    {
      Response res = page.navigate(server.PREFIX + "/serviceworkers/fetch/sw.html");
      assertEquals(false, res.fromServiceWorker());
    }
    page.evaluate("() => window['activationPromise']");
    {
      Response res = page.waitForResponse(Pattern.compile("example\\.txt"), () -> {
        page.evaluate("() => fetch('/example.txt')");
      });
      assertEquals(true, res.fromServiceWorker());
    }
  }

  @PlaywrightTest
  @DisplayName("NE-050 should return body for prefetch script")
  @DisabledOnBrowser(value = BrowserName.WEBKIT, reason = "No prefetch in WebKit: https://caniuse.com/link-rel-prefetch")
  void shouldReturnBodyForPrefetchScript(Page page, TestServer server, BrowserName browserName, Browser browser) {
    assumeFalse(browserName == BrowserName.CHROMIUM && browserMajorVersion(browser) < 138, "Requires Sec-Purpose header, shipped in Chrome 138");
    Response response = page.waitForResponse("**/prefetch.js", () -> {
      page.navigate(server.PREFIX + "/prefetch.html");
    });
    byte[] body = response.body();
    assertEquals("// Scripts will be pre-fetched", new String(body, StandardCharsets.UTF_8));
  }

  @PlaywrightTest
  @DisplayName("NE-051 should return body for image with evicted body")
  @DisabledOnBrowser(value = BrowserName.WEBKIT, os = OS.MAC, reason = "fixme: WebKit on Mac evicts the body and returns empty buffer")
  void shouldReturnBodyForImageWithEvictedBody(Page page, TestServer server) {
    String imageBase64 = "R0lGODlhAQABAAAAACw="; // truncated 1x1 gif, Chromium evicts its body
    server.setRoute("/pixel.gif", (req, res) -> {
      res.setHeader("content-type", "image/gif");
      res.end(Base64.getDecoder().decode(imageBase64));
    });
    server.setRoute("/page.html", (req, res) -> {
      res.setHeader("content-type", "text/html");
      res.end("<html><body><img src=\"/pixel.gif\"></body></html>");
    });
    Response response = page.waitForResponse("**/pixel.gif", () -> {
      page.navigate(server.PREFIX + "/page.html");
    });
    byte[] body = response.body();
    assertEquals(imageBase64, Base64.getEncoder().encodeToString(body));
  }

  @PlaywrightTest
  @DisplayName("NE-052 should bypass disk cache when page interception is enabled")
  void shouldBypassDiskCacheWhenPageInterceptionIsEnabled(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/frames/one-frame.html");
    page.route("**/api*", route -> route.resume());
    {
      List<TestServer.Request> requests = Collections.synchronizedList(new ArrayList<>());
      server.setRoute("/api", (req, res) -> {
        requests.add(req);
        res.statusCode = 200;
        res.setHeader("content-type", "text/plain");
        res.setHeader("cache-control", "public, max-age=31536000");
        res.end("Hello");
      });
      for (int i = 0; i < 3; i++) {
        // it.step(`main frame iteration ${i}`)
        Response response = page.waitForResponse("**/api", () -> {
          page.evaluate("async () => {\n" +
            "  const response = await fetch('/api');\n" +
            "  return response.status;\n" +
            "}");
        });
        assertEquals(200, response.status());
        assertEquals(i + 1, requests.size());
      }
    }

    {
      List<TestServer.Request> requests = Collections.synchronizedList(new ArrayList<>());
      server.setRoute("/frame/api", (req, res) -> {
        requests.add(req);
        res.statusCode = 200;
        res.setHeader("content-type", "text/plain");
        res.setHeader("cache-control", "public, max-age=31536000");
        res.end("Hello");
      });
      for (int i = 0; i < 3; i++) {
        // it.step(`subframe iteration ${i}`)
        Response response = page.waitForResponse("**/frame/api", () -> {
          page.frameByUrl("**/frame.html").evaluate("async () => {\n" +
            "  const response = await fetch('/frame/api');\n" +
            "  return response.status;\n" +
            "}");
        });
        assertEquals(200, response.status());
        assertEquals(i + 1, requests.size());
      }
    }
  }

  @PlaywrightTest
  @DisplayName("NE-053 request.existingResponse should return null before response is received")
  void requestExistingResponseShouldReturnNullBeforeResponseIsReceived(Page page, TestServer server) throws Exception {
    page.navigate(server.EMPTY_PAGE);
    AtomicReference<TestServer.Response> serverResponse = new AtomicReference<>();
    server.setRoute("/get", (req, res) -> {
      serverResponse.set(res);
      // Don't end the response yet
    });

    CompletableFuture<TestServer.Request> serverRequest = server.waitForRequest("/get");
    // Java: waitForEvent('request') without a predicate is waitForRequest with a predicate that accepts all.
    Request request = page.waitForRequest(r -> true, () -> {
      page.evaluate("() => { void fetch('./get', { method: 'GET' }); }");
    });
    serverRequest.get();

    // Response hasn't been received yet
    assertNull(request.existingResponse());

    // Now send the response
    // Java: the response event is awaited before the server answers, the page can not miss it.
    page.waitForResponse(r -> true, () -> {
      serverResponse.get().setHeader("Content-Type", "text/plain; charset=utf-8");
      serverResponse.get().end("done");
    });

    // After response is received, existingResponse should return the response
    Response existingResponse = request.existingResponse();
    assertNotNull(existingResponse);
    assertEquals(200, existingResponse.status());
  }

  @PlaywrightTest
  @DisplayName("NE-054 request.existingResponse should return the response after it is received")
  void requestExistingResponseShouldReturnTheResponseAfterItIsReceived(Page page, TestServer server) {
    Response response = page.navigate(server.EMPTY_PAGE);
    Request request = response.request();
    assertEquals(response, request.existingResponse());
  }

  @PlaywrightTest
  @DisplayName("NE-055 should return http version")
  void shouldReturnHttpVersion(Page page, TestServer server) {
    Response response = page.navigate(server.EMPTY_PAGE);
    assertEquals("HTTP/1.1", response.httpVersion());
  }

  @PlaywrightTest
  @DisplayName("NE-056 Response.formData() should parse multipart/form-data in page context")
  void responseFormDataShouldParseMultipartFormDataInPageContext(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    @SuppressWarnings("unchecked")
    Map<String, Object> result = (Map<String, Object>) page.evaluate("async () => {\n" +
      "  const boundary = '----WebKitFormBoundary1234';\n" +
      "  const body = [\n" +
      "    `--${boundary}`,\n" +
      "    'Content-Disposition: form-data; name=\"field1\"',\n" +
      "    '',\n" +
      "    'value1',\n" +
      "    `--${boundary}`,\n" +
      "    'Content-Disposition: form-data; name=\"file1\"; filename=\"test.txt\"',\n" +
      "    'Content-Type: text/plain',\n" +
      "    '',\n" +
      "    'hello',\n" +
      "    `--${boundary}--`,\n" +
      "  ].join('\\r\\n');\n" +
      "  const response = new Response(body, {\n" +
      "    headers: { 'Content-Type': `multipart/form-data; boundary=${boundary}` },\n" +
      "  });\n" +
      "  const fd = await response.formData();\n" +
      "  const file = fd.get('file1');\n" +
      "  return {\n" +
      "    field1: fd.get('field1'),\n" +
      "    filename: file instanceof File ? file.name : null,\n" +
      "    fileContent: file instanceof File ? await file.text() : null,\n" +
      "  };\n" +
      "}");
    assertEquals("value1", result.get("field1"));
    assertEquals("test.txt", result.get("filename"));
    assertEquals("hello", result.get("fileContent"));
  }

  @PlaywrightTest
  @DisplayName("NE-057 should give a readable error when response.body() races with navigation")
  @DisabledOnBrowser(value = BrowserName.FIREFOX, reason = "Firefox keeps the response body available after navigating away, so it never throws")
  void shouldGiveAReadableErrorWhenResponseBodyRacesWithNavigation(Page page, TestServer server) {
    Response response = page.waitForResponse(server.PREFIX + "/title.html", () -> {
      page.navigate(server.PREFIX + "/title.html");
    });
    // Navigate away — the browser frees the network resource from the first page load.
    // The first page must have a non-empty body, otherwise WebKit returns an empty buffer instead of throwing.
    page.navigate(server.PREFIX + "/grid.html");
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> response.body());
    assertTrue(error.getMessage().contains("navigated away"));
  }
}
