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

import com.microsoft.playwright.impl.PlaywrightImpl;
import java.util.*;

/**
 * Playwright module provides a method to launch a browser instance. The following is a typical example of using Playwright
 * to drive automation:
 *
 * <p> By default, the {@code playwright} NPM package automatically downloads browser executables during installation. The
 * {@code playwright-core} NPM package can be used to skip automatic downloads.
 */
public interface Playwright {
  /**
   * This object can be used to launch or connect to Chromium, returning instances of {@code ChromiumBrowser}.
   */
  BrowserType chromium();
  /**
   * Returns a list of devices to be used with [{@code method: Browser.newContext}] or [{@code method: Browser.newPage}]. Actual list of
   * devices can be found in
   * [src/server/deviceDescriptors.ts](https://github.com/Microsoft/playwright/blob/master/src/server/deviceDescriptors.ts).
   */
  Map<String, DeviceDescriptor> devices();
  /**
   * This object can be used to launch or connect to Firefox, returning instances of {@code FirefoxBrowser}.
   */
  BrowserType firefox();
  /**
   * Selectors can be used to install custom selector engines. See
   * [Working with selectors](./selectors.md#working-with-selectors) for more information.
   */
  Selectors selectors();
  /**
   * This object can be used to launch or connect to WebKit, returning instances of {@code WebKitBrowser}.
   */
  BrowserType webkit();

  static Playwright create() {
    return PlaywrightImpl.create();
  }

  void close() throws Exception;
}

