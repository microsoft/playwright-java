/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
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
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;


import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/selectors-frame.spec.ts. */
public class TestSelectorsFrame {
  // Java: route.fulfill(...).catch(() => {}) is a try/catch, fulfilling can fail when the page is gone.
  private static void fulfill(Route route, String body) {
    try {
      route.fulfill(new Route.FulfillOptions().setBody(body).setContentType("text/html"));
    } catch (PlaywrightException e) {
    }
  }

  private static void routeIframe(Page page) {
    page.route("**/empty.html", route -> {
      fulfill(route, "<iframe src=\"iframe.html\"></iframe>");
    });
    page.route("**/iframe.html", route -> {
      fulfill(route, "\n" +
        "        <html>\n" +
        "          <div>\n" +
        "            <button>Hello iframe</button>\n" +
        "            <iframe src=\"iframe-2.html\"></iframe>\n" +
        "          </div>\n" +
        "          <span>1</span>\n" +
        "          <span>2</span>\n" +
        "        </html>");
    });
    page.route("**/iframe-2.html", route -> {
      fulfill(route, "<html><button tag=\"iframe2\">Hello nested iframe</button></html>");
    });
  }

  @PlaywrightTest
  @DisplayName("SE-098 should work for iframe @smoke")
  void shouldWorkForIframe(Page page, TestServer server) {
    routeIframe(page);
    page.navigate(server.EMPTY_PAGE);
    Locator button = page.locator("iframe >> internal:control=enter-frame >> button");
    button.waitFor();
    assertEquals("Hello iframe", button.innerText());
    assertThat(button).hasText("Hello iframe");
    button.click();
  }

  @PlaywrightTest
  @DisplayName("SE-099 should work for iframe (handle)")
  void shouldWorkForIframeHandle(Page page, TestServer server) {
    routeIframe(page);
    page.navigate(server.EMPTY_PAGE);
    ElementHandle body = page.querySelector("body");
    ElementHandle button = body.waitForSelector("iframe >> internal:control=enter-frame >> button");
    assertEquals("Hello iframe", button.innerText());
    assertEquals("Hello iframe", button.textContent());
    button.click();
  }

  @PlaywrightTest
  @DisplayName("SE-100 should work for nested iframe")
  void shouldWorkForNestedIframe(Page page, TestServer server) {
    routeIframe(page);
    page.navigate(server.EMPTY_PAGE);
    Locator button = page.locator("iframe >> internal:control=enter-frame >> iframe >> internal:control=enter-frame >> button");
    button.waitFor();
    assertEquals("Hello nested iframe", button.innerText());
    assertThat(button).hasText("Hello nested iframe");
    button.click();
  }

  @PlaywrightTest
  @DisplayName("SE-101 should work for nested iframe (handle)")
  void shouldWorkForNestedIframeHandle(Page page, TestServer server) {
    routeIframe(page);
    page.navigate(server.EMPTY_PAGE);
    ElementHandle body = page.querySelector("body");
    ElementHandle button = body.waitForSelector("iframe >> internal:control=enter-frame >> iframe >> internal:control=enter-frame >> button");
    assertEquals("Hello nested iframe", button.innerText());
    assertEquals("Hello nested iframe", button.textContent());
    button.click();
  }

  @PlaywrightTest
  @DisplayName("SE-102 should work for $ and $$")
  void shouldWorkForAnd(Page page, TestServer server) {
    routeIframe(page);
    page.navigate(server.EMPTY_PAGE);
    ElementHandle element = page.querySelector("iframe >> internal:control=enter-frame >> button");
    assertEquals("Hello iframe", element.textContent());
    java.util.List<ElementHandle> elements = page.querySelectorAll("iframe >> internal:control=enter-frame >> span");
    assertEquals(2, elements.size());
  }

  @PlaywrightTest
  @DisplayName("SE-103 $ should not wait for frame")
  void shouldNotWaitForFrame(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    assertNull(page.querySelector("iframe >> internal:control=enter-frame >> canvas"));
    ElementHandle body = page.querySelector("body");
    assertNull(body.querySelector("iframe >> internal:control=enter-frame >> canvas"));
  }

  @PlaywrightTest
  @DisplayName("SE-104 $$ should not wait for frame")
  void shouldNotWaitForFrame2(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    assertEquals(0, page.querySelectorAll("iframe >> internal:control=enter-frame >> canvas").size());
    ElementHandle body = page.querySelector("body");
    assertEquals(0, body.querySelectorAll("iframe >> internal:control=enter-frame >> canvas").size());
  }

