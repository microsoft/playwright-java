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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.OutputStreamWriter;
import java.util.List;

import static com.microsoft.playwright.BrowserContext.EventType.PAGE;
import static com.microsoft.playwright.Page.EventType.POPUP;
import static org.junit.jupiter.api.Assertions.*;

public class TestBrowserContextBasic extends TestBase {
  @Test
  void shouldCreateNewContext() {
    assertEquals(1, browser.contexts().size());
    BrowserContext context = browser.newContext();
    assertEquals(2, browser.contexts().size());
    assertTrue(browser.contexts().indexOf(context) != -1);
    assertEquals(context.browser(), browser);
    context.close();
    assertEquals(1, browser.contexts().size());
    assertEquals(context.browser(), browser);
  }

  @Test
  void windowOpenShouldUseParentTabContext() {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    Deferred<Event<Page.EventType>> popupEvent = page.futureEvent(POPUP);
    page.evaluate("url => window.open(url)", server.EMPTY_PAGE);
    Page popup = (Page) popupEvent.get().data();
    assertEquals(context, popup.context());
    context.close();
  }

  @Test
  void shouldIsolateLocalStorageAndCookies() {
    // Create two incognito contexts.
    BrowserContext context1 = browser.newContext();
    BrowserContext context2 = browser.newContext();
    assertEquals(0, context1.pages().size());
    assertEquals(0, context2.pages().size());

    // Create a page in first incognito context.
    Page page1 = context1.newPage();
    page1.navigate(server.EMPTY_PAGE);
    page1.evaluate("() => {\n" +
      "  localStorage.setItem('name', 'page1');\n" +
      "  document.cookie = 'name=page1';\n" +
      "}");

    assertEquals(1, context1.pages().size());
    assertEquals(0, context2.pages().size());

    // Create a page in second incognito context.
    Page page2 = context2.newPage();
    page2.navigate(server.EMPTY_PAGE);
    page2.evaluate("() => {\n" +
      "  localStorage.setItem('name', 'page2');\n" +
      "  document.cookie = 'name=page2';\n" +
      "}");

    assertEquals(1, context1.pages().size());
    assertEquals(1, context2.pages().size());
    assertEquals(page1, context1.pages().get(0));
    assertEquals(page2, context2.pages().get(0));

    // Make sure pages don"t share localstorage or cookies.
    assertEquals("page1", page1.evaluate("() => localStorage.getItem('name')"));
    assertEquals("name=page1", page1.evaluate("() => document.cookie"));
    assertEquals("page2", page2.evaluate("() => localStorage.getItem('name')"));
    assertEquals("name=page2", page2.evaluate("() => document.cookie"));

    // Cleanup contexts.
    context1.close();
    context2.close();

    assertEquals(1, browser.contexts().size());
  }

  static void verifyViewport(Page page, int width, int height) {
    assertEquals(width, page.viewportSize().width());
    assertEquals(height, page.viewportSize().height());
    assertEquals(width, page.evaluate("window.innerWidth"));
    assertEquals(height, page.evaluate("window.innerHeight"));
  }

