/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
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

package com.microsoft.playwright.tests.library;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.ConsoleMessage;
import com.microsoft.playwright.Dialog;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.JSHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.WebError;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.WebErrorLocation;
import org.junit.jupiter.api.DisplayName;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/library/browsercontext-events.spec.ts. */
public class TestBrowsercontextEvents {
  // Java: expect.poll(() => value).toBe(expected); waitForCondition() dispatches the events while waiting.
  private static <T> void expectPollToBe(Page page, Supplier<T> value, T expected) {
    try {
      page.waitForCondition(() -> expected.equals(value.get()), new Page.WaitForConditionOptions().setTimeout(5000));
    } catch (TimeoutError e) {
      // The assertion below reports the last value.
    }
    assertEquals(expected, value.get());
  }

  @PlaywrightTest
  @DisplayName("BC-032 console event should work @smoke")
  void consoleEventShouldWork(Page page) {
    ConsoleMessage message = page.context().waitForConsoleMessage(() -> page.evaluate("() => console.log('hello')"));

    assertEquals("hello", message.text());
    assertEquals(page, message.page());
  }

  @PlaywrightTest
  @DisplayName("BC-033 console event should work with element handles")
  void consoleEventShouldWorkWithElementHandles(Page page) {
    page.setContent("<body>hello</body>");
    ConsoleMessage message = page.context().waitForConsoleMessage(() -> page.evaluate("() => console.log(document.body)"));
    JSHandle body = message.args().get(0);
    assertEquals("BODY", body.evaluate("x => x.nodeName"));
    ((ElementHandle) body).click();
  }

  @PlaywrightTest
  @DisplayName("BC-034 console event should work in popup")
  void consoleEventShouldWorkInPopup(Page page) {
    ConsoleMessage[] message = {null};
    Page popup = page.waitForPopup(() -> {
      message[0] = page.context().waitForConsoleMessage(() -> page.evaluate("() => {\n" +
        "  const win = window.open('');\n" +
        "  win.console.log('hello');\n" +
        "}"));
    });

    assertEquals("hello", message[0].text());
    assertEquals(popup, message[0].page());
  }

  @PlaywrightTest
  @DisplayName("BC-035 console event should work in popup 2")
  void consoleEventShouldWorkInPopup2(Page page, BrowserName browserName) {
    assumeFalse(browserName == BrowserName.FIREFOX && !Env.isBidi(), "fixme: console message from javascript: url is not reported at all");

    ConsoleMessage[] message = {null};
    Page popup = page.context().waitForPage(() -> {
      message[0] = page.context().waitForConsoleMessage(
        new BrowserContext.WaitForConsoleMessageOptions().setPredicate(msg -> msg.type().equals("log")),
        () -> page.evaluate("async () => {\n" +
          "  const win = window.open('javascript:console.log(\"hello\")');\n" +
          "  await new Promise(f => window.builtins.setTimeout(f, 0));\n" +
          "  win.close();\n" +
          "}"));
    });

    assertEquals("hello", message[0].text());
    assertEquals(popup, message[0].page());
  }

  @PlaywrightTest
  @DisplayName("BC-036 console event should work in immediately closed popup")
  void consoleEventShouldWorkInImmediatelyClosedPopup(Page page, BrowserName browserName) {
    assumeFalse(browserName == BrowserName.FIREFOX && !Env.isBidi(), "fixme: console message is not reported at all");

    ConsoleMessage[] message = {null};
    Page popup = page.waitForPopup(() -> {
      message[0] = page.context().waitForConsoleMessage(() -> page.evaluate("async () => {\n" +
        "  const win = window.open();\n" +
        "  win.console.log('hello');\n" +
        "  win.close();\n" +
        "}"));
    });

    assertEquals("hello", message[0].text());
    assertEquals(popup, message[0].page());
  }

  @PlaywrightTest
  @DisplayName("BC-037 dialog event should work @smoke")
  void dialogEventShouldWork(Page page) throws Exception {
    // Java: waitForEvent('dialog') of the context and of the page are handlers registered before the evaluate;
    // the context handler accepts the dialog, the evaluate returns after that.
    Dialog[] dialog1 = {null};
    Dialog[] dialog2 = {null};
    page.context().onDialog(dialog -> {
      dialog1[0] = dialog;
      dialog.accept("hello");
    });
    page.onDialog(dialog -> dialog2[0] = dialog);
    Object result = page.evaluate("() => prompt('hey?')");
    page.waitForCondition(() -> dialog1[0] != null && dialog2[0] != null);

    assertEquals(dialog2[0], dialog1[0]);
    assertEquals("hey?", dialog1[0].message());
    assertEquals(page, dialog1[0].page());
    assertEquals("hello", result);
  }

