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

// Port of isomorphic/time.ts.
package com.microsoft.playwright.impl;

// java: no setTimeOrigin and timeOrigin, the client does not use them.
final class Time {
  private Time() {
  }

  // java: System.nanoTime() stands for performance.now(), in milliseconds.
  static double monotonicTime() {
    return Math.floor(System.nanoTime() / 1000.0) / 1000;
  }

  static final int DEFAULT_PLAYWRIGHT_TIMEOUT = 30_000;
  static final int DEFAULT_PLAYWRIGHT_LAUNCH_TIMEOUT = 3 * 60 * 1000; // 3 minutes
}
