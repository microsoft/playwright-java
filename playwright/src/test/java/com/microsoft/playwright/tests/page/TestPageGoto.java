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
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.HttpsTestServer;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.WaitUntilState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.microsoft.playwright.fixtures.Utils.asset;
import static com.microsoft.playwright.fixtures.Utils.expectedSSLError;
import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/page/page-goto.spec.ts. */
public class TestPageGoto {
  /** Java: this is {@code await expect(() => ...).toPass({ timeout })}, retried every 100ms. */
  private static void toPass(Page page, Runnable assertion, int timeout) {
    long deadline = System.currentTimeMillis() + timeout;
    while (true) {
      try {
        assertion.run();
        return;
      } catch (AssertionError e) {
        if (System.currentTimeMillis() > deadline) {
          throw e;
        }
      }
      page.waitForTimeout(100);
    }
  }

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
  @DisplayName("NA-001 should work @smoke")
  void shouldWork(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    assertEquals(server.EMPTY_PAGE, page.url());
  }

  @PlaywrightTest
  @DisplayName("NA-002 should work with file URL")
  void shouldWorkWithFileURL(Page page) {
    assumeFalse("webkit-wsl".equals(Env.channel()), "separate filesystem on wsl");

    // Java: url.pathToFileURL(path).href is Path.toUri().
    String fileurl = asset("empty.html").toUri().toString();
    page.navigate(fileurl);
    assertEquals(fileurl.toLowerCase(), page.url().toLowerCase());
    assertEquals(1, page.frames().size());
  }

  @PlaywrightTest
  @DisplayName("NA-003 should navigate from file URL to about:blank")
  void shouldNavigateFromFileURLToAboutBlank(Page page) {
    assumeFalse("webkit-wsl".equals(Env.channel()), "separate filesystem on wsl");

    page.navigate(asset("empty.html").toUri().toString());
    page.navigate("about:blank");
  }

  @PlaywrightTest
  @DisplayName("NA-004 should work with file URL with subframes")
  void shouldWorkWithFileURLWithSubframes(Page page) {
    assumeFalse("webkit-wsl".equals(Env.channel()), "separate filesystem on wsl");

    String fileurl = asset("frames/two-frames.html").toUri().toString();
    page.navigate(fileurl);
    assertEquals(fileurl.toLowerCase(), page.url().toLowerCase());
    assertEquals(3, page.frames().size());
  }

