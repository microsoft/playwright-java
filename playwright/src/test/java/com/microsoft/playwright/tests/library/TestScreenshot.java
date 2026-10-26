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
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.ContextFactory;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.ImageComparator;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestInfo;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.ScreenshotScale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.microsoft.playwright.fixtures.Utils.verifyViewport;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/library/screenshot.spec.ts. */
public class TestScreenshot {
  // The pixel() helper of the tests: the RGBA of the pixel (x, y) of a decoded image.
  private static int[] pixel(ImageComparator.Image decoded, int x, int y) {
    return decoded.pixel(x, y);
  }

  private static final String WINDOW_SIZE = "() => ({ width: window.innerWidth * window.devicePixelRatio, height: window.innerHeight * window.devicePixelRatio })";
  private static final String BODY_SIZE = "() => ({ width: document.body.offsetWidth, height: document.body.offsetHeight })";

  @Nested
  @DisabledOnBrowser(value = BrowserName.FIREFOX, headed = true, reason = "Firefox headed produces a different image.")
  class PageScreenshot {
    @PlaywrightTest
    @DisplayName("SS-068 should run in parallel in multiple pages")
    void shouldRunInParallelInMultiplePages(TestServer server, ContextFactory contextFactory, TestInfo testInfo) {
      BrowserContext context = contextFactory.newContext();
      int N = 5;
      // Java: the pages are created one after another, Promise.all() has no counterpart in the blocking API.
      List<Page> pages = new ArrayList<>();
      for (int i = 0; i < N; i++) {
        Page page = context.newPage();
        page.navigate(server.PREFIX + "/grid.html");
        pages.add(page);
      }
      List<byte[]> screenshots = new ArrayList<>();
      for (int i = 0; i < N; ++i) {
        screenshots.add(pages.get(i).screenshot(new Page.ScreenshotOptions().setClip(50 * (i % 2), 0, 50, 50)));
      }
      for (int i = 0; i < N; ++i) {
        testInfo.expectToMatchSnapshot(screenshots.get(i), "grid-cell-" + (i % 2) + ".png");
      }
      for (Page page : pages) {
        page.close();
      }
    }

    @PlaywrightTest
    @DisplayName("SS-069 should work with a mobile viewport")
    @DisabledOnBrowser(BrowserName.FIREFOX)
    void shouldWorkWithAMobileViewport(Browser browser, TestServer server, BrowserName browserName, TestInfo testInfo) {
      BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(320, 480).setIsMobile(true));
      Page page = context.newPage();
      page.navigate(server.PREFIX + "/overflow.html");
      byte[] screenshot = page.screenshot();
      testInfo.expectToMatchSnapshot(screenshot, "screenshot-mobile.png");
      context.close();
    }

