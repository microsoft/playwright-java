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

// Port of network.ts.
package com.microsoft.playwright.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.impl.protocol.RouteAbortParams;
import com.microsoft.playwright.impl.protocol.RouteChannel;
import com.microsoft.playwright.impl.protocol.RouteContinueParams;
import com.microsoft.playwright.impl.protocol.RouteFulfillParams;
import com.microsoft.playwright.impl.protocol.RouteInitializer;
import com.microsoft.playwright.impl.protocol.RouteRedirectNavigationRequestParams;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.microsoft.playwright.impl.Headers.headersObjectToArray;
import static com.microsoft.playwright.impl.MimeType.getMimeTypeForPath;

public class RouteImpl extends ChannelOwner implements Route {
  private WaitableResult<Boolean> handlingPromise = null;
  BrowserContextImpl context;
  boolean didThrow = false;

  final RouteChannel channel = new RouteChannel(this);
  private final RouteInitializer routeInitializer;

  RouteImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
    routeInitializer = connection.protocolGson().fromJson(initializer, RouteInitializer.class);
  }

  @Override
  public RequestImpl request() {
    return routeInitializer.request;
  }

  // java: takes the pending reply of a message sent with sendMessageAsync.
  private void raceWithTargetClose(WaitableResult<JsonElement> promise) {
    // When page closes or crashes, we catch any potential rejects from this Route.
    // Note that page could be missing when routing popup's initial request that
    // does not have a Page initialized just yet.
    request().targetClosedScope().safeRace(promise);
  }

  WaitableResult<Boolean> startHandling() {
    handlingPromise = new WaitableResult<>();
    return handlingPromise;
  }

  @Override
  public void fallback(FallbackOptions options) {
    if (options == null) {
      options = new FallbackOptions();
    }
    checkNotHandled();
    request().applyFallbackOverrides(fallbackOverrides(options.url, options.method, options.headers, options.postData));
    reportHandled(false);
    // java: nobody waits for a fallback() after the handler returned (see RouteHandler.handleInternal), so the
    // request continues as the end of BrowserContext.onRoute() does.
    if (handlerReturned) {
      try {
        innerContinue(true);
      } catch (PlaywrightException e) {
      }
    }
  }

  @Override
  public void abort(String errorCode) {
    handleRoute(() -> {
      RouteAbortParams params = new RouteAbortParams();
      params.errorCode = errorCode;
      raceWithTargetClose(sendMessageAsync("abort", params));
    });
  }

  void redirectNavigationRequest(String url) {
    handleRoute(() -> {
      RouteRedirectNavigationRequestParams params = new RouteRedirectNavigationRequestParams();
      params.url = url;
      raceWithTargetClose(sendMessageAsync("redirectNavigationRequest", params));
    });
  }

  @Override
  public APIResponse fetch(FetchOptions options) {
    if (options == null) {
      options = new FetchOptions();
    }
    FetchOptions fetchOptions = options;
    return wrapApiCall(() -> {
      // java: the FetchOptions fields as RequestOptionsImpl, data is options.postData.
      RequestOptionsImpl requestOptions = new RequestOptionsImpl();
      requestOptions.data = fetchOptions.postData;
      requestOptions.method = fetchOptions.method;
      requestOptions.headers = fetchOptions.headers;
      requestOptions.maxRedirects = fetchOptions.maxRedirects;
      requestOptions.maxRetries = fetchOptions.maxRetries;
      requestOptions.timeout = fetchOptions.timeout;
      return ((APIRequestContextImpl) context.request()).innerFetch(fetchOptions.url, request(), requestOptions);
    }, null, false);
  }

  @Override
  public void fulfill(FulfillOptions options) {
    handleRoute(() -> {
      innerFulfill(options);
    });
  }

  private void handleRoute(Runnable callback) {
    checkNotHandled();
    try {
      callback.run();
      reportHandled(true);
    } catch (RuntimeException e) {
      didThrow = true;
      throw e;
    }
  }

  private void innerFulfill(FulfillOptions options) {
    if (options == null) {
      options = new FulfillOptions();
    }
    String fetchResponseUid = null;
    Integer statusOption = options.status;
    Map<String, String> headersOption = options.headers;
    // java: body is a String (body) or a byte[] (bodyBytes); there is no json option.
    Object body = options.body != null ? options.body : options.bodyBytes;

    if (options.response instanceof APIResponseImpl) {
      APIResponseImpl response = (APIResponseImpl) options.response;
      if (statusOption == null) {
        statusOption = response.status();
      }
      if (headersOption == null) {
        headersOption = response.headers();
      }
      if (body == null && options.path == null) {
        if (response.request.connection == this.connection) {
          fetchResponseUid = response.fetchUid();
        } else {
          body = response.body();
        }
      }
    }

    boolean isBase64 = false;
    int length = 0;
    if (options.path != null) {
      byte[] buffer = readFile(options.path);
      body = Base64.getEncoder().encodeToString(buffer);
      isBase64 = true;
      length = buffer.length;
    } else if (body instanceof String) {
      isBase64 = false;
      length = ((String) body).getBytes(StandardCharsets.UTF_8).length;
    } else if (body != null) {
      byte[] buffer = (byte[]) body;
      length = buffer.length;
      body = Base64.getEncoder().encodeToString(buffer);
      isBase64 = true;
    }

    Map<String, String> headers = new LinkedHashMap<>();
    if (headersOption != null) {
      for (Map.Entry<String, String> header : headersOption.entrySet()) {
        headers.put(header.getKey().toLowerCase(), String.valueOf(header.getValue()));
      }
    }
    if (options.contentType != null) {
      headers.put("content-type", options.contentType);
    } else if (options.path != null) {
      String mimeType = getMimeTypeForPath(options.path.toString());
      headers.put("content-type", mimeType != null ? mimeType : "application/octet-stream");
    }
    if (length != 0 && !headers.containsKey("content-length")) {
      headers.put("content-length", String.valueOf(length));
    }

    RouteFulfillParams params = new RouteFulfillParams();
    params.status = statusOption != null && statusOption != 0 ? statusOption : 200;
    params.headers = headersObjectToArray(headers);
    params.body = (String) body;
    params.isBase64 = isBase64;
    params.fetchResponseUid = fetchResponseUid;
    raceWithTargetClose(sendMessageAsync("fulfill", params));
  }

  // java: resume() is continue() of the Node.js API.
  @Override
  public void resume(ResumeOptions options) {
    ResumeOptions resumeOptions = options == null ? new ResumeOptions() : options;
    handleRoute(() -> {
      request().applyFallbackOverrides(fallbackOverrides(resumeOptions.url, resumeOptions.method, resumeOptions.headers, resumeOptions.postData));
      innerContinue(false /* isFallback */);
    });
  }

  void checkNotHandled() {
    if (handlingPromise == null) {
      throw new PlaywrightException("Route is already handled!");
    }
  }

  void reportHandled(boolean done) {
    WaitableResult<Boolean> chain = handlingPromise;
    handlingPromise = null;
    chain.complete(done);
  }

  void innerContinue(boolean isFallback) {
    RequestImpl.SerializedFallbackOverrides options = request().fallbackOverridesForContinue();
    RouteContinueParams params = new RouteContinueParams();
    params.url = options.url;
    params.method = options.method;
    params.headers = options.headers != null ? headersObjectToArray(options.headers) : null;
    params.postData = options.postDataBuffer;
    params.isFallback = isFallback;
    raceWithTargetClose(sendMessageAsync("continue", params));
  }

  // java: members with no counterpart upstream; handlerReturned is set by RouteHandler when the handler
  // returned before it handled the route or fell back.
  boolean handlerReturned;

  private static RequestImpl.FallbackOverrides fallbackOverrides(String url, String method, Map<String, String> headers, Object postData) {
    RequestImpl.FallbackOverrides overrides = new RequestImpl.FallbackOverrides();
    overrides.url = url;
    overrides.method = method;
    overrides.headers = headers;
    overrides.postData = postData;
    return overrides;
  }

  private static byte[] readFile(Path path) {
    try {
      return Files.readAllBytes(path);
    } catch (IOException e) {
      throw new PlaywrightException("Failed to read from file: " + path, e);
    }
  }
}
