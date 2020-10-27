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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Response;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class ResponseImpl extends ChannelOwner implements Response {
  private final Map<String, String> headers = new HashMap<>();

  ResponseImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);

    for (JsonElement e : initializer.getAsJsonArray("headers")) {
      JsonObject item = e.getAsJsonObject();
      headers.put(item.get("name").getAsString().toLowerCase(), item.get("value").getAsString());
    }
  }

  @Override
  public byte[] body() {
    JsonObject json = sendMessage("body").getAsJsonObject();
    return Base64.getDecoder().decode(json.get("binary").getAsString());
  }

  @Override
  public String finished() {
    JsonObject json = sendMessage("finished").getAsJsonObject();
    if (json.has("error")) {
      return json.get("error").getAsString();
    }
    return null;
  }

  @Override
  public Frame frame() {
    return request().frame();
  }

  @Override
  public Map<String, String> headers() {
    return headers;
  }

  @Override
  public boolean ok() {
    return status() == 0 || (status() >= 200 && status() <= 299);
  }

  @Override
  public Request request() {
    return connection.getExistingObject(initializer.getAsJsonObject("request").get("guid").getAsString());
  }

  @Override
  public int status() {
    return initializer.get("status").getAsInt();
  }

  @Override
  public String statusText() {
    return initializer.get("statusText").getAsString();
  }

  @Override
  public String text() {
    return new String(body(), StandardCharsets.UTF_8);
  }

  @Override
  public String url() {
    return initializer.get("url").getAsString();
  }
}
