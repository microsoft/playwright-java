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
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestInfo;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.ScreenshotType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.microsoft.playwright.fixtures.Utils.async;
import static com.microsoft.playwright.fixtures.Utils.rafraf;
import static com.microsoft.playwright.fixtures.Utils.verifyViewport;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/elementhandle-screenshot.spec.ts. */
public class TestElementhandleScreenshot {
  @Nested
  @DisabledOnBrowser(value = BrowserName.FIREFOX, headed = true)
  class ElementScreenshot {
    @PlaywrightTest
    @DisplayName("EH-074 should work")
    void shouldWork(Page page, TestServer server, TestInfo testInfo) {
      page.setViewportSize(500, 500);
      page.navigate(server.PREFIX + "/grid.html");
      page.evaluate("() => window.scrollBy(50, 100)");
      ElementHandle elementHandle = page.querySelector(".box:nth-of-type(3)");
      byte[] screenshot = elementHandle.screenshot();
      testInfo.expectToMatchSnapshot(screenshot, "screenshot-element-bounding-box.png");
    }

    @PlaywrightTest
    @DisplayName("EH-076 should work when main world busts JSON.stringify")
    void shouldWorkWhenMainWorldBustsJSONStringify(Page page, TestServer server, TestInfo testInfo) {
      page.setViewportSize(500, 500);
      page.navigate(server.PREFIX + "/grid.html");
      page.evaluate("() => {\n" +
        "      window.scrollBy(50, 100);\n" +
        "      JSON.stringify = () => undefined;\n" +
        "    }");
      ElementHandle elementHandle = page.querySelector(".box:nth-of-type(3)");
      byte[] screenshot = elementHandle.screenshot();
      testInfo.expectToMatchSnapshot(screenshot, "screenshot-element-bounding-box.png");
    }

    @PlaywrightTest
    @DisplayName("EH-077 should take into account padding and border")
    @Fixme(value = "harness: the text 'oooo' of the first div overflows into the top border rows of div#d and is rendered differently from the golden (14 pixels differ in firefox, 11 in webkit; the local fonts differ from the ones of the golden files)", browsers = {BrowserName.FIREFOX, BrowserName.WEBKIT})
    void shouldTakeIntoAccountPaddingAndBorder(Page page, TestInfo testInfo) {
      page.setViewportSize(500, 500);
      page.setContent("\n" +
        "      <div style=\"height: 14px\">oooo</div>\n" +
        "      <style>div {\n" +
        "        border: 2px solid blue;\n" +
        "        background: green;\n" +
        "        width: 50px;\n" +
        "        height: 50px;\n" +
        "      }\n" +
        "      </style>\n" +
        "      <div id=\"d\"></div>\n" +
        "    ");
      ElementHandle elementHandle = page.querySelector("div#d");
      byte[] screenshot = elementHandle.screenshot();
      testInfo.expectToMatchSnapshot(screenshot, "screenshot-element-padding-border.png");
    }

    @PlaywrightTest
    @DisplayName("EH-078 should capture full element when larger than viewport in parallel")
    void shouldCaptureFullElementWhenLargerThanViewportInParallel(Page page, TestInfo testInfo) throws Exception {
      page.setViewportSize(500, 500);

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
      List<ElementHandle> elementHandles = page.querySelectorAll("div.to-screenshot");
      List<CompletableFuture<byte[]>> promises = new ArrayList<>();
      for (ElementHandle handle : elementHandles) {
        promises.add(async(() -> handle.screenshot()));
      }
      List<byte[]> screenshots = new ArrayList<>();
      for (CompletableFuture<byte[]> promise : promises) {
        screenshots.add(promise.get());
      }
      testInfo.expectToMatchSnapshot(screenshots.get(2), "screenshot-element-larger-than-viewport.png");

      verifyViewport(page, 500, 500);
    }

    @PlaywrightTest
    @DisplayName("EH-079 should capture full element when larger than viewport")
    void shouldCaptureFullElementWhenLargerThanViewport(Page page, TestInfo testInfo) {
      page.setViewportSize(500, 500);

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
      ElementHandle elementHandle = page.querySelector("div.to-screenshot");
      byte[] screenshot = elementHandle.screenshot();
      testInfo.expectToMatchSnapshot(screenshot, "screenshot-element-larger-than-viewport.png");

      verifyViewport(page, 500, 500);
    }

