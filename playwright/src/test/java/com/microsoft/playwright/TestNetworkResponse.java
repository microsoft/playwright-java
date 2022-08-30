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

import com.microsoft.playwright.options.ServerAddr;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class TestNetworkResponse extends TestBase {
  @Test
  void shouldWork() {
    server.setRoute("/empty.html", exchange -> {
      exchange.getResponseHeaders().add("foo", "bar");
      exchange.getResponseHeaders().add("BaZ", "bAz");
      exchange.sendResponseHeaders(200, 0);
      exchange.getResponseBody().close();
    });
    Response response = page.navigate(server.EMPTY_PAGE);
    assertEquals("bar", response.headers().get("foo"));
    assertEquals("bAz", response.headers().get("baz"));
    assertNull(response.headers().get("BaZ"));
  }

  @Test
  void shouldReturnText() {
    Response response = page.navigate(server.PREFIX + "/simple.json");
    assertEquals("{\"foo\": \"bar\"}\n", response.text());
  }

  @Test
  void shouldReturnUncompressedText() {
    server.enableGzip("/simple.json");
    Response response = page.navigate(server.PREFIX + "/simple.json");
    assertEquals("gzip", response.headers().get("content-encoding"));
    assertEquals("{\"foo\": \"bar\"}\n", response.text());
  }

  @Test
  void shouldThrowWhenRequestingBodyOfRedirectedResponse() {
    server.setRedirect("/foo.html", "/empty.html");
    Response response = page.navigate(server.PREFIX + "/foo.html");
    Request redirectedFrom = response.request().redirectedFrom();
    assertNotNull(redirectedFrom);
    Response redirected = redirectedFrom.response();
    assertEquals(302, redirected.status());
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> redirected.text());
    assertTrue(e.getMessage().contains("Response body is unavailable for redirect responses"));
  }

  @Test
  void shouldWaitUntilResponseCompletes() throws ExecutionException, InterruptedException {
    page.navigate(server.EMPTY_PAGE);
    Semaphore responseWritten = new Semaphore(0);
    Semaphore responseRead = new Semaphore(0);
    server.setRoute("/get", exchange -> {
      // In Firefox, |fetch| will be hanging until it receives |Content-Type| header
      // from server.
      exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
      exchange.sendResponseHeaders(200, 0);
      try (OutputStreamWriter writer = new OutputStreamWriter(exchange.getResponseBody())) {
        writer.write("hello ");
        writer.flush();
        responseWritten.release();
        responseRead.acquire();
        writer.write("wor");
        writer.flush();
        writer.write("ld!");
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      responseWritten.release();
    });
    // Setup page to trap response.
    boolean[] requestFinished = {false};
    page.onRequestFinished(request -> {
      requestFinished[0] |= request.url().contains("/get");
    });
    // send request and wait for server response
    Response pageResponse = page.waitForResponse("**",
      () -> page.evaluate("() => fetch('./get', { method: 'GET'})"));
    assertNotNull(pageResponse);
    responseWritten.acquire();
    assertEquals(200, pageResponse.status());
    assertEquals(false, requestFinished[0]);
    responseRead.release();
    responseWritten.acquire();
    assertEquals("hello world!", pageResponse.text());
    assertEquals(true, requestFinished[0]);
  }

  void shouldReturnJson() {
    // Not exposed in Java.
  }
  @Test
  void shouldReturnBody() throws IOException {
    Response response = page.navigate(server.PREFIX + "/pptr.png");
    byte[] expected = Files.readAllBytes(Paths.get("src/test/resources/pptr.png"));
    assertTrue(Arrays.equals(expected, response.body()));
  }

  @Test
  void shouldReturnBodyWithCompression() throws IOException {
    server.enableGzip("/pptr.png");
    Response response = page.navigate(server.PREFIX + "/pptr.png");
    byte[] expected = Files.readAllBytes(Paths.get("src/test/resources/pptr.png"));
    assertTrue(Arrays.equals(expected, response.body()));
  }

  @Test
  void shouldReturnStatusText() {
    server.setRoute("/cool", exchange -> {
      exchange.sendResponseHeaders(200, 0);
      exchange.getResponseBody().close();
    });
    Response response = page.navigate(server.PREFIX + "/cool");
    assertEquals("OK", response.statusText());
  }

  @Test
  void shouldReturnServerAddress() {
    Response response = page.navigate(server.EMPTY_PAGE);
    ServerAddr address = response.serverAddr();
    assertNotNull(address);
    assertEquals(server.PORT, address.port);
    assertTrue(asList("127.0.0.1", "::1").contains(address.ipAddress), address.ipAddress);
  }
}
