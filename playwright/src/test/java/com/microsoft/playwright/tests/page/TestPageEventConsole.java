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

import com.microsoft.playwright.ConsoleMessage;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.ConsoleMessagesFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.condition.OS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/page/page-event-console.spec.ts. */
public class TestPageEventConsole {
  // Java: page.once('console', m => message = m) is a handler that removes itself first.
  private static void onceConsoleMessage(Page page, ConsoleMessage[] message) {
    page.onConsoleMessage(new Consumer<ConsoleMessage>() {
      @Override
      public void accept(ConsoleMessage m) {
        page.offConsoleMessage(this);
        message[0] = m;
      }
    });
  }

  // Java: expect.poll(() => value).toEqual(expected); waitForCondition() dispatches the events while waiting.
  private static <T> void expectPollToEqual(Page page, Supplier<T> value, T expected) {
    try {
      page.waitForCondition(() -> expected.equals(value.get()), new Page.WaitForConditionOptions().setTimeout(5000));
    } catch (TimeoutError e) {
      // The assertion below reports the last value.
    }
    assertEquals(expected, value.get());
  }

  @PlaywrightTest
  @DisplayName("CM-001 should work @smoke")
  void shouldWork(Page page, BrowserName browserName) {
    ConsoleMessage[] message = {null};
    onceConsoleMessage(page, message);
    page.waitForConsoleMessage(() -> page.evaluate("() => console.log('hello', 5, { foo: 'bar' })"));
    if (browserName != BrowserName.FIREFOX || Env.isBidi()) {
      assertEquals("hello 5 {foo: bar}", message[0].text());
    } else {
      assertEquals("hello 5 JSHandle@object", message[0].text());
    }
    assertEquals("log", message[0].type());
    assertEquals("hello", message[0].args().get(0).jsonValue());
    assertEquals(5, message[0].args().get(1).jsonValue());
    assertEquals(mapOf("foo", "bar"), message[0].args().get(2).jsonValue());
  }

  @PlaywrightTest
  @DisplayName("CM-002 should emit same log twice")
  void shouldEmitSameLogTwice(Page page) {
    List<String> messages = new ArrayList<>();
    page.onConsoleMessage(m -> messages.add(m.text()));
    page.evaluate("() => {\n" +
      "  for (let i = 0; i < 2; ++i)\n" +
      "    console.log('hello');\n" +
      "}");
    assertEquals(Arrays.asList("hello", "hello"), messages);
  }

  @PlaywrightTest
  @DisplayName("CM-004 should work for different console API calls")
  void shouldWorkForDifferentConsoleAPICalls(Page page) {
    List<ConsoleMessage> messages = new ArrayList<>();
    page.onConsoleMessage(msg -> messages.add(msg));
    // All console events will be reported before `page.evaluate` is finished.
    page.evaluate("() => {\n" +
      "  // A pair of time/timeEnd generates only one Console API call.\n" +
      "  console.time('calling console.time');\n" +
      "  console.timeEnd('calling console.time');\n" +
      "  console.trace('calling console.trace');\n" +
      "  console.dir('calling console.dir');\n" +
      "  console.warn('calling console.warn');\n" +
      "  console.error('calling console.error');\n" +
      "  console.info('calling console.info');\n" +
      "  console.debug('calling console.debug');\n" +
      "  console.log(Promise.resolve('should not wait until resolved!'));\n" +
      "}");
    // WebKit uses console.debug() to report binding calls, make sure they don't get reported.
    page.exposeBinding("foobar", (source, args) -> page.evaluate("value => console.log(value)", args[0]));
    page.evaluate("() => window['foobar']('Using bindings')");

    assertEquals(Arrays.asList(
      "timeEnd", "trace", "dir", "warning", "error", "info", "debug", "log", "log"),
      messages.stream().map(msg -> msg.type()).collect(Collectors.toList()));
    assertTrue(messages.get(0).text().contains("calling console.time"));
    assertEquals(Arrays.asList(
      "calling console.trace",
      "calling console.dir",
      "calling console.warn",
      "calling console.error",
      "calling console.info",
      "calling console.debug",
      "Promise",
      "Using bindings"),
      messages.subList(1, messages.size()).stream().map(msg -> msg.text()).collect(Collectors.toList()));
  }

