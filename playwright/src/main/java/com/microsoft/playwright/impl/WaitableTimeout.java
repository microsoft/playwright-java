/**
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

class WaitableTimeout implements Waitable {
  private final long deadline;
  private final int timeout;

  WaitableTimeout(int millis) {
    timeout = millis;
    deadline = System.nanoTime() + millis * 1_000_000;
  }


  @Override
  public boolean isDone() {
    return System.nanoTime() > deadline;
  }

  @Override
  public Object get() {
    throw new RuntimeException("Timeout " + timeout + "ms exceeded");
  }

  @Override
  public void dispose() {
  }
}
