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

package com.microsoft.playwright.tests.page;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.ElementState;
import org.junit.jupiter.api.DisplayName;

import static com.microsoft.playwright.fixtures.Utils.rafraf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/elementhandle-wait-for-element-state.spec.ts. */
public class TestElementhandleWaitForElementState {
  @PlaywrightTest
  @DisplayName("EH-112 should wait for visible")
  void shouldWaitForVisible(Page page) throws Exception {
    page.setContent("<div style='display:none'>content</div>");
    ElementHandle div = page.querySelector("div");
    // Java: the wait runs after the action, it cannot be pending while the handle is driven from the same thread.
    div.evaluate("div => div.style.display = 'block'");
    div.waitForElementState(ElementState.VISIBLE);
  }

  @PlaywrightTest
  @DisplayName("EH-113 should wait for already visible")
  void shouldWaitForAlreadyVisible(Page page) {
    page.setContent("<div>content</div>");
    ElementHandle div = page.querySelector("div");
    div.waitForElementState(ElementState.VISIBLE);
  }

  @PlaywrightTest
  @DisplayName("EH-114 should timeout waiting for visible")
  void shouldTimeoutWaitingForVisible(Page page) {
    page.setContent("<div style='display:none'>content</div>");
    ElementHandle div = page.querySelector("div");
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> div.waitForElementState(ElementState.VISIBLE, new ElementHandle.WaitForElementStateOptions().setTimeout(1000)));
    assertTrue(error.getMessage().contains("Timeout 1000ms exceeded"));
  }

  @PlaywrightTest
  @DisplayName("EH-115 should throw waiting for visible when detached")
  void shouldThrowWaitingForVisibleWhenDetached(Page page) {
    page.setContent("<div style='display:none'>content</div>");
    ElementHandle div = page.querySelector("div");
    // Java: the wait runs after the action, it cannot be pending while the handle is driven from the same thread.
    div.evaluate("div => div.remove()");
    Throwable error = assertThrows(PlaywrightException.class, () -> div.waitForElementState(ElementState.VISIBLE));
    assertTrue(error.getMessage().contains("Element is not attached to the DOM"));
  }

  @PlaywrightTest
  @DisplayName("EH-116 should wait for hidden")
  void shouldWaitForHidden(Page page) throws Exception {
    page.setContent("<div>content</div>");
    ElementHandle div = page.querySelector("div");
    // Java: the wait runs after the action, it cannot be pending while the handle is driven from the same thread.
    div.evaluate("div => div.style.display = 'none'");
    div.waitForElementState(ElementState.HIDDEN);
  }

  @PlaywrightTest
  @DisplayName("EH-117 should wait for already hidden")
  void shouldWaitForAlreadyHidden(Page page) {
    page.setContent("<div></div>");
    ElementHandle div = page.querySelector("div");
    div.waitForElementState(ElementState.HIDDEN);
  }

  @PlaywrightTest
  @DisplayName("EH-118 should wait for hidden when detached")
  void shouldWaitForHiddenWhenDetached(Page page) throws Exception {
    page.setContent("<div>content</div>");
    ElementHandle div = page.querySelector("div");
    // Java: the wait runs after the action, it cannot be pending while the handle is driven from the same thread.
    div.evaluate("div => div.remove()");
    div.waitForElementState(ElementState.HIDDEN);
  }

  @PlaywrightTest
  @DisplayName("EH-119 should throw waiting for enabled when detached")
  void shouldThrowWaitingForEnabledWhenDetached(Page page) {
    page.setContent("<button disabled>Target</button>");
    ElementHandle button = page.querySelector("button");
    // Java: the wait runs after the action, it cannot be pending while the handle is driven from the same thread.
    button.evaluate("button => button.remove()");
    Throwable error = assertThrows(PlaywrightException.class, () -> button.waitForElementState(ElementState.ENABLED));
    assertTrue(error.getMessage().contains("Element is not attached to the DOM"));
  }

  @PlaywrightTest
  @DisplayName("EH-120 should wait for aria enabled button")
  void shouldWaitForAriaEnabledButton(Page page) throws Exception {
    page.setContent("<button aria-disabled=true><span>Target</span></button>");
    ElementHandle span = page.querySelector("text=Target");
    // Java: the wait runs after the action, it cannot be pending while the handle is driven from the same thread.
    span.evaluate("span => span.parentElement.setAttribute('aria-disabled', 'false')");
    span.waitForElementState(ElementState.ENABLED);
  }

  @PlaywrightTest
  @DisplayName("EH-121 should wait for button with an aria-disabled parent")
  void shouldWaitForButtonWithAnAriaDisabledParent(Page page) throws Exception {
    page.setContent("<div role=\"group\" aria-disabled=true><button><span>Target</span></button></div>");
    ElementHandle span = page.querySelector("text=Target");
    // Java: the wait runs after the action, it cannot be pending while the handle is driven from the same thread.
    span.evaluate("span => span.parentElement.parentElement.setAttribute('aria-disabled', 'false')");
    span.waitForElementState(ElementState.ENABLED);
  }

  @PlaywrightTest
  @DisplayName("EH-122 should wait for stable position")
  void shouldWaitForStablePosition(Page page, TestServer server) throws Exception {
    page.navigate(server.PREFIX + "/input/button.html");
    ElementHandle button = page.querySelector("button");
    page.evalOnSelector("button", "button => {\n" +
      "    button.style.transition = 'margin 10000ms linear 0s';\n" +
      "    button.style.marginLeft = '20000px';\n" +
      "  }");
    // rafraf for Firefox to kick in the animation.
    rafraf(page);
    // Java: the wait runs after the action, it cannot be pending while the handle is driven from the same thread.
    button.evaluate("button => button.style.transition = ''");
    button.waitForElementState(ElementState.STABLE);
  }

  @PlaywrightTest
  @DisplayName("EH-123 should wait for editable input")
  void shouldWaitForEditableInput(Page page, TestServer server) throws Exception {
    page.setContent("<input readonly>");
    ElementHandle input = page.querySelector("input");
    // Java: the wait runs after the action, it cannot be pending while the handle is driven from the same thread.
    input.evaluate("input => input.readOnly = false");
    input.waitForElementState(ElementState.EDITABLE);
  }
}
