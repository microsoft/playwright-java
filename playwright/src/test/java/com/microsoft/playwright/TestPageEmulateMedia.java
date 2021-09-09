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

import com.microsoft.playwright.options.ForcedColors;
import com.microsoft.playwright.options.ReducedMotion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;

import java.util.function.Supplier;

import static com.microsoft.playwright.options.ColorScheme.DARK;
import static com.microsoft.playwright.options.ColorScheme.LIGHT;
import static com.microsoft.playwright.options.Media.PRINT;
import static com.microsoft.playwright.Utils.attachFrame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestPageEmulateMedia extends TestBase {
  @Test
  void shouldEmulateType() {
    assertEquals(true, page.evaluate("() => matchMedia('screen').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('print').matches"));
    page.emulateMedia(new Page.EmulateMediaOptions().setMedia(PRINT));
    assertEquals(false, page.evaluate("() => matchMedia('screen').matches"));
    assertEquals(true, page.evaluate("() => matchMedia('print').matches"));
    page.emulateMedia(new Page.EmulateMediaOptions());
    assertEquals(false, page.evaluate("() => matchMedia('screen').matches"));
    assertEquals(true, page.evaluate("() => matchMedia('print').matches"));
    page.emulateMedia(new Page.EmulateMediaOptions().setMedia(null));
    assertEquals(true, page.evaluate("() => matchMedia('screen').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('print').matches"));
  }

  void shouldThrowInCaseOfBadTypeArgument() {
    // Impossible in Java.
  }

  @Test
  void shouldEmulateSchemeWork() {
    page.emulateMedia(new Page.EmulateMediaOptions().setColorScheme(LIGHT));
    assertEquals(true, page.evaluate("() => matchMedia('(prefers-color-scheme: light)').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('(prefers-color-scheme: dark)').matches"));
    page.emulateMedia(new Page.EmulateMediaOptions().setColorScheme(DARK));
    assertEquals(true, page.evaluate("() => matchMedia('(prefers-color-scheme: dark)').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('(prefers-color-scheme: light)').matches"));
  }

  @Test
  void shouldDefaultToLight() {
    assertEquals(true, page.evaluate("() => matchMedia('(prefers-color-scheme: light)').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('(prefers-color-scheme: dark)').matches"));

    page.emulateMedia(new Page.EmulateMediaOptions().setColorScheme(DARK));
    assertEquals(true, page.evaluate("() => matchMedia('(prefers-color-scheme: dark)').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('(prefers-color-scheme: light)').matches"));

    page.emulateMedia(new Page.EmulateMediaOptions().setColorScheme(null));
    assertEquals(false, page.evaluate("() => matchMedia('(prefers-color-scheme: dark)').matches"));
    assertEquals(true, page.evaluate("() => matchMedia('(prefers-color-scheme: light)').matches"));
  }

  void shouldThrowInCaseOfBadArgument() {
    // Impossible in Java.
  }

//  @Test
  void shouldWorkDuringNavigation() {
    // TODO: requires async API
  }

  @Test
  void shouldWorkInPopup() {
    {
      BrowserContext context = browser.newContext(new Browser.NewContextOptions().setColorScheme(DARK));
      Page page = context.newPage();
      page.navigate(server.EMPTY_PAGE);
      Page popup = page.waitForPopup(() -> page.evaluate("url => { window.open(url); }", server.EMPTY_PAGE));
      assertEquals(false, popup.evaluate("() => matchMedia('(prefers-color-scheme: light)').matches"));
      assertEquals(true, popup.evaluate("() => matchMedia('(prefers-color-scheme: dark)').matches"));
      context.close();
    }
    {
      Page page = browser.newPage(new Browser.NewPageOptions().setColorScheme(LIGHT));
      page.navigate(server.EMPTY_PAGE);
      Page popup = page.waitForPopup(() -> page.evaluate("url => { window.open(url); }", server.EMPTY_PAGE));
      assertEquals(true, popup.evaluate("() => matchMedia('(prefers-color-scheme: light)').matches"));
      assertEquals(false, popup.evaluate("() => matchMedia('(prefers-color-scheme: dark)').matches"));
      page.close();
    }
  }

  @Test
  void shouldWorkInCrossProcessIframe() {
    Page page = browser.newPage(new Browser.NewPageOptions().setColorScheme(DARK));
    page.navigate(server.EMPTY_PAGE);
    attachFrame(page, "frame1", server.CROSS_PROCESS_PREFIX + "/empty.html");
    Frame frame = page.frames().get(1);
    assertEquals(true, frame.evaluate("() => matchMedia('(prefers-color-scheme: dark)').matches"));
    page.close();
  }

  @Test
  void shouldChangeTheActualColorsInCss() {
    page.setContent("<style>\n" +
      "@media (prefers-color-scheme: dark) {\n" +
      "  div {\n" +
      "    background: black;\n" +
      "    color: white;\n" +
      "  }\n" +
      "}\n" +
      "@media (prefers-color-scheme: light) {\n" +
      "  div {\n" +
      "    background: white;\n" +
      "    color: black;\n" +
      "  }\n" +
      "}\n" +
      "</style>\n" +
      "<div>Hello</div>");

    Supplier<Object> backgroundColor = () -> {
      return page.evalOnSelector("div", "div => window.getComputedStyle(div).backgroundColor");
    };

    page.emulateMedia(new Page.EmulateMediaOptions().setColorScheme(LIGHT));
    assertEquals("rgb(255, 255, 255)", backgroundColor.get());

    page.emulateMedia(new Page.EmulateMediaOptions().setColorScheme(DARK));
    assertEquals("rgb(0, 0, 0)", backgroundColor.get());

    page.emulateMedia(new Page.EmulateMediaOptions().setColorScheme(LIGHT));
    assertEquals("rgb(255, 255, 255)", backgroundColor.get());
  }

  @Test
  void shouldEmulateReducedMotion() {
    assertEquals(true, page.evaluate("() => matchMedia('(prefers-reduced-motion: no-preference)').matches"));
    page.emulateMedia(new Page.EmulateMediaOptions().setReducedMotion(ReducedMotion.REDUCE));
    assertEquals(true, page.evaluate("() => matchMedia('(prefers-reduced-motion: reduce)').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('(prefers-reduced-motion: no-preference)').matches"));
    page.emulateMedia(new Page.EmulateMediaOptions().setReducedMotion(ReducedMotion.NO_PREFERENCE));
    assertEquals(false, page.evaluate("() => matchMedia('(prefers-reduced-motion: reduce)').matches"));
    assertEquals(true, page.evaluate("() => matchMedia('(prefers-reduced-motion: no-preference)').matches"));
    page.emulateMedia(new Page.EmulateMediaOptions().setReducedMotion(null));
  }

  @Test
  @DisabledIf(value="com.microsoft.playwright.TestBase#isWebKit", disabledReason="https://bugs.webkit.org/show_bug.cgi?id=225281")
  void shouldEmulateForcedColors() {
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
}
