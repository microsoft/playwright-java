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
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import org.junit.jupiter.api.DisplayName;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/** Port of tests/library/launcher.spec.ts. */
public class TestLauncher {
  @PlaywrightTest
  @DisplayName("BT-021 should throw a friendly error if its headed and there is no xserver on linux running")
  void shouldThrowAFriendlyErrorIfItsHeadedAndThereIsNoXserverOnLinuxRunning(BrowserType browserType) {
    assumeTrue(Env.platform().equals("linux"));
    assumeFalse("chromium-headless-shell".equals(Env.channel()), "shell is never headed");

    // Java: inheritAndCleanEnv({ DISPLAY: undefined }) is the environment of this process without DISPLAY.
    Map<String, String> env = new HashMap<>(System.getenv());
    env.remove("DISPLAY");
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> browserType.launch(Env.launchOptions().setHeadless(false).setEnv(env)));
    assertTrue(Pattern.compile("Looks like you launched a headed browser without having a XServer running.").matcher(error.getMessage()).find());
    assertTrue(Pattern.compile("xvfb-run").matcher(error.getMessage()).find());
  }
}
