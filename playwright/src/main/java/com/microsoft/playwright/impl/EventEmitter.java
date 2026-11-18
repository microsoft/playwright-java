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
// Port of packages/playwright-core/src/client/eventEmitter.ts
package com.microsoft.playwright.impl;

import com.microsoft.playwright.PlaywrightException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

// java: upstream's ChannelOwner and the rest of the client reach the logging helpers through
// imports; here they stay reachable as inherited statics.
class EventEmitter extends LoggingSupport {
  private final Map<String, List<Consumer<?>>> events = new HashMap<>();

  <T> void emit(String type, T arg) {
    List<Consumer<?>> handlers = events.get(type);
    if (handlers == null) {
      return;
    }
    // The handlers are copied, a handler may remove itself.
    for (Consumer<?> handler : new ArrayList<>(handlers)) {
      ((Consumer<T>) handler).accept(arg);
    }
  }

  void addListener(String type, Consumer<?> listener) {
    if (listener == null) {
      throw new PlaywrightException("Can't add a null listener");
    }
    List<Consumer<?>> list = events.get(type);
    if (list == null) {
      list = new ArrayList<>();
      events.put(type, list);
    }
    list.add(listener);
  }

  void on(String type, Consumer<?> listener) {
    addListener(type, listener);
  }

  void removeListener(String type, Consumer<?> listener) {
    List<Consumer<?>> list = events.get(type);
    if (list == null) {
      return;
    }
    // java: removes every registration of the listener, upstream removes the last one only.
    list.removeAll(java.util.Collections.singleton(listener));
    if (list.isEmpty()) {
      events.remove(type);
    }
  }

  void off(String type, Consumer<?> listener) {
    removeListener(type, listener);
  }

  void removeAllListeners(String type) {
    events.remove(type);
  }

  int listenerCount(String type) {
    List<Consumer<?>> list = events.get(type);
    return list == null ? 0 : list.size();
  }

  List<Consumer<?>> listeners(String type) {
    List<Consumer<?>> list = events.get(type);
    return list == null ? new ArrayList<Consumer<?>>() : new ArrayList<>(list);
  }
}
