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


import static com.microsoft.playwright.fixtures.Utils.attachFrame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/page-wait-for-selector-1.spec.ts. */
public class TestPageWaitForSelector1 {

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
    // Java: the wait runs after the action; the state stays satisfied.
    div.evaluate("div => div.innerHTML = '<span>target</span>'");
    ElementHandle span = div.waitForSelector("span", new ElementHandle.WaitForSelectorOptions().setState(WaitForSelectorState.ATTACHED));
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

  // WA-033 elementHandle.waitForSelector should throw on navigation: not ported, see upstream_not_ported.txt.

  @PlaywrightTest
  @DisplayName("WA-034 should work with removed MutationObserver")
  void shouldWorkWithRemovedMutationObserver(Page page) throws Exception {
    page.evaluate("() => delete window.MutationObserver");
    // Java: the wait runs after the action; the state stays satisfied.
    page.setContent("<div class='zombo'>anything</div>");
    ElementHandle handle = page.waitForSelector(".zombo");
    assertEquals("anything", page.evaluate("x => x.textContent", handle));
  }

  @PlaywrightTest
  @DisplayName("WA-035 should resolve promise when node is added")
  void shouldResolvePromiseWhenNodeIsAdded(Page page, TestServer server) throws Exception {
    page.navigate(server.EMPTY_PAGE);
    Frame frame = page.mainFrame();
    // Java: the wait runs after the action; the state stays satisfied.
    frame.evaluate(addElement, "br");
    frame.evaluate(addElement, "div");
    ElementHandle eHandle = frame.waitForSelector("div", new Frame.WaitForSelectorOptions().setState(WaitForSelectorState.ATTACHED));
    Object tagName = eHandle.getProperty("tagName").jsonValue();
    assertEquals("DIV", tagName);
  }

  // WA-036 should report logs while waiting for visible: not ported, see upstream_not_ported.txt.

  // WA-037 should report logs while waiting for hidden: not ported, see upstream_not_ported.txt.

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
    // Java: the wait runs after the action; the state stays satisfied.
    ElementHandle handle = page.waitForSelector("span");
    assertEquals("Hello from shadow", handle.evaluate("e => e.textContent"));
  }

  @PlaywrightTest
  @DisplayName("WA-040 should work when node is added through innerHTML")
  void shouldWorkWhenNodeIsAddedThroughInnerHTML(Page page, TestServer server) throws Exception {
    page.navigate(server.EMPTY_PAGE);
    // Java: the wait runs after the action; the state stays satisfied.
    page.evaluate(addElement, "span");
    page.evaluate("() => document.querySelector('span').innerHTML = '<h3><div></div></h3>'");
    page.waitForSelector("h3 div", new Page.WaitForSelectorOptions().setState(WaitForSelectorState.ATTACHED));
  }

  @PlaywrightTest
  @DisplayName("WA-041 page.waitForSelector is shortcut for main frame")
  void pageWaitForSelectorIsShortcutForMainFrame(Page page, TestServer server) throws Exception {
    page.navigate(server.EMPTY_PAGE);
    attachFrame(page, "frame1", server.EMPTY_PAGE);
    Frame otherFrame = page.frames().get(1);
    // Java: the wait runs after the action; the state stays satisfied.
    otherFrame.evaluate(addElement, "div");
    page.evaluate(addElement, "div");
    ElementHandle eHandle = page.waitForSelector("div", new Page.WaitForSelectorOptions().setState(WaitForSelectorState.ATTACHED));
    assertEquals(page.mainFrame(), eHandle.ownerFrame());
  }

  @PlaywrightTest
  @DisplayName("WA-042 should run in specified frame")
  void shouldRunInSpecifiedFrame(Page page, TestServer server) throws Exception {
    attachFrame(page, "frame1", server.EMPTY_PAGE);
    attachFrame(page, "frame2", server.EMPTY_PAGE);
    Frame frame1 = page.frames().get(1);
    Frame frame2 = page.frames().get(2);
    // Java: the wait runs after the action; the state stays satisfied.
    frame1.evaluate(addElement, "div");
    frame2.evaluate(addElement, "div");
    ElementHandle eHandle = frame2.waitForSelector("div", new Frame.WaitForSelectorOptions().setState(WaitForSelectorState.ATTACHED));
    assertEquals(frame2, eHandle.ownerFrame());
  }

  // WA-043 should throw when frame is detached: not ported, see upstream_not_ported.txt.
}
