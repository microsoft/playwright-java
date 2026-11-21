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

// Port of serializers.ts (packages/protocol) and of the module-level functions and types of jsHandle.ts.
package com.microsoft.playwright.impl;

import com.microsoft.playwright.impl.protocol.*;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.*;
import com.microsoft.playwright.options.BrowserChannel;
import com.microsoft.playwright.options.ConsoleMessagesFilter;

import java.io.*;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;

import static com.microsoft.playwright.impl.Utils.createGuid;
import static com.microsoft.playwright.impl.Utils.toJsRegexFlags;
import static com.microsoft.playwright.impl.Utils.fromJsRegexFlags;

class Serialization {
  // java: no systemErrorCode and systemErrorMessage, a Java exception has no Node.js system error code.

  static Object parseSerializedValue(SerializedValue value, List<Object> handles) {
    return innerParseSerializedValue(value, handles, new HashMap<>(), new ArrayList<>());
  }

  private static Object innerParseSerializedValue(SerializedValue value, List<Object> handles, Map<Integer, Object> refs, List<Object> accessChain) {
    if (value.ref != null)
      return refs.get(value.ref);
    if (value.n != null) {
      // java: an integral number is an Integer, any other number a Double.
      if (value.n.doubleValue() == (double) value.n.intValue())
        return value.n.intValue();
      return value.n;
    }
    if (value.s != null)
      return value.s;
    if (value.b != null)
      return value.b;
    if (value.v != null) {
      // java: undefined is null.
      if (value.v.equals("undefined"))
        return null;
      if (value.v.equals("null"))
        return null;
      if (value.v.equals("NaN"))
        return Double.NaN;
      if (value.v.equals("Infinity"))
        return Double.POSITIVE_INFINITY;
      if (value.v.equals("-Infinity"))
        return Double.NEGATIVE_INFINITY;
      if (value.v.equals("-0"))
        return -0.0;
    }
    if (value.d != null)
      return Date.from(Instant.parse(value.d));
    if (value.u != null) {
      // java: new URL() throws a checked exception.
      try {
        return new URL(value.u);
      } catch (MalformedURLException e) {
        throw new PlaywrightException("Unexpected value: " + value.u, e);
      }
    }
    if (value.bi != null)
      return new BigInteger(value.bi);
    if (value.e != null) {
      // java: an Exception has no settable name or stack, its message is the stack (tests pin it).
      return new Exception(value.e.s);
    }
    if (value.r != null)
      return Pattern.compile(value.r.p, fromJsRegexFlags(value.r.f));
    if (value.ta != null) {
      Class<?> ctor = typedArrayKindToConstructor.get(value.ta.k);
      // java: an unknown kind fails here instead of in the constructor call.
      if (ctor == null)
        throw new PlaywrightException("Unexpected typed array kind: " + value.ta.k);
      return fromTypedArrayBytes(ctor, value.ta.b);
    }

    if (value.a != null) {
      List<Object> result = new ArrayList<>();
      refs.put(value.id, result);
      for (int i = 0; i < value.a.size(); i++)
        result.add(innerParseSerializedValue(value.a.get(i), handles, refs, append(accessChain, i)));
      return result;
    }
    if (value.o != null) {
      Map<String, Object> result = new LinkedHashMap<>();
      refs.put(value.id, result);
      for (SerializedValue.SerializedValueO o : value.o)
        result.put(o.k, innerParseSerializedValue(o.v, handles, refs, append(accessChain, o.k)));
      return result;
    }
    if (value.h != null) {
      if (handles == null)
        throw new PlaywrightException("Unexpected handle");
      return handles.get(value.h);
    }
    if (value.fn != null) {
      // java: a FunctionCallback stands for a JS function, its toString() is the function name.
      String name = value.fn;
      FunctionCallback dummy = new FunctionCallback() {
        @Override
        public Object call(Object... args) {
          return null;
        }

        @Override
        public String toString() {
          return name;
        }
      };
      return dummy;
    }
    throw new PlaywrightException("Attempting to deserialize unexpected value" + accessChainToDisplayString(accessChain) + ": " + gson().toJson(value));
  }

