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
import java.util.ArrayList;
import java.util.List;

import static com.microsoft.playwright.Utils.verifyViewport;
import static java.util.Arrays.asList;
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
    Page popup = page.waitForPopup(() -> page.evaluate("url => window.open(url)", server.EMPTY_PAGE));
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

  @Test
  void shouldPropagateDefaultViewportToThePage() {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(456, 789));
    Page page = context.newPage();
    verifyViewport(page, 456, 789);
    context.close();
  }

  void shouldMakeACopyOfDefaultViewport() {
    // Doesn't make sense in Java.
  }

  @Test
  void shouldRespectDeviceScaleFactor() {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setDeviceScaleFactor(3.0));
    Page page = context.newPage();
    assertEquals(3, page.evaluate("window.devicePixelRatio"));
    context.close();
  }


  @Test
  void shouldNotAllowDeviceScaleFactorWithNullViewport() {
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      browser.newContext(new Browser.NewContextOptions().setDeviceScaleFactor(1.0).setViewportSize(null));
    });
    assertTrue(e.getMessage().contains("\"deviceScaleFactor\" option is not supported with null \"viewport\""));
  }

  @Test
  void shouldNotAllowIsMobileWithNullViewport() {
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      browser.newContext(new Browser.NewContextOptions().setIsMobile(true).setViewportSize(null));
    });
    assertTrue(e.getMessage().contains("\"isMobile\" option is not supported with null \"viewport\""));
  }

  @Test
  void closeShouldWorkForEmptyContext() {
    BrowserContext context = browser.newContext();
    context.close();
  }

  @Test
  void closeShouldAbortFutureEvent() {
    BrowserContext context = browser.newContext();
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      context.waitForPage(() -> context.close());
    });
    assertTrue(e.getMessage().contains("Context closed"));
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
      } catch (RuntimeException e) {
        e.printStackTrace();
        throw e;
      }
    });
    Page[] popup = {null};
    context.onPage(page1 -> popup[0] = page1);
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
      BrowserContext context = browser.newContext(new Browser.NewContextOptions().setJavaScriptEnabled(false));
      Page page = context.newPage();
      page.navigate("data:text/html, <script>var something = 'forbidden'</script>");
      PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.evaluate("something"));
      if (isWebKit())
        assertTrue(e.getMessage().contains("Can\'t find variable: something"));
      else
        assertTrue(e.getMessage().contains("something is not defined"));
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
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setJavaScriptEnabled(false));
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    context.close();
  }

  @Test
  void shouldWorkWithOfflineOption() {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setOffline(true));
    Page page = context.newPage();
    assertThrows(PlaywrightException.class, () -> page.navigate(server.EMPTY_PAGE));

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

  @Test
  void shouldWaitForCondition() {
    List<String> messages = new ArrayList<>();
    page.onConsoleMessage(m -> messages.add(m.text()));
    page.evaluate("setTimeout(() => {\n" +
      "  console.log('foo');\n" +
      "  console.log('bar');\n" +
      "}, 100);");
    context.waitForCondition(() -> messages.size() > 1);
    assertEquals(asList("foo", "bar"), messages);
  }

  @Test
  void waitForConditionTimeout() {
    PlaywrightException e = assertThrows(PlaywrightException.class,
      () -> context.waitForCondition(() -> false, new BrowserContext.WaitForConditionOptions().setTimeout(100)));
    assertTrue(e.getMessage().contains("Timeout"), e.getMessage());
  }
  @Test
  void waitForConditionPageClosed() {
    PlaywrightException e = assertThrows(PlaywrightException.class,
      () -> context.waitForCondition(() -> {
        context.close();
        return false;
      }));
    assertTrue(e.getMessage().contains("Context closed"), e.getMessage());
  }

}
