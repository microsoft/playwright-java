/**
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestRequestContinue extends TestBase {

  @Test
  void shouldWork() {
    page.route("**/*", (route, request) -> route.continue_());
    page.navigate(server.EMPTY_PAGE);
  }

  @Test
  void shouldAmendHTTPHeaders() throws ExecutionException, InterruptedException {
    page.route("**/*", (route, request) -> {
      Map<String, String> headers = new HashMap<>(route.request().headers());
      headers.put("FOO", "bar");
      route.continue_(new Route.ContinueOverrides().withHeaders(headers));
    });
    page.navigate(server.EMPTY_PAGE);
    Future<Server.Request> request = server.waitForRequest("/sleep.zzz");
    page.evaluate("() => fetch('/sleep.zzz')");
    assertEquals(Arrays.asList("bar"), request.get().headers.get("foo"));
  }

  @Test
  void shouldAmendMethod() throws ExecutionException, InterruptedException {
    Future<Server.Request> sRequest = server.waitForRequest("/sleep.zzz");
    page.navigate(server.EMPTY_PAGE);
    page.route("**/*", (route, request) -> route.continue_(new Route.ContinueOverrides().withMethod("POST")));
    Future<Server.Request> request = server.waitForRequest("/sleep.zzz");
    page.evaluate("() => fetch('/sleep.zzz')");
    assertEquals("POST", request.get().method);
    assertEquals("POST", sRequest.get().method);
  }

  @Test
  void shouldAmendMethodOnMainRequest() throws ExecutionException, InterruptedException {
    Future<Server.Request> request = server.waitForRequest("/empty.html");
    page.route("**/*", (route, req) -> route.continue_(new Route.ContinueOverrides().withMethod("POST")));
    page.navigate(server.EMPTY_PAGE);
    assertEquals("POST", request.get().method);
  }

  @Test
  void shouldAmendPostData() throws ExecutionException, InterruptedException {
    page.navigate(server.EMPTY_PAGE);
    page.route("**/*", (route, request) -> {
      route.continue_(new Route.ContinueOverrides().withPostData("doggo"));
    });
    Future<Server.Request> serverRequest = server.waitForRequest("/sleep.zzz");
    page.evaluate("() => fetch('/sleep.zzz', { method: 'POST', body: 'birdy' })");
    assertEquals("doggo", new String(serverRequest.get().postBody, UTF_8));
  }

  @Test
  void shouldAmendUtf8PostData() throws ExecutionException, InterruptedException {
    page.navigate(server.EMPTY_PAGE);
    page.route("**/*", (route, request) -> {
      route.continue_(new Route.ContinueOverrides().withPostData("пушкин"));
    });
    Future<Server.Request> serverRequest = server.waitForRequest("/sleep.zzz");
    page.evaluate("() => fetch('/sleep.zzz', { method: 'POST', body: 'birdy' })");
    assertEquals("POST", serverRequest.get().method);
    assertEquals("пушкин", new String(serverRequest.get().postBody, UTF_8));
  }

  @Test
  void shouldAmendLongerPostData() throws ExecutionException, InterruptedException {
    page.navigate(server.EMPTY_PAGE);
    page.route("**/*", (route, request) -> {
      route.continue_(new Route.ContinueOverrides().withPostData("doggo-is-longer-than-birdy"));
    });
    Future<Server.Request> serverRequest = server.waitForRequest("/sleep.zzz");
    page.evaluate("() => fetch('/sleep.zzz', { method: 'POST', body: 'birdy' })");
    assertEquals("POST", serverRequest.get().method);
    assertEquals("doggo-is-longer-than-birdy", new String(serverRequest.get().postBody, UTF_8));
  }

  @Test
  void shouldAmendBinaryPostData() throws ExecutionException, InterruptedException {
    page.navigate(server.EMPTY_PAGE);
    byte[] arr = new byte[256];
    for (int i = 0; i < arr.length; i++) {
      arr[i] = (byte) i;
    }
    page.route("**/*", (route, request) -> {
      route.continue_(new Route.ContinueOverrides().withPostData(arr));
    });
    Future<Server.Request> serverRequest = server.waitForRequest("/sleep.zzz");
    page.evaluate("() => fetch('/sleep.zzz', { method: 'POST', body: 'birdy' })");
    assertEquals("POST", serverRequest.get().method);
    byte[] buffer = serverRequest.get().postBody;
    assertEquals(arr.length, buffer.length);
    assertTrue(Arrays.equals(arr, buffer));
  }

}
