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

import java.util.*;
import java.util.function.Consumer;

class ListenerCollection <EventType> {
  private final HashMap<EventType, List<Consumer<?>>> listeners = new HashMap<>();
  private final ChannelOwner channelOwner;

  ListenerCollection() {
    this(null, null);
  }
  ListenerCollection(Map<EventType, String> eventSubscriptions, ChannelOwner channelOwner) {
    this.channelOwner = channelOwner;
    if (eventSubscriptions != null) {
      // java: the per-class EventType enums stay until EventEmitter is ported, the owner is keyed by their names.
      Map<String, String> mapping = new HashMap<>();
      for (Map.Entry<EventType, String> entry : eventSubscriptions.entrySet()) {
        mapping.put(entry.getKey().toString(), entry.getValue());
      }
      channelOwner.setEventToSubscriptionMapping(mapping);
    }
  }

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
    if (listener == null) {
      throw new PlaywrightException("Can't add a null listener");
    }
    List<Consumer<?>> list = listeners.get(type);
    if (list == null) {
      list = new ArrayList<>();
      listeners.put(type, list);
      updateSubscription(type, true);
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
      updateSubscription(type, false);
      listeners.remove(type);
    }
  }

  boolean hasListeners(EventType type) {
    return listeners.containsKey(type);
  }

  private void updateSubscription(EventType eventType, boolean enabled) {
    if (channelOwner != null) {
      channelOwner.updateSubscription(eventType.toString(), enabled);
    }
  }
}