  @PlaywrightTest
  @DisplayName("CM-005 should format the message correctly with time/timeLog/timeEnd")
  void shouldFormatTheMessageCorrectlyWithTimeTimeLogTimeEnd(Page page, BrowserName browserName) {
    assumeFalse(browserName == BrowserName.FIREFOX && !Env.isBidi(), "fixme: https://github.com/microsoft/playwright/issues/10580");
    List<ConsoleMessage> messages = new ArrayList<>();
    page.onConsoleMessage(msg -> messages.add(msg));
    page.evaluate("async () => {\n" +
      "  console.time('foo time');\n" +
      "  await new Promise(x => window.builtins.setTimeout(x, 100));\n" +
      "  console.timeLog('foo time');\n" +
      "  await new Promise(x => window.builtins.setTimeout(x, 100));\n" +
      "  console.timeEnd('foo time');\n" +
      "}");
    assertEquals(2, messages.size());
    if (browserName == BrowserName.WEBKIT) {
      assertEquals("timeEnd", messages.get(0).type());
    } else if (browserName == BrowserName.CHROMIUM) {
      assertEquals("log", messages.get(0).type());
    } else if (browserName == BrowserName.FIREFOX) {
      assertEquals("timeLog", messages.get(0).type());
    }
    assertEquals("timeEnd", messages.get(1).type());

    // WebKit has no space before the unit: https://bugs.webkit.org/show_bug.cgi?id=233556
    // Firefox/Bidi has no fractional part.
    assertTrue(Pattern.compile("foo time: \\d+(.\\d+)? ?ms").matcher(messages.get(0).text()).find());
    assertTrue(Pattern.compile("foo time: \\d+(.\\d+)? ?ms").matcher(messages.get(1).text()).find());
  }

  @PlaywrightTest
  @DisplayName("CM-006 should not fail for window object")
  void shouldNotFailForWindowObject(Page page, BrowserName browserName) {
    ConsoleMessage[] message = {null};
    onceConsoleMessage(page, message);
    page.waitForConsoleMessage(() -> page.evaluate("() => console.error(window)"));
    if (browserName != BrowserName.FIREFOX || Env.isBidi()) {
      assertEquals("Window", message[0].text());
    } else {
      assertEquals("JSHandle@object", message[0].text());
    }
  }

  @PlaywrightTest
  @DisplayName("CM-007 should not fail for object that can't be coerced to a primitive")
  void shouldNotFailForObjectThatCantBeCoercedToAPrimitive(Page page, BrowserName browserName) {
    ConsoleMessage[] message = {null};
    onceConsoleMessage(page, message);
    page.waitForConsoleMessage(() -> page.evaluate("() => console.log({ toString() { return {}; }, valueOf() { return {}; } })"));
    if (browserName != BrowserName.FIREFOX || Env.isBidi()) {
      assertTrue(Pattern.compile("\\{toString: (Function)?, valueOf: (Function)?\\}").matcher(message[0].text()).find());
    } else {
      assertEquals("JSHandle@object", message[0].text());
    }
  }

  @PlaywrightTest
  @DisplayName("CM-008 should trigger correct Log")
  @DisabledOnBrowser(value = BrowserName.WEBKIT, os = OS.WINDOWS, reason = "Upstream issue https://bugs.webkit.org/show_bug.cgi?id=229515")
  void shouldTriggerCorrectLog(Page page, TestServer server) {
    page.navigate("about:blank");
    ConsoleMessage message = page.waitForConsoleMessage(() -> page.evaluate("async url => fetch(url).catch(e => {})", server.EMPTY_PAGE));
    assertTrue(Pattern.compile("Access-Control-Allow-Origin|CORS").matcher(message.text()).find());
    assertEquals("error", message.type());
  }

  @PlaywrightTest
  @DisplayName("CM-009 should have location for console API calls")
  void shouldHaveLocationForConsoleAPICalls(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    ConsoleMessage message = page.waitForConsoleMessage(
      new Page.WaitForConsoleMessageOptions().setPredicate(m -> m.text().startsWith("here:")),
      () -> page.navigate(server.PREFIX + "/consolelog.html"));
    assertEquals("log", message.type());
    // Engines have different column notion.
    // Java: location() is the string "url:lineNumber:columnNumber", it is split into the fields of the upstream object.
    java.util.regex.Matcher location = Pattern.compile("^(.*):(\\d+):(\\d+)$").matcher(message.location());
    assertTrue(location.matches());
    String url = location.group(1);
    int line = Integer.parseInt(location.group(2));
    int lineNumber = Integer.parseInt(location.group(2));
    assertEquals(mapOf(
      "url", server.PREFIX + "/consolelog.html",
      "line", 7,
      "lineNumber", 7), mapOf("url", url, "line", line, "lineNumber", lineNumber));
  }

