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

package com.microsoft.playwright.tests.page;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.ColorScheme;
import com.microsoft.playwright.options.Contrast;
import com.microsoft.playwright.options.ForcedColors;
import com.microsoft.playwright.options.Media;
import com.microsoft.playwright.options.ReducedMotion;
import org.junit.jupiter.api.DisplayName;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/** Port of tests/page/page-emulate-media.spec.ts. */
public class TestPageEmulateMedia {
  // Java: the custom matcher toMatchMedia of the upstream file, as a helper
  private static boolean matchesMedia(Page page, String mediaQuery) {
    try {
      return Boolean.TRUE.equals(page.evaluate("mediaQuery => matchMedia(mediaQuery).matches", mediaQuery));
    } catch (RuntimeException e) {
      return false;
    }
  }

  private static void expectToMatchMedia(Page page, String mediaQuery) {
    if (!matchesMedia(page, mediaQuery)) {
      fail("Expected \"" + mediaQuery + "\" to match, but it did not");
    }
  }

  private static void expectNotToMatchMedia(Page page, String mediaQuery) {
    if (matchesMedia(page, mediaQuery)) {
      fail("Expected \"" + mediaQuery + "\" not to match, but it did");
    }
  }

  @PlaywrightTest
  @DisplayName("EM-001 should emulate type @smoke")
  void shouldEmulateType(Page page) {
    assertEquals(true, page.evaluate("() => matchMedia('screen').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('print').matches"));
    page.emulateMedia(new Page.EmulateMediaOptions().setMedia(Media.PRINT));
    assertEquals(false, page.evaluate("() => matchMedia('screen').matches"));
    assertEquals(true, page.evaluate("() => matchMedia('print').matches"));
    page.emulateMedia(new Page.EmulateMediaOptions());
    assertEquals(false, page.evaluate("() => matchMedia('screen').matches"));
    assertEquals(true, page.evaluate("() => matchMedia('print').matches"));
    page.emulateMedia(new Page.EmulateMediaOptions().setMedia(null));
    assertEquals(true, page.evaluate("() => matchMedia('screen').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('print').matches"));
  }

  @PlaywrightTest
  @DisplayName("EM-003 should emulate colorScheme should work @smoke")
  void shouldEmulateColorSchemeShouldWork(Page page) {
    page.emulateMedia(new Page.EmulateMediaOptions().setColorScheme(ColorScheme.LIGHT));
    assertEquals(true, page.evaluate("() => matchMedia('(prefers-color-scheme: light)').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('(prefers-color-scheme: dark)').matches"));
    page.emulateMedia(new Page.EmulateMediaOptions().setColorScheme(ColorScheme.DARK));
    assertEquals(true, page.evaluate("() => matchMedia('(prefers-color-scheme: dark)').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('(prefers-color-scheme: light)').matches"));
  }

  @PlaywrightTest
  @DisplayName("EM-004 should default to light")
  void shouldDefaultToLight(Page page) {
    assertEquals(true, page.evaluate("() => matchMedia('(prefers-color-scheme: light)').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('(prefers-color-scheme: dark)').matches"));

    page.emulateMedia(new Page.EmulateMediaOptions().setColorScheme(ColorScheme.DARK));
    assertEquals(true, page.evaluate("() => matchMedia('(prefers-color-scheme: dark)').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('(prefers-color-scheme: light)').matches"));

    page.emulateMedia(new Page.EmulateMediaOptions().setColorScheme(null));
    assertEquals(false, page.evaluate("() => matchMedia('(prefers-color-scheme: dark)').matches"));
    assertEquals(true, page.evaluate("() => matchMedia('(prefers-color-scheme: light)').matches"));
  }

  @PlaywrightTest
  @DisplayName("EM-007 should change the actual colors in css")
  void shouldChangeTheActualColorsInCss(Page page) {
    page.setContent("\n" +
      "    <style>\n" +
      "      @media (prefers-color-scheme: dark) {\n" +
      "        div {\n" +
      "          background: black;\n" +
      "          color: white;\n" +
      "        }\n" +
      "      }\n" +
      "      @media (prefers-color-scheme: light) {\n" +
      "        div {\n" +
      "          background: white;\n" +
      "          color: black;\n" +
      "        }\n" +
      "      }\n" +
      "\n" +
      "    </style>\n" +
      "    <div>Hello</div>\n" +
      "  ");

    page.emulateMedia(new Page.EmulateMediaOptions().setColorScheme(ColorScheme.LIGHT));
    assertEquals("rgb(255, 255, 255)", getBackgroundColor(page));

    page.emulateMedia(new Page.EmulateMediaOptions().setColorScheme(ColorScheme.DARK));
    assertEquals("rgb(0, 0, 0)", getBackgroundColor(page));

    page.emulateMedia(new Page.EmulateMediaOptions().setColorScheme(ColorScheme.LIGHT));
    assertEquals("rgb(255, 255, 255)", getBackgroundColor(page));
  }

