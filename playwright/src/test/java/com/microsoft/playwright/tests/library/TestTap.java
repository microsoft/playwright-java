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

package com.microsoft.playwright.tests.library;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.JSHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.fixtures.Fixture;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.KeyboardModifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.concurrent.CompletableFuture;

import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** Port of tests/library/tap.spec.ts. */
public class TestTap {
  @Fixture
  Browser.NewContextOptions contextOptions(Browser.NewContextOptions contextOptions) {
    return contextOptions.setHasTouch(true);
  }

  @PlaywrightTest
  @DisplayName("TA-001 should send all of the correct events @smoke")
  void shouldSendAllOfTheCorrectEvents(Page page) {
    page.setContent("\n" +
      "  <div id=\"a\" style=\"background: lightblue; width: 50px; height: 50px\">a</div>\n" +
      "  <div id=\"b\" style=\"background: pink; width: 50px; height: 50px\">b</div>\n");
    page.tap("#a");
    JSHandle eventsHandle = trackEvents(page.querySelector("#b"));
    page.tap("#b");
    // webkit doesn't send pointerenter or pointerleave or mouseout
    assertEquals(asList(
      "pointerover", "pointerenter",
      "pointerdown", "touchstart",
      "pointerup", "pointerout",
      "pointerleave", "touchend",
      "mouseover", "mouseenter",
      "mousemove", "mousedown",
      "mouseup", "click"), eventsHandle.jsonValue());
  }

  @PlaywrightTest
  @DisplayName("TA-002 trial run should not tap")
  void trialRunShouldNotTap(Page page) {
    page.setContent("\n" +
      "    <div id=\"a\" style=\"background: lightblue; width: 50px; height: 50px\">a</div>\n" +
      "    <div id=\"b\" style=\"background: pink; width: 50px; height: 50px\">b</div>\n" +
      "  ");
    page.tap("#a");
    JSHandle eventsHandle = trackEvents(page.querySelector("#b"));
    page.tap("#b", new Page.TapOptions().setTrial(true));
    Object expected = asList("pointerover", "pointerenter", "pointerout", "pointerleave");
    assertEquals(expected, eventsHandle.jsonValue());
  }

  @PlaywrightTest
  @DisplayName("TA-003 should not send mouse events touchstart is canceled")
  void shouldNotSendMouseEventsTouchstartIsCanceled(Page page) {
    page.setContent("<div style=\"width: 50px; height: 50px; background: red\">");
    page.evaluate("() => {\n" +
      "  // touchstart is not cancelable unless passive is false\n" +
      "  document.addEventListener('touchstart', t => t.preventDefault(), { passive: false });\n" +
      "}");
    JSHandle eventsHandle = trackEvents(page.querySelector("div"));
    page.tap("div");
    assertEquals(asList(
      "pointerover", "pointerenter",
      "pointerdown", "touchstart",
      "pointerup", "pointerout",
      "pointerleave", "touchend"), eventsHandle.jsonValue());
  }

  @PlaywrightTest
  @DisplayName("TA-004 should not send mouse events when touchend is canceled")
  void shouldNotSendMouseEventsWhenTouchendIsCanceled(Page page) {
    page.setContent("<div style=\"width: 50px; height: 50px; background: red\">");
    page.evaluate("() => {\n" +
      "  document.addEventListener('touchend', t => t.preventDefault());\n" +
      "}");
    JSHandle eventsHandle = trackEvents(page.querySelector("div"));
    page.tap("div");
    assertEquals(asList(
      "pointerover", "pointerenter",
      "pointerdown", "touchstart",
      "pointerup", "pointerout",
      "pointerleave", "touchend"), eventsHandle.jsonValue());
  }

