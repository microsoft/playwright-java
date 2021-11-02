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

import com.microsoft.playwright.options.Sizes;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestPageNetworkSizes extends TestBase {
  @Test
  void shouldSetBodySizeAndHeadersSize() {
    page.navigate(server.EMPTY_PAGE);
    Request request = page.waitForRequest("**/*", () -> {
      page.evaluate("() => fetch('./get', { method: 'POST', body: '12345' }).then(r => r.text())");
    });
    Sizes sizes = request.sizes();
    assertEquals(5, sizes.requestBodySize);
    assertTrue(sizes.requestHeadersSize >= 250);
  }

  @Test
  void shouldSetBodySizeTo0IfThereWasNoBody() {
    page.navigate(server.EMPTY_PAGE);
    Request request = page.waitForRequest("**/*",
      () -> page.evaluate("() => fetch('./get').then(r => r.text())"));
    Sizes sizes = request.sizes();
    assertEquals(0, sizes.requestBodySize);
    assertTrue(sizes.requestHeadersSize >= 200);
  }

  @Test
  void shouldSetBodySizeHeadersSizeAndTransferSize() throws ExecutionException, InterruptedException {
    server.setRoute("/get", exchange -> {
      // In Firefox, |fetch| will be hanging until it receives |Content-Type| header
      // from server.
      exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
      exchange.sendResponseHeaders(200, 6);
      try (OutputStreamWriter writer = new OutputStreamWriter(exchange.getResponseBody())) {
        writer.write("abc134");
      }
    });
    Future<Server.Request> request = server.futureRequest("/get");
    page.navigate(server.EMPTY_PAGE);
    Response response = page.waitForResponse("**/*",
      () -> page.evaluate("async () => fetch('./get').then(r => r.text())"));
    request.get();
    Sizes sizes = response.request().sizes();
    assertEquals("abc134", response.text());
    assertEquals(6, sizes.responseBodySize);
    assertTrue(sizes.responseHeadersSize > 10);
  }

  @Test
  void shouldSetBodySizeTo0WhenThereWasNoResponseBody() {
    Response response = page.navigate(server.EMPTY_PAGE);
    Sizes sizes = response.request().sizes();
    assertEquals(0, sizes.responseBodySize);
    assertTrue(sizes.responseHeadersSize > 10, "" + sizes.responseHeadersSize);
  }

  @Test
  void shouldHaveTheCorrectResponseBodySize() throws IOException {
    Response response = page.navigate(server.PREFIX + "/simplezip.json");
    Sizes sizes = response.request().sizes();
    assertEquals(Files.size(Paths.get("src/test/resources/simplezip.json")), sizes.responseBodySize);
  }}
