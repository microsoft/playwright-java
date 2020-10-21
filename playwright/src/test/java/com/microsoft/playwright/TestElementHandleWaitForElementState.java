/**
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

import static com.microsoft.playwright.ElementHandle.ElementState.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class TestElementHandleWaitForElementState extends TestBase {

  static void giveItAChanceToResolve(Page page) {
    for (int i = 0; i < 5; i++) {
      page.evaluate("() => new Promise(f => requestAnimationFrame(() => requestAnimationFrame(f)))");
    }
  }

  @Test
  void shouldWaitForVisible() {
    page.setContent("<div style='display:none'>content</div>");
    ElementHandle div = page.querySelector("div");
    Deferred<Void> promise = div.waitForElementState(VISIBLE);
    giveItAChanceToResolve(page);
    div.evaluate("div => div.style.display = 'block'");
    promise.get();
  }

  @Test
  void shouldWaitForAlreadyVisible() {
    page.setContent("<div>content</div>");
    ElementHandle div = page.querySelector("div");
    div.waitForElementState(VISIBLE);
  }

  @Test
  void shouldTimeoutWaitingForVisible() {
    page.setContent("<div style='display:none'>content</div>");
    ElementHandle div = page.querySelector("div");
    Deferred<Void> result = div.waitForElementState(VISIBLE, new ElementHandle.WaitForElementStateOptions().withTimeout(1000));
    try {
      result.get();
      fail("did not throw");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("Timeout 1000ms exceeded"));
    }
  }

  @Test
  void shouldThrowWaitingForVisibleWhenDetached() {
    page.setContent("<div style='display:none'>content</div>");
    ElementHandle div = page.querySelector("div");
    Deferred<Void> promise = div.waitForElementState(VISIBLE);
    div.evaluate("div => div.remove()");
    try {
      promise.get();
      fail("did not throw");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("Element is not attached to the DOM"));
    }
  }

  @Test
  void shouldWaitForHidden() {
    page.setContent("<div>content</div>");
    ElementHandle div = page.querySelector("div");
    Deferred<Void> promise = div.waitForElementState(HIDDEN);
    giveItAChanceToResolve(page);
    div.evaluate("div => div.style.display = 'none'");
    promise.get();
  }

  @Test
  void shouldWaitForAlreadyHidden() {
    page.setContent("<div></div>");
    ElementHandle div = page.querySelector("div");
    Deferred<Void> result = div.waitForElementState(HIDDEN);
    result.get();
  }

  @Test
  void shouldWaitForHiddenWhenDetached() {
    page.setContent("<div>content</div>");
    ElementHandle div = page.querySelector("div");
    Deferred<Void> promise = div.waitForElementState(HIDDEN);
    giveItAChanceToResolve(page);
    div.evaluate("div => div.remove()");
    promise.get();
  }

  @Test
  void shouldWaitForEnabledButton() {
    page.setContent("<button disabled><span>Target</span></button>");
    ElementHandle span = page.querySelector("text=Target");
    Deferred<Void> promise = span.waitForElementState(ENABLED);
    giveItAChanceToResolve(page);
    span.evaluate("span => span.parentElement.disabled = false");
    promise.get();
  }

  @Test
  void shouldThrowWaitingForEnabledWhenDetached() {
    page.setContent("<button disabled>Target</button>");
    ElementHandle button = page.querySelector("button");
    Deferred<Void> promise = button.waitForElementState(ENABLED);
    button.evaluate("button => button.remove()");
    try {
      promise.get();
      fail("did not throw");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("Element is not attached to the DOM"));
    }
  }

  @Test
  void shouldWaitForDisabledButton() {
    page.setContent("<button><span>Target</span></button>");
    ElementHandle span = page.querySelector("text=Target");
    Deferred<Void> promise = span.waitForElementState(DISABLED);
    giveItAChanceToResolve(page);
    span.evaluate("span => span.parentElement.disabled = true");
    promise.get();
  }

  @Test
  void shouldWaitForStablePosition() {
    // TODO: test.fixme(browserName === "firefox" && platform === "linux");
    page.navigate(server.PREFIX + "/input/button.html");
    ElementHandle button = page.querySelector("button");
    page.evalOnSelector("button", "button => {\n" +
      "  button.style.transition = 'margin 10000ms linear 0s';\n" +
      "  button.style.marginLeft = '20000px';\n" +
      "}");
    Deferred<Void> promise = button.waitForElementState(STABLE);
    giveItAChanceToResolve(page);
    button.evaluate("button => button.style.transition = ''");
    promise.get();
  }
}
