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

import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.microsoft.playwright.fixtures.Utils.async;
import static com.microsoft.playwright.fixtures.Utils.asset;
import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/page/page-history.spec.ts. */
public class TestPageHistory {
  /** Java: this is {@code await expect.poll(() => value).toEqual(expected)} with the 5s expect timeout. */
  private static void expectPollToEqual(Page page, java.util.function.Supplier<Object> value, Object expected) {
    long deadline = System.currentTimeMillis() + 5000;
    Object received = value.get();
    while (!expected.equals(received) && System.currentTimeMillis() < deadline) {
      page.waitForTimeout(100);
      received = value.get();
    }
    assertEquals(expected, received);
  }

  @PlaywrightTest
  @DisplayName("NA-071 page.goBack should work @smoke")
  void pageGoBackShouldWork(Page page, TestServer server) {
    assertNull(page.goBack());

    page.navigate(server.EMPTY_PAGE);
    page.navigate(server.PREFIX + "/grid.html");

    Response response = page.goBack();
    assertEquals(true, response.ok());
    assertTrue(response.url().contains(server.EMPTY_PAGE));

    response = page.goForward();
    assertEquals(true, response.ok());
    assertTrue(response.url().contains("/grid.html"));

    response = page.goForward();
    assertNull(response);
  }

  @PlaywrightTest
  @DisplayName("NA-072 page.goBack should work with HistoryAPI")
  void pageGoBackShouldWorkWithHistoryAPI(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.evaluate("() => {\n" +
      "  history.pushState({}, '', '/first.html');\n" +
      "  history.pushState({}, '', '/second.html');\n" +
      "}");
    assertEquals(server.PREFIX + "/second.html", page.url());

    page.goBack();
    assertEquals(server.PREFIX + "/first.html", page.url());
    page.goBack();
    assertEquals(server.EMPTY_PAGE, page.url());
    page.goForward();
    assertEquals(server.PREFIX + "/first.html", page.url());
  }

  @PlaywrightTest
  @DisplayName("NA-073 page.goBack should work for file urls")
  void pageGoBackShouldWorkForFileUrls(Page page, TestServer server) {
    assumeFalse("webkit-wsl".equals(Env.channel()));

    String url1 = asset("consolelog.html").toUri().toString();
    String url2 = server.PREFIX + "/consolelog.html";
    page.waitForConsoleMessage(new Page.WaitForConsoleMessageOptions().setPredicate(message -> message.text().equals("here:" + url1)),
      () -> page.navigate(url1));
    page.setContent("<a href='" + url2 + "'>url2</a>");
    assertEquals(url1.toLowerCase(), page.url().toLowerCase());

    page.waitForConsoleMessage(new Page.WaitForConsoleMessageOptions().setPredicate(message -> message.text().equals("here:" + url2)),
      () -> page.click("a"));
    assertEquals(url2, page.url());

    page.waitForConsoleMessage(new Page.WaitForConsoleMessageOptions().setPredicate(message -> message.text().equals("here:" + url1)),
      () -> page.goBack());
    assertEquals(url1.toLowerCase(), page.url().toLowerCase());
    // Should be able to evaluate in the new context, and
    // not reach for the old cross-process one.
    assertEquals(0, page.evaluate("() => window.scrollX"));
    // Should be able to screenshot.
    page.screenshot();

    page.waitForConsoleMessage(new Page.WaitForConsoleMessageOptions().setPredicate(message -> message.text().equals("here:" + url2)),
      () -> page.goForward());
    assertEquals(url2, page.url());
    assertEquals(0, page.evaluate("() => window.scrollX"));
    page.screenshot();
  }

  @PlaywrightTest
  @DisplayName("NA-074 goBack/goForward should work with bfcache-able pages")
  void goBackGoForwardShouldWorkWithBfcacheAblePages(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/cached/bfcached.html");
    page.setContent("<a href=\"" + server.PREFIX + "/cached/bfcached.html?foo\">click me</a>");
    page.click("a");

    Response response = page.goBack();
    assertEquals(server.PREFIX + "/cached/bfcached.html", response.url());
    // BFCache should be disabled.
    assertEquals(mapOf("persisted", false), page.evaluate("window.didShow"));

    response = page.goForward();
    assertEquals(server.PREFIX + "/cached/bfcached.html?foo", response.url());
  }

