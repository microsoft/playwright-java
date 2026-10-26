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

import com.microsoft.playwright.Page;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.BoundingBox;
import com.microsoft.playwright.options.MouseButton;
import com.microsoft.playwright.Mouse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.condition.OS;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static com.microsoft.playwright.fixtures.Utils.rafraf;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** Port of tests/page/page-mouse.spec.ts. */
public class TestPageMouse {
  private static final String dimensions = "() => {\n" +
    "  const rect = document.querySelector('textarea').getBoundingClientRect();\n" +
    "  return {\n" +
    "    x: rect.left,\n" +
    "    y: rect.top,\n" +
    "    width: rect.width,\n" +
    "    height: rect.height\n" +
    "  };\n" +
    "}";

  @PlaywrightTest
  @DisplayName("MO-001 should click the document @smoke")
  void shouldClickTheDocument(Page page, TestServer server) {
    page.evaluate("() => {\n" +
      "  window['clickPromise'] = new Promise(resolve => {\n" +
      "    document.addEventListener('click', event => {\n" +
      "      resolve({\n" +
      "        type: event.type,\n" +
      "        detail: event.detail,\n" +
      "        clientX: event.clientX,\n" +
      "        clientY: event.clientY,\n" +
      "        isTrusted: event.isTrusted,\n" +
      "        button: event.button\n" +
      "      });\n" +
      "    });\n" +
      "  });\n" +
      "}");
    page.mouse().click(50, 60);
    @SuppressWarnings("unchecked")
    Map<String, Object> event = (Map<String, Object>) page.evaluate("() => window['clickPromise']");
    assertEquals("click", event.get("type"));
    assertEquals(1, event.get("detail"));
    assertEquals(50, event.get("clientX"));
    assertEquals(60, event.get("clientY"));
    assertEquals(true, event.get("isTrusted"));
    assertEquals(0, event.get("button"));
  }

  @PlaywrightTest
  @DisplayName("MO-002 should dblclick the div")
  void shouldDblclickTheDiv(Page page, TestServer server) {
    page.setContent("<div style='width: 100px; height: 100px;'>Click me</div>");
    page.evaluate("() => {\n" +
      "  window['dblclickPromise'] = new Promise(resolve => {\n" +
      "    document.querySelector('div').addEventListener('dblclick', event => {\n" +
      "      resolve({\n" +
      "        type: event.type,\n" +
      "        detail: event.detail,\n" +
      "        clientX: event.clientX,\n" +
      "        clientY: event.clientY,\n" +
      "        isTrusted: event.isTrusted,\n" +
      "        button: event.button,\n" +
      "      });\n" +
      "    });\n" +
      "  });\n" +
      "}");
    page.mouse().dblclick(50, 60);
    @SuppressWarnings("unchecked")
    Map<String, Object> event = (Map<String, Object>) page.evaluate("() => window['dblclickPromise']");
    assertEquals("dblclick", event.get("type"));
    assertEquals(2, event.get("detail"));
    assertEquals(50, event.get("clientX"));
    assertEquals(60, event.get("clientY"));
    assertEquals(true, event.get("isTrusted"));
    assertEquals(0, event.get("button"));
  }

  @PlaywrightTest
  @DisplayName("MO-003 down and up should generate click")
  void downAndUpShouldGenerateClick(Page page, TestServer server) {
    page.evaluate("() => {\n" +
      "  window['clickPromise'] = new Promise(resolve => {\n" +
      "    document.addEventListener('click', event => {\n" +
      "      resolve({\n" +
      "        type: event.type,\n" +
      "        detail: event.detail,\n" +
      "        clientX: event.clientX,\n" +
      "        clientY: event.clientY,\n" +
      "        isTrusted: event.isTrusted,\n" +
      "        button: event.button\n" +
      "      });\n" +
      "    });\n" +
      "  });\n" +
      "}");
    page.mouse().move(50, 60);
    page.mouse().down();
    page.mouse().up();
    @SuppressWarnings("unchecked")
    Map<String, Object> event = (Map<String, Object>) page.evaluate("() => window['clickPromise']");
    assertEquals("click", event.get("type"));
    assertEquals(1, event.get("detail"));
    assertEquals(50, event.get("clientX"));
    assertEquals(60, event.get("clientY"));
    assertEquals(true, event.get("isTrusted"));
    assertEquals(0, event.get("button"));
  }

