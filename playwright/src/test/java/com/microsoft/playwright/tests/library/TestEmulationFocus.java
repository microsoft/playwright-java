/**
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

package com.microsoft.playwright.tests.library;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.ContextFactory;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestInfo;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

import static com.microsoft.playwright.fixtures.Utils.async;
import static com.microsoft.playwright.fixtures.Utils.attachFrame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/library/emulation-focus.spec.ts. */
public class TestEmulationFocus {
  // Java: Promise.all([a, b, ...]) runs all calls but the last one with async(), the last one inline, and returns the results in order.
  @SafeVarargs
  private static List<Object> promiseAll(Callable<Object>... calls) throws Exception {
    List<CompletableFuture<Object>> futures = new ArrayList<>();
    for (int i = 0; i < calls.length - 1; i++) {
      futures.add(async(calls[i]));
    }
    Object last = calls[calls.length - 1].call();
    List<Object> results = new ArrayList<>();
    for (CompletableFuture<Object> future : futures) {
      results.add(future.get());
    }
    results.add(last);
    return results;
  }

  @PlaywrightTest
  @DisplayName("EM-013 should think that it is focused by default")
  void shouldThinkThatItIsFocusedByDefault(Page page) {
    assertEquals(true, page.evaluate("document.hasFocus()"));
  }

  @PlaywrightTest
  @DisplayName("EM-014 should think that all pages are focused @smoke")
  void shouldThinkThatAllPagesAreFocused(Page page) {
    Page page2 = page.context().newPage();
    assertEquals(true, page.evaluate("document.hasFocus()"));
    assertEquals(true, page2.evaluate("document.hasFocus()"));
    page2.close();
  }

  @PlaywrightTest
  @DisplayName("EM-015 should focus popups by default")
  void shouldFocusPopupsByDefault(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    Page popup = page.waitForPopup(() -> page.evaluate("url => { window.open(url); }", server.EMPTY_PAGE));
    assertEquals(true, popup.evaluate("document.hasFocus()"));
    assertEquals(true, page.evaluate("document.hasFocus()"));
  }

  @PlaywrightTest
  @DisplayName("EM-016 should provide target for keyboard events")
  void shouldProvideTargetForKeyboardEvents(Page page, TestServer server) throws Exception {
    Page page2 = page.context().newPage();
    promiseAll(
      () -> page.navigate(server.PREFIX + "/input/textarea.html"),
      () -> page2.navigate(server.PREFIX + "/input/textarea.html"));
    promiseAll(
      () -> { page.focus("input"); return null; },
      () -> { page2.focus("input"); return null; });
    String text = "first";
    String text2 = "second";
    promiseAll(
      () -> { page.keyboard().type(text); return null; },
      () -> { page2.keyboard().type(text2); return null; });
    List<Object> results = promiseAll(
      () -> page.evaluate("result"),
      () -> page2.evaluate("result"));
    assertEquals(Arrays.asList(text, text2), results);
  }

  @PlaywrightTest
  @DisplayName("EM-017 should not affect mouse event target page")
  void shouldNotAffectMouseEventTargetPage(Page page, TestServer server) throws Exception {
    Page page2 = page.context().newPage();
    String clickCounter = "function clickCounter() {\n" +
      "    document.onclick = () => window['clickCount']  = (window['clickCount'] || 0) + 1;\n" +
      "  }";
    promiseAll(
      () -> page.evaluate(clickCounter),
      () -> page2.evaluate(clickCounter),
      () -> { page.focus("body"); return null; },
      () -> { page2.focus("body"); return null; });
    promiseAll(
      () -> { page.mouse().click(1, 1); return null; },
      () -> { page2.mouse().click(1, 1); return null; });
    List<Object> counters = promiseAll(
      () -> page.evaluate("window.clickCount"),
      () -> page2.evaluate("window.clickCount"));
    assertEquals(Arrays.asList(1, 1), counters);
  }

