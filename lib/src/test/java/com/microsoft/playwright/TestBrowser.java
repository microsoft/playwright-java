/**
 * Copyright (c) Microsoft Corporation.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.playwright;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class TestBrowser {
  private static Playwright playwright;
  private Browser browser;
  private boolean isChromium;

  @BeforeAll
  static void beforeAll() {
    playwright = Playwright.create();
  }

  @BeforeEach
  void setUp() {
    BrowserType.LaunchOptions options = new BrowserType.LaunchOptions();
    browser = playwright.chromium().launch(options);
    isChromium = true;
  }

  @AfterEach
  void tearDown() {
    browser.close();
  }

  @Test
  void should_create_new_page() {
    Page page1 = browser.newPage();
    assertEquals(1, browser.contexts().size());

    Page page2 = browser.newPage();
    assertEquals(2, browser.contexts().size());

    page1.close();
    assertEquals(1, browser.contexts().size());

    page2.close();
    assertEquals(0, browser.contexts().size());
  }

  @Test
  void should_throw_upon_second_create_new_page() {
    Page page = browser.newPage();
    try {
      page.context().newPage();
      fail("newPage should throw");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("Please use browser.newContext()"));
    }
    page.close();
  }


  @Test
  void version_should_work() {
    if (isChromium)
      assertTrue(Pattern.matches("^\\d+\\.\\d+\\.\\d+\\.\\d+$", browser.version()));
    else
      assertTrue(Pattern.matches("^\\d+\\.\\d+", browser.version()));
  }
}
