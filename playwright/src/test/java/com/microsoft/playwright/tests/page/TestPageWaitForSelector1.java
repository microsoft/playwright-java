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

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.DisplayName;

import java.util.concurrent.CompletableFuture;

import static com.microsoft.playwright.fixtures.Utils.async;
import static com.microsoft.playwright.fixtures.Utils.attachFrame;
import static com.microsoft.playwright.fixtures.Utils.cause;
import static com.microsoft.playwright.fixtures.Utils.detachFrame;
import static com.microsoft.playwright.fixtures.Utils.rafraf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/page-wait-for-selector-1.spec.ts. */
public class TestPageWaitForSelector1 {
  private static void giveItTimeToLog(Frame frame) {
    rafraf(frame, 2);
  }

  private static final String addElement = "tag => document.body.appendChild(document.createElement(tag))";

  // WA-027 should throw on waitFor: options.waitFor is undocumented and not in the Java API.
  // WA-028 should tolerate waitFor=visible: options.waitFor is undocumented and not in the Java API.

  @PlaywrightTest
  @DisplayName("WA-029 should immediately resolve promise if node exists")
  void shouldImmediatelyResolvePromiseIfNodeExists(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    Frame frame = page.mainFrame();
    frame.waitForSelector("*");
    frame.evaluate(addElement, "div");
    frame.waitForSelector("div", new Frame.WaitForSelectorOptions().setState(WaitForSelectorState.ATTACHED));
  }

  @PlaywrightTest
  @DisplayName("WA-030 elementHandle.waitForSelector should immediately resolve if node exists")
  void elementHandleWaitForSelectorShouldImmediatelyResolveIfNodeExists(Page page) {
    page.setContent("<span>extra</span><div><span>target</span></div>");
    ElementHandle div = page.querySelector("div");
    ElementHandle span = div.waitForSelector("span", new ElementHandle.WaitForSelectorOptions().setState(WaitForSelectorState.ATTACHED));
    assertEquals("target", span.evaluate("e => e.textContent"));
  }

  @PlaywrightTest
  @DisplayName("WA-031 elementHandle.waitForSelector should wait")
  void elementHandleWaitForSelectorShouldWait(Page page) throws Exception {
    page.setContent("<div></div>");
    ElementHandle div = page.querySelector("div");
    CompletableFuture<ElementHandle> promise = async(() -> div.waitForSelector("span", new ElementHandle.WaitForSelectorOptions().setState(WaitForSelectorState.ATTACHED)));
    div.evaluate("div => div.innerHTML = '<span>target</span>'");
    ElementHandle span = promise.get();
    assertEquals("target", span.evaluate("e => e.textContent"));
  }

