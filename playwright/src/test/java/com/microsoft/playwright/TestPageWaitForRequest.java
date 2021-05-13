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

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class TestPageWaitForRequest extends TestBase {

  @Test
  void shouldWork() {
    page.navigate(getServer().EMPTY_PAGE);
    Request request = page.waitForRequest(getServer().PREFIX + "/digits/2.png", () -> {
      page.evaluate("() => {\n" +
        "  fetch('/digits/1.png');\n" +
        "  fetch('/digits/2.png');\n" +
        "  fetch('/digits/3.png');\n" +
        "}");
    });
    assertEquals(getServer().PREFIX + "/digits/2.png", request.url());
  }

  @Test
  void shouldWorkWithPredicate() {
    page.navigate(getServer().EMPTY_PAGE);
    Request request = page.waitForRequest(r -> r.url().equals(getServer().PREFIX + "/digits/2.png"), () -> {
      page.evaluate("() => {\n" +
        "  fetch('/digits/1.png');\n" +
        "  fetch('/digits/2.png');\n" +
        "  fetch('/digits/3.png');\n" +
        "}");
    });
    assertEquals(getServer().PREFIX + "/digits/2.png", request.url());
  }

  @Test
  void shouldRespectTimeout() {
    try {
      page.waitForRequest(url -> false, new Page.WaitForRequestOptions().setTimeout(1), () -> {});
      fail("did not throw");
    } catch (PlaywrightException e) {
      assertTrue(e.getMessage().contains("Timeout"), e.getMessage());
    }
  }

  @Test
  void shouldRespectDefaultTimeout() {
    page.setDefaultTimeout(1);
    try {
      page.waitForRequest(request -> false, () -> {});
      fail("did not throw");
    } catch (PlaywrightException e) {
      assertTrue(e.getMessage().contains("Timeout"), e.getMessage());
    }
  }

  @Test
  void shouldWorkWithNoTimeout() {
    page.navigate(getServer().EMPTY_PAGE);
    Request request = page.waitForRequest(
      getServer().PREFIX + "/digits/2.png",
      new Page.WaitForRequestOptions().setTimeout(0),() -> {
        page.evaluate("() => setTimeout(() => {\n" +
          "  fetch('/digits/1.png');\n" +
          "  fetch('/digits/2.png');\n" +
          "  fetch('/digits/3.png');\n" +
          "}, 50)");
      });
    assertEquals(getServer().PREFIX + "/digits/2.png", request.url());
  }

  @Test
  void shouldWorkWithUrlMatch() {
    page.navigate(getServer().EMPTY_PAGE);
    Request request = page.waitForRequest(Pattern.compile(".*digits/\\d\\.png"), () -> {
      page.evaluate("() => {\n" +
        "  fetch('/digits/1.png');\n" +
        "}");
    });
    assertEquals(getServer().PREFIX + "/digits/1.png", request.url());
  }

  void shouldWorkWithUrlMatchRegularExpressionFromADifferentContext() {
    // Node.js specific
  }
}
