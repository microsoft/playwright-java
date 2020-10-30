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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.playwright.PlaywrightException;

import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static com.microsoft.playwright.impl.Serialization.gson;

class Message {
  int id;
  String guid;
  String method;
  JsonObject params;
  JsonElement result;
  SerializedError error;

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
  private final Map<String, ChannelOwner> objects = new HashMap<>();
  private final Root root;
  private int lastId = 0;
  private final Map<Integer, WaitableResult<JsonElement>> callbacks = new HashMap<>();

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
    return (JsonElement) root.toDeferred(sendMessageAsync(guid, method, params)).get();
  }

  public WaitableResult<JsonElement> sendMessageAsync(String guid, String method, JsonObject params) {
    return internalSendMessage(guid, method, params);
  }

  public void sendMessageNoWait(String guid, String method, JsonObject params) {
    internalSendMessage(guid, method, params);
  }

  private WaitableResult<JsonElement> internalSendMessage(String guid, String method, JsonObject params) {
    int id = ++lastId;
    WaitableResult<JsonElement> result = new WaitableResult<>();
    callbacks.put(id, result);
    JsonObject message = new JsonObject();
    message.addProperty("id", id);
    message.addProperty("guid", guid);
    message.addProperty("method", method);
    message.add("params", params);
    transport.send(gson().toJson(message));
    return result;
  }

  public ChannelOwner waitForObjectWithKnownName(String guid) {
    while (!objects.containsKey(guid)) {
      processOneMessage();
    }
    return objects.get(guid);
  }

  public <T> T getExistingObject(String guid) {
    @SuppressWarnings("unchecked") T result = (T) objects.get(guid);
    if (result == null)
      throw new PlaywrightException("Object doesn't exist: " + guid);
    return result;
  }

  void registerObject(String guid, ChannelOwner object) {
    objects.put(guid, object);
  }

  void unregisterObject(String guid) {
    objects.remove(guid);
  }

  void processOneMessage() {
    String messageString = transport.poll(Duration.ofMillis(10));
    if (messageString == null) {
      return;
    }
    Gson gson = gson();
    Message message = gson.fromJson(messageString, Message.class);
    dispatch(message);
  }

  private void dispatch(Message message) {
//    System.out.println("Message: " + message.method + " " + message.id);
    if (message.id != 0) {
      WaitableResult<JsonElement> callback = callbacks.get(message.id);
      if (callback == null) {
        throw new PlaywrightException("Cannot find command to respond: " + message.id);
      }
      callbacks.remove(message.id);
//      System.out.println("Message: " + message.id + " " + message);
      if (message.error == null) {
        callback.complete(message.result);
      } else {
        callback.completeExceptionally(new PlaywrightException(message.error.toString()));
      }
      return;
    }

    // TODO: throw?
    if (message.method == null) {
      return;
    }
    if (message.method.equals("__create__")) {
      createRemoteObject(message.guid, message.params);
      return;
    }
    if (message.method.equals("__dispose__")) {
      ChannelOwner object = objects.get(message.guid);
      if (object == null) {
        throw new PlaywrightException("Cannot find object to dispose: " + message.guid);
      }
      object.disconnect();
      return;
    }
    ChannelOwner object = objects.get(message.guid);
    if (object == null) {
      throw new PlaywrightException("Cannot find object to call " + message.method + ": " + message.guid);
    }
    object.handleEvent(message.method, message.params);
  }

  private ChannelOwner createRemoteObject(String parentGuid, JsonObject params) {
    String type = params.get("type").getAsString();
    String guid = params.get("guid").getAsString();

    ChannelOwner parent = objects.get(parentGuid);
    if (parent == null) {
      throw new PlaywrightException("Cannot find parent object " + parentGuid + " to create " + guid);
    }
    JsonObject initializer = params.getAsJsonObject("initializer");
    ChannelOwner result = null;
    switch (type) {
      case "BindingCall":
        result = new BindingCall(parent, type, guid, initializer);
        break;
      case "BrowserType":
        result = new BrowserTypeImpl(parent, type, guid, initializer);
        break;
      case "Browser":
        result = new BrowserImpl(parent, type, guid, initializer);
        break;
      case "BrowserContext":
        result = new BrowserContextImpl(parent, type, guid, initializer);
        break;
      case "ConsoleMessage":
        result = new ConsoleMessageImpl(parent, type, guid, initializer);
        break;
      case "Dialog":
        result = new DialogImpl(parent, type, guid, initializer);
        break;
      case "Download":
        result = new DownloadImpl(parent, type, guid, initializer);
        break;
      case "Electron":
//        result = new Playwright(parent, type, guid, initializer);
        break;
      case "ElementHandle":
        result = new ElementHandleImpl(parent, type, guid, initializer);
        break;
      case "Frame":
        result = new FrameImpl(parent, type, guid, initializer);
        break;
      case "JSHandle":
        result = new JSHandleImpl(parent, type, guid, initializer);
        break;
      case "Page":
        result = new PageImpl(parent, type, guid, initializer);
        break;
      case "Playwright":
        result = new PlaywrightImpl(parent, type, guid, initializer);
        break;
      case "Request":
        result = new RequestImpl(parent, type, guid, initializer);
        break;
      case "Response":
        result = new ResponseImpl(parent, type, guid, initializer);
        break;
      case "Route":
        result = new RouteImpl(parent, type, guid, initializer);
        break;
      case "Stream":
        result = new Stream(parent, type, guid, initializer);
        break;
      case "Selectors":
//        result = new Playwright(parent, type, guid, initializer);
        break;
      case "Worker":
        result = new WorkerImpl(parent, type, guid, initializer);
        break;
      default:
        throw new PlaywrightException("Unknown type " + type);
    }

    return result;
  }
}
