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

package com.microsoft.playwright.tests.page;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;

import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static com.microsoft.playwright.fixtures.Utils.rafraf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/** Port of tests/page/wheel.spec.ts. */
public class TestWheel {
  private boolean ignoreDelta = false;

  // Java: it.beforeAll is a @BeforeEach, the field is reset for every test anyway.
  @BeforeEach
  void beforeAll(BrowserName browserName) {
    if (browserName == BrowserName.CHROMIUM && Env.platform().equals("darwin")) {
      // Chromium reports deltaX/deltaY scaled by host device scale factor.
      // https://bugs.chromium.org/p/chromium/issues/detail?id=1324819
      // https://github.com/microsoft/playwright/issues/7362
      // Different bots have different scale factors (usually 1 or 2), so we just ignore the values
      // instead of guessing the host scale factor.
      ignoreDelta = true;
    }
  }

  @SuppressWarnings("unchecked")
  private void expectEvent(Page page, Map<String, Object> expected) {
    // Java: there is no expect.poll, this is expect.poll(() => page.evaluate('window.lastEvent')).toBeTruthy() with its 5s timeout.
    long deadline = System.currentTimeMillis() + 5000;
    Object received = page.evaluate("window.lastEvent");
    while (received == null && System.currentTimeMillis() < deadline) {
      page.waitForTimeout(100);
      received = page.evaluate("window.lastEvent");
    }
    assertNotNull(received);
    Map<String, Object> receivedMap = (Map<String, Object>) received;
    if (ignoreDelta) {
      receivedMap.remove("deltaX");
      receivedMap.remove("deltaY");
      expected.remove("deltaX");
      expected.remove("deltaY");
    }
    assertEquals(expected, receivedMap);
  }

  @PlaywrightTest
  @DisplayName("MO-017 should dispatch wheel events @smoke")
  void shouldDispatchWheelEvents(Page page, TestServer server) {
    page.setContent("<div style=\"width: 5000px; height: 5000px;\"></div>");
    page.mouse().move(50, 60);
    listenForWheelEvents(page, "div");
    page.mouse().wheel(0, 100);
    page.waitForFunction("window.scrollY === 100");
    expectEvent(page, mapOf(
      "deltaX", 0,
      "deltaY", 100,
      "clientX", 50,
      "clientY", 60,
      "deltaMode", 0,
      "ctrlKey", false,
      "shiftKey", false,
      "altKey", false,
      "metaKey", false));
  }

  @PlaywrightTest
  @DisplayName("MO-018 should dispatch wheel events after popup was opened @smoke")
  void shouldDispatchWheelEventsAfterPopupWasOpened(Page page, TestServer server) {
    page.setContent("\n" +
      "    <div style=\"width: 5000px; height: 5000px;\"></div>\n" +
      "  ");
    page.mouse().move(50, 60);
    listenForWheelEvents(page, "div");
    page.waitForPopup(() -> page.evaluate("() => window.open('')"));
    page.mouse().wheel(0, 100);
    page.waitForFunction("window.scrollY === 100");
    expectEvent(page, mapOf(
      "deltaX", 0,
      "deltaY", 100,
      "clientX", 50,
      "clientY", 60,
      "deltaMode", 0,
      "ctrlKey", false,
      "shiftKey", false,
      "altKey", false,
      "metaKey", false));
  }

  @PlaywrightTest
  @DisplayName("MO-019 should dispatch wheel event on svg element")
  void shouldDispatchWheelEventOnSvgElement(Page page, BrowserName browserName) {
    page.setContent("\n" +
      "    <body>\n" +
      "      <svg class=\"scroll-box\"></svg>\n" +
      "    </body>\n" +
      "    <style>\n" +
      "      .scroll-box {\n" +
      "        position: absolute;\n" +
      "        top: 0px;\n" +
      "        left: 0px;\n" +
      "        background-color: brown;\n" +
      "        width: 200px;\n" +
      "        height: 200px;\n" +
      "      }\n" +
      "    </style>");
    listenForWheelEvents(page, "svg");
    page.mouse().move(100, 100);
    page.mouse().wheel(0, 100);
    page.waitForFunction("!!window.lastEvent");
    expectEvent(page, mapOf(
      "deltaX", 0,
      "deltaY", 100,
      "clientX", 100,
      "clientY", 100,
      "deltaMode", 0,
      "ctrlKey", false,
      "shiftKey", false,
      "altKey", false,
      "metaKey", false));
  }

