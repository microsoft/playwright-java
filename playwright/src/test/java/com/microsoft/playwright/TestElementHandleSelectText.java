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

import static org.junit.jupiter.api.Assertions.*;

public class TestElementHandleSelectText extends TestBase {

  @Test
  void shouldSelectTextarea() {
    page.navigate(server.PREFIX + "/input/textarea.html");
    ElementHandle textarea = page.querySelector("textarea");
    textarea.evaluate("textarea => textarea.value = 'some value'");
    textarea.selectText();
    if (isFirefox()) {
      assertEquals(0, textarea.evaluate("el => el.selectionStart"));
      assertEquals(10, textarea.evaluate("el => el.selectionEnd"));
    } else {
      assertEquals("some value", page.evaluate("() => window.getSelection().toString()"));
    }
  }

  @Test
  void shouldSelectInput() {
    page.navigate(server.PREFIX + "/input/textarea.html");
    ElementHandle input = page.querySelector("input");
    input.evaluate("input => input.value = 'some value'");
    input.selectText();
    if (isFirefox()) {
      assertEquals(0, input.evaluate("el => el.selectionStart"));
      assertEquals(10, input.evaluate("el => el.selectionEnd"));
    } else {
      assertEquals("some value", page.evaluate("() => window.getSelection().toString()"));
    }
  }

  @Test
  void shouldSelectPlainDiv() {
    page.navigate(server.PREFIX + "/input/textarea.html");
    ElementHandle div = page.querySelector("div.plain");
    div.selectText();
    assertEquals("Plain div", page.evaluate("() => window.getSelection().toString()"));
  }

  @Test
  void shouldTimeoutWaitingForInvisibleElement() {
    page.navigate(server.PREFIX + "/input/textarea.html");
    ElementHandle textarea = page.querySelector("textarea");
    textarea.evaluate("e => e.style.display = 'none'");
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      textarea.selectText(new ElementHandle.SelectTextOptions().setTimeout(3000));
    });
    assertTrue(e.getMessage().contains("element is not visible"));
  }

//  @Test
  void shouldWaitForVisible() {
    // TODO Wait for Async API implementation
  }
}
