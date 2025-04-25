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

import com.microsoft.playwright.options.HttpHeader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;

import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class TestPageRequestContinue extends TestBase {
  @Test
  void shouldDeleteHeaderWithUndefinedValue() throws ExecutionException, InterruptedException {
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
      Map<String, String> headers = route.request().allHeaders();
      headers.remove("foo");
      route.resume(new Route.ResumeOptions().setHeaders(headers));
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

  @Test
  void shouldNotThrowWhenContinuingAfterPageIsClosed() {
    boolean[] done = {false};
    page.route("**/*", route -> {
      page.close();
      route.resume();
      done[0] = true;
    });
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.navigate(server.EMPTY_PAGE));
    assertTrue(e.getMessage().contains("Target page, context or browser has been closed") ||
      e.getMessage().contains("frame was detached"), e.getMessage());
    assertTrue(done[0]);
  }

  @Test
  @DisabledIf(value = "com.microsoft.playwright.TestBase#isFirefox", disabledReason = "We currently clear all headers during interception in firefox")
  void continueShouldNotPropagateCookieOverrideToRedirects() throws ExecutionException, InterruptedException {
    // https://github.com/microsoft/playwright/issues/35168
    server.setRoute("/set-cookie", exchange -> {
      exchange.getResponseHeaders().add("Set-Cookie", "foo=bar;");
      exchange.sendResponseHeaders(200, 0);
      exchange.getResponseBody().close();
    });
    page.navigate(server.PREFIX + "/set-cookie");
    assertEquals("foo=bar", page.evaluate("() => document.cookie"));

    server.setRedirect("/redirect", server.PREFIX + "/empty.html");
    page.route("**/redirect", route -> {
      Map<String, String> headers = new HashMap<>(route.request().allHeaders());
      headers.put("cookie", "override");
      route.resume(new Route.ResumeOptions().setHeaders(headers));
    });

    Future<Server.Request> serverRequest = server.futureRequest("/empty.html");
    page.navigate(server.PREFIX + "/redirect");
    assertEquals(asList("foo=bar"), serverRequest.get().headers.get("cookie"));
  }

  @Test
  @DisabledIf(value = "com.microsoft.playwright.TestBase#isFirefox", disabledReason = "We currently clear all headers during interception in firefox")
  void continueShouldNotOverrideCookie() throws ExecutionException, InterruptedException {
    // https://github.com/microsoft/playwright/issues/35168
    server.setRoute("/set-cookie", exchange -> {
      exchange.getResponseHeaders().add("Set-Cookie", "foo=bar;");
      exchange.sendResponseHeaders(200, 0);
      exchange.getResponseBody().close();
    });
    page.navigate(server.PREFIX + "/set-cookie");
    assertEquals("foo=bar", page.evaluate("() => document.cookie"));

    page.route("**", route -> {
      Map<String, String> headers = new HashMap<>(route.request().allHeaders());
      headers.put("cookie", "override");
      headers.put("custom", "value");
      route.resume(new Route.ResumeOptions().setHeaders(headers));
    });

    Future<Server.Request> serverRequest = server.futureRequest("/empty.html");
    page.navigate(server.EMPTY_PAGE);

    // Original cookie from the browser's cookie jar should be sent.
    assertEquals(asList("foo=bar"), serverRequest.get().headers.get("cookie"));
    assertEquals(asList("value"), serverRequest.get().headers.get("custom"));
  }

  @Test
  void redirectAfterContinueShouldBeAbleToDeleteCookie() throws ExecutionException, InterruptedException {
    // https://github.com/microsoft/playwright/issues/35168
    server.setRoute("/set-cookie", exchange -> {
      exchange.getResponseHeaders().add("Set-Cookie", "foo=bar;");
      exchange.sendResponseHeaders(200, 0);
      exchange.getResponseBody().close();
    });
    page.navigate(server.PREFIX + "/set-cookie");
    assertEquals("foo=bar", page.evaluate("() => document.cookie"));

    server.setRoute("/delete-cookie", exchange -> {
      exchange.getResponseHeaders().add("Set-Cookie", "foo=bar; expires=Thu, 01 Jan 1970 00:00:00 GMT");
      exchange.sendResponseHeaders(200, 0);
      exchange.getResponseBody().close();
    });
    server.setRedirect("/redirect", "/delete-cookie");
    page.route("**/redirect", route -> {
      // Pass original headers explicitly when continuing.
      route.resume(new Route.ResumeOptions().setHeaders(route.request().allHeaders()));
    });
    page.navigate(server.PREFIX + "/redirect");

    Future<Server.Request> serverRequest = server.futureRequest("/empty.html");
    page.navigate(server.EMPTY_PAGE);
    assertNull(serverRequest.get().headers.get("cookie"));
  }
}
