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
import com.microsoft.playwright.WebSocketFrame;
import com.microsoft.playwright.WebSocketRoute;
import com.microsoft.playwright.impl.protocol.WebSocketRouteChannel;
import com.microsoft.playwright.impl.protocol.WebSocketRouteClosePageEvent;
import com.microsoft.playwright.impl.protocol.WebSocketRouteClosePageParams;
import com.microsoft.playwright.impl.protocol.WebSocketRouteCloseServerEvent;
import com.microsoft.playwright.impl.protocol.WebSocketRouteCloseServerParams;
import com.microsoft.playwright.impl.protocol.WebSocketRouteConnectParams;
import com.microsoft.playwright.impl.protocol.WebSocketRouteInitializer;
import com.microsoft.playwright.impl.protocol.WebSocketRouteMessageFromPageEvent;
import com.microsoft.playwright.impl.protocol.WebSocketRouteMessageFromServerEvent;
import com.microsoft.playwright.impl.protocol.WebSocketRouteSendToPageParams;
import com.microsoft.playwright.impl.protocol.WebSocketRouteSendToServerParams;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.microsoft.playwright.impl.Utils.toProtocol;

public class WebSocketRouteImpl extends ChannelOwner implements WebSocketRoute {
  // java: from is not ported, generated results already hold the impl object.

  private Consumer<WebSocketFrame> onPageMessage;
  private BiConsumer<Integer, String> onPageClose;
  private Consumer<WebSocketFrame> onServerMessage;
  private BiConsumer<Integer, String> onServerClose;
  private final WebSocketRoute server;
  private boolean connected = false;

  final WebSocketRouteChannel channel = new WebSocketRouteChannel(this);
  private final WebSocketRouteInitializer webSocketRouteInitializer;

  WebSocketRouteImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
    webSocketRouteInitializer = connection.protocolGson().fromJson(initializer, WebSocketRouteInitializer.class);

