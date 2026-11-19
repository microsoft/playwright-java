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

import com.microsoft.playwright.impl.protocol.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.playwright.*;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.microsoft.playwright.impl.Utils.convertType;

public class RouteImpl extends ChannelOwner implements Route {
  private final RequestImpl request;
  private boolean handled;
  BrowserContextImpl browserContext;
  boolean fallbackCalled;
  boolean shouldResumeIfFallbackIsCalled;

  final RouteChannel channel = new RouteChannel(this);

  public RouteImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
    request = connection.getExistingObject(initializer.getAsJsonObject("request").get("guid").getAsString());
  }

  @Override
  public void abort(String errorCode) {
    handleRoute(() -> {
      RouteAbortParams params = new RouteAbortParams();
      params.errorCode = errorCode;
      raceWithTargetClose("abort", params);
    });
  }

  boolean isHandled() {
    return handled;
  }

  @Override
  public void resume(ResumeOptions options) {
    handleRoute(() -> {
      applyOverrides(convertType(options, FallbackOptions.class));
      raceWithTargetClose("continue", continueParams(false));
    });
  }

  // java: upstream awaits _innerContinue(true) and ignores its error; nothing observes the reply here.
  void continueAsFallback() {
    checkNotHandled();
    handled = true;
    sendMessageAsync("continue", continueParams(true));
  }

  private void handleRoute(Runnable callback) {
    checkNotHandled();
    callback.run();
    handled = true;
  }

  // java: pumps the connection until the reply arrives; when the page closes first the call returns
  // without an error, as safeRace() of the target closed scope does upstream.
  private void raceWithTargetClose(String method, Object params) {
    WaitableResult<JsonElement> result = sendMessageAsync(method, params);
    PageImpl page = request.safePage();
    if (page == null) {
      runUntil(() -> {}, result);
      return;
    }
    if (page.isClosed()) {
      return;
    }
    Waitable<JsonElement> closed = page.createWaitForCloseHelper();
    runUntil(() -> {}, new WaitableRace<>(Arrays.asList(result, new Waitable<JsonElement>() {
      @Override
      public boolean isDone() {
        return closed.isDone();
      }

      @Override
      public JsonElement get() {
        return null;
      }

      @Override
      public void dispose() {
        closed.dispose();
      }
    })));
  }

  @Override
  public void fallback(FallbackOptions options) {
    fallbackCalled = true;
    if (handled) {
      throw new PlaywrightException("Route is already handled!");
    }
    applyOverrides(options);
    if (shouldResumeIfFallbackIsCalled) {
      continueAsFallback();
    }
  }

  @Override
  public APIResponse fetch(FetchOptions fetchOptions) {
    RequestOptionsImpl options = convertType(fetchOptions, RequestOptionsImpl.class);
    if (options == null) {
      options = new RequestOptionsImpl();
    }
    if (options.method == null) {
      options.method = request.method();
    }
    if (options.headers == null) {
      options.headers = request.headers();
    }
    if (fetchOptions != null && fetchOptions.postData != null) {
      options.data = fetchOptions.postData;
    } else {
      options.data = request.postDataBuffer();
    }
    if (fetchOptions != null && fetchOptions.timeout != null) {
      options.timeout = fetchOptions.timeout;
    }
    APIRequestContextImpl apiRequest = browserContext.request();
    String url = (fetchOptions == null || fetchOptions.url == null) ? request().url() : fetchOptions.url;
    return apiRequest.fetch(url, options);
  }

  private void applyOverrides(FallbackOptions options) {
    if (options == null) {
      return;
    }
    RequestImpl.FallbackOverrides overrides = new RequestImpl.FallbackOverrides();
    overrides.url = options.url;
    overrides.method = options.method;
    overrides.headers = options.headers;
    if (options.postData != null) {
      overrides.postData = getPostDataBytes(options.postData);
    }
    request().applyFallbackOverrides(overrides);
  }

  private RouteContinueParams continueParams(boolean isFallback) {
    RequestImpl.FallbackOverrides options = request().fallbackOverridesForResume();
    RouteContinueParams params = new RouteContinueParams();
    if (options != null) {
      params.url = options.url;
      params.method = options.method;
      if (options.headers != null) {
        params.headers = toNameValues(withoutNullValues(options.headers));
      }
      params.postData = options.postData;
    }
    params.isFallback = isFallback;
    return params;
  }

  private static Map<String, String> withoutNullValues(Map<String, String> map) {
    Map<String, String> result = new LinkedHashMap<>();
    for (Map.Entry<String, String> e : map.entrySet()) {
      if (e.getValue() != null) {
        result.put(e.getKey(), e.getValue());
      }
    }
    return result;
  }

  private static List<NameValue> toNameValues(Map<String, String> map) {
    List<NameValue> result = new ArrayList<>();
    for (Map.Entry<String, String> e : map.entrySet()) {
      if (e.getValue() == null) {
        throw new PlaywrightException("Value cannot be null");
      }
      NameValue nv = new NameValue();
      nv.name = e.getKey();
      nv.value = e.getValue();
      result.add(nv);
    }
    return result;
  }

  private static byte[] getPostDataBytes(Object postData) {
    if (postData instanceof byte[]) {
      return (byte[]) postData;
    }
    if (postData instanceof String) {
      return ((String) postData).getBytes(StandardCharsets.UTF_8);
    }
    throw new PlaywrightException("postData must be either String or byte[], found: " + postData.getClass().getName());
  }

  @Override
  public void fulfill(FulfillOptions options) {
    handleRoute(() -> fulfillImpl(options));
  }

  private void fulfillImpl(FulfillOptions options) {
    if (options == null) {
      options = new FulfillOptions();
    }

    Integer status = options.status;
    Map<String, String> headersOption = options.headers;
    String fetchResponseUid = null;

    if (options.response != null) {
      if (status == null) {
        status = options.response.status();
      }
      if (headersOption == null) {
        headersOption = options.response.headers();
      }
    }
    if (status == null) {
      status = 200;
    }
    String body = null;
    boolean isBase64 = false;
    int length = 0;
    if (options.path != null) {
      try {
        byte[] buffer = Files.readAllBytes(options.path);
        body = Base64.getEncoder().encodeToString(buffer);
        isBase64 = true;
        length = buffer.length;
      } catch (IOException e) {
        throw new PlaywrightException("Failed to read from file: " + options.path, e);
      }
    } else if (options.body != null) {
      body = options.body;
      isBase64 = false;
      length = body.getBytes().length;
    } else if (options.bodyBytes != null) {
      body = Base64.getEncoder().encodeToString(options.bodyBytes);
      isBase64 = true;
      length = options.bodyBytes.length;
    } else if (options.response != null) {
      APIResponseImpl response = (APIResponseImpl) options.response;
      if (response.context.connection == connection) {
        fetchResponseUid = response.fetchUid();
      } else {
        byte[] bodyBytes = response.body();
        body = Base64.getEncoder().encodeToString(bodyBytes);
        isBase64 = true;
        length = bodyBytes.length;
      }
    }


    Map<String, String> headers = new LinkedHashMap<>();
    if (headersOption != null) {
      for (Map.Entry<String, String> h : headersOption.entrySet()) {
        headers.put(h.getKey().toLowerCase(), h.getValue());
      }
    }
    if (options.contentType != null) {
      headers.put("content-type", options.contentType);
    } else if (options.path != null) {
      headers.put("content-type", Utils.mimeType(options.path));
    }
    if (length != 0 && !headers.containsKey("content-length")) {
      headers.put("content-length", Integer.toString(length));
    }
    RouteFulfillParams params = new RouteFulfillParams();
    params.status = status;
    params.headers = toNameValues(headers);
    params.isBase64 = isBase64;
    params.body = body;
    params.fetchResponseUid = fetchResponseUid;
    raceWithTargetClose("fulfill", params);
  }

  @Override
  public RequestImpl request() {
    return request;
  }

  void redirectNavigationRequest(String redirectURL) {
    handleRoute(() -> {
      RouteRedirectNavigationRequestParams params = new RouteRedirectNavigationRequestParams();
      params.url = redirectURL;
      raceWithTargetClose("redirectNavigationRequest", params);
    });
  }

  private void checkNotHandled() {
    if (handled) {
      throw new PlaywrightException("Route is already handled!");
    }
  }

}
