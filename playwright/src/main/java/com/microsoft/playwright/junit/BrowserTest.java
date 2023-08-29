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

package com.microsoft.playwright.junit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;

public class BrowserTest extends PlaywrightTest {
  protected Browser browser;
  protected String browserName;

  @BeforeAll
  protected void launchBrowser() {
    BrowserType browserType;
    if ("firefox".equals(PlaywrightSettings.browserName())) {
      browserType = playwright.firefox();
    } else if ("webkit".equals(PlaywrightSettings.browserName())) {
      browserType = playwright.webkit();
    } else {
      browserType = playwright.chromium();
    }
    browserName = browserType.name();

    browser = browserType.launch(new BrowserType.LaunchOptions()
        .setHeadless(PlaywrightSettings.headless()));
  }

  @AfterAll
  protected void closeBrowser() {
    if (browser != null) {
    browser.close();
    browser = null;
    }
  }
}
