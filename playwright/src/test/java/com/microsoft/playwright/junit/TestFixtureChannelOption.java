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

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Server;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import static com.microsoft.playwright.junit.ServerLifecycle.serverMap;
import static org.junit.jupiter.api.Assertions.*;

@FixtureTest
@UsePlaywright(TestFixtureChannelOption.CustomOptions.class)
@EnabledIf("isChannelSpecified")
public class TestFixtureChannelOption {

  public static class CustomOptions implements OptionsFactory {
    @Override
    public Options getOptions() {
      return new Options().setChannel(channel());
    }
  }

  public static boolean isChannelSpecified() {
    return channel() != null;
  }

  public static String channel() {
    return System.getenv("BROWSER_CHANNEL");
  }

  @Test
  public void testBrowserChannel(Server server, Browser browser, Page page) {
    assertEquals(browser.browserType().name(), "chromium");
    page.navigate(server.EMPTY_PAGE);
    String brands = (String) page.evaluate("navigator.userAgentData?.brands.map(b => b.brand).join(', ') || ''");
    if (channel().contains("chrome")) {
      assertTrue(brands.contains("Google Chrome") || brands.contains("HeadlessChrome"), brands);
    } else if (channel().contains("msedge")) {
      assertTrue(brands.contains("Microsoft Edge") || brands.contains("HeadlessEdg"), brands);
    } else {
      fail("Unknown channel: " + channel());
    }
  }
}
