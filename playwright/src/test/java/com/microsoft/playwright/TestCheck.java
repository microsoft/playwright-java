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
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestCheck extends TestBase {

  @Test
  void shouldCheckTheBox() {
    page.setContent("<input id='checkbox' type='checkbox'></input>");
    page.check("input");
    assertTrue((Boolean) page.evaluate("() => window['checkbox'].checked"));
  }

  @Test
  void shouldNotCheckTheCheckedBox() {
    page.setContent("<input id='checkbox' type='checkbox' checked></input>");
    page.check("input");
    assertTrue((Boolean) page.evaluate("() => window['checkbox'].checked"));
  }

  @Test
  void shouldUncheckTheBox() {
    page.setContent("<input id='checkbox' type='checkbox' checked></input>");
    page.uncheck("input");
    assertEquals(false, page.evaluate("() => window['checkbox'].checked"));
  }

  @Test
  void shouldNotUncheckTheUncheckedBox() {
    page.setContent("<input id='checkbox' type='checkbox'></input>");
    page.uncheck("input");
    assertEquals(false, page.evaluate("() => window['checkbox'].checked"));
  }

  @Test
  void shouldCheckTheBoxByLabel() {
    page.setContent("<label for='checkbox'><input id='checkbox' type='checkbox'></input></label>");
    page.check("label");
    assertTrue((Boolean) page.evaluate("() => window['checkbox'].checked"));
  }

  @Test
  void shouldCheckTheBoxOutsideLabel() {
    page.setContent("<label for='checkbox'>Text</label><div><input id='checkbox' type='checkbox'></input></div>");
    page.check("label");
    assertTrue((Boolean) page.evaluate("() => window['checkbox'].checked"));
  }

  @Test
  void shouldCheckTheBoxInsideLabelWOId() {
    page.setContent("<label>Text<span><input id='checkbox' type='checkbox'></input></span></label>");
    page.check("label");
    assertTrue((Boolean) page.evaluate("() => window['checkbox'].checked"));
  }

  @Test
  void shouldCheckRadio() {
    page.setContent("<input type='radio'>one</input>\n" +
      "<input id='two' type='radio'>two</input>\n" +
      "<input type='radio'>three</input>");
    page.check("#two");
    assertTrue((Boolean) page.evaluate("() => window['two'].checked"));
  }

  @Test
  void shouldCheckTheBoxByAriaRole() {
    page.setContent("<div role='checkbox' id='checkbox'>CHECKBOX</div>\n" +
      " <script>\n" +
      "  checkbox.addEventListener('click', () => checkbox.setAttribute('aria-checked', 'true'));\n" +
      "</script>");
    page.check("div");
    assertEquals("true", page.evaluate("() => window['checkbox'].getAttribute('aria-checked')"));
  }
}
