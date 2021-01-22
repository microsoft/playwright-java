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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

class ListenerCollection <EventType> {
  private final HashMap<EventType, List<Consumer<?>>> listeners = new HashMap<>();

  <T> void notify(EventType eventType, T param) {
    List<Consumer<?>> list = listeners.get(eventType);
    if (list == null) {
      return;
    }

    for (Consumer<?> listener: new ArrayList<>(list)) {
      ((Consumer<T>) listener).accept(param);
    }
  }

  void add(EventType type, Consumer<?> listener) {
    List<Consumer<?>> list = listeners.get(type);
    if (list == null) {
      list = new ArrayList<>();
      listeners.put(type, list);
    }
    list.add(listener);
  }

  void remove(EventType type, Consumer<?>  listener) {
    List<Consumer<?>> list = listeners.get(type);
    if (list == null) {
      return;
    }
    list.removeAll(Collections.singleton(listener));
    if (list.isEmpty()) {
      listeners.remove(type);
    }
  }

  boolean hasListeners(EventType type) {
    return listeners.containsKey(type);
  }
}
