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

import java.util.regex.Pattern;

/**
 * The {@code PageAssertions} class provides assertion methods that can be used to make assertions about the {@code Page}
 * state in the tests.
 * <pre>{@code
 * ...
 * import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
 *
 * public class TestPage {
 *   ...
 *   @Test
 *   void navigatesToLoginPage() {
 *     ...
 *     page.getByText("Sign in").click();
 *     assertThat(page).hasURL(Pattern.compile(".*\/login"));
 *   }
 * }
 * }</pre>
 */
public interface PageAssertions {
  class HasTitleOptions {
    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public Double timeout;

    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public HasTitleOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class HasURLOptions {
    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public Double timeout;

    /**
     * Time to retry the assertion for in milliseconds. Defaults to {@code 5000}.
     */
    public HasURLOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  /**
   * Makes the assertion check for the opposite condition. For example, this code tests that the page URL doesn't contain
   * {@code "error"}:
   * <pre>{@code
   * assertThat(page).not().hasURL("error");
   * }</pre>
   *
   * @since v1.20
   */
  PageAssertions not();
  /**
   * Ensures the page has the given title.
   *
   * <p> **Usage**
   * <pre>{@code
   * assertThat(page).hasTitle("Playwright");
   * }</pre>
   *
   * @param titleOrRegExp Expected title or RegExp.
   * @since v1.20
   */
  default void hasTitle(String titleOrRegExp) {
    hasTitle(titleOrRegExp, null);
  }
  /**
   * Ensures the page has the given title.
   *
   * <p> **Usage**
   * <pre>{@code
   * assertThat(page).hasTitle("Playwright");
   * }</pre>
   *
   * @param titleOrRegExp Expected title or RegExp.
   * @since v1.20
   */
  void hasTitle(String titleOrRegExp, HasTitleOptions options);
  /**
   * Ensures the page has the given title.
   *
   * <p> **Usage**
   * <pre>{@code
   * assertThat(page).hasTitle("Playwright");
   * }</pre>
   *
   * @param titleOrRegExp Expected title or RegExp.
   * @since v1.20
   */
  default void hasTitle(Pattern titleOrRegExp) {
    hasTitle(titleOrRegExp, null);
  }
  /**
   * Ensures the page has the given title.
   *
   * <p> **Usage**
   * <pre>{@code
   * assertThat(page).hasTitle("Playwright");
   * }</pre>
   *
   * @param titleOrRegExp Expected title or RegExp.
   * @since v1.20
   */
  void hasTitle(Pattern titleOrRegExp, HasTitleOptions options);
  /**
   * Ensures the page is navigated to the given URL.
   *
   * <p> **Usage**
   * <pre>{@code
   * assertThat(page).hasURL(".com");
   * }</pre>
   *
   * @param urlOrRegExp Expected URL string or RegExp.
   * @since v1.20
   */
  default void hasURL(String urlOrRegExp) {
    hasURL(urlOrRegExp, null);
  }
  /**
   * Ensures the page is navigated to the given URL.
   *
   * <p> **Usage**
   * <pre>{@code
   * assertThat(page).hasURL(".com");
   * }</pre>
   *
   * @param urlOrRegExp Expected URL string or RegExp.
   * @since v1.20
   */
  void hasURL(String urlOrRegExp, HasURLOptions options);
  /**
   * Ensures the page is navigated to the given URL.
   *
   * <p> **Usage**
   * <pre>{@code
   * assertThat(page).hasURL(".com");
   * }</pre>
   *
   * @param urlOrRegExp Expected URL string or RegExp.
   * @since v1.20
   */
  default void hasURL(Pattern urlOrRegExp) {
    hasURL(urlOrRegExp, null);
  }
  /**
   * Ensures the page is navigated to the given URL.
   *
   * <p> **Usage**
   * <pre>{@code
   * assertThat(page).hasURL(".com");
   * }</pre>
   *
   * @param urlOrRegExp Expected URL string or RegExp.
   * @since v1.20
   */
  void hasURL(Pattern urlOrRegExp, HasURLOptions options);
}

