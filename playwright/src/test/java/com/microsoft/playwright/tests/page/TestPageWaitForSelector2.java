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
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.DisplayName;


import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.microsoft.playwright.fixtures.Utils.attachFrame;
import static com.microsoft.playwright.fixtures.Utils.detachFrame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/page-wait-for-selector-2.spec.ts. */
public class TestPageWaitForSelector2 {
  private static final String addElement = "tag => document.body.appendChild(document.createElement(tag))";

  @PlaywrightTest
  @DisplayName("WA-044 should survive cross-process navigation")
  void shouldSurviveCrossProcessNavigation(Page page, TestServer server) throws Exception {
    // Java: the wait runs after the actions, it cannot be pending while the page is driven from the same thread.
    page.navigate(server.EMPTY_PAGE);
    page.reload();
    page.navigate(server.CROSS_PROCESS_PREFIX + "/grid.html");
    assertNotNull(page.waitForSelector(".box"));
  }

  @PlaywrightTest
  @DisplayName("WA-045 should wait for visible")
  void shouldWaitForVisible(Page page, TestServer server) throws Exception {
    // Java: the wait runs after the actions, it cannot be pending while the page is driven from the same thread.
    page.setContent("<div style='display: none; visibility: hidden;'>1</div>");
    page.evaluate("() => document.querySelector('div').style.removeProperty('display')");
    page.evaluate("() => document.querySelector('div').style.removeProperty('visibility')");
    assertNotNull(page.waitForSelector("div"));
  }

