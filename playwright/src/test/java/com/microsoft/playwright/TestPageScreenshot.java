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

import com.microsoft.playwright.options.Clip;
import com.microsoft.playwright.options.ScreenshotAnimations;
import com.microsoft.playwright.options.ScreenshotCaret;
import com.microsoft.playwright.options.ScreenshotScale;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.opentest4j.AssertionFailedError;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

import static com.microsoft.playwright.options.ScreenshotAnimations.DISABLED;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

// TODO: suite.skip(browserName === "firefox" && headful");
public class TestPageScreenshot extends TestBase {
  @Test
  void shouldWork() throws IOException {
    page.setViewportSize(500, 500);
    page.navigate(server.PREFIX + "/grid.html");
    byte[] screenshot = page.screenshot();
    BufferedImage image = ImageIO.read(new ByteArrayInputStream(screenshot));
    assertEquals(500, image.getWidth());
    assertEquals(500, image.getHeight());
//    expect(screenshot).toMatchSnapshot("screenshot-sanity.png");
  }

  @Test
  void shouldClipRect() throws IOException {
    page.setViewportSize(500, 500);
    page.navigate(server.PREFIX + "/grid.html");
    byte[] screenshot = page.screenshot(new Page.ScreenshotOptions()
      .setClip(new Clip(50, 100, 150, 100)));
    BufferedImage image = ImageIO.read(new ByteArrayInputStream(screenshot));
    assertEquals(150, image.getWidth());
    assertEquals(100, image.getHeight());
//    expect(screenshot).toMatchSnapshot("screenshot-clip-rect.png");
  }

  static private void rafraf(Page page) {
    // Do a double raf since single raf does not
    // actually guarantee a new animation frame.
    page.evaluate("() => new Promise(x => {\n" +
      "      requestAnimationFrame(() => requestAnimationFrame(x));\n" +
      "  })");
  }

  @Test
  void shouldNotCaptureInfiniteCssAnimation() {
    page.navigate(server.PREFIX + "/rotate-z.html");
    Locator div = page.locator("div");
    byte[] screenshot = div.screenshot(new Locator.ScreenshotOptions().setAnimations(DISABLED));
    for (int i = 0; i < 10; ++i) {
      rafraf(page);
      byte[] newScreenshot = div.screenshot(new Locator.ScreenshotOptions().setAnimations(DISABLED));
      assertArrayEquals(screenshot, newScreenshot);
    }
  }

  @Test
  void shouldNotCapturePseudoElementCssAnimation() {
    page.navigate(server.PREFIX + "/rotate-pseudo.html");
    Locator div = page.locator("div");
    byte[] screenshot = div.screenshot(new Locator.ScreenshotOptions().setAnimations(DISABLED));
    for (int i = 0; i < 10; ++i) {
      rafraf(page);
      byte[] newScreenshot = div.screenshot(new Locator.ScreenshotOptions().setAnimations(DISABLED));
      assertArrayEquals(screenshot, newScreenshot);
    }
  }

