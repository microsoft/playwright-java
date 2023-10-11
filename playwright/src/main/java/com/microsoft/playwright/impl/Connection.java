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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.TimeoutError;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static com.microsoft.playwright.impl.Serialization.gson;
import static java.lang.System.currentTimeMillis;

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
  final boolean isRemote;
  private int lastId = 0;
  private final StackTraceCollector stackTraceCollector;
  private final Map<Integer, WaitableResult<JsonElement>> callbacks = new HashMap<>();
  private String apiName;
  private static final boolean isLogging;
  static {
    String debug = System.getenv("DEBUG");
    isLogging = (debug != null) && debug.contains("pw:channel");
  }
  LocalUtils localUtils;
  final Map<String, String> env;
  private int tracingCount;

  class Root extends ChannelOwner {
    Root(Connection connection) {
      super(connection, "Root", "");
    }

    Playwright initialize() {
      JsonObject params = new JsonObject();
      params.addProperty("sdkLanguage", "java");
      JsonElement result = sendMessage("initialize", params.getAsJsonObject());
      return this.connection.getExistingObject(result.getAsJsonObject().getAsJsonObject("playwright").get("guid").getAsString());
    }
  }

  Connection(Transport pipe, Map<String, String> env, LocalUtils localUtils) {
    this(pipe, env, true);
    this.localUtils = localUtils;
  }

  Connection(Transport transport, Map<String, String> env) {
    this(transport, env, false);
  }

  private Connection(Transport transport, Map<String, String> env, boolean isRemote) {
    this.env = env;
    this.isRemote = isRemote;
    if (isLogging) {
      transport = new TransportLogger(transport);
    }
    this.transport = transport;
    root = new Root(this);
    stackTraceCollector = StackTraceCollector.createFromEnv(env);
  }

  void setIsTracing(boolean tracing) {
    if (tracing) {
      ++tracingCount;
    } else {
      --tracingCount;
    }
  }

  String setApiName(String name) {
    String previous = apiName;
    apiName = name;
    return previous;
  }

  void close() throws IOException {
    transport.close();
  }

  public JsonElement sendMessage(String guid, String method, JsonObject params) {
    return root.runUntil(() -> {}, sendMessageAsync(guid, method, params));
  }

  public WaitableResult<JsonElement> sendMessageAsync(String guid, String method, JsonObject params) {
    return internalSendMessage(guid, method, params, true);
  }

  private WaitableResult<JsonElement> internalSendMessage(String guid, String method, JsonObject params, boolean sendStack) {
    int id = ++lastId;
    WaitableResult<JsonElement> result = new WaitableResult<>();
    callbacks.put(id, result);
    JsonObject message = new JsonObject();
    message.addProperty("id", id);
    message.addProperty("guid", guid);
    message.addProperty("method", method);
    message.add("params", params);
    JsonObject metadata = new JsonObject();
    metadata.addProperty("wallTime", currentTimeMillis());
    JsonArray stack = null;
    if (apiName == null) {
      metadata.addProperty("internal", true);
    } else {
      metadata.addProperty("apiName", apiName);
      // All but first message in an API call are considered internal and will be hidden from the inspector.
      apiName = null;
      if (stackTraceCollector != null) {
        stack = stackTraceCollector.currentStackTrace();
        if (!stack.isEmpty()) {
          JsonObject location = new JsonObject();
          JsonObject frame = stack.get(0).getAsJsonObject();
          location.addProperty("file", frame.get("file").getAsString());
          location.addProperty("line", frame.get("line").getAsInt());
          location.addProperty("column", frame.get("column").getAsInt());
          metadata.add("location", location);
        }
      }
    }
    message.add("metadata", metadata);
    transport.send(message);
    if (sendStack && tracingCount > 0 && stack != null && !method.startsWith("LocalUtils")) {
      JsonObject callData = new JsonObject();
      callData.addProperty("id", id);
      callData.add("stack", stack);
      JsonObject stackParams = new JsonObject();
      stackParams.add("callData", callData);
      internalSendMessage(localUtils.guid,"addStackToTracingNoReply", stackParams, false);
    }
    return result;
  }

  public PlaywrightImpl initializePlaywright() {
    return (PlaywrightImpl) this.root.initialize();
  }

  LocalUtils localUtils() {
    return localUtils;
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
    JsonObject message = transport.poll(Duration.ofMillis(10));
    if (message == null) {
      return;
    }
    Gson gson = gson();
    Message messageObj = gson.fromJson(message, Message.class);
    dispatch(messageObj);
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
        if (message.error.error == null) {
          callback.completeExceptionally(new PlaywrightException(message.error.toString()));
        } else if ("TimeoutError".equals(message.error.error.name)) {
          callback.completeExceptionally(new TimeoutError(message.error.error.toString()));
        } else {
          callback.completeExceptionally(new DriverException(message.error.error));
        }
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

    ChannelOwner object = objects.get(message.guid);
    if (object == null) {
      throw new PlaywrightException("Cannot find object to call " + message.method + ": " + message.guid);
    }
    if (message.method.equals("__adopt__")) {
      String childGuid = message.params.get("guid").getAsString();
      ChannelOwner child = objects.get(childGuid);
      if (child == null) {
        throw new PlaywrightException("Unknown new child:  " + childGuid);
      }
      object.adopt(child);
      return;
    }
    if (message.method.equals("__dispose__")) {
      boolean wasCollected = message.params.has("reason") && "gc".equals(message.params.get("reason").getAsString());
      object.disposeChannelOwner(wasCollected);
      return;
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
      case "Android":
//        result = new Android(parent, type, guid, initializer);
        break;
      case "AndroidSocket":
//        result = new AndroidSocket(parent, type, guid, initializer);
        break;
      case "AndroidDevice":
//        result = new AndroidDevice(parent, type, guid, initializer);
        break;
      case "Artifact":
        result = new ArtifactImpl(parent, type, guid, initializer);
        break;
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
      case "Dialog":
        result = new DialogImpl(parent, type, guid, initializer);
        break;
      case "Electron":
//        result = new Playwright(parent, type, guid, initializer);
        break;
      case "ElementHandle":
        result = new ElementHandleImpl(parent, type, guid, initializer);
        break;
      case "APIRequestContext":
        // Create fake object as this API is experimental an only exposed in Node.js.
        result = new APIRequestContextImpl(parent, type, guid, initializer);
        break;
      case "Frame":
        result = new FrameImpl(parent, type, guid, initializer);
        break;
      case "JSHandle":
        result = new JSHandleImpl(parent, type, guid, initializer);
        break;
      case "JsonPipe":
        result = new JsonPipe(parent, type, guid, initializer);
        break;
      case "LocalUtils":
        result = new LocalUtils(parent, type, guid, initializer);
        if (localUtils == null) {
          localUtils = (LocalUtils) result;
        }
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
        result = new SelectorsImpl(parent, type, guid, initializer);
        break;
      case "SocksSupport":
        break;
      case "Tracing":
        result = new TracingImpl(parent, type, guid, initializer);
        break;
      case "WebSocket":
        result = new WebSocketImpl(parent, type, guid, initializer);
        break;
      case "Worker":
        result = new WorkerImpl(parent, type, guid, initializer);
        break;
      case "WritableStream":
        result = new WritableStream(parent, type, guid, initializer);
        break;
      case "CDPSession":
        result = new CDPSessionImpl(parent, type, guid, initializer);
        break;
      default:
        throw new PlaywrightException("Unknown type " + type);
    }

    return result;
  }
}
