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

import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.impl.protocol.NameValue;
import com.microsoft.playwright.options.HttpHeader;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class RawHeaders {
  private final List<NameValue> headersArray;
  // java: MultiMap is a map of lists.
  private final Map<String, List<String>> headersMap = new LinkedHashMap<>();

  static RawHeaders fromHeadersObjectLossy(Map<String, String> headers) {
    List<NameValue> headersArray = new ArrayList<>();
    for (Map.Entry<String, String> entry : headers.entrySet()) {
      if (entry.getValue() == null) {
        continue;
      }
      NameValue header = new NameValue();
      header.name = entry.getKey();
      header.value = entry.getValue();
      headersArray.add(header);
    }
    return new RawHeaders(headersArray);
  }

  RawHeaders(List<NameValue> headers) {
    this.headersArray = headers;
    for (NameValue header : headers) {
      String name = header.name.toLowerCase();
      List<String> values = headersMap.get(name);
      if (values == null) {
        values = new ArrayList<>();
        headersMap.put(name, values);
      }
      values.add(header.value);
    }
  }

  String get(String name) {
    List<String> values = getAll(name);
    if (values == null || values.isEmpty()) {
      return null;
    }
    return String.join("set-cookie".equals(name.toLowerCase()) ? "\n" : ", ", values);
  }

  List<String> getAll(String name) {
    List<String> values = headersMap.get(name.toLowerCase());
    return values == null ? new ArrayList<>() : new ArrayList<>(values);
  }

  Map<String, String> headers() {
    Map<String, String> result = new LinkedHashMap<>();
    for (String name : headersMap.keySet()) {
      result.put(name, get(name));
    }
    return result;
  }

  // java: a new list of the public HttpHeader type on every call.
  List<HttpHeader> headersArray() {
    List<HttpHeader> result = new ArrayList<>();
    for (NameValue header : headersArray) {
      HttpHeader h = new HttpHeader();
      h.name = header.name;
      h.value = header.value;
      result.add(h);
    }
    return result;
  }

  // java: module-level functions of network.ts and isomorphic/headers.ts used with headers.
  static void validateHeaders(Map<String, String> headers) {
    if (headers == null) {
      return;
    }
    for (Map.Entry<String, String> entry : headers.entrySet()) {
      // java: a null value is a JavaScript null, typeof null is "object".
      if (entry.getValue() == null) {
        throw new PlaywrightException("Expected value of header \"" + entry.getKey() + "\" to be String, but \"object\" is found.");
      }
    }
  }

  static List<NameValue> headersObjectToArray(Map<String, String> headers) {
    List<NameValue> result = new ArrayList<>();
    for (Map.Entry<String, String> entry : headers.entrySet()) {
      if (entry.getValue() == null) {
        continue;
      }
      NameValue header = new NameValue();
      header.name = entry.getKey();
      header.value = entry.getValue();
      result.add(header);
    }
    return result;
  }
}
