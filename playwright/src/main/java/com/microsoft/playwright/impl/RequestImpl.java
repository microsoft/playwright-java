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
  private FallbackOverrides fallbackOverrides;

  static class FallbackOverrides {
    String url;
    String method;
    byte[] postData;
    Map<String, String> headers;
  }

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
    FrameImpl frame = connection.getExistingObject(initializer.getAsJsonObject("frame").get("guid").getAsString());
    if (frame.page == null) {
      throw new PlaywrightException("Frame for this navigation request is not available, because the request\n" +
        "was issued before the frame is created. You can check whether the request\n" +
        "is a navigation request by calling isNavigationRequest() method.");
    }
    return frame;
  }

  @Override
  public Map<String, String> headers() {
    if (fallbackOverrides != null && fallbackOverrides.headers != null) {
      return new RawHeaders(Utils.toHeadersList(fallbackOverrides.headers)).headers();
    }
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
    if (fallbackOverrides != null && fallbackOverrides.method != null) {
      return fallbackOverrides.method;
    }
    return initializer.get("method").getAsString();
  }

  @Override
  public String postData() {
    byte[] buffer = postDataBuffer();
    if (buffer == null) {
      return null;
    }
    return new String(buffer, StandardCharsets.UTF_8);
  }

  @Override
  public byte[] postDataBuffer() {
    if (fallbackOverrides != null && fallbackOverrides.postData != null) {
      return fallbackOverrides.postData;
    }
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
    if (fallbackOverrides != null && fallbackOverrides.url != null) {
      return fallbackOverrides.url;
    }
    return initializer.get("url").getAsString();
  }

  Request finalRequest() {
    return redirectedTo != null ? redirectedTo.finalRequest() : this;
  }

  private RawHeaders getRawHeaders() {
    if (fallbackOverrides != null && fallbackOverrides.headers != null) {
      return new RawHeaders(Utils.toHeadersList(fallbackOverrides.headers));
    }
    if (rawHeaders != null) {
      return rawHeaders;
    }
    JsonArray rawHeadersJson = withLogging("Request.allHeaders", () -> {
      JsonObject result = sendMessage("rawRequestHeaders").getAsJsonObject();
      return result.getAsJsonArray("headers");
    });

    // The field may have been initialized in a nested call but it is ok.
    rawHeaders = new RawHeaders(asList(gson().fromJson(rawHeadersJson, HttpHeader[].class)));
    return rawHeaders;
  }

  void applyFallbackOverrides(FallbackOverrides overrides) {
    if (fallbackOverrides == null) {
      fallbackOverrides = new FallbackOverrides();
    }
    if (overrides.url != null) {
      fallbackOverrides.url = overrides.url;
    }
    if (overrides.method != null) {
      fallbackOverrides.method = overrides.method;
    }
    if (overrides.headers != null) {
      fallbackOverrides.headers = overrides.headers;
    }
    if (overrides.postData != null) {
      fallbackOverrides.postData = overrides.postData;
    }
  }

  FallbackOverrides fallbackOverridesForResume() {
    return fallbackOverrides;
  }
}