  @PlaywrightTest
  @DisplayName("NA-005 should use http for no protocol")
  void shouldUseHttpForNoProtocol(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE.substring("http://".length()));
    assertEquals(server.EMPTY_PAGE, page.url());
  }

  @PlaywrightTest
  @DisplayName("NA-006 should work cross-process")
  void shouldWorkCrossProcess(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    assertEquals(server.EMPTY_PAGE, page.url());

    String url = server.CROSS_PROCESS_PREFIX + "/empty.html";
    Frame[] requestFrame = {null};
    page.onRequest(r -> {
      if (r.url().equals(url)) {
        requestFrame[0] = r.frame();
      }
    });
    Response response = page.navigate(url);
    assertEquals(url, page.url());
    assertEquals(page.mainFrame(), response.frame());
    assertEquals(page.mainFrame(), requestFrame[0]);
    assertEquals(url, response.url());
  }

  @PlaywrightTest
  @DisplayName("NA-007 should work with cross-process that fails before committing")
  void shouldWorkWithCrossProcessThatFailsBeforeCommitting(Page page, TestServer server) {
    server.setRoute("/empty.html", (req, res) -> {
      req.socket().destroy();
    });
    Response response1 = page.navigate(server.CROSS_PROCESS_PREFIX + "/title.html");
    response1.finished();
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.navigate(server.EMPTY_PAGE));
    assertTrue(error instanceof Exception);
  }

  @PlaywrightTest
  @DisplayName("NA-008 should work with Cross-Origin-Opener-Policy")
  void shouldWorkWithCrossOriginOpenerPolicy(Page page, TestServer server) {
    server.setRoute("/empty.html", (req, res) -> {
      res.setHeader("Cross-Origin-Opener-Policy", "same-origin");
      // Note: without 'onload', Firefox sometimes does not fire the load event
      // over the protocol. The reason is unclear.
      res.end("\n" +
        "      <div>Hello there!</div>\n" +
        "      <script>window.onload = () => console.log('onload')</script>\n" +
        "    ");
    });
    Set<Object> requests = new HashSet<>();
    List<String> events = new ArrayList<>();
    page.onRequest(r -> {
      events.add("request");
      requests.add(r);
    });
    page.onRequestFailed(r -> {
      events.add("requestfailed");
      requests.add(r);
    });
    page.onRequestFinished(r -> {
      events.add("requestfinished");
      requests.add(r);
    });
    page.onResponse(r -> {
      events.add("response");
      requests.add(r.request());
    });
    Response response = page.navigate(server.EMPTY_PAGE);
    assertEquals(server.EMPTY_PAGE, page.url());
    response.finished();
    assertEquals(Arrays.asList("request", "response", "requestfinished"), events);
    assertEquals(1, requests.size());
    assertNull(response.request().failure());
  }

  @Nested
  @ParameterizedClass
  @NullSource
  @ValueSource(strings = {"require-corp"})
  class CrossOriginEmbedderPolicy {
    @Parameter
    String crossOriginEmbedderPolicy;

    @PlaywrightTest
    @DisplayName("NA-009 should work with Cross-Origin-Opener-Policy${crossOriginEmbedderPolicy ? ' and Cross-Origin-Embedder-Policy' : ''} and history state")
    void shouldWorkWithCrossOriginOpenerPolicyAndHistoryState(Page page, TestServer server) {
      server.setRoute("/empty.html", (req, res) -> {
        res.setHeader("Cross-Origin-Opener-Policy", "same-origin");
        if (crossOriginEmbedderPolicy != null) {
          res.setHeader("Cross-Origin-Embedder-Policy", crossOriginEmbedderPolicy);
        }
        res.end("\n" +
          "        <!doctype html>\n" +
          "        <h1>hello</h1>\n" +
          "        <script type=\"module\">history.replaceState({ key: 1 }, '', location.href);</script>\n" +
          "      ");
      });
      Response response = page.navigate(server.EMPTY_PAGE);
      assertEquals(200, response.status());
      assertEquals(server.EMPTY_PAGE, page.url());
      assertEquals(mapOf("key", 1), page.evaluate("() => history.state"));
      assertThat(page.locator("h1")).hasText("hello");
    }
  }

  @PlaywrightTest
  @DisplayName("NA-010 should work with Cross-Origin-Opener-Policy and interception")
  void shouldWorkWithCrossOriginOpenerPolicyAndInterception(Page page, TestServer server) {
    server.setRoute("/empty.html", (req, res) -> {
      res.setHeader("Cross-Origin-Opener-Policy", "same-origin");
      // Note: without 'onload', Firefox sometimes does not fire the load event
      // over the protocol. The reason is unclear.
      res.end("\n" +
        "      <div>Hello there!</div>\n" +
        "      <script>window.onload = () => console.log('onload')</script>\n" +
        "    ");
    });
    Set<Object> requests = new HashSet<>();
    List<String> events = new ArrayList<>();
    page.onRequest(r -> {
      events.add("request");
      requests.add(r);
    });
    page.onRequestFailed(r -> {
      events.add("requestfailed");
      requests.add(r);
    });
    page.onRequestFinished(r -> {
      events.add("requestfinished");
      requests.add(r);
    });
    page.onResponse(r -> {
      events.add("response");
      requests.add(r.request());
    });
    page.route("**/*", route -> {
      page.waitForTimeout(100);
      route.resume();
    });
    Response response = page.navigate(server.EMPTY_PAGE);
    assertEquals(server.EMPTY_PAGE, page.url());
    response.finished();
    assertEquals(Arrays.asList("request", "response", "requestfinished"), events);
    assertEquals(1, requests.size());
    assertNull(response.request().failure());
  }

  @PlaywrightTest
  @DisplayName("NA-011 should work with Cross-Origin-Opener-Policy after redirect")
  void shouldWorkWithCrossOriginOpenerPolicyAfterRedirect(Page page, TestServer server) {
    server.setRedirect("/redirect", "/empty.html");
    server.setRoute("/empty.html", (req, res) -> {
      res.setHeader("Cross-Origin-Opener-Policy", "same-origin");
      // Note: without 'onload', Firefox sometimes does not fire the load event
      // over the protocol. The reason is unclear.
      res.end("\n" +
        "      <div>Hello there!</div>\n" +
        "      <script>window.onload = () => console.log('onload')</script>\n" +
        "    ");
    });
    Set<Object> requests = new HashSet<>();
    List<String> events = new ArrayList<>();
    page.onRequest(r -> {
      events.add("request");
      requests.add(r);
    });
    page.onRequestFailed(r -> {
      events.add("requestfailed");
      requests.add(r);
    });
    page.onRequestFinished(r -> {
      events.add("requestfinished");
      requests.add(r);
    });
    page.onResponse(r -> {
      events.add("response");
      requests.add(r.request());
    });
    Response response = page.navigate(server.PREFIX + "/redirect");
    assertEquals(server.EMPTY_PAGE, page.url());
    response.finished();
    assertEquals(Arrays.asList("request", "response", "requestfinished", "request", "response", "requestfinished"), events);
    assertEquals(2, requests.size());
    assertNull(response.request().failure());
    Request firstRequest = response.request().redirectedFrom();
    assertNotNull(firstRequest);
    assertEquals(server.PREFIX + "/redirect", firstRequest.url());
  }

  @PlaywrightTest
  @DisplayName("NA-012 should capture iframe navigation request")
  void shouldCaptureIframeNavigationRequest(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    assertEquals(server.EMPTY_PAGE, page.url());

    Frame[] requestFrame = {null};
    page.onRequest(r -> {
      if (r.url().equals(server.PREFIX + "/frames/frame.html")) {
        requestFrame[0] = r.frame();
      }
    });
    Response response = page.navigate(server.PREFIX + "/frames/one-frame.html");
    assertEquals(server.PREFIX + "/frames/one-frame.html", page.url());
    assertEquals(page.mainFrame(), response.frame());
    assertEquals(server.PREFIX + "/frames/one-frame.html", response.url());

    assertEquals(2, page.frames().size());
    assertEquals(page.frames().get(1), requestFrame[0]);
  }

  @PlaywrightTest
  @DisplayName("NA-013 should capture cross-process iframe navigation request")
  void shouldCaptureCrossProcessIframeNavigationRequest(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    assertEquals(server.EMPTY_PAGE, page.url());

    Frame[] requestFrame = {null};
    page.onRequest(r -> {
      if (r.url().equals(server.CROSS_PROCESS_PREFIX + "/frames/frame.html")) {
        requestFrame[0] = r.frame();
      }
    });
    Response response = page.navigate(server.CROSS_PROCESS_PREFIX + "/frames/one-frame.html");
    assertEquals(server.CROSS_PROCESS_PREFIX + "/frames/one-frame.html", page.url());
    assertEquals(page.mainFrame(), response.frame());
    assertEquals(server.CROSS_PROCESS_PREFIX + "/frames/one-frame.html", response.url());

    assertEquals(2, page.frames().size());
    assertEquals(page.frames().get(1), requestFrame[0]);
  }

  @PlaywrightTest
  @DisplayName("NA-014 should work with anchor navigation")
  void shouldWorkWithAnchorNavigation(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    assertEquals(server.EMPTY_PAGE, page.url());
    page.navigate(server.EMPTY_PAGE + "#foo");
    assertEquals(server.EMPTY_PAGE + "#foo", page.url());
    page.navigate(server.EMPTY_PAGE + "#bar");
    assertEquals(server.EMPTY_PAGE + "#bar", page.url());
  }

  @PlaywrightTest
  @DisplayName("NA-015 should work with redirects")
  void shouldWorkWithRedirects(Page page, TestServer server) {
    server.setRedirect("/redirect/1.html", "/redirect/2.html");
    server.setRedirect("/redirect/2.html", "/empty.html");
    Response response = page.navigate(server.PREFIX + "/redirect/1.html");
    assertEquals(200, response.status());
    assertEquals(server.EMPTY_PAGE, page.url());
  }

  @PlaywrightTest
  @DisplayName("NA-016 should navigate to about:blank")
  void shouldNavigateToAboutBlank(Page page, TestServer server) {
    Response response = page.navigate("about:blank");
    assertNull(response);
  }

  @PlaywrightTest
  @DisplayName("NA-017 should return response when page changes its URL after load")
  void shouldReturnResponseWhenPageChangesItsURLAfterLoad(Page page, TestServer server) {
    Response response = page.navigate(server.PREFIX + "/historyapi.html");
    assertEquals(200, response.status());
  }

  @PlaywrightTest
  @DisplayName("NA-018 should work with subframes return 204")
  void shouldWorkWithSubframesReturn204(Page page, TestServer server) {
    server.setRoute("/frames/frame.html", (req, res) -> {
      res.statusCode = 204;
      res.end();
    });
    page.navigate(server.PREFIX + "/frames/one-frame.html");
  }

  @PlaywrightTest
  @DisplayName("NA-019 should work with subframes return 204 with domcontentloaded")
  void shouldWorkWithSubframesReturn204WithDomcontentloaded(Page page, TestServer server) {
    server.setRoute("/frames/frame.html", (req, res) -> {
      res.statusCode = 204;
      res.end();
    });
    page.navigate(server.PREFIX + "/frames/one-frame.html", new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
  }

  @PlaywrightTest
  @DisplayName("NA-020 should fail when server returns 204")
  @DisabledOnBrowser(value = BrowserName.WEBKIT, os = OS.LINUX, reason = "fixme: Regressed in https://github.com/microsoft/playwright-browsers/pull/1297")
  void shouldFailWhenServerReturns204(Page page, TestServer server, BrowserName browserName) {
    // WebKit just loads an empty page.
    server.setRoute("/empty.html", (req, res) -> {
      res.statusCode = 204;
      res.end();
    });
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.navigate(server.EMPTY_PAGE));
    if (browserName == BrowserName.CHROMIUM) {
      assertTrue(error.getMessage().contains("net::ERR_ABORTED"));
    } else if (browserName == BrowserName.WEBKIT) {
      assertTrue(error.getMessage().contains("Aborted: 204 No Content"));
    } else {
      assertTrue(error.getMessage().contains("NS_BINDING_ABORTED"));
    }
  }

  @PlaywrightTest
  @DisplayName("NA-021 should navigate to empty page with domcontentloaded")
  void shouldNavigateToEmptyPageWithDomcontentloaded(Page page, TestServer server) {
    Response response = page.navigate(server.EMPTY_PAGE, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
    assertEquals(200, response.status());
  }

  @PlaywrightTest
  @DisplayName("NA-022 should work when page calls history API in beforeunload")
  void shouldWorkWhenPageCallsHistoryAPIInBeforeunload(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.evaluate("() => {\n" +
      "  window.addEventListener('beforeunload', () => history.replaceState(null, 'initial', window.location.href), false);\n" +
      "}");
    Response response = page.navigate(server.PREFIX + "/grid.html");
    assertEquals(200, response.status());
  }

  @PlaywrightTest
  @DisplayName("NA-023 should fail when navigating to bad url")
  void shouldFailWhenNavigatingToBadUrl(Page page, BrowserName browserName) {
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.navigate("asdfasdf"));
    if (browserName == BrowserName.CHROMIUM && Env.isBidi()) {
      assertTrue(error.getMessage().contains("Invalid URL"));
    } else if (browserName == BrowserName.FIREFOX && Env.isBidi()) {
      assertTrue(error.getMessage().contains("NS_ERROR_MALFORMED_URI"));
    } else if (browserName == BrowserName.CHROMIUM || browserName == BrowserName.WEBKIT) {
      assertTrue(error.getMessage().contains("Cannot navigate to invalid URL"));
    } else {
      assertTrue(error.getMessage().contains("Invalid url"));
    }
  }

  @PlaywrightTest
  @DisplayName("NA-024 should fail when navigating to bad SSL")
  void shouldFailWhenNavigatingToBadSSL(Page page, BrowserName browserName, HttpsTestServer httpsServer) {
    // Make sure that network events do not emit 'undefined'.
    // @see https://crbug.com/750469
    page.onRequest(request -> assertNotNull(request));
    page.onRequestFinished(request -> assertNotNull(request));
    page.onRequestFailed(request -> assertNotNull(request));
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.navigate(httpsServer.EMPTY_PAGE));
    assertTrue(expectedSSLError(browserName).matcher(error.getMessage()).find());
  }

  @PlaywrightTest
  @DisplayName("NA-025 should fail when navigating to bad SSL after redirects")
  void shouldFailWhenNavigatingToBadSSLAfterRedirects(Page page, BrowserName browserName, TestServer server, HttpsTestServer httpsServer) {
    server.setRedirect("/redirect/1.html", "/redirect/2.html");
    server.setRedirect("/redirect/2.html", "/empty.html");
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.navigate(httpsServer.PREFIX + "/redirect/1.html"));
    assertTrue(expectedSSLError(browserName).matcher(error.getMessage()).find());
  }

  @PlaywrightTest
  @DisplayName("NA-026 should not crash when navigating to bad SSL after a cross origin navigation")
  void shouldNotCrashWhenNavigatingToBadSSLAfterACrossOriginNavigation(Page page, TestServer server, HttpsTestServer httpsServer) {
    page.navigate(server.CROSS_PROCESS_PREFIX + "/empty.html");
    try {
      page.navigate(httpsServer.EMPTY_PAGE);
    } catch (PlaywrightException e) {
      // void 0
    }
  }

  @PlaywrightTest
  @DisplayName("NA-029 should fail when main resources failed to load")
  void shouldFailWhenMainResourcesFailedToLoad(Page page, BrowserName browserName) {
    assumeFalse("webkit-wsl".equals(Env.channel()), "Networking mode mirrored ends up stalling connections rather than terminating them, see https://github.com/microsoft/WSL/issues/10855.");
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.navigate("http://localhost:44123/non-existing-url"));
    if (browserName == BrowserName.CHROMIUM) {
      assertTrue(error.getMessage().contains("net::ERR_CONNECTION_REFUSED"));
    } else if (browserName == BrowserName.WEBKIT && Env.isWindows && !"webkit-wsl".equals(Env.channel())) {
      assertTrue(error.getMessage().contains("Could not connect to server"));
    } else if (browserName == BrowserName.WEBKIT) {
      assertTrue(error.getMessage().contains("Could not connect"));
    } else {
      assertTrue(error.getMessage().contains("NS_ERROR_CONNECTION_REFUSED"));
    }
  }

  @PlaywrightTest
  @DisplayName("NA-030 should fail when exceeding maximum navigation timeout")
  @Fixme("java: the message is 'Timeout 1ms exceeded.' and the call log, without the 'page.goto: ' prefix")
  void shouldFailWhenExceedingMaximumNavigationTimeout(Page page, TestServer server) {
    // Hang for request to the empty.html
    server.setRoute("/empty.html", (req, res) -> {});
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.navigate(server.PREFIX + "/empty.html", new Page.NavigateOptions().setTimeout(1)));
    assertTrue(error.getMessage().contains("page.goto: Timeout 1ms exceeded."));
    assertTrue(error.getMessage().contains(server.PREFIX + "/empty.html"));
    assertTrue(error instanceof TimeoutError);
  }

  @PlaywrightTest
  @DisplayName("NA-031 should fail when exceeding default maximum navigation timeout")
  @Fixme("java: the message is 'Timeout 1ms exceeded.' and the call log, without the 'page.goto: ' prefix")
  void shouldFailWhenExceedingDefaultMaximumNavigationTimeout(Page page, TestServer server) {
    // Hang for request to the empty.html
    server.setRoute("/empty.html", (req, res) -> {});
    page.context().setDefaultNavigationTimeout(2);
    page.setDefaultNavigationTimeout(1);
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.navigate(server.PREFIX + "/empty.html"));
    assertTrue(error.getMessage().contains("page.goto: Timeout 1ms exceeded."));
    assertTrue(error.getMessage().contains(server.PREFIX + "/empty.html"));
    assertTrue(error instanceof TimeoutError);
  }

  @PlaywrightTest
  @DisplayName("NA-032 should fail when exceeding browser context navigation timeout")
  @Fixme("java: the message is 'Timeout 2ms exceeded.' and the call log, without the 'page.goto: ' prefix")
  void shouldFailWhenExceedingBrowserContextNavigationTimeout(Page page, TestServer server) {
    // Hang for request to the empty.html
    server.setRoute("/empty.html", (req, res) -> {});
    page.context().setDefaultNavigationTimeout(2);
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.navigate(server.PREFIX + "/empty.html"));
    assertTrue(error.getMessage().contains("page.goto: Timeout 2ms exceeded."));
    assertTrue(error.getMessage().contains(server.PREFIX + "/empty.html"));
    assertTrue(error instanceof TimeoutError);
  }

  @PlaywrightTest
  @DisplayName("NA-033 should fail when exceeding default maximum timeout")
  @Fixme("java: the message is 'Timeout 1ms exceeded.' and the call log, without the 'page.goto: ' prefix")
  void shouldFailWhenExceedingDefaultMaximumTimeout(Page page, TestServer server) {
    // Hang for request to the empty.html
    server.setRoute("/empty.html", (req, res) -> {});
    // Undo what harness did.
    // Java: the Java harness does not set a default navigation timeout, there is nothing to undo.
    page.context().setDefaultTimeout(2);
    page.setDefaultTimeout(1);
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.navigate(server.PREFIX + "/empty.html"));
    assertTrue(error.getMessage().contains("page.goto: Timeout 1ms exceeded."));
    assertTrue(error.getMessage().contains(server.PREFIX + "/empty.html"));
    assertTrue(error instanceof TimeoutError);
  }

  @PlaywrightTest
  @DisplayName("NA-034 should fail when exceeding browser context timeout")
  @Fixme("java: the message is 'Timeout 2ms exceeded.' and the call log, without the 'page.goto: ' prefix")
  void shouldFailWhenExceedingBrowserContextTimeout(Page page, TestServer server) {
    // Hang for request to the empty.html
    server.setRoute("/empty.html", (req, res) -> {});
    // Undo what harness did.
    // Java: the Java harness does not set a default navigation timeout, there is nothing to undo.
    page.context().setDefaultTimeout(2);
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.navigate(server.PREFIX + "/empty.html"));
    assertTrue(error.getMessage().contains("page.goto: Timeout 2ms exceeded."));
    assertTrue(error.getMessage().contains(server.PREFIX + "/empty.html"));
    assertTrue(error instanceof TimeoutError);
  }

  @PlaywrightTest
  @DisplayName("NA-035 should prioritize default navigation timeout over default timeout")
  @Fixme("java: the message is 'Timeout 1ms exceeded.' and the call log, without the 'page.goto: ' prefix")
  void shouldPrioritizeDefaultNavigationTimeoutOverDefaultTimeout(Page page, TestServer server) {
    // Hang for request to the empty.html
    server.setRoute("/empty.html", (req, res) -> {});
    page.setDefaultTimeout(0);
    page.setDefaultNavigationTimeout(1);
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.navigate(server.PREFIX + "/empty.html"));
    assertTrue(error.getMessage().contains("page.goto: Timeout 1ms exceeded."));
    assertTrue(error.getMessage().contains(server.PREFIX + "/empty.html"));
    assertTrue(error instanceof TimeoutError);
  }

  @PlaywrightTest
  @DisplayName("NA-036 should disable timeout when its set to 0")
  void shouldDisableTimeoutWhenItsSetTo0(Page page, TestServer server) {
    boolean[] loaded = {false};
    // Java: page.once('load', h) is a handler that removes itself.
    page.onLoad(new Consumer<Page>() {
      @Override
      public void accept(Page p) {
        page.offLoad(this);
        loaded[0] = true;
      }
    });
    page.navigate(server.PREFIX + "/grid.html", new Page.NavigateOptions().setTimeout(0).setWaitUntil(WaitUntilState.LOAD));
    assertEquals(true, loaded[0]);
  }

  @PlaywrightTest
  @DisplayName("NA-038 js redirect overrides url bar navigation ")
  @Fixme(value = "java: the message has no 'page.goto: ' prefix: 'Navigation to \"...\" is interrupted by another navigation to \"...\"'", browsers = BrowserName.WEBKIT)
  void jsRedirectOverridesUrlBarNavigation(Page page, TestServer server, BrowserName browserName) {
    server.setRoute("/a", (req, res) -> {
      res.writeHead(200, "content-type", "text/html");
      res.end("\n" +
        "        <body>\n" +
        "          <script>\n" +
        "            setTimeout(() => {\n" +
        "              window.location.pathname = '/c';\n" +
        "            }, 1000);\n" +
        "          </script>\n" +
        "        </body>\n" +
        "      ");
    });
    List<String> events = Collections.synchronizedList(new ArrayList<String>());
    server.setRoute("/b", (req, res) -> {
      events.add("started b");
      Thread.sleep(2000);
      res.writeHead(200, "content-type", "text/html");
      res.end("BBB");
      events.add("finished b");
    });
    server.setRoute("/c", (req, res) -> {
      events.add("started c");
      Thread.sleep(2000);
      res.writeHead(200, "content-type", "text/html");
      res.end("CCC");
      events.add("finished c");
    });
    page.navigate(server.PREFIX + "/a");
    PlaywrightException error = null;
    try {
      page.navigate(server.PREFIX + "/b");
    } catch (PlaywrightException e) {
      error = e;
    }
    List<String> expectEvents = (browserName == BrowserName.CHROMIUM) ?
      Arrays.asList("started b", "finished b") :
      Arrays.asList("started b", "started c", "finished b", "finished c");
    toPass(page, () -> assertEquals(expectEvents, events), 30000);
    assertEquals(expectEvents, events);
    if (browserName == BrowserName.CHROMIUM) {
      // Chromium prioritizes the url bar navigation over the js redirect.
      assertNull(error);
      assertThat(page).hasURL(server.PREFIX + "/b");
    } else if (browserName == BrowserName.WEBKIT) {
      assertTrue(error.getMessage().contains("page.goto: Navigation to \"" + server.PREFIX + "/b\" is interrupted by another navigation to \"" + server.PREFIX + "/c\""));
      assertThat(page).hasURL(server.PREFIX + "/c");
    } else if (browserName == BrowserName.FIREFOX) {
      assertTrue(error.getMessage().contains("NS_BINDING_ABORTED"));
      assertThat(page).hasURL(server.PREFIX + "/c");
    }
  }

  @PlaywrightTest
  @DisplayName("NA-039 should succeed on url bar navigation when there is pending navigation")
  void shouldSucceedOnUrlBarNavigationWhenThereIsPendingNavigation(Page page, TestServer server, BrowserName browserName) {
    assumeFalse("frozen".equals(System.getenv("PW_CLOCK")));
    server.setRoute("/a", (req, res) -> {
      res.writeHead(200, "content-type", "text/html");
      res.end("\n" +
        "      <body>\n" +
        "        <script>\n" +
        "          setTimeout(() => {\n" +
        "            window.location.pathname = '/c';\n" +
        "          }, 10);\n" +
        "        </script>\n" +
        "      </body>\n" +
        "    ");
    });
    List<String> events = Collections.synchronizedList(new ArrayList<String>());
    server.setRoute("/b", (req, res) -> {
      events.add("started b");
      Thread.sleep(2000);
      res.writeHead(200, "content-type", "text/html");
      res.end("BBB");
      events.add("finished b");
    });
    server.setRoute("/c", (req, res) -> {
      events.add("started c");
      Thread.sleep(2000);
      res.writeHead(200, "content-type", "text/html");
      res.end("CCC");
      events.add("finished c");
    });
    page.navigate(server.PREFIX + "/a");
    page.waitForTimeout(1000);
    PlaywrightException error = null;
    try {
      page.navigate(server.PREFIX + "/b");
    } catch (PlaywrightException e) {
      error = e;
    }
    List<String> expectEvents = Arrays.asList("started c", "started b", "finished c", "finished b");
    toPass(page, () -> assertEquals(expectEvents, events), 5000);
    assertEquals(expectEvents, events);
    assertNull(error);
    assertEquals(server.PREFIX + "/b", page.url());
  }

  @PlaywrightTest
  @DisplayName("NA-040 should work when navigating to valid url")
  void shouldWorkWhenNavigatingToValidUrl(Page page, TestServer server) {
    Response response = page.navigate(server.EMPTY_PAGE);
    assertEquals(true, response.ok());
  }

  @PlaywrightTest
  @DisplayName("NA-041 should work when navigating to data url")
  void shouldWorkWhenNavigatingToDataUrl(Page page, TestServer server) {
    Response response = page.navigate("data:text/html,hello");
    assertNull(response);
  }

  @PlaywrightTest
  @DisplayName("NA-042 should work when navigating to 404")
  void shouldWorkWhenNavigatingTo404(Page page, TestServer server) {
    Response response = page.navigate(server.PREFIX + "/not-found");
    assertEquals(false, response.ok());
    assertEquals(404, response.status());
  }

  @PlaywrightTest
  @DisplayName("NA-043 should return last response in redirect chain")
  void shouldReturnLastResponseInRedirectChain(Page page, TestServer server) {
    server.setRedirect("/redirect/1.html", "/redirect/2.html");
    server.setRedirect("/redirect/2.html", "/redirect/3.html");
    server.setRedirect("/redirect/3.html", server.EMPTY_PAGE);
    Response response = page.navigate(server.PREFIX + "/redirect/1.html");
    assertEquals(true, response.ok());
    assertEquals(server.EMPTY_PAGE, response.url());
  }

  @PlaywrightTest
  @DisplayName("NA-047 should navigate to dataURL and not fire dataURL requests")
  void shouldNavigateToDataURLAndNotFireDataURLRequests(Page page, TestServer server) {
    List<Request> requests = new ArrayList<>();
    page.onRequest(request -> requests.add(request));
    String dataURL = "data:text/html,<div>yo</div>";
    Response response = page.navigate(dataURL);
    assertNull(response);
    assertEquals(0, requests.size());
  }

  @PlaywrightTest
  @DisplayName("NA-048 should navigate to URL with hash and fire requests without hash")
  void shouldNavigateToURLWithHashAndFireRequestsWithoutHash(Page page, TestServer server) {
    List<Request> requests = new ArrayList<>();
    page.onRequest(request -> requests.add(request));
    Response response = page.navigate(server.EMPTY_PAGE + "#hash");
    assertEquals(200, response.status());
    assertEquals(server.EMPTY_PAGE, response.url());
    assertEquals(1, requests.size());
    assertEquals(server.EMPTY_PAGE, requests.get(0).url());
  }

  @PlaywrightTest
  @DisplayName("NA-049 should work with self requesting page")
  void shouldWorkWithSelfRequestingPage(Page page, TestServer server) {
    Response response = page.navigate(server.PREFIX + "/self-request.html");
    assertEquals(200, response.status());
    assertTrue(response.url().contains("self-request.html"));
  }

  @PlaywrightTest
  @DisplayName("NA-050 should fail when navigating and show the url at the error message")
  void shouldFailWhenNavigatingAndShowTheUrlAtTheErrorMessage(Page page, TestServer server, HttpsTestServer httpsServer) {
    String url = httpsServer.PREFIX + "/redirect/1.html";
    PlaywrightException error = null;
    try {
      page.navigate(url);
    } catch (PlaywrightException e) {
      error = e;
    }
    assertTrue(error.getMessage().contains(url));
  }

  @PlaywrightTest
  @DisplayName("NA-051 should be able to navigate to a page controlled by service worker")
  void shouldBeAbleToNavigateToAPageControlledByServiceWorker(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/serviceworkers/fetch/sw.html");
    page.evaluate("() => window['activationPromise']");
    page.navigate(server.PREFIX + "/serviceworkers/fetch/sw.html");
  }

  @PlaywrightTest
  @DisplayName("NA-052 should send referer")
  void shouldSendReferer(Page page, TestServer server) throws Exception {
    CompletableFuture<TestServer.Request> request1Promise = server.waitForRequest("/grid.html");
    CompletableFuture<TestServer.Request> request2Promise = server.waitForRequest("/digits/1.png");
    page.navigate(server.PREFIX + "/grid.html", new Page.NavigateOptions()
      .setReferer("http://google.com/"));
    TestServer.Request request1 = request1Promise.get();
    TestServer.Request request2 = request2Promise.get();
    assertEquals("http://google.com/", request1.headers.get("referer"));
    // Make sure subresources do not inherit referer.
    assertEquals(server.PREFIX + "/grid.html", request2.headers.get("referer"));
    assertEquals(server.PREFIX + "/grid.html", page.url());
  }

  @PlaywrightTest
  @DisplayName("NA-053 should send referer of cross-origin URL")
  void shouldSendRefererOfCrossOriginURL(Page page, TestServer server) throws Exception {
    CompletableFuture<TestServer.Request> request1Promise = server.waitForRequest("/grid.html");
    CompletableFuture<TestServer.Request> request2Promise = server.waitForRequest("/digits/1.png");
    page.navigate(server.PREFIX + "/grid.html", new Page.NavigateOptions()
      .setReferer("https://microsoft.com/xbox/"));
    TestServer.Request request1 = request1Promise.get();
    TestServer.Request request2 = request2Promise.get();
    assertEquals("https://microsoft.com/xbox/", request1.headers.get("referer"));
    // Make sure subresources do not inherit referer.
    assertEquals(server.PREFIX + "/grid.html", request2.headers.get("referer"));
    assertEquals(server.PREFIX + "/grid.html", page.url());
  }

  @PlaywrightTest
  @DisplayName("NA-054 should reject referer option when setExtraHTTPHeaders provides referer")
  void shouldRejectRefererOptionWhenSetExtraHTTPHeadersProvidesReferer(Page page, TestServer server) {
    page.setExtraHTTPHeaders(mapOfStrings("referer", "http://microsoft.com/"));
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.navigate(server.PREFIX + "/grid.html", new Page.NavigateOptions()
      .setReferer("http://google.com/")));
    assertTrue(error.getMessage().contains("\"referer\" is already specified as extra HTTP header"));
    assertTrue(error.getMessage().contains(server.PREFIX + "/grid.html"));
  }

  @PlaywrightTest
  @DisplayName("NA-055 should override referrer-policy")
  void shouldOverrideReferrerPolicy(Page page, TestServer server) throws Exception {
    server.setRoute("/grid.html", (req, res) -> {
      res.setHeader("Referrer-Policy", "no-referrer");
      server.serveFile(req, res);
    });
    CompletableFuture<TestServer.Request> request1Promise = server.waitForRequest("/grid.html");
    CompletableFuture<TestServer.Request> request2Promise = server.waitForRequest("/digits/1.png");
    page.navigate(server.PREFIX + "/grid.html", new Page.NavigateOptions()
      .setReferer("http://microsoft.com/"));
    TestServer.Request request1 = request1Promise.get();
    TestServer.Request request2 = request2Promise.get();
    assertEquals("http://microsoft.com/", request1.headers.get("referer"));
    // Make sure subresources do not inherit referer.
    assertNull(request2.headers.get("referer"));
    assertEquals(server.PREFIX + "/grid.html", page.url());
  }

  @PlaywrightTest
  @DisplayName("NA-057 should work with lazy loading iframes")
  void shouldWorkWithLazyLoadingIframes(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/frames/lazy-frame.html");
    assertEquals(2, page.frames().size());
  }

  @PlaywrightTest
  @DisplayName("NA-058 should report raw buffer for main resource")
  @DisabledOnBrowser(value = BrowserName.CHROMIUM, reason = "fail: Chromium sends main resource as text")
  void shouldReportRawBufferForMainResource(Page page, TestServer server, BrowserName browserName) {
    assumeFalse(browserName == BrowserName.WEBKIT && Env.platform().equals("win32") && !"webkit-wsl".equals(Env.channel()), "fail: Same here");

    server.setRoute("/empty.html", (req, res) -> {
      res.statusCode = 200;
      res.end("Ü (lowercase ü)".getBytes(StandardCharsets.UTF_8));
    });
    Response response = page.navigate(server.PREFIX + "/empty.html");
    byte[] body = response.body();
    assertEquals("Ü (lowercase ü)", new String(body, StandardCharsets.UTF_8));
  }

  @PlaywrightTest
  @DisplayName("NA-059 should not throw unhandled rejections on invalid url")
  void shouldNotThrowUnhandledRejectionsOnInvalidUrl(Page page, TestServer server) {
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.navigate("https://www.youtube Panel Title.com/"));
    assertTrue(e.toString().contains("Panel Title"));
  }

  @PlaywrightTest
  @DisplayName("NA-060 should not crash when RTCPeerConnection is used")
  void shouldNotCrashWhenRTCPeerConnectionIsUsed(Page page, TestServer server, BrowserName browserName) {
    server.setRoute("/rtc.html", (req, res) -> {
      res.end("\n" +
        "      <!DOCTYPE html>\n" +
        "      <html>\n" +
        "        <body>\n" +
        "          <script>\n" +
        "            window.RTCPeerConnection && new window.RTCPeerConnection({\n" +
        "              iceServers: []\n" +
        "            });\n" +
        "          </script>\n" +
        "        </body>\n" +
        "      </html>\n");
    });
    page.navigate(server.PREFIX + "/rtc.html");
    page.evaluate("() => {\n" +
      "  // RTCPeerConnection is not present on WebKit Linux\n" +
      "  window.RTCPeerConnection && new window.RTCPeerConnection({\n" +
      "    iceServers: []\n" +
      "  });\n" +
      "}");
  }

  @PlaywrightTest
  @DisplayName("NA-061 should properly wait for load")
  void shouldProperlyWaitForLoad(Page page, TestServer server, BrowserName browserName) {
    server.setRoute("/slow.js", (req, res) -> {
      Thread.sleep(100);
      res.writeHead(200, "Content-Type", "application/javascript");
      res.end("window.results.push('slow module');export const foo = 'slow';");
    });
    page.navigate(server.PREFIX + "/load-event/load-event.html");
    Object results = page.evaluate("window.results");
    assertEquals(Arrays.asList(
      "script tag after after module",
      "slow module",
      "module",
      "DOMContentLoaded",
      "load"), results);
  }

  @PlaywrightTest
  @DisplayName("NA-064 should return when navigation is committed if commit is specified")
  void shouldReturnWhenNavigationIsCommittedIfCommitIsSpecified(Page page, TestServer server) {
    server.setRoute("/script.js", (req, res) -> {});
    server.setRoute("/empty.html", (req, res) -> {
      res.setHeader("content-type", "text/html");
      res.end("<title>Hello</title><script src=\"script.js\"></script>");
    });
    Response response = page.navigate(server.EMPTY_PAGE, new Page.NavigateOptions().setWaitUntil(WaitUntilState.COMMIT));
    assertEquals(200, response.status());
    assertEquals("Hello", page.title());
  }

  @PlaywrightTest
  @DisplayName("NA-065 should wait for load when iframe attaches and detaches")
  void shouldWaitForLoadWhenIframeAttachesAndDetaches(Page page, TestServer server) throws Exception {
    assumeFalse("frozen".equals(System.getenv("PW_CLOCK")));
    server.setRoute("/empty.html", (req, res) -> {
      res.writeHead(200, "content-type", "text/html");
      res.end("\n" +
        "      <body>\n" +
        "        <script>\n" +
        "          const iframe = document.createElement('iframe');\n" +
        "          iframe.src = './iframe.html';\n" +
        "          document.body.appendChild(iframe);\n" +
        "          setTimeout(() => iframe.remove(), 1000);\n" +
        "        </script>\n" +
        "      </body>\n" +
        "    ");
    });

    server.setRoute("/iframe.html", (req, res) -> {
      res.writeHead(200, "content-type", "text/html");
      res.end("\n" +
        "      <link rel=\"stylesheet\" href=\"./style2.css\">\n" +
        "    ");
    });

    // Stall the css so that 'load' does not fire.
    server.setRoute("/style2.css", (req, res) -> {});

    // Java: page.waitForEvent('framedetached') is a handler that removes itself and completes a future.
    CompletableFuture<Frame> frameDetached = new CompletableFuture<>();
    page.onFrameDetached(new Consumer<Frame>() {
      @Override
      public void accept(Frame frame) {
        page.offFrameDetached(this);
        frameDetached.complete(frame);
      }
    });
    // Java: the navigation waits for 'load', which comes after the iframe is gone.
    page.navigate(server.EMPTY_PAGE, new Page.NavigateOptions().setWaitUntil(WaitUntilState.LOAD));
    page.waitForCondition(frameDetached::isDone); // Make sure that iframe is gone.
    assertNull(page.querySelector("iframe"));
  }

  @PlaywrightTest
  @DisplayName("NA-066 should return url with basic auth info")
  void shouldReturnUrlWithBasicAuthInfo(Page page, TestServer server) {
    // Java: the loopback fixture is not set, it is 'localhost'.
    String url = "http://admin:admin@" + "localhost" + ":" + server.PORT + "/empty.html";
    page.navigate(url);
    assertEquals(url, page.url());
  }

  private static java.util.Map<String, String> mapOfStrings(String key, String value) {
    return Collections.singletonMap(key, value);
  }
}