  private static Object getBackgroundColor(Page page) {
    return page.evalOnSelector("div", "div => window.getComputedStyle(div).backgroundColor");
  }

  @PlaywrightTest
  @DisplayName("EM-008 should emulate reduced motion")
  void shouldEmulateReducedMotion(Page page) {
    assertEquals(true, page.evaluate("() => matchMedia('(prefers-reduced-motion: no-preference)').matches"));
    page.emulateMedia(new Page.EmulateMediaOptions().setReducedMotion(ReducedMotion.REDUCE));
    assertEquals(true, page.evaluate("() => matchMedia('(prefers-reduced-motion: reduce)').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('(prefers-reduced-motion: no-preference)').matches"));
    page.emulateMedia(new Page.EmulateMediaOptions().setReducedMotion(ReducedMotion.NO_PREFERENCE));
    assertEquals(false, page.evaluate("() => matchMedia('(prefers-reduced-motion: reduce)').matches"));
    assertEquals(true, page.evaluate("() => matchMedia('(prefers-reduced-motion: no-preference)').matches"));
    page.emulateMedia(new Page.EmulateMediaOptions().setReducedMotion(null));
  }

  @PlaywrightTest
  @DisplayName("EM-009 should keep reduced motion and color emulation after reload")
  void shouldKeepReducedMotionAndColorEmulationAfterReload(Page page, TestServer server) {
    // Pre-conditions
    assertEquals(false, page.evaluate("() => matchMedia('(prefers-reduced-motion: reduce)').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('(forced-colors: active)').matches"));

    // Emulation
    page.emulateMedia(new Page.EmulateMediaOptions().setForcedColors(ForcedColors.ACTIVE).setReducedMotion(ReducedMotion.REDUCE));
    assertEquals(true, page.evaluate("() => matchMedia('(prefers-reduced-motion: reduce)').matches"));
    assertEquals(true, page.evaluate("() => matchMedia('(forced-colors: active)').matches"));

    // Force CanonicalBrowsingContext replacement in Firefox.
    server.setRoute("/empty.html", (req, res) -> {
      res.setHeader("Cross-Origin-Opener-Policy", "same-origin");
      // Note: without 'onload', Firefox sometimes does not fire the load event
      // over the protocol. The reason is unclear.
      res.end("\n" +
        "      <div>Hello there!</div>\n" +
        "      <script>window.onload = () => console.log('onload')</script>\n" +
        "    ");
    });
    page.navigate(server.EMPTY_PAGE);

    assertEquals(true, page.evaluate("() => matchMedia('(prefers-reduced-motion: reduce)').matches"));
    assertEquals(true, page.evaluate("() => matchMedia('(forced-colors: active)').matches"));
  }

  @PlaywrightTest
  @DisplayName("EM-010 should emulate forcedColors ")
  void shouldEmulateForcedColors(Page page) {
    assertEquals(true, page.evaluate("() => matchMedia('(forced-colors: none)').matches"));
    page.emulateMedia(new Page.EmulateMediaOptions().setForcedColors(ForcedColors.NONE));
    assertEquals(true, page.evaluate("() => matchMedia('(forced-colors: none)').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('(forced-colors: active)').matches"));
    page.emulateMedia(new Page.EmulateMediaOptions().setForcedColors(ForcedColors.ACTIVE));
    assertEquals(false, page.evaluate("() => matchMedia('(forced-colors: none)').matches"));
    assertEquals(true, page.evaluate("() => matchMedia('(forced-colors: active)').matches"));
    page.emulateMedia(new Page.EmulateMediaOptions().setForcedColors(null));
    assertEquals(true, page.evaluate("() => matchMedia('(forced-colors: none)').matches"));
  }

  @PlaywrightTest
  @DisplayName("EM-011 should emulate contrast ")
  void shouldEmulateContrast(Page page) {
    expectToMatchMedia(page, "(prefers-contrast: no-preference)");
    page.emulateMedia(new Page.EmulateMediaOptions().setContrast(Contrast.NO_PREFERENCE));
    expectToMatchMedia(page, "(prefers-contrast: no-preference)");
    expectNotToMatchMedia(page, "(prefers-contrast: more)");
    page.emulateMedia(new Page.EmulateMediaOptions().setContrast(Contrast.MORE));
    expectNotToMatchMedia(page, "(prefers-contrast: no-preference)");
    expectToMatchMedia(page, "(prefers-contrast: more)");
    page.emulateMedia(new Page.EmulateMediaOptions().setContrast(null));
    expectToMatchMedia(page, "(prefers-contrast: no-preference)");
  }

  @PlaywrightTest
  @DisplayName("EM-012 should report hover and fine pointer for desktop")
  void shouldReportHoverAndFinePointerForDesktop(Page page) {
    expectToMatchMedia(page, "(hover: hover)");
    expectToMatchMedia(page, "(pointer: fine)");
  }
}
