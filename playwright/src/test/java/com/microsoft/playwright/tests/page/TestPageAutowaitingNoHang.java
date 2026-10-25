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
import com.microsoft.playwright.fixtures.HttpsTestServer;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.microsoft.playwright.fixtures.Utils.async;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** Port of tests/page/page-autowaiting-no-hang.spec.ts. */
public class TestPageAutowaitingNoHang {
  @PlaywrightTest
  @DisplayName("AW-013 clicking on links which do not commit navigation")
  void clickingOnLinksWhichDoNotCommitNavigation(Page page, TestServer server, HttpsTestServer httpsServer) {
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<a href='" + httpsServer.EMPTY_PAGE + "'>foobar</a>");
    page.click("a");
  }

  @PlaywrightTest
  @DisplayName("AW-014 calling window.stop async")
  void callingWindowstopAsync(Page page, TestServer server) {
    server.setRoute("/empty.html", (req, res) -> {});
    page.evaluate("url => {\n" +
      "    window.location.href = url;\n" +
      "    setTimeout(() => window.stop(), 100);\n" +
      "  }", server.EMPTY_PAGE);
  }

  @PlaywrightTest
  @DisplayName("AW-015 calling window.stop sync")
  void callingWindowstopSync(Page page, TestServer server) {
    page.evaluate("url => {\n" +
      "    window.location.href = url;\n" +
      "    window.stop();\n" +
      "  }", server.EMPTY_PAGE);
  }

  @PlaywrightTest
  @DisplayName("AW-016 assigning location to about:blank")
  void assigningLocationToAboutblank(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.evaluate("window.location.href = \"about:blank\";");
  }

  @PlaywrightTest
  @DisplayName("AW-017 assigning location to about:blank after non-about:blank")
  void assigningLocationToAboutblankAfterNonaboutblank(Page page, TestServer server) {
    server.setRoute("/empty.html", (req, res) -> {});
    page.evaluate("\n" +
      "      window.location.href = \"" + server.EMPTY_PAGE + "\";\n" +
      "      window.location.href = \"about:blank\";");
  }

  @PlaywrightTest
  @DisplayName("AW-018 calling window.open and window.close")
  void callingWindowopenAndWindowclose(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.evaluate("() => {\n" +
      "    const popup = window.open(window.location.href);\n" +
      "    popup.close();\n" +
      "  }");
  }

  @PlaywrightTest
  @DisplayName("AW-019 opening a popup")
  void openingAPopup(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.waitForPopup(() -> page.evaluate("() => window.open(window.location.href) && 1"));
  }

  @PlaywrightTest
  @DisplayName("AW-020 clicking in the middle of navigation that aborts")
  void clickingInTheMiddleOfNavigationThatAborts(Page page, TestServer server) throws Exception {
    CompletableFuture<Void> abortPromise = new CompletableFuture<>();

    CompletableFuture<Void> stallPromise = new CompletableFuture<>();

    server.setRoute("/stall.html", (req, res) -> {
      stallPromise.complete(null);
      abortPromise.get();
      req.socket().destroy();
    });

    page.navigate(server.PREFIX + "/one-style.html");
    async(() -> {
      try {
        return page.navigate(server.PREFIX + "/stall.html");
      } catch (PlaywrightException e) {
        return null;
      }
    });
    stallPromise.get();

    CompletableFuture<Object> clickPromise = async(() -> {
      page.click("body");
      return null;
    });
    page.waitForTimeout(1000);
    abortPromise.complete(null);

    clickPromise.get();
  }

  @PlaywrightTest
  @DisplayName("AW-021 clicking in the middle of navigation that commits")
  void clickingInTheMiddleOfNavigationThatCommits(Page page, TestServer server) throws Exception {
    CompletableFuture<Void> abortPromise = new CompletableFuture<>();

    CompletableFuture<Void> stallPromise = new CompletableFuture<>();

    server.setRoute("/stall.html", (req, res) -> {
      stallPromise.complete(null);
      abortPromise.get();
      res.writeHead(200, "Content-Type", "text/html");
      res.end("hello world");
    });

    page.navigate(server.PREFIX + "/one-style.html");
    async(() -> {
      try {
        return page.navigate(server.PREFIX + "/stall.html");
      } catch (PlaywrightException e) {
        return null;
      }
    });
    stallPromise.get();

    CompletableFuture<Object> clickPromise = async(() -> {
      page.click("body");
      return null;
    });
    page.waitForTimeout(1000);
    abortPromise.complete(null);

    clickPromise.get();
    assertThat(page.locator("body")).containsText("hello world");
  }

  @PlaywrightTest
  @DisplayName("AW-022 clicking a link intercepted by the Navigation API same-document")
  void clickingALinkInterceptedByTheNavigationAPISamedocument(Page page, TestServer server) {
    server.setRoute("/intercept.html", (req, res) -> {
      res.writeHead(200, "Content-Type", "text/html");
      res.end("\n" +
        "      <a id=\"go\" href=\"/other\">go</a>\n" +
        "      <p id=\"status\">initial</p>\n" +
        "      <script>\n" +
        "        navigation.addEventListener('navigate', event => {\n" +
        "          if (!event.canIntercept)\n" +
        "            return;\n" +
        "          event.intercept({\n" +
        "            handler: async () => {\n" +
        "              const dest = new URL(event.destination.url).pathname;\n" +
        "              document.getElementById('status').textContent = 'intercepted:' + dest;\n" +
        "            },\n" +
        "          });\n" +
        "        });\n" +
        "      </script>\n" +
        "    ");
    });

    page.navigate(server.PREFIX + "/intercept.html");
    assertThat(page.locator("#status")).hasText("initial");

    // Should not hang waiting for the same-document navigation to commit.
    page.locator("#go").click();

    assertThat(page.locator("#status")).hasText("intercepted:/other");
    assertEquals("/other", URI.create(page.url()).getPath());
  }

  @PlaywrightTest
  @DisplayName("AW-023 goBack in the middle of navigation that commits")
  void goBackInTheMiddleOfNavigationThatCommits(Page page, TestServer server) throws Exception {
    CompletableFuture<Void> commitPromise = new CompletableFuture<>();

    CompletableFuture<Void> stallPromise = new CompletableFuture<>();

    server.setRoute("/stall.html", (req, res) -> {
      stallPromise.complete(null);
      commitPromise.get();
      res.writeHead(200, "Content-Type", "text/html");
      res.end("hello world");
    });

    page.navigate(server.PREFIX + "/one-style.html");
    async(() -> {
      try {
        return page.navigate(server.PREFIX + "/stall.html");
      } catch (PlaywrightException e) {
        return null;
      }
    });
    stallPromise.get();

    CompletableFuture<Object> goBackPromise = async(() -> {
      try {
        return page.goBack();
      } catch (PlaywrightException e) {
        return null;
      }
    });
    page.waitForTimeout(1000);
    commitPromise.complete(null);

    goBackPromise.get();
  }
}
