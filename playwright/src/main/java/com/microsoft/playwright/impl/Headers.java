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
package com.microsoft.playwright.impl;

import com.microsoft.playwright.impl.protocol.NameValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// java: headersArrayToObject and splitSetCookieHeader have no caller in the client.
final class Headers {
  private Headers() {
  }

  // java: the client never passes separator and setCookieSeparator; a null value is undefined.
  static List<NameValue> headersObjectToArray(Map<String, String> headers) {
    List<NameValue> result = new ArrayList<>();
    for (Map.Entry<String, String> entry : headers.entrySet()) {
      String name = entry.getKey();
      String values = entry.getValue();
      if (values == null) {
        continue;
      }
      NameValue header = new NameValue();
      header.name = name;
      header.value = values;
      result.add(header);
    }
    return result;
  }
}
