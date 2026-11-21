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
package com.microsoft.playwright.impl.isomorphic;

// Hopefully, this file is never used in injected sources,
// because it does not use `builtins.performance`,
// and can break when clock emulation is engaged.

// java: the time of class loading stands for performance.timeOrigin, System.nanoTime() since then for
// performance.now().
public final class Time {
  private Time() {
  }

  private static final long performanceStartNanos = System.nanoTime();
  private static final double performanceTimeOrigin = System.currentTimeMillis();

  private static double timeOrigin = performanceTimeOrigin;
  private static double timeShift = 0;

  public static void setTimeOrigin(double origin) {
    timeOrigin = origin;
    timeShift = performanceTimeOrigin - origin;
  }

  public static double timeOrigin() {
    return timeOrigin;
  }

  public static double monotonicTime() {
    return Math.floor((performanceNow() + timeShift) * 1000) / 1000;
  }

  public static final int DEFAULT_PLAYWRIGHT_TIMEOUT = 30_000;
  public static final int DEFAULT_PLAYWRIGHT_LAUNCH_TIMEOUT = 3 * 60 * 1000; // 3 minutes

  // java: helpers with no counterpart upstream.

  // java: performance.now(), in milliseconds.
  private static double performanceNow() {
    return (System.nanoTime() - performanceStartNanos) / 1_000_000.0;
  }
}
