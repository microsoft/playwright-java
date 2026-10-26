/**
 * Copyright 2019 Google Inc. All rights reserved.
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

package com.microsoft.playwright.tests.library.chromium;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.ConsoleMessage;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.EnabledOnBrowser;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.Fixture;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static com.microsoft.playwright.fixtures.Utils.asset;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/library/chromium/extensions.spec.ts. */
@EnabledOnBrowser(BrowserName.CHROMIUM)
public class TestExtensions {
  public static class LaunchPersistentContext {
    private final BrowserType browserType;

    LaunchPersistentContext(BrowserType browserType) {
      this.browserType = browserType;
    }

    BrowserContext launch(Path extensionPath) {
      return launch(extensionPath, Env.launchPersistentContextOptions());
    }

    BrowserContext launch(Path extensionPath, BrowserType.LaunchPersistentContextOptions options) {
      options.setArgs(Arrays.asList(
        "--disable-extensions-except=" + extensionPath,
        "--load-extension=" + extensionPath));
      return browserType.launchPersistentContext(Paths.get(""), options);
    }
  }

  @Fixture
  LaunchPersistentContext launchPersistentContext(BrowserType browserType) {
    return new LaunchPersistentContext(browserType);
  }

  @BeforeEach
  void skipHeadlessShell(BrowserName browserName) {
    assumeFalse(Env.isHeadlessShell(browserName), "Headless Shell has no support for extensions");
  }

  @Nested
  class MV3 {
    @BeforeEach
    void skipChrome() {
      assumeFalse(Env.channel() != null && Env.channel().startsWith("chrome"), "--load-extension is not supported in Chrome anymore. https://groups.google.com/a/chromium.org/g/chromium-extensions/c/1-g8EFx2BBY/m/S0ET5wPjCAAJ");
    }

    @PlaywrightTest
    @DisplayName("CR-040 should report console messages from content script")
    void shouldReportConsoleMessagesFromContentScript(LaunchPersistentContext launchPersistentContext, TestServer server) {
      // { annotation: { type: 'issue', description: 'https://github.com/microsoft/playwright/issues/32762' } }
      Path extensionPath = asset("extension-mv3-with-logging");
      BrowserContext context = launchPersistentContext.launch(extensionPath);
      Page page = context.newPage();
      ConsoleMessage message = page.waitForConsoleMessage(
        new Page.WaitForConsoleMessageOptions().setPredicate(e -> e.text().contains("Test console log from a third-party execution context")),
        () -> page.navigate(server.EMPTY_PAGE));
      assertTrue(message.text().contains("Test console log from a third-party execution context"));
      context.close();
    }
  }
}
