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

package com.microsoft.playwright.junit;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static com.microsoft.playwright.junit.ServerLifecycle.serverMap;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@FixtureTest
@UsePlaywright(TestFixtureOptions.CustomOptions.class)
public class TestFixtureOptions {

  public static class CustomOptions implements OptionsFactory {
    @Override
    public Options getOptions() {
      return new Options()
        .setBaseUrl(serverMap.get(TestFixtureOptions.class).EMPTY_PAGE)
        .setBrowserName("webkit")
        .setTestIdAttribute("data-my-custom-testid");
    }
  }

  @Test
  public void testCustomBrowser(Browser browser) {
    assertEquals(browser.browserType().name(), "webkit");
  }

  @Test
  public void testBaseUrl(Page page) {
    page.navigate("/");
    assertThat(page).hasURL(Pattern.compile("localhost"));
  }

  @Test
  void testCustomTestId(Page page) {
    page.setContent("<div><div data-my-custom-testid='Hello'>Hello world</div></div>");
    assertThat(page.getByTestId("Hello")).hasText("Hello world");
    assertThat(page.mainFrame().getByTestId("Hello")).hasText("Hello world");
    assertThat(page.locator("div").getByTestId("Hello")).hasText("Hello world");
  }
}
