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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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

  @Test
  void should_click_offscreen_buttons() {
    page.navigate(server.PREFIX + "/offscreenbuttons.html");
    List<String> messages = new ArrayList<>();
    page.addConsoleListener(msg -> messages.add(msg.text()));
    for (int i = 0; i < 11; ++i) {
      // We might've scrolled to click a button - reset to (0, 0).
      page.evaluate("() => window.scrollTo(0, 0)");
      page.click("#btn" + i);
    }
    assertEquals(Arrays.asList(
      "button #0 clicked",
      "button #1 clicked",
      "button #2 clicked",
      "button #3 clicked",
      "button #4 clicked",
      "button #5 clicked",
      "button #6 clicked",
      "button #7 clicked",
      "button #8 clicked",
      "button #9 clicked",
      "button #10 clicked"
    ), messages);
  }

  @Test
  void should_waitFor_visible_when_already_visible() {
    page.navigate(server.PREFIX + "/input/button.html");
    page.click("button");
    assertEquals("Clicked", page.evaluate("result"));
  }

  @Test
  void should_not_wait_with_force() {
    page.navigate(server.PREFIX + "/input/button.html");
    page.evalOnSelector("button", "b => b.style.display = 'none'");
    Exception exception = null;
    try {
      page.click("button", new Page.ClickOptions().withForce(true));
    } catch (RuntimeException e) {
      exception = e;
    }
    assertNotNull(exception);
    assertTrue(exception.getMessage().contains("Element is not visible"));
    assertEquals("Was not clicked", page.evaluate("result"));
  }

  // TODO: not supported in sync api
  void should_waitFor_display_none_to_be_gone() {
  }
}
