/*
 * Copyright Microsoft Corporation. All rights reserved.
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
import com.microsoft.playwright.JSHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;


import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/page-dispatchevent.spec.ts. */
public class TestPageDispatchevent {
  // Java: the JavaScript truthiness of a value that comes from the page.
  private static boolean isFalsy(Object value) {
    return value == null || Boolean.FALSE.equals(value) || (value instanceof Number && ((Number) value).doubleValue() == 0) || "".equals(value);
  }

  @PlaywrightTest
  @DisplayName("DE-001 should dispatch click event @smoke")
  void shouldDispatchClickEvent(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/button.html");
    page.dispatchEvent("button", "click");
    assertEquals("Clicked", page.evaluate("() => window['result']"));
  }

  @PlaywrightTest
  @DisplayName("DE-002 should dispatch click event properties")
  void shouldDispatchClickEventProperties(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/button.html");
    page.dispatchEvent("button", "click");
    assertEquals(true, page.evaluate("bubbles"));
    assertEquals(true, page.evaluate("cancelable"));
    assertEquals(true, page.evaluate("composed"));
  }

  @PlaywrightTest
  @DisplayName("DE-003 should dispatch click svg")
  void shouldDispatchClickSvg(Page page) {
    page.setContent("\n" +
      "    <svg height=\"100\" width=\"100\">\n" +
      "      <circle onclick=\"javascript:window.__CLICKED=42\" cx=\"50\" cy=\"50\" r=\"40\" stroke=\"black\" stroke-width=\"3\" fill=\"red\" />\n" +
      "    </svg>\n" +
      "  ");
    page.dispatchEvent("circle", "click");
    assertEquals(42, page.evaluate("() => window['__CLICKED']"));
  }

  @PlaywrightTest
  @DisplayName("DE-004 should dispatch click on a span with an inline element inside")
  void shouldDispatchClickOnASpanWithAnInlineElementInside(Page page) {
    page.setContent("\n" +
      "    <style>\n" +
      "    span::before {\n" +
      "      content: 'q';\n" +
      "    }\n" +
      "    </style>\n" +
      "    <span onclick='javascript:window.CLICKED=42'></span>\n" +
      "  ");
    page.dispatchEvent("span", "click");
    assertEquals(42, page.evaluate("() => window['CLICKED']"));
  }

  @PlaywrightTest
  @DisplayName("DE-005 should dispatch click after navigation ")
  void shouldDispatchClickAfterNavigation(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/button.html");
    page.dispatchEvent("button", "click");
    page.navigate(server.PREFIX + "/input/button.html");
    page.dispatchEvent("button", "click");
    assertEquals("Clicked", page.evaluate("() => window['result']"));
  }

  @PlaywrightTest
  @DisplayName("DE-006 should dispatch click after a cross origin navigation ")
  void shouldDispatchClickAfterACrossOriginNavigation(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/button.html");
    page.dispatchEvent("button", "click");
    page.navigate(server.CROSS_PROCESS_PREFIX + "/input/button.html");
    page.dispatchEvent("button", "click");
    assertEquals("Clicked", page.evaluate("() => window['result']"));
  }

  @PlaywrightTest
  @DisplayName("DE-007 should not fail when element is blocked on hover")
  void shouldNotFailWhenElementIsBlockedOnHover(Page page) {
    page.setContent("<style>\n" +
      "    container { display: block; position: relative; width: 200px; height: 50px; }\n" +
      "    div, button { position: absolute; left: 0; top: 0; bottom: 0; right: 0; }\n" +
      "    div { pointer-events: none; }\n" +
      "    container:hover div { pointer-events: auto; background: red; }\n" +
      "  </style>\n" +
      "  <container>\n" +
      "    <button onclick=\"window.clicked=true\">Click me</button>\n" +
      "    <div></div>\n" +
      "  </container>");
    page.dispatchEvent("button", "click");
    assertEquals(true, page.evaluate("() => window['clicked']"));
  }

  @PlaywrightTest
  @DisplayName("DE-009 should be atomic")
  void shouldBeAtomic(Playwright playwright, Page page) {
    // Java: the script of a selector engine is the object literal that createDummySelector returns.
    String createDummySelector = "{\n" +
      "  query(root, selector) {\n" +
      "    const result = root.querySelector(selector);\n" +
      "    if (result)\n" +
      "      void Promise.resolve().then(() => result.onclick = '');\n" +
      "    return result;\n" +
      "  },\n" +
      "  queryAll(root, selector) {\n" +
      "    const result = Array.from(root.querySelectorAll(selector));\n" +
      "    for (const e of result)\n" +
      "      void Promise.resolve().then(() => e.onclick = null);\n" +
      "    return result;\n" +
      "  }\n" +
      "}";
    playwright.selectors().register("dispatchEvent", createDummySelector);
    page.navigate("data:text/html,<div onclick=\"window._clicked=true\">Hello</div>");
    page.dispatchEvent("dispatchEvent=div", "click");
    assertEquals(true, page.evaluate("() => window['_clicked']"));
  }

  @PlaywrightTest
  @DisplayName("DE-010 should dispatch drag drop events")
  void shouldDispatchDragDropEvents(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/drag-n-drop.html");
    JSHandle dataTransfer = page.evaluateHandle("() => new DataTransfer()");
    page.dispatchEvent("#source", "dragstart", mapOf("dataTransfer", dataTransfer));
    page.dispatchEvent("#target", "drop", mapOf("dataTransfer", dataTransfer));
    ElementHandle source = page.querySelector("#source");
    ElementHandle target = page.querySelector("#target");
    assertEquals(true, page.evaluate("({ source, target }) => {\n" +
      "  return source.parentElement === target;\n" +
      "}", mapOf("source", source, "target", target)));
  }