  @PlaywrightTest
  @DisplayName("CM-010 should not throw when there are console messages in detached iframes")
  void shouldNotThrowWhenThereAreConsoleMessagesInDetachedIframes(Page page, TestServer server) {
    // @see https://github.com/GoogleChrome/puppeteer/issues/3865
    page.navigate(server.EMPTY_PAGE);
    Page popup = page.waitForPopup(() -> page.evaluate("async () => {\n" +
      "  // 1. Create a popup that Playwright is not connected to.\n" +
      "  const win = window.open('');\n" +
      "  window['_popup'] = win;\n" +
      "  if (window.document.readyState !== 'complete')\n" +
      "    await new Promise(f => window.addEventListener('load', f));\n" +
      "  // 2. In this popup, create an iframe that console.logs a message.\n" +
      "  win.document.body.innerHTML = `<iframe src='/consolelog.html'></iframe>`;\n" +
      "  const frame = win.document.querySelector('iframe');\n" +
      "  if (!frame.contentDocument || frame.contentDocument.readyState !== 'complete')\n" +
      "    await new Promise(f => frame.addEventListener('load', f));\n" +
      "  // 3. After that, remove the iframe.\n" +
      "  frame.remove();\n" +
      "}"));
    // 4. Connect to the popup and make sure it doesn't throw.
    assertEquals(2, popup.evaluate("1 + 1"));
  }

  @PlaywrightTest
  @DisplayName("CM-011 should use object previews for arrays and objects")
  void shouldUseObjectPreviewsForArraysAndObjects(Page page, BrowserName browserName) {
    String[] text = {null};
    page.onConsoleMessage(message -> {
      text[0] = message.text();
    });
    page.evaluate("() => console.log([1, 2, 3], { a: 1 }, window)");

    if (browserName != BrowserName.FIREFOX || Env.isBidi()) {
      assertEquals("[1, 2, 3] {a: 1} Window", text[0]);
    } else {
      assertEquals("Array JSHandle@object JSHandle@object", text[0]);
    }
  }

  @PlaywrightTest
  @DisplayName("CM-012 should use object previews for errors")
  void shouldUseObjectPreviewsForErrors(Page page, BrowserName browserName) {
    String[] text = {null};
    page.onConsoleMessage(message -> {
      text[0] = message.text();
    });
    page.evaluate("() => console.log(new Error('Exception'))");
    if (Env.isBidi() || browserName == BrowserName.FIREFOX) {
      assertEquals("Error", text[0]);
    } else if (browserName == BrowserName.CHROMIUM) {
      assertTrue(text[0].contains(".evaluate"));
    } else if (browserName == BrowserName.WEBKIT) {
      assertEquals("Error: Exception", text[0]);
    }
  }

  @PlaywrightTest
  @DisplayName("CM-013 do not update console count on unhandled rejections")
  void doNotUpdateConsoleCountOnUnhandledRejections(Page page) {
    List<String> messages = new ArrayList<>();
    Consumer<ConsoleMessage> consoleEventListener = m -> messages.add(m.text());
    page.onConsoleMessage(consoleEventListener);

    page.evaluate("() => {\n" +
      "  const fail = async () => Promise.reject(new Error('error'));\n" +
      "  console.log('begin');\n" +
      "  void fail();\n" +
      "  void fail();\n" +
      "  fail().catch(() => {\n" +
      "    console.log('end');\n" +
      "  });\n" +
      "}");

    expectPollToEqual(page, () -> messages, Arrays.asList("begin", "end"));
  }

  @PlaywrightTest
  @DisplayName("CM-014 should have timestamp")
  void shouldHaveTimestamp(Page page) {
    // Generous slack to absorb host wall-clock resolution (e.g. ~15.6ms on Windows)
    // vs sub-millisecond browser timestamps.
    double before = System.currentTimeMillis() - 100;
    ConsoleMessage message = page.waitForConsoleMessage(() -> page.evaluate("() => console.log('timestamp test')"));
    double after = System.currentTimeMillis() + 100;
    assertTrue(message.timestamp() >= before);
    assertTrue(message.timestamp() <= after);
  }

