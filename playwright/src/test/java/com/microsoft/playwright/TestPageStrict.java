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

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class TestPageStrict extends TestBase {
  @Test
  void shouldFailPageTextContentInStrictMode() {
    page.setContent("<span>span1</span><div><span>target</span></div>");
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.textContent("span", new Page.TextContentOptions().setStrict(true));
    });
    assertTrue(e.getMessage().contains("strict mode violation"));
  }

  @Test
  void shouldFailPageGetAttributeInStrictMode() {
    page.setContent("<span>span1</span><div><span>target</span></div>");
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.getAttribute("span", "id", new Page.GetAttributeOptions().setStrict(true));
    });
    assertTrue(e.getMessage().contains("strict mode violation"));
  }

  @Test
  void shouldFailPageFillInStrictMode() {
    page.setContent("<input></input><div><input></input></div>");
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.fill("input", "text", new Page.FillOptions().setStrict(true));
    });
    assertTrue(e.getMessage().contains("strict mode violation"));
  }


  @Test
  void shouldFailPageInStrictMode() {
    page.setContent("<span>span1</span><div><span>target</span></div>");
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.querySelector("span", new Page.QuerySelectorOptions().setStrict(true));
    });
    assertTrue(e.getMessage().contains("strict mode violation"));
  }

  @Test
  void shouldFailPageWaitForSelectorInStrictMode() {
    page.setContent("<span>span1</span><div><span>target</span></div>");
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.waitForSelector("span", new Page.WaitForSelectorOptions().setStrict(true));
    });
    assertTrue(e.getMessage().contains("strict mode violation"));
  }

  @Test
  void shouldFailPageDispatchEventInStrictMode() {
    page.setContent("<span></span><div><span></span></div>");
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.dispatchEvent("span", "click", new HashMap<>(), new Page.DispatchEventOptions().setStrict(true));
    });
    assertTrue(e.getMessage().contains("strict mode violation"));
  }
}
