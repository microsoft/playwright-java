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
    public byte[] payload;

    public WebSocketFramereceived withPayload(byte[] payload) {
      this.payload = payload;
      return this;
    }
  }
  class WebSocketFramesent {
    public byte[] payload;

    public WebSocketFramesent withPayload(byte[] payload) {
      this.payload = payload;
      return this;
    }
  }
  boolean isClosed();
  String url();
  default Object waitForEvent(String event) {
    return waitForEvent(event, null);
  }
  Object waitForEvent(String event, String optionsOrPredicate);
}

