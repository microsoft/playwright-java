/**
 * Copyright (c) Microsoft Corporation.
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
import com.microsoft.playwright.Page;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.ColorScheme;
import com.microsoft.playwright.options.Contrast;
import com.microsoft.playwright.options.ForcedColors;
import com.microsoft.playwright.options.Media;
import com.microsoft.playwright.options.ReducedMotion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.Arrays;
import java.util.Map;

import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** Port of tests/library/emulation-coop.spec.ts. */
public class TestEmulationCoop {
  private static void setupRoutes(TestServer server) {
    String[] paths = {"/plain.html", "/coop.html"};
    boolean[] coops = {false, true};
    for (int i = 0; i < paths.length; i++) {
      boolean coop = coops[i];
      server.setRoute(paths[i], (req, res) -> {
        if (coop) {
          res.setHeader("Cross-Origin-Opener-Policy", "same-origin");
        }
        res.setHeader("Content-Type", "text/html");
        res.end("<title>t</title><p>hello</p>");
      });
    }
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> probe(Browser browser, TestServer server, Browser.NewContextOptions options, String path, Page.EmulateMediaOptions media) {
    BrowserContext context = browser.newContext(options);
    Page page = context.newPage();
    if (media != null) {
      page.emulateMedia(media);
    }
    page.navigate(server.PREFIX + path);
    Map<String, Object> result = (Map<String, Object>) page.evaluate("() => ({\n" +
      "  dark: matchMedia('(prefers-color-scheme: dark)').matches,\n" +
      "  dpr: devicePixelRatio,\n" +
      "  reducedMotion: matchMedia('(prefers-reduced-motion: reduce)').matches,\n" +
      "  forcedColors: matchMedia('(forced-colors: active)').matches,\n" +
      "  contrast: matchMedia('(prefers-contrast: more)').matches,\n" +
      "  print: matchMedia('print').matches,\n" +
      "  lang: navigator.language,\n" +
      "  tz: Intl.DateTimeFormat().resolvedOptions().timeZone,\n" +
      "  ua: navigator.userAgent,\n" +
      "  touch: 'ontouchstart' in window,\n" +
      "  coarse: matchMedia('(pointer: coarse)').matches,\n" +
      "  viewport: [innerWidth, innerHeight],\n" +
      "  screen: [screen.width, screen.height],\n" +
      "  orientation: screen.orientation.type,\n" +
      "})");
    context.close();
    return result;
  }

  // Java: expect(actual).toEqual(expect.objectContaining(expected)) checks the expected keys only.
  private static void assertObjectContaining(Map<String, Object> expected, Map<String, Object> actual) {
    for (Map.Entry<String, Object> entry : expected.entrySet()) {
      assertEquals(entry.getValue(), actual.get(entry.getKey()), entry.getKey());
    }
  }

  @Nested
  class EmulationWithCOOP {
    @PlaywrightTest
    @DisplayName("EM-025 should preserve context options")
    @Fixme(value = "driver: Firefox 157 of driver 1.64.1 loses the emulation overrides after a COOP navigation (the /coop.html result differs from /plain.html), fixed by the Firefox roll to r1564, microsoft/playwright#43494", browsers = BrowserName.FIREFOX)
    void shouldPreserveContextOptions(Browser browser, TestServer server) {
      setupRoutes(server);
      Browser.NewContextOptions options = new Browser.NewContextOptions()
        .setColorScheme(ColorScheme.DARK)
        .setDeviceScaleFactor(2)
        .setReducedMotion(ReducedMotion.REDUCE)
        .setContrast(Contrast.MORE)
        .setLocale("de-DE")
        .setTimezoneId("Asia/Tokyo")
        .setUserAgent("MyUA")
        .setHasTouch(true)
        .setViewportSize(500, 600);
      Map<String, Object> plain = probe(browser, server, options, "/plain.html", null);
      assertObjectContaining(mapOf(
        "dark", true,
        "dpr", 2,
        "reducedMotion", true,
        "contrast", true,
        "lang", "de-DE",
        "tz", "Asia/Tokyo",
        "ua", "MyUA",
        "touch", true,
        "viewport", Arrays.asList(500, 600)), plain);
      Map<String, Object> coop = probe(browser, server, options, "/coop.html", null);
      assertEquals(plain, coop);
    }

    @PlaywrightTest
    @DisplayName("EM-026 should preserve page.emulateMedia")
    @Fixme(value = "driver: Firefox 157 of driver 1.64.1 loses the emulation overrides after a COOP navigation (the /coop.html result differs from /plain.html), fixed by the Firefox roll to r1564, microsoft/playwright#43494", browsers = BrowserName.FIREFOX)
    void shouldPreservePageEmulateMedia(Browser browser, TestServer server) {
      setupRoutes(server);
      Page.EmulateMediaOptions media = new Page.EmulateMediaOptions()
        .setMedia(Media.PRINT)
        .setColorScheme(ColorScheme.DARK)
        .setReducedMotion(ReducedMotion.REDUCE)
        .setContrast(Contrast.MORE)
        .setForcedColors(ForcedColors.ACTIVE);
      Map<String, Object> plain = probe(browser, server, new Browser.NewContextOptions(), "/plain.html", media);
      assertObjectContaining(mapOf(
        "print", true,
        "reducedMotion", true,
        "contrast", true,
        "forcedColors", true), plain);
      Map<String, Object> coop = probe(browser, server, new Browser.NewContextOptions(), "/coop.html", media);
      assertEquals(plain, coop);
    }

    @PlaywrightTest
    @DisplayName("EM-027 should preserve mobile emulation")
    @Fixme(value = "driver: Firefox 157 of driver 1.64.1 loses the emulation overrides after a COOP navigation (the /coop.html result differs from /plain.html), fixed by the Firefox roll to r1564, microsoft/playwright#43494", browsers = BrowserName.FIREFOX)
    void shouldPreserveMobileEmulation(Browser browser, TestServer server) {
      setupRoutes(server);
      Browser.NewContextOptions options = new Browser.NewContextOptions()
        .setIsMobile(true)
        .setHasTouch(true)
        .setViewportSize(500, 600)
        .setScreenSize(700, 900);
      Map<String, Object> plain = probe(browser, server, options, "/plain.html", null);
      assertObjectContaining(mapOf(
        "coarse", true,
        "orientation", "portrait-primary"), plain);
      Map<String, Object> coop = probe(browser, server, options, "/coop.html", null);
      assertEquals(plain, coop);
    }
  }
}
