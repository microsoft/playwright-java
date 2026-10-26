/*
 * Copyright 2020 Microsoft Corporation. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.playwright.tests.library;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

import static com.microsoft.playwright.fixtures.Utils.async;
import static com.microsoft.playwright.fixtures.Utils.cause;
import static com.microsoft.playwright.fixtures.Utils.kTargetClosedErrorMessage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/library/browser.spec.ts. */
public class TestBrowser {
  @PlaywrightTest
  @DisplayName("BR-001 should return browserType")
  void shouldReturnBrowserType(Browser browser, BrowserType browserType) {
    assertEquals(browserType, browser.browserType());
  }

  @PlaywrightTest
  @DisplayName("BR-002 should create new page @smoke")
  void shouldCreateNewPage(Browser browser) {
    Page page1 = browser.newPage();
    assertEquals(1, browser.contexts().size());

    Page page2 = browser.newPage();
    assertEquals(2, browser.contexts().size());

    page1.close();
    assertEquals(1, browser.contexts().size());

    page2.close();
    assertEquals(0, browser.contexts().size());
  }

  @PlaywrightTest
  @DisplayName("BR-003 should throw upon second create new page")
  void shouldThrowUponSecondCreateNewPage(Browser browser) {
    Page page = browser.newPage();
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.context().newPage());
    page.close();
    assertTrue(error.getMessage().contains("Please use browser.newContext()"));
  }

  @PlaywrightTest
  @DisplayName("BR-004 version should work")
  void versionShouldWork(Browser browser, BrowserName browserName) {
    String version = browser.version();
    if (browserName == BrowserName.CHROMIUM) {
      assertTrue(Pattern.compile("^\\d+\\.\\d+\\.\\d+\\.\\d+$").matcher(version).find());
    } else {
      assertTrue(Pattern.compile("^\\d+\\.\\d+").matcher(version).find());
    }
  }

  @PlaywrightTest
  @DisplayName("BR-005 should dispatch page.on(close) upon browser.close and reject evaluate")
  void shouldDispatchPageOnCloseUponBrowserCloseAndRejectEvaluate(BrowserType browserType) {
    Browser browser = browserType.launch(Env.launchOptions());
    Page page = browser.newPage();
    boolean[] closed = {false};
    page.onClose(p -> closed[0] = true);
    // Java: the pending evaluate runs in async().
    CompletableFuture<Object> promise = async(() -> page.evaluate("() => new Promise(() => {})"));
    browser.close();
    assertEquals(true, closed[0]);
    Throwable error = cause(promise);
    assertTrue(error.getMessage().contains(kTargetClosedErrorMessage));
  }

  @PlaywrightTest
  @DisplayName("BR-006 should fire context event on newContext")
  void shouldFireContextEventOnNewContext(Browser browser) {
    List<BrowserContext> events = new ArrayList<>();
    browser.onContext(ctx -> events.add(ctx));
    BrowserContext context = browser.newContext();
    assertEquals(Arrays.asList(context), events);
    context.close();
  }
}