  @PlaywrightTest
  @DisplayName("MO-004 should pointerdown the div with a custom button")
  void shouldPointerdownTheDivWithACustomButton(Page page, TestServer server, BrowserName browserName) {
    page.setContent("<div style='width: 100px; height: 100px;'>Click me</div>");
    page.evaluate("() => {\n" +
      "  window['pointerdownPromise'] = new Promise(resolve => {\n" +
      "    document.querySelector('div').addEventListener('pointerdown', event => {\n" +
      "      resolve({\n" +
      "        type: event.type,\n" +
      "        detail: event.detail,\n" +
      "        clientX: event.clientX,\n" +
      "        clientY: event.clientY,\n" +
      "        isTrusted: event.isTrusted,\n" +
      "        button: event.button,\n" +
      "        buttons: event.buttons,\n" +
      "        pointerId: event.pointerId,\n" +
      "      });\n" +
      "    });\n" +
      "  });\n" +
      "}");
    page.mouse().click(50, 60, new Mouse.ClickOptions()
      .setButton(MouseButton.MIDDLE));
    @SuppressWarnings("unchecked")
    Map<String, Object> event = (Map<String, Object>) page.evaluate("() => window['pointerdownPromise']");
    assertEquals("pointerdown", event.get("type"));
    assertEquals(browserName == BrowserName.WEBKIT ? 1 : 0, event.get("detail"));
    assertEquals(50, event.get("clientX"));
    assertEquals(60, event.get("clientY"));
    assertEquals(true, event.get("isTrusted"));
    assertEquals(1, event.get("button"));
    assertEquals(4, event.get("buttons"));
    assertEquals(browserName == BrowserName.FIREFOX ? 0 : 1, event.get("pointerId"));
  }

  @PlaywrightTest
  @DisplayName("MO-005 should report correct buttons property")
  void shouldReportCorrectButtonsProperty(Page page) {
    page.evaluate("() => {\n" +
      "  window.__EVENTS = [];\n" +
      "  const handler = event => {\n" +
      "    window.__EVENTS.push({\n" +
      "      type: event.type,\n" +
      "      button: event.button,\n" +
      "      buttons: event.buttons,\n" +
      "    });\n" +
      "  };\n" +
      "  window.addEventListener('mousedown', handler, false);\n" +
      "  window.addEventListener('mouseup', handler, false);\n" +
      "}");
    page.mouse().move(50, 60);
    page.mouse().down(new Mouse.DownOptions()
      .setButton(MouseButton.MIDDLE));
    page.mouse().down(new Mouse.DownOptions()
      .setButton(MouseButton.LEFT));
    page.mouse().up(new Mouse.UpOptions()
      .setButton(MouseButton.MIDDLE));
    page.mouse().up(new Mouse.UpOptions()
      .setButton(MouseButton.LEFT));
    assertEquals(Arrays.asList(
      mapOf("type", "mousedown", "button", 1, "buttons", 4),
      mapOf("type", "mousedown", "button", 0, "buttons", 5),
      mapOf("type", "mouseup", "button", 1, "buttons", 1),
      mapOf("type", "mouseup", "button", 0, "buttons", 0)),
      page.evaluate("() => window.__EVENTS"));
  }

  @PlaywrightTest
  @DisplayName("MO-006 should report correct pointerType property")
  void shouldReportCorrectPointerTypeProperty(Page page) {
    page.mouse().move(50, 60);
    page.evaluate("() => {\n" +
      "  window.__EVENTS = [];\n" +
      "  const handler = event => {\n" +
      "    window.__EVENTS.push({\n" +
      "      type: event.type,\n" +
      "      pointerType: event.pointerType,\n" +
      "    });\n" +
      "  };\n" +
      "  window.addEventListener('pointerdown', handler, false);\n" +
      "  window.addEventListener('pointermove', handler, false);\n" +
      "  window.addEventListener('pointerup', handler, false);\n" +
      "}");
    page.mouse().move(60, 50);
    page.mouse().down();
    page.mouse().up();
    assertEquals(Arrays.asList(
      mapOf("type", "pointermove", "pointerType", "mouse"),
      mapOf("type", "pointerdown", "pointerType", "mouse"),
      mapOf("type", "pointerup", "pointerType", "mouse")),
      page.evaluate("() => window.__EVENTS"));
  }

