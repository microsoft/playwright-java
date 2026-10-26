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
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Port of tests/page/page-request-fallback.spec.ts. */
public class TestPageRequestFallback {
  private static boolean truthy(String value) {
    return value != null && !value.isEmpty();
  }

  @PlaywrightTest
  @DisplayName("RO-120 should work")
  void shouldWork(Page page, TestServer server) {
    page.route("**/*", route -> route.fallback());
    page.navigate(server.EMPTY_PAGE);
  }

  @PlaywrightTest
  @DisplayName("RO-121 should fall back")
  void shouldFallBack(Page page, TestServer server) {
    List<Integer> intercepted = new ArrayList<>();
    page.route("**/empty.html", route -> {
      intercepted.add(1);
      route.fallback();
    });
    page.route("**/empty.html", route -> {
      intercepted.add(2);
      route.fallback();
    });
    page.route("**/empty.html", route -> {
      intercepted.add(3);
      route.fallback();
    });
    page.navigate(server.EMPTY_PAGE);
    assertEquals(Arrays.asList(3, 2, 1), intercepted);
  }

  @PlaywrightTest
  @DisplayName("RO-122 should fall back async")
  void shouldFallBackAsync(Page page, TestServer server) {
    List<Integer> intercepted = new ArrayList<>();
    page.route("**/empty.html", route -> {
      intercepted.add(1);
      page.waitForTimeout(100);
      route.fallback();
    });
    page.route("**/empty.html", route -> {
      intercepted.add(2);
      page.waitForTimeout(100);
      route.fallback();
    });
    page.route("**/empty.html", route -> {
      intercepted.add(3);
      page.waitForTimeout(100);
      route.fallback();
    });
    page.navigate(server.EMPTY_PAGE);
    assertEquals(Arrays.asList(3, 2, 1), intercepted);
  }

  @PlaywrightTest
  @DisplayName("RO-123 should not chain fulfill")
  void shouldNotChainFulfill(Page page, TestServer server) {
    boolean[] failed = {false};
    page.route("**/empty.html", route -> {
      failed[0] = true;
    });
    page.route("**/empty.html", route -> {
      route.fulfill(new Route.FulfillOptions().setStatus(200).setBody("fulfilled"));
    });
    page.route("**/empty.html", route -> {
      route.fallback();
    });
    Response response = page.navigate(server.EMPTY_PAGE);
    byte[] body = response.body();
    assertEquals("fulfilled", new String(body, StandardCharsets.UTF_8));
    assertFalse(failed[0]);
  }

  @PlaywrightTest
  @DisplayName("RO-124 should not chain abort")
  void shouldNotChainAbort(Page page, TestServer server) {
    boolean[] failed = {false};
    page.route("**/empty.html", route -> {
      failed[0] = true;
    });
    page.route("**/empty.html", route -> {
      route.abort();
    });
    page.route("**/empty.html", route -> {
      route.fallback();
    });
    assertThrows(PlaywrightException.class, () -> page.navigate(server.EMPTY_PAGE));
    assertFalse(failed[0]);
  }

  @PlaywrightTest
  @DisplayName("RO-125 should fall back after exception")
  @Fixme("java: route.fulfill() marks the route as handled before it validates the options, so route.fallback() after the exception throws PlaywrightException 'Route is already handled!'")
  void shouldFallBackAfterException(Page page, TestServer server) {
    page.route("**/empty.html", route -> {
      route.resume();
    });
    page.route("**/empty.html", route -> {
      try {
        // Java: `{} as any` is an object that is not an APIResponse.
        APIResponse invalidResponse = (APIResponse) Proxy.newProxyInstance(
          APIResponse.class.getClassLoader(), new Class<?>[]{APIResponse.class}, (proxy, method, args) -> null);
        route.fulfill(new Route.FulfillOptions().setResponse(invalidResponse));
      } catch (Exception e) {
        route.fallback();
      }
    });
    page.navigate(server.EMPTY_PAGE);
  }

  @PlaywrightTest
  @DisplayName("RO-126 should chain once")
  void shouldChainOnce(Page page, TestServer server) {
    page.route("**/empty.html", route -> {
      route.fulfill(new Route.FulfillOptions().setStatus(200).setBody("fulfilled one"));
    }, new Page.RouteOptions().setTimes(1));
    page.route("**/empty.html", route -> {
      route.fallback();
    }, new Page.RouteOptions().setTimes(1));
    Response response = page.navigate(server.EMPTY_PAGE);
    byte[] body = response.body();
    assertEquals("fulfilled one", new String(body, StandardCharsets.UTF_8));
  }

  @PlaywrightTest
  @DisplayName("RO-127 should amend HTTP headers")
  void shouldAmendHTTPHeaders(Page page, TestServer server) throws Exception {
    List<String> values = new ArrayList<>();
    page.route("**/sleep.zzz", route -> {
      values.add(route.request().headers().get("foo"));
      values.add(route.request().headerValue("FOO"));
      route.resume();
    });
    page.route("**/*", route -> {
      Map<String, String> headers = new HashMap<>(route.request().headers());
      headers.put("FOO", "bar");
      route.fallback(new Route.FallbackOptions().setHeaders(headers));
    });
    page.navigate(server.EMPTY_PAGE);
    CompletableFuture<TestServer.Request> request = server.waitForRequest("/sleep.zzz");
    page.evaluate("() => fetch('/sleep.zzz')");
    values.add(request.get().headers.get("foo"));
    assertEquals(Arrays.asList("bar", "bar", "bar"), values);
  }

