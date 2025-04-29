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
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.ClientCertificate;
import com.microsoft.playwright.options.FilePayload;
import com.microsoft.playwright.options.HttpHeader;

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
import java.util.stream.Collectors;

import static com.microsoft.playwright.impl.Serialization.toJsonArray;
import static java.nio.file.Files.readAllBytes;

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

  static void addFilePathUploadParams(Path[] items, JsonObject params, BrowserContextImpl context) {
    List<Path> localPaths = new ArrayList<>();
    Path localDirectory = resolvePathsAndDirectoryForInputFiles(items, localPaths);
    if (items.length == 0) {
      // FIXME: shouldBeAbleToResetSelectedFilesWithEmptyFileList tesst hangs in Chromium if we pass empty paths list.
      params.add("payloads", new JsonArray());
    } else if (context.connection.isRemote) {
      if (localDirectory != null) {
        localPaths = collectFiles(localDirectory);
      }
      JsonObject json = createTempFiles(context, localDirectory, localPaths);
      JsonArray writableStreams = json.getAsJsonArray("writableStreams");
      JsonArray jsonStreams = copyLocalToTempFiles(context, localPaths, writableStreams);
      if (json.has("rootDir")) {
        params.add("directoryStream", json.get("rootDir"));
      } else {
        params.add("streams", jsonStreams);
      }
    } else {
      if (!localPaths.isEmpty()) {
        params.add("localPaths", toJsonArray(localPaths.toArray(new Path[0])));
      }
      if (localDirectory != null) {
        params.addProperty("localDirectory", localDirectory.toString());
      }
    }
  }

  private static Path resolvePathsAndDirectoryForInputFiles(Path[] items, List<Path> outLocalPaths) {
    Path localDirectory = null;
    try {
      for (Path item : items) {
        if (Files.isDirectory(item)) {
          if (localDirectory != null) {
            throw new PlaywrightException("Multiple directories are not supported");
          }
          localDirectory = item.toRealPath();
        } else {
          outLocalPaths.add(item.toRealPath());
        }
      }
    } catch (IOException e) {
      throw new PlaywrightException("Cannot get absolute file path",  e);
    }
    if (!outLocalPaths.isEmpty() && localDirectory != null) {
      throw new PlaywrightException("File paths must be all files or a single directory");
    }
    return localDirectory;
  }

  private static List<Path> collectFiles(Path localDirectory) {
    try {
      return Files.walk(localDirectory).filter(e -> Files.isRegularFile(e)).collect(Collectors.toList());
    } catch (IOException e) {
      throw new PlaywrightException("Failed to traverse directory", e);
    }
  }

  private static JsonArray copyLocalToTempFiles(BrowserContextImpl context, List<Path> localPaths, JsonArray writableStreams) {
    JsonArray jsonStreams = new JsonArray();
    for (int i = 0; i < localPaths.size(); i++) {
      JsonObject jsonStream = writableStreams.get(i).getAsJsonObject();
      WritableStream temp = context.connection.getExistingObject(jsonStream.get("guid").getAsString());
      try (OutputStream out = temp.stream()) {
        Files.copy(localPaths.get(i), out);
      } catch (IOException e) {
        throw new PlaywrightException("Failed to copy file to remote server.", e);
      }
      jsonStreams.add(temp.toProtocolRef());
    }
    return jsonStreams;
  }

  private static JsonObject createTempFiles(BrowserContextImpl context, Path localDirectory, List<Path> localPaths) {
    JsonObject tempFilesParams = new JsonObject();
    if (localDirectory != null) {
      tempFilesParams.addProperty("rootDirName", localDirectory.getFileName().toString());
    }
    JsonArray items = new JsonArray();
    for (Path path : localPaths) {
      long lastModifiedMs;
      try {
        lastModifiedMs = Files.getLastModifiedTime(path).toMillis();
      } catch (IOException e) {
        throw new PlaywrightException("Cannot read file timestamp: " + path, e);
      }
      Path name;
      if (localDirectory == null) {
        name = path.getFileName();
      } else {
        name = localDirectory.relativize(path);
      }
      JsonObject item = new JsonObject();
      item.addProperty("name", name.toString());
      item.addProperty("lastModifiedMs", lastModifiedMs);
      items.add(item);
    }
    tempFilesParams.add("items", items);
    return context.sendMessage("createTempFiles", tempFilesParams).getAsJsonObject();
  }

  static void checkFilePayloadSize(FilePayload[] files) {
    long totalSize = 0;
    for (FilePayload file: files) {
      totalSize += file.buffer.length;
    }
    if (totalSize > 50 * 1024 * 1024) {
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

  static String addSourceUrlToScript(String source, Path path) {
    return source + "\n//# sourceURL=" + path.toString().replace("\n", "");
  }

  static void addToProtocol(JsonObject params, List<ClientCertificate> clientCertificateList) {
    if (clientCertificateList == null) {
      return;
    }
    JsonArray clientCertificates = new JsonArray();
    for (ClientCertificate cert: clientCertificateList) {
      JsonObject jsonCert = new JsonObject();
      jsonCert.addProperty("origin", cert.origin);
      try {
        String certBase64 = base64Buffer(cert.cert, cert.certPath);
        if (certBase64 != null) {
          jsonCert.addProperty("cert",  certBase64);
        }
        String keyBase64 = base64Buffer(cert.key, cert.keyPath);
        if (keyBase64 != null) {
          jsonCert.addProperty("key", keyBase64);
        }
        String pfxBase64 = base64Buffer(cert.pfx, cert.pfxPath);
        if (pfxBase64 != null) {
          jsonCert.addProperty("pfx", pfxBase64);
        }
      } catch (IOException e) {
        throw new PlaywrightException("Failed to read from file", e);
      }
      if (cert.passphrase != null) {
        jsonCert.addProperty("passphrase", cert.passphrase);
      }
      clientCertificates.add(jsonCert);
    }
    params.remove("clientCertificates");
    params.add("clientCertificates", clientCertificates);
  }

  private static String base64Buffer(byte[] bytes, Path path) throws IOException {
    if (path != null) {
      bytes = readAllBytes(path);
    }
    if (bytes == null) {
      return null;
    }
    return Base64.getEncoder().encodeToString(bytes);
  }

  static JsonObject interceptionPatterns(List<UrlMatcher> matchers) {
    JsonArray jsonPatterns = new JsonArray();
    for (UrlMatcher matcher: matchers) {
      JsonObject jsonPattern = new JsonObject();
      if (matcher.glob != null) {
        jsonPattern.addProperty("glob", matcher.glob);
      } else if (matcher.pattern != null) {
        jsonPattern.addProperty("regexSource", matcher.pattern.pattern());
        jsonPattern.addProperty("regexFlags", toJsRegexFlags(matcher.pattern));
      } else {
        // Match all requests.
        jsonPattern.addProperty("glob", "**/*");
        jsonPatterns = new JsonArray();
        jsonPatterns.add(jsonPattern);
        break;
      }
      jsonPatterns.add(jsonPattern);
    }
    JsonObject result = new JsonObject();
    result.add("patterns", jsonPatterns);
    return result;
  }
}
