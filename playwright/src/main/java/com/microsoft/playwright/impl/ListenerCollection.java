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

import com.microsoft.playwright.Deferred;
import com.microsoft.playwright.Event;
import com.microsoft.playwright.Listener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

class ListenerCollection <EventType> {
  private final HashMap<EventType, List<Listener<EventType>>> listeners = new HashMap<>();

  void notify(EventType eventType, Object param) {
    List<Listener<EventType>> list = listeners.get(eventType);
    if (list == null) {
      return;
    }

    Event<EventType> event = new Event<EventType>() {
      @Override
      public EventType type() {
        return eventType;
      }

      @Override
      public Object data() {
        return param;
      }
    };

    for (Listener<EventType> listener: new ArrayList<>(list)) {
      listener.handle(event);
    }
  }

  void add(EventType type, Listener<EventType> listener) {
    List<Listener<EventType>> list = listeners.get(type);
    if (list == null) {
      list = new ArrayList<>();
      listeners.put(type, list);
    }
    list.add(listener);
  }

  void remove(EventType type, Listener<EventType>  listener) {
    List<Listener<EventType>> list = listeners.get(type);
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
