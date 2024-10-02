package com.microsoft.playwright.impl;

import com.google.gson.JsonObject;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.WebSocketFrame;
import com.microsoft.playwright.WebSocketRoute;

import java.util.Base64;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.microsoft.playwright.impl.Serialization.gson;

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
      JsonObject params = gson().toJsonTree(options).getAsJsonObject();
      params.addProperty("wasClean", true);
      sendMessageAsync("closeServer", params);
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
      JsonObject params = new JsonObject();
      params.addProperty("message", message);
      params.addProperty("isBase64", false);
      sendMessageAsync("sendToServer", params);
    }

    @Override
    public void send(byte[] message) {
      JsonObject params = new JsonObject();
      String base64 = Base64.getEncoder().encodeToString(message);
      params.addProperty("message", base64);
      params.addProperty("isBase64", true);
      sendMessageAsync("sendToServer", params);
    }

    @Override
    public String url() {
      return initializer.get("url").getAsString();
    }
  };

  WebSocketRouteImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
  }

    @Override
  public void close(CloseOptions options) {
      if (options == null) {
        options = new CloseOptions();
      }
      JsonObject params = gson().toJsonTree(options).getAsJsonObject();
      params.addProperty("wasClean", true);
      sendMessageAsync("closePage", params);
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
    JsonObject params = new JsonObject();
    params.addProperty("message", message);
    params.addProperty("isBase64", false);
    sendMessageAsync("sendToPage", params);
  }

  @Override
  public void send(byte[] message) {
    JsonObject params = new JsonObject();
    String base64 = Base64.getEncoder().encodeToString(message);
    params.addProperty("message", base64);
    params.addProperty("isBase64", true);
    sendMessageAsync("sendToPage", params);
  }

  @Override
  public String url() {
    return initializer.get("url").getAsString();
  }

  void afterHandle() {
    if (this.connected) {
      return;
    }
    // Ensure that websocket is "open" and can send messages without an actual server connection.
    sendMessageAsync("ensureOpened");
  }

  @Override
  protected void handleEvent(String event, JsonObject params) {
    if ("messageFromPage".equals(event)) {
      String message = params.get("message").getAsString();
      boolean isBase64 = params.get("isBase64").getAsBoolean();
      if (onPageMessage != null) {
        onPageMessage.accept(new WebSocketFrameImpl(message, isBase64));
      } else if (connected) {
        JsonObject messageParams = new JsonObject();
        messageParams.addProperty("message", message);
        messageParams.addProperty("isBase64", isBase64);
        sendMessageAsync("sendToServer", messageParams);
      }
    } else if ("messageFromServer".equals(event)) {
      String message = params.get("message").getAsString();
      boolean isBase64 = params.get("isBase64").getAsBoolean();
      if (onServerMessage != null) {
        onServerMessage.accept(new WebSocketFrameImpl(message, isBase64));
      } else {
        JsonObject messageParams = new JsonObject();
        messageParams.addProperty("message", message);
        messageParams.addProperty("isBase64", isBase64);
        sendMessageAsync("sendToPage", messageParams);
      }
    } else if ("closePage".equals(event)) {
      int code = params.get("code").getAsInt();
      String reason = params.get("reason").getAsString();
      boolean wasClean = params.get("wasClean").getAsBoolean();
      if (onPageClose != null) {
        onPageClose.accept(code, reason);
      } else {
        JsonObject closeParams = new JsonObject();
        closeParams.addProperty("code", code);
        closeParams.addProperty("reason", reason);
        closeParams.addProperty("wasClean", wasClean);
        sendMessageAsync("closeServer", closeParams);
      }
    } else if ("closeServer".equals(event)) {
      int code = params.get("code").getAsInt();
      String reason = params.get("reason").getAsString();
      boolean wasClean = params.get("wasClean").getAsBoolean();
      if (onServerClose != null) {
        onServerClose.accept(code, reason);
      } else {
        JsonObject closeParams = new JsonObject();
        closeParams.addProperty("code", code);
        closeParams.addProperty("reason", reason);
        closeParams.addProperty("wasClean", wasClean);
        sendMessageAsync("closePage", closeParams);
      }
    }
  }
}
