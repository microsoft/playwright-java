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

// Port of clientHelper.ts.
package com.microsoft.playwright.impl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.impl.protocol.NameValue;
import com.microsoft.playwright.options.FunctionCallback;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.microsoft.playwright.impl.Serialization.kFunctionBindingPrefix;
import static com.microsoft.playwright.impl.Utils.createGuid;
import static com.microsoft.playwright.impl.Utils.toJsRegexFlags;

import static java.nio.charset.StandardCharsets.UTF_8;

class ClientHelper {
  static List<NameValue> envObjectToArray(Map<String, String> env) {
    List<NameValue> result = new ArrayList<>();
    for (String name : env.keySet()) {
      if (env.get(name) != null) {
        NameValue nameValue = new NameValue();
        nameValue.name = name;
        nameValue.value = String.valueOf(env.get(name));
        result.add(nameValue);
      }
    }
    return result;
  }

  // java: fun is a script String or a file Path, the { path, content } object of upstream. The
  // typeof fun === 'function' branch is not ported, a Java value has no JavaScript source; a null arg is undefined.
  static String evaluationScript(Object fun, Object arg, boolean addSourceUrl) {
    if (arg != null)
      throw new PlaywrightException("Cannot evaluate a string with arguments");
    if (fun instanceof String)
      return (String) fun;
    if (fun instanceof Path) {
      Path path = (Path) fun;
      String source;
      try {
        source = new String(Files.readAllBytes(path), UTF_8);
      } catch (IOException e) {
        throw new PlaywrightException("Failed to read script from file", e);
      }
      if (addSourceUrl)
        source = addSourceUrlToScript(source, path);
      return source;
    }
    throw new PlaywrightException("Either path or content property must be present");
  }

  // java: fun is the source of the function; a FunctionCallback stands for a JS function in arg; expose returns
  // once the callback is exposed, so there is no Promise.all.
  static String initScriptSourceWithExposedFunctions(String fun, Object arg, BiConsumer<String, FunctionCallback> expose) {
    JsonElement serialized = serializeAsCallArgument(arg, value -> {
      if (value instanceof FunctionCallback) {
        String name = kFunctionBindingPrefix + createGuid();
        expose.accept(name, (FunctionCallback) value);
        JsonObject fn = new JsonObject();
        fn.addProperty("fn", name);
        return fn;
      }
      return null;
    });
    // Bindings backing the functions are registered through their own init scripts
    // that are guaranteed to run first, so the controller is available here.
    return "(" + fun + ")(globalThis['" + kBindingsControllerProperty + "'].parseInitScriptArg(" + new Gson().toJson(serialized) + "))";
  }

  static String addSourceUrlToScript(String source, Path path) {
    return source + "\n//# sourceURL=" + path.toString().replace("\n", "");
  }

  // java: helpers with no counterpart in clientHelper.ts.

  // java: kBindingsControllerProperty of isomorphic/utilityScriptSerializers.ts, which has no Java port.
  static final String kBindingsControllerProperty = "__playwright__binding__controller__";

  // java: serializeAsCallArgument of isomorphic/utilityScriptSerializers.ts, which has no Java port, for the
  // values a Java arg can hold (the Java types Serialization.serializeValue accepts, without typed arrays and the
  // extended Map/Set serialization). The handle serializer returns the { fn } value, or null for { fallThrough: value }.
  // A null is undefined, as in Serialization.
  private static JsonElement serializeAsCallArgument(Object value, Function<Object, JsonElement> handleSerializer) {
    return innerSerializeAsCallArgument(value, handleSerializer, new IdentityHashMap<>(), new int[] {0});
  }

  private static JsonElement innerSerializeAsCallArgument(Object value, Function<Object, JsonElement> handleSerializer, Map<Object, Integer> visited, int[] lastId) {
    JsonElement handle = handleSerializer.apply(value);
    if (handle != null)
      return handle;

    if (value == null)
      return specialValue("undefined");
    if (value instanceof Double || value instanceof Float) {
      double number = ((Number) value).doubleValue();
      if (Double.isNaN(number))
        return specialValue("NaN");
      if (number == Double.POSITIVE_INFINITY)
        return specialValue("Infinity");
      if (number == Double.NEGATIVE_INFINITY)
        return specialValue("-Infinity");
      if (number == 0 && 1 / number < 0)
        return specialValue("-0");
    }

    if (value instanceof Boolean)
      return new JsonPrimitive((Boolean) value);
    if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long || value instanceof Float || value instanceof Double)
      return new JsonPrimitive((Number) value);
    if (value instanceof String)
      return new JsonPrimitive((String) value);
    if (value instanceof BigInteger) {
      JsonObject result = new JsonObject();
      result.addProperty("bi", value.toString());
      return result;
    }

    if (value instanceof Throwable) {
      Throwable error = (Throwable) value;
      String name = error.getClass().getSimpleName();
      StringWriter stack = new StringWriter();
      error.printStackTrace(new PrintWriter(stack));
      JsonObject e = new JsonObject();
      e.addProperty("n", name);
      e.addProperty("m", error.getMessage());
      e.addProperty("s", name + ": " + error.getMessage() + "\n" + stack);
      JsonObject result = new JsonObject();
      result.add("e", e);
      return result;
    }
    if (value instanceof Date || value instanceof LocalDateTime) {
      JsonObject result = new JsonObject();
      result.addProperty("d", (value instanceof Date ? ((Date) value).toInstant() : ((LocalDateTime) value).atZone(ZoneId.systemDefault()).toInstant()).toString());
      return result;
    }
    if (value instanceof URL) {
      JsonObject result = new JsonObject();
      result.addProperty("u", value.toString());
      return result;
    }
    if (value instanceof java.util.regex.Pattern) {
      JsonObject r = new JsonObject();
      r.addProperty("p", ((java.util.regex.Pattern) value).pattern());
      r.addProperty("f", toJsRegexFlags((java.util.regex.Pattern) value));
      JsonObject result = new JsonObject();
      result.add("r", r);
      return result;
    }

    Integer id = visited.get(value);
    if (id != null) {
      JsonObject result = new JsonObject();
      result.addProperty("ref", id);
      return result;
    }

    if (value instanceof List || value instanceof Object[]) {
      List<?> array = value instanceof List ? (List<?>) value : Arrays.asList((Object[]) value);
      JsonArray a = new JsonArray();
      id = ++lastId[0];
      visited.put(value, id);
      for (Object item : array)
        a.add(innerSerializeAsCallArgument(item, handleSerializer, visited, lastId));
      JsonObject result = new JsonObject();
      result.add("a", a);
      result.addProperty("id", id);
      return result;
    }

    if (value instanceof Map) {
      JsonArray o = new JsonArray();
      id = ++lastId[0];
      visited.put(value, id);
      for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
        JsonObject item = new JsonObject();
        item.addProperty("k", String.valueOf(entry.getKey()));
        item.add("v", innerSerializeAsCallArgument(entry.getValue(), handleSerializer, visited, lastId));
        o.add(item);
      }
      JsonObject result = new JsonObject();
      result.add("o", o);
      result.addProperty("id", id);
      return result;
    }

    // java: any other value, a function without the binding prefix upstream, serializes as undefined.
    return specialValue("undefined");
  }

  private static JsonObject specialValue(String v) {
    JsonObject result = new JsonObject();
    result.addProperty("v", v);
    return result;
  }

  // java: the overload without arg, the Java API never passes one.
  static String evaluationScript(Object fun, boolean addSourceUrl) {
    return evaluationScript(fun, null, addSourceUrl);
  }
}
