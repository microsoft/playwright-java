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

package com.microsoft.playwright.impl;

import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.DeviceDescriptor;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.ViewportSize;

class DeviceDescriptorImpl implements DeviceDescriptor {
  PlaywrightImpl playwright;
  private ViewportSize viewport;
  private String userAgent;
  private double deviceScaleFactor;
  private boolean isMobile;
  private boolean hasTouch;
  private String defaultBrowserType;

  @Override
  public ViewportSize viewportSize() {
    return viewport;
  }

  @Override
  public String userAgent() {
    return userAgent;
  }

  @Override
  public double deviceScaleFactor() {
    return deviceScaleFactor;
  }

  @Override
  public boolean isMobile() {
    return isMobile;
  }

  @Override
  public boolean hasTouch() {
    return hasTouch;
  }

  @Override
  public BrowserType defaultBrowserType() {
    switch (defaultBrowserType) {
      case "chromium": return playwright.chromium();
      case "firefox": return playwright.firefox();
      case "webkit": return playwright.webkit();
      default: throw new PlaywrightException("Unknown type: " + defaultBrowserType);
    }
  }
}