  @Test
  void shouldPropagateDefaultViewportToThePage() {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().withViewport(456, 789));
    Page page = context.newPage();
    verifyViewport(page, 456, 789);
    context.close();
  }

  void shouldMakeACopyOfDefaultViewport() {
    // Doesn't make sense in Java.
  }

  @Test
  void shouldRespectDeviceScaleFactor() {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().withDeviceScaleFactor(3));
    Page page = context.newPage();
    assertEquals(3, page.evaluate("window.devicePixelRatio"));
    context.close();
  }


  @Test
  @Disabled("TODO: supported null viewport option")
  void shouldNotAllowDeviceScaleFactorWithNullViewport() {
    try {
      browser.newContext(new Browser.NewContextOptions().withDeviceScaleFactor(1));
      fail("did not throw");
    } catch (PlaywrightException e) {
      assertTrue(e.getMessage().contains("\"deviceScaleFactor\" option is not supported with null \"viewport\""));
    }
  }

  @Test
  @Disabled("TODO: supported null viewport option")
  void shouldNotAllowIsMobileWithNullViewport() {
    try {
      browser.newContext(new Browser.NewContextOptions().withIsMobile(true));
      fail("did not throw");
    } catch (PlaywrightException e) {
      assertTrue(e.getMessage().contains("\"isMobile\" option is not supported with null \"viewport\""));
    }
  }

  @Test
  void closeShouldWorkForEmptyContext() {
    BrowserContext context = browser.newContext();
    context.close();
  }

  @Test
  void closeShouldAbortFutureEvent() {
    BrowserContext context = browser.newContext();
    Deferred<Event<BrowserContext.EventType>> pageEvent = context.futureEvent(PAGE);
    context.close();
    try {
      pageEvent.get();
      fail("did not throw");
    } catch (PlaywrightException e) {
      assertTrue(e.getMessage().contains("Context closed"));
    }
  }

  @Test
  void closeShouldBeCallableTwice() {
    BrowserContext context = browser.newContext();
    context.close();
    context.close();
    context.close();
  }

  @Test
  void shouldNotReportFramelessPagesOnError() {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    server.setRoute("/empty.html", exchange -> {
      exchange.sendResponseHeaders(200, 0);
      try (OutputStreamWriter writer = new OutputStreamWriter(exchange.getResponseBody())) {
        writer.write("<a href='" + server.EMPTY_PAGE + "' target='_blank'>Click me</a>");
      }
    });
    Page[] popup = {null};
    context.addListener(PAGE, event -> popup[0] = (Page) event.data());
    page.navigate(server.EMPTY_PAGE);
    page.click("'Click me'");
    context.close();
    if (popup[0] != null) {
      // This races on Firefox :/
      assertTrue(popup[0].isClosed());
      assertNotNull(popup[0].mainFrame());
    }
  }

  @Test
  void shouldReturnAllOfThePages() {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    Page second = context.newPage();
    List<Page> allPages = context.pages();
    assertEquals(2, allPages.size());
    assertTrue(allPages.contains(page));
    assertTrue(allPages.contains(second));
    context.close();
  }

  @Test
  void shouldCloseAllBelongingPagesOnceClosingContext() {
    BrowserContext context = browser.newContext();
    context.newPage();
    assertEquals(1, context.pages().size());
    context.close();
    assertEquals(0, context.pages().size());
  }

  @Test
  void shouldDisableJavascript() {
    {
      BrowserContext context = browser.newContext(new Browser.NewContextOptions().withJavaScriptEnabled(false));
      Page page = context.newPage();
      page.navigate("data:text/html, <script>var something = 'forbidden'</script>");
      try {
        page.evaluate("something");
        fail("did not throw");
      } catch (PlaywrightException e) {
        if (isWebKit())
          assertTrue(e.getMessage().contains("Can\'t find variable: something"));
        else
          assertTrue(e.getMessage().contains("something is not defined"));
      }
      context.close();
    }

    {
      BrowserContext context = browser.newContext();
      Page page = context.newPage();
      page.navigate("data:text/html, <script>var something = 'forbidden'</script>");
      assertEquals("forbidden", page.evaluate("something"));
      context.close();
    }
  }

  @Test
  void shouldBeAbleToNavigateAfterDisablingJavascript() {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().withJavaScriptEnabled(false));
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    context.close();
  }

  @Test
  void shouldWorkWithOfflineOption() {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().withOffline(true));
    Page page = context.newPage();
    try {
      page.navigate(server.EMPTY_PAGE);
      fail("did not throw");
    } catch (PlaywrightException e) {
    }

    context.setOffline(false);
    Response response = page.navigate(server.EMPTY_PAGE);
    assertEquals(200, response.status());
    context.close();
  }

  @Test
  void shouldEmulateNavigatorOnLine() {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    assertEquals(true, page.evaluate("() => window.navigator.onLine"));
    context.setOffline(true);
    assertEquals(false, page.evaluate("() => window.navigator.onLine"));
    context.setOffline(false);
    assertEquals(true, page.evaluate("() => window.navigator.onLine"));
    context.close();
  }
}
