/**
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

import com.microsoft.playwright.ConsoleMessage;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/page-event-pageerror.spec.ts. */
public class TestPageEventPageerror {
  // Java: the pageerror event is a String "name: message\nstack"; this splits it into the
  // name, message and stack of the Error that upstream receives.
  private static class PageError {
    final String name;
    final String message;
    final String stack;

    PageError(String error) {
      int newline = error.indexOf('\n');
      String first = newline == -1 ? error : error.substring(0, newline);
      stack = newline == -1 ? "" : error.substring(newline + 1);
      int colon = first.indexOf(": ");
      name = first.substring(0, colon);
      message = first.substring(colon + 2);
    }
  }

  // Java: stands for Promise.all([page.waitForEvent('pageerror'), action])
  private static PageError waitForPageError(Page page, Runnable action) {
    List<String> errors = new ArrayList<>();
    Consumer<String> handler = errors::add;
    page.onPageError(handler);
    action.run();
    page.waitForCondition(() -> !errors.isEmpty());
    page.offPageError(handler);
    return new PageError(errors.get(0));
  }

  @PlaywrightTest
  @DisplayName("ER-001 should fire")
  @Fixme(value = "java: the bundled driver 1.64.1 reports zero-based locations for the cross-origin script error (error.js:13:10 instead of error.js:14:11); fixed upstream in microsoft/playwright#43468", browsers = BrowserName.CHROMIUM)
  void shouldFire(Page page, TestServer server, BrowserName browserName) {
    String url = server.PREFIX + "/error.html";
    PageError error = waitForPageError(page, () -> page.navigate(url));
    assertEquals("Error", error.name);
    assertEquals("Fancy error!", error.message);
    if (browserName == BrowserName.CHROMIUM || (browserName == BrowserName.FIREFOX && Env.isBidi())) {
      assertEquals("Error: Fancy error!\n" +
        "    at c (myscript.js:14:11)\n" +
        "    at b (myscript.js:10:5)\n" +
        "    at a (myscript.js:6:5)\n" +
        "    at myscript.js:3:1", error.stack);
    } else if (browserName == BrowserName.WEBKIT) {
      assertEquals("Error: Fancy error!\n" +
        "    at c (" + url + ":14:36)\n" +
        "    at b (" + url + ":10:6)\n" +
        "    at a (" + url + ":6:6)\n" +
        "    at global code (" + url + ":3:2)", error.stack);
    } else if (browserName == BrowserName.FIREFOX) {
      assertEquals("Error: Fancy error!\n" +
        "    at c (myscript.js:14:11)\n" +
        "    at b (myscript.js:10:5)\n" +
        "    at a (myscript.js:6:5)\n" +
        "    at  (myscript.js:3:1)", error.stack);
    }

    // The same script from another origin: Chromium reports it without an exception object,
    // so the stack is built from the CDP call frames, which are zero-based.
    String crossOriginUrl = server.CROSS_PROCESS_PREFIX + "/error.js";
    server.setRoute("/error.js", (req, res) -> {
      res.setHeader("Content-Type", "text/javascript");
      // Same lines as error.html, so the stack has the same locations.
      res.end("\n" +
        "console.error('Not a JS error');\n" +
        "a();\n" +
        "\n" +
        "function a() {\n" +
        "    b();\n" +
        "}\n" +
        "\n" +
        "function b() {\n" +
        "    c();\n" +
        "}\n" +
        "\n" +
        "function c() {\n" +
        "    throw new Error('Fancy error!');\n" +
        "}\n");
    });
    server.setRoute("/cross-origin-error.html", (req, res) -> {
      res.setHeader("Content-Type", "text/html");
      res.end("<script src=\"" + crossOriginUrl + "\"></script>");
    });
    PageError crossOriginError = waitForPageError(page, () -> page.navigate(server.PREFIX + "/cross-origin-error.html"));
    assertEquals("Fancy error!", crossOriginError.message);
    if (browserName == BrowserName.CHROMIUM) {
      assertEquals("Uncaught Error: Fancy error!\n" +
        "    at c (" + crossOriginUrl + ":14:11)\n" +
        "    at b (" + crossOriginUrl + ":10:5)\n" +
        "    at a (" + crossOriginUrl + ":6:5)\n" +
        "    at <anonymous> (" + crossOriginUrl + ":3:1)", crossOriginError.stack);
    } else if (browserName == BrowserName.WEBKIT) {
      assertEquals("Error: Fancy error!\n" +
        "    at c (" + crossOriginUrl + ":14:36)\n" +
        "    at b (" + crossOriginUrl + ":10:6)\n" +
        "    at a (" + crossOriginUrl + ":6:6)\n" +
        "    at global code (" + crossOriginUrl + ":3:2)", crossOriginError.stack);
    } else if (browserName == BrowserName.FIREFOX && !Env.isBidi()) {
      assertEquals("Error: Fancy error!\n" +
        "    at c (" + crossOriginUrl + ":14:11)\n" +
        "    at b (" + crossOriginUrl + ":10:5)\n" +
        "    at a (" + crossOriginUrl + ":6:5)\n" +
        "    at  (" + crossOriginUrl + ":3:1)", crossOriginError.stack);
    }
  }

