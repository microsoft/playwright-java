/**
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
import com.microsoft.playwright.Page;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.HttpCredentials;
import com.microsoft.playwright.options.LoadState;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/library/popup.spec.ts. */
public class TestPopup {
  @PlaywrightTest
  @DisplayName("PO-014 should inherit user agent from browser context @smoke")
  void shouldInheritUserAgentFromBrowserContext(Browser browser, TestServer server) throws Exception {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setUserAgent("hey"));
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<a target=_blank rel=noopener href=\"/popup/popup.html\">link</a>");
    CompletableFuture<TestServer.Request> requestPromise = server.waitForRequest("/popup/popup.html");
    Page popup = context.waitForPage(() -> page.click("a"));
    popup.waitForLoadState(LoadState.DOMCONTENTLOADED);
    Object userAgent = popup.evaluate("() => window['initialUserAgent']");
    TestServer.Request request = requestPromise.get();
    context.close();
    assertEquals("hey", userAgent);
    assertEquals("hey", request.headers.get("user-agent"));
  }

  @PlaywrightTest
  @DisplayName("PO-015 should respect routes from browser context")
  void shouldRespectRoutesFromBrowserContext(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<a target=_blank rel=noopener href=\"empty.html\">link</a>");
    boolean[] intercepted = {false};
    context.route("**/empty.html", route -> {
      route.resume();
      intercepted[0] = true;
    });
    context.waitForPage(() -> page.click("a"));
    context.close();
    assertTrue(intercepted[0]);
  }

  @PlaywrightTest
  @DisplayName("PO-016 should inherit extra headers from browser context")
  void shouldInheritExtraHeadersFromBrowserContext(Browser browser, TestServer server) throws Exception {
    Map<String, String> extraHTTPHeaders = new HashMap<>();
    extraHTTPHeaders.put("foo", "bar");
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setExtraHTTPHeaders(extraHTTPHeaders));
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    CompletableFuture<TestServer.Request> requestPromise = server.waitForRequest("/dummy.html");
    page.evaluate("url => window['_popup'] = window.open(url)", server.PREFIX + "/dummy.html");
    TestServer.Request request = requestPromise.get();
    context.close();
    assertEquals("bar", request.headers.get("foo"));
  }

  @PlaywrightTest
  @DisplayName("PO-017 should inherit offline from browser context")
  void shouldInheritOfflineFromBrowserContext(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    context.setOffline(true);
    Object online = page.evaluate("url => {\n" +
      "  const win = window.open(url);\n" +
      "  return win.navigator.onLine;\n" +
      "}", server.PREFIX + "/dummy.html");
    context.close();
    assertEquals(false, online);
  }

  @PlaywrightTest
  @DisplayName("PO-018 should inherit http credentials from browser context")
  void shouldInheritHttpCredentialsFromBrowserContext(Browser browser, TestServer server) {
    server.setAuth("/title.html", "user", "pass");
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setHttpCredentials(new HttpCredentials("user", "pass")));
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    Page popup = page.waitForPopup(() -> {
      page.evaluate("url => window['_popup'] = window.open(url)", server.PREFIX + "/title.html");
    });
    popup.waitForLoadState(LoadState.DOMCONTENTLOADED);
    assertEquals("Woof-Woof", popup.title());
    context.close();
  }

  @PlaywrightTest
  @DisplayName("PO-019 should inherit touch support from browser context")
  void shouldInheritTouchSupportFromBrowserContext(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setViewportSize(400, 500)
      .setHasTouch(true));
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    Object hasTouch = page.evaluate("() => {\n" +
      "  const win = window.open('');\n" +
      "  return 'ontouchstart' in win;\n" +
      "}");
    context.close();
    assertEquals(true, hasTouch);
  }

  @PlaywrightTest
  @DisplayName("PO-020 should inherit viewport size from browser context")
  void shouldInheritViewportSizeFromBrowserContext(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setViewportSize(400, 500));
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    Object size = page.evaluate("() => {\n" +
      "  const win = window.open('about:blank');\n" +
      "  return { width: win.innerWidth, height: win.innerHeight };\n" +
      "}");
    context.close();
    assertEquals(mapOf("width", 400, "height", 500), size);
  }

  @PlaywrightTest
  @DisplayName("PO-021 should use viewport size from window features")
  void shouldUseViewportSizeFromWindowFeatures(Browser browser, TestServer server, BrowserName browserName) {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setViewportSize(700, 700));
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    Object[] size = new Object[1];
    Page popup = page.waitForPopup(() -> {
      size[0] = page.evaluate("async () => {\n" +
        "  const win = window.open(window.location.href, 'Title', 'toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=yes,width=600,height=300,top=0,left=0');\n" +
        "  await new Promise(resolve => {\n" +
        "    const interval = window.builtins.setInterval(() => {\n" +
        "      if (win.innerWidth === 600 && win.innerHeight === 300) {\n" +
        "        window.builtins.clearInterval(interval);\n" +
        "        resolve();\n" +
        "      }\n" +
        "    }, 10);\n" +
        "  });\n" +
        "  return { width: win.innerWidth, height: win.innerHeight };\n" +
        "}");
    });
    popup.setViewportSize(500, 400);
    popup.waitForLoadState();
    Object resized = popup.evaluate("() => ({ width: window.innerWidth, height: window.innerHeight })");
    context.close();
    assertEquals(mapOf("width", 600, "height", 300), size[0]);
    assertEquals(mapOf("width", 500, "height", 400), resized);
  }

  @PlaywrightTest
  @DisplayName("PO-022 should respect routes from browser context when using window.open")
  void shouldRespectRoutesFromBrowserContextWhenUsingWindowOpen(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    boolean[] intercepted = {false};
    context.route("**/empty.html", route -> {
      route.resume();
      intercepted[0] = true;
    });
    page.waitForPopup(() -> {
      page.evaluate("url => window['__popup'] = window.open(url)", server.EMPTY_PAGE);
    });
    assertTrue(intercepted[0]);
    context.close();
  }

  @PlaywrightTest
  @DisplayName("PO-023 BrowserContext.addInitScript should apply to an in-process popup")
  void browserContextAddInitScriptShouldApplyToAnInProcessPopup(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext();
    context.addInitScript("window['injected'] = 123");
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    Object injected = page.evaluate("() => {\n" +
      "  const win = window.open('about:blank');\n" +
      "  return win['injected'];\n" +
      "}");
    context.close();
    assertEquals(123, injected);
  }

  @PlaywrightTest
  @DisplayName("PO-024 BrowserContext.addInitScript should apply to a cross-process popup")
  void browserContextAddInitScriptShouldApplyToACrossProcessPopup(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext();
    context.addInitScript("window['injected'] = 123");
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    Page popup = page.waitForPopup(() -> {
      page.evaluate("url => window.open(url)", server.CROSS_PROCESS_PREFIX + "/title.html");
    });
    assertEquals(123, popup.evaluate("injected"));
    popup.reload();
    assertEquals(123, popup.evaluate("injected"));
    context.close();
  }

  @PlaywrightTest
  @DisplayName("PO-025 should expose function from browser context")
  void shouldExposeFunctionFromBrowserContext(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext();
    List<String> messages = new ArrayList<>();
    context.exposeFunction("add", args -> {
      messages.add("binding");
      return ((Number) args[0]).intValue() + ((Number) args[1]).intValue();
    });
    Page page = context.newPage();
    context.onPage(p -> messages.add("page"));
    page.navigate(server.EMPTY_PAGE);
    Object added = page.evaluate("async () => {\n" +
      "  const win = window.open('about:blank');\n" +
      "  return win['add'](9, 4);\n" +
      "}");
    context.close();
    assertEquals(13, added);
    assertEquals("page|binding", String.join("|", messages));
  }

  @PlaywrightTest
  @DisplayName("PO-026 should not dispatch binding on a closed page")
  void shouldNotDispatchBindingOnAClosedPage(Browser browser, TestServer server, BrowserName browserName) {
    BrowserContext context = browser.newContext();
    Boolean[] wasClosed = new Boolean[1];
    context.exposeBinding("add", (source, args) -> {
      wasClosed[0] = source.page().isClosed();
      return ((Number) args[0]).intValue() + ((Number) args[1]).intValue();
    });
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    page.waitForPopup(() -> {
      page.evaluate("async () => {\n" +
        "  const win = window.open('about:blank');\n" +
        "  win['add'](9, 4);\n" +
        "  win.close();\n" +
        "}");
    });
    // Give it a chance to dispatch the binding on the closed page.
    page.waitForTimeout(1000);
    context.close();
    assertFalse(Boolean.TRUE.equals(wasClosed[0]));
  }

  @PlaywrightTest
  @DisplayName("PO-027 should not throttle rAF in the opener page")
  void shouldNotThrottleRAFInTheOpenerPage(Page page, TestServer server) {
    // it.info().annotations.push({ type: 'issue', description: 'https://github.com/microsoft/playwright/issues/14557' });
    page.navigate(server.EMPTY_PAGE);
    Page popup = page.waitForPopup(() -> {
      page.evaluate("() => { window.open('about:blank'); }");
    });
    // Java: the two waits run one after the other; the opener is throttled, or not, while the popup is open either way.
    waitForRafs(page, 30);
    waitForRafs(popup, 30);
  }

  @PlaywrightTest
  @DisplayName("PO-028 should not throw when click closes popup")
  @DisabledOnBrowser(value = BrowserName.FIREFOX, reason = "fixme: locator.click: Target page, context or browser has been closed")
  void shouldNotThrowWhenClickClosesPopup(BrowserName browserName, Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    Page popup = page.waitForPopup(() -> {
      page.evaluate("async browserName => {\n" +
        "  const w = window.open('about:blank');\n" +
        "  if (browserName === 'firefox')\n" +
        "    await new Promise(x => w.onload = x);\n" +
        "  w.document.body.innerHTML = `<button onclick=\"window.close()\">close</button>`;\n" +
        "}", browserName.toString());
    });
    popup.getByRole(AriaRole.BUTTON).click();
  }

  private static void waitForRafs(Page page, int count) {
    page.evaluate("count => new Promise(resolve => {\n" +
      "  const onRaf = () => {\n" +
      "    --count;\n" +
      "    if (!count)\n" +
      "      resolve();\n" +
      "    else\n" +
      "      window.builtins.requestAnimationFrame(onRaf);\n" +
      "  };\n" +
      "  window.builtins.requestAnimationFrame(onRaf);\n" +
      "})", count);
  }
}
