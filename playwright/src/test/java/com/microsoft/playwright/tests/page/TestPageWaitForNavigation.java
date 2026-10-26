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

import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.HttpsTestServer;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.WaitUntilState;
import org.junit.jupiter.api.DisplayName;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static com.microsoft.playwright.fixtures.Utils.async;
import static com.microsoft.playwright.fixtures.Utils.cause;
import static com.microsoft.playwright.fixtures.Utils.expectedSSLError;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/page-wait-for-navigation.spec.ts. */
public class TestPageWaitForNavigation {
  @PlaywrightTest
  @DisplayName("WA-095 should work")
  void shouldWork(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    Response response = page.waitForNavigation(() -> {
      page.evaluate("url => window.location.href = url", server.PREFIX + "/grid.html");
    });
    assertEquals(true, response.ok());
    assertTrue(response.url().contains("grid.html"));
  }

  @PlaywrightTest
  @DisplayName("WA-096 should respect timeout")
  @Fixme("java: the message has no 'page.waitForNavigation:' prefix")
  void shouldRespectTimeout(Page page, TestServer server) {
    CompletableFuture<Response> promise = async(() -> page.waitForNavigation(
      new Page.WaitForNavigationOptions().setUrl("**/frame.html").setTimeout(5000), () -> {}));
    page.navigate(server.EMPTY_PAGE);
    Throwable error = cause(promise);
    assertTrue(error.getMessage().contains("page.waitForNavigation: Timeout 5000ms exceeded."));
    assertTrue(error.getMessage().contains("waiting for navigation to \"**/frame.html\" until \"load\""));
    assertTrue(error.getMessage().contains("navigated to \"" + server.EMPTY_PAGE + "\""));
  }

  @PlaywrightTest
  @DisplayName("WA-097 should work with both domcontentloaded and load")
  void shouldWorkWithBothDomcontentloadedAndLoad(Page page, TestServer server) throws Exception {
    AtomicReference<TestServer.Response> response = new AtomicReference<>();
    server.setRoute("/one-style.css", (req, res) -> response.set(res));
    CompletableFuture<TestServer.Request> cssRequest = server.waitForRequest("/one-style.css");
    CompletableFuture<Response> navigationPromise = async(() -> page.navigate(server.PREFIX + "/one-style.html"));
    CompletableFuture<Response> domContentLoadedPromise = async(() -> page.waitForNavigation(
      new Page.WaitForNavigationOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED), () -> {}));

    AtomicBoolean bothFired = new AtomicBoolean(false);
    CompletableFuture<Response> loadPromise = async(() -> page.waitForNavigation(
      new Page.WaitForNavigationOptions().setWaitUntil(WaitUntilState.LOAD), () -> {}));
    CompletableFuture<Void> bothFiredPromise = CompletableFuture.allOf(loadPromise, domContentLoadedPromise)
      .thenRun(() -> bothFired.set(true));