  @PlaywrightTest
  @DisplayName("DE-011 should dispatch drag drop events via ElementHandles")
  void shouldDispatchDragDropEventsViaElementHandles(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/drag-n-drop.html");
    JSHandle dataTransfer = page.evaluateHandle("() => new DataTransfer()");
    ElementHandle source = page.querySelector("#source");
    source.dispatchEvent("dragstart", mapOf("dataTransfer", dataTransfer));
    ElementHandle target = page.querySelector("#target");
    target.dispatchEvent("drop", mapOf("dataTransfer", dataTransfer));
    assertEquals(true, page.evaluate("({ source, target }) => {\n" +
      "  return source.parentElement === target;\n" +
      "}", mapOf("source", source, "target", target)));
  }

  @PlaywrightTest
  @DisplayName("DE-012 should dispatch click event via ElementHandles")
  void shouldDispatchClickEventViaElementHandles(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/button.html");
    ElementHandle button = page.querySelector("button");
    button.dispatchEvent("click");
    assertEquals("Clicked", page.evaluate("() => window['result']"));
  }

  @PlaywrightTest
  @DisplayName("DE-013 should dispatch wheel event")
  void shouldDispatchWheelEvent(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/scrollable.html");
    JSHandle eventsHandle = page.locator("body").evaluateHandle("e => {\n" +
      "  const events = [];\n" +
      "  e.addEventListener('wheel', event => {\n" +
      "    events.push(event);\n" +
      "    console.log(event);\n" +
      "  });\n" +
      "  return events;\n" +
      "}");
    page.locator("body").dispatchEvent("wheel", mapOf("deltaX", 100, "deltaY", 200));
    assertEquals(1, eventsHandle.evaluate("e => e.length"));
    assertEquals(true, eventsHandle.evaluate("e => e[0] instanceof WheelEvent"));
    assertEquals(mapOf("deltaX", 100, "deltaY", 200), eventsHandle.evaluate("e => ({ deltaX: e[0].deltaX, deltaY: e[0].deltaY })"));
  }

  @PlaywrightTest
  @DisplayName("DE-014 should dispatch device orientation event")
  void shouldDispatchDeviceOrientationEvent(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/device-orientation.html");
    page.locator("html").dispatchEvent("deviceorientation", mapOf("alpha", 10, "beta", 20, "gamma", 30));
    assertEquals("Oriented", page.evaluate("result"));
    assertEquals(10, page.evaluate("alpha"));
    assertEquals(20, page.evaluate("beta"));
    assertEquals(30, page.evaluate("gamma"));
    // Java: toBeFalsy() on a value that is false or undefined in the page.
    assertTrue(isFalsy(page.evaluate("absolute")));
  }

  @PlaywrightTest
  @DisplayName("DE-015 should dispatch absolute device orientation event")
  void shouldDispatchAbsoluteDeviceOrientationEvent(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/device-orientation.html");
    page.locator("html").dispatchEvent("deviceorientationabsolute", mapOf("alpha", 10, "beta", 20, "gamma", 30, "absolute", true));
    assertEquals("Oriented", page.evaluate("result"));
    assertEquals(10, page.evaluate("alpha"));
    assertEquals(20, page.evaluate("beta"));
    assertEquals(30, page.evaluate("gamma"));
    assertEquals(true, page.evaluate("absolute"));
  }

  @PlaywrightTest
  @DisplayName("DE-016 should dispatch device motion event")
  void shouldDispatchDeviceMotionEvent(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/device-motion.html");
    page.locator("html").dispatchEvent("devicemotion", mapOf(
      "acceleration", mapOf("x", 10, "y", 20, "z", 30),
      "accelerationIncludingGravity", mapOf("x", 15, "y", 25, "z", 35),
      "rotationRate", mapOf("alpha", 5, "beta", 10, "gamma", 15),
      "interval", 16));
    assertEquals("Moved", page.evaluate("result"));
    assertEquals(10, page.evaluate("acceleration.x"));
    assertEquals(20, page.evaluate("acceleration.y"));
    assertEquals(30, page.evaluate("acceleration.z"));
    assertEquals(15, page.evaluate("accelerationIncludingGravity.x"));
    assertEquals(25, page.evaluate("accelerationIncludingGravity.y"));
    assertEquals(35, page.evaluate("accelerationIncludingGravity.z"));
    assertEquals(5, page.evaluate("rotationRate.alpha"));
    assertEquals(10, page.evaluate("rotationRate.beta"));
    assertEquals(15, page.evaluate("rotationRate.gamma"));
    assertEquals(16, page.evaluate("interval"));
  }

  @PlaywrightTest
  @DisplayName("DE-017 should throw if argument is from different frame")
  void shouldThrowIfArgumentIsFromDifferentFrame(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/frames/one-frame.html");
    {
      JSHandle dataTransfer = page.frames().get(1).evaluateHandle("() => new DataTransfer()");
      page.frameLocator("iframe").locator("div").dispatchEvent("drop", mapOf("dataTransfer", dataTransfer));
    }
    {
      JSHandle dataTransfer = page.evaluateHandle("() => new DataTransfer()");
      PlaywrightException error = assertThrows(PlaywrightException.class,
        () -> page.frameLocator("iframe").locator("div").dispatchEvent("drop", mapOf("dataTransfer", dataTransfer)));
      assertTrue(error.getMessage().contains("JSHandles can be evaluated only in the context they were created!"));
    }
  }
}
