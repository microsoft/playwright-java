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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestInfo;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.microsoft.playwright.fixtures.Utils.asset;
import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/page-request-fulfill.spec.ts. */
public class TestPageRequestFulfill {
  // Java: rewriteAndroidLoopbackURL only changes the URL on Android, so the tests use the URL as is.

  @PlaywrightTest
  @DisplayName("RO-096 should work")
  void shouldWork(Page page, TestServer server) {
    page.route("**/*", route -> {
      route.fulfill(new Route.FulfillOptions()
        .setStatus(201)
        .setHeaders(Collections.singletonMap("foo", "bar"))
        .setContentType("text/html")
        .setBody("Yo, page!"));
    });
    Response response = page.navigate(server.EMPTY_PAGE);
    assertEquals(201, response.status());
    assertEquals("bar", response.headers().get("foo"));
    assertEquals("Yo, page!", page.evaluate("() => document.body.textContent"));
  }

  @PlaywrightTest
  @DisplayName("RO-097 should work with buffer as body")
  void shouldWorkWithBufferAsBody(Page page, TestServer server, BrowserName browserName) {
    page.route("**/*", route -> {
      route.fulfill(new Route.FulfillOptions()
        .setStatus(200)
        .setContentType("text/plain")
        .setBodyBytes("Yo, page!".getBytes(StandardCharsets.UTF_8)));
    });
    Response response = page.navigate(server.EMPTY_PAGE);
    assertEquals(200, response.status());
    assertEquals("Yo, page!", page.evaluate("() => document.body.textContent"));
  }

  @PlaywrightTest
  @DisplayName("RO-098 should work with status code 422")
  void shouldWorkWithStatusCode422(Page page, TestServer server) {
    page.route("**/*", route -> {
      route.fulfill(new Route.FulfillOptions()
        .setStatus(422)
        .setBody("Yo, page!"));
    });
    Response response = page.navigate(server.EMPTY_PAGE);
    assertEquals(422, response.status());
    assertEquals("Unprocessable Entity", response.statusText());
    assertEquals("Yo, page!", page.evaluate("() => document.body.textContent"));
  }

  @PlaywrightTest
  @DisplayName("RO-099 should fulfill with unuassigned status codes")
  void shouldFulfillWithUnuassignedStatusCodes(Page page, TestServer server, BrowserName browserName) throws Exception {
    // Java: route.fulfill() is synchronous, so the promise holds the error it threw, if any.
    CompletableFuture<PlaywrightException> fulfillPromise = new CompletableFuture<>();
    page.route("**/data.json", route -> {
      try {
        route.fulfill(new Route.FulfillOptions()
          .setStatus(430)
          .setBody("Yo, page!"));
        fulfillPromise.complete(null);
      } catch (PlaywrightException e) {
        fulfillPromise.complete(e);
      }
    });
    page.navigate(server.EMPTY_PAGE);
    Map<?, ?> response = (Map<?, ?>) page.evaluate("async url => {\n" +
      "  const { status, statusText } = await fetch(url);\n" +
      "  return { status, statusText };\n" +
      "}", server.PREFIX + "/data.json");
    PlaywrightException error = fulfillPromise.get();
    assertNull(error);
    assertEquals(430, response.get("status"));
    assertEquals("Unknown", response.get("statusText"));
  }

  @PlaywrightTest
  @DisplayName("RO-100 should not throw if request was cancelled by the page")
  void shouldNotThrowIfRequestWasCancelledByThePage(Page page, TestServer server) {
    // it.info().annotations.push({ type: 'issue', description: 'https://github.com/microsoft/playwright/issues/28490' });
    Route[] interceptedRoute = {null};
    page.route("**/data.json", route -> interceptedRoute[0] = route);
    page.navigate(server.EMPTY_PAGE);
    page.evaluate("url => {\n" +
      "  globalThis.controller = new AbortController();\n" +
      "  fetch(url, { signal: globalThis.controller.signal }).catch(() => {});\n" +
      "}", server.PREFIX + "/data.json");
    page.waitForCondition(() -> interceptedRoute[0] != null);
    Request[] cancelledRequest = {null};
    page.onRequestFailed(request -> cancelledRequest[0] = request);
    page.evaluate("() => globalThis.controller.abort()");
    page.waitForCondition(() -> cancelledRequest[0] != null);
    assertNotNull(cancelledRequest[0].failure());
    assertTrue(Pattern.compile("cancelled|aborted", Pattern.CASE_INSENSITIVE).matcher(cancelledRequest[0].failure()).find(), cancelledRequest[0].failure());
    interceptedRoute[0].fulfill(new Route.FulfillOptions().setStatus(200)); // Should not throw.
  }

