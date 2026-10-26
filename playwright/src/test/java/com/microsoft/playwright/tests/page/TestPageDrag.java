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

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.JSHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.EnabledOnBrowser;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.microsoft.playwright.fixtures.Utils.attachFrame;
import static com.microsoft.playwright.fixtures.Utils.browserMajorVersion;
import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/page/page-drag.spec.ts. */
@DisabledOnBrowser(value = {BrowserName.CHROMIUM, BrowserName.FIREFOX, BrowserName.WEBKIT}, headed = true, reason = "Stray mouse events mess up the tests.")
public class TestPageDrag {
  @BeforeEach
  void skipOldChromium(Browser browser, BrowserName browserName) {
    assumeFalse(browserName == BrowserName.CHROMIUM && browserMajorVersion(browser) < 91);
  }

  // Java: expect.poll(() => value).toEqual(expected); waitForCondition() dispatches the events while waiting.
  private static <T> void expectPollToEqual(Page page, Supplier<T> value, T expected) {
    try {
      page.waitForCondition(() -> expected.equals(value.get()), new Page.WaitForConditionOptions().setTimeout(5000));
    } catch (TimeoutError e) {
      // The assertion below reports the last value.
    }
    assertEquals(expected, value.get());
  }

  @Nested
  class DragAndDrop {
    @PlaywrightTest
    @DisplayName("DD-001 should work @smoke")
    void shouldWork(Page page, TestServer server) {
      page.navigate(server.PREFIX + "/drag-n-drop.html");
      page.hover("#source");
      page.mouse().down();
      page.hover("#target");
      page.mouse().up();
      assertEquals(true, page.evalOnSelector("#target", "target => target.contains(document.querySelector('#source'))")); // could not find source in target
    }

    @PlaywrightTest
    @DisplayName("DD-002 should send the right events")
    void shouldSendTheRightEvents(TestServer server, Page page, BrowserName browserName) {
      page.navigate(server.PREFIX + "/drag-n-drop.html");
      JSHandle events = trackEvents(page.querySelector("body"));
      page.hover("#source");
      page.mouse().down();
      page.hover("#target");
      page.mouse().up();
      assertEquals(Arrays.asList(
        "mousemove at 120;86",
        "mousedown at 120;86",
        browserName == BrowserName.FIREFOX ? "dragstart at 120;86" : "mousemove at 240;350",
        browserName == BrowserName.FIREFOX ? "mousemove at 240;350" : "dragstart at 120;86",
        "dragenter at 240;350",
        "dragover at 240;350",
        "drop at 240;350",
        "dragend"), events.jsonValue());
    }

    @PlaywrightTest
    @DisplayName("DD-003 should not send dragover on the first mousemove")
    @EnabledOnBrowser(value = BrowserName.CHROMIUM, reason = "fixme: ")
    void shouldNotSendDragoverOnTheFirstMousemove(TestServer server, Page page, BrowserName browserName) {
      page.navigate(server.PREFIX + "/drag-n-drop.html");
      JSHandle events = trackEvents(page.querySelector("body"));
      page.hover("#source");
      page.mouse().down();
      page.hover("#target");
      assertEquals(Arrays.asList(
        "mousemove at 120;86",
        "mousedown at 120;86",
        browserName == BrowserName.FIREFOX ? "dragstart at 120;86" : "mousemove at 240;350",
        browserName == BrowserName.FIREFOX ? "mousemove at 240;350" : "dragstart at 120;86",
        "dragenter at 240;350"), events.jsonValue());
    }

    @PlaywrightTest
    @DisplayName("DD-004 should work inside iframe")
    void shouldWorkInsideIframe(Page page, TestServer server, BrowserName browserName) {
      page.navigate(server.EMPTY_PAGE);
      Frame frame = attachFrame(page, "myframe", server.PREFIX + "/drag-n-drop.html");
      page.evalOnSelector("iframe", "iframe => {\n" +
        "  iframe.style.width = '500px';\n" +
        "  iframe.style.height = '600px';\n" +
        "  iframe.style.marginLeft = '80px';\n" +
        "  iframe.style.marginTop = '60px';\n" +
        "}");
      JSHandle pageEvents = trackEvents(page.querySelector("body"));
      JSHandle frameEvents = trackEvents(frame.querySelector("body"));
      frame.hover("#source");
      page.mouse().down();
      frame.hover("#target");
      page.mouse().up();
      assertEquals(true, frame.evalOnSelector("#target", "target => target.contains(document.querySelector('#source'))")); // could not find source in target
      assertEquals(Arrays.asList(
        "mousemove at 120;86",
        "mousedown at 120;86",
        browserName == BrowserName.FIREFOX ? "dragstart at 120;86" : "mousemove at 240;350",
        browserName == BrowserName.FIREFOX ? "mousemove at 240;350" : "dragstart at 120;86",
        "dragenter at 240;350",
        "dragover at 240;350",
        "drop at 240;350",
        "dragend"), frameEvents.jsonValue());
      assertEquals(Arrays.asList(), pageEvents.jsonValue());
    }

