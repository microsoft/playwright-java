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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Route;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class RouteImpl extends ChannelOwner implements Route {
  public RouteImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
  }

  @Override
  public void abort(String errorCode) {
    JsonObject params = new JsonObject();
    params.addProperty("errorCode", errorCode);
    sendMessage("abort", params);
  }

  @Override
  public void continue_(ContinueOverrides overrides) {
    if (overrides == null) {
      overrides = new ContinueOverrides();
    }
    JsonObject params = new JsonObject();
    if (overrides.url != null) {
      params.addProperty("url", overrides.url);
    }
    if (overrides.method != null) {
      params.addProperty("method", overrides.method);
    }
    if (overrides.headers != null) {
      params.add("headers", Serialization.toProtocol(overrides.headers));
    }
    if (overrides.postData != null) {
      String base64 = Base64.getEncoder().encodeToString(overrides.postData);
      params.addProperty("postData", base64);
    }
    sendMessage("continue", params);
  }

  @Override
  public void fulfill(FulfillResponse response) {
    if (response == null) {
      response = new FulfillResponse();
    }

    int status = response.status == 0 ? 200 : response.status;
    String body = "";
    boolean isBase64 = false;
    int length = 0;
    if (response.path != null) {
      try {
         byte[] buffer = Files.readAllBytes(response.path);
         body = Base64.getEncoder().encodeToString(buffer);
         isBase64 = true;
         length = buffer.length;
      } catch (IOException e) {
        throw new PlaywrightException("Failed to read from file: " + response.path, e);
      }
    } else if (response.body != null) {
      body = response.body;
      isBase64 = false;
      length = body.getBytes().length;
    } else if (response.bodyBytes != null) {
      body = Base64.getEncoder().encodeToString(response.bodyBytes);
      isBase64 = true;
      length = response.bodyBytes.length;
    }

    Map<String, String> headers = new LinkedHashMap<>();
    if (response.headers != null) {
      for (Map.Entry<String, String> h : response.headers.entrySet()) {
        headers.put(h.getKey().toLowerCase(), h.getValue());
      }
    }
    if (response.contentType != null) {
      headers.put("content-type", response.contentType);
    } else if (response.path != null) {
      headers.put("content-type", Utils.mimeType(response.path));
    }
    if (length != 0 && !headers.containsKey("content-length")) {
      headers.put("content-length", Integer.toString(length));
    }
    JsonObject params = new JsonObject();
    params.addProperty("status", status);
    params.add("headers", Serialization.toProtocol(headers));
    params.addProperty("isBase64", isBase64);
    params.addProperty("body", body);
    sendMessage("fulfill", params);
  }

  @Override
  public Request request() {
    return connection.getExistingObject(initializer.getAsJsonObject("request").get("guid").getAsString());
  }
}
