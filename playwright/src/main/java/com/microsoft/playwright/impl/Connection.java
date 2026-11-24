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

// Port of connection.ts.
package com.microsoft.playwright.impl;

import com.microsoft.playwright.impl.protocol.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.TimeoutError;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static com.microsoft.playwright.impl.Serialization.gson;
import static com.microsoft.playwright.impl.isomorphic.Time.monotonicTime;

class Root extends ChannelOwner {
  Root(Connection connection) {
    super(connection, "Root", "");
  }

  PlaywrightImpl initialize() {
    RootInitializeParams params = new RootInitializeParams();
    params.sdkLanguage = "java";
    params.supportsLoneSurrogates = true;
    return new RootChannel(this).initialize(params, NO_TIMEOUT).playwright;
  }

  // java: the deadline of raceAgainstDeadline in connect.ts, the wait pumps the connection until then.
  PlaywrightImpl initialize(double deadline, double timeout) {
    RootInitializeParams params = new RootInitializeParams();
    params.sdkLanguage = "java";
    params.supportsLoneSurrogates = true;
    WaitableResult<JsonElement> result = sendMessageAsync("initialize", params);
    Waitable<JsonElement> deadlineWaitable = new Waitable<JsonElement>() {
      @Override
      public boolean isDone() {
        return monotonicTime() > deadline;
      }

      @Override
      public JsonElement get() {
        throw new TimeoutError("Timeout " + Waiter.formatTimeout(timeout) + "ms exceeded");
      }

      @Override
      public void dispose() {
      }
    };
    JsonElement json = runUntil(() -> {}, new WaitableRace<>(Arrays.asList(result, deadlineWaitable)));
    return connection.protocolGson().fromJson(json, RootInitializeResult.class).playwright;
  }
}

class DummyChannelOwner extends ChannelOwner {
  DummyChannelOwner(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
  }
}

interface ChannelOwnerFactory {
  ChannelOwner create(ChannelOwner parent, String type, String guid, JsonObject initializer);
}

public class Connection extends EventEmitter {
  final Map<String, ChannelOwner> objects = new HashMap<>();
  // java: onmessage is the transport, sendMessageToServer sends to it.
  // Upstream creates the ids with createCallIdGenerator(); the prefix is unique for each client to avoid id clashes in a trace.
  private final String callIdPrefix = createCallIdPrefix();
  private int lastId = 0;
  // java: upstream keeps { resolve, reject, signal, title, type, method } per call, nothing but the result is needed here.
  private final Map<String, WaitableResult<JsonElement>> callbacks = new HashMap<>();
  private final Root rootObject;
  private TargetClosedError closedError;
  private boolean isRemote = false;
  private LocalUtils localUtils;
  private boolean rawBuffers = false;
  // java: toImpl is not ported, there are no in-process dispatchers.
  private int tracingCount;
  final ClientInstrumentation instrumentation;
  // Used from @playwright/test fixtures -> TODO remove?
  final List<NameValue> headers;
  private final Map<String, ChannelOwnerFactory> objectFactories = new HashMap<>();

  // java: everything below has no counterpart in connection.ts.
  private final Transport transport;
  private final Deque<Message> deferredRouteEvents = new ArrayDeque<>();
  private final StackTraceCollector stackTraceCollector;
  private final Gson protocolGson = gson().newBuilder()
    .registerTypeAdapterFactory(new ChannelOwnerAdapterFactory())
    .registerTypeAdapter(byte[].class, new Base64Adapter().nullSafe())
    .create();
  // java: upstream keeps the api zone in an AsyncLocalStorage; the single API thread keeps it here, see ChannelOwner.wrapApiCall.
  ApiZone apiZone;
  PlaywrightImpl playwright;
  final Map<String, String> env;
  private static final long DRIVER_RESPONSE_GRACE_MS = 10_000;
  private static final boolean isLogging;
  static {
    String debug = System.getenv("DEBUG");
    isLogging = (debug != null) && debug.contains("pw:channel");
  }

