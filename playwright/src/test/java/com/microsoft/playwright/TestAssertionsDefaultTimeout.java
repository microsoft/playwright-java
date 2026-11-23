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

import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import org.junit.jupiter.api.parallel.Isolated;
import org.opentest4j.AssertionFailedError;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

// The default assertion timeout is global, so these tests do not run in parallel with any other test.
@Isolated
public class TestAssertionsDefaultTimeout {
  @PlaywrightTest
  void defaultTimeoutHasTextFail(Page page) {
    page.setContent("<div></div>");
    Locator locator = page.locator("div");
    PlaywrightAssertions.setDefaultAssertionTimeout(1000);
    AssertionFailedError exception = assertThrows(AssertionFailedError.class, () -> assertThat(locator).hasText("foo"));
    assertTrue(exception.getMessage().contains("Assert \"hasText\" locator(\"div\") with timeout 1000ms"), exception.getMessage());
    // Restore default.
    PlaywrightAssertions.setDefaultAssertionTimeout(5_000);
  }

  @PlaywrightTest
  void defaultTimeoutHasTextPass(Page page) {
    page.setContent("<div>foo</div>");
    Locator locator = page.locator("div");
    PlaywrightAssertions.setDefaultAssertionTimeout(1000);
    assertThat(locator).hasText("foo");
    // Restore default.
    PlaywrightAssertions.setDefaultAssertionTimeout(5_000);
  }

  @PlaywrightTest
  void defaultTimeoutZeroHasTextPass(Page page) {
    page.setContent("<div>foo</div>");
    Locator locator = page.locator("div");
    PlaywrightAssertions.setDefaultAssertionTimeout(0);
    assertThat(locator).hasText("foo");
    // Restore default.
    PlaywrightAssertions.setDefaultAssertionTimeout(5_000);
  }
}
