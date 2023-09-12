package com.microsoft.playwright;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;

import java.io.OutputStreamWriter;
import java.io.Writer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestBrowserContextEvents extends TestBase {
  @Test
  void consoleEventShouldWorkSmoke() {
    ConsoleMessage message = context.waitForConsoleMessage(() -> {
      page.evaluate("console.log('hello')");
    });
    assertEquals("hello", message.text());
    assertEquals(page, message.page());
  }

  @Test
  void consoleEventShouldWorkInPopup() {
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

  @Test
  @DisabledIf(value="com.microsoft.playwright.TestBase#isFirefox", disabledReason="console message from javascript: url is not reported at all")
  void consoleEventShouldWorkInPopup2() {
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

  @Test
  @DisabledIf(value="com.microsoft.playwright.TestBase#isFirefox", disabledReason="console message is not reported at all")
  void consoleEventShouldWorkInImmediatelyClosedPopup() {
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

  @Test
  void dialogEventShouldWorkSmoke() {
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

  @Test
  void dialogEventShouldWorkInPopup() {
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

  @Test
  @DisabledIf(value="com.microsoft.playwright.TestBase#isFirefox", disabledReason="dialog from javascript: url is not reported at all")
  void dialogEventShouldWorkInPopup2() {
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

  @Test
  void dialogEventShouldWorkInImmdiatelyClosedPopup() {
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

  @Test
  void dialogEventShouldWorkWithInlineScriptTag() {
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

  @Test
  void pageErrorEventShouldWork() {
    WebError[] webError = { null };
    context.onWebError(e -> {
      webError[0] = e;
    });
    page.setContent("<script>throw new Error('boom')</script>");
    waitForCondition(() -> webError[0] != null);
    assertEquals(page, webError[0].page());
    assertTrue(webError[0].error().contains("boom"), webError[0].error());
  }

}
