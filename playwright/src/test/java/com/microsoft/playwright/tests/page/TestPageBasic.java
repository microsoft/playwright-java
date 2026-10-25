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

import com.microsoft.playwright.ConsoleMessage;
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.microsoft.playwright.fixtures.Utils.async;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/page/page-basic.spec.ts. */
public class TestPageBasic {
  @PlaywrightTest
  @DisplayName("PA-001 should fire load when expected")
  void shouldFireLoadWhenExpected(Page page) {
    // Java: waitForEvent('load') is a future completed by the page event, awaited with waitForCondition().
    CompletableFuture<Page> loaded = new CompletableFuture<>();
    page.onLoad(p -> loaded.complete(p));
    page.navigate("about:blank");
    page.waitForCondition(() -> loaded.isDone());
  }

  @PlaywrightTest
  @DisplayName("PA-002 async stacks should work")
  void asyncStacksShouldWork(Page page, TestServer server) {
    server.setRoute("/empty.html", (req, res) -> {
      req.socket().end();
    });
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.navigate(server.EMPTY_PAGE));
    // Java: the stack trace of the error has the file of the test instead of error.stack.
    boolean stackHasTestFile = false;
    for (StackTraceElement element : error.getStackTrace()) {
      if ("TestPageBasic.java".equals(element.getFileName()))
        stackHasTestFile = true;
    }
    assertTrue(stackHasTestFile);
  }

  @PlaywrightTest
  @DisplayName("PA-003 should provide access to the opener page")
  void shouldProvideAccessToTheOpenerPage(Page page) {
    Page popup = page.waitForPopup(() -> {
      page.evaluate("() => window.open('about:blank')");
    });
    Page opener = popup.opener();
    assertEquals(page, opener);
  }

  @PlaywrightTest
  @DisplayName("PA-004 should fire domcontentloaded when expected")
  void shouldFireDomcontentloadedWhenExpected(Page page) {
    // Java: waitForEvent('domcontentloaded') is a future completed by the page event, awaited with waitForCondition().
    CompletableFuture<Page> loaded = new CompletableFuture<>();
    page.onDOMContentLoaded(p -> loaded.complete(p));
    page.navigate("about:blank");
    page.waitForCondition(() -> loaded.isDone());
  }

  @PlaywrightTest
  @DisplayName("PA-005 should pass self as argument to domcontentloaded event")
  void shouldPassSelfAsArgumentToDomcontentloadedEvent(Page page) throws Exception {
    CompletableFuture<Page> eventArgPromise = new CompletableFuture<>();
    page.onDOMContentLoaded(p -> eventArgPromise.complete(p));
    page.navigate("about:blank");
    page.waitForCondition(() -> eventArgPromise.isDone());
    Page eventArg = eventArgPromise.get();
    assertEquals(page, eventArg);
  }

  @PlaywrightTest
  @DisplayName("PA-006 should pass self as argument to load event")
  void shouldPassSelfAsArgumentToLoadEvent(Page page) throws Exception {
    CompletableFuture<Page> eventArgPromise = new CompletableFuture<>();
    page.onLoad(p -> eventArgPromise.complete(p));
    page.navigate("about:blank");
    page.waitForCondition(() -> eventArgPromise.isDone());
    Page eventArg = eventArgPromise.get();
    assertEquals(page, eventArg);
  }

  @PlaywrightTest
  @DisplayName("PA-007 page.url should work")
  void pageUrlShouldWork(Page page, TestServer server) {
    assertEquals("about:blank", page.url());
    page.navigate(server.EMPTY_PAGE);
    assertEquals(server.EMPTY_PAGE, page.url());
  }

  @PlaywrightTest
  @DisplayName("PA-008 page.url should include hashes")
  void pageUrlShouldIncludeHashes(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE + "#hash");
    assertEquals(server.EMPTY_PAGE + "#hash", page.url());
    page.evaluate("() => {\n" +
      "  window.location.hash = 'dynamic';\n" +
      "}");
    assertEquals(server.EMPTY_PAGE + "#dynamic", page.url());
  }

  @PlaywrightTest
  @DisplayName("PA-009 page.title should return the page title")
  void pageTitleShouldReturnThePageTitle(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/title.html");
    assertEquals("Woof-Woof", page.title());
  }

  @PlaywrightTest
  @DisplayName("PA-010 page.title should not throw during navigation")
  void pageTitleShouldNotThrowDuringNavigation(Page page, TestServer server) throws Exception {
    page.setContent("<title>hello</title>");
    CompletableFuture<?> promise = async(() -> page.navigate(server.PREFIX + "/title.html"));
    Object titleOrError;
    try {
      titleOrError = page.title();
    } catch (PlaywrightException e) {
      titleOrError = e;
    }
    promise.get();
    assertTrue(titleOrError instanceof String);
    assertTrue(Pattern.compile("^(hello|Loading http.*title.html||Woof-Woof)$").matcher((String) titleOrError).find());
    assertThat(page).hasTitle("Woof-Woof");
  }

  @PlaywrightTest
  @DisplayName("PA-011 page.close should work with window.close")
  void pageCloseShouldWorkWithWindowClose(Page page) {
    Page newPage = page.waitForPopup(() -> {
      page.evaluate("() => window['newPage'] = window.open('about:blank')");
    });
    // Java: the promise of the close event is a future completed by the page event, awaited with waitForCondition().
    CompletableFuture<Page> closedPromise = new CompletableFuture<>();
    newPage.onClose(x -> closedPromise.complete(x));
    page.evaluate("() => window['newPage'].close()");
    page.waitForCondition(() -> closedPromise.isDone());
  }

  @PlaywrightTest
  @DisplayName("PA-012 page.frame should respect name")
  void pageFrameShouldRespectName(Page page) {
    assumeFalse(Env.isBidi(), "page.frame({ name }) is racy with BiDi");

    page.setContent("<iframe name=target></iframe>");
    assertNull(page.frame("bogus"));
    Frame frame = page.frame("target");
    assertNotNull(frame);
    assertTrue(frame == page.mainFrame().childFrames().get(0));
  }

  @PlaywrightTest
  @DisplayName("PA-013 page.frame should respect url")
  void pageFrameShouldRespectUrl(Page page, TestServer server) {
    page.setContent("<iframe src=\"" + server.EMPTY_PAGE + "\"></iframe>");
    assertNull(page.frameByUrl(Pattern.compile("bogus")));
    assertEquals(server.EMPTY_PAGE, page.frameByUrl(Pattern.compile("empty")).url());
  }

  @PlaywrightTest
  @DisplayName("PA-014 should have sane user agent")
  void shouldHaveSaneUserAgent(Page page, BrowserName browserName) {
    String userAgent = (String) page.evaluate("() => navigator.userAgent");
    // Java: split() with a negative limit keeps the trailing empty strings like JavaScript does.
    String[] parts = userAgent.split("[()]", -1);
    for (int i = 0; i < parts.length; i++)
      parts[i] = parts[i].trim();
    String part1 = parts[0];
    // part2 is skipped
    String part3 = parts.length > 2 ? parts[2] : null;
    String part4 = parts.length > 3 ? parts[3] : null;
    String part5 = parts.length > 4 ? parts[4] : null;
    // First part is always "Mozilla/5.0"
    assertEquals("Mozilla/5.0", part1);
    // Second part in parenthesis is platform - ignore it.

    // Third part for Firefox is the last one and encodes engine and browser versions.
    if (browserName == BrowserName.FIREFOX) {
      String[] engineAndBrowser = part3.split(" ");
      String engine = engineAndBrowser[0];
      String browser = engineAndBrowser[1];
      assertEquals(true, engine.startsWith("Gecko"));
      assertEquals(true, browser.startsWith("Firefox"));
      assertNull(part4);
      assertNull(part5);
      return;
    }
    // For both options.CHROMIUM and options.WEBKIT, third part is the AppleWebKit version.
    assertEquals(true, part3.startsWith("AppleWebKit/"));
    assertEquals("KHTML, like Gecko", part4);
    // 5th part encodes real browser name and engine version.
    String[] tokens = part5.split(" ");
    String engine = tokens[0];
    String browser = null;
    for (String t : tokens) {
      if (t.startsWith("Safari/")) {
        browser = t;
        break;
      }
    }
    assertEquals(true, browser != null && browser.startsWith("Safari/"));
    if (browserName == BrowserName.CHROMIUM)
      assertEquals(true, engine.contains("Chrome/"));
    else
      assertEquals(true, engine.startsWith("Version/"));
  }

  @PlaywrightTest
  @DisplayName("PA-015 page.press should work")
  void pagePressShouldWork(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/textarea.html");
    page.press("textarea", "a");
    assertEquals("a", page.evaluate("() => document.querySelector('textarea').value"));
  }

  @PlaywrightTest
  @DisplayName("PA-016 page.press should work for Enter")
  void pagePressShouldWorkForEnter(Page page) {
    page.setContent("<input onkeypress=\"console.log('press')\"></input>");
    List<ConsoleMessage> messages = new ArrayList<>();
    page.onConsoleMessage(message -> messages.add(message));
    page.press("input", "Enter");
    assertEquals("press", messages.get(0).text());
  }

  @PlaywrightTest
  @DisplayName("PA-017 frame.press should work")
  void framePressShouldWork(Page page, TestServer server) {
    page.setContent("<iframe name=inner src=\"" + server.PREFIX + "/input/textarea.html\"></iframe>");
    Frame frame = page.frame("inner");
    frame.press("textarea", "a");
    assertEquals("a", frame.evaluate("() => document.querySelector('textarea').value"));
  }

  @PlaywrightTest
  @DisplayName("PA-018 has navigator.webdriver set to true")
  void hasNavigatorWebdriverSetToTrue(Page page) {
    assertEquals(true, page.evaluate("() => navigator.webdriver"));
  }
}
