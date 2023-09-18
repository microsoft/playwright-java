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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.FilePayload;
import com.microsoft.playwright.options.HttpHeader;
import com.microsoft.playwright.options.SelectOption;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

import static com.microsoft.playwright.impl.Serialization.toJsonArray;

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

  static <T> T clone(T f) {
    if (f == null) {
      return f;
    }
    return convertType(f, (Class<T>) f.getClass());
  }


  // https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Regular_expressions#escaping
  static Set<Character> escapeGlobChars = new HashSet<>(Arrays.asList('$', '^', '+', '.', '*', '(', ')', '|', '\\', '?', '{', '}', '[', ']'));

  static String globToRegex(String glob) {
    StringBuilder tokens = new StringBuilder();
    tokens.append('^');
    boolean inGroup = false;
    for (int i = 0; i < glob.length(); ++i) {
      char c = glob.charAt(i);
      if (c == '\\' && i + 1 < glob.length()) {
        char nextChar = glob.charAt(++i);
        if (escapeGlobChars.contains(nextChar)) {
          tokens.append('\\');
        }
        tokens.append(nextChar);
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
        case '[':
          tokens.append('[');
          break;
        case ']':
          tokens.append(']');
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
          tokens.append("\\").append(c);
          break;
        default:
          if (escapeGlobChars.contains(c)) {
            tokens.append('\\');
          }
          tokens.append(c);
          break;
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

  static final long maxUploadBufferSize = 50 * 1024 * 1024;

  static boolean hasLargeFile(Path[] files) {
    long totalSize = 0;
    for (Path file: files) {
      try {
        totalSize += Files.size(file);
      } catch (IOException e) {
        throw new PlaywrightException("Cannot get file size.", e);
      }
    }
    return totalSize > maxUploadBufferSize;
  }

  static void addLargeFileUploadParams(Path[] files, JsonObject params, BrowserContextImpl context) {
    if (context.connection.isRemote) {
      List<WritableStream> streams = new ArrayList<>();
      JsonArray jsonStreams = new JsonArray();
      for (Path path : files) {
        WritableStream temp = context.createTempFile(path.getFileName().toString());
        streams.add(temp);
        try (OutputStream out = temp.stream()) {
          Files.copy(path, out);
        } catch (IOException e) {
          throw new PlaywrightException("Failed to copy file to remote server.", e);
        }
        jsonStreams.add(temp.toProtocolRef());
      }
      params.add("streams", jsonStreams);
    } else {
      Path[] absolute = Arrays.stream(files).map(f -> {
        try {
          return f.toRealPath();
        } catch (IOException e) {
          throw new PlaywrightException("Cannot get absolute file path", e);
        }
      }).toArray(Path[]::new);
      params.add("localPaths", toJsonArray(absolute));
    }
  }

  static void checkFilePayloadSize(FilePayload[] files) {
    long totalSize = 0;
    for (FilePayload file: files) {
      totalSize += file.buffer.length;
    }
    if (totalSize > maxUploadBufferSize) {
      throw new PlaywrightException("Cannot set buffer larger than 50Mb, please write it to a file and pass its path instead.");
    }
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
          throw new PlaywrightException("Failed to create parent directory: " + dir, e);
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
      byte[] buf = new byte[1024 * 1024];
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

  static List<HttpHeader> toHeadersList(Map<String, String> headers) {
    List<HttpHeader> list = new ArrayList<>();
    for (Map.Entry<String, String> entry: headers.entrySet()) {
      HttpHeader header = new HttpHeader();
      header.name = entry.getKey();
      header.value = entry.getValue();
      list.add(header);
    }
    return list;
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
}
