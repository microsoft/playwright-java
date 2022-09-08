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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

public class TestRequestContinue extends TestBase {

  @Test
  void shouldWork() {
    page.route("**/*", route -> route.resume());
    page.navigate(server.EMPTY_PAGE);
  }

  @Test
  void shouldAmendHTTPHeaders() throws ExecutionException, InterruptedException {
    page.route("**/*", route -> {
      Map<String, String> headers = new HashMap<>(route.request().headers());
      headers.put("FOO", "bar");
      route.resume(new Route.ResumeOptions().setHeaders(headers));
    });
    page.navigate(server.EMPTY_PAGE);
    Future<Server.Request> request = server.futureRequest("/sleep.zzz");
    page.evaluate("() => fetch('/sleep.zzz')");
    assertEquals(Arrays.asList("bar"), request.get().headers.get("foo"));
  }

  @Test
  void shouldAmendMethod() throws ExecutionException, InterruptedException {
    Future<Server.Request> sRequest = server.futureRequest("/sleep.zzz");
    page.navigate(server.EMPTY_PAGE);
    page.route("**/*", route -> route.resume(new Route.ResumeOptions().setMethod("POST")));
    Future<Server.Request> request = server.futureRequest("/sleep.zzz");
    page.evaluate("() => fetch('/sleep.zzz')");
    assertEquals("POST", request.get().method);
    assertEquals("POST", sRequest.get().method);
  }

  @Test
  void shouldOverrideRequestUrl() throws ExecutionException, InterruptedException {
    Future<Server.Request> serverRequest = server.futureRequest("/global-var.html");
    page.route("**/foo", route -> {
      route.resume(new Route.ResumeOptions().setUrl(server.PREFIX + "/global-var.html"));
    });
    Response response = page.navigate(server.PREFIX + "/foo");
    assertEquals(server.PREFIX + "/global-var.html", response.url());
    assertEquals(server.PREFIX + "/global-var.html", response.request().url());
    assertEquals(123, page.evaluate("window['globalVar']"));
    assertEquals("GET", serverRequest.get().method);
  }

  @Test
  @Disabled("resume() method is now asynchronous")
  void shouldNotAllowChangingProtocolWhenOverridingUrl() {
    page.route("**/*", route -> {
      PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
        route.resume(new Route.ResumeOptions().setUrl("file:///tmp/foo"));
      });
      assertTrue(e.getMessage().contains("New URL must have same protocol as overridden URL"), e.getMessage());
      route.resume();
    });
    page.navigate(server.EMPTY_PAGE);
  }

  @Test
  void shouldOverrideMethodAlongWithUrl() throws ExecutionException, InterruptedException {
    Future<Server.Request> serverRequest = server.futureRequest("/empty.html");
    page.route("**/foo", route -> {
      route.resume(new Route.ResumeOptions().setUrl(server.EMPTY_PAGE).setMethod("POST"));
    });
    page.navigate(server.PREFIX + "/foo");
    assertEquals("POST", serverRequest.get().method);
  }

  @Test
  void shouldAmendMethodOnMainRequest() throws ExecutionException, InterruptedException {
    Future<Server.Request> request = server.futureRequest("/empty.html");
    page.route("**/*", route -> route.resume(new Route.ResumeOptions().setMethod("POST")));
    page.navigate(server.EMPTY_PAGE);
    assertEquals("POST", request.get().method);
  }

  @Test
  void shouldAmendPostData() throws ExecutionException, InterruptedException {
    page.navigate(server.EMPTY_PAGE);
    page.route("**/*", route -> {
      route.resume(new Route.ResumeOptions().setPostData("doggo"));
    });
    Future<Server.Request> serverRequest = server.futureRequest("/sleep.zzz");
    page.evaluate("() => fetch('/sleep.zzz', { method: 'POST', body: 'birdy' })");
    assertEquals("doggo", new String(serverRequest.get().postBody, UTF_8));
  }

  @Test
  void shouldAmendUtf8PostData() throws ExecutionException, InterruptedException {
    page.navigate(server.EMPTY_PAGE);
    page.route("**/*", route -> {
      route.resume(new Route.ResumeOptions().setPostData("пушкин"));
    });
    Future<Server.Request> serverRequest = server.futureRequest("/sleep.zzz");
    page.evaluate("() => fetch('/sleep.zzz', { method: 'POST', body: 'birdy' })");
    assertEquals("POST", serverRequest.get().method);
    assertEquals("пушкин", new String(serverRequest.get().postBody, UTF_8));
  }

  @Test
  void shouldAmendLongerPostData() throws ExecutionException, InterruptedException {
    page.navigate(server.EMPTY_PAGE);
    page.route("**/*", route -> {
      route.resume(new Route.ResumeOptions().setPostData("doggo-is-longer-than-birdy"));
    });
    Future<Server.Request> serverRequest = server.futureRequest("/sleep.zzz");
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
    page.route("**/*", route -> {
      route.resume(new Route.ResumeOptions().setPostData(arr));
    });
    Future<Server.Request> serverRequest = server.futureRequest("/sleep.zzz");
    page.evaluate("() => fetch('/sleep.zzz', { method: 'POST', body: 'birdy' })");
    assertEquals("POST", serverRequest.get().method);
    byte[] buffer = serverRequest.get().postBody;
    assertEquals(arr.length, buffer.length);
    assertTrue(Arrays.equals(arr, buffer));
  }

}