    @PlaywrightTest
    @DisplayName("DD-005 should cancel on escape")
    void shouldCancelOnEscape(TestServer server, Page page, BrowserName browserName) {
      page.navigate(server.PREFIX + "/drag-n-drop.html");
      JSHandle events = trackEvents(page.querySelector("body"));
      page.hover("#source");
      page.mouse().down();
      page.hover("#target");
      page.keyboard().press("Escape");
      page.mouse().up();
      assertEquals(false, page.evalOnSelector("#target", "target => target.contains(document.querySelector('#source'))")); // found source in target
      // Java: [..., null, ...].filter(Boolean) drops the null entry.
      assertEquals(Arrays.asList(
        "mousemove at 120;86",
        "mousedown at 120;86",
        browserName == BrowserName.FIREFOX ? "dragstart at 120;86" : "mousemove at 240;350",
        browserName == BrowserName.FIREFOX ? "mousemove at 240;350" : "dragstart at 120;86",
        "dragenter at 240;350",
        browserName == BrowserName.CHROMIUM ? null : "dragover at 240;350",
        "dragend",
        "mouseup at 240;350").stream().filter(Objects::nonNull).collect(Collectors.toList()), events.jsonValue());
    }

    @Nested
    @DisabledOnBrowser(value = {BrowserName.CHROMIUM, BrowserName.FIREFOX, BrowserName.WEBKIT}, reason = "fixme: implement dragging with iframes")
    class Iframe {
      @PlaywrightTest
      @DisplayName("DD-006 should drag into an iframe")
      void shouldDragIntoAnIframe(TestServer server, Page page, BrowserName browserName) {
        page.navigate(server.PREFIX + "/drag-n-drop.html");
        Frame frame = attachFrame(page, "oopif", server.PREFIX + "/drag-n-drop.html");
        page.evalOnSelector("iframe", "iframe => {\n" +
          "  iframe.style.width = '500px';\n" +
          "  iframe.style.height = '600px';\n" +
          "  iframe.style.marginLeft = '500px';\n" +
          "  iframe.style.marginTop = '60px';\n" +
          "}");
        JSHandle pageEvents = trackEvents(page.querySelector("body"));
        JSHandle frameEvents = trackEvents(frame.querySelector("body"));
        page.hover("#source");
        page.mouse().down();
        frame.hover("#target");
        page.mouse().up();
        assertEquals(true, frame.evalOnSelector("#target", "target => target.contains(document.querySelector('#source'))")); // could not find source in target
        assertEquals(Arrays.asList(
          "mousemove",
          "mousedown",
          browserName == BrowserName.FIREFOX ? "dragstart" : "mousemove",
          browserName == BrowserName.FIREFOX ? "mousemove" : "dragstart"), pageEvents.jsonValue());
        assertEquals(Arrays.asList(
          "dragenter",
          "dragover",
          "drop"), frameEvents.jsonValue());
      }

      @PlaywrightTest
      @DisplayName("DD-007 should drag out of an iframe")
      void shouldDragOutOfAnIframe(TestServer server, Page page) {
        page.navigate(server.PREFIX + "/drag-n-drop.html");
        Frame frame = attachFrame(page, "oopif", server.PREFIX + "/drag-n-drop.html");
        JSHandle pageEvents = trackEvents(page.querySelector("body"));
        JSHandle frameEvents = trackEvents(frame.querySelector("body"));
        frame.hover("#source");
        page.mouse().down();
        page.hover("#target");
        page.mouse().up();
        assertEquals(true, page.evalOnSelector("#target", "target => target.contains(document.querySelector('#source'))")); // could not find source in target
        assertEquals(Arrays.asList(
          "mousemove",
          "mousedown",
          "dragstart",
          "dragend"), frameEvents.jsonValue());
        assertEquals(Arrays.asList(
          "dragenter",
          "dragover",
          "drop"), pageEvents.jsonValue());
      }
    }

