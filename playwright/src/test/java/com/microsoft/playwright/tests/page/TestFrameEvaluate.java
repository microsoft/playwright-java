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

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.JSHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.WaitUntilState;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.microsoft.playwright.fixtures.Utils.async;
import static com.microsoft.playwright.fixtures.Utils.attachFrame;
import static com.microsoft.playwright.fixtures.Utils.detachFrame;
import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/frame-evaluate.spec.ts. */
public class TestFrameEvaluate {
  @PlaywrightTest
  @DisplayName("FR-001 should have different execution contexts")
  void shouldHaveDifferentExecutionContexts(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    attachFrame(page, "frame1", server.EMPTY_PAGE);
    assertEquals(2, page.frames().size());
    page.frames().get(0).evaluate("() => window['FOO'] = 'foo'");
    page.frames().get(1).evaluate("() => window['FOO'] = 'bar'");
    assertEquals("foo", page.frames().get(0).evaluate("() => window['FOO']"));
    assertEquals("bar", page.frames().get(1).evaluate("() => window['FOO']"));
  }

  @PlaywrightTest
  @DisplayName("FR-002 should have correct execution contexts @smoke")
  void shouldHaveCorrectExecutionContexts(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/frames/one-frame.html");
    assertEquals(2, page.frames().size());
    assertEquals("", page.frames().get(0).evaluate("() => document.body.textContent.trim()"));
    assertEquals("Hi, I'm frame", page.frames().get(1).evaluate("() => document.body.textContent.trim()"));
  }

  // function expectContexts(pageImpl, count, browserName) reads the internals of the driver.

  // FR-003 should dispose context on navigation: not ported, see upstream_not_ported.txt.

  // FR-004 should dispose context on cross-origin navigation: not ported, see upstream_not_ported.txt.

  @PlaywrightTest
  @DisplayName("FR-005 should execute after cross-site navigation")
  void shouldExecuteAfterCrossSiteNavigation(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    Frame mainFrame = page.mainFrame();
    assertTrue(((String) mainFrame.evaluate("() => window.location.href")).contains(server.EMPTY_PAGE));
    page.navigate(server.CROSS_PROCESS_PREFIX + "/empty.html");
    assertTrue(((String) mainFrame.evaluate("() => window.location.href")).contains(server.CROSS_PROCESS_PREFIX));
  }

