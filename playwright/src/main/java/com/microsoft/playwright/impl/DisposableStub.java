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

class DisposableStub implements AutoCloseable {
  private Runnable dispose;

  DisposableStub(Runnable dispose) {
    this.dispose = dispose;
  }

  @Override
  public void close() {
    if (dispose == null)
      return;
    Runnable d = dispose;
    dispose = null;
    d.run();
  }
}
