/*
 * Copyright (c) Microsoft Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.playwright;

import com.microsoft.playwright.options.WaitUntilState;
import org.junit.jupiter.api.Test;

import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.concurrent.Semaphore;

import static java.util.Collections.nCopies;
import static org.junit.jupiter.api.Assertions.*;

public class TestPageWaitForUrl extends TestBase {
  @Test
  void shouldWork() {
    page.navigate(server.EMPTY_PAGE);
    page.evaluate("url => window.location.href = url", server.PREFIX + "/grid.html");
    page.waitForURL("**/grid.html");
  }

  @Test
  void shouldRespectTimeout() {
    page.navigate(server.EMPTY_PAGE);
    TimeoutError e = assertThrows(TimeoutError.class, () -> {
      page.waitForURL("**/frame.html", new Page.WaitForURLOptions().setTimeout(2500));
    });
    assertTrue(e.getMessage().contains("Timeout 2500ms exceeded"));
  }

  @Test
  void shouldWorkWithBothDomcontentloadedAndLoad() {
    page.navigate(server.PREFIX + "/one-style.html");
    page.waitForURL("**/one-style.html", new Page.WaitForURLOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
    page.waitForURL("**/one-style.html", new Page.WaitForURLOptions().setWaitUntil(WaitUntilState.LOAD));
  }

  @Test
  void shouldWorkWithCommit() {
    server.setRoute("/script.js", exchange -> {});
    server.setRoute("/empty.html", exchange -> {
      exchange.getResponseHeaders().add("Content-Type", "text/html");
      exchange.sendResponseHeaders(200, 0);
      try (OutputStreamWriter writer = new OutputStreamWriter(exchange.getResponseBody())) {
        writer.write("<title>Hello</title><script src=\"script.js\"></script>");
      }
    });
    try {
      page.navigate(server.EMPTY_PAGE, new Page.NavigateOptions().setTimeout(100));
    } catch (TimeoutError e) {
    }
    page.waitForURL("**/empty.html", new Page.WaitForURLOptions().setWaitUntil(WaitUntilState.COMMIT));
    assertEquals("Hello", page.title());
  }

  @Test
  void shouldWorkWithCommitAndAboutBlank() {
    page.waitForURL("about:blank", new Page.WaitForURLOptions().setWaitUntil(WaitUntilState.COMMIT));
  }

  @Test
  void shouldWorkWithClickingOnAnchorLinks() {
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<a href='#foobar'>foobar</a>");
    page.click("a");
    page.waitForURL("**/*#foobar");
  }

  @Test
  void shouldWorkWithHistoryPushState() {
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<a onclick='javascript:pushState()'>SPA</a>\n" +
      "<script>\n" +
      "  function pushState() { history.pushState({}, '', 'wow.html') }\n" +
      "</script>");
    page.click("a");
    page.waitForURL("**/wow.html");
    assertEquals(server.PREFIX + "/wow.html", page.url());
  }

  @Test
  void shouldWorkWithHistoryReplaceState() {
    page.navigate(server.EMPTY_PAGE);
    page.setContent(" <a onclick='javascript:replaceState()'>SPA</a>\n" +
      "<script>\n" +
      "  function replaceState() { history.replaceState({}, '', '/replaced.html') }\n" +
      "</script>");
    page.click("a");
    page.waitForURL("**/replaced.html");
    assertEquals(server.PREFIX + "/replaced.html", page.url());
  }

  @Test
  void shouldWorkWithDOMHistoryBackHistoryForward() {
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<a id=back onclick='javascript:goBack()'>back</a>\n" +
      "<a id=forward onclick='javascript:goForward()'>forward</a>\n" +
      "<script>\n" +
      "  function goBack() { history.back(); }\n" +
      "  function goForward() { history.forward(); }\n" +
      "  history.pushState({}, '', '/first.html');\n" +
      "  history.pushState({}, '', '/second.html');\n" +
      "</script>");
    assertEquals(server.PREFIX + "/second.html", page.url());

    page.click("a#back");
    page.waitForURL("**/first.html");
    assertEquals(server.PREFIX + "/first.html", page.url());

    page.click("a#forward");
    page.waitForURL("**/second.html");
    assertEquals(server.PREFIX + "/second.html", page.url());
  }

  @Test
  void shouldWorkOnFrame() {
    page.navigate(server.PREFIX + "/frames/one-frame.html");
    Frame frame = page.frames().get(1);
    frame.evaluate("url => window.location.href = url", server.PREFIX + "/grid.html");
    frame.waitForURL("**/grid.html");
  }
}
