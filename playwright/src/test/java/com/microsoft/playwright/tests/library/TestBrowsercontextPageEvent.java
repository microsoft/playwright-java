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
import com.microsoft.playwright.Page;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.KeyboardModifier;
import com.microsoft.playwright.options.LoadState;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/library/browsercontext-page-event.spec.ts. */
public class TestBrowsercontextPageEvent {
  @PlaywrightTest
  @DisplayName("BC-062 should have url")
  void shouldHaveUrl(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    Page otherPage = context.waitForPage(() -> page.evaluate("url => window.open(url)", server.EMPTY_PAGE));
    assertEquals(server.EMPTY_PAGE, otherPage.url());
    context.close();
  }

  @PlaywrightTest
  @DisplayName("BC-063 should have url after domcontentloaded")
  void shouldHaveUrlAfterDomcontentloaded(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    Page otherPage = context.waitForPage(() -> page.evaluate("url => window.open(url)", server.EMPTY_PAGE));
    otherPage.waitForLoadState(LoadState.DOMCONTENTLOADED);
    assertEquals(server.EMPTY_PAGE, otherPage.url());
    context.close();
  }

  @PlaywrightTest
  @DisplayName("BC-064 should have about:blank url with domcontentloaded")
  void shouldHaveAboutBlankUrlWithDomcontentloaded(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    Page otherPage = context.waitForPage(() -> page.evaluate("url => window.open(url)", "about:blank"));
    otherPage.waitForLoadState(LoadState.DOMCONTENTLOADED);
    assertEquals("about:blank", otherPage.url());
    context.close();
  }

  @PlaywrightTest
  @DisplayName("BC-065 should have about:blank for empty url with domcontentloaded")
  void shouldHaveAboutBlankForEmptyUrlWithDomcontentloaded(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    Page otherPage = context.waitForPage(() -> page.evaluate("() => window.open()"));
    otherPage.waitForLoadState(LoadState.DOMCONTENTLOADED);
    assertEquals("about:blank", otherPage.url());
    context.close();
  }

  @PlaywrightTest
  @DisplayName("BC-066 should report when a new page is created and closed")
  void shouldReportWhenANewPageIsCreatedAndClosed(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    Page otherPage = context.waitForPage(() -> page.evaluate("url => window.open(url)", server.CROSS_PROCESS_PREFIX + "/empty.html"));
    // The url is about:blank in FF when 'page' event is fired.
    assertTrue(otherPage.url().contains(server.CROSS_PROCESS_PREFIX));
    assertEquals("Hello world", otherPage.evaluate("() => ['Hello', 'world'].join(' ')"));
    assertNotNull(otherPage.querySelector("body"));

    List<Page> allPages = context.pages();
    assertTrue(allPages.contains(page));
    assertTrue(allPages.contains(otherPage));

    boolean[] closeEventReceived = {false};
    otherPage.onClose(new Consumer<Page>() {
      @Override
      public void accept(Page p) {
        otherPage.offClose(this);
        closeEventReceived[0] = true;
      }
    });
    otherPage.close();
    assertTrue(closeEventReceived[0]);

    allPages = context.pages();
    assertTrue(allPages.contains(page));
    assertFalse(allPages.contains(otherPage));
    context.close();
  }

  @PlaywrightTest
  @DisplayName("BC-067 should report initialized pages")
  void shouldReportInitializedPages(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext();
    Page newPage = context.waitForPage(() -> context.newPage());
    assertEquals("about:blank", newPage.url());

    Page popup = context.waitForPage(() -> newPage.evaluate("() => window.open('about:blank')"));
    assertEquals("about:blank", popup.url());
    context.close();
  }

  @PlaywrightTest
  @DisplayName("BC-068 should not crash while redirecting of original request was missed")
  void shouldNotCrashWhileRedirectingOfOriginalRequestWasMissed(Browser browser, TestServer server) throws Exception {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    TestServer.Response[] serverResponse = {null};
    server.setRoute("/one-style.css", (req, res) -> serverResponse[0] = res);
    // Open a new page. Use window.open to connect to the page later.
    CompletableFuture<TestServer.Request> styleRequest = server.waitForRequest("/one-style.css");
    Page newPage = context.waitForPage(() -> page.evaluate("url => window.open(url)", server.PREFIX + "/one-style.html"));
    styleRequest.get();
    // Issue a redirect.
    serverResponse[0].writeHead(302, "location", "/injectedstyle.css");
    serverResponse[0].end();
    newPage.waitForLoadState(LoadState.DOMCONTENTLOADED);
    assertEquals(server.PREFIX + "/one-style.html", newPage.url());
    // Cleanup.
    context.close();
  }

  @PlaywrightTest
  @DisplayName("BC-069 should have an opener")
  void shouldHaveAnOpener(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    Page popup = context.waitForPage(() -> page.navigate(server.PREFIX + "/popup/window-open.html"));
    assertEquals(server.PREFIX + "/popup/popup.html", popup.url());
    assertEquals(page, popup.opener());
    assertNull(page.opener());
    context.close();
  }

  @PlaywrightTest
  @DisplayName("BC-070 should fire page lifecycle events")
  void shouldFirePageLifecycleEvents(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext();
    List<String> events = new ArrayList<>();
    context.onPage(page -> {
      events.add("CREATED: " + page.url());
      page.onClose(p -> events.add("DESTROYED: " + page.url()));
    });
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    page.close();
    assertEquals(Arrays.asList(
      "CREATED: about:blank",
      "DESTROYED: " + server.EMPTY_PAGE), events);
    context.close();
  }

  @PlaywrightTest
  @DisplayName("BC-071 should work with Shift-clicking")
  void shouldWorkWithShiftClicking(Browser browser, TestServer server, BrowserName browserName) {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<a href=\"/one-style.html\">yo</a>");
    Page popup = context.waitForPage(() -> page.click("a", new Page.ClickOptions().setModifiers(Arrays.asList(KeyboardModifier.SHIFT))));
    assertNull(popup.opener());
    context.close();
  }

  @PlaywrightTest
  @DisplayName("BC-072 should work with Ctrl-clicking")
  void shouldWorkWithCtrlClicking(Browser browser, TestServer server, BrowserName browserName) {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<a href=\"/one-style.html\">yo</a>");
    Page popup = context.waitForPage(() -> page.click("a", new Page.ClickOptions().setModifiers(Arrays.asList(KeyboardModifier.CONTROLORMETA))));
    assertEquals(browserName == BrowserName.FIREFOX ? page : null, popup.opener());
    context.close();
  }
}
