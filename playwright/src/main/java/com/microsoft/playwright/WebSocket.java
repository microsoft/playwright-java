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

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * The {@code WebSocket} class represents websocket connections in the page.
 */
public interface WebSocket {

  /**
   * Fired when the websocket closes.
   */
  void onClose(Consumer<WebSocket> handler);
  /**
   * Removes handler that was previously added with {@link #onClose onClose(handler)}.
   */
  void offClose(Consumer<WebSocket> handler);

  /**
   * Fired when the websocket receives a frame.
   */
  void onFrameReceived(Consumer<WebSocketFrame> handler);
  /**
   * Removes handler that was previously added with {@link #onFrameReceived onFrameReceived(handler)}.
   */
  void offFrameReceived(Consumer<WebSocketFrame> handler);

  /**
   * Fired when the websocket sends a frame.
   */
  void onFrameSent(Consumer<WebSocketFrame> handler);
  /**
   * Removes handler that was previously added with {@link #onFrameSent onFrameSent(handler)}.
   */
  void offFrameSent(Consumer<WebSocketFrame> handler);

  /**
   * Fired when the websocket has an error.
   */
  void onSocketError(Consumer<String> handler);
  /**
   * Removes handler that was previously added with {@link #onSocketError onSocketError(handler)}.
   */
  void offSocketError(Consumer<String> handler);

  class WaitForFrameReceivedOptions {
    /**
     * Receives the {@code WebSocketFrame} object and resolves to truthy value when the waiting should resolve.
     */
    public Predicate<WebSocketFrame> predicate;
    /**
     * Maximum time to wait for in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The
     * default value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}.
     */
    public Double timeout;

    /**
     * Receives the {@code WebSocketFrame} object and resolves to truthy value when the waiting should resolve.
     */
    public WaitForFrameReceivedOptions setPredicate(Predicate<WebSocketFrame> predicate) {
      this.predicate = predicate;
      return this;
    }
    /**
     * Maximum time to wait for in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The
     * default value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}.
     */
    public WaitForFrameReceivedOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class WaitForFrameSentOptions {
    /**
     * Receives the {@code WebSocketFrame} object and resolves to truthy value when the waiting should resolve.
     */
    public Predicate<WebSocketFrame> predicate;
    /**
     * Maximum time to wait for in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The
     * default value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}.
     */
    public Double timeout;

    /**
     * Receives the {@code WebSocketFrame} object and resolves to truthy value when the waiting should resolve.
     */
    public WaitForFrameSentOptions setPredicate(Predicate<WebSocketFrame> predicate) {
      this.predicate = predicate;
      return this;
    }
    /**
     * Maximum time to wait for in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The
     * default value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}.
     */
    public WaitForFrameSentOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  /**
   * Indicates that the web socket has been closed.
   *
   * @since v1.8
   */
  boolean isClosed();
  /**
   * Contains the URL of the WebSocket.
   *
   * @since v1.8
   */
  String url();
  /**
   * Performs action and waits for a frame to be sent. If predicate is provided, it passes {@code WebSocketFrame} value into
   * the {@code predicate} function and waits for {@code predicate(webSocketFrame)} to return a truthy value. Will throw an
   * error if the WebSocket or Page is closed before the frame is received.
   *
   * @param callback Callback that performs the action triggering the event.
   * @since v1.10
   */
  default WebSocketFrame waitForFrameReceived(Runnable callback) {
    return waitForFrameReceived(null, callback);
  }
  /**
   * Performs action and waits for a frame to be sent. If predicate is provided, it passes {@code WebSocketFrame} value into
   * the {@code predicate} function and waits for {@code predicate(webSocketFrame)} to return a truthy value. Will throw an
   * error if the WebSocket or Page is closed before the frame is received.
   *
   * @param callback Callback that performs the action triggering the event.
   * @since v1.10
   */
  WebSocketFrame waitForFrameReceived(WaitForFrameReceivedOptions options, Runnable callback);
  /**
   * Performs action and waits for a frame to be sent. If predicate is provided, it passes {@code WebSocketFrame} value into
   * the {@code predicate} function and waits for {@code predicate(webSocketFrame)} to return a truthy value. Will throw an
   * error if the WebSocket or Page is closed before the frame is sent.
   *
   * @param callback Callback that performs the action triggering the event.
   * @since v1.10
   */
  default WebSocketFrame waitForFrameSent(Runnable callback) {
    return waitForFrameSent(null, callback);
  }
  /**
   * Performs action and waits for a frame to be sent. If predicate is provided, it passes {@code WebSocketFrame} value into
   * the {@code predicate} function and waits for {@code predicate(webSocketFrame)} to return a truthy value. Will throw an
   * error if the WebSocket or Page is closed before the frame is sent.
   *
   * @param callback Callback that performs the action triggering the event.
   * @since v1.10
   */
  WebSocketFrame waitForFrameSent(WaitForFrameSentOptions options, Runnable callback);
}