    cssRequest.get();
    domContentLoadedPromise.get();
    assertEquals(false, bothFired.get());
    response.get().end();
    bothFiredPromise.get();
    navigationPromise.get();
  }

  @PlaywrightTest
  @DisplayName("WA-098 should work with commit")
  void shouldWorkWithCommit(Page page, TestServer server) {
    server.setRoute("/script.js", (req, res) -> {});
    server.setRoute("/empty.html", (req, res) -> {
      res.setHeader("content-type", "text/html");
      res.end("<title>Hello</title><script src=\"script.js\"></script>");
    });
    async(() -> {
      try {
        page.navigate(server.EMPTY_PAGE);
      } catch (PlaywrightException e) {
      }
      return null;
    });
    page.waitForNavigation(new Page.WaitForNavigationOptions().setWaitUntil(WaitUntilState.COMMIT), () -> {});
    assertEquals("Hello", page.title());
  }

  @PlaywrightTest
  @DisplayName("WA-099 should work with clicking on anchor links")
  void shouldWorkWithClickingOnAnchorLinks(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<a href='#foobar'>foobar</a>");
    Response response = page.waitForNavigation(() -> {
      page.click("a");
    });
    assertNull(response);
    assertEquals(server.EMPTY_PAGE + "#foobar", page.url());
  }

  @PlaywrightTest
  @DisplayName("WA-100 should work with clicking on links which do not commit navigation")
  void shouldWorkWithClickingOnLinksWhichDoNotCommitNavigation(Page page, TestServer server, HttpsTestServer httpsServer, BrowserName browserName) {
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<a href='" + httpsServer.EMPTY_PAGE + "'>foobar</a>");
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.waitForNavigation(() -> {
      page.click("a");
    }));
    assertTrue(expectedSSLError(browserName).matcher(error.getMessage()).find());
  }

  @PlaywrightTest
  @DisplayName("WA-101 should work with history.pushState()")
  void shouldWorkWithHistoryPushState(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.setContent("\n" +
      "    <a onclick='javascript:pushState()'>SPA</a>\n" +
      "    <script>\n" +
      "      function pushState() { history.pushState({}, '', 'wow.html') }\n" +
      "    </script>\n" +
      "  ");
    Response response = page.waitForNavigation(() -> {
      page.click("a");
    });
    assertNull(response);
    assertEquals(server.PREFIX + "/wow.html", page.url());
  }

  @PlaywrightTest
  @DisplayName("WA-102 should work with history.replaceState()")
  void shouldWorkWithHistoryReplaceState(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.setContent("\n" +
      "    <a onclick='javascript:replaceState()'>SPA</a>\n" +
      "    <script>\n" +
      "      function replaceState() { history.replaceState({}, '', '/replaced.html') }\n" +
      "    </script>\n" +
      "  ");
    Response response = page.waitForNavigation(() -> {
      page.click("a");
    });
    assertNull(response);
    assertEquals(server.PREFIX + "/replaced.html", page.url());
  }

  @PlaywrightTest
  @DisplayName("WA-103 should work with DOM history.back()/history.forward()")
  void shouldWorkWithDOMHistoryBackHistoryForward(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.setContent("\n" +
      "    <a id=back onclick='javascript:goBack()'>back</a>\n" +
      "    <a id=forward onclick='javascript:goForward()'>forward</a>\n" +
      "    <script>\n" +
      "      function goBack() { history.back(); }\n" +
      "      function goForward() { history.forward(); }\n" +
      "      history.pushState({}, '', '/first.html');\n" +
      "      history.pushState({}, '', '/second.html');\n" +
      "    </script>\n" +
      "  ");
    assertEquals(server.PREFIX + "/second.html", page.url());
    Response backResponse = page.waitForNavigation(() -> {
      page.click("a#back");
    });
    assertNull(backResponse);
    assertEquals(server.PREFIX + "/first.html", page.url());
    Response forwardResponse = page.waitForNavigation(() -> {
      page.click("a#forward");
    });
    assertNull(forwardResponse);
    assertEquals(server.PREFIX + "/second.html", page.url());
  }

  @PlaywrightTest
  @DisplayName("WA-104 should work when subframe issues window.stop()")
  @DisabledOnBrowser(value = BrowserName.WEBKIT, reason = "fixme: WebKit issues load event in some cases, but not always")
  void shouldWorkWhenSubframeIssuesWindowStop(Page page, TestServer server, BrowserName browserName) {
    server.setRoute("/frames/style.css", (req, res) -> {});
    AtomicBoolean done = new AtomicBoolean(false);
    // Java: the listeners are registered before the navigation starts, as the single-threaded Node.js client registers them before any event can arrive.
    CompletableFuture<Frame> frameAttached = new CompletableFuture<>();
    page.onFrameAttached(new Consumer<Frame>() {
      @Override
      public void accept(Frame f) {
        page.offFrameAttached(this);
        frameAttached.complete(f);
      }
    });
    CompletableFuture<Void> frameNavigated = new CompletableFuture<>();
    page.onFrameNavigated(f -> {
      if (frameAttached.isDone() && f == frameAttached.getNow(null)) {
        frameNavigated.complete(null);
      }
    });
    async(() -> {
      try {
        page.navigate(server.PREFIX + "/frames/one-frame.html");
        done.set(true);
      } catch (PlaywrightException e) {
      }
      return null;
    });
    // Java: events are dispatched only while a Playwright call waits, so awaiting a promise that a page event settles is waitForCondition.
    page.waitForCondition(frameAttached::isDone);
    Frame frame = frameAttached.getNow(null);
    page.waitForCondition(frameNavigated::isDone);
    frame.evaluate("() => window.stop()");
    assertEquals(true, done.get());
  }

  @PlaywrightTest
  @DisplayName("WA-105 should work with url match")
  void shouldWorkWithUrlMatch(Page page, TestServer server) throws Exception {
    AtomicReference<Response> response1 = new AtomicReference<>();
    CompletableFuture<Object> response1Promise = async(() -> {
      response1.set(page.waitForNavigation(new Page.WaitForNavigationOptions().setUrl(Pattern.compile("one-style\\.html")), () -> {}));
      return null;
    });
    AtomicReference<Response> response2 = new AtomicReference<>();
    CompletableFuture<Object> response2Promise = async(() -> {
      response2.set(page.waitForNavigation(new Page.WaitForNavigationOptions().setUrl(Pattern.compile("/frame.html")), () -> {}));
      return null;
    });
    AtomicReference<Response> response3 = new AtomicReference<>();
    CompletableFuture<Object> response3Promise = async(() -> {
      response3.set(page.waitForNavigation(new Page.WaitForNavigationOptions().setUrl(url -> "bar".equals(searchParam(url, "foo"))), () -> {}));
      return null;
    });
    assertNull(response1.get());
    assertNull(response2.get());
    assertNull(response3.get());
    page.navigate(server.EMPTY_PAGE);
    assertNull(response1.get());
    assertNull(response2.get());
    assertNull(response3.get());
    page.navigate(server.PREFIX + "/frame.html");
    assertNull(response1.get());
    response2Promise.get();
    assertNotNull(response2.get());
    assertNull(response3.get());
    page.navigate(server.PREFIX + "/one-style.html");
    response1Promise.get();
    assertNotNull(response1.get());
    assertNotNull(response2.get());
    assertNull(response3.get());
    page.navigate(server.PREFIX + "/frame.html?foo=bar");
    response3Promise.get();
    assertNotNull(response1.get());
    assertNotNull(response2.get());
    assertNotNull(response3.get());
    page.navigate(server.PREFIX + "/empty.html");
    assertEquals(server.PREFIX + "/one-style.html", response1.get().url());
    assertEquals(server.PREFIX + "/frame.html", response2.get().url());
    assertEquals(server.PREFIX + "/frame.html?foo=bar", response3.get().url());
  }

  @PlaywrightTest
  @DisplayName("WA-106 should work with url match for same document navigations")
  void shouldWorkWithUrlMatchForSameDocumentNavigations(Page page, TestServer server) throws Exception {
    page.navigate(server.EMPTY_PAGE);
    AtomicBoolean resolved = new AtomicBoolean(false);
    CompletableFuture<Object> waitPromise = async(() -> {
      page.waitForNavigation(new Page.WaitForNavigationOptions().setUrl(Pattern.compile("third\\.html")), () -> {});
      resolved.set(true);
      return null;
    });
    assertEquals(false, resolved.get());
    page.evaluate("() => {\n" +
      "  history.pushState({}, '', '/first.html');\n" +
      "}");
    assertEquals(false, resolved.get());
    page.evaluate("() => {\n" +
      "  history.pushState({}, '', '/second.html');\n" +
      "}");
    assertEquals(false, resolved.get());
    page.evaluate("() => {\n" +
      "  history.pushState({}, '', '/third.html');\n" +
      "}");
    waitPromise.get();
    assertEquals(true, resolved.get());
  }

  @PlaywrightTest
  @DisplayName("WA-107 should work for cross-process navigations")
  void shouldWorkForCrossProcessNavigations(Page page, TestServer server) throws Exception {
    page.navigate(server.EMPTY_PAGE);
    CompletableFuture<Response> waitPromise = async(() -> page.waitForNavigation(
      new Page.WaitForNavigationOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED), () -> {}));
    String url = server.CROSS_PROCESS_PREFIX + "/empty.html";
    CompletableFuture<Response> gotoPromise = async(() -> page.navigate(url));
    Response response = waitPromise.get();
    assertEquals(url, response.url());
    assertEquals(url, page.url());
    assertEquals(url, page.evaluate("document.location.href"));
    gotoPromise.get();
  }

  @PlaywrightTest
  @DisplayName("WA-108 should work on frame")
  void shouldWorkOnFrame(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/frames/one-frame.html");
    Frame frame = page.frames().get(1);
    Response response = frame.waitForNavigation(() -> {
      frame.evaluate("url => window.location.href = url", server.PREFIX + "/grid.html");
    });
    assertEquals(true, response.ok());
    assertTrue(response.url().contains("grid.html"));
    assertEquals(frame, response.frame());
    assertTrue(page.url().contains("/frames/one-frame.html"));
  }

  @PlaywrightTest
  @Fixme("java: the message is 'Navigating frame was detached' without the call log 'waiting for navigation until \"load\"'")
  @DisplayName("WA-109 should fail when frame detaches")
  void shouldFailWhenFrameDetaches(Page page, TestServer server) throws Exception {
    page.navigate(server.PREFIX + "/frames/one-frame.html");
    Frame frame = page.frames().get(1);
    server.setRoute("/empty.html", (req, res) -> {});
    server.setRoute("/one-style.css", (req, res) -> {});
    CompletableFuture<TestServer.Request> cssRequest = server.waitForRequest("/one-style.css");
    CompletableFuture<Response> navigation = async(() -> frame.waitForNavigation(() -> {}));
    page.evalOnSelector("iframe", "frame => { frame.contentWindow.location.href = '/one-style.html'; }");
    // Make sure policy checks pass and navigation actually begins before removing the frame to avoid other errors
    cssRequest.get();
    page.evalOnSelector("iframe", "frame => window.builtins.setTimeout(() => frame.remove(), 0)");
    Throwable error = cause(navigation);
    assertTrue(error.getMessage().contains("waiting for navigation until \"load\""));
    assertTrue(error.getMessage().contains("frame was detached"));
  }

  private static String searchParam(String url, String name) {
    String query = URI.create(url).getQuery();
    if (query == null) {
      return null;
    }
    for (String pair : query.split("&")) {
      String[] parts = pair.split("=", 2);
      if (parts[0].equals(name)) {
        return parts.length > 1 ? parts[1] : "";
      }
    }
    return null;
  }
}
