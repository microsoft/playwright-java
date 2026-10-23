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

import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.PlaywrightTest;

import java.io.OutputStreamWriter;
import java.io.Writer;

import static com.microsoft.playwright.Utils.waitForCondition;
import static com.microsoft.playwright.fixtures.BrowserName.FIREFOX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestBrowserContextEvents {
  @PlaywrightTest
  void consoleEventShouldWorkSmoke(Page page, BrowserContext context) {
    ConsoleMessage message = context.waitForConsoleMessage(() -> {
      page.evaluate("console.log('hello')");
    });
    assertEquals("hello", message.text());
    assertEquals(page, message.page());
  }

  @PlaywrightTest
  void consoleEventShouldWorkInPopup(Page page, BrowserContext context) {
    Page[] popup = { null };
    ConsoleMessage message = context.waitForConsoleMessage(() -> {
      popup[0] = page.waitForPopup(() -> {
        page.evaluate("const win = window.open('');\n" +
          "win.console.log('hello');\n");
      });
    });
    assertEquals("hello", message.text());
    assertEquals(popup[0], message.page());
  }

  @PlaywrightTest
  @DisabledOnBrowser(value = FIREFOX, reason = "console message from javascript: url is not reported at all")
  void consoleEventShouldWorkInPopup2(Page page, BrowserContext context) {
    Page[] popup = { null };
    ConsoleMessage message = context.waitForConsoleMessage(
      new BrowserContext.WaitForConsoleMessageOptions().setPredicate(msg -> "log".equals(msg.type())),
      () -> {
        popup[0] = context.waitForPage(() -> {
          page.evaluate("async () => {\n" +
            "  const win = window.open('javascript:console.log(\"hello\")');\n" +
            "  await new Promise(f => setTimeout(f, 0));\n" +
            "  win.close();\n" +
            "}");
        });
      });
    assertEquals("hello", message.text());
    assertEquals(popup[0], message.page());
  }

  @PlaywrightTest
  @DisabledOnBrowser(value = FIREFOX, reason = "console message is not reported at all")
  void consoleEventShouldWorkInImmediatelyClosedPopup(Page page, BrowserContext context) {
    Page[] popup = { null };
    ConsoleMessage message = context.waitForConsoleMessage(() -> {
      popup[0] = page.waitForPopup(() -> {
        page.evaluate("async () => {\n" +
          "      const win = window.open();\n" +
          "      win.console.log('hello');\n" +
          "      win.close();\n" +
          "    }\n");
      });
    });
    assertEquals("hello", message.text());
    assertEquals(popup[0], message.page());
  }

  @PlaywrightTest
  void dialogEventShouldWorkSmoke(Page page, BrowserContext context) {
    Dialog[] dialog = { null };
    context.onDialog(d -> {
      dialog[0] = d;
      dialog[0].accept("hello");
    });
    Object result = page.evaluate("prompt('hey?')");
    assertEquals("hello", result);
    context.waitForCondition(() -> dialog[0] != null);
    assertEquals("hey?", dialog[0].message());
    assertEquals(page, dialog[0].page());
  }

  @PlaywrightTest
  void dialogEventShouldWorkInPopup(Page page, BrowserContext context) {
    Dialog[] dialog = { null };
    context.onDialog(d -> {
      dialog[0] = d;
      d.accept("hello");
    });
    Page popup = page.waitForPopup(() -> {
      Object result = page.evaluate("() => {\n" +
        "    const win = window.open('');\n" +
        "    return win.prompt('hey?');\n" +
        "  }");
      assertEquals("hello", result);
    });
    assertEquals("hey?", dialog[0].message());
    assertEquals(popup, dialog[0].page());
  }

  @PlaywrightTest
  @DisabledOnBrowser(value = FIREFOX, reason = "dialog from javascript: url is not reported at all")
  void dialogEventShouldWorkInPopup2(Page page, BrowserContext context) {
    Dialog[] dialog = { null };
    context.onDialog(d -> {
      dialog[0] = d;
      d.accept("hello");
    });
    page.evaluate("window.open('javascript:prompt(\"hey?\")');");
    context.waitForCondition(() -> dialog[0] != null);
    assertEquals("hey?", dialog[0].message());
    assertEquals(null, dialog[0].page());
  }

  @PlaywrightTest
  void dialogEventShouldWorkInImmdiatelyClosedPopup(Page page, BrowserContext context) {
    Dialog[] dialog = { null };
    context.onDialog(d -> {
      dialog[0] = d;
      d.accept("hello");
    });
    Page popup = page.waitForPopup(() -> {
      Object result = page.evaluate("async () => {\n" +
        "    const win = window.open();\n" +
        "    const result = win.prompt('hey?');\n" +
        "    win.close();\n" +
        "    return result;\n" +
        "  }");
      assertEquals("hello", result);
    });
    assertEquals("hey?", dialog[0].message());
    assertEquals(popup, dialog[0].page());
  }

  @PlaywrightTest
  void dialogEventShouldWorkWithInlineScriptTag(Page page, BrowserContext context, Server server) {
    server.setRoute("/popup.html", exchange -> {
      exchange.getResponseHeaders().add("content-type", "text/html");
      exchange.sendResponseHeaders(200, 0);
      try (Writer writer = new OutputStreamWriter(exchange.getResponseBody())) {
        writer.write("<script>window.result = prompt('hey?')</script>");
      }
    });
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<a href='popup.html' target=_blank>Click me</a>");
    Dialog[] dialog = { null };
    context.onDialog(d -> {
      dialog[0] = d;
      d.accept("hello");
    });
    Page popup = context.waitForPage(() -> page.click("a"));
    page.waitForCondition(() -> dialog[0] != null);
    assertEquals("hey?", dialog[0].message());
    assertEquals(popup, dialog[0].page());
    page.waitForCondition(() -> "hello".equals(popup.evaluate("window.result")),
      new Page.WaitForConditionOptions().setTimeout(5_000));
  }

  @PlaywrightTest
  void pageErrorEventShouldWork(Page page, BrowserContext context) {
    WebError[] webError = { null };
    context.onWebError(e -> {
      webError[0] = e;
    });
    page.setContent("<script>throw new Error('boom')</script>");
    waitForCondition(page, () -> webError[0] != null);
    assertEquals(page, webError[0].page());
    assertTrue(webError[0].error().contains("boom"), webError[0].error());
  }

  @PlaywrightTest
  void weberrorEventShouldIncludeLocation(Page page, BrowserContext context, Server server) {
    server.setRoute("/error.js", exchange -> {
      exchange.getResponseHeaders().add("content-type", "application/javascript");
      exchange.sendResponseHeaders(200, 0);
      try (Writer writer = new OutputStreamWriter(exchange.getResponseBody())) {
        writer.write("\nfunction foo() {\n  throw new Error('boom');\n}\nfoo();\n");
      }
    });
    server.setRoute("/error.html", exchange -> {
      exchange.getResponseHeaders().add("content-type", "text/html");
      exchange.sendResponseHeaders(200, 0);
      try (Writer writer = new OutputStreamWriter(exchange.getResponseBody())) {
        writer.write("<script src=\"/error.js\"></script>");
      }
    });
    WebError[] webError = { null };
    context.onWebError(e -> webError[0] = e);
    page.navigate(server.PREFIX + "/error.html");
    waitForCondition(page, () -> webError[0] != null);
    com.microsoft.playwright.options.WebErrorLocation location = webError[0].location();
    assertEquals(server.PREFIX + "/error.js", location.url);
    assertEquals(2, location.line);
    assertTrue(location.column > 0, "expected column > 0, got " + location.column);
  }

  @PlaywrightTest
  void pageLoadEventShouldWork(Page page, BrowserContext context, Server server) {
    Page[] loaded = { null };
    context.onPageLoad(p -> loaded[0] = p);
    page.navigate(server.EMPTY_PAGE);
    waitForCondition(page, () -> loaded[0] != null);
    assertEquals(page, loaded[0]);
  }

  @PlaywrightTest
  void frameNavigatedEventShouldWork(Page page, BrowserContext context, Server server) {
    Frame[] navigated = { null };
    context.onFrameNavigated(f -> navigated[0] = f);
    page.navigate(server.EMPTY_PAGE);
    waitForCondition(page, () -> navigated[0] != null);
    assertEquals(page.mainFrame(), navigated[0]);
    assertEquals(server.EMPTY_PAGE, navigated[0].url());
  }

  @PlaywrightTest
  void pageCloseEventShouldWork(Page page, BrowserContext context) {
    Page newPage = context.newPage();
    Page[] closed = { null };
    context.onPageClose(p -> closed[0] = p);
    newPage.close();
    waitForCondition(page, () -> closed[0] != null);
    assertEquals(newPage, closed[0]);
  }

  @PlaywrightTest
  void frameAttachedEventShouldWork(Page page, BrowserContext context, Server server) {
    page.navigate(server.EMPTY_PAGE);
    Frame[] attached = { null };
    context.onFrameAttached(f -> attached[0] = f);
    page.evaluate("() => {\n" +
      "  const iframe = document.createElement('iframe');\n" +
      "  iframe.src = 'about:blank';\n" +
      "  document.body.appendChild(iframe);\n" +
      "}");
    waitForCondition(page, () -> attached[0] != null);
    assertEquals(page.mainFrame(), attached[0].parentFrame());
  }

  @PlaywrightTest
  void frameDetachedEventShouldWork(Page page, BrowserContext context, Server server) {
    page.navigate(server.EMPTY_PAGE);
    page.evaluate("() => {\n" +
      "  const iframe = document.createElement('iframe');\n" +
      "  iframe.id = 'x';\n" +
      "  iframe.src = 'about:blank';\n" +
      "  document.body.appendChild(iframe);\n" +
      "}");
    page.waitForSelector("iframe");
    Frame[] detached = { null };
    context.onFrameDetached(f -> detached[0] = f);
    page.evaluate("() => document.getElementById('x').remove()");
    waitForCondition(page, () -> detached[0] != null);
    assertEquals(page.mainFrame(), detached[0].parentFrame());
  }

  @PlaywrightTest
  void dialogClosedEventShouldWork(Page page, BrowserContext context) {
    Dialog[] dialog = {null};
    Dialog[] closedOnContext = {null};
    Dialog[] closedOnPage = {null};
    context.onDialogClosed(d -> closedOnContext[0] = d);
    page.onDialogClosed(d -> closedOnPage[0] = d);
    context.onDialog(d -> {
      dialog[0] = d;
      d.accept("hello");
    });
    Object result = page.evaluate("prompt('hey?')");
    assertEquals("hello", result);
    context.waitForCondition(() -> closedOnContext[0] != null && closedOnPage[0] != null);
    assertEquals(dialog[0], closedOnContext[0]);
    assertEquals(dialog[0], closedOnPage[0]);
  }
}
