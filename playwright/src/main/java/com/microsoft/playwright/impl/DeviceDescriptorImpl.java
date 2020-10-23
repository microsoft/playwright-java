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

import com.microsoft.playwright.DeviceDescriptor;

class DeviceDescriptorImpl implements DeviceDescriptor {
  private static class ViewportImpl implements Viewport{
    private int width;
    private int height;

    @Override
    public int width() {
      return width;
    }

    @Override
    public int height() {
      return height;
    }
  }
  private ViewportImpl viewport;
  private String userAgent;
  private int deviceScaleFactor;
  private boolean isMobile;
  private boolean hasTouch;
  // 'chromium' | 'firefox' | 'webkit'
  private String defaultBrowserType;

  @Override
  public Viewport viewport() {
    return viewport;
  }

  @Override
  public String userAgent() {
    return userAgent;
  }

  @Override
  public int deviceScaleFactor() {
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
      case "chromium": return BrowserType.CHROMIUM;
      case "firefox": return BrowserType.FIREFOX;
      case "webkit": return BrowserType.WEBKIT;
      default: throw new RuntimeException("Unknown type: " + defaultBrowserType);
    }
  }
}