    @PlaywrightTest
    @DisplayName("DD-008 should respect the drop effect")
    void shouldRespectTheDropEffect(Page page, BrowserName browserName) {
      assumeFalse(browserName == BrowserName.WEBKIT && Env.isLinux && Env.headed(), "fixme: https://github.com/microsoft/playwright/issues/21646");
      assumeFalse(browserName == BrowserName.CHROMIUM && !Env.isMac && Env.headed(), "fixme: https://github.com/microsoft/playwright/issues/21646");

      assertEquals(true, testIfDropped(page, "copy", "copy"));
      assertEquals(false, testIfDropped(page, "copy", "move"));
      assertEquals(true, testIfDropped(page, "all", "link"));
      assertEquals(false, testIfDropped(page, "all", "none"));

      assertEquals(true, testIfDropped(page, "copyMove", "copy"));
      assertEquals(true, testIfDropped(page, "copyLink", "copy"));
      assertEquals(false, testIfDropped(page, "linkMove", "copy"));

      assertEquals(false, testIfDropped(page, "copyMove", "link"));
      assertEquals(true, testIfDropped(page, "copyLink", "link"));
      assertEquals(true, testIfDropped(page, "linkMove", "link"));

      assertEquals(true, testIfDropped(page, "copyMove", "move"));
      assertEquals(false, testIfDropped(page, "copyLink", "move"));
      assertEquals(true, testIfDropped(page, "linkMove", "move"));

      assertEquals(true, testIfDropped(page, "uninitialized", "copy"));
    }

    private Object testIfDropped(Page page, String effectAllowed, String dropEffect) {
      page.setContent("\n" +
        "        <div draggable=\"true\">drag target</div>\n" +
        "        <drop-target>this is the drop target</drop-target>\n" +
        "      ");
      page.evaluate("({ effectAllowed, dropEffect }) => {\n" +
        "  window['dropped'] = false;\n" +
        "\n" +
        "  document.querySelector('div').addEventListener('dragstart', event => {\n" +
        "    event.dataTransfer.effectAllowed = effectAllowed;\n" +
        "    event.dataTransfer.setData('text/plain', 'drag data');\n" +
        "  });\n" +
        "\n" +
        "  const dropTarget = document.querySelector('drop-target');\n" +
        "  dropTarget.addEventListener('dragover', event => {\n" +
        "    event.dataTransfer.dropEffect = dropEffect;\n" +
        "    event.preventDefault();\n" +
        "  });\n" +
        "  dropTarget.addEventListener('drop', event => {\n" +
        "    window['dropped'] = true;\n" +
        "  });\n" +
        "}", mapOf("effectAllowed", effectAllowed, "dropEffect", dropEffect));
      page.hover("div");
      page.mouse().down();
      page.hover("drop-target");
      page.mouse().up();
      return page.evaluate("dropped");
    }

    @PlaywrightTest
    @DisplayName("DD-028 should report the default drop effect from effectAllowed")
    @Fixme(value = "driver: the bundled WebKit reports the dropEffect 'copyMove' instead of 'move' (expected: <move> but was: <copyMove>)", browsers = BrowserName.WEBKIT)
    void shouldReportTheDefaultDropEffectFromEffectAllowed(Page page, BrowserName browserName) {
      assumeFalse(browserName == BrowserName.WEBKIT && Env.isLinux && Env.headed(), "fixme: https://github.com/microsoft/playwright/issues/21646");

      page.setContent("\n" +
        "      <div draggable=\"true\">drag target</div>\n" +
        "      <drop-target>this is the drop target</drop-target>\n" +
        "    ");
      page.evaluate("() => {\n" +
        "  window['dropEffect'] = 'none';\n" +
        "\n" +
        "  document.querySelector('div').addEventListener('dragstart', event => {\n" +
        "    event.dataTransfer.effectAllowed = 'copyMove';\n" +
        "    event.dataTransfer.setData('text/plain', 'drag data');\n" +
        "  });\n" +
        "  document.querySelector('div').addEventListener('dragend', event => {\n" +
        "    window['dropEffect'] = event.dataTransfer.dropEffect;\n" +
        "  });\n" +
        "\n" +
        "  // The target accepts the drag without setting dropEffect.\n" +
        "  document.querySelector('drop-target').addEventListener('dragover', event => {\n" +
        "    event.preventDefault();\n" +
        "  });\n" +
        "}");
      page.hover("div");
      page.mouse().down();
      page.hover("drop-target");
      page.mouse().up();
      assertEquals("move", page.evaluate("dropEffect"));
    }