  @PlaywrightTest
  @DisplayName("MO-007 should select the text with mouse")
  void shouldSelectTheTextWithMouse(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/textarea.html");
    page.focus("textarea");
    String text = "This is the text that we are going to try to select. Let's see how it goes.";
    page.keyboard().type(text);
    // Firefox needs an extra frame here after typing or it will fail to set the scrollTop
    rafraf(page);
    page.evaluate("() => document.querySelector('textarea').scrollTop = 0");
    @SuppressWarnings("unchecked")
    Map<String, Object> box = (Map<String, Object>) page.evaluate(dimensions);
    double x = ((Number) box.get("x")).doubleValue();
    double y = ((Number) box.get("y")).doubleValue();
    page.mouse().move(x + 2, y + 2);
    page.mouse().down();
    page.mouse().move(200, 200);
    page.mouse().up();
    assertEquals(text, page.evaluate("() => {\n" +
      "  const textarea = document.querySelector('textarea');\n" +
      "  return textarea.value.substring(textarea.selectionStart, textarea.selectionEnd);\n" +
      "}"));
  }

  @PlaywrightTest
  @DisplayName("MO-008 should trigger hover state")
  @DisabledOnBrowser(value = {BrowserName.CHROMIUM, BrowserName.FIREFOX, BrowserName.WEBKIT}, headed = true, reason = "headed messes up with hover")
  void shouldTriggerHoverState(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/scrollable.html");
    page.hover("#button-6");
    assertEquals("button-6", page.evaluate("() => document.querySelector('button:hover').id"));
    page.hover("#button-2");
    assertEquals("button-2", page.evaluate("() => document.querySelector('button:hover').id"));
    page.hover("#button-91");
    assertEquals("button-91", page.evaluate("() => document.querySelector('button:hover').id"));
  }

  @PlaywrightTest
  @DisplayName("MO-009 should trigger hover state on disabled button")
  @DisabledOnBrowser(value = {BrowserName.CHROMIUM, BrowserName.FIREFOX, BrowserName.WEBKIT}, headed = true, reason = "headed messes up with hover")
  void shouldTriggerHoverStateOnDisabledButton(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/scrollable.html");
    page.evalOnSelector("#button-6", "button => button.disabled = true");
    page.hover("#button-6", new Page.HoverOptions().setTimeout(5000));
    assertEquals("button-6", page.evaluate("() => document.querySelector('button:hover').id"));
  }

  @PlaywrightTest
  @DisplayName("MO-010 should trigger hover state with removed window.Node")
  @DisabledOnBrowser(value = {BrowserName.CHROMIUM, BrowserName.FIREFOX, BrowserName.WEBKIT}, headed = true, reason = "headed messes up with hover")
  void shouldTriggerHoverStateWithRemovedWindowNode(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/scrollable.html");
    page.evaluate("() => delete window.Node");
    page.hover("#button-6");
    assertEquals("button-6", page.evaluate("() => document.querySelector('button:hover').id"));
  }

