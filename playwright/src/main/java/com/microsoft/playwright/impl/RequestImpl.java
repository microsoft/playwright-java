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

package com.microsoft.playwright.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Response;

import java.util.HashMap;
import java.util.Map;

public class RequestImpl extends ChannelOwner implements Request {
  private RequestImpl redirectedFrom;
  private RequestImpl redirectedTo;
  private final Map<String, String> headers = new HashMap<>();
  RequestFailure failureText;

  RequestImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);

    if (initializer.has("redirectedFrom")) {
      redirectedFrom = connection.getExistingObject(initializer.getAsJsonObject("redirectedFrom").get("guid").getAsString());
      redirectedFrom.redirectedTo = this;
    }
    for (JsonElement e : initializer.getAsJsonArray("headers")) {
      JsonObject item = e.getAsJsonObject();
      headers.put(item.get("name").getAsString().toLowerCase(), item.get("value").getAsString());
    }
  }

  @Override
  public RequestFailure failure() {
    return null;
  }

  @Override
  public Frame frame() {
    return connection.getExistingObject(initializer.getAsJsonObject("frame").get("guid").getAsString());
  }

  @Override
  public Map<String, String> headers() {
    return headers;
  }

  @Override
  public boolean isNavigationRequest() {
    return initializer.get("isNavigationRequest").getAsBoolean();
  }

  @Override
  public String method() {
    return initializer.get("method").getAsString();
  }

  @Override
  public String postData() {
    return null;
  }

  @Override
  public byte[] postDataBuffer() {
    return new byte[0];
  }

  @Override
  public RequestPostDataJSON postDataJSON() {
    return null;
  }

  @Override
  public Request redirectedFrom() {
    return redirectedFrom;
  }

  @Override
  public Request redirectedTo() {
    return redirectedTo;
  }

  @Override
  public String resourceType() {
    return initializer.get("resourceType").getAsString();
  }

  @Override
  public Response response() {
    JsonObject result = sendMessage("response").getAsJsonObject();
    if (!result.has("response")) {
      return null;
    }
    return connection.getExistingObject(result.getAsJsonObject("response").get("guid").getAsString());
  }

  @Override
  public String url() {
    return initializer.get("url").getAsString();
  }

  Request finalRequest() {
    return redirectedTo != null ? redirectedTo.finalRequest() : this;
  }

}