  @PlaywrightTest
  @DisplayName("RO-101 should allow mocking binary responses")
  @DisabledOnBrowser(value = BrowserName.FIREFOX, headed = true, reason = "Firefox headed produces a different image.")
  void shouldAllowMockingBinaryResponses(Page page, TestServer server, BrowserName browserName, TestInfo testInfo) {
    page.route("**/*", route -> {
      byte[] imageBuffer = readFile(asset("pptr.png"));
      route.fulfill(new Route.FulfillOptions()
        .setContentType("image/png")
        .setBodyBytes(imageBuffer));
    });
    page.evaluate("PREFIX => {\n" +
      "  const img = document.createElement('img');\n" +
      "  img.src = PREFIX + '/does-not-exist.png';\n" +
      "  document.body.appendChild(img);\n" +
      "  return new Promise(fulfill => img.onload = fulfill);\n" +
      "}", server.PREFIX);
    ElementHandle img = page.querySelector("img");
    testInfo.expectToMatchSnapshot(img.screenshot(), "mock-binary-response.png");
  }

  @PlaywrightTest
  @DisplayName("RO-102 should allow mocking svg with charset")
  @DisabledOnBrowser(value = BrowserName.FIREFOX, headed = true, reason = "Firefox headed produces a different image.")
  void shouldAllowMockingSvgWithCharset(Page page, TestServer server, BrowserName browserName, TestInfo testInfo) {
    page.route("**/*", route -> {
      route.fulfill(new Route.FulfillOptions()
        .setContentType("image/svg+xml ; charset=utf-8")
        .setBody("<svg width=\"50\" height=\"50\" version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\"><rect x=\"10\" y=\"10\" width=\"30\" height=\"30\" stroke=\"black\" fill=\"transparent\" stroke-width=\"5\"/></svg>"));
    });
    page.evaluate("PREFIX => {\n" +
      "  const img = document.createElement('img');\n" +
      "  img.src = PREFIX + '/does-not-exist.svg';\n" +
      "  document.body.appendChild(img);\n" +
      "  return new Promise((f, r) => { img.onload = f; img.onerror = r; });\n" +
      "}", server.PREFIX);
    ElementHandle img = page.querySelector("img");
    testInfo.expectToMatchSnapshot(img.screenshot(), "mock-svg.png");
  }

  @PlaywrightTest
  @DisplayName("RO-103 should work with file path")
  void shouldWorkWithFilePath(Page page, TestServer server, TestInfo testInfo) {
    page.route("**/*", route -> route.fulfill(new Route.FulfillOptions().setContentType("shouldBeIgnored").setPath(asset("pptr.png"))));
    page.evaluate("PREFIX => {\n" +
      "  const img = document.createElement('img');\n" +
      "  img.src = PREFIX + '/does-not-exist.png';\n" +
      "  document.body.appendChild(img);\n" +
      "  return new Promise(fulfill => img.onload = fulfill);\n" +
      "}", server.PREFIX);
    ElementHandle img = page.querySelector("img");
    testInfo.expectToMatchSnapshot(img.screenshot(), "mock-binary-response.png");
  }

  @PlaywrightTest
  @DisplayName("RO-104 should stringify intercepted request response headers")
  void shouldStringifyInterceptedRequestResponseHeaders(Page page, TestServer server) {
    page.route("**/*", route -> {
      route.fulfill(new Route.FulfillOptions()
        .setStatus(200)
        .setHeaders(Collections.singletonMap("foo", "true"))
        .setBody("Yo, page!"));
    });
    Response response = page.navigate(server.EMPTY_PAGE);
    assertEquals(200, response.status());
    Map<String, String> headers = response.headers();
    assertEquals("true", headers.get("foo"));
    assertEquals("Yo, page!", page.evaluate("() => document.body.textContent"));
  }

