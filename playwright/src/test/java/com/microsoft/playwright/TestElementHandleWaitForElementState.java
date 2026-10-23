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

import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import org.junit.jupiter.api.condition.OS;

import static com.microsoft.playwright.fixtures.BrowserName.FIREFOX;
import static com.microsoft.playwright.options.ElementState.*;
import static org.junit.jupiter.api.Assertions.*;

public class TestElementHandleWaitForElementState {

  static void giveItAChanceToResolve(Page page) {
    for (int i = 0; i < 5; i++) {
      page.evaluate("() => new Promise(f => requestAnimationFrame(() => requestAnimationFrame(f)))");
    }
  }

  @PlaywrightTest
  void shouldWaitForVisible(Page page) {
    page.setContent("<div style='display:none'>content</div>");
    ElementHandle div = page.querySelector("div");
    giveItAChanceToResolve(page);
    div.evaluate("div => div.style.display = 'block'");
    div.waitForElementState(VISIBLE);
  }

  @PlaywrightTest
  void shouldWaitForAlreadyVisible(Page page) {
    page.setContent("<div>content</div>");
    ElementHandle div = page.querySelector("div");
    div.waitForElementState(VISIBLE);
  }

  @PlaywrightTest
  void shouldTimeoutWaitingForVisible(Page page) {
    page.setContent("<div style='display:none'>content</div>");
    ElementHandle div = page.querySelector("div");
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      div.waitForElementState(VISIBLE, new ElementHandle.WaitForElementStateOptions().setTimeout(1000));
    });
    assertTrue(e.getMessage().contains("Timeout 1000ms exceeded"));
  }

  @PlaywrightTest
  void shouldThrowWaitingForVisibleWhenDetached(Page page) {
    page.setContent("<div style='display:none'>content</div>");
    ElementHandle div = page.querySelector("div");
    div.evaluate("div => div.remove()");
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> div.waitForElementState(VISIBLE));
    assertTrue(e.getMessage().contains("Element is not attached to the DOM"));
  }

  @PlaywrightTest
  void shouldWaitForHidden(Page page) {
    page.setContent("<div>content</div>");
    ElementHandle div = page.querySelector("div");
    giveItAChanceToResolve(page);
    div.evaluate("div => div.style.display = 'none'");
    div.waitForElementState(HIDDEN);
  }

  @PlaywrightTest
  void shouldWaitForAlreadyHidden(Page page) {
    page.setContent("<div></div>");
    ElementHandle div = page.querySelector("div");
    div.waitForElementState(HIDDEN);
  }

  @PlaywrightTest
  void shouldWaitForHiddenWhenDetached(Page page) {
    page.setContent("<div>content</div>");
    ElementHandle div = page.querySelector("div");
    giveItAChanceToResolve(page);
    div.evaluate("div => div.remove()");
    div.waitForElementState(HIDDEN);
  }

  @PlaywrightTest
  void shouldWaitForEnabledButton(Page page) {
    page.setContent("<button disabled><span>Target</span></button>");
    ElementHandle span = page.querySelector("text=Target");
    giveItAChanceToResolve(page);
    span.evaluate("span => span.parentElement.disabled = false");
    span.waitForElementState(ENABLED);
  }

  @PlaywrightTest
  void shouldThrowWaitingForEnabledWhenDetached(Page page) {
    page.setContent("<button disabled>Target</button>");
    ElementHandle button = page.querySelector("button");
    button.evaluate("button => button.remove()");
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> button.waitForElementState(ENABLED));
    assertTrue(e.getMessage().contains("Element is not attached to the DOM"));
  }

  @PlaywrightTest
  void shouldWaitForDisabledButton(Page page) {
    page.setContent("<button><span>Target</span></button>");
    ElementHandle span = page.querySelector("text=Target");
    giveItAChanceToResolve(page);
    span.evaluate("span => span.parentElement.disabled = true");
    span.waitForElementState(DISABLED);
  }

  @PlaywrightTest
  @DisabledOnBrowser(value = FIREFOX, os = OS.LINUX, reason = "fixme")
  void shouldWaitForStablePosition(Page page, Server server) {
    page.navigate(server.PREFIX + "/input/button.html");
    ElementHandle button = page.querySelector("button");
    page.evalOnSelector("button", "button => {\n" +
      "  button.style.transition = 'margin 10000ms linear 0s';\n" +
      "  button.style.marginLeft = '20000px';\n" +
      "}");
    giveItAChanceToResolve(page);
    button.evaluate("button => button.style.transition = ''");
    button.waitForElementState(STABLE);
  }
}
