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

import java.util.Collection;

class WaitableRace<T> implements Waitable<T> {
  private final Collection<Waitable<T>> waitables;
  private Waitable<T> firstReady;

  WaitableRace(Collection<Waitable<T>> waitables) {
    this.waitables = waitables;
  }

  @Override
  public boolean isDone() {
    if (firstReady != null) {
      return true;
    }
    for (Waitable<T> w : waitables) {
      if (w.isDone()) {
        firstReady = w;
        return true;
      }
    }
    return false;
  }

  @Override
  public T get() {
    try {
      return firstReady.get();
    } finally {
      dispose();
    }
  }

  @Override
  public void dispose() {
    for (Waitable<T> w : waitables) {
      w.dispose();
    }
  }
}