  @PlaywrightTest
  @DisplayName("BC-038 dialogclosed event should work")
  void dialogclosedEventShouldWork(Page page) throws Exception {
    // Java: the dialog is accepted in the handler, the handlers for dialogclosed are registered before the evaluate.
    Dialog[] opened = {null};
    Dialog[] closed1 = {null};
    Dialog[] closed2 = {null};
    page.context().onDialog(d -> {
      opened[0] = d;
      d.accept("hello");
    });
    page.context().onDialogClosed(d -> closed1[0] = d);
    page.onDialogClosed(d -> closed2[0] = d);
    Object result = page.evaluate("() => prompt('hey?')");
    page.waitForCondition(() -> closed1[0] != null && closed2[0] != null);
    assertEquals(opened[0], closed1[0]);
    assertEquals(opened[0], closed2[0]);
    assertEquals("hello", result);
  }

  @PlaywrightTest
  @DisplayName("BC-039 dialog event should work in popup")
  void dialogEventShouldWorkInPopup(Page page) throws Exception {
    Dialog[] dialog = {null};
    Page[] popup = {null};
    // Java: the dialog is accepted in the handler, the evaluate returns after that.
    page.context().onDialog(d -> {
      dialog[0] = d;
      d.accept("hello");
    });
    page.onPopup(p -> popup[0] = p);
    Object result = page.evaluate("() => {\n" +
      "  const win = window.open('');\n" +
      "  return win.prompt('hey?');\n" +
      "}");

    page.waitForCondition(() -> dialog[0] != null && popup[0] != null);

    assertEquals("hey?", dialog[0].message());
    assertEquals(popup[0], dialog[0].page());
    assertEquals("hello", result);
  }

  @PlaywrightTest
  @DisplayName("BC-040 dialog event should work in popup 2")
  @DisabledOnBrowser(value = BrowserName.FIREFOX, reason = "fixme: dialog from javascript: url is not reported at all")
  void dialogEventShouldWorkInPopup2(Page page) throws Exception {
    Dialog[] dialog = {null};
    // Java: the dialog is accepted in the handler.
    page.context().onDialog(d -> {
      dialog[0] = d;
      d.accept("hello");
    });
    page.evaluate("async () => {\n" +
      "  window.open('javascript:prompt(\"hey?\")');\n" +
      "}");

    page.waitForCondition(() -> dialog[0] != null);

    assertEquals("hey?", dialog[0].message());
    assertNull(dialog[0].page());
  }

  @PlaywrightTest
  @DisplayName("BC-041 dialog event should work in immediately closed popup")
  void dialogEventShouldWorkInImmediatelyClosedPopup(Page page) throws Exception {
    Dialog[] dialog = {null};
    Page[] popup = {null};
    // Java: the dialog is accepted in the handler, the evaluate returns after that.
    page.context().onDialog(d -> {
      dialog[0] = d;
      d.accept("hello");
    });
    page.onPopup(p -> popup[0] = p);
    Object result = page.evaluate("async () => {\n" +
      "  const win = window.open();\n" +
      "  const result = win.prompt('hey?');\n" +
      "  win.close();\n" +
      "  return result;\n" +
      "}");

    page.waitForCondition(() -> dialog[0] != null && popup[0] != null);

    assertEquals("hey?", dialog[0].message());
    assertEquals(popup[0], dialog[0].page());
    assertEquals("hello", result);
  }

  @PlaywrightTest
  @DisplayName("BC-042 dialog event should work with inline script tag")
  void dialogEventShouldWorkWithInlineScriptTag(Page page, TestServer server) throws Exception {
    server.setRoute("/popup.html", (req, res) -> {
      res.setHeader("content-type", "text/html");
      res.end("<script>window.result = prompt('hey?')</script>");
    });

    page.navigate(server.EMPTY_PAGE);
    page.setContent("<a href='popup.html' target=_blank>Click me</a>");

    Dialog[] dialog = {null};
    Page[] popup = {null};
    // Java: the dialog is accepted in the handler.
    page.context().onDialog(d -> {
      dialog[0] = d;
      d.accept("hello");
    });
    page.context().onPage(p -> popup[0] = p);
    page.click("a");
    page.waitForCondition(() -> dialog[0] != null && popup[0] != null);

    assertEquals("hey?", dialog[0].message());
    assertEquals(popup[0], dialog[0].page());
    expectPollToBe(popup[0], () -> popup[0].evaluate("window.result"), "hello");
  }

