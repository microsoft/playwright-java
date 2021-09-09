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
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.HttpHeader;
import com.microsoft.playwright.options.SecurityDetails;
import com.microsoft.playwright.options.ServerAddr;
import com.microsoft.playwright.options.Timing;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.microsoft.playwright.impl.Serialization.gson;
import static com.microsoft.playwright.impl.Utils.toHeadersMap;
import static java.util.Arrays.asList;

public class ResponseImpl extends ChannelOwner implements Response {
  private final Map<String, String> headers = new HashMap<>();
  private List<HttpHeader> rawHeaders;
  private final RequestImpl request;

  ResponseImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);

    for (JsonElement e : initializer.getAsJsonArray("headers")) {
      JsonObject item = e.getAsJsonObject();
      headers.put(item.get("name").getAsString().toLowerCase(), item.get("value").getAsString());
    }

    request = connection.getExistingObject(initializer.getAsJsonObject("request").get("guid").getAsString());
    request.timing = gson().fromJson(initializer.get("timing"), Timing.class);
  }

  @Override
  public Map<String, String> allHeaders() {
    return withLogging("Response.allHeaders", () -> toHeadersMap(getRawHeaders()));
  }

  @Override
  public byte[] body() {
    return withLogging("Response.body", () -> {
      JsonObject json = sendMessage("body").getAsJsonObject();
      return Base64.getDecoder().decode(json.get("binary").getAsString());
    });
  }

  @Override
  public String finished() {
    List<Waitable<String>> waitables = new ArrayList<>();
    waitables.add(new WaitableNever<String>() {
      @Override
      public boolean isDone() {
        return request.didFailOrFinish;
      }
      @Override
      public String get() {
        return request.failure();
      }
    });
    PageImpl page = request.frame().page;
    waitables.add(page.createWaitForCloseHelper());
    waitables.add(page.createWaitableTimeout(null));
    runUntil(() -> {}, new WaitableRace<>(waitables));
    return request.failure();
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
  public List<HttpHeader> headersArray() {
    return withLogging("Response.headersArray", () -> getRawHeaders());
  }

  @Override
  public boolean ok() {
    return status() == 0 || (status() >= 200 && status() <= 299);
  }

  @Override
  public RequestImpl request() {
    return request;
  }

  @Override
  public SecurityDetails securityDetails() {
    return withLogging("Response.securityDetails", () -> {
      JsonObject json = sendMessage("securityDetails").getAsJsonObject();
      if (json.has("value")) {
        return gson().fromJson(json.get("value"), SecurityDetails.class);
      }
      return null;
    });
  }

  @Override
  public ServerAddr serverAddr() {
    return withLogging("Response.serverAddr", () -> {
      JsonObject json = sendMessage("serverAddr").getAsJsonObject();
      if (json.has("value")) {
        return gson().fromJson(json.get("value"), ServerAddr.class);
      }
      return null;
    });
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

  private List<HttpHeader> getRawHeaders() {
    if (rawHeaders == null) {
      JsonObject json = sendMessage("rawResponseHeaders").getAsJsonObject();
      rawHeaders = asList(gson().fromJson(json.getAsJsonArray("headers"), HttpHeader[].class));
    }
    return rawHeaders;
  }
}
