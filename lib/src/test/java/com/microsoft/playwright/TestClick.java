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
    BrowserType.LaunchOptions options = new BrowserType.LaunchOptions().withHeadless(false);
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
}
