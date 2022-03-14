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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Paths;

import static com.microsoft.playwright.options.ScreenshotAnimations.DISABLED;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

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
    try {
      assertArrayEquals(buffer1, buffer2);
    } catch (AssertionFailedError e) {
      return;
    }
    fail("Screenshots are equal");
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
    try {
      assertArrayEquals(buffer1, buffer2);
    } catch (AssertionFailedError e) {
      return;
    }
    fail("Screenshots are equal");
  }

  @Test
  void maskShouldWork() {
    page.setViewportSize(500, 500);
    page.navigate(server.PREFIX + "/grid.html");
    byte[] screenshot = page.screenshot(new Page.ScreenshotOptions()
      .setMask(asList(page.locator("div").nth(5))));
    // TODO: toMatchSnapshot is not present in java, so we only checks that masked screenshot is different.
    byte[] originalScreenshot = page.screenshot();
    try {
      assertArrayEquals(screenshot, originalScreenshot);
    } catch (AssertionFailedError e) {
      return;
    }
    fail("Screenshots are equal");

  }
}
