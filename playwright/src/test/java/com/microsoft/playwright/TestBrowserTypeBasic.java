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

import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import org.junit.jupiter.api.Assumptions;

import java.nio.file.Files;
import java.nio.file.Paths;

import static com.microsoft.playwright.fixtures.BrowserName.CHROMIUM;
import static com.microsoft.playwright.fixtures.BrowserName.WEBKIT;
import static org.junit.jupiter.api.Assertions.*;

public class TestBrowserTypeBasic {
  @PlaywrightTest
  void browserTypeExecutablePathShouldWork(BrowserType browserType) {
    Assumptions.assumeTrue(Env.channel() == null);
    Assumptions.assumeTrue(Env.launchOptions().executablePath == null, "Skip with custom executable path");
    String executablePath = browserType.executablePath();
    assertTrue(Files.exists(Paths.get(executablePath)));
  }

  @PlaywrightTest
  void browserTypeNameShouldWork(BrowserType browserType, BrowserName browserName) {
    assertEquals(browserName.toString(), browserType.name());
  }

  @PlaywrightTest
  @DisabledOnBrowser(value = {CHROMIUM, WEBKIT}, reason = "Connecting over CDP is supported in Chromium and WebKit")
  void shouldThrowWhenTryingToConnectWithNotChromium(BrowserType browserType) {
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> browserType.connectOverCDP("foo"));
    assertTrue(e.getMessage().contains("Connecting over CDP is only supported in Chromium and WebKit."));
  }
}
