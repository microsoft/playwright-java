/**
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

package com.microsoft.playwright.tests.library;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.ConsoleMessage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.BoundingBox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.List;
import java.util.Map;

import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/library/browsercontext-viewport-mobile.spec.ts. */
public class TestBrowsercontextViewportMobile {
  @Nested
  class MobileViewport {
    @BeforeEach
    void skipFirefox(BrowserName browserName) {
      assumeFalse(browserName == BrowserName.FIREFOX && !Env.isBidi());
    }

    @PlaywrightTest
    @DisplayName("DV-014 should detect touch when applying viewport with touches")
    void shouldDetectTouchWhenApplyingViewportWithTouches(Browser browser, TestServer server) {
      BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(800, 600).setHasTouch(true));
      Page page = context.newPage();
      page.navigate(server.EMPTY_PAGE);
      assertEquals(true, page.evaluate("() => 'ontouchstart' in window || !!window.TouchEvent"));
      context.close();
    }

    @PlaywrightTest
    @DisplayName("DV-016 should support window.orientation emulation")
    void shouldSupportWindowOrientationEmulation(Browser browser, TestServer server) {
      BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(300, 400).setIsMobile(true));
      Page page = context.newPage();
      page.navigate(server.PREFIX + "/mobile.html");
      assertEquals(0, page.evaluate("() => window.orientation"));
      page.setViewportSize(400, 300);
      assertEquals(90, page.evaluate("() => window.orientation"));
      context.close();
    }

    @PlaywrightTest
    @DisplayName("DV-017 should preserve window.orientation override after navigation")
    void shouldPreserveWindowOrientationOverrideAfterNavigation(Browser browser, TestServer server) {
      BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(400, 300).setIsMobile(true));
      Page page = context.newPage();
      page.navigate(server.PREFIX + "/mobile.html");
      assertEquals(90, page.evaluate("() => window.orientation"));
      page.navigate(server.CROSS_PROCESS_PREFIX + "/mobile.html");
      assertEquals(90, page.evaluate("() => window.orientation"));
      context.close();
    }

    @PlaywrightTest
    @DisplayName("DV-018 should preserve screen.orientation.type override after navigation")
    void shouldPreserveScreenOrientationTypeOverrideAfterNavigation(Browser browser, TestServer server) {
      BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(300, 400).setIsMobile(true));
      Page page = context.newPage();
      page.navigate(server.PREFIX + "/mobile.html");
      assertEquals("portrait-primary", page.evaluate("() => window.screen.orientation.type"));
      page.navigate(server.CROSS_PROCESS_PREFIX + "/mobile.html");
      assertEquals("portrait-primary", page.evaluate("() => window.screen.orientation.type"));
      context.close();
    }

    @PlaywrightTest
    @DisplayName("DV-019 should fire orientationchange event")
    void shouldFireOrientationchangeEvent(Browser browser, TestServer server) {
      BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(300, 400).setIsMobile(true));
      Page page = context.newPage();
      page.navigate(server.PREFIX + "/mobile.html");
      page.evaluate("() => {\n" +
        "  let counter = 0;\n" +
        "  window.addEventListener('orientationchange', () => console.log(++counter));\n" +
        "}");

      ConsoleMessage event1 = page.waitForConsoleMessage(() -> page.setViewportSize(400, 300));
      assertEquals("1", event1.text());

      ConsoleMessage event2 = page.waitForConsoleMessage(() -> page.setViewportSize(300, 400));
      assertEquals("2", event2.text());
      context.close();
    }

    @PlaywrightTest
    @DisplayName("DV-020 default mobile viewports to 980 width")
    void defaultMobileViewportsTo980Width(Browser browser, TestServer server) {
      BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(320, 480).setIsMobile(true));
      Page page = context.newPage();
      page.navigate(server.PREFIX + "/empty.html");
      assertEquals(980, page.evaluate("() => window.innerWidth"));
      context.close();
    }

    @PlaywrightTest
    @DisplayName("DV-021 respect meta viewport tag")
    void respectMetaViewportTag(Browser browser, TestServer server) {
      BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(320, 480).setIsMobile(true));
      Page page = context.newPage();
      page.navigate(server.PREFIX + "/mobile.html");
      assertEquals(320, page.evaluate("() => window.innerWidth"));
      context.close();
    }

    @PlaywrightTest
    @DisplayName("DV-023 mouse should work with mobile viewports and cross process navigations")
    void mouseShouldWorkWithMobileViewportsAndCrossProcessNavigations(Browser browser, TestServer server) {
      // @see https://crbug.com/929806
      BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(360, 640).setIsMobile(true));
      Page page = context.newPage();
      page.navigate(server.EMPTY_PAGE);
      page.navigate(server.CROSS_PROCESS_PREFIX + "/mobile.html");
      page.evaluate("() => {\n" +
        "  document.addEventListener('click', event => {\n" +
        "    window['result'] = { x: event.clientX, y: event.clientY };\n" +
        "  });\n" +
        "}");

      page.mouse().click(30, 40);

      assertEquals(mapOf("x", 30, "y", 40), page.evaluate("result"));
      context.close();
    }

    @PlaywrightTest
    @DisplayName("DV-024 should scroll when emulating a mobile viewport")
    void shouldScrollWhenEmulatingAMobileViewport(Browser browser, TestServer server, BrowserName browserName) {
      BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1000, 600).setIsMobile(true));
      Page page = context.newPage();
      page.navigate(server.PREFIX + "/input/scrollable.html");
      page.mouse().move(50, 60);
      PlaywrightException error = null;
      try {
        page.mouse().wheel(0, 100);
      } catch (PlaywrightException e) {
        error = e;
      }
      if (browserName == BrowserName.WEBKIT) {
        assertTrue(error.getMessage().contains("Mouse wheel is not supported in mobile WebKit"));
      } else {
        page.waitForFunction("window.scrollY === 100");
      }
      context.close();
    }

    @PlaywrightTest
    @DisplayName("DV-026 view scale should reset after navigation")
    void viewScaleShouldResetAfterNavigation(Browser browser) {
      BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(390, 664).setIsMobile(true));
      Page page = context.newPage();
      page.navigate("data:text/html,<meta name='viewport' content='device-width, initial-scale=1'><button>Mobile Viewport</button>");
      page.route("**/button.html", route -> {
        route.fulfill(new Route.FulfillOptions()
          .setBody("<body>\n" +
            "          <button>Click me</button>\n" +
            "          <script>\n" +
            "            window.clicks = [];\n" +
            "            document.addEventListener('click', e => {\n" +
            "              const dot = document.createElement('div');\n" +
            "              dot.style.position = 'absolute';\n" +
            "              dot.style.width = '10px';\n" +
            "              dot.style.height = '10px';\n" +
            "              dot.style.borderRadius = '5px';\n" +
            "              dot.style.backgroundColor = 'red';\n" +
            "              dot.style.left = e.pageX + 'px';\n" +
            "              dot.style.top = e.pageY + 'px';\n" +
            "              dot.textContent = 'x: ' + e.pageX + ' y: ' + e.pageY;\n" +
            "              document.body.appendChild(dot);\n" +
            "              window.clicks.push({ x: e.pageX, y: e.pageY });\n" +
            "            });\n" +
            "          </script>\n" +
            "        </body>")
          .setContentType("text/html"));
      });
      page.navigate("http://localhost/button.html");
      page.getByText("Click me").click(new Locator.ClickOptions().setForce(true));
      BoundingBox box = page.locator("button").boundingBox();
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> clicks = (List<Map<String, Object>>) page.evaluate("() => window.clicks");
      assertEquals(1, clicks.size());
      // Java: the destructuring [{ x, y }] of the first click; page numbers come as Integer or Double.
      double x = ((Number) clicks.get(0).get("x")).doubleValue();
      double y = ((Number) clicks.get(0).get("y")).doubleValue();
      boolean isClickInsideButton = box.x <= x && x <= box.x + box.width && box.y <= y && y <= box.y + box.height;
      assertEquals(true, isClickInsideButton);
      context.close();
    }
  }
}
