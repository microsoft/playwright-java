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

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestInfo;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.Clip;
import com.microsoft.playwright.options.ScreenshotAnimations;
import com.microsoft.playwright.options.ScreenshotCaret;
import com.microsoft.playwright.options.ScreenshotType;
import com.microsoft.playwright.options.WaitUntilState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.microsoft.playwright.fixtures.Utils.attachFrame;
import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static com.microsoft.playwright.fixtures.Utils.rafraf;
import static com.microsoft.playwright.fixtures.Utils.verifyViewport;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/page/page-screenshot.spec.ts. */
public class TestPageScreenshot {
  @Nested
  @DisabledOnBrowser(value = BrowserName.FIREFOX, headed = true, reason = "Firefox headed produces a different image.")
  class PageScreenshot {
    @PlaywrightTest
    @DisplayName("SS-001 should work @smoke")
    void shouldWork(Page page, TestServer server, TestInfo testInfo) {
      page.setViewportSize(500, 500);
      page.navigate(server.PREFIX + "/grid.html");
      byte[] screenshot = page.screenshot();
      testInfo.expectToMatchSnapshot(screenshot, "screenshot-sanity.png");
    }

    @PlaywrightTest
    @DisplayName("SS-002 should not capture blinking caret by default")
    void shouldNotCaptureBlinkingCaretByDefault(Page page, TestServer server, BrowserName browserName) {
      page.setContent("\n" +
        "      <!-- Refer to stylesheet from other origin. Accessing this\n" +
        "           stylesheet rules will throw.\n" +
        "      -->\n" +
        "      <link rel=stylesheet href=\"" + server.CROSS_PROCESS_PREFIX + "/injectedstyle.css" + "\">\n" +
        "      <!-- make life harder: define caret color in stylesheet -->\n" +
        "      <style>\n" +
        "        div {\n" +
        "          caret-color: #000 !important;\n" +
        "        }\n" +
        "      </style>\n" +
        "      <div contenteditable=\"true\"></div>\n" +
        "    ");
      Locator div = page.locator("div");
      div.type("foo bar");
      byte[] screenshot = div.screenshot();
      for (int i = 0; i < 10; ++i) {
        // Caret blinking time is set to 500ms.
        // Try to capture variety of screenshots to make
        // sure we don't capture blinking caret.
        page.waitForTimeout(150);
        byte[] newScreenshot = div.screenshot();
        assertEquals(true, Arrays.equals(newScreenshot, screenshot));
      }
    }

    @PlaywrightTest
    @DisplayName("SS-003 should capture blinking caret if explicitly asked for")
    @DisabledOnBrowser(value = BrowserName.FIREFOX, reason = "browser-level screenshot API in firefox does not capture caret")
    void shouldCaptureBlinkingCaretIfExplicitlyAskedFor(Page page, TestServer server, BrowserName browserName) {
      page.setContent("\n" +
        "      <!-- Refer to stylesheet from other origin. Accessing this\n" +
        "           stylesheet rules will throw.\n" +
        "      -->\n" +
        "      <link rel=stylesheet href=\"" + server.CROSS_PROCESS_PREFIX + "/injectedstyle.css" + "\">\n" +
        "      <!-- make life harder: define caret color in stylesheet -->\n" +
        "      <style>\n" +
        "        div {\n" +
        "          caret-color: #000 !important;\n" +
        "        }\n" +
        "      </style>\n" +
        "      <div contenteditable=\"true\"></div>\n" +
        "    ");
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
      assertEquals(true, hasDifferentScreenshots);
    }

    @PlaywrightTest
    @DisplayName("SS-004 should capture blinking caret in shadow dom")
    void shouldCaptureBlinkingCaretInShadowDom(Page page, BrowserName browserName) {
      // it.info().annotations.push({ type: 'issue', description: 'https://github.com/microsoft/playwright/issues/16732' });
      page.addScriptTag(new Page.AddScriptTagOptions().setContent("\n" +
        "      class CustomElementContainer extends HTMLElement {\n" +
        "        #shadowRoot;\n" +
        "        constructor() {\n" +
        "          super();\n" +
        "          this.#shadowRoot = this.attachShadow({ mode: 'open' });\n" +
        "          this.#shadowRoot.innerHTML = '<custom-element-input-wrapper><input type=\"text\"/></custom-element-input-wrapper>';\n" +
        "        }\n" +
        "      }\n" +
        "      class CustomElementInputWrapper extends HTMLElement {\n" +
        "        #shadowRoot;\n" +
        "        constructor() {\n" +
        "          super();\n" +
        "          this.#shadowRoot = this.attachShadow({ mode: 'open' });\n" +
        "          this.#shadowRoot.innerHTML = '<style>:host { all: initial; }</style><slot/>';\n" +
        "        }\n" +
        "      }\n" +
        "      customElements.define('custom-element-input-wrapper', CustomElementInputWrapper);\n" +
        "      customElements.define('custom-element-container', CustomElementContainer);\n" +
        "\n" +
        "      const container = document.createElement('custom-element-container');\n" +
        "      document.body.appendChild(container);"));

      Locator input = page.locator("input");
      // TODO: click fails in webkit
      input.focus();

      byte[] screenshot = input.screenshot();
      boolean hasDifferentScreenshots = false;
      for (int i = 0; !hasDifferentScreenshots && i < 10; ++i) {
        // Caret blinking time is set to 500ms.
        // Try to capture variety of screenshots to make
        // sure we capture blinking caret.
        page.waitForTimeout(150);
        byte[] newScreenshot = input.screenshot(new Locator.ScreenshotOptions().setCaret(ScreenshotCaret.HIDE));
        hasDifferentScreenshots = !Arrays.equals(newScreenshot, screenshot);
      }
      assertEquals(false, hasDifferentScreenshots);
    }

