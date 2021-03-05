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

import com.microsoft.playwright.TimeoutError;

class WaitableTimeout<T> implements Waitable<T> {
  private final long deadline;
  private final double timeout;

  WaitableTimeout(double millis) {
    timeout = millis;
    deadline = System.nanoTime() + (long) millis * 1_000_000;
  }

  @Override
  public boolean isDone() {
    return System.nanoTime() > deadline;
  }

  @Override
  public T get() {
    String timeoutStr = Double.toString(timeout);
    if (timeoutStr.endsWith(".0")) {
      timeoutStr = timeoutStr.substring(0, timeoutStr.length() - 2);
    }
    throw new TimeoutError("Timeout " + timeoutStr + "ms exceeded");
  }

  @Override
  public void dispose() {
  }
}

