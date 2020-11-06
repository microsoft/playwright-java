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

/**
 * The WebSocket class represents websocket connections in the page.
 */
public interface WebSocket {
  enum EventType {
    CLOSE,
    FRAMERECEIVED,
    FRAMESENT,
    SOCKETERROR,
  }

  void addListener(EventType type, Listener<EventType> listener);
  void removeListener(EventType type, Listener<EventType> listener);
  class WebSocketFramereceived {
    /**
     * frame payload
     */
    public byte[] payload;

    public WebSocketFramereceived withPayload(byte[] payload) {
      this.payload = payload;
      return this;
    }
  }
  class WebSocketFramesent {
    /**
     * frame payload
     */
    public byte[] payload;

    public WebSocketFramesent withPayload(byte[] payload) {
      this.payload = payload;
      return this;
    }
  }
  /**
   * Indicates that the web socket has been closed.
   */
  boolean isClosed();
  /**
   * Contains the URL of the WebSocket.
   */
  String url();
  default Object waitForEvent(String event) {
    return waitForEvent(event, null);
  }
  /**
   * Waits for event to fire and passes its value into the predicate function. Resolves when the predicate returns truthy value. Will throw an error if the webSocket is closed before the event
   * <p>
   * is fired.
   * @param event Event name, same one would pass into {@code webSocket.on(event)}.
   * @param optionsOrPredicate Either a predicate that receives an event or an options object.
   * @return Promise which resolves to the event data value.
   */
  Object waitForEvent(String event, String optionsOrPredicate);
}

