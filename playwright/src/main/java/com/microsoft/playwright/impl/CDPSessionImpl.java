package com.microsoft.playwright.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.playwright.CDPSession;

import java.util.HashMap;
import java.util.function.Consumer;

public class CDPSessionImpl extends ChannelOwner implements CDPSession {
  private final ListenerCollection<String> listeners = new ListenerCollection<>(new HashMap<>(), this);

  protected CDPSessionImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
  }

  @Override
  void handleEvent(String event, JsonObject parameters) {
    super.handleEvent(event, parameters);
    if ("event".equals(event)) {
      String method = parameters.get("method").getAsString();
      JsonObject params = parameters.get("params").getAsJsonObject();
      listeners.notify(method, params);
    }
  }

  public JsonObject send(String method) {
    return send(method, null);
  }

  public JsonObject send(String method, JsonObject params) {
    JsonObject args = new JsonObject();
    if (params != null) {
      args.add("params", params);
    }
    args.addProperty("method", method);
    JsonElement response = connection.sendMessage(guid, "send", args);
    if (response == null) return null;
    else return response.getAsJsonObject().get("result").getAsJsonObject();
  }

  @Override
  public void on(String event, Consumer<JsonObject> handler) {
    listeners.add(event, handler);
  }

  @Override
  public void off(String event, Consumer<JsonObject> handler) {
    listeners.remove(event, handler);
  }

  @Override
  public void detach() {
    sendMessage("detach");
  }
}
