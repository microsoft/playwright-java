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
import org.opentest4j.AssertionFailedError;

import java.io.OutputStreamWriter;
import java.io.Writer;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class TestAPIResponseAssertions extends TestBase {
  @Test
  void passWithResponse() {
    APIResponse res = page.request().get(server.EMPTY_PAGE);
    assertThat(res).isOK();
  }

  @Test
  void passWithNot() {
    APIResponse res = page.request().get(server.PREFIX + "/unknown");
    assertThat(res).not().isOK();
  }

  @Test
  void fail() {
    APIResponse res = page.request().get(server.PREFIX + "/unknown");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> assertThat(res).isOK());
    assertTrue(e.getMessage().contains("→ GET " + server.PREFIX + "/unknown"), "Actual error: " + e.toString());
    assertTrue(e.getMessage().contains("← 404 Not Found"), "Actual error: " + e.toString());
  }

  @Test
  void shouldPrintResponseTextIfIdOkFails() {
    APIResponse res = page.request().get(server.PREFIX + "/unknown");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> assertThat(res).isOK());
    assertTrue(e.getMessage().contains("File not found"), "Actual error: " + e.toString());
  }

  @Test
  void shouldOnlyPrintResponseWithTextContentTypeIfIsOkFails() {
    {
      server.setRoute("/text-content-type", exchange -> {
        exchange.getResponseHeaders().set("Content-type", "text/plain");
        exchange.sendResponseHeaders(404, 0);
        try (Writer writer = new OutputStreamWriter(exchange.getResponseBody())) {
          writer.write("Text error");
        }
      });
      AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> assertThat(page.request().get(server.PREFIX + "/text-content-type")).isOK());
      assertTrue(e.getMessage().contains("Text error"), "Actual error: " + e);
    }
    {
      server.setRoute("/svg-xml-content-type", exchange -> {
        exchange.getResponseHeaders().set("Content-type", "image/svg+xml");
        exchange.sendResponseHeaders(404, 0);
        try (Writer writer = new OutputStreamWriter(exchange.getResponseBody())) {
          writer.write("Json error");
        }
      });
      AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> assertThat(page.request().get(server.PREFIX + "/svg-xml-content-type")).isOK());
      assertTrue(e.getMessage().contains("Json error"), "Actual error: " + e);
    }
    {
      server.setRoute("/no-content-type", exchange -> {
        exchange.sendResponseHeaders(404, 0);
        try (Writer writer = new OutputStreamWriter(exchange.getResponseBody())) {
          writer.write("No content type error");
        }
      });
      AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> assertThat(page.request().get(server.PREFIX + "/no-content-type")).isOK());
      assertFalse(e.getMessage().contains("No content type error"), "Actual error: " + e);
    }
    {
      server.setRoute("/image-content-type", exchange -> {
        exchange.getResponseHeaders().set("Content-type", "image/bmp");
        exchange.sendResponseHeaders(404, 0);
        try (Writer writer = new OutputStreamWriter(exchange.getResponseBody())) {
          writer.write("Image type error");
        }
      });
      AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> assertThat(page.request().get(server.PREFIX + "/image-content-type")).isOK());
      assertFalse(e.getMessage().contains("Image type error"), "Actual error: " + e);
    }
  }
}