  @PlaywrightTest
  @DisplayName("RO-105 should not modify the headers sent to the server")
  void shouldNotModifyTheHeadersSentToTheServer(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/empty.html");
    List<TestServer.Request> interceptedRequests = Collections.synchronizedList(new ArrayList<>());

    // this is just to enable request interception, which disables caching in chromium
    page.route(server.PREFIX + "/unused", route -> {});

    server.setRoute("/something", (request, response) -> {
      interceptedRequests.add(request);
      response.writeHead(200, "Access-Control-Allow-Origin", "*");
      response.end("done");
    });

    Object text = page.evaluate("async url => {\n" +
      "  const data = await fetch(url);\n" +
      "  return data.text();\n" +
      "}", server.CROSS_PROCESS_PREFIX + "/something");
    assertEquals("done", text);

    page.route(server.CROSS_PROCESS_PREFIX + "/something", route -> {
      route.resume(new Route.ResumeOptions().setHeaders(new LinkedHashMap<>(route.request().headers())));
    });

    Object textAfterRoute = page.evaluate("async url => {\n" +
      "  const data = await fetch(url);\n" +
      "  return data.text();\n" +
      "}", server.CROSS_PROCESS_PREFIX + "/something");
    assertEquals("done", textAfterRoute);

    assertEquals(2, interceptedRequests.size());
    assertEquals(interceptedRequests.get(0).headers, interceptedRequests.get(1).headers);
  }

  @PlaywrightTest
  @DisplayName("RO-106 should include the origin header")
  void shouldIncludeTheOriginHeader(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/empty.html");
    Request[] interceptedRequest = {null};
    page.route(server.CROSS_PROCESS_PREFIX + "/something", route -> {
      interceptedRequest[0] = route.request();
      route.fulfill(new Route.FulfillOptions()
        .setHeaders(Collections.singletonMap("Access-Control-Allow-Origin", "*"))
        .setContentType("text/plain")
        .setBody("done"));
    });

    Object text = page.evaluate("async url => {\n" +
      "  const data = await fetch(url);\n" +
      "  return data.text();\n" +
      "}", server.CROSS_PROCESS_PREFIX + "/something");
    assertEquals("done", text);
    assertEquals(server.PREFIX, interceptedRequest[0].headers().get("origin"));
  }

  @PlaywrightTest
  @DisplayName("RO-107 should fulfill with global fetch result")
  void shouldFulfillWithGlobalFetchResult(Playwright playwright, Page page, TestServer server) {
    page.route("**/*", route -> {
      APIRequestContext request = playwright.request().newContext();
      APIResponse response = request.get(server.PREFIX + "/simple.json");
      route.fulfill(new Route.FulfillOptions().setResponse(response));
    });
    Response response = page.navigate(server.EMPTY_PAGE);
    assertEquals(200, response.status());
    // Java: Response has no json().
    assertEquals(mapOf("foo", "bar"), new Gson().fromJson(response.text(), Map.class));
  }

  @PlaywrightTest
  @DisplayName("RO-108 should fulfill with fetch result")
  void shouldFulfillWithFetchResult(Page page, TestServer server) {
    page.route("**/*", route -> {
      APIResponse response = page.request().get(server.PREFIX + "/simple.json");
      route.fulfill(new Route.FulfillOptions().setResponse(response));
    });
    Response response = page.navigate(server.EMPTY_PAGE);
    assertEquals(200, response.status());
    // Java: Response has no json().
    assertEquals(mapOf("foo", "bar"), new Gson().fromJson(response.text(), Map.class));
  }

  @PlaywrightTest
  @DisplayName("RO-109 should fulfill with fetch result and overrides")
  void shouldFulfillWithFetchResultAndOverrides(Page page, TestServer server) {
    page.route("**/*", route -> {
      APIResponse response = page.request().get(server.PREFIX + "/simple.json");
      Map<String, String> headers = new LinkedHashMap<>();
      headers.put("Content-Type", "application/json"); // Case matters for the tested behavior
      headers.put("foo", "bar");
      route.fulfill(new Route.FulfillOptions()
        .setResponse(response)
        .setStatus(201)
        .setHeaders(headers));
    });
    Response response = page.navigate(server.EMPTY_PAGE);
    assertEquals(201, response.status());
    assertEquals("bar", response.allHeaders().get("foo"));
    // Java: Response has no json().
    assertEquals(mapOf("foo", "bar"), new Gson().fromJson(response.text(), Map.class));
  }

