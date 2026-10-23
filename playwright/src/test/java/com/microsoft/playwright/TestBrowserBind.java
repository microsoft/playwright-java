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

import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.options.BindResult;

import static org.junit.jupiter.api.Assertions.*;

public class TestBrowserBind {
  @PlaywrightTest
  void shouldBindAndUnbindBrowser(Browser browser) {
    BindResult serverInfo = browser.bind("default");
    try {
      assertNotNull(serverInfo);
      assertNotNull(serverInfo.endpoint);
      assertFalse(serverInfo.endpoint.isEmpty());
    } finally {
      browser.unbind();
    }
  }

  @PlaywrightTest
  void shouldBindWithCustomTitleAndOptions(Browser browser) {
    BindResult serverInfo = browser.bind("my-title",
      new Browser.BindOptions().setHost("127.0.0.1").setPort(0));
    try {
      assertNotNull(serverInfo);
      assertNotNull(serverInfo.endpoint);
      assertFalse(serverInfo.endpoint.isEmpty());
    } finally {
      browser.unbind();
    }
  }
}