    @PlaywrightTest
    @DisplayName("SS-005 should clip rect")
    void shouldClipRect(Page page, TestServer server, TestInfo testInfo) {
      page.setViewportSize(500, 500);
      page.navigate(server.PREFIX + "/grid.html");
      byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setClip(new Clip(50, 100, 150, 100)));
      testInfo.expectToMatchSnapshot(screenshot, "screenshot-clip-rect.png");
    }

    @PlaywrightTest
    @DisplayName("SS-006 should clip rect with fullPage")
    void shouldClipRectWithFullPage(Page page, TestServer server, TestInfo testInfo) {
      page.setViewportSize(500, 500);
      page.navigate(server.PREFIX + "/grid.html");
      page.evaluate("() => window.scrollBy(150, 200)");
      byte[] screenshot = page.screenshot(new Page.ScreenshotOptions()
        .setFullPage(true)
        .setClip(new Clip(50, 100, 150, 100)));
      testInfo.expectToMatchSnapshot(screenshot, "screenshot-clip-rect.png");
    }

    @PlaywrightTest
    @DisplayName("SS-007 should clip elements to the viewport")
    void shouldClipElementsToTheViewport(Page page, TestServer server, TestInfo testInfo) {
      page.setViewportSize(500, 500);
      page.navigate(server.PREFIX + "/grid.html");
      byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setClip(new Clip(50, 450, 1000, 100)));
      testInfo.expectToMatchSnapshot(screenshot, "screenshot-offscreen-clip.png");
    }

    @PlaywrightTest
    @DisplayName("SS-008 should throw on clip outside the viewport")
    void shouldThrowOnClipOutsideTheViewport(Page page, TestServer server) {
      page.setViewportSize(500, 500);
      page.navigate(server.PREFIX + "/grid.html");
      PlaywrightException screenshotError = assertThrows(PlaywrightException.class,
        () -> page.screenshot(new Page.ScreenshotOptions().setClip(new Clip(50, 650, 100, 100))));
      assertTrue(screenshotError.getMessage().contains("Clipped area is either empty or outside the resulting image"));
    }

    @PlaywrightTest
    @DisplayName("SS-009 should throw on a negative clip size")
    void shouldThrowOnANegativeClipSize(Page page, TestServer server) {
      page.setViewportSize(500, 500);
      page.navigate(server.PREFIX + "/grid.html");
      PlaywrightException screenshotError = assertThrows(PlaywrightException.class,
        () -> page.screenshot(new Page.ScreenshotOptions().setClip(new Clip(50, 50, -100, 100))));
      assertTrue(screenshotError.getMessage().contains("Expected options.clip.width to be greater than 0"));
    }

    // SS-010 should run in parallel: not ported, see upstream_not_ported.txt.

    @PlaywrightTest
    @DisplayName("SS-011 should take fullPage screenshots")
    void shouldTakeFullPageScreenshots(Page page, TestServer server, TestInfo testInfo) {
      page.setViewportSize(500, 500);
      page.navigate(server.PREFIX + "/grid.html");
      byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
      testInfo.expectToMatchSnapshot(screenshot, "screenshot-grid-fullpage.png");
    }

    @PlaywrightTest
    @DisplayName("SS-012 should take fullPage screenshots and mask elements outside of it")
    void shouldTakeFullPageScreenshotsAndMaskElementsOutsideOfIt(Page page, TestServer server, TestInfo testInfo) {
      // it.info().annotations.push({ type: 'issue', description: 'https://github.com/microsoft/playwright/issues/30770' });
      page.setViewportSize(500, 500);
      page.navigate(server.PREFIX + "/grid.html");
      byte[] screenshot = page.screenshot(new Page.ScreenshotOptions()
        .setFullPage(true)
        .setMask(asList(page.locator(".box").nth(144))));
      testInfo.expectToMatchSnapshot(screenshot, "screenshot-grid-fullpage-mask-outside-viewport.png");
    }

    @PlaywrightTest
    @DisplayName("SS-013 should restore viewport after fullPage screenshot")
    void shouldRestoreViewportAfterFullPageScreenshot(Page page, TestServer server) {
      page.setViewportSize(500, 500);
      page.navigate(server.PREFIX + "/grid.html");
      byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
      // Java: toBeInstanceOf(Buffer), a Buffer is a byte[].
      assertTrue(((Object) screenshot) instanceof byte[]);
      verifyViewport(page, 500, 500);
    }

    @PlaywrightTest
    @DisplayName("SS-014 should allow transparency")
    @DisabledOnBrowser(value = BrowserName.FIREFOX, reason = "fail: ")
    void shouldAllowTransparency(Page page, BrowserName browserName, TestInfo testInfo) {
      assumeFalse(Env.isBidi(), "fail: ");

      page.setViewportSize(300, 300);
      page.setContent("\n" +
        "      <style>\n" +
        "        body { margin: 0 }\n" +
        "        div { width: 300px; height: 100px; }\n" +
        "      </style>\n" +
        "      <div style=\"background:black\"></div>\n" +
        "      <div style=\"background:white\"></div>\n" +
        "      <div style=\"background:transparent\"></div>\n" +
        "    ");
      byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setOmitBackground(true));
      testInfo.expectToMatchSnapshot(screenshot, "transparent.png");
    }

    @PlaywrightTest
    @DisplayName("SS-015 should render white background on jpeg file")
    void shouldRenderWhiteBackgroundOnJpegFile(Page page, TestServer server, TestInfo testInfo) {
      page.setViewportSize(300, 300);
      page.navigate(server.EMPTY_PAGE);
      byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setOmitBackground(true).setType(ScreenshotType.JPEG));
      testInfo.expectToMatchSnapshot(screenshot, "white.jpg");
    }

    @PlaywrightTest
    @DisplayName("SS-016 should produce a valid webp screenshot")
    @Fixme("harness: TestInfo.expectToMatchSnapshot() decodes only png and jpeg images, the golden red.webp is not decoded")
    void shouldProduceAValidWebpScreenshot(Page page, TestServer server, TestInfo testInfo) {
      page.setViewportSize(300, 300);
      page.navigate(server.EMPTY_PAGE);
      page.evaluate("() => (document.body.style.background = 'rgb(255, 0, 0)')");
      byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setType(ScreenshotType.WEBP));
      testInfo.expectToMatchSnapshot(screenshot, "red.webp");
    }

    @PlaywrightTest
    @DisplayName("SS-017 path option should detect webp")
    @Fixme("harness: TestInfo.expectToMatchSnapshot() decodes only png and jpeg images, the golden red.webp is not decoded")
    void pathOptionShouldDetectWebp(Page page, TestServer server, TestInfo testInfo) throws IOException {
      page.setViewportSize(300, 300);
      page.navigate(server.EMPTY_PAGE);
      page.evaluate("() => (document.body.style.background = 'rgb(255, 0, 0)')");
      Path outputPath = testInfo.outputPath("screenshot.webp");
      byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setPath(outputPath));
      testInfo.expectToMatchSnapshot(Files.readAllBytes(outputPath), "red.webp");
      testInfo.expectToMatchSnapshot(screenshot, "red.webp");
    }

    @PlaywrightTest
    @DisplayName("SS-018 quality option should work for webp")
    void qualityOptionShouldWorkForWebp(Page page, TestServer server) {
      page.navigate(server.PREFIX + "/grid.html");
      byte[] lowQuality = page.screenshot(new Page.ScreenshotOptions().setType(ScreenshotType.WEBP).setQuality(0));
      byte[] highQuality = page.screenshot(new Page.ScreenshotOptions().setType(ScreenshotType.WEBP).setQuality(100));
      assertTrue(lowQuality.length < highQuality.length);
    }

    // SS-019 webp screenshots should be lossless by default: not ported, TODO harness: needs utils.isLosslessWebp

    // SS-020 should allow transparency with webp: not ported, TODO harness: needs utils.decodeWebp

    @PlaywrightTest
    @DisplayName("SS-021 quality option should throw for webp when out of range")
    void qualityOptionShouldThrowForWebpWhenOutOfRange(Page page) {
      PlaywrightException error = assertThrows(PlaywrightException.class,
        () -> page.screenshot(new Page.ScreenshotOptions().setType(ScreenshotType.WEBP).setQuality(101)));
      assertTrue(error.getMessage().contains("Expected options.quality to be between 0 and 100"));
    }

    @PlaywrightTest
    @DisplayName("SS-022 should work with odd clip size on Retina displays")
    void shouldWorkWithOddClipSizeOnRetinaDisplays(Page page, TestInfo testInfo) {
      byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setClip(new Clip(0, 0, 11, 11)));
      testInfo.expectToMatchSnapshot(screenshot, "screenshot-clip-odd-size.png");
    }

    @PlaywrightTest
    @DisplayName("SS-023 should work for canvas")
    void shouldWorkForCanvas(Page page, TestServer server, TestInfo testInfo) {
      page.setViewportSize(500, 500);
      page.navigate(server.PREFIX + "/screenshots/canvas.html");
      byte[] screenshot = page.screenshot();
      // Allow 4 corners to be rendered differently on various platforms/browsers.
      testInfo.expectToMatchSnapshot(screenshot, "screenshot-canvas.png", 4);
    }

    private void addLine(Page page, int step) {
      page.evaluate("n => {\n" +
        "        const canvas = document.querySelector('canvas');\n" +
        "        const ctx = canvas.getContext('2d');\n" +
        "        ctx.lineWidth = 1;\n" +
        "        ctx.beginPath();\n" +
        "        ctx.moveTo(0, n * 100);\n" +
        "        ctx.lineTo(300, n * 100);\n" +
        "        ctx.stroke();\n" +
        "      }", step);
    }

    @PlaywrightTest
    @DisplayName("SS-024 should capture canvas changes")
    void shouldCaptureCanvasChanges(Page page, TestInfo testInfo) {
      page.navigate("data:text/html,<canvas></canvas>");
      page.evaluate("() => {\n" +
        "      const canvas = document.querySelector('canvas');\n" +
        "      canvas.width = 600;\n" +
        "      canvas.height = 600;\n" +
        "    }");

      for (int i = 0; i < 3; i++) {
        addLine(page, i);
        // With the slight delay WebKit stops reflecting changes in the screenshots on macOS.
        page.waitForTimeout(100);
        byte[] screenshot = page.screenshot();
        testInfo.expectToMatchSnapshot(screenshot, "canvas-changes-" + i + ".png");
      }
    }

    @PlaywrightTest
    @DisplayName("SS-025 should work for webgl")
    void shouldWorkForWebgl(Page page, TestServer server, BrowserName browserName, TestInfo testInfo) {
      // Java: os.arch() === 'x64' is os.arch of the JVM, x86_64 or amd64.
      assumeFalse(browserName == BrowserName.WEBKIT && Env.platform().equals("darwin") && (System.getProperty("os.arch").equals("x86_64") || System.getProperty("os.arch").equals("amd64")),
        "WebGL is not available on Intel macOS - https://bugs.webkit.org/show_bug.cgi?id=278277");

      page.setViewportSize(640, 480);
      page.navigate(server.PREFIX + "/screenshots/webgl.html");
      byte[] screenshot = page.screenshot();
      testInfo.expectToMatchSnapshot(screenshot, "screenshot-webgl.png");
    }

    // SS-095 should capture webgl canvas after its drawing buffer is resized: not ported, TODO harness: needs comparePNGs

    @PlaywrightTest
    @DisplayName("SS-026 should work for translateZ")
    void shouldWorkForTranslateZ(Page page, TestServer server, TestInfo testInfo) {
      page.setViewportSize(500, 500);
      page.navigate(server.PREFIX + "/screenshots/translateZ.html");
      byte[] screenshot = page.screenshot();
      testInfo.expectToMatchSnapshot(screenshot, "screenshot-translateZ.png");
    }

    @PlaywrightTest
    @DisplayName("SS-027 should work while navigating")
    void shouldWorkWhileNavigating(Page page, TestServer server) {
      page.setViewportSize(500, 500);
      page.navigate(server.PREFIX + "/redirectloop1.html");
      for (int i = 0; i < 10; i++) {
        byte[] screenshot;
        try {
          screenshot = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
        } catch (PlaywrightException e) {
          if (e.getMessage().contains("Cannot take a screenshot while page is navigating"))
            screenshot = new byte[0];
          else
            throw e;
        }
        // Java: toBeInstanceOf(Buffer), a Buffer is a byte[].
        assertTrue(((Object) screenshot) instanceof byte[]);
      }
    }

    @PlaywrightTest
    @DisplayName("SS-028 should work with iframe in shadow")
    void shouldWorkWithIframeInShadow(Page page, TestServer server, TestInfo testInfo) {
      page.setViewportSize(500, 500);
      page.navigate(server.PREFIX + "/grid-iframe-in-shadow.html");
      testInfo.expectToMatchSnapshot(page.screenshot(), "screenshot-iframe.png");
    }

    @PlaywrightTest
    @DisplayName("SS-029 path option should work")
    void pathOptionShouldWork(Page page, TestServer server, TestInfo testInfo) throws IOException {
      page.setViewportSize(500, 500);
      page.navigate(server.PREFIX + "/grid.html");
      Path outputPath = testInfo.outputPath("screenshot.png");
      page.screenshot(new Page.ScreenshotOptions().setPath(outputPath));
      testInfo.expectToMatchSnapshot(Files.readAllBytes(outputPath), "screenshot-sanity.png");
    }

    @PlaywrightTest
    @DisplayName("SS-030 path option should create subdirectories")
    void pathOptionShouldCreateSubdirectories(Page page, TestServer server, TestInfo testInfo) throws IOException {
      page.setViewportSize(500, 500);
      page.navigate(server.PREFIX + "/grid.html");
      Path outputPath = testInfo.outputPath("these", "are", "directories", "screenshot.png");
      page.screenshot(new Page.ScreenshotOptions().setPath(outputPath));
      testInfo.expectToMatchSnapshot(Files.readAllBytes(outputPath), "screenshot-sanity.png");
    }

    @PlaywrightTest
    @DisplayName("SS-031 path option should detect jpeg")
    void pathOptionShouldDetectJpeg(Page page, TestServer server, TestInfo testInfo) throws IOException {
      // { annotation: { type: 'issue', description: 'https://github.com/microsoft/playwright/issues/43323' } }
      page.setViewportSize(300, 300);
      page.navigate(server.EMPTY_PAGE);
      Path outputPath = testInfo.outputPath("screenshot.jpg");
      byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setOmitBackground(true).setPath(outputPath));
      testInfo.expectToMatchSnapshot(Files.readAllBytes(outputPath), "white.jpg");
      testInfo.expectToMatchSnapshot(screenshot, "white.jpg");
      Path upperCasePath = testInfo.outputPath("screenshot.JPG");
      page.screenshot(new Page.ScreenshotOptions().setOmitBackground(true).setPath(upperCasePath));
      testInfo.expectToMatchSnapshot(Files.readAllBytes(upperCasePath), "white.jpg");
    }

    @PlaywrightTest
    @DisplayName("SS-032 path option should throw for unsupported mime type")
    void pathOptionShouldThrowForUnsupportedMimeType(Page page) {
      PlaywrightException error = assertThrows(PlaywrightException.class,
        () -> page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("file.txt"))));
      assertTrue(error.getMessage().contains("path: unsupported mime type \"text/plain\""));
    }

    @PlaywrightTest
    @DisplayName("SS-033 quality option should throw for png")
    void qualityOptionShouldThrowForPng(Page page) {
      PlaywrightException error = assertThrows(PlaywrightException.class,
        () -> page.screenshot(new Page.ScreenshotOptions().setQuality(10)));
      assertTrue(error.getMessage().contains("options.quality is unsupported for the png"));
    }

    @PlaywrightTest
    @DisplayName("SS-034 zero quality option should throw for png")
    void zeroQualityOptionShouldThrowForPng(Page page) {
      PlaywrightException error = assertThrows(PlaywrightException.class,
        () -> page.screenshot(new Page.ScreenshotOptions().setQuality(0).setType(ScreenshotType.PNG)));
      assertTrue(error.getMessage().contains("options.quality is unsupported for the png"));
    }

    @PlaywrightTest
    @DisplayName("SS-035 quality option should work for jpeg")
    void qualityOptionShouldWorkForJpeg(Page page, TestServer server) {
      page.navigate(server.PREFIX + "/grid.html");
      byte[] zeroQuality = page.screenshot(new Page.ScreenshotOptions().setType(ScreenshotType.JPEG).setQuality(0));
      byte[] highQuality = page.screenshot(new Page.ScreenshotOptions().setType(ScreenshotType.JPEG).setQuality(100));
      assertTrue(zeroQuality.length < highQuality.length);
    }

    @PlaywrightTest
    @DisplayName("SS-036 should prefer type over extension")
    void shouldPreferTypeOverExtension(Page page, TestInfo testInfo) {
      Path outputPath = testInfo.outputPath("file.png");
      byte[] buffer = page.screenshot(new Page.ScreenshotOptions().setPath(outputPath).setType(ScreenshotType.JPEG));
      // Java: bytes are signed, compare their unsigned values.
      assertEquals(asList(0xFF, 0xD8, 0xFF), asList(buffer[0] & 0xFF, buffer[1] & 0xFF, buffer[2] & 0xFF));
    }

    @PlaywrightTest
    @DisplayName("SS-037 should not issue resize event")
    void shouldNotIssueResizeEvent(Page page, TestServer server) {
      page.navigate(server.PREFIX + "/grid.html");
      AtomicBoolean resizeTriggered = new AtomicBoolean(false);
      page.exposeFunction("resize", args -> {
        resizeTriggered.set(true);
        return null;
      });
      page.evaluate("() => {\n" +
        "      window.addEventListener('resize', () => window.resize());\n" +
        "    }");
      page.screenshot();
      assertEquals(false, resizeTriggered.get());
    }

    @PlaywrightTest
    @DisplayName("SS-038 should work with Array deleted")
    void shouldWorkWithArrayDeleted(Page page, TestServer server, TestInfo testInfo) {
      page.setViewportSize(500, 500);
      page.navigate(server.PREFIX + "/grid.html");
      page.evaluate("() => delete window.Array");
      byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
      testInfo.expectToMatchSnapshot(screenshot, "screenshot-grid-fullpage.png");
    }

    // SS-039 should take fullPage screenshots during navigation: not ported, see upstream_not_ported.txt.

    @Nested
    class MaskOption {
      @PlaywrightTest
      @DisplayName("SS-040 should work")
      void shouldWork(Page page, TestServer server, TestInfo testInfo) {
        page.setViewportSize(500, 500);
        page.navigate(server.PREFIX + "/grid.html");
        testInfo.expectToMatchSnapshot(page.screenshot(new Page.ScreenshotOptions()
          .setMask(asList(page.locator("div").nth(5)))), "mask-should-work.png");
      }

      @PlaywrightTest
      @DisplayName("SS-041 should work with locator")
      void shouldWorkWithLocator(Page page, TestServer server, TestInfo testInfo) {
        page.setViewportSize(500, 500);
        page.navigate(server.PREFIX + "/grid.html");
        Locator bodyLocator = page.locator("body");
        testInfo.expectToMatchSnapshot(bodyLocator.screenshot(new Locator.ScreenshotOptions()
          .setMask(asList(page.locator("div").nth(5)))), "mask-should-work-with-locator.png");
      }

      @PlaywrightTest
      @DisplayName("SS-042 should work with elementhandle")
      void shouldWorkWithElementhandle(Page page, TestServer server, TestInfo testInfo) {
        page.setViewportSize(500, 500);
        page.navigate(server.PREFIX + "/grid.html");
        ElementHandle bodyHandle = page.querySelector("body");
        testInfo.expectToMatchSnapshot(bodyHandle.screenshot(new ElementHandle.ScreenshotOptions()
          .setMask(asList(page.locator("div").nth(5)))), "mask-should-work-with-elementhandle.png");
      }

      @PlaywrightTest
      @DisplayName("SS-043 should mask multiple elements")
      void shouldMaskMultipleElements(Page page, TestServer server, TestInfo testInfo) {
        page.setViewportSize(500, 500);
        page.navigate(server.PREFIX + "/grid.html");
        testInfo.expectToMatchSnapshot(page.screenshot(new Page.ScreenshotOptions()
          .setMask(asList(
            page.locator("div").nth(5),
            page.locator("div").nth(12)))), "should-mask-multiple-elements.png");
      }

      @PlaywrightTest
      @DisplayName("SS-044 should mask inside iframe")
      void shouldMaskInsideIframe(Page page, TestServer server, TestInfo testInfo) {
        page.setViewportSize(500, 500);
        page.navigate(server.PREFIX + "/grid.html");
        attachFrame(page, "frame1", server.PREFIX + "/grid.html");
        page.addStyleTag(new Page.AddStyleTagOptions().setContent("iframe { border: none; }"));
        testInfo.expectToMatchSnapshot(page.screenshot(new Page.ScreenshotOptions()
          .setMask(asList(
            page.locator("div").nth(5),
            page.frameLocator("#frame1").locator("div").nth(12)))), "should-mask-inside-iframe.png");
      }

      // SS-045 should mask in parallel: not ported, see upstream_not_ported.txt.

      @PlaywrightTest
      @DisplayName("SS-046 should remove mask after screenshot")
      void shouldRemoveMaskAfterScreenshot(Page page, TestServer server) {
        page.setViewportSize(500, 500);
        page.navigate(server.PREFIX + "/grid.html");
        byte[] screenshot1 = page.screenshot();
        page.screenshot(new Page.ScreenshotOptions()
          .setMask(asList(page.locator("div").nth(1))));
        byte[] screenshot2 = page.screenshot();
        assertEquals(true, Arrays.equals(screenshot1, screenshot2));
      }

      @PlaywrightTest
      @DisplayName("SS-047 should work when subframe has stalled navigation")
      void shouldWorkWhenSubframeHasStalledNavigation(Page page, TestServer server) throws Exception {
        // Java: the route handler runs the code that upstream runs after the pending setContent.
        page.route("**/subframe.html", route -> {
          page.screenshot(new Page.ScreenshotOptions().setMask(asList(page.locator("non-existent"))));
          route.fulfill(new Route.FulfillOptions().setBody(""));
        }); // Stalling subframe.

        page.navigate(server.EMPTY_PAGE);
        page.setContent("<iframe src='/subframe.html'></iframe>");
      }

      @PlaywrightTest
      @DisplayName("SS-048 should work when subframe used document.open after a weird url")
      void shouldWorkWhenSubframeUsedDocumentOpenAfterAWeirdUrl(Page page, TestServer server) {
        page.navigate(server.EMPTY_PAGE);
        page.evaluate("() => {\n" +
          "        const iframe = document.createElement('iframe');\n" +
          "        iframe.src = 'javascript:hi';\n" +
          "        document.body.appendChild(iframe);\n" +
          "        iframe.contentDocument.open();\n" +
          "        iframe.contentDocument.write('Hello');\n" +
          "        iframe.contentDocument.close();\n" +
          "      }");
        page.screenshot(new Page.ScreenshotOptions().setMask(asList(page.locator("non-existent"))));
      }

      @PlaywrightTest
      @DisplayName("SS-049 should work when mask color is not pink #F0F")
      void shouldWorkWhenMaskColorIsNotPinkF0F(Page page, TestServer server, TestInfo testInfo) {
        page.setViewportSize(500, 500);
        page.navigate(server.PREFIX + "/grid.html");
        testInfo.expectToMatchSnapshot(page.screenshot(new Page.ScreenshotOptions()
          .setMask(asList(page.locator("div").nth(5)))
          .setMaskColor("#00FF00")), "mask-color-should-work.png");
      }

      @PlaywrightTest
      @DisplayName("SS-050 should hide elements based on attr")
      void shouldHideElementsBasedOnAttr(Page page, TestServer server, TestInfo testInfo) {
        page.setViewportSize(500, 500);
        page.navigate(server.PREFIX + "/grid.html");
        page.locator("div").nth(5).evaluate("element => {\n" +
          "          element.setAttribute('data-test-screenshot', 'hide');\n" +
          "        }");
        testInfo.expectToMatchSnapshot(page.screenshot(new Page.ScreenshotOptions()
          .setStyle("[data-test-screenshot=\"hide\"] {\n" +
            "          visibility: hidden;\n" +
            "        }")), "hide-should-work.png");
        Object visibility = page.locator("div").nth(5).evaluate("element => element.style.visibility");
        assertEquals("", visibility);
      }

      @PlaywrightTest
      @DisplayName("SS-051 should remove elements based on attr")
      void shouldRemoveElementsBasedOnAttr(Page page, TestServer server, TestInfo testInfo) {
        page.setViewportSize(500, 500);
        page.navigate(server.PREFIX + "/grid.html");
        page.locator("div").nth(5).evaluate("element => {\n" +
          "          element.setAttribute('data-test-screenshot', 'remove');\n" +
          "        }");
        testInfo.expectToMatchSnapshot(page.screenshot(new Page.ScreenshotOptions()
          .setStyle("[data-test-screenshot=\"remove\"] {\n" +
            "          display: none;\n" +
            "        }")), "remove-should-work.png");
        Object display = page.locator("div").nth(5).evaluate("element => element.style.display");
        assertEquals("", display);
      }
    }
  }

  @Nested
  class PageScreenshotAnimations {
    @PlaywrightTest
    @DisplayName("SS-052 should not capture infinite css animation")
    void shouldNotCaptureInfiniteCssAnimation(Page page, TestServer server) {
      page.navigate(server.PREFIX + "/rotate-z.html");
      Locator div = page.locator("div");
      byte[] screenshot = div.screenshot(new Locator.ScreenshotOptions()
        .setAnimations(ScreenshotAnimations.DISABLED));
      for (int i = 0; i < 10; ++i) {
        rafraf(page);
        byte[] newScreenshot = div.screenshot(new Locator.ScreenshotOptions()
          .setAnimations(ScreenshotAnimations.DISABLED));
        assertEquals(true, Arrays.equals(newScreenshot, screenshot));
      }
    }

    @PlaywrightTest
    @DisplayName("SS-053 should not capture pseudo element css animation")
    void shouldNotCapturePseudoElementCssAnimation(Page page, TestServer server) {
      page.navigate(server.PREFIX + "/rotate-pseudo.html");
      Locator div = page.locator("div");
      byte[] screenshot = div.screenshot(new Locator.ScreenshotOptions()
        .setAnimations(ScreenshotAnimations.DISABLED));
      for (int i = 0; i < 10; ++i) {
        rafraf(page);
        byte[] newScreenshot = div.screenshot(new Locator.ScreenshotOptions()
          .setAnimations(ScreenshotAnimations.DISABLED));
        assertEquals(true, Arrays.equals(newScreenshot, screenshot));
      }
    }

    @PlaywrightTest
    @DisplayName("SS-054 should not capture css animations in shadow DOM")
    void shouldNotCaptureCssAnimationsInShadowDOM(Page page, TestServer server) {
      page.navigate(server.PREFIX + "/rotate-z-shadow-dom.html");
      byte[] screenshot = page.screenshot(new Page.ScreenshotOptions()
        .setAnimations(ScreenshotAnimations.DISABLED));
      for (int i = 0; i < 4; ++i) {
        rafraf(page);
        byte[] newScreenshot = page.screenshot(new Page.ScreenshotOptions()
          .setAnimations(ScreenshotAnimations.DISABLED));
        assertEquals(true, Arrays.equals(newScreenshot, screenshot));
      }
    }

    // SS-055 should stop animations that happen right before screenshot: not ported, uses the __testHookBeforeScreenshot option

    // SS-056 should resume infinite animations: not ported, TODO harness: needs comparePNGs

    // SS-057 should not capture infinite web animations: not ported, TODO harness: needs comparePNGs

    @PlaywrightTest
    @DisplayName("SS-058 should fire transitionend for finite transitions")
    void shouldFireTransitionendForFiniteTransitions(Page page, TestServer server, BrowserName browserName) {
      page.navigate(server.PREFIX + "/css-transition.html");
      Locator div = page.locator("div");
      div.evaluate("el => {\n" +
        "      el.addEventListener('transitionend', () => window['__TRANSITION_END'] = true, false);\n" +
        "    }");

      // it.step('make sure transition is actually running')
      {
        byte[] screenshot1 = page.screenshot();
        rafraf(page);
        byte[] screenshot2 = page.screenshot();
        assertEquals(false, Arrays.equals(screenshot1, screenshot2));
      }

      // Make a screenshot that finishes all finite animations.
      byte[] screenshot1 = div.screenshot(new Locator.ScreenshotOptions()
        .setAnimations(ScreenshotAnimations.DISABLED));
      rafraf(page);
      // Make sure finite transition is not restarted.
      byte[] screenshot2 = div.screenshot(new Locator.ScreenshotOptions().setAnimations(ScreenshotAnimations.ALLOW));
      assertEquals(true, Arrays.equals(screenshot1, screenshot2));

      assertEquals(true, page.evaluate("() => window['__TRANSITION_END']"));
    }

    @PlaywrightTest
    @DisplayName("SS-059 should capture screenshots after layoutchanges in transitionend event")
    void shouldCaptureScreenshotsAfterLayoutchangesInTransitionendEvent(Page page, TestServer server, BrowserName browserName) {
      page.navigate(server.PREFIX + "/css-transition.html");
      Locator div = page.locator("div");
      div.evaluate("el => {\n" +
        "      el.addEventListener('transitionend', () => {\n" +
        "        const time = window.builtins.Date.now();\n" +
        "        // Block main thread for 200ms, emulating heavy layout.\n" +
        "        while (window.builtins.Date.now() - time < 200) {}\n" +
        "        const h1 = document.createElement('h1');\n" +
        "        h1.textContent = 'woof-woof';\n" +
        "        document.body.append(h1);\n" +
        "      }, false);\n" +
        "    }");

      // it.step('make sure transition is actually running')
      {
        byte[] screenshot1 = page.screenshot();
        rafraf(page);
        byte[] screenshot2 = page.screenshot();
        assertEquals(false, Arrays.equals(screenshot1, screenshot2));
      }

      // 1. Make a screenshot that finishes all finite animations
      //    and triggers layout.
      byte[] screenshot1 = page.screenshot(new Page.ScreenshotOptions()
        .setAnimations(ScreenshotAnimations.DISABLED));

      // 2. Make a second screenshot after h1 is on screen.
      assertThat(page.locator("h1")).isVisible();
      assertThat(page.locator("h1")).hasText("woof-woof");
      byte[] screenshot2 = page.screenshot();

      // 3. Make sure both screenshots are equal, meaning that
      //    first screenshot actually was taken after transitionend
      //    changed layout.
      assertEquals(true, Arrays.equals(screenshot1, screenshot2));
    }

    @PlaywrightTest
    @DisplayName("SS-060 should not change animation with playbackRate equal to 0")
    void shouldNotChangeAnimationWithPlaybackRateEqualTo0(Page page, TestServer server) {
      page.navigate(server.PREFIX + "/rotate-z.html");
      page.evaluate("async () => {\n" +
        "      window.animation = document.getAnimations()[0];\n" +
        "      await window.animation.ready;\n" +
        "      window.animation.updatePlaybackRate(0);\n" +
        "      await window.animation.ready;\n" +
        "      window.animation.currentTime = 500;\n" +
        "    }");
      byte[] screenshot1 = page.screenshot(new Page.ScreenshotOptions()
        .setAnimations(ScreenshotAnimations.DISABLED));
      rafraf(page);
      byte[] screenshot2 = page.screenshot(new Page.ScreenshotOptions()
        .setAnimations(ScreenshotAnimations.DISABLED));
      assertEquals(true, Arrays.equals(screenshot1, screenshot2));
      assertEquals(mapOf(
        "playbackRate", 0,
        "currentTime", 500), page.evaluate("() => ({\n" +
        "      playbackRate: window.animation.playbackRate,\n" +
        "      currentTime: window.animation.currentTime,\n" +
        "    })"));
    }

    @PlaywrightTest
    @DisplayName("SS-061 should trigger particular events for css transitions")
    void shouldTriggerParticularEventsForCssTransitions(Page page, TestServer server) {
      page.navigate(server.PREFIX + "/css-transition.html");
      Locator div = page.locator("div");
      div.evaluate("async el => {\n" +
        "      window._EVENTS = [];\n" +
        "      el.addEventListener('transitionend', () => {\n" +
        "        window._EVENTS.push('transitionend');\n" +
        "        console.log('transitionend');\n" +
        "      }, false);\n" +
        "      const animation = el.getAnimations()[0];\n" +
        "      animation.oncancel = () => window._EVENTS.push('oncancel');\n" +
        "      animation.onfinish = () => window._EVENTS.push('onfinish');\n" +
        "      animation.onremove = () => window._EVENTS.push('onremove');\n" +
        "      await animation.ready;\n" +
        "    }");
      page.waitForConsoleMessage(new Page.WaitForConsoleMessageOptions().setPredicate(msg -> msg.text().equals("transitionend")),
        () -> page.screenshot(new Page.ScreenshotOptions().setAnimations(ScreenshotAnimations.DISABLED)));
      assertEquals(asList(
        "onfinish", "transitionend"), page.evaluate("() => window._EVENTS"));
    }

    @PlaywrightTest
    @DisplayName("SS-062 should trigger particular events for INfinite css animation")
    void shouldTriggerParticularEventsForINfiniteCssAnimation(Page page, TestServer server) {
      page.navigate(server.PREFIX + "/rotate-z.html");
      Locator div = page.locator("div");
      div.evaluate("async el => {\n" +
        "      window._EVENTS = [];\n" +
        "      el.addEventListener('animationcancel', () => {\n" +
        "        window._EVENTS.push('animationcancel');\n" +
        "        console.log('animationcancel');\n" +
        "      }, false);\n" +
        "      const animation = el.getAnimations()[0];\n" +
        "      animation.oncancel = () => window._EVENTS.push('oncancel');\n" +
        "      animation.onfinish = () => window._EVENTS.push('onfinish');\n" +
        "      animation.onremove = () => window._EVENTS.push('onremove');\n" +
        "      await animation.ready;\n" +
        "    }");
      page.waitForConsoleMessage(new Page.WaitForConsoleMessageOptions().setPredicate(msg -> msg.text().equals("animationcancel")),
        () -> page.screenshot(new Page.ScreenshotOptions().setAnimations(ScreenshotAnimations.DISABLED)));
      assertEquals(asList(
        "oncancel", "animationcancel"), page.evaluate("() => window._EVENTS"));
    }

    @PlaywrightTest
    @DisplayName("SS-063 should trigger particular events for finite css animation")
    void shouldTriggerParticularEventsForFiniteCssAnimation(Page page, TestServer server) {
      page.navigate(server.PREFIX + "/rotate-z.html");
      Locator div = page.locator("div");
      div.evaluate("async el => {\n" +
        "      window._EVENTS = [];\n" +
        "      // Make CSS animation to be finite.\n" +
        "      el.style.setProperty('animation-iteration-count', '1000');\n" +
        "      el.addEventListener('animationend', () => {\n" +
        "        window._EVENTS.push('animationend');\n" +
        "        console.log('animationend');\n" +
        "      }, false);\n" +
        "      const animation = el.getAnimations()[0];\n" +
        "      animation.oncancel = () => window._EVENTS.push('oncancel');\n" +
        "      animation.onfinish = () => window._EVENTS.push('onfinish');\n" +
        "      animation.onremove = () => window._EVENTS.push('onremove');\n" +
        "      await animation.ready;\n" +
        "    }");
      // Ensure CSS animation is finite.
      assertEquals(true, div.evaluate("async el => Number.isFinite(el.getAnimations()[0].effect.getComputedTiming().endTime)"));
      page.waitForConsoleMessage(new Page.WaitForConsoleMessageOptions().setPredicate(msg -> msg.text().equals("animationend")),
        () -> page.screenshot(new Page.ScreenshotOptions().setAnimations(ScreenshotAnimations.DISABLED)));
      assertEquals(asList(
        "onfinish", "animationend"), page.evaluate("() => window._EVENTS"));
    }

    @PlaywrightTest
    @DisplayName("SS-064 should wait for fonts to load")
    void shouldWaitForFontsToLoad(Page page, TestServer server, TestInfo testInfo) throws Exception {
      assumeFalse(Env.isWindows, "fixme: This requires a windows-specific test expectations. https://github.com/microsoft/playwright/issues/12707");
      page.setViewportSize(500, 500);
      CompletableFuture<AbstractMap.SimpleEntry<TestServer.Request, TestServer.Response>> fontRequestPromise = new CompletableFuture<>();
      // Stall font loading.
      server.setRoute("/webfont/iconfont.woff2", (request, response) -> {
        fontRequestPromise.complete(new AbstractMap.SimpleEntry<>(request, response));
      });
      page.navigate(server.PREFIX + "/webfont/webfont.html", new Page.NavigateOptions()
        .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)); // 'load' will not happen if webfont is pending

      // Make sure screenshot times out while webfont is stalled.
      PlaywrightException error = assertThrows(PlaywrightException.class,
        () -> page.screenshot(new Page.ScreenshotOptions().setTimeout(200)));
      assertTrue(error.getMessage().contains("waiting for fonts to load..."));
      assertTrue(error.getMessage().contains("Timeout 200ms exceeded"));

      AbstractMap.SimpleEntry<TestServer.Request, TestServer.Response> fontRequest = fontRequestPromise.get();
      server.serveFile(fontRequest.getKey(), fontRequest.getValue());
      byte[] iconsScreenshot = page.screenshot();
      testInfo.expectToMatchSnapshot(iconsScreenshot, "screenshot-web-font.png", 50);
    }
  }

  @PlaywrightTest
  @DisplayName("SS-065 should throw if screenshot size is too large")
  void shouldThrowIfScreenshotSizeIsTooLarge(Page page, BrowserName browserName) {
    boolean isMac = Env.isMac;
    boolean isBidi = Env.isBidi();
    int maxSize = browserName == BrowserName.FIREFOX && isBidi ? 65535 : 32767;
    // it.info().annotations.push({ type: 'issue', description: 'https://github.com/microsoft/playwright/issues/16727' });
    {
      page.setContent("<style>body {margin: 0; padding: 0;}</style><div style='min-height: " + maxSize + "px; background: red;'></div>");
      byte[] result = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
      assertNotNull(result);
    }
    {
      page.setContent("<style>body {margin: 0; padding: 0;}</style><div style='min-height: " + (maxSize + 1) + "px; background: red;'></div>");
      PlaywrightException exception = null;
      try {
        page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
      } catch (PlaywrightException e) {
        exception = e;
      }
      if ((browserName == BrowserName.FIREFOX && !isBidi) || (browserName == BrowserName.WEBKIT && !isMac))
        assertTrue(exception.getMessage().contains("Cannot take screenshot larger than 32767"));
      else if (browserName == BrowserName.FIREFOX && isBidi)
        assertTrue(exception.getMessage().contains("65536 exceeds the maximum allowed screenshot height of 65535 pixels"));
    }
  }

  // SS-066 page screenshot should capture css transform: not ported, expect(page).toHaveScreenshot() is not in the Java API

  // SS-067 should capture css box-shadow: not ported, expect(page).toHaveScreenshot() is not in the Java API
}