  @PlaywrightTest
  @DisplayName("MO-020 should scroll when nobody is listening")
  void shouldScrollWhenNobodyIsListening(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/scrollable.html");
    page.mouse().move(50, 60);
    page.mouse().wheel(0, 100);
    page.waitForFunction("window.scrollY === 100");
  }

  @PlaywrightTest
  @DisplayName("MO-021 should set the modifiers")
  void shouldSetTheModifiers(Page page) {
    page.setContent("<div style=\"width: 5000px; height: 5000px;\"></div>");
    page.mouse().move(50, 60);
    listenForWheelEvents(page, "div");
    page.keyboard().down("Shift");
    page.mouse().wheel(0, 100);
    expectEvent(page, mapOf(
      "deltaX", 0,
      "deltaY", 100,
      "clientX", 50,
      "clientY", 60,
      "deltaMode", 0,
      "ctrlKey", false,
      "shiftKey", true,
      "altKey", false,
      "metaKey", false));
  }

  @PlaywrightTest
  @DisplayName("MO-022 should scroll horizontally")
  void shouldScrollHorizontally(Page page) {
    page.setContent("<div style=\"width: 5000px; height: 5000px;\"></div>");
    page.mouse().move(50, 60);
    listenForWheelEvents(page, "div");
    page.mouse().wheel(100, 0);
    expectEvent(page, mapOf(
      "deltaX", 100,
      "deltaY", 0,
      "clientX", 50,
      "clientY", 60,
      "deltaMode", 0,
      "ctrlKey", false,
      "shiftKey", false,
      "altKey", false,
      "metaKey", false));
    page.waitForFunction("window.scrollX === 100");
  }

  @PlaywrightTest
  @DisplayName("MO-023 should work when the event is canceled")
  void shouldWorkWhenTheEventIsCanceled(Page page) {
    page.setContent("<div style=\"width: 5000px; height: 5000px;\"></div>");
    page.mouse().move(50, 60);
    listenForWheelEvents(page, "div");
    page.evaluate("() => {\n" +
      "  document.querySelector('div').addEventListener('wheel', e => e.preventDefault());\n" +
      "}");
    // Give wheel listener a chance to propagate through all the layers in Firefox.
    rafraf(page, 10);
    page.mouse().wheel(0, 100);
    expectEvent(page, mapOf(
      "deltaX", 0,
      "deltaY", 100,
      "clientX", 50,
      "clientY", 60,
      "deltaMode", 0,
      "ctrlKey", false,
      "shiftKey", false,
      "altKey", false,
      "metaKey", false));
    // Give the page a chance to scroll.
    page.waitForFunction("!!window['lastEvent']");
    // Ensure that it did not.
    assertEquals(0, page.evaluate("window.scrollY"));
  }

  private static void listenForWheelEvents(Page page, String selector) {
    page.evaluate("selector => {\n" +
      "  document.querySelector(selector).addEventListener('wheel', event => {\n" +
      "    const e = event;\n" +
      "    window['lastEvent'] = {\n" +
      "      deltaX: e.deltaX,\n" +
      "      deltaY: e.deltaY,\n" +
      "      clientX: e.clientX,\n" +
      "      clientY: e.clientY,\n" +
      "      deltaMode: e.deltaMode,\n" +
      "      ctrlKey: e.ctrlKey,\n" +
      "      shiftKey: e.shiftKey,\n" +
      "      altKey: e.altKey,\n" +
      "      metaKey: e.metaKey,\n" +
      "    };\n" +
      "  }, { passive: false });\n" +
      "}", selector);
  }
}
