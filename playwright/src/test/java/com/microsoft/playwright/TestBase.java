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

import org.junit.jupiter.api.*;

import com.microsoft.playwright.options.SameSiteAttribute;

import javax.sql.rowset.Predicate;
import java.io.IOException;
import java.security.Provider;
import java.time.Duration;
import java.time.Instant;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import static com.microsoft.playwright.Utils.getBrowserNameFromEnv;
import static com.microsoft.playwright.Utils.nextFreePort;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestBase {
  // Fields reset once before all tests in a class.
  Server server;
  Server httpsServer;
  BrowserType browserType;
  Playwright playwright;
  Browser browser;

  static final boolean isMac = Utils.getOS() == Utils.OS.MAC;
  static final boolean isWindows = Utils.getOS() == Utils.OS.WINDOWS;
  static final boolean headful;
  static final SameSiteAttribute defaultSameSiteCookieValue;
  static {
    String headfulEnv = System.getenv("HEADFUL");
    headful = headfulEnv != null && !"0".equals(headfulEnv) && !"false".equals(headfulEnv);
    defaultSameSiteCookieValue = initSameSiteAttribute();
  }

  // Fields reset before each test.
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

  static String getBrowserChannelFromEnv() {
    return System.getenv("BROWSER_CHANNEL");
  }

  static BrowserType.LaunchOptions createLaunchOptions() {
    BrowserType.LaunchOptions options;
    options = new BrowserType.LaunchOptions();
    options.headless = !headful;
    options.channel = getBrowserChannelFromEnv();
    return options;
  }

  Playwright.CreateOptions playwrightOptions() {
    return null;
  }

  void initBrowserType() {
    playwright = Playwright.create(playwrightOptions());
    browserType = Utils.getBrowserTypeFromEnv(playwright);
  }

  void launchBrowser(BrowserType.LaunchOptions launchOptions) {
    initBrowserType();
    browser = browserType.launch(launchOptions);
  }

  @BeforeAll
  void launchBrowser() {
    launchBrowser(createLaunchOptions());
  }

  @AfterAll
  void closeBrowser() {
    if (browser != null) {
      browser.close();
      browser = null;
    }
  }

  @BeforeAll
  void startServer() throws IOException {
    server = Server.createHttp(nextFreePort());
    httpsServer = Server.createHttps(nextFreePort());
  }

  @AfterAll
  void stopServer() {
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
  void closePlaywright() {
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

  void waitForCondition(BooleanSupplier predicate) {
    waitForCondition(predicate, 5_000);
  }
  void waitForCondition(BooleanSupplier predicate, int timeoutMs) {
    page.waitForCondition(predicate, new Page.WaitForConditionOptions().setTimeout(timeoutMs));
  }

  private static SameSiteAttribute initSameSiteAttribute() {
    if (isChromium()) return SameSiteAttribute.LAX;
    if (isWebKit()) return SameSiteAttribute.NONE;
    // for firefox version >= 103 'None' is used.
    return SameSiteAttribute.NONE;
  }
}
