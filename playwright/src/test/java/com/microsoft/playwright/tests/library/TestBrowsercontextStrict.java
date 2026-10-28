/**
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

package com.microsoft.playwright.tests.library;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.Fixture;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/library/browsercontext-strict.spec.ts. */
public class TestBrowsercontextStrict {
  @PlaywrightTest
  @DisplayName("SM-011 should not fail page.textContent in non-strict mode")
  void shouldNotFailPageTextContentInNonStrictMode(Page page) {
    page.setContent("<span>span1</span><div><span>target</span></div>");
    assertEquals("span1", page.textContent("span", new Page.TextContentOptions().setStrict(false)));
  }

  @Nested
  class StrictContextMode {
    @Fixture
    Browser.NewContextOptions contextOptions(Browser.NewContextOptions contextOptions) {
      return contextOptions.setStrictSelectors(true);
    }

    @PlaywrightTest
    @DisplayName("SM-012 should fail page.textContent in strict mode")
    void shouldFailPageTextContentInStrictMode(Page page) {
      page.setContent("<span>span1</span><div><span>target</span></div>");
      PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.textContent("span"));
      assertTrue(error.getMessage().contains("strict mode violation"));
    }

    @PlaywrightTest
    @DisplayName("SM-013 should fail page.click in strict mode")
    void shouldFailPageClickInStrictMode(Page page) {
      page.setContent("<button>button1</button><button>target</button>");
      PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.click("button"));
      assertTrue(error.getMessage().contains("strict mode violation"));
    }

    @PlaywrightTest
    @DisplayName("SM-014 should opt out of strict mode")
    void shouldOptOutOfStrictMode(Page page) {
      page.setContent("<span>span1</span><div><span>target</span></div>");
      assertEquals("span1", page.textContent("span", new Page.TextContentOptions().setStrict(false)));
    }
  }
}