  @PlaywrightTest
  @DisplayName("NA-075 page.reload should work")
  void pageReloadShouldWork(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.evaluate("() => window['_foo'] = 10");
    page.reload();
    assertNull(page.evaluate("() => window['_foo']"));
  }

  @PlaywrightTest
  @DisplayName("NA-076 page.reload should work with data url")
  void pageReloadShouldWorkWithDataUrl(Page page, TestServer server) {
    page.navigate("data:text/html,hello");
    assertTrue(page.content().contains("hello"));
    assertNull(page.reload());
    assertTrue(page.content().contains("hello"));
  }

  @PlaywrightTest
  @DisplayName("NA-077 page.reload during renderer-initiated navigation")
  void pageReloadDuringRendererInitiatedNavigation(Page page, TestServer server) throws Exception {
    page.navigate(server.PREFIX + "/one-style.html");
    page.setContent("<form method='POST' action='/post'>Form is here<input type='submit'></form>");
    server.setRoute("/post", (req, res) -> {});

    CompletableFuture<Void> reloadFailedPromise = new CompletableFuture<>();
    // Java: page.once('request', async () => ...) is a handler that removes itself.
    page.onRequest(new Consumer<Request>() {
      @Override
      public void accept(Request request) {
        page.offRequest(this);
        try {
          page.reload();
        } catch (PlaywrightException e) {
          // .catch(e => {})
        }
        reloadFailedPromise.complete(null);
      }
    });
    // Java: the request handler runs inside the click call, on the test thread.
    try {
      page.click("input[type=submit]");
    } catch (PlaywrightException e) {
      // .catch(e => {})
    }
    reloadFailedPromise.get();

    // Form submit should be canceled, and reload should eventually arrive
    // to the original one-style.html.
    page.waitForSelector("text=hello");
  }

  @PlaywrightTest
  @DisplayName("NA-079 page.reload should work with same origin redirect")
  void pageReloadShouldWorkWithSameOriginRedirect(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    server.setRedirect("/empty.html", server.PREFIX + "/title.html");
    page.reload();
    assertThat(page).hasURL(server.PREFIX + "/title.html");
  }

  @PlaywrightTest
  @DisplayName("NA-080 page.reload should work with cross-origin redirect")
  void pageReloadShouldWorkWithCrossOriginRedirect(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    server.setRedirect("/empty.html", server.CROSS_PROCESS_PREFIX + "/title.html");
    page.reload();
    assertThat(page).hasURL(server.CROSS_PROCESS_PREFIX + "/title.html");
  }