  @Test
  void shouldNotCaptureCssAnimationsInShadowDOM() {
    page.navigate(server.PREFIX + "/rotate-z-shadow-dom.html");
    byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setAnimations(DISABLED));
    for (int i = 0; i < 10; ++i) {
      rafraf(page);
      byte[] newScreenshot = page.screenshot(new Page.ScreenshotOptions().setAnimations(DISABLED));
      assertArrayEquals(screenshot, newScreenshot);
    }
  }

  @Test
  void shouldResumeInfiniteAnimations() {
    page.navigate(server.PREFIX + "/rotate-z.html");
    page.screenshot(new Page.ScreenshotOptions().setAnimations(DISABLED));
    byte[] buffer1 = page.screenshot();
    rafraf(page);
    byte[] buffer2 = page.screenshot();
    assertThrows(AssertionFailedError.class, () -> assertArrayEquals(buffer1, buffer2));
  }

  @Test
  void shouldNotCaptureInfiniteWebAnimations() {
    page.navigate(server.PREFIX + "/web-animation.html");
    Locator div = page.locator("div");
    byte[] screenshot = div.screenshot(new Locator.ScreenshotOptions().setAnimations(DISABLED));
    for (int i = 0; i < 10; ++i) {
      rafraf(page);
      byte[] newScreenshot = div.screenshot(new Locator.ScreenshotOptions().setAnimations(DISABLED));
      assertArrayEquals(screenshot, newScreenshot);
    }
    // Should resume infinite web animation.
    byte[] buffer1 = page.screenshot();
    rafraf(page);
    byte[] buffer2 = page.screenshot();
    assertThrows(AssertionFailedError.class, () -> assertArrayEquals(buffer1, buffer2));
  }

  @Test
  void maskShouldWork() {
    page.setViewportSize(500, 500);
    page.navigate(server.PREFIX + "/grid.html");
    byte[] screenshot = page.screenshot(new Page.ScreenshotOptions()
      .setMask(asList(page.locator("div").nth(5))));
    // TODO: toMatchSnapshot is not present in java, so we only checks that masked screenshot is different.
    byte[] originalScreenshot = page.screenshot();
    assertThrows(AssertionFailedError.class, () -> assertArrayEquals(screenshot, originalScreenshot));
  }

  @Test
  void shouldWorkWithDeviceScaleFactorAndClip() {
    try (BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setViewportSize(500, 500).setDeviceScaleFactor(3))) {
      Page page = context.newPage();
      page.navigate(server.PREFIX + "/grid.html");
      byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setClip(50, 100, 150, 100));
      assertNotNull(screenshot);
      // TODO:
      // expect(screenshot).toMatchSnapshot("screenshot-device-scale-factor-clip.png");
    }
  }

  @Test
  void shouldWorkWithDeviceScaleFactorAndScaleCss() {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setViewportSize(320, 480).setDeviceScaleFactor(2));
    Page page = context.newPage();
    page.navigate(server.PREFIX + "/grid.html");
    byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setScale(ScreenshotScale.CSS));
    assertNotNull(screenshot);
    // TODO:
    // expect(screenshot).toMatchSnapshot("screenshot-device-scale-factor-css-size.png");
  }

  @Test
  void shouldWorkWithDeviceScaleFactorAndScaleDevice() {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setViewportSize(320, 480).setDeviceScaleFactor(2));
    Page page = context.newPage();
    page.navigate(server.PREFIX + "/grid.html");
    byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setScale(ScreenshotScale.DEVICE));
    assertNotNull(screenshot);
    // TODO:
    // expect(screenshot).toMatchSnapshot("screenshot-device-scale-factor-device-size.png");
  }

  @Test
  void shouldNotCaptureBlinkingCaretByDefault() {
    page.setContent("<!-- Refer to stylesheet from other origin. Accessing this\n" +
      "           stylesheet rules will throw.\n" +
      "      -->\n" +
      "      <link rel=stylesheet href=\"" + server.CROSS_PROCESS_PREFIX + "/injectedstyle.css\">\n" +
      "      <!-- make life harder: define caret color in stylesheet -->\n" +
      "      <style>\n" +
      "        div {\n" +
      "          caret-color: #000 !important;\n" +
      "        }\n" +
      "      </style>\n" +
      "      <div contenteditable=\"true\"></div>\n");
    Locator div = page.locator("div");
    div.type("foo bar");
    byte[] screenshot = div.screenshot();
    for (int i = 0; i < 10; ++i) {
      // Caret blinking time is set to 500ms.
      // Try to capture variety of screenshots to make
      // sure we don"t capture blinking caret.
      page.waitForTimeout(150);
      byte[] newScreenshot = div.screenshot();
      assertArrayEquals(screenshot, newScreenshot);
    }
  }

  @Test
  @DisabledIf(value="com.microsoft.playwright.TestBase#isFirefox", disabledReason="fixme")
  void shouldCaptureBlinkingCaretIfExplicitlyAskedFor() {
    page.setContent("      <!-- Refer to stylesheet from other origin. Accessing this\n" +
      "           stylesheet rules will throw.\n" +
      "      -->\n" +
      "      <link rel=stylesheet href=\"" + server.CROSS_PROCESS_PREFIX + "/injectedstyle.css'}\">\n" +
      "      <!-- make life harder: define caret color in stylesheet -->\n" +
      "      <style>\n" +
      "        div {\n" +
      "          caret-color: #000 !important;\n" +
      "        }\n" +
      "      </style>\n" +
      "      <div contenteditable=\"true\"></div>\n");
    Locator div = page.locator("div");
    div.type("foo bar");
    byte[] screenshot = div.screenshot();
    boolean hasDifferentScreenshots = false;
    for (int i = 0; !hasDifferentScreenshots && i < 10; ++i) {
      // Caret blinking time is set to 500ms.
      // Try to capture variety of screenshots to make
      // sure we capture blinking caret.
      page.waitForTimeout(150);
      byte[] newScreenshot = div.screenshot(new Locator.ScreenshotOptions().setCaret(ScreenshotCaret.INITIAL));
      hasDifferentScreenshots = !Arrays.equals(newScreenshot, screenshot);
    }
    assertTrue(hasDifferentScreenshots);
  }

  @Test
  void shouldWorkWhenMaskColorIsNotPinkF0F() {
    page.setViewportSize(500, 500);
    page.navigate(server.PREFIX + "/grid.html");
    byte[] screenshot1 = page.screenshot(
      new Page.ScreenshotOptions()
        .setMask(asList(page.locator("div").nth(5)))
        .setMaskColor("#00FF00"));
    byte[] screenshot2 = page.screenshot(
      new Page.ScreenshotOptions()
        .setMask(asList(page.locator("div").nth(5))));
    assertThrows(AssertionError.class, () -> assertArrayEquals(screenshot1, screenshot2));
  }

}
