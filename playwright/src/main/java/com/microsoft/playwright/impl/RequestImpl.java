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
import com.google.gson.JsonObject;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.options.HttpHeader;
import com.microsoft.playwright.options.Sizes;
import com.microsoft.playwright.options.Timing;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static com.microsoft.playwright.impl.Serialization.gson;
import static com.microsoft.playwright.impl.Utils.toHeadersMap;
import static java.util.Arrays.asList;

public class RequestImpl extends ChannelOwner implements Request {
  private final byte[] postData;
  private RequestImpl redirectedFrom;
  private RequestImpl redirectedTo;
  private final RawHeaders headers;
  private RawHeaders rawHeaders;
  String failure;
  Timing timing;
  boolean didFailOrFinish;

  RequestImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);

    if (initializer.has("redirectedFrom")) {
      redirectedFrom = connection.getExistingObject(initializer.getAsJsonObject("redirectedFrom").get("guid").getAsString());
      redirectedFrom.redirectedTo = this;
    }
    headers = new RawHeaders(asList(gson().fromJson(initializer.getAsJsonArray("headers"), HttpHeader[].class)));
    if (initializer.has("postData")) {
      postData = Base64.getDecoder().decode(initializer.get("postData").getAsString());
    } else {
      postData = null;
    }
  }

  @Override
  public Map<String, String> allHeaders() {
    return withLogging("Request.allHeaders", () -> getRawHeaders().headers());
  }

  @Override
  public String failure() {
    return failure;
  }

  @Override
  public FrameImpl frame() {
    return connection.getExistingObject(initializer.getAsJsonObject("frame").get("guid").getAsString());
  }

  @Override
  public Map<String, String> headers() {
    return headers.headers();
  }

  @Override
  public List<HttpHeader> headersArray() {
    return withLogging("Request.headersArray", () -> getRawHeaders().headersArray());
  }

  @Override
  public String headerValue(String name) {
    return withLogging("Request.headerValue", () -> getRawHeaders().get(name));
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
    if (postData == null) {
      return null;
    }
    return new String(postData, StandardCharsets.UTF_8);
  }

  @Override
  public byte[] postDataBuffer() {
    return postData;
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
  public ResponseImpl response() {
    return withLogging("Request.response", () -> {
      JsonObject result = sendMessage("response").getAsJsonObject();
      if (!result.has("response")) {
        return null;
      }
      return connection.getExistingObject(result.getAsJsonObject("response").get("guid").getAsString());
    });
  }

  @Override
  public Sizes sizes() {
    return withLogging("Request.sizes", () -> {
      ResponseImpl response = response();
      if (response == null) {
        throw new PlaywrightException("Unable to fetch sizes for failed request");
      }
      JsonObject json = response.sendMessage("sizes").getAsJsonObject();
      return gson().fromJson(json.getAsJsonObject("sizes"), Sizes.class);
    });
  }

  @Override
  public Timing timing() {
    return timing;
  }

  @Override
  public String url() {
    return initializer.get("url").getAsString();
  }

  Request finalRequest() {
    return redirectedTo != null ? redirectedTo.finalRequest() : this;
  }

  private RawHeaders getRawHeaders() {
    if (rawHeaders != null) {
      return rawHeaders;
    }
    ResponseImpl response = response();
    // there is no response, so should we return the headers we have now?
    if (response == null) {
      return headers;
    }
    JsonArray rawHeadersJson = response.withLogging("Request.allHeaders", () -> {
      JsonObject result = response.sendMessage("rawRequestHeaders").getAsJsonObject();
      return result.getAsJsonArray("headers");
    });

    // The field may have been initialized in a nested call but it is ok.
    rawHeaders = new RawHeaders(asList(gson().fromJson(rawHeadersJson, HttpHeader[].class)));
    return rawHeaders;
  }
}
