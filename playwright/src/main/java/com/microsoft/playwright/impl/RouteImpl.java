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

import com.google.gson.JsonObject;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Route;

import java.util.Base64;

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
  }

  @Override
  public Request request() {
    return connection.getExistingObject(initializer.getAsJsonObject("request").get("guid").getAsString());
  }
}
