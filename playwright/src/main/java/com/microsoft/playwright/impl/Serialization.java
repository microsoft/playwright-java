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
import com.microsoft.playwright.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

class Serialization {
  private static Gson gson;

  static Gson gson() {
    if (gson == null) {
      gson = new GsonBuilder()
        .registerTypeAdapter(BrowserContext.SameSite.class, new SameSiteAdapter().nullSafe())
        .registerTypeAdapter(ColorScheme.class, new ColorSchemeAdapter().nullSafe())
        .registerTypeAdapter(Page.EmulateMediaOptions.Media.class, new MediaSerializer())
        .registerTypeAdapter(ElementHandle.ScreenshotOptions.Type.class, new ToLowerCaseSerializer<ElementHandle.ScreenshotOptions.Type>())
        .registerTypeAdapter(Page.ScreenshotOptions.Type.class, new ToLowerCaseSerializer<Page.ScreenshotOptions.Type>())
        .registerTypeAdapter(Mouse.Button.class, new ToLowerCaseSerializer<Mouse.Button>())
        .registerTypeAdapter(Frame.LoadState.class, new ToLowerCaseSerializer<Frame.LoadState>())
        .registerTypeAdapter(ElementHandle.WaitForSelectorOptions.State.class, new ToLowerCaseSerializer<ElementHandle.WaitForSelectorOptions.State>())
        .registerTypeAdapter(Frame.WaitForSelectorOptions.State.class, new ToLowerCaseSerializer<Frame.WaitForSelectorOptions.State>())
        .registerTypeAdapter(Page.WaitForSelectorOptions.State.class, new ToLowerCaseSerializer<Page.WaitForSelectorOptions.State>())
        .registerTypeAdapter((new TypeToken<Set<Keyboard.Modifier>>(){}).getType(), new KeyboardModifiersSerializer())
        .registerTypeAdapter(Optional.class, new OptionalSerializer())
        .registerTypeHierarchyAdapter(JSHandleImpl.class, new HandleSerializer())
        .registerTypeHierarchyAdapter(Map.class, new StringMapSerializer())
        .registerTypeAdapter(Path.class, new PathSerializer()).create();
    }
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

  private static SerializedValue serializeValue(Object value, List<JSHandleImpl> handles, int depth) {
    if (depth > 100) {
      throw new PlaywrightException("Maximum argument depth exceeded");
    }
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
    } else if (value instanceof List) {
      List<SerializedValue> list = new ArrayList<>();
      for (Object o : (List<?>) value) {
        list.add(serializeValue(o, handles, depth + 1));
      }
      result.a = list.toArray(new SerializedValue[0]);
    } else if (value instanceof Map) {
      List<SerializedValue.O> list = new ArrayList<>();
      @SuppressWarnings("unchecked")
      Map<String, ?> map = (Map<String, ?>) value;
      for (Map.Entry<String, ?> e : map.entrySet()) {
        SerializedValue.O o = new SerializedValue.O();
        o.k = e.getKey();
        o.v = serializeValue(e.getValue(), handles, depth + 1);
        list.add(o);
      }
      result.o = list.toArray(new SerializedValue.O[0]);
    } else if (value instanceof Object[]) {
      List<SerializedValue> list = new ArrayList<>();
      for (Object o : (Object[]) value) {
        list.add(serializeValue(o, handles, depth + 1));
      }
      result.a = list.toArray(new SerializedValue[0]);
    } else {
      throw new PlaywrightException("Unsupported type of argument: " + value);
    }
    return result;
  }

