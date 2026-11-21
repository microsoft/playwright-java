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

import com.google.gson.JsonElement;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.impl.protocol.APIRequestContextDisposeAPIResponseParams;
import com.microsoft.playwright.impl.protocol.APIRequestContextFetchLogParams;
import com.microsoft.playwright.impl.protocol.APIRequestContextFetchResponseBodyParams;
import com.microsoft.playwright.impl.protocol.APIRequestContextFetchResponseBodyResult;
import com.microsoft.playwright.impl.protocol.FormField;
import com.microsoft.playwright.impl.protocol.NameValue;
import com.microsoft.playwright.impl.protocol.ResourceTiming;
import com.microsoft.playwright.options.FilePayload;
import com.microsoft.playwright.options.HttpHeader;
import com.microsoft.playwright.options.SecurityDetails;
import com.microsoft.playwright.options.ServerAddr;
import com.microsoft.playwright.options.Timing;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.microsoft.playwright.impl.ChannelOwner.NO_TIMEOUT;
import static com.microsoft.playwright.impl.Errors.isTargetClosedError;

class APIResponseImpl implements APIResponse {
  private final com.microsoft.playwright.impl.protocol.APIResponse initializer;
  private final RawHeaders headers;
  final APIRequestContextImpl request;
  String apiName = "APIResponse";

  APIResponseImpl(APIRequestContextImpl context, com.microsoft.playwright.impl.protocol.APIResponse initializer) {
    this.request = context;
    this.initializer = initializer;
    this.headers = new RawHeaders(this.initializer.headers);

    // java: no inspect.custom, Node.js inspection has no Java counterpart.
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
    return Utils.convertType(initializer.serverAddr, ServerAddr.class);
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
        // java: the channel returns null for an empty reply.
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

  // java: the parsed JSON is a JsonElement.
  JsonElement json() {
    String content = text();
    return RequestImpl.parseJSON(content);
  }

  // java: [Symbol.asyncDispose] is not ported, Java has no async dispose.

  @Override
  public void dispose() {
    APIRequestContextDisposeAPIResponseParams params = new APIRequestContextDisposeAPIResponseParams();
    params.fetchUid = fetchUid();
    request.channel.disposeAPIResponse(params, NO_TIMEOUT);
  }

  // java: _inspect is not ported, Node.js inspection has no Java counterpart.

  String fetchUid() {
    return initializer.fetchUid;
  }

  List<String> fetchLog() {
    APIRequestContextFetchLogParams params = new APIRequestContextFetchLogParams();
    params.fetchUid = fetchUid();
    return request.channel.fetchLog(params, NO_TIMEOUT).log;
  }

  static FormField.FormFieldFile filePayloadToJson(FilePayload payload) {
    FormField.FormFieldFile file = new FormField.FormFieldFile();
    file.name = payload.name;
    file.mimeType = payload.mimeType;
    file.buffer = payload.buffer;
    return file;
  }

  // java: a file Path stands in for upstream's fs.ReadStream.
  static FormField.FormFieldFile readStreamToJson(Path path) {
    FilePayload payload = Utils.toFilePayload(path);
    FormField.FormFieldFile file = new FormField.FormFieldFile();
    file.name = payload.name;
    file.buffer = payload.buffer;
    return file;
  }

  static boolean isJsonContentType(List<NameValue> headers) {
    if (headers == null) {
      return false;
    }
    for (NameValue header : headers) {
      if ("content-type".equals(header.name.toLowerCase())) {
        return "application/json".equals(header.value.split(";", -1)[0].trim().toLowerCase());
      }
    }
    return false;
  }

  static List<NameValue> objectToArray(Map<String, ?> map) {
    if (map == null) {
      return null;
    }
    List<NameValue> result = new ArrayList<>();
    for (Map.Entry<String, ?> entry : map.entrySet()) {
      if (entry.getValue() != null) {
        NameValue nameValue = new NameValue();
        nameValue.name = entry.getKey();
        nameValue.value = String.valueOf(entry.getValue());
        result.add(nameValue);
      }
    }
    return result;
  }

  // java: the Java type decides it; a FilePayload with an empty name or mimeType is still a file payload.
  static boolean isFilePayload(Object value) {
    return value instanceof FilePayload;
  }
}
