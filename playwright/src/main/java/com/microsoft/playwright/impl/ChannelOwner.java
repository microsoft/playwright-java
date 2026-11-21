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

// Port of channelOwner.ts.
package com.microsoft.playwright.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.playwright.PlaywrightException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ChannelOwner extends EventEmitter {
  final Connection connection;
  private ChannelOwner parent;
  private final Map<String, ChannelOwner> objects = new LinkedHashMap<>();

  final String type;
  final String guid;
  // java: _channel is the generated <Name>Channel field of each subclass.
  final JsonObject initializer;
  // java: the Java API has no Logger, the field keeps upstream's Logger | undefined as an opaque Object.
  Object logger;
  final ClientInstrumentation instrumentation;
  private Map<String, String> eventToSubscriptionMapping = new HashMap<>();
  boolean wasCollected;

  protected ChannelOwner(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    this(parent.connection, parent, type, guid, initializer);
  }

  // java: upstream has one constructor taking `parent: ChannelOwner | Connection`; this is the Connection case.
  protected ChannelOwner(Connection connection, String type, String guid) {
    this(connection, null, type, guid, new JsonObject());
  }

  private ChannelOwner(Connection connection, ChannelOwner parent, String type, String guid, JsonObject initializer) {
    setMaxListeners(0);
    this.connection = connection;
    this.type = type;
    this.guid = guid;
    this.parent = parent;
    this.instrumentation = this.connection.instrumentation;

    this.connection.registerObject(guid, this);
    if (this.parent != null) {
      this.parent.objects.put(guid, this);
      this.logger = this.parent.logger;
    }

    // java: _channel = _createChannel(new EventEmitter()) is the channel field of the subclass.
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
      try {
        sendMessageAsync("updateSubscription", params);
      } catch (PlaywrightException e) {
        // java: .catch(() => {}) upstream, the send throws synchronously when the connection is closed.
      }
    }
  }

  // java: the listener is added first so that a null listener fails before anything is sent.
  @Override
  void on(String event, Consumer<?> listener) {
    super.on(event, listener);
    if (listenerCount(event) == 1) {
      updateSubscription(event, true);
    }
  }

  @Override
  void addListener(String event, Consumer<?> listener) {
    super.addListener(event, listener);
    if (listenerCount(event) == 1) {
      updateSubscription(event, true);
    }
  }

  @Override
  void prependListener(String event, Consumer<?> listener) {
    super.prependListener(event, listener);
    if (listenerCount(event) == 1) {
      updateSubscription(event, true);
    }
  }

  // java: super.off calls removeListener below, which updates the subscription; upstream updates it a second time here.
  @Override
  void off(String event, Consumer<?> listener) {
    super.off(event, listener);
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

  JsonObject debugScopeState() {
    JsonObject result = new JsonObject();
    result.addProperty("_guid", guid);
    JsonArray objects = new JsonArray();
    for (ChannelOwner o : this.objects.values()) {
      objects.add(o.debugScopeState());
    }
    result.add("objects", objects);
    return result;
  }

  // java: _validatorToWireContext is not ported, there are no protocol validators; Connection.protocolGson()
  // serializes the params (channel owners as {guid}, binary as base64).

  // java: upstream's createChannel() wraps every channel call in wrapApiCall and sends the message
  // from there; the sendMessage family at the bottom sends it, and this builds the options of that message.
  // The zone of a call without wrapApiCall is not installed, so that the calls of event handlers dispatched
  // while the call waits are reported on their own. The metainfo `internal` flag of a method has no Java counterpart.
  private Connection.CallOptions callOptions(String method, JsonObject params, Double timeout) {
    ApiZone apiZone = connection.apiZone;
    if (apiZone == null) {
      apiZone = new ApiZone(null, null, false);
    }
    Connection.CallOptions options = new Connection.CallOptions();
    options.timeout = timeout;
    if (!apiZone.internal && !apiZone.reported) {
      // Reporting/tracing/logging this api call for the first time.
      apiZone.reported = true;
      // java: the stack trace is captured by the first reported message, not by wrapApiCall.
      apiZone.frames = connection.captureLibraryStackTrace();
      instrumentation.onApiCallBegin(apiZone, type, method, params);
      // java: without an api name from the stack, the log names the protocol call.
      apiZone.logName = apiZone.apiName != null ? apiZone.apiName : Character.toLowerCase(type.charAt(0)) + type.substring(1) + "." + method;
      logApiCall(logger, "=> " + apiZone.logName + " started");
      options.apiName = apiZone.apiName;
      options.title = apiZone.title;
      options.frames = apiZone.frames;
      options.callId = apiZone.callId;
    } else {
      // Since this api call is either internal, or has already been reported/traced once,
      // passing as internal.
      options.internal = true;
    }
    return options;
  }

  <R> R wrapApiCall(Supplier<R> func, String title, boolean internal) {
    Object logger = this.logger;
    ApiZone existingApiZone = connection.apiZone;
    if (existingApiZone != null) {
      return func.get();
    }

    // java: captureLibraryStackTrace() runs on the first reported message (see callOptions); its apiName
    // is not computed (see StackTraceCollector), the title stands in for it.
    String apiName = title;
    ApiZone apiZone = new ApiZone(title, apiName, internal);

    try {
      R result;
      connection.apiZone = apiZone;
      try {
        result = func.get();
      } finally {
        connection.apiZone = null;
      }
      if (!internal) {
        if (apiZone.logName != null) {
          logApiCall(logger, "<= " + apiZone.logName + " succeeded");
        }
        instrumentation.onApiCallEnd(apiZone);
      }
      return result;
    } catch (RuntimeException e) {
      // java: the message is not prefixed with the api name and the stack is not rewritten with the
      // library frames; Java exceptions keep the message of the server and their own stack.
      if (!internal) {
        apiZone.error = e;
        if (apiZone.logName != null) {
          logApiCall(logger, "<= " + apiZone.logName + " failed");
        }
        instrumentation.onApiCallEnd(apiZone);
      }
      throw e;
    }
  }

  // java: upstream's toJSON keeps Jest from printing the object graph.
  JsonObject toJSON() {
    // Jest's expect library tries to print objects sometimes.
    // RPC objects can contain links to lots of other objects,
    // which can cause jest to crash. Let's help it out
    // by just returning the important values.
    JsonObject result = new JsonObject();
    result.addProperty("_type", type);
    result.addProperty("_guid", guid);
    return result;
  }

  // java: the apiCall and internal decorators and wrapApiCallDecorator are withTitle(title, ...) and
  // wrapApiCall(..., title, internal) below.

  // java: the logger is an opaque Object, nothing logs to it; debugLogger.log('api', ...) is logApiIfEnabled.
  static void logApiCall(Object logger, String message) {
    logApiIfEnabled(message);
  }

  // java: tChannelImplToWire is toProtocolRef() below and the channel owner adapter of Connection.protocolGson().

  // java: the members below have no counterpart in channelOwner.ts.

  // java: a call without a timeout; the server does not enforce one.
  static Double NO_TIMEOUT = null;

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
    return connection.sendMessageToServer(this, method, params, callOptions(method, params, null));
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
    connection.sendMessageToServer(this, method, params, callOptions(method, params, null));
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
    return connection.sendMessage(this, method, params, callOptions(method, params, timeout));
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
        try {
          connection.processOneMessage();
        } catch (PlaywrightException e) {
          // java: a closed pipe throws from the pump right after it dispatched the close events; the outcome
          // of the waitable wins, as the promise settled by those events does upstream.
          if (!waitable.isDone()) {
            throw e;
          }
        }
      }
      return waitable.get();
    } finally {
      waitable.dispose();
    }
  }

  // java: upstream's Connection.dispatch emits the event on the channel; subclasses override this.
  void handleEvent(String event, JsonObject parameters) {
  }

  // java: upstream's tChannelImplToWire.
  JsonObject toProtocolRef() {
    JsonObject json = new JsonObject();
    json.addProperty("guid", guid);
    return json;
  }
}

// java: the ApiZone type of channelOwner.ts; ApiCallData is its structural supertype upstream.
class ApiZone extends ApiCallData {
  String apiName;
  final boolean internal;
  boolean reported;

  // java: the name the pw:api log prints for the call, set by its first reported message.
  String logName;

  ApiZone(String title, String apiName, boolean internal) {
    this.title = title;
    this.apiName = apiName;
    this.internal = internal;
  }
}