  @PlaywrightTest
  @DisplayName("BC-043 weberror event should work")
  void weberrorEventShouldWork(Page page) {
    WebError[] webError = {null};
    page.context().onWebError(error -> webError[0] = error);
    page.setContent("<script>throw new Error(\"boom\")</script>");
    page.waitForCondition(() -> webError[0] != null);
    assertEquals(page, webError[0].page());
    assertTrue(webError[0].error().contains("boom"));
  }

  @PlaywrightTest
  @DisplayName("BC-044 weberror event should include location")
  void weberrorEventShouldIncludeLocation(Page page, TestServer server) {
    server.setRoute("/error.js", (req, res) -> {
      res.setHeader("content-type", "application/javascript");
      res.end("\n" +
        "      function foo() {\n" +
        "        throw new Error('boom');\n" +
        "      }\n" +
        "      foo();\n" +
        "    ");
    });

    server.setRoute("/error.html", (req, res) -> {
      res.setHeader("content-type", "text/html");
      res.end("<script src=\"/error.js\"></script>");
    });

    WebError[] webError = {null};
    page.context().onWebError(error -> webError[0] = error);
    page.navigate(server.PREFIX + "/error.html");
    page.waitForCondition(() -> webError[0] != null);

    WebErrorLocation location = webError[0].location();
    assertEquals(server.PREFIX + "/error.js", location.url);
    assertEquals(2, location.line);
    assertTrue(location.column > 0); // column is not consistent across browsers
  }

  @PlaywrightTest
  @DisplayName("BC-045 pageload event should work @smoke")
  void pageloadEventShouldWork(Page page, TestServer server) {
    Page[] eventPage = {null};
    page.context().onPageLoad(p -> eventPage[0] = p);
    page.navigate(server.EMPTY_PAGE);
    page.waitForCondition(() -> eventPage[0] != null);
    assertEquals(page, eventPage[0]);
  }

  @PlaywrightTest
  @DisplayName("BC-046 framenavigated event should work @smoke")
  void framenavigatedEventShouldWork(Page page, TestServer server) {
    Frame[] frame = {null};
    page.context().onFrameNavigated(f -> frame[0] = f);
    page.navigate(server.EMPTY_PAGE);
    page.waitForCondition(() -> frame[0] != null);
    assertEquals(page.mainFrame(), frame[0]);
    assertEquals(server.EMPTY_PAGE, frame[0].url());
  }

  @PlaywrightTest
  @DisplayName("BC-047 pageclose event should work @smoke")
  void pagecloseEventShouldWork(BrowserContext context) {
    Page page = context.newPage();
    Page[] closed = {null};
    context.onPageClose(p -> closed[0] = p);
    page.close();
    context.waitForCondition(() -> closed[0] != null);
    assertEquals(page, closed[0]);
  }

  @PlaywrightTest
  @DisplayName("BC-048 frameattached event should work @smoke")
  void frameattachedEventShouldWork(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    Frame[] frame = {null};
    page.context().onFrameAttached(f -> frame[0] = f);
    page.evaluate("() => {\n" +
      "  const iframe = document.createElement('iframe');\n" +
      "  iframe.src = 'about:blank';\n" +
      "  document.body.appendChild(iframe);\n" +
      "}");
    page.waitForCondition(() -> frame[0] != null);
    assertEquals(page.mainFrame(), frame[0].parentFrame());
  }

  @PlaywrightTest
  @DisplayName("BC-049 framedetached event should work @smoke")
  void framedetachedEventShouldWork(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.evaluate("() => {\n" +
      "  const iframe = document.createElement('iframe');\n" +
      "  iframe.id = 'x';\n" +
      "  iframe.src = 'about:blank';\n" +
      "  document.body.appendChild(iframe);\n" +
      "}");
    page.waitForSelector("iframe");
    Frame[] frame = {null};
    page.context().onFrameDetached(f -> frame[0] = f);
    page.evaluate("() => document.getElementById('x').remove()");
    page.waitForCondition(() -> frame[0] != null);
    assertEquals(page.mainFrame(), frame[0].parentFrame());
  }

  @PlaywrightTest
  @DisplayName("BC-050 download event should work @smoke")
  void downloadEventShouldWork(Page page, TestServer server) {
    server.setRoute("/download", (req, res) -> {
      res.setHeader("Content-Type", "application/octet-stream");
      res.setHeader("Content-Disposition", "attachment; filename=file.txt");
      res.end("Hello world");
    });
    page.setContent("<a href=\"" + server.PREFIX + "/download\">download</a>");
    Download[] download = {null};
    page.context().onDownload(d -> download[0] = d);
    page.click("a");
    page.waitForCondition(() -> download[0] != null);
    assertEquals("file.txt", download[0].suggestedFilename());
    assertEquals(page, download[0].page());
  }
}
