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

package com.microsoft.playwright.impl.junit;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.junit.Options;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestBrowserContextOptionsExtension {
  @Test
  void shouldThrowWhenBrowserNameDoesNotMatchDevice() {
    try (Playwright playwright = Playwright.create()) {
      Options options = new Options().setDeviceName("iPhone 14").setBrowserName("chromium");
      PlaywrightException e = assertThrows(PlaywrightException.class,
        () -> BrowserContextOptionsExtension.getContextOptions(playwright, options));
      assertEquals("Device \"iPhone 14\" requires \"webkit\" browser, but browserName is \"chromium\"", e.getMessage());
    }
  }

  @Test
  void shouldAllowBrowserNameMatchingDevice() {
    try (Playwright playwright = Playwright.create()) {
      Options options = new Options().setDeviceName("iPhone 14").setBrowserName("webkit");
      Browser.NewContextOptions contextOptions = BrowserContextOptionsExtension.getContextOptions(playwright, options);
      assertTrue(contextOptions.isMobile);
    }
  }
}
