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

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestAccessibility extends TestBase {

  @Test
  void shouldWorkWithRegularText() {
    page.setContent("<div>Hello World</div>");
    AccessibilityNode snapshot = page.accessibility().snapshot();
    AccessibilityNode node = snapshot.children().get(0);
    assertEquals(isFirefox ? "text leaf" : "text", node.role());
    assertEquals("Hello World", node.name());
  }

  @Test
  void roledescription() {
    page.setContent("<div tabIndex=-1 aria-roledescription='foo'>Hi</div>");
    AccessibilityNode snapshot = page.accessibility().snapshot();
    assertEquals("foo", snapshot.children().get(0).roledescription());
  }

  @Test
  void orientation() {
    page.setContent("<a href='' role='slider' aria-orientation='vertical'>11</a>");
    AccessibilityNode snapshot = page.accessibility().snapshot();
    assertEquals("vertical", snapshot.children().get(0).orientation());
  }

  @Test
  void autocomplete() {
    page.setContent("<div role='textbox' aria-autocomplete='list'>hi</div>");
    AccessibilityNode snapshot = page.accessibility().snapshot();
    assertEquals("list", snapshot.children().get(0).autocomplete());
  }

  @Test
  void multiselectable() {
    page.setContent("<div role='grid' tabIndex=-1 aria-multiselectable=true>hey</div>");
    AccessibilityNode snapshot = page.accessibility().snapshot();
    assertEquals(true, snapshot.children().get(0).multiselectable());
  }

  @Test
  void keyshortcuts() {
    page.setContent("<div role='grid' tabIndex=-1 aria-keyshortcuts='foo'>hey</div>");
    AccessibilityNode snapshot = page.accessibility().snapshot();
    assertEquals("foo", snapshot.children().get(0).keyshortcuts());
  }
}
