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

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.HttpHeader;
import org.junit.jupiter.api.DisplayName;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static com.microsoft.playwright.fixtures.Utils.asset;
import static com.microsoft.playwright.fixtures.Utils.browserMajorVersion;
import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static com.microsoft.playwright.fixtures.Utils.relativeToCwd;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/page/page-request-intercept.spec.ts. */
public class TestPageRequestIntercept {
  // Java: rewriteAndroidLoopbackURL only changes the URL on Android, so the tests use the URL as is.

  @PlaywrightTest
  @DisplayName("RO-134 should fulfill intercepted response")
  void shouldFulfillInterceptedResponse(Page page, TestServer server) {
    page.route("**/*", route -> {
      APIResponse response = page.request().fetch(route.request());
      route.fulfill(new Route.FulfillOptions()
        .setResponse(response)
        .setStatus(201)
        .setHeaders(Collections.singletonMap("foo", "bar"))
        .setContentType("text/plain")
        .setBody("Yo, page!"));
    });
    Response response = page.navigate(server.PREFIX + "/empty.html");
    assertEquals(201, response.status());
    assertEquals("bar", response.headers().get("foo"));
    assertEquals("text/plain", response.headers().get("content-type"));
    assertEquals("Yo, page!", page.evaluate("() => document.body.textContent"));
  }

  @PlaywrightTest
  @DisplayName("RO-135 should fulfill response with empty body")
  void shouldFulfillResponseWithEmptyBody(Page page, TestServer server, BrowserName browserName, Browser browser) {
    assumeFalse(browserName == BrowserName.CHROMIUM && browserMajorVersion(browser) <= 91, "Fails in Electron that uses old Chromium");
    page.route("**/*", route -> {
      APIResponse response = page.request().fetch(route.request());
      route.fulfill(new Route.FulfillOptions()
        .setResponse(response)
        .setHeaders(Collections.singletonMap("content-length", "0"))
        .setStatus(201)
        .setBody(""));
    });
    Response response = page.navigate(server.PREFIX + "/title.html");
    assertEquals(201, response.status());
    assertEquals("", response.text());
  }

  @PlaywrightTest
  @DisplayName("RO-136 should override with defaults when intercepted response not provided")
  void shouldOverrideWithDefaultsWhenInterceptedResponseNotProvided(Page page, TestServer server, BrowserName browserName) {
    server.setRoute("/empty.html", (req, res) -> {
      res.setHeader("foo", "bar");
      res.end("my content");
    });
    page.route("**/*", route -> {
      page.request().fetch(route.request());
      route.fulfill(new Route.FulfillOptions().setStatus(201));
    });
    Response response = page.navigate(server.EMPTY_PAGE);
    assertEquals(201, response.status());
    assertEquals("", response.text());
    if (browserName == BrowserName.WEBKIT)
      assertEquals(mapOf("content-type", "text/plain"), response.headers());
    else
      assertEquals(mapOf(), response.headers());
  }

  @PlaywrightTest
  @DisplayName("RO-137 should fulfill with any response")
  void shouldFulfillWithAnyResponse(Page page, TestServer server) {
    server.setRoute("/sample", (req, res) -> {
      res.setHeader("foo", "bar");
      res.end("Woo-hoo");
    });
    APIResponse sampleResponse = page.request().get(server.PREFIX + "/sample");

    page.route("**/*", route -> {
      route.fulfill(new Route.FulfillOptions()
        .setResponse(sampleResponse)
        .setStatus(201)
        .setContentType("text/plain"));
    });
    Response response = page.navigate(server.EMPTY_PAGE);
    assertEquals(201, response.status());
    assertEquals("Woo-hoo", response.text());
    assertEquals("bar", response.headers().get("foo"));
  }

  @PlaywrightTest
  @DisplayName("RO-138 should support fulfill after intercept")
  void shouldSupportFulfillAfterIntercept(Page page, TestServer server) throws Exception {
    CompletableFuture<TestServer.Request> requestPromise = server.waitForRequest("/title.html");
    page.route("**", route -> {
      APIResponse response = page.request().fetch(route.request());
      route.fulfill(new Route.FulfillOptions().setResponse(response));
    });
    Response response = page.navigate(server.PREFIX + "/title.html");
    TestServer.Request request = requestPromise.get();
    assertEquals("/title.html", request.url);
    String original = new String(Files.readAllBytes(asset("title.html")), StandardCharsets.UTF_8);
    assertEquals(original, response.text());
  }

