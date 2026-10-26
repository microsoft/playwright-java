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

import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.Sizes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import static com.microsoft.playwright.fixtures.Utils.asset;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/page-network-sizes.spec.ts. */
public class TestPageNetworkSizes {
  @PlaywrightTest
  @DisplayName("NE-058 should set bodySize and headersSize")
  void shouldSetBodySizeAndHeadersSize(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    // Java: waitForEvent('request') without a predicate is waitForRequest with a predicate that accepts all.
    Request request = page.waitForRequest(r -> true, () -> {
      page.evaluate("() => fetch('./get', { method: 'POST', body: '12345' }).then(r => r.text())");
    });
    Sizes sizes = request.sizes();
    assertEquals(5, sizes.requestBodySize);
    assertTrue(sizes.requestHeadersSize >= 250);
  }

  @PlaywrightTest
  @DisplayName("NE-059 should set bodySize to 0 if there was no body")
  void shouldSetBodySizeTo0IfThereWasNoBody(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    // Java: waitForEvent('request') without a predicate is waitForRequest with a predicate that accepts all.
    Request request = page.waitForRequest(r -> true, () -> {
      page.evaluate("() => fetch('./get').then(r => r.text())");
    });
    Sizes sizes = request.sizes();
    assertEquals(0, sizes.requestBodySize);
    assertTrue(sizes.requestHeadersSize >= 190);
  }

  @PlaywrightTest
  @DisplayName("NE-060 should set bodySize, headersSize, and transferSize")
  void shouldSetBodySizeHeadersSizeAndTransferSize(Page page, TestServer server) throws Exception {
    server.setRoute("/get", (req, res) -> {
      // In Firefox, |fetch| will be hanging until it receives |Content-Type| header
      // from server.
      res.setHeader("Content-Type", "text/plain; charset=utf-8");
      res.end("abc134");
    });
    page.navigate(server.EMPTY_PAGE);
    CompletableFuture<TestServer.Request> serverRequest = server.waitForRequest("/get");
    // Java: waitForEvent('response') without a predicate is waitForResponse with a predicate that accepts all.
    Response response = page.waitForResponse(r -> true, () -> {
      page.evaluate("async () => fetch('./get').then(r => r.text())");
    });
    serverRequest.get();
    Sizes sizes = response.request().sizes();
    assertEquals(6, sizes.responseBodySize);
    assertTrue(sizes.responseHeadersSize >= 100);
  }

  @PlaywrightTest
  @DisplayName("NE-061 should set bodySize to 0 when there was no response body")
  void shouldSetBodySizeTo0WhenThereWasNoResponseBody(Page page, TestServer server) {
    Response response = page.navigate(server.EMPTY_PAGE);
    Sizes sizes = response.request().sizes();
    assertEquals(0, sizes.responseBodySize);
    assertTrue(sizes.responseHeadersSize >= 150);
  }

  @PlaywrightTest
  @DisplayName("NE-062 should have the correct responseBodySize")
  void shouldHaveTheCorrectResponseBodySize(Page page, TestServer server) throws Exception {
    Response response = page.navigate(server.PREFIX + "/simplezip.json");
    Sizes sizes = response.request().sizes();
    assertEquals(Files.size(asset("simplezip.json")), sizes.responseBodySize);
  }

  @PlaywrightTest
  @DisplayName("NE-064 should have the correct responseBodySize with gzip compression")
  void shouldHaveTheCorrectResponseBodySizeWithGzipCompression(Page page, TestServer server) throws Exception {
    server.enableGzip("/simplezip.json");
    page.navigate(server.EMPTY_PAGE);
    // Java: waitForEvent('response') without a predicate is waitForResponse with a predicate that accepts all.
    Response response = page.waitForResponse(r -> true, () -> {
      page.evaluate("() => fetch('./simplezip.json').then(r => r.text())");
    });
    Sizes sizes = response.request().sizes();

    // Java: the gzip stream is built with GZIPOutputStream, the same as the test server does.
    ByteArrayOutputStream chunks = new ByteArrayOutputStream();
    try (GZIPOutputStream gzip = new GZIPOutputStream(chunks)) {
      gzip.write(Files.readAllBytes(asset("simplezip.json")));
    }

    assertEquals(chunks.size(), sizes.responseBodySize);
  }

