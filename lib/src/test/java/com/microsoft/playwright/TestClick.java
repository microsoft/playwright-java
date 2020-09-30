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

package com.microsoft.playwright;

import org.junit.jupiter.api.*;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestClick {
  private static Playwright playwright;
  private static Server server;
  private Browser browser;
  private boolean isChromium;
  private BrowserContext context;
  private Page page;

  @BeforeAll
  static void createPlaywright() {
    playwright = Playwright.create();
  }

  @BeforeAll
  static void startServer() throws IOException {
    server = new Server(8907);
  }

  @AfterAll
  static void stopServer() throws IOException {
    server.stop();
    server = null;
  }

  @BeforeEach
  void setUp() {
//    BrowserType.LaunchOptions options = new BrowserType.LaunchOptions().withHeadless(false).withSlowMo(1000);
    BrowserType.LaunchOptions options = new BrowserType.LaunchOptions();
    browser = playwright.chromium().launch(options);
    isChromium = true;
    context = browser.newContext();
    page = context.newPage();
  }

  @AfterEach
  void tearDown() {
    browser.close();
  }

  @Test
  void should_click_the_button() {
    page.navigate(server.PREFIX + "/input/button.html");
    page.click("button");
    assertEquals("Clicked", page.evaluate("result"));
    System.out.println("done 1");
  }

  @Test
  void should_click_svg() {
    page.setContent("<svg height='100' width='100'>\n" +
      "<circle onclick='javascript:window.__CLICKED=42' cx='50' cy='50' r='40' stroke='black' stroke-width='3' fill='red'/>\n" +
      "</svg>\n");
    page.click("circle");
    assertEquals(42, page.evaluate("__CLICKED"));
  }

  @Test
  void should_click_the_button_if_window_Node_is_removed() {
    page.navigate(server.PREFIX + "/input/button.html");
    page.evaluate("() => delete window.Node");
    page.click("button");
    assertEquals("Clicked", page.evaluate("result"));
  }

  // @see https://github.com/GoogleChrome/puppeteer/issues/4281
  @Test
  void should_click_on_a_span_with_an_inline_element_inside() {
    page.setContent(
      "<style>\n" +
      "  span::before {\n" +
      "    content: 'q';\n" +
      "  }\n" +
      "</style>\n" +
      "<span onclick='javascript:window.CLICKED=42'></span>\n");
    page.click("span");
    assertEquals(42, page.evaluate("CLICKED"));
  }

  // TODO: it('should not throw UnhandledPromiseRejection when page closes'

  @Test
  void should_click_the_1x1_div() {
    page.setContent("<div style='width: 1px; height: 1px;' onclick='window.__clicked = true'></div>");
    page.click("div");
    assertTrue((Boolean) page.evaluate("window.__clicked"));
  }

  @Test
  void should_click_the_button_after_navigation() {
    page.navigate(server.PREFIX + "/input/button.html");
    page.click("button");
    page.navigate(server.PREFIX + "/input/button.html");
    page.click("button");
    assertEquals("Clicked", page.evaluate("result"));
  }

  @Test
  void should_click_the_button_after_a_cross_origin_navigation() {
    page.navigate(server.PREFIX + "/input/button.html");
    page.click("button");
    page.navigate(server.CROSS_PROCESS_PREFIX + "/input/button.html");
    page.click("button");
    assertEquals("Clicked", page.evaluate("result"));
  }

  // TODO: it('should click with disabled javascript'

  @Test
  void should_click_when_one_of_inline_box_children_is_outside_of_viewport() {
    page.setContent(
      "<style>\n" +
      "i {\n" +
      "  position: absolute;\n" +
      "  top: -1000px;\n" +
      "}\n" +
      "</style>\n" +
      "<span onclick='javascript:window.CLICKED = 42;'><i>woof</i><b>doggo</b></span>\n");
    page.click("span");
    assertEquals(42, page.evaluate("CLICKED"));
  }

  @Test
  void should_select_the_text_by_triple_clicking() {
    page.navigate(server.PREFIX + "/input/textarea.html");
    String text = "This is the text that we are going to try to select. Let's see how it goes.";
    page.fill("textarea", text);
    page.click("textarea", new Page.ClickOptions().withClickCount(3));
    assertEquals(text, page.evaluate("() => {\n" +
      "  const textarea = document.querySelector('textarea');\n" +
      "  return textarea.value.substring(textarea.selectionStart, textarea.selectionEnd);\n" +
      "}"));
  };
}