  /** The options of upstream's sendMessageToServer; {@code timeout == null} means that no timeout is sent. */
  static class CallOptions {
    String apiName;
    String title;
    boolean internal;
    JsonArray frames;
    String callId;
    // java: the AbortSignal is not ported.
    Double timeout;
  }

  // java: the connection of a connect() call is remote and shares LocalUtils with the driver connection.
  Connection(Transport pipe, Map<String, String> env, LocalUtils localUtils) {
    this(pipe, env, localUtils, null, new ArrayList<>(), true);
  }

  // java: connect.ts passes the instrumentation of the parent connection.
  Connection(Transport pipe, Map<String, String> env, LocalUtils localUtils, ClientInstrumentation instrumentation) {
    this(pipe, env, localUtils, instrumentation, new ArrayList<>(), true);
  }

  // java: connect.ts also passes the headers of the connect result.
  Connection(Transport pipe, Map<String, String> env, LocalUtils localUtils, ClientInstrumentation instrumentation, List<NameValue> headers) {
    this(pipe, env, localUtils, instrumentation, headers, true);
  }

  Connection(Transport transport, Map<String, String> env) {
    this(transport, env, null, null, new ArrayList<>(), false);
  }

  private Connection(Transport transport, Map<String, String> env, LocalUtils localUtils, ClientInstrumentation instrumentation, List<NameValue> headers, boolean remote) {
    this.env = env;
    if (isLogging) {
      transport = new TransportLogger(transport);
    }
    this.transport = transport;
    stackTraceCollector = StackTraceCollector.createFromEnv(env);
    this.instrumentation = instrumentation != null ? instrumentation : ClientInstrumentation.createInstrumentation();
    this.localUtils = localUtils;
    this.rootObject = new Root(this);
    this.headers = headers;
    if (remote) {
      markAsRemote();
    }
    Map<String, ChannelOwnerFactory> factories = new LinkedHashMap<>();
    // java: android.ts and electron.ts are not ported, their objects are ignored.
    factories.put("Android", (parent, type, guid, init) -> null);
    factories.put("AndroidDevice", (parent, type, guid, init) -> null);
    factories.put("AndroidSocket", (parent, type, guid, init) -> null);
    factories.put("APIRequestContext", (parent, type, guid, init) -> new APIRequestContextImpl(parent, type, guid, init));
    factories.put("Artifact", (parent, type, guid, init) -> new ArtifactImpl(parent, type, guid, init));
    factories.put("BindingCall", (parent, type, guid, init) -> new BindingCall(parent, type, guid, init));
    factories.put("Browser", (parent, type, guid, init) -> new BrowserImpl(parent, type, guid, init));
    factories.put("BrowserContext", (parent, type, guid, init) -> new BrowserContextImpl(parent, type, guid, init));
    factories.put("BrowserType", (parent, type, guid, init) -> new BrowserTypeImpl(parent, type, guid, init));
    factories.put("CDPSession", (parent, type, guid, init) -> new CDPSessionImpl(parent, type, guid, init));
    factories.put("Debugger", (parent, type, guid, init) -> new DebuggerImpl(parent, type, guid, init));
    factories.put("Dialog", (parent, type, guid, init) -> new DialogImpl(parent, type, guid, init));
    factories.put("Disposable", (parent, type, guid, init) -> new DisposableObject(parent, type, guid, init));
    factories.put("Electron", (parent, type, guid, init) -> null);
    factories.put("ElectronApplication", (parent, type, guid, init) -> null);
    factories.put("ElementHandle", (parent, type, guid, init) -> new ElementHandleImpl(parent, type, guid, init));
    factories.put("Frame", (parent, type, guid, init) -> new FrameImpl(parent, type, guid, init));
    factories.put("JSHandle", (parent, type, guid, init) -> new JSHandleImpl(parent, type, guid, init));
    factories.put("JsonPipe", (parent, type, guid, init) -> new JsonPipe(parent, type, guid, init));
    factories.put("LocalUtils", (parent, type, guid, init) -> {
      LocalUtils result = new LocalUtils(parent, type, guid, init);
      if (this.localUtils == null) {
        this.localUtils = result;
      }
      return result;
    });
    factories.put("Page", (parent, type, guid, init) -> new PageImpl(parent, type, guid, init));
    factories.put("Playwright", (parent, type, guid, init) -> new PlaywrightImpl(parent, type, guid, init));
    factories.put("Request", (parent, type, guid, init) -> new RequestImpl(parent, type, guid, init));
    factories.put("Response", (parent, type, guid, init) -> new ResponseImpl(parent, type, guid, init));
    factories.put("Route", (parent, type, guid, init) -> new RouteImpl(parent, type, guid, init));
    factories.put("Stream", (parent, type, guid, init) -> new Stream(parent, type, guid, init));
    factories.put("SocksSupport", (parent, type, guid, init) -> new DummyChannelOwner(parent, type, guid, init));
    factories.put("Tracing", (parent, type, guid, init) -> new TracingImpl(parent, type, guid, init));
    factories.put("WebSocket", (parent, type, guid, init) -> new WebSocketImpl(parent, type, guid, init));
    factories.put("WebSocketRoute", (parent, type, guid, init) -> new WebSocketRouteImpl(parent, type, guid, init));
    factories.put("Worker", (parent, type, guid, init) -> new WorkerImpl(parent, type, guid, init));
    factories.put("WritableStream", (parent, type, guid, init) -> new WritableStream(parent, type, guid, init));
    registerObjectFactories(factories);
  }

