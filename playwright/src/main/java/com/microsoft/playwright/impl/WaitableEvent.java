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

class WaitableEvent<T> implements Waitable<T>, Consumer<T> {
  final EventEmitter emitter;
  private final String type;
  private final Predicate<T> predicate;
  private T eventArg;

  WaitableEvent(EventEmitter emitter, String type) {
    this(emitter, type, null);
  }

  WaitableEvent(EventEmitter emitter, String type, Predicate<T> predicate) {
    this.emitter = emitter;
    this.type = type;
    this.predicate = predicate;
    emitter.on(type, this);
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
    emitter.off(type, this);
  }

  @Override
  public T get() {
    return eventArg;
  }
}
