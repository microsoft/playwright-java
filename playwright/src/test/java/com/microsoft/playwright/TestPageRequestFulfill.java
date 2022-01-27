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

import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.Utils.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestPageRequestFulfill extends TestBase {
  @Test
  void shouldFulfillWithGlobalFetchResult() {
    page.route("**/*", route -> {
      APIRequestContext request = playwright.request().newContext();
      APIResponse response = request.get(server.PREFIX + "/simple.json");
      route.fulfill(new Route.FulfillOptions().setResponse(response));
    });
    Response response = page.navigate(server.EMPTY_PAGE);
    assertEquals("application/json", response.headers().get("content-type"));
    assertEquals(200, response.status());
    assertEquals("{\"foo\": \"bar\"}\n", response.text());
  }

  @Test
  void shouldFulfillWithFetchResult() {
    page.route("**/*", route -> {
      APIResponse response = page.request().get(server.PREFIX + "/simple.json");
      route.fulfill(new Route.FulfillOptions().setResponse(response));
    });
    Response response = page.navigate(server.EMPTY_PAGE);
    assertEquals("application/json", response.headers().get("content-type"));
    assertEquals(200, response.status());
    assertEquals("{\"foo\": \"bar\"}\n", response.text());
  }

  @Test
  void shouldFulfillWithFetchResultAndOverrides() {
    page.route("**/*", route -> {
      APIResponse response = page.request().get(server.PREFIX + "/simple.json");
      route.fulfill(new Route.FulfillOptions().setResponse(response)
        .setStatus(201).setHeaders(mapOf("Content-Type", "application/json", "foo", "bar")));
    });
    Response response = page.navigate(server.EMPTY_PAGE);
    assertEquals("application/json", response.headers().get("content-type"));
    assertEquals(201, response.status());
    assertEquals("bar", response.allHeaders().get("foo"));
    assertEquals("{\"foo\": \"bar\"}\n", response.text());
  }
}
