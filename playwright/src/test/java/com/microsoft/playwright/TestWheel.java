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

import java.util.Map;

import static com.microsoft.playwright.Utils.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestWheel extends TestBase {
  private void expectEvent(Map<String, Object> expected) {
    // Chromium reports deltaX/deltaY scaled by host device scale factor.
    // https://bugs.chromium.org/p/chromium/issues/detail?id=1324819
    // https://github.com/microsoft/playwright/issues/7362
    // Different bots have different scale factors (usually 1 or 2), so we just ignore the values
    // instead of guessing the host scale factor.
    // This first appeared in Chromium 102.
    boolean ignoreDelta = isChromium() && isMac;
    Map<String, Object> received = (Map<String, Object>) page.evaluate("window.lastEvent");
    if (ignoreDelta) {
      expected.remove("deltaX");
      expected.remove("deltaY");
      received.remove("deltaX");
      received.remove("deltaY");
    }
    assertEquals(expected, received);
  }

  @Test
  void shouldDispatchWheelEvents() {
    page.setContent("<div style='width: 5000px; height: 5000px;'></div>");
    page.mouse().move(50, 60);
    listenForWheelEvents(page, "div");
    page.mouse().wheel(0, 100);

    Map<String, Object> expected = mapOf(
      "deltaX", 0,
      "deltaY", 100,
      "clientX", 50,
      "clientY", 60,
      "deltaMode", 0,
      "ctrlKey", false,
      "shiftKey", false,
      "altKey", false,
      "metaKey", false);
    page.waitForFunction("window.scrollY === 100");
    expectEvent(expected);
  }

  @Test
  void shouldScrollWhenNobodyIsListening() {
    page.navigate(server.PREFIX + "/input/scrollable.html");
    page.mouse().move(50, 60);
    page.mouse().wheel(0, 100);
    page.waitForFunction("window.scrollY === 100");
  }


  @Test
  void shouldSetTheModifiers() {
    page.setContent("<div style='width: 5000px; height: 5000px;'></div>");
    page.mouse().move(50, 60);
    listenForWheelEvents(page, "div");
    page.keyboard().down("Shift");
    page.mouse().wheel(0, 100);
    Map<String, Object> expected = mapOf(
      "deltaX", 0,
      "deltaY", 100,
      "clientX", 50,
      "clientY", 60,
      "deltaMode", 0,
      "ctrlKey", false,
      "shiftKey", true,
      "altKey", false,
      "metaKey", false);
    expectEvent(expected);
  }

  @Test
  void shouldScrollHorizontally() {
    page.setContent("<div style='width: 5000px; height: 5000px;'></div>");
    page.mouse().move(50, 60);
    listenForWheelEvents(page, "div");
    page.mouse().wheel(100, 0);
    Map<String, Object> expected = mapOf(
      "deltaX", 100,
      "deltaY", 0,
      "clientX", 50,
      "clientY", 60,
      "deltaMode", 0,
      "ctrlKey", false,
      "shiftKey", false,
      "altKey", false,
      "metaKey", false);
    page.waitForFunction("window.scrollX === 100");
    expectEvent(expected);
  }

  @Test
  void shouldWorkWhenTheEventIsCanceled() {
    page.setContent("<div style='width: 5000px; height: 5000px;'></div>");
    page.mouse().move(50, 60);
    listenForWheelEvents(page, "div");
    page.evaluate("() => {\n" +
      "    document.querySelector('div').addEventListener('wheel', e => e.preventDefault());\n" +
      "  }");
    page.mouse().wheel(0, 100);
    // give the page a chance to scroll
    page.waitForFunction("!!window['lastEvent']");
    Map<String, Object> expected = mapOf(
      "deltaX", 0,
      "deltaY", 100,
      "clientX", 50,
      "clientY", 60,
      "deltaMode", 0,
      "ctrlKey", false,
      "shiftKey", false,
      "altKey", false,
      "metaKey", false);
    expectEvent(expected);
    // ensure that it did not.
    assertEquals(0, page.evaluate("window.scrollY"));
  }

  private static void listenForWheelEvents(Page page, String selector) {
    page.evaluate("selector => {\n" +
      "    document.querySelector(selector).addEventListener('wheel', e => {\n" +
      "      window['lastEvent'] = {\n" +
      "        deltaX: e.deltaX,\n" +
      "        deltaY: e.deltaY,\n" +
      "        clientX: e.clientX,\n" +
      "        clientY: e.clientY,\n" +
      "        deltaMode: e.deltaMode,\n" +
      "        ctrlKey: e.ctrlKey,\n" +
      "        shiftKey: e.shiftKey,\n" +
      "        altKey: e.altKey,\n" +
      "        metaKey: e.metaKey,\n" +
      "      };\n" +
      "    }, { passive: false });\n" +
      "  }", selector);
  }
}