  // java: HandleOrValue is a class with a flag for 'fallThrough' in handle.
  static class HandleOrValue {
    Integer h;
    String fn;
    boolean hasFallThrough;
    Object fallThrough;
  }

  private static class VisitorInfo {
    // java: an IdentityHashMap, keyed by reference like a JavaScript Map.
    final Map<Object, Integer> visited = new IdentityHashMap<>();
    int lastId;
  }

  static SerializedValue serializeValue(Object value, Function<Object, HandleOrValue> handleSerializer) {
    return innerSerializeValue(value, handleSerializer, new VisitorInfo(), new ArrayList<>());
  }

  static SerializedValue serializePlainValue(Object arg) {
    return serializeValue(arg, value -> fallThrough(value));
  }

  private static SerializedValue innerSerializeValue(Object value, Function<Object, HandleOrValue> handleSerializer, VisitorInfo visitorInfo, List<Object> accessChain) {
    HandleOrValue handle = handleSerializer.apply(value);
    if (handle.hasFallThrough) {
      value = handle.fallThrough;
    } else {
      SerializedValue result = new SerializedValue();
      result.h = handle.h;
      result.fn = handle.fn;
      return result;
    }

    SerializedValue result = new SerializedValue();
    // java: there are no symbols; null is undefined (tests pin it), so null never reaches { v: 'null' }.
    if (value == null) {
      result.v = "undefined";
      return result;
    }
    if (objectIs(value, Double.NaN)) {
      result.v = "NaN";
      return result;
    }
    if (objectIs(value, Double.POSITIVE_INFINITY)) {
      result.v = "Infinity";
      return result;
    }
    if (objectIs(value, Double.NEGATIVE_INFINITY)) {
      result.v = "-Infinity";
      return result;
    }
    if (objectIs(value, -0.0)) {
      result.v = "-0";
      return result;
    }
    if (value instanceof Boolean) {
      result.b = (Boolean) value;
      return result;
    }
    // java: a number is a Byte, Short, Integer, Long, Float or Double.
    if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long || value instanceof Float || value instanceof Double) {
      result.n = ((Number) value).doubleValue();
      return result;
    }
    if (value instanceof String) {
      result.s = (String) value;
      return result;
    }
    // java: a bigint is a BigInteger.
    if (value instanceof BigInteger) {
      result.bi = value.toString();
      return result;
    }
    if (isError(value)) {
      Throwable error = (Throwable) value;
      result.e = new SerializedValue.SerializedValueE();
      // java: the name is the simple class name, the stack is the printed Java stack trace.
      result.e.n = error.getClass().getSimpleName();
      result.e.m = error.getMessage();
      StringWriter stack = new StringWriter();
      error.printStackTrace(new PrintWriter(stack));
      result.e.s = stack.toString();
      return result;
    }
    if (isDate(value)) {
      // java: a LocalDateTime is in the default time zone.
      Instant instant = value instanceof Date ? ((Date) value).toInstant() : ((LocalDateTime) value).atZone(ZoneId.systemDefault()).toInstant();
      result.d = instant.toString();
      return result;
    }
    if (isURL(value)) {
      result.u = value.toString();
      return result;
    }
    if (isRegExp(value)) {
      result.r = new SerializedValue.SerializedValueR();
      result.r.p = ((Pattern) value).pattern();
      result.r.f = toJsRegexFlags((Pattern) value);
      return result;
    }

    String typedArrayKind = constructorToTypedArrayKind.get(value.getClass());
    if (typedArrayKind != null) {
      result.ta = new SerializedValue.SerializedValueTa();
      result.ta.b = toTypedArrayBytes(value);
      result.ta.k = typedArrayKind;
      return result;
    }

