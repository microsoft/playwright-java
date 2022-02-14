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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.HttpHeader;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static com.microsoft.playwright.impl.Serialization.gson;
import static com.microsoft.playwright.impl.Utils.isSafeCloseError;
import static java.util.Arrays.asList;

class APIResponseImpl implements APIResponse {
  final APIRequestContextImpl context;
  private final JsonObject initializer;
  private final RawHeaders headers;

  APIResponseImpl(APIRequestContextImpl apiRequestContext, JsonObject response) {
    context = apiRequestContext;
    initializer = response;
    headers = new RawHeaders(asList(gson().fromJson(initializer.getAsJsonArray("headers"), HttpHeader[].class)));
  }

  @Override
  public byte[] body() {
    return context.withLogging("APIResponse.body", () -> {
      try {
        JsonObject params = new JsonObject();
        params.addProperty("fetchUid", fetchUid());
        JsonObject json = context.sendMessage("fetchResponseBody", params).getAsJsonObject();
        if (!json.has("binary")) {
          throw new PlaywrightException("Response has been disposed");
        }
        return Base64.getDecoder().decode(json.get("binary").getAsString());
      } catch (PlaywrightException e) {
        if (isSafeCloseError(e)) {
          throw new PlaywrightException("Response has been disposed");
        }
        throw e;
      }
    });
  }

  @Override
  public void dispose() {
    context.withLogging("APIResponse.dispose", () -> {
      JsonObject params = new JsonObject();
      params.addProperty("fetchUid", fetchUid());
      context.sendMessage("disposeAPIResponse", params);
    });
  }

  @Override
  public Map<String, String> headers() {
    return headers.headers();
  }

  @Override
  public List<HttpHeader> headersArray() {
    return headers.headersArray();
  }

  @Override
  public boolean ok() {
    int status = status();
    return status == 0 || (status >= 200 && status <= 299);
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

  String fetchUid() {
    return initializer.get("fetchUid").getAsString();
  }

  List<String> fetchLog() {
    JsonObject params = new JsonObject();
    params.addProperty("fetchUid", fetchUid());
    JsonObject json = context.sendMessage("fetchLog", params).getAsJsonObject();
    JsonArray log = json.get("log").getAsJsonArray();
    return gson().fromJson(log, new TypeToken<List<String>>() {}.getType());
  }

}
