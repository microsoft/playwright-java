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

import com.microsoft.playwright.impl.protocol.*;
import com.google.gson.JsonObject;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.WebSocket;
import com.microsoft.playwright.WebSocketFrame;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class WebSocketImpl extends ChannelOwner implements WebSocket {
  private final PageImpl page;
  private boolean isClosed;

  final WebSocketChannel channel = new WebSocketChannel(this);

  WebSocketImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
    page = (PageImpl) parent;
  }

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
  public WebSocketFrame waitForFrameReceived(WaitForFrameReceivedOptions options, Runnable code) {
    if (options == null) {
      options = new WaitForFrameReceivedOptions();
    }
    return waitForEvent(Events.WebSocket.FRAME_RECEIVED, options.predicate, options.timeout, code);
  }

  @Override
  public WebSocketFrame waitForFrameSent(WaitForFrameSentOptions options, Runnable code) {
    if (options == null) {
      options = new WaitForFrameSentOptions();
    }
    return waitForEvent(Events.WebSocket.FRAME_SENT, options.predicate, options.timeout, code);
  }

  @Override
  public boolean isClosed() {
    return isClosed;
  }

  @Override
  public String url() {
    return initializer.get("url").getAsString();
  }

  // waitForEvent in network.ts
  private WebSocketFrame waitForEvent(String event, Predicate<WebSocketFrame> predicate, Double timeout, Runnable code) {
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
    WebSocketFrame result = waiter.waitForEvent(this, event, predicate, code);
    waiter.dispose();
    return result;
  }

  @Override
  void handleEvent(String event, JsonObject parameters) {
    switch (event) {
      case "frameSent": {
        int opCode = parameters.get("opcode").getAsInt();
        if (opCode != 1 && opCode != 2) {
          break;
        }
        WebSocketFrameImpl WebSocketFrame = new WebSocketFrameImpl(
          parameters.get("data").getAsString(), opCode == 2);
        emit(Events.WebSocket.FRAME_SENT, WebSocketFrame);
        break;
      }
      case "frameReceived": {
        int opCode = parameters.get("opcode").getAsInt();
        if (opCode != 1 && opCode != 2) {
          break;
        }
        WebSocketFrameImpl WebSocketFrame = new WebSocketFrameImpl(
          parameters.get("data").getAsString(), opCode == 2);
        emit(Events.WebSocket.FRAME_RECEIVED, WebSocketFrame);
        break;
      }
      case "socketError": {
        String error = parameters.get("error").getAsString();
        emit(Events.WebSocket.ERROR, error);
        break;
      }
      case "close": {
        isClosed = true;
        emit(Events.WebSocket.CLOSE, this);
        break;
      }
      default: {
        throw new PlaywrightException("Unknown event: " + event);
      }
    }
  }
}
