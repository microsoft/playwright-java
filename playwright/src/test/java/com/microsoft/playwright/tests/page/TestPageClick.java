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

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.KeyboardModifier;
import com.microsoft.playwright.options.MouseButton;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.microsoft.playwright.fixtures.Utils.attachFrame;
import static com.microsoft.playwright.fixtures.Utils.rafraf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/page-click.spec.ts. */
public class TestPageClick {

  // Java: expect.poll(() => value).toEqual(expected); waitForCondition() dispatches the events while waiting.
  private static <T> void expectPollToEqual(Page page, Supplier<T> value, T expected) {
    try {
      page.waitForCondition(() -> expected.equals(value.get()), new Page.WaitForConditionOptions().setTimeout(5000));
    } catch (TimeoutError e) {
      // The assertion below reports the last value.
    }
    assertEquals(expected, value.get());
  }

  @PlaywrightTest
  @DisplayName("CL-001 should click the button @smoke")
  void shouldClickTheButton(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/button.html");
    page.click("button");
    assertEquals("Clicked", page.evaluate("result"));
  }

  @PlaywrightTest
  @DisplayName("CL-002 should click button inside frameset")
  void shouldClickButtonInsideFrameset(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/frames/frameset.html");
    ElementHandle frameElement = page.querySelector("frame");
    frameElement.evaluate("(frame) => frame.src = '/input/button.html'");
    Frame frame = frameElement.contentFrame();
    frame.click("button");
    assertEquals("Clicked", frame.evaluate("result"));
  }

  // CL-003 should issue clicks in parallel in page and popup: not ported, see upstream_not_ported.txt.

  @PlaywrightTest
  @DisplayName("CL-004 should click svg")
  void shouldClickSvg(Page page) {
    page.setContent("\n" +
      "    <svg height=\"100\" width=\"100\">\n" +
      "      <circle onclick=\"window.__CLICKED=42\" cx=\"50\" cy=\"50\" r=\"40\" stroke=\"black\" stroke-width=\"3\" fill=\"red\" />\n" +
      "    </svg>\n" +
      "  ");
    page.click("circle");
    assertEquals(42, page.evaluate("__CLICKED"));
  }

  @PlaywrightTest
  @DisplayName("CL-005 should click the button if window.Node is removed")
  void shouldClickTheButtonIfWindowNodeIsRemoved(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/button.html");
    page.evaluate("() => delete window.Node");
    page.click("button");
    assertEquals("Clicked", page.evaluate("result"));
  }

  // @see https://github.com/GoogleChrome/puppeteer/issues/4281
  @PlaywrightTest
  @DisplayName("CL-006 should click on a span with an inline element inside")
  void shouldClickOnASpanWithAnInlineElementInside(Page page) {
    page.setContent("\n" +
      "    <style>\n" +
      "    span::before {\n" +
      "      content: 'q';\n" +
      "    }\n" +
      "    </style>\n" +
      "    <span onclick='window.CLICKED=42'></span>\n" +
      "  ");
    page.click("span");
    assertEquals(42, page.evaluate("CLICKED"));
  }

  @PlaywrightTest
  @DisplayName("CL-007 should click the aligned 1x1 div")
  void shouldClickTheAligned1x1Div(Page page) {
    page.setContent("<div style=\"width: 1px; height: 1px;\" onclick=\"window.__clicked = true\"></div>");
    page.click("div");
    assertEquals(true, page.evaluate("window.__clicked"));
  }

  @PlaywrightTest
  @DisplayName("CL-008 should click the half-aligned 1x1 div")
  void shouldClickTheHalfAligned1x1Div(Page page) {
    page.setContent("<div style=\"margin-left: 20.5px; margin-top: 11.5px; width: 1px; height: 1px;\" onclick=\"window.__clicked = true\"></div>");
    page.click("div");
    assertEquals(true, page.evaluate("window.__clicked"));
  }

  @PlaywrightTest
  @DisplayName("CL-009 should click the unaligned 1x1 div v1")
  void shouldClickTheUnaligned1x1DivV1(Page page) {
    page.setContent("<div style=\"margin-left: 20.23px; margin-top: 11.65px; width: 1px; height: 1px;\" onclick=\"window.__clicked = true\"></div>");
    page.click("div");
    assertEquals(true, page.evaluate("window.__clicked"));
  }

  @PlaywrightTest
  @DisplayName("CL-010 should click the unaligned 1x1 div v2")
  void shouldClickTheUnaligned1x1DivV2(Page page) {
    page.setContent("<div style=\"margin-left: 20.68px; margin-top: 11.13px; width: 1px; height: 1px;\" onclick=\"window.__clicked = true\"></div>");
    page.click("div");
    assertEquals(true, page.evaluate("window.__clicked"));
  }

  @PlaywrightTest
  @DisplayName("CL-011 should click the unaligned 1x1 div v3")
  void shouldClickTheUnaligned1x1DivV3(Page page) {
    page.setContent("<div style=\"margin-left: 20.68px; margin-top: 11.52px; width: 1px; height: 1px;\" onclick=\"window.__clicked = true\"></div>");
    page.click("div");
    assertEquals(true, page.evaluate("window.__clicked"));
  }

  @PlaywrightTest
  @DisplayName("CL-012 should click the unaligned 1x1 div v4")
  void shouldClickTheUnaligned1x1DivV4(Page page) {
    page.setContent("<div style=\"margin-left: 20.15px; margin-top: 11.24px; width: 1px; height: 1px;\" onclick=\"window.__clicked = true\"></div>");
    page.click("div");
    assertEquals(true, page.evaluate("window.__clicked"));
  }

  @PlaywrightTest
  @DisplayName("CL-013 should click the button after navigation ")
  void shouldClickTheButtonAfterNavigation(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/button.html");
    page.click("button");
    page.navigate(server.PREFIX + "/input/button.html");
    page.click("button");
    assertEquals("Clicked", page.evaluate("result"));
  }

  @PlaywrightTest
  @DisplayName("CL-014 should click the button after a cross origin navigation ")
  void shouldClickTheButtonAfterACrossOriginNavigation(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/button.html");
    page.click("button");
    page.navigate(server.CROSS_PROCESS_PREFIX + "/input/button.html");
    page.click("button");
    assertEquals("Clicked", page.evaluate("result"));
  }

