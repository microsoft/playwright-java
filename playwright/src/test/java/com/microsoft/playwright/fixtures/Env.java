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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

  /** The {@code isBidi} fixture: the channel drives the browser over WebDriver BiDi. */
  public static boolean isBidi() {
    String channel = channel();
    return channel != null && (channel.startsWith("bidi-chrom") || channel.startsWith("moz-firefox"));
  }

  /** The {@code isHeadlessShell} fixture: Chromium runs as the headless shell rather than the full browser. */
  public static boolean isHeadlessShell(BrowserName browserName) {
    String channel = channel();
    boolean isShell = "chromium-headless-shell".equals(channel) || (channel == null && !headed());
    return browserName == BrowserName.CHROMIUM && isShell;
  }

  /** The {@code platform} fixture: {@code win32}, {@code darwin} or {@code linux}. */
  public static String platform() {
    return isWindows ? "win32" : isMac ? "darwin" : "linux";
  }

  /** The {@code macVersion} fixture: the major macOS version, 0 elsewhere. */
  public static int macVersion() {
    if (!isMac) {
      return 0;
    }
    return Integer.parseInt(System.getProperty("os.version").split("\\.")[0]);
  }

  /**
   * {@code playwright/src/test}, where the assets, config and snapshots directories live. Found
   * from the {@code playwright.test.dir} system property or by walking up from the working
   * directory, so tests run from the module, the repository root or an IDE.
   */
  public static Path srcTestDir() {
    String property = System.getProperty("playwright.test.dir");
    if (property != null) {
      return Paths.get(property).toAbsolutePath();
    }
    Path start = Paths.get("").toAbsolutePath();
    for (Path dir = start; dir != null; dir = dir.getParent()) {
      for (String candidate : new String[]{"src/test", "playwright/src/test"}) {
        Path testDir = dir.resolve(candidate);
        if (Files.isDirectory(testDir.resolve("assets"))) {
          return testDir;
        }
      }
    }
    throw new IllegalStateException("Cannot find playwright/src/test from " + start + "; set -Dplaywright.test.dir");
  }

  /** Fresh launch options honoring {@link #headed()} and {@link #channel()}. */
  public static BrowserType.LaunchOptions launchOptions() {
    BrowserType.LaunchOptions options = new BrowserType.LaunchOptions();
    options.headless = !headed();
    options.channel = channel();
    return options;
  }
}
