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

package com.microsoft.playwright.tests.library.chromium;

import com.google.gson.JsonObject;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.CDPSession;
import com.microsoft.playwright.Dialog;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.CreateUserDataDir;
import com.microsoft.playwright.fixtures.EnabledOnBrowser;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.BeforeEach;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.microsoft.playwright.fixtures.Utils.async;
import static com.microsoft.playwright.fixtures.Utils.browserMajorVersion;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/** Port of tests/library/chromium/chromium.spec.ts. */
@EnabledOnBrowser(BrowserName.CHROMIUM)
public class TestChromium {
  @PlaywrightTest
  @DisplayName("CR-009 should pass args with spaces")
  void shouldPassArgsWithSpaces(BrowserType browserType, CreateUserDataDir createUserDataDir) {
    BrowserContext browser = browserType.launchPersistentContext(createUserDataDir.create(), Env.launchPersistentContextOptions()
      .setArgs(Arrays.asList("--user-agent=I am Foo")));
    Page page = browser.newPage();
    Object userAgent = page.evaluate("() => navigator.userAgent");
    browser.close();
    assertEquals("I am Foo", userAgent);
  }

  @PlaywrightTest
  @DisplayName("CR-012 should report failure (due to content-type) of main service worker request")
  void shouldReportFailureDueToContentTypeOfMainServiceWorkerRequest(TestServer server, Page page, BrowserContext context, Browser browser) throws Exception {
    assumeFalse(browserMajorVersion(browser) < 104, "Requires http://crrev.com/1012503 or later.");
    server.setRoute("/serviceworkers/fetch/sw.js", (req, res) -> {
      res.writeHead(200, "Content-Type", "text/html");
      res.write("console.log('hi from sw');");
      res.end();
    });
    CompletableFuture<TestServer.Request> serverRequest = server.waitForRequest("/serviceworkers/fetch/sw.js");
    // Java: context.waitForEvent('request', predicate) is a future that the onRequest handler completes.
    CompletableFuture<Request> mainRequest = new CompletableFuture<>();
    context.onRequest(r -> {
      if (r.url().endsWith("sw.js"))
        mainRequest.complete(r);
    });
    page.navigate(server.PREFIX + "/serviceworkers/fetch/sw.html");
    serverRequest.get();
    context.waitForCondition(mainRequest::isDone);
    Request main = mainRequest.get();
    // This will timeout today
    main.response();
  }

  @PlaywrightTest
  @DisplayName("CR-013 should report failure (due to redirect) of main service worker request")
  void shouldReportFailureDueToRedirectOfMainServiceWorkerRequest(TestServer server, Page page, BrowserContext context, Browser browser) throws Exception {
    assumeFalse(browserMajorVersion(browser) < 104, "Requires http://crrev.com/1012503 or later.");
    server.setRedirect("/serviceworkers/empty/sw.js", "/dev/null");
    CompletableFuture<TestServer.Request> serverRequest = server.waitForRequest("/serviceworkers/empty/sw.js");
    // Java: context.waitForEvent('request', predicate) is a future that the onRequest handler completes.
    CompletableFuture<Request> mainRequest = new CompletableFuture<>();
    context.onRequest(r -> {
      if (r.url().endsWith("sw.js"))
        mainRequest.complete(r);
    });
    page.navigate(server.PREFIX + "/serviceworkers/empty/sw.html");
    serverRequest.get();
    context.waitForCondition(mainRequest::isDone);
    Request main = mainRequest.get();
    // This will timeout today
    Response resp = main.response();
    assertEquals(302, resp.status());
  }

  @PlaywrightTest
  @DisplayName("CR-022 should throw when connecting twice to an already running persistent context (--remote-debugging-port)")
  void shouldThrowWhenConnectingTwiceToAnAlreadyRunningPersistentContextRemoteDebuggingPort(BrowserType browserType, CreateUserDataDir createUserDataDir, BrowserName browserName) {
    assumeFalse(Env.isHeadlessShell(browserName), "Headless shell does not create a ProcessSingleton");
    assumeFalse(Env.platform().equals("win32"), "fixme: Windows does not print something to the console when the profile is already in use by another instance of Chromium.");
    assumeFalse(Env.channel() != null && Env.channel().startsWith("msedge"), "Edge does not print the ProcessSingleton line");
    Path userDataDir = createUserDataDir.create();
    BrowserContext browser = browserType.launchPersistentContext(userDataDir, Env.launchPersistentContextOptions()
      .setArgs(Arrays.asList("--remote-debugging-port=9222")));
    try {
      PlaywrightException error = assertThrows(PlaywrightException.class, () -> browserType.launchPersistentContext(userDataDir, Env.launchPersistentContextOptions()
        .setArgs(Arrays.asList("--remote-debugging-port=9223"))));
      assertTrue(error.getMessage().contains("This usually means that the profile is already in use by another instance of Chromium."));
    } finally {
      browser.close();
    }
  }

