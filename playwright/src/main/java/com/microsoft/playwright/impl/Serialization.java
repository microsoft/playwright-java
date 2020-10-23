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
import com.google.gson.JsonObject;
import com.microsoft.playwright.FileChooser;
import com.microsoft.playwright.JSHandle;
import com.microsoft.playwright.Keyboard;
import com.microsoft.playwright.Mouse;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.*;

class Serialization {
  static SerializedError serializeError(Throwable e) {
    SerializedError result = new SerializedError();
    result.error = new SerializedError.Error();
    result.error.message = e.getMessage();
    result.error.name = e.getClass().getName();

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    e.printStackTrace(new PrintStream(out));
    result.error.stack = new String(out.toByteArray());
    return  result;
  }

  private static SerializedValue serializeValue(Object value, List<JSHandleImpl> handles, int depth) {
    if (depth > 100) {
      throw new RuntimeException("Maximum argument depth exceeded");
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
      double d = ((Double) value).doubleValue();
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
      for (Object o : (List) value) {
        list.add(serializeValue(o, handles, depth + 1));
      }
      result.a = list.toArray(new SerializedValue[0]);
    } else if (value instanceof Map) {
      List<SerializedValue.O> list = new ArrayList<>();
      @SuppressWarnings("unchecked")
      Map<String, Object> map = (Map<String, Object>) value;
      for (Map.Entry<String, Object> e : map.entrySet()) {
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
      throw new RuntimeException("Unsupported type of argument: " + value);
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
        case "-0":
          return (T) Double.valueOf(-0);
        case "NaN":
          return (T) Double.valueOf(Double.NaN);
        default:
          throw new RuntimeException("Unexpected value: " + value.v);
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
    throw new RuntimeException("Unexpected result: " + new Gson().toJson(value));
  }

  static String toProtocol(Mouse.Button button) {
    switch (button) {
      case LEFT: return "left";
      case RIGHT: return "right";
      case MIDDLE: return "middle";
      default: throw new RuntimeException("Unexpected value: " + button);
    }
  }

  static JsonArray toProtocol(Set<Keyboard.Modifier> modifiers) {
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
}
