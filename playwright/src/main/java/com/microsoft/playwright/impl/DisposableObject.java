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

// Port of disposable.ts.
package com.microsoft.playwright.impl;

import com.microsoft.playwright.impl.protocol.*;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

// java: upstream's Disposable interface is AutoCloseable.
public class DisposableObject extends ChannelOwner implements AutoCloseable {
  final DisposableChannel channel = new DisposableChannel(this);

  DisposableObject(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
  }

  // java: from is not ported, generated results already hold the impl object.

  // java: Symbol.asyncDispose is AutoCloseable.close().
  @Override
  public void close() {
    dispose();
  }

  void dispose() {
    try {
      channel.dispose(NO_TIMEOUT);
    } catch (TargetClosedError e) {
      return;
    }
  }

  // java: the module function of disposable.ts; Promise.all runs the disposals one after another.
  static void disposeAll(List<? extends AutoCloseable> disposables) {
    List<AutoCloseable> copy = new ArrayList<>(disposables);
    disposables.clear();
    for (AutoCloseable d : copy) {
      try {
        d.close();
      } catch (RuntimeException e) {
        throw e;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }
}
