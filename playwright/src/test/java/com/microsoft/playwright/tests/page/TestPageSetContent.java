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
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.WaitUntilState;
import org.junit.jupiter.api.DisplayName;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/page-set-content.spec.ts. */
public class TestPageSetContent {
  private static final String expectedOutput = "<html><head></head><body><div>hello</div></body></html>";

  @PlaywrightTest
  @DisplayName("PA-020 should work @smoke")
  void shouldWork(Page page, TestServer server) {
    page.setContent("<div>hello</div>");
    String result = page.content();
    assertEquals(expectedOutput, result);
  }

  @PlaywrightTest
  @DisplayName("PA-021 should work with domcontentloaded")
  void shouldWorkWithDomcontentloaded(Page page, TestServer server) {
    page.setContent("<div>hello</div>", new Page.SetContentOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
    String result = page.content();
    assertEquals(expectedOutput, result);
  }

  @PlaywrightTest
  @DisplayName("PA-022 should work with commit")
  void shouldWorkWithCommit(Page page) {
    page.setContent("<div>hello</div>", new Page.SetContentOptions().setWaitUntil(WaitUntilState.COMMIT));
    String result = page.content();
    assertEquals(expectedOutput, result);
  }

  @PlaywrightTest
  @DisplayName("PA-023 should work with doctype")
  void shouldWorkWithDoctype(Page page, TestServer server) {
    String doctype = "<!DOCTYPE html>";
    page.setContent(doctype + "<div>hello</div>");
    String result = page.content();
    assertEquals(doctype + expectedOutput, result);
  }

  @PlaywrightTest
  @DisplayName("PA-024 should work with HTML 4 doctype")
  void shouldWorkWithHTML4Doctype(Page page, TestServer server) {
    String doctype = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" " +
      "\"http://www.w3.org/TR/html4/strict.dtd\">";
    page.setContent(doctype + "<div>hello</div>");
    String result = page.content();
    assertEquals(doctype + expectedOutput, result);
  }

  @PlaywrightTest
  @DisplayName("PA-025 should include shadow roots")
  void shouldIncludeShadowRoots(Page page) {
    String html = "<!DOCTYPE html><html lang=\"en\"><head></head><body><div id=\"host\"><template shadowrootmode=\"open\"><div id=\"nested\"><template shadowrootmode=\"open\"><span>nested</span></template><slot></slot></div></template><span>light</span></div><div id=\"closed\"></div></body></html>";
    // Closed shadow roots are not accessible from script and are never serialized.
    page.setContent(html.replace("<div id=\"closed\">", "<div id=\"closed\"><template shadowrootmode=\"closed\"><span>closed</span></template>"));
    assertEquals("<!DOCTYPE html><html lang=\"en\"><head></head><body><div id=\"host\"><span>light</span></div><div id=\"closed\"></div></body></html>", page.content());
    assertEquals(html, page.content(new Page.ContentOptions().setIncludeShadow(true)));
  }

  @PlaywrightTest
  @DisplayName("PA-026 should respect timeout")
  void shouldRespectTimeout(Page page, TestServer server) {
    String imgPath = "/img.png";
    // stall for image
    server.setRoute(imgPath, (req, res) -> {});
    assertThrows(TimeoutError.class, () -> page.setContent("<img src=\"" + server.PREFIX + imgPath + "\"></img>", new Page.SetContentOptions().setTimeout(1)));
  }

  @PlaywrightTest
  @DisplayName("PA-027 should respect default navigation timeout")
  @Fixme("java: the error message has no 'page.setContent:' prefix, it is 'Timeout 1ms exceeded.'")
  void shouldRespectDefaultNavigationTimeout(Page page, TestServer server) {
    page.setDefaultNavigationTimeout(1);
    String imgPath = "/img.png";
    // stall for image
    server.setRoute(imgPath, (req, res) -> {});
    TimeoutError error = assertThrows(TimeoutError.class, () -> page.setContent("<img src=\"" + server.PREFIX + imgPath + "\"></img>"));
    assertTrue(error.getMessage().contains("page.setContent: Timeout 1ms exceeded."));
  }

  @PlaywrightTest
  @DisplayName("PA-028 should await resources to load")
  void shouldAwaitResourcesToLoad(Page page, TestServer server) throws Exception {
    String imgPath = "/img.png";
    AtomicBoolean loaded = new AtomicBoolean(false);
    AtomicBoolean loadedWhenRequested = new AtomicBoolean(true);
    // Java: upstream ends the response after setContent has started; the route handler runs on the
    // thread of the server, so it ends the response and setContent is called inline.
    server.setRoute(imgPath, (req, res) -> {
      loadedWhenRequested.set(loaded.get());
      res.end();
    });
    page.setContent("<img src=\"" + server.PREFIX + imgPath + "\"></img>");
    loaded.set(true);
    assertEquals(false, loadedWhenRequested.get());
  }

  @PlaywrightTest
  @DisplayName("PA-029 should work fast enough")
  void shouldWorkFastEnough(Page page, TestServer server) {
    for (int i = 0; i < 20; ++i)
      page.setContent("<div>yo</div>");
  }

  @PlaywrightTest
  @DisplayName("PA-030 should work with tricky content")
  void shouldWorkWithTrickyContent(Page page, TestServer server) {
    page.setContent("<div>hello world</div>" + "\u007F");
    assertEquals("hello world", page.evalOnSelector("div", "div => div.textContent"));
  }

  @PlaywrightTest
  @DisplayName("PA-031 should work with accents")
  void shouldWorkWithAccents(Page page, TestServer server) {
    page.setContent("<div>aberración</div>");
    assertEquals("aberración", page.evalOnSelector("div", "div => div.textContent"));
  }

  @PlaywrightTest
  @DisplayName("PA-032 should work with emojis")
  void shouldWorkWithEmojis(Page page, TestServer server) {
    page.setContent("<div>🐥</div>");
    assertEquals("🐥", page.evalOnSelector("div", "div => div.textContent"));
  }

  @PlaywrightTest
  @DisplayName("PA-033 should work with newline")
  void shouldWorkWithNewline(Page page, TestServer server) {
    page.setContent("<div>\n</div>");
    assertEquals("\n", page.evalOnSelector("div", "div => div.textContent"));
  }

  @PlaywrightTest
  @DisplayName("PA-035 should return empty content there is no iframe src")
  @DisabledOnBrowser(value = BrowserName.WEBKIT, reason = "fixme: there is no utility context")
  void shouldReturnEmptyContentThereIsNoIframeSrc(Page page) {
    page.setContent("<iframe src=\"javascript:console.log(1)\"></iframe>");
    assertEquals(2, page.frames().size());
    assertEquals("<html><head></head><body></body></html>", page.frames().get(1).content());
  }
}