    @PlaywrightTest
    @DisplayName("DD-009 should work if the drag is canceled")
    void shouldWorkIfTheDragIsCanceled(Page page, TestServer server) {
      page.navigate(server.PREFIX + "/drag-n-drop.html");
      page.evaluate("() => {\n" +
        "  document.body.addEventListener('dragstart', event => {\n" +
        "    event.preventDefault();\n" +
        "  }, false);\n" +
        "}");
      page.hover("#source");
      page.mouse().down();
      page.hover("#target");
      page.mouse().up();
      assertEquals(false, page.evalOnSelector("#target", "target => target.contains(document.querySelector('#source'))"));
    }

    @PlaywrightTest
    @DisplayName("DD-010 should work if the drag event is captured but not canceled")
    void shouldWorkIfTheDragEventIsCapturedButNotCanceled(Page page, TestServer server) {
      page.navigate(server.PREFIX + "/drag-n-drop.html");
      page.evaluate("() => {\n" +
        "  document.body.addEventListener('dragstart', event => {\n" +
        "    event.stopImmediatePropagation();\n" +
        "  }, false);\n" +
        "}");
      page.hover("#source");
      page.mouse().down();
      page.hover("#target");
      page.mouse().up();
      assertEquals(true, page.evalOnSelector("#target", "target => target.contains(document.querySelector('#source'))"));
    }

    @PlaywrightTest
    @DisplayName("DD-011 should be able to drag the mouse in a frame")
    void shouldBeAbleToDragTheMouseInAFrame(Page page, TestServer server) {
      page.navigate(server.PREFIX + "/frames/one-frame.html");
      JSHandle eventsHandle = trackEvents(page.frames().get(1).querySelector("html"));
      page.mouse().move(30, 30);
      page.mouse().down();
      page.mouse().move(60, 60);
      page.mouse().up();
      assertEquals(Arrays.asList("mousemove at 20;20", "mousedown at 20;20", "mousemove at 50;50", "mouseup at 50;50"), eventsHandle.jsonValue());
    }

    @PlaywrightTest
    @DisplayName("DD-012 should work if a frame is stalled")
    void shouldWorkIfAFrameIsStalled(Page page, TestServer server) throws Exception {
      page.navigate(server.PREFIX + "/drag-n-drop.html");
      CompletableFuture<Route> routePromise = new CompletableFuture<>();
      page.route("**/empty.html", route -> {
        routePromise.complete(route);
      });
      // Java: attachFrame(...).catch(() => {}) is not awaited upstream, but its evaluate waits for the frame to load, which never ends while the frame is stalled; the frame is attached without waiting for the load.
      page.evaluate("url => {\n" +
        "  const frame = document.createElement('iframe');\n" +
        "  frame.src = url;\n" +
        "  frame.id = 'frame';\n" +
        "  document.body.appendChild(frame);\n" +
        "}", server.EMPTY_PAGE);
      page.context().waitForCondition(() -> routePromise.isDone());
      Route route = routePromise.get();
      page.hover("#source");
      page.mouse().down();
      page.hover("#target");
      page.mouse().up();
      route.abort();
      assertEquals(true, page.evalOnSelector("#target", "target => target.contains(document.querySelector('#source'))")); // could not find source in target
    }

    @PlaywrightTest
    @DisplayName("DD-013 should work with the helper method")
    void shouldWorkWithTheHelperMethod(Page page, TestServer server) {
      page.navigate(server.PREFIX + "/drag-n-drop.html");
      page.dragAndDrop("#source", "#target");
      assertEquals(true, page.evalOnSelector("#target", "target => target.contains(document.querySelector('#source'))")); // could not find source in target
    }

    @Nested
    @ParameterizedClass
    @ValueSource(strings = {"dragAndDrop", "dragTo"})
    class TweenedMouseMovement {
      @Parameter
      String title;

