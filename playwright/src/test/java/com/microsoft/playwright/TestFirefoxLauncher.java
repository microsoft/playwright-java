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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import static com.microsoft.playwright.Utils.mapOf;
import static org.junit.jupiter.api.Assertions.*;

public class TestFirefoxLauncher extends TestBase {

  @Override
  @BeforeAll
  // Hide base class method to not launch browser.
  void launchBrowser() {
  }

  @Override
  void createContextAndPage() {
    // Do nothing
  }

  @Test
  @EnabledIf(value="com.microsoft.playwright.TestBase#isFirefox", disabledReason="skip")
  void shouldPassFirefoxUserPreferences() {
    BrowserType.LaunchOptions options = new BrowserType.LaunchOptions().setFirefoxUserPrefs(
      mapOf(
        "network.proxy.type", 1,
        "network.proxy.http", "127.0.0.1",
        "network.proxy.http_port", 3333));
    launchBrowser(options);
    Page page = browser.newPage();
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.navigate("http://example.com"));
    assertTrue(e.getMessage().contains("NS_ERROR_PROXY_CONNECTION_REFUSED"));
  }
}
