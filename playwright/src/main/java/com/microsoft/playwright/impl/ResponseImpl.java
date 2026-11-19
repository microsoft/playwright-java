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

import com.microsoft.playwright.impl.protocol.*;
import com.google.gson.JsonObject;
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.HttpHeader;
import com.microsoft.playwright.options.SecurityDetails;
import com.microsoft.playwright.options.ServerAddr;
import com.microsoft.playwright.options.Timing;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static com.microsoft.playwright.impl.Serialization.gson;
import static com.microsoft.playwright.impl.ChannelOwner.NO_TIMEOUT;
import static java.util.Arrays.asList;

public class ResponseImpl extends ChannelOwner implements Response {
  private final RawHeaders headers;
  private RawHeaders rawHeaders;
  final RequestImpl request;

  final ResponseChannel channel = new ResponseChannel(this);

  ResponseImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
    headers = new RawHeaders(asList(gson().fromJson(initializer.getAsJsonArray("headers"), HttpHeader[].class)));
    request = connection.getExistingObject(initializer.getAsJsonObject("request").get("guid").getAsString());
    request.existingResponse = this;
    request.timing = gson().fromJson(initializer.get("timing"), Timing.class);
  }

  @Override
  public Map<String, String> allHeaders() {
    return getRawHeaders().headers();
  }

  @Override
  public byte[] body() {
    return channel.body(NO_TIMEOUT).binary;
  }

  @Override
  public String finished() {
    List<Waitable<String>> waitables = new ArrayList<>();
    waitables.add(new WaitableNever<String>() {
      @Override
      public boolean isDone() {
        return request.didFailOrFinish;
      }
      @Override
      public String get() {
        return request.failure();
      }
    });
    PageImpl page = request.safePage();
    if (page != null) {
      waitables.add(page.createWaitForCloseHelper());
      waitables.add(page.createWaitableTimeout(null));
    }
    runUntil(() -> {}, new WaitableRace<>(waitables));
    return request.failure();
  }

  @Override
  public Frame frame() {
    return request().frame();
  }

  @Override
  public boolean fromServiceWorker() {
    return initializer.get("fromServiceWorker").getAsBoolean();
  }

  @Override
  public String httpVersion() {
    return channel.httpVersion(NO_TIMEOUT).value;
  }

  @Override
  public Map<String, String> headers() {
    return headers.headers();
  }

  @Override
  public List<HttpHeader> headersArray() {
    return getRawHeaders().headersArray();
  }

  @Override
  public String headerValue(String name) {
    return getRawHeaders().get(name);
  }

  @Override
  public List<String> headerValues(String name) {
    return getRawHeaders().getAll(name);
  }

  @Override
  public boolean ok() {
    return status() == 0 || (status() >= 200 && status() <= 299);
  }

  @Override
  public RequestImpl request() {
    return request;
  }

  @Override
  public SecurityDetails securityDetails() {
    com.microsoft.playwright.impl.protocol.SecurityDetails value = channel.securityDetails(NO_TIMEOUT).value;
    if (value == null) {
      return null;
    }
    SecurityDetails result = new SecurityDetails();
    result.issuer = value.issuer;
    result.protocol = value.protocol;
    result.subjectName = value.subjectName;
    result.validFrom = value.validFrom;
    result.validTo = value.validTo;
    return result;
  }

  @Override
  public ServerAddr serverAddr() {
    RemoteAddr value = channel.serverAddr(NO_TIMEOUT).value;
    if (value == null) {
      return null;
    }
    ServerAddr result = new ServerAddr();
    result.ipAddress = value.ipAddress;
    result.port = value.port;
    return result;
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

  private RawHeaders getRawHeaders() {
    if (rawHeaders == null) {
      List<HttpHeader> headers = new ArrayList<>();
      for (NameValue nv : channel.rawResponseHeaders(NO_TIMEOUT).headers) {
        HttpHeader h = new HttpHeader();
        h.name = nv.name;
        h.value = nv.value;
        headers.add(h);
      }
      rawHeaders = new RawHeaders(headers);
    }
    return rawHeaders;
  }
}