  @PlaywrightTest
  @DisplayName("FR-006 should not allow cross-frame js handles")
  void shouldNotAllowCrossFrameJsHandles(Page page, TestServer server) {
    // TODO: this should actually be possible because frames script each other,
    // but protocol implementations do not support this. For now, assume current
    // behavior.
    page.navigate(server.PREFIX + "/frames/one-frame.html");
    JSHandle handle = page.evaluateHandle("() => {\n" +
      "    const iframe = document.querySelector('iframe');\n" +
      "    const foo = { bar: 'baz' };\n" +
      "    iframe.contentWindow['__foo'] = foo;\n" +
      "    return foo;\n" +
      "  }");
    Frame childFrame = page.mainFrame().childFrames().get(0);
    Object childResult = childFrame.evaluate("() => window['__foo']");
    assertEquals(mapOf("bar", "baz"), childResult);
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> childFrame.evaluate("foo => foo.bar", handle));
    assertTrue(error.getMessage().contains("JSHandles can be evaluated only in the context they were created!"));
  }

  @PlaywrightTest
  @DisplayName("FR-007 should allow cross-frame element handles")
  void shouldAllowCrossFrameElementHandles(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/frames/one-frame.html");
    ElementHandle bodyHandle = page.mainFrame().childFrames().get(0).querySelector("body");
    String result = (String) page.evaluate("body => body.innerHTML", bodyHandle);
    assertEquals("<div>Hi, I'm frame</div>", result.trim());
  }

  @PlaywrightTest
  @DisplayName("FR-008 should not allow cross-frame element handles when frames do not script each other")
  void shouldNotAllowCrossFrameElementHandlesWhenFramesDoNotScriptEachOther(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    Frame frame = attachFrame(page, "frame1", server.CROSS_PROCESS_PREFIX + "/empty.html");
    ElementHandle bodyHandle = frame.querySelector("body");
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.evaluate("body => body.innerHTML", bodyHandle));
    assertTrue(error.getMessage().contains("Unable to adopt element handle from a different document"));
  }

  @PlaywrightTest
  @DisplayName("FR-009 should throw for detached frames")
  @Fixme("java: the error message is 'Frame was detached' without the 'frame.evaluate: ' prefix")
  void shouldThrowForDetachedFrames(Page page, TestServer server) {
    Frame frame1 = attachFrame(page, "frame1", server.EMPTY_PAGE);
    detachFrame(page, "frame1");
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> frame1.evaluate("() => 7 * 8"));
    assertTrue(error.getMessage().contains("frame.evaluate: Frame was detached"));
  }

  @PlaywrightTest
  @DisplayName("FR-010 should be isolated between frames")
  void shouldBeIsolatedBetweenFrames(Page page, TestServer server) throws Exception {
    page.navigate(server.EMPTY_PAGE);
    attachFrame(page, "frame1", server.EMPTY_PAGE);
    assertEquals(2, page.frames().size());
    List<Frame> frames = page.frames();
    Frame frame1 = frames.get(0);
    Frame frame2 = frames.get(1);
    assertTrue(frame1 != frame2);

    // Promise.all
    CompletableFuture<Object> set1 = async(() -> frame1.evaluate("() => window['a'] = 1"));
    frame2.evaluate("() => window['a'] = 2");
    set1.get();
    // Promise.all
    CompletableFuture<Object> get1 = async(() -> frame1.evaluate("() => window['a']"));
    Object a2 = frame2.evaluate("() => window['a']");
    Object a1 = get1.get();
    assertEquals(1, a1);
    assertEquals(2, a2);
  }

  @PlaywrightTest
  @DisplayName("FR-011 should work in iframes that failed initial navigation")
  void shouldWorkInIframesThatFailedInitialNavigation(Page page, BrowserName browserName) {
    page.setContent("\n" +
      "    <meta http-equiv=\"Content-Security-Policy\" content=\"script-src 'none';\">\n" +
      "    <iframe src='javascript:\"\"'></iframe>\n" +
      "  ", new Page.SetContentOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
    page.evaluate("() => {\n" +
      "    const iframe = document.querySelector('iframe');\n" +
      "    const div = iframe.contentDocument.createElement('div');\n" +
      "    iframe.contentDocument.body.appendChild(div);\n" +
      "  }");
    assertEquals(browserName == BrowserName.WEBKIT ? "about:blank" : "", page.frames().get(1).url());
    // Main world should work.
    assertEquals("about:blank", page.frames().get(1).evaluate("() => window.location.href"));
    // Utility world should work.
    assertNotNull(page.frames().get(1).querySelector("div"));
  }

  @PlaywrightTest
  @DisplayName("FR-012 should work in iframes that interrupted initial javascript url navigation")
  void shouldWorkInIframesThatInterruptedInitialJavascriptUrlNavigation(Page page, TestServer server) {
    // Chromium does not report isolated world for the iframe.
    page.navigate(server.EMPTY_PAGE);
    page.evaluate("() => {\n" +
      "    const iframe = document.createElement('iframe');\n" +
      "    iframe.src = 'javascript:\"\"';\n" +
      "    document.body.appendChild(iframe);\n" +
      "    iframe.contentDocument.open();\n" +
      "    iframe.contentDocument.write('<div>hello</div>');\n" +
      "    iframe.contentDocument.close();\n" +
      "  }");
    // Main world should work.
    assertEquals(server.EMPTY_PAGE, page.frames().get(1).evaluate("() => window.top.location.href"));
    // Utility world should work.
    assertNotNull(page.frames().get(1).querySelector("div"));
  }

  @PlaywrightTest
  @DisplayName("FR-013 evaluateHandle should work")
  void evaluateHandleShouldWork(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    Frame mainFrame = page.mainFrame();
    JSHandle windowHandle = mainFrame.evaluateHandle("() => window");
    assertNotNull(windowHandle);
  }
}
