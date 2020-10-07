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
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.*;

public class TestPageRoute {
  private static Server server;
  private static Browser browser;
  private static boolean isChromium;
  private static boolean isWebKit;
  private static boolean isFirefox;
  private BrowserContext context;
  private Page page;

  @BeforeAll
  static void launchBrowser() {
    Playwright playwright = Playwright.create();
    BrowserType.LaunchOptions options = new BrowserType.LaunchOptions();
    browser = playwright.chromium().launch(options);
    isChromium = true;

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
  void shouldIntercept() {
    boolean[] intercepted = {false};
    page.route("**/empty.html", (route, request) -> {
      assertEquals(request, route.request());
      assertTrue(request.url().contains("empty.html"));
      assertNotNull(request.headers().get("user-agent"));
      assertEquals("GET", request.method());
      assertNull(request.postData());
      assertTrue(request.isNavigationRequest());
      assertEquals("document", request.resourceType());
      assertTrue(request.frame() == page.mainFrame());
      assertEquals("about:blank", request.frame().url());
      route.continue_();
      intercepted[0] = true;
    });
    Response response = page.navigate(server.EMPTY_PAGE);
    assertTrue(response.ok());
    assertTrue(intercepted[0]);
  }


  @Test
  void shouldUnroute() {
    List<Integer> intercepted = new ArrayList<>();
    BiConsumer<Route, Request> handler1 = (route, request) -> {
      intercepted.add(1);
      route.continue_();
    };
    page.route("**/empty.html", handler1);
    page.route("**/empty.html", (route, request) -> {
      intercepted.add(2);
      route.continue_();
    });
    page.route("**/empty.html", (route, request) -> {
      intercepted.add(3);
      route.continue_();
    });
    page.route("**/*", (route, request) -> {
      intercepted.add(4);
      route.continue_();
    });
    page.navigate(server.EMPTY_PAGE);
    assertEquals(Arrays.asList(1), intercepted);

    intercepted.clear();
    page.unroute("**/empty.html", handler1);
    page.navigate(server.EMPTY_PAGE);
    assertEquals(Arrays.asList(2), intercepted);

    intercepted.clear();
    page.unroute("**/empty.html");
    page.navigate(server.EMPTY_PAGE);
    assertEquals(Arrays.asList(4), intercepted);
  }
}
