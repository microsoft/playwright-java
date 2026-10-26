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
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.WaitUntilState;
import org.junit.jupiter.api.DisplayName;

import java.util.regex.Pattern;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/page-wait-for-url.spec.ts. */
public class TestPageWaitForUrl {
  @PlaywrightTest
  @DisplayName("WA-110 should work")
  void shouldWork(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.evaluate("url => window.location.href = url", server.PREFIX + "/grid.html");
    page.waitForURL("**/grid.html");
  }

  @PlaywrightTest
  @DisplayName("WA-111 should respect timeout")
  @Fixme("java: the message has no 'page.waitForURL:' prefix")
  void shouldRespectTimeout(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    // Java: the wait comes after the navigation; it is not satisfied by it either.
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.waitForURL("**/frame.html", new Page.WaitForURLOptions().setTimeout(2500)));
    assertTrue(error.getMessage().contains("page.waitForURL: Timeout 2500ms exceeded."));
  }

  @PlaywrightTest
  @DisplayName("WA-112 should work with both domcontentloaded and load")
  void shouldWorkWithBothDomcontentloadedAndLoad(Page page, TestServer server) {
    // Java: the stylesheet is held by the route until the test releases it, so the navigation returns on
    // domcontentloaded first; "load has not fired yet" is checked on the document.
    CompletableFuture<Void> response = new CompletableFuture<>();
    server.setRoute("/one-style.css", (req, res) -> {
      response.get();
      res.end();
    });
    page.navigate(server.PREFIX + "/one-style.html", new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
    page.waitForURL("**/one-style.html", new Page.WaitForURLOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
    assertEquals("interactive", page.evaluate("document.readyState"));
    response.complete(null);
    page.waitForURL("**/one-style.html", new Page.WaitForURLOptions().setWaitUntil(WaitUntilState.LOAD));
    assertEquals("complete", page.evaluate("document.readyState"));
  }

  @PlaywrightTest
  @DisplayName("WA-113 should work with commit")
  void shouldWorkWithCommit(Page page, TestServer server) {
    server.setRoute("/script.js", (req, res) -> {});
    server.setRoute("/empty.html", (req, res) -> {
      res.setHeader("content-type", "text/html");
      res.end("<title>Hello</title><script src=\"script.js\"></script>");
    });
    // Java: upstream starts the navigation without awaiting it; here it returns on commit.
    page.navigate(server.EMPTY_PAGE, new Page.NavigateOptions().setWaitUntil(WaitUntilState.COMMIT));
    page.waitForURL("**/empty.html", new Page.WaitForURLOptions().setWaitUntil(WaitUntilState.COMMIT));
    assertEquals("Hello", page.title());
  }

  @PlaywrightTest
  @DisplayName("WA-114 should work with commit and about:blank")
  void shouldWorkWithCommitAndAboutBlank(Page page, TestServer server) {
    page.waitForURL("about:blank", new Page.WaitForURLOptions().setWaitUntil(WaitUntilState.COMMIT));
  }

  @PlaywrightTest
  @DisplayName("WA-115 should work with clicking on anchor links")
  void shouldWorkWithClickingOnAnchorLinks(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<a href='#foobar'>foobar</a>");
    page.click("a");
    page.waitForURL("**/*#foobar");
  }

  @PlaywrightTest
  @DisplayName("WA-116 should work with history.pushState()")
  void shouldWorkWithHistoryPushState(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.setContent("\n" +
      "    <a onclick='javascript:pushState()'>SPA</a>\n" +
      "    <script>\n" +
      "      function pushState() { history.pushState({}, '', 'wow.html') }\n" +
      "    </script>\n" +
      "  ");
    page.click("a");
    page.waitForURL("**/wow.html");
    assertEquals(server.PREFIX + "/wow.html", page.url());
  }

  @PlaywrightTest
  @DisplayName("WA-117 should work with history.replaceState()")
  void shouldWorkWithHistoryReplaceState(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.setContent("\n" +
      "    <a onclick='javascript:replaceState()'>SPA</a>\n" +
      "    <script>\n" +
      "      function replaceState() { history.replaceState({}, '', '/replaced.html') }\n" +
      "    </script>\n" +
      "  ");
    page.click("a");
    page.waitForURL("**/replaced.html");
    assertEquals(server.PREFIX + "/replaced.html", page.url());
  }

  @PlaywrightTest
  @DisplayName("WA-118 should work with DOM history.back()/history.forward()")
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

    page.click("a#back");
    page.waitForURL("**/first.html");
    assertEquals(server.PREFIX + "/first.html", page.url());

    page.click("a#forward");
    page.waitForURL("**/second.html");
    assertEquals(server.PREFIX + "/second.html", page.url());
  }

  @PlaywrightTest
  @DisplayName("WA-119 should work with url match for same document navigations")
  void shouldWorkWithUrlMatchForSameDocumentNavigations(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
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
    // Java: the wait comes after the navigations; it stays satisfied.
    page.waitForURL(Pattern.compile("third\\.html"));
  }

  @PlaywrightTest
  @DisplayName("WA-120 should work on frame")
  void shouldWorkOnFrame(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/frames/one-frame.html");
    Frame frame = page.frames().get(1);
    frame.evaluate("url => window.location.href = url", server.PREFIX + "/grid.html");
    frame.waitForURL("**/grid.html");
  }
}
