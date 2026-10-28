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

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.AriaSnapshotMode;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.options.WaitUntilState;
import org.junit.jupiter.api.DisplayName;
import org.opentest4j.AssertionFailedError;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/locator-any-frame.spec.ts. */
public class TestLocatorAnyFrame {
  private static void routePage(Page page, String url, String body) {
    page.route("**/" + url, route -> {
      try {
        route.fulfill(new Route.FulfillOptions().setBody(body).setContentType("text/html"));
      } catch (PlaywrightException e) {
        // catch(() => {})
      }
    });
  }

  // Java: expect.poll(fn).toBe(expected) with the defaults of expect.poll: 5000ms timeout, intervals 100, 250, 500, 1000ms
  private static void expectPollToBe(Page page, Supplier<Object> fn, Object expected) {
    int[] intervals = {100, 250, 500, 1000};
    long deadline = System.currentTimeMillis() + 5000;
    Object last = fn.get();
    for (int i = 0; !Objects.equals(expected, last) && System.currentTimeMillis() < deadline; i++) {
      page.waitForTimeout(intervals[Math.min(i, intervals.length - 1)]);
      last = fn.get();
    }
    assertEquals(expected, last);
  }

  private static void waitForAllFrames(Page page, int frameCount, String selector) {
    // Wait for all child frames to load their content, so that the search
    // deterministically sees elements in all of them.
    expectPollToBe(page, () -> page.frames().size(), frameCount);
    for (Frame frame : page.frames()) {
      if (frame != page.mainFrame()) {
        frame.waitForSelector(selector, new Frame.WaitForSelectorOptions().setState(WaitForSelectorState.ATTACHED));
      }
    }
  }

  private static Frame findFrame(Page page, String urlPart) {
    for (Frame frame : page.frames()) {
      if (frame.url().contains(urlPart)) {
        return frame;
      }
    }
    return null;
  }

  @PlaywrightTest
  @DisplayName("LO-001 should click a button inside an iframe")
  void shouldClickAButtonInsideAnIframe(Page page, TestServer server) {
    routePage(page, "empty.html", "<iframe src=\"a.html\"></iframe>");
    routePage(page, "a.html", "<button onclick=\"window.__clicked = true\">Click me</button>");
    page.navigate(server.EMPTY_PAGE);
    page.frameLocator().getByRole(AriaRole.BUTTON, new FrameLocator.GetByRoleOptions().setName("Click me")).click();
    assertEquals(true, page.frames().get(1).evaluate("() => window.__clicked"));
  }

  @PlaywrightTest
  @DisplayName("LO-002 should click a button in the main frame")
  void shouldClickAButtonInTheMainFrame(Page page, TestServer server) {
    routePage(page, "empty.html", "<iframe src=\"a.html\"></iframe><button onclick=\"window.__clicked = true\">Click me</button>");
    routePage(page, "a.html", "<div>No buttons here</div>");
    page.navigate(server.EMPTY_PAGE);
    page.frameLocator().locator("button").click();
    assertEquals(true, page.evaluate("() => window.__clicked"));
  }