  @PlaywrightTest
  @DisplayName("CR-023 should throw when connecting twice to an already running persistent context (--remote-debugging-pipe)")
  void shouldThrowWhenConnectingTwiceToAnAlreadyRunningPersistentContextRemoteDebuggingPipe(BrowserType browserType, CreateUserDataDir createUserDataDir, BrowserName browserName) {
    assumeFalse(Env.isHeadlessShell(browserName), "Headless shell does not create a ProcessSingleton");
    assumeFalse(Env.platform().equals("win32"), "fixme: Windows does not print something to the console when the profile is already in use by another instance of Chromium.");
    assumeFalse(Env.channel() != null && Env.channel().startsWith("msedge"), "Edge does not print the ProcessSingleton line");
    Path userDataDir = createUserDataDir.create();
    BrowserContext browser = browserType.launchPersistentContext(userDataDir, Env.launchPersistentContextOptions());
    try {
      PlaywrightException error = assertThrows(PlaywrightException.class, () -> browserType.launchPersistentContext(userDataDir, Env.launchPersistentContextOptions()));
      assertTrue(error.getMessage().contains("This usually means that the profile is already in use by another instance of Chromium."));
    } finally {
      browser.close();
    }
  }

  @Nested
  class WebUINavigation {
    private boolean isEdge(String channel) {
      return channel != null && channel.startsWith("msedge");
    }

    private String gotoError(Page page, String url) {
      try {
        page.navigate(url);
        return "";
      } catch (PlaywrightException e) {
        return e.getMessage();
      }
    }

    @PlaywrightTest
    @DisplayName("CR-027 should refuse WebUI pages that crash the browser in an isolated context")
    void shouldRefuseWebUIPagesThatCrashTheBrowserInAnIsolatedContext(Browser browser, Page page) {
      // { type: 'issue', description: 'https://github.com/microsoft/playwright/issues/41935' }
      assumeFalse(isEdge(Env.channel()), "Edge only disallows chrome://history");

      List<String> hosts = Arrays.asList("apps", "extensions", "help", "history", "password-manager", "settings");
      List<String> urls = new ArrayList<>();
      for (String host : hosts)
        urls.add("chrome://" + host);
      urls.addAll(Arrays.asList("chrome://extensions/", "chrome://settings/help", "chrome://SETTINGS", "chrome:settings", "chrome:///settings", "view-source:chrome://settings"));
      for (String url : urls)
        assertTrue(gotoError(page, url).contains("Cannot navigate to \"" + url + "\""));
      // The refusal only matters because it keeps the browser process alive.
      assertEquals(true, browser.isConnected());
      assertEquals(2, page.evaluate("() => 1 + 1"));
    }

    @PlaywrightTest
    @DisplayName("CR-028 should refuse WebUI pages that crash Edge in an isolated context")
    void shouldRefuseWebUIPagesThatCrashEdgeInAnIsolatedContext(Browser browser, Page page) {
      assumeFalse(!isEdge(Env.channel()), "Edge has its own list of pages disallowed in InPrivate");

      for (String url : Arrays.asList("edge://history", "chrome://history", "view-source:edge://history"))
        assertTrue(gotoError(page, url).contains("Cannot navigate to \"" + url + "\""));
      // Edge does allow these in InPrivate, so they must not be refused.
      for (String url : Arrays.asList("edge://settings", "edge://extensions"))
        assertFalse(gotoError(page, url).contains("Cannot navigate to"));
      assertEquals(true, browser.isConnected());
    }

    @PlaywrightTest
    @DisplayName("CR-029 should navigate to WebUI pages that work in an isolated context")
    void shouldNavigateToWebUIPagesThatWorkInAnIsolatedContext(Page page) {
      assumeFalse(!Env.headed(), "WebUI pages are not available in headless");

      Response response = page.navigate("chrome://version");
      assertEquals(200, response.status());
    }

    @PlaywrightTest
    @DisplayName("CR-030 should navigate to any WebUI page in a persistent context")
    void shouldNavigateToAnyWebUIPageInAPersistentContext(BrowserType browserType, CreateUserDataDir createUserDataDir) {
      assumeFalse(!Env.headed(), "WebUI pages are not available in headless");

      BrowserContext context = browserType.launchPersistentContext(createUserDataDir.create(), Env.launchPersistentContextOptions());
      try {
        Page page = context.newPage();
        // Committing the navigation is all this asserts - waiting for the WebUI to load is slow and beside the point.
        Response response = page.navigate("chrome://extensions", new Page.NavigateOptions().setWaitUntil(com.microsoft.playwright.options.WaitUntilState.COMMIT));
        assertEquals(200, response.status());
      } finally {
        context.close();
      }
    }
  }

  @PlaywrightTest
  @DisplayName("CR-031 should fire dialogclosed event when dialog is closed out of band")
  void shouldFireDialogclosedEventWhenDialogIsClosedOutOfBand(Page page) throws Exception {
    // Establish the CDP session up front: creating one while a dialog is blocking the page hangs.
    CDPSession client = page.context().newCDPSession(page);
    client.send("Page.enable");
    // Java: the dialog promises are futures that the handlers complete; the handler leaves the dialog open.
    CompletableFuture<Dialog> dialogPromise = new CompletableFuture<>();
    page.onDialog(dialogPromise::complete);
    CompletableFuture<Dialog> closedPromise = new CompletableFuture<>();
    page.onDialogClosed(closedPromise::complete);
    // Java: the evaluate runs on a thread of its own, it is blocked by the dialog.
    CompletableFuture<Object> evaluatePromise = async(() -> page.evaluate("() => alert('yo')"));
    page.waitForCondition(dialogPromise::isDone);
    Dialog dialog = dialogPromise.get();
    // Handle the dialog out of band, similar to the user closing it in the headed browser.
    JsonObject params = new JsonObject();
    params.addProperty("accept", true);
    client.send("Page.handleJavaScriptDialog", params);
    page.waitForCondition(closedPromise::isDone);
    assertSame(dialog, closedPromise.get());
    evaluatePromise.get();
  }
}
