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

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED;
import static com.microsoft.playwright.Utils.mapOf;
import static org.junit.jupiter.api.Assertions.*;

public class TestPopup extends TestBase {

  @Test
  void shouldInheritUserAgentFromBrowserContext() throws ExecutionException, InterruptedException {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setUserAgent("hey"));
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<a target=_blank rel=noopener href='/popup/popup.html'>link</a>");
    Future<Server.Request> requestPromise = server.futureRequest("/popup/popup.html");
    Page popup = context.waitForPage(() -> page.click("a"));
    popup.waitForLoadState(DOMCONTENTLOADED);
    String userAgent = (String) popup.evaluate("() => window['initialUserAgent']");
    Server.Request request = requestPromise.get();
    context.close();
    assertEquals("hey", userAgent);
    assertEquals(Arrays.asList("hey"), request.headers.get("user-agent"));
  }

  @Test
  void shouldRespectRoutesFromBrowserContext() {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<a target=_blank rel=noopener href='empty.html'>link</a>");
    boolean[] intercepted = {false};
    context.route("**/empty.html", route -> {
      route.resume();
      intercepted[0] = true;
    });
    context.waitForPage(() -> page.click("a"));

    context.close();
    assertTrue(intercepted[0]);
  }

  @Test
  void shouldInheritExtraHeadersFromBrowserContext() throws ExecutionException, InterruptedException {
    @SuppressWarnings("unchecked")
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setExtraHTTPHeaders(mapOf("foo", "bar")));
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    Future<Server.Request> requestPromise = server.futureRequest("/dummy.html");
    page.evaluate("url => window['_popup'] = window.open(url)", server.PREFIX + "/dummy.html");
    Server.Request request = requestPromise.get();
    context.close();
    assertEquals(Arrays.asList("bar"), request.headers.get("foo"));
  }

  @Test
  void shouldInheritOfflineFromBrowserContext() {
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
  void shouldInheritHttpCredentialsFromBrowserContext() {
    server.setAuth("/title.html", "user", "pass");
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setHttpCredentials("user", "pass"));
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    Page popup = page.waitForPopup(() -> page.evaluate(
      "url => window['_popup'] = window.open(url)", server.PREFIX + "/title.html"));
    popup.waitForLoadState(DOMCONTENTLOADED);
    assertEquals("Woof-Woof", popup.title());
    context.close();
  }

  @Test
  void shouldInheritTouchSupportFromBrowserContext() {
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

  @Test
  void shouldInheritViewportSizeFromBrowserContext() {
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

  @Test
  void shouldUseViewportSizeFromWindowFeatures() {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setViewportSize(700, 700));
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    Object[] size = {null};
    Page popup = page.waitForPopup(() -> {
      size[0] = page.evaluate("async () => {\n" +
        "  const win = window.open(window.location.href, 'Title', 'toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=yes,width=600,height=300,top=0,left=0');\n" +
        "  await new Promise(resolve => {\n" +
        "    const interval = setInterval(() => {\n" +
        "      if (win.innerWidth === 600 && win.innerHeight === 300) {\n" +
        "        clearInterval(interval);\n" +
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

  @Test
  void shouldRespectRoutesFromBrowserContextWithWindowOpen() {
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

  @Test
  void BrowserContextAddInitScriptShouldApplyToAnInProcessPopup() {
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

  @Test
  void BrowserContextAddInitScriptShouldApplyToACrossProcessPopup() {
    BrowserContext context = browser.newContext();
    context.addInitScript("(() => window['injected'] = 123)()");
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

  @Test
  void shouldExposeFunctionFromBrowserContext() {
    BrowserContext context = browser.newContext();
    List<String> messages = new ArrayList<>();
    context.exposeFunction("add", args -> {
      messages.add("binding");
      return (int) args[0] + (int) args[1];
    });
    Page page = context.newPage();
//    context.on("page", () => messages.push('page'));
    page.navigate(server.EMPTY_PAGE);
    Object added = page.evaluate("async () => {\n" +
      "  const win = window.open('about:blank');\n" +
      "  return win['add'](9, 4);\n" +
      "}");
    context.close();
    assertEquals(13, added);
//    assertEquals(messages.join("|"), "page|binding");
  }

  // TODO: checks event order, not specific to java.
  void shouldNotDispatchBindingOnAClosedPage() {
  }
}
