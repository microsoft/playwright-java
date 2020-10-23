/**
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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;

public class TestBase {
  static Server server;
  static Server httpsServer;
  static BrowserType browserType;
  static Playwright playwright;
  static Browser browser;
  static boolean isChromium;
  static boolean isWebKit;
  static boolean isFirefox;
  static boolean headful;
  Page page;
  BrowserContext context;

  @BeforeAll
  static void launchBrowser() {
    playwright = Playwright.create();
    BrowserType.LaunchOptions options = new BrowserType.LaunchOptions();
    browserType = playwright.chromium();
    browser = browserType.launch(options);
    isChromium = true;
    isWebKit = false;
    headful = false;
  }

  @AfterAll
  static void closeBrowser() {
    browser.close();
    browser = null;
  }

  @BeforeAll
  static void startServer() throws IOException {
    server = Server.createHttp(8907);
    httpsServer = Server.createHttps(8908);
  }

  @AfterAll
  static void stopServer() throws IOException {
    server.stop();
    server = null;
    httpsServer.stop();
    httpsServer = null;
  }

  @BeforeEach
  void createContextAndPage() {
    server.reset();
    httpsServer.reset();
    context = browser.newContext();
    page = context.newPage();
  }

  @AfterEach
  void closeContext() {
    context.close();
    context = null;
    page = null;
  }
}