  @PlaywrightTest
  @DisplayName("RO-110 should fetch original request and fulfill")
  void shouldFetchOriginalRequestAndFulfill(Page page, TestServer server) {
    page.route("**/*", route -> {
      APIResponse response = page.request().fetch(route.request());
      route.fulfill(new Route.FulfillOptions().setResponse(response));
    });
    Response response = page.navigate(server.PREFIX + "/title.html");
    assertEquals(200, response.status());
    assertEquals("Woof-Woof", page.title());
  }

  // Java: .split(';').map(s => s.trim()).sort()
  private static List<String> splitTrimSort(String cookie) {
    List<String> result = new ArrayList<>();
    for (String s : cookie.split(";"))
      result.add(s.trim());
    Collections.sort(result);
    return result;
  }

  @PlaywrightTest
  @DisplayName("RO-111 should fulfill with multiple set-cookie")
  void shouldFulfillWithMultipleSetCookie(Page page, TestServer server) {
    List<String> cookies = Arrays.asList("a=b", "c=d");
    page.route("**/multiple-set-cookie.html", route -> {
      Map<String, String> headers = new LinkedHashMap<>();
      headers.put("X-Header-1", "v1");
      headers.put("Set-Cookie", String.join("\n", cookies));
      headers.put("X-Header-2", "v2");
      route.fulfill(new Route.FulfillOptions()
        .setStatus(200)
        .setHeaders(headers)
        .setBody(""));
    });
    Response response = page.navigate(server.PREFIX + "/multiple-set-cookie.html");
    assertEquals(cookies, splitTrimSort((String) page.evaluate("() => document.cookie")));
    assertEquals("v1", response.headerValue("X-Header-1"));
    assertEquals("v2", response.headerValue("X-Header-2"));
  }

  @PlaywrightTest
  @DisplayName("RO-113 headerValue should return set-cookie from intercepted response")
  @DisabledOnBrowser(value = BrowserName.CHROMIUM, reason = "fail: Set-Cookie is missing in response after interception")
  @DisabledOnBrowser(value = BrowserName.WEBKIT, reason = "Set-Cookie with \\n in intercepted response does not pass validation in WebCore, see also https://github.com/microsoft/playwright/pull/9273")
  void headerValueShouldReturnSetCookieFromInterceptedResponse(Page page, TestServer server, BrowserName browserName) {
    page.route("**/empty.html", route -> {
      route.fulfill(new Route.FulfillOptions()
        .setStatus(200)
        .setHeaders(Collections.singletonMap("Set-Cookie", "a=b"))
        .setBody(""));
    });
    Response response = page.navigate(server.EMPTY_PAGE);
    assertEquals("a=b", response.headerValue("Set-Cookie"));
  }

  @PlaywrightTest
  @DisplayName("RO-114 should fulfill with har response")
  void shouldFulfillWithHarResponse(Page page) throws IOException {
    Path harPath = asset("har-fulfill.har");
    JsonObject har = new Gson().fromJson(new String(Files.readAllBytes(harPath), StandardCharsets.UTF_8), JsonObject.class);
    page.route("**/*", route -> {
      JsonObject response = findResponse(har, route.request().url());
      Map<String, String> headers = new LinkedHashMap<>();
      for (JsonElement header : response.getAsJsonArray("headers")) {
        JsonObject h = header.getAsJsonObject();
        headers.put(h.get("name").getAsString(), h.get("value").getAsString());
      }
      JsonObject content = response.getAsJsonObject("content");
      String text = content.has("text") && !content.get("text").getAsString().isEmpty() ? content.get("text").getAsString() : "";
      String encoding = content.has("encoding") && !content.get("encoding").getAsString().isEmpty() ? content.get("encoding").getAsString() : "utf-8";
      // Java: Buffer.from(text, encoding)
      byte[] body = encoding.equals("base64") ? Base64.getDecoder().decode(text) : text.getBytes(StandardCharsets.UTF_8);
      route.fulfill(new Route.FulfillOptions()
        .setStatus(response.get("status").getAsInt())
        .setHeaders(headers)
        .setBodyBytes(body));
    });
    page.navigate("http://no.playwright/");
    // HAR contains a redirect for the script.
    assertEquals("foo", page.evaluate("window.value"));
    // HAR contains a POST for the css file but we match ignoring the method, so the file should be served.
    assertThat(page.locator("body")).hasCSS("background-color", "rgb(0, 255, 255)");
  }