  @PlaywrightTest
  @DisplayName("SE-105 $eval should throw for missing frame")
  @Fixme("java: the message has no 'page.$eval: ' prefix, it is 'Failed to find element matching selector \"iframe >> internal:control=enter-frame >> canvas\"'")
  void evalShouldThrowForMissingFrame(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    {
      PlaywrightException error = assertThrows(PlaywrightException.class,
        () -> page.evalOnSelector("iframe >> internal:control=enter-frame >> canvas", "e => 1"));
      assertTrue(error.getMessage().contains("page.$eval: Failed to find element matching selector"));
    }
    {
      ElementHandle body = page.querySelector("body");
      PlaywrightException error = assertThrows(PlaywrightException.class,
        () -> body.evalOnSelector("iframe >> internal:control=enter-frame >> canvas", "e => 1"));
      assertTrue(error.getMessage().contains("elementHandle.$eval: Failed to find element matching selector"));
    }
  }

  @PlaywrightTest
  @DisplayName("SE-106 $$eval should not throw for missing frame")
  void evalShouldNotThrowForMissingFrame(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    assertEquals(0, page.evalOnSelectorAll("iframe >> internal:control=enter-frame >> canvas", "e => e.length"));
    ElementHandle body = page.querySelector("body");
    assertEquals(0, body.evalOnSelectorAll("iframe >> internal:control=enter-frame >> canvas", "e => e.length"));
  }

  @PlaywrightTest
  @DisplayName("SE-107 should work for $ and $$ (handle)")
  void shouldWorkForAndHandle(Page page, TestServer server) {
    routeIframe(page);
    page.navigate(server.EMPTY_PAGE);
    ElementHandle body = page.querySelector("body");
    ElementHandle element = body.querySelector("iframe >> internal:control=enter-frame >> button");
    assertEquals("Hello iframe", element.textContent());
    java.util.List<ElementHandle> elements = body.querySelectorAll("iframe >> internal:control=enter-frame >> span");
    assertEquals(2, elements.size());
  }

  @PlaywrightTest
  @DisplayName("SE-108 should work for $eval")
  void shouldWorkForEval(Page page, TestServer server) {
    routeIframe(page);
    page.navigate(server.EMPTY_PAGE);
    Object value = page.evalOnSelector("iframe >> internal:control=enter-frame >> button", "b => b.nodeName");
    assertEquals("BUTTON", value);
  }

  @PlaywrightTest
  @DisplayName("SE-109 should work for $eval (handle)")
  void shouldWorkForEvalHandle(Page page, TestServer server) {
    routeIframe(page);
    page.navigate(server.EMPTY_PAGE);
    ElementHandle body = page.querySelector("body");
    Object value = body.evalOnSelector("iframe >> internal:control=enter-frame >> button", "b => b.nodeName");
    assertEquals("BUTTON", value);
  }

  @PlaywrightTest
  @DisplayName("SE-110 should work for $$eval")
  void shouldWorkForEval2(Page page, TestServer server) {
    routeIframe(page);
    page.navigate(server.EMPTY_PAGE);
    Object value = page.evalOnSelectorAll("iframe >> internal:control=enter-frame >> span", "ss => ss.map(s => s.textContent)");
    assertEquals(asList("1", "2"), value);
  }

  @PlaywrightTest
  @DisplayName("SE-111 should work for $$eval (handle)")
  void shouldWorkForEvalHandle2(Page page, TestServer server) {
    routeIframe(page);
    page.navigate(server.EMPTY_PAGE);
    ElementHandle body = page.querySelector("body");
    Object value = body.evalOnSelectorAll("iframe >> internal:control=enter-frame >> span", "ss => ss.map(s => s.textContent)");
    assertEquals(asList("1", "2"), value);
  }

