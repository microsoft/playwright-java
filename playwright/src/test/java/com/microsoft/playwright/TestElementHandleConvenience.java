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

public class TestElementHandleConvenience extends TestBase {
  @Test
  void isVisibleAndIsHiddenShouldWork() {
    page.setContent("<div>Hi</div><span></span>");
    ElementHandle div = page.querySelector("div");
    assertTrue(div.isVisible());
    assertFalse(div.isHidden());
    assertTrue(page.isVisible("div"));
    assertFalse(page.isHidden("div"));
    ElementHandle span = page.querySelector("span");
    assertFalse(span.isVisible());
    assertTrue(span.isHidden());
    assertFalse(page.isVisible("span"));
    assertTrue(page.isHidden("span"));
  }

  @Test
  void isEnabledAndIsDisabledShouldWork() {
    page.setContent(" <button disabled>button1</button>\n" +
      "<button>button2</button>\n" +
      "<div>div</div>");
    ElementHandle div = page.querySelector("div");
    assertTrue(div.isEnabled());
    assertFalse(div.isDisabled());
    assertTrue(page.isEnabled("div"));
    assertFalse(page.isDisabled("div"));
    ElementHandle button1 = page.querySelector(":text('button1')");
    assertFalse(button1.isEnabled());
    assertTrue(button1.isDisabled());
    assertFalse(page.isEnabled(":text('button1')"));
    assertTrue(page.isDisabled(":text('button1')"));
    ElementHandle button2 = page.querySelector(":text('button2')");
    assertTrue(button2.isEnabled());
    assertFalse(button2.isDisabled());
    assertTrue(page.isEnabled(":text('button2')"));
    assertFalse(page.isDisabled(":text('button2')"));
  }

  @Test
  void isEditableShouldWork() {
    page.setContent("<input id=input1 disabled><textarea></textarea><input id=input2>");
    page.evalOnSelector("textarea", "t => t.readOnly = true");
    ElementHandle input1 = page.querySelector("#input1");
    assertFalse(input1.isEditable());
    assertFalse(page.isEditable("#input1"));
    ElementHandle input2 = page.querySelector("#input2");
    assertTrue(input2.isEditable());
    assertTrue(page.isEditable("#input2"));
    ElementHandle textarea = page.querySelector("textarea");
    assertFalse(textarea.isEditable());
    assertFalse(page.isEditable("textarea"));
  }

  @Test
  void isCheckedShouldWork() {
    page.setContent("<input type='checkbox' checked><div>Not a checkbox</div>");
    ElementHandle handle = page.querySelector("input");
    assertTrue(handle.isChecked());
    assertTrue(page.isChecked("input"));
    handle.evaluate("input => input.checked = false");
    assertFalse(handle.isChecked());
    assertFalse(page.isChecked("input"));
    try {
      page.isChecked("div");
      fail("did not throw");
    } catch (PlaywrightException e) {
      assertTrue(e.getMessage().contains("Not a checkbox or radio button"));
    }
  }
}
