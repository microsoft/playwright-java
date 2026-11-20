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

// Port of fetch.ts.
package com.microsoft.playwright.impl;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.impl.protocol.APIRequestContextDisposeAPIResponseParams;
import com.microsoft.playwright.impl.protocol.APIRequestContextFetchLogParams;
import com.microsoft.playwright.impl.protocol.APIRequestContextFetchResponseBodyParams;
import com.microsoft.playwright.impl.protocol.APIRequestContextFetchResponseBodyResult;
import com.microsoft.playwright.impl.protocol.ResourceTiming;
import com.microsoft.playwright.options.HttpHeader;
import com.microsoft.playwright.options.SecurityDetails;
import com.microsoft.playwright.options.ServerAddr;
import com.microsoft.playwright.options.Timing;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.microsoft.playwright.impl.ChannelOwner.NO_TIMEOUT;
import static com.microsoft.playwright.impl.Errors.isTargetClosedError;

class APIResponseImpl implements APIResponse {
  private final com.microsoft.playwright.impl.protocol.APIResponse initializer;
  private final RawHeaders headers;
  final APIRequestContextImpl request;

  APIResponseImpl(APIRequestContextImpl context, com.microsoft.playwright.impl.protocol.APIResponse initializer) {
    this.request = context;
    this.initializer = initializer;
    this.headers = new RawHeaders(this.initializer.headers);
  }

  <R> R wrapApiCall(Supplier<R> func, String title, boolean internal) {
    return request.wrapApiCall(func, title, internal);
  }

  @Override
  public boolean ok() {
    return initializer.status >= 200 && initializer.status <= 299;
  }

  @Override
  public String url() {
    return initializer.url;
  }

  @Override
  public int status() {
    return initializer.status;
  }

  @Override
  public String statusText() {
    return initializer.statusText;
  }

  @Override
  public Map<String, String> headers() {
    return headers.headers();
  }

  @Override
  public List<HttpHeader> headersArray() {
    return headers.headersArray();
  }

  // java: the protocol SecurityDetails and RemoteAddr are converted to the API types.
  @Override
  public SecurityDetails securityDetails() {
    return Utils.convertType(initializer.securityDetails, SecurityDetails.class);
  }

  @Override
  public ServerAddr serverAddr() {
    if (initializer.serverAddr == null) {
      return null;
    }
    ServerAddr result = new ServerAddr();
    result.ipAddress = initializer.serverAddr.ipAddress;
    result.port = initializer.serverAddr.port == null ? 0 : initializer.serverAddr.port;
    return result;
  }

  @Override
  public Timing timing() {
    ResourceTiming t = initializer.timing;
    Timing timing = new Timing();
    timing.startTime = -1;
    timing.domainLookupStart = -1;
    timing.domainLookupEnd = -1;
    timing.connectStart = -1;
    timing.secureConnectionStart = -1;
    timing.connectEnd = -1;
    timing.requestStart = -1;
    timing.responseStart = -1;
    // java: the spread of this._initializer.timing, field by field.
    if (t != null) {
      if (t.startTime != null) {
        timing.startTime = t.startTime;
      }
      if (t.domainLookupStart != null) {
        timing.domainLookupStart = t.domainLookupStart;
      }
      if (t.domainLookupEnd != null) {
        timing.domainLookupEnd = t.domainLookupEnd;
      }
      if (t.connectStart != null) {
        timing.connectStart = t.connectStart;
      }
      if (t.secureConnectionStart != null) {
        timing.secureConnectionStart = t.secureConnectionStart;
      }
      if (t.connectEnd != null) {
        timing.connectEnd = t.connectEnd;
      }
      if (t.requestStart != null) {
        timing.requestStart = t.requestStart;
      }
      if (t.responseStart != null) {
        timing.responseStart = t.responseStart;
      }
    }
    timing.responseEnd = initializer.responseEndTiming != null ? initializer.responseEndTiming : -1;
    return timing;
  }

  @Override
  public byte[] body() {
    return wrapApiCall(() -> {
      try {
        APIRequestContextFetchResponseBodyParams params = new APIRequestContextFetchResponseBodyParams();
        params.fetchUid = fetchUid();
        APIRequestContextFetchResponseBodyResult result = request.channel.fetchResponseBody(params, NO_TIMEOUT);
        if (result == null || result.binary == null) {
          throw new PlaywrightException("Response has been disposed");
        }
        return result.binary;
      } catch (PlaywrightException e) {
        if (isTargetClosedError(e)) {
          throw new PlaywrightException("Response has been disposed");
        }
        throw e;
      }
    }, null, true);
  }

  @Override
  public String text() {
    byte[] content = body();
    return new String(content, StandardCharsets.UTF_8);
  }

  @Override
  public void dispose() {
    APIRequestContextDisposeAPIResponseParams params = new APIRequestContextDisposeAPIResponseParams();
    params.fetchUid = fetchUid();
    request.channel.disposeAPIResponse(params, NO_TIMEOUT);
  }

  String fetchUid() {
    return initializer.fetchUid;
  }

  List<String> fetchLog() {
    APIRequestContextFetchLogParams params = new APIRequestContextFetchLogParams();
    params.fetchUid = fetchUid();
    return request.channel.fetchLog(params, NO_TIMEOUT).log;
  }
}
