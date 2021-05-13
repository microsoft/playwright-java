/*
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

import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class TestBrowser extends TestBase {
  @Override
  void createContextAndPage() {
    // Do not create anything.
  }

  @Test
  void shouldCreateNewPage() {
    Page page1 = getBrowser().newPage();
    assertEquals(1, getBrowser().contexts().size());

    Page page2 = getBrowser().newPage();
    assertEquals(2, getBrowser().contexts().size());

    page1.close();
    assertEquals(1, getBrowser().contexts().size());

    page2.close();
    assertEquals(0, getBrowser().contexts().size());
  }

  @Test
  void shouldThrowUponSecondCreateNewPage() {
    Page page = getBrowser().newPage();
    try {
      page.context().newPage();
      fail("newPage should throw");
    } catch (PlaywrightException e) {
      assertTrue(e.getMessage().contains("Please use browser.newContext()"));
    }
    page.close();
  }

  @Test
  void versionShouldWork() {
    if (isChromium()) {
      assertTrue(Pattern.matches("^\\d+\\.\\d+\\.\\d+\\.\\d+$", getBrowser().version()));
    } else if (isWebKit()) {
      assertTrue(Pattern.matches("^\\d+\\.\\d+", getBrowser().version()));
    } else if (isFirefox()) {
      // It can be 85.0b1 in Firefox.
      assertTrue(Pattern.matches("^\\d+\\.\\d+.*", getBrowser().version()));
    }
  }
}
