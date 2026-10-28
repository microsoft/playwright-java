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
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;
import org.opentest4j.AssertionFailedError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.microsoft.playwright.fixtures.Utils.stripAnsi;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/page-autowaiting-basic.spec.ts. */
public class TestPageAutowaitingBasic {
  private static List<String> initServer(TestServer server) {
    List<String> messages = Collections.synchronizedList(new ArrayList<>());
    server.setRoute("/empty.html", (req, res) -> {
      messages.add("route");
      res.setHeader("Content-Type", "text/html");
      res.end("<link rel='stylesheet' href='./one-style.css'>");
    });
    return messages;
  }

  // Java: page.waitForEvent('framenavigated') is a one-shot handler on the event, registered before the action.
  private static CompletableFuture<Frame> waitForFrameNavigated(Page page) {
    CompletableFuture<Frame> future = new CompletableFuture<>();
    page.onFrameNavigated(new Consumer<Frame>() {
      @Override
      public void accept(Frame frame) {
        page.offFrameNavigated(this);
        future.complete(frame);
      }
    });
    return future;
  }

  // Java: page.waitForEvent('load') is a one-shot handler on the event, registered before the action.
  private static CompletableFuture<Page> waitForLoad(Page page) {
    CompletableFuture<Page> future = new CompletableFuture<>();
    page.onLoad(new Consumer<Page>() {
      @Override
      public void accept(Page p) {
        page.offLoad(this);
        future.complete(p);
      }
    });
    return future;
  }

  @PlaywrightTest
  @DisplayName("AW-001 should await navigation when clicking anchor")
  void shouldAwaitNavigationWhenClickingAnchor(Page page, TestServer server) throws Exception {
    List<String> messages = initServer(server);
    page.setContent("<a id=\"anchor\" href=\"" + server.EMPTY_PAGE + "\">empty.html</a>");
    CompletableFuture<Frame> navigated = waitForFrameNavigated(page);
    navigated.thenRun(() -> messages.add("navigated"));
    page.click("a");
    messages.add("click");
    navigated.get();
    assertEquals("route|navigated|click", String.join("|", messages));
  }

  @PlaywrightTest
  @DisplayName("AW-002 should not stall on JS navigation link")
  void shouldNotStallOnJSNavigationLink(Page page) {
    page.setContent("<a href=\"javascript:console.log(1)\">console.log</a>");
    page.click("a");
  }

  @PlaywrightTest
  @DisplayName("AW-003 should await cross-process navigation when clicking anchor")
  void shouldAwaitCrossprocessNavigationWhenClickingAnchor(Page page, TestServer server) throws Exception {
    List<String> messages = initServer(server);
    page.setContent("<a href=\"" + (server.CROSS_PROCESS_PREFIX + "/empty.html") + "\">empty.html</a>");

    CompletableFuture<Frame> navigated = waitForFrameNavigated(page);
    navigated.thenRun(() -> messages.add("navigated"));
    page.click("a");
    messages.add("click");
    navigated.get();
    assertEquals("route|navigated|click", String.join("|", messages));
  }

  @PlaywrightTest
  @DisplayName("AW-004 should await form-get on click")
  void shouldAwaitFormgetOnClick(Page page, TestServer server) throws Exception {
    List<String> messages = Collections.synchronizedList(new ArrayList<>());
    server.setRoute("/empty.html?foo=bar", (req, res) -> {
      messages.add("route");
      res.setHeader("Content-Type", "text/html");
      res.end("<link rel='stylesheet' href='./one-style.css'>");
    });

    page.setContent("\n" +
      "    <form action=\"" + server.EMPTY_PAGE + "\" method=\"get\">\n" +
      "      <input name=\"foo\" value=\"bar\">\n" +
      "      <input type=\"submit\" value=\"Submit\">\n" +
      "    </form>");

    CompletableFuture<Frame> navigated = waitForFrameNavigated(page);
    navigated.thenRun(() -> messages.add("navigated"));
    page.click("input[type=submit]");
    messages.add("click");
    navigated.get();
    assertEquals("route|navigated|click", String.join("|", messages));
  }

