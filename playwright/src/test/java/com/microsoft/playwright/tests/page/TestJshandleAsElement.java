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
import com.microsoft.playwright.JSHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/** Port of tests/page/jshandle-as-element.spec.ts. */
public class TestJshandleAsElement {
  @PlaywrightTest
  @DisplayName("JH-001 should work @smoke")
  void shouldWork(Page page) {
    JSHandle aHandle = page.evaluateHandle("() => document.body");
    ElementHandle element = aHandle.asElement();
    assertNotNull(element);
  }

  @PlaywrightTest
  @DisplayName("JH-002 should return null for non-elements")
  void shouldReturnNullForNonElements(Page page) {
    JSHandle aHandle = page.evaluateHandle("() => 2");
    ElementHandle element = aHandle.asElement();
    assertNull(element);
  }

  @PlaywrightTest
  @DisplayName("JH-003 should return ElementHandle for TextNodes")
  void shouldReturnElementHandleForTextNodes(Page page) {
    page.setContent("<div>ee!</div>");
    JSHandle aHandle = page.evaluateHandle("() => document.querySelector('div').firstChild");
    ElementHandle element = aHandle.asElement();
    assertNotNull(element);
    assertEquals(true, page.evaluate("e => e.nodeType === Node.TEXT_NODE", element));
  }

  @PlaywrightTest
  @DisplayName("JH-004 should work with nullified Node")
  void shouldWorkWithNullifiedNode(Page page) {
    page.setContent("<section>test</section>");
    page.evaluate("delete Node");
    JSHandle handle = page.evaluateHandle("() => document.querySelector('section')");
    ElementHandle element = handle.asElement();
    assertNotNull(element);
  }
}
