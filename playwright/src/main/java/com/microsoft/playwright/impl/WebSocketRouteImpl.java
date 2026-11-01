package com.microsoft.playwright.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.WebSocketFrame;
import com.microsoft.playwright.WebSocketRoute;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.microsoft.playwright.impl.Utils.toProtocol;

class WebSocketRouteImpl extends ChannelOwner implements WebSocketRoute {

  private Consumer<WebSocketFrame> onPageMessage;
  private BiConsumer<Integer, String> onPageClose;
  private Consumer<WebSocketFrame> onServerMessage;
  private BiConsumer<Integer, String> onServerClose;
  private boolean connected;
  private WebSocketRoute server = new WebSocketRoute() {
    @Override
    public void close(CloseOptions options) {
      if (options == null) {
        options = new CloseOptions();
      }
      WebSocketRouteCloseServerParams params = toProtocol(options, WebSocketRouteCloseServerParams.class);
      params.wasClean = true;
      sendMessageAsync("closeServer", toJson(params));
    }

    @Override
    public WebSocketRoute connectToServer() {
      throw new PlaywrightException("connectToServer must be called on the page-side WebSocketRoute");
    }

    @Override
    public void onClose(BiConsumer<Integer, String> handler) {
      onServerClose = handler;
    }

    @Override
    public void onMessage(Consumer<WebSocketFrame> handler) {
      onServerMessage = handler;
    }

    @Override
    public void send(String message) {
      WebSocketRouteSendToServerParams params = new WebSocketRouteSendToServerParams();
      params.message = message;
      params.isBase64 = false;
      sendMessageAsync("sendToServer", toJson(params));
    }

    @Override
    public void send(byte[] message) {
      WebSocketRouteSendToServerParams params = new WebSocketRouteSendToServerParams();
      params.message = Base64.getEncoder().encodeToString(message);
      params.isBase64 = true;
      sendMessageAsync("sendToServer", toJson(params));
    }

    @Override
    public String url() {
      return initializer.get("url").getAsString();
    }

    @Override
    public List<String> protocols() {
      return readProtocols();
    }
  };

  final WebSocketRouteChannel channel = new WebSocketRouteChannel(this);

  WebSocketRouteImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
  }

    @Override
  public void close(CloseOptions options) {
      if (options == null) {
        options = new CloseOptions();
      }
      WebSocketRouteClosePageParams params = toProtocol(options, WebSocketRouteClosePageParams.class);
      params.wasClean = true;
      sendMessageAsync("closePage", toJson(params));
  }

  @Override
  public WebSocketRoute connectToServer() {
    if (connected) {
      throw new PlaywrightException("Already connected to the server");
    }
    connected = true;
    sendMessageAsync("connect");
    return server;
  }

  @Override
  public void onClose(BiConsumer<Integer, String> handler) {
    onPageClose = handler;
  }

  @Override
  public void onMessage(Consumer<WebSocketFrame> handler) {
    onPageMessage = handler;
  }

  @Override
  public void send(String message) {
    WebSocketRouteSendToPageParams params = new WebSocketRouteSendToPageParams();
    params.message = message;
    params.isBase64 = false;
    sendMessageAsync("sendToPage", toJson(params));
  }

  @Override
  public void send(byte[] message) {
    WebSocketRouteSendToPageParams params = new WebSocketRouteSendToPageParams();
    params.message = Base64.getEncoder().encodeToString(message);
    params.isBase64 = true;
    sendMessageAsync("sendToPage", toJson(params));
  }

  @Override
  public String url() {
    return initializer.get("url").getAsString();
  }

  @Override
  public List<String> protocols() {
    return readProtocols();
  }

  private List<String> readProtocols() {
    List<String> result = new ArrayList<>();
    if (!initializer.has("protocols")) {
      return result;
    }
    for (JsonElement element : initializer.getAsJsonArray("protocols")) {
      result.add(element.getAsString());
    }
    return result;
  }

  private JsonObject toJson(Object params) {
    return connection.protocolGson().toJsonTree(params).getAsJsonObject();
  }

  void afterHandle() {
    if (this.connected) {
      return;
    }
    // Ensure that websocket is "open" and can send messages without an actual server connection.
    try {
      sendMessageAsync("ensureOpened");
    } catch (PlaywrightException e) {
      // If this happens after the page has been closed, ignore the error.
    }
  }

  @Override
  protected void handleEvent(String event, JsonObject params) {
    if ("messageFromPage".equals(event)) {
      String message = params.get("message").getAsString();
      boolean isBase64 = params.get("isBase64").getAsBoolean();
      if (onPageMessage != null) {
        onPageMessage.accept(new WebSocketFrameImpl(message, isBase64));
      } else if (connected) {
        WebSocketRouteSendToServerParams messageParams = new WebSocketRouteSendToServerParams();
        messageParams.message = message;
        messageParams.isBase64 = isBase64;
        sendMessageAsync("sendToServer", toJson(messageParams));
      }
    } else if ("messageFromServer".equals(event)) {
      String message = params.get("message").getAsString();
      boolean isBase64 = params.get("isBase64").getAsBoolean();
      if (onServerMessage != null) {
        onServerMessage.accept(new WebSocketFrameImpl(message, isBase64));
      } else {
        WebSocketRouteSendToPageParams messageParams = new WebSocketRouteSendToPageParams();
        messageParams.message = message;
        messageParams.isBase64 = isBase64;
        sendMessageAsync("sendToPage", toJson(messageParams));
      }
    } else if ("closePage".equals(event)) {
      Integer code = params.has("code") ? params.get("code").getAsInt() : null;
      String reason = params.has("reason") ? params.get("reason").getAsString() : null;
      boolean wasClean = params.has("wasClean") && params.get("wasClean").getAsBoolean();
      if (onPageClose != null) {
        onPageClose.accept(code, reason);
      } else {
        WebSocketRouteCloseServerParams closeParams = new WebSocketRouteCloseServerParams();
        closeParams.code = code;
        closeParams.reason = reason;
        closeParams.wasClean = wasClean;
        sendMessageAsync("closeServer", toJson(closeParams));
      }
    } else if ("closeServer".equals(event)) {
      Integer code = params.has("code") ? params.get("code").getAsInt() : null;
      String reason = params.has("reason") ? params.get("reason").getAsString() : null;
      boolean wasClean = params.has("wasClean") && params.get("wasClean").getAsBoolean();
      if (onServerClose != null) {
        onServerClose.accept(code, reason);
      } else {
        WebSocketRouteClosePageParams closeParams = new WebSocketRouteClosePageParams();
        closeParams.code = code;
        closeParams.reason = reason;
        closeParams.wasClean = wasClean;
        sendMessageAsync("closePage", toJson(closeParams));
      }
    }
  }
}
