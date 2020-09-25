/**
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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

class Message {
  int id;
  String guid;
  String method;
  JsonObject params;
  JsonElement result;
  JsonObject error;

  @Override
  public String toString() {
    return "Message{" +
      "id='" + id + '\'' +
      ", guid='" + guid + '\'' +
      ", method='" + method + '\'' +
      ", params=" + (params == null ? null : "<...>") +
      ", result='" + result + '\'' +
      ", error='" + error + '\'' +
      '}';
  }
}


public class Connection {
  private final Transport transport;
  private final Map<String, ChannelOwner> objects = new HashMap();
  private final Root root;
  private int lastId = 0;
  private final Map<Integer, CompletableFuture<Message>> callbacks = new HashMap();

  class Root extends ChannelOwner {
    Root(Connection connection) {
      super(connection, "", "");
    }
  }

  public Connection(InputStream in, OutputStream out) {
    transport = new Transport(in, out);
    root = new Root(this);
  }

  public JsonElement sendMessage(String guid, String method, JsonObject params) {
    CompletableFuture<Message> result = internalSendMessage(guid, method, params);
    while (!result.isDone()) {
      processOneMessage();
    }
    try {
      return result.get().result;
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  public void sendMessageNoWait(String guid, String method, JsonObject params) {
    internalSendMessage(guid, method, params);
  }

  private CompletableFuture<Message> internalSendMessage(String guid, String method, JsonObject params) {
    int id = ++lastId;
    CompletableFuture<Message> result = new CompletableFuture();
    callbacks.put(id, result);
    JsonObject message = new JsonObject();
    message.addProperty("id", id);
    message.addProperty("guid", guid);
    message.addProperty("method", method);
    message.add("params", params);
    transport.send(new Gson().toJson(message));
    return result;
  }


  public ChannelOwner waitForObjectWithKnownName(String guid) {
    while (!objects.containsKey(guid)) {
      processOneMessage();
    }
    return objects.get(guid);
  }

  public <T> T getExistingObject(String guid) {
    T result = (T) objects.get(guid);
    if (result == null)
      throw new RuntimeException("Object doesn't exist: " + guid);
    return result;
  }

  void registerObject(String guid, ChannelOwner object) {
    objects.put(guid, object);
  }

  void unregisterObject(String guid, ChannelOwner object) {
    objects.remove(guid);
  }

  void processOneMessage() {
    String messageString = transport.read();
    Gson gson = new Gson();
    Message message = gson.fromJson(messageString, Message.class);
    dispatch(message);
  }

  private void dispatch(Message message) {
//    System.out.println("Message: " + message.method + " " + message.id);
    if (message.id != 0) {
      CompletableFuture<Message> callback = callbacks.get(message.id);
      if (callback == null) {
        throw new RuntimeException("Cannot find command to respond: " + message.id);
      }
      callbacks.remove(message.id);
//      System.out.println("Message: " + message.id + " " + message);
      if (message.error == null)
        callback.complete(message);
      else
        callback.completeExceptionally(new RuntimeException(message.error.toString()));
      return;
    }

    // TODO: throw?
    if (message.method == null)
      return;
    if (message.method.equals("__create__")) {
      createRemoteObject(message.guid, message.params);
      return;
    }
    if (message.method.equals("__dispose__")) {
      ChannelOwner object = objects.get(message.guid);
      if (object == null)
        throw new RuntimeException("Cannot find object to dispose: " + message.guid);
      object.dispose();
      return;
    }
    ChannelOwner object = objects.get(message.guid);
    if (object == null)
      throw new RuntimeException("Cannot find object to call " + message.method + ": " + message.guid);
//    object._channel.emit(message.method, this._replaceGuidsWithChannels(message.params));
    object.onEvent(message.method, message.params);
  }

  private ChannelOwner createRemoteObject(String parentGuid, JsonObject params) {
    String type = params.get("type").getAsString();
    String guid = params.get("guid").getAsString();

    ChannelOwner parent = objects.get(parentGuid);
    if (parent == null)
      throw new RuntimeException("Cannot find parent object " + parentGuid + " to create " + guid);
    JsonObject initializer = params.getAsJsonObject("initializer");
    ChannelOwner result = null;
//    initializer = this._replaceGuidsWithChannels(initializer);
    switch (type) {
      case "BrowserType":
        result = new BrowserType(parent, type, guid, initializer);
        break;
      case "Browser":
        result = new Browser(parent, type, guid, initializer);
        break;
      case "BrowserContext":
        result = new BrowserContext(parent, type, guid, initializer);
        break;
      case "ConsoleMessage":
        result = new ConsoleMessage(parent, type, guid, initializer);
        break;
      case "Dialog":
        result = new Dialog(parent, type, guid, initializer);
        break;
      case "Download":
        result = new Download(parent, type, guid, initializer);
        break;
      case "Electron":
//        result = new Playwright(parent, type, guid, initializer);
        break;
      case "Frame":
        result = new Frame(parent, type, guid, initializer);
        break;
      case "JSHandle":
//        result = new JSHandle(parent, type, guid, initializer);
        break;
      case "Page":
        result = new Page(parent, type, guid, initializer);
        break;
      case "Playwright":
        result = new Playwright(parent, type, guid, initializer);
        break;
      case "Request":
        result = new Request(parent, type, guid, initializer);
        break;
      case "Response":
        result = new Response(parent, type, guid, initializer);
        break;
      case "Selectors":
//        result = new Playwright(parent, type, guid, initializer);
        break;
      default:
        throw new RuntimeException("Unknown type " + type);
    }

    return result;
  }
}