    @PlaywrightTest
    @DisplayName("EH-080 should scroll element into view")
    void shouldScrollElementIntoView(Page page, TestInfo testInfo) {
      page.setViewportSize(500, 500);
      page.setContent("\n" +
        "      <div style=\"height: 14px\">oooo</div>\n" +
        "      <style>div.above {\n" +
        "        border: 2px solid blue;\n" +
        "        background: red;\n" +
        "        height: 1500px;\n" +
        "      }\n" +
        "      div.to-screenshot {\n" +
        "        border: 2px solid blue;\n" +
        "        background: green;\n" +
        "        width: 50px;\n" +
        "        height: 50px;\n" +
        "      }\n" +
        "      </style>\n" +
        "      <div class=\"above\"></div>\n" +
        "      <div class=\"to-screenshot\"></div>\n" +
        "    ");
      ElementHandle elementHandle = page.querySelector("div.to-screenshot");
      byte[] screenshot = elementHandle.screenshot();
      testInfo.expectToMatchSnapshot(screenshot, "screenshot-element-scrolled-into-view.png");
    }

    @PlaywrightTest
    @DisplayName("EH-081 should scroll 15000px into view")
    void shouldScroll15000pxIntoView(Page page, TestInfo testInfo) {
      page.setViewportSize(500, 500);
      page.setContent("\n" +
        "      <div style=\"height: 14px\">oooo</div>\n" +
        "      <style>div.above {\n" +
        "        border: 2px solid blue;\n" +
        "        background: red;\n" +
        "        height: 15000px;\n" +
        "      }\n" +
        "      div.to-screenshot {\n" +
        "        border: 2px solid blue;\n" +
        "        background: green;\n" +
        "        width: 50px;\n" +
        "        height: 50px;\n" +
        "      }\n" +
        "      </style>\n" +
        "      <div class=\"above\"></div>\n" +
        "      <div class=\"to-screenshot\"></div>\n" +
        "    ");
      ElementHandle elementHandle = page.querySelector("div.to-screenshot");
      byte[] screenshot = elementHandle.screenshot();
      testInfo.expectToMatchSnapshot(screenshot, "screenshot-element-scrolled-into-view.png");
    }

    @PlaywrightTest
    @DisplayName("EH-082 should work with a rotated element")
    void shouldWorkWithARotatedElement(Page page, TestInfo testInfo) {
      page.setViewportSize(500, 500);
      page.setContent("<div style=\"position:absolute;\n" +
        "                                      top: 100px;\n" +
        "                                      left: 100px;\n" +
        "                                      width: 100px;\n" +
        "                                      height: 100px;\n" +
        "                                      background: green;\n" +
        "                                      transform: rotateZ(200deg);\">&nbsp;</div>");
      ElementHandle elementHandle = page.querySelector("div");
      byte[] screenshot = elementHandle.screenshot();
      testInfo.expectToMatchSnapshot(screenshot, "screenshot-element-rotate.png");
    }

    @PlaywrightTest
    @DisplayName("EH-083 should fail to screenshot a detached element")
    void shouldFailToScreenshotADetachedElement(Page page, TestServer server) {
      page.setContent("<h1>remove this</h1>");
      ElementHandle elementHandle = page.querySelector("h1");
      page.evaluate("element => element.remove()", elementHandle);
      PlaywrightException screenshotError = assertThrows(PlaywrightException.class, () -> elementHandle.screenshot());
      assertTrue(screenshotError.getMessage().contains("Element is not attached to the DOM"));
    }

    @PlaywrightTest
    @DisplayName("EH-084 should timeout waiting for visible")
    @Fixme("java: the message is \"Error { message='Timeout 3000ms exceeded. name='TimeoutError ...\" without the 'elementHandle.screenshot: ' prefix")
    void shouldTimeoutWaitingForVisible(Page page, TestServer server) {
      page.setContent("<div style=\"width: 50px; height: 0\"></div>");
      ElementHandle div = page.querySelector("div");
      PlaywrightException error = assertThrows(PlaywrightException.class,
        () -> div.screenshot(new ElementHandle.ScreenshotOptions().setTimeout(3000)));
      assertTrue(error.getMessage().contains("elementHandle.screenshot: Timeout 3000ms exceeded"));
      assertTrue(error.getMessage().contains("element is not visible"));
    }

