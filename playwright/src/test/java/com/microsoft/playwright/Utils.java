/**
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

import java.util.HashMap;
import java.util.Map;

class Utils {
  static Map mapOf(Object... entries) {
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

  enum OS { WINDOWS, MAC, LINUX, UNKNOWN }
  static OS getOS() {
    String name = System.getProperty("os.name").toLowerCase();
    System.out.println(name);
    if (name.contains("win")) {
      return OS.WINDOWS;
    }
    if (name.contains("linux")) {
      return OS.LINUX;
    }
    if (name.contains("mac")) {
      return OS.MAC;
    }
    return OS.UNKNOWN;
  }

  static String expectedSSLError(String browserName) {
    switch (browserName) {
      case "chromium":
        return "net::ERR_CERT_AUTHORITY_INVALID";
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
