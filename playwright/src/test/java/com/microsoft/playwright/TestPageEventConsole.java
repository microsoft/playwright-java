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
import org.junit.jupiter.api.condition.DisabledIf;

import java.util.ArrayList;
import java.util.List;

import static com.microsoft.playwright.Utils.getOS;
import static com.microsoft.playwright.Utils.mapOf;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestPageEventConsole extends TestBase {
  @Test
  void shouldWork() {
    ConsoleMessage[] event = {null};
    page.onConsoleMessage(m -> event[0] = m);
    ConsoleMessage message = page.waitForConsoleMessage(() -> page.evaluate("() => console.log('hello', 5, {foo: 'bar'});"));
    if (isFirefox()) {
      assertEquals("hello 5 JSHandle@object", message.text());
    } else {
      assertEquals("hello 5 {foo: bar}", message.text());
    }
    assertEquals("log", message.type());
    assertEquals("hello", message.args().get(0).jsonValue());
    assertEquals(5, message.args().get(1).jsonValue());
    assertEquals(mapOf("foo", "bar"), message.args().get(2).jsonValue());
    assertEquals(message, event[0]);
  }

  @Test
  void shouldEmitSameLogTwice() {
    List<String> messages = new ArrayList<>();
    page.onConsoleMessage(m -> messages.add(m.text()));
    page.evaluate("() => { for (let i = 0; i < 2; ++i) console.log('hello'); }");
    assertEquals(asList("hello", "hello"), messages);
  }

  @Test
  void shouldWorkForDifferentConsoleAPICalls() {
    List<ConsoleMessage> messages = new ArrayList<>();
    page.onConsoleMessage(msg -> messages.add(msg));
    // All console events will be reported before "page.evaluate" is finished.
    page.evaluate("() => {\n" +
      "    // A pair of time/timeEnd generates only one Console API call.\n" +
      "    console.time('calling console.time');\n" +
      "    console.timeEnd('calling console.time');\n" +
      "    console.trace('calling console.trace');\n" +
      "    console.dir('calling console.dir');\n" +
      "    console.warn('calling console.warn');\n" +
      "    console.error('calling console.error');\n" +
      "    console.log(Promise.resolve('should not wait until resolved!'));\n" +
      "  }");
    assertEquals(asList("timeEnd", "trace", "dir", "warning", "error", "log"),
      messages.stream().map(msg -> msg.type()).collect(toList()));
    assertTrue(messages.get(0).text().contains("calling console.time"));

    assertEquals(asList(
      "calling console.trace",
      "calling console.dir",
      "calling console.warn",
      "calling console.error",
      "Promise"), messages.subList(1, messages.size()).stream().map(msg -> msg.text()).collect(toList()));
  }

  @Test
  void shouldNotFailForWindowObject() {
    ConsoleMessage message = page.waitForConsoleMessage(() -> page.evaluate("console.error(window)"));
    if (isFirefox()) {
      assertEquals("JSHandle@object", message.text());
    } else {
      assertEquals("Window", message.text());
    }
  }

  static boolean isWebKitWindows() {
    return isWebKit() && getOS() == Utils.OS.WINDOWS;
  }

  @Test
  @DisabledIf(value="isWebKitWindows", disabledReason="Upstream issue https://bugs.webkit.org/show_bug.cgi?id=229515")
  void shouldTriggerCorrectLog() {
    page.navigate("about:blank");
    ConsoleMessage message = page.waitForConsoleMessage(() -> {
      page.evaluate("async url => fetch(url).catch(e => {})", server.EMPTY_PAGE);
    });
    assertTrue(message.text().contains("Access-Control-Allow-Origin"));
    assertEquals("error", message.type());
  }

  @Test
  void shouldHaveLocationForConsoleAPICalls() {
    page.navigate(server.EMPTY_PAGE);
    ConsoleMessage message = page.waitForConsoleMessage(
      new Page.WaitForConsoleMessageOptions().setPredicate(m -> "yellow".equals(m.text())),
      () -> page.navigate(server.PREFIX + "/consolelog.html"));
    assertEquals("log", message.type());
    // Engines have different column notion.
    assertTrue(message.location().startsWith(server.PREFIX + "/consolelog.html:7:"), message.location());
  }

  @Test
  void shouldSupportPredicate() {
    page.navigate(server.EMPTY_PAGE);
    ConsoleMessage message = page.waitForConsoleMessage(
      new Page.WaitForConsoleMessageOptions().setPredicate(m -> "info".equals(m.type())),
      () -> {
        page.evaluate("console.log(1)");
        page.evaluate("console.info(2)");
      });
    assertEquals("2", message.text());
    assertEquals("info", message.type());
  }
}
