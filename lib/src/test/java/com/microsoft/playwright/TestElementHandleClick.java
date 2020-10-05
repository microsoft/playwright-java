/**
 * Copyright (c) Microsoft Corporation.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.playwright;

import org.junit.jupiter.api.*;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class TestElementHandleClick {
  private static Playwright playwright;
  private static Server server;
  private static Browser browser;
  private static boolean isChromium;
  private static boolean isWebKit;
  private static boolean headful;
  private BrowserContext context;
  private Page page;

  @BeforeAll
  static void launchBrowser() {
    playwright = Playwright.create();
    BrowserType.LaunchOptions options = new BrowserType.LaunchOptions();
    browser = playwright.chromium().launch(options);
    isChromium = true;
    isWebKit = false;
    headful = false;
  }

  @BeforeAll
  static void startServer() throws IOException {
    server = new Server(8907);
  }

  @AfterAll
  static void stopServer() throws IOException {
    browser.close();
    server.stop();
    server = null;
  }

  @BeforeEach
  void setUp() {
    context = browser.newContext();
    page = context.newPage();
  }

  @AfterEach
  void tearDown() {
    context.close();
    context = null;
    page = null;
  }

  @Test
  void should_work() {
    page.navigate(server.PREFIX + "/input/button.html");
    ElementHandle button = page.querySelector("button");
    button.click();
    assertEquals("Clicked", page.evaluate("() => window['result']"));
  }

  @Test
  void should_work_with_Node_removed() {
    page.navigate(server.PREFIX + "/input/button.html");
    page.evaluate("() => delete window['Node']");
    ElementHandle button  = page.querySelector("button");
    button.click();
    assertEquals("Clicked", page.evaluate("() => window['result']"));
  }

  @Test
  void should_work_for_Shadow_DOM_v1() {
    page.navigate(server.PREFIX + "/shadow.html");
    ElementHandle buttonHandle = page.evaluateHandle("() => window['button']").asElement();
    buttonHandle.click();
    assertEquals(true, page.evaluate("clicked"));
  }

  @Test
  void should_work_for_TextNodes() {
    page.navigate(server.PREFIX + "/input/button.html");
    ElementHandle buttonTextNode = page.evaluateHandle("() => document.querySelector('button').firstChild").asElement();
    buttonTextNode.click();
    assertEquals("Clicked", page.evaluate("() => window['result']"));
  }

  @Test
  void should_throw_for_detached_nodes() {
    page.navigate(server.PREFIX + "/input/button.html");
    ElementHandle button = page.querySelector("button");
    page.evaluate("button => button.remove()", button);
    try {
      button.click();
      fail("click should throw");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("Element is not attached to the DOM"));
    }
  }

  @Test
  void should_throw_for_hidden_nodes_with_force() {
    page.navigate(server.PREFIX + "/input/button.html");
    ElementHandle button = page.querySelector("button");
    page.evaluate("button => button.style.display = 'none'", button);
    try {
      button.click(new ElementHandle.ClickOptions().withForce(true));
      fail("click should throw");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("Element is not visible"));
    }
  }

  @Test
  void should_throw_for_recursively_hidden_nodes_with_force() {
    page.navigate(server.PREFIX + "/input/button.html");
    ElementHandle button = page.querySelector("button");
    page.evaluate("button => button.parentElement.style.display = 'none'", button);
    try {
      button.click(new ElementHandle.ClickOptions().withForce(true));
      fail("click should throw");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("Element is not visible"));
    }
  }

  @Test
  void should_throw_for__br__elements_with_force() {
    page.setContent("hello<br>goodbye");
    ElementHandle br = page.querySelector("br");
    try {
      br.click(new ElementHandle.ClickOptions().withForce(true));
      fail("click should throw");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("Element is outside of the viewport"));
    }
  }

  @Test
  void should_double_click_the_button() {
    page.navigate(server.PREFIX + "/input/button.html");
    page.evaluate("() => {\n" +
      "  window['double'] = false;\n" +
      "  const button = document.querySelector('button');\n" +
      "  button.addEventListener('dblclick', event => {\n" +
      "    window['double'] = true;\n" +
      "  });\n" +
      "}");
    ElementHandle button = page.querySelector("button");
    button.dblclick();
    assertEquals(true, page.evaluate("double"));
    assertEquals("Clicked", page.evaluate("result"));
  }
}
