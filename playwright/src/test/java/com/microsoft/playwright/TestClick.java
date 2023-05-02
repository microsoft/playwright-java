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
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.condition.EnabledIf;

import java.util.ArrayList;
import java.util.List;

import static com.microsoft.playwright.options.KeyboardModifier.SHIFT;
import static com.microsoft.playwright.options.MouseButton.RIGHT;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;

public class TestClick extends TestBase {

  @Test
  void shouldClickTheButton() {
    page.navigate(server.PREFIX + "/input/button.html");
    page.click("button");
    assertEquals("Clicked", page.evaluate("result"));
  }

  @Test
  void shouldClickSvg() {
    page.setContent("<svg height='100' width='100'>\n" +
      "<circle onclick='javascript:window.__CLICKED=42' cx='50' cy='50' r='40' stroke='black' stroke-width='3' fill='red'/>\n" +
      "</svg>\n");
    page.click("circle");
    assertEquals(42, page.evaluate("__CLICKED"));
  }

  @Test
  void shouldClickTheButtonIfWindowNodeIsRemoved() {
    page.navigate(server.PREFIX + "/input/button.html");
    page.evaluate("() => delete window.Node");
    page.click("button");
    assertEquals("Clicked", page.evaluate("result"));
  }

  // @see https://github.com/GoogleChrome/puppeteer/issues/4281
  @Test
  void shouldClickOnASpanWithAnInlineElementInside() {
    page.setContent(
      "<style>\n" +
        "  span::before {\n" +
        "    content: 'q';\n" +
        "  }\n" +
        "</style>\n" +
        "<span onclick='javascript:window.CLICKED=42'></span>\n");
    page.click("span");
    assertEquals(42, page.evaluate("CLICKED"));
  }

  void shouldNotThrowUnhandledPromiseRejectionWhenPageCloses() {
    // not supported in sync api
  }

  @Test
  void shouldClickThe1x1Div() {
    page.setContent("<div style='width: 1px; height: 1px;' onclick='window.__clicked = true'></div>");
    page.click("div");
    assertTrue((Boolean) page.evaluate("window.__clicked"));
  }

  @Test
  void shouldClickTheButtonAfterNavigation() {
    page.navigate(server.PREFIX + "/input/button.html");
    page.click("button");
    page.navigate(server.PREFIX + "/input/button.html");
    page.click("button");
    assertEquals("Clicked", page.evaluate("result"));
  }

  @Test
  void shouldClickTheButtonAfterACrossOriginNavigation() {
    page.navigate(server.PREFIX + "/input/button.html");
    page.click("button");
    page.navigate(server.CROSS_PROCESS_PREFIX + "/input/button.html");
    page.click("button");
    assertEquals("Clicked", page.evaluate("result"));
  }

  @Test
  void shouldClickWithDisabledJavascript() {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setJavaScriptEnabled(false));
    Page page = context.newPage();
    page.navigate(server.PREFIX + "/wrappedlink.html");

    page.waitForNavigation(() -> page.click("a"));

