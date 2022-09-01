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

import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

import static com.microsoft.playwright.options.KeyboardModifier.ALT;
import static com.microsoft.playwright.Utils.mapOf;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestTap extends TestBase {

  @Override
  BrowserContext createContext() {
    return browser.newContext(new Browser.NewContextOptions().setHasTouch(true));
  }

  private JSHandle trackEvents(ElementHandle target) {
    return target.evaluateHandle("target => {\n" +
      "  const events = [];\n" +
      "  for (const event of [\n" +
      "    'mousedown', 'mouseenter', 'mouseleave', 'mousemove', 'mouseout', 'mouseover', 'mouseup', 'click',\n" +
      "    'pointercancel', 'pointerdown', 'pointerenter', 'pointerleave', 'pointermove', 'pointerout', 'pointerover', 'pointerup',\n" +
      "    'touchstart', 'touchend', 'touchmove', 'touchcancel'])\n" +
      "    target.addEventListener(event, () => events.push(event), false);\n" +
      "  return events;\n" +
      "}");
  }

  @Test
  void shouldSendAllOfTheCorrectEvents() {
    page.setContent("<div id='a' style='background: lightblue; width: 50px; height: 50px'>a</div>\n" +
      "<div id='b' style='background: pink; width: 50px; height: 50px'>b</div>");
    page.tap("#a");
    JSHandle eventsHandle = trackEvents(page.querySelector("#b"));
    page.tap("#b");
    // webkit doesnt send pointerenter or pointerleave or mouseout
    assertEquals(asList("pointerover",  "pointerenter",
      "pointerdown",  "touchstart",
      "pointerup",    "pointerout",
      "pointerleave", "touchend",
      "mouseover",    "mouseenter",
      "mousemove",    "mousedown",
      "mouseup",      "click"),
      eventsHandle.jsonValue());
  }

  @Test
  void shouldNotSendMouseEventsTouchstartIsCanceled() {
    page.setContent("<div style='width: 50px; height: 50px; background: red'>");
    page.evaluate("() => {\n" +
      "    // touchstart is not cancelable unless passive is false\n" +
      "    document.addEventListener('touchstart', t => t.preventDefault(), {passive: false});\n" +
      "  }");
    JSHandle eventsHandle = trackEvents(page.querySelector("div"));
    page.tap("div");
    assertEquals(asList("pointerover",  "pointerenter",
      "pointerdown",  "touchstart",
      "pointerup",    "pointerout",
      "pointerleave", "touchend"),
      eventsHandle.jsonValue());
  }

  @Test
  void shouldNotSendMouseEventsWhenTouchendIsCanceled() {
    page.setContent("<div style='width: 50px; height: 50px; background: red'>");
    page.evaluate("() => document.addEventListener('touchend', t => t.preventDefault())");
    JSHandle eventsHandle = trackEvents(page.querySelector("div"));
    page.tap("div");
    assertEquals(asList("pointerover",  "pointerenter",
      "pointerdown",  "touchstart",
      "pointerup",    "pointerout",
      "pointerleave", "touchend"),
      eventsHandle.jsonValue());
  }

  @Test
  void shouldWaitForANavigationCausedByATap() throws InterruptedException {
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<a href='/intercept-this.html'>link</a>;");
    Semaphore responseWritten = new Semaphore(0);
    List<String> events = Collections.synchronizedList(new ArrayList<>());
    server.setRoute("/intercept-this.html", exchange -> {
      // make sure the tap doesnt resolve too early
      assertDoesNotThrow(() -> Thread.sleep(100));
      exchange.getResponseHeaders().add("Content-Type", "application/octet-stream");
      exchange.sendResponseHeaders(200, 0);
      try (OutputStreamWriter writer = new OutputStreamWriter(exchange.getResponseBody())) {
        writer.write("foo");
      }
      events.add("sent response");
      responseWritten.release();
    });
    page.tap("a");
    events.add("tap finished");
    responseWritten.acquire();
    assertEquals(asList("sent response", "tap finished"), events);
  }

  @Test
  void shouldWorkWithModifiers() {
    page.setContent("hello world");
    page.evaluate("() => {\n" +
      "  window.touchPromise = new Promise(resolve => {\n" +
      "    document.addEventListener('touchstart', event => {\n" +
      "      resolve(event.altKey);\n" +
      "    }, {passive: false});\n" +
      "  });\n" +
      "}");
    page.tap("body", new Page.TapOptions().setModifiers(asList(ALT)));
    Object altKey = page.evaluate("() => window.touchPromise");
    assertEquals(true, altKey);
  }

  @Test
  void shouldSendWellFormedTouchPoints() {
    page.evaluate("() => {\n" +
      "  window.touchStartPromise = new Promise(resolve => {\n" +
      "      document.addEventListener('touchstart', event => {\n" +
      "        resolve([...event.touches].map(t => ({\n" +
      "          identifier: t.identifier,\n" +
      "          clientX: t.clientX,\n" +
      "          clientY: t.clientY,\n" +
      "          pageX: t.pageX,\n" +
      "          pageY: t.pageY,\n" +
      "          radiusX: 'radiusX' in t ? t.radiusX : t['webkitRadiusX'],\n" +
      "          radiusY: 'radiusY' in t ? t.radiusY : t['webkitRadiusY'],\n" +
      "          rotationAngle: 'rotationAngle' in t ? t.rotationAngle : t['webkitRotationAngle'],\n" +
      "          force: 'force' in t ? t.force : t['webkitForce'],\n" +
      "        })));\n" +
      "      }, false);\n" +
      "    })\n" +
      "  }");
    page.evaluate("() => {\n" +
      "  window.touchEndPromise = new Promise(resolve => {\n" +
      "      document.addEventListener('touchend', event => {\n" +
      "        resolve([...event.touches].map(t => ({\n" +
      "          identifier: t.identifier,\n" +
      "          clientX: t.clientX,\n" +
      "          clientY: t.clientY,\n" +
      "          pageX: t.pageX,\n" +
      "          pageY: t.pageY,\n" +
      "          radiusX: 'radiusX' in t ? t.radiusX : t['webkitRadiusX'],\n" +
      "          radiusY: 'radiusY' in t ? t.radiusY : t['webkitRadiusY'],\n" +
      "          rotationAngle: 'rotationAngle' in t ? t.rotationAngle : t['webkitRotationAngle'],\n" +
      "          force: 'force' in t ? t.force : t['webkitForce'],\n" +
      "        })));\n" +
      "      }, false);\n" +
      "    })\n" +
      "  }");

    page.touchscreen().tap(40, 60);
    Object touchStart = page.evaluate("() => window.touchStartPromise");
    assertEquals(asList(mapOf(
      "clientX", 40,
      "clientY", 60,
      "force", 1,
      "identifier", 0,
      "pageX", 40,
      "pageY", 60,
      "radiusX", 1,
      "radiusY", 1,
      "rotationAngle", 0
    )), touchStart);
    Object touchEnd = page.evaluate("() => window.touchEndPromise");
    assertEquals(Collections.emptyList(), touchEnd);
  }

  void shouldWaitUntilAnElementIsVisibleToTapIt() {
    // Ignored in sync api.
  }
}