      // Java: the drag functions of the upstream table, selected by the title.
      private void drag(Page page, int steps) {
        if (title.equals("dragAndDrop")) {
          page.dragAndDrop("#red", "#blue", new Page.DragAndDropOptions().setSteps(steps));
        } else {
          page.locator("#red").dragTo(page.locator("#blue"), new com.microsoft.playwright.Locator.DragToOptions().setSteps(steps));
        }
      }

      @PlaywrightTest
      @DisplayName("DD-014 should ${title} with tweened mouse movement")
      @DisabledOnBrowser(value = {BrowserName.CHROMIUM, BrowserName.FIREFOX, BrowserName.WEBKIT}, headed = true, reason = "actual mouse interferes with the exact mousemove events")
      void shouldDragWithTweenedMouseMovement(Page page) {
        page.setContent("\n" +
          "        <body style=\"margin: 0; padding: 0;\">\n" +
          "          <div style=\"width:100px;height:100px;background:red;\" id=\"red\"></div>\n" +
          "          <div style=\"width:300px;height:100px;background:blue;\" id=\"blue\"></div>\n" +
          "        </body>\n" +
          "      ");
        JSHandle eventsHandle = page.evaluateHandle("() => {\n" +
          "  const events = [];\n" +
          "  document.addEventListener('mousedown', event => {\n" +
          "    events.push({\n" +
          "      type: 'mousedown',\n" +
          "      x: event.pageX,\n" +
          "      y: event.pageY,\n" +
          "    });\n" +
          "  });\n" +
          "  document.addEventListener('mouseup', event => {\n" +
          "    events.push({\n" +
          "      type: 'mouseup',\n" +
          "      x: event.pageX,\n" +
          "      y: event.pageY,\n" +
          "    });\n" +
          "  });\n" +
          "  document.addEventListener('mousemove', event => {\n" +
          "    events.push({\n" +
          "      type: 'mousemove',\n" +
          "      x: event.pageX,\n" +
          "      y: event.pageY,\n" +
          "    });\n" +
          "  });\n" +
          "  return events;\n" +
          "}");
        drag(page, 4);
        expectPollToEqual(page, () -> eventsHandle.jsonValue(), (Object) Arrays.asList(
          mapOf("type", "mousemove", "x", 50, "y", 50),
          mapOf("type", "mousedown", "x", 50, "y", 50),
          mapOf("type", "mousemove", "x", 75, "y", 75),
          mapOf("type", "mousemove", "x", 100, "y", 100),
          mapOf("type", "mousemove", "x", 125, "y", 125),
          mapOf("type", "mousemove", "x", 150, "y", 150),
          mapOf("type", "mouseup", "x", 150, "y", 150)));
      }
    }

    @PlaywrightTest
    @DisplayName("DD-015 should allow specifying the position")
    void shouldAllowSpecifyingThePosition(Page page, TestServer server) {
      page.setContent("\n" +
        "      <div style=\"width:100px;height:100px;background:red;\" id=\"red\">\n" +
        "      </div>\n" +
        "      <div style=\"width:100px;height:100px;background:blue;\" id=\"blue\">\n" +
        "      </div>\n" +
        "    ");
      JSHandle eventsHandle = page.evaluateHandle("() => {\n" +
        "  const events = [];\n" +
        "  document.getElementById('red').addEventListener('mousedown', event => {\n" +
        "    events.push({\n" +
        "      type: 'mousedown',\n" +
        "      x: event.offsetX,\n" +
        "      y: event.offsetY,\n" +
        "    });\n" +
        "  });\n" +
        "  document.getElementById('blue').addEventListener('mouseup', event => {\n" +
        "    events.push({\n" +
        "      type: 'mouseup',\n" +
        "      x: event.offsetX,\n" +
        "      y: event.offsetY,\n" +
        "    });\n" +
        "  });\n" +
        "  return events;\n" +
        "}");
      page.dragAndDrop("#red", "#blue", new Page.DragAndDropOptions()
        .setSourcePosition(34, 7)
        .setTargetPosition(10, 20));
      assertEquals(Arrays.asList(
        mapOf("type", "mousedown", "x", 34, "y", 7),
        mapOf("type", "mouseup", "x", 10, "y", 20)), eventsHandle.jsonValue());
    }

