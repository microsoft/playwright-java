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

package com.microsoft.playwright;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Assumptions;

import java.io.*;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Utils {
  private static final AtomicInteger nextUnusedPort = new AtomicInteger(9000);

  private static boolean available(int port) {
    try (ServerSocket ignored = new ServerSocket(port)) {
      return true;
    } catch (IOException ignored) {
      return false;
    }
  }

  static int nextFreePort() {
    for (int i = 0; i < 100; i++) {
      int port = nextUnusedPort.getAndIncrement();
      if (available(port)) {
        return port;
      }
    }
    throw new RuntimeException("Cannot find free port: " + nextUnusedPort.get());
  }

  static void assertJsonEquals(Object expected, Object actual) {
    assertJsonEquals(new Gson().toJson(expected), actual);
  }

  static void assertJsonEquals(String expected, Object actual) {
    JsonElement actualJson = JsonParser.parseString(new Gson().toJson(actual));
    assertEquals(JsonParser.parseString(expected), actualJson);
  }

  @SuppressWarnings("unchecked")
  static <K,V> Map<K, V> mapOf(Object... entries) {
    Map result = new HashMap();
    for (int i = 0; i + 1 < entries.length; i += 2) {
      result.put(entries[i], entries[i + 1]);
    }
    return result;
  }

  static Frame attachFrame(Page page, String name, String url) {
    JSHandle handle = page.evaluateHandle("async ({ frameId, url }) => {\n" +
      "  const frame = document.createElement('iframe');\n" +
      "  frame.src = url;\n" +
      "  frame.id = frameId;\n" +
      "  document.body.appendChild(frame);\n" +
      "  await new Promise(x => frame.onload = x);\n" +
      "  return frame;\n" +
      "}", mapOf("frameId", name, "url", url));
    return handle.asElement().contentFrame();
  }

  static void copy(InputStream in, OutputStream out) throws IOException {
    byte[] buffer = new byte[8192];
    int read;
    while ((read = in.read(buffer, 0, 8192)) != -1) {
      out.write(buffer, 0, read);
    }
  }

  static Map<String, byte[]> parseZip(Path trace) throws IOException {
    Map<String, byte[]> entries = new HashMap<>();
    try (ZipInputStream zis = new ZipInputStream(new FileInputStream(trace.toFile()))) {
      for (ZipEntry zipEntry = zis.getNextEntry(); zipEntry != null; zipEntry = zis.getNextEntry()) {
        ByteArrayOutputStream content = new ByteArrayOutputStream();
        try (OutputStream output = content) {
          copy(zis, output);
        }
        entries.put(zipEntry.getName(), content.toByteArray());
      }
      zis.closeEntry();
    }
    return entries;
  }

  static Map<String, byte[]> extractZip(Path zipPath, Path toDir) throws IOException {
    Map<String, byte[]> entries = new HashMap<>();
    Files.createDirectories(toDir);
    try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipPath.toFile()))) {
      for (ZipEntry zipEntry = zis.getNextEntry(); zipEntry != null; zipEntry = zis.getNextEntry()) {
        Path toPath = toDir.resolve(zipEntry.getName());
        if (!toPath.normalize().startsWith(toDir.normalize())) {
          throw new IOException("Bad zip entry");
        }
        if (zipEntry.isDirectory()) {
          Files.createDirectories(toPath);
        } else {
          Files.copy(zis, toPath);
        }
        zis.closeEntry();
      }
    }
    return entries;
  }


  enum OS { WINDOWS, MAC, LINUX, UNKNOWN }
  static OS getOS() {
    String name = System.getProperty("os.name").toLowerCase();
    if (name.contains("win")) {
      return OS.WINDOWS;
    }
    if (name.contains("linux")) {
      return OS.LINUX;
    }
    if (name.contains("mac os x")) {
      return OS.MAC;
    }
    return OS.UNKNOWN;
  }

  static int osVersion() {
    return Integer.parseInt(System.getProperty("os.version").split("\\.")[0]);
  }

  static List<String> expectedSSLError(String browserName) {
    switch (browserName) {
      case "chromium":
        switch (getOS()) {
          case MAC:
            return Arrays.asList("net::ERR_CERT_INVALID", "net::ERR_CERT_AUTHORITY_INVALID");
          default:
            return Arrays.asList("net::ERR_CERT_AUTHORITY_INVALID");
        }
      case "webkit": {
        switch (getOS()) {
          case MAC:
            return Arrays.asList("The certificate for this server is invalid");
          case WINDOWS:
            return Arrays.asList("SSL peer certificate or SSH remote key was not OK", "SSL connect error");
          default:
            return Arrays.asList("Unacceptable TLS certificate", "Server required TLS certificate");
        }
      }
      default:
        return Arrays.asList("SSL_ERROR_UNKNOWN");
    }
  }

  static String getBrowserNameFromEnv() {
    String browserName = System.getenv("BROWSER");
    if (browserName == null) {
      browserName = "chromium";
    }
    return browserName;
  }

  static BrowserType getBrowserTypeFromEnv(Playwright playwright) {
    String browserName = getBrowserNameFromEnv();
    switch (browserName) {
      case "webkit":
        return playwright.webkit();
      case "firefox":
        return playwright.firefox();
      case "chromium":
        return playwright.chromium();
      default:
        throw new IllegalArgumentException("Unknown browser: " + browserName);
    }
  }

  static void verifyViewport(Page page, int width, int height) {
    assertEquals(width, page.viewportSize().width);
    assertEquals(height, page.viewportSize().height);
    assertEquals(width, page.evaluate("window.innerWidth"));
    assertEquals(height, page.evaluate("window.innerHeight"));
  }

  static String generateDifferentOriginScheme(final Server server){
    return server.PREFIX.startsWith("http://") ?
      server.PREFIX.replace("http://", "https://") :
      server.PREFIX.replace("https://", "http://");
  }

  static String generateDifferentOriginHostname(final Server server){
    return server.PREFIX.replace("localhost", "mismatching-hostname");
  }

  static String generateDifferentOriginPort(final Server server){
    return server.PREFIX.replace(String.valueOf(server.PORT), String.valueOf(server.PORT+1));
  }

  static Path relativePathOrSkipTest(Path path) {
    Path cwd = Paths.get("").toAbsolutePath();
    try  {
      return cwd.relativize(path.toAbsolutePath());
    } catch (IllegalArgumentException e) {
      // May happen on Windows when the path and temp are on different disks.
      if (e.getMessage().contains("has different root")) {
        Assumptions.assumeTrue(false, "cwd is on another disk, skipping the test.");
      }
      throw e;
    }
  }
}
