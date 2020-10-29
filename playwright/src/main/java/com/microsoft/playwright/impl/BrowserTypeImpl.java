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

package com.microsoft.playwright.impl;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.PlaywrightException;

import java.io.IOException;
import java.util.Map;

class BrowserTypeImpl extends ChannelOwner implements BrowserType {
  BrowserTypeImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
  }

  @Override
  public BrowserImpl launch(LaunchOptions options) {
    if (options == null) {
      options = new LaunchOptions();
    }
    JsonObject params = new Gson().toJsonTree(options).getAsJsonObject();
    JsonElement result = sendMessage("launch", params);
    return connection.getExistingObject(result.getAsJsonObject().getAsJsonObject("browser").get("guid").getAsString());
  }

  public String executablePath() {
    return initializer.get("executablePath").getAsString();
  }


  private static class ColorSchemeAdapter extends TypeAdapter<LaunchPersistentContextOptions.ColorScheme> {
    @Override
    public void write(JsonWriter out, LaunchPersistentContextOptions.ColorScheme value) throws IOException {
      String stringValue;
      switch (value) {
        case DARK:
          stringValue = "dark";
          break;
        case LIGHT:
          stringValue = "light";
          break;
        case NO_PREFERENCE:
          stringValue = "no-preference";
          break;
        default:
          throw new PlaywrightException("Unexpected value: " + value);
      }
      out.value(stringValue);
    }

    @Override
    public LaunchPersistentContextOptions.ColorScheme read(JsonReader in) throws IOException {
      String value = in.nextString();
      switch (value) {
        case "dark": return LaunchPersistentContextOptions.ColorScheme.DARK;
        case "light": return LaunchPersistentContextOptions.ColorScheme.LIGHT;
        case "no-preference": return LaunchPersistentContextOptions.ColorScheme.NO_PREFERENCE;
        default: throw new PlaywrightException("Unexpected value: " + value);
      }
    }
  }

  @Override
  public BrowserContext launchPersistentContext(String userDataDir, LaunchPersistentContextOptions options) {
    if (options == null) {
      options = new LaunchPersistentContextOptions();
    }
    Gson gson = new GsonBuilder().registerTypeAdapter(LaunchPersistentContextOptions.ColorScheme.class, new ColorSchemeAdapter().nullSafe()).create();
    JsonObject params = gson.toJsonTree(options).getAsJsonObject();
    if (options.extraHTTPHeaders != null) {
      params.remove("extraHTTPHeaders");
      params.add("extraHTTPHeaders", Serialization.toProtocol(options.extraHTTPHeaders));
    }
    params.addProperty("userDataDir", userDataDir);
    JsonObject json = sendMessage("launchPersistentContext", params).getAsJsonObject();
    return connection.getExistingObject(json.getAsJsonObject("context").get("guid").getAsString());
  }

  public String name() {
    return initializer.get("name").getAsString();
  }

}