    @PlaywrightTest
    @DisplayName("EH-085 should wait for visible")
    void shouldWaitForVisible(Page page, TestServer server, TestInfo testInfo) throws Exception {
      page.setViewportSize(500, 500);
      page.navigate(server.PREFIX + "/grid.html");
      page.evaluate("() => window.scrollBy(50, 100)");
      ElementHandle elementHandle = page.querySelector(".box:nth-of-type(3)");
      elementHandle.evaluate("e => e.style.visibility = 'hidden'");
      AtomicBoolean done = new AtomicBoolean(false);
      CompletableFuture<byte[]> promise = async(() -> {
        byte[] buffer = elementHandle.screenshot();
        done.set(true);
        return buffer;
      });
      rafraf(page, 10);
      assertFalse(done.get());
      elementHandle.evaluate("e => e.style.visibility = 'visible'");
      byte[] screenshot = promise.get();
      testInfo.expectToMatchSnapshot(screenshot, "screenshot-element-bounding-box.png");
    }

    @PlaywrightTest
    @DisplayName("EH-086 should work for an element with fractional dimensions")
    void shouldWorkForAnElementWithFractionalDimensions(Page page, TestInfo testInfo) {
      page.setContent("<div style=\"width:48.51px;height:19.8px;border:1px solid black;\"></div>");
      ElementHandle elementHandle = page.querySelector("div");
      byte[] screenshot = elementHandle.screenshot();
      testInfo.expectToMatchSnapshot(screenshot, "screenshot-element-fractional.png");
    }

    @PlaywrightTest
    @DisplayName("EH-087 should work for an element with an offset")
    void shouldWorkForAnElementWithAnOffset(Page page, TestInfo testInfo) {
      page.setContent("<div style=\"position:absolute; top: 10.3px; left: 20.4px;width:50.3px;height:20.2px;border:1px solid black;\"></div>");
      ElementHandle elementHandle = page.querySelector("div");
      byte[] screenshot = elementHandle.screenshot();
      testInfo.expectToMatchSnapshot(screenshot, "screenshot-element-fractional-offset.png");
    }

    @PlaywrightTest
    @DisplayName("EH-088 should wait for element to stop moving")
    void shouldWaitForElementToStopMoving(Page page, TestServer server, TestInfo testInfo) {
      page.setViewportSize(500, 500);
      page.navigate(server.PREFIX + "/grid.html");
      ElementHandle elementHandle = page.querySelector(".box:nth-of-type(3)");
      elementHandle.evaluate("e => e.classList.add('animation')");
      rafraf(page);
      byte[] screenshot = elementHandle.screenshot();
      testInfo.expectToMatchSnapshot(screenshot, "screenshot-element-bounding-box.png");
    }

    @PlaywrightTest
    @DisplayName("EH-089 should take screenshot of disabled button")
    void shouldTakeScreenshotOfDisabledButton(Page page) {
      page.setViewportSize(500, 500);
      page.setContent("<button disabled>Click me</button>");
      ElementHandle button = page.querySelector("button");
      Object screenshot = button.screenshot();
      assertTrue(screenshot instanceof byte[]);
    }

    @PlaywrightTest
    @DisplayName("EH-090 path option should create subdirectories")
    void pathOptionShouldCreateSubdirectories(Page page, TestServer server, TestInfo testInfo) throws Exception {
      page.setViewportSize(500, 500);
      page.navigate(server.PREFIX + "/grid.html");
      page.evaluate("() => window.scrollBy(50, 100)");
      ElementHandle elementHandle = page.querySelector(".box:nth-of-type(3)");
      Path outputPath = testInfo.outputPath("these", "are", "directories", "screenshot.png");
      elementHandle.screenshot(new ElementHandle.ScreenshotOptions().setPath(outputPath));
      testInfo.expectToMatchSnapshot(Files.readAllBytes(outputPath), "screenshot-element-bounding-box.png");
    }

    @PlaywrightTest
    @DisplayName("EH-091 should prefer type over extension")
    void shouldPreferTypeOverExtension(Page page, TestServer server, TestInfo testInfo) {
      page.setViewportSize(500, 500);
      page.navigate(server.PREFIX + "/grid.html");
      page.evaluate("() => window.scrollBy(50, 100)");
      ElementHandle elementHandle = page.querySelector(".box:nth-of-type(3)");
      Path outputPath = testInfo.outputPath("file.png");
      byte[] buffer = elementHandle.screenshot(new ElementHandle.ScreenshotOptions().setPath(outputPath).setType(ScreenshotType.JPEG));
      assertEquals(Arrays.asList(0xFF, 0xD8, 0xFF), Arrays.asList(buffer[0] & 0xFF, buffer[1] & 0xFF, buffer[2] & 0xFF));
    }

    @PlaywrightTest
    @DisplayName("EH-092 should not issue resize event")
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
      ElementHandle elementHandle = page.querySelector(".box:nth-of-type(3)");
      elementHandle.screenshot();
      assertFalse(resizeTriggered.get());
    }
  }
}
