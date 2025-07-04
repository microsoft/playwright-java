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

import com.google.gson.JsonObject;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.options.HarNotFound;

import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;

import static com.microsoft.playwright.impl.ChannelOwner.NO_TIMEOUT;
import static com.microsoft.playwright.impl.LoggingSupport.*;
import static com.microsoft.playwright.impl.Serialization.fromNameValues;
import static com.microsoft.playwright.impl.Serialization.gson;

public class HARRouter {
  private final LocalUtils localUtils;
  private final HarNotFound defaultAction;
  private final String harId;

  HARRouter(LocalUtils localUtils, Path harFile, HarNotFound defaultAction) {
    this.localUtils = localUtils;
    this.defaultAction = defaultAction;

    JsonObject params = new JsonObject();
    params.addProperty("file", harFile.toString());
    JsonObject json = localUtils.sendMessage("harOpen", params, NO_TIMEOUT).getAsJsonObject();
    if (json.has("error")) {
      throw new PlaywrightException(json.get("error").getAsString());
    }
    harId = json.get("harId").getAsString();
  }

  void handle(Route route) {
    Request request = route.request();

    JsonObject params = new JsonObject();
    params.addProperty("harId", harId);
    params.addProperty("url", request.url());
    params.addProperty("method", request.method());
    params.add("headers", gson().toJsonTree(request.headersArray()));
    if (request.postDataBuffer() != null) {
      String base64 = Base64.getEncoder().encodeToString(request.postDataBuffer());
      params.addProperty("postData", base64);
    }
    params.addProperty("isNavigationRequest", request.isNavigationRequest());
    JsonObject response = localUtils.sendMessage("harLookup", params, NO_TIMEOUT).getAsJsonObject();

    String action = response.get("action").getAsString();
    if ("redirect".equals(action)) {
      String redirectURL = response.get("redirectURL").getAsString();
      logApiIfEnabled("HAR: " + route.request().url() + " redirected to " + redirectURL);
      ((RouteImpl) route).redirectNavigationRequest(redirectURL);
      return;
    }

    if ("fulfill".equals(action)) {
      int status = response.get("status").getAsInt();
      // If the response status is -1, the request was canceled or stalled, so we just stall it here.
      // See https://github.com/microsoft/playwright/issues/29311.
      // TODO: it'd be better to abort such requests, but then we likely need to respect the timing,
      // because the request might have been stalled for a long time until the very end of the
      // test when HAR was recorded but we'd abort it immediately.
      if (status == -1) {
        return;
      }
      Map<String, String> headers = fromNameValues(response.getAsJsonArray("headers"));
      byte[] buffer = Base64.getDecoder().decode(response.get("body").getAsString());
      route.fulfill(new Route.FulfillOptions()
        .setStatus(status)
        .setHeaders(headers)
        .setBodyBytes(buffer));
      return;
    }

    if ("error".equals(action)) {
      logApiIfEnabled("HAR: " + response.get("message").getAsString());
      // Report the error, but fall through to the default handler.
    }

    if (defaultAction == HarNotFound.FALLBACK) {
      route.fallback();
      return;
    }

    // By default abort not matching requests.
    route.abort();
  }

  void dispose() {
    JsonObject params = new JsonObject();
    params.addProperty("harId", harId);
    localUtils.sendMessageAsync("harClose", params);
  }
}