  @PlaywrightTest
  @DisplayName("EM-018 should change document.activeElement")
  void shouldChangeDocumentActiveElement(Page page, TestServer server) throws Exception {
    Page page2 = page.context().newPage();
    promiseAll(
      () -> page.navigate(server.PREFIX + "/input/textarea.html"),
      () -> page2.navigate(server.PREFIX + "/input/textarea.html"));
    promiseAll(
      () -> { page.focus("input"); return null; },
      () -> { page2.focus("textarea"); return null; });
    List<Object> active = promiseAll(
      () -> page.evaluate("document.activeElement.tagName"),
      () -> page2.evaluate("document.activeElement.tagName"));
    assertEquals(Arrays.asList("INPUT", "TEXTAREA"), active);
  }

  @PlaywrightTest
  @DisplayName("EM-019 should not affect screenshots")
  void shouldNotAffectScreenshots(Page page, TestServer server, BrowserName browserName, TestInfo testInfo) throws Exception {
    assumeFalse(browserName == BrowserName.WEBKIT && Env.isWindows && Env.headed(), "WebKit/Windows/headed has a larger minimal viewport. See https://github.com/microsoft/playwright/issues/22616");
    assumeFalse(browserName == BrowserName.WEBKIT && Env.isLinux && Env.headed(), "WebKit headed has a larger minimal viewport on gtk4.");
    assumeFalse(browserName == BrowserName.FIREFOX && Env.headed(), "Firefox headed produces a different image");

    Page page2 = page.context().newPage();
    promiseAll(
      () -> { page.setViewportSize(500, 500); return null; },
      () -> page.navigate(server.PREFIX + "/grid.html"),
      () -> { page2.setViewportSize(50, 50); return null; },
      () -> page2.navigate(server.PREFIX + "/grid.html"));
    promiseAll(
      () -> { page.focus("body"); return null; },
      () -> { page2.focus("body"); return null; });
    List<Object> screenshots = promiseAll(
      () -> page.screenshot(),
      () -> page2.screenshot());
    testInfo.expectToMatchSnapshot((byte[]) screenshots.get(0), "screenshot-sanity.png");
    testInfo.expectToMatchSnapshot((byte[]) screenshots.get(1), "grid-cell-0.png");
  }

  @PlaywrightTest
  @DisplayName("EM-020 should change focused iframe")
  void shouldChangeFocusedIframe(Page page, TestServer server, BrowserName browserName) throws Exception {
    assumeFalse(browserName == BrowserName.FIREFOX && Env.headed(), "Headed FF might lose focus");
    page.navigate(server.EMPTY_PAGE);
    List<Object> frames = promiseAll(
      () -> attachFrame(page, "frame1", server.PREFIX + "/input/textarea.html"),
      () -> attachFrame(page, "frame2", server.PREFIX + "/input/textarea.html"));
    Frame frame1 = (Frame) frames.get(0);
    Frame frame2 = (Frame) frames.get(1);
    String logger = "function logger() {\n" +
      "    self['_events'] = [];\n" +
      "    const element = document.querySelector('input');\n" +
      "    element.onfocus = element.onblur = e => self['_events'].push(e.type);\n" +
      "  }";
    promiseAll(
      () -> frame1.evaluate(logger),
      () -> frame2.evaluate(logger));
    List<Object> focused = promiseAll(
      () -> frame1.evaluate("document.hasFocus()"),
      () -> frame2.evaluate("document.hasFocus()"));
    assertEquals(Arrays.asList(false, false), focused);
    {
      frame1.focus("input");
      List<Object> events = promiseAll(
        () -> frame1.evaluate("self._events"),
        () -> frame2.evaluate("self._events"));
      assertEquals(Arrays.asList(Arrays.asList("focus"), Collections.emptyList()), events);
      List<Object> focused2 = promiseAll(
        () -> frame1.evaluate("document.hasFocus()"),
        () -> frame2.evaluate("document.hasFocus()"));
      assertEquals(Arrays.asList(true, false), focused2);
    }
    {
      frame2.focus("input");
      List<Object> events = promiseAll(
        () -> frame1.evaluate("self._events"),
        () -> frame2.evaluate("self._events"));
      assertEquals(Arrays.asList(Arrays.asList("focus", "blur"), Arrays.asList("focus")), events);
      List<Object> focused2 = promiseAll(
        () -> frame1.evaluate("document.hasFocus()"),
        () -> frame2.evaluate("document.hasFocus()"));
      assertEquals(Arrays.asList(false, true), focused2);
    }
  }

