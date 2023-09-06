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

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

import static com.microsoft.playwright.impl.Utils.toJsRegexFlags;
import static com.microsoft.playwright.impl.Utils.fromJsRegexFlags;

class Serialization {
  private static final Gson gson = new GsonBuilder().disableHtmlEscaping()
    .registerTypeAdapter(Date.class, new DateSerializer())
    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer())
    .registerTypeAdapter(SameSiteAttribute.class, new SameSiteAdapter().nullSafe())
    .registerTypeAdapter(BrowserChannel.class, new ToLowerCaseAndDashSerializer<BrowserChannel>())
    .registerTypeAdapter(ColorScheme.class, new ToLowerCaseAndDashSerializer<ColorScheme>())
    .registerTypeAdapter(Media.class, new ToLowerCaseSerializer<Media>())
    .registerTypeAdapter(ForcedColors.class, new ToLowerCaseSerializer<ForcedColors>())
    .registerTypeAdapter(ReducedMotion.class, new ToLowerCaseAndDashSerializer<ReducedMotion>())
    .registerTypeAdapter(ScreenshotAnimations.class, new ToLowerCaseSerializer<ScreenshotAnimations>())
    .registerTypeAdapter(ScreenshotType.class, new ToLowerCaseSerializer<ScreenshotType>())
    .registerTypeAdapter(ScreenshotScale.class, new ToLowerCaseSerializer<ScreenshotScale>())
    .registerTypeAdapter(ScreenshotCaret.class, new ToLowerCaseSerializer<ScreenshotCaret>())
    .registerTypeAdapter(ServiceWorkerPolicy.class, new ToLowerCaseAndDashSerializer<ServiceWorkerPolicy>())
    .registerTypeAdapter(MouseButton.class, new ToLowerCaseSerializer<MouseButton>())
    .registerTypeAdapter(LoadState.class, new ToLowerCaseSerializer<LoadState>())
    .registerTypeAdapter(WaitUntilState.class, new ToLowerCaseSerializer<WaitUntilState>())
    .registerTypeAdapter(WaitForSelectorState.class, new ToLowerCaseSerializer<WaitForSelectorState>())
    .registerTypeAdapter((new TypeToken<List<KeyboardModifier>>(){}).getType(), new KeyboardModifiersSerializer())
    .registerTypeAdapter(Optional.class, new OptionalSerializer())
    .registerTypeHierarchyAdapter(JSHandleImpl.class, new HandleSerializer())
    .registerTypeAdapter((new TypeToken<Map<String, String>>(){}).getType(), new StringMapSerializer())
    .registerTypeAdapter((new TypeToken<Map<String, Object>>(){}).getType(), new FirefoxUserPrefsSerializer())
    .registerTypeHierarchyAdapter(Path.class, new PathSerializer()).create();

  static Gson gson() {
    return gson;
  }

  static SerializedError serializeError(Throwable e) {
    SerializedError result = new SerializedError();
    result.error = new SerializedError.Error();
    result.error.message = e.getMessage();
    result.error.name = e.getClass().getName();

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    e.printStackTrace(new PrintStream(out));
    result.error.stack = new String(out.toByteArray(), StandardCharsets.UTF_8);
    return result;
  }

  private static class ValueSerializer {
    // hashCode() of a map containing itself as a key will throw stackoverflow exception,
    // so we user wrappers.
    private static class HashableValue {
      final Object value;
      HashableValue(Object value) {
        this.value = value;
      }

      @Override
      public boolean equals(Object o) {
        return value == ((HashableValue) o).value;
      }

      @Override
      public int hashCode() {
        return System.identityHashCode(value);
      }
    }
    private final Map<HashableValue, Integer> valueToId = new HashMap<>();
    private int lastId = 0;
    private final List<JSHandleImpl> handles = new ArrayList<>();
    private final SerializedValue serializedValue;

    ValueSerializer(Object value) {
      serializedValue = serializeValue(value);
    }

    SerializedArgument toSerializedArgument() {
      SerializedArgument result = new SerializedArgument();
      result.value = serializedValue;
      result.handles = new Channel[handles.size()];
      int i = 0;
      for (JSHandleImpl handle : handles) {
        result.handles[i] = new Channel();
        result.handles[i].guid = handle.guid;
        ++i;
      }
      return result;
    }

    private SerializedValue serializeValue(Object value) {
      SerializedValue result = new SerializedValue();
      if (value instanceof JSHandleImpl) {
        result.h = handles.size();
        handles.add((JSHandleImpl) value);
        return result;
      }
      if (value == null) {
        result.v = "undefined";
      } else if (value instanceof Double) {
        double d = ((Double) value);
        if (d == Double.POSITIVE_INFINITY) {
          result.v = "Infinity";
        } else if (d == Double.NEGATIVE_INFINITY) {
          result.v = "-Infinity";
        } else if (d == -0) {
          result.v = "-0";
        } else if (Double.isNaN(d)) {
          result.v = "NaN";
        } else {
          result.n = d;
        }
      } else if (value instanceof Boolean) {
        result.b = (Boolean) value;
      } else if (value instanceof Integer) {
        result.n = (Integer) value;
      } else if (value instanceof String) {
        result.s = (String) value;
      } else if (value instanceof Date) {
        result.d = ((Date)value).toInstant().toString();
      } else if (value instanceof LocalDateTime) {
        result.d = ((LocalDateTime)value).atZone(ZoneId.systemDefault()).toInstant().toString();
      } else if (value instanceof URL) {
        result.u = ((URL)value).toString();
      } else if (value instanceof BigInteger) {
        result.bi = ((BigInteger)value).toString();
      } else if (value instanceof Pattern) {
        result.r = new SerializedValue.R();
        result.r.p = ((Pattern)value).pattern();
        result.r.f = toJsRegexFlags(((Pattern)value));
      } else {
        HashableValue mapKey = new HashableValue(value);
        Integer id = valueToId.get(mapKey);
        if (id != null) {
          result.ref = id;
        } else {
          result.id = ++lastId;
          valueToId.put(mapKey, lastId);
          if (value instanceof List) {
            List<SerializedValue> list = new ArrayList<>();
            for (Object o : (List<?>) value) {
              list.add(serializeValue(o));
            }
            result.a = list.toArray(new SerializedValue[0]);
          } else if (value instanceof Map) {
            List<SerializedValue.O> list = new ArrayList<>();
            @SuppressWarnings("unchecked")
            Map<String, ?> map = (Map<String, ?>) value;
            for (Map.Entry<String, ?> e : map.entrySet()) {
              SerializedValue.O o = new SerializedValue.O();
              o.k = e.getKey();
              o.v = serializeValue(e.getValue());
              list.add(o);
            }
            result.o = list.toArray(new SerializedValue.O[0]);
          } else if (value instanceof Object[]) {
            List<SerializedValue> list = new ArrayList<>();
            for (Object o : (Object[]) value) {
              list.add(serializeValue(o));
            }
            result.a = list.toArray(new SerializedValue[0]);
          } else {
            throw new PlaywrightException("Unsupported type of argument: " + value);
          }
        }
      }
      return result;
    }
  }

  static SerializedArgument serializeArgument(Object arg) {
    return new ValueSerializer(arg).toSerializedArgument();
  }

  static <T> T deserialize(SerializedValue value) {
    return deserialize(value, new HashMap<>());
  }

  @SuppressWarnings("unchecked")
  private static <T> T deserialize(SerializedValue value, Map<Integer, Object> idToValue) {
    if (value.ref != null) {
      return (T) idToValue.get(value.ref);
    }
    if (value.n != null) {
      if (value.n.doubleValue() == (double) value.n.intValue()) {
        return (T) Integer.valueOf(value.n.intValue());
      }
      return (T) Double.valueOf(value.n.doubleValue());
    }
    if (value.b != null)
      return (T) value.b;
    if (value.s != null)
      return (T) value.s;
    if (value.u != null) {
      try {
        return (T)(new URL(value.u));
      } catch (MalformedURLException e) {
        throw new PlaywrightException("Unexpected value: " + value.u, e);
      }
    }
    if (value.bi != null) {
      return (T) new BigInteger(value.bi);
    }
    if (value.d != null)
      return (T)(Date.from(Instant.parse(value.d)));
    if (value.r != null)
      return (T)(Pattern.compile(value.r.p, fromJsRegexFlags(value.r.f)));
    if (value.v != null) {
      switch (value.v) {
        case "undefined":
        case "null":
          return null;
        case "Infinity":
          return (T) Double.valueOf(Double.POSITIVE_INFINITY);
        case "-Infinity":
          return (T) Double.valueOf(Double.NEGATIVE_INFINITY);
        case "-0": {
          return (T) Double.valueOf(-0.0);
        }
        case "NaN":
          return (T) Double.valueOf(Double.NaN);
        default:
          throw new PlaywrightException("Unexpected value: " + value.v);
      }
    }
    if (value.a != null) {
      List<Object> list = new ArrayList<>();
      idToValue.put(value.id, list);
      for (SerializedValue v : value.a) {
        list.add(deserialize(v, idToValue));
      }
      return (T) list;
    }
    if (value.o != null) {
      Map<String, Object> map = new LinkedHashMap<>();
      idToValue.put(value.id, map);
      for (SerializedValue.O o : value.o) {
        map.put(o.k, deserialize(o.v, idToValue));
      }
      return (T) map;
    }
    if (value.m != null) {
      Map<?, ?> map = new LinkedHashMap<>();
      idToValue.put(value.id, map);
      return (T) map;
    }
    if (value.se != null) {
      Map<?, ?> map = new LinkedHashMap<>();
      idToValue.put(value.id, map);
      return (T) map;
    }
    throw new PlaywrightException("Unexpected result: " + gson().toJson(value));
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
      if (modifiers.contains(KeyboardModifier.META)) {
        result.add("Meta");
      }
      if (modifiers.contains(KeyboardModifier.SHIFT)) {
        result.add("Shift");
      }
      return result;
    }
  }

  static JsonArray toJsonArray(Path[] files) {
    JsonArray jsonFiles = new JsonArray();
    for (Path p : files) {
      jsonFiles.add(p.toAbsolutePath().toString());
    }
    return jsonFiles;
  }

  static JsonArray toJsonArray(FilePayload[] files) {
    JsonArray jsonFiles = new JsonArray();
    for (FilePayload p : files) {
      jsonFiles.add(toProtocol(p));
    }
    return jsonFiles;
  }

  static JsonObject toProtocol(FilePayload p) {
    JsonObject jsonFile = new JsonObject();
    jsonFile.addProperty("name", p.name);
    jsonFile.addProperty("mimeType", p.mimeType);
    jsonFile.addProperty("buffer", Base64.getEncoder().encodeToString(p.buffer));
    return jsonFile;
  }

  static JsonArray toProtocol(ElementHandle[] handles) {
    JsonArray jsonElements = new JsonArray();
    for (ElementHandle handle : handles) {
      jsonElements.add(((ElementHandleImpl) handle).toProtocolRef());
    }
    return jsonElements;
  }

  static JsonArray toProtocol(Map<String, String> map) {
    for (String value : map.values()) {
      if (value == null) {
        throw new PlaywrightException("Value cannot be null");
      }
    }
    return toNameValueArray(map);
  }

  static void addHarUrlFilter(JsonObject options, Object urlFilter) {
      if (urlFilter instanceof String) {
        options.addProperty("urlGlob", (String) urlFilter);
      } else if (urlFilter instanceof Pattern) {
        Pattern pattern = (Pattern) urlFilter;
        options.addProperty("urlRegexSource", pattern.pattern());
        options.addProperty("urlRegexFlags", toJsRegexFlags(pattern));
      }
  }

  static JsonArray toNameValueArray(Map<String, ?> map) {
    JsonArray array = new JsonArray();
    for (Map.Entry<String, ?> e : map.entrySet()) {
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

  static JsonArray toSelectValueOrLabel(String[] values) {
    JsonArray jsonOptions = new JsonArray();
    for (String value : values) {
      JsonObject option = new JsonObject();
      option.addProperty("valueOrLabel", value);
      jsonOptions.add(option);
    }
    return jsonOptions;
  }

  static Map<String, String> fromNameValues(JsonArray array) {
    Map<String, String> map = new LinkedHashMap<>();
    for (JsonElement element : array) {
      JsonObject pair = element.getAsJsonObject();
      map.put(pair.get("name").getAsString(), pair.get("value").getAsString());
    }
    return map;
  }

  static List<String> parseStringList(JsonArray array) {
    List<String> result = new ArrayList<>();
    for (JsonElement e : array) {
      result.add(e.getAsString());
    }
    return result;
  }

  private static class OptionalSerializer implements JsonSerializer<Optional<?>> {
    private static boolean isSupported(Type type) {
      return new TypeToken<Optional<Media>>() {}.getType().getTypeName().equals(type.getTypeName()) ||
        new TypeToken<Optional<ColorScheme>>() {}.getType().getTypeName().equals(type.getTypeName()) ||
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

  private static class LocalDateTimeSerializer implements JsonSerializer<LocalDateTime> {
    @Override
    public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
      ZoneOffset offset = ZoneId.systemDefault().getRules().getOffset(src);
      Instant instant = src.toInstant(offset);
      return new JsonPrimitive(dateFormat.format(Date.from(instant)));
    }
  }

}

