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

import com.microsoft.playwright.fixtures.EnabledOnBrowser;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static com.microsoft.playwright.Utils.mapOf;
import static com.microsoft.playwright.fixtures.BrowserName.FIREFOX;
import static org.junit.jupiter.api.Assertions.*;

@EnabledOnBrowser(value = FIREFOX, reason = "skip")
public class TestFirefoxLauncher {
  @PlaywrightTest
  void shouldPassFirefoxUserPreferences(BrowserType browserType) {
    BrowserType.LaunchOptions options = new BrowserType.LaunchOptions().setFirefoxUserPrefs(
      mapOf(
        "network.proxy.type", 1,
        "network.proxy.http", "127.0.0.1",
        "network.proxy.http_port", 3333));
    try (Browser browser = browserType.launch(options)) {
      Page page = browser.newPage();
      PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.navigate("http://example.com"));
      assertTrue(e.getMessage().contains("NS_ERROR_PROXY_CONNECTION_REFUSED"));
    }
  }

  @PlaywrightTest
  void shouldPassFirefoxUserPreferencesInPersistent(BrowserType browserType, @TempDir Path tmpDir) {
    BrowserType.LaunchPersistentContextOptions options = new BrowserType.LaunchPersistentContextOptions().setFirefoxUserPrefs(
      mapOf(
        "network.proxy.type", 1,
        "network.proxy.http", "127.0.0.1",
        "network.proxy.http_port", 3333));
    try (BrowserContext context = browserType.launchPersistentContext(tmpDir.resolve("user-data-dir"), options)) {
      assertNotNull(context);
      Page page = context.pages().get(0);
      PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.navigate("http://example.com"));
      assertTrue(e.getMessage().contains("NS_ERROR_PROXY_CONNECTION_REFUSED"));
    }
  }
}
