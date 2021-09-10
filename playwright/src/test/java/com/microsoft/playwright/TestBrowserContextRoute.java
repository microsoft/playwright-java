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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class TestBrowserContextRoute extends TestBase {

  @Test
  void shouldIntercept() {
    BrowserContext context = browser.newContext();
    boolean[] intercepted = {false};
    Page page = context.newPage();
    context.route("**/empty.html", route -> {
      intercepted[0] = true;
      Request request = route.request();
      assertTrue(request.url().contains("empty.html"));
      assertNotNull(request.headers().get("user-agent"));
      assertEquals("GET", request.method());
      assertNull(request.postData());
      assertTrue(request.isNavigationRequest());
      assertEquals("document", request.resourceType());
      assertEquals(page.mainFrame(), request.frame());
      assertEquals("about:blank", request.frame().url());
      route.resume();
    });
    Response response = page.navigate(server.EMPTY_PAGE);
    assertTrue(response.ok());
    assertTrue(intercepted[0]);
    context.close();
  }

  @Test
  void shouldUnroute() {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();

    List<Integer> intercepted = new ArrayList<>();
    context.route("**/*", route -> {
      intercepted.add(1);
      route.resume();
    });
    context.route("**/empty.html", route -> {
      intercepted.add(2);
      route.resume();
    });
    context.route("**/empty.html", route -> {
      intercepted.add(3);
      route.resume();
    });
    Consumer<Route> handler4 = route -> {
      intercepted.add(4);
      route.resume();
    };
    context.route("**/empty.html", handler4);
    page.navigate(server.EMPTY_PAGE);
    assertEquals(asList(4), intercepted);

    intercepted.clear();
    context.unroute("**/empty.html", handler4);
    page.navigate(server.EMPTY_PAGE);
    assertEquals(asList(3), intercepted);

    intercepted.clear();
    context.unroute("**/empty.html");
    page.navigate(server.EMPTY_PAGE);
    assertEquals(asList(1), intercepted);

    context.close();
  }

  @Test
  void shouldYieldToPageRoute() {
    BrowserContext context = browser.newContext();
    context.route("**/empty.html", route -> {
      route.fulfill(new Route.FulfillOptions().setStatus(200).setBody("context"));
    });
    Page page = context.newPage();
    page.route("**/empty.html", route -> {
      route.fulfill(new Route.FulfillOptions().setStatus(200).setBody("page"));
    });
    Response response = page.navigate(server.EMPTY_PAGE);
    assertTrue(response.ok());
    assertEquals("page", response.text());
    context.close();
  }

  @Test
  void shouldFallBackToContextRoute() {
    BrowserContext context = browser.newContext();
    context.route("**/empty.html", route -> {
      route.fulfill(new Route.FulfillOptions().setStatus(200).setBody("context"));
    });
    Page page = context.newPage();
    page.route("**/non-empty.html", route -> {
      route.fulfill(new Route.FulfillOptions().setStatus(200).setBody("page"));
    });
    Response response = page.navigate(server.EMPTY_PAGE);
    assertTrue(response.ok());
    assertEquals("context", response.text());
    context.close();
  }

  @Test
  void shouldSupportTheTimesParameterWithRouteMatching() {
    int[] intercepted = {0};
    context.route("**/empty.html", route -> {
      ++intercepted[0];
      route.resume();
    }, new BrowserContext.RouteOptions().setTimes(2));
    page.navigate(server.EMPTY_PAGE);
    page.navigate(server.EMPTY_PAGE);
    page.navigate(server.EMPTY_PAGE);
    assertEquals(2, intercepted[0]);
  }
}
