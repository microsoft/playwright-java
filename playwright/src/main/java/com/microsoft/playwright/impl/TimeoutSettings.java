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

// Port of timeoutSettings.ts. java: no AbortSignal and no PWDEBUG=inspector branch, a timeout is a Double
// and 0 means no deadline.
class TimeoutSettings {
  static final int DEFAULT_TIMEOUT = 30_000;
  static final int DEFAULT_LAUNCH_TIMEOUT = 180_000;

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

  double navigationTimeout(Double timeout) {
    if (timeout != null) {
      return timeout;
    }
    if (defaultNavigationTimeout != null) {
      return defaultNavigationTimeout;
    }
    if (defaultTimeout != null) {
      return defaultTimeout;
    }
    if (parent != null) {
      return parent.navigationTimeout(timeout);
    }
    return DEFAULT_TIMEOUT;
  }

  double timeout(Double timeout) {
    if (timeout != null) {
      return timeout;
    }
    if (defaultTimeout != null) {
      return defaultTimeout;
    }
    if (parent != null) {
      return parent.timeout(timeout);
    }
    return DEFAULT_TIMEOUT;
  }

  double launchTimeout(Double timeout) {
    if (timeout != null) {
      return timeout;
    }
    if (parent != null) {
      return parent.launchTimeout(timeout);
    }
    return DEFAULT_LAUNCH_TIMEOUT;
  }

  // java: removed once every wait goes through Waiter.
  <T> Waitable<T> createWaitable(Double timeout) {
    if (timeout != null && timeout == 0) {
      return new WaitableNever<>();
    }
    return new WaitableTimeout<>(timeout(timeout));
  }
}
