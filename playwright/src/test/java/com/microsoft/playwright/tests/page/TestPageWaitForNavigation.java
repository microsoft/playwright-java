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
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.HttpsTestServer;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.WaitUntilState;
import org.junit.jupiter.api.DisplayName;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import static com.microsoft.playwright.fixtures.Utils.expectedSSLError;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.waitForNavigation(
      new Page.WaitForNavigationOptions().setUrl("**/frame.html").setTimeout(5000), () -> {
        page.navigate(server.EMPTY_PAGE);
      }));
    assertTrue(error.getMessage().contains("page.waitForNavigation: Timeout 5000ms exceeded."));
    assertTrue(error.getMessage().contains("waiting for navigation to \"**/frame.html\" until \"load\""));
    assertTrue(error.getMessage().contains("navigated to \"" + server.EMPTY_PAGE + "\""));
  }

  @PlaywrightTest
  @DisplayName("WA-097 should work with both domcontentloaded and load")
  void shouldWorkWithBothDomcontentloadedAndLoad(Page page, TestServer server) {
    // Java: the stylesheet is held by the route until the test releases it; the waits nest around the navigation,
    // which returns on domcontentloaded, and "load has not fired yet" is checked on the document.
    CompletableFuture<Void> response = new CompletableFuture<>();
    server.setRoute("/one-style.css", (req, res) -> {
      response.get();
      res.end();
    });
    page.waitForNavigation(new Page.WaitForNavigationOptions().setWaitUntil(WaitUntilState.LOAD), () -> {
      page.waitForNavigation(new Page.WaitForNavigationOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED), () -> {
        page.navigate(server.PREFIX + "/one-style.html", new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
      });
      assertEquals("interactive", page.evaluate("document.readyState"));
      response.complete(null);
    });
    assertEquals("complete", page.evaluate("document.readyState"));
  }

  @PlaywrightTest
  @DisplayName("WA-098 should work with commit")
  void shouldWorkWithCommit(Page page, TestServer server) {
    server.setRoute("/script.js", (req, res) -> {});
    server.setRoute("/empty.html", (req, res) -> {
      res.setHeader("content-type", "text/html");
      res.end("<title>Hello</title><script src=\"script.js\"></script>");
    });
    // Java: upstream starts the navigation without awaiting it; here it returns on commit.
    page.waitForNavigation(new Page.WaitForNavigationOptions().setWaitUntil(WaitUntilState.COMMIT), () -> {
      page.navigate(server.EMPTY_PAGE, new Page.NavigateOptions().setWaitUntil(WaitUntilState.COMMIT));
    });
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
  @DisplayName("WA-105 should work with url match")
  void shouldWorkWithUrlMatch(Page page, TestServer server) throws Exception {
    AtomicReference<Response> response1 = new AtomicReference<>();
    AtomicReference<Response> response2 = new AtomicReference<>();
    AtomicReference<Response> response3 = new AtomicReference<>();
    // Java: the three waits are nested, the navigations run in the innermost callback.
    response1.set(page.waitForNavigation(new Page.WaitForNavigationOptions().setUrl(Pattern.compile("one-style\\.html")), () -> {
      response2.set(page.waitForNavigation(new Page.WaitForNavigationOptions().setUrl(Pattern.compile("/frame.html")), () -> {
        response3.set(page.waitForNavigation(new Page.WaitForNavigationOptions().setUrl(url -> "bar".equals(searchParam(url, "foo"))), () -> {
          page.navigate(server.EMPTY_PAGE);
          page.navigate(server.PREFIX + "/frame.html");
          page.navigate(server.PREFIX + "/one-style.html");
          page.navigate(server.PREFIX + "/frame.html?foo=bar");
        }));
      }));
    }));
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
    page.waitForNavigation(new Page.WaitForNavigationOptions().setUrl(Pattern.compile("third\\.html")), () -> {
      page.evaluate("() => {\n" +
        "  history.pushState({}, '', '/first.html');\n" +
        "}");
      assertFalse(page.url().contains("third.html"));
      page.evaluate("() => {\n" +
        "  history.pushState({}, '', '/second.html');\n" +
        "}");
      assertFalse(page.url().contains("third.html"));
      page.evaluate("() => {\n" +
        "  history.pushState({}, '', '/third.html');\n" +
        "}");
    });
  }

  @PlaywrightTest
  @DisplayName("WA-107 should work for cross-process navigations")
  void shouldWorkForCrossProcessNavigations(Page page, TestServer server) throws Exception {
    page.navigate(server.EMPTY_PAGE);
    String url = server.CROSS_PROCESS_PREFIX + "/empty.html";
    Response response = page.waitForNavigation(
      new Page.WaitForNavigationOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED), () -> {
        page.navigate(url);
      });
    assertEquals(url, response.url());
    assertEquals(url, page.url());
    assertEquals(url, page.evaluate("document.location.href"));
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
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> frame.waitForNavigation(() -> {
      page.evalOnSelector("iframe", "frame => { frame.contentWindow.location.href = '/one-style.html'; }");
      // Make sure policy checks pass and navigation actually begins before removing the frame to avoid other errors
      cssRequest.join();
      page.evalOnSelector("iframe", "frame => window.builtins.setTimeout(() => frame.remove(), 0)");
    }));
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
