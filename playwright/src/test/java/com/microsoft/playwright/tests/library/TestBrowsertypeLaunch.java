/*
 * Copyright 2017 Google Inc. All rights reserved.
 * Modifications copyright (c) Microsoft Corporation.
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
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import org.junit.jupiter.api.DisplayName;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

import static com.microsoft.playwright.fixtures.Utils.asset;
import static com.microsoft.playwright.fixtures.Utils.async;
import static com.microsoft.playwright.fixtures.Utils.cause;
import static com.microsoft.playwright.fixtures.Utils.kTargetClosedErrorMessage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/library/browsertype-launch.spec.ts. */
public class TestBrowsertypeLaunch {
  @PlaywrightTest
  @DisplayName("BT-004 should reject all promises when browser is closed")
  void shouldRejectAllPromisesWhenBrowserIsClosed(BrowserType browserType) {
    Browser browser = browserType.launch(Env.launchOptions());
    Page page = browser.newContext().newPage();
    // Java: the pending evaluate runs in async().
    CompletableFuture<Object> neverResolves = async(() -> page.evaluate("() => new Promise(r => {})"));
    page.evaluate("() => new Promise(f => window.builtins.setTimeout(f, 0))");
    browser.close();
    Throwable error = cause(neverResolves);
    // WebKit under task-set -c 1 is giving browser, rest are giving target.
    assertTrue(error.getMessage().contains(" closed"));
  }

  @PlaywrightTest
  @DisplayName("BT-006 should throw if userDataDir is passed as an argument")
  @Fixme("java: the driver words the error for Java: \"Pass userDataDir parameter to 'BrowserType.launchPersistentContext(userDataDir, options)' instead of specifying '--user-data-dir' argument\", not 'browserType.launchPersistentContext")
  void shouldThrowIfUserDataDirIsPassedAsAnArgument(BrowserType browserType) {
    PlaywrightException waitError = assertThrows(PlaywrightException.class,
      () -> browserType.launch(Env.launchOptions().setArgs(Arrays.asList("--user-data-dir=random-path", "--profile=random-path"))));
    assertTrue(waitError.getMessage().contains("Pass userDataDir parameter to 'browserType.launchPersistentContext"));
  }

  @PlaywrightTest
  @DisplayName("BT-009 should throw if page argument is passed")
  void shouldThrowIfPageArgumentIsPassed(BrowserType browserType, BrowserName browserName) {
    assumeFalse(browserName == BrowserName.FIREFOX && !Env.isBidi());

    PlaywrightException waitError = assertThrows(PlaywrightException.class,
      () -> browserType.launch(Env.launchOptions().setArgs(Arrays.asList("http://example.com"))));
    assertTrue(waitError.getMessage().contains("can not specify page"));
  }

  @PlaywrightTest
  @DisplayName("BT-010 should reject if launched browser fails immediately")
  @Fixme("java: the error message has no 'browserType.launch:' prefix: \"Target page, context or browser has been closed\\nBrowser logs:\\n\\n<launching> ...\"")
  void shouldRejectIfLaunchedBrowserFailsImmediately(BrowserType browserType) {
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> browserType.launch(Env.launchOptions().setExecutablePath(asset("dummy_bad_browser_executable.js"))));
    if ("webkit-wsl".equals(Env.channel())) {
      assertTrue(error.getMessage().contains("Failed to launch"));
    } else {
      assertTrue(Pattern.compile("browserType\\.launch(.|\\n)*(spawn UNKNOWN|spawn EFTYPE|Browser logs:)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE).matcher(error.getMessage()).find());
    }
  }

  @PlaywrightTest
  @DisplayName("BT-011 should reject if executable path is invalid")
  void shouldRejectIfExecutablePathIsInvalid(BrowserType browserType) {
    PlaywrightException waitError = assertThrows(PlaywrightException.class,
      () -> browserType.launch(Env.launchOptions().setExecutablePath(Paths.get("random-invalid-path"))));
    assertTrue(waitError.getMessage().contains("Failed to launch"));
  }

  @PlaywrightTest
  @DisplayName("BT-015 should fire close event for all contexts")
  void shouldFireCloseEventForAllContexts(BrowserType browserType) {
    Browser browser = browserType.launch(Env.launchOptions());
    BrowserContext context = browser.newContext();
    boolean[] closed = {false};
    context.onClose(c -> closed[0] = true);
    browser.close();
    assertEquals(true, closed[0]);
  }

  @PlaywrightTest
  @DisplayName("BT-016 should be callable twice")
  void shouldBeCallableTwice(BrowserType browserType) throws Exception {
    Browser browser = browserType.launch(Env.launchOptions());
    // Java: Promise.all of two independent actions, one runs in async().
    CompletableFuture<Void> close1 = async(() -> {
      browser.close();
      return null;
    });
    browser.close();
    close1.get();
    browser.close();
  }

  @PlaywrightTest
  @DisplayName("BT-017 should allow await using")
  void shouldAllowAwaitUsing(BrowserType browserType) {
    Browser b;
    BrowserContext c;
    Page p;
    // Java: `await using` is a try-with-resources block.
    try (Browser browser = browserType.launch(Env.launchOptions())) {
      b = browser;
      try (BrowserContext context = browser.newContext()) {
        c = context;
        try (Page page = context.newPage()) {
          p = page;
        }
        assertTrue(p.isClosed());
      }
      PlaywrightException error = assertThrows(PlaywrightException.class, () -> c.clearCookies());
      assertTrue(error.getMessage().contains(kTargetClosedErrorMessage));
    }
    assertFalse(b.isConnected());
  }
}