    @PlaywrightTest
    @DisplayName("DD-016 should work with locators")
    void shouldWorkWithLocators(Page page, TestServer server) {
      page.navigate(server.PREFIX + "/drag-n-drop.html");
      page.locator("#source").dragTo(page.locator("#target"));
      assertEquals(true, page.evalOnSelector("#target", "target => target.contains(document.querySelector('#source'))")); // could not find source in target
    }
  }

  @PlaywrightTest
  @DisplayName("DD-017 should work if not doing a drag")
  void shouldWorkIfNotDoingADrag(Page page) {
    JSHandle eventsHandle = trackEvents(page.querySelector("html"));
    page.mouse().move(50, 50);
    page.mouse().down();
    page.mouse().move(100, 100);
    page.mouse().up();
    assertEquals(Arrays.asList("mousemove at 50;50", "mousedown at 50;50", "mousemove at 100;100", "mouseup at 100;100"), eventsHandle.jsonValue());
  }

  @PlaywrightTest
  @DisplayName("DD-018 should report event.buttons")
  void shouldReportEventButtons(Page page) {
    JSHandle logsHandle = page.evaluateHandle("async () => {\n" +
      "  const div = document.createElement('div');\n" +
      "  document.body.appendChild(div);\n" +
      "  div.style.width = '200px';\n" +
      "  div.style.height = '200px';\n" +
      "  div.style.backgroundColor = 'blue';\n" +
      "  div.addEventListener('mousedown', onEvent);\n" +
      "  div.addEventListener('mousemove', onEvent, { passive: false });\n" +
      "  div.addEventListener('mouseup', onEvent);\n" +
      "  const logs = [];\n" +
      "  function onEvent(event) {\n" +
      "    logs.push({ type: event.type, buttons: event.buttons });\n" +
      "  }\n" +
      "  await new Promise(window.builtins.requestAnimationFrame);\n" +
      "  return logs;\n" +
      "}");
    page.mouse().move(20, 20);
    page.mouse().down();
    page.mouse().move(40, 40);
    page.mouse().up();
    Object logs = logsHandle.jsonValue();
    assertEquals(Arrays.asList(
      mapOf("type", "mousemove", "buttons", 0),
      mapOf("type", "mousedown", "buttons", 1),
      mapOf("type", "mousemove", "buttons", 1),
      mapOf("type", "mouseup", "buttons", 0)), logs);
  }

  private static JSHandle trackEvents(ElementHandle target) {
    JSHandle eventsHandle = target.evaluateHandle("target => {\n" +
      "  const events = [];\n" +
      "  for (const event of [\n" +
      "    'mousedown', 'mousemove', 'mouseup',\n" +
      "    'dragstart', 'dragend', 'dragover', 'dragenter', 'dragleave', 'dragexit',\n" +
      "    'drop'\n" +
      "  ]) {\n" +
      "    target.addEventListener(event, e => {\n" +
      "      const pe = e;\n" +
      "      // Browsers are all over the place with dragend position.\n" +
      "      if (event === 'dragend')\n" +
      "        events.push('dragend');\n" +
      "      else\n" +
      "        events.push(`${event} at ${pe.clientX};${pe.clientY}`);\n" +
      "    }, false);\n" +
      "  }\n" +
      "  return events;\n" +
      "}");
    return eventsHandle;
  }

  // DD-019 should handle custom dataTransfer: not ported, see upstream_not_ported.txt.

  @PlaywrightTest
  @DisplayName("DD-020 what happens when dragging element is destroyed")
  void whatHappensWhenDraggingElementIsDestroyed(Page page) {
    page.setContent("\n" +
      "    <button draggable=\"true\">Draggable</button>\n" +
      "    <div id=target>drop here</div>\n" +
      "  ");

    page.evaluate("() => {\n" +
      "  document.querySelector('#target').addEventListener('dragover', event => {\n" +
      "    document.querySelector('button')?.remove();\n" +
      "  }, false);\n" +
      "\n" +
      "  document.querySelector('#target').addEventListener('drop', event => {\n" +
      "    document.querySelector('#target').textContent = 'dropped';\n" +
      "  }, false);\n" +
      "}");

    page.locator("button").dragTo(page.locator("div"));
    assertThat(page.locator("div")).hasText("drop here");
  }
}