  @PlaywrightTest
  @DisplayName("ER-002 should not receive console message for pageError")
  void shouldNotReceiveConsoleMessageForPageError(Page page, TestServer server) {
    List<ConsoleMessage> messages = new ArrayList<>();
    page.onConsoleMessage(messages::add);
    waitForPageError(page, () -> page.navigate(server.PREFIX + "/error.html"));
    assertEquals(1, messages.size());
  }

  @PlaywrightTest
  @DisplayName("ER-003 should contain sourceURL")
  @DisabledOnBrowser(value = BrowserName.WEBKIT, reason = "fail: ")
  void shouldContainSourceURL(Page page, TestServer server, BrowserName browserName) {
    PageError error = waitForPageError(page, () -> page.navigate(server.PREFIX + "/error.html"));
    assertTrue(error.stack.contains("myscript.js"));
  }

  @PlaywrightTest
  @DisplayName("ER-004 should contain the Error.name property")
  void shouldContainTheErrorNameProperty(Page page) {
    PageError error = waitForPageError(page, () -> page.evaluate("() => {\n" +
      "      window.builtins.setTimeout(() => {\n" +
      "        const error = new Error('my-message');\n" +
      "        error.name = 'my-name';\n" +
      "        throw error;\n" +
      "      }, 0);\n" +
      "    }"));
    assertEquals("my-name", error.name);
    assertEquals("my-message", error.message);
  }

  @PlaywrightTest
  @DisplayName("ER-005 should support an empty Error.name property")
  void shouldSupportAnEmptyErrorNameProperty(Page page) {
    PageError error = waitForPageError(page, () -> page.evaluate("() => {\n" +
      "      window.builtins.setTimeout(() => {\n" +
      "        const error = new Error('my-message');\n" +
      "        error.name = '';\n" +
      "        throw error;\n" +
      "      }, 0);\n" +
      "    }"));
    assertEquals("", error.name);
    assertEquals("my-message", error.message);
  }

  @PlaywrightTest
  @DisplayName("ER-006 should handle odd values")
  @Fixme("java: a null argument of evaluate() reaches the page as undefined, so 'throw value' gives the message 'undefined' instead of 'null'")
  void shouldHandleOddValues(Page page) {
    Object[][] cases = {
      {null, "null"},
      // Java: undefined and null are both null
      {null, "undefined"},
      {0, "0"},
      {"", ""},
    };
    for (Object[] c : cases) {
      Object value = c[0];
      String message = (String) c[1];
      PageError error = waitForPageError(page, () -> page.evaluate("value => {\n" +
        "        window.builtins.setTimeout(() => { throw value; }, 0);\n" +
        "      }", value));
      assertEquals(message, error.message);
    }
  }

  @PlaywrightTest
  @DisplayName("ER-007 should handle object")
  void shouldHandleObject(Page page, BrowserName browserName) {
    PageError error = waitForPageError(page, () -> page.evaluate("() => {\n" +
      "      window.builtins.setTimeout(() => { throw {}; }, 0);\n" +
      "    }"));
    assertEquals(browserName == BrowserName.CHROMIUM ? "Object" : "[object Object]", error.message);
  }

  @PlaywrightTest
  @DisplayName("ER-008 should handle window")
  void shouldHandleWindow(Page page, BrowserName browserName) {
    PageError error = waitForPageError(page, () -> page.evaluate("() => {\n" +
      "      window.builtins.setTimeout(() => { throw window; }, 0);\n" +
      "    }"));
    assertEquals(browserName == BrowserName.CHROMIUM ? "Window" : "[object Window]", error.message);
  }

