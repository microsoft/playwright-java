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

package com.microsoft.playwright.assertions;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.impl.APIResponseAssertionsImpl;
import com.microsoft.playwright.impl.AssertionsTimeout;
import com.microsoft.playwright.impl.LocatorAssertionsImpl;
import com.microsoft.playwright.impl.PageAssertionsImpl;

/**
 * Playwright gives you Web-First Assertions with convenience methods for creating assertions that will wait and retry
 * until the expected condition is met.
 *
 * <p> Consider the following example:
 * <pre>{@code
 * ...
 * import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
 *
 * public class TestExample {
 *   ...
 *   @Test
 *   void statusBecomesSubmitted() {
 *     ...
 *     page.locator("#submit-button").click();
 *     assertThat(page.locator(".status")).hasText("Submitted");
 *   }
 * }
 * }</pre>
 *
 * <p> Playwright will be re-testing the node with the selector {@code .status} until fetched Node has the {@code "Submitted"}
 * text. It will be re-fetching the node and checking it over and over, until the condition is met or until the timeout is
 * reached. You can pass this timeout as an option.
 *
 * <p> By default, the timeout for assertions is set to 5 seconds.
 */
public interface PlaywrightAssertions {
  /**
   * Creates a {@code APIResponseAssertions} object for the given {@code APIResponse}.
   *
   * <p> **Usage**
   * <pre>{@code
   * PlaywrightAssertions.assertThat(response).isOK();
   * }</pre>
   *
   * @param response {@code APIResponse} object to use for assertions.
   * @since v1.18
   */
  static APIResponseAssertions assertThat(APIResponse response) {
    return new APIResponseAssertionsImpl(response);
  }

  /**
   * Creates a {@code LocatorAssertions} object for the given {@code Locator}.
   *
   * <p> **Usage**
   * <pre>{@code
   * PlaywrightAssertions.assertThat(locator).isVisible();
   * }</pre>
   *
   * @param locator {@code Locator} object to use for assertions.
   * @since v1.18
   */
  static LocatorAssertions assertThat(Locator locator) {
    return new LocatorAssertionsImpl(locator);
  }

  /**
   * Creates a {@code PageAssertions} object for the given {@code Page}.
   *
   * <p> **Usage**
   * <pre>{@code
   * PlaywrightAssertions.assertThat(page).hasTitle("News");
   * }</pre>
   *
   * @param page {@code Page} object to use for assertions.
   * @since v1.18
   */
  static PageAssertions assertThat(Page page) {
    return new PageAssertionsImpl(page);
  }

  /**
   * Changes default timeout for Playwright assertions from 5 seconds to the specified value.
   *
   * <p> **Usage**
   * <pre>{@code
   * PlaywrightAssertions.setDefaultAssertionTimeout(30_000);
   * }</pre>
   *
   * @param timeout Timeout in milliseconds.
   * @since v1.25
   */
  static void setDefaultAssertionTimeout(double milliseconds) {
    AssertionsTimeout.setDefaultTimeout(milliseconds);
  }

}

