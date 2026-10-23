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

import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.opentest4j.AssertionFailedError;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class TestLocatorAnyFrame {
  private static void routePage(Page page, String url, String body) {
    page.route("**/" + url, route -> route.fulfill(new Route.FulfillOptions().setBody(body).setContentType("text/html")));
  }

  private static void waitForAllFrames(Page page, int frameCount, String selector) {
    // Wait for all child frames to load their content, so that the search
    // deterministically sees elements in all of them.
    page.waitForCondition(() -> page.frames().size() == frameCount);
    for (Frame frame : page.frames()) {
      if (frame != page.mainFrame()) {
        frame.waitForSelector(selector, new Frame.WaitForSelectorOptions().setState(WaitForSelectorState.ATTACHED));
      }
    }
  }

  @PlaywrightTest
  void shouldClickAButtonInsideAnIframe(Page page, Server server) {
    routePage(page, "empty.html", "<iframe src=\"a.html\"></iframe>");
    routePage(page, "a.html", "<button onclick=\"window.__clicked = true\">Click me</button>");
    page.navigate(server.EMPTY_PAGE);
    page.frameLocator().getByRole(AriaRole.BUTTON, new FrameLocator.GetByRoleOptions().setName("Click me")).click();
    assertEquals(true, page.frames().get(1).evaluate("() => window.__clicked"));
  }

  @PlaywrightTest
  void shouldClickAButtonInTheMainFrame(Page page, Server server) {
    routePage(page, "empty.html", "<iframe src=\"a.html\"></iframe><button onclick=\"window.__clicked = true\">Click me</button>");
    routePage(page, "a.html", "<div>No buttons here</div>");
    page.navigate(server.EMPTY_PAGE);
    page.frameLocator().locator("button").click();
    assertEquals(true, page.evaluate("() => window.__clicked"));
  }

  @PlaywrightTest
  void shouldFailClickWhenElementsMatchInMultipleFrames(Page page, Server server) {
    routePage(page, "empty.html", "<iframe src=\"a.html\"></iframe><iframe src=\"b.html\"></iframe>");
    routePage(page, "a.html", "<button>one</button>");
    routePage(page, "b.html", "<button>two</button>");
    page.navigate(server.EMPTY_PAGE);
    waitForAllFrames(page, 3, "button");
    PlaywrightException e = assertThrows(PlaywrightException.class,
      () -> page.frameLocator().locator("button").click(new Locator.ClickOptions().setTimeout(3000)));
    assertTrue(e.getMessage().contains("frameLocator() matched elements in multiple frames"), e.getMessage());
  }

  @PlaywrightTest
  void shouldFailClickUponStrictModeViolationInsideASingleFrame(Page page, Server server) {
    routePage(page, "empty.html", "<iframe src=\"a.html\"></iframe>");
    routePage(page, "a.html", "<button>one</button><button>two</button>");
    page.navigate(server.EMPTY_PAGE);
    waitForAllFrames(page, 2, "button");
    PlaywrightException e = assertThrows(PlaywrightException.class,
      () -> page.frameLocator().locator("button").click(new Locator.ClickOptions().setTimeout(3000)));
    assertTrue(e.getMessage().contains("strict mode violation"), e.getMessage());
  }

  @PlaywrightTest
  void shouldTimeOutOnClickWhenThereAreNoMatches(Page page, Server server) {
    routePage(page, "empty.html", "<iframe src=\"a.html\"></iframe>");
    routePage(page, "a.html", "<div>Nothing here</div>");
    page.navigate(server.EMPTY_PAGE);
    PlaywrightException e = assertThrows(PlaywrightException.class,
      () -> page.frameLocator().locator("button").click(new Locator.ClickOptions().setTimeout(1000)));
    assertTrue(e.getMessage().contains("Timeout 1000ms exceeded"), e.getMessage());
  }

  @PlaywrightTest
  void shouldCountElementsInASingleFrame(Page page, Server server) {
    routePage(page, "empty.html", "<iframe src=\"a.html\"></iframe>");
    routePage(page, "a.html", "<div>1</div><div>2</div><div>3</div>");
    page.navigate(server.EMPTY_PAGE);
    waitForAllFrames(page, 2, "div");
    assertEquals(3, page.frameLocator().locator("div").count());
    assertEquals(0, page.frameLocator().locator("button").count());
  }

  @PlaywrightTest
  void shouldFailCountWhenElementsMatchInMultipleFrames(Page page, Server server) {
    routePage(page, "empty.html", "<div>main</div><iframe src=\"a.html\"></iframe>");
    routePage(page, "a.html", "<div>child</div>");
    page.navigate(server.EMPTY_PAGE);
    waitForAllFrames(page, 2, "div");
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.frameLocator().locator("div").count());
    assertTrue(e.getMessage().contains("frameLocator() matched elements in multiple frames"), e.getMessage());
  }

  @PlaywrightTest
  void shouldSupportHasCount(Page page, Server server) {
    routePage(page, "empty.html", "<iframe src=\"a.html\"></iframe>");
    routePage(page, "a.html", "<span>one</span><span>two</span>");
    page.navigate(server.EMPTY_PAGE);
    assertThat(page.frameLocator().locator("span")).hasCount(2);
    assertThat(page.frameLocator().locator("button")).hasCount(0);
  }

  @PlaywrightTest
  void shouldWaitForAFrameToAppearWithHasCount(Page page, Server server) {
    routePage(page, "empty.html", "<div>No frames yet</div>");
    routePage(page, "a.html", "<span>one</span><span>two</span>");
    page.navigate(server.EMPTY_PAGE);
    page.evaluate("() => {\n" +
      "  setTimeout(() => {\n" +
      "    const iframe = document.createElement('iframe');\n" +
      "    iframe.src = 'a.html';\n" +
      "    document.body.appendChild(iframe);\n" +
      "  }, 500);\n" +
      "}");
    assertThat(page.frameLocator().locator("span")).hasCount(2);
  }

  @PlaywrightTest
  void shouldFailHasCountWhenElementsMatchInMultipleFrames(Page page, Server server) {
    routePage(page, "empty.html", "<iframe src=\"a.html\"></iframe><iframe src=\"b.html\"></iframe>");
    routePage(page, "a.html", "<span>one</span>");
    routePage(page, "b.html", "<span>two</span>");
    page.navigate(server.EMPTY_PAGE);
    waitForAllFrames(page, 3, "span");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> assertThat(page.frameLocator().locator("span"))
      .hasCount(2, new LocatorAssertions.HasCountOptions().setTimeout(3000)));
    assertTrue(e.getMessage().contains("frameLocator() matched elements in multiple frames"), e.getMessage());
  }

  @PlaywrightTest
  void shouldSupportHasText(Page page, Server server) {
    routePage(page, "empty.html", "<iframe src=\"a.html\"></iframe>");
    routePage(page, "a.html", "<div>Hello iframe</div>");
    page.navigate(server.EMPTY_PAGE);
    assertThat(page.frameLocator().locator("div")).hasText("Hello iframe");
  }

  @PlaywrightTest
  void shouldSupportEvaluate(Page page, Server server) {
    routePage(page, "empty.html", "<iframe src=\"a.html\"></iframe>");
    routePage(page, "a.html", "<button>one</button>");
    page.navigate(server.EMPTY_PAGE);
    assertEquals("one", page.frameLocator().locator("button").evaluate("e => e.textContent"));
  }

  @PlaywrightTest
  void shouldSupportFirstLastNth(Page page, Server server) {
    routePage(page, "empty.html", "<iframe src=\"a.html\"></iframe>");
    routePage(page, "a.html", "<span>one</span><span>two</span><span>three</span>");
    page.navigate(server.EMPTY_PAGE);
    assertThat(page.frameLocator().locator("span").first()).hasText("one");
    assertThat(page.frameLocator().locator("span").last()).hasText("three");
    assertThat(page.frameLocator().locator("span").nth(1)).hasText("two");
  }

  @PlaywrightTest
  void shouldNotAllowNthFrameOnAnyFrameLocator(Page page) {
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.frameLocator().first());
    assertTrue(e.getMessage().contains("Selecting the nth frame is not allowed on frameLocator()"), e.getMessage());
    assertThrows(PlaywrightException.class, () -> page.frameLocator().last());
    assertThrows(PlaywrightException.class, () -> page.frameLocator().nth(1));
  }

  @PlaywrightTest
  void shouldSupportTwoFrameLocators(Page page, Server server) {
    routePage(page, "empty.html", "<iframe src=\"a.html\"></iframe>");
    routePage(page, "a.html", "<iframe id=\"x\" src=\"b.html\"></iframe>");
    routePage(page, "b.html", "<iframe id=\"y\" src=\"c.html\"></iframe><button>decoy</button>");
    routePage(page, "c.html", "<button>bottom</button>");
    page.navigate(server.EMPTY_PAGE);
    assertThat(page.frameLocator().frameLocator("#x").frameLocator("#y").locator("button")).hasText("bottom");
  }

  @PlaywrightTest
  void shouldSupportLocatorBeforeFrameLocator(Page page, Server server) {
    routePage(page, "empty.html", "<iframe src=\"a.html\"></iframe>");
    routePage(page, "a.html", "<section><iframe src=\"b.html\"></iframe></section><iframe src=\"c.html\"></iframe>");
    routePage(page, "b.html", "<button>in-section</button>");
    routePage(page, "c.html", "<button>outside</button>");
    page.navigate(server.EMPTY_PAGE);
    assertThat(page.frameLocator().locator("section").frameLocator("iframe").locator("button")).hasText("in-section");
  }

  @PlaywrightTest
  void shouldSupportOwnerOfAFrameLocator(Page page, Server server) {
    routePage(page, "empty.html", "<iframe src=\"a.html\"></iframe>");
    routePage(page, "a.html", "<iframe id=\"target\" src=\"b.html\"></iframe>");
    routePage(page, "b.html", "<button>inside</button>");
    page.navigate(server.EMPTY_PAGE);
    assertEquals("target", page.frameLocator().frameLocator("#target").owner().getAttribute("id"));
  }

  @PlaywrightTest
  void shouldSupportContentFrameAfterAnyFrameLocator(Page page, Server server) {
    routePage(page, "empty.html", "<iframe src=\"a.html\"></iframe>");
    routePage(page, "a.html", "<iframe id=\"my-frame\" src=\"b.html\"></iframe>");
    routePage(page, "b.html", "<button>inside</button>");
    page.navigate(server.EMPTY_PAGE);
    assertThat(page.frameLocator().locator("#my-frame").contentFrame().getByRole(AriaRole.BUTTON)).hasText("inside");
  }

  @PlaywrightTest
  void shouldWorkOnFrame(Page page, Server server) {
    routePage(page, "empty.html", "<iframe src=\"a.html\"></iframe>");
    routePage(page, "a.html", "<iframe src=\"b.html\"></iframe>");
    routePage(page, "b.html", "<button>deep</button>");
    page.navigate(server.EMPTY_PAGE);
    page.waitForCondition(() -> page.frames().size() == 3);
    assertThat(page.frames().get(1).frameLocator().locator("button")).hasText("deep");
  }
}