    @PlaywrightTest
    @DisplayName("SS-070 should work with a mobile viewport and clip")
    @DisabledOnBrowser(BrowserName.FIREFOX)
    void shouldWorkWithAMobileViewportAndClip(Browser browser, TestServer server, BrowserName browserName, TestInfo testInfo) {
      BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(320, 480).setIsMobile(true));
      Page page = context.newPage();
      page.navigate(server.PREFIX + "/overflow.html");
      byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setClip(10, 10, 100, 150));
      testInfo.expectToMatchSnapshot(screenshot, "screenshot-mobile-clip.png");
      context.close();
    }

    @PlaywrightTest
    @DisplayName("SS-071 should work with a mobile viewport and fullPage")
    @DisabledOnBrowser(BrowserName.FIREFOX)
    void shouldWorkWithAMobileViewportAndFullPage(Browser browser, TestServer server, BrowserName browserName, TestInfo testInfo) {
      BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(320, 480).setIsMobile(true));
      Page page = context.newPage();
      page.navigate(server.PREFIX + "/overflow-large.html");
      byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
      testInfo.expectToMatchSnapshot(screenshot, "screenshot-mobile-fullpage.png");
      context.close();
    }

    @PlaywrightTest
    @DisplayName("SS-072 should work with device scale factor")
    void shouldWorkWithDeviceScaleFactor(Browser browser, TestServer server, BrowserName browserName, TestInfo testInfo) {
      BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(320, 480).setDeviceScaleFactor(2));
      Page page = context.newPage();
      page.navigate(server.PREFIX + "/grid.html");
      byte[] screenshot = page.screenshot();
      testInfo.expectToMatchSnapshot(screenshot, "screenshot-device-scale-factor.png");
      context.close();
    }

    @PlaywrightTest
    @DisplayName("SS-073 should work with device scale factor and clip")
    void shouldWorkWithDeviceScaleFactorAndClip(Browser browser, TestServer server, TestInfo testInfo) {
      BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(500, 500).setDeviceScaleFactor(3));
      Page page = context.newPage();
      page.navigate(server.PREFIX + "/grid.html");
      byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setClip(50, 100, 150, 100));
      testInfo.expectToMatchSnapshot(screenshot, "screenshot-device-scale-factor-clip.png");
      context.close();
    }

    @PlaywrightTest
    @DisplayName("SS-074 should work with device scale factor and scale:css")
    void shouldWorkWithDeviceScaleFactorAndScaleCss(Browser browser, TestServer server, TestInfo testInfo) {
      BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(320, 480).setDeviceScaleFactor(2));
      Page page = context.newPage();
      page.navigate(server.PREFIX + "/grid.html");
      byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setScale(ScreenshotScale.CSS));
      testInfo.expectToMatchSnapshot(screenshot, "screenshot-device-scale-factor-css-size.png");
      context.close();
    }

    @PlaywrightTest
    @DisplayName("SS-075 should produce screenshot of correct size with scale:css and null viewport")
    void shouldProduceScreenshotOfCorrectSizeWithScaleCssAndNullViewport(Browser browser, TestServer server) {
      BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(null));
      Page page = context.newPage();
      page.navigate(server.PREFIX + "/grid.html");
      @SuppressWarnings("unchecked")
      List<Object> size = (List<Object>) page.evaluate("() => [window.innerWidth, window.innerHeight]");
      Object innerWidth = size.get(0);
      Object innerHeight = size.get(1);
      byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setScale(ScreenshotScale.CSS));
      ImageComparator.Image decoded = ImageComparator.decode(screenshot);
      assertEquals(innerWidth, decoded.width);
      assertEquals(innerHeight, decoded.height);
      context.close();
    }

    @PlaywrightTest
    @DisplayName("SS-076 should work with device scale factor, clip and scale:css")
    void shouldWorkWithDeviceScaleFactorClipAndScaleCss(Browser browser, TestServer server, TestInfo testInfo) {
      BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(500, 500).setDeviceScaleFactor(3));
      Page page = context.newPage();
      page.navigate(server.PREFIX + "/grid.html");
      byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setClip(50, 100, 150, 100).setScale(ScreenshotScale.CSS));
      testInfo.expectToMatchSnapshot(screenshot, "screenshot-device-scale-factor-clip-css-size.png");
      context.close();
    }

    @PlaywrightTest
    @DisplayName("SS-077 should throw if screenshot size is too large with device scale factor")
    void shouldThrowIfScreenshotSizeIsTooLargeWithDeviceScaleFactor(Browser browser, BrowserName browserName) {
      BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(500, 500).setDeviceScaleFactor(2));
      Page page = context.newPage();
      {
        page.setContent("<style>body {margin: 0; padding: 0;}</style><div style='min-height: 16383px; background: red;'></div>");
        byte[] result = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
        assertNotNull(result);
      }
      {
        page.setContent("<style>body {margin: 0; padding: 0;}</style><div style='min-height: 16384px; background: red;'></div>");
        PlaywrightException exception = null;
        try {
          page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
        } catch (PlaywrightException e) {
          exception = e;
        }
        if ((browserName == BrowserName.FIREFOX && !Env.isBidi()) || (browserName == BrowserName.WEBKIT && !Env.isMac)) {
          assertTrue(exception.getMessage().contains("Cannot take screenshot larger than 32767"));
        }

        byte[] image = page.screenshot(new Page.ScreenshotOptions().setFullPage(true).setScale(ScreenshotScale.CSS));
        assertNotNull(image);
      }
      context.close();
    }

    @PlaywrightTest
    @DisplayName("SS-078 should work with large size")
    void shouldWorkWithLargeSize(BrowserName browserName, ContextFactory contextFactory) {
      assumeFalse(browserName == BrowserName.CHROMIUM && Env.headed() && Env.isLinux, "fixme: Chromium has gpu problems on linux with large screenshots");

      BrowserContext context = contextFactory.newContext();
      Page page = context.newPage();

      page.setViewportSize(1280, 800);
      page.evaluate("() => {\n" +
        "  document.body.style.margin = '0';\n" +
        "  document.body.style.padding = '0';\n" +
        "  document.documentElement.style.margin = '0';\n" +
        "  document.documentElement.style.padding = '0';\n" +
        "  const div = document.createElement('div');\n" +
        "  div.style.width = '1250px';\n" +
        "  div.style.height = '8440px';\n" +
        "  div.style.background = 'linear-gradient(red, blue)';\n" +
        "  document.body.appendChild(div);\n" +
        "}");
      byte[] buffer = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
      ImageComparator.Image decoded = ImageComparator.decode(buffer);

      assertTrue(pixel(decoded, 0, 0)[0] > 128);
      assertTrue(pixel(decoded, 0, 0)[2] < 128);
      assertTrue(pixel(decoded, 0, 8339)[0] < 128);
      assertTrue(pixel(decoded, 0, 8339)[2] > 128);
    }

    @PlaywrightTest
    @DisplayName("SS-079 should handle vh units ")
    void shouldHandleVhUnits(ContextFactory contextFactory) {
      BrowserContext context = contextFactory.newContext();
      Page page = context.newPage();

      page.setViewportSize(800, 500);
      page.evaluate("() => {\n" +
        "  document.body.style.margin = '0';\n" +
        "  document.body.style.padding = '0';\n" +
        "  document.documentElement.style.margin = '0';\n" +
        "  document.documentElement.style.padding = '0';\n" +
        "  const div = document.createElement('div');\n" +
        "  div.style.width = '100%';\n" +
        "  div.style.borderTop = '100vh solid red';\n" +
        "  div.style.borderBottom = '100vh solid blue';\n" +
        "  document.body.appendChild(div);\n" +
        "}");
      byte[] buffer = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
      ImageComparator.Image decoded = ImageComparator.decode(buffer);

      assertTrue(pixel(decoded, 0, 0)[0] > 128);
      assertTrue(pixel(decoded, 0, 0)[2] < 128);
      assertTrue(pixel(decoded, 0, 999)[0] < 128);
      assertTrue(pixel(decoded, 0, 999)[2] > 128);
    }

    // SS-080 should not hang when event loop is blocked: not ported, see upstream_not_ported.txt.
  }

  @Nested
  @DisabledOnBrowser(value = BrowserName.FIREFOX, headed = true)
  class ElementScreenshot {
    @PlaywrightTest
    @DisplayName("SS-081 element screenshot should work with a mobile viewport")
    void elementScreenshotShouldWorkWithAMobileViewport(Browser browser, TestServer server, BrowserName browserName, TestInfo testInfo) {
      assumeFalse(browserName == BrowserName.FIREFOX && !Env.isBidi());

      BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(320, 480).setIsMobile(true));
      Page page = context.newPage();
      page.navigate(server.PREFIX + "/grid.html");
      page.evaluate("() => window.scrollBy(50, 100)");
      ElementHandle elementHandle = page.querySelector(".box:nth-of-type(3)");
      byte[] screenshot = elementHandle.screenshot();
      testInfo.expectToMatchSnapshot(screenshot, "screenshot-element-mobile.png");
      context.close();
    }

    @PlaywrightTest
    @DisplayName("SS-082 element screenshot should work with device scale factor")
    void elementScreenshotShouldWorkWithDeviceScaleFactor(Browser browser, TestServer server, BrowserName browserName, TestInfo testInfo) {
      assumeFalse(browserName == BrowserName.FIREFOX && !Env.isBidi());

      BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(320, 480).setDeviceScaleFactor(2));
      Page page = context.newPage();
      page.navigate(server.PREFIX + "/grid.html");
      page.evaluate("() => window.scrollBy(50, 100)");
      ElementHandle elementHandle = page.querySelector(".box:nth-of-type(3)");
      byte[] screenshot = elementHandle.screenshot();
      testInfo.expectToMatchSnapshot(screenshot, "screenshot-element-mobile-dsf.png");
      context.close();
    }

    @PlaywrightTest
    @DisplayName("SS-083 should take screenshots when default viewport is null")
    void shouldTakeScreenshotsWhenDefaultViewportIsNull(TestServer server, Browser browser) {
      BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(null));
      Page page = context.newPage();
      page.setContent("<div style='height: 10000px; background: red'></div>");
      @SuppressWarnings("unchecked")
      Map<String, Object> windowSize = (Map<String, Object>) page.evaluate(WINDOW_SIZE);
      @SuppressWarnings("unchecked")
      Map<String, Object> sizeBefore = (Map<String, Object>) page.evaluate(BODY_SIZE);

      byte[] screenshot = page.screenshot();
      assertTrue(screenshot instanceof byte[]);
      ImageComparator.Image decoded = ImageComparator.decode(screenshot);
      assertEquals(((Number) windowSize.get("width")).doubleValue(), (double) decoded.width);
      assertEquals(((Number) windowSize.get("height")).doubleValue(), (double) decoded.height);

      @SuppressWarnings("unchecked")
      Map<String, Object> sizeAfter = (Map<String, Object>) page.evaluate(BODY_SIZE);
      assertEquals(sizeBefore.get("width"), sizeAfter.get("width"));
      assertEquals(sizeBefore.get("height"), sizeAfter.get("height"));
      context.close();
    }

    @PlaywrightTest
    @DisplayName("SS-084 should take fullPage screenshots when default viewport is null")
    void shouldTakeFullPageScreenshotsWhenDefaultViewportIsNull(TestServer server, Browser browser) {
      BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(null));
      Page page = context.newPage();
      page.navigate(server.PREFIX + "/grid.html");
      @SuppressWarnings("unchecked")
      Map<String, Object> sizeBefore = (Map<String, Object>) page.evaluate(BODY_SIZE);
      byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
      assertTrue(screenshot instanceof byte[]);

      @SuppressWarnings("unchecked")
      Map<String, Object> sizeAfter = (Map<String, Object>) page.evaluate(BODY_SIZE);
      assertEquals(sizeBefore.get("width"), sizeAfter.get("width"));
      assertEquals(sizeBefore.get("height"), sizeAfter.get("height"));
      context.close();
    }

    @PlaywrightTest
    @DisplayName("SS-085 should restore default viewport after fullPage screenshot")
    void shouldRestoreDefaultViewportAfterFullPageScreenshot(Browser browser) {
      BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(456, 789));
      Page page = context.newPage();
      verifyViewport(page, 456, 789);
      byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
      assertTrue(screenshot instanceof byte[]);
      verifyViewport(page, 456, 789);
      context.close();
    }

    // SS-086 should restore viewport after page screenshot and exception: not ported, see upstream_not_ported.txt.

    // SS-087 should restore viewport after page screenshot and timeout: not ported, see upstream_not_ported.txt.

    @PlaywrightTest
    @DisplayName("SS-088 should take element screenshot when default viewport is null and restore back")
    void shouldTakeElementScreenshotWhenDefaultViewportIsNullAndRestoreBack(TestServer server, Browser browser) {
      BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(null));
      Page page = context.newPage();
      page.setContent("\n" +
        "      <div style=\"height: 14px\">oooo</div>\n" +
        "      <style>\n" +
        "      div.to-screenshot {\n" +
        "        border: 1px solid blue;\n" +
        "        width: 600px;\n" +
        "        height: 600px;\n" +
        "        margin-left: 50px;\n" +
        "      }\n" +
        "      ::-webkit-scrollbar{\n" +
        "        display: none;\n" +
        "      }\n" +
        "      </style>\n" +
        "      <div class=\"to-screenshot\"></div>\n" +
        "      <div class=\"to-screenshot\"></div>\n" +
        "      <div class=\"to-screenshot\"></div>\n" +
        "    ");
      @SuppressWarnings("unchecked")
      Map<String, Object> sizeBefore = (Map<String, Object>) page.evaluate(BODY_SIZE);
      ElementHandle elementHandle = page.querySelector("div.to-screenshot");
      byte[] screenshot = elementHandle.screenshot();
      assertTrue(screenshot instanceof byte[]);
      @SuppressWarnings("unchecked")
      Map<String, Object> sizeAfter = (Map<String, Object>) page.evaluate(BODY_SIZE);
      assertEquals(sizeBefore.get("width"), sizeAfter.get("width"));
      assertEquals(sizeBefore.get("height"), sizeAfter.get("height"));
      context.close();
    }

    // SS-089 should restore viewport after element screenshot and exception: not ported, see upstream_not_ported.txt.

    @PlaywrightTest
    @DisplayName("SS-090 element screenshots should handle vh units ")
    void elementScreenshotsShouldHandleVhUnits(ContextFactory contextFactory) {
      BrowserContext context = contextFactory.newContext();
      Page page = context.newPage();

      page.setViewportSize(800, 500);
      page.evaluate("() => {\n" +
        "  const div = document.createElement('div');\n" +
        "  div.style.width = '100%';\n" +
        "  div.style.borderTop = '100vh solid red';\n" +
        "  div.style.borderBottom = '100vh solid blue';\n" +
        "  document.body.appendChild(div);\n" +
        "}");
      ElementHandle elementHandle = page.querySelector("div");
      byte[] buffer = elementHandle.screenshot();
      ImageComparator.Image decoded = ImageComparator.decode(buffer);

      assertTrue(pixel(decoded, 0, 0)[0] > 128);
      assertTrue(pixel(decoded, 0, 0)[2] < 128);
      assertTrue(pixel(decoded, 0, 999)[0] < 128);
      assertTrue(pixel(decoded, 0, 999)[2] > 128);
    }

    @PlaywrightTest
    @DisplayName("SS-091 should work if the main resource hangs")
    @DisabledOnBrowser(value = BrowserName.CHROMIUM, reason = "https://github.com/microsoft/playwright/issues/9757")
    void shouldWorkIfTheMainResourceHangs(Browser browser, BrowserName browserName, TestServer server, TestInfo testInfo) {
      Page page = browser.newPage();
      server.setRoute("/slow", (req, res) -> {
        res.writeHead(200,
          "content-length", "4096",
          "content-type", "text/html");
      });
      try {
        try {
          page.navigate(server.PREFIX + "/slow", new Page.NavigateOptions().setTimeout(1000));
        } catch (PlaywrightException e) {
        }
        byte[] screenshot = page.screenshot();
        testInfo.expectToMatchSnapshot(screenshot, "hanging-main-resource.png");
      } finally {
        page.close();
      }
    }

    @PlaywrightTest
    @DisplayName("SS-092 should capture full element when larger than viewport with device scale factor")
    void shouldCaptureFullElementWhenLargerThanViewportWithDeviceScaleFactor(Browser browser, TestInfo testInfo) {
      BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(501, 501).setDeviceScaleFactor(2.5));
      Page page = context.newPage();
      page.setContent("\n" +
        "      <div style=\"height: 14px\">oooo</div>\n" +
        "      <style>\n" +
        "      div.to-screenshot {\n" +
        "        border: 4px solid red;\n" +
        "        box-sizing: border-box;\n" +
        "        width: 600px;\n" +
        "        height: 600px;\n" +
        "        margin-left: 50px;\n" +
        "        background: rgb(0, 100, 200);\n" +
        "      }\n" +
        "      ::-webkit-scrollbar{\n" +
        "        display: none;\n" +
        "      }\n" +
        "      </style>\n" +
        "      <div class=\"to-screenshot\"></div>\n" +
        "    ");
      byte[] screenshot = page.locator("div.to-screenshot").screenshot();
      testInfo.expectToMatchSnapshot(screenshot, "element-larger-than-viewport-dsf.png");
      context.close();
    }

    @PlaywrightTest
    @DisplayName("SS-093 should capture full element when larger than viewport with device scale factor and scale:css")
    void shouldCaptureFullElementWhenLargerThanViewportWithDeviceScaleFactorAndScaleCss(Browser browser, TestInfo testInfo) {
      BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(501, 501).setDeviceScaleFactor(2.5));
      Page page = context.newPage();
      page.setContent("\n" +
        "      <div style=\"height: 14px\">oooo</div>\n" +
        "      <style>\n" +
        "      div.to-screenshot {\n" +
        "        border: 4px solid red;\n" +
        "        box-sizing: border-box;\n" +
        "        width: 600px;\n" +
        "        height: 600px;\n" +
        "        margin-left: 50px;\n" +
        "        background: rgb(0, 100, 200);\n" +
        "      }\n" +
        "      ::-webkit-scrollbar{\n" +
        "        display: none;\n" +
        "      }\n" +
        "      </style>\n" +
        "      <div class=\"to-screenshot\"></div>\n" +
        "    ");
      byte[] screenshot = page.locator("div.to-screenshot").screenshot(new com.microsoft.playwright.Locator.ScreenshotOptions().setScale(ScreenshotScale.CSS));
      testInfo.expectToMatchSnapshot(screenshot, "element-larger-than-viewport-dsf-css-size.png");
      context.close();
    }

    @PlaywrightTest
    @DisplayName("SS-094 page screenshot should capture css transform with device pixels")
    @DisabledOnBrowser(value = BrowserName.WEBKIT, reason = "fixme: ")
    void pageScreenshotShouldCaptureCssTransformWithDevicePixels(Browser browser, BrowserName browserName, TestInfo testInfo) {
      BrowserContext context = browser.newContext(new Browser.NewContextOptions()
        .setViewportSize(500, 500)
        .setDeviceScaleFactor(3));
      Page page = context.newPage();
      page.setContent("\n" +
        "      <style>\n" +
        "      .container {\n" +
        "        width: 150px;\n" +
        "        height: 150px;\n" +
        "        margin: 75px 0 0 75px;\n" +
        "        border: none;\n" +
        "      }\n" +
        "\n" +
        "      .cube {\n" +
        "        width: 100%;\n" +
        "        height: 100%;\n" +
        "        perspective: 550px;\n" +
        "        perspective-origin: 150% 150%;\n" +
        "      }\n" +
        "\n" +
        "      .face {\n" +
        "        display: block;\n" +
        "        position: absolute;\n" +
        "        width: 100px;\n" +
        "        height: 100px;\n" +
        "        border: none;\n" +
        "      }\n" +
        "\n" +
        "      .right {\n" +
        "        background: rgba(196, 0, 0, 0.7);\n" +
        "        transform: rotateY(70deg);\n" +
        "      }\n" +
        "\n" +
        "      </style>\n" +
        "      <div class=\"container\">\n" +
        "        <div class=\"cube showbf\">\n" +
        "          <div class=\"face right\"></div>\n" +
        "        </div>\n" +
        "      </div>\n" +
        "    ");

      // Java: expect(page).toHaveScreenshot({ scale: 'device' }) compares the screenshot with the golden file of that assertion.
      testInfo.expectToMatchSnapshot(page.screenshot(new Page.ScreenshotOptions().setScale(ScreenshotScale.DEVICE)),
        "element-screenshot-SS-094-page-screenshot-should-capture-css-transform-with-device-pixels-1.png");
      context.close();
    }
  }
}