    this.server = new WebSocketRoute() {
      @Override
      public void onMessage(Consumer<WebSocketFrame> handler) {
        onServerMessage = handler;
      }

      @Override
      public void onClose(BiConsumer<Integer, String> handler) {
        onServerClose = handler;
      }

      @Override
      public WebSocketRoute connectToServer() {
        throw new PlaywrightException("connectToServer must be called on the page-side WebSocketRoute");
      }

      @Override
      public String url() {
        return webSocketRouteInitializer.url;
      }

      @Override
      public List<String> protocols() {
        return new ArrayList<>(webSocketRouteInitializer.protocols);
      }

      @Override
      public void close(CloseOptions options) {
        if (options == null) {
          options = new CloseOptions();
        }
        WebSocketRouteCloseServerParams params = toProtocol(options, WebSocketRouteCloseServerParams.class);
        params.wasClean = true;
        try {
          channel.closeServer(params, NO_TIMEOUT);
        } catch (PlaywrightException e) {
        }
      }

      // java: send(string | Buffer) is two overloads.
      @Override
      public void send(String message) {
        WebSocketRouteSendToServerParams params = new WebSocketRouteSendToServerParams();
        params.message = message;
        params.isBase64 = false;
        sendNoWait("sendToServer", params);
      }

      @Override
      public void send(byte[] message) {
        WebSocketRouteSendToServerParams params = new WebSocketRouteSendToServerParams();
        params.message = Base64.getEncoder().encodeToString(message);
        params.isBase64 = true;
        sendNoWait("sendToServer", params);
      }

      // java: [Symbol.asyncDispose] is not ported, Java has no async dispose.
    };
  }

  // java: upstream subscribes in the constructor with this._channel.on(...); the Java channel dispatches events here.
  @Override
  void handleEvent(String event, JsonObject params) {
    switch (event) {
      case "messageFromPage": {
        WebSocketRouteMessageFromPageEvent e = connection.protocolGson().fromJson(params, WebSocketRouteMessageFromPageEvent.class);
        if (onPageMessage != null) {
          onPageMessage.accept(new WebSocketFrameImpl(e.message, e.isBase64));
        } else if (connected) {
          WebSocketRouteSendToServerParams sendParams = new WebSocketRouteSendToServerParams();
          sendParams.message = e.message;
          sendParams.isBase64 = e.isBase64;
          sendNoWait("sendToServer", sendParams);
        }
        break;
      }
      case "messageFromServer": {
        WebSocketRouteMessageFromServerEvent e = connection.protocolGson().fromJson(params, WebSocketRouteMessageFromServerEvent.class);
        if (onServerMessage != null) {
          onServerMessage.accept(new WebSocketFrameImpl(e.message, e.isBase64));
        } else {
          WebSocketRouteSendToPageParams sendParams = new WebSocketRouteSendToPageParams();
          sendParams.message = e.message;
          sendParams.isBase64 = e.isBase64;
          sendNoWait("sendToPage", sendParams);
        }
        break;
      }
      case "closePage": {
        WebSocketRouteClosePageEvent e = connection.protocolGson().fromJson(params, WebSocketRouteClosePageEvent.class);
        if (onPageClose != null) {
          onPageClose.accept(e.code, e.reason);
        } else {
          WebSocketRouteCloseServerParams closeParams = new WebSocketRouteCloseServerParams();
          closeParams.code = e.code;
          closeParams.reason = e.reason;
          closeParams.wasClean = e.wasClean;
          sendNoWait("closeServer", closeParams);
        }
        break;
      }
      case "closeServer": {
        WebSocketRouteCloseServerEvent e = connection.protocolGson().fromJson(params, WebSocketRouteCloseServerEvent.class);
        if (onServerClose != null) {
          onServerClose.accept(e.code, e.reason);
        } else {
          WebSocketRouteClosePageParams closeParams = new WebSocketRouteClosePageParams();
          closeParams.code = e.code;
          closeParams.reason = e.reason;
          closeParams.wasClean = e.wasClean;
          sendNoWait("closePage", closeParams);
        }
        break;
      }
    }
  }

  @Override
  public String url() {
    return webSocketRouteInitializer.url;
  }

  @Override
  public List<String> protocols() {
    return new ArrayList<>(webSocketRouteInitializer.protocols);
  }

  @Override
  public void close(CloseOptions options) {
    if (options == null) {
      options = new CloseOptions();
    }
    WebSocketRouteClosePageParams params = toProtocol(options, WebSocketRouteClosePageParams.class);
    params.wasClean = true;
    try {
      channel.closePage(params, NO_TIMEOUT);
    } catch (PlaywrightException e) {
    }
  }

  @Override
  public WebSocketRoute connectToServer() {
    if (connected) {
      throw new PlaywrightException("Already connected to the server");
    }
    connected = true;
    sendNoWait("connect", new WebSocketRouteConnectParams());
    return server;
  }

  // java: send(string | Buffer) is two overloads.
  @Override
  public void send(String message) {
    WebSocketRouteSendToPageParams params = new WebSocketRouteSendToPageParams();
    params.message = message;
    params.isBase64 = false;
    sendNoWait("sendToPage", params);
  }

  @Override
  public void send(byte[] message) {
    WebSocketRouteSendToPageParams params = new WebSocketRouteSendToPageParams();
    params.message = Base64.getEncoder().encodeToString(message);
    params.isBase64 = true;
    sendNoWait("sendToPage", params);
  }

  @Override
  public void onMessage(Consumer<WebSocketFrame> handler) {
    onPageMessage = handler;
  }

  @Override
  public void onClose(BiConsumer<Integer, String> handler) {
    onPageClose = handler;
  }

  // java: [Symbol.asyncDispose] is not ported, Java has no async dispose.

  void afterHandle() {
    if (connected) {
      return;
    }
    // Ensure that websocket is "open" and can send messages without an actual server connection.
    // If this happens after the page has been closed, ignore the error.
    try {
      channel.ensureOpened(NO_TIMEOUT);
    } catch (PlaywrightException e) {
    }
  }

  // java: a channel call that is not awaited, `this._channel.foo(...).catch(() => {})` upstream.
  private void sendNoWait(String method, Object params) {
    try {
      sendMessageAsync(method, params);
    } catch (PlaywrightException e) {
    }
  }
}
