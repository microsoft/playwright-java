/*
 * Copyright 2017 Google Inc. All rights reserved.
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
import com.microsoft.playwright.Response;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/page-wait-for-response.spec.ts. */
public class TestPageWaitForResponse {
  @PlaywrightTest
  @DisplayName("WA-129 should work")
  void shouldWork(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    Response response = page.waitForResponse(server.PREFIX + "/digits/2.png", () -> {
      page.evaluate("() => {\n" +
        "  void fetch('/digits/1.png');\n" +
        "  void fetch('/digits/2.png');\n" +
        "  void fetch('/digits/3.png');\n" +
        "}");
    });
    assertEquals(server.PREFIX + "/digits/2.png", response.url());
  }

  @PlaywrightTest
  @DisplayName("WA-130 should respect timeout")
  void shouldRespectTimeout(Page page) {
    assertThrows(TimeoutError.class,
      () -> page.waitForResponse(r -> false, new Page.WaitForResponseOptions().setTimeout(1), () -> {}));
  }

  @PlaywrightTest
  @DisplayName("WA-131 should respect default timeout")
  void shouldRespectDefaultTimeout(Page page) {
    page.setDefaultTimeout(1);
    TimeoutError error = assertThrows(TimeoutError.class, () -> page.waitForResponse(r -> false, () -> {}));
    // Error stack should point to the api call.
    // Java: error.stack becomes the Java stack trace, which must lead to the call in this file.
    boolean found = false;
    for (StackTraceElement element : error.getStackTrace()) {
      found = found || element.getClassName().startsWith(TestPageWaitForResponse.class.getName());
    }
    assertTrue(found);
  }

  @PlaywrightTest
  @DisplayName("WA-132 should log the url")
  @Fixme("java: the message is 'Timeout 1000ms exceeded' without the wait log")
  void shouldLogTheUrl(Page page) {
    PlaywrightException error1 = assertThrows(PlaywrightException.class,
      () -> page.waitForResponse("foo.css", new Page.WaitForResponseOptions().setTimeout(1000), () -> {}));
    assertTrue(error1.getMessage().contains("waiting for response \"foo.css\""));
    PlaywrightException error2 = assertThrows(PlaywrightException.class,
      () -> page.waitForResponse(Pattern.compile("foo.css", Pattern.CASE_INSENSITIVE), new Page.WaitForResponseOptions().setTimeout(1000), () -> {}));
    assertTrue(error2.getMessage().contains("waiting for response /foo.css/i"));
  }

  @PlaywrightTest
  @DisplayName("WA-133 should work with predicate")
  void shouldWorkWithPredicate(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    Response response = page.waitForResponse(r -> r.url().equals(server.PREFIX + "/digits/2.png"), () -> {
      page.evaluate("() => {\n" +
        "  void fetch('/digits/1.png');\n" +
        "  void fetch('/digits/2.png');\n" +
        "  void fetch('/digits/3.png');\n" +
        "}");
    });
    assertEquals(server.PREFIX + "/digits/2.png", response.url());
  }

  @PlaywrightTest
  @DisplayName("WA-134 should work with async predicate")
  void shouldWorkWithAsyncPredicate(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    AtomicReference<Response> response2 = new AtomicReference<>();
    Response response1 = page.waitForResponse(r -> {
      String text = r.text();
      return text.contains("contents of the file");
    }, () -> {
      response2.set(page.waitForResponse(r -> {
        String text = r.text();
        return text.contains("bar");
      }, () -> {
        page.evaluate("() => {\n" +
          "  void fetch('/simple.json').then(r => r.json());\n" +
          "  void fetch('/file-to-upload.txt').then(r => r.text());\n" +
          "}");
      }));
    });
    assertEquals(server.PREFIX + "/file-to-upload.txt", response1.url());
    assertEquals(server.PREFIX + "/simple.json", response2.get().url());
  }

  @PlaywrightTest
  @DisplayName("WA-135 sync predicate should be only called once")
  void syncPredicateShouldBeOnlyCalledOnce(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    AtomicInteger counter = new AtomicInteger(0);
    Response response = page.waitForResponse(r -> {
      counter.incrementAndGet();
      return r.url().equals(server.PREFIX + "/digits/1.png");
    }, () -> {
      page.evaluate("async () => {\n" +
        "  await fetch('/digits/1.png');\n" +
        "  await fetch('/digits/2.png');\n" +
        "  await fetch('/digits/3.png');\n" +
        "}");
    });
    assertEquals(server.PREFIX + "/digits/1.png", response.url());
    assertEquals(1, counter.get());
  }

  @PlaywrightTest
  @DisplayName("WA-136 should work with no timeout")
  void shouldWorkWithNoTimeout(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    Response response = page.waitForResponse(server.PREFIX + "/digits/2.png", new Page.WaitForResponseOptions().setTimeout(0), () -> {
      page.evaluate("() => window.builtins.setTimeout(() => {\n" +
        "  void fetch('/digits/1.png');\n" +
        "  void fetch('/digits/2.png');\n" +
        "  void fetch('/digits/3.png');\n" +
        "}, 50)");
    });
    assertEquals(server.PREFIX + "/digits/2.png", response.url());
  }
}
