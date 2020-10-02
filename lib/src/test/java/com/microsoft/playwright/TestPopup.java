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
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.microsoft.playwright.Page.LoadState.DOMCONTENTLOADED;
import static com.microsoft.playwright.Utils.mapOf;
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
    popup.waitForLoadState(DOMCONTENTLOADED);
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
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .withExtraHTTPHeaders(mapOf("foo", "bar")));
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

  @Test
  void should_inherit_http_credentials_from_browser_context() {
    server.setAuth("/title.html", "user", "pass");
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setHttpCredentials().withUsername("user").withPassword("pass").done());
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    Deferred<Page> popup = page.waitForPopup();
    page.evaluate("url => window['_popup'] = window.open(url)", server.PREFIX + "/title.html");
    popup.get().waitForLoadState(DOMCONTENTLOADED);
    assertEquals("Woof-Woof", popup.get().title());
    context.close();
  }

  @Test
  void should_inherit_touch_support_from_browser_context() {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setViewport().withWidth(400).withHeight(500).done()
      .withHasTouch(true));
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    Object hasTouch = page.evaluate("() => {\n" +
      "  const win = window.open('');\n" +
      "  return 'ontouchstart' in win;\n" +
      "}");
    context.close();
    assertEquals(true, hasTouch);
  }

  @Test
  void should_inherit_viewport_size_from_browser_context() {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setViewport().withWidth(400).withHeight(500).done());
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    Object size = page.evaluate("() => {\n" +
      "  const win = window.open('about:blank');\n" +
      "  return { width: win.innerWidth, height: win.innerHeight };\n" +
      "}");
    context.close();
    assertEquals(mapOf("width", 400, "height", 500), size);
  }

  @Test
  void should_use_viewport_size_from_window_features() {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setViewport().withWidth(700).withHeight(700).done());
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    Deferred<Page> popupEvent = page.waitForPopup();
    Object size = page.evaluate("() => {\n" +
      "  const win = window.open(window.location.href, 'Title', 'toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=yes,width=600,height=300,top=0,left=0');\n" +
      "  return { width: win.innerWidth, height: win.innerHeight };\n" +
      "}");
    Page popup = popupEvent.get();
    popup.setViewportSize(new Page.ViewportSize().withWidth(500).withHeight(400));
    popup.waitForLoadState();
    Object resized = popup.evaluate("() => ({ width: window.innerWidth, height: window.innerHeight })");
    context.close();
    assertEquals(mapOf("width", 600, "height", 300), size);
    assertEquals(mapOf("width", 500, "height", 400), resized);
  }
}
