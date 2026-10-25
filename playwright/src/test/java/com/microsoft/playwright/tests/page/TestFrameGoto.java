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
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.WaitUntilState;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.microsoft.playwright.fixtures.Utils.async;
import static com.microsoft.playwright.fixtures.Utils.attachFrame;
import static com.microsoft.playwright.fixtures.Utils.cause;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/frame-goto.spec.ts. */
public class TestFrameGoto {
  @PlaywrightTest
  @DisplayName("FR-020 should navigate subframes @smoke")
  void shouldNavigateSubframes(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/frames/one-frame.html");
    assertTrue(page.frames().get(0).url().contains("/frames/one-frame.html"));
    assertTrue(page.frames().get(1).url().contains("/frames/frame.html"));

    Response response = page.frames().get(1).navigate(server.EMPTY_PAGE);
    assertEquals(true, response.ok());
    assertEquals(page.frames().get(1), response.frame());
  }

  @PlaywrightTest
  @DisplayName("FR-021 should reject when frame detaches")
  void shouldRejectWhenFrameDetaches(Page page, TestServer server, BrowserName browserName) throws ExecutionException, InterruptedException {
    page.navigate(server.PREFIX + "/frames/one-frame.html");

    server.setRoute("/one-style.css", (req, res) -> {});
    CompletableFuture<Response> navigationPromise = async(() -> page.frames().get(1).navigate(server.PREFIX + "/one-style.html"));
    server.waitForRequest("/one-style.css").get();

    page.evalOnSelector("iframe", "frame => frame.remove()");
    Throwable error = cause(navigationPromise);
    if (browserName == BrowserName.CHROMIUM) {
      assertEquals(true, error.getMessage().contains("net::ERR_ABORTED") || error.getMessage().toLowerCase().contains("frame was detached"));
    } else {
      assertTrue(error.getMessage().toLowerCase().contains("frame was detached"));
    }
  }

  @PlaywrightTest
  @DisplayName("FR-022 should continue after client redirect")
  @DisabledOnBrowser(value = BrowserName.FIREFOX, reason = "fixme: script.js is requested before navigationCommitted arrives")
  @Fixme("java: the error message has no 'page.goto:' prefix and no call log")
  void shouldContinueAfterClientRedirect(Page page, TestServer server) {
    server.setRoute("/frames/script.js", (req, res) -> {});
    String url = server.PREFIX + "/frames/child-redirect.html";
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.navigate(url, new Page.NavigateOptions().setTimeout(5000).setWaitUntil(WaitUntilState.NETWORKIDLE)));
    assertTrue(error.getMessage().contains("page.goto: Timeout 5000ms exceeded."));
    assertTrue(error.getMessage().contains("navigating to \"" + url + "\", waiting until \"networkidle\""));
  }

  @PlaywrightTest
  @DisplayName("FR-023 should return matching responses")
  void shouldReturnMatchingResponses(Page page, TestServer server) throws Exception {
    page.navigate(server.EMPTY_PAGE);
    // Attach three frames.
    List<Frame> frames = Arrays.asList(
      attachFrame(page, "frame1", server.EMPTY_PAGE),
      attachFrame(page, "frame2", server.EMPTY_PAGE),
      attachFrame(page, "frame3", server.EMPTY_PAGE));
    List<TestServer.Response> serverResponses = new ArrayList<>();
    server.setRoute("/0.html", (req, res) -> serverResponses.add(res));
    server.setRoute("/1.html", (req, res) -> serverResponses.add(res));
    server.setRoute("/2.html", (req, res) -> serverResponses.add(res));
    List<CompletableFuture<Response>> navigations = new ArrayList<>();
    for (int i = 0; i < 3; ++i) {
      int index = i;
      navigations.add(async(() -> frames.get(index).navigate(server.PREFIX + "/" + index + ".html")));
      server.waitForRequest("/" + i + ".html").get();
    }
    // Respond from server out-of-order.
    String[] serverResponseTexts = {"AAA", "BBB", "CCC"};
    for (int i : new int[]{1, 2, 0}) {
      serverResponses.get(i).end(serverResponseTexts[i]);
      Response response = navigations.get(i).get();
      assertEquals(frames.get(i), response.frame());
      assertEquals(serverResponseTexts[i], response.text());
    }
  }
}
