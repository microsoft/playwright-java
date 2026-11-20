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
import com.microsoft.playwright.Response;
import com.microsoft.playwright.impl.protocol.RemoteAddr;
import com.microsoft.playwright.impl.protocol.ResourceTiming;
import com.microsoft.playwright.impl.protocol.ResponseChannel;
import com.microsoft.playwright.impl.protocol.ResponseInitializer;
import com.microsoft.playwright.options.HttpHeader;
import com.microsoft.playwright.options.SecurityDetails;
import com.microsoft.playwright.options.ServerAddr;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ResponseImpl extends ChannelOwner implements Response {
  private final RawHeaders provisionalHeaders;
  // java: the resolved value of _actualHeadersPromise.
  private RawHeaders actualHeadersPromise;
  private final RequestImpl request;
  final WaitableResult<String> finishedPromise = new WaitableResult<>();

  final ResponseChannel channel = new ResponseChannel(this);
  final ResponseInitializer responseInitializer;

  ResponseImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
    responseInitializer = connection.protocolGson().fromJson(initializer, ResponseInitializer.class);
    this.provisionalHeaders = new RawHeaders(responseInitializer.headers != null ? responseInitializer.headers : new ArrayList<>());
    this.request = responseInitializer.request;
    this.request.response = this;
    assignTiming(this.request, responseInitializer.timing);
  }

  @Override
  public String url() {
    return responseInitializer.url;
  }

  @Override
  public boolean ok() {
    // Status 0 is for file:// URLs
    return responseInitializer.status == 0 || (responseInitializer.status >= 200 && responseInitializer.status <= 299);
  }

  @Override
  public int status() {
    return responseInitializer.status;
  }

  @Override
  public String statusText() {
    return responseInitializer.statusText;
  }

  @Override
  public boolean fromServiceWorker() {
    return responseInitializer.fromServiceWorker != null && responseInitializer.fromServiceWorker;
  }

  @Override
  public Map<String, String> headers() {
    return provisionalHeaders.headers();
  }

  RawHeaders actualHeaders() {
    if (actualHeadersPromise == null) {
      actualHeadersPromise = new RawHeaders(channel.rawResponseHeaders(NO_TIMEOUT).headers);
    }
    return actualHeadersPromise;
  }

  @Override
  public Map<String, String> allHeaders() {
    return actualHeaders().headers();
  }

  @Override
  public List<HttpHeader> headersArray() {
    return actualHeaders().headersArray();
  }

  @Override
  public String headerValue(String name) {
    return actualHeaders().get(name);
  }

  @Override
  public List<String> headerValues(String name) {
    return actualHeaders().getAll(name);
  }

  @Override
  public String finished() {
    return request().targetClosedScope().race(finishedPromise);
  }

  @Override
  public byte[] body() {
    return channel.body(NO_TIMEOUT).binary;
  }

  @Override
  public String text() {
    byte[] content = body();
    return new String(content, StandardCharsets.UTF_8);
  }

  // java: json() is not in the Java API.

  @Override
  public RequestImpl request() {
    return request;
  }

  @Override
  public FrameImpl frame() {
    return request.frame();
  }

  @Override
  public ServerAddr serverAddr() {
    RemoteAddr value = channel.serverAddr(NO_TIMEOUT).value;
    if (value == null) {
      return null;
    }
    // java: the protocol struct is converted to the public options type.
    ServerAddr result = new ServerAddr();
    result.ipAddress = value.ipAddress;
    result.port = value.port;
    return result;
  }

  @Override
  public SecurityDetails securityDetails() {
    com.microsoft.playwright.impl.protocol.SecurityDetails value = channel.securityDetails(NO_TIMEOUT).value;
    if (value == null) {
      return null;
    }
    // java: the protocol struct is converted to the public options type.
    SecurityDetails result = new SecurityDetails();
    result.issuer = value.issuer;
    result.protocol = value.protocol;
    result.subjectName = value.subjectName;
    result.validFrom = value.validFrom;
    result.validTo = value.validTo;
    return result;
  }

  @Override
  public String httpVersion() {
    return channel.httpVersion(NO_TIMEOUT).value;
  }

  // java: Object.assign(this._request._timing, this._initializer.timing).
  private static void assignTiming(RequestImpl request, ResourceTiming timing) {
    if (timing == null) {
      return;
    }
    if (timing.startTime != null) {
      request.timing.startTime = timing.startTime;
    }
    if (timing.domainLookupStart != null) {
      request.timing.domainLookupStart = timing.domainLookupStart;
    }
    if (timing.domainLookupEnd != null) {
      request.timing.domainLookupEnd = timing.domainLookupEnd;
    }
    if (timing.connectStart != null) {
      request.timing.connectStart = timing.connectStart;
    }
    if (timing.secureConnectionStart != null) {
      request.timing.secureConnectionStart = timing.secureConnectionStart;
    }
    if (timing.connectEnd != null) {
      request.timing.connectEnd = timing.connectEnd;
    }
    if (timing.requestStart != null) {
      request.timing.requestStart = timing.requestStart;
    }
    if (timing.responseStart != null) {
      request.timing.responseStart = timing.responseStart;
    }
  }
}