  @PlaywrightTest
  @DisplayName("SE-112 should not allow dangling enter-frame")
  void shouldNotAllowDanglingEnterFrame(Page page, TestServer server) {
    routeIframe(page);
    page.navigate(server.EMPTY_PAGE);
    Locator button = page.locator("iframe >> internal:control=enter-frame");
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> button.click());
    assertTrue(error.getMessage().contains("Selector cannot end with"));
    assertTrue(error.getMessage().contains("iframe >> internal:control=enter-frame"));
  }

  @PlaywrightTest
  @DisplayName("SE-113 should not allow leading enter-frame")
  void shouldNotAllowLeadingEnterFrame(Page page, TestServer server) {
    routeIframe(page);
    page.navigate(server.EMPTY_PAGE);
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.waitForSelector("internal:control=enter-frame >> button"));
    assertTrue(error.getMessage().contains("Selector cannot start with"));
  }

  @PlaywrightTest
  @DisplayName("SE-114 should not allow capturing before enter-frame")
  void shouldNotAllowCapturingBeforeEnterFrame(Page page, TestServer server) {
    routeIframe(page);
    page.navigate(server.EMPTY_PAGE);
    Locator button = page.locator("*css=iframe >> internal:control=enter-frame >> div");
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> button.click());
    assertTrue(error.getMessage().contains("Can not capture the selector before diving into the frame"));
  }

  @PlaywrightTest
  @DisplayName("SE-115 should capture after the enter-frame")
  void shouldCaptureAfterTheEnterFrame(Page page, TestServer server) {
    routeIframe(page);
    page.navigate(server.EMPTY_PAGE);
    Locator div = page.locator("iframe >> internal:control=enter-frame >> *css=div >> button");
    assertTrue(div.innerHTML().contains("<button>"));
  }

  // SE-116 should click in lazy iframe: not ported, see upstream_not_ported.txt.

  @PlaywrightTest
  @DisplayName("SE-117 waitFor should survive frame reattach")
  void waitForShouldSurviveFrameReattach(Page page, TestServer server) throws Exception {
    routeIframe(page);
    page.navigate(server.EMPTY_PAGE);
    Locator button = page.locator("iframe >> internal:control=enter-frame >> button:has-text(\"Hello nested iframe\")");
    page.locator("iframe").evaluate("e => e.remove()");
    page.evaluate("() => {\n" +
      "    const iframe = document.createElement('iframe');\n" +
      "    iframe.src = 'iframe-2.html';\n" +
      "    document.body.appendChild(iframe);\n" +
      "  }");
    // Java: the wait stays satisfied once the button is there, so it runs after the reattach.
    button.waitFor();
  }

  @PlaywrightTest
  @DisplayName("SE-118 waitForSelector should survive frame reattach (handle)")
  void waitForSelectorShouldSurviveFrameReattachHandle(Page page, TestServer server) throws Exception {
    routeIframe(page);
    page.navigate(server.EMPTY_PAGE);
    ElementHandle body = page.querySelector("body");
    page.locator("iframe").evaluate("e => e.remove()");
    page.evaluate("() => {\n" +
      "    const iframe = document.createElement('iframe');\n" +
      "    iframe.src = 'iframe-2.html';\n" +
      "    document.body.appendChild(iframe);\n" +
      "  }");
    // Java: the wait stays satisfied once the button is there, so it runs after the reattach.
    body.waitForSelector("iframe >> internal:control=enter-frame >> button:has-text(\"Hello nested iframe\")");
  }

  @PlaywrightTest
  @DisplayName("SE-119 waitForSelector should survive iframe navigation (handle)")
  void waitForSelectorShouldSurviveIframeNavigationHandle(Page page, TestServer server) throws Exception {
    routeIframe(page);
    page.navigate(server.EMPTY_PAGE);
    ElementHandle body = page.querySelector("body");
    page.locator("iframe").evaluate("e => e.src = 'iframe-2.html'");
    // Java: the wait stays satisfied once the button is there, so it runs after the navigation.
    body.waitForSelector("iframe >> internal:control=enter-frame >> button:has-text(\"Hello nested iframe\")");
  }

  // SE-120 click should survive frame reattach: not ported, see upstream_not_ported.txt.

  // SE-121 click should survive iframe navigation: not ported, see upstream_not_ported.txt.

  // SE-122 click should survive navigation: not ported, see upstream_not_ported.txt.

  @PlaywrightTest
  @DisplayName("SE-123 should non work for non-frame")
  void shouldNonWorkForNonFrame(Page page, TestServer server) {
    routeIframe(page);
    page.setContent("<div></div>");
    Locator button = page.locator("div >> internal:control=enter-frame >> button");
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> button.waitFor());
    assertTrue(error.getMessage().contains("<div></div>"));
    assertTrue(error.getMessage().contains("<iframe> was expected"));
  }

  @PlaywrightTest
  @DisplayName("SE-124 should match in a descendant frame")
  void shouldMatchInADescendantFrame(Page page, TestServer server) {
    routeIframe(page);
    page.navigate(server.EMPTY_PAGE);
    // The main frame has no <div>; only the iframe.html child frame does.
    Locator div = page.locator("internal:control=any-frame >> div");
    div.waitFor();
    assertThat(div).hasCount(1);
    assertTrue(div.innerHTML().contains("<button>Hello iframe</button>"));
  }

  @PlaywrightTest
  @DisplayName("SE-125 should match in a deeply nested frame")
  void shouldMatchInADeeplyNestedFrame(Page page, TestServer server) {
    routeIframe(page);
    page.navigate(server.EMPTY_PAGE);
    // The main frame has no <div>; only the iframe.html child frame does.
    Locator button = page.locator("internal:control=any-frame >> button[tag=\"iframe2\"]");
    button.waitFor();
    assertThat(button).hasCount(1);
    assertEquals("Hello nested iframe", button.textContent());
  }

  @PlaywrightTest
  @DisplayName("SE-126 should not match a chain across frames")
  void shouldNotMatchAChainAcrossFrames(Page page, TestServer server) {
    routeIframe(page);
    page.navigate(server.EMPTY_PAGE);
    // The <div> lives in the iframe.html frame, while the button lives in the frame nested below it.
    assertThat(page.locator("internal:control=any-frame >> div >> button[tag=\"iframe2\"]")).hasCount(0);
    assertThat(page.locator("internal:control=any-frame >> div >> button")).hasText("Hello iframe");
  }

  @PlaywrightTest
  @DisplayName("SE-127 should match multiple elements")
  void shouldMatchMultipleElements(Page page, TestServer server) {
    page.route("**/empty.html", route -> {
      fulfill(route, "<iframe src=\"a.html\"></iframe><iframe src=\"b.html\"></iframe>");
    });
    page.route("**/a.html", route -> {
      fulfill(route, "<div>one</div>");
    });
    page.route("**/b.html", route -> {
      fulfill(route, "<span>two</span><span>three</span>");
    });
    page.navigate(server.EMPTY_PAGE);

    Object texts = page.evalOnSelectorAll("internal:control=any-frame >> span", "els => els.map(e => e.textContent)");
    assertEquals(asList("two", "three"), texts);
  }

  @PlaywrightTest
  @DisplayName("SE-128 should throw when matching elements in multiple frames")
  void shouldThrowWhenMatchingElementsInMultipleFrames(Page page, TestServer server) {
    page.route("**/empty.html", route -> {
      fulfill(route, "<iframe src=\"a.html\"></iframe><iframe src=\"b.html\"></iframe>");
    });
    page.route("**/a.html", route -> {
      fulfill(route, "<div>one</div>");
    });
    page.route("**/b.html", route -> {
      fulfill(route, "<div>two</div>");
    });
    page.navigate(server.EMPTY_PAGE);

    // Make sure both child frames have their <div> before matching, otherwise resolution
    // may transiently collapse to a single frame.
    // Java: there is no expect.poll, this polls page.frames().size() for up to 5 seconds before asserting it is 3.
    for (int i = 0; i < 50 && page.frames().size() != 3; i++) {
      page.waitForTimeout(100);
    }
    assertEquals(3, page.frames().size());
    for (Frame frame : page.frames()) {
      if (frame != page.mainFrame())
        frame.waitForSelector("div");
    }

    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.locator("internal:control=any-frame >> div").innerHTML());
    assertTrue(error.getMessage().contains("frameLocator() matched elements in multiple frames"));
  }

  @PlaywrightTest
  @DisplayName("SE-129 should not allow any-frame in the middle of a selector")
  void shouldNotAllowAnyFrameInTheMiddleOfASelector(Page page, TestServer server) {
    routeIframe(page);
    page.navigate(server.EMPTY_PAGE);
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.locator("iframe >> internal:control=any-frame >> div").waitFor());
    assertTrue(error.getMessage().contains("\"any-frame\" is only allowed as the first selector token"));
  }

  @PlaywrightTest
  @DisplayName("SE-130 should allow entering frames from any frame")
  void shouldAllowEnteringFramesFromAnyFrame(Page page, TestServer server) {
    routeIframe(page);
    page.navigate(server.EMPTY_PAGE);
    Locator button = page.locator("internal:control=any-frame >> iframe[src=\"iframe-2.html\"] >> internal:control=enter-frame >> button");
    button.waitFor();
    assertEquals("Hello nested iframe", button.innerText());
  }

  @PlaywrightTest
  @DisplayName("SE-131 should not allow any-frame after entering a frame")
  void shouldNotAllowAnyFrameAfterEnteringAFrame(Page page) {
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.locator("iframe >> internal:control=enter-frame >> internal:control=any-frame >> button").count());
    assertTrue(error.getMessage().contains("\"any-frame\" is only allowed as the first selector token"));
  }

  @PlaywrightTest
  @DisplayName("SE-132 should not allow dangling enter-frame after any-frame")
  void shouldNotAllowDanglingEnterFrameAfterAnyFrame(Page page) {
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.locator("internal:control=any-frame >> iframe >> internal:control=enter-frame").count());
    assertTrue(error.getMessage().contains("Selector cannot end with entering frame"));
  }
}