  @PlaywrightTest
  @DisplayName("NA-081 page.reload should work on a page with a hash")
  void pageReloadShouldWorkOnAPageWithAHash(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE + "#hash");
    page.reload();
    assertThat(page).hasURL(server.EMPTY_PAGE + "#hash");
  }

  @PlaywrightTest
  @DisplayName("NA-082 page.reload should work on a page with a hash at the end")
  void pageReloadShouldWorkOnAPageWithAHashAtTheEnd(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE + "#");
    page.reload();
    assertThat(page).hasURL(server.EMPTY_PAGE + "#");
  }

  @PlaywrightTest
  @DisplayName("NA-083 page.goBack during renderer-initiated navigation")
  void pageGoBackDuringRendererInitiatedNavigation(Page page, TestServer server) throws Exception {
    page.navigate(server.PREFIX + "/one-style.html");
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<form method='POST' action='/post'>Form is here<input type='submit'></form>");
    server.setRoute("/post", (req, res) -> {});

    CompletableFuture<Void> reloadFailedPromise = new CompletableFuture<>();
    // Java: page.once('request', async () => ...) is a handler that removes itself.
    page.onRequest(new Consumer<Request>() {
      @Override
      public void accept(Request request) {
        page.offRequest(this);
        try {
          page.goBack();
        } catch (PlaywrightException e) {
          // .catch(e => {})
        }
        reloadFailedPromise.complete(null);
      }
    });
    // Java: the request handler runs inside the click call, on the test thread.
    try {
      page.click("input[type=submit]");
    } catch (PlaywrightException e) {
      // .catch(e => {})
    }
    reloadFailedPromise.get();

    // Form submit should be canceled, and goBack should eventually arrive
    // to the original one-style.html.
    page.waitForSelector("text=hello");
  }

  @PlaywrightTest
  @DisplayName("NA-084 page.goForward during renderer-initiated navigation")
  void pageGoForwardDuringRendererInitiatedNavigation(Page page, TestServer server) throws Exception {
    page.navigate(server.EMPTY_PAGE);
    page.navigate(server.PREFIX + "/one-style.html");
    page.goBack();

    page.setContent("<form method='POST' action='/post'>Form is here<input type='submit'></form>");
    server.setRoute("/post", (req, res) -> {});

    CompletableFuture<Void> reloadFailedPromise = new CompletableFuture<>();
    // Java: page.once('request', async () => ...) is a handler that removes itself.
    page.onRequest(new Consumer<Request>() {
      @Override
      public void accept(Request request) {
        page.offRequest(this);
        try {
          page.goForward();
        } catch (PlaywrightException e) {
          // .catch(e => {})
        }
        reloadFailedPromise.complete(null);
      }
    });
    // Java: the request handler runs inside the click call, on the test thread.
    try {
      page.click("input[type=submit]");
    } catch (PlaywrightException e) {
      // .catch(e => {})
    }
    reloadFailedPromise.get();

    // Form submit should be canceled, and goForward should eventually arrive
    // to the original one-style.html.
    page.waitForSelector("text=hello");
  }

  @PlaywrightTest
  @DisplayName("NA-085 regression test for issue 20791")
  void regressionTestForIssue20791(Page page, TestServer server) {
    assumeFalse("frozen".equals(System.getenv("PW_CLOCK")));
    server.setRoute("/iframe.html", (req, res) -> {
      res.writeHead(200, "content-type", "text/html; charset=utf-8");
      // iframe access parent frame to log a value from it.
      res.end("\n" +
        "      <!doctype html>\n" +
        "      <script type=\"text/javascript\">\n" +
        "        console.log(window.parent.foo);\n" +
        "      </script>\n" +
        "    ");
    });
    server.setRoute("/main.html", (req, res) -> {
      res.writeHead(200, "content-type", "text/html; charset=utf-8");
      res.end("\n" +
        "      <!doctype html>\n" +
        "      <iframe id=myframe src=\"about:blank\"></iframe>\n" +
        "      <script type=\"text/javascript\">\n" +
        "        setTimeout(() => window.foo = 'foo', 0);\n" +
        "        setTimeout(() => myframe.contentDocument.location.href = '" + server.PREFIX + "/iframe.html', 0);\n" +
        "      </script>\n" +
        "    ");
    });
    List<String> messages = Collections.synchronizedList(new ArrayList<String>());
    page.onConsoleMessage(msg -> messages.add(msg.text()));
    page.navigate(server.PREFIX + "/main.html");
    expectPollToEqual(page, () -> new ArrayList<>(messages), Arrays.asList("foo"));
    page.reload();
    expectPollToEqual(page, () -> new ArrayList<>(messages), Arrays.asList("foo", "foo"));
  }

  @PlaywrightTest
  @DisplayName("NA-086 should reload proper page")
  void shouldReloadProperPage(Page page, TestServer server) {
    AtomicInteger mainRequest = new AtomicInteger(0);
    AtomicInteger popupRequest = new AtomicInteger(0);
    server.setRoute("/main.html", (req, res) -> {
      res.writeHead(200, "content-type", "text/html; charset=utf-8");
      res.end("<!doctype html><h1>main: " + mainRequest.incrementAndGet() + "</h1>");
    });
    server.setRoute("/popup.html", (req, res) -> {
      res.writeHead(200, "content-type", "text/html; charset=utf-8");
      res.end("<!doctype html><h1>popup: " + popupRequest.incrementAndGet() + "</h1>");
    });
    page.navigate(server.PREFIX + "/main.html");
    Page popup = page.waitForPopup(() -> page.evaluate("() => window.open('/popup.html')"));
    assertThat(page.locator("h1")).hasText("main: 1");
    assertThat(popup.locator("h1")).hasText("popup: 1");

    page.reload();
    assertThat(page.locator("h1")).hasText("main: 2");
    assertThat(popup.locator("h1")).hasText("popup: 1");

    popup.reload();
    assertThat(page.locator("h1")).hasText("main: 2");
    assertThat(popup.locator("h1")).hasText("popup: 2");
  }
}
