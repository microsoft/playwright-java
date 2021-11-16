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

import com.google.gson.*;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.FilePayload;
import com.microsoft.playwright.options.HttpHeader;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

class Utils {
  static <F, T> T convertViaReflection(F f, Class<T> t) {
    if (f == null) {
      return null;
    }

    try {
      T result = t.getDeclaredConstructor().newInstance();
      for (Field toField : t.getDeclaredFields()) {
        if (Modifier.isStatic(toField.getModifiers()) ||
          !Modifier.isPublic(toField.getModifiers())) {
          continue;
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

  // TODO: generate converter.
  static <F, T> T convertViaJson(F f, Class<T> t) {
    Gson gson = new GsonBuilder()
      // Necessary to avoid access to private fields/classes,
      // see https://github.com/microsoft/playwright-java/issues/423
      .registerTypeAdapter(Optional.class, new OptionalSerializer())
      .create();
    String json = gson.toJson(f);
    return gson.fromJson(json, t);
  }

  private static class OptionalSerializer implements JsonSerializer<Optional> {
    @Override
    public JsonElement serialize(Optional src, Type typeOfSrc, JsonSerializationContext context) {
      JsonObject result = new JsonObject();
      if (src.isPresent()) {
        result.add("value", context.serialize(src.get()));
      }
      return result;
    }
  }

  static Set<Character> escapeGlobChars = new HashSet<>(Arrays.asList('/', '$', '^', '+', '.', '(', ')', '=', '!', '|'));

  static String globToRegex(String glob) {
    StringBuilder tokens = new StringBuilder();
    tokens.append('^');
    boolean inGroup = false;
    for (int i = 0; i < glob.length(); ++i) {
      char c = glob.charAt(i);
      if (escapeGlobChars.contains(c)) {
        tokens.append("\\" + c);
        continue;
      }
      if (c == '*') {
        boolean beforeDeep = i < 1 || glob.charAt(i - 1) == '/';
        int starCount = 1;
        while (i + 1 < glob.length() && glob.charAt(i + 1) == '*') {
          starCount++;
          i++;
        }
        boolean afterDeep = i + 1 >= glob.length() || glob.charAt(i + 1) == '/';
        boolean isDeep = starCount > 1 && beforeDeep && afterDeep;
        if (isDeep) {
          tokens.append("((?:[^/]*(?:\\/|$))*)");
          i++;
        } else {
          tokens.append("([^/]*)");
        }
        continue;
      }

      switch (c) {
        case '?':
          tokens.append('.');
          break;
        case '{':
          inGroup = true;
          tokens.append('(');
          break;
        case '}':
          inGroup = false;
          tokens.append(')');
          break;
        case ',':
          if (inGroup) {
            tokens.append('|');
            break;
          }
          tokens.append("\\" + c);
          break;
        default:
          tokens.append(c);
      }
    }
    tokens.append('$');
    return tokens.toString();
  }

  static String mimeType(Path path) {
    String mimeType;
    try {
      mimeType = Files.probeContentType(path);
    } catch (IOException e) {
      throw new PlaywrightException("Failed to determine mime type", e);
    }
    if (mimeType == null) {
      mimeType = "application/octet-stream";
    }
    return mimeType;
  }

  static FilePayload[] toFilePayloads(Path[] files) {
    List<FilePayload> payloads = new ArrayList<>();
    for (Path file : files) {
      payloads.add(toFilePayload(file));
    }
    return payloads.toArray(new FilePayload[0]);
  }

  static FilePayload toFilePayload(Path file) {
    byte[] buffer;
    try {
      buffer = Files.readAllBytes(file);
    } catch (IOException e) {
      throw new PlaywrightException("Failed to read from file", e);
    }
    return new FilePayload(file.getFileName().toString(), mimeType(file), buffer);
  }

  static void mkParentDirs(Path file) {
    Path dir = file.getParent();
    if (dir != null) {
      if (!Files.exists(dir)) {
        try {
          Files.createDirectories(dir);
        } catch (IOException e) {
          throw new PlaywrightException("Failed to create parent directory: " + dir.toString(), e);
        }
      }
    }
  }

  static void writeToFile(byte[] buffer, Path path) {
    mkParentDirs(path);
    try (FileOutputStream out = new FileOutputStream(path.toFile())) {
      out.write(buffer);
    } catch (IOException e) {
      throw new PlaywrightException("Failed to write to file", e);
    }
  }

  static void writeToFile(InputStream inputStream, Path path) {
    mkParentDirs(path);
    try (FileOutputStream out = new FileOutputStream(path.toFile())) {
      byte[] buf = new byte[8192];
      int length;
      while ((length = inputStream.read(buf)) > 0) {
        out.write(buf, 0, length);
      }
    } catch (IOException e) {
      throw new PlaywrightException("Failed to write to file", e);
    }
  }

  static boolean isSafeCloseError(PlaywrightException exception) {
    return isSafeCloseError(exception.getMessage());
  }

  static boolean isSafeCloseError(String error) {
    return error.contains("Browser has been closed") || error.contains("Target page, context or browser has been closed");
  }

  static String createGuid() {
    StringBuffer result = new StringBuffer();
    Random random = new Random();
    for (int i = 0; i < 4; i++) {
      result.append(Integer.toHexString(random.nextInt()));
    }
    return result.toString();
  }

  static Map<String, String> toHeadersMap(List<HttpHeader> headers) {
    Map<String, String> map = new LinkedHashMap<>();
    for (HttpHeader header: headers) {
      map.put(header.name.toLowerCase(), header.value);
    }
    return map;
  }
}
