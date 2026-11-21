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

// Port of eventEmitter.ts.
package com.microsoft.playwright.impl;

import com.microsoft.playwright.PlaywrightException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

// java: upstream's ChannelOwner and the rest of the client reach the logging helpers through
// imports; here they stay reachable as inherited statics.
// java: event types are strings, Java has no symbols; listeners are Consumers of the one event argument.
class EventEmitter extends LoggingSupport {

  // java: upstream keeps one function or an array of them per type; here it is always a list.
  private Map<String, List<Consumer<?>>> events = new LinkedHashMap<>();
  private int eventsCount = 0;
  private Integer maxListeners = null;
  // java: _pendingHandlers and _rejectionHandler are not ported, a Consumer returns no promise to track.

  // java: the constructor resets the _events of the prototype and aliases on/off to addListener/removeListener;
  // the Java fields start fresh and on/off are methods below.

  EventEmitter setMaxListeners(int n) {
    // java: typeof n and Number.isNaN(n) checks are not ported, n is an int.
    if (n < 0) {
      throw new IllegalArgumentException("The value of \"n\" is out of range. It must be a non-negative number. Received " + n + ".");
    }
    maxListeners = n;
    return this;
  }

  int getMaxListeners() {
    return maxListeners == null ? defaultMaxListeners : maxListeners;
  }

  <T> boolean emit(String type, T arg) {
    List<Consumer<?>> handler = events.get(type);
    if (handler == null) {
      return false;
    }
    // java: the handler is always a list; the listeners are copied, a handler may remove itself.
    List<Consumer<?>> listeners = new ArrayList<>(handler);
    for (Consumer<?> listener : listeners) {
      callHandler(type, listener, arg);
    }
    return true;
  }

  @SuppressWarnings("unchecked")
  private <T> void callHandler(String type, Consumer<?> handler, T arg) {
    try {
      ((Consumer<T>) handler).accept(arg);
    } catch (RuntimeException error) {
      handleListenerError(type, error);
    }
    // java: the branch for a handler that returns a promise is not ported, a Consumer returns nothing.
  }

  private void handleListenerError(String type, RuntimeException error) {
    if ("error".equals(type) || listenerCount("error") == 0) {
      throw error;
    }
    emit("error", error);
  }

  void addListener(String type, Consumer<?> listener) {
    addListener(type, listener, false);
  }

  void on(String type, Consumer<?> listener) {
    addListener(type, listener, false);
  }

  private void addListener(String type, Consumer<?> listener, boolean prepend) {
    checkListener(listener);
    // java: the newListener event is not emitted, upstream passes it two arguments and Java listeners take one.
    List<Consumer<?>> existing = events.get(type);

    if (existing == null) {
      // java: a list of one, upstream stores the function itself.
      existing = new ArrayList<>();
      existing.add(listener);
      events.put(type, existing);
      ++eventsCount;
    } else {
      if (prepend) {
        existing.add(0, listener);
      } else {
        existing.add(listener);
      }

      // Check for listener leak
      int m = getMaxListeners();
      if (m > 0 && existing.size() > m && !warned.contains(existing)) {
        warned.add(existing);
        // No error code for this since it is a Warning
        // java: console.warn(w) of the MaxListenersExceededWarning error; isUnderTest() has no Java counterpart.
        System.err.println("MaxListenersExceededWarning: Possible EventEmitter memory leak detected. " +
            existing.size() + " " + type + " listeners " +
            "added. Use emitter.setMaxListeners() to " +
            "increase limit");
      }
    }
  }

  void prependListener(String type, Consumer<?> listener) {
    addListener(type, listener, true);
  }

  void once(String type, Consumer<?> listener) {
    checkListener(listener);
    on(type, new OnceWrapper(this, type, listener).wrapperFunction);
  }

  void prependOnceListener(String type, Consumer<?> listener) {
    checkListener(listener);
    prependListener(type, new OnceWrapper(this, type, listener).wrapperFunction);
  }

  void removeListener(String type, Consumer<?> listener) {
    // java: checkListener(listener) is not called, removing a null listener stays a no-op as it always was in Java.

    List<Consumer<?>> list = events.get(type);
    if (list == null) {
      return;
    }

    // java: removes every registration of the listener, upstream removes the last one only.
    list.removeIf(l -> l == listener || wrappedListener(l) == listener);
    if (list.isEmpty()) {
      events.remove(type);
      --eventsCount;
    }
    // java: the removeListener event is not emitted, upstream passes it two arguments and Java listeners take one.
  }