  @PlaywrightTest
  @DisplayName("RO-139 should give access to the intercepted response")
  void shouldGiveAccessToTheInterceptedResponse(Page page, TestServer server) throws Exception {
    page.navigate(server.EMPTY_PAGE);

    // Java: the route handler runs the code that upstream runs after the pending evaluate.
    CompletableFuture<APIResponse> responsePromise = new CompletableFuture<>();
    page.route("**/title.html", route -> {
      APIResponse apiResponse = page.request().fetch(route.request());
      responsePromise.complete(apiResponse);
      route.fulfill(new Route.FulfillOptions().setResponse(apiResponse));
    });

    page.evaluate("url => fetch(url)", server.PREFIX + "/title.html");

    APIResponse response = responsePromise.get();

    assertEquals(200, response.status());
    assertEquals("OK", response.statusText());
    assertTrue(response.ok());
    assertEquals(true, response.url().endsWith("/title.html"));
    assertEquals("text/html; charset=utf-8", response.headers().get("content-type"));
    List<HttpHeader> contentTypeHeaders = new ArrayList<>();
    for (HttpHeader header : response.headersArray()) {
      if (header.name.toLowerCase().equals("content-type"))
        contentTypeHeaders.add(header);
    }
    // Java: HttpHeader has no equals(), compare the fields.
    assertEquals(1, contentTypeHeaders.size());
    assertEquals("Content-Type", contentTypeHeaders.get(0).name);
    assertEquals("text/html; charset=utf-8", contentTypeHeaders.get(0).value);
  }

  @PlaywrightTest
  @DisplayName("RO-140 should give access to the intercepted response body")
  void shouldGiveAccessToTheInterceptedResponseBody(Page page, TestServer server) throws Exception {
    page.navigate(server.EMPTY_PAGE);

    // Java: the route handler runs the code that upstream runs after the pending evaluate.
    CompletableFuture<APIResponse> responsePromise = new CompletableFuture<>();
    page.route("**/simple.json", route -> {
      APIResponse apiResponse = page.request().fetch(route.request());
      responsePromise.complete(apiResponse);
      route.fulfill(new Route.FulfillOptions().setResponse(apiResponse));
    });

    page.evaluate("url => fetch(url)", server.PREFIX + "/simple.json");

    APIResponse response = responsePromise.get();

    assertEquals("{\"foo\": \"bar\"}\n", response.text());
  }

