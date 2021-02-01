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
 */
public interface Playwright extends AutoCloseable {
  /**
   * This object can be used to launch or connect to Chromium, returning instances of {@code ChromiumBrowser}.
   */
  BrowserType chromium();
  /**
   * Returns a dictionary of devices to be used with [{@code method: Browser.newContext}] or [{@code method: Browser.newPage}].
   */
  Map<String, DeviceDescriptor> devices();
  /**
   * This object can be used to launch or connect to Firefox, returning instances of {@code FirefoxBrowser}.
   */
  BrowserType firefox();
  /**
   * Selectors can be used to install custom selector engines. See [Working with selectors](./selectors.md) for more
   * information.
   */
  Selectors selectors();
  /**
   * This object can be used to launch or connect to WebKit, returning instances of {@code WebKitBrowser}.
   */
  BrowserType webkit();
  /**
   * Terminates this instance of Playwright, will also close all created browsers if they are still running.
   */
  void close();

  static Playwright create() {
    return PlaywrightImpl.create();
  }
}