    Integer id = visitorInfo.visited.get(value);
    if (id != null) {
      result.ref = id;
      return result;
    }

    // java: an array is a List or an Object[].
    if (value instanceof List || value instanceof Object[]) {
      List<?> array = value instanceof List ? (List<?>) value : Arrays.asList((Object[]) value);
      List<SerializedValue> a = new ArrayList<>();
      id = ++visitorInfo.lastId;
      visitorInfo.visited.put(value, id);
      for (int i = 0; i < array.size(); ++i)
        a.add(innerSerializeValue(array.get(i), handleSerializer, visitorInfo, append(accessChain, i)));
      result.a = a;
      result.id = id;
      return result;
    }
    // java: an object is a Map, its keys are converted to strings; any other Java object is unexpected.
    if (value instanceof Map) {
      List<SerializedValue.SerializedValueO> o = new ArrayList<>();
      id = ++visitorInfo.lastId;
      visitorInfo.visited.put(value, id);
      for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
        String name = String.valueOf(entry.getKey());
        SerializedValue.SerializedValueO item = new SerializedValue.SerializedValueO();
        item.k = name;
        item.v = innerSerializeValue(entry.getValue(), handleSerializer, visitorInfo, append(accessChain, name));
        o.add(item);
      }
      result.o = o;
      result.id = id;
      return result;
    }
    // Likely only functions can reach here.
    throw new PlaywrightException("Attempting to serialize unexpected value" + accessChainToDisplayString(accessChain) + ": " + value);
  }

  private static String accessChainToDisplayString(List<Object> accessChain) {
    StringBuilder chainString = new StringBuilder();
    for (int i = 0; i < accessChain.size(); i++) {
      Object accessor = accessChain.get(i);
      if (accessor instanceof String)
        chainString.append(i != 0 ? "." + accessor : accessor);
      else
        chainString.append("[").append(accessor).append("]");
    }

    return chainString.length() > 0 ? " at position \"" + chainString + "\"" : "";
  }

  private static boolean isRegExp(Object obj) {
    return obj instanceof Pattern;
  }

  // java: a date is a Date or a LocalDateTime.
  private static boolean isDate(Object obj) {
    return obj instanceof Date || obj instanceof LocalDateTime;
  }

  private static boolean isURL(Object obj) {
    return obj instanceof URL;
  }

  // java: an error is a Throwable.
  private static boolean isError(Object obj) {
    return obj instanceof Throwable;
  }

  // java: a typed array is a primitive array, several kinds share one array type.
  private static final Map<String, Class<?>> typedArrayKindToConstructor = new LinkedHashMap<>();
  static {
    typedArrayKindToConstructor.put("i8", byte[].class);
    typedArrayKindToConstructor.put("ui8", byte[].class);
    typedArrayKindToConstructor.put("ui8c", byte[].class);
    typedArrayKindToConstructor.put("i16", short[].class);
    typedArrayKindToConstructor.put("ui16", short[].class);
    typedArrayKindToConstructor.put("i32", int[].class);
    typedArrayKindToConstructor.put("ui32", int[].class);
    typedArrayKindToConstructor.put("f32", float[].class);
    typedArrayKindToConstructor.put("f64", double[].class);
    typedArrayKindToConstructor.put("bi64", long[].class);
    typedArrayKindToConstructor.put("bui64", long[].class);
  }
  // java: not the inverse of the map above, a Java array type is sent as the signed kind.
  private static final Map<Class<?>, String> constructorToTypedArrayKind = new HashMap<>();
  static {
    constructorToTypedArrayKind.put(byte[].class, "i8");
    constructorToTypedArrayKind.put(short[].class, "i16");
    constructorToTypedArrayKind.put(int[].class, "i32");
    constructorToTypedArrayKind.put(float[].class, "f32");
    constructorToTypedArrayKind.put(double[].class, "f64");
    constructorToTypedArrayKind.put(long[].class, "bi64");
  }

  // jsHandle.ts

  // java: the optional registerCallback is an overload.
  static SerializedArgument serializeArgument(Object arg) {
    return serializeArgument(arg, null);
  }

  // This function takes care of converting all JSHandles to their channels,
  // so that generic channel serializer converts them to guids.
  // java: a FunctionCallback stands for a JS function.
  static SerializedArgument serializeArgument(Object arg, Function<FunctionCallback, String> registerCallback) {
    List<ChannelOwner> handles = new ArrayList<>();
    Function<ChannelOwner, Integer> pushHandle = channel -> {
      handles.add(channel);
      return handles.size() - 1;
    };
    SerializedValue value = serializeValue(arg, v -> {
      // java: a JSHandleImpl is its own channel.
      if (v instanceof JSHandleImpl)
        return handle(pushHandle.apply((JSHandleImpl) v));
      if (v instanceof FunctionCallback && registerCallback != null)
        return fn(registerCallback.apply((FunctionCallback) v));
      return fallThrough(v);
    });
    SerializedArgument result = new SerializedArgument();
    result.value = value;
    result.handles = handles;
    return result;
  }

  // java: the ExposeFunctionsOptions type, a null exposeFunctions is undefined.
  static class ExposeFunctionsOptions {
    Boolean exposeFunctions;
  }

  // java: the EvaluateOptions alias of ExposeFunctionsOptions is a subclass.
  static class EvaluateOptions extends ExposeFunctionsOptions {
  }

  // java: each exposeEvaluateCallback() call returns once the callback is exposed, so there is no Promise.all.
  static SerializedArgument serializeArgumentWithCallbacks(ChannelOwner owner, PageImpl page, Object arg) {
    return owner.wrapApiCall(() -> {
      SerializedArgument serialized = serializeArgument(arg, callback -> {
        if (page == null)
          throw new PlaywrightException("Passing a function is not supported as an argument here");
        String name = kFunctionBindingPrefix + createGuid();
        page.exposeEvaluateCallback(name, callback);
        return name;
      });
      return serialized;
    }, null, true);
  }

  static Object parseResult(SerializedValue value) {
    return parseSerializedValue(value, null);
  }

  // java: no assertMaxArguments, it checks the JS arguments count.

  // java: options is an Object so that the check keeps its meaning: a null is undefined, a String, Number, Boolean
  // or FunctionCallback is not a JS object, a List or a Java array is a JS array.
  static void assertEvaluateOptions(Object options) {
    if (options != null && (options instanceof String || options instanceof Number || options instanceof Boolean || options instanceof FunctionCallback || options instanceof List || options.getClass().isArray()))
      throw new PlaywrightException("Too many arguments. If you need to pass more than 1 argument to the function wrap them in an object.");
  }

  // java: helpers of the ported part.

  // java: kFunctionBindingPrefix of isomorphic/utilityScriptSerializers.ts, which has no Java port.
  static final String kFunctionBindingPrefix = "__pw_fn_";

  // java: parseResult cast to the caller's result type, the name other files call.
  @SuppressWarnings("unchecked")
  static <T> T deserialize(SerializedValue value) {
    return (T) parseResult(value);
  }

  private static HandleOrValue handle(int h) {
    HandleOrValue result = new HandleOrValue();
    result.h = h;
    return result;
  }

  private static HandleOrValue fn(String fn) {
    HandleOrValue result = new HandleOrValue();
    result.fn = fn;
    return result;
  }

  private static HandleOrValue fallThrough(Object value) {
    HandleOrValue result = new HandleOrValue();
    result.hasFallThrough = true;
    result.fallThrough = value;
    return result;
  }

  private static List<Object> append(List<Object> accessChain, Object accessor) {
    List<Object> result = new ArrayList<>(accessChain);
    result.add(accessor);
    return result;
  }

  private static boolean objectIs(Object value, double number) {
    if (!(value instanceof Double) && !(value instanceof Float))
      return false;
    return Double.compare(((Number) value).doubleValue(), number) == 0;
  }

  private static Object fromTypedArrayBytes(Class<?> ctor, byte[] bytes) {
    ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
    if (ctor == byte[].class)
      return bytes;
    if (ctor == short[].class) {
      short[] result = new short[bytes.length / 2];
      buffer.asShortBuffer().get(result);
      return result;
    }
    if (ctor == int[].class) {
      int[] result = new int[bytes.length / 4];
      buffer.asIntBuffer().get(result);
      return result;
    }
    if (ctor == float[].class) {
      float[] result = new float[bytes.length / 4];
      buffer.asFloatBuffer().get(result);
      return result;
    }
    if (ctor == double[].class) {
      double[] result = new double[bytes.length / 8];
      buffer.asDoubleBuffer().get(result);
      return result;
    }
    long[] result = new long[bytes.length / 8];
    buffer.asLongBuffer().get(result);
    return result;
  }

  private static byte[] toTypedArrayBytes(Object value) {
    if (value instanceof byte[])
      return ((byte[]) value).clone();
    ByteBuffer buffer;
    if (value instanceof short[]) {
      short[] array = (short[]) value;
      buffer = ByteBuffer.allocate(array.length * 2).order(ByteOrder.LITTLE_ENDIAN);
      buffer.asShortBuffer().put(array);
    } else if (value instanceof int[]) {
      int[] array = (int[]) value;
      buffer = ByteBuffer.allocate(array.length * 4).order(ByteOrder.LITTLE_ENDIAN);
      buffer.asIntBuffer().put(array);
    } else if (value instanceof float[]) {
      float[] array = (float[]) value;
      buffer = ByteBuffer.allocate(array.length * 4).order(ByteOrder.LITTLE_ENDIAN);
      buffer.asFloatBuffer().put(array);
    } else if (value instanceof double[]) {
      double[] array = (double[]) value;
      buffer = ByteBuffer.allocate(array.length * 8).order(ByteOrder.LITTLE_ENDIAN);
      buffer.asDoubleBuffer().put(array);
    } else {
      long[] array = (long[]) value;
      buffer = ByteBuffer.allocate(array.length * 8).order(ByteOrder.LITTLE_ENDIAN);
      buffer.asLongBuffer().put(array);
    }
    return buffer.array();
  }

  // java: Gson configuration, protocol conversions and error helpers, no counterpart in serializers.ts.

  private static final Gson gson = new GsonBuilder().disableHtmlEscaping()
    .registerTypeAdapter(Date.class, new DateSerializer())
    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer())
    .registerTypeAdapter(SameSiteAttribute.class, new SameSiteAdapter().nullSafe())
    .registerTypeAdapter(AnnotatePosition.class, new ToLowerCaseAndDashSerializer<AnnotatePosition>())
    .registerTypeAdapter(BrowserChannel.class, new ToLowerCaseAndDashSerializer<BrowserChannel>())
    .registerTypeAdapter(ColorScheme.class, new ToLowerCaseAndDashSerializer<ColorScheme>())
    .registerTypeAdapter(Contrast.class, new ToLowerCaseAndDashSerializer<Contrast>())
    .registerTypeAdapter(Media.class, new ToLowerCaseSerializer<Media>())
    .registerTypeAdapter(ForcedColors.class, new ToLowerCaseSerializer<ForcedColors>())
    .registerTypeAdapter(HttpCredentialsSend.class, new ToLowerCaseSerializer<HttpCredentialsSend>())
    .registerTypeAdapter(ReducedMotion.class, new ToLowerCaseAndDashSerializer<ReducedMotion>())
    .registerTypeAdapter(ScreencastCursor.class, new ToLowerCaseSerializer<ScreencastCursor>())
    .registerTypeAdapter(ScreenshotAnimations.class, new ToLowerCaseSerializer<ScreenshotAnimations>())
    .registerTypeAdapter(ScreenshotType.class, new ToLowerCaseSerializer<ScreenshotType>())
    .registerTypeAdapter(ScreenshotScale.class, new ToLowerCaseSerializer<ScreenshotScale>())
    .registerTypeAdapter(ScreenshotCaret.class, new ToLowerCaseSerializer<ScreenshotCaret>())
    .registerTypeAdapter(ServiceWorkerPolicy.class, new ToLowerCaseAndDashSerializer<ServiceWorkerPolicy>())
    .registerTypeAdapter(MouseButton.class, new ToLowerCaseSerializer<MouseButton>())
    .registerTypeAdapter(ScrollMode.class, new ToLowerCaseSerializer<ScrollMode>())
    .registerTypeAdapter(ConsoleMessagesFilter.class, new ConsoleMessagesFilterSerializer())
    .registerTypeAdapter(AriaSnapshotMode.class, new ToLowerCaseSerializer<AriaSnapshotMode>())
    .registerTypeAdapter(PseudoElement.class, new ToLowerCaseSerializer<PseudoElement>())
    .registerTypeAdapter(LoadState.class, new ToLowerCaseSerializer<LoadState>())
    .registerTypeAdapter(WaitUntilState.class, new ToLowerCaseSerializer<WaitUntilState>())
    .registerTypeAdapter(WaitForSelectorState.class, new ToLowerCaseSerializer<WaitForSelectorState>())
    .registerTypeAdapter((new TypeToken<List<KeyboardModifier>>(){}).getType(), new KeyboardModifiersSerializer())
    .registerTypeAdapter(Optional.class, new OptionalSerializer())
    .registerTypeHierarchyAdapter(JSHandleImpl.class, new HandleSerializer())
    .registerTypeHierarchyAdapter(ChannelOwner.class, new ChannelOwnerSerializer())
    .registerTypeAdapter((new TypeToken<Map<String, String>>(){}).getType(), new StringMapSerializer())
    .registerTypeAdapter((new TypeToken<Map<String, Object>>(){}).getType(), new FirefoxUserPrefsSerializer())
    .registerTypeAdapter(LocatorImpl.class, new LocatorImplSerializer())
    .registerTypeHierarchyAdapter(Path.class, new PathSerializer()).create();

  static Gson gson() {
    return gson;
  }

  static final Gson jsonDataSerializer = new GsonBuilder().disableHtmlEscaping()
    .registerTypeAdapter(Date.class, new DateSerializer())
    .registerTypeAdapter(LocalDate.class, new LocalDateSerializer())
    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer())
    .registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeSerializer())
    .serializeNulls().create();

  private static final Gson prettyGson = new GsonBuilder().disableHtmlEscaping().serializeNulls().setPrettyPrinting().create();

  static String toPrettyJson(String json) {
    return prettyGson.toJson(JsonParser.parseString(json));
  }

  static SerializedError serializeError(Throwable e) {
    SerializedError result = new SerializedError();
    result.error = new SerializedError.SerializedErrorError();
    result.error.message = e.getMessage();
    result.error.name = e.getClass().getName();

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    e.printStackTrace(new PrintStream(out));
    result.error.stack = new String(out.toByteArray(), StandardCharsets.UTF_8);
    return result;
  }

  private static class KeyboardModifiersSerializer implements JsonSerializer<List<KeyboardModifier>> {
    @Override
    public JsonArray serialize(List<KeyboardModifier> modifiers, Type typeOfSrc, JsonSerializationContext context) {
      JsonArray result = new JsonArray();
      if (modifiers.contains(KeyboardModifier.ALT)) {
        result.add("Alt");
      }
      if (modifiers.contains(KeyboardModifier.CONTROL)) {
        result.add("Control");
      }
      if (modifiers.contains(KeyboardModifier.CONTROLORMETA)) {
        result.add("ControlOrMeta");
      }
      if (modifiers.contains(KeyboardModifier.META)) {
        result.add("Meta");
      }
      if (modifiers.contains(KeyboardModifier.SHIFT)) {
        result.add("Shift");
      }
      return result;
    }
  }

  static JsonArray toProtocol(Map<String, String> map) {
    for (String value : map.values()) {
      if (value == null) {
        throw new PlaywrightException("Value cannot be null");
      }
    }
    return toNameValueArray(map.entrySet());
  }

  static JsonObject toClearCookiesParams(Object name, Object domain, Object path) {
    JsonObject params = new JsonObject();
    setStringOrRegex(params, "name", name);
    setStringOrRegex(params, "domain", domain);
    setStringOrRegex(params, "path", path);
    return params;
  }

  private static void setStringOrRegex(JsonObject params, String name, Object value) {
    if (value instanceof String) {
      params.addProperty(name, (String) value);
    } else if (value instanceof Pattern) {
      Pattern pattern = (Pattern) value;
      params.addProperty(name + "RegexSource", pattern.pattern());
      params.addProperty(name + "RegexFlags", toJsRegexFlags(pattern));
    }
  }

  static JsonArray toNameValueArray(Iterable<? extends Map.Entry<String, ?>> collection) {
    JsonArray array = new JsonArray();
    for (Map.Entry<String, ?> e : collection) {
      JsonObject item = new JsonObject();
      item.addProperty("name", e.getKey());
      if (e.getValue() instanceof FilePayload) {
        item.add("value", gson().toJsonTree(e.getValue()));
      } else {
        item.addProperty("value", "" + e.getValue());
      }
      array.add(item);
    }
    return array;
  }

  static String parseError(JsonObject object) {
    SerializedError error = gson().fromJson(object, SerializedError.class);
    String errorStr = "";
    if (error.error != null) {
      errorStr = error.error.name + ": " + error.error.message;
      if (error.error.stack != null && !error.error.stack.isEmpty()) {
        errorStr += "\n" + error.error.stack;
      }
    }
    return errorStr;
  }

  private static class OptionalSerializer implements JsonSerializer<Optional<?>> {
    private static boolean isSupported(Type type) {
      return new TypeToken<Optional<Media>>() {}.getType().getTypeName().equals(type.getTypeName()) ||
        new TypeToken<Optional<ColorScheme>>() {}.getType().getTypeName().equals(type.getTypeName()) ||
        new TypeToken<Optional<Contrast>>() {}.getType().getTypeName().equals(type.getTypeName()) ||
        new TypeToken<Optional<ForcedColors>>() {}.getType().getTypeName().equals(type.getTypeName()) ||
        new TypeToken<Optional<ReducedMotion>>() {}.getType().getTypeName().equals(type.getTypeName()) ||
        new TypeToken<Optional<ViewportSize>>() {}.getType().getTypeName().equals(type.getTypeName());
    }

    @Override
    public JsonElement serialize(Optional<?> src, Type typeOfSrc, JsonSerializationContext context) {
      assert isSupported(typeOfSrc) : "Unexpected optional type: " + typeOfSrc.getTypeName();
      if (!src.isPresent()) {
        return new JsonPrimitive("no-override");
      }
      return context.serialize(src.get());
    }
  }

  private static class HandleSerializer implements JsonSerializer<JSHandleImpl> {
    @Override
    public JsonElement serialize(JSHandleImpl src, Type typeOfSrc, JsonSerializationContext context) {
      return src.toProtocolRef();
    }
  }

  // Serialization only: a channel owner travels as {guid}. Deserialization needs a connection, see Connection.gson().
  private static class ChannelOwnerSerializer implements JsonSerializer<ChannelOwner> {
    @Override
    public JsonElement serialize(ChannelOwner src, Type typeOfSrc, JsonSerializationContext context) {
      return src.toProtocolRef();
    }
  }

  private static class FirefoxUserPrefsSerializer implements JsonSerializer<Map<String, Object>> {
    @Override
    public JsonElement serialize(Map<String, Object> src, Type typeOfSrc, JsonSerializationContext context) {
      if (!"java.util.Map<java.lang.String, java.lang.Object>".equals(typeOfSrc.getTypeName())) {
        throw new PlaywrightException("Unexpected map type: " + typeOfSrc);
      }
      return context.serialize(src, Map.class);
    }
  }

  private static class StringMapSerializer implements JsonSerializer<Map<String, String>> {
    @Override
    public JsonElement serialize(Map<String, String> src, Type typeOfSrc, JsonSerializationContext context) {
      if (!"java.util.Map<java.lang.String, java.lang.String>".equals(typeOfSrc.getTypeName())) {
        throw new PlaywrightException("Unexpected map type: " + typeOfSrc);
      }
      return toProtocol(src);
    }
  }

  private static class ConsoleMessagesFilterSerializer implements JsonSerializer<ConsoleMessagesFilter> {
    @Override
    public JsonElement serialize(ConsoleMessagesFilter src, Type typeOfSrc, JsonSerializationContext context) {
      switch (src) {
        case ALL: return new JsonPrimitive("all");
        case SINCE_NAVIGATION: return new JsonPrimitive("since-navigation");
        default: throw new PlaywrightException("Unknown ConsoleMessagesFilter: " + src);
      }
    }
  }

  private static class ToLowerCaseAndDashSerializer<E extends Enum<E>> implements JsonSerializer<E> {
    @Override
    public JsonElement serialize(E src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(src.toString().toLowerCase().replace('_', '-'));
    }
  }

  private static class PathSerializer implements JsonSerializer<Path> {
    @Override
    public JsonElement serialize(Path src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(src.toString());
    }
  }

  private static class ToLowerCaseSerializer<E extends Enum<E>> implements JsonSerializer<E> {
    @Override
    public JsonElement serialize(E src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(src.toString().toLowerCase());
    }
  }

  private static class LocatorImplSerializer implements JsonSerializer<LocatorImpl> {
    @Override
    public JsonElement serialize(LocatorImpl src, Type typeOfSrc, JsonSerializationContext context) {
      return src.toProtocol();
    }
  }

  private static class SameSiteAdapter extends TypeAdapter<SameSiteAttribute> {
    @Override
    public void write(JsonWriter out, SameSiteAttribute value) throws IOException {
      String stringValue;
      switch (value) {
        case STRICT:
          stringValue = "Strict";
          break;
        case LAX:
          stringValue = "Lax";
          break;
        case NONE:
          stringValue = "None";
          break;
        default:
          throw new PlaywrightException("Unexpected value: " + value);
      }
      out.value(stringValue);
    }

    @Override
    public SameSiteAttribute read(JsonReader in) throws IOException {
      String value = in.nextString();
      return SameSiteAttribute.valueOf(value.toUpperCase());
    }
  }

  private static DateFormat iso8601Format() {
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    return dateFormat;
  }
  private static final DateFormat dateFormat = iso8601Format();

  private static class DateSerializer implements JsonSerializer<Date> {
    @Override
    public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(dateFormat.format(src));
    }
  }

  private static class OffsetDateTimeSerializer implements JsonSerializer<OffsetDateTime> {
    @Override
    public JsonElement serialize(OffsetDateTime src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(dateFormat.format(Date.from(src.toInstant())));
    }
  }

  private static class LocalDateSerializer implements JsonSerializer<LocalDate> {
    @Override
    public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext context) {
      // LocalDate has no time or zone, so emit the ISO-8601 date (yyyy-MM-dd) as-is to
      // avoid shifting the calendar date when converting through a time zone.
      return new JsonPrimitive(src.toString());
    }
  }

  private static class LocalDateTimeSerializer implements JsonSerializer<LocalDateTime> {
    @Override
    public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
      ZoneOffset offset = ZoneId.systemDefault().getRules().getOffset(src);
      Instant instant = src.toInstant(offset);
      return new JsonPrimitive(dateFormat.format(Date.from(instant)));
    }
  }
}