  @PlaywrightTest
  @DisplayName("TA-005 should not wait for a navigation caused by a tap")
  void shouldNotWaitForANavigationCausedByATap(Page page, TestServer server) throws Exception {
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<a href=\"/intercept-this.html\">link</a>;");
    CompletableFuture<Object> routed = new CompletableFuture<>();
    server.setRoute("/intercept-this.html", (req, res) -> routed.complete(req));
    page.tap("a");
    routed.get();
  }

  @PlaywrightTest
  @DisplayName("TA-006 should work with modifiers")
  void shouldWorkWithModifiers(Page page) throws Exception {
    page.setContent("hello world");
    // Java: the promise stays in the page, evaluate() awaits it after the tap.
    page.evaluate("() => { window.altKeyPromise = new Promise(resolve => {\n" +
      "  document.addEventListener('touchstart', event => {\n" +
      "    resolve(event.altKey);\n" +
      "  }, { passive: false });\n" +
      "}); }");
    page.tap("body", new Page.TapOptions().setModifiers(asList(KeyboardModifier.ALT)));
    assertEquals(true, page.evaluate("() => window.altKeyPromise"));
  }

  @PlaywrightTest
  @DisplayName("TA-007 should send well formed touch points")
  void shouldSendWellFormedTouchPoints(Page page) throws Exception {
    // Java: the promise stays in the page, evaluate() awaits it after the tap.
    page.evaluate("() => { window.touchstartPromise = new Promise(resolve => {\n" +
      "  document.addEventListener('touchstart', event => {\n" +
      "    resolve([...event.touches].map(t => ({\n" +
      "      identifier: t.identifier,\n" +
      "      clientX: t.clientX,\n" +
      "      clientY: t.clientY,\n" +
      "      pageX: t.pageX,\n" +
      "      pageY: t.pageY,\n" +
      "      radiusX: 'radiusX' in t ? t.radiusX : t['webkitRadiusX'],\n" +
      "      radiusY: 'radiusY' in t ? t.radiusY : t['webkitRadiusY'],\n" +
      "      rotationAngle: 'rotationAngle' in t ? t.rotationAngle : t['webkitRotationAngle'],\n" +
      "      force: 'force' in t ? t.force : t['webkitForce'],\n" +
      "    })));\n" +
      "  }, false);\n" +
      "}); }");
    // Java: the promise stays in the page, evaluate() awaits it after the tap.
    page.evaluate("() => { window.touchendPromise = new Promise(resolve => {\n" +
      "  document.addEventListener('touchend', event => {\n" +
      "    resolve([...event.touches].map(t => ({\n" +
      "      identifier: t.identifier,\n" +
      "      clientX: t.clientX,\n" +
      "      clientY: t.clientY,\n" +
      "      pageX: t.pageX,\n" +
      "      pageY: t.pageY,\n" +
      "      radiusX: 'radiusX' in t ? t.radiusX : t['webkitRadiusX'],\n" +
      "      radiusY: 'radiusY' in t ? t.radiusY : t['webkitRadiusY'],\n" +
      "      rotationAngle: 'rotationAngle' in t ? t.rotationAngle : t['webkitRotationAngle'],\n" +
      "      force: 'force' in t ? t.force : t['webkitForce'],\n" +
      "    })));\n" +
      "  }, false);\n" +
      "}); }");
    page.touchscreen().tap(40, 60);
    Object touchstart = page.evaluate("() => window.touchstartPromise");
    Object touchend = page.evaluate("() => window.touchendPromise");

    assertEquals(asList(mapOf(
      "clientX", 40,
      "clientY", 60,
      "force", 1,
      "identifier", 0,
      "pageX", 40,
      "pageY", 60,
      "radiusX", 1,
      "radiusY", 1,
      "rotationAngle", 0)), touchstart);
    assertEquals(asList(), touchend);
  }

  // TA-008 should wait until an element is visible to tap it: not ported, see upstream_not_ported.txt.

  private static JSHandle trackEvents(ElementHandle target) {
    JSHandle eventsHandle = target.evaluateHandle("target => {\n" +
      "  const events = [];\n" +
      "  for (const event of [\n" +
      "    'mousedown', 'mouseenter', 'mouseleave', 'mousemove', 'mouseout', 'mouseover', 'mouseup', 'click',\n" +
      "    'pointercancel', 'pointerdown', 'pointerenter', 'pointerleave', 'pointermove', 'pointerout', 'pointerover', 'pointerup',\n" +
      "    'touchstart', 'touchend', 'touchmove', 'touchcancel',\n" +
      "  ])\n" +
      "    target.addEventListener(event, () => events.push(event), false);\n" +
      "  return events;\n" +
      "}");
    return eventsHandle;
  }

  @Nested
  class Locators {
    @PlaywrightTest
    @DisplayName("TA-009 should send all of the correct events")
    void shouldSendAllOfTheCorrectEvents(Page page) {
      page.setContent("\n" +
        "      <div id=\"a\" style=\"background: lightblue; width: 50px; height: 50px\">a</div>\n" +
        "      <div id=\"b\" style=\"background: pink; width: 50px; height: 50px\">b</div>\n" +
        "    ");
      page.locator("#a").tap();
      page.locator("#b").tap();
    }
  }
}