  @PlaywrightTest
  @DisplayName("CM-015 should have increasing timestamps")
  void shouldHaveIncreasingTimestamps(Page page) {
    List<ConsoleMessage> messages = new ArrayList<>();
    page.onConsoleMessage(msg -> messages.add(msg));
    page.evaluate("() => {\n" +
      "  console.log('first');\n" +
      "  console.log('second');\n" +
      "  console.log('third');\n" +
      "}");
    assertEquals(3, messages.size());
    for (int i = 1; i < messages.size(); i++) {
      assertTrue(messages.get(i).timestamp() >= messages.get(i - 1).timestamp());
    }
  }

  @PlaywrightTest
  @DisplayName("CM-016 should have timestamp in consoleMessages")
  void shouldHaveTimestampInConsoleMessages(Page page) {
    // Generous slack to absorb host wall-clock resolution (e.g. ~15.6ms on Windows)
    // vs sub-millisecond browser timestamps.
    double before = System.currentTimeMillis() - 100;
    page.evaluate("() => console.log('stored message')");
    double after = System.currentTimeMillis() + 100;
    List<ConsoleMessage> messages = page.consoleMessages();
    assertTrue(messages.size() >= 1);
    ConsoleMessage last = messages.get(messages.size() - 1);
    assertEquals("stored message", last.text());
    assertTrue(last.timestamp() >= before);
    assertTrue(last.timestamp() <= after);
  }

  @PlaywrightTest
  @DisplayName("CM-017 consoleMessages should work")
  void consoleMessagesShouldWork(Page page) {
    page.evaluate("() => {\n" +
      "  for (let i = 0; i < 301; i++)\n" +
      "    console.log('message' + i);\n" +
      "}");

    List<ConsoleMessage> messages = page.consoleMessages();
    List<Object> objects = messages.stream().map(m -> (Object) mapOf("text", m.text(), "type", m.type(), "page", m.page())).collect(Collectors.toList());

    List<Object> expected = new ArrayList<>();
    for (int i = 201; i < 301; i++) {
      expected.add(mapOf("text", "message" + i, "type", "log", "page", page));
    }

    assertTrue(objects.size() >= 100, "should be at least 100 messages");
    assertEquals(expected, objects.subList(objects.size() - expected.size(), objects.size()), "should return last messages");
  }

  @PlaywrightTest
  @DisplayName("CM-018 clearConsoleMessages should work")
  void clearConsoleMessagesShouldWork(Page page) {
    page.evaluate("() => {\n" +
      "  console.log('message1');\n" +
      "  console.log('message2');\n" +
      "}");

    List<ConsoleMessage> messages = page.consoleMessages();
    assertTrue(messages.stream().map(m -> m.text()).collect(Collectors.toList()).contains("message1"));
    assertTrue(messages.stream().map(m -> m.text()).collect(Collectors.toList()).contains("message2"));

    page.clearConsoleMessages();

    messages = page.consoleMessages();
    assertEquals(Arrays.asList(), messages);

    page.evaluate("() => console.log('message3')");
    messages = page.consoleMessages();
    assertEquals(1, messages.size());
    assertEquals("message3", messages.get(0).text());
  }

  @PlaywrightTest
  @DisplayName("CM-019 consoleMessages since-navigation filter should work")
  void consoleMessagesSinceNavigationFilterShouldWork(Page page, TestServer server) {
    page.evaluate("() => console.log('before navigation')");
    page.navigate(server.EMPTY_PAGE);
    page.evaluate("() => console.log('after navigation')");

    List<ConsoleMessage> all = page.consoleMessages(new Page.ConsoleMessagesOptions().setFilter(ConsoleMessagesFilter.ALL));
    assertTrue(all.stream().map(m -> m.text()).collect(Collectors.toList()).contains("before navigation"));
    assertTrue(all.stream().map(m -> m.text()).collect(Collectors.toList()).contains("after navigation"));

    // since-navigation is the default
    List<ConsoleMessage> sinceNav = page.consoleMessages();
    assertFalse(sinceNav.stream().map(m -> m.text()).collect(Collectors.toList()).contains("before navigation"));
    assertTrue(sinceNav.stream().map(m -> m.text()).collect(Collectors.toList()).contains("after navigation"));
  }
}
