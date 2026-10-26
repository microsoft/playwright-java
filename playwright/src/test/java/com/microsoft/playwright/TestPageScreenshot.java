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

import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.options.Clip;
import com.microsoft.playwright.options.ScreenshotAnimations;
import com.microsoft.playwright.options.ScreenshotCaret;
import com.microsoft.playwright.options.ScreenshotScale;
import com.microsoft.playwright.options.ScreenshotType;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.io.TempDir;
import org.opentest4j.AssertionFailedError;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static com.microsoft.playwright.fixtures.BrowserName.CHROMIUM;
import static com.microsoft.playwright.fixtures.BrowserName.FIREFOX;
import static com.microsoft.playwright.fixtures.BrowserName.WEBKIT;
import static com.microsoft.playwright.options.ScreenshotAnimations.DISABLED;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

// TODO: suite.skip(browserName === "firefox" && headed");
public class TestPageScreenshot {
  @PlaywrightTest
  void shouldWork(Page page, Server server) throws IOException {
    page.setViewportSize(500, 500);
    page.navigate(server.PREFIX + "/grid.html");
    byte[] screenshot = page.screenshot();
    BufferedImage image = ImageIO.read(new ByteArrayInputStream(screenshot));
    assertEquals(500, image.getWidth());
    assertEquals(500, image.getHeight());
//    expect(screenshot).toMatchSnapshot("screenshot-sanity.png");
  }

  @PlaywrightTest
  void shouldClipRect(Page page, Server server) throws IOException {
    page.setViewportSize(500, 500);
    page.navigate(server.PREFIX + "/grid.html");
    byte[] screenshot = page.screenshot(new Page.ScreenshotOptions()
      .setClip(new Clip(50, 100, 150, 100)));
    BufferedImage image = ImageIO.read(new ByteArrayInputStream(screenshot));
    assertEquals(150, image.getWidth());
    assertEquals(100, image.getHeight());
//    expect(screenshot).toMatchSnapshot("screenshot-clip-rect.png");
  }

  private static void assertWebp(byte[] screenshot) {
    // WebP magic: "RIFF" at offset 0, "WEBP" at offset 8.
    assertTrue(screenshot.length > 12);
    assertEquals("RIFF", new String(screenshot, 0, 4, java.nio.charset.StandardCharsets.US_ASCII));
    assertEquals("WEBP", new String(screenshot, 8, 4, java.nio.charset.StandardCharsets.US_ASCII));
  }

