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

package com.microsoft.playwright;

import com.sun.net.httpserver.HttpExchange;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultipartFormData {
  static MultipartFormData parseRequest(HttpExchange exchange) throws IOException {
    ByteArrayOutputStream bodyBytes = new ByteArrayOutputStream();
    try (OutputStream output = bodyBytes) {
      Utils.copy(exchange.getRequestBody(), output);
    }
    String body = new String(bodyBytes.toByteArray(), StandardCharsets.UTF_8);
    String contentType = exchange.getRequestHeaders().get("content-type").get(0);
    Matcher matcher = Pattern.compile("boundary=(.*)$").matcher(contentType);
    if (!matcher.find()) {
      throw new RuntimeException("Boundary not found!");
    }
    String boundary = matcher.group(1);
    return new MultipartFormData(body, boundary);
  }

  static class Field {
    final String filename;
    final String content;

    Field(String filename, String content) {
      this.filename = filename;
      this.content = content;
    }
  }

  final List<Field> fields = new ArrayList<>();

  MultipartFormData(String body, String boundary) {
    String[] parts = Pattern.compile("--" + boundary + "(--)?\r\n", Pattern.MULTILINE).split(body);
    for (String part : parts) {
      if (part.trim().length() == 0) {
        continue;
      }
      String[] headersAndContent = Pattern.compile("\r\n\r\n", Pattern.MULTILINE).split(part);
      if (headersAndContent.length != 2) {
        throw new RuntimeException("Unexpected format: " + part);
      }
      String headers = headersAndContent[0];
      String filename = null;
      for (String header: Pattern.compile("\r\n", Pattern.MULTILINE).split(headers)) {
        Matcher matcher = Pattern.compile("content-disposition: .*filename=\"([^\"]+)\"", Pattern.CASE_INSENSITIVE).matcher(header);
        if (!matcher.find()) {
          continue;
        }
        filename = matcher.group(1);
      }
      String content = headersAndContent[1];
      content = content.substring(0, content.length() - "\r\n".length());
      fields.add(new Field(filename, content));
    }
  }
}
