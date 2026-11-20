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

import com.google.gson.JsonObject;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.impl.protocol.NameValue;
import com.microsoft.playwright.impl.protocol.RequestChannel;
import com.microsoft.playwright.impl.protocol.RequestInitializer;
import com.microsoft.playwright.impl.protocol.RequestSizes;
import com.microsoft.playwright.options.HttpHeader;
import com.microsoft.playwright.options.Sizes;
import com.microsoft.playwright.options.Timing;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.microsoft.playwright.impl.Serialization.gson;

public class RequestImpl extends ChannelOwner implements Request {
  // java: SerializedFallbackOverrides.
  static class SerializedFallbackOverrides {
    String url;
    String method;
    Map<String, String> headers;
    byte[] postDataBuffer;
  }

  // java: FallbackOverrides, built by RouteImpl from the Route options classes.
  static class FallbackOverrides {
    String url;
    String method;
    Map<String, String> headers;
    Object postData;
  }

  private RequestImpl redirectedFrom;
  private RequestImpl redirectedTo;
  String failureText;
  ResponseImpl response;
  private final RawHeaders provisionalHeaders;
  // java: the resolved value of _actualHeadersPromise.
  private RawHeaders actualHeadersPromise;
  final Timing timing;
  private final SerializedFallbackOverrides fallbackOverrides = new SerializedFallbackOverrides();

  final RequestChannel channel = new RequestChannel(this);
  final RequestInitializer requestInitializer;

  RequestImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
    requestInitializer = connection.protocolGson().fromJson(initializer, RequestInitializer.class);
    this.redirectedFrom = requestInitializer.redirectedFrom;
    if (this.redirectedFrom != null) {
      this.redirectedFrom.redirectedTo = this;
    }
    this.provisionalHeaders = new RawHeaders(requestInitializer.headers != null ? requestInitializer.headers : new ArrayList<>());
    this.timing = new Timing();
    this.timing.startTime = 0;
    this.timing.domainLookupStart = -1;
    this.timing.domainLookupEnd = -1;
    this.timing.connectStart = -1;
    this.timing.secureConnectionStart = -1;
    this.timing.connectEnd = -1;
    this.timing.requestStart = -1;
    this.timing.responseStart = -1;
    this.timing.responseEnd = -1;
  }

  @Override
  public String url() {
    return fallbackOverrides.url != null && !fallbackOverrides.url.isEmpty() ? fallbackOverrides.url : requestInitializer.url;
  }

  @Override
  public String resourceType() {
    return requestInitializer.resourceType;
  }

  @Override
  public String method() {
    return fallbackOverrides.method != null && !fallbackOverrides.method.isEmpty() ? fallbackOverrides.method : requestInitializer.method;
  }

  @Override
  public String postData() {
    byte[] buffer = fallbackOverrides.postDataBuffer != null ? fallbackOverrides.postDataBuffer : requestInitializer.postData;
    return buffer != null ? new String(buffer, StandardCharsets.UTF_8) : null;
  }

  @Override
  public byte[] postDataBuffer() {
    if (fallbackOverrides.postDataBuffer != null) {
      return fallbackOverrides.postDataBuffer;
    }
    return requestInitializer.postData;
  }

  // java: postDataJSON() is not in the Java API.

  @Override
  public Map<String, String> headers() {
    if (fallbackOverrides.headers != null) {
      return RawHeaders.fromHeadersObjectLossy(fallbackOverrides.headers).headers();
    }
    return provisionalHeaders.headers();
  }

  RawHeaders actualHeaders() {
    if (fallbackOverrides.headers != null) {
      return RawHeaders.fromHeadersObjectLossy(fallbackOverrides.headers);
    }

    if (actualHeadersPromise == null) {
      List<NameValue> headers = channel.rawRequestHeaders(NO_TIMEOUT).headers;
      // java: a nested call may have set it while this one waited, the values are the same.
      actualHeadersPromise = new RawHeaders(headers);
    }
    return actualHeadersPromise;
  }

  @Override
  public Map<String, String> allHeaders() {
    return wrapApiCall(() -> actualHeaders().headers(), null, true);
  }

  @Override
  public List<HttpHeader> headersArray() {
    return wrapApiCall(() -> actualHeaders().headersArray(), null, true);
  }

  @Override
  public String headerValue(String name) {
    return wrapApiCall(() -> actualHeaders().get(name), null, true);
  }

  @Override
  public ResponseImpl response() {
    return channel.response(NO_TIMEOUT).response;
  }

  ResponseImpl internalResponse() {
    return channel.response(NO_TIMEOUT).response;
  }

  @Override
  public ResponseImpl existingResponse() {
    return response;
  }

  @Override
  public FrameImpl frame() {
    if (requestInitializer.frame == null) {
      throw new PlaywrightException("Service Worker requests do not have an associated frame.");
    }
    FrameImpl frame = requestInitializer.frame;
    if (frame.page == null) {
      throw new PlaywrightException(String.join("\n",
        "Frame for this navigation request is not available, because the request",
        "was issued before the frame is created. You can check whether the request",
        "is a navigation request by calling isNavigationRequest() method."));
    }
    return frame;
  }

  PageImpl safePage() {
    FrameImpl frame = requestInitializer.frame;
    return frame != null ? frame.page : null;
  }

  // java: not in the Java API, kept for targetClosedScope().
  WorkerImpl serviceWorker() {
    return requestInitializer.serviceWorker;
  }

  @Override
  public boolean isNavigationRequest() {
    return requestInitializer.isNavigationRequest != null && requestInitializer.isNavigationRequest;
  }

  @Override
  public RequestImpl redirectedFrom() {
    return redirectedFrom;
  }

  @Override
  public RequestImpl redirectedTo() {
    return redirectedTo;
  }

  // java: the error text itself, the Java API returns a String.
  @Override
  public String failure() {
    return failureText;
  }

  @Override
  public Timing timing() {
    return timing;
  }

  @Override
  public Sizes sizes() {
    ResponseImpl response = response();
    if (response == null) {
      throw new PlaywrightException("Unable to fetch sizes for failed request");
    }
    RequestSizes sizes = response.channel.sizes(NO_TIMEOUT).sizes;
    Sizes result = new Sizes();
    result.requestBodySize = sizes.requestBodySize;
    result.requestHeadersSize = sizes.requestHeadersSize;
    result.responseBodySize = sizes.responseBodySize;
    result.responseHeadersSize = sizes.responseHeadersSize;
    return result;
  }

  void setResponseEndTiming(double responseEndTiming) {
    timing.responseEnd = responseEndTiming;
    if (timing.responseStart == -1) {
      timing.responseStart = responseEndTiming;
    }
  }

  RequestImpl finalRequest() {
    return redirectedTo != null ? redirectedTo.finalRequest() : this;
  }

  void applyFallbackOverrides(FallbackOverrides overrides) {
    if (overrides.url != null && !overrides.url.isEmpty()) {
      fallbackOverrides.url = overrides.url;
    }
    if (overrides.method != null && !overrides.method.isEmpty()) {
      fallbackOverrides.method = overrides.method;
    }
    if (overrides.headers != null) {
      fallbackOverrides.headers = overrides.headers;
    }

    if (overrides.postData instanceof String) {
      fallbackOverrides.postDataBuffer = ((String) overrides.postData).getBytes(StandardCharsets.UTF_8);
    } else if (overrides.postData instanceof byte[]) {
      fallbackOverrides.postDataBuffer = (byte[]) overrides.postData;
    } else if (overrides.postData != null) {
      fallbackOverrides.postDataBuffer = gson().toJson(overrides.postData).getBytes(StandardCharsets.UTF_8);
    }
  }

  SerializedFallbackOverrides fallbackOverridesForContinue() {
    return fallbackOverrides;
  }

  // java: there is no LongStandingScope; the scope watches the close events of the service worker or the page.
  TargetClosedScope targetClosedScope() {
    WorkerImpl serviceWorker = serviceWorker();
    if (serviceWorker != null) {
      return new TargetClosedScope(this, serviceWorker, Events.Worker.CLOSE, serviceWorker::closeErrorWithReason, null, null);
    }
    PageImpl page = safePage();
    if (page != null) {
      return new TargetClosedScope(this, page, Events.Page.CLOSE, page::closeErrorWithReason, Events.Page.CRASH, TargetClosedError::new);
    }
    return new TargetClosedScope(this, null, null, null, null, null);
  }

  // java: the LongStandingScope of a service worker or a page, built on Waitables.
  static class TargetClosedScope {
    private final ChannelOwner owner;
    private final EventEmitter target;
    private final String closeEvent;
    private final Supplier<RuntimeException> closeError;
    private final String crashEvent;
    private final Supplier<RuntimeException> crashError;

    TargetClosedScope(ChannelOwner owner, EventEmitter target, String closeEvent, Supplier<RuntimeException> closeError,
                      String crashEvent, Supplier<RuntimeException> crashError) {
      this.owner = owner;
      this.target = target;
      this.closeEvent = closeEvent;
      this.closeError = closeError;
      this.crashEvent = crashEvent;
      this.crashError = crashError;
    }

    private boolean isClosed() {
      if (target instanceof PageImpl) {
        return ((PageImpl) target).isClosed();
      }
      return false;
    }

    <T> T race(Waitable<T> promise) {
      return race(promise, false);
    }

    <T> T safeRace(Waitable<T> promise) {
      return race(promise, true);
    }

    private <T> T race(Waitable<T> promise, boolean safe) {
      // java: a settled promise wins over the terminate promise, as its reaction runs first upstream.
      if (target == null || promise.isDone()) {
        return owner.runUntil(() -> {}, promise);
      }
      if (isClosed()) {
        promise.dispose();
        if (safe) {
          return null;
        }
        throw closeError.get();
      }
      List<Waitable<T>> waitables = new ArrayList<>();
      waitables.add(terminateWaitable(closeEvent, closeError, safe));
      if (crashEvent != null) {
        waitables.add(terminateWaitable(crashEvent, crashError, safe));
      }
      waitables.add(promise);
      return owner.runUntil(() -> {}, new WaitableRace<>(waitables));
    }

    private <T> Waitable<T> terminateWaitable(String event, Supplier<RuntimeException> error, boolean safe) {
      return new WaitableEvent<T>(target, event) {
        @Override
        public T get() {
          if (safe) {
            return null;
          }
          throw error.get();
        }
      };
    }
  }
}
