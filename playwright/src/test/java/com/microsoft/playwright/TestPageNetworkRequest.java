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

import com.google.gson.Gson;
import com.microsoft.playwright.options.HttpHeader;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class TestPageNetworkRequest extends TestBase {
  @Test
  void shouldReportRawHeaders() throws InterruptedException {
    List<HttpHeader> serverHeaders = new ArrayList<>();
    Semaphore responseWritten = new Semaphore(0);
    server.setRoute("/headers", exchange -> {
      for (Map.Entry<String, List<String>> entry : exchange.getRequestHeaders().entrySet()) {
        for (String value : entry.getValue()) {
          HttpHeader header = new HttpHeader();
          header.name = entry.getKey();
          header.value = value;
          serverHeaders.add(header);
        }
      }
      exchange.sendResponseHeaders(200, 0);
      exchange.getResponseBody().close();
      responseWritten.release();
    });
    page.navigate(server.EMPTY_PAGE);
    Request request = page.waitForRequest("**/*", () -> {
      page.evaluate("() => fetch('/headers', {\n" +
        "      headers: [\n" +
        "        ['header-a', 'value-a'],\n" +
        "        ['header-b', 'value-b'],\n" +
        "        ['header-a', 'value-a-1'],\n" +
        "        ['header-a', 'value-a-2'],\n" +
        "      ]\n" +
        "    })");
    });

    responseWritten.acquire();
    List<HttpHeader> expectedHeaders = serverHeaders;
    if (isWebKit() && isWindows) {
      expectedHeaders = expectedHeaders.stream()
        .filter(h -> !"accept-encoding".equals(h.name.toLowerCase()) && !"accept-language".equals(h.name.toLowerCase()))
        .collect(Collectors.toList());
    }

    List<HttpHeader> headers = request.headersArray();
    // Java HTTP server normalizes header names, work around that:
    expectedHeaders = expectedHeaders.stream().map(h -> {
      h.name = h.name.toLowerCase();
      return h;
    }).collect(Collectors.toList());
    headers = headers.stream().map(h -> {
      h.name = h.name.toLowerCase();
      return h;
    }).collect(Collectors.toList());
    Comparator<HttpHeader> comparator = Comparator.comparing(h -> h.name);
    expectedHeaders.sort(comparator);
    headers.sort(comparator);
    assertEquals(new Gson().toJsonTree(expectedHeaders), new Gson().toJsonTree(headers));
    assertEquals("value-a, value-a-1, value-a-2", request.headerValue("header-a"));
    assertEquals(null, request.headerValue("not-there"));
  }

  @Test
  void shouldReportAllCookiesInOneHeader() {
    page.navigate(server.EMPTY_PAGE);
    page.evaluate("() => {\n" +
      "    document.cookie = 'myCookie=myValue';\n" +
      "    document.cookie = 'myOtherCookie=myOtherValue';\n" +
      "  }");
    Response response = page.navigate(server.EMPTY_PAGE);
    String cookie = response.request().allHeaders().get("cookie");
    assertEquals("myCookie=myValue; myOtherCookie=myOtherValue", cookie);
  }

  @Test
  void shouldReportPostDataFor403Request() throws InterruptedException, ExecutionException {
    server.setRoute("/upload", exchange -> {
      exchange.sendResponseHeaders(403, 0);
      exchange.getResponseBody().close();
    });
    Future<Server.Request> serverRequest = server.futureRequest("/upload");
    page.navigate(server.EMPTY_PAGE);
    Request request = page.waitForRequest("**/*", () -> {
      page.evaluate("() => fetch('/upload', { method: 'POST', body: 'test'})");
    });
    assertEquals("test", new String(serverRequest.get().postBody, StandardCharsets.UTF_8));
    assertEquals("test", request.postData());
    assertEquals("POST", request.method());
    assertEquals(403, request.response().status());
  }

  @Test
  void shouldNotAllowToAccessFrameOnPopupMainRequest() {
    page.setContent("<a target=_blank href='" + server.EMPTY_PAGE + "'>click me</a>");
    Request[] request = { null };
    PlaywrightException[] error = { null };
    page.context().onRequest(req -> {
      request[0] = req;
      try {
        req.frame();
      } catch (PlaywrightException e) {
        error[0] = e;
      }
    });
    page.getByText("click me").click();
    waitForCondition(() -> request[0] != null);
    assertTrue(request[0].isNavigationRequest());
    assertTrue(error[0].getMessage().contains("Frame for this navigation request is not available"), error[0].getMessage());
  }

  @Test
  void shouldThrowIfRequestWasGCed() {
    List<Request> requests = new ArrayList<>();
    page.onRequest(req -> requests.add(req));
    for (int i = 0; i < 1001; i++) {
      page.navigate(server.EMPTY_PAGE);
    }
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> requests.get(0).response());
    assertEquals("The object has been collected to prevent unbounded heap growth.", e.getMessage());
  }
}
