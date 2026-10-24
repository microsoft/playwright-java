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

package com.microsoft.playwright.fixtures;

import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;

/**
 * The browser a test invocation runs in. Available as a fixture parameter.
 */
public enum BrowserName {
  CHROMIUM("chromium"),
  FIREFOX("firefox"),
  WEBKIT("webkit");

  private final String name;

  BrowserName(String name) {
    this.name = name;
  }

  public static BrowserName fromString(String name) {
    for (BrowserName value : values()) {
      if (value.name.equals(name)) {
        return value;
      }
    }
    throw new IllegalArgumentException("Unknown browser: " + name);
  }

  public BrowserType browserType(Playwright playwright) {
    switch (this) {
      case CHROMIUM: return playwright.chromium();
      case FIREFOX: return playwright.firefox();
      case WEBKIT: return playwright.webkit();
      default: throw new IllegalStateException();
    }
  }

  @Override
  public String toString() {
    return name;
  }
}
