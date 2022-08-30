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

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;

import java.nio.file.Files;
import java.nio.file.Paths;

import static com.microsoft.playwright.Utils.getBrowserNameFromEnv;
import static org.junit.jupiter.api.Assertions.*;

public class TestBrowserTypeBasic extends TestBase {
  @Test
  void browserTypeExecutablePathShouldWork() {
    Assumptions.assumeTrue(getBrowserChannelFromEnv() == null);
    Assumptions.assumeTrue(createLaunchOptions().executablePath == null, "Skip with custom executable path");
    String executablePath = browserType.executablePath();
    assertTrue(Files.exists(Paths.get(executablePath)));
  }

  @Test
  void browserTypeNameShouldWork() {
    assertEquals(getBrowserNameFromEnv(), browserType.name());
  }

  @Test
  @DisabledIf(value="com.microsoft.playwright.TestBase#isChromium", disabledReason="Non-chromium behavior")
  void shouldThrowWhenTryingToConnectWithNotChromium() {
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> browserType.connectOverCDP("foo"));
    assertTrue(e.getMessage().contains("Connecting over CDP is only supported in Chromium."));
  }
}
