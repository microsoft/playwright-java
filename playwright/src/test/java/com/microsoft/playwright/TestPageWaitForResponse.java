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

import static com.microsoft.playwright.Page.EventType.REQUEST;
import static com.microsoft.playwright.Page.EventType.RESPONSE;
import static org.junit.jupiter.api.Assertions.*;

public class TestPageWaitForResponse extends TestBase {
  @Test
  void shouldWork() {
    page.navigate(server.EMPTY_PAGE);
    Deferred<Response> response = page.futureResponse(server.PREFIX + "/digits/2.png");
    page.evaluate("() => {\n" +
      "  fetch('/digits/1.png');\n" +
      "  fetch('/digits/2.png');\n" +
      "  fetch('/digits/3.png');\n" +
      "}");
    assertEquals(server.PREFIX + "/digits/2.png", response.get().url());
  }

  @Test
  void shouldRespectTimeout() {
    page.navigate(server.EMPTY_PAGE);
    Deferred<Response> response = page.futureResponse(url -> url.equals(server.PREFIX + "/digits/2.png"));
    page.evaluate("() => {\n" +
      "  fetch('/digits/1.png');\n" +
      "  fetch('/digits/2.png');\n" +
      "  fetch('/digits/3.png');\n" +
      "}");
    assertEquals(server.PREFIX + "/digits/2.png", response.get().url());
  }

  @Test
  void shouldWorkWithPredicate() {
    page.navigate(server.EMPTY_PAGE);
    Deferred<Response> response = page.futureResponse(url -> url.equals(server.PREFIX + "/digits/2.png"));
    page.evaluate("() => {\n" +
      "  fetch('/digits/1.png');\n" +
      "  fetch('/digits/2.png');\n" +
      "  fetch('/digits/3.png');\n" +
      "}");
    assertEquals(server.PREFIX + "/digits/2.png", response.get().url());
  }

  @Test
  void shouldRespectDefaultTimeout() {
    page.setDefaultTimeout(1);
    try {
      page.futureEvent(RESPONSE, url -> false).get();
      fail("did not throw");
    } catch (PlaywrightException e) {
      assertTrue(e.getMessage().contains("Timeout"), e.getMessage());
    }
  }

  @Test
  void shouldWorkWithNoTimeout() {
    page.navigate(server.EMPTY_PAGE);
    Deferred<Response> response = page.futureResponse(server.PREFIX + "/digits/2.png",
      new Page.FutureResponseOptions().withTimeout(0));
    page.evaluate("() => setTimeout(() => {\n" +
      "  fetch('/digits/1.png');\n" +
      "  fetch('/digits/2.png');\n" +
      "  fetch('/digits/3.png');\n" +
      "}, 50)");
    assertEquals(server.PREFIX + "/digits/2.png", response.get().url());
  }
}
