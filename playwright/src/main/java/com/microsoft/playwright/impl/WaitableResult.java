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

import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.TimeoutError;

class WaitableResult<T> implements Waitable<T> {
  private T result;
  private RuntimeException exception;
  private boolean isDone;

  void complete(T result) {
    if (isDone) {
      return;
    }
    this.result = result;
    isDone = true;
  }

  void completeExceptionally(RuntimeException exception) {
    if (isDone) {
      return;
    }
    this.exception = exception;
    isDone = true;
  }

  @Override
  public boolean isDone() {
    return isDone;
  }

  @Override
  public T get() {
    if (exception != null) {
      if (exception instanceof TimeoutError) {
        throw new TimeoutError(exception.getMessage(), exception);
      }
      throw new PlaywrightException(exception.getMessage(), exception);
    }
    return result;
  }

  @Override
  public void dispose() {
  }
}
