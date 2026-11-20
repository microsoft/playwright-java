/*
 * Copyright (c) Microsoft Corporation.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.playwright.impl;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.FilePayload;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.regex.Pattern;

// java: helpers with no upstream counterpart.
public class Utils {
  static <F, T> T convertType(F f, Class<T> t) {
    if (f == null) {
      return null;
    }

    // Make sure shallow copy is sufficient
    if (!t.getSuperclass().equals(Object.class) && !t.getSuperclass().equals(Enum.class)) {
      throw new PlaywrightException("Cannot convert to " + t.getCanonicalName() + " that has superclass " + t.getSuperclass().getCanonicalName());
    }
    if (!f.getClass().getSuperclass().equals(t.getSuperclass())) {
      throw new PlaywrightException("Cannot convert from " + t.getCanonicalName() + " that has superclass " + t.getSuperclass().getCanonicalName());
    }

    if (f instanceof Enum) {
      return (T) Enum.valueOf((Class) t, ((Enum) f).name());
    }

    try {
      T result = t.getDeclaredConstructor().newInstance();
      for (Field toField : t.getDeclaredFields()) {
        // Skip fields added by test coverage tools, see https://github.com/microsoft/playwright-java/issues/802
        if (toField.isSynthetic()) {
          continue;
        }
        if (Modifier.isStatic(toField.getModifiers())) {
            throw new RuntimeException("Unexpected field modifiers: " + t.getCanonicalName() + "." + toField.getName() + ", modifiers: " + toField.getModifiers());
        }
        try {
          Field fromField = f.getClass().getDeclaredField(toField.getName());
          Object value = fromField.get(f);
          if (value != null) {
            toField.set(result, value);
          }
        } catch (NoSuchFieldException e) {
          continue;
        }
      }
      return result;
    } catch (Exception e) {
      throw new PlaywrightException("Internal error", e);
    }
  }

  public static <T> T clone(T f) {
    if (f == null) {
      return f;
    }
    return convertType(f, (Class<T>) f.getClass());
  }

  /**
   * Copies public API options into a generated protocol params class with the same Gson that serializes them
   * on the wire. Params that carry channel references (a locator mask holds a frame) must be built with the
   * overload that takes the owner, which resolves the references through its connection (tChannelImplFromWire
   * upstream): this variant refuses to read them.
   */
  static <T> T toProtocol(Object options, Class<T> paramsClass) {
    return toProtocol(noChannelReadsGson, options, paramsClass);
  }

  static <T> T toProtocol(ChannelOwner owner, Object options, Class<T> paramsClass) {
    return toProtocol(owner.connection.protocolGson(), options, paramsClass);
  }

  private static <T> T toProtocol(Gson reader, Object options, Class<T> paramsClass) {
    if (options == null) {
      try {
        return paramsClass.getDeclaredConstructor().newInstance();
      } catch (Exception e) {
        throw new PlaywrightException("Internal error", e);
      }
    }
    return reader.fromJson(Serialization.gson().toJsonTree(options), paramsClass);
  }

  private static final Gson noChannelReadsGson = Serialization.gson().newBuilder()
    .registerTypeAdapterFactory(new TypeAdapterFactory() {
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
          public ChannelOwner read(JsonReader in) {
            throw new PlaywrightException("Internal error: params with channel references must be built with Utils.toProtocol(owner, options, paramsClass)");
          }
        };
      }
    }).create();

  static FilePayload toFilePayload(Path file) {
    byte[] buffer;
    try {
      buffer = Files.readAllBytes(file);
    } catch (IOException e) {
      throw new PlaywrightException("Failed to read from file", e);
    }
    return new FilePayload(file.getFileName().toString(), null, buffer);
  }

  static void writeToFile(byte[] buffer, Path path) {
    FileUtils.mkdirIfNeeded(path);
    try (FileOutputStream out = new FileOutputStream(path.toFile())) {
      out.write(buffer);
    } catch (IOException e) {
      throw new PlaywrightException("Failed to write to file", e);
    }
  }

  static void writeToFile(InputStream inputStream, Path path) {
    FileUtils.mkdirIfNeeded(path);
    try (FileOutputStream out = new FileOutputStream(path.toFile())) {
      byte[] buf = new byte[1024 * 1024];
      int length;
      while ((length = inputStream.read(buf)) > 0) {
        out.write(buf, 0, length);
      }
    } catch (IOException e) {
      throw new PlaywrightException("Failed to write to file", e);
    }
  }

  static String toJsRegexFlags(Pattern pattern) {
    String regexFlags = "";
    if ((pattern.flags() & Pattern.CASE_INSENSITIVE) != 0) {
      // Case-insensitive search.
      regexFlags += "i";
    }
    if ((pattern.flags() & Pattern.DOTALL) != 0) {
      // Allows . to match newline characters.
      regexFlags += "s";
    }
    if ((pattern.flags() & Pattern.MULTILINE) != 0) {
      // Multi-line search.
      regexFlags += "m";
    }
    if ((pattern.flags() & ~(Pattern.MULTILINE | Pattern.CASE_INSENSITIVE | Pattern.DOTALL)) != 0) {
      throw new PlaywrightException("Unexpected RegEx flag, only CASE_INSENSITIVE, DOTALL and MULTILINE are supported.");
    }
    return regexFlags;
  }

  static int fromJsRegexFlags(String regexFlags) {
    int flags = 0;
    if (regexFlags.contains("i")) {
      flags |= Pattern.CASE_INSENSITIVE;
    }
    if (regexFlags.contains("s")) {
      flags |= Pattern.DOTALL;
    }
    if (regexFlags.contains("m")) {
      flags |= Pattern.MULTILINE;
    }
    return flags;
  }

  // java: createGuid of utils/crypto.ts, a Node.js utils file outside the client.
  static String createGuid() {
    StringBuffer result = new StringBuffer();
    Random random = new Random();
    for (int i = 0; i < 4; i++) {
      result.append(Integer.toHexString(random.nextInt()));
    }
    return result.toString();
  }
}
