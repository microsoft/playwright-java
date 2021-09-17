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

import com.microsoft.playwright.options.HttpHeader;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class RawHeaders {
  private final List<HttpHeader> headersArray;
  private final Map<String, List<String>> headersMap = new LinkedHashMap<>();

  RawHeaders(List<HttpHeader> headers) {
    headersArray = headers;
    for (HttpHeader h: headers) {
      String name = h.name.toLowerCase();
      List<String> values = headersMap.get(name);
      if (values == null) {
        values = new ArrayList<>();
        headersMap.put(name, values);
      }
      values.add(h.value);
    }
  }

  String get(String name) {
    List<String> values = getAll(name);
    if (values == null) {
      return null;
    }
    return String.join("set-cookie".equals(name.toLowerCase()) ? "\n" : ", ", values);
  }

  List<String> getAll(String name) {
    return headersMap.get(name.toLowerCase());
  }

  Map<String, String> headers() {
    Map<String, String> result = new LinkedHashMap<>();
    for (String name: headersMap.keySet()) {
      result.put(name, get(name));
    }
    return result;
  }

  List<HttpHeader> headersArray() {
    return headersArray;
  }

}
