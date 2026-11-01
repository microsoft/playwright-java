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
import com.microsoft.playwright.options.HttpHeader;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.microsoft.playwright.impl.ChannelOwner.NO_TIMEOUT;
import static com.microsoft.playwright.impl.LoggingSupport.*;

public class HARRouter {
  private final LocalUtils localUtils;
  private final HarNotFound defaultAction;
  private final String harId;

  HARRouter(LocalUtils localUtils, Path harFile, HarNotFound defaultAction) {
    this.localUtils = localUtils;
    this.defaultAction = defaultAction;

    LocalUtilsHarOpenParams params = new LocalUtilsHarOpenParams();
    params.file = harFile.toString();
    LocalUtilsHarOpenResult result = localUtils.channel.harOpen(params, NO_TIMEOUT);
    if (result.error != null) {
      throw new PlaywrightException(result.error);
    }
    harId = result.harId;
  }

  void handle(Route route) {
    Request request = route.request();

    LocalUtilsHarLookupParams params = new LocalUtilsHarLookupParams();
    params.harId = harId;
    params.url = request.url();
    params.method = request.method();
    params.headers = new ArrayList<>();
    for (HttpHeader header : request.headersArray()) {
      NameValue nv = new NameValue();
      nv.name = header.name;
      nv.value = header.value;
      params.headers.add(nv);
    }
    if (request.postDataBuffer() != null) {
      params.postData = request.postDataBuffer();
    }
    params.isNavigationRequest = request.isNavigationRequest();
    LocalUtilsHarLookupResult response = localUtils.channel.harLookup(params, NO_TIMEOUT);

    String action = response.action;
    if ("redirect".equals(action)) {
      String redirectURL = response.redirectURL;
      logApiIfEnabled("HAR: " + route.request().url() + " redirected to " + redirectURL);
      ((RouteImpl) route).redirectNavigationRequest(redirectURL);
      return;
    }

    if ("fulfill".equals(action)) {
      int status = response.status;
      // If the response status is -1, the request was canceled or stalled, so we just stall it here.
      // See https://github.com/microsoft/playwright/issues/29311.
      // TODO: it'd be better to abort such requests, but then we likely need to respect the timing,
      // because the request might have been stalled for a long time until the very end of the
      // test when HAR was recorded but we'd abort it immediately.
      if (status == -1) {
        return;
      }
      Map<String, String> headers = mergeSetCookieHeaders(response.headers);
      byte[] buffer = response.body;
      route.fulfill(new Route.FulfillOptions()
        .setStatus(status)
        .setHeaders(headers)
        .setBodyBytes(buffer));
      return;
    }

    if ("error".equals(action)) {
      logApiIfEnabled("HAR: " + response.message);
      // Report the error, but fall through to the default handler.
    }

    if (defaultAction == HarNotFound.FALLBACK) {
      route.fallback();
      return;
    }

    // By default abort not matching requests.
    route.abort();
  }

  private static Map<String, String> mergeSetCookieHeaders(List<NameValue> headersArray) {
    Map<String, String> result = new java.util.LinkedHashMap<>();
    for (NameValue pair : headersArray) {
      String name = pair.name;
      String value = pair.value;
      if ("set-cookie".equalsIgnoreCase(name)) {
        if (!result.containsKey("set-cookie")) {
          result.put("set-cookie", value);
        } else {
          result.put("set-cookie", result.get("set-cookie") + "\n" + value);
        }
      } else {
        result.put(name, value);
      }
    }
    return result;
  }

  void dispose() {
    LocalUtilsHarCloseParams params = new LocalUtilsHarCloseParams();
    params.harId = harId;
    localUtils.sendMessageAsync("harClose", params);
  }
}
