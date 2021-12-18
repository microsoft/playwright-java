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

import com.microsoft.playwright.options.BoundingBox;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TestElementHandleBoundingBox extends TestBase {

  static boolean isFirefoxHeadful() {
    return isFirefox() && isHeadful();
  }

  @Test
  @DisabledIf(value="isFirefoxHeadful", disabledReason="fail")
  void shouldWork() {
    page.setViewportSize(500, 500);
    page.navigate(server.PREFIX + "/grid.html");
    ElementHandle elementHandle = page.querySelector(".box:nth-of-type(13)");
    BoundingBox box = elementHandle.boundingBox();
    assertEquals(100, box.x);
    assertEquals(50, box.y);
    assertEquals(50, box.width);
    assertEquals(50, box.height);
  }

  @Test
  void shouldHandleNestedFrames() {
    page.setViewportSize(616, 500);
    page.navigate(server.PREFIX + "/frames/nested-frames.html");
    Frame nestedFrame = page.frame("dos");
    assertNotNull(nestedFrame);
    ElementHandle elementHandle = nestedFrame.querySelector("div");
    BoundingBox box = elementHandle.boundingBox();
    assertEquals(24, box.x);
    assertEquals(224, box.y);
    assertEquals(268, box.width);
    assertEquals(18, box.height);
  }

  @Test
  void shouldReturnNullForInvisibleElements() {
    page.setContent("<div style='display:none'>hi</div>");
    ElementHandle element = page.querySelector("div");
    assertNull(element.boundingBox());
  }

  @Test
  void shouldForceALayout() {
    page.setViewportSize(500, 500);
    page.setContent("<div style='width: 100px; height: 100px'>hello</div>");
    ElementHandle elementHandle = page.querySelector("div");
    page.evaluate("element => element.style.height = '200px'", elementHandle);
    BoundingBox box = elementHandle.boundingBox();
    assertEquals(box.x, 8);
    assertEquals(box.y, 8);
    assertEquals(box.width, 100);
    assertEquals(box.height, 200);
  }

  @Test
  void shouldWorkWithSVGNodes() {
    page.setContent("<svg xmlns='http://www.w3.org/2000/svg' width='500' height='500'>\n" +
      "<rect id='theRect' x='30' y='50' width='200' height='300'></rect>\n" +
      "</svg>");
    ElementHandle element = page.querySelector("#therect");
    BoundingBox pwBoundingBox = element.boundingBox();
    @SuppressWarnings("unchecked")
    Map<String, Integer> webBoundingBox = (Map<String, Integer>) page.evaluate("e => {\n" +
      "  const rect = e.getBoundingClientRect();\n" +
      "  return { x: rect.x, y: rect.y, width: rect.width, height: rect.height };\n" +
      "}", element);
    assertEquals(webBoundingBox.get("x").doubleValue(), pwBoundingBox.x);
    assertEquals(webBoundingBox.get("y").doubleValue(), pwBoundingBox.y);
    assertEquals(webBoundingBox.get("width").doubleValue(), pwBoundingBox.width);
    assertEquals(webBoundingBox.get("height").doubleValue(), pwBoundingBox.height);
  }

  @Test
  @DisabledIf(value="com.microsoft.playwright.TestBase#isFirefox", disabledReason="skip")
  void shouldWorkWithPageScale() {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setViewportSize(400, 400).setIsMobile(true));
    Page page = context.newPage();
    page.navigate(server.PREFIX + "/input/button.html");
    ElementHandle button = page.querySelector("button");
    button.evaluate("button => {\n" +
      "  document.body.style.margin = '0';\n" +
      "  button.style.borderWidth = '0';\n" +
      "  button.style.width = '200px';\n" +
      "  button.style.height = '20px';\n" +
      "  button.style.marginLeft = '17px';\n" +
      "  button.style.marginTop = '23px';\n" +
      "}");
    BoundingBox box = button.boundingBox();
    assertEquals(17 * 100, round(box.x * 100));
    assertEquals(23 * 100, round(box.y * 100));
    assertEquals(200 * 100, round(box.width * 100));
    assertEquals(20 * 100, round(box.height * 100));
    context.close();
  }

  static long round(Object o) {
    if (o instanceof Integer) {
      return ((Integer) o).longValue();
    }
    return Math.round((Double) o);
  }

  @Test
  void shouldWorkWhenInlineBoxChildIsOutsideOfViewport() {
    page.setContent("<style>\n" +
      "i {\n" +
      "  position: absolute;\n" +
      "  top: -1000px;\n" +
      "}\n" +
      "body {\n" +
      "  margin: 0;\n" +
      "  font-size: 12px;\n" +
      "}\n" +
      "</style>\n" +
      "<span><i>woof</i><b>doggo</b></span>");
    ElementHandle handle = page.querySelector("span");
    BoundingBox pwBoundingBox = handle.boundingBox();
    @SuppressWarnings("unchecked")
    Map<String, Object> webBoundingBox = (Map<String, Object>) handle.evaluate("e => {\n" +
      "  const rect = e.getBoundingClientRect();\n" +
      "  return { x: rect.x, y: rect.y, width: rect.width, height: rect.height };\n" +
      "}");

    assertEquals(round(webBoundingBox.get("x")), round(pwBoundingBox.x));
    assertEquals(round(webBoundingBox.get("y")), round(pwBoundingBox.y));
    assertEquals(round(webBoundingBox.get("width")), round(pwBoundingBox.width));
    assertEquals(round(webBoundingBox.get("height")), round(pwBoundingBox.height));
  }
}
