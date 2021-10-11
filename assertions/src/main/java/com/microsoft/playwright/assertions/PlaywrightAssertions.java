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

import java.util.*;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.impl.PageAssertionsImpl;

/**
 * The {@code PlaywrightAssertions} class provides convenience methods for creating assertions that will wait until the expected
 * condition is met.
 *
 * <p> Consider the following example:
 * <pre>{@code
 * assertThat(page.locator('.status')).hasText('Submitted');
 * }</pre>
 *
 * <p> Playwright will be re-testing the node with the selector {@code .status} until fetched Node has the {@code "Submitted"} text. It
 * will be re-fetching the node and checking it over and over, until the condition is met or until the timeout is reached.
 * You can pass this timeout as an option.
 *
 * <p> By default, the timeout for assertions is set to 5 seconds.
 */
public interface PlaywrightAssertions {
  /**
   * Creates a {@code PageAssertions} object for the given {@code Page}.
   * <pre>{@code
   * PlaywrightAssertions.assertThat(page).hasTitle("News");
   * }</pre>
   *
   * @param page {@code Page} object to use for assertions.
   */
  static PageAssertions assertThat(Page page) {
    return new PageAssertionsImpl(page);
  }

}