  @PlaywrightTest
  @DisplayName("LO-003 should fail click when elements match in multiple frames")
  @Fixme("java: the call log says waiting for frameLocator().locator(\"button\") with double quotes, not locator('button')")
  void shouldFailClickWhenElementsMatchInMultipleFrames(Page page, TestServer server) {
    routePage(page, "empty.html", "<iframe src=\"a.html\"></iframe><iframe src=\"b.html\"></iframe>");
    routePage(page, "a.html", "<button>one</button>");
    routePage(page, "b.html", "<button>two</button>");
    page.navigate(server.EMPTY_PAGE);
    waitForAllFrames(page, 3, "button");
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.frameLocator().locator("button").click(new Locator.ClickOptions().setTimeout(3000)));
    assertTrue(error.getMessage().contains("frameLocator() matched elements in multiple frames"));
    assertTrue(error.getMessage().contains("waiting for frameLocator().locator('button')"));
  }

  @PlaywrightTest
  @DisplayName("LO-004 should fail click upon strict mode violation inside a single frame")
  @Fixme("java: the call log says waiting for frameLocator().locator(\"button\") with double quotes, not locator('button')")
  void shouldFailClickUponStrictModeViolationInsideASingleFrame(Page page, TestServer server) {
    routePage(page, "empty.html", "<iframe src=\"a.html\"></iframe>");
    routePage(page, "a.html", "<button>one</button><button>two</button>");
    page.navigate(server.EMPTY_PAGE);
    waitForAllFrames(page, 2, "button");
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.frameLocator().locator("button").click(new Locator.ClickOptions().setTimeout(3000)));
    assertTrue(error.getMessage().contains("strict mode violation"));
    assertTrue(error.getMessage().contains("waiting for frameLocator().locator('button')"));
  }

  @PlaywrightTest
  @DisplayName("LO-005 should time out on click when there are no matches")
  @Fixme("java: the call log says waiting for frameLocator().locator(\"button\") with double quotes, not locator('button')")
  void shouldTimeOutOnClickWhenThereAreNoMatches(Page page, TestServer server) {
    routePage(page, "empty.html", "<iframe src=\"a.html\"></iframe>");
    routePage(page, "a.html", "<div>Nothing here</div>");
    page.navigate(server.EMPTY_PAGE);
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.frameLocator().locator("button").click(new Locator.ClickOptions().setTimeout(1000)));
    assertTrue(error.getMessage().contains("Timeout 1000ms exceeded"));
    assertTrue(error.getMessage().contains("waiting for frameLocator().locator('button')"));
  }

  @PlaywrightTest
  @DisplayName("LO-006 should count elements in a single frame")
  void shouldCountElementsInASingleFrame(Page page, TestServer server) {
    routePage(page, "empty.html", "<iframe src=\"a.html\"></iframe>");
    routePage(page, "a.html", "<div>1</div><div>2</div><div>3</div>");
    page.navigate(server.EMPTY_PAGE);
    waitForAllFrames(page, 2, "div");
    assertEquals(3, page.frameLocator().locator("div").count());
    assertEquals(0, page.frameLocator().locator("button").count());
  }

  @PlaywrightTest
  @DisplayName("LO-007 should fail count when elements match in multiple frames")
  void shouldFailCountWhenElementsMatchInMultipleFrames(Page page, TestServer server) {
    routePage(page, "empty.html", "<div>main</div><iframe src=\"a.html\"></iframe>");
    routePage(page, "a.html", "<div>child</div>");
    page.navigate(server.EMPTY_PAGE);
    waitForAllFrames(page, 2, "div");
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.frameLocator().locator("div").count());
    assertTrue(error.getMessage().contains("frameLocator() matched elements in multiple frames"));
  }

  @PlaywrightTest
  @DisplayName("LO-008 should support toHaveCount")
  void shouldSupportToHaveCount(Page page, TestServer server) {
    routePage(page, "empty.html", "<iframe src=\"a.html\"></iframe>");
    routePage(page, "a.html", "<span>one</span><span>two</span>");
    page.navigate(server.EMPTY_PAGE);
    assertThat(page.frameLocator().locator("span")).hasCount(2);
    assertThat(page.frameLocator().locator("button")).hasCount(0);
  }

  @PlaywrightTest
  @DisplayName("LO-009 should wait for a frame to appear with toHaveCount")
  void shouldWaitForAFrameToAppearWithToHaveCount(Page page, TestServer server) {
    routePage(page, "empty.html", "<div>No frames yet</div>");
    routePage(page, "a.html", "<span>one</span><span>two</span>");
    page.navigate(server.EMPTY_PAGE);
    page.evaluate("() => {\n" +
      "    window.builtins.setTimeout(() => {\n" +
      "      const iframe = document.createElement('iframe');\n" +
      "      iframe.src = 'a.html';\n" +
      "      document.body.appendChild(iframe);\n" +
      "    }, 500);\n" +
      "  }");
    assertThat(page.frameLocator().locator("span")).hasCount(2);
  }

  @PlaywrightTest
  @DisplayName("LO-010 should fail toHaveCount when elements match in multiple frames")
  @Fixme("java: the assertion message has no 'Locator: frameLocator().locator(...)' line, only the call log: Assert \"hasCount\" frameLocator().locator(\"span\")")
  void shouldFailToHaveCountWhenElementsMatchInMultipleFrames(Page page, TestServer server) {
    routePage(page, "empty.html", "<iframe src=\"a.html\"></iframe><iframe src=\"b.html\"></iframe>");
    routePage(page, "a.html", "<span>one</span>");
    routePage(page, "b.html", "<span>two</span>");
    page.navigate(server.EMPTY_PAGE);
    waitForAllFrames(page, 3, "span");
    AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertThat(page.frameLocator().locator("span")).hasCount(2, new LocatorAssertions.HasCountOptions().setTimeout(3000)));
    assertTrue(error.getMessage().contains("frameLocator() matched elements in multiple frames"));
    assertTrue(error.getMessage().contains("Locator: frameLocator().locator('span')"));
  }

  @PlaywrightTest
  @DisplayName("LO-011 should support toHaveText")
  void shouldSupportToHaveText(Page page, TestServer server) {
    routePage(page, "empty.html", "<iframe src=\"a.html\"></iframe>");
    routePage(page, "a.html", "<div>Hello iframe</div>");
    page.navigate(server.EMPTY_PAGE);
    assertThat(page.frameLocator().locator("div")).hasText("Hello iframe");
  }

  @PlaywrightTest
  @DisplayName("LO-012 should support toHaveText with an array")
  void shouldSupportToHaveTextWithAnArray(Page page, TestServer server) {
    routePage(page, "empty.html", "<iframe src=\"a.html\"></iframe>");
    routePage(page, "a.html", "<span>one</span><span>two</span>");
    page.navigate(server.EMPTY_PAGE);
    assertThat(page.frameLocator().locator("span")).hasText(new String[]{"one", "two"});
  }

  @PlaywrightTest
  @DisplayName("LO-013 should fail toHaveText when elements match in multiple frames")
  @Fixme("java: the assertion message has no 'Locator: frameLocator().locator(...)' line, only the call log: Assert \"hasCount\" frameLocator().locator(\"span\")")
  void shouldFailToHaveTextWhenElementsMatchInMultipleFrames(Page page, TestServer server) {
    routePage(page, "empty.html", "<iframe src=\"a.html\"></iframe><iframe src=\"b.html\"></iframe>");
    routePage(page, "a.html", "<div>one</div>");
    routePage(page, "b.html", "<div>two</div>");
    page.navigate(server.EMPTY_PAGE);
    waitForAllFrames(page, 3, "div");
    AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertThat(page.frameLocator().locator("div")).hasText("one", new LocatorAssertions.HasTextOptions().setTimeout(3000)));
    assertTrue(error.getMessage().contains("frameLocator() matched elements in multiple frames"));
    assertTrue(error.getMessage().contains("Locator: frameLocator().locator('div')"));
  }

  @PlaywrightTest
  @DisplayName("LO-014 should fail toHaveText with an array when elements match in multiple frames")
  void shouldFailToHaveTextWithAnArrayWhenElementsMatchInMultipleFrames(Page page, TestServer server) {
    routePage(page, "empty.html", "<iframe src=\"a.html\"></iframe><iframe src=\"b.html\"></iframe>");
    routePage(page, "a.html", "<span>one</span>");
    routePage(page, "b.html", "<span>two</span>");
    page.navigate(server.EMPTY_PAGE);
    waitForAllFrames(page, 3, "span");
    AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertThat(page.frameLocator().locator("span")).hasText(new String[]{"one", "two"}, new LocatorAssertions.HasTextOptions().setTimeout(3000)));
    assertTrue(error.getMessage().contains("frameLocator() matched elements in multiple frames"));
  }

  @PlaywrightTest
  @DisplayName("LO-015 should fail toHaveText upon strict mode violation inside a single frame")
  @Fixme("java: the assertion message has no 'Locator: frameLocator().locator(...)' line, only the call log: Assert \"hasCount\" frameLocator().locator(\"span\")")
  void shouldFailToHaveTextUponStrictModeViolationInsideASingleFrame(Page page, TestServer server) {
    routePage(page, "empty.html", "<iframe src=\"a.html\"></iframe>");
    routePage(page, "a.html", "<div>one</div><div>two</div>");
    page.navigate(server.EMPTY_PAGE);
    waitForAllFrames(page, 2, "div");
    AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertThat(page.frameLocator().locator("div")).hasText("one", new LocatorAssertions.HasTextOptions().setTimeout(3000)));
    assertTrue(error.getMessage().contains("strict mode violation"));
    assertTrue(error.getMessage().contains("Locator: frameLocator().locator('div')"));
  }

  @PlaywrightTest
  @DisplayName("LO-016 should support evaluate")
  void shouldSupportEvaluate(Page page, TestServer server) {
    routePage(page, "empty.html", "<iframe src=\"a.html\"></iframe>");
    routePage(page, "a.html", "<div data-foo=\"bar\">Hello</div>");
    page.navigate(server.EMPTY_PAGE);
    assertEquals("bar", page.frameLocator().locator("div").evaluate("e => e.getAttribute('data-foo')"));
  }

  @PlaywrightTest
  @DisplayName("LO-017 should fail evaluate when elements match in multiple frames")
  void shouldFailEvaluateWhenElementsMatchInMultipleFrames(Page page, TestServer server) {
    routePage(page, "empty.html", "<iframe src=\"a.html\"></iframe><iframe src=\"b.html\"></iframe>");
    routePage(page, "a.html", "<div>one</div>");
    routePage(page, "b.html", "<div>two</div>");
    page.navigate(server.EMPTY_PAGE);
    waitForAllFrames(page, 3, "div");
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.frameLocator().locator("div").evaluate("e => e.textContent", null, new Locator.EvaluateOptions().setTimeout(3000)));
    assertTrue(error.getMessage().contains("frameLocator() matched elements in multiple frames"));
  }

  @PlaywrightTest
  @DisplayName("LO-018 should time out on evaluate when there are no matches")
  @Fixme("java: the call log says waiting for frameLocator().locator(\"button\") with double quotes, not locator('button')")
  void shouldTimeOutOnEvaluateWhenThereAreNoMatches(Page page, TestServer server) {
    routePage(page, "empty.html", "<iframe src=\"a.html\"></iframe>");
    routePage(page, "a.html", "<div>Nothing here</div>");
    page.navigate(server.EMPTY_PAGE);
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.frameLocator().locator("button").evaluate("e => e.textContent", null, new Locator.EvaluateOptions().setTimeout(1000)));
    assertTrue(error.getMessage().contains("Timeout 1000ms exceeded"));
    assertTrue(error.getMessage().contains("waiting for frameLocator().locator('button')"));
  }

  @PlaywrightTest
  @DisplayName("LO-019 should support evaluateAll")
  void shouldSupportEvaluateAll(Page page, TestServer server) {
    routePage(page, "empty.html", "<iframe src=\"a.html\"></iframe>");
    routePage(page, "a.html", "<span>one</span><span>two</span>");
    page.navigate(server.EMPTY_PAGE);
    waitForAllFrames(page, 2, "span");
    assertEquals(asList("one", "two"), page.frameLocator().locator("span").evaluateAll("els => els.map(e => e.textContent)"));
    assertEquals(0, page.frameLocator().locator("button").evaluateAll("els => els.length"));
  }

  @PlaywrightTest
  @DisplayName("LO-020 should fail evaluateAll when elements match in multiple frames")
  void shouldFailEvaluateAllWhenElementsMatchInMultipleFrames(Page page, TestServer server) {
    routePage(page, "empty.html", "<iframe src=\"a.html\"></iframe><iframe src=\"b.html\"></iframe>");
    routePage(page, "a.html", "<span>one</span>");
    routePage(page, "b.html", "<span>two</span>");
    page.navigate(server.EMPTY_PAGE);
    waitForAllFrames(page, 3, "span");
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.frameLocator().locator("span").evaluateAll("els => els.length"));
    assertTrue(error.getMessage().contains("frameLocator() matched elements in multiple frames"));
  }

  @PlaywrightTest
  @DisplayName("LO-021 should support hasText filter")
  void shouldSupportHasTextFilter(Page page, TestServer server) {
    routePage(page, "empty.html", "<iframe src=\"a.html\"></iframe>");
    routePage(page, "a.html", "<div>foo</div><div>bar</div>");
    page.navigate(server.EMPTY_PAGE);
    assertThat(page.frameLocator().locator("div", new FrameLocator.LocatorOptions().setHasText("bar"))).hasText("bar");
  }

  @PlaywrightTest
  @DisplayName("LO-022 should support first/last/nth")
  void shouldSupportFirstLastNth(Page page, TestServer server) {
    routePage(page, "empty.html", "<iframe src=\"a.html\"></iframe>");
    routePage(page, "a.html", "<span>one</span><span>two</span><span>three</span>");
    page.navigate(server.EMPTY_PAGE);
    waitForAllFrames(page, 2, "span");
    assertThat(page.frameLocator().locator("span").first()).hasText("one");
    assertThat(page.frameLocator().locator("span").last()).hasText("three");
    assertThat(page.frameLocator().locator("span").nth(1)).hasText("two");
  }

  @PlaywrightTest
  @DisplayName("LO-023 should support nth in the middle of the chain")
  void shouldSupportNthInTheMiddleOfTheChain(Page page, TestServer server) {
    routePage(page, "empty.html", "<iframe src=\"a.html\"></iframe>");
    routePage(page, "a.html", "<div><span>one</span></div><div><span>two</span></div>");
    page.navigate(server.EMPTY_PAGE);
    assertThat(page.frameLocator().locator("div").nth(1).locator("span")).hasText("two");
  }

  @PlaywrightTest
  @DisplayName("LO-024 should support composite locators")
  void shouldSupportCompositeLocators(Page page, TestServer server) {
    routePage(page, "empty.html", "<iframe src=\"a.html\"></iframe>");
    routePage(page, "a.html", "<div><span>foo</span></div><div><i>bar</i></div>");
    page.navigate(server.EMPTY_PAGE);
    assertThat(page.frameLocator().locator("div", new FrameLocator.LocatorOptions().setHas(page.locator("span")))).hasText("foo");
  }

  @PlaywrightTest
  @DisplayName("LO-025 should support capture")
  void shouldSupportCapture(Page page, TestServer server) {
    routePage(page, "empty.html", "<iframe src=\"a.html\"></iframe>");
    routePage(page, "a.html", "<div id=\"target\"><span>hello</span></div>");
    page.navigate(server.EMPTY_PAGE);
    assertThat(page.frameLocator().locator("*css=div >> span")).hasAttribute("id", "target");
  }

  @PlaywrightTest
  @DisplayName("LO-026 should find a frame inside the scope")
  void shouldFindAFrameInsideTheScope(Page page, TestServer server) {
    routePage(page, "empty.html", "<section><iframe src=\"a.html\"></iframe></section><button>outside</button>");
    routePage(page, "a.html", "<button>inside</button>");
    page.navigate(server.EMPTY_PAGE);
    waitForAllFrames(page, 2, "button");
    ElementHandle scope = page.querySelector("section");
    List<ElementHandle> buttons = scope.querySelectorAll("internal:control=any-frame >> button");
    assertEquals(1, buttons.size());
    assertEquals("inside", buttons.get(0).textContent());
  }

  @PlaywrightTest
  @DisplayName("LO-027 should find a nested frame inside the scope")
  void shouldFindANestedFrameInsideTheScope(Page page, TestServer server) {
    routePage(page, "empty.html", "<section><iframe src=\"a.html\"></iframe></section><iframe src=\"b.html\"></iframe>");
    routePage(page, "a.html", "<iframe src=\"c.html\"></iframe>");
    routePage(page, "b.html", "<button>outside</button>");
    routePage(page, "c.html", "<button>deep</button>");
    page.navigate(server.EMPTY_PAGE);
    expectPollToBe(page, () -> page.frames().size(), 4);
    for (Frame frame : page.frames()) {
      if (frame.url().contains("b.html") || frame.url().contains("c.html")) {
        frame.waitForSelector("button", new Frame.WaitForSelectorOptions().setState(WaitForSelectorState.ATTACHED));
      }
    }
    ElementHandle scope = page.querySelector("section");
    List<ElementHandle> buttons = scope.querySelectorAll("internal:control=any-frame >> button");
    assertEquals(1, buttons.size());
    assertEquals("deep", buttons.get(0).textContent());
  }

  @PlaywrightTest
  @DisplayName("LO-028 should find a frame inside the scope while another iframe is stalled")
  void shouldFindAFrameInsideTheScopeWhileAnotherIframeIsStalled(Page page, TestServer server) {
    routePage(page, "empty.html", "<section><iframe src=\"a.html\"></iframe><iframe src=\"stall.html\"></iframe></section>");
    routePage(page, "a.html", "<button>inside</button>");
    page.route("**/stall.html", route -> {});
    page.navigate(server.EMPTY_PAGE, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
    expectPollToBe(page, () -> page.frames().size(), 3);
    ElementHandle scope = page.querySelector("section");
    List<ElementHandle> buttons = scope.querySelectorAll("internal:control=any-frame >> button");
    assertEquals(1, buttons.size());
    assertEquals("inside", buttons.get(0).textContent());
  }

  @PlaywrightTest
  @DisplayName("LO-029 should respect the scope without a frame inside the scope")
  void shouldRespectTheScopeWithoutAFrameInsideTheScope(Page page, TestServer server) {
    routePage(page, "empty.html", "<section><button>target</button></section><iframe src=\"a.html\"></iframe>");
    routePage(page, "a.html", "<button>in-frame</button>");
    page.navigate(server.EMPTY_PAGE);
    waitForAllFrames(page, 2, "button");
    ElementHandle scope = page.querySelector("section");
    List<ElementHandle> buttons = scope.querySelectorAll("internal:control=any-frame >> button");
    assertEquals(1, buttons.size());
    assertEquals("target", buttons.get(0).textContent());
  }

  @PlaywrightTest
  @DisplayName("LO-030 should not match a chain across a frame boundary")
  void shouldNotMatchAChainAcrossAFrameBoundary(Page page, TestServer server) {
    routePage(page, "empty.html", "<section><iframe src=\"a.html\"></iframe></section>");
    routePage(page, "a.html", "<iframe src=\"b.html\"></iframe>");
    routePage(page, "b.html", "<button>deep</button>");
    page.navigate(server.EMPTY_PAGE);
    expectPollToBe(page, () -> page.frames().size(), 3);
    Frame deepFrame = findFrame(page, "b.html");
    deepFrame.waitForSelector("button", new Frame.WaitForSelectorOptions().setState(WaitForSelectorState.ATTACHED));
    // "section" lives in the main frame, while the button is two frames below it.
    assertThat(page.frameLocator().locator("section").locator("button")).hasCount(0);
    assertThat(page.frameLocator().locator("button")).hasText("deep");
  }

  @PlaywrightTest
  @DisplayName("LO-031 should only search frames inside the starting frame")
  void shouldOnlySearchFramesInsideTheStartingFrame(Page page, TestServer server) {
    routePage(page, "empty.html", "<iframe src=\"a.html\"></iframe><button>main</button>");
    routePage(page, "a.html", "<iframe src=\"b.html\"></iframe>");
    routePage(page, "b.html", "<button>deep</button>");
    page.navigate(server.EMPTY_PAGE);
    expectPollToBe(page, () -> page.frames().size(), 3);
    Frame deepFrame = findFrame(page, "b.html");
    deepFrame.waitForSelector("button", new Frame.WaitForSelectorOptions().setState(WaitForSelectorState.ATTACHED));
    Frame middleFrame = findFrame(page, "a.html");
    assertThat(middleFrame.frameLocator().locator("button")).hasText("deep");
  }

  @PlaywrightTest
  @DisplayName("LO-032 should enter a frame found in a nested frame")
  void shouldEnterAFrameFoundInANestedFrame(Page page, TestServer server) {
    routePage(page, "empty.html", "<iframe src=\"a.html\"></iframe><button>main</button>");
    routePage(page, "a.html", "<iframe id=\"target\" src=\"b.html\"></iframe><button>decoy</button>");
    routePage(page, "b.html", "<button>inside</button>");
    page.navigate(server.EMPTY_PAGE);
    assertThat(page.frameLocator().frameLocator("#target").locator("button")).hasText("inside");
  }

  @PlaywrightTest
  @DisplayName("LO-033 should click inside an entered frame")
  void shouldClickInsideAnEnteredFrame(Page page, TestServer server) {
    routePage(page, "empty.html", "<iframe src=\"a.html\"></iframe>");
    routePage(page, "a.html", "<iframe id=\"target\" src=\"b.html\"></iframe><button>Click me</button>");
    routePage(page, "b.html", "<button onclick=\"window.__clicked = true\">Click me</button>");
    page.navigate(server.EMPTY_PAGE);
    page.frameLocator().frameLocator("#target").getByRole(AriaRole.BUTTON, new FrameLocator.GetByRoleOptions().setName("Click me")).click();
    Frame frame = findFrame(page, "b.html");
    assertEquals(true, frame.evaluate("() => window.__clicked"));
  }

  @PlaywrightTest
  @DisplayName("LO-034 should not search nested frames after entering a frame")
  void shouldNotSearchNestedFramesAfterEnteringAFrame(Page page, TestServer server) {
    routePage(page, "empty.html", "<iframe id=\"target\" src=\"a.html\"></iframe><button>main</button>");
    routePage(page, "a.html", "<iframe src=\"b.html\"></iframe>");
    routePage(page, "b.html", "<button>deep</button>");
    page.navigate(server.EMPTY_PAGE);
    expectPollToBe(page, () -> page.frames().size(), 3);
    // The entered frame itself has no button, and we do not look inside its nested frames.
    assertThat(page.frameLocator().frameLocator("#target").locator("button")).hasCount(0);
  }

  @PlaywrightTest
  @DisplayName("LO-035 should support two frameLocators")
  void shouldSupportTwoFrameLocators(Page page, TestServer server) {
    routePage(page, "empty.html", "<iframe src=\"a.html\"></iframe>");
    routePage(page, "a.html", "<iframe id=\"x\" src=\"b.html\"></iframe>");
    routePage(page, "b.html", "<iframe id=\"y\" src=\"c.html\"></iframe><button>decoy</button>");
    routePage(page, "c.html", "<button>bottom</button>");
    page.navigate(server.EMPTY_PAGE);
    assertThat(page.frameLocator().frameLocator("#x").frameLocator("#y").locator("button")).hasText("bottom");
  }

  @PlaywrightTest
  @DisplayName("LO-036 should support locator before frameLocator")
  void shouldSupportLocatorBeforeFrameLocator(Page page, TestServer server) {
    routePage(page, "empty.html", "<iframe src=\"a.html\"></iframe>");
    routePage(page, "a.html", "<section><iframe src=\"b.html\"></iframe></section><iframe src=\"c.html\"></iframe>");
    routePage(page, "b.html", "<button>in-section</button>");
    routePage(page, "c.html", "<button>outside</button>");
    page.navigate(server.EMPTY_PAGE);
    assertThat(page.frameLocator().locator("section").frameLocator("iframe").locator("button")).hasText("in-section");
  }

  @PlaywrightTest
  @DisplayName("LO-037 should support owner of a frameLocator")
  void shouldSupportOwnerOfAFrameLocator(Page page, TestServer server) {
    routePage(page, "empty.html", "<iframe src=\"a.html\"></iframe>");
    routePage(page, "a.html", "<iframe id=\"target\" src=\"b.html\"></iframe>");
    routePage(page, "b.html", "<button>inside</button>");
    page.navigate(server.EMPTY_PAGE);
    assertEquals("target", page.frameLocator().frameLocator("#target").owner().getAttribute("id"));
  }

  @PlaywrightTest
  @DisplayName("LO-038 should wait for the frame to enter to appear")
  void shouldWaitForTheFrameToEnterToAppear(Page page, TestServer server) {
    routePage(page, "empty.html", "<iframe src=\"a.html\"></iframe>");
    routePage(page, "a.html", "<div>Nothing yet</div>");
    routePage(page, "b.html", "<button>late</button>");
    page.navigate(server.EMPTY_PAGE);
    expectPollToBe(page, () -> page.frames().size(), 2);
    page.frames().get(1).evaluate("() => {\n" +
      "    window.builtins.setTimeout(() => {\n" +
      "      const iframe = document.createElement('iframe');\n" +
      "      iframe.id = 'late';\n" +
      "      iframe.src = 'b.html';\n" +
      "      document.body.appendChild(iframe);\n" +
      "    }, 3000);\n" +
      "  }");
    assertThat(page.frameLocator().frameLocator("#late").locator("button")).hasText("late");
  }

  @PlaywrightTest
  @DisplayName("LO-039 should fail when the frame to enter matches in multiple frames")
  void shouldFailWhenTheFrameToEnterMatchesInMultipleFrames(Page page, TestServer server) {
    routePage(page, "empty.html", "<iframe src=\"a.html\"></iframe><iframe src=\"b.html\"></iframe>");
    routePage(page, "a.html", "<iframe class=\"inner\" src=\"c.html\"></iframe>");
    routePage(page, "b.html", "<iframe class=\"inner\" src=\"c.html\"></iframe>");
    routePage(page, "c.html", "<button>Click me</button>");
    page.navigate(server.EMPTY_PAGE);
    expectPollToBe(page, () -> page.frames().size(), 5);
    for (Frame frame : page.frames()) {
      if (frame.url().contains("c.html")) {
        frame.waitForSelector("button", new Frame.WaitForSelectorOptions().setState(WaitForSelectorState.ATTACHED));
      }
    }
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.frameLocator().frameLocator(".inner").locator("button").click(new Locator.ClickOptions().setTimeout(3000)));
    assertTrue(error.getMessage().contains("frameLocator() matched elements in multiple frames"));
  }

  @PlaywrightTest
  @DisplayName("LO-040 should support contentFrame")
  void shouldSupportContentFrame(Page page, TestServer server) {
    routePage(page, "empty.html", "<iframe src=\"a.html\"></iframe>");
    routePage(page, "a.html", "<iframe id=\"target\" src=\"b.html\"></iframe><button>decoy</button>");
    routePage(page, "b.html", "<button>inside</button>");
    page.navigate(server.EMPTY_PAGE);
    assertThat(page.frameLocator().locator("#target").contentFrame().locator("button")).hasText("inside");
  }

  @PlaywrightTest
  @DisplayName("LO-041 should render frameLocator() in the locator description")
  @Fixme("java: Locator.toString() is 'Locator@internal:control=any-frame >> #x >> internal:control=enter-frame >> button', not \"frameLocator().locator('#x').contentFrame().locator('button')\"")
  void shouldRenderFrameLocatorInTheLocatorDescription(Page page) {
    assertEquals("frameLocator().locator('#x').contentFrame().locator('button')", String.valueOf(page.frameLocator().frameLocator("#x").locator("button")));
    assertEquals("frameLocator().locator('section').locator('iframe').contentFrame().getByText('foo')", String.valueOf(page.frameLocator().locator("section").frameLocator("iframe").getByText("foo")));
  }

  @PlaywrightTest
  @DisplayName("LO-042 should not allow frameLocator() inside a composite locator")
  @Fixme("java: the driver message quotes with double quotes: while querying \"locator(\"button\").or(frameLocator().locator(\"button\"))\", not locator('button')")
  void shouldNotAllowFrameLocatorInsideACompositeLocator(Page page, TestServer server) {
    routePage(page, "empty.html", "<button>main</button><iframe src=\"a.html\"></iframe>");
    routePage(page, "a.html", "<a href=\"#\">link</a>");
    page.navigate(server.EMPTY_PAGE);
    waitForAllFrames(page, 2, "a");

    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.locator("button").or(page.frameLocator().locator("a")).count());
    assertTrue(error.getMessage().contains("frameLocator() is not allowed inside composite locators, while querying \"locator('button').or(frameLocator().locator('a'))\""));

    PlaywrightException error2 = assertThrows(PlaywrightException.class, () -> page.locator("button").filter(new Locator.FilterOptions().setHas(page.frameLocator().locator("a"))).count());
    assertTrue(error2.getMessage().contains("frameLocator() is not allowed inside composite locators"));

    // Repeating frameLocator() in the operand is not allowed either, even though the outer locator has it.
    PlaywrightException error3 = assertThrows(PlaywrightException.class, () -> page.frameLocator().locator("button").or(page.frameLocator().locator("a")).count());
    assertTrue(error3.getMessage().contains("frameLocator() is not allowed inside composite locators, while querying \"frameLocator().locator('button').or(frameLocator().locator('a'))\""));

    // Repeating frameLocator() is not allowed even when the rest of the frame chain matches.
    PlaywrightException error4 = assertThrows(PlaywrightException.class, () -> page.frameLocator().frameLocator("#f").locator("a").or(page.frameLocator().frameLocator("#f").locator("button")).count());
    assertTrue(error4.getMessage().contains("frameLocator() is not allowed inside composite locators"));

    // With frameLocator() first, the token applies to the whole locator, so both operands are searched in every frame.
    PlaywrightException error5 = assertThrows(PlaywrightException.class, () -> page.frameLocator().locator("a").or(page.locator("button")).count());
    assertTrue(error5.getMessage().contains("frameLocator() matched elements in multiple frames"));
  }

  @PlaywrightTest
  @DisplayName("LO-043 should support a composite locator under frameLocator()")
  void shouldSupportACompositeLocatorUnderFrameLocator(Page page, TestServer server) {
    routePage(page, "empty.html", "<iframe src=\"a.html\"></iframe>");
    routePage(page, "a.html", "<div class=\"classname\">first</div><button>second</button>");
    page.navigate(server.EMPTY_PAGE);
    waitForAllFrames(page, 2, "button");
    assertThat(page.frameLocator().locator(".classname").or(page.getByRole(AriaRole.BUTTON))).hasText(new String[]{"first", "second"});
  }

  @PlaywrightTest
  @DisplayName("LO-044 should support a composite locator under frameLocator() and a frame locator")
  void shouldSupportACompositeLocatorUnderFrameLocatorAndAFrameLocator(Page page, TestServer server) {
    routePage(page, "empty.html", "<iframe src=\"a.html\"></iframe>");
    routePage(page, "a.html", "<iframe id=\"f\" src=\"b.html\"></iframe>");
    routePage(page, "b.html", "<div class=\"classname\">first</div><button>second</button>");
    page.navigate(server.EMPTY_PAGE);
    expectPollToBe(page, () -> page.frames().size(), 3);
    assertThat(page.frameLocator().frameLocator("#f").locator(".classname").or(page.frameLocator("#f").getByRole(AriaRole.BUTTON))).hasText(new String[]{"first", "second"});
  }

  @PlaywrightTest
  @DisplayName("LO-045 should not allow first/last/nth on frameLocator()")
  void shouldNotAllowFirstLastNthOnFrameLocator(Page page) {
    PlaywrightException error1 = assertThrows(PlaywrightException.class, () -> page.frameLocator().first());
    assertTrue(error1.getMessage().contains("Selecting the nth frame is not allowed on frameLocator()"));
    PlaywrightException error2 = assertThrows(PlaywrightException.class, () -> page.frameLocator().last());
    assertTrue(error2.getMessage().contains("Selecting the nth frame is not allowed on frameLocator()"));
    PlaywrightException error3 = assertThrows(PlaywrightException.class, () -> page.frameLocator().nth(1));
    assertTrue(error3.getMessage().contains("Selecting the nth frame is not allowed on frameLocator()"));
  }

  @PlaywrightTest
  @DisplayName("LO-046 should not allow owner on frameLocator()")
  void shouldNotAllowOwnerOnFrameLocator(Page page) {
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.frameLocator().owner().count());
    assertTrue(error.getMessage().contains("Selector cannot be empty after frameLocator()"));
  }

  @PlaywrightTest
  @DisplayName("LO-047 should resolve aria-ref selectors")
  void shouldResolveAriaRefSelectors(Page page, TestServer server) {
    routePage(page, "empty.html", "<button>main</button><iframe src=\"a.html\"></iframe>");
    routePage(page, "a.html", "<button>inside</button>");
    page.navigate(server.EMPTY_PAGE);
    waitForAllFrames(page, 2, "button");
    String snapshot = page.ariaSnapshot(new Page.AriaSnapshotOptions().setMode(AriaSnapshotMode.AI));
    // Refs are unique across frames, so every starting frame resolves them to the same element.
    Matcher insideMatch = Pattern.compile("button \"inside\" \\[ref=(.*?)\\]").matcher(snapshot);
    assertTrue(insideMatch.find());
    assertTrue(Pattern.compile("^f\\d+e\\d+$").matcher(insideMatch.group(1)).find());
    assertThat(page.frameLocator().locator("aria-ref=" + insideMatch.group(1))).hasText("inside");
  }

  @PlaywrightTest
  @DisplayName("LO-048 should click while another iframe is stalled")
  void shouldClickWhileAnotherIframeIsStalled(Page page, TestServer server) {
    routePage(page, "empty.html", "<iframe src=\"a.html\"></iframe><iframe src=\"stall.html\"></iframe>");
    routePage(page, "a.html", "<button onclick=\"window.__clicked = true\">Click me</button>");
    page.route("**/stall.html", route -> {});
    page.navigate(server.EMPTY_PAGE, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
    expectPollToBe(page, () -> page.frames().size(), 3);
    page.frameLocator().locator("button").click();
    Frame frame = findFrame(page, "a.html");
    assertEquals(true, frame.evaluate("() => window.__clicked"));
  }

  @PlaywrightTest
  @DisplayName("LO-049 should support toBeVisible while another iframe is stalled")
  void shouldSupportToBeVisibleWhileAnotherIframeIsStalled(Page page, TestServer server) {
    routePage(page, "empty.html", "<iframe src=\"a.html\"></iframe><iframe src=\"stall.html\"></iframe>");
    routePage(page, "a.html", "<button>Click me</button>");
    page.route("**/stall.html", route -> {});
    page.navigate(server.EMPTY_PAGE, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
    expectPollToBe(page, () -> page.frames().size(), 3);
    assertThat(page.frameLocator().locator("button")).isVisible();
  }

  @PlaywrightTest
  @DisplayName("LO-050 should support toHaveCount while another iframe is stalled")
  void shouldSupportToHaveCountWhileAnotherIframeIsStalled(Page page, TestServer server) {
    routePage(page, "empty.html", "<iframe src=\"a.html\"></iframe><iframe src=\"stall.html\"></iframe>");
    routePage(page, "a.html", "<button>Click me</button>");
    page.route("**/stall.html", route -> {});
    page.navigate(server.EMPTY_PAGE, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
    expectPollToBe(page, () -> page.frames().size(), 3);
    assertThat(page.frameLocator().locator("button")).hasCount(1);
  }

  @PlaywrightTest
  @DisplayName("LO-051 should treat elements inside hidden iframe as hidden")
  void shouldTreatElementsInsideHiddenIframeAsHidden(Page page, TestServer server) {
    routePage(page, "empty.html", "<iframe src=\"a.html\" style=\"visibility: hidden\"></iframe>");
    routePage(page, "a.html", "<button onclick=\"window.__clicked = true\">Click me</button>");
    page.navigate(server.EMPTY_PAGE);
    Locator button = page.frameLocator().locator("button");
    assertThat(button).isAttached();
    assertEquals(false, button.isVisible());
    assertThat(button).isHidden();

    PlaywrightException error = assertThrows(PlaywrightException.class, () -> button.click(new Locator.ClickOptions().setTimeout(1000)));
    assertTrue(error.getMessage().contains("element is inside a hidden frame, retrying"));
    assertNull(page.frames().get(1).evaluate("() => window.__clicked"));

    page.evaluate("() => document.querySelector('iframe').style.visibility = 'visible'");
    assertThat(button).isVisible();
    button.click();
    assertEquals(true, page.frames().get(1).evaluate("() => window.__clicked"));
  }
}
