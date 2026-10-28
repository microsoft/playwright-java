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
import org.junit.jupiter.api.DisplayName;

import static com.microsoft.playwright.fixtures.Utils.attachFrame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** Port of tests/page/elementhandle-content-frame.spec.ts. */
public class TestElementhandleContentFrame {
  @PlaywrightTest
  @DisplayName("EH-019 should work")
  void shouldWork(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    attachFrame(page, "frame1", server.EMPTY_PAGE);
    ElementHandle elementHandle = page.querySelector("#frame1");
    Frame frame = elementHandle.contentFrame();
    assertEquals(page.frames().get(1), frame);
  }

  @PlaywrightTest
  @DisplayName("EH-020 should work for cross-process iframes")
  void shouldWorkForCrossProcessIframes(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    attachFrame(page, "frame1", server.CROSS_PROCESS_PREFIX + "/empty.html");
    ElementHandle elementHandle = page.querySelector("#frame1");
    Frame frame = elementHandle.contentFrame();
    assertEquals(page.frames().get(1), frame);
  }

  @PlaywrightTest
  @DisplayName("EH-021 should work for cross-frame evaluations")
  void shouldWorkForCrossFrameEvaluations(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    attachFrame(page, "frame1", server.EMPTY_PAGE);
    Frame frame = page.frames().get(1);
    ElementHandle elementHandle = frame.evaluateHandle("() => window.top.document.querySelector('#frame1')").asElement();
    assertEquals(frame, elementHandle.contentFrame());
  }

  @PlaywrightTest
  @DisplayName("EH-022 should return null for non-iframes")
  void shouldReturnNullForNonIframes(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    attachFrame(page, "frame1", server.EMPTY_PAGE);
    Frame frame = page.frames().get(1);
    ElementHandle elementHandle = frame.evaluateHandle("() => document.body").asElement();
    assertNull(elementHandle.contentFrame());
  }

  @PlaywrightTest
  @DisplayName("EH-023 should return null for document.documentElement")
  void shouldReturnNullForDocumentDocumentElement(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    attachFrame(page, "frame1", server.EMPTY_PAGE);
    Frame frame = page.frames().get(1);
    ElementHandle elementHandle = frame.evaluateHandle("() => document.documentElement").asElement();
    assertNull(elementHandle.contentFrame());
  }
}