  @PlaywrightTest
  @DisplayName("NE-065 should handle redirects")
  void shouldHandleRedirects(Page page, TestServer server) {
    server.setRedirect("/foo", "/bar");
    server.setRoute("/bar", (req, resp) -> resp.end("bar"));
    page.navigate(server.EMPTY_PAGE);
    // Java: waitForEvent('response') without a predicate is waitForResponse with a predicate that accepts all.
    Response response = page.waitForResponse(r -> true, () -> {
      page.evaluate("async () => fetch('/foo', {\n" +
        "  method: 'POST',\n" +
        "  body: '12345',\n" +
        "}).then(r => r.text())");
    });
    assertEquals(5, response.request().sizes().requestBodySize);
    Request newRequest = response.request().redirectedTo();
    assertEquals(3, newRequest.sizes().responseBodySize);
  }

  @PlaywrightTest
  @DisplayName("NE-066 should throw for failed requests")
  void shouldThrowForFailedRequests(Page page, TestServer server) throws Exception {
    server.setRoute("/one-style.css", (req, res) -> {
      res.setHeader("Content-Type", "text/css");
      res.socket().destroy();
    });
    page.navigate(server.EMPTY_PAGE);
    // Java: waitForEvent('requestfailed') is a future completed by the page event.
    CompletableFuture<Request> failed = new CompletableFuture<>();
    page.onRequestFailed(r -> failed.complete(r));
    page.navigate(server.PREFIX + "/one-style.html");
    page.waitForCondition(() -> failed.isDone());
    Request request = failed.get();
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> request.sizes());
    assertTrue(error.getMessage().contains("Unable to fetch sizes for failed request"));
  }

  @Nested
  @ParameterizedClass
  @ValueSource(ints = {200, 401, 404, 500})
  class ShouldWorkWithStatusCode {
    @Parameter
    int statusCode;

    @PlaywrightTest
    @DisplayName("NE-067 should work with ${statusCode} status code")
    void shouldWorkWithStatusCode(Page page, TestServer server) {
      server.setRoute("/foo", (req, resp) -> {
        resp.writeHead(statusCode,
          "Content-Type", "text/plain; charset=utf-8",
          "Content-Length", "3");
        resp.end("bar");
      });
      page.navigate(server.EMPTY_PAGE);
      // Java: waitForEvent('response') without a predicate is waitForResponse with a predicate that accepts all.
      Response response = page.waitForResponse(r -> true, () -> {
        page.evaluate("async () => fetch('/foo', {\n" +
          "  method: 'POST',\n" +
          "  body: '12345',\n" +
          "}).then(r => r.text())");
      });
      assertEquals(statusCode, response.status());
      Sizes sizes = response.request().sizes();
      assertEquals(5, sizes.requestBodySize);
      assertEquals(3, sizes.responseBodySize);
    }
  }

  @PlaywrightTest
  @DisplayName("NE-068 should have correct responseBodySize for 404 with content")
  @DisabledOnBrowser(value = BrowserName.CHROMIUM, reason = "fixme")
  void shouldHaveCorrectResponseBodySizeFor404WithContent(Page page, TestServer server) {
    server.setRoute("/broken-image.png", (req, resp) -> {
      resp.writeHead(404);
      resp.end("<p>this should have a non-negative size</p>");
    });
    server.setRoute("/page-with-404-image.html", (req, resp) -> {
      resp.end("\n" +
        "    <!DOCTYPE html>\n" +
        "    <html>\n" +
        "      <head><title>Page with Broken Image</title></head>\n" +
        "      <body>\n" +
        "        <img src=\"/broken-image.png\" />\n" +
        "      </body>\n" +
        "    </html>\n" +
        "    ");
    });

    Request req = page.waitForRequest(Pattern.compile("broken-image\\.png$"), () -> {
      page.navigate(server.PREFIX + "/page-with-404-image.html");
    });

    int responseBodySize = req.sizes().responseBodySize;
    assertTrue(responseBodySize >= 0);
  }

  @PlaywrightTest
  @DisplayName("NE-069 should return sizes without hanging")
  @DisabledOnBrowser(value = BrowserName.CHROMIUM, reason = "fixme")
  void shouldReturnSizesWithoutHanging(Page page, TestServer server) {
    server.setRoute("/has-abandoned-fetch", (req, resp) -> {
      resp.end("\n" +
        "    <!DOCTYPE html>\n" +
        "    <html>\n" +
        "      <head><title>t</title></head>\n" +
        "      <body>\n" +
        "        <script>\n" +
        "          fetch(\"./404\");\n" +
        "        </script>\n" +
        "      </body>\n" +
        "    </html>\n" +
        "    ");
    });

    Request req = page.waitForRequest(Pattern.compile("404$"), () -> {
      page.navigate(server.PREFIX + "/has-abandoned-fetch");
    });

    req.sizes();
  }
}
