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

import com.microsoft.playwright.options.BrowserChannel;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;

import static com.microsoft.playwright.Utils.getBrowserNameFromEnv;

public class TestBase {
  static Server server;
  static Server httpsServer;
  static BrowserType browserType;
  static Playwright playwright;
  static Browser browser;
  static boolean isMac = Utils.getOS() == Utils.OS.MAC;
  static boolean isWindows = Utils.getOS() == Utils.OS.WINDOWS;
  static boolean headful;
  Page page;
  BrowserContext context;

  static boolean isHeadful() {
    return headful;
  }

  static boolean isChromium() {
    return "chromium".equals(getBrowserNameFromEnv());
  }

  static boolean isWebKit() {
    return "webkit".equals(getBrowserNameFromEnv());
  }

  static boolean isFirefox() {
    return "firefox".equals(getBrowserNameFromEnv());
  }

  private static BrowserChannel getBrowserChannelFromEnv() {
    String channel = System.getenv("BROWSER_CHANNEL");
    if (channel == null) {
      return null;
    }
    switch (channel) {
      case "chrome": return BrowserChannel.CHROME;
      case "chrome-beta": return BrowserChannel.CHROME_BETA;
      case "chrome-dev": return BrowserChannel.CHROME_DEV;
      case "chrome-canary": return BrowserChannel.CHROME_CANARY;
      case "msedge": return BrowserChannel.MSEDGE;
      case "msedge-beta": return BrowserChannel.MSEDGE_BETA;
      case "msedge-dev": return BrowserChannel.MSEDGE_DEV;
      case "msedge-canary": return BrowserChannel.MSEDGE_CANARY;
      default: throw new IllegalArgumentException("Unknown BROWSER_CHANNEL " + channel);
    }
  }

  static BrowserType.LaunchOptions createLaunchOptions() {
    String headfulEnv = System.getenv("HEADFUL");
    headful = headfulEnv != null && !"0".equals(headfulEnv) && !"false".equals(headfulEnv);
    BrowserType.LaunchOptions options;
    options = new BrowserType.LaunchOptions();
    options.headless = !headful;
    options.channel = getBrowserChannelFromEnv();
    return options;
  }

  static void initBrowserType() {
    playwright = Playwright.create();
    browserType = Utils.getBrowserTypeFromEnv(playwright);
  }

  static void launchBrowser(BrowserType.LaunchOptions launchOptions) {
    initBrowserType();
    browser = browserType.launch(launchOptions);
  }

  @BeforeAll
  static void launchBrowser() {
    launchBrowser(createLaunchOptions());
  }

  @AfterAll
  static void closeBrowser() {
    if (browser != null) {
      browser.close();
      browser = null;
    }
  }

  @BeforeAll
  static void startServer() throws IOException {
    server = Server.createHttp(8907);
    httpsServer = Server.createHttps(8908);
  }

  @AfterAll
  static void stopServer() throws IOException {
    if (server != null) {
      server.stop();
      server = null;
    }
    if (httpsServer != null) {
      httpsServer.stop();
      httpsServer = null;
    }
  }

  @AfterAll
  static void closePlaywright() throws Exception {
    if (playwright != null) {
      playwright.close();
      playwright = null;
    }
  }

  BrowserContext createContext() {
    return browser.newContext();
  }

  @BeforeEach
  void createContextAndPage() {
    server.reset();
    httpsServer.reset();
    context = createContext();
    page = context.newPage();
  }

  @AfterEach
  void closeContext() {
    if (context != null) {
      context.close();
      context = null;
      page = null;
    }
  }
}
