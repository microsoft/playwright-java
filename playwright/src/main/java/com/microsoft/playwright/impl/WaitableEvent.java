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

import com.microsoft.playwright.Event;
import com.microsoft.playwright.Listener;
import com.microsoft.playwright.Page;

import java.util.function.Predicate;

class WaitableEvent<EventType> implements Waitable, Listener<EventType> {
  private final ListenerCollection<EventType> listeners;
  private final EventType type;
  private final Predicate<Event<EventType>> predicate;
  private Event<EventType> event;

  WaitableEvent(ListenerCollection<EventType> listeners, EventType type, Predicate<Event<EventType>> predicate) {
    this.listeners = listeners;
    this.type = type;
    this.predicate = predicate;
    listeners.add(type, this);
  }

  @Override
  public void handle(Event<EventType> event) {
    assert type.equals(event.type());
    if (!predicate.test(event)) {
      return;
    }

    this.event = event;
    dispose();
  }

  @Override
  public boolean isDone() {
    return event != null;
  }

  @Override
  public void dispose() {
    listeners.remove(type, this);
  }

  public Object get() {
    return event.data();
  }
}
