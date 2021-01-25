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

import java.util.function.Consumer;
import java.util.function.Predicate;

class WaitableEvent<EventType, T> implements Waitable<T>, Consumer<T> {
  final ListenerCollection<EventType> listeners;
  private final EventType type;
  private final Predicate<T> predicate;
  private T eventArg;

  WaitableEvent(ListenerCollection<EventType> listeners, EventType type) {
    this(listeners, type, null);
  }

  WaitableEvent(ListenerCollection<EventType> listeners, EventType type, Predicate<T> predicate) {
    this.listeners = listeners;
    this.type = type;
    this.predicate = predicate;
    listeners.add(type, this);
  }

  @Override
  public void accept(T eventArg) {
    if (predicate != null && !predicate.test(eventArg)) {
      return;
    }

    this.eventArg = eventArg;
    dispose();
  }

  @Override
  public boolean isDone() {
    return eventArg != null;
  }

  @Override
  public void dispose() {
    listeners.remove(type, this);
  }

  @Override
  public T get() {
    return eventArg;
  }
}