  @PlaywrightTest
  void shouldProduceAValidWebpScreenshot(Page page, Server server) {
    page.setViewportSize(300, 300);
    page.navigate(server.EMPTY_PAGE);
    byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setType(ScreenshotType.WEBP));
    assertWebp(screenshot);
  }

  @PlaywrightTest
  void pathOptionShouldDetectWebp(Page page, Server server, @TempDir Path tmpDir) throws IOException {
    page.setViewportSize(300, 300);
    page.navigate(server.EMPTY_PAGE);
    Path outputPath = tmpDir.resolve("screenshot.webp");
    byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setPath(outputPath));
    assertWebp(screenshot);
    assertWebp(Files.readAllBytes(outputPath));
  }

  @PlaywrightTest
  void qualityOptionShouldWorkForWebp(Page page, Server server) {
    page.navigate(server.PREFIX + "/grid.html");
    byte[] lowQuality = page.screenshot(new Page.ScreenshotOptions().setType(ScreenshotType.WEBP).setQuality(0));
    byte[] highQuality = page.screenshot(new Page.ScreenshotOptions().setType(ScreenshotType.WEBP).setQuality(100));
    assertTrue(lowQuality.length < highQuality.length);
  }

  static private void rafraf(Page page) {
    // Do a double raf since single raf does not
    // actually guarantee a new animation frame.
    page.evaluate("() => new Promise(x => {\n" +
      "      requestAnimationFrame(() => requestAnimationFrame(x));\n" +
      "  })");
  }

  @PlaywrightTest
  void shouldNotCaptureInfiniteCssAnimation(Page page, Server server) {
    page.navigate(server.PREFIX + "/rotate-z.html");
    Locator div = page.locator("div");
    byte[] screenshot = div.screenshot(new Locator.ScreenshotOptions().setAnimations(DISABLED));
    for (int i = 0; i < 10; ++i) {
      rafraf(page);
      byte[] newScreenshot = div.screenshot(new Locator.ScreenshotOptions().setAnimations(DISABLED));
      assertArrayEquals(screenshot, newScreenshot);
    }
  }

  @PlaywrightTest
  void shouldNotCapturePseudoElementCssAnimation(Page page, Server server) {
    page.navigate(server.PREFIX + "/rotate-pseudo.html");
    Locator div = page.locator("div");
    byte[] screenshot = div.screenshot(new Locator.ScreenshotOptions().setAnimations(DISABLED));
    for (int i = 0; i < 10; ++i) {
      rafraf(page);
      byte[] newScreenshot = div.screenshot(new Locator.ScreenshotOptions().setAnimations(DISABLED));
      assertArrayEquals(screenshot, newScreenshot);
    }
  }

  @PlaywrightTest
  void shouldNotCaptureCssAnimationsInShadowDOM(Page page, Server server) {
    page.navigate(server.PREFIX + "/rotate-z-shadow-dom.html");
    byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setAnimations(DISABLED));
    for (int i = 0; i < 10; ++i) {
      rafraf(page);
      byte[] newScreenshot = page.screenshot(new Page.ScreenshotOptions().setAnimations(DISABLED));
      assertArrayEquals(screenshot, newScreenshot);
    }
  }

  @PlaywrightTest
  void shouldResumeInfiniteAnimations(Page page, Server server) {
    page.navigate(server.PREFIX + "/rotate-z.html");
    page.screenshot(new Page.ScreenshotOptions().setAnimations(DISABLED));
    byte[] buffer1 = page.screenshot();
    rafraf(page);
    byte[] buffer2 = page.screenshot();
    assertThrows(AssertionFailedError.class, () -> assertArrayEquals(buffer1, buffer2));
  }

  @PlaywrightTest
  void shouldNotCaptureInfiniteWebAnimations(Page page, Server server) {
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

  @PlaywrightTest
  void maskShouldWorkForPage(Page page, Server server) {
    page.setViewportSize(500, 500);
    page.navigate(server.PREFIX + "/grid.html");
    byte[] screenshot = page.screenshot(new Page.ScreenshotOptions()
      .setMask(asList(page.locator("div").nth(5))));
    // TODO: toMatchSnapshot is not present in java, so we only checks that masked screenshot is different.
    byte[] originalScreenshot = page.screenshot();
    assertThrows(AssertionFailedError.class, () -> assertArrayEquals(screenshot, originalScreenshot));
  }

  @PlaywrightTest
  void maskShouldWorkForLocator(Page page, Server server) {
    page.navigate(server.PREFIX + "/grid.html");
    Locator locatorToScreenshot = page.locator("div").first();
    byte[] screenshot = locatorToScreenshot.screenshot(new Locator.ScreenshotOptions()
      .setMask(asList(page.locator("img"))));
    // TODO: toMatchSnapshot is not present in java, so we only checks that masked screenshot is different.
    byte[] originalScreenshot = locatorToScreenshot.screenshot();
    assertThrows(AssertionFailedError.class, () -> assertArrayEquals(screenshot, originalScreenshot));
  }

  @PlaywrightTest
  void shouldWorkWithDeviceScaleFactorAndClip(Browser browser, Server server) {
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

  @PlaywrightTest
  void shouldWorkWithDeviceScaleFactorAndScaleCss(Browser browser, Server server) {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setViewportSize(320, 480).setDeviceScaleFactor(2));
    Page page = context.newPage();
    page.navigate(server.PREFIX + "/grid.html");
    byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setScale(ScreenshotScale.CSS));
    assertNotNull(screenshot);
    // TODO:
    // expect(screenshot).toMatchSnapshot("screenshot-device-scale-factor-css-size.png");
  }

  @PlaywrightTest
  void shouldWorkWithDeviceScaleFactorAndScaleDevice(Browser browser, Server server) {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setViewportSize(320, 480).setDeviceScaleFactor(2));
    Page page = context.newPage();
    page.navigate(server.PREFIX + "/grid.html");
    byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setScale(ScreenshotScale.DEVICE));
    assertNotNull(screenshot);
    // TODO:
    // expect(screenshot).toMatchSnapshot("screenshot-device-scale-factor-device-size.png");
  }

  @PlaywrightTest
  void shouldNotCaptureBlinkingCaretByDefault(Page page, Server server) {
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

  @PlaywrightTest
  @DisabledOnBrowser(value = FIREFOX, reason = "fixme")
  void shouldCaptureBlinkingCaretIfExplicitlyAskedFor(Page page, Server server) {
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

  @PlaywrightTest
  void shouldWorkWhenMaskColorIsNotPinkF0F(Page page, Server server) {
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

  static boolean isScreenshotTestDisabled(BrowserName browserName) {
    if (browserName == WEBKIT || browserName == CHROMIUM) {
      // Array lengths differ.
      return true;
    }
    if (Env.channel() != null) {
      // Stable builds may have different expectations.
      return true;
    }
    return false;
  }

  @PlaywrightTest
  void shouldHideElementsBasedOnAttr(Page page, BrowserType browserType, Server server, BrowserName browserName) throws IOException {
    Assumptions.assumeFalse(isScreenshotTestDisabled(browserName), "array lengths differ");
    page.setViewportSize(500, 500);
    page.navigate(server.PREFIX + "/grid.html");
    page.locator("div").nth(5).evaluate("element => {\n" +
      "  element.setAttribute('data-test-screenshot', 'hide');\n" +
      "}");
    byte[] actual = page.screenshot(new Page.ScreenshotOptions().setStyle("[data-test-screenshot=\"hide\"] {\n" +
      "          visibility: hidden;\n" +
      "        }"));
    assertArrayEquals(expectedScreenshot(browserType, "hide-should-work"), actual, "Screenshots should match");
    Object visibility = page.locator("div").nth(5).evaluate("element => element.style.visibility");
    assertEquals("", visibility);
  }

  @PlaywrightTest
  void shouldRemoveElementsBasedOnAttr(Page page, BrowserType browserType, Server server, BrowserName browserName) throws IOException {
    Assumptions.assumeFalse(isScreenshotTestDisabled(browserName), "array lengths differ");
    page.setViewportSize(500, 500);
    page.navigate(server.PREFIX + "/grid.html");
    page.locator("div").nth(5).evaluate("element => {\n" +
      "        element.setAttribute('data-test-screenshot', 'remove');\n" +
      "      }");
    byte[] actual = page.screenshot(new Page.ScreenshotOptions().setStyle("[data-test-screenshot=\"remove\"] {\n" +
      "          display: none;\n" +
      "        }"));
    assertArrayEquals(expectedScreenshot(browserType, "remove-should-work"), actual, "Screenshots should match");
    Object display = page.locator("div").nth(5).evaluate("element => element.style.display");
    assertEquals("", display);
  }

  private byte[] expectedScreenshot(BrowserType browserType, String name) throws IOException {
    Path path = Paths.get("src/test/resources/expectations");
    return Files.readAllBytes(path.resolve(name + "-" + browserType.name() + ".png"));
  }

  @PlaywrightTest
  void pathOptionShouldDetectJpeg(Page page, @TempDir Path tmpDir) throws IOException {
    Path outputPath = tmpDir.resolve("screenshot.jpg");
    byte[] buffer = page.screenshot(new Page.ScreenshotOptions().setPath(outputPath));
    assertEquals(asList(0xFF, 0xD8, 0xFF), asList(buffer[0] & 0xFF, buffer[1] & 0xFF, buffer[2] & 0xFF));
    assertArrayEquals(buffer, Files.readAllBytes(outputPath));
  }

  @PlaywrightTest
  void shouldPreferTypeOverPathExtension(Page page, @TempDir Path tmpDir) {
    Path outputPath = tmpDir.resolve("screenshot.png");
    byte[] buffer = page.screenshot(new Page.ScreenshotOptions().setPath(outputPath).setType(ScreenshotType.JPEG));
    assertEquals(asList(0xFF, 0xD8, 0xFF), asList(buffer[0] & 0xFF, buffer[1] & 0xFF, buffer[2] & 0xFF));
  }

  @PlaywrightTest
  void pathOptionShouldThrowForUnsupportedExtension(Page page, @TempDir Path tmpDir) {
    PlaywrightException e = assertThrows(PlaywrightException.class,
      () -> page.screenshot(new Page.ScreenshotOptions().setPath(tmpDir.resolve("screenshot.txt"))));
    assertTrue(e.getMessage().contains("path: unsupported mime type \"text/plain\""), e.getMessage());
    e = assertThrows(PlaywrightException.class,
      () -> page.locator("body").screenshot(new Locator.ScreenshotOptions().setPath(tmpDir.resolve("screenshot.txt"))));
    assertTrue(e.getMessage().contains("path: unsupported mime type \"text/plain\""), e.getMessage());
  }
}
