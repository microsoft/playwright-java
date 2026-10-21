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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@FixtureTest
@UsePlaywright
public class TestFixturePlaywrightOptionsStatic {
  // Only checked as an option value, never navigated to.
  private static final String BASE_URL = "http://localhost:1234/base/";
  private static final String NESTED_BASE_URL = "http://localhost:1234/nested/";

  @PlaywrightOptions
  static final Options options = new Options().setBaseUrl(BASE_URL).setIgnoreHTTPSErrors(true);

  // A static member does not need a test instance.
  @BeforeAll
  static void beforeAll(Browser.NewContextOptions contextOptions) {
    assertEquals(BASE_URL, contextOptions.baseURL);
  }

  @Test
  void shouldUseStaticOptions(Browser.NewContextOptions contextOptions) {
    assertEquals(BASE_URL, contextOptions.baseURL);
  }

  // Runs after the nested classes, so the options of the enclosing class must be in effect again.
  @AfterAll
  static void afterAll(Browser.NewContextOptions contextOptions) {
    assertEquals(BASE_URL, contextOptions.baseURL);
  }

  @Nested
  class NestedClass {
    @Test
    void shouldUseOptionsOfEnclosingClass(Browser.NewContextOptions contextOptions) {
      assertEquals(BASE_URL, contextOptions.baseURL);
    }
  }

  @Nested
  class NestedClassWithOwnOptions {
    // Inner classes cannot declare static fields, so the options are read for each nested test instance.
    @PlaywrightOptions
    Options options = new Options().setBaseUrl(NESTED_BASE_URL);

    @Test
    void shouldOverrideOptionsOfEnclosingClass(Browser.NewContextOptions contextOptions) {
      assertEquals(NESTED_BASE_URL, contextOptions.baseURL);
    }
  }

  @Nested
  class NestedClassWithOwnContextOptions {
    @PlaywrightOptions
    Browser.NewContextOptions contextOptions = new Browser.NewContextOptions().setLocale("de-DE");

    @Test
    void shouldOverrideOnlyContextOptionsOfEnclosingClass(Browser.NewContextOptions contextOptions) {
      assertEquals("de-DE", contextOptions.locale);
      // The other options of the enclosing class still apply.
      assertEquals(BASE_URL, contextOptions.baseURL);
      assertEquals(true, contextOptions.ignoreHTTPSErrors);
    }
  }
}