  void off(String type, Consumer<?> listener) {
    removeListener(type, listener);
  }

  void removeAllListeners() {
    removeAllListeners(null);
  }

  void removeAllListeners(String type) {
    removeAllListenersImpl(type);
  }

  // java: the options bag { behavior } is the behavior string; upstream returns a promise for this overload.
  void removeAllListeners(String type, String behavior) {
    removeAllListenersImpl(type);

    if ("wait".equals(behavior)) {
      // java: _waitFor and the errors collected by _rejectionHandler are not ported, a Consumer leaves
      // no pending promise behind.
      return;
    }

    if ("ignoreErrors".equals(behavior)) {
      // java: _rejectionHandler is not ported, a Consumer leaves no rejection to ignore.
    }
  }

  // java: _removeAllListeners; the Impl suffix avoids the clash with removeAllListeners(type).
  private void removeAllListenersImpl(String type) {
    // not listening for removeListener, no need to emit
    // java: the removeListener event is never emitted, so the branch that emits it for every listener is not ported.
    if (type == null) {
      events = new LinkedHashMap<>();
      eventsCount = 0;
    } else if (events.get(type) != null) {
      if (--eventsCount == 0) {
        events = new LinkedHashMap<>();
      } else {
        events.remove(type);
      }
    }
  }

  List<Consumer<?>> listeners(String type) {
    return listeners(this, type, true);
  }

  List<Consumer<?>> rawListeners(String type) {
    return listeners(this, type, false);
  }

  int listenerCount(String type) {
    List<Consumer<?>> listener = events.get(type);
    if (listener != null) {
      return listener.size();
    }
    return 0;
  }

  List<String> eventNames() {
    return eventsCount > 0 ? new ArrayList<>(events.keySet()) : new ArrayList<>();
  }

  // java: _waitFor is not ported, a Consumer leaves no pending promise to wait for.

  private List<Consumer<?>> listeners(EventEmitter target, String type, boolean unwrap) {
    List<Consumer<?>> listener = target.events.get(type);
    if (listener == null) {
      return new ArrayList<>();
    }

    return unwrap ? unwrapListeners(listener) : new ArrayList<>(listener);
  }

  // java: the message of the existing Java client, tests match on it; upstream throws a TypeError
  // 'The "listener" argument must be of type Function. Received type ...'.
  private static void checkListener(Consumer<?> listener) {
    if (listener == null) {
      throw new PlaywrightException("Can't add a null listener");
    }
  }

  private static class OnceWrapper {
    private boolean fired = false;
    final WrapperFunction wrapperFunction;
    final Consumer<?> listener;
    private final EventEmitter eventEmitter;
    private final String eventType;

    OnceWrapper(EventEmitter eventEmitter, String eventType, Consumer<?> listener) {
      this.eventEmitter = eventEmitter;
      this.eventType = eventType;
      this.listener = listener;
      this.wrapperFunction = new WrapperFunction(this::handle, listener);
    }

    @SuppressWarnings("unchecked")
    private void handle(Object arg) {
      if (fired) {
        return;
      }
      fired = true;
      eventEmitter.removeListener(eventType, wrapperFunction);
      ((Consumer<Object>) listener).accept(arg);
    }
  }

  private static Consumer<?> unwrapListener(Consumer<?> l) {
    Consumer<?> wrapped = wrappedListener(l);
    return wrapped != null ? wrapped : l;
  }

  private static List<Consumer<?>> unwrapListeners(List<Consumer<?>> arr) {
    List<Consumer<?>> result = new ArrayList<>();
    for (Consumer<?> l : arr) {
      result.add(unwrapListener(l));
    }
    return result;
  }

  private static Consumer<?> wrappedListener(Consumer<?> l) {
    return l instanceof WrapperFunction ? ((WrapperFunction) l).listener : null;
  }

  // java: helpers with no counterpart in eventEmitter.ts.
  // java: NodeEventEmitter.defaultMaxListeners.
  static final int defaultMaxListeners = 10;

  // java: the warned flag that upstream sets on the listener array.
  private final Set<List<Consumer<?>>> warned = Collections.newSetFromMap(new IdentityHashMap<>());

  // java: the wrapper function of OnceWrapper with its listener property.
  private static class WrapperFunction implements Consumer<Object> {
    private final Consumer<Object> handle;
    final Consumer<?> listener;

    WrapperFunction(Consumer<Object> handle, Consumer<?> listener) {
      this.handle = handle;
      this.listener = listener;
    }

    @Override
    public void accept(Object arg) {
      handle.accept(arg);
    }
  }
}
