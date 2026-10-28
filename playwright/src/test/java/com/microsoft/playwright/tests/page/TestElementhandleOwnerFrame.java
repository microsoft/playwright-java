/**
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
import com.microsoft.playwright.Page;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.LoadState;
import org.junit.jupiter.api.DisplayName;

import static com.microsoft.playwright.fixtures.Utils.attachFrame;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** Port of tests/page/elementhandle-owner-frame.spec.ts. */
public class TestElementhandleOwnerFrame {
  @PlaywrightTest
  @DisplayName("EH-055 should work")
  void shouldWork(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    attachFrame(page, "frame1", server.EMPTY_PAGE);
    Frame frame = page.frames().get(1);
    ElementHandle elementHandle = frame.evaluateHandle("() => document.body").asElement();
    assertEquals(frame, elementHandle.ownerFrame());
  }

  @PlaywrightTest
  @DisplayName("EH-056 should work for cross-process iframes")
  void shouldWorkForCrossProcessIframes(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    attachFrame(page, "frame1", server.CROSS_PROCESS_PREFIX + "/empty.html");
    Frame frame = page.frames().get(1);
    ElementHandle elementHandle = frame.evaluateHandle("() => document.body").asElement();
    assertEquals(frame, elementHandle.ownerFrame());
  }

  @PlaywrightTest
  @DisplayName("EH-057 should work for document")
  void shouldWorkForDocument(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    attachFrame(page, "frame1", server.EMPTY_PAGE);
    Frame frame = page.frames().get(1);
    ElementHandle elementHandle = frame.evaluateHandle("() => document").asElement();
    assertEquals(frame, elementHandle.ownerFrame());
  }

  @PlaywrightTest
  @DisplayName("EH-058 should work for iframe elements")
  void shouldWorkForIframeElements(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    attachFrame(page, "frame1", server.EMPTY_PAGE);
    Frame frame = page.mainFrame();
    ElementHandle elementHandle = frame.evaluateHandle("() => document.querySelector('#frame1')").asElement();
    assertEquals(frame, elementHandle.ownerFrame());
  }

  @PlaywrightTest
  @DisplayName("EH-059 should work for cross-frame evaluations")
  void shouldWorkForCrossFrameEvaluations(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    attachFrame(page, "frame1", server.EMPTY_PAGE);
    Frame frame = page.mainFrame();
    ElementHandle elementHandle = frame.evaluateHandle("() => document.querySelector('iframe').contentWindow.document.body").asElement();
    assertEquals(frame.childFrames().get(0), elementHandle.ownerFrame());
  }

  @PlaywrightTest
  @DisplayName("EH-060 should work for detached elements")
  void shouldWorkForDetachedElements(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    ElementHandle divHandle = page.evaluateHandle("() => {\n" +
      "    const div = document.createElement('div');\n" +
      "    document.body.appendChild(div);\n" +
      "    return div;\n" +
      "  }").asElement();
    assertEquals(page.mainFrame(), divHandle.ownerFrame());
    page.evaluate("() => {\n" +
      "    const div = document.querySelector('div');\n" +
      "    document.body.removeChild(div);\n" +
      "  }");
    assertEquals(page.mainFrame(), divHandle.ownerFrame());
  }

  @PlaywrightTest
  @DisplayName("EH-061 should work for adopted elements")
  void shouldWorkForAdoptedElements(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    Page popup = page.waitForPopup(() -> page.evaluate("url => window['__popup'] = window.open(url)", server.EMPTY_PAGE));
    ElementHandle divHandle = page.evaluateHandle("() => {\n" +
      "    const div = document.createElement('div');\n" +
      "    document.body.appendChild(div);\n" +
      "    return div;\n" +
      "  }").asElement();
    assertEquals(page.mainFrame(), divHandle.ownerFrame());
    popup.waitForLoadState(LoadState.DOMCONTENTLOADED);
    page.evaluate("() => {\n" +
      "    const div = document.querySelector('div');\n" +
      "    window['__popup'].document.body.appendChild(div);\n" +
      "  }");
    assertEquals(popup.mainFrame(), divHandle.ownerFrame());
  }
}
