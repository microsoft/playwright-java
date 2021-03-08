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

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.microsoft.playwright.Utils.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestNetworkPostData extends TestBase {
  @Test
  void shouldReturnCorrectPostDataBufferForUtf8Body() {
    page.navigate(server.EMPTY_PAGE);
    String value = "baẞ";
    Request request = page.waitForRequest("**", () -> {
      page.evaluate("({url, value}) => {\n" +
        "  const request = new Request(url, {\n" +
        "    method: 'POST',\n" +
        "    body: JSON.stringify(value),\n" +
        "  });\n" +
        "  request.headers.set('content-type', 'application/json;charset=UTF-8');\n" +
        "  return fetch(request);\n" +
        "}", mapOf("url", server.PREFIX + "/title.html", "value", value));
    });
    assertTrue(Arrays.equals(new Gson().toJson(value).getBytes(StandardCharsets.UTF_8), request.postDataBuffer()));
    assertEquals(new Gson().toJson(value), request.postData());
  }

  @Test
  void shouldReturnPostDataWOContentType() {
    page.navigate(server.EMPTY_PAGE);
    Request request = page.waitForRequest("**", () -> {
      page.evaluate("({url}) => {\n" +
        "  const request = new Request(url, {\n" +
        "    method: 'POST',\n" +
        "    body: JSON.stringify({ value: 42 }),\n" +
        "  });\n" +
        "  request.headers.set('content-type', '');\n" +
        "  return fetch(request);\n" +
        "}", mapOf("url", server.PREFIX + "/title.html"));
    });
    assertEquals(new Gson().toJson(mapOf("value", 42)), request.postData());
  }

  void shouldThrowOnInvalidJSONInPostData() {
      // Thre is no postDataJSON() in java
  }

  @Test
  void shouldReturnPostDataForPUTRequests() {
    page.navigate(server.EMPTY_PAGE);
    Request request = page.waitForRequest("**", () -> {
      page.evaluate("({url}) => {\n" +
        "  const request = new Request(url, {\n" +
        "    method: 'PUT',\n" +
        "    body: JSON.stringify({ value: 42 }),\n" +
        "  });\n" +
        "  return fetch(request);\n" +
        "}", mapOf("url", server.PREFIX + "/title.html"));
    });
    assertEquals(new Gson().toJson(mapOf("value", 42)), request.postData());
  }
}
