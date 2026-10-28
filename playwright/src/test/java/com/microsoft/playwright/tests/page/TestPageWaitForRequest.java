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
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/page-wait-for-request.spec.ts. */
public class TestPageWaitForRequest {
  @PlaywrightTest
  @DisplayName("WA-121 should work")
  void shouldWork(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    Request request = page.waitForRequest(server.PREFIX + "/digits/2.png", () -> {
      page.evaluate("() => {\n" +
        "  void fetch('/digits/1.png');\n" +
        "  void fetch('/digits/2.png');\n" +
        "  void fetch('/digits/3.png');\n" +
        "}");
    });
    assertEquals(server.PREFIX + "/digits/2.png", request.url());
  }

  @PlaywrightTest
  @DisplayName("WA-122 should work with predicate")
  void shouldWorkWithPredicate(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    Request request = page.waitForRequest(r -> r.url().equals(server.PREFIX + "/digits/2.png"), () -> {
      page.evaluate("() => {\n" +
        "  void fetch('/digits/1.png');\n" +
        "  void fetch('/digits/2.png');\n" +
        "  void fetch('/digits/3.png');\n" +
        "}");
    });
    assertEquals(server.PREFIX + "/digits/2.png", request.url());
  }

  @PlaywrightTest
  @DisplayName("WA-123 should respect timeout")
  @Fixme("java: the message is 'Timeout 1ms exceeded' without the event name")
  void shouldRespectTimeout(Page page) {
    TimeoutError error = assertThrows(TimeoutError.class,
      () -> page.waitForRequest(r -> false, new Page.WaitForRequestOptions().setTimeout(1), () -> {}));
    assertTrue(error.getMessage().contains("Timeout 1ms exceeded while waiting for event \"request\""));
    // Java: there is no error.stack pointing at the api call.
  }

  @PlaywrightTest
  @DisplayName("WA-124 should respect default timeout")
  void shouldRespectDefaultTimeout(Page page) {
    page.setDefaultTimeout(1);
    assertThrows(TimeoutError.class, () -> page.waitForRequest(r -> false, () -> {}));
  }

  @PlaywrightTest
  @DisplayName("WA-125 should log the url")
  @Fixme("java: the message is 'Timeout 1000ms exceeded' without the wait log")
  void shouldLogTheUrl(Page page) {
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.waitForRequest(
      "long-long-long-long-long-long-long-long-long-long-long-long-long-long.css",
      new Page.WaitForRequestOptions().setTimeout(1000), () -> {}));
    assertTrue(error.getMessage().contains("waiting for request \"long-long-long-long-long-long-long-long-long-long…\""));
  }

  @PlaywrightTest
  @DisplayName("WA-126 should work with no timeout")
  void shouldWorkWithNoTimeout(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    Request request = page.waitForRequest(server.PREFIX + "/digits/2.png", new Page.WaitForRequestOptions().setTimeout(0), () -> {
      page.evaluate("() => window.builtins.setTimeout(() => {\n" +
        "  void fetch('/digits/1.png');\n" +
        "  void fetch('/digits/2.png');\n" +
        "  void fetch('/digits/3.png');\n" +
        "}, 50)");
    });
    assertEquals(server.PREFIX + "/digits/2.png", request.url());
  }

  @PlaywrightTest
  @DisplayName("WA-127 should work with url match")
  void shouldWorkWithUrlMatch(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    Request request = page.waitForRequest(Pattern.compile("digits/\\d\\.png"), () -> {
      page.evaluate("() => {\n" +
        "  void fetch('/digits/1.png');\n" +
        "}");
    });
    assertEquals(server.PREFIX + "/digits/1.png", request.url());
  }
}
