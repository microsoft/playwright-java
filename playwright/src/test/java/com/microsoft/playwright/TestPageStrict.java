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

import com.microsoft.playwright.fixtures.PlaywrightTest;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class TestPageStrict {
  @PlaywrightTest
  void shouldFailPageTextContentInStrictMode(Page page) {
    page.setContent("<span>span1</span><div><span>target</span></div>");
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.textContent("span", new Page.TextContentOptions().setStrict(true));
    });
    assertTrue(e.getMessage().contains("strict mode violation"));
  }

  @PlaywrightTest
  void shouldFailPageGetAttributeInStrictMode(Page page) {
    page.setContent("<span>span1</span><div><span>target</span></div>");
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.getAttribute("span", "id", new Page.GetAttributeOptions().setStrict(true));
    });
    assertTrue(e.getMessage().contains("strict mode violation"));
  }

  @PlaywrightTest
  void shouldFailPageFillInStrictMode(Page page) {
    page.setContent("<input></input><div><input></input></div>");
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.fill("input", "text", new Page.FillOptions().setStrict(true));
    });
    assertTrue(e.getMessage().contains("strict mode violation"));
  }


  @PlaywrightTest
  void shouldFailPageInStrictMode(Page page) {
    page.setContent("<span>span1</span><div><span>target</span></div>");
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.querySelector("span", new Page.QuerySelectorOptions().setStrict(true));
    });
    assertTrue(e.getMessage().contains("strict mode violation"));
  }

  @PlaywrightTest
  void shouldFailPageWaitForSelectorInStrictMode(Page page) {
    page.setContent("<span>span1</span><div><span>target</span></div>");
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.waitForSelector("span", new Page.WaitForSelectorOptions().setStrict(true));
    });
    assertTrue(e.getMessage().contains("strict mode violation"));
  }

  @PlaywrightTest
  void shouldFailPageDispatchEventInStrictMode(Page page) {
    page.setContent("<span></span><div><span></span></div>");
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.dispatchEvent("span", "click", new HashMap<>(), new Page.DispatchEventOptions().setStrict(true));
    });
    assertTrue(e.getMessage().contains("strict mode violation"));
  }
}
