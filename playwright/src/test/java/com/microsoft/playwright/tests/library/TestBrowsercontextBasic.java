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

package com.microsoft.playwright.tests.library;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.JSHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.ContextFactory;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.ColorScheme;
import com.microsoft.playwright.options.ViewportSize;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.microsoft.playwright.fixtures.Utils.async;
import static com.microsoft.playwright.fixtures.Utils.attachFrame;
import static com.microsoft.playwright.fixtures.Utils.browserMajorVersion;
import static com.microsoft.playwright.fixtures.Utils.kTargetClosedErrorMessage;
import static com.microsoft.playwright.fixtures.Utils.verifyViewport;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/library/browsercontext-basic.spec.ts. */
public class TestBrowsercontextBasic {
  // Java: stands for Promise.all(promises) of several async calls.
  private static <T> List<T> promiseAll(List<CompletableFuture<T>> promises) throws Exception {
    List<T> results = new ArrayList<>();
    for (CompletableFuture<T> promise : promises) {
      results.add(promise.get());
    }
    return results;
  }

  // Java: expect.poll(() => value).toEqual(expected); waitForCondition() dispatches the events while waiting.
  private static <T> void expectPollToEqual(Page page, Supplier<T> value, T expected) {
    try {
      page.waitForCondition(() -> expected.equals(value.get()), new Page.WaitForConditionOptions().setTimeout(5000));
    } catch (TimeoutError e) {
      // The assertion below reports the last value.
    }
    assertEquals(expected, value.get());
  }

  @PlaywrightTest
  @DisplayName("BC-001 should create new context @smoke")
  void shouldCreateNewContext(Browser browser) {
    assertEquals(0, browser.contexts().size());
    BrowserContext context = browser.newContext();
    assertEquals(Arrays.asList(context), browser.contexts());
    assertEquals(context.browser(), browser);
    BrowserContext context2 = browser.newContext();
    assertEquals(Arrays.asList(context, context2), browser.contexts());
    context.close();
    assertEquals(Arrays.asList(context2), browser.contexts());
    assertEquals(context.browser(), browser);
    context2.close();
  }

  private static Page createPage(Browser browser) {
    Page page = browser.newPage();
    page.setContent("<button>Click me</button>");
    page.locator("button").evaluate("button => {\n" +
      "  window['clicks'] = 0;\n" +
      "  button.addEventListener('click', () => ++window['clicks'], false);\n" +
      "}");
    return page;
  }

  private static void clickInPage(Page page, int count) {
    for (int i = 0; i < count; ++i) {
      page.locator("button").click();
    }
  }

  private static Object getClicks(Page page) {
    return page.evaluate("() => window['clicks']");
  }

  @PlaywrightTest
  @DisplayName("BC-002 should be able to click across browser contexts")
  void shouldBeAbleToClickAcrossBrowserContexts(Browser browser) throws Exception {
    assertEquals(0, browser.contexts().size());

    Page page1 = createPage(browser);
    Page page2 = createPage(browser);

    int CLICK_COUNT = 20;
    // Java: Promise.all of two independent actions, one runs in async().
    CompletableFuture<Void> clicks1 = async(() -> {
      clickInPage(page1, CLICK_COUNT);
      return null;
    });
    clickInPage(page2, CLICK_COUNT);
    clicks1.get();
    assertEquals(CLICK_COUNT, getClicks(page1));
    assertEquals(CLICK_COUNT, getClicks(page2));

    page1.close();
    page2.close();
  }