  private static JsonObject findResponse(JsonObject har, String url) {
    JsonObject entry = null;
    String originalUrl = url;
    while (!url.trim().isEmpty()) {
      entry = null;
      for (JsonElement e : har.getAsJsonObject("log").getAsJsonArray("entries")) {
        if (e.getAsJsonObject().getAsJsonObject("request").get("url").getAsString().equals(url)) {
          entry = e.getAsJsonObject();
          break;
        }
      }
      url = entry == null ? null : entry.getAsJsonObject("response").get("redirectURL").getAsString();
    }
    assertNotNull(entry, originalUrl);
    return entry.getAsJsonObject("response");
  }

  @PlaywrightTest
  @DisplayName("RO-115 should fulfill preload link requests")
  void shouldFulfillPreloadLinkRequests(Page page, TestServer server, BrowserName browserName) {
    boolean[] intercepted = {false};
    page.route("**/one-style.css", route -> {
      intercepted[0] = true;
      Map<String, String> headers = new LinkedHashMap<>();
      headers.put("content-type", "text/css; charset=utf-8");
      headers.put("cache-control", "no-cache, no-store");
      headers.put("custom", "value");
      route.fulfill(new Route.FulfillOptions()
        .setStatus(200)
        .setHeaders(headers)
        .setBody("body { background-color: green; }"));
    });
    Response response = page.waitForResponse("**/one-style.css", () -> page.navigate(server.PREFIX + "/preload.html"));
    assertEquals("value", response.headerValue("custom"));
    page.waitForFunction("() => window.preloadedStyles");
    assertEquals(true, intercepted[0]);
    Object color = page.evaluate("() => window.getComputedStyle(document.body).backgroundColor");
    assertEquals("rgb(0, 128, 0)", color);
  }

  @PlaywrightTest
  @DisplayName("RO-117 should fulfill with gzip and readback")
  void shouldFulfillWithGzipAndReadback(Page page, TestServer server) {
    server.enableGzip("/one-style.html");
    page.route("**/one-style.html", route -> {
      APIResponse response = route.fetch();
      assertEquals("gzip", response.headers().get("content-encoding"));
      route.fulfill(new Route.FulfillOptions().setResponse(response));
    });

    Response response = page.navigate(server.PREFIX + "/one-style.html");
    assertThat(page.locator("div")).hasText("hello, world!");
    assertThat(page.locator("body")).hasCSS("background-color", "rgb(255, 192, 203)");
    assertTrue(response.text().contains("<div>hello, world!</div>"));
  }

  @PlaywrightTest
  @DisplayName("RO-118 should not go to the network for fulfilled requests body")
  void shouldNotGoToTheNetworkForFulfilledRequestsBody(Page page, TestServer server) {
    page.route("**/one-style.css", route -> {
      route.fulfill(new Route.FulfillOptions()
        .setStatus(404)
        .setContentType("text/plain")
        .setBody("Not Found! (mocked)"));
    });

    AtomicBoolean serverHit = new AtomicBoolean(false);
    server.setRoute("/one-style.css", (req, res) -> {
      serverHit.set(true);
      res.setHeader("Content-Type", "text/css");
      res.end("body { background-color: green; }");
    });

    Response response = page.waitForResponse("**/one-style.css", () -> page.navigate(server.PREFIX + "/one-style.html"));
    byte[] body = response.body();
    assertEquals("Not Found! (mocked)", new String(body, StandardCharsets.UTF_8));
    assertEquals(false, serverHit.get());
  }

  @PlaywrightTest
  @DisplayName("RO-119 should return body for fulfilled responses")
  void shouldReturnBodyForFulfilledResponses(Page page, TestServer server) {
    for (int status : new int[]{100, 200, 404, 500}) {
      // it.step(`status ${status}`)
      String bodyOverride = "Custom body " + status;
      page.route("**/one-style.css", route -> {
        route.fulfill(new Route.FulfillOptions()
          .setStatus(status)
          .setContentType("text/plain")
          .setBody(bodyOverride));
      });

      Response response = page.waitForResponse("**/one-style.css", () -> page.navigate(server.PREFIX + "/one-style.html"));
      byte[] body = response.body();
      assertEquals(bodyOverride, new String(body, StandardCharsets.UTF_8));
      page.unrouteAll();
    }
  }

  private static byte[] readFile(Path path) {
    try {
      return Files.readAllBytes(path);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