    assertEquals(server.PREFIX + "/wrappedlink.html#clicked", page.url());
    context.close();
  }

  @Test
  void shouldClickWhenOneOfInlineBoxChildrenIsOutsideOfViewport() {
    page.setContent(
      "<style>\n" +
        "i {\n" +
        "  position: absolute;\n" +
        "  top: -1000px;\n" +
        "}\n" +
        "</style>\n" +
        "<span onclick='javascript:window.CLICKED = 42;'><i>woof</i><b>doggo</b></span>\n");
    page.click("span");
    assertEquals(42, page.evaluate("CLICKED"));
  }

  @Test
  void shouldSelectTheTextByTripleClicking() {
    page.navigate(server.PREFIX + "/input/textarea.html");
    String text = "This is the text that we are going to try to select. Let's see how it goes.";
    page.fill("textarea", text);
    page.click("textarea", new Page.ClickOptions().setClickCount(3));
    assertEquals(text, page.evaluate("() => {\n" +
      "  const textarea = document.querySelector('textarea');\n" +
      "  return textarea.value.substring(textarea.selectionStart, textarea.selectionEnd);\n" +
      "}"));
  }

  @Test
  void shouldClickOffscreenButtons() {
    page.navigate(server.PREFIX + "/offscreenbuttons.html");
    List<String> messages = new ArrayList<>();
    page.onConsoleMessage(message -> messages.add(message.text()));
    for (int i = 0; i < 11; ++i) {
      // We might've scrolled to click a button - reset to (0, 0).
      page.evaluate("() => window.scrollTo(0, 0)");
      page.click("#btn" + i);
    }
    assertEquals(asList(
      "button #0 clicked",
      "button #1 clicked",
      "button #2 clicked",
      "button #3 clicked",
      "button #4 clicked",
      "button #5 clicked",
      "button #6 clicked",
      "button #7 clicked",
      "button #8 clicked",
      "button #9 clicked",
      "button #10 clicked"
    ), messages);
  }

  @Test
  void shouldWaitForVisibleWhenAlreadyVisible() {
    page.navigate(server.PREFIX + "/input/button.html");
    page.click("button");
    assertEquals("Clicked", page.evaluate("result"));
  }

  @Test
  void shouldNotWaitWithForce() {
    page.navigate(server.PREFIX + "/input/button.html");
    page.evalOnSelector("button", "b => b.style.display = 'none'");
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.click("button", new Page.ClickOptions().setForce(true));
    });
    assertTrue(e.getMessage().contains("Element is not visible"));
    assertEquals("Was not clicked", page.evaluate("result"));
  }

  void shouldWaitForDisplayNoneToBeGone() {
    // not supported in sync api
  }

  void shouldWaitForVisibilityHiddenToBeGone() {
    // not supported in sync api
  }

  void shouldWaitForVisibleWhenParentIsHidden() {
    // not supported in sync api
  }

  @Test
  void shouldClickWrappedLinks() {
    page.navigate(server.PREFIX + "/wrappedlink.html");
    page.click("a");
    assertTrue((Boolean) page.evaluate("__clicked"));
  }

  @Test
  void shouldClickOnCheckboxInputAndToggle() {
    page.navigate(server.PREFIX + "/input/checkbox.html");
    assertNull(page.evaluate("() => window['result'].check"));
    page.click("input#agree");
    assertTrue((Boolean) page.evaluate("() => window['result'].check"));
    assertEquals(asList(
      "mouseover",
      "mouseenter",
      "mousemove",
      "mousedown",
      "mouseup",
      "click",
      "input",
      "change"),
      page.evaluate("() => window['result'].events"));
    page.click("input#agree");
    assertFalse((Boolean) page.evaluate("() => window['result'].check"));
  }

  @Test
  void shouldClickOnCheckboxLabelAndToggle() {
    page.navigate(server.PREFIX + "/input/checkbox.html");
    assertNull(page.evaluate("() => window['result'].check"));
    page.click("label[for='agree']");
    assertTrue((Boolean) page.evaluate("() => window['result'].check"));
    assertEquals(asList(
      "click",
      "input",
      "change"),
      page.evaluate("() => window['result'].events"));
    page.click("label[for='agree']");
    assertFalse((Boolean) page.evaluate("() => window['result'].check"));
  }

  @Test
  void shouldNotHangWithTouchEnabledViewports() {
    // @see https://github.com/GoogleChrome/puppeteer/issues/161
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setViewportSize(375, 667)
      .setHasTouch(true));
    Page page = context.newPage();
    page.mouse().down();
    page.mouse().move(100, 10);
    page.mouse().up();
    context.close();
  }

  @Test
  void shouldScrollAndClickTheButton() {
    page.navigate(server.PREFIX + "/input/scrollable.html");
    page.click("#button-5");
    assertEquals("clicked", page.evaluate("() => document.querySelector('#button-5').textContent"));
    page.click("#button-80");
    assertEquals("clicked", page.evaluate("() => document.querySelector('#button-80').textContent"));
  }

  @Test
  void shouldDoubleClickTheButton() {
    page.navigate(server.PREFIX + "/input/button.html");
    page.evaluate("() => {\n" +
      "  window['double'] = false;\n" +
      "  const button = document.querySelector('button');\n" +
      "  button.addEventListener('dblclick', event => {\n" +
      "    window['double'] = true;\n" +
      "  });\n" +
      "}");
    page.dblclick("button");
    assertEquals(true, page.evaluate("double"));
    assertEquals("Clicked", page.evaluate("result"));
  }

  @Test
  void shouldClickAPartiallyObscuredButton() {
    page.navigate(server.PREFIX + "/input/button.html");
    page.evaluate("() => {\n" +
      "  const button = document.querySelector('button');\n" +
      "  button.textContent = 'Some really long text that will go offscreen';\n" +
      "  button.style.position = 'absolute';\n" +
      "  button.style.left = '368px';\n" +
      "}");
    page.click("button");
    assertEquals("Clicked", page.evaluate("() => window['result']"));
  }

  @Test
  void shouldClickARotatedButton() {
    page.navigate(server.PREFIX + "/input/rotatedButton.html");
    page.click("button");
    assertEquals("Clicked", page.evaluate("result"));
  }

  @Test
  void shouldFireContextmenuEventOnRightClick() {
    page.navigate(server.PREFIX + "/input/scrollable.html");
    page.click("#button-8", new Page.ClickOptions().setButton(RIGHT));
    assertEquals("context menu", page.evaluate("() => document.querySelector('#button-8').textContent"));
  }

  @Test
  void shouldClickLinksWhichCauseNavigation() {
    // @see https://github.com/GoogleChrome/puppeteer/issues/206
    page.setContent("<a href=" + server.EMPTY_PAGE + ">empty.html</a>");
    // This should not hang.
    page.click("a");
  }

  @Test
  void shouldClickTheButtonInsideAnIframe() {
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<div style='width:100px;height:100px'>spacer</div>");
    Utils.attachFrame(page, "button-test", server.PREFIX + "/input/button.html");
    Frame frame = page.frames().get(1);
    ElementHandle button = frame.querySelector("button");
    button.click();
    assertEquals("Clicked", frame.evaluate("() => window['result']"));
  }

  @Test
  @EnabledIf(value="com.microsoft.playwright.TestBase#isFirefox", disabledReason="fixme")
  void shouldClickTheButtonWithFixedPositionInsideAnIframe() {
    // @see https://github.com/GoogleChrome/puppeteer/issues/4110
    // @see https://bugs.chromium.org/p/chromium/issues/detail?id=986390
    // @see https://chromium-review.googlesource.com/c/chromium/src/+/1742784
    page.navigate(server.EMPTY_PAGE);
    page.setViewportSize(500, 500);
    page.setContent("<div style='width:100px;height:2000px'>spacer</div>");
    Utils.attachFrame(page, "button-test", server.CROSS_PROCESS_PREFIX + "/input/button.html");
    Frame frame = page.frames().get(1);
    frame.evalOnSelector("button", "button => button.style.setProperty('position', 'fixed')");
    frame.click("button");
    assertEquals("Clicked", frame.evaluate("() => window['result']"));
  }


  @Test
  void shouldClickTheButtonWithDeviceScaleFactorSet() {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setViewportSize(400, 400)
      .setDeviceScaleFactor(5.0));
    Page page = context.newPage();
    assertEquals(5, page.evaluate("() => window.devicePixelRatio"));
    page.setContent("<div style='width:100px;height:100px'>spacer</div>");
    Utils.attachFrame(page, "button-test", server.PREFIX + "/input/button.html");
    Frame frame = page.frames().get(1);
    ElementHandle button = frame.querySelector("button");
    button.click();
    assertEquals("Clicked", frame.evaluate("window['result']"));
    context.close();
  }

  @Test
  void shouldClickTheButtonWithPxBorderWithOffset() {
    page.navigate(server.PREFIX + "/input/button.html");
    page.evalOnSelector("button", "button => button.style.borderWidth = '8px'");
    page.click("button", new Page.ClickOptions().setPosition(20, 10));
    assertEquals(page.evaluate("result"), "Clicked");
    // Safari reports border-relative offsetX/offsetY.
    assertEquals(isWebKit() ? 20 + 8 : 20, page.evaluate("offsetX"));
    assertEquals(isWebKit() ? 10 + 8 : 10, page.evaluate("offsetY"));
  }

  @Test
  void shouldClickTheButtonWithEmBorderWithOffset() {
    page.navigate(server.PREFIX + "/input/button.html");
    page.evalOnSelector("button", "button => button.style.borderWidth = '2em'");
    page.evalOnSelector("button", "button => button.style.fontSize = '12px'");
    page.click("button", new Page.ClickOptions().setPosition(20, 10));
    assertEquals("Clicked", page.evaluate("result"));
    // Safari reports border-relative offsetX/offsetY.
    assertEquals(isWebKit() ? 12 * 2 + 20 : 20, page.evaluate("offsetX"));
    assertEquals(isWebKit() ? 12 * 2 + 10 : 10, page.evaluate("offsetY"));
  }

  @Test
  void shouldClickAVeryLargeButtonWithOffset() {
    page.navigate(server.PREFIX + "/input/button.html");
    page.evalOnSelector("button", "button => button.style.borderWidth = '8px'");
    page.evalOnSelector("button", "button => button.style.height = button.style.width = '2000px'");
    page.click("button", new Page.ClickOptions().setPosition(1900, 1910));
    assertEquals("Clicked", page.evaluate("() => window['result']"));
    // Safari reports border-relative offsetX/offsetY.
    assertEquals(isWebKit() ? 1900 + 8 : 1900, page.evaluate("offsetX"));
    assertEquals(isWebKit() ? 1910 + 8 : 1910, page.evaluate("offsetY"));
  }

  @Test
  void shouldClickAButtonInScrollingContainerWithOffset() {
    page.navigate(server.PREFIX + "/input/button.html");
    page.evalOnSelector("button", "button => {\n" +
      "  const container = document.createElement('div');\n" +
      "  container.style.overflow = 'auto';\n" +
      "  container.style.width = '200px';\n" +
      "  container.style.height = '200px';\n" +
      "  button.parentElement.insertBefore(container, button);\n" +
      "  container.appendChild(button);\n" +
      "  button.style.height = '2000px';\n" +
      "  button.style.width = '2000px';\n" +
      "  button.style.borderWidth = '8px';\n" +
      "}");
    page.click("button", new Page.ClickOptions().setPosition(1900, 1910));
    assertEquals("Clicked", page.evaluate("() => window['result']"));
    // Safari reports border-relative offsetX/offsetY.
    assertEquals(isWebKit() ? 1900 + 8 : 1900, page.evaluate("offsetX"));
    assertEquals(isWebKit() ? 1910 + 8 : 1910, page.evaluate("offsetY"));
  }

  private static void expectCloseTo(double expected, double actual) {
    if (Math.abs(expected - actual) > 2)
      fail("Expected: " + expected + ", received: " + actual);
  }


  @Test
  @DisabledIf(value="com.microsoft.playwright.TestBase#isFirefox", disabledReason="skip")
  void shouldClickTheButtonWithOffsetWithPageScale() {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setViewportSize(400, 400)
      .setIsMobile(true));
    Page page = context.newPage();
    page.navigate(server.PREFIX + "/input/button.html");
    page.evalOnSelector("button", "button => {\n" +
      "  button.style.borderWidth = '8px';\n" +
      "  document.body.style.margin = '0';\n" +
      "}");
    page.click("button", new Page.ClickOptions().setPosition(20, 10));
    assertEquals("Clicked", page.evaluate("result"));
    // Expect 20;10 + 8px of border in each direction. Allow some delta as different
    // browsers round up or down differently during css -> dip -> css conversion.
    expectCloseTo(28, (Integer) page.evaluate("pageX"));
    expectCloseTo(18, (Integer) page.evaluate("pageY"));
    context.close();
  }

  @Test
  void shouldWaitForStablePosition() {
    page.navigate(server.PREFIX + "/input/button.html");
    page.evalOnSelector("button", "button => {\n" +
      "  button.style.transition = 'margin 500ms linear 0s';\n" +
      "  button.style.marginLeft = '200px';\n" +
      "  button.style.borderWidth = '0';\n" +
      "  button.style.width = '200px';\n" +
      "  button.style.height = '20px';\n" +
      "  // Set display to 'block' - otherwise Firefox layouts with non-even\n" +
      "  // values on Linux.\n" +
      "  button.style.display = 'block';\n" +
      "  document.body.style.margin = '0';\n" +
      "}");
    page.click("button");
    assertEquals("Clicked", page.evaluate("result"));
    assertEquals(300, page.evaluate("pageX"));
    assertEquals(10, page.evaluate("pageY"));
  }

  void shouldWaitForBecomingHitTarget() {
    // not supported in sync api
  }

  @Test
  void shouldFailWhenObscuredAndNotWaitingForHitTarget() {
    page.navigate(server.PREFIX + "/input/button.html");
    ElementHandle button = page.querySelector("button");
    page.evaluate("() => {\n" +
      "  document.body.style.position = 'relative';\n" +
      "  const blocker = document.createElement('div');\n" +
      "  blocker.style.position = 'absolute';\n" +
      "  blocker.style.width = '400px';\n" +
      "  blocker.style.height = '20px';\n" +
      "  blocker.style.left = '0';\n" +
      "  blocker.style.top = '0';\n" +
      "  document.body.appendChild(blocker);\n" +
      "}");
    button.click(new ElementHandle.ClickOptions().setForce(true));
    assertEquals("Was not clicked", page.evaluate("window['result']"));
  }

  void shouldWaitForButtonToBeEnabled() {
    // not supported in sync api
  }

  void shouldWaitForInputToBeEnabled() {
    // not supported in sync api
  }

  void shouldWaitForSelectToBeEnabled() {
    // not supported in sync api
  }

  @Test
  void shouldClickDisabledDiv() {
    page.setContent("<div onclick='javascript:window.__CLICKED=true;' disabled>Click target</div>");
    page.click("text=Click target");
    assertEquals(true, page.evaluate("__CLICKED"));
  }

  @Test
  void shouldClimbDomForInnerLabelWithPointerEventsNone() {
    page.setContent("<button onclick='javascript:window.__CLICKED=true;'><label style='pointer-events:none'>Click target</label></button>");
    page.click("text=Click target");
    assertEquals(true, page.evaluate("__CLICKED"));
  }

  @Test
  void shouldWorkWithUnicodeSelectors() {
    page.setContent("<button onclick='javascript:window.__CLICKED=true;'><label style='pointer-events:none'>Найти</label></button>");
    page.click("text=Найти");
    assertEquals(true, page.evaluate("__CLICKED"));
  }

  @Test
  void shouldClimbUpTo_roleButton_() {
    page.setContent("<div role=button onclick='javascript:window.__CLICKED=true;'><div style='pointer-events:none'><span><div>Click target</div></span></div>");
    page.click("text=Click target");
    assertEquals(true, page.evaluate("__CLICKED"));
  }

  void shouldWaitForBUTTONToBeClickableWhenItHasPointerEventsNone() {
    // not supported in sync api
  }

  void shouldWaitForLABELToBeClickableWhenItHasPointerEventsNone() {
    // not supported in sync api
  }

  @Test
  void shouldUpdateModifiersCorrectly() {
    page.navigate(server.PREFIX + "/input/button.html");
    page.click("button", new Page.ClickOptions().setModifiers(asList(SHIFT)));
    assertEquals(true, page.evaluate("shiftKey"));
    page.click("button", new Page.ClickOptions().setModifiers(emptyList()));
    assertEquals(false, page.evaluate("shiftKey"));

    page.keyboard().down("Shift");
    page.click("button", new Page.ClickOptions().setModifiers(emptyList()));
    assertEquals(false, page.evaluate("shiftKey"));
    page.click("button");
    assertEquals(true, page.evaluate("shiftKey"));
    page.keyboard().up("Shift");
    page.click("button");
    assertEquals(false, page.evaluate("shiftKey"));
  }

  @Test
  void shouldClickAnOffscreenElementWhenScrollBehaviorIsSmooth() {
    page.setContent(
      "<div style='border: 1px solid black; height: 500px; overflow: auto; width: 500px; scroll-behavior: smooth'>\n" +
        "    <button style='margin-top: 2000px' onClick='window.clicked = true'>hi</button>\n" +
        "    </div>");
    page.click("button");
    assertEquals(true, page.evaluate("window.clicked"));
  }

  @Test
  void shouldReportNiceErrorWhenElementIsDetachedAndForceClicked() {
    page.navigate(server.PREFIX + "/input/animating-button.html");
    page.evaluate("addButton()");
    ElementHandle handle = page.querySelector("button");
    page.evaluate("stopButton(true)");
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      handle.click(new ElementHandle.ClickOptions().setForce(true));
    });
    assertTrue(e.getMessage().contains("Element is not attached to the DOM"));
    assertEquals(null, page.evaluate("window.clicked"));
  }

  void shouldFailWhenElementDetachesAfterAnimation() {
    // not supported in sync api
  }
  void shouldRetryWhenElementDetachesAfterAnimation() {
    // not supported in sync api
  }
  void shouldRetryWhenElementIsAnimatingFromOutsideTheViewport() {
    // not supported in sync api
  }
  void shouldFailWhenElementIsAnimatingFromOutsideTheViewportWithForce() {
    // not supported in sync api
  }
  @Test
  void shouldDispatchMicrotasksInOrder() {
    page.setContent(
      "<button id=button>Click me</button>\n" +
      "<script>\n" +
      "  let mutationCount = 0;\n" +
      "  const observer = new MutationObserver((mutationsList, observer) => {\n" +
      "    for(let mutation of mutationsList)\n" +
      "    ++mutationCount;\n" +
      "  });\n" +
      "  observer.observe(document.body, { attributes: true, childList: true, subtree: true });\n" +
      "  button.addEventListener('mousedown', () => {\n" +
      "    mutationCount = 0;\n" +
      "    document.body.appendChild(document.createElement('div'));\n" +
      "  });\n" +
      "  button.addEventListener('mouseup', () => {\n" +
      "    window['result'] = mutationCount;\n" +
      "  });\n" +
      "</script>");
    page.click("button");
    assertEquals(1, page.evaluate("() => window['result']"));
  }

  @Test
  void shouldClickTheButtonWhenWindowInnerWidthIsCorrupted() {
    page.navigate(server.PREFIX + "/input/button.html");
    page.evaluate("() => Object.defineProperty(window, 'innerWidth', {value: 0})");
    page.click("button");
    assertEquals("Clicked", page.evaluate("result"));
  }
}
