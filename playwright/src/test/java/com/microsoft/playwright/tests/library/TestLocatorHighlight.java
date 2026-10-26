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
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** Port of tests/library/locator-highlight.spec.ts. */
public class TestLocatorHighlight {
  @PlaywrightTest
  @DisplayName("LO-193 highlight should accept a CSS string style")
  void highlightShouldAcceptACSSStringStyle(Browser browser, TestServer server, BrowserName browserName) {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    page.navigate(server.PREFIX + "/input/button.html");

    page.getByRole(AriaRole.BUTTON).highlight(new Locator.HighlightOptions().setStyle("outline: 3px solid rgb(255, 0, 0); background-color: rgba(0, 255, 0, 0.25)"));

    Locator highlight = page.locator("x-pw-highlight");
    assertThat(highlight).isVisible();
    @SuppressWarnings("unchecked")
    Map<String, Object> style = (Map<String, Object>) highlight.evaluate("(el) => ({\n" +
      "  outline: el.style.outline,\n" +
      "  backgroundColor: el.style.backgroundColor,\n" +
      "})");
    if (browserName == BrowserName.CHROMIUM || browserName == BrowserName.FIREFOX)
      assertEquals("rgb(255, 0, 0) solid 3px", style.get("outline"));
    else
      assertEquals("3px solid rgb(255, 0, 0)", style.get("outline"));
    assertEquals("rgba(0, 255, 0, 0.25)", style.get("backgroundColor"));

    context.close();
  }

  @PlaywrightTest
  @DisplayName("LO-195 hideHighlight removes a styled highlight")
  void hideHighlightRemovesAStyledHighlight(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    page.navigate(server.PREFIX + "/input/button.html");

    Locator button = page.getByRole(AriaRole.BUTTON);
    button.highlight(new Locator.HighlightOptions().setStyle("outline: 2px solid red"));
    assertThat(page.locator("x-pw-highlight")).isVisible();

    button.hideHighlight();
    assertThat(page.locator("x-pw-highlight")).hasCount(0);

    context.close();
  }

  @PlaywrightTest
  @DisplayName("LO-196 highlight should survive navigation")
  void highlightShouldSurviveNavigation(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    page.setContent("<button onclick=\"console.log(1)\">Clicker</button>");

    page.getByRole(AriaRole.BUTTON).highlight();
    assertThat(page.locator("x-pw-highlight")).hasCount(1);

    // Highlights are resolved again after the navigation.
    page.navigate(server.PREFIX + "/input/button.html");
    assertThat(page.locator("x-pw-highlight")).hasCount(1);

    page.hideHighlight();
    assertThat(page.locator("x-pw-highlight")).hasCount(0);

    context.close();
  }

  @PlaywrightTest
  @DisplayName("LO-197 Page.hideHighlight clears all locator highlights")
  void pageHideHighlightClearsAllLocatorHighlights(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    page.setContent("<button>One</button><button>Two</button>");

    page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("One")).highlight();
    page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Two")).highlight();
    assertThat(page.locator("x-pw-highlight")).hasCount(2);

    page.hideHighlight();
    assertThat(page.locator("x-pw-highlight")).hasCount(0);

    context.close();
  }

  @PlaywrightTest
  @DisplayName("LO-198 highlight should work with a custom selector engine that runs in the main world")
  void highlightShouldWorkWithACustomSelectorEngineThatRunsInTheMainWorld(Playwright playwright, Browser browser) {
    // Engines registered without "contentScript" run in the main world and can see page globals.
    // Java: a selector engine function is registered by its source, '(' + fn.toString() + ')()'.
    String createEngine = "(() => ({\n" +
      "  query(root, selector) {\n" +
      "    return window['__engineEnabled'] ? root.querySelector(selector) : null;\n" +
      "  },\n" +
      "  queryAll(root, selector) {\n" +
      "    return window['__engineEnabled'] ? Array.from(root.querySelectorAll(selector)) : [];\n" +
      "  },\n" +
      "}))()";
    playwright.selectors().register("highlight-tag", createEngine);

    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    page.setContent("<button>Button</button>");
    page.evaluate("() => window['__engineEnabled'] = true");

    Locator button = page.locator("highlight-tag=button");
    button.highlight();
    assertThat(page.locator("x-pw-highlight")).hasCount(1);

    button.hideHighlight();
    assertThat(page.locator("x-pw-highlight")).hasCount(0);

    button.highlight();
    assertThat(page.locator("x-pw-highlight")).hasCount(1);

    page.hideHighlight();
    assertThat(page.locator("x-pw-highlight")).hasCount(0);

    context.close();
  }

  @PlaywrightTest
  @DisplayName("LO-199 highlight should resolve relative to the frame of the locator")
  void highlightShouldResolveRelativeToTheFrameOfTheLocator(Browser browser) {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    page.setContent("<iframe name=\"frame\" srcdoc=\"<button>foo</button>\"></iframe><button>bar</button>");
    Frame frame = page.frame("frame");

    page.locator("button").highlight();
    frame.locator("button").highlight();
    assertThat(page.locator("x-pw-highlight")).hasCount(1);
    assertThat(frame.locator("x-pw-highlight")).hasCount(1);

    page.locator("button").hideHighlight();
    assertThat(page.locator("x-pw-highlight")).hasCount(0);
    assertThat(frame.locator("x-pw-highlight")).hasCount(1);

    frame.locator("button").hideHighlight();
    assertThat(frame.locator("x-pw-highlight")).hasCount(0);

    context.close();
  }
}
