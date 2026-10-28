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
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Port of tests/page/page-navigation.spec.ts. */
public class TestPageNavigation {
  @PlaywrightTest
  @DisplayName("NA-067 should work with _blank target")
  void shouldWorkWithBlankTarget(Page page, TestServer server) {
    server.setRoute("/empty.html", (req, res) -> {
      res.end("<a href=\"" + server.EMPTY_PAGE + "\" target=\"_blank\">Click me</a>");
    });
    page.navigate(server.EMPTY_PAGE);
    page.click("\"Click me\"");
  }

  @PlaywrightTest
  @DisplayName("NA-068 should work with cross-process _blank target")
  void shouldWorkWithCrossProcessBlankTarget(Page page, TestServer server) {
    server.setRoute("/empty.html", (req, res) -> {
      res.end("<a href=\"" + server.CROSS_PROCESS_PREFIX + "/empty.html\" target=\"_blank\">Click me</a>");
    });
    page.navigate(server.EMPTY_PAGE);
    page.click("\"Click me\"");
  }

  @PlaywrightTest
  @DisplayName("NA-069 should work with _blank target in form")
  void shouldWorkWithBlankTargetInForm(Page page, TestServer server) {
    server.setRoute("/done.html?", (req, res) -> {
      res.end("Done");
    });
    server.setRoute("/done.html", (req, res) -> {
      res.end("Done");
    });
    page.navigate(server.EMPTY_PAGE);

    page.setContent("<form target=\"_blank\" action=\"done.html\" >\n" +
      "    <input type=\"submit\" value=\"Click me\">\n" +
      "  </form>");
    page.waitForPopup(() -> page.click("\"Click me\""));

    page.setContent("<form target=\"_blank\" action=\"done.html\" method=\"post\">\n" +
      "    <input type=\"submit\" value=\"Click me\">\n" +
      "  </form>");
    page.waitForPopup(() -> page.click("\"Click me\""));
  }

  @PlaywrightTest
  @DisplayName("NA-070 should not throw TargetClosedException on cross-origin redirect after click")
  void shouldNotThrowTargetClosedExceptionOnCrossOriginRedirectAfterClick(Page page, TestServer server) {
    server.setRoute("/target.html", (req, res) -> res.end("<title>final page</title>"));
    server.setRedirect("/redirect", server.CROSS_PROCESS_PREFIX + "/target.html");
    page.navigate(server.PREFIX + "/empty.html");
    page.setContent("\n" +
      "    <form action=\"" + server.PREFIX + "/redirect\" method=\"POST\">\n" +
      "      <button type=\"submit\">Submit</button>\n" +
      "    </form>\n" +
      "  ");
    page.waitForNavigation(() -> {
      // triggers POST -> 302 -> cross-origin
      page.click("button");
    });
    assertThat(page).hasURL(server.CROSS_PROCESS_PREFIX + "/target.html");
    assertThat(page).hasTitle("final page");
  }
}