  static SerializedArgument serializeArgument(Object arg) {
    SerializedArgument result = new SerializedArgument();
    List<JSHandleImpl> handles = new ArrayList<>();
    result.value = serializeValue(arg, handles, 0);
    result.handles = new Channel[handles.size()];
    int i = 0;
    for (JSHandleImpl handle : handles) {
      result.handles[i] = new Channel();
      result.handles[i].guid = handle.guid;
      ++i;
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  static <T> T deserialize(SerializedValue value) {
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
      for (SerializedValue v : value.a) {
        list.add(deserialize(v));
      }
      return (T) list;
    }
    if (value.o != null) {
      Map<String, Object> map = new LinkedHashMap<>();
      for (SerializedValue.O o : value.o) {
        map.put(o.k, deserialize(o.v));
      }
      return (T) map;
    }
    throw new PlaywrightException("Unexpected result: " + gson().toJson(value));
  }

  private static class KeyboardModifiersSerializer implements JsonSerializer<Set<Keyboard.Modifier>> {
    @Override
    public JsonArray serialize(Set<Keyboard.Modifier> modifiers, Type typeOfSrc, JsonSerializationContext context) {
      JsonArray result = new JsonArray();
      if (modifiers.contains(Keyboard.Modifier.ALT)) {
        result.add("Alt");
      }
      if (modifiers.contains(Keyboard.Modifier.CONTROL)) {
        result.add("Control");
      }
      if (modifiers.contains(Keyboard.Modifier.META)) {
        result.add("Meta");
      }
      if (modifiers.contains(Keyboard.Modifier.SHIFT)) {
        result.add("Shift");
      }
      return result;
    }
  }

  static JsonArray toJsonArray(FileChooser.FilePayload[] files) {
    JsonArray jsonFiles = new JsonArray();
    for (FileChooser.FilePayload p : files) {
      JsonObject jsonFile = new JsonObject();
      jsonFile.addProperty("name", p.name);
      jsonFile.addProperty("mimeType", p.mimeType);
      jsonFile.addProperty("buffer", Base64.getEncoder().encodeToString(p.buffer));
      jsonFiles.add(jsonFile);
    }
    return jsonFiles;
  }

  static JsonArray toProtocol(ElementHandle[] handles) {
    JsonArray jsonElements = new JsonArray();
    for (ElementHandle handle : handles) {
      JsonObject jsonHandle = new JsonObject();
      jsonHandle.addProperty("guid", ((ElementHandleImpl) handle).guid);
      jsonElements.add(jsonHandle);
    }
    return jsonElements;
  }

  static JsonArray toProtocol(Map<String, String> map) {
    JsonArray array = new JsonArray();
    for (Map.Entry<String, String> e : map.entrySet()) {
      JsonObject item = new JsonObject();
      item.addProperty("name", e.getKey());
      item.addProperty("value", e.getValue());
      array.add(item);
    }
    return array;
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
      return new TypeToken<Optional<Page.EmulateMediaOptions.Media>>() {}.getType().getTypeName().equals(type.getTypeName()) ||
        new TypeToken<Optional<ColorScheme>>() {}.getType().getTypeName().equals(type.getTypeName());
    }

    @Override
    public JsonElement serialize(Optional<?> src, Type typeOfSrc, JsonSerializationContext context) {
      assert isSupported(typeOfSrc);
      if (!src.isPresent()) {
        return new JsonPrimitive("null");
      }
      return context.serialize(src.get());
    }
  }

  private static class HandleSerializer implements JsonSerializer<JSHandleImpl> {
    @Override
    public JsonElement serialize(JSHandleImpl src, Type typeOfSrc, JsonSerializationContext context) {
      JsonObject json = new JsonObject();
      json.addProperty("guid", src.guid);
      return json;
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

  private static class MediaSerializer implements JsonSerializer<Page.EmulateMediaOptions.Media> {
    @Override
    public JsonElement serialize(Page.EmulateMediaOptions.Media src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(src.toString().toLowerCase());
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

  private static class SameSiteAdapter extends TypeAdapter<BrowserContext.SameSite> {
    @Override
    public void write(JsonWriter out, BrowserContext.SameSite value) throws IOException {
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
    public BrowserContext.SameSite read(JsonReader in) throws IOException {
      String value = in.nextString();
      return BrowserContext.SameSite.valueOf(value.toUpperCase());
    }
  }

  private static class ColorSchemeAdapter extends TypeAdapter<ColorScheme> {
    @Override
    public void write(JsonWriter out, ColorScheme value) throws IOException {
      String stringValue;
      switch (value) {
        case DARK:
          stringValue = "dark";
          break;
        case LIGHT:
          stringValue = "light";
          break;
        case NO_PREFERENCE:
          stringValue = "no-preference";
          break;
        default:
          throw new PlaywrightException("Unexpected value: " + value);
      }
      out.value(stringValue);
    }

    @Override
    public ColorScheme read(JsonReader in) throws IOException {
      String value = in.nextString();
      switch (value) {
        case "dark": return ColorScheme.DARK;
        case "light": return ColorScheme.LIGHT;
        case "no-preference": return ColorScheme.NO_PREFERENCE;
        default: throw new PlaywrightException("Unexpected value: " + value);
      }
    }
  }
}

