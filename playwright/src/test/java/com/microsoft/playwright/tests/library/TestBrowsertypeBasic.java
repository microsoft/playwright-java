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

import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.EnabledOnBrowser;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import org.junit.jupiter.api.DisplayName;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/library/browsertype-basic.spec.ts. */
public class TestBrowsertypeBasic {
  @PlaywrightTest
  @DisplayName("BT-001 browserType.executablePath should work")
  void browserTypeExecutablePathShouldWork(BrowserType browserType, BrowserType.LaunchOptions launchOptions) {
    assumeFalse(Env.channel() != null, "We skip browser download when testing a channel");
    assumeFalse(launchOptions.executablePath != null, "Skip with custom executable path");

    String executablePath = browserType.executablePath();
    assertEquals(true, Files.exists(Paths.get(executablePath)));
  }

  @PlaywrightTest
  @DisplayName("BT-002 browserType.name should work")
  void browserTypeNameShouldWork(BrowserType browserType, BrowserName browserName) {
    assertEquals(browserName.toString(), browserType.name());
  }

  @PlaywrightTest
  @DisplayName("BT-003 should throw when trying to connect with not-chromium")
  @EnabledOnBrowser(BrowserName.FIREFOX)
  void shouldThrowWhenTryingToConnectWithNotChromium(BrowserType browserType) {
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> browserType.connectOverCDP("ws://foo"));
    assertEquals("Connecting over CDP is only supported in Chromium and WebKit.", error.getMessage());
  }
}
