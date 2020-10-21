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

import java.util.function.Function;

class WaitableAdapter<F, T> implements Waitable<T> {
  private final Waitable<F> waitable;
  private final Function<F, T> transformation;

  WaitableAdapter(Waitable<F> waitable, Function<F, T> transformation) {
    this.waitable = waitable;
    this.transformation = transformation;
  }
  @Override
  public boolean isDone() {
    return waitable.isDone();
  }

  @Override
  public T get() {
    return transformation.apply(waitable.get());
  }

  @Override
  public void dispose() {
    waitable.dispose();
  }
}
