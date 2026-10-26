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

package com.microsoft.playwright.tests.page;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitUntilState;
import org.junit.jupiter.api.DisplayName;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.microsoft.playwright.fixtures.Utils.async;
import static com.microsoft.playwright.fixtures.Utils.browserMajorVersion;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/page-wait-for-load-state.spec.ts. */
public class TestPageWaitForLoadState {
  @PlaywrightTest
  @DisplayName("WA-077 should pick up ongoing navigation")
  void shouldPickUpOngoingNavigation(Page page, TestServer server) throws Exception {
    AtomicReference<TestServer.Response> response = new AtomicReference<>();
    server.setRoute("/one-style.css", (req, res) -> response.set(res));
    CompletableFuture<TestServer.Request> request = server.waitForRequest("/one-style.css");
    page.navigate(server.PREFIX + "/one-style.html", new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
    request.get();
    CompletableFuture<Object> waitPromise = async(() -> {
      page.waitForLoadState();
      return null;
    });
    response.get().statusCode = 404;
    response.get().end("Not found");
    waitPromise.get();
  }

  @PlaywrightTest
  @DisplayName("WA-078 should respect timeout")
  @Fixme("java: the message has no 'page.waitForLoadState:' prefix")
  void shouldRespectTimeout(Page page, TestServer server) {
    server.setRoute("/one-style.css", (req, res) -> {});
    page.navigate(server.PREFIX + "/one-style.html", new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(1)));
    assertTrue(error.getMessage().contains("page.waitForLoadState: Timeout 1ms exceeded."));
    // Java: error.stack becomes the Java stack trace, which must lead to the call in this file.
    boolean found = false;
    for (StackTraceElement element : error.getStackTrace()) {
      found = found || element.getClassName().startsWith(TestPageWaitForLoadState.class.getName());
    }
    assertTrue(found);
  }

  @PlaywrightTest
  @DisplayName("WA-079 should resolve immediately if loaded")
  void shouldResolveImmediatelyIfLoaded(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/one-style.html");
    page.waitForLoadState();
  }

  // WA-080 should throw for bad state: LoadState is an enum in Java, 'bad' can not be passed.

  @PlaywrightTest
  @DisplayName("WA-081 should resolve immediately if load state matches")
  void shouldResolveImmediatelyIfLoadStateMatches(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    server.setRoute("/one-style.css", (req, res) -> {});
    page.navigate(server.PREFIX + "/one-style.html", new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
    page.waitForLoadState(LoadState.DOMCONTENTLOADED);
  }

  @PlaywrightTest
  @DisplayName("WA-082 should work with pages that have loaded before being connected to")
  void shouldWorkWithPagesThatHaveLoadedBeforeBeingConnectedTo(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    Page popup = page.waitForPopup(() -> {
      page.evaluate("() => window['_popup'] = window.open(document.location.href)");
    });
    // The url is about:blank in FF.
    // expect(popup.url()).toBe(server.EMPTY_PAGE);
    popup.waitForLoadState();
    assertEquals(server.EMPTY_PAGE, popup.url());
  }

  @PlaywrightTest
  @DisplayName("WA-083 should wait for load state of empty url popup")
  void shouldWaitForLoadStateOfEmptyUrlPopup(Page page, Browser browser, BrowserName browserName) {
    AtomicReference<Object> readyState = new AtomicReference<>();
    Page popup = page.waitForPopup(() -> {
      readyState.set(page.evaluate("() => {\n" +
        "  const popup = window.open('');\n" +
        "  return popup.document.readyState;\n" +
        "}"));
    });
    popup.waitForLoadState();
    boolean isOldFirefox = browserName == BrowserName.FIREFOX && browserMajorVersion(browser) < 148;
    assertEquals(isOldFirefox && !Env.isBidi() ? "uninitialized" : "complete", readyState.get());
    assertEquals(isOldFirefox && !Env.isBidi() ? "uninitialized" : "complete", popup.evaluate("() => document.readyState"));
  }

  @PlaywrightTest
  @DisplayName("WA-084 should wait for load state of about:blank popup ")
  void shouldWaitForLoadStateOfAboutBlankPopup(Page page) {
    Page popup = page.waitForPopup(() -> {
      page.evaluate("() => window.open('about:blank') && 1");
    });
    popup.waitForLoadState();
    assertEquals("complete", popup.evaluate("() => document.readyState"));
  }

  @PlaywrightTest
  @DisplayName("WA-085 should wait for load state of about:blank popup with noopener ")
  void shouldWaitForLoadStateOfAboutBlankPopupWithNoopener(Page page) {
    Page popup = page.waitForPopup(() -> {
      page.evaluate("() => window.open('about:blank', null, 'noopener') && 1");
    });
    popup.waitForLoadState();
    assertEquals("complete", popup.evaluate("() => document.readyState"));
  }

  @PlaywrightTest
  @DisplayName("WA-086 should wait for load state of popup with network url ")
  void shouldWaitForLoadStateOfPopupWithNetworkUrl(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    Page popup = page.waitForPopup(() -> {
      page.evaluate("url => window.open(url) && 1", server.EMPTY_PAGE);
    });
    popup.waitForLoadState();
    assertEquals("complete", popup.evaluate("() => document.readyState"));
  }

  @PlaywrightTest
  @DisplayName("WA-087 should wait for load state of popup with network url and noopener ")
  void shouldWaitForLoadStateOfPopupWithNetworkUrlAndNoopener(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    Page popup = page.waitForPopup(() -> {
      page.evaluate("url => window.open(url, null, 'noopener') && 1", server.EMPTY_PAGE);
    });
    popup.waitForLoadState();
    assertEquals("complete", popup.evaluate("() => document.readyState"));
  }

  @PlaywrightTest
  @DisplayName("WA-088 should work with clicking target=_blank")
  void shouldWorkWithClickingTargetBlank(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<a target=_blank rel=\"opener\" href=\"/one-style.html\">yo</a>");
    Page popup = page.waitForPopup(() -> {
      page.click("a");
    });
    popup.waitForLoadState();
    assertEquals("complete", popup.evaluate("() => document.readyState"));
  }

