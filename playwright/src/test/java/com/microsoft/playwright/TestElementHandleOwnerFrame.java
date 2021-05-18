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

import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED;
import static com.microsoft.playwright.Utils.attachFrame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestElementHandleOwnerFrame extends TestBase {
  @Test
  void shouldWork() {
    page.navigate(server.EMPTY_PAGE);
    attachFrame(page, "frame1", server.EMPTY_PAGE);
    Frame frame = page.frames().get(1);
    JSHandle jsHandle = frame.evaluateHandle("() => document.body");
    ElementHandle elementHandle = jsHandle.asElement();
    assertNotNull(elementHandle);
    assertEquals(frame, elementHandle.ownerFrame());
  }

  @Test
  void shouldWorkForCrossProcessIframes() {
    page.navigate(server.EMPTY_PAGE);
    attachFrame(page, "frame1", server.CROSS_PROCESS_PREFIX + "/empty.html");
    Frame frame = page.frames().get(1);
    JSHandle jsHandle = frame.evaluateHandle("() => document.body");
    ElementHandle elementHandle = jsHandle.asElement();
    assertNotNull(elementHandle);
    assertEquals(frame, elementHandle.ownerFrame());
  }

  @Test
  void shouldWorkForDocument() {
    // TODO: test.flaky(platform === "win32" && browserName === "webkit");
    page.navigate(server.EMPTY_PAGE);
    attachFrame(page, "frame1", server.EMPTY_PAGE);
    Frame frame = page.frames().get(1);
    JSHandle jsHandle = frame.evaluateHandle("document");
    ElementHandle elementHandle = jsHandle.asElement();
    assertNotNull(elementHandle);
    assertEquals(frame, elementHandle.ownerFrame());
  }

  @Test
  void shouldWorkForIframeElements() {
    page.navigate(server.EMPTY_PAGE);
    attachFrame(page, "frame1", server.EMPTY_PAGE);
    Frame frame = page.mainFrame();
    JSHandle jsHandle = frame.evaluateHandle("() => document.querySelector('#frame1')");
    ElementHandle elementHandle = jsHandle.asElement();
    assertNotNull(elementHandle);
    assertEquals(frame, elementHandle.ownerFrame());
  }

  @Test
  void shouldWorkForCrossFrameEvaluations() {
    page.navigate(server.EMPTY_PAGE);
    attachFrame(page, "frame1", server.EMPTY_PAGE);
    Frame frame = page.mainFrame();
    JSHandle elementHandle = frame.evaluateHandle(  "() => document.querySelector('iframe').contentWindow.document.body");
    assertEquals(frame.childFrames().get(0), elementHandle.asElement().ownerFrame());
  }

  @Test
  void shouldWorkForDetachedElements() {
    page.navigate(server.EMPTY_PAGE);
    JSHandle divHandle = page.evaluateHandle("() => {\n" +
      "  const div = document.createElement('div');\n" +
      "  document.body.appendChild(div);\n" +
      "  return div;\n" +
      "}");
    assertEquals(page.mainFrame(), divHandle.asElement().ownerFrame());
    page.evaluate("() => {\n" +
      "  const div = document.querySelector('div');\n" +
      "  document.body.removeChild(div);\n" +
      "}");
    assertEquals(page.mainFrame(), divHandle.asElement().ownerFrame());
}

  @Test
  void shouldWorkForAdoptedElements() {
    page.navigate(server.EMPTY_PAGE);
    Page popup = page.waitForPopup(() -> page.evaluate("url => window['__popup'] = window.open(url)", server.EMPTY_PAGE));
    JSHandle divHandle = page.evaluateHandle("() => {\n" +
     "  const div = document.createElement('div');\n" +
     "  document.body.appendChild(div);\n" +
     "  return div;\n" +
     "}");
    assertEquals(page.mainFrame(), divHandle.asElement().ownerFrame());
    popup.waitForLoadState(DOMCONTENTLOADED);
    page.evaluate("() => {\n" +
      "  const div = document.querySelector('div');\n" +
      "  window['__popup'].document.body.appendChild(div);\n" +
      "}");
    assertEquals(popup.mainFrame(), divHandle.asElement().ownerFrame());
  }
}
