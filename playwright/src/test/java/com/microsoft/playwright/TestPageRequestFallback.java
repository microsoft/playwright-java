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

import com.microsoft.playwright.fixtures.PlaywrightTest;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class TestPageRequestFallback {
  @PlaywrightTest
  void shouldWork(Page page, Server server) {
    page.route("**/*", route -> route.fallback());
    page.navigate(server.EMPTY_PAGE);
  }

  @PlaywrightTest
  void shouldFallBack(Page page, Server server) {
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
    assertEquals(asList(3, 2, 1), intercepted);
  }

  @PlaywrightTest
  void shouldFallBackAsync(Page page, Server server) {
    List<Integer> intercepted = new ArrayList<>();
    page.route("**/empty.html", route -> {
      intercepted.add(1);
      page.waitForTimeout(50);
      route.fallback();
    });
    page.route("**/empty.html", route -> {
      intercepted.add(2);
      page.waitForTimeout(100);
      route.fallback();
    });
    page.route("**/empty.html", route -> {
      intercepted.add(3);
      page.waitForTimeout(150);
      route.fallback();
    });
    page.navigate(server.EMPTY_PAGE);
    assertEquals(asList(3, 2, 1), intercepted);
  }

  @PlaywrightTest
  void shouldNotChainFulfill(Page page, Server server) {
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
    assertEquals("fulfilled", response.text());
    assertFalse(failed[0]);
  }

  @PlaywrightTest
  void shouldNotChainAbort(Page page, Server server) {
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
  void shouldFallBackAfterException(Page page, Server server) {
    page.route("**/empty.html", route -> {
      route.resume();
    });
    page.route("**/empty.html", route -> {
      try {
        route.fulfill(new Route.FulfillOptions());
      } catch (PlaywrightException e) {
        route.fallback();
      }
    });
    page.navigate(server.EMPTY_PAGE);
  }

  @PlaywrightTest
  void shouldChainOnce(Page page, Server server) {
    boolean didFulfill[] = {false};
    page.route("**/title.html", route -> {
      route.fulfill(new Route.FulfillOptions().setStatus(200).setBody("fulfilled one"));
      didFulfill[0] = true;
    }, new Page.RouteOptions().setTimes(1));
    page.route("**/title.html", route -> {
      route.fallback();
    }, new Page.RouteOptions().setTimes(1));
    Response response = page.navigate(server.PREFIX + "/title.html");
    assertTrue(didFulfill[0]);
    assertEquals("fulfilled one", response.text());
  }

  @PlaywrightTest
  void shouldAmendHTTPHeaders(Page page, Server server) throws ExecutionException, InterruptedException {
    List<String> values = new ArrayList<>();
    page.route("**/sleep.zzz", route -> {
      values.add(route.request().headers().get("foo"));
      values.add(route.request().headerValue("FOO"));
      route.resume();
    });
    page.route("**/*", route -> {
      Map<String, String> headers = route.request().headers();
      headers.put("FOO", "bar");
      route.fallback(new Route.FallbackOptions().setHeaders(headers));
    });
    page.navigate(server.EMPTY_PAGE);
    Future<Server.Request> request = server.futureRequest("/sleep.zzz");
    page.evaluate("() => fetch('/sleep.zzz')");
    values.addAll(request.get().headers.get("foo"));
    assertEquals(asList("bar", "bar", "bar"), values);
  }

  @PlaywrightTest
  void shouldDeleteHeaderWithUndefinedValue(Page page, Server server) throws ExecutionException, InterruptedException {
    // https://github.com/microsoft/playwright/issues/13106
    page.navigate(server.PREFIX + "/empty.html");
    Future<Server.Request> serverRequest = server.futureRequest("/something");
    server.setRoute("/something", exchange -> {
      exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
      exchange.sendResponseHeaders(200, 0);
      try (OutputStreamWriter writer = new OutputStreamWriter(exchange.getResponseBody())) {
        writer.write("done");
      }
    });

    Request[] interceptedRequest = {null};
    page.route(server.PREFIX + "/something", route -> {
      interceptedRequest[0] = route.request();
      route.resume();
    });
    page.route(server.PREFIX + "/something", route -> {
      Map<String, String> headers = route.request().allHeaders();
      headers.remove("foo");
      route.fallback(new Route.FallbackOptions().setHeaders(headers));
    });

    Object text = page.evaluate("async url => {\n" +
      "      const data = await fetch(url, {\n" +
      "        headers: {\n" +
      "          foo: 'a',\n" +
      "          bar: 'b',\n" +
      "        }\n" +
      "      });\n" +
      "      return data.text();\n" +
      "    }", server.PREFIX + "/something");

    assertEquals("done", text);
    assertNull(interceptedRequest[0].headers().get("foo"));
    assertNull(serverRequest.get().headers.get("foo"));
    assertEquals(asList("b"), serverRequest.get().headers.get("bar"));
  }

  @PlaywrightTest
  void shouldAmendMethod(Page page, Server server) throws ExecutionException, InterruptedException {
    Future<Server.Request> sRequest = server.futureRequest("/sleep.zzz");
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
  void shouldOverrideRequestUrl(Page page, Server server) throws ExecutionException, InterruptedException {
    Future<Server.Request> request = server.futureRequest("/global-var.html");
    String[] url = {null};
    page.route("**/global-var.html", route -> {
      url[0] = route.request().url();
      route.resume();
    });
    page.route("**/foo", route -> route.fallback(new Route.FallbackOptions().setUrl(server.PREFIX + "/global-var.html")));
    Response response = page.waitForResponse("**/*", () -> page.navigate(server.PREFIX + "/foo"));
    assertEquals(server.PREFIX + "/global-var.html", url[0]);
    assertEquals(server.PREFIX + "/global-var.html", response.url());
    assertEquals(server.PREFIX + "/global-var.html", response.request().url());
    assertEquals(123, page.evaluate("() => window['globalVar']"));
    assertEquals("GET", request.get().method);
  }

  @PlaywrightTest
  void shouldAmendPostData(Page page, Server server) throws ExecutionException, InterruptedException {
    page.navigate(server.EMPTY_PAGE);
    String[] postData = {null};
    page.route("**/*", route -> {
      postData[0] = route.request().postData();
      route.resume();
    });
    page.route("**/*", route -> {
      route.fallback(new Route.FallbackOptions().setPostData("doggo"));
    });
    Future<Server.Request> serverRequest = server.futureRequest("/sleep.zzz");
    page.evaluate("() => fetch('/sleep.zzz', { method: 'POST', body: 'birdy' })");
    assertEquals("doggo", postData[0]);
    assertEquals("doggo", new String(serverRequest.get().postBody, StandardCharsets.UTF_8));
  }

  @PlaywrightTest
  void shouldAmendBinaryPostData(Page page, Server server) throws ExecutionException, InterruptedException {
    page.navigate(server.EMPTY_PAGE);
    byte[] arr = new byte[256];
    for (int i = 0; i < arr.length; i++) {
      arr[i] = (byte) i;
    }
    byte[][] postDataBuffer = {null};
    page.route("**/*", route -> {
      postDataBuffer[0] = route.request().postDataBuffer();
      route.resume();
    });
    page.route("**/*", route -> {
      route.fallback(new Route.FallbackOptions().setPostData(arr));
    });
    Future<Server.Request> serverRequest = server.futureRequest("/sleep.zzz");
    page.evaluate("() => fetch('/sleep.zzz', { method: 'POST', body: 'birdy' })");
    byte[] buffer = serverRequest.get().postBody;
    assertArrayEquals(arr, buffer);
    assertArrayEquals(arr, postDataBuffer[0]);
  }

}