  @PlaywrightTest
  @DisplayName("MO-011 should set modifier keys on click")
  void shouldSetModifierKeysOnClick(Page page, TestServer server, BrowserName browserName) {
    page.navigate(server.PREFIX + "/input/scrollable.html");
    page.evaluate("() => document.querySelector('#button-3').addEventListener('mousedown', e => window['lastEvent'] = e, true)");
    Map<String, String> modifiers = new LinkedHashMap<>();
    modifiers.put("Shift", "shiftKey");
    modifiers.put("Control", "ctrlKey");
    modifiers.put("Alt", "altKey");
    modifiers.put("Meta", "metaKey");
    // In Firefox, the Meta modifier only exists on Mac
    if (browserName == BrowserName.FIREFOX && !Env.isMac) {
      modifiers.remove("Meta");
    }
    for (String modifier : modifiers.keySet()) {
      page.keyboard().down(modifier);
      page.click("#button-3");
      if (!(Boolean) page.evaluate("mod => window['lastEvent'][mod]", modifiers.get(modifier))) {
        throw new Error(modifiers.get(modifier) + " should be true");
      }
      page.keyboard().up(modifier);
    }
    page.click("#button-3");
    for (String modifier : modifiers.keySet()) {
      if ((Boolean) page.evaluate("mod => window['lastEvent'][mod]", modifiers.get(modifier))) {
        throw new Error(modifiers.get(modifier) + " should be false");
      }
    }
  }

  @PlaywrightTest
  @DisplayName("MO-012 should tween mouse movement")
  @Fixme(value = "harness: WebKit reports movementX and movementY as 0 for mouse.move in this environment: [[120, 140, 0, 0], ...]", browsers = BrowserName.WEBKIT)
  @DisabledOnBrowser(value = {BrowserName.CHROMIUM, BrowserName.FIREFOX, BrowserName.WEBKIT}, headed = true, reason = "actual mouse interferes with the exact mousemove events")
  void shouldTweenMouseMovement(Page page, BrowserName browserName) {
    // The test becomes flaky on WebKit without next line.
    if (browserName == BrowserName.WEBKIT) {
      page.evaluate("() => new Promise(requestAnimationFrame)");
    }
    page.mouse().move(100, 100);
    page.evaluate("() => {\n" +
      "  window['result'] = [];\n" +
      "  document.addEventListener('mousemove', event => {\n" +
      "    window['result'].push([event.clientX, event.clientY, event.movementX, event.movementY]);\n" +
      "  });\n" +
      "}");
    page.mouse().move(200, 300, new Mouse.MoveOptions().setSteps(5));
    assertEquals(Arrays.asList(
      Arrays.asList(120, 140, 20, 40),
      Arrays.asList(140, 180, 20, 40),
      Arrays.asList(160, 220, 20, 40),
      Arrays.asList(180, 260, 20, 40),
      Arrays.asList(200, 300, 20, 40)),
      page.evaluate("result"));
  }

  @PlaywrightTest
  @DisplayName("MO-013 should always round down")
  void shouldAlwaysRoundDown(Page page) {
    page.evaluate("() => {\n" +
      "  document.addEventListener('mousedown', event => {\n" +
      "    window['result'] = [event.clientX, event.clientY];\n" +
      "  });\n" +
      "}");
    page.mouse().click(50.1, 50.9);
    assertEquals(Arrays.asList(50, 50), page.evaluate("result"));
  }

  @PlaywrightTest
  @DisplayName("MO-014 should not crash on mouse drag with any button")
  void shouldNotCrashOnMouseDragWithAnyButton(Page page) {
    page.evaluate("() => {\n" +
      "  // Do not show contextmenu on right click since it is poorly supported.\n" +
      "  window.addEventListener('contextmenu', e => e.preventDefault(), false);\n" +
      "}");
    for (MouseButton button : new MouseButton[]{MouseButton.LEFT, MouseButton.MIDDLE, MouseButton.RIGHT}) {
      page.mouse().move(50, 50);
      page.mouse().down(new Mouse.DownOptions().setButton(button));
      page.mouse().move(100, 100);
    }
  }