  void registerObjectFactories(Map<String, ChannelOwnerFactory> factories) {
    for (Map.Entry<String, ChannelOwnerFactory> entry : factories.entrySet())
      objectFactories.put(entry.getKey(), entry.getValue());
  }

  void markAsRemote() {
    isRemote = true;
  }

  boolean isRemote() {
    return isRemote;
  }

  void useRawBuffers() {
    rawBuffers = true;
  }

  // java: protocolGson() always converts binary to base64; the flag is kept but changes nothing.
  boolean rawBuffers() {
    return rawBuffers;
  }

  LocalUtils localUtils() {
    return localUtils;
  }

  public PlaywrightImpl initializePlaywright() {
    // java: also remembered for getByTestId, which needs the selectors of the Playwright object.
    playwright = rootObject.initialize();
    return playwright;
  }

  // java: connect.ts races the initialization against the deadline of connect().
  PlaywrightImpl initializePlaywright(double deadline, double timeout) {
    playwright = rootObject.initialize(deadline, timeout);
    return playwright;
  }

  @SuppressWarnings("unchecked")
  <T> T getObjectWithKnownName(String guid) {
    return (T) objects.get(guid);
  }

  void setIsTracing(boolean isTracing) {
    if (isTracing) {
      tracingCount++;
    } else {
      tracingCount--;
    }
  }

