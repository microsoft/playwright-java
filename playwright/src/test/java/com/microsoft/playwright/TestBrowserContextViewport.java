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

import com.microsoft.playwright.fixtures.PlaywrightTest;

import java.util.Map;

import static com.microsoft.playwright.Utils.verifyViewport;
import static org.junit.jupiter.api.Assertions.*;

public class TestBrowserContextViewport {

  @PlaywrightTest
  void shouldGetTheProperDefaultViewPortSize(Page page) {
    verifyViewport(page, 1280, 720);
  }

  @PlaywrightTest
  void shouldSetTheProperViewportSize(Page page) {
    verifyViewport(page, 1280, 720);
    page.setViewportSize(123, 456);
    verifyViewport(page,123, 456);
  }

  @PlaywrightTest
  void shouldReturnCorrectOuterWidthAndOuterHeight(Page page) {
    Map<String, Integer> size = (Map<String, Integer>) page.evaluate("() => {\n" +
      "  return {\n" +
      "    innerWidth: window.innerWidth,\n" +
      "    innerHeight: window.innerHeight,\n" +
      "    outerWidth: window.outerWidth,\n" +
      "    outerHeight: window.outerHeight,\n" +
      "  };\n" +
      "}");
    assertEquals(1280, size.get("innerWidth"));
    assertEquals(720, size.get("innerHeight"));
    assertTrue(size.get("outerWidth") >= size.get("innerWidth"));
    assertTrue(size.get("outerHeight") >= size.get("innerHeight"));
  }

  @PlaywrightTest
  void shouldEmulateDeviceWidth(Page page) {
    verifyViewport(page, 1280, 720);
    page.setViewportSize(200, 200);
    assertEquals(200, page.evaluate("() => window.screen.width"));
    assertEquals(true, page.evaluate("() => matchMedia('(min-device-width: 100px)').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('(min-device-width: 300px)').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('(max-device-width: 100px)').matches"));
    assertEquals(true, page.evaluate("() => matchMedia('(max-device-width: 300px)').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('(device-width: 500px)').matches"));
    assertEquals(true, page.evaluate("() => matchMedia('(device-width: 200px)').matches"));
    page.setViewportSize(500, 500);
    assertEquals(500, page.evaluate("() => window.screen.width"));
    assertEquals(true, page.evaluate("() => matchMedia('(min-device-width: 400px)').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('(min-device-width: 600px)').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('(max-device-width: 400px)').matches"));
    assertEquals(true, page.evaluate("() => matchMedia('(max-device-width: 600px)').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('(device-width: 200px)').matches"));
    assertEquals(true, page.evaluate("() => matchMedia('(device-width: 500px)').matches"));
  }

  @PlaywrightTest
  void shouldEmulateDeviceHeight(Page page) {
    verifyViewport(page, 1280, 720);
    page.setViewportSize(200, 200);
    assertEquals(200, page.evaluate("() => window.screen.height"));
    assertEquals(true, page.evaluate("() => matchMedia('(min-device-height: 100px)').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('(min-device-height: 300px)').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('(max-device-height: 100px)').matches"));
    assertEquals(true, page.evaluate("() => matchMedia('(max-device-height: 300px)').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('(device-height: 500px)').matches"));
    assertEquals(true, page.evaluate("() => matchMedia('(device-height: 200px)').matches"));
    page.setViewportSize(500, 500);
    assertEquals(500, page.evaluate("() => window.screen.height"));
    assertEquals(true, page.evaluate("() => matchMedia('(min-device-height: 400px)').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('(min-device-height: 600px)').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('(max-device-height: 400px)').matches"));
    assertEquals(true, page.evaluate("() => matchMedia('(max-device-height: 600px)').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('(device-height: 200px)').matches"));
    assertEquals(true, page.evaluate("() => matchMedia('(device-height: 500px)').matches"));
  }

  @PlaywrightTest
  void shouldEmulateAvailWidthAndAvailHeight(Page page) {
    page.setViewportSize(500, 600);
    assertEquals(500, page.evaluate("() => window.screen.availWidth"));
    assertEquals(600, page.evaluate("() => window.screen.availHeight"));
  }

  @PlaywrightTest
  void shouldNotHaveTouchByDefault(Page page, Server server) {
    page.navigate(server.PREFIX + "/mobile.html");
    assertEquals(false, page.evaluate("() => 'ontouchstart' in window"));
    page.navigate(server.PREFIX + "/detect-touch.html");
    assertEquals("NO", page.evaluate("() => document.body.textContent.trim()"));
  }

  @PlaywrightTest
  void shouldSupportTouchWithNullViewport(Browser browser, Server server) {
    Browser.NewContextOptions options = new Browser.NewContextOptions()
      .setHasTouch(true).setViewportSize(null);
    BrowserContext context = browser.newContext(options);
    Page page = context.newPage();
    page.navigate(server.PREFIX + "/mobile.html");
    assertEquals(true, page.evaluate("() => 'ontouchstart' in window"));
    context.close();
  }

  @PlaywrightTest
  void shouldReportNullViewPortSizeWhenGivenNullViewport(Browser browser) {
    Browser.NewContextOptions options = new Browser.NewContextOptions().setViewportSize(null);
    BrowserContext context = browser.newContext(options);
    Page page = context.newPage();
    assertNull(page.viewportSize());
    context.close();
  }
}
