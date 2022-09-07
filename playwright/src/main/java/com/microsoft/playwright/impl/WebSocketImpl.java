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

import com.google.gson.JsonObject;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.WebSocket;
import com.microsoft.playwright.WebSocketFrame;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

class WebSocketImpl extends ChannelOwner implements WebSocket {
  private final ListenerCollection<EventType> listeners = new ListenerCollection<>();
  private final PageImpl page;
  private boolean isClosed;

  enum EventType {
    CLOSE,
    FRAMERECEIVED,
    FRAMESENT,
    SOCKETERROR,
  }

  WebSocketImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
    page = (PageImpl) parent;
  }

  @Override
  public void onClose(Consumer<WebSocket> handler) {
    listeners.add(EventType.CLOSE, handler);
  }

  @Override
  public void offClose(Consumer<WebSocket> handler) {
    listeners.remove(EventType.CLOSE, handler);
  }

  @Override
  public void onFrameReceived(Consumer<WebSocketFrame> handler) {
    listeners.add(EventType.FRAMERECEIVED, handler);
  }

  @Override
  public void offFrameReceived(Consumer<WebSocketFrame> handler) {
    listeners.remove(EventType.FRAMERECEIVED, handler);
  }

  @Override
  public void onFrameSent(Consumer<WebSocketFrame> handler) {
    listeners.add(EventType.FRAMESENT, handler);
  }

  @Override
  public void offFrameSent(Consumer<WebSocketFrame> handler) {
    listeners.remove(EventType.FRAMESENT, handler);
  }

  @Override
  public void onSocketError(Consumer<String> handler) {
    listeners.add(EventType.SOCKETERROR, handler);
  }

  @Override
  public void offSocketError(Consumer<String> handler) {
    listeners.remove(EventType.SOCKETERROR, handler);
  }

  @Override
  public WebSocketFrame waitForFrameReceived(WaitForFrameReceivedOptions options, Runnable code) {
    return withWaitLogging("WebSocket.waitForFrameReceived", logger -> waitForFrameReceivedImpl(options, code));
  }

  private WebSocketFrame waitForFrameReceivedImpl(WaitForFrameReceivedOptions options, Runnable code) {
    if (options == null) {
      options = new WaitForFrameReceivedOptions();
    }
    return waitForEventWithTimeout(EventType.FRAMERECEIVED, code, options.predicate, options.timeout);
  }

  @Override
  public WebSocketFrame waitForFrameSent(WaitForFrameSentOptions options, Runnable code) {
    return withWaitLogging("WebSocket.waitForFrameSent", logger -> waitForFrameSentImpl(options, code));
  }

  private WebSocketFrame waitForFrameSentImpl(WaitForFrameSentOptions options, Runnable code) {
    if (options == null) {
      options = new WaitForFrameSentOptions();
    }
    return waitForEventWithTimeout(EventType.FRAMESENT, code, options.predicate, options.timeout);
  }

  @Override
  public boolean isClosed() {
    return isClosed;
  }

  @Override
  public String url() {
    return initializer.get("url").getAsString();
  }

  private class WaitableWebSocketClose<T> extends WaitableEvent<EventType, T> {
    WaitableWebSocketClose() {
      super(WebSocketImpl.this.listeners, EventType.CLOSE);
    }

    @Override
    public T get() {
      throw new PlaywrightException("Socket closed");
    }
  }

  private class WaitableWebSocketError<T> extends WaitableEvent<EventType, T> {
    WaitableWebSocketError() {
      super(WebSocketImpl.this.listeners, EventType.SOCKETERROR);
    }

    @Override
    public T get() {
      throw new PlaywrightException("Socket error");
    }
  }

  private WebSocketFrame waitForEventWithTimeout(EventType eventType, Runnable code, Predicate<WebSocketFrame> predicate, Double timeout) {
    List<Waitable<WebSocketFrame>> waitables = new ArrayList<>();
    waitables.add(new WaitableEvent<>(listeners, eventType, predicate));
    waitables.add(new WaitableWebSocketClose<>());
    waitables.add(new WaitableWebSocketError<>());
    waitables.add(page.createWaitForCloseHelper());
    waitables.add(page.createWaitableTimeout(timeout));
    return runUntil(code, new WaitableRace<>(waitables));
  }

  private static class WebSocketFrameImpl implements WebSocketFrame {
    private final byte[] bytes;

    WebSocketFrameImpl(String payload, boolean isBase64) {
      if (isBase64) {
        bytes = Base64.getDecoder().decode(payload);
      } else {
        bytes = payload.getBytes();
      }
    }

    @Override
    public byte[] binary() {
      return bytes;
    }

    @Override
    public String text() {
      return new String(bytes, StandardCharsets.UTF_8);
    }
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
        listeners.notify(EventType.FRAMESENT, WebSocketFrame);
        break;
      }
      case "frameReceived": {
        int opCode = parameters.get("opcode").getAsInt();
        if (opCode != 1 && opCode != 2) {
          break;
        }
        WebSocketFrameImpl WebSocketFrame = new WebSocketFrameImpl(
          parameters.get("data").getAsString(), opCode == 2);
        listeners.notify(EventType.FRAMERECEIVED, WebSocketFrame);
        break;
      }
      case "socketError": {
        String error = parameters.get("error").getAsString();
        listeners.notify(EventType.SOCKETERROR, error);
        break;
      }
      case "close": {
        isClosed = true;
        listeners.notify(EventType.CLOSE, this);
        break;
      }
      default: {
        throw new PlaywrightException("Unknown event: " + event);
      }
    }
  }
}