  // java: upstream returns a promise; the result is completed by dispatch() while some thread pumps messages.
  WaitableResult<JsonElement> sendMessageToServer(ChannelOwner object, String method, JsonObject params, CallOptions options) {
    // Fire-and-forget: server intentionally never replies to __waitInfo__,
    // so silently drop it after the connection is closed or the object was collected.
    if ("__waitInfo__".equals(method) && (closedError != null || object.wasCollected)) {
      return completed();
    }
    if (closedError != null) {
      // java: a new exception, so that the stack trace points at the caller.
      throw new TargetClosedError(closedError.getMessage(), closedError);
    }
    if (object.wasCollected) {
      throw new PlaywrightException("The object has been collected to prevent unbounded heap growth.");
    }

    // java: the signal.aborted check is not ported, there is no AbortSignal.

    String guid = object.guid;
    String type = object.type;
    String id = options.callId != null ? options.callId : nextCallId();
    JsonObject message = new JsonObject();
    message.addProperty("id", id);
    message.addProperty("guid", guid);
    message.addProperty("method", method);
    message.add("params", params);
    // java: the debugLogger 'channel' log is TransportLogger, installed for DEBUG=pw:channel.
    JsonObject metadata = new JsonObject();
    if (options.title != null) {
      metadata.addProperty("title", options.title);
    }
    if (options.frames != null && !options.frames.isEmpty()) {
      JsonObject frame = options.frames.get(0).getAsJsonObject();
      JsonObject location = new JsonObject();
      location.addProperty("file", frame.get("file").getAsString());
      location.addProperty("line", frame.get("line").getAsInt());
      location.addProperty("column", frame.get("column").getAsInt());
      metadata.add("location", location);
    }
    if (options.internal) {
      metadata.addProperty("internal", true);
    }
    // java: upstream always sends a timeout, 0 by default; here null stays absent.
    if (options.timeout != null) {
      metadata.addProperty("timeout", options.timeout);
    }
    message.add("metadata", metadata);
    // java: registered before sending, the callback is looked up by id only when a reply arrives.
    WaitableResult<JsonElement> result = new WaitableResult<>();
    // Fire-and-forget: server intentionally never replies to __waitInfo__.
    boolean expectsReply = !"__waitInfo__".equals(method);
    if (expectsReply) {
      callbacks.put(id, result);
    }
    // java: the stack goes to tracing after the message is sent, the zones of emptyZone.run are not ported.
    transport.send(message);
    if (tracingCount > 0 && options.frames != null && !"LocalUtils".equals(type)) {
      JsonObject callData = new JsonObject();
      callData.addProperty("id", id);
      callData.add("stack", options.frames);
      JsonObject stackParams = new JsonObject();
      stackParams.add("callData", callData);
      // java: LocalUtils belongs to the driver connection, which is not this one when the browser came from connect().
      localUtils.addStackToTracingNoReply(stackParams);
    }
    // java: the abort listener is not ported, there is no AbortSignal.
    return expectsReply ? result : completed();
  }

  // java: _validatorFromWireContext is not ported, there are no protocol validators; protocolGson() parses
  // the results, events and initializers (channel owners from {guid}, binary from base64).

  // java: the message is the parsed wire shape, see Message below.
  void dispatch(Message message) {
    if (closedError != null) {
      return;
    }

    if (message.id != null) {
      WaitableResult<JsonElement> callback = callbacks.get(message.id);
      if (callback == null) {
        throw new PlaywrightException("Cannot find command to respond: " + message.id);
      }
      callbacks.remove(message.id);
      if (message.error != null && message.result == null) {
        // java: the AbortError cause is not ported, there is no AbortSignal. The call log is appended by parseError;
        // the log and the error details (not validated, there are no validators) are kept by ServerErrorWithDetails.
        PlaywrightException parsedError = Errors.parseError(message.error, formatCallLog(message.log));
        if (message.errorDetails != null) {
          parsedError = new ServerErrorWithDetails(parsedError, message.errorDetails, message.log);
        }
        callback.completeExceptionally(parsedError);
      } else {
        // java: the result is parsed by the caller with protocolGson(), there are no validators.
        callback.complete(message.result);
      }
      return;
    }

    // java: the debugLogger 'channel' log is TransportLogger.

    // java: a message with neither id nor method is ignored.
    if (message.method == null) {
      return;
    }
    if (message.method.equals("__create__")) {
      createRemoteObject(message.guid, message.params.get("type").getAsString(), message.params.get("guid").getAsString(), message.params.getAsJsonObject("initializer"));
      return;
    }

    ChannelOwner object = objects.get(message.guid);
    if (object == null) {
      throw new PlaywrightException("Cannot find object to \"" + message.method + "\": " + message.guid);
    }
    if (message.method.equals("__adopt__")) {
      String childGuid = message.params.get("guid").getAsString();
      ChannelOwner child = objects.get(childGuid);
      if (child == null) {
        throw new PlaywrightException("Unknown new child: " + childGuid);
      }
      object.adopt(child);
      return;
    }
    if (message.method.equals("__dispose__")) {
      String reason = message.params != null && message.params.has("reason") ? message.params.get("reason").getAsString() : null;
      object.dispose(reason);
      return;
    }

    // java: upstream validates the params and emits the event on the channel; ChannelOwner.handleEvent of the
    // object parses them with protocolGson() and handles the event.
    object.handleEvent(message.method, message.params);
  }