  @PlaywrightTest
  @DisplayName("CL-015 should click when one of inline box children is outside of viewport")
  void shouldClickWhenOneOfInlineBoxChildrenIsOutsideOfViewport(Page page) {
    page.setContent("\n" +
      "    <style>\n" +
      "    i {\n" +
      "      position: absolute;\n" +
      "      top: -1000px;\n" +
      "    }\n" +
      "    </style>\n" +
      "    <span onclick='window.CLICKED = 42;'><i>woof</i><b>doggo</b></span>\n" +
      "  ");
    page.click("span");
    assertEquals(42, page.evaluate("CLICKED"));
  }

  @PlaywrightTest
  @DisplayName("CL-016 should select the text by triple clicking")
  void shouldSelectTheTextByTripleClicking(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/textarea.html");
    String text = "This is the text that we are going to try to select. Let's see how it goes.";
    page.fill("textarea", text);
    page.click("textarea", new Page.ClickOptions().setClickCount(3));
    assertEquals(text, page.evaluate("() => {\n" +
      "  const textarea = document.querySelector('textarea');\n" +
      "  return textarea.value.substring(textarea.selectionStart, textarea.selectionEnd);\n" +
      "}"));
  }

  @PlaywrightTest
  @DisplayName("CL-017 should click offscreen buttons")
  void shouldClickOffscreenButtons(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/offscreenbuttons.html");
    List<String> messages = new ArrayList<>();
    page.onConsoleMessage(msg -> messages.add(msg.text()));
    for (int i = 0; i < 11; ++i) {
      // We might've scrolled to click a button - reset to (0, 0).
      page.evaluate("() => window.scrollTo(0, 0)");
      page.click("#btn" + i);
    }
    assertEquals(Arrays.asList(
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
      "button #10 clicked"), messages);
  }

  @PlaywrightTest
  @DisplayName("CL-018 should waitFor visible when already visible")
  void shouldWaitForVisibleWhenAlreadyVisible(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/button.html");
    page.click("button");
    assertEquals("Clicked", page.evaluate("result"));
  }

