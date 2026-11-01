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
import com.google.gson.reflect.TypeToken;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.HttpHeader;
import com.microsoft.playwright.options.SecurityDetails;
import com.microsoft.playwright.options.ServerAddr;
import com.microsoft.playwright.options.Timing;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static com.microsoft.playwright.impl.ChannelOwner.NO_TIMEOUT;
import static com.microsoft.playwright.impl.Serialization.gson;
import static com.microsoft.playwright.impl.Utils.isSafeCloseError;
import static java.util.Arrays.asList;

class APIResponseImpl implements APIResponse {
  final APIRequestContextImpl context;
  private final ProtocolAPIResponse response;
  private final RawHeaders headers;

  APIResponseImpl(APIRequestContextImpl apiRequestContext, ProtocolAPIResponse response) {
    context = apiRequestContext;
    this.response = response;
    List<HttpHeader> headerList = new ArrayList<>();
    for (NameValue nv : response.headers) {
      HttpHeader h = new HttpHeader();
      h.name = nv.name;
      h.value = nv.value;
      headerList.add(h);
    }
    headers = new RawHeaders(headerList);
  }

  @Override
  public byte[] body() {
    try {
      APIRequestContextFetchResponseBodyParams params = new APIRequestContextFetchResponseBodyParams();
      params.fetchUid = fetchUid();
      APIRequestContextFetchResponseBodyResult result = context.channel.fetchResponseBody(params, NO_TIMEOUT);
      if (result.binary == null) {
        throw new PlaywrightException("Response has been disposed");
      }
      return result.binary;
    } catch (PlaywrightException e) {
      if (isSafeCloseError(e)) {
        throw new PlaywrightException("Response has been disposed");
      }
      throw e;
    }
  }

  @Override
  public void dispose() {
    APIRequestContextDisposeAPIResponseParams params = new APIRequestContextDisposeAPIResponseParams();
    params.fetchUid = fetchUid();
    context.channel.disposeAPIResponse(params, NO_TIMEOUT);
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
  public SecurityDetails securityDetails() {
    ProtocolSecurityDetails d = response.securityDetails;
    if (d == null) {
      return null;
    }
    SecurityDetails result = new SecurityDetails();
    result.issuer = d.issuer;
    result.protocol = d.protocol;
    result.subjectName = d.subjectName;
    result.validFrom = d.validFrom;
    result.validTo = d.validTo;
    return result;
  }

  @Override
  public ServerAddr serverAddr() {
    if (response.serverAddr == null) {
      return null;
    }
    ServerAddr result = new ServerAddr();
    result.ipAddress = response.serverAddr.ipAddress;
    result.port = response.serverAddr.port == null ? 0 : response.serverAddr.port;
    return result;
  }

  @Override
  public int status() {
    return response.status;
  }

  @Override
  public String statusText() {
    return response.statusText;
  }

  @Override
  public String text() {
    return new String(body(), StandardCharsets.UTF_8);
  }

  @Override
  public Timing timing() {
    Timing timing;
    ResourceTiming t = response.timing;
    if (t != null) {
      timing = new Timing();
      timing.startTime = orZero(t.startTime);
      timing.domainLookupStart = orZero(t.domainLookupStart);
      timing.domainLookupEnd = orZero(t.domainLookupEnd);
      timing.connectStart = orZero(t.connectStart);
      timing.secureConnectionStart = orZero(t.secureConnectionStart);
      timing.connectEnd = orZero(t.connectEnd);
      timing.requestStart = orZero(t.requestStart);
      timing.responseStart = orZero(t.responseStart);
    } else {
      timing = new Timing();
      timing.startTime = -1;
      timing.domainLookupStart = -1;
      timing.domainLookupEnd = -1;
      timing.connectStart = -1;
      timing.secureConnectionStart = -1;
      timing.connectEnd = -1;
      timing.requestStart = -1;
      timing.responseStart = -1;
    }
    timing.responseEnd = response.responseEndTiming != null ? response.responseEndTiming : -1;
    return timing;
  }

  private static double orZero(Double value) {
    return value == null ? 0 : value;
  }

  @Override
  public String url() {
    return response.url;
  }

  String fetchUid() {
    return response.fetchUid;
  }

  List<String> fetchLog() {
    APIRequestContextFetchLogParams params = new APIRequestContextFetchLogParams();
    params.fetchUid = fetchUid();
    return context.channel.fetchLog(params, NO_TIMEOUT).log;
  }

}