  void close() throws IOException {
    close(null);
  }

  void close(String cause) throws IOException {
    if (closedError != null) {
      return;
    }
    closedError = new TargetClosedError(cause);
    for (WaitableResult<JsonElement> callback : callbacks.values()) {
      callback.completeExceptionally(closedError);
    }
    callbacks.clear();
    emit("close", null);
    // java: the transport is closed here, the 'close' listener of connect.ts closes it upstream.
    transport.close();
  }

  // java: _tChannelImplFromWire is the read() of ChannelOwnerAdapterFactory below, through getExistingObject.

  private ChannelOwner createRemoteObject(String parentGuid, String type, String guid, JsonObject initializer) {
    ChannelOwner parent = objects.get(parentGuid);
    if (parent == null) {
      throw new PlaywrightException("Cannot find parent object " + parentGuid + " to create " + guid);
    }
    // java: the initializer is not validated, there are no validators; each object parses it with protocolGson().
    ChannelOwnerFactory factory = objectFactories.get(type);
    if (factory == null) {
      throw new PlaywrightException("Missing type " + type);
    }
    return factory.create(parent, type, guid, initializer);
  }

  // java: helpers with no counterpart in connection.ts.

  // java: upstream's _tChannelImplFromWire and, in channelOwner.ts, tChannelImplToWire; Gson adapters instead of validator hooks.
  /** Gson that serializes channel owners as {guid} and resolves them back through this connection. */
  Gson protocolGson() {
    return protocolGson;
  }

