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

import static com.microsoft.playwright.Utils.attachFrame;
import static org.junit.jupiter.api.Assertions.*;

public class TestElementHandleContentFrame extends TestBase {

  @Test
  void shouldWork() {
    page.navigate(server.EMPTY_PAGE);
    attachFrame(page, "frame1", server.EMPTY_PAGE);
    ElementHandle elementHandle = page.querySelector("#frame1");
    Frame frame = elementHandle.contentFrame();
    assertEquals(page.frames().get(1), frame);
  }

  @Test
  void shouldWorkForCrossProcessIframes() {
    page.navigate(server.EMPTY_PAGE);
    attachFrame(page, "frame1", server.CROSS_PROCESS_PREFIX + "/empty.html");
    ElementHandle elementHandle = page.querySelector("#frame1");
    Frame frame = elementHandle.contentFrame();
    assertEquals(page.frames().get(1), frame);
  }

  @Test
  void shouldWorkForCrossFrameEvaluations() {
    page.navigate(server.EMPTY_PAGE);
    attachFrame(page, "frame1", server.EMPTY_PAGE);
    Frame frame = page.frames().get(1);
    JSHandle jsHandle = frame.evaluateHandle("() => window.top.document.querySelector('#frame1')");
    ElementHandle elementHandle = jsHandle.asElement();
    assertNotNull(elementHandle);
    assertEquals(frame, elementHandle.contentFrame());
  }

  @Test
  void shouldReturnNullForNonIframes() {
    page.navigate(server.EMPTY_PAGE);
    attachFrame(page, "frame1", server.EMPTY_PAGE);
    Frame frame = page.frames().get(1);
    JSHandle jsHandle = frame.evaluateHandle("() => document.body");
    ElementHandle elementHandle = jsHandle.asElement();
    assertNotNull(elementHandle);
    assertNull(elementHandle.contentFrame());
  }

  @Test
  void shouldReturnNullForDocumentDocumentElement() {
    page.navigate(server.EMPTY_PAGE);
    attachFrame(page, "frame1", server.EMPTY_PAGE);
    Frame frame = page.frames().get(1);
    JSHandle jsHandle = frame.evaluateHandle("() => document.documentElement");
    ElementHandle elementHandle = jsHandle.asElement();
    assertNotNull(elementHandle);
    assertNull(elementHandle.contentFrame());
  }
}