  @PlaywrightTest
  @DisplayName("CL-019 should not wait with force")
  void shouldNotWaitWithForce(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/button.html");
    page.evalOnSelector("button", "b => b.style.display = 'none'");
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.click("button", new Page.ClickOptions().setForce(true)));
    assertTrue(error.getMessage().contains("Element is not visible"));
    assertEquals("Was not clicked", page.evaluate("result"));
  }

  // CL-020 should waitFor display:none to be gone: not ported, see upstream_not_ported.txt.

  // CL-021 should waitFor visibility:hidden to be gone: not ported, see upstream_not_ported.txt.

  // CL-022 should waitFor visible when parent is hidden: not ported, see upstream_not_ported.txt.

  @PlaywrightTest
  @DisplayName("CL-023 should click wrapped links")
  void shouldClickWrappedLinks(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/wrappedlink.html");
    page.click("a");
    assertEquals(true, page.evaluate("__clicked"));
  }

  @PlaywrightTest
  @DisplayName("CL-024 should click on checkbox input and toggle")
  @SuppressWarnings("unchecked")
  void shouldClickOnCheckboxInputAndToggle(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/checkbox.html");
    assertNull(page.evaluate("() => window['result'].check"));
    page.click("input#agree");
    assertEquals(true, page.evaluate("() => window['result'].check"));
    List<String> events = (List<String>) page.evaluate("() => window['result'].events");
    if (Env.headed()) {
      events = events.stream().filter(e -> !e.equals("mouseout") && !e.equals("mouseleave")).collect(Collectors.toList());
    }
    assertEquals(Arrays.asList(
      "mouseover",
      "mouseenter",
      "mousemove",
      "mousedown",
      "mouseup",
      "click",
      "input",
      "change"), events);
    page.click("input#agree");
    assertEquals(false, page.evaluate("() => window['result'].check"));
  }

  @PlaywrightTest
  @DisplayName("CL-025 should click on checkbox label and toggle")
  void shouldClickOnCheckboxLabelAndToggle(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/checkbox.html");
    assertNull(page.evaluate("() => window['result'].check"));
    page.click("label[for=\"agree\"]");
    assertEquals(true, page.evaluate("() => window['result'].check"));
    assertEquals(Arrays.asList(
      "click",
      "input",
      "change"), page.evaluate("() => window['result'].events"));
    page.click("label[for=\"agree\"]");
    assertEquals(false, page.evaluate("() => window['result'].check"));
  }

  @PlaywrightTest
  @DisplayName("CL-026 should scroll and click the button")
  void shouldScrollAndClickTheButton(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/scrollable.html");
    page.click("#button-5");
    assertEquals("clicked", page.evaluate("() => document.querySelector('#button-5').textContent"));
    page.click("#button-80");
    assertEquals("clicked", page.evaluate("() => document.querySelector('#button-80').textContent"));
  }

  @PlaywrightTest
  @DisplayName("CL-027 should scroll and click the button with smooth scroll behavior")
  void shouldScrollAndClickTheButtonWithSmoothScrollBehavior(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/scrollable.html");
    page.addStyleTag(new Page.AddStyleTagOptions().setContent("html { scroll-behavior: smooth; }"));
    for (int i = 0; i < 5; i++) {
      page.click("#button-80");
      assertEquals("clicked", page.evaluate("() => document.querySelector('#button-80').textContent"));
      page.click("#button-20");
      assertEquals("clicked", page.evaluate("() => document.querySelector('#button-20').textContent"));
    }
  }

  @PlaywrightTest
  @DisplayName("CL-028 should double click the button")
  void shouldDoubleClickTheButton(Page page, TestServer server) {
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

  @PlaywrightTest
  @DisplayName("CL-029 should click a partially obscured button")
  void shouldClickAPartiallyObscuredButton(Page page, TestServer server) {
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

  @PlaywrightTest
  @DisplayName("CL-030 should click a rotated button")
  void shouldClickARotatedButton(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/rotatedButton.html");
    page.click("button");
    assertEquals("Clicked", page.evaluate("result"));
  }

  @PlaywrightTest
  @DisplayName("CL-031 should fire contextmenu event on right click")
  void shouldFireContextmenuEventOnRightClick(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/scrollable.html");
    page.click("#button-8", new Page.ClickOptions().setButton(MouseButton.RIGHT));
    assertEquals("context menu", page.evaluate("() => document.querySelector('#button-8').textContent"));
  }

  @PlaywrightTest
  @DisplayName("CL-032 should click links which cause navigation")
  void shouldClickLinksWhichCauseNavigation(Page page, TestServer server) {
    // @see https://github.com/GoogleChrome/puppeteer/issues/206
    page.setContent("<a href=\"" + server.EMPTY_PAGE + "\">empty.html</a>");
    // This await should not hang.
    page.click("a");
  }

  @PlaywrightTest
  @DisplayName("CL-033 should not wait for navigation after clicking a link answered with 204")
  @Fixme(value = "driver: the bundled driver 1.64.1 waits for scheduled navigations after the click and fails with 'Timeout 5000ms exceeded.' (upstream issue 43279)", browsers = BrowserName.CHROMIUM)
  void shouldNotWaitForNavigationAfterClickingALinkAnsweredWith204(Page page, TestServer server) {
    server.setRoute("/204", (req, res) -> {
      res.statusCode = 204;
      res.end();
    });
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<a href=\"" + server.PREFIX + "/204\">link</a>");
    page.click("a", new Page.ClickOptions().setTimeout(5000));
    assertEquals(server.EMPTY_PAGE, page.url());
  }

  @PlaywrightTest
  @DisplayName("CL-034 should click the button inside an iframe")
  void shouldClickTheButtonInsideAnIframe(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<div style=\"width:100px;height:100px\">spacer</div>");
    attachFrame(page, "button-test", server.PREFIX + "/input/button.html");
    Frame frame = page.frames().get(1);
    ElementHandle button = frame.querySelector("button");
    button.click();
    assertEquals("Clicked", frame.evaluate("() => window['result']"));
  }

  @PlaywrightTest
  @DisplayName("CL-035 should click the button with fixed position inside an iframe")
  @DisabledOnBrowser(value = BrowserName.CHROMIUM, reason = "fixme: ")
  void shouldClickTheButtonWithFixedPositionInsideAnIframe(Page page, TestServer server) {
    // @see https://github.com/GoogleChrome/puppeteer/issues/4110
    // @see https://bugs.chromium.org/p/chromium/issues/detail?id=986390
    // @see https://chromium-review.googlesource.com/c/chromium/src/+/1742784
    page.navigate(server.EMPTY_PAGE);
    page.setViewportSize(500, 500);
    page.setContent("<div style=\"width:100px;height:2000px\">spacer</div>");
    attachFrame(page, "button-test", server.CROSS_PROCESS_PREFIX + "/input/button.html");
    Frame frame = page.frames().get(1);
    frame.evalOnSelector("button", "button => button.style.setProperty('position', 'fixed')");
    frame.click("button");
    assertEquals("Clicked", frame.evaluate("() => window['result']"));
  }

  @PlaywrightTest
  @DisplayName("CL-036 should click the button behind sticky header")
  void shouldClickTheButtonBehindStickyHeader(Page page) {
    page.setViewportSize(500, 240);
    page.setContent("\n" +
      "    <style>\n" +
      "    * {\n" +
      "      padding: 0;\n" +
      "      margin: 0;\n" +
      "    }\n" +
      "    li {\n" +
      "      height: 80px;\n" +
      "      border: 1px solid black;\n" +
      "    }\n" +
      "    ol {\n" +
      "      padding-top: 160px;\n" +
      "    }\n" +
      "    div.fixed {\n" +
      "      position: fixed;\n" +
      "      z-index: 1001;\n" +
      "      width: 100%;\n" +
      "      background: red;\n" +
      "      height: 160px;\n" +
      "    }\n" +
      "    </style>\n" +
      "\n" +
      "    <div class=fixed></div>\n" +
      "\n" +
      "    <ol>\n" +
      "    <li>hi1</li><li>hi2</li><li>hi3</li><li>hi4</li><li>hi5</li><li>hi6</li><li>hi7</li><li>hi8</li>\n" +
      "    <li id=target onclick=\"window.__clicked = true\">hi9</li>\n" +
      "    <li>hi10</li><li>hi11</li><li>hi12</li><li>hi13</li><li id=li14>hi14</li>\n" +
      "    </ol>\n" +
      "  ");
    page.evalOnSelector("#li14", "e => e.scrollIntoView()");
    page.click("#target");
    assertEquals(true, page.evaluate("() => window['__clicked']"));
  }

  @PlaywrightTest
  @DisplayName("CL-037 should click the button behind position:absolute header")
  void shouldClickTheButtonBehindPositionAbsoluteHeader(Page page) {
    page.setViewportSize(500, 240);
    page.setContent("\n" +
      "    <style>\n" +
      "    * {\n" +
      "      padding: 0;\n" +
      "      margin: 0;\n" +
      "    }\n" +
      "    li {\n" +
      "      height: 80px;\n" +
      "      border: 1px solid black;\n" +
      "    }\n" +
      "    ol {\n" +
      "      height: 100vh;\n" +
      "      overflow: scroll;\n" +
      "      padding-top: 160px;\n" +
      "    }\n" +
      "    body {\n" +
      "      position: relative;\n" +
      "    }\n" +
      "    div.fixed {\n" +
      "      position: absolute;\n" +
      "      top: 0;\n" +
      "      z-index: 1001;\n" +
      "      width: 100%;\n" +
      "      background: red;\n" +
      "      height: 160px;\n" +
      "    }\n" +
      "    </style>\n" +
      "\n" +
      "    <ol>\n" +
      "    <li>hi1</li><li>hi2</li><li>hi3</li><li>hi4</li><li>hi5</li><li>hi6</li><li>hi7</li><li>hi8</li>\n" +
      "    <li id=target onclick=\"window.__clicked = true\">hi9</li>\n" +
      "    <li>hi10</li><li>hi11</li><li>hi12</li><li>hi13</li><li id=li14>hi14</li>\n" +
      "    </ol>\n" +
      "\n" +
      "    <div class=fixed>Overlay</div>\n" +
      "  ");
    page.evalOnSelector("ol", "e => {\n" +
      "  const target = document.querySelector('#target');\n" +
      "  e.scrollTo({ top: target.offsetTop, behavior: 'instant' });\n" +
      "}");
    page.click("#target");
    assertEquals(true, page.evaluate("() => window['__clicked']"));
  }

  @PlaywrightTest
  @DisplayName("CL-038 should click the button with px border with offset")
  void shouldClickTheButtonWithPxBorderWithOffset(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/button.html");
    page.evalOnSelector("button", "button => button.style.borderWidth = '8px'");
    page.click("button", new Page.ClickOptions().setPosition(20, 10));
    assertEquals("Clicked", page.evaluate("result"));
    // Safari reports border-relative offsetX/offsetY.
    assertEquals(20, page.evaluate("offsetX"));
    assertEquals(10, page.evaluate("offsetY"));
  }

  @PlaywrightTest
  @DisplayName("CL-039 should click the button with em border with offset")
  void shouldClickTheButtonWithEmBorderWithOffset(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/button.html");
    page.evalOnSelector("button", "button => button.style.borderWidth = '2em'");
    page.evalOnSelector("button", "button => button.style.fontSize = '12px'");
    page.click("button", new Page.ClickOptions().setPosition(20, 10));
    assertEquals("Clicked", page.evaluate("result"));
    // Safari reports border-relative offsetX/offsetY.
    assertEquals(20, page.evaluate("offsetX"));
    assertEquals(10, page.evaluate("offsetY"));
  }

  @PlaywrightTest
  @DisplayName("CL-040 should click a very large button with offset")
  void shouldClickAVeryLargeButtonWithOffset(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/button.html");
    page.evalOnSelector("button", "button => button.style.borderWidth = '8px'");
    page.evalOnSelector("button", "button => button.style.height = button.style.width = '2000px'");
    page.click("button", new Page.ClickOptions().setPosition(1900, 1910));
    assertEquals("Clicked", page.evaluate("() => window['result']"));
    // Safari reports border-relative offsetX/offsetY.
    assertEquals(1900, page.evaluate("offsetX"));
    assertEquals(1910, page.evaluate("offsetY"));
  }

  @PlaywrightTest
  @DisplayName("CL-041 should click a button in scrolling container with offset")
  void shouldClickAButtonInScrollingContainerWithOffset(Page page, TestServer server) {
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
    assertEquals(1900, page.evaluate("offsetX"));
    assertEquals(1910, page.evaluate("offsetY"));
  }

  @PlaywrightTest
  @DisplayName("CL-042 should wait for stable position")
  void shouldWaitForStablePosition(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/button.html");
    page.evalOnSelector("button", "button => {\n" +
      "  button.style.transition = 'margin 500ms linear 0s';\n" +
      "  button.style.marginLeft = '200px';\n" +
      "  button.style.borderWidth = '0';\n" +
      "  button.style.width = '200px';\n" +
      "  button.style.height = '20px';\n" +
      "  // Set display to \"block\" - otherwise Firefox layouts with non-even\n" +
      "  // values on Linux.\n" +
      "  button.style.display = 'block';\n" +
      "  document.body.style.margin = '0';\n" +
      "}");
    // rafraf for Firefox to kick in the animation.
    rafraf(page);
    page.click("button");
    assertEquals("Clicked", page.evaluate("() => window['result']"));
    assertEquals(300, page.evaluate("pageX"));
    assertEquals(10, page.evaluate("pageY"));
  }

  // CL-043 should wait for becoming hit target: not ported, see upstream_not_ported.txt.

  // CL-044 should wait for becoming hit target with trial run: not ported, see upstream_not_ported.txt.

  @PlaywrightTest
  @DisplayName("CL-045 trial run should work with short timeout")
  void trialRunShouldWorkWithShortTimeout(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/button.html");
    page.evalOnSelector("button", "button => button.disabled = true");
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.click("button", new Page.ClickOptions().setTrial(true).setTimeout(2000)));
    assertTrue(error.getMessage().contains("click action (trial run)"));
    assertEquals("Was not clicked", page.evaluate("() => window['result']"));
  }

  @PlaywrightTest
  @DisplayName("CL-046 trial run should not click")
  void trialRunShouldNotClick(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/button.html");
    page.click("button", new Page.ClickOptions().setTrial(true));
    assertEquals("Was not clicked", page.evaluate("() => window['result']"));
  }

  @PlaywrightTest
  @DisplayName("CL-047 trial run should not double click")
  void trialRunShouldNotDoubleClick(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/button.html");
    page.evaluate("() => {\n" +
      "  window['double'] = false;\n" +
      "  const button = document.querySelector('button');\n" +
      "  button.addEventListener('dblclick', event => {\n" +
      "    window['double'] = true;\n" +
      "  });\n" +
      "}");
    page.dblclick("button", new Page.DblclickOptions().setTrial(true));
    assertEquals(false, page.evaluate("double"));
    assertEquals("Was not clicked", page.evaluate("result"));
  }

  @PlaywrightTest
  @DisplayName("CL-048 should fail when obscured and not waiting for hit target")
  void shouldFailWhenObscuredAndNotWaitingForHitTarget(Page page, TestServer server) {
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
    assertEquals("Was not clicked", page.evaluate("() => window['result']"));
  }

  // CL-049 should wait for button to be enabled: not ported, see upstream_not_ported.txt.

  // CL-050 should wait for element to become non-inert: not ported, see upstream_not_ported.txt.

  // CL-051 should wait for modal dialog to close: not ported, see upstream_not_ported.txt.

  // CL-052 should wait for input to be enabled: not ported, see upstream_not_ported.txt.

  // CL-053 should wait for select to be enabled: not ported, see upstream_not_ported.txt.

  @PlaywrightTest
  @DisplayName("CL-054 should click disabled div")
  void shouldClickDisabledDiv(Page page) {
    page.setContent("<div onclick=\"window.__CLICKED=true\" disabled>Click target</div>");
    page.click("text=Click target");
    assertEquals(true, page.evaluate("__CLICKED"));
  }

  // CL-055 should wait for BUTTON to be clickable when it has pointer-events:none: not ported, see upstream_not_ported.txt.

  // CL-056 should wait for LABEL to be clickable when it has pointer-events:none: not ported, see upstream_not_ported.txt.

  @PlaywrightTest
  @DisplayName("CL-057 should update modifiers correctly")
  void shouldUpdateModifiersCorrectly(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/button.html");
    page.click("button", new Page.ClickOptions().setModifiers(Arrays.asList(KeyboardModifier.SHIFT)));
    assertEquals(true, page.evaluate("shiftKey"));
    page.click("button", new Page.ClickOptions().setModifiers(Collections.<KeyboardModifier>emptyList()));
    assertEquals(false, page.evaluate("shiftKey"));

    page.keyboard().down("Shift");
    page.click("button", new Page.ClickOptions().setModifiers(Collections.<KeyboardModifier>emptyList()));
    assertEquals(false, page.evaluate("shiftKey"));
    page.click("button");
    assertEquals(true, page.evaluate("shiftKey"));
    page.keyboard().up("Shift");
    page.click("button");
    assertEquals(false, page.evaluate("shiftKey"));
  }

  @PlaywrightTest
  @DisplayName("CL-058 should click an offscreen element when scroll-behavior is smooth")
  void shouldClickAnOffscreenElementWhenScrollBehaviorIsSmooth(Page page) {
    page.setContent("\n" +
      "    <div style=\"border: 1px solid black; height: 500px; overflow: auto; width: 500px; scroll-behavior: smooth\">\n" +
      "    <button style=\"margin-top: 2000px\" onClick=\"window.clicked = true\">hi</button>\n" +
      "    </div>\n" +
      "  ");
    page.click("button");
    assertEquals(true, page.evaluate("window.clicked"));
  }

  @PlaywrightTest
  @DisplayName("CL-059 should report nice error when element is detached and force-clicked")
  void shouldReportNiceErrorWhenElementIsDetachedAndForceClicked(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/animating-button.html");
    page.evaluate("addButton()");
    ElementHandle handle = page.querySelector("button");
    page.evaluate("stopButton(true)");
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> handle.click(new ElementHandle.ClickOptions().setForce(true)));
    assertNull(page.evaluate("window.clicked"));
    assertTrue(error.getMessage().contains("Element is not attached to the DOM"));
  }

  // CL-060 should fail when element detaches after animation: not ported, see upstream_not_ported.txt.

  // CL-061 should retry when element detaches after animation: not ported, see upstream_not_ported.txt.

  // CL-062 should retry when element is animating from outside the viewport: not ported, see upstream_not_ported.txt.

  // CL-063 should fail when element is animating from outside the viewport with force: not ported, see upstream_not_ported.txt.

  @PlaywrightTest
  @DisplayName("CL-064 should dispatch microtasks in order")
  void shouldDispatchMicrotasksInOrder(Page page) {
    page.setContent("\n" +
      "    <button id=button>Click me</button>\n" +
      "    <script>\n" +
      "      let mutationCount = 0;\n" +
      "      const observer = new MutationObserver((mutationsList, observer) => {\n" +
      "        for(let mutation of mutationsList)\n" +
      "          ++mutationCount;\n" +
      "      });\n" +
      "      observer.observe(document.body, { attributes: true, childList: true, subtree: true });\n" +
      "      button.addEventListener('mousedown', () => {\n" +
      "        mutationCount = 0;\n" +
      "        document.body.appendChild(document.createElement('div'));\n" +
      "      });\n" +
      "      button.addEventListener('mouseup', () => {\n" +
      "        window['result'] = mutationCount;\n" +
      "      });\n" +
      "    </script>\n" +
      "  ");
    page.click("button");
    assertEquals(1, page.evaluate("() => window['result']"));
  }

  @PlaywrightTest
  @DisplayName("CL-065 should click the button when window.innerWidth is corrupted")
  void shouldClickTheButtonWhenWindowInnerWidthIsCorrupted(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/button.html");
    page.evaluate("() => Object.defineProperty(window, 'innerWidth', { value: 0 })");
    page.click("button");
    assertEquals("Clicked", page.evaluate("result"));
  }

  @PlaywrightTest
  @DisplayName("CL-066 should click zero-sized input by label")
  void shouldClickZeroSizedInputByLabel(Page page) {
    page.setContent("\n" +
      "    <label>\n" +
      "      Click me\n" +
      "      <input onclick=\"window.__clicked=true\" style=\"width:0;height:0;padding:0;margin:0;border:0;\">\n" +
      "    </label>\n" +
      "  ");
    page.click("text=Click me");
    assertEquals(true, page.evaluate("window.__clicked"));
  }

  @PlaywrightTest
  @DisplayName("CL-070 should climb dom for inner label with pointer-events:none")
  void shouldClimbDomForInnerLabelWithPointerEventsNone(Page page) {
    page.setContent("<button onclick=\"window.__CLICKED=true;\"><label style=\"pointer-events:none\">Click target</label></button>");
    page.click("text=Click target");
    assertEquals(true, page.evaluate("__CLICKED"));
  }

  @PlaywrightTest
  @DisplayName("CL-071 should climb up to [role=button]")
  void shouldClimbUpToRoleButton(Page page) {
    page.setContent("<div role=button onclick=\"window.__CLICKED=true;\"><div style=\"pointer-events:none\"><span><div>Click target</div></span></div>");
    page.click("text=Click target");
    assertEquals(true, page.evaluate("__CLICKED"));
  }

  @PlaywrightTest
  @DisplayName("CL-072 should climb up to a anchor")
  void shouldClimbUpToAAnchor(Page page) {
    // For Firefox its not allowed to return anything: https://bugzilla.mozilla.org/show_bug.cgi?id=1392046
    // Note the intermediate div - it is necessary, otherwise <a><non-clickable/></a> is not recognized as a clickable link.
    page.setContent("<a href=\"#\" onclick=\"window.__CLICKED=true\" id=\"outer\"><div id=\"intermediate\"><div id=\"inner\" style=\"pointer-events: none\">Inner</div></div></a>");
    page.click("#inner");
    assertEquals(true, page.evaluate("__CLICKED"));
  }

  @PlaywrightTest
  @DisplayName("CL-073 should climb up to a [role=link]")
  void shouldClimbUpToARoleLink(Page page) {
    page.setContent("<div role=link onclick=\"window.__CLICKED=true\" id=\"outer\"><div id=\"inner\" style=\"pointer-events: none\">Inner</div></div>");
    page.click("#inner");
    assertEquals(true, page.evaluate("__CLICKED"));
  }

  @PlaywrightTest
  @DisplayName("CL-074 should click in an iframe with border")
  void shouldClickInAnIframeWithBorder(Page page) {
    page.setContent("\n" +
      "    <style>\n" +
      "      body, html, iframe { margin: 0; padding: 0; border: none; box-sizing: border-box; }\n" +
      "      iframe { border: 4px solid black; background: gray; margin-left: 33px; margin-top: 24px; width: 400px; height: 400px; }\n" +
      "    </style>\n" +
      "    <iframe srcdoc=\"\n" +
      "      <style>\n" +
      "        body, html { margin: 0; padding: 0; }\n" +
      "        div { margin-left: 10px; margin-top: 20px; width: 2px; height: 2px; }\n" +
      "      </style>\n" +
      "      <div>Target</div>\n" +
      "      <script>\n" +
      "        document.querySelector('div').addEventListener('click', () => window.top._clicked = true);\n" +
      "      </script>\n" +
      "    \"></iframe>\n" +
      "  ");
    Locator locator = page.frameLocator("iframe").locator("div");
    locator.click();
    assertEquals(true, page.evaluate("window._clicked"));
  }

  @PlaywrightTest
  @DisplayName("CL-075 should click in an iframe with border 2")
  void shouldClickInAnIframeWithBorder2(Page page) {
    page.setContent("\n" +
      "    <style>\n" +
      "      body, html, iframe { margin: 0; padding: 0; border: none; }\n" +
      "      iframe { border: 4px solid black; background: gray; margin-left: 33px; margin-top: 24px; width: 400px; height: 400px; }\n" +
      "    </style>\n" +
      "    <iframe srcdoc=\"\n" +
      "      <style>\n" +
      "        body, html { margin: 0; padding: 0; }\n" +
      "        div { margin-left: 10px; margin-top: 20px; width: 2px; height: 2px; }\n" +
      "      </style>\n" +
      "      <div>Target</div>\n" +
      "      <script>\n" +
      "        document.querySelector('div').addEventListener('click', () => window.top._clicked = true);\n" +
      "      </script>\n" +
      "    \"></iframe>\n" +
      "  ");
    Locator locator = page.frameLocator("iframe").locator("div");
    locator.click();
    assertEquals(true, page.evaluate("window._clicked"));
  }

  @PlaywrightTest
  @DisplayName("CL-076 should not retain removed iframe after clicking inside it")
  void shouldNotRetainRemovedIframeAfterClickingInsideIt(Page page) {
    page.setContent("<iframe srcdoc=\"<button>Click</button>\"></iframe>");
    Locator button = page.frameLocator("iframe").getByRole(AriaRole.BUTTON);
    button.waitFor();
    page.evaluate("() => {\n" +
      "  window.iframeRef = new WeakRef(document.querySelector('iframe'));\n" +
      "}");
    button.click();
    page.evaluate("() => document.querySelector('iframe').remove()");
    // Move the mouse away to release Chromium's own last-hovered-node retention.
    page.mouse().move(500, 500);
    page.requestGC();
    Object retained = page.evaluate("() => Boolean(window.iframeRef.deref())");
    assertEquals(false, retained);
  }

  @PlaywrightTest
  @DisplayName("CL-077 should click in a transformed iframe")
  void shouldClickInATransformedIframe(Page page) {
    page.setContent("\n" +
      "    <style>\n" +
      "      body, html, iframe { margin: 0; padding: 0; border: none; }\n" +
      "      iframe {\n" +
      "        border: 4px solid black;\n" +
      "        background: gray;\n" +
      "        margin-left: 33px;\n" +
      "        margin-top: 24px;\n" +
      "        width: 400px;\n" +
      "        height: 400px;\n" +
      "        transform: translate(100px, 100px) scale(1.2) rotate3d(1, 1, 1, 25deg);\n" +
      "      }\n" +
      "    </style>\n" +
      "    <iframe srcdoc=\"\n" +
      "      <style>\n" +
      "        body, html { margin: 0; padding: 0; }\n" +
      "        div { margin-left: 10px; margin-top: 20px; width: 2px; height: 2px; }\n" +
      "      </style>\n" +
      "      <div>Target</div>\n" +
      "      <script>\n" +
      "        document.querySelector('div').addEventListener('click', () => window.top._clicked = true);\n" +
      "      </script>\n" +
      "    \"></iframe>\n" +
      "  ");
    Locator locator = page.frameLocator("iframe").locator("div");
    locator.click();
    assertEquals(true, page.evaluate("window._clicked"));
  }

  @PlaywrightTest
  @DisplayName("CL-078 should click a button that is overlaid by a permission popup")
  void shouldClickAButtonThatIsOverlaidByAPermissionPopup(Page page, TestServer server) {
    page.setViewportSize(500, 500);
    page.navigate(server.EMPTY_PAGE);
    page.setContent("\n" +
      "    <style>body, html { padding: 0; margin: 0; }</style>\n" +
      "    <script type='text/javascript'>\n" +
      "      window.addEventListener('DOMContentLoaded', () => {\n" +
      "        // Viewport filled with buttons.\n" +
      "        for (let i = 0; i < 100; ++i) {\n" +
      "          const button = document.createElement('button');\n" +
      "          button.textContent = i;\n" +
      "          button.style.setProperty('width', '50px');\n" +
      "          button.style.setProperty('height', '50px');\n" +
      "          document.body.append(button);\n" +
      "        }\n" +
      "      }, false);\n" +
      "    </script>\n" +
      "  ");
    // Issue a geolocation request. This should show a browser popup.
    // NOTE: this is a bit racy since we can't wait for the geolocation
    // popup to be shown.
    page.evaluate("() => {\n" +
      "  navigator.geolocation.getCurrentPosition(position => { });\n" +
      "}");
    // If popup blocks the click, then some of the `page.click` calls below will hang.
    for (int i = 0; i < 30; ++i) {
      page.click("text=" + i);
    }
  }

  @PlaywrightTest
  @DisplayName("CL-079 should click in a transformed iframe with force")
  void shouldClickInATransformedIframeWithForce(Page page) {
    page.setContent("\n" +
      "    <style>\n" +
      "      body, html, iframe { margin: 0; padding: 0; border: none; }\n" +
      "      iframe { background: gray; margin-left: 33px; margin-top: 24px; width: 400px; height: 400px; transform: translate(-40px, -40px) scale(0.8); }\n" +
      "    </style>\n" +
      "    <iframe srcdoc=\"\n" +
      "      <style>\n" +
      "        body, html { margin: 0; padding: 0; }\n" +
      "        div { margin-left: 10px; margin-top: 20px; width: 2px; height: 2px; }\n" +
      "      </style>\n" +
      "      <div>Target</div>\n" +
      "      <script>\n" +
      "        document.querySelector('div').addEventListener('click', () => window.top._clicked = true);\n" +
      "      </script>\n" +
      "    \"></iframe>\n" +
      "  ");
    Locator locator = page.frameLocator("iframe").locator("div");
    locator.click(new Locator.ClickOptions().setForce(true));
    assertEquals(true, page.evaluate("window._clicked"));
  }

  @PlaywrightTest
  @DisplayName("CL-080 should click in a nested transformed iframe")
  void shouldClickInANestedTransformedIframe(Page page) {
    page.setContent("\n" +
      "    <style>\n" +
      "      body, html, iframe { margin: 0; padding: 0; box-sizing: border-box; }\n" +
      "      iframe { border: 1px solid black; background: gray; margin-left: 33px; margin-top: 24px; width: 400px; height: 400px; transform: scale(0.8); }\n" +
      "    </style>\n" +
      "    <iframe srcdoc=\"\n" +
      "      <style>\n" +
      "        body, html, iframe { margin: 0; padding: 0; box-sizing: border-box; }\n" +
      "        iframe { border: 3px solid black; background: gray; margin-left: 18px; margin-top: 14px; width: 200px; height: 200px; transform: scale(0.7); }\n" +
      "      </style>\n" +
      "      <iframe srcdoc='\n" +
      "        <style>\n" +
      "          div { margin-left: 10px; margin-top: 20px; width: 2px; height: 2px; }\n" +
      "        </style>\n" +
      "        <div>Target</div>\n" +
      "      '></iframe>\n" +
      "    \"></iframe>\n" +
      "  ");
    Locator locator = page.frameLocator("iframe").frameLocator("iframe").locator("div");
    locator.evaluate("div => {\n" +
      "  div.addEventListener('click', () => window.top['_clicked'] = true);\n" +
      "}");
    locator.click();
    assertEquals(true, page.evaluate("window._clicked"));
  }

  @PlaywrightTest
  @DisplayName("CL-081 ensure events are dispatched in the individual tasks")
  void ensureEventsAreDispatchedInTheIndividualTasks(Page page) {
    page.setContent("\n" +
      "    <div id=\"outer\" style=\"background: #d4d4d4; width: 60px; height: 60px;\">\n" +
      "      <div id=\"inner\" style=\"background: #adadad; width: 46px; height: 46px;\"></div>\n" +
      "    </div>\n" +
      "  ");

    page.evaluate("() => {\n" +
      "  function onClick(name) {\n" +
      "    console.log(`click ${name}`);\n" +
      "\n" +
      "    window.builtins.setTimeout(function() {\n" +
      "      console.log(`timeout ${name}`);\n" +
      "    }, 0);\n" +
      "\n" +
      "    void Promise.resolve().then(function() {\n" +
      "      console.log(`promise ${name}`);\n" +
      "    });\n" +
      "  }\n" +
      "\n" +
      "  document.getElementById('inner').addEventListener('click', () => onClick('inner'));\n" +
      "  document.getElementById('outer').addEventListener('click', () => onClick('outer'));\n" +
      "}");

    // Capture console messages
    List<String> messages = new ArrayList<>();
    page.onConsoleMessage(msg -> messages.add(msg.text()));

    // Click on the inner div element
    page.locator("#inner").click();

    expectPollToEqual(page, () -> messages, Arrays.asList(
      "click inner",
      "promise inner",
      "click outer",
      "promise outer",
      "timeout inner",
      "timeout outer"));
  }

  @PlaywrightTest
  @DisplayName("CL-082 should click if opened select covers the button")
  void shouldClickIfOpenedSelectCoversTheButton(Page page) {
    page.setContent("\n" +
      "    <div>\n" +
      "      <select>\n" +
      "        <option>very long text #1</option>\n" +
      "        <option>very long text #2</option>\n" +
      "        <option>very long text #3</option>\n" +
      "        <option>very long text #4</option>\n" +
      "        <option>very long text #5</option>\n" +
      "        <option>very long text #6</option>\n" +
      "      </select>\n" +
      "    </div>\n" +
      "    <div>\n" +
      "      <button onclick=\"window.__CLICKED=42\">clickme</button>\n" +
      "    </div>\n" +
      "  ");
    page.click("select");
    page.click("button");
    assertEquals(42, page.evaluate("window.__CLICKED"));
  }

  @PlaywrightTest
  @DisplayName("CL-083 should fire contextmenu event on right click in correct order")
  void shouldFireContextmenuEventOnRightClickInCorrectOrder(Page page, TestServer server, BrowserName browserName) {
    page.navigate(server.EMPTY_PAGE);
    page.setContent("\n" +
      "    <button id=\"target\">Click me</button>\n" +
      "  ");
    page.evaluate("() => {\n" +
      "  const logEvent = e => console.log(e.type);\n" +
      "  document.addEventListener('mousedown', logEvent);\n" +
      "  document.addEventListener('mouseup', logEvent);\n" +
      "  document.addEventListener('contextmenu', logEvent);\n" +
      "}");
    List<String> entries = new ArrayList<>();
    page.onConsoleMessage(message -> entries.add(message.text()));
    page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Click me")).click(new Locator.ClickOptions().setButton(MouseButton.RIGHT));
    if (browserName == BrowserName.CHROMIUM && Env.isWindows) {
      expectPollToEqual(page, () -> entries, Arrays.asList("mousedown", "mouseup", "contextmenu"));
    } else {
      expectPollToEqual(page, () -> entries, Arrays.asList("mousedown", "contextmenu", "mouseup"));
    }
  }

  @PlaywrightTest
  @DisplayName("CL-084 should click after a right click")
  void shouldClickAfterARightClick(Page page) {
    page.setContent("\n" +
      "    <button>Click me</button>\n" +
      "    <script>\n" +
      "      const button = document.querySelector('button');\n" +
      "      button.addEventListener('click', () => button.textContent = 'Clicked!');\n" +
      "    </script>\n" +
      "  ");
    page.getByRole(AriaRole.BUTTON).click(new Locator.ClickOptions().setButton(MouseButton.RIGHT));
    page.getByRole(AriaRole.BUTTON).click();
    assertThat(page.getByRole(AriaRole.BUTTON)).hasText("Clicked!");
  }

  @PlaywrightTest
  @DisplayName("CL-085 should set PointerEvent.pressure on pointerdown")
  void shouldSetPointerEventPressureOnPointerdown(Page page) {
    page.setContent("\n" +
      "    <button id=\"target\">Click me</button>\n" +
      "    <script>\n" +
      "      window['pressures'] = [];\n" +
      "      document.addEventListener('pointerdown', e => window['pressures'].push(['pointerdown', e.pressure]));\n" +
      "      document.addEventListener('pointerup', e => window['pressures'].push(['pointerup', e.pressure]));\n" +
      "    </script>\n" +
      "  ");
    page.click("button");
    // Java: expect.arrayContaining([...]) is a check that each expected element is in the array.
    List<?> pressures = (List<?>) page.evaluate("() => window['pressures']");
    assertTrue(pressures.contains(Arrays.asList("pointerdown", 0.5)));
    assertTrue(pressures.contains(Arrays.asList("pointerup", 0)));
  }

  @PlaywrightTest
  @DisplayName("CL-086 should set PointerEvent.pressure on pointermove")
  void shouldSetPointerEventPressureOnPointermove(Page page) {
    page.setContent("\n" +
      "    <body style=\"margin: 0; padding: 0;\">\n" +
      "      <div id=\"target\" style=\"width: 500px; height: 500px; background-color: red;\"></div>\n" +
      "      <script>\n" +
      "        window['pressures'] = [];\n" +
      "        document.addEventListener('pointermove', e => window['pressures'].push([e.pressure, e.clientX, e.clientY]));\n" +
      "      </script>\n" +
      "    </body>\n" +
      "  ");
    page.click("div#target");
    page.mouse().move(10, 10);
    page.mouse().down();
    page.mouse().move(250, 250);
    page.mouse().up();
    page.mouse().move(50, 50);
    // Java: expect.arrayContaining([...]) is a check that each expected element is in the array.
    List<?> pressures = (List<?>) page.evaluate("() => window['pressures']");
    assertTrue(pressures.contains(Arrays.asList(0, 250, 250)));
    assertTrue(pressures.contains(Arrays.asList(0, 10, 10)));
    assertTrue(pressures.contains(Arrays.asList(0.5, 250, 250)));
    assertTrue(pressures.contains(Arrays.asList(0, 50, 50)));
  }

  @PlaywrightTest
  @DisplayName("CL-087 should click into shadow root with slotted div")
  void shouldClickIntoShadowRootWithSlottedDiv(Page page) {
    page.setContent("\n" +
      "    <my-button>\n" +
      "      <template shadowrootmode=\"open\">\n" +
      "        <button><slot></slot></button>\n" +
      "      </template>\n" +
      "      <div>Foo</div>\n" +
      "    </my-button>\n" +
      "  ");

    page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Foo")).click();
  }

  @PlaywrightTest
  @DisplayName("CL-088 should click shadow root button")
  void shouldClickShadowRootButton(Page page) {
    page.setContent("\n" +
      "    <my-button>\n" +
      "      <template shadowrootmode=\"open\">\n" +
      "        <button><slot></slot></button>\n" +
      "      </template>\n" +
      "      <div>Foo</div>\n" +
      "    </my-button>\n" +
      "  ");

    page.locator("my-button").click();
  }

  @PlaywrightTest
  @DisplayName("CL-089 should click with tweened mouse movement")
  @DisabledOnBrowser(value = {BrowserName.CHROMIUM, BrowserName.FIREFOX, BrowserName.WEBKIT}, headed = true, reason = "System cursor tends to interfere with this test")
  void shouldClickWithTweenedMouseMovement(Page page, BrowserName browserName) {
    page.setContent("\n" +
      "    <body style=\"margin: 0; padding: 0; height: 500px; width: 500px;\">\n" +
      "      <div style=\"position: relative; top: 280px; left: 150px; width: 100px; height: 40px\">Click me</div>\n" +
      "    </body>\n" +
      "  ");

    // The test becomes flaky on WebKit without next line.
    if (browserName == BrowserName.WEBKIT) {
      page.evaluate("() => new Promise(requestAnimationFrame)");
    }
    page.mouse().move(100, 100);
    page.evaluate("() => {\n" +
      "  window['result'] = [];\n" +
      "  document.addEventListener('mousemove', event => {\n" +
      "    window['result'].push([event.clientX, event.clientY]);\n" +
      "  });\n" +
      "}");
    // Centerpoint at 150 + 100/2, 280 + 40/2 = 200, 300
    page.locator("div").click(new Locator.ClickOptions().setSteps(5));
    assertEquals(Arrays.asList(
      Arrays.asList(120, 140),
      Arrays.asList(140, 180),
      Arrays.asList(160, 220),
      Arrays.asList(180, 260),
      Arrays.asList(200, 300)), page.evaluate("result"));
  }
}
