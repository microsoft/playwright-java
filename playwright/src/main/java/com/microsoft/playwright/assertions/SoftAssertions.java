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

import com.microsoft.playwright.impl.SoftAssertionsImpl;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * The {@code SoftAssertions} class provides assertion methods that can be used to make multiple assertions without failing
 * the test immediately.
 * <pre>{@code
 * ...
 * import com.microsoft.playwright.assertions.SoftAssertions;
 *
 * public class TestPage {
 *   ...
 *   @Test
 *   void hasUrlTextPass() {
 *     SoftAssertions softly = SoftAssertions.create();
 *     page.getByText("Sign in").click();
 *     softly.assertThat(page).hasURL(Pattern.compile(".*\/login"));
 *     softly.assertAll();
 *   }
 * }
 * }</pre>
 */
public interface SoftAssertions {
  /**
   * Creates a {@code SoftAssertions} object.
   *
   * <p> **Usage**
   * <pre>{@code
   * SoftAssertions softly = SoftAssertions.create();
   * }</pre>
   *
   * @since v1.38
   */
  static SoftAssertions create() {
    return new SoftAssertionsImpl();
  }
  /**
   * Creates a {@code LocatorAssertions} object for the given {@code Locator}.
   *
   * <p> **Usage**
   * <pre>{@code
   * SoftAssertions softly = SoftAssertions.create();
   * ...
   * softly.assertThat(locator).isVisible();
   * }</pre>
   *
   * @param locator {@code Locator} object to use for assertions.
   * @since v1.38
   */
  LocatorAssertions assertThat(Locator locator);
  /**
   * Creates a {@code PageAssertions} object for the given {@code Page}.
   *
   * <p> **Usage**
   * <pre>{@code
   * SoftAssertions softly = SoftAssertions.create();
   * ...
   * softly.assertThat(page).hasTitle("News");
   * }</pre>
   *
   * @param page {@code Page} object to use for assertions.
   * @since v1.38
   */
  PageAssertions assertThat(Page page);
  /**
   * Creates a {@code APIResponseAssertions} object for the given {@code APIResponse}.
   *
   * <p> **Usage**
   * <pre>{@code
   * SoftAssertions softly = SoftAssertions.create();
   * ...
   * softly.assertThat(response).isOK();
   * }</pre>
   *
   * @param response {@code APIResponse} object to use for assertions.
   * @since v1.38
   */
  APIResponseAssertions assertThat(APIResponse response);
  /**
   * Runs all the assertions have been executed for this {@code SoftAssertions} object.  If any assertions fail, this method
   * throws an AssertionFailedError with the details of all the failed assertions.
   *
   * <p> **Usage**
   * <pre>{@code
   * SoftAssertions softly = SoftAssertions.create();
   * ...
   * softly.assertAll();
   * }</pre>
   *
   * @since v1.38
   */
  void assertAll();
}

