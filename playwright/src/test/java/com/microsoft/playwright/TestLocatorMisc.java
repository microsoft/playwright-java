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

import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class TestLocatorMisc extends TestBase{
  @Test
  void shouldCheckTheBoxUsingSetChecked() {
    page.setContent("<input id='checkbox' type='checkbox'></input>");
    Locator input = page.locator("input");
    input.setChecked(true);
    assertEquals(true, page.evaluate("checkbox.checked"));
    input.setChecked(false);
    assertEquals(false, page.evaluate("checkbox.checked"));
  }

  @Test
  void shouldWaitFor() {
    page.setContent("<div></div>");
    Locator locator = page.locator("span");
    page.evalOnSelector("div", "div => setTimeout(() => div.innerHTML = '<span>target</span>', 500)");
    locator.waitFor();
    assertTrue(locator.textContent().contains("target"));
  }

  @Test
  void shouldWaitForHidden() {
    page.setContent("<div><span>target</span></div>");
    Locator locator = page.locator("span");
    page.evalOnSelector("div", "div => setTimeout(() => div.innerHTML = '', 500)");
    locator.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
  }

  @Test
  void locatorsHasDoesNotEncodeUnicode() {
    page.navigate(server.EMPTY_PAGE);
    Locator[] locators = new Locator[]{
      page.locator("button", new Page.LocatorOptions().setHasText("Драматург")),
      page.locator("button", new Page.LocatorOptions().setHasText(Pattern.compile("Драматург"))),
      page.locator("button", new Page.LocatorOptions().setHas(page.locator("text=Драматург")))
    };
    for (Locator locator: locators) {
      try {
        locator.click(new Locator.ClickOptions().setTimeout(100));
        fail("did not throw");
      } catch (PlaywrightException e) {
        assertTrue(e.getMessage().contains("Драматург"), e.getMessage());
      }
    }
  }
}
