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

package com.microsoft.playwright.fixtures;

/**
 * Runs every cleanup step even when earlier ones fail, then reports the first failure with the
 * others attached as suppressed exceptions, so a broken teardown fails the test instead of being lost.
 * Steps may run on several threads at once.
 */
final class Teardown {
  interface Step {
    void run() throws Exception;
  }

  private Throwable failure;

  void run(Step step) {
    try {
      step.run();
    } catch (Throwable e) {
      record(e);
    }
  }

  private synchronized void record(Throwable e) {
    if (failure == null) {
      failure = e;
    } else if (failure != e) {
      failure.addSuppressed(e);
    }
  }

  synchronized boolean failed() {
    return failure != null;
  }

  synchronized void rethrow() throws Exception {
    if (failure instanceof Error) {
      throw (Error) failure;
    }
    if (failure != null) {
      throw (Exception) failure;
    }
  }
}
