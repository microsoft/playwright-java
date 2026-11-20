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

// Port of network.ts.
package com.microsoft.playwright.impl;

import com.google.gson.JsonObject;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.WebSocket;
import com.microsoft.playwright.WebSocketFrame;
import com.microsoft.playwright.impl.protocol.WebSocketChannel;
import com.microsoft.playwright.impl.protocol.WebSocketFrameReceivedEvent;
import com.microsoft.playwright.impl.protocol.WebSocketFrameSentEvent;
import com.microsoft.playwright.impl.protocol.WebSocketInitializer;
import com.microsoft.playwright.impl.protocol.WebSocketSocketErrorEvent;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class WebSocketImpl extends ChannelOwner implements WebSocket {
  private final PageImpl page;
  private boolean isClosed;

  final WebSocketChannel channel = new WebSocketChannel(this);
  private final WebSocketInitializer webSocketInitializer;

  WebSocketImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
    webSocketInitializer = connection.protocolGson().fromJson(initializer, WebSocketInitializer.class);
    this.isClosed = false;
    this.page = (PageImpl) parent;
  }

  // java: upstream subscribes in the constructor with this._channel.on(...); the Java channel dispatches events here.
  @Override
  void handleEvent(String event, JsonObject params) {
    switch (event) {
      case "frameSent": {
        WebSocketFrameSentEvent e = connection.protocolGson().fromJson(params, WebSocketFrameSentEvent.class);
        if (e.opcode == 1) {
          emit(Events.WebSocket.FRAME_SENT, new WebSocketFrameImpl(e.data, false));
        } else if (e.opcode == 2) {
          emit(Events.WebSocket.FRAME_SENT, new WebSocketFrameImpl(e.data, true));
        }
        break;
      }
      case "frameReceived": {
        WebSocketFrameReceivedEvent e = connection.protocolGson().fromJson(params, WebSocketFrameReceivedEvent.class);
        if (e.opcode == 1) {
          emit(Events.WebSocket.FRAME_RECEIVED, new WebSocketFrameImpl(e.data, false));
        } else if (e.opcode == 2) {
          emit(Events.WebSocket.FRAME_RECEIVED, new WebSocketFrameImpl(e.data, true));
        }
        break;
      }
      case "socketError": {
        WebSocketSocketErrorEvent e = connection.protocolGson().fromJson(params, WebSocketSocketErrorEvent.class);
        emit(Events.WebSocket.ERROR, e.error);
        break;
      }
      case "close": {
        isClosed = true;
        emit(Events.WebSocket.CLOSE, this);
        break;
      }
    }
  }

  @Override
  public String url() {
    return webSocketInitializer.url;
  }

  @Override
  public boolean isClosed() {
    return isClosed;
  }

  // java: the predicate and timeout are passed separately and the code to run is a Runnable; no wrapApiCall
  // for @apiCall(), so that the calls of the callback are reported on their own (see WorkerImpl).
  private <T> T waitForEvent(String event, Predicate<T> predicate, Double timeout, Runnable code) {
    double timeoutMs = page.timeoutSettings.timeout(timeout);
    Waiter waiter = Waiter.createForEvent(this, event);
    waiter.rejectOnTimeout(timeoutMs, "Timeout " + Waiter.formatTimeout(timeoutMs) + "ms exceeded while waiting for event \"" + event + "\"");
    if (!Events.WebSocket.ERROR.equals(event)) {
      waiter.rejectOnEvent(this, Events.WebSocket.ERROR, () -> new PlaywrightException("Socket error"));
    }
    if (!Events.WebSocket.CLOSE.equals(event)) {
      waiter.rejectOnEvent(this, Events.WebSocket.CLOSE, () -> new PlaywrightException("Socket closed"));
    }
    waiter.rejectOnEvent(page, Events.Page.CLOSE, () -> page.closeErrorWithReason());
    T result = waiter.waitForEvent(this, event, predicate, code);
    waiter.dispose();
    return result;
  }

  // java: typed on/off/waitFor overloads of the generated WebSocket interface.
  @Override
  public void onClose(Consumer<WebSocket> handler) {
    on(Events.WebSocket.CLOSE, handler);
  }

  @Override
  public void offClose(Consumer<WebSocket> handler) {
    off(Events.WebSocket.CLOSE, handler);
  }

  @Override
  public void onFrameReceived(Consumer<WebSocketFrame> handler) {
    on(Events.WebSocket.FRAME_RECEIVED, handler);
  }

  @Override
  public void offFrameReceived(Consumer<WebSocketFrame> handler) {
    off(Events.WebSocket.FRAME_RECEIVED, handler);
  }

  @Override
  public void onFrameSent(Consumer<WebSocketFrame> handler) {
    on(Events.WebSocket.FRAME_SENT, handler);
  }

  @Override
  public void offFrameSent(Consumer<WebSocketFrame> handler) {
    off(Events.WebSocket.FRAME_SENT, handler);
  }

  @Override
  public void onSocketError(Consumer<String> handler) {
    on(Events.WebSocket.ERROR, handler);
  }

  @Override
  public void offSocketError(Consumer<String> handler) {
    off(Events.WebSocket.ERROR, handler);
  }

  @Override
  public WebSocketFrame waitForFrameReceived(WaitForFrameReceivedOptions options, Runnable callback) {
    if (options == null) {
      options = new WaitForFrameReceivedOptions();
    }
    return waitForEvent(Events.WebSocket.FRAME_RECEIVED, options.predicate, options.timeout, callback);
  }

  @Override
  public WebSocketFrame waitForFrameSent(WaitForFrameSentOptions options, Runnable callback) {
    if (options == null) {
      options = new WaitForFrameSentOptions();
    }
    return waitForEvent(Events.WebSocket.FRAME_SENT, options.predicate, options.timeout, callback);
  }
}
