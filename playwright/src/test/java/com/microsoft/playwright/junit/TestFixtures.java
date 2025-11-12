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

import com.microsoft.playwright.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@UsePlaywright
public class TestFixtures {
  private static Playwright playwrightFromBeforeAll;
  private static Browser browserFromBeforeAll;
  private BrowserContext browserContextFromBeforeEach;
  private Page pageFromBeforeEach;
  private static APIRequestContext apiRequestContextFromBeforeAll;
  private APIRequestContext apiRequestContextFromBeforeEach;
  private static Browser.NewContextOptions newBrowserContextOptionsFromBeforeAll;

  @BeforeAll
  public static void beforeAll(Playwright playwright, Browser browser, APIRequestContext apiRequestContext, Browser.NewContextOptions newBrowserContextOptions) {
    assertNotNull(playwright);
    assertNotNull(browser);
    assertNotNull(apiRequestContext);
    assertNotNull(newBrowserContextOptions);

    playwrightFromBeforeAll = playwright;
    browserFromBeforeAll = browser;
    apiRequestContextFromBeforeAll = apiRequestContext;
    newBrowserContextOptionsFromBeforeAll = newBrowserContextOptions;
  }

  @BeforeEach
  public void beforeEach(Playwright playwright, Browser browser, BrowserContext browserContext, Page page, APIRequestContext apiRequestContext, Browser.NewContextOptions newBrowserContextOptions) {
    assertEquals(playwrightFromBeforeAll, playwright);
    assertEquals(browserFromBeforeAll, browser);
    assertEquals(newBrowserContextOptionsFromBeforeAll, newBrowserContextOptions);
    assertNotEquals(apiRequestContextFromBeforeAll, apiRequestContext);

    assertNotNull(browserContext);
    assertNotNull(page);
    browserContextFromBeforeEach = browserContext;
    pageFromBeforeEach = page;
    apiRequestContextFromBeforeEach = apiRequestContext;
  }

  @Test
  public void objectShouldBeSameAsBeforeAll(Playwright playwright, Browser browser, Browser.NewContextOptions browserNextContextOptions) {
    assertEquals(playwrightFromBeforeAll, playwright);
    assertEquals(browserFromBeforeAll, browser);
    assertEquals(newBrowserContextOptionsFromBeforeAll, browserNextContextOptions);
  }

  @Test
  public void objectShouldBeSameAsBeforeEach(BrowserContext browserContext, Page page, APIRequestContext apiRequestContext) {
    assertEquals(browserContextFromBeforeEach, browserContext);
    assertEquals(pageFromBeforeEach, page);
    assertEquals(apiRequestContextFromBeforeEach, apiRequestContext);
  }
}
