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
import com.microsoft.playwright.Response;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitUntilState;
import org.junit.jupiter.api.DisplayName;


import static org.junit.jupiter.api.Assertions.assertEquals;

/** Port of tests/page/page-network-idle.spec.ts. */
public class TestPageNetworkIdle {
  @PlaywrightTest
  @DisplayName("NE-070 should navigate to empty page with networkidle")
  void shouldNavigateToEmptyPageWithNetworkidle(Page page, TestServer server) {
    Response response = page.navigate(server.EMPTY_PAGE, new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));
    assertEquals(200, response.status());
  }

  @PlaywrightTest
  @DisplayName("NE-079 should wait for networkidle from the popup")
  void shouldWaitForNetworkidleFromThePopup(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.setContent("\n" +
      "    <button id=box1 onclick=\"window.open('./popup/popup.html')\">Button1</button>\n" +
      "    <button id=box2 onclick=\"window.open('./popup/popup.html')\">Button2</button>\n" +
      "    <button id=box3 onclick=\"window.open('./popup/popup.html')\">Button3</button>\n" +
      "    <button id=box4 onclick=\"window.open('./popup/popup.html')\">Button4</button>\n" +
      "    <button id=box5 onclick=\"window.open('./popup/popup.html')\">Button5</button>\n" +
      "  ");
    for (int i = 1; i < 6; ++i) {
      int box = i;
      Page popup = page.waitForPopup(() -> page.click("#box" + box));
      popup.waitForLoadState(LoadState.NETWORKIDLE);
    }
  }

  @PlaywrightTest
  @DisplayName("NE-081 should work after repeated navigations in the same page")
  void shouldWorkAfterRepeatedNavigationsInTheSamePage(Page page, TestServer server) {
    int[] requestCount = {0};
    page.route("**/empty.html", route -> {
      route.fulfill(new Route.FulfillOptions()
        .setContentType("text/html")
        .setBody("\n" +
          "        <script>\n" +
          "          fetch('http://localhost:8000/sample').then(res => console.log(res.json()))\n" +
          "        </script>"));
    });

    page.route("**/sample", route -> {
      requestCount[0]++;
      route.fulfill(new Route.FulfillOptions()
        .setContentType("application/json")
        .setBody("{\"content\":\"sample\"}"));
    });

    page.navigate(server.EMPTY_PAGE, new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));
    assertEquals(1, requestCount[0]);
    page.navigate(server.EMPTY_PAGE, new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));
    assertEquals(2, requestCount[0]);
  }

  @PlaywrightTest
  @DisplayName("NE-082 should not wait for an open EventSource connection")
  void shouldNotWaitForAnOpenEventSourceConnection(Page page, TestServer server) {
    server.setRoute("/sse", (req, res) -> {
      res.writeHead(200,
        "Content-Type", "text/event-stream",
        "Connection", "keep-alive",
        "Cache-Control", "no-cache");
      res.write("data: hello\n\n");
    });
    server.setRoute("/sse-page.html", (req, res) -> {
      res.writeHead(200, "Content-Type", "text/html");
      res.end("<script>new EventSource('/sse');</script>");
    });

    Response response = page.navigate(server.PREFIX + "/sse-page.html", new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));
    assertEquals(200, response.status());
  }

  @PlaywrightTest
  @DisplayName("NE-083 should not wait for an open EventSource connection in setContent")
  void shouldNotWaitForAnOpenEventSourceConnectionInSetContent(Page page, TestServer server) {
    server.setRoute("/sse", (req, res) -> {
      res.writeHead(200,
        "Content-Type", "text/event-stream",
        "Connection", "keep-alive",
        "Cache-Control", "no-cache");
      res.write("data: hello\n\n");
    });

    page.navigate(server.EMPTY_PAGE);
    page.setContent("<script>\n" +
      "    window.__sseOpened = new Promise(resolve => {\n" +
      "      const es = new EventSource('/sse');\n" +
      "      es.onmessage = () => resolve();\n" +
      "    });\n" +
      "  </script>", new Page.SetContentOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));
    page.evaluate("() => window['__sseOpened']");
  }
}
