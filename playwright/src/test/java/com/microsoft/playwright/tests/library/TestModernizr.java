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

package com.microsoft.playwright.tests.library;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.EnabledOnBrowser;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.HttpsTestServer;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.condition.OS;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;

import static com.microsoft.playwright.fixtures.Utils.asset;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/library/modernizr.spec.ts. */
public class TestModernizr {
  private static class Features {
    JsonObject actual;
    JsonObject expected;
  }

  private static Features checkFeatures(String name, BrowserContext context, HttpsTestServer server) throws IOException {
    try {
      Page page = context.newPage();
      page.navigate(server.PREFIX + "/modernizr/index.html");
      Object actual = page.evaluate("window.report");
      String expected = new String(Files.readAllBytes(asset("modernizr/" + name + ".json")), StandardCharsets.UTF_8);
      Features features = new Features();
      // Java: the values are compared as JSON trees.
      features.actual = new Gson().toJsonTree(actual).getAsJsonObject();
      features.expected = JsonParser.parseString(expected).getAsJsonObject();
      return features;
    } finally {
      context.close();
    }
  }

  // Java: stands for `!!value` of a property of a JSON object.
  private static boolean truthy(JsonObject object, String key) {
    JsonElement value = object.get(key);
    if (value == null || value.isJsonNull()) {
      return false;
    }
    if (value.isJsonPrimitive()) {
      if (value.getAsJsonPrimitive().isBoolean()) {
        return value.getAsBoolean();
      }
      if (value.getAsJsonPrimitive().isNumber()) {
        return value.getAsDouble() != 0;
      }
      return !value.getAsString().isEmpty();
    }
    return true;
  }

  @PlaywrightTest
  @DisplayName("CA-030 Safari Desktop")
  @EnabledOnBrowser(BrowserName.WEBKIT)
  @Fixme(value = "driver: the WebKit of driver 1.64.1 supports <input type=month> (inputtypes.month is true), the upstream 1.65 assets/modernizr/safari-26.json expects false", os = OS.LINUX)
  void safariDesktop(Browser browser, BrowserName browserName, HttpsTestServer httpsServer) throws IOException {
    assumeFalse(browserName == BrowserName.WEBKIT && Env.platform().equals("darwin") && Arrays.asList("x86_64", "amd64").contains(System.getProperty("os.arch")), "Modernizr uses WebGL which is not available on Intel macOS - https://bugs.webkit.org/show_bug.cgi?id=278277");
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setDeviceScaleFactor(2)
      .setIgnoreHTTPSErrors(true));
    Features features = checkFeatures("safari-26", context, httpsServer);
    JsonObject actual = features.actual;
    JsonObject expected = features.expected;

    // Shipping Safari exposes `font-display` as a settable CSS property (element.style.fontDisplay === ''),
    // but open-source WebKit keeps it a @font-face descriptor only, so it is undefined here. No runtime
    // flag bridges the gap, hence the override.
    expected.addProperty("fontdisplay", false);

    expected.remove("webglextensions");
    actual.remove("webglextensions");
    expected.addProperty("audio", truthy(expected, "audio"));
    actual.addProperty("audio", truthy(actual, "audio"));
    expected.addProperty("video", truthy(expected, "video"));
    actual.addProperty("video", truthy(actual, "video"));

    if (Env.platform().equals("linux") || "webkit-wsl".equals(Env.channel())) {
      expected.addProperty("speechrecognition", false);
      expected.addProperty("mediastream", false);
      expected.addProperty("todataurlwebp", true);

      // GHA
      actual.remove("variablefonts");
      expected.remove("variablefonts");
    }

    if (Env.platform().equals("win32") && !"webkit-wsl".equals(Env.channel())) {
      expected.addProperty("getusermedia", false);
      expected.addProperty("peerconnection", false);
      expected.addProperty("speechrecognition", false);
      expected.addProperty("speechsynthesis", false);
      expected.addProperty("todataurlwebp", true);
      expected.addProperty("webaudio", false);
      expected.addProperty("gamepads", false);

      expected.remove("datalistelem");

      expected.addProperty("mediastream", false);
      expected.addProperty("mediasource", false);
      expected.addProperty("datachannel", false);

      expected.getAsJsonObject("inputtypes").addProperty("color", false);
      expected.getAsJsonObject("inputtypes").addProperty("date", false);
      expected.getAsJsonObject("inputtypes").addProperty("datetime-local", false);
      expected.getAsJsonObject("inputtypes").addProperty("time", false);
    }

    assertEquals(expected, actual);
  }
}
