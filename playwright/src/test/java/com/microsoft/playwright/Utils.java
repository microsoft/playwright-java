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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Utils {
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

  static String expectedSSLError(String browserName) {
    switch (browserName) {
      case "chromium":
        switch (getOS()) {
          case MAC:
            return "net::ERR_CERT_INVALID";
          default:
            return "net::ERR_CERT_AUTHORITY_INVALID";
        }
      case "webkit": {
        switch (getOS()) {
          case MAC:
            return "The certificate for this server is invalid";
          case WINDOWS:
            return "SSL peer certificate or SSH remote key was not OK";
          default:
            return "Unacceptable TLS certificate";
        }
      }
      default:
        return "SSL_ERROR_UNKNOWN";
    }
  }
}
