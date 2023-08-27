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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.microsoft.playwright.Browser.NewContextOptions;
import com.microsoft.playwright.junit.PageTest;
import static com.microsoft.playwright.Utils.getBrowserNameFromEnv;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.microsoft.playwright.options.ColorScheme.DARK;

class TestPageBaseClass extends PageTest {

  @Override
  public NewContextOptions contextOptions() {
    return new Browser.NewContextOptions().setColorScheme(DARK);
  }

  @Test
  void shouldWork() {
    page.navigate("data:text/html,<div>A</div>");
    assertThat(page).hasURL("data:text/html,<div>A</div>");
    Locator div = page.locator("div");
    assertThat(div).containsText("A");
  }

  @Test
  void browserNameShouldWork() {
    assertEquals(getBrowserNameFromEnv(), browserName);
  }

  @Test
  void shouldOverrideContextOptions() {
    assertEquals(false, page.evaluate("matchMedia('(prefers-color-scheme: light)').matches"));
    assertEquals(true, page.evaluate("matchMedia('(prefers-color-scheme: dark)').matches"));
  }

}
