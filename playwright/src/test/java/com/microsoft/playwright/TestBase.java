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
import java.util.concurrent.atomic.AtomicInteger;

import static com.microsoft.playwright.Utils.getBrowserNameFromEnv;
import static com.microsoft.playwright.Utils.nextFreePort;

public class TestBase {
  private static final ThreadLocal<Server> server = new ThreadLocal<>();
  private static final ThreadLocal<Server> httpsServer = new ThreadLocal<>();
  private static final ThreadLocal<BrowserType> browserType = new ThreadLocal<>();
  private static final ThreadLocal<Playwright> playwright = new ThreadLocal<>();
  private static final ThreadLocal<Browser> browser = new ThreadLocal<>();
  static final boolean isMac = Utils.getOS() == Utils.OS.MAC;
  static final boolean isWindows = Utils.getOS() == Utils.OS.WINDOWS;
  static final boolean headful;
  static {
    String headfulEnv = System.getenv("HEADFUL");
    headful = headfulEnv != null && !"0".equals(headfulEnv) && !"false".equals(headfulEnv);
  }
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

  static BrowserChannel getBrowserChannelFromEnv() {
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
    BrowserType.LaunchOptions options;
    options = new BrowserType.LaunchOptions();
    options.headless = !headful;
    options.channel = getBrowserChannelFromEnv();
    return options;
  }

  static void initBrowserType() {
    setPlaywright(Playwright.create());
    setBrowserType(Utils.getBrowserTypeFromEnv(getPlaywright()));
  }

  static void launchBrowser(BrowserType.LaunchOptions launchOptions) {
    initBrowserType();
    setBrowser(getBrowserType().launch(launchOptions));
  }

  @BeforeAll
  static void launchBrowser() {
    launchBrowser(createLaunchOptions());
  }

  @AfterAll
  static void closeBrowser() {
    if (getBrowser() != null) {
      getBrowser().close();
      setBrowser(null);
    }
  }

  @BeforeAll
  static void startServer() throws IOException {
    setServer(Server.createHttp(nextFreePort()));
    setHttpsServer(Server.createHttps(nextFreePort()));
  }

  @AfterAll
  static void stopServer() {
    if (getServer() != null) {
      getServer().stop();
      setServer(null);
    }
    if (getHttpsServer() != null) {
      getHttpsServer().stop();
      setHttpsServer(null);
    }
  }

  @AfterAll
  static void closePlaywright() {
    if (getPlaywright() != null) {
      getPlaywright().close();
      setPlaywright(null);
    }
  }

  static Server getServer() {
    return server.get();
  }

  static void setServer(Server server) {
    TestBase.server.set(server);
  }

  static Server getHttpsServer() {
    return httpsServer.get();
  }

  static void setHttpsServer(Server httpsServer) {
    TestBase.httpsServer.set(httpsServer);
  }

  static BrowserType getBrowserType() {
    return browserType.get();
  }

  static void setBrowserType(BrowserType browserType) {
    TestBase.browserType.set(browserType);
  }

  static Playwright getPlaywright() {
    return playwright.get();
  }

  static void setPlaywright(Playwright playwright) {
    TestBase.playwright.set(playwright);
  }

  static Browser getBrowser() {
    return browser.get();
  }

  static void setBrowser(Browser browser) {
    TestBase.browser.set(browser);
  }

  BrowserContext createContext() {
    return getBrowser().newContext();
  }

  @BeforeEach
  void createContextAndPage() {
    getServer().reset();
    getHttpsServer().reset();
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
