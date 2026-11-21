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

// Port of timeoutSettings.ts.
package com.microsoft.playwright.impl;

import static com.microsoft.playwright.impl.isomorphic.Time.DEFAULT_PLAYWRIGHT_LAUNCH_TIMEOUT;
import static com.microsoft.playwright.impl.isomorphic.Time.DEFAULT_PLAYWRIGHT_TIMEOUT;

// java: kNoTimeout is ChannelOwner.NO_TIMEOUT.

// java: no AbortSignal, a timeout is a Double and 0 means no deadline.
class TimeoutSettings {
  private final TimeoutSettings parent;
  private Double defaultTimeout;
  private Double defaultNavigationTimeout;

  TimeoutSettings() {
    this(null);
  }

  TimeoutSettings(TimeoutSettings parent) {
    this.parent = parent;
  }

  void setDefaultTimeout(Double timeout) {
    defaultTimeout = timeout;
  }

  void setDefaultNavigationTimeout(Double timeout) {
    defaultNavigationTimeout = timeout;
  }

  Double defaultNavigationTimeout() {
    return defaultNavigationTimeout;
  }

  Double defaultTimeout() {
    return defaultTimeout;
  }

  // java: navigationTimeout, timeout and launchTimeout return the number of _navigationTimeout, _timeout and
  // _launchTimeout; the { signal, timeout } object they wrap it in has no signal in Java.
  double navigationTimeout(Double timeout) {
    if (timeout != null) {
      return timeout;
    }
    if (defaultNavigationTimeout != null) {
      return defaultNavigationTimeout;
    }
    if ("inspector".equals(debugMode())) {
      return 0;
    }
    if (defaultTimeout != null) {
      return defaultTimeout;
    }
    if (parent != null) {
      return parent.navigationTimeout(timeout);
    }
    return DEFAULT_PLAYWRIGHT_TIMEOUT;
  }

  double timeout(Double timeout) {
    if (timeout != null) {
      return timeout;
    }
    if ("inspector".equals(debugMode())) {
      return 0;
    }
    if (defaultTimeout != null) {
      return defaultTimeout;
    }
    if (parent != null) {
      return parent.timeout(timeout);
    }
    return DEFAULT_PLAYWRIGHT_TIMEOUT;
  }

  double launchTimeout(Double timeout) {
    if (timeout != null) {
      return timeout;
    }
    if ("inspector".equals(debugMode())) {
      return 0;
    }
    if (parent != null) {
      return parent.launchTimeout(timeout);
    }
    return DEFAULT_PLAYWRIGHT_LAUNCH_TIMEOUT;
  }

  // java: debugMode() of utils/debug.ts, PWDEBUG of the process environment.
  static String debugMode() {
    String debugEnv = System.getenv("PWDEBUG");
    if (debugEnv == null) {
      debugEnv = "";
    }
    if (debugEnv.equals("console")) {
      return "console";
    }
    if (debugEnv.equals("0") || debugEnv.equals("false")) {
      return "";
    }
    return !debugEnv.isEmpty() ? "inspector" : "";
  }

  // java: removed once every wait goes through Waiter.
  <T> Waitable<T> createWaitable(Double timeout) {
    if (timeout != null && timeout == 0) {
      return new WaitableNever<>();
    }
    return new WaitableTimeout<>(timeout(timeout));
  }
}
