/*
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
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.BoundingBox;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;

import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** Port of tests/page/elementhandle-bounding-box.spec.ts. */
public class TestElementhandleBoundingBox {
  // Java: BoundingBox has no equals(); its fields are compared as the object of toEqual.
  private static Map<String, Object> toMap(BoundingBox box) {
    return mapOf("x", box.x, "y", box.y, "width", box.width, "height", box.height);
  }

  // Java: the object that page.evaluate returns, with its numbers as doubles like BoundingBox.
  private static Map<String, Object> toMap(Map<String, Object> box) {
    return mapOf("x", ((Number) box.get("x")).doubleValue(), "y", ((Number) box.get("y")).doubleValue(),
      "width", ((Number) box.get("width")).doubleValue(), "height", ((Number) box.get("height")).doubleValue());
  }

  @PlaywrightTest
  @DisplayName("EH-001 should work")
  void shouldWork(Page page, TestServer server) {
    page.setViewportSize(500, 500);
    page.navigate(server.PREFIX + "/grid.html");
    ElementHandle elementHandle = page.querySelector(".box:nth-of-type(13)");
    BoundingBox box = elementHandle.boundingBox();
    assertEquals(mapOf("x", 100.0, "y", 50.0, "width", 50.0, "height", 50.0), toMap(box));
  }

  @PlaywrightTest
  @DisplayName("EH-002 should handle nested frames")
  void shouldHandleNestedFrames(Page page, TestServer server) {
    page.setViewportSize(616, 500);
    page.navigate(server.PREFIX + "/frames/nested-frames.html");
    FrameLocator nestedFrame = page.frameLocator("[name=\"2frames\"]").frameLocator("[name=dos]");
    ElementHandle elementHandle = nestedFrame.locator("div").elementHandle();
    BoundingBox box = elementHandle.boundingBox();
    assertEquals(mapOf("x", 24.0, "y", 224.0, "width", 268.0, "height", 18.0), toMap(box));
  }

  @PlaywrightTest
  @DisplayName("EH-003 should get frame box")
  void shouldGetFrameBox(Page page) {
    page.setViewportSize(250, 250);
    page.setContent("<style>\n" +
      "  body {\n" +
      "      display: flex;\n" +
      "      height: 500px;\n" +
      "      margin: 0px;\n" +
      "  }\n" +
      "  body iframe {\n" +
      "      flex-shrink: 1;\n" +
      "      border: 0;\n" +
      "      background-color: green;\n" +
      "  }\n" +
      "  </style>\n" +
      "  <iframe></iframe>\n" +
      "  ");
    ElementHandle elementHandle = page.querySelector("iframe");
    BoundingBox box = elementHandle.boundingBox();
    assertEquals(mapOf("x", 0.0, "y", 0.0, "width", 300.0, "height", 500.0), toMap(box));
  }


  @PlaywrightTest
  @DisplayName("EH-004 should handle scroll offset and click")
  void shouldHandleScrollOffsetAndClick(Page page, TestServer server) {
    page.setContent("\n" +
      "    <style>* { margin: 0; padding: 0; }</style>\n" +
      "    <div style=\"width:8000px; height:8000px;\">\n" +
      "      <div id=target style=\"width:20px; height:20px; margin-left:230px; margin-top:340px;\"\n" +
      "        onclick=\"window.__clicked = true\">\n" +
      "      </div>\n" +
      "    </div>\n" +
      "  ");
    ElementHandle elementHandle = page.querySelector("#target");
    BoundingBox box1 = elementHandle.boundingBox();
    page.evaluate("() => window.scrollBy(200, 300)");
    BoundingBox box2 = elementHandle.boundingBox();
    assertEquals(mapOf("x", 230.0, "y", 340.0, "width", 20.0, "height", 20.0), toMap(box1));
    assertEquals(mapOf("x", 30.0, "y", 40.0, "width", 20.0, "height", 20.0), toMap(box2));
    page.mouse().click(box2.x + 10, box2.y + 10);
    assertEquals(true, page.evaluate("() => window['__clicked']"));
  }

  @PlaywrightTest
  @DisplayName("EH-005 should return null for invisible elements")
  void shouldReturnNullForInvisibleElements(Page page, TestServer server) {
    page.setContent("<div style=\"display:none\">hi</div>");
    ElementHandle element = page.querySelector("div");
    assertNull(element.boundingBox());
  }