  @PlaywrightTest
  @DisplayName("WA-032 elementHandle.waitForSelector should timeout")
  void elementHandleWaitForSelectorShouldTimeout(Page page) {
    page.setContent("<div></div>");
    ElementHandle div = page.querySelector("div");
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> div.waitForSelector("span", new ElementHandle.WaitForSelectorOptions().setTimeout(100)));
    assertTrue(error.getMessage().contains("Timeout 100ms exceeded."));
  }

  @PlaywrightTest
  @DisplayName("WA-033 elementHandle.waitForSelector should throw on navigation")
  void elementHandleWaitForSelectorShouldThrowOnNavigation(Page page, TestServer server) {
    page.setContent("<div></div>");
    ElementHandle div = page.querySelector("div");
    CompletableFuture<ElementHandle> promise = async(() -> div.waitForSelector("span"));
    // Give it some time before navigating.
    for (int i = 0; i < 10; i++) {
      page.evaluate("() => 1");
    }
    page.navigate(server.EMPTY_PAGE);
    Throwable error = cause(promise);
    assertTrue(error.getMessage().contains("waiting for locator('span') to be visible"));
  }

  @PlaywrightTest
  @DisplayName("WA-034 should work with removed MutationObserver")
  void shouldWorkWithRemovedMutationObserver(Page page) throws Exception {
    page.evaluate("() => delete window.MutationObserver");
    CompletableFuture<ElementHandle> promise = async(() -> page.waitForSelector(".zombo"));
    page.setContent("<div class='zombo'>anything</div>");
    ElementHandle handle = promise.get();
    assertEquals("anything", page.evaluate("x => x.textContent", handle));
  }

  @PlaywrightTest
  @DisplayName("WA-035 should resolve promise when node is added")
  void shouldResolvePromiseWhenNodeIsAdded(Page page, TestServer server) throws Exception {
    page.navigate(server.EMPTY_PAGE);
    Frame frame = page.mainFrame();
    CompletableFuture<ElementHandle> watchdog = async(() -> frame.waitForSelector("div", new Frame.WaitForSelectorOptions().setState(WaitForSelectorState.ATTACHED)));
    frame.evaluate(addElement, "br");
    frame.evaluate(addElement, "div");
    ElementHandle eHandle = watchdog.get();
    Object tagName = eHandle.getProperty("tagName").jsonValue();
    assertEquals("DIV", tagName);
  }

  @PlaywrightTest
  @DisplayName("WA-036 should report logs while waiting for visible")
  @Fixme("java: the message has no 'frame.waitForSelector:' prefix and the call log says locator(\"div\") with double quotes")
  void shouldReportLogsWhileWaitingForVisible(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    Frame frame = page.mainFrame();
    CompletableFuture<ElementHandle> watchdog = async(() -> frame.waitForSelector("div", new Frame.WaitForSelectorOptions().setTimeout(5000)));

    frame.evaluate("() => {\n" +
      "  const div = document.createElement('div');\n" +
      "  div.className = 'foo bar';\n" +
      "  div.id = 'mydiv';\n" +
      "  div.setAttribute('style', 'display: none');\n" +
      "  div.setAttribute('foo', '123456789012345678901234567890123456789012345678901234567890');\n" +
      "  div.textContent = 'abcdefghijklmnopqrstuvwyxzabcdefghijklmnopqrstuvwyxzabcdefghijklmnopqrstuvwyxz';\n" +
      "  document.body.appendChild(div);\n" +
      "}");
    giveItTimeToLog(frame);

    frame.evaluate("() => document.querySelector('div').remove()");
    giveItTimeToLog(frame);

    frame.evaluate("() => {\n" +
      "  const div = document.createElement('div');\n" +
      "  div.className = 'another';\n" +
      "  div.style.display = 'none';\n" +
      "  document.body.appendChild(div);\n" +
      "}");
    giveItTimeToLog(frame);

    Throwable error = cause(watchdog);
    assertTrue(error.getMessage().contains("frame.waitForSelector: Timeout 5000ms exceeded."));
    assertTrue(error.getMessage().contains("waiting for locator('div') to be visible"));
    assertTrue(error.getMessage().contains("locator resolved to hidden <div id=\"mydiv\" class=\"foo bar\" foo=\"123456789012345678901234567890123456789012345678901234567890\">abcdefghijklmnopqrstuvwyxzabcdefghijklmnopqrstuvw…</div>"));
    assertTrue(error.getMessage().contains("locator resolved to hidden <div class=\"another\"></div>"));
  }

  @PlaywrightTest
  @DisplayName("WA-037 should report logs while waiting for hidden")
  @Fixme("java: the message has no 'frame.waitForSelector:' prefix and the call log says locator(\"div\") with double quotes")
  void shouldReportLogsWhileWaitingForHidden(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    Frame frame = page.mainFrame();
    frame.evaluate("() => {\n" +
      "  const div = document.createElement('div');\n" +
      "  div.className = 'foo bar';\n" +
      "  div.id = 'mydiv';\n" +
      "  div.textContent = 'hello';\n" +
      "  document.body.appendChild(div);\n" +
      "}");

    CompletableFuture<ElementHandle> watchdog = async(() -> frame.waitForSelector("div", new Frame.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN).setTimeout(5000)));
    giveItTimeToLog(frame);

    frame.evaluate("() => {\n" +
      "  document.querySelector('div').remove();\n" +
      "  const div = document.createElement('div');\n" +
      "  div.className = 'another';\n" +
      "  div.textContent = 'hello';\n" +
      "  document.body.appendChild(div);\n" +
      "}");
    giveItTimeToLog(frame);

    Throwable error = cause(watchdog);
    assertTrue(error.getMessage().contains("frame.waitForSelector: Timeout 5000ms exceeded."));
    assertTrue(error.getMessage().contains("waiting for locator('div') to be hidden"));
    assertTrue(error.getMessage().contains("locator resolved to visible <div id=\"mydiv\" class=\"foo bar\">hello</div>"));
    assertTrue(error.getMessage().contains("locator resolved to visible <div class=\"another\">hello</div>"));
  }

  @PlaywrightTest
  @DisplayName("WA-038 should report logs when the selector resolves to multiple elements")
  void shouldReportLogsWhenTheSelectorResolvesToMultipleElements(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.setContent("\n" +
      "    <button style=\"display: none; position: absolute; top: 0px; left: 0px; width: 100%;\">Reset</button>\n" +
      "    <button>Reset</button>\n" +
      "  ");
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.click("text=Reset", new Page.ClickOptions().setTimeout(1000)));
    assertTrue(error.toString().contains("locator resolved to 2 elements. Proceeding with the first one: <button>Reset</button>"));
  }

  @PlaywrightTest
  @DisplayName("WA-039 should resolve promise when node is added in shadow dom")
  void shouldResolvePromiseWhenNodeIsAddedInShadowDom(Page page, TestServer server) throws Exception {
    page.navigate(server.EMPTY_PAGE);
    CompletableFuture<ElementHandle> watchdog = async(() -> page.waitForSelector("span"));
    page.evaluate("() => {\n" +
      "  const div = document.createElement('div');\n" +
      "  div.attachShadow({ mode: 'open' });\n" +
      "  document.body.appendChild(div);\n" +
      "}");
    page.waitForTimeout(100);
    page.evaluate("() => {\n" +
      "  const span = document.createElement('span');\n" +
      "  span.textContent = 'Hello from shadow';\n" +
      "  document.querySelector('div').shadowRoot.appendChild(span);\n" +
      "}");
    ElementHandle handle = watchdog.get();
    assertEquals("Hello from shadow", handle.evaluate("e => e.textContent"));
  }

  @PlaywrightTest
  @DisplayName("WA-040 should work when node is added through innerHTML")
  void shouldWorkWhenNodeIsAddedThroughInnerHTML(Page page, TestServer server) throws Exception {
    page.navigate(server.EMPTY_PAGE);
    CompletableFuture<ElementHandle> watchdog = async(() -> page.waitForSelector("h3 div", new Page.WaitForSelectorOptions().setState(WaitForSelectorState.ATTACHED)));
    page.evaluate(addElement, "span");
    page.evaluate("() => document.querySelector('span').innerHTML = '<h3><div></div></h3>'");
    watchdog.get();
  }

  @PlaywrightTest
  @DisplayName("WA-041 page.waitForSelector is shortcut for main frame")
  void pageWaitForSelectorIsShortcutForMainFrame(Page page, TestServer server) throws Exception {
    page.navigate(server.EMPTY_PAGE);
    attachFrame(page, "frame1", server.EMPTY_PAGE);
    Frame otherFrame = page.frames().get(1);
    CompletableFuture<ElementHandle> watchdog = async(() -> page.waitForSelector("div", new Page.WaitForSelectorOptions().setState(WaitForSelectorState.ATTACHED)));
    otherFrame.evaluate(addElement, "div");
    page.evaluate(addElement, "div");
    ElementHandle eHandle = watchdog.get();
    assertEquals(page.mainFrame(), eHandle.ownerFrame());
  }

  @PlaywrightTest
  @DisplayName("WA-042 should run in specified frame")
  void shouldRunInSpecifiedFrame(Page page, TestServer server) throws Exception {
    attachFrame(page, "frame1", server.EMPTY_PAGE);
    attachFrame(page, "frame2", server.EMPTY_PAGE);
    Frame frame1 = page.frames().get(1);
    Frame frame2 = page.frames().get(2);
    CompletableFuture<ElementHandle> waitForSelectorPromise = async(() -> frame2.waitForSelector("div", new Frame.WaitForSelectorOptions().setState(WaitForSelectorState.ATTACHED)));
    frame1.evaluate(addElement, "div");
    frame2.evaluate(addElement, "div");
    ElementHandle eHandle = waitForSelectorPromise.get();
    assertEquals(frame2, eHandle.ownerFrame());
  }

  @PlaywrightTest
  @DisplayName("WA-043 should throw when frame is detached")
  @Fixme("java: the message is \"Error {message='Frame was detached ...\" without the 'frame.waitForSelector:' prefix")
  void shouldThrowWhenFrameIsDetached(Page page, TestServer server) {
    attachFrame(page, "frame1", server.EMPTY_PAGE);
    Frame frame = page.frames().get(1);
    CompletableFuture<ElementHandle> waitPromise = async(() -> frame.waitForSelector(".box"));
    detachFrame(page, "frame1");
    Throwable waitError = cause(waitPromise);
    assertNotNull(waitError);
    assertTrue(waitError.getMessage().contains("frame.waitForSelector: Frame was detached"));
  }
}
