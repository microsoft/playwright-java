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

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.*;

public class TestPageRoute extends TestBase {

  @Test
  void shouldIntercept() {
    boolean[] intercepted = {false};
    page.route("**/empty.html", (route, request) -> {
      assertEquals(request, route.request());
      assertTrue(request.url().contains("empty.html"));
      assertNotNull(request.headers().get("user-agent"));
      assertEquals("GET", request.method());
      assertNull(request.postData());
      assertTrue(request.isNavigationRequest());
      assertEquals("document", request.resourceType());
      assertTrue(request.frame() == page.mainFrame());
      assertEquals("about:blank", request.frame().url());
      route.continue_();
      intercepted[0] = true;
    });
    Response response = page.navigate(server.EMPTY_PAGE);
    assertTrue(response.ok());
    assertTrue(intercepted[0]);
  }


  @Test
  void shouldUnroute() {
    List<Integer> intercepted = new ArrayList<>();
    BiConsumer<Route, Request> handler1 = (route, request) -> {
      intercepted.add(1);
      route.continue_();
    };
    page.route("**/empty.html", handler1);
    page.route("**/empty.html", (route, request) -> {
      intercepted.add(2);
      route.continue_();
    });
    page.route("**/empty.html", (route, request) -> {
      intercepted.add(3);
      route.continue_();
    });
    page.route("**/*", (route, request) -> {
      intercepted.add(4);
      route.continue_();
    });
    page.navigate(server.EMPTY_PAGE);
    assertEquals(Arrays.asList(1), intercepted);

    intercepted.clear();
    page.unroute("**/empty.html", handler1);
    page.navigate(server.EMPTY_PAGE);
    assertEquals(Arrays.asList(2), intercepted);

    intercepted.clear();
    page.unroute("**/empty.html");
    page.navigate(server.EMPTY_PAGE);
    assertEquals(Arrays.asList(4), intercepted);
  }

  @Test
  void shouldWorkWhenPOSTIsRedirectedWith302() {
    server.setRedirect("/rredirect", "/empty.html");
    page.navigate(server.EMPTY_PAGE);
    page.route("**/*", (route, request) -> route.continue_());
    page.setContent("<form action='/rredirect' method='post'>\n" +
      "  <input type='hidden' id='foo' name='foo' value='FOOBAR'>\n" +
      "</form>");
    page.evalOnSelector("form", "form => form.submit()");
    page.waitForNavigation().get();
  }

  // @see https://github.com/GoogleChrome/puppeteer/issues/3973
  @Test
  void shouldWorkWhenHeaderManipulationHeadersWithRedirect() {
    server.setRedirect("/rrredirect", "/empty.html");
    page.route("**/*", (route, request) -> {
      Map<String, String> headers = new HashMap<>(route.request().headers());
      headers.put("foo", "bar");
      route.continue_(new Route.ContinueOverrides().withHeaders(headers));
    });
    page.navigate(server.PREFIX + "/rrredirect");
  }

  // @see https://github.com/GoogleChrome/puppeteer/issues/4743
  @Test
  void shouldBeAbleToRemoveHeaders() throws ExecutionException, InterruptedException {
    page.route("**/*", (route, request) -> {
      Map<String, String> headers = new HashMap<>(route.request().headers());
      headers.put("foo", "bar");
      headers.remove("accept");
      route.continue_(new Route.ContinueOverrides().withHeaders(headers));
    });

    Future<Server.Request> serverRequest = server.waitForRequest("/empty.html");
    page.navigate(server.PREFIX + "/empty.html");
    assertFalse(serverRequest.get().headers.containsKey("accept"));
  }

  @Test
  void shouldContainRefererHeader() {
    List<Request> requests = new ArrayList<>();
    page.route("**/*", (route, request) -> {
      requests.add(route.request());
      route.continue_();
    });
    page.navigate(server.PREFIX + "/one-style.html");
    assertTrue(requests.get(1).url().contains("/one-style.css"));
    assertTrue(requests.get(1).headers().containsKey("referer"));
    assertTrue(requests.get(1).headers().get("referer").contains("/one-style.html"));
  }
}
