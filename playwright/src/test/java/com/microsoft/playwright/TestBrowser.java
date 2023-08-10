/*
 * Copyright (c) Microsoft Corporation.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.playwright;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.playwright.options.BrowserChannel;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class TestBrowser extends TestBase {
  @Override
  void createContextAndPage() {
    // Do not create anything.
  }

  @Test
  void shouldCreateNewPage() {
    Page page1 = browser.newPage();
    assertEquals(1, browser.contexts().size());

    Page page2 = browser.newPage();
    assertEquals(2, browser.contexts().size());

    page1.close();
    assertEquals(1, browser.contexts().size());

    page2.close();
    assertEquals(0, browser.contexts().size());
  }

  @Test
  void shouldThrowUponSecondCreateNewPage() {
    Page page = browser.newPage();
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.context().newPage());
    assertTrue(e.getMessage().contains("Please use browser.newContext()"));
    page.close();
  }

  @Test
  void versionShouldWork() {
    if (isChromium()) {
      assertTrue(Pattern.matches("^\\d+\\.\\d+\\.\\d+\\.\\d+$", browser.version()));
    } else if (isWebKit()) {
      assertTrue(Pattern.matches("^\\d+\\.\\d+", browser.version()));
    } else if (isFirefox()) {
      // It can be 85.0b1 in Firefox.
      assertTrue(Pattern.matches("^\\d+\\.\\d+.*", browser.version()));
    }
  }

  private static BrowserChannel getBrowserChannelEnumFromEnv() {
    String channel = getBrowserChannelFromEnv();
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

  @Test
  void shouldSupportDeprecatedChannelEnum() {
    BrowserChannel channel = getBrowserChannelEnumFromEnv();
    Assumptions.assumeTrue(channel != null);
    BrowserType.LaunchOptions options = createLaunchOptions();
    options.setChannel(channel);
    Browser browser = browserType.launch(options);
    assertNotNull(browser);
    browser.close();
  }

  @Test
  void shouldReturnBrowserType() {
    assertEquals(browserType, browser.browserType());
  }

  @Test
  @EnabledIf(value = "com.microsoft.playwright.TestBase#isChromium", disabledReason = "Chrome Devtools Protocol supported by chromium only")
  void shouldWorkWithNewBrowserCDPSession() {
    CDPSession session = browser.newBrowserCDPSession();

    JsonElement response = session.send("Browser.getVersion");
    assertNotNull(response.getAsJsonObject().get("userAgent").toString());

    AtomicReference<Boolean> gotEvent = new AtomicReference<>(false);

    session.on("Target.targetCreated", jsonElement -> {
      gotEvent.set(true);
    });

    JsonObject params = new JsonObject();
    params.addProperty("discover", true);
    session.send("Target.setDiscoverTargets", params);

    Page page = browser.newPage();
    assertTrue(gotEvent.get());
    page.close();

    session.detach();
  }
}
