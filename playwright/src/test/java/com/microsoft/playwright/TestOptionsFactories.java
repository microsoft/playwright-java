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

import com.microsoft.playwright.junit.Options;
import com.microsoft.playwright.junit.OptionsFactory;

public class TestOptionsFactories {

  public static class BasicOptionsFactory implements OptionsFactory {
    @Override
    public Options getOptions() {
      return new Options().setBrowserName(getBrowserName());
    }
  }

  public static String getBrowserChannelFromEnv() {
    return System.getenv("BROWSER_CHANNEL");
  }

  public static BrowserType.LaunchOptions createLaunchOptions() {
    BrowserType.LaunchOptions options;
    options = new BrowserType.LaunchOptions();
    options.headless = !getHeadful();
    return options;
  }

  private static boolean getHeadful() {
    String headfulEnv = System.getenv("HEADFUL");
    return headfulEnv != null && !"0".equals(headfulEnv) && !"false".equals(headfulEnv);
  }

  private static String getBrowserName() {
    String browserName = System.getenv("BROWSER");
    if (browserName == null) {
      browserName = "chromium";
    }
    return browserName;
  }

  public static boolean isChromium() {
    return getBrowserName().equals("chromium");
  }

  public static boolean isFirefox() {
    return getBrowserName().equals("firefox");
  }

  public static boolean isWebKit() {
    return getBrowserName().equals("webkit");
  }
}