  @PlaywrightTest
  @DisplayName("MO-015 should dispatch mouse move after context menu was opened")
  @DisabledOnBrowser(value = BrowserName.CHROMIUM, os = OS.WINDOWS, reason = "fixme: context menu support is best-effort for Linux and MacOS")
  void shouldDispatchMouseMoveAfterContextMenuWasOpened(Page page, BrowserName browserName) {
    page.evaluate("() => {\n" +
      "  window['contextMenuPromise'] = new Promise(x => {\n" +
      "    window.addEventListener('contextmenu', x, false);\n" +
      "  });\n" +
      "}");
    int CX = 100;
    int CY = 100;
    page.mouse().move(CX, CY);
    page.mouse().down(new Mouse.DownOptions().setButton(MouseButton.RIGHT));
    page.evaluate("() => window['contextMenuPromise']");
    int N = 20;
    for (int radius : new int[]{10, 30, 60, 90}) {
      for (int i = 0; i < N; ++i) {
        double angle = 2 * Math.PI * i / N;
        long x = CX + Math.round(radius * Math.cos(angle));
        long y = CY + Math.round(radius * Math.sin(angle));
        page.mouse().move(x, y);
      }
    }
  }

  @PlaywrightTest
  @DisplayName("MO-016 should track hover across iframe boundaries")
  @DisabledOnBrowser(value = {BrowserName.CHROMIUM, BrowserName.FIREFOX, BrowserName.WEBKIT}, headed = true, reason = "headed messes up with hover")
  void shouldTrackHoverAcrossIframeBoundaries(Page page) {
    page.setContent("\n" +
      "    <style>\n" +
      "      body, html { margin: 0; padding: 0; }\n" +
      "      #parentBox { position: absolute; left: 10px; top: 10px; width: 100px; height: 100px; }\n" +
      "      iframe { position: absolute; left: 200px; top: 10px; width: 200px; height: 200px; border: none; }\n" +
      "    </style>\n" +
      "    <div id=\"parentBox\"></div>\n" +
      "    <iframe srcdoc=\"\n" +
      "      <style>body, html { margin: 0; padding: 0; } #childBox { width: 180px; height: 180px; }</style>\n" +
      "      <div id='childBox'></div>\n" +
      "      <script>\n" +
      "        const box = document.querySelector('#childBox');\n" +
      "        box.addEventListener('mouseenter', () => window.top.__log.push('child:enter'));\n" +
      "        box.addEventListener('mouseleave', () => window.top.__log.push('child:leave'));\n" +
      "      </script>\n" +
      "    \"></iframe>\n" +
      "    <script>\n" +
      "      window.__log = [];\n" +
      "      const box = document.querySelector('#parentBox');\n" +
      "      box.addEventListener('mouseenter', () => window.__log.push('parent:enter'));\n" +
      "      box.addEventListener('mouseleave', () => window.__log.push('parent:leave'));\n" +
      "    </script>\n" +
      "  ");
    page.waitForSelector("iframe");
    page.frames().get(1).waitForSelector("#childBox");
    Supplier<Object> log = () -> page.evaluate("() => window['__log']");
    BoundingBox parentBox = page.locator("#parentBox").boundingBox();
    BoundingBox iframeBox = page.locator("iframe").boundingBox();
    double[] parentCenter = {parentBox.x + parentBox.width / 2, parentBox.y + parentBox.height / 2};
    double[] childCenter = {iframeBox.x + 90, iframeBox.y + 90};

    page.mouse().move(parentCenter[0], parentCenter[1]);
    expectPollToEqual(page, log, Arrays.asList("parent:enter"));

    // Crossing into the iframe leaves the parent element and enters the child.
    page.mouse().move(childCenter[0], childCenter[1]);
    expectPollToEqual(page, log, Arrays.asList("parent:enter", "parent:leave", "child:enter"));

    // Crossing back out leaves the child element and re-enters the parent.
    page.mouse().move(parentCenter[0], parentCenter[1]);
    expectPollToEqual(page, log, Arrays.asList("parent:enter", "parent:leave", "child:enter", "child:leave", "parent:enter"));
  }

  /** Java: there is no expect.poll, this is {@code await expect.poll(log).toEqual(expected)} with its 5s timeout. */
  private static void expectPollToEqual(Page page, Supplier<Object> log, Object expected) {
    long deadline = System.currentTimeMillis() + 5000;
    Object received = log.get();
    while (!expected.equals(received) && System.currentTimeMillis() < deadline) {
      page.waitForTimeout(100);
      received = log.get();
    }
    assertEquals(expected, received);
  }
}
