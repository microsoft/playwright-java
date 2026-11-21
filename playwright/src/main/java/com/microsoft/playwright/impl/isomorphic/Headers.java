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

// Port of isomorphic/headers.ts.
package com.microsoft.playwright.impl.isomorphic;

import com.microsoft.playwright.impl.protocol.NameValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

// java: HeadersArray is a List of the protocol NameValue, HeadersObject a Map; a null value is undefined.
public final class Headers {
  private Headers() {
  }

  public static List<NameValue> headersObjectToArray(Map<String, String> headers) {
    return headersObjectToArray(headers, null, null);
  }

  public static List<NameValue> headersObjectToArray(Map<String, String> headers, String separator) {
    return headersObjectToArray(headers, separator, null);
  }

  // java: split() takes the separator literally and keeps trailing empty strings, as String.prototype.split does;
  // String.trim() only removes the ASCII whitespace of JS trim().
  public static List<NameValue> headersObjectToArray(Map<String, String> headers, String separator, String setCookieSeparator) {
    if (setCookieSeparator == null || setCookieSeparator.isEmpty())
      setCookieSeparator = separator;
    List<NameValue> result = new ArrayList<>();
    for (Map.Entry<String, String> entry : headers.entrySet()) {
      String name = entry.getKey();
      String values = entry.getValue();
      if (values == null)
        continue;
      if (separator != null && !separator.isEmpty()) {
        String sep = name.toLowerCase().equals("set-cookie") ? setCookieSeparator : separator;
        for (String value : values.split(Pattern.quote(sep), -1))
          result.add(nameValue(name, value.trim()));
      } else {
        result.add(nameValue(name, values));
      }
    }
    return result;
  }

  public static Map<String, String> headersArrayToObject(List<NameValue> headers, boolean lowerCase) {
    Map<String, String> result = new LinkedHashMap<>();
    for (NameValue header : headers)
      result.put(lowerCase ? header.name.toLowerCase() : header.name, header.value);
    return result;
  }

  public static List<NameValue> splitSetCookieHeader(List<NameValue> headers) {
    int index = -1;
    for (int i = 0; i < headers.size(); i++) {
      if (headers.get(i).name.toLowerCase().equals("set-cookie")) {
        index = i;
        break;
      }
    }
    if (index == -1)
      return headers;

    NameValue header = headers.get(index);
    List<String> values = Arrays.asList(header.value.split("\n", -1));
    if (values.size() == 1)
      return headers;
    List<NameValue> result = new ArrayList<>(headers);
    result.remove(index);
    List<NameValue> split = new ArrayList<>();
    for (String value : values)
      split.add(nameValue(header.name, value));
    result.addAll(index, split);
    return result;
  }

  // java: helpers with no counterpart upstream.

  // java: the { name, value } object literal.
  private static NameValue nameValue(String name, String value) {
    NameValue header = new NameValue();
    header.name = name;
    header.value = value;
    return header;
  }
}