  @PlaywrightTest
  @DisplayName("ER-009 should remove a listener of a non-existing event handler")
  void shouldRemoveAListenerOfANonExistingEventHandler(Page page) {
    page.offPageError(e -> {});
  }

  @PlaywrightTest
  @DisplayName("ER-010 should emit error from unhandled rejects")
  void shouldEmitErrorFromUnhandledRejects(Page page, BrowserName browserName) {
    PageError error = waitForPageError(page, () -> page.setContent("\n" +
      "        <script>\n" +
      "          Promise.reject(new Error('sad :('));\n" +
      "        </script>\n" +
      "    "));
    assertTrue(error.message.contains("sad :("));
  }

  @PlaywrightTest
  @DisplayName("ER-011 pageErrors should work")
  void pageErrorsShouldWork(Page page) {
    page.evaluate("async () => {\n" +
      "    for (let i = 0; i < 301; i++)\n" +
      "      window.builtins.setTimeout(() => { throw new Error('error' + i); }, 0);\n" +
      "    await new Promise(f => window.builtins.setTimeout(f, 2000));\n" +
      "  }");

    List<String> errors = page.pageErrors();
    List<String> messages = new ArrayList<>();
    for (String e : errors) {
      messages.add(new PageError(e).message);
    }

    List<String> expected = new ArrayList<>();
    for (int i = 201; i < 301; i++) {
      expected.add("error" + i);
    }

    assertTrue(messages.size() >= 100, "should be at least 100 errors");
    assertEquals(expected, messages.subList(messages.size() - expected.size(), messages.size()), "should return last errors");
  }

  @PlaywrightTest
  @DisplayName("ER-012 clearPageErrors should work")
  void clearPageErrorsShouldWork(Page page) {
    page.evaluate("() => {\n" +
      "    window.builtins.setTimeout(() => { throw new Error('error1'); }, 0);\n" +
      "    window.builtins.setTimeout(() => { throw new Error('error2'); }, 0);\n" +
      "  }");
    page.waitForTimeout(1000);

    List<String> errors = page.pageErrors();
    List<String> messages = new ArrayList<>();
    for (String e : errors) {
      messages.add(new PageError(e).message);
    }
    assertTrue(messages.contains("error1"));
    assertTrue(messages.contains("error2"));

    page.clearPageErrors();

    errors = page.pageErrors();
    assertEquals(Collections.emptyList(), errors);

    page.evaluate("() => {\n" +
      "    window.builtins.setTimeout(() => { throw new Error('error3'); }, 0);\n" +
      "  }");
    page.waitForTimeout(1000);

    errors = page.pageErrors();
    assertEquals(1, errors.size());
    assertTrue(new PageError(errors.get(0)).message.contains("error3"));
  }

  @PlaywrightTest
  @DisplayName("ER-013 should fire illegal character error")
  void shouldFireIllegalCharacterError(Page page, TestServer server, BrowserName browserName) {
    server.setRoute("/error.html", (req, res) -> {
      res.end("\n" +
        "      <!doctype html>\n" +
        "      <html lang=\"en\">\n" +
        "        <head>\n" +
        "          <meta charset=\"UTF-8\" />\n" +
        "          <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />\n" +
        "          <title>vite-project</title>\n" +
        "        </head>\n" +
        "        <body>\n" +
        "          <div id=\"app\"></div>\n" +
        "          <script >\n" +
        "            let a=10；//   !!!note: ； not ;\n" +
        "          </script>\n" +
        "        </body>\n" +
        "      </html>\n" +
        "    ");
    });
    String url = server.PREFIX + "/error.html";
    PageError error = waitForPageError(page, () -> page.navigate(url));
    if (browserName == BrowserName.CHROMIUM) {
      assertTrue(error.message.contains("Invalid or unexpected token"));
    } else if (browserName == BrowserName.WEBKIT && Env.isWindows && !"webkit-wsl".equals(Env.channel())) {
      assertTrue(error.message.contains("No identifiers allowed directly after numeric literal"));
    } else if (browserName == BrowserName.WEBKIT) {
      assertTrue(error.message.contains("Invalid character"));
    } else {
      assertTrue(error.message.contains("illegal character"));
    }
  }
}