  @PlaywrightTest
  @DisplayName("RO-128 should delete header with undefined value")
  @Fixme("java: a null header value in route.fallback() headers throws PlaywrightException 'Value cannot be null' instead of deleting the header")
  void shouldDeleteHeaderWithUndefinedValue(Page page, TestServer server, BrowserName browserName) throws Exception {
    page.navigate(server.PREFIX + "/empty.html");
    server.setRoute("/something", (request, response) -> {
      response.writeHead(200, "Access-Control-Allow-Origin", "*");
      response.end("done");
    });
    Request[] interceptedRequest = {null};
    page.route("**/*", route -> {
      interceptedRequest[0] = route.request();
      route.resume();
    });
    page.route(server.PREFIX + "/something", route -> {
      Map<String, String> headers = new HashMap<>(route.request().allHeaders());
      // Java: undefined is null.
      headers.put("foo", null);
      route.fallback(new Route.FallbackOptions().setHeaders(headers));
    });
    CompletableFuture<TestServer.Request> serverRequestPromise = server.waitForRequest("/something");
    Object text = page.evaluate("async url => {\n" +
      "  const data = await fetch(url, {\n" +
      "    headers: {\n" +
      "      foo: 'a',\n" +
      "      bar: 'b',\n" +
      "    }\n" +
      "  });\n" +
      "  return data.text();\n" +
      "}", server.PREFIX + "/something");
    TestServer.Request serverRequest = serverRequestPromise.get();
    assertEquals("done", text);
    assertNull(interceptedRequest[0].headers().get("foo"));
    assertEquals("b", interceptedRequest[0].headers().get("bar"));
    assertFalse(truthy(serverRequest.headers.get("foo")));
    assertEquals("b", serverRequest.headers.get("bar"));
  }

  @PlaywrightTest
  @DisplayName("RO-129 should amend method")
  void shouldAmendMethod(Page page, TestServer server) throws Exception {
    CompletableFuture<TestServer.Request> sRequest = server.waitForRequest("/sleep.zzz");
    page.navigate(server.EMPTY_PAGE);

    String[] method = {null};
    page.route("**/*", route -> {
      method[0] = route.request().method();
      route.resume();
    });
    page.route("**/*", route -> route.fallback(new Route.FallbackOptions().setMethod("POST")));

    Request request = page.waitForRequest("**/sleep.zzz", () -> page.evaluate("() => fetch('/sleep.zzz')"));
    assertEquals("POST", method[0]);
    assertEquals("POST", request.method());
    assertEquals("POST", sRequest.get().method);
  }

  @PlaywrightTest
  @DisplayName("RO-130 should override request url")
  void shouldOverrideRequestUrl(Page page, TestServer server) throws Exception {
    CompletableFuture<TestServer.Request> serverRequest = server.waitForRequest("/global-var.html");

    String[] url = {null};
    page.route("**/global-var.html", route -> {
      url[0] = route.request().url();
      route.resume();
    });

    page.route("**/foo", route -> route.fallback(new Route.FallbackOptions().setUrl(server.PREFIX + "/global-var.html")));

    Response response = page.navigate(server.PREFIX + "/foo");
    assertEquals(server.PREFIX + "/global-var.html", url[0]);
    assertEquals(server.PREFIX + "/global-var.html", response.request().url());
    assertEquals(server.PREFIX + "/global-var.html", response.url());
    assertEquals(123, page.evaluate("() => window['globalVar']"));
    assertEquals("GET", serverRequest.get().method);
  }

  @Nested
  class PostData {
    @PlaywrightTest
    @DisplayName("RO-131 should amend post data")
    void shouldAmendPostData(Page page, TestServer server) throws Exception {
      page.navigate(server.EMPTY_PAGE);
      String[] postData = {null};
      page.route("**/*", route -> {
        postData[0] = route.request().postData();
        route.resume();
      });
      page.route("**/*", route -> {
        route.fallback(new Route.FallbackOptions().setPostData("doggo"));
      });
      CompletableFuture<TestServer.Request> serverRequest = server.waitForRequest("/sleep.zzz");
      page.evaluate("() => fetch('/sleep.zzz', { method: 'POST', body: 'birdy' })");
      assertEquals("doggo", postData[0]);
      assertEquals("doggo", new String(serverRequest.get().postBody(), StandardCharsets.UTF_8));
    }

    @PlaywrightTest
    @DisplayName("RO-132 should amend binary post data")
    void shouldAmendBinaryPostData(Page page, TestServer server) throws Exception {
      page.navigate(server.EMPTY_PAGE);
      // Java: Buffer.from(arr) is a byte[].
      byte[] arr = new byte[256];
      for (int i = 0; i < arr.length; i++)
        arr[i] = (byte) i;
      byte[][] postDataBuffer = {null};
      page.route("**/*", route -> {
        postDataBuffer[0] = route.request().postDataBuffer();
        route.resume();
      });
      page.route("**/*", route -> {
        route.fallback(new Route.FallbackOptions().setPostData(arr));
      });
      CompletableFuture<TestServer.Request> serverRequestPromise = server.waitForRequest("/sleep.zzz");
      page.evaluate("() => fetch('/sleep.zzz', { method: 'POST', body: 'birdy' })");
      byte[] buffer = serverRequestPromise.get().postBody();
      assertEquals(arr.length, postDataBuffer[0].length);
      assertEquals(arr.length, buffer.length);
      for (int i = 0; i < arr.length; ++i) {
        assertEquals(arr[i], buffer[i]);
        assertEquals(arr[i], postDataBuffer[0][i]);
      }
    }
  }
}
