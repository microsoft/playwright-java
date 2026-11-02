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

// Port of packages/playwright-core/src/client/channelOwner.ts
package com.microsoft.playwright.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.playwright.PlaywrightException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ChannelOwner extends EventEmitter {
  final Connection connection;
  private ChannelOwner parent;
  private final Map<String, ChannelOwner> objects = new HashMap<>();

  final String type;
  final String guid;
  final JsonObject initializer;
  private Map<String, String> eventToSubscriptionMapping = new HashMap<>();
  boolean wasCollected;

  // java: a call without a timeout; the server does not enforce one.
  static Double NO_TIMEOUT = null;

  protected ChannelOwner(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    this(parent.connection, parent, type, guid, initializer);
  }

  // java: upstream has one constructor taking `parent: ChannelOwner | Connection`; this is the Connection case.
  protected ChannelOwner(Connection connection, String type, String guid) {
    this(connection, null, type, guid, new JsonObject());
  }

  private ChannelOwner(Connection connection, ChannelOwner parent, String type, String guid, JsonObject initializer) {
    this.connection = connection;
    this.type = type;
    this.guid = guid;
    this.parent = parent;

    this.connection.registerObject(guid, this);
    if (this.parent != null) {
      this.parent.objects.put(guid, this);
    }

    this.initializer = initializer;
  }

  void setEventToSubscriptionMapping(Map<String, String> mapping) {
    this.eventToSubscriptionMapping = mapping;
  }

  void updateSubscription(String event, boolean enabled) {
    String protocolEvent = eventToSubscriptionMapping.get(event);
    if (protocolEvent != null) {
      JsonObject params = new JsonObject();
      params.addProperty("event", protocolEvent);
      params.addProperty("enabled", enabled);
      sendMessageAsync("updateSubscription", params);
    }
  }

  // java: the listener is added first so that a null listener fails before anything is sent.
  @Override
  void on(String event, Consumer<?> listener) {
    addListener(event, listener);
  }

  @Override
  void addListener(String event, Consumer<?> listener) {
    super.addListener(event, listener);
    if (listenerCount(event) == 1) {
      updateSubscription(event, true);
    }
  }

  @Override
  void off(String event, Consumer<?> listener) {
    removeListener(event, listener);
  }

  @Override
  void removeListener(String event, Consumer<?> listener) {
    // java: nothing is sent when there was no listener to remove, upstream sends the disable message anyway.
    boolean hadListeners = listenerCount(event) > 0;
    super.removeListener(event, listener);
    if (hadListeners && listenerCount(event) == 0) {
      updateSubscription(event, false);
    }
  }

  void adopt(ChannelOwner child) {
    child.parent.objects.remove(child.guid);
    objects.put(child.guid, child);
    child.parent = this;
  }

  ChannelOwner parentOfType(String type) {
    ChannelOwner parent = this.parent;
    while (parent != null) {
      if (parent.type.equals(type)) {
        return parent;
      }
      parent = parent.parent;
    }
    return null;
  }

  void dispose(String reason) {
    // Clean up from parent and connection.
    if (parent != null) {
      parent.objects.remove(guid);
    }
    connection.unregisterObject(guid);
    wasCollected = "gc".equals(reason);

    // Dispose all children.
    for (ChannelOwner object : new ArrayList<>(objects.values())) {
      object.dispose(reason);
    }
    objects.clear();
  }

  // java: upstream's createChannel() wraps every channel call in wrapApiCall and sends the message
  // from there. This builds the options of that message; the zone of a call without wrapApiCall is
  // not installed, so that the calls of event handlers dispatched while the call waits are reported on their own.
  private Connection.CallOptions callOptions(Double timeout) {
    ApiZone apiZone = connection.apiZone;
    if (apiZone == null) {
      apiZone = new ApiZone(null, false);
    }
    Connection.CallOptions options = new Connection.CallOptions();
    options.timeout = timeout;
    if (!apiZone.internal && !apiZone.reported) {
      // Reporting/tracing/logging this api call for the first time.
      apiZone.reported = true;
      options.title = apiZone.title;
      options.frames = connection.captureLibraryStackTrace();
    } else {
      // Since this api call is either internal, or has already been reported/traced once,
      // passing as internal.
      options.internal = true;
    }
    return options;
  }

  <R> R wrapApiCall(Supplier<R> func, String title, boolean internal) {
    if (connection.apiZone != null) {
      return func.get();
    }
    // java: no stack trace is captured here, the first message of the call does it (see callOptions).
    ApiZone apiZone = new ApiZone(title, internal);
    connection.apiZone = apiZone;
    try {
      return func.get();
    } finally {
      connection.apiZone = null;
    }
  }

  // java: the Runnable overload for calls without a result.
  void wrapApiCall(Runnable func, String title, boolean internal) {
    wrapApiCall(() -> {
      func.run();
      return null;
    }, title, internal);
  }

  // java: the tracing title of a call, upstream passes it to @apiCall / _wrapApiCall.
  void withTitle(String title, Runnable code) {
    wrapApiCall(code, title, false);
  }

  <T> T withTitle(String title, Supplier<T> code) {
    return wrapApiCall(code, title, false);
  }

  // java: the sendMessage family is the typed channel proxy of upstream's createChannel().
  WaitableResult<JsonElement> sendMessageAsync(String method) {
    return sendMessageAsync(method, new JsonObject());
  }

  WaitableResult<JsonElement> sendMessageAsync(String method, JsonObject params) {
    return connection.sendMessageToServer(this, method, params, callOptions(null));
  }

  /** Typed counterpart of {@link #sendMessageAsync(String, JsonObject)}; {@code null} params mean an empty object. */
  WaitableResult<JsonElement> sendMessageAsync(String method, Object params) {
    return sendMessageAsync(method, paramsToJson(params));
  }

  /** Typed counterpart of {@link #sendMessageNoReply(String, JsonObject)}. */
  void sendMessageNoReply(String method, Object params) {
    sendMessageNoReply(method, paramsToJson(params));
  }

  private JsonObject paramsToJson(Object params) {
    if (params == null) {
      return new JsonObject();
    }
    JsonObject json = connection.protocolGson().toJsonTree(params).getAsJsonObject();
    json.remove("timeout");
    return json;
  }

  // Fire-and-forget: Connection.sendMessageToServer registers no callback for __waitInfo__, the only caller,
  // and drops it silently when the object was collected.
  void sendMessageNoReply(String method, JsonObject params) {
    connection.sendMessageToServer(this, method, params, callOptions(null));
  }

  JsonElement sendMessage(String method) {
    return sendMessage(method, new JsonObject(), NO_TIMEOUT);
  }

  JsonElement sendMessage(String method, JsonObject params, Double timeout) {
    if (timeout != null) {
      // Timeout is passed in the message metadata, remove potential leftover from serialized options.
      params.remove("timeout");
    } else if (params.has("timeout")) {
      throw new PlaywrightException("Internal error: timeout must be passed explicitly.");
    }
    return connection.sendMessage(this, method, params, callOptions(timeout));
  }

  /**
   * Typed counterpart of {@link #sendMessage(String, JsonObject, Double)}: {@code params} is serialized
   * with the connection's Gson (channel owners travel as {@code {guid}}), the reply is deserialized into
   * {@code resultClass}. {@code null} params mean an empty object, {@code null} or {@code Void.class}
   * result class discards the reply.
   */
  public <R> R sendMessage(String method, Object params, Class<R> resultClass, Double timeout) {
    JsonObject paramsJson = params == null ? new JsonObject() : connection.protocolGson().toJsonTree(params).getAsJsonObject();
    JsonElement result = sendMessage(method, paramsJson, timeout);
    if (resultClass == null || resultClass == Void.class || result == null || result.isJsonNull()) {
      return null;
    }
    return connection.protocolGson().fromJson(result, resultClass);
  }

  public void sendMessage(String method, Object params, Double timeout) {
    sendMessage(method, params, Void.class, timeout);
  }

  // java: pumps the connection until the waitable is done; upstream awaits promises.
  <T> T runUntil(Runnable code, Waitable<T> waitable) {
    try {
      code.run();
      while (!waitable.isDone()) {
        connection.processOneMessage();
      }
      return waitable.get();
    } finally {
      waitable.dispose();
    }
  }

  // java: upstream's Connection.dispatch emits the event on the channel; subclasses override this until EventEmitter is ported.
  void handleEvent(String event, JsonObject parameters) {
  }

  // java: upstream's tChannelImplToWire.
  JsonObject toProtocolRef() {
    JsonObject json = new JsonObject();
    json.addProperty("guid", guid);
    return json;
  }
}

class ApiZone {
  String title;
  final boolean internal;
  boolean reported;

  ApiZone(String title, boolean internal) {
    this.title = title;
    this.internal = internal;
  }
}