  @PlaywrightTest
  @DisplayName("EH-006 should get bounding box of element inside a cross-origin iframe")
  @SuppressWarnings("unchecked")
  void shouldGetBoundingBoxOfElementInsideACrossOriginIframe(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.setContent("\n" +
      "    <div style=\"height:120px\"></div>\n" +
      "    <iframe style=\"border:0;margin-left:30px;width:300px;height:200px\" src=\"" + server.CROSS_PROCESS_PREFIX + "/input/button.html\"></iframe>\n" +
      "  ");
    Frame frame = page.frames().get(1);
    ElementHandle button = frame.waitForSelector("button");
    BoundingBox iframeBox = page.querySelector("iframe").boundingBox();
    Map<String, Object> inner = (Map<String, Object>) button.evaluate("b => { const r = b.getBoundingClientRect(); return { x: r.left, y: r.top }; }");
    BoundingBox box = button.boundingBox();
    assertEquals(Math.round(iframeBox.x + ((Number) inner.get("x")).doubleValue()), Math.round(box.x));
    assertEquals(Math.round(iframeBox.y + ((Number) inner.get("y")).doubleValue()), Math.round(box.y));
  }

  @PlaywrightTest
  @DisplayName("EH-007 should force a layout")
  void shouldForceALayout(Page page, TestServer server) {
    page.setViewportSize(500, 500);
    page.setContent("<div style=\"width: 100px; height: 100px\">hello</div>");
    ElementHandle elementHandle = page.querySelector("div");
    page.evaluate("element => element.style.height = '200px'", elementHandle);
    BoundingBox box = elementHandle.boundingBox();
    assertEquals(mapOf("x", 8.0, "y", 8.0, "width", 100.0, "height", 200.0), toMap(box));
  }

  @PlaywrightTest
  @DisplayName("EH-008 should work with SVG nodes")
  @SuppressWarnings("unchecked")
  void shouldWorkWithSVGNodes(Page page, TestServer server) {
    page.setContent("\n" +
      "      <svg xmlns=\"http://www.w3.org/2000/svg\" width=\"500\" height=\"500\">\n" +
      "        <rect id=\"theRect\" x=\"30\" y=\"50\" width=\"200\" height=\"300\"></rect>\n" +
      "      </svg>\n" +
      "    ");
    ElementHandle element = page.querySelector("#therect");
    BoundingBox pwBoundingBox = element.boundingBox();
    Map<String, Object> webBoundingBox = (Map<String, Object>) page.evaluate("e => {\n" +
      "  const rect = e.getBoundingClientRect();\n" +
      "  return { x: rect.x, y: rect.y, width: rect.width, height: rect.height };\n" +
      "}", element);
    assertEquals(toMap(pwBoundingBox), toMap(webBoundingBox));
  }

  @PlaywrightTest
  @DisplayName("EH-009 should work when inline box child is outside of viewport")
  @SuppressWarnings("unchecked")
  void shouldWorkWhenInlineBoxChildIsOutsideOfViewport(Page page, TestServer server) {
    page.setContent("\n" +
      "      <style>\n" +
      "      i {\n" +
      "        position: absolute;\n" +
      "        top: -1000px;\n" +
      "      }\n" +
      "      body {\n" +
      "        margin: 0;\n" +
      "        font-size: 12px;\n" +
      "      }\n" +
      "      </style>\n" +
      "      <span><i>woof</i><b>doggo</b></span>\n" +
      "    ");
    ElementHandle handle = page.querySelector("span");
    BoundingBox box = handle.boundingBox();
    Map<String, Object> webBoundingBox = (Map<String, Object>) handle.evaluate("e => {\n" +
      "  const rect = e.getBoundingClientRect();\n" +
      "  return { x: rect.x, y: rect.y, width: rect.width, height: rect.height };\n" +
      "}");
    assertEquals(round(toMap(box)), round(toMap(webBoundingBox)));
  }

  private static Map<String, Object> round(Map<String, Object> box) {
    return mapOf(
      "x", Math.round((Double) box.get("x") * 100),
      "y", Math.round((Double) box.get("y") * 100),
      "width", Math.round((Double) box.get("width") * 100),
      "height", Math.round((Double) box.get("height") * 100));
  }
}
