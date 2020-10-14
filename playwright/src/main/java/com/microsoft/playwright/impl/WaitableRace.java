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

import java.util.Arrays;
import java.util.Collection;

class WaitableRace implements Waitable {
  private final Collection<Waitable> waitables;

  WaitableRace(Waitable... waitables) {
    this(Arrays.asList(waitables));
  }

  WaitableRace(Collection<Waitable> waitables) {
    this.waitables = waitables;
  }

  @Override
  public boolean isDone() {
    for (Waitable w : waitables) {
      if (w.isDone()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Object get() {
    assert isDone();
    dispose();
    for (Waitable w : waitables) {
      if (w.isDone()) {
        return w.get();
      }
    }
    throw new IllegalStateException("At least one element must be ready");
  }

  @Override
  public void dispose() {
    for (Waitable w : waitables) {
      w.dispose();
    }
  }
}