  @PlaywrightTest
  @DisplayName("RO-141 should intercept multipart/form-data request body")
  @DisabledOnBrowser(value = {BrowserName.CHROMIUM, BrowserName.WEBKIT}, reason = "fixme")
  void shouldInterceptMultipartFormDataRequestBody(Page page, TestServer server, BrowserName browserName) throws Exception {
    page.navigate(server.PREFIX + "/input/fileupload.html");
    Path filePath = relativeToCwd(asset("file-to-upload.txt"));
    page.locator("input[type=file]").setInputFiles(filePath);
    CompletableFuture<Request> requestPromise = new CompletableFuture<>();
    page.route("**/upload", route -> {
      requestPromise.complete(route.request());
    });
    page.click("input[type=submit]", new Page.ClickOptions().setNoWaitAfter(true));
    // Java: the route handler only runs while a call is pending, so wait for it to settle the promise.
    page.context().waitForCondition(() -> requestPromise.isDone());
    Request request = requestPromise.get();
    assertEquals("POST", request.method());
    assertTrue(request.postData().contains(new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8)));
  }

  @PlaywrightTest
  @DisplayName("RO-142 should fulfill intercepted response using alias")
  void shouldFulfillInterceptedResponseUsingAlias(Page page, TestServer server) {
    page.route("**/*", route -> {
      APIResponse response = route.fetch();
      route.fulfill(new Route.FulfillOptions().setResponse(response));
    });
    Response response = page.navigate(server.PREFIX + "/empty.html");
    assertEquals(200, response.status());
    assertTrue(response.headers().get("content-type").contains("text/html"));
  }

  @PlaywrightTest
  @DisplayName("RO-143 should support timeout option in route.fetch")
  @Fixme("java: the error message of route.fetch() has no 'route.fetch:' prefix")
  void shouldSupportTimeoutOptionInRouteFetch(Page page, TestServer server) {
    server.setRoute("/slow", (req, res) -> {
      res.writeHead(200,
        "content-length", "4096",
        "content-type", "text/html");
    });
    page.route("**/*", route -> {
      PlaywrightException error = assertThrows(PlaywrightException.class, () -> route.fetch(new Route.FetchOptions().setTimeout(1000)));
      assertTrue(error.getMessage().contains("route.fetch: Timeout 1000ms exceeded"));
    });
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.navigate(server.PREFIX + "/slow", new Page.NavigateOptions().setTimeout(2000)));
    assertTrue(error.getMessage().contains("Timeout 2000ms exceeded"));
  }

  @PlaywrightTest
  @DisplayName("RO-144 should not follow redirects when maxRedirects is set to 0 in route.fetch")
  void shouldNotFollowRedirectsWhenMaxRedirectsIsSetTo0InRouteFetch(Page page, TestServer server) {
    server.setRedirect("/foo", "/empty.html");
    page.route("**/*", route -> {
      APIResponse response = route.fetch(new Route.FetchOptions().setMaxRedirects(0));
      assertEquals("/empty.html", response.headers().get("location"));
      assertEquals(302, response.status());
      route.fulfill(new Route.FulfillOptions().setBody("hello"));
    });
    page.navigate(server.PREFIX + "/foo");
    assertTrue(page.content().contains("hello"));
  }

  @PlaywrightTest
  @DisplayName("RO-145 should intercept with url override")
  void shouldInterceptWithUrlOverride(Page page, TestServer server) {
    page.route("**/*.html", route -> {
      APIResponse response = route.fetch(new Route.FetchOptions().setUrl(server.PREFIX + "/one-style.html"));
      route.fulfill(new Route.FulfillOptions().setResponse(response));
    });
    Response response = page.navigate(server.PREFIX + "/empty.html");
    assertEquals(200, response.status());
    assertTrue(new String(response.body(), StandardCharsets.UTF_8).contains("one-style.css"));
  }

  @PlaywrightTest
  @DisplayName("RO-147 request.postData is not null when fetching FormData with a Blob")
  @DisabledOnBrowser(value = BrowserName.WEBKIT, reason = "fixme: The body is empty in WebKit when intercepting")
  void requestPostDataIsNotNullWhenFetchingFormDataWithABlob(Page page, TestServer server, BrowserName browserName) throws Exception {
    page.navigate(server.EMPTY_PAGE);
    page.setContent("\n" +
      "<script>\n" +
      "  function doStuff() {\n" +
      "    const formData = new FormData();\n" +
      "    formData.append('file', new Blob([\"hello\"], { type: \"text/plain\" }));\n" +
      "    fetch('/upload', {\n" +
      "      method: 'POST',\n" +
      "      body: formData\n" +
      "    });\n" +
      "  }\n" +
      "</script>\n" +
      "<body>\n" +
      "<button onclick=\"doStuff()\" data-testid=\"click-me\">Click me!</button>\n" +
      "</body>");
    CompletableFuture<String> postDataPromise = new CompletableFuture<>();
    page.route(server.PREFIX + "/upload", route -> {
      Request request = route.request();
      assertEquals("POST", request.method());
      postDataPromise.complete(request.postData());
      route.fulfill(new Route.FulfillOptions()
        .setStatus(200)
        .setBody("ok"));
    });
    page.getByTestId("click-me").click();
    // Java: the route handler only runs while a call is pending, so wait for it to settle the promise.
    page.context().waitForCondition(() -> postDataPromise.isDone());
    String postData = postDataPromise.get();
    assertTrue(postData.contains("Content-Disposition: form-data; name=\"file\"; filename=\"blob\""));
    assertTrue(postData.contains("\r\nhello\r\n"));
  }

  @PlaywrightTest
  @DisplayName("RO-148 should abort favicon requests if interception is enabled")
  void shouldAbortFaviconRequestsIfInterceptionIsEnabled(Page page, TestServer server, BrowserName browserName) {
    AtomicInteger requestCount = new AtomicInteger(0);
    server.setRoute("/favicon.ico", (req, res) -> {
      requestCount.incrementAndGet();
      res.setHeader("content-type", "text/plain");
      res.end("my content");
    });
    // Intercept all requests.
    page.route("**/*", route -> {
      route.fulfill(new Route.FulfillOptions()
        .setStatus(200)
        .setBody("Hello, world!"));
    });
    page.navigate(server.EMPTY_PAGE);
    Object response = page.evaluate("() => fetch('/favicon.ico').then(r => r.text()).catch(e => 'load failed')");
    assertEquals("load failed", response);
    // Browsers can send favicon requests in the background.
    page.waitForTimeout(1000);
    assertEquals(0, requestCount.get());
  }
}
