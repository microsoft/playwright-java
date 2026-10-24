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

import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.options.BrowserChannel;
import org.junit.jupiter.api.Assumptions;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestBrowser2 {

  private static BrowserChannel getBrowserChannelEnumFromEnv() {
    String channel = Env.channel();
    if (channel == null) {
      return null;
    }
    switch (channel) {
      case "chrome":
        return BrowserChannel.CHROME;
      case "chrome-beta":
        return BrowserChannel.CHROME_BETA;
      case "chrome-dev":
        return BrowserChannel.CHROME_DEV;
      case "chrome-canary":
        return BrowserChannel.CHROME_CANARY;
      case "msedge":
        return BrowserChannel.MSEDGE;
      case "msedge-beta":
        return BrowserChannel.MSEDGE_BETA;
      case "msedge-dev":
        return BrowserChannel.MSEDGE_DEV;
      case "msedge-canary":
        return BrowserChannel.MSEDGE_CANARY;
      default:
        throw new IllegalArgumentException("Unknown BROWSER_CHANNEL " + channel);
    }
  }

  @PlaywrightTest
  void shouldSupportDeprecatedChannelEnum(BrowserType browserType) {
    BrowserChannel channel = getBrowserChannelEnumFromEnv();
    Assumptions.assumeTrue(channel != null);
    BrowserType.LaunchOptions options = Env.launchOptions();
    options.setChannel(channel);
    Browser browser = browserType.launch(options);
    assertNotNull(browser);
    browser.close();
  }
}
