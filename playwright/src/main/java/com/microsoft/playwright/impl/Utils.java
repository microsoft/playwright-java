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

import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.FilePayload;
import com.microsoft.playwright.options.HttpHeader;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

class Utils {
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
    return new FilePayload(file.getFileName().toString(), null, buffer);
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