  @PlaywrightTest
  @DisplayName("BC-003 should be able to hover across browser contexts in parallel")
  @DisabledOnBrowser(value = BrowserName.FIREFOX, headed = true, reason = "fixme: Hover is flaky in headed Firefox")
  void shouldBeAbleToHoverAcrossBrowserContextsInParallel(Browser browser) throws Exception {
    String html = "\n" +
      "    <style>\n" +
      "      [role=\"tooltip\"] { display: none; }\n" +
      "      [role=\"tooltip\"].visible { display: block; }\n" +
      "    </style>\n" +
      "    <button>Hover me</button>\n" +
      "    <div role=\"tooltip\">Tooltip content</div>\n" +
      "    <script>\n" +
      "      const button = document.querySelector('button');\n" +
      "      const tooltip = document.querySelector('[role=\"tooltip\"]');\n" +
      "      button.addEventListener('pointerenter', () => tooltip.classList.add('visible'));\n" +
      "      button.addEventListener('pointerleave', () => tooltip.classList.remove('visible'));\n" +
      "    </script>\n" +
      "  ";

    List<CompletableFuture<Page>> pagePromises = new ArrayList<>();
    for (int i = 1; i <= 5; i++) {
      pagePromises.add(async(() -> {
        BrowserContext context = browser.newContext();
        Page page = context.newPage();
        page.setContent(html);
        return page;
      }));
    }
    List<Page> pages = promiseAll(pagePromises);

    List<CompletableFuture<Void>> hoverPromises = new ArrayList<>();
    for (int i = 0; i < pages.size(); i++) {
      Page page = pages.get(i);
      hoverPromises.add(async(() -> {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Hover me")).hover();
        // Tooltip for page ${index + 1} should be visible
        assertThat(page.getByRole(AriaRole.TOOLTIP)).isVisible();
        return null;
      }));
    }
    promiseAll(hoverPromises);

    List<CompletableFuture<Void>> closePromises = new ArrayList<>();
    for (Page page : pages) {
      closePromises.add(async(() -> {
        page.context().close();
        return null;
      }));
    }
    promiseAll(closePromises);
  }

  @PlaywrightTest
  @DisplayName("BC-004 window.open should use parent tab context")
  void windowOpenShouldUseParentTabContext(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    Page popup = page.waitForPopup(() -> page.evaluate("url => window.open(url)", server.EMPTY_PAGE));
    assertEquals(context, popup.context());
    context.close();
  }

  @PlaywrightTest
  @DisplayName("BC-005 should isolate localStorage and cookies @smoke")
  void shouldIsolateLocalStorageAndCookies(Browser browser, TestServer server) throws Exception {
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

    // Make sure pages don't share localstorage or cookies.
    assertEquals("page1", page1.evaluate("() => localStorage.getItem('name')"));
    assertEquals("name=page1", page1.evaluate("() => document.cookie"));
    assertEquals("page2", page2.evaluate("() => localStorage.getItem('name')"));
    assertEquals("name=page2", page2.evaluate("() => document.cookie"));

    // Cleanup contexts.
    // Java: Promise.all of two independent actions, one runs in async().
    CompletableFuture<Void> close1 = async(() -> {
      context1.close();
      return null;
    });
    context2.close();
    close1.get();
    assertEquals(0, browser.contexts().size());
  }

