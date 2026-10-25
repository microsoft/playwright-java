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
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import static com.microsoft.playwright.fixtures.Utils.attachFrame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/frame-frame-element.spec.ts. */
public class TestFrameFrameElement {
  @PlaywrightTest
  @DisplayName("FR-014 should work @smoke")
  void shouldWork(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    Frame frame1 = attachFrame(page, "frame1", server.EMPTY_PAGE);
    attachFrame(page, "frame2", server.EMPTY_PAGE);
    Frame frame3 = attachFrame(page, "frame3", server.EMPTY_PAGE);
    ElementHandle frame1handle1 = page.querySelector("#frame1");
    ElementHandle frame1handle2 = frame1.frameElement();
    ElementHandle frame3handle1 = page.querySelector("#frame3");
    ElementHandle frame3handle2 = frame3.frameElement();
    assertEquals(true, frame1handle1.evaluate("(a, b) => a === b", frame1handle2));
    assertEquals(true, frame3handle1.evaluate("(a, b) => a === b", frame3handle2));
    assertEquals(false, frame1handle1.evaluate("(a, b) => a === b", frame3handle1));
  }

  @PlaywrightTest
  @DisplayName("FR-015 should work with contentFrame")
  void shouldWorkWithContentFrame(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    Frame frame = attachFrame(page, "frame1", server.EMPTY_PAGE);
    ElementHandle handle = frame.frameElement();
    Frame contentFrame = handle.contentFrame();
    assertEquals(frame, contentFrame);
  }

  @PlaywrightTest
  @DisplayName("FR-016 should work with frameset")
  void shouldWorkWithFrameset(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/frames/frameset.html");
    ElementHandle frameElement1 = page.querySelector("frame");
    Frame frame = frameElement1.contentFrame();
    ElementHandle frameElement2 = frame.frameElement();
    assertEquals(true, frameElement1.evaluate("(a, b) => a === b", frameElement2));
  }

  @PlaywrightTest
  @DisplayName("FR-017 should throw when detached")
  void shouldThrowWhenDetached(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    Frame frame1 = attachFrame(page, "frame1", server.EMPTY_PAGE);
    page.evalOnSelector("#frame1", "e => e.remove()");
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> frame1.frameElement());
    assertTrue(error.getMessage().contains("Frame has been detached."));
  }

  @PlaywrightTest
  @DisplayName("FR-018 should work inside closed shadow root")
  void shouldWorkInsideClosedShadowRoot(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.setContent("\n" +
      "    <div id=framecontainer>\n" +
      "    </div>\n" +
      "    <script>\n" +
      "      const iframe = document.createElement('iframe');\n" +
      "      iframe.setAttribute('name', 'myframe');\n" +
      "      iframe.setAttribute('srcdoc', 'find me');\n" +
      "      const div = document.getElementById('framecontainer');\n" +
      "      const host = div.attachShadow({ mode: 'closed' });\n" +
      "      host.appendChild(iframe);\n" +
      "    </script>\n" +
      "  ");

    Frame frame = page.frames().get(1);
    ElementHandle element = frame.frameElement();
    assertEquals("myframe", element.getAttribute("name"));
  }

  @PlaywrightTest
  @DisplayName("FR-019 should work inside declarative shadow root")
  void shouldWorkInsideDeclarativeShadowRoot(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.setContent("\n" +
      "    <div>\n" +
      "      <template shadowrootmode=\"open\">\n" +
      "        <iframe name=\"myframe\" srcdoc=\"<h1>Hi!</h1>\"></iframe>\n" +
      "        <slot></slot>\n" +
      "      </template>\n" +
      "      <span>footer</span>\n" +
      "    </div>\n" +
      "  ");
    Frame frame = page.frames().get(1);
    ElementHandle element = frame.frameElement();
    assertEquals("myframe", element.getAttribute("name"));
  }
}