  @PlaywrightTest
  @DisplayName("WA-046 should not consider visible when zero-sized")
  @Fixme("java: the message has no 'page.waitForSelector:' prefix")
  void shouldNotConsiderVisibleWhenZeroSized(Page page, TestServer server) {
    page.setContent("<div style='width: 0; height: 0;'>1</div>");
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.waitForSelector("div", new Page.WaitForSelectorOptions().setTimeout(1000)));
    assertTrue(error.getMessage().contains("page.waitForSelector: Timeout 1000ms exceeded"));
    page.evaluate("() => document.querySelector('div').style.width = '10px'");
    error = assertThrows(PlaywrightException.class,
      () -> page.waitForSelector("div", new Page.WaitForSelectorOptions().setTimeout(1000)));
    assertTrue(error.getMessage().contains("page.waitForSelector: Timeout 1000ms exceeded"));
    page.evaluate("() => document.querySelector('div').style.height = '10px'");
    assertNotNull(page.waitForSelector("div", new Page.WaitForSelectorOptions().setTimeout(1000)));
  }

  @PlaywrightTest
  @DisplayName("WA-047 should wait for visible recursively")
  void shouldWaitForVisibleRecursively(Page page, TestServer server) throws Exception {
    // Java: the wait runs after the actions, it cannot be pending while the page is driven from the same thread.
    page.setContent("<div style='display: none; visibility: hidden;'><div id=\"inner\">hi</div></div>");
    page.evaluate("() => document.querySelector('div').style.removeProperty('display')");
    page.evaluate("() => document.querySelector('div').style.removeProperty('visibility')");
    assertNotNull(page.waitForSelector("div#inner"));
  }

  @PlaywrightTest
  @DisplayName("WA-048 should consider outside of viewport visible")
  void shouldConsiderOutsideOfViewportVisible(Page page) {
    page.setContent("\n" +
      "    <style>\n" +
      "      .cover {\n" +
      "        position: fixed;\n" +
      "        left: 0;\n" +
      "        top: 0;\n" +
      "        width: 100px;\n" +
      "        height: 100px;\n" +
      "        background-color: red;\n" +
      "        transform: translateX(-200px);\n" +
      "      }\n" +
      "    </style>\n" +
      "    <div class=\"cover\">cover</div>\n" +
      "  ");

    Locator cover = page.locator(".cover");
    cover.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    assertThat(cover).isVisible();
  }

  @PlaywrightTest
  @DisplayName("WA-049 hidden should wait for hidden")
  void hiddenShouldWaitForHidden(Page page, TestServer server) throws Exception {
    // Java: the wait runs after the actions, it cannot be pending while the page is driven from the same thread.
    page.setContent("<div style='display: block;'>content</div>");
    page.waitForSelector("div"); // do a round trip
    page.evaluate("() => document.querySelector('div').style.setProperty('visibility', 'hidden')");
    assertNull(page.waitForSelector("div", new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN)));
  }

  @PlaywrightTest
  @DisplayName("WA-050 hidden should wait for display: none")
  void hiddenShouldWaitForDisplayNone(Page page, TestServer server) throws Exception {
    // Java: the wait runs after the actions, it cannot be pending while the page is driven from the same thread.
    page.setContent("<div style='display: block;'>content</div>");
    page.waitForSelector("div"); // do a round trip
    page.evaluate("() => document.querySelector('div').style.setProperty('display', 'none')");
    assertNull(page.waitForSelector("div", new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN)));
  }

  @PlaywrightTest
  @DisplayName("WA-051 hidden should wait for removal")
  void hiddenShouldWaitForRemoval(Page page, TestServer server) throws Exception {
    // Java: the wait runs after the actions, it cannot be pending while the page is driven from the same thread.
    page.setContent("<div>content</div>");
    page.waitForSelector("div"); // do a round trip
    page.evaluate("() => document.querySelector('div').remove()");
    assertNull(page.waitForSelector("div", new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN)));
  }

  @PlaywrightTest
  @DisplayName("WA-052 should return null if waiting to hide non-existing element")
  void shouldReturnNullIfWaitingToHideNonExistingElement(Page page, TestServer server) {
    ElementHandle handle = page.waitForSelector("non-existing", new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));
    assertNull(handle);
  }

  @PlaywrightTest
  @DisplayName("WA-053 should respect timeout")
  @Fixme("java: the message has no 'page.waitForSelector:' prefix and the call log says locator(\"div\") with double quotes")
  void shouldRespectTimeout(Page page) {
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.waitForSelector("div", new Page.WaitForSelectorOptions().setTimeout(3000).setState(WaitForSelectorState.ATTACHED)));
    assertNotNull(error);
    assertTrue(error.getMessage().contains("page.waitForSelector: Timeout 3000ms exceeded"));
    assertTrue(error.getMessage().contains("waiting for locator('div')"));
    assertTrue(error instanceof TimeoutError);
  }

  @PlaywrightTest
  @DisplayName("WA-054 should have an error message specifically for awaiting an element to be hidden")
  @Fixme("java: the message has no 'page.waitForSelector:' prefix and the call log says locator(\"div\") with double quotes")
  void shouldHaveAnErrorMessageSpecificallyForAwaitingAnElementToBeHidden(Page page, TestServer server) {
    page.setContent("<div>content</div>");
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.waitForSelector("div", new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN).setTimeout(1000)));
    assertNotNull(error);
    assertTrue(error.getMessage().contains("page.waitForSelector: Timeout 1000ms exceeded"));
    assertTrue(error.getMessage().contains("waiting for locator('div') to be hidden"));
  }

  @PlaywrightTest
  @DisplayName("WA-055 should respond to node attribute mutation")
  void shouldRespondToNodeAttributeMutation(Page page, TestServer server) throws Exception {
    // Java: the wait runs after the actions, it cannot be pending while the page is driven from the same thread.
    page.setContent("<div class='notZombo'></div>");
    page.evaluate("() => document.querySelector('div').className = 'zombo'");
    assertNotNull(page.waitForSelector(".zombo", new Page.WaitForSelectorOptions().setState(WaitForSelectorState.ATTACHED)));
  }

  @PlaywrightTest
  @DisplayName("WA-056 should return the element handle")
  void shouldReturnTheElementHandle(Page page, TestServer server) throws Exception {
    // Java: the wait runs after the actions, it cannot be pending while the page is driven from the same thread.
    page.setContent("<div class='zombo'>anything</div>");
    assertEquals("anything", page.evaluate("x => x.textContent", page.waitForSelector(".zombo")));
  }

  @PlaywrightTest
  @DisplayName("WA-057 should have correct stack trace for timeout")
  void shouldHaveCorrectStackTraceForTimeout(Page page, TestServer server) {
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.waitForSelector(".zombo", new Page.WaitForSelectorOptions().setTimeout(10)));
    // Java: error.stack becomes the Java stack trace, which must lead to the call in this file.
    boolean found = false;
    for (StackTraceElement element : error.getStackTrace()) {
      found = found || element.getClassName().startsWith(TestPageWaitForSelector2.class.getName());
    }
    assertTrue(found);
  }

  // WA-058 should throw for unknown state option: state is the WaitForSelectorState enum in Java.
  // WA-059 should throw for visibility option: options.visibility is not in the Java API.
  // WA-060 should throw for true state option: state is the WaitForSelectorState enum in Java.
  // WA-061 should throw for false state option: state is the WaitForSelectorState enum in Java.

  @PlaywrightTest
  @DisplayName("WA-062 should support >> selector syntax")
  void shouldSupportSelectorSyntax(Page page, TestServer server) throws Exception {
    page.navigate(server.EMPTY_PAGE);
    Frame frame = page.mainFrame();
    // Java: the wait runs after the actions, it cannot be pending while the page is driven from the same thread.
    frame.evaluate(addElement, "br");
    frame.evaluate(addElement, "div");
    frame.evaluate("() => document.querySelector('div').appendChild(document.createElement('span'))");
    ElementHandle eHandle = frame.waitForSelector("css=div >> css=span", new Frame.WaitForSelectorOptions().setState(WaitForSelectorState.ATTACHED));
    Object tagName = eHandle.getProperty("tagName").jsonValue();
    assertEquals("SPAN", tagName);
  }

  @PlaywrightTest
  @DisplayName("WA-063 should wait for detached if already detached")
  void shouldWaitForDetachedIfAlreadyDetached(Page page, TestServer server) {
    page.setContent("<section id=\"testAttribute\">43543</section>");
    assertNull(page.waitForSelector("css=div", new Page.WaitForSelectorOptions().setState(WaitForSelectorState.DETACHED)));
  }

  @PlaywrightTest
  @DisplayName("WA-064 should wait for detached")
  void shouldWaitForDetached(Page page, TestServer server) throws Exception {
    page.setContent("<section id=\"testAttribute\"><div>43543</div></section>");
    // Java: the wait runs after the actions, it cannot be pending while the page is driven from the same thread.
    page.waitForSelector("css=section");
    page.evalOnSelector("div", "div => div.remove()");
    assertNull(page.waitForSelector("css=div", new Page.WaitForSelectorOptions().setState(WaitForSelectorState.DETACHED)));
  }

  @PlaywrightTest
  @DisplayName("WA-065 should support some fancy xpath")
  void shouldSupportSomeFancyXpath(Page page, TestServer server) {
    page.setContent("<p>red herring</p><p>hello  world  </p>");
    ElementHandle waitForXPath = page.waitForSelector("//p[normalize-space(.)=\"hello world\"]");
    assertEquals("hello  world  ", page.evaluate("x => x.textContent", waitForXPath));
  }

  @PlaywrightTest
  @DisplayName("WA-066 should respect timeout xpath")
  @Fixme("java: the message has no 'page.waitForSelector:' prefix and the call log says locator(\"//div\") with double quotes")
  void shouldRespectTimeoutXpath(Page page) {
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.waitForSelector("//div", new Page.WaitForSelectorOptions().setState(WaitForSelectorState.ATTACHED).setTimeout(3000)));
    assertNotNull(error);
    assertTrue(error.getMessage().contains("page.waitForSelector: Timeout 3000ms exceeded"));
    assertTrue(error.getMessage().contains("waiting for locator('//div')"));
    assertTrue(error instanceof TimeoutError);
  }

  @PlaywrightTest
  @DisplayName("WA-067 should run in specified frame xpath")
  void shouldRunInSpecifiedFrameXpath(Page page, TestServer server) throws Exception {
    attachFrame(page, "frame1", server.EMPTY_PAGE);
    attachFrame(page, "frame2", server.EMPTY_PAGE);
    Frame frame1 = page.frames().get(1);
    Frame frame2 = page.frames().get(2);
    // Java: the wait runs after the actions, it cannot be pending while the page is driven from the same thread.
    frame1.evaluate(addElement, "div");
    frame2.evaluate(addElement, "div");
    ElementHandle eHandle = frame2.waitForSelector("//div", new Frame.WaitForSelectorOptions().setState(WaitForSelectorState.ATTACHED));
    assertEquals(frame2, eHandle.ownerFrame());
  }

  @PlaywrightTest
  @DisplayName("WA-068 should throw when frame is detached xpath")
  @Fixme("java: the message is \"Error {message='Frame was detached ...\" without the 'frame.waitForSelector:' prefix")
  void shouldThrowWhenFrameIsDetachedXpath(Page page, TestServer server) {
    attachFrame(page, "frame1", server.EMPTY_PAGE);
    Frame frame = page.frames().get(1);
    // Java: the wait runs after the actions, it cannot be pending while the page is driven from the same thread.
    detachFrame(page, "frame1");
    Throwable waitError = assertThrows(PlaywrightException.class, () -> frame.waitForSelector("//*[@class=\"box\"]"));
    assertTrue(waitError.getMessage().contains("frame.waitForSelector: Frame was detached"));
  }

  @PlaywrightTest
  @DisplayName("WA-069 should return the element handle xpath")
  void shouldReturnTheElementHandleXpath(Page page, TestServer server) throws Exception {
    // Java: the wait runs after the actions, it cannot be pending while the page is driven from the same thread.
    page.setContent("<div class='zombo'>anything</div>");
    assertEquals("anything", page.evaluate("x => x.textContent", page.waitForSelector("//*[@class=\"zombo\"]")));
  }

  @PlaywrightTest
  @DisplayName("WA-070 should allow you to select an element with single slash xpath")
  void shouldAllowYouToSelectAnElementWithSingleSlashXpath(Page page, TestServer server) {
    page.setContent("<div>some text</div>");
    ElementHandle waitForXPath = page.waitForSelector("//html/body/div");
    assertEquals("some text", page.evaluate("x => x.textContent", waitForXPath));
  }

  @PlaywrightTest
  @DisplayName("WA-071 should correctly handle hidden shadow host")
  void shouldCorrectlyHandleHiddenShadowHost(Page page, TestServer server) {
    page.setContent("\n" +
      "    <x-host hidden></x-host>\n" +
      "    <script>\n" +
      "      const host = document.querySelector('x-host');\n" +
      "      const root = host.attachShadow({ mode: 'open' });\n" +
      "      const style = document.createElement('style');\n" +
      "      style.textContent = ':host([hidden]) { display: none; }';\n" +
      "      root.appendChild(style);\n" +
      "      const child = document.createElement('div');\n" +
      "      child.textContent = 'Find me';\n" +
      "      root.appendChild(child);\n" +
      "    </script>\n" +
      "  ");
    assertEquals("Find me", page.textContent("div"));
    page.waitForSelector("div", new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));
  }

  // WA-072 should work when navigating before node adoption: __testHookBeforeAdoptNode is a JavaScript function passed to the in-process server.
  // WA-073 should fail when navigating while on handle: __testHookBeforeAdoptNode is a JavaScript function passed to the in-process server.

  @PlaywrightTest
  @DisplayName("WA-074 should fail if element handle was detached while waiting")
  void shouldFailIfElementHandleWasDetachedWhileWaiting(Page page, TestServer server) {
    page.setContent("<button>hello</button>");
    ElementHandle button = page.querySelector("button");
    // Java: the wait runs after the actions, it cannot be pending while the page is driven from the same thread.
    page.evaluate("() => document.body.innerText = ''");
    Throwable error = assertThrows(PlaywrightException.class, () -> button.waitForSelector("something"));
    assertTrue(error.getMessage().contains("Element is not attached to the DOM"));
  }
}