  @PlaywrightTest
  @DisplayName("BC-006 should propagate default viewport to the page")
  void shouldPropagateDefaultViewportToThePage(Browser browser) {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(456, 789));
    Page page = context.newPage();
    verifyViewport(page, 456, 789);
    context.close();
  }

  @PlaywrightTest
  @DisplayName("BC-007 should make a copy of default viewport")
  void shouldMakeACopyOfDefaultViewport(Browser browser) {
    ViewportSize viewport = new ViewportSize(456, 789);
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(viewport));
    viewport.width = 567;
    Page page = context.newPage();
    verifyViewport(page, 456, 789);
    context.close();
  }

  @PlaywrightTest
  @DisplayName("BC-008 should respect deviceScaleFactor")
  void shouldRespectDeviceScaleFactor(Browser browser) {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setDeviceScaleFactor(3));
    Page page = context.newPage();
    assertEquals(3, page.evaluate("window.devicePixelRatio"));
    context.close();
  }

  @PlaywrightTest
  @DisplayName("BC-009 should not allow deviceScaleFactor with null viewport")
  void shouldNotAllowDeviceScaleFactorWithNullViewport(Browser browser) {
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> browser.newContext(new Browser.NewContextOptions().setViewportSize(null).setDeviceScaleFactor(1)));
    assertTrue(error.getMessage().contains("\"deviceScaleFactor\" option is not supported with null \"viewport\""));
  }

  @PlaywrightTest
  @DisplayName("BC-010 should not allow isMobile with null viewport")
  void shouldNotAllowIsMobileWithNullViewport(Browser browser) {
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> browser.newContext(new Browser.NewContextOptions().setViewportSize(null).setIsMobile(true)));
    assertTrue(error.getMessage().contains("\"isMobile\" option is not supported with null \"viewport\""));
  }

  @PlaywrightTest
  @DisplayName("BC-011 close() should work for empty context")
  void closeShouldWorkForEmptyContext(Browser browser) {
    BrowserContext context = browser.newContext();
    context.close();
  }

  @PlaywrightTest
  @DisplayName("BC-012 close() should abort waitForEvent")
  void closeShouldAbortWaitForEvent(Browser browser) {
    BrowserContext context = browser.newContext();
    // Java: the promise of waitForEvent('page') is the waitForPage() call, which closes the context in its callback.
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> context.waitForPage(() -> context.close()));
    assertTrue(error.getMessage().contains(kTargetClosedErrorMessage));
  }

  @PlaywrightTest
  @DisplayName("BC-013 close() should be callable twice")
  void closeShouldBeCallableTwice(Browser browser) {
    BrowserContext context = browser.newContext();
    context.close();
    context.close();
  }

  @PlaywrightTest
  @DisplayName("BC-014 should pass self to close event")
  void shouldPassSelfToCloseEvent(Browser browser) {
    BrowserContext newContext = browser.newContext();
    BrowserContext[] closedContext = {null};
    newContext.onClose(context -> closedContext[0] = context);
    newContext.close();
    assertEquals(newContext, closedContext[0]);
  }

  @PlaywrightTest
  @DisplayName("BC-015 should not report frameless pages on error")
  void shouldNotReportFramelessPagesOnError(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    server.setRoute("/empty.html", (req, res) -> {
      res.end("<a href=\"" + server.EMPTY_PAGE + "\" target=\"_blank\">Click me</a>");
    });
    Page[] popup = {null};
    context.onPage(p -> popup[0] = p);
    page.navigate(server.EMPTY_PAGE);
    page.click("\"Click me\"");
    context.close();
    if (popup[0] != null) {
      // This races on Firefox :/
      assertTrue(popup[0].isClosed());
      assertNotNull(popup[0].mainFrame());
    }
  }

  @PlaywrightTest
  @DisplayName("BC-016 should return all of the pages")
  void shouldReturnAllOfThePages(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    Page second = context.newPage();
    List<Page> allPages = context.pages();
    assertEquals(2, allPages.size());
    assertTrue(allPages.contains(page));
    assertTrue(allPages.contains(second));
    context.close();
  }

  @PlaywrightTest
  @DisplayName("BC-017 should close all belonging pages once closing context")
  void shouldCloseAllBelongingPagesOnceClosingContext(Browser browser) {
    BrowserContext context = browser.newContext();
    context.newPage();
    assertEquals(1, context.pages().size());

    context.close();
    assertEquals(0, context.pages().size());
  }

  @PlaywrightTest
  @DisplayName("BC-018 should disable javascript")
  void shouldDisableJavascript(Browser browser, BrowserName browserName) {
    {
      BrowserContext context = browser.newContext(new Browser.NewContextOptions().setJavaScriptEnabled(false));
      Page page = context.newPage();
      page.navigate("data:text/html, <script>var something = \"forbidden\"</script>");
      PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.evaluate("something"));
      if (browserName == BrowserName.WEBKIT) {
        assertTrue(error.getMessage().contains("Can't find variable: something"));
      } else {
        assertTrue(error.getMessage().contains("something is not defined"));
      }
      context.close();
    }

    {
      BrowserContext context = browser.newContext();
      Page page = context.newPage();
      page.navigate("data:text/html, <script>var something = \"forbidden\"</script>");
      assertEquals("forbidden", page.evaluate("something"));
      context.close();
    }
  }

  @PlaywrightTest
  @DisplayName("BC-019 should be able to navigate after disabling javascript")
  void shouldBeAbleToNavigateAfterDisablingJavascript(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setJavaScriptEnabled(false));
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    context.close();
  }

  @PlaywrightTest
  @DisplayName("BC-020 should not hang on promises after disabling javascript")
  void shouldNotHangOnPromisesAfterDisablingJavascript(BrowserName browserName, ContextFactory contextFactory) {
    assumeFalse(browserName == BrowserName.FIREFOX && !Env.isBidi(), "fixme");
    BrowserContext context = contextFactory.newContext(new Browser.NewContextOptions().setJavaScriptEnabled(false));
    Page page = context.newPage();
    assertEquals(1, page.evaluate("() => 1"));
    assertEquals(2, page.evaluate("async () => 2"));
  }

  @PlaywrightTest
  @DisplayName("BC-021 setContent should work after disabling javascript")
  void setContentShouldWorkAfterDisablingJavascript(ContextFactory contextFactory) {
    BrowserContext context = contextFactory.newContext(new Browser.NewContextOptions().setJavaScriptEnabled(false));
    Page page = context.newPage();
    page.setContent("<h1>Hello</h1>");
    assertThat(page.locator("h1")).hasText("Hello");
  }

  @PlaywrightTest
  @DisplayName("BC-022 should retry click after disabling javascript")
  void shouldRetryClickAfterDisablingJavascript(ContextFactory contextFactory) throws Exception {
    BrowserContext context = contextFactory.newContext(new Browser.NewContextOptions().setJavaScriptEnabled(false));
    Page page = context.newPage();
    page.setContent("\n" +
      "    <details><summary>Continue</summary>Details</details>\n" +
      "    <div id=\"cover\" style=\"position: fixed; inset: 0;\"></div>\n" +
      "  ");
    boolean[] clicked = {false};
    // Java: the pending click promise runs in async().
    CompletableFuture<Void> clickPromise = async(() -> {
      page.locator("summary").click();
      clicked[0] = true;
      return null;
    });
    // Give it enough time to go through a few retries while the button is covered.
    page.waitForTimeout(2000);
    assertEquals(false, clicked[0]);
    page.locator("#cover").evaluate("e => e.remove()");
    clickPromise.get();
    assertThat(page.locator("details")).hasAttribute("open", "");
  }

  @PlaywrightTest
  @DisplayName("BC-023 should work with offline option")
  void shouldWorkWithOfflineOption(Browser browser, TestServer server, BrowserName browserName) {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setOffline(true));
    Page page = context.newPage();
    PlaywrightException[] error = {null};
    if (browserName == BrowserName.FIREFOX) {
      // Firefox navigates to an error page, and this navigation might conflict with the
      // next navigation we do in test.
      // So we need to wait for the navigation explicitly.
      // Java: the framenavigated promise is a handler that sets a flag, awaited with waitForCondition().
      boolean[] frameNavigated = {false};
      page.onFrameNavigated(frame -> frameNavigated[0] = true);
      try {
        page.navigate(server.EMPTY_PAGE);
      } catch (PlaywrightException e) {
        error[0] = e;
      }
      page.waitForCondition(() -> frameNavigated[0]);
    } else {
      try {
        page.navigate(server.EMPTY_PAGE);
      } catch (PlaywrightException e) {
        error[0] = e;
      }
    }
    assertNotNull(error[0]);
    context.setOffline(false);
    Response response = page.navigate(server.EMPTY_PAGE);
    assertEquals(200, response.status());
    context.close();
  }

  @PlaywrightTest
  @DisplayName("BC-024 fetch with keepalive should throw when offline")
  void fetchWithKeepaliveShouldThrowWhenOffline(ContextFactory contextFactory, TestServer server) {
    BrowserContext context = contextFactory.newContext();
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);

    String url = server.PREFIX + "/fetch";
    server.setRoute("/fetch", (req, res) -> {
      res.setHeader("Content-Type", "application/json");
      res.end("\"hello\"");
    });

    Object okResponse = page.evaluate("url => fetch(url, { cache: 'no-store', keepalive: true }).then(response => response.json())", url);
    assertEquals("hello", okResponse);

    context.setOffline(true);
    Object offlineResponse = page.evaluate("async url => {\n" +
      "  try {\n" +
      "    const response = await fetch(url, { cache: 'no-store', keepalive: true });\n" +
      "    return await response.json();\n" +
      "  } catch {\n" +
      "    return 'error';\n" +
      "  }\n" +
      "}", url);
    assertEquals("error", offlineResponse);
  }

  @PlaywrightTest
  @DisplayName("BC-025 should emulate navigator.onLine")
  void shouldEmulateNavigatorOnLine(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    assertEquals(true, page.evaluate("() => window.navigator.onLine"));
    context.setOffline(true);
    assertEquals(false, page.evaluate("() => window.navigator.onLine"));
    context.setOffline(false);
    assertEquals(true, page.evaluate("() => window.navigator.onLine"));
    context.close();
  }

  @PlaywrightTest
  @DisplayName("BC-026 should emulate navigator.onLine across navigations")
  void shouldEmulateNavigatorOnLineAcrossNavigations(Browser browser, TestServer server, BrowserName browserName) {
    assumeFalse(browserName == BrowserName.CHROMIUM && browserMajorVersion(browser) < 155, "does not survive cross-process navgiation");

    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    assertEquals(true, page.evaluate("() => window.navigator.onLine"));
    context.setOffline(true);
    assertEquals(false, page.evaluate("() => window.navigator.onLine"));

    page.navigate("about:blank");
    assertEquals(false, page.evaluate("() => window.navigator.onLine"));
    page.navigate("data:text/html,<title>offline</title>");
    assertEquals(false, page.evaluate("() => window.navigator.onLine"));

    if (browserName == BrowserName.CHROMIUM) {
      // Try a cross-process navigation in Chromium, which allows routing while offline.
      page.route("**/*", route -> route.fulfill(new Route.FulfillOptions().setContentType("text/html").setBody("<html></html>")));
      page.navigate(server.CROSS_PROCESS_PREFIX + "/empty.html");
      assertEquals(false, page.evaluate("() => window.navigator.onLine"));
    }

    context.setOffline(false);
    assertEquals(true, page.evaluate("() => window.navigator.onLine"));
    context.close();
  }

  @PlaywrightTest
  @DisplayName("BC-027 should emulate offline event")
  void shouldEmulateOfflineEvent(Browser browser) {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    JSHandle events = page.evaluateHandle("() => {\n" +
      "  const events = [];\n" +
      "  window.addEventListener('offline', () => events.push('offline'));\n" +
      "  window.addEventListener('online', () => events.push('online'));\n" +
      "  return events;\n" +
      "}");
    context.setOffline(true);
    expectPollToEqual(page, events::jsonValue, Arrays.<Object>asList("offline"));
    context.setOffline(false);
    expectPollToEqual(page, events::jsonValue, Arrays.<Object>asList("offline", "online"));
    context.close();
  }

  @PlaywrightTest
  @DisplayName("BC-028 should emulate media in popup")
  void shouldEmulateMediaInPopup(Browser browser, TestServer server) {
    {
      BrowserContext context = browser.newContext(new Browser.NewContextOptions().setColorScheme(ColorScheme.DARK));
      Page page = context.newPage();
      page.navigate(server.EMPTY_PAGE);
      Page popup = page.waitForPopup(() -> page.evaluate("url => { window.open(url); }", server.EMPTY_PAGE));
      assertEquals(false, popup.evaluate("() => matchMedia('(prefers-color-scheme: light)').matches"));
      assertEquals(true, popup.evaluate("() => matchMedia('(prefers-color-scheme: dark)').matches"));
      context.close();
    }
    {
      Page page = browser.newPage(new Browser.NewPageOptions().setColorScheme(ColorScheme.LIGHT));
      page.navigate(server.EMPTY_PAGE);
      Page popup = page.waitForPopup(() -> page.evaluate("url => { window.open(url); }", server.EMPTY_PAGE));
      assertEquals(true, popup.evaluate("() => matchMedia('(prefers-color-scheme: light)').matches"));
      assertEquals(false, popup.evaluate("() => matchMedia('(prefers-color-scheme: dark)').matches"));
      page.close();
    }
  }

  @PlaywrightTest
  @DisplayName("BC-029 should emulate media in cross-process iframe")
  void shouldEmulateMediaInCrossProcessIframe(Browser browser, TestServer server) {
    Page page = browser.newPage(new Browser.NewPageOptions().setColorScheme(ColorScheme.DARK));
    page.navigate(server.EMPTY_PAGE);
    attachFrame(page, "frame1", server.CROSS_PROCESS_PREFIX + "/empty.html");
    Frame frame = page.frames().get(1);
    assertEquals(true, frame.evaluate("() => matchMedia('(prefers-color-scheme: dark)').matches"));
    page.close();
  }

  @PlaywrightTest
  @DisplayName("BC-031 should create two pages in parallel in various contexts")
  void shouldCreateTwoPagesInParallelInVariousContexts(Browser browser) throws Exception {
    BrowserContext context1 = browser.newContext();
    BrowserContext context2 = browser.newContext();
    // Java: Promise.all of four independent actions, each runs in async().
    promiseAll(Arrays.asList(
      async(() -> context1.newPage()),
      async(() -> context1.newPage()),
      async(() -> context2.newPage()),
      async(() -> context2.newPage())));
    context1.close();
    context2.close();
    BrowserContext context3 = browser.newContext();
    promiseAll(Arrays.asList(
      async(() -> context3.newPage()),
      async(() -> context3.newPage())));
    context3.close();
  }
}
