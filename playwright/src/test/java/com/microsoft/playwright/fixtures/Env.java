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

package com.microsoft.playwright.fixtures;

import com.microsoft.playwright.BrowserType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Test configuration taken from the environment, the counterpart of the project options and
 * platform fixtures upstream.
 */
public final class Env {
  public static final boolean isMac;
  public static final boolean isLinux;
  public static final boolean isWindows;

  static {
    String os = System.getProperty("os.name").toLowerCase();
    isWindows = os.contains("win");
    isLinux = os.contains("linux");
    isMac = os.contains("mac os x");
  }

  private Env() {
  }

  /**
   * Browsers every {@link PlaywrightTest} runs in: the comma-separated {@code BROWSER} environment
   * variable, or all three browsers when it is not set.
   */
  public static List<BrowserName> browserNames() {
    String env = System.getenv("BROWSER");
    if (env == null || env.trim().isEmpty()) {
      return Collections.unmodifiableList(Arrays.asList(BrowserName.values()));
    }
    List<BrowserName> result = new ArrayList<>();
    for (String name : env.split(",")) {
      BrowserName browserName = BrowserName.fromString(name.trim());
      if (!result.contains(browserName)) {
        result.add(browserName);
      }
    }
    return Collections.unmodifiableList(result);
  }

  /** {@code HEADED=1} launches the browsers with a window. */
  public static boolean headed() {
    String value = System.getenv("HEADED");
    return value != null && !"0".equals(value) && !"false".equals(value);
  }

  /** {@code BROWSER_CHANNEL} picks a browser distribution such as {@code chrome} or {@code msedge}. */
  public static String channel() {
    return System.getenv("BROWSER_CHANNEL");
  }

  /** Fresh launch options honoring {@link #headed()} and {@link #channel()}. */
  public static BrowserType.LaunchOptions launchOptions() {
    BrowserType.LaunchOptions options = new BrowserType.LaunchOptions();
    options.headless = !headed();
    options.channel = channel();
    return options;
  }
}
