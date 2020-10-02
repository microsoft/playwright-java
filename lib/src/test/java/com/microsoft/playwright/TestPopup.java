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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

public class TestPopup {
  private static Playwright playwright;
  private static Server server;
  private Browser browser;
  private boolean isChromium;
  private boolean isWebKit;
  private boolean headful;
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
    isWebKit = false;
    headful = false;
    context = browser.newContext();
    page = context.newPage();
  }

  @AfterEach
  void tearDown() {
    browser.close();
  }

  @Test
  void should_inherit_user_agent_from_browser_context() throws ExecutionException, InterruptedException {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().withUserAgent("hey"));
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<a target=_blank rel=noopener href='/popup/popup.html'>link</a>");
    Future<Server.Request> requestPromise = server.waitForRequest("/popup/popup.html");
    Deferred<Page> popupPromise = context.waitForPage();
    page.click("a");
    Page popup = popupPromise.get();
    popup.waitForLoadState(Page.LoadState.DOMCONTENTLOADED);
    String userAgent = (String) popup.evaluate("() => window['initialUserAgent']");
    Server.Request request = requestPromise.get();
    context.close();
    assertEquals("hey", userAgent);
    assertEquals(Arrays.asList("hey"), request.headers.get("user-agent"));
  }

  @Test
  void should_respect_routes_from_browser_context() {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<a target=_blank rel=noopener href='empty.html'>link</a>");
    boolean[] intercepted = {false};
    context.route("**/empty.html", (route, request) -> {
      route.continue_();
      intercepted[0] = true;
    });

    Deferred<Page> popup = context.waitForPage();
    page.click("a");
    popup.get();

    context.close();
    assertTrue(intercepted[0]);
  }

  @Test
  void should_inherit_extra_headers_from_browser_context() throws ExecutionException, InterruptedException {
    Map<String, String> headers =  new HashMap<String, String>();
    headers.put("foo", "bar");
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().withExtraHTTPHeaders(headers));
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    Future<Server.Request> requestPromise = server.waitForRequest("/dummy.html");
    page.evaluate("url => window['_popup'] = window.open(url)", server.PREFIX + "/dummy.html");
    Server.Request request = requestPromise.get();
    context.close();
    assertEquals(Arrays.asList("bar"), request.headers.get("foo"));
  }

  @Test
  void should_inherit_offline_from_browser_context() {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    context.setOffline(true);
    boolean online = (boolean) page.evaluate("url => {\n" +
      "  const win = window.open(url);\n" +
      "  return win.navigator.onLine;\n" +
      "}", server.PREFIX + "/dummy.html");
    context.close();
    assertFalse(online);
  }
}
