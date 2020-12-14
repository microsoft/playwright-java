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

package com.microsoft.playwright;

import java.util.*;
import java.util.function.Predicate;

/**
 * The WebSocket class represents websocket connections in the page.
 */
public interface WebSocket {
  interface FrameData {
    byte[] body();
    String text();
  }

  class WaitForEventOptions {
    public Integer timeout;
    public Predicate<Event<EventType>> predicate;
    public WaitForEventOptions withTimeout(int millis) {
      timeout = millis;
      return this;
    }
    public WaitForEventOptions withPredicate(Predicate<Event<EventType>> predicate) {
      this.predicate = predicate;
      return this;
    }
  }

  enum EventType {
    CLOSE,
    FRAMERECEIVED,
    FRAMESENT,
    SOCKETERROR,
  }

  void addListener(EventType type, Listener<EventType> listener);
  void removeListener(EventType type, Listener<EventType> listener);
  /**
   * Indicates that the web socket has been closed.
   */
  boolean isClosed();
  /**
   * Contains the URL of the WebSocket.
   */
  String url();
  default Deferred<Event<EventType>> waitForEvent(EventType event) {
    return waitForEvent(event, (WaitForEventOptions) null);
  }
  default Deferred<Event<EventType>> waitForEvent(EventType event, Predicate<Event<EventType>> predicate) {
    WaitForEventOptions options = new WaitForEventOptions();
    options.predicate = predicate;
    return waitForEvent(event, options);
  }
  /**
   * Returns the event data value.
   * <p>
   * Waits for event to fire and passes its value into the predicate function. Resolves when the predicate returns truthy
   * <p>
   * value. Will throw an error if the webSocket is closed before the event is fired.
   * @param event Event name, same one would pass into {@code webSocket.on(event)}.
   */
  Deferred<Event<EventType>> waitForEvent(EventType event, WaitForEventOptions options);
}