  @PlaywrightTest
  @DisplayName("WA-089 should wait for load state of newPage")
  void shouldWaitForLoadStateOfNewPage(Page page) {
    Page newPage = page.context().waitForPage(() -> {
      page.context().newPage();
    });
    newPage.waitForLoadState();
    assertEquals("complete", newPage.evaluate("() => document.readyState"));
  }

  @PlaywrightTest
  @DisplayName("WA-090 should resolve after popup load")
  void shouldResolveAfterPopupLoad(Page page, TestServer server) throws Exception {
    page.navigate(server.EMPTY_PAGE);
    // Stall the 'load' by delaying css.
    AtomicReference<TestServer.Response> cssResponse = new AtomicReference<>();
    server.setRoute("/one-style.css", (req, res) -> cssResponse.set(res));
    CompletableFuture<TestServer.Request> cssRequest = server.waitForRequest("/one-style.css");
    Page popup = page.waitForPopup(() -> {
      page.evaluate("url => window['popup'] = window.open(url)", server.PREFIX + "/one-style.html");
    });
    cssRequest.get();
    AtomicBoolean resolved = new AtomicBoolean(false);
    CompletableFuture<Boolean> loadSatePromise = async(() -> {
      popup.waitForLoadState();
      resolved.set(true);
      return true;
    });
    // Round trips!
    for (int i = 0; i < 5; i++) {
      page.evaluate("window");
    }
    assertEquals(false, resolved.get());
    cssResponse.get().end("");
    loadSatePromise.get();
    assertEquals(true, resolved.get());
    assertEquals(server.PREFIX + "/one-style.html", popup.url());
  }

  @PlaywrightTest
  @DisplayName("WA-091 should work for frame")
  void shouldWorkForFrame(Page page, TestServer server) throws Exception {
    page.navigate(server.PREFIX + "/frames/one-frame.html");
    Frame frame = page.frames().get(1);

    CompletableFuture<Route> requestPromise = new CompletableFuture<>();
    page.route(server.PREFIX + "/one-style.css", route -> requestPromise.complete(route));
    frame.navigate(server.PREFIX + "/one-style.html", new Frame.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
    // Java: events are dispatched only while a Playwright call waits, so awaiting a promise that a page event settles is waitForCondition.
    page.waitForCondition(requestPromise::isDone);
    Route request = requestPromise.get();
    AtomicBoolean resolved = new AtomicBoolean(false);
    CompletableFuture<Boolean> loadPromise = async(() -> {
      frame.waitForLoadState();
      resolved.set(true);
      return true;
    });
    // give the promise a chance to resolve, even though it shouldn't
    page.evaluate("1");
    assertEquals(false, resolved.get());
    request.resume();
    loadPromise.get();
  }

  @PlaywrightTest
  @DisplayName("WA-092 should work with javascript: iframe")
  void shouldWorkWithJavascriptIframe(Page page, TestServer server, BrowserName browserName) {
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<iframe src=\"javascript:false\"></iframe>", new Page.SetContentOptions().setWaitUntil(WaitUntilState.COMMIT));
    page.waitForLoadState(LoadState.DOMCONTENTLOADED);
    page.waitForLoadState(LoadState.LOAD);
    page.waitForLoadState(LoadState.NETWORKIDLE);
  }

  @PlaywrightTest
  @DisplayName("WA-093 should work with broken data-url iframe")
  void shouldWorkWithBrokenDataUrlIframe(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<iframe src=\"data:text/html\"></iframe>", new Page.SetContentOptions().setWaitUntil(WaitUntilState.COMMIT));
    page.waitForLoadState(LoadState.DOMCONTENTLOADED);
    page.waitForLoadState(LoadState.LOAD);
    page.waitForLoadState(LoadState.NETWORKIDLE);
  }

  @PlaywrightTest
  @DisplayName("WA-094 should work with broken blob-url iframe")
  void shouldWorkWithBrokenBlobUrlIframe(Page page, TestServer server, BrowserName browserName) {
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<iframe src=\"blob:\"></iframe>", new Page.SetContentOptions().setWaitUntil(WaitUntilState.COMMIT));
    page.waitForLoadState(LoadState.DOMCONTENTLOADED);
    page.waitForLoadState(LoadState.LOAD);
    page.waitForLoadState(LoadState.NETWORKIDLE);
  }
}