  // @see https://github.com/microsoft/playwright/issues/3476
  @PlaywrightTest
  @DisplayName("EM-021 should focus with more than one page/context")
  void shouldFocusWithMoreThanOnePageContext(ContextFactory contextFactory) {
    Page page1 = contextFactory.newContext().newPage();
    Page page2 = contextFactory.newContext().newPage();
    page1.setContent("<button id=\"foo\" onfocus=\"window.gotFocus=true\">foo</button>");
    page2.setContent("<button id=\"foo\" onfocus=\"window.gotFocus=true\">foo</button>");
    page1.focus("#foo");
    page2.focus("#foo");
    assertEquals(true, page1.evaluate("() => !!window['gotFocus']"));
    assertEquals(true, page2.evaluate("() => !!window['gotFocus']"));
  }

  @PlaywrightTest
  @DisplayName("EM-022 should not fire blur events when interacting with more than one page/context")
  void shouldNotFireBlurEventsWhenInteractingWithMoreThanOnePageContext(ContextFactory contextFactory, BrowserName browserName) {
    Page page1 = contextFactory.newContext().newPage();
    Page page2 = contextFactory.newContext().newPage();
    page1.setContent("<button id=\"foo\" onblur=\"window.gotBlur=true\">foo</button>");
    page2.setContent("<button id=\"foo\" onblur=\"window.gotBlur=true\">foo</button>");
    page1.click("#foo");
    page2.click("#foo");
    assertEquals(false, page1.evaluate("() => !!window['gotBlur']"));
    assertEquals(false, page2.evaluate("() => !!window['gotBlur']"));
  }

  @PlaywrightTest
  @DisplayName("EM-023 should not blur iframe when interacting with another page/context")
  @DisabledOnBrowser(value = BrowserName.FIREFOX, reason = "fixme: https://github.com/microsoft/playwright/issues/43280")
  void shouldNotBlurIframeWhenInteractingWithAnotherPageContext(ContextFactory contextFactory, BrowserName browserName) {
    Page page1 = contextFactory.newContext().newPage();
    Page page2 = contextFactory.newContext().newPage();
    String srcdoc = "<button id=foo>foo</button><script>window.addEventListener('blur', () => window.gotBlur = true)</script>";
    page1.setContent("<iframe srcdoc=\"" + srcdoc + "\"></iframe><iframe srcdoc=\"" + srcdoc + "\"></iframe>");
    List<Frame> frames = page1.frames();
    Frame frame1 = frames.get(1);
    Frame frame2 = frames.get(2);
    frame1.click("#foo");
    page2.setContent("<p>other page</p>");
    page2.mouse().click(10, 10);
    assertEquals(false, frame1.evaluate("() => !!window['gotBlur']"));
    assertEquals(true, frame1.evaluate("() => document.hasFocus()"));
    assertEquals(false, frame2.evaluate("() => document.hasFocus()"));
  }

  @PlaywrightTest
  @DisplayName("EM-024 should trigger hover state concurrently")
  void shouldTriggerHoverStateConcurrently(BrowserType browserType) {
    assumeFalse(Env.headed(), "headed messes up with hover");

    Browser browser1 = browserType.launch(Env.launchOptions());
    BrowserContext context1 = browser1.newContext();
    Page page1 = context1.newPage();
    Page page2 = context1.newPage();
    Browser browser2 = browserType.launch(Env.launchOptions());
    Page page3 = browser2.newPage();

    for (Page page : Arrays.asList(page1, page2, page3)) {
      page.setContent("\n" +
        "      <style>\n" +
        "        button { display: none; }\n" +
        "        div:hover button { display: inline };\n" +
        "      </style>\n" +
        "      <div><span>hover me</span><button onclick=\"window.clicked=1+(window.clicked || 0)\">click me</button></div>\n" +
        "    ");
    }

    for (Page page : Arrays.asList(page1, page2, page3)) {
      page.hover("span");
    }
    for (Page page : Arrays.asList(page1, page2, page3)) {
      page.click("button");
    }
    for (Page page : Arrays.asList(page1, page2, page3)) {
      assertEquals(1, page.evaluate("window.clicked"));
    }
    for (Page page : Arrays.asList(page1, page2, page3)) {
      page.click("button");
    }
    for (Page page : Arrays.asList(page1, page2, page3)) {
      assertEquals(2, page.evaluate("window.clicked"));
    }
  }
}