  @PlaywrightTest
  @DisplayName("AW-005 should await form-post on click")
  void shouldAwaitFormpostOnClick(Page page, TestServer server) throws Exception {
    List<String> messages = initServer(server);
    page.setContent("\n" +
      "    <form action=\"" + server.EMPTY_PAGE + "\" method=\"post\">\n" +
      "      <input name=\"foo\" value=\"bar\">\n" +
      "      <input type=\"submit\" value=\"Submit\">\n" +
      "    </form>");

    CompletableFuture<Frame> navigated = waitForFrameNavigated(page);
    navigated.thenRun(() -> messages.add("navigated"));
    page.click("input[type=submit]");
    messages.add("click");
    navigated.get();
    assertEquals("route|navigated|click", String.join("|", messages));
  }

  @PlaywrightTest
  @DisplayName("AW-006 should work with noWaitAfter: true")
  void shouldWorkWithNoWaitAfterTrue(Page page, TestServer server) {
    server.setRoute("/empty.html", (req, res) -> {});
    page.setContent("<a id=\"anchor\" href=\"" + server.EMPTY_PAGE + "\">empty.html</a>");
    page.click("a", new Page.ClickOptions().setNoWaitAfter(true));
  }

  @PlaywrightTest
  @DisplayName("AW-007 should work with dblclick without noWaitAfter when navigation is stalled")
  void shouldWorkWithDblclickWithoutNoWaitAfterWhenNavigationIsStalled(Page page, TestServer server) {
    server.setRoute("/empty.html", (req, res) -> {});
    page.setContent("<a id=\"anchor\" href=\"" + server.EMPTY_PAGE + "\">empty.html</a>");
    page.dblclick("a");
  }

  @PlaywrightTest
  @DisplayName("AW-008 should work with waitForLoadState(load)")
  void shouldWorkWithWaitForLoadStateload(Page page, TestServer server) throws Exception {
    List<String> messages = initServer(server);
    page.setContent("<a id=\"anchor\" href=\"" + server.EMPTY_PAGE + "\">empty.html</a>");
    CompletableFuture<Page> load = waitForLoad(page);
    load.thenRun(() -> messages.add("load"));
    page.click("a");
    page.waitForLoadState(com.microsoft.playwright.options.LoadState.LOAD);
    messages.add("clickload");
    load.get();
    assertEquals("route|load|clickload", String.join("|", messages));
  }

  @PlaywrightTest
  @DisplayName("AW-009 should work with goto following click")
  void shouldWorkWithGotoFollowingClick(Page page, TestServer server) {
    server.setRoute("/login.html", (req, res) -> {
      res.setHeader("Content-Type", "text/html");
      res.end("You are logged in");
    });

    page.setContent("\n" +
      "    <form action=\"" + server.PREFIX + "/login.html\" method=\"get\">\n" +
      "      <input type=\"text\">\n" +
      "      <input type=\"submit\" value=\"Submit\">\n" +
      "    </form>");

    page.fill("input[type=text]", "admin");
    page.click("input[type=submit]");
    page.navigate(server.EMPTY_PAGE);
  }

  @PlaywrightTest
  @DisplayName("AW-011 should report and collapse log in action")
  void shouldReportAndCollapseLogInAction(Page page, TestServer server) {
    page.setContent("<input id='checkbox' type='checkbox' style=\"visibility: hidden\"></input>");
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.locator("input").click(new com.microsoft.playwright.Locator.ClickOptions().setTimeout(5000)));
    String message = stripAnsi(error.getMessage());
    assertTrue(message.contains("Call log:"));
    assertTrue(Pattern.compile("\\d+ × waiting for").matcher(message).find());
    String[] logLines = message.substring(message.indexOf("Call log:")).split("\n");
    assertTrue(logLines.length < 30);
  }

  @PlaywrightTest
  @DisplayName("AW-012 should report and collapse log in expect")
  void shouldReportAndCollapseLogInExpect(Page page, TestServer server) {
    page.setContent("<input id='checkbox' type='checkbox' style=\"visibility: hidden\"></input>");
    AssertionFailedError error = assertThrows(AssertionFailedError.class,
      () -> assertThat(page.locator("input")).isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(5000)));
    String message = stripAnsi(error.getMessage());
    assertTrue(message.contains("Call log:"));
    assertTrue(Pattern.compile("\\d+ × locator resolved to").matcher(message).find());
  }
}
