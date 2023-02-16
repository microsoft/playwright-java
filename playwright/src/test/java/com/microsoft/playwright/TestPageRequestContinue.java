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

import java.io.OutputStreamWriter;
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
}
