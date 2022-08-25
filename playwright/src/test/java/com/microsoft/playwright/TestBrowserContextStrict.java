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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestBrowserContextStrict extends TestBase {
  @Override
  BrowserContext createContext() {
    return browser.newContext(new Browser.NewContextOptions().setStrictSelectors(true));
  }

  @Test
  void shouldNotFailPageTextContentInNonStrictMode() {
    try (BrowserContext context = browser.newContext()) {
      Page page = context.newPage();
      page.setContent("<span>span1</span><div><span>target</span></div>");
      assertEquals("span1", page.textContent("span"));
    }
  }

  @Test
  void shouldFailPageTextContentInStrictMode() {
    page.setContent("<span>span1</span><div><span>target</span></div>");
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.textContent("span"));
    assertTrue(e.getMessage().contains("strict mode violation"));
  }

  @Test
  void shouldOptOutOfStrictMode() {
    page.setContent("<span>span1</span><div><span>target</span></div>");
    assertEquals("span1", page.textContent("span", new Page.TextContentOptions().setStrict(false)));
  }
}
