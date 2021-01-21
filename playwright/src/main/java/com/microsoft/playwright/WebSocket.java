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
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * The {@code WebSocket} class represents websocket connections in the page.
 */
public interface WebSocket {
  interface FrameData {
    byte[] body();
    String text();
  }


  void onClose(Runnable handler);
  void offClose(Runnable handler);

  void onFrameReceived(Consumer<FrameData> handler);
  void offFrameReceived(Consumer<FrameData> handler);

  void onFrameSent(Consumer<FrameData> handler);
  void offFrameSent(Consumer<FrameData> handler);

  void onSocketError(Consumer<String> handler);
  void offSocketError(Consumer<String> handler);


  class WaitForFrameReceivedOptions {
    public Double timeout;
    public WaitForFrameReceivedOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  FrameData waitForFrameReceived(Runnable code, WaitForFrameReceivedOptions options);
  default FrameData waitForFrameReceived(Runnable code) { return waitForFrameReceived(code, null); }

  class WaitForFrameSentOptions {
    public Double timeout;
    public WaitForFrameSentOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  FrameData waitForFrameSent(Runnable code, WaitForFrameSentOptions options);
  default FrameData waitForFrameSent(Runnable code) { return waitForFrameSent(code, null); }

  class WaitForSocketErrorOptions {
    public Double timeout;
    public WaitForSocketErrorOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  String waitForSocketError(Runnable code, WaitForSocketErrorOptions options);
  default String waitForSocketError(Runnable code) { return waitForSocketError(code, null); }

  /**
   * Indicates that the web socket has been closed.
   */
  boolean isClosed();
  /**
   * Contains the URL of the WebSocket.
   */
  String url();
}