  private class ChannelOwnerAdapterFactory implements TypeAdapterFactory {
    @Override
    @SuppressWarnings("unchecked")
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
      if (!ChannelOwner.class.isAssignableFrom(type.getRawType())) {
        return null;
      }
      return (TypeAdapter<T>) new TypeAdapter<ChannelOwner>() {
        @Override
        public void write(JsonWriter out, ChannelOwner value) throws IOException {
          if (value == null) {
            out.nullValue();
            return;
          }
          out.beginObject();
          out.name("guid");
          out.value(value.guid);
          out.endObject();
        }

        @Override
        public ChannelOwner read(JsonReader in) throws IOException {
          if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
          }
          String guid = null;
          in.beginObject();
          while (in.hasNext()) {
            String name = in.nextName();
            if ("guid".equals(name)) {
              guid = in.nextString();
            } else {
              in.skipValue();
            }
          }
          in.endObject();
          return getExistingObject(guid);
        }
      };
    }
  }

  private static class Base64Adapter extends TypeAdapter<byte[]> {
    @Override
    public void write(JsonWriter out, byte[] value) throws IOException {
      out.value(Base64.getEncoder().encodeToString(value));
    }

    @Override
    public byte[] read(JsonReader in) throws IOException {
      return Base64.getDecoder().decode(in.nextString());
    }
  }

  // java: upstream's captureLibraryStackTrace() lives in clientStackTrace.ts; null when PLAYWRIGHT_JAVA_SRC is not set.
  JsonArray captureLibraryStackTrace() {
    return stackTraceCollector == null ? null : stackTraceCollector.captureLibraryStackTrace();
  }

  // java: the generator function of _nextCallId.
  private String nextCallId() {
    return callIdPrefix + "@" + (++lastId);
  }

  // java: upstream has getObjectWithKnownName(guid) for the same lookup; this one throws for an unknown guid.
  public <T> T getExistingObject(String guid) {
    @SuppressWarnings("unchecked") T result = (T) objects.get(guid);
    if (result == null)
      throw new PlaywrightException("Object doesn't exist: " + guid);
    return result;
  }

  // java: the _objects.set / _objects.delete of channelOwner.ts.
  void registerObject(String guid, ChannelOwner object) {
    objects.put(guid, object);
  }

  void unregisterObject(String guid) {
    objects.remove(guid);
  }

  private static WaitableResult<JsonElement> completed() {
    WaitableResult<JsonElement> result = new WaitableResult<>();
    result.complete(null);
    return result;
  }

  // java: sends the message and pumps messages until the reply arrives; upstream awaits the promise of sendMessageToServer.
  JsonElement sendMessage(ChannelOwner object, String method, JsonObject params, CallOptions options) {
    if (closedError != null && transport instanceof JsonPipe) {
      // java: a call over a closed JsonPipe fails like the pipe does, with the reason the server gave
      // ('Browser closed' after it went away); async calls and waits get the closedError.
      throw new TargetClosedError(((JsonPipe) transport).closeReason(), closedError);
    }
    WaitableResult<JsonElement> result = sendMessageToServer(object, method, params, options);
    Double timeout = options.timeout;
    if (timeout == null || timeout == 0) {
      return rootObject.runUntil(() -> {}, result);
    }
    // The driver enforces the timeout, but the driver itself may hang.
    long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeout.longValue() + DRIVER_RESPONSE_GRACE_MS);
    while (!result.isDone()) {
      if (!processOneMessage() && System.nanoTime() - deadline > 0) {
        result.completeExceptionally(new TimeoutError("Timeout " + timeout.longValue() + "ms exceeded while waiting for the driver to respond to " +
          method + ". The driver is not responding."));
      }
    }
    return result.get();
  }

  // java: upstream receives messages through onmessage(); here the pump polls the transport.
  boolean processOneMessage() {
    return processOneMessage(true);
  }

  // java: route-action waits defer route events so queued requests do not nest on the Java stack.
  boolean processOneMessage(boolean dispatchRoutes) {
    if (dispatchRoutes && !deferredRouteEvents.isEmpty()) {
      dispatch(deferredRouteEvents.removeFirst());
      return true;
    }
    JsonObject message = transport.poll(Duration.ofMillis(10));
    if (message == null) {
      return false;
    }
    Gson gson = gson();
    Message messageObj = gson.fromJson(message, Message.class);
    if (!dispatchRoutes && messageObj.id == null && "route".equals(messageObj.method)) {
      deferredRouteEvents.addLast(messageObj);
    } else {
      dispatch(messageObj);
    }
    return true;
  }

  private static String createCallIdPrefix() {
    String alphabet = "abcdefghijklmnopqrstuvwxyz";
    StringBuilder prefix = new StringBuilder();
    for (int i = 0; i < 4; i++) {
      prefix.append(alphabet.charAt(ThreadLocalRandom.current().nextInt(alphabet.length())));
    }
    return prefix.toString();
  }

  // java: each line is prefixed with "- " and not dimmed, as the Java client always printed it.
  private static String formatCallLog(JsonArray log) {
    if (log == null) {
      return "";
    }
    boolean allEmpty = true;
    for (JsonElement e: log) {
      if (!e.getAsString().isEmpty()) {
        allEmpty = false;
        break;
      }
    }
    if (allEmpty) {
      return "";
    }
    List<String> lines = new ArrayList<>();
    lines.add("");
    lines.add("Call log:");
    for (JsonElement e: log) {
      lines.add("- " + e.getAsString());
    }
    lines.add("");
    return String.join("\n", lines);
  }
}

// java: the wire shape of an incoming message.
class Message {
  String id;
  String guid;
  String method;
  JsonObject params;
  JsonElement result;
  SerializedError error;
  JsonObject errorDetails;
  JsonArray log;

  @Override
  public String toString() {
    return "Message{" +
      "id='" + id + '\'' +
      ", guid='" + guid + '\'' +
      ", method='" + method + '\'' +
      ", params=" + (params == null ? null : "<...>") +
      ", result='" + result + '\'' +
      ", error='" + Errors.errorToString(error) + '\'' +
      '}';
  }
}
