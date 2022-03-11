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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
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

  @Test
  void shouldOverwritePostBodyWithEmptyString() throws ExecutionException, InterruptedException {
    boolean[] routeHandled = {false};
    context.route("**/empty.html", route -> {
      route.resume(new Route.ResumeOptions().setPostData(""));
      routeHandled[0] = true;
    });

    Future<Server.Request> req = server.futureRequest("/empty.html");
    page.setContent("<script>\n" +
      "        (async () => {\n" +
      "            await fetch('" + server.EMPTY_PAGE + "', {\n" +
      "              method: 'POST',\n" +
      "              body: 'original',\n" +
      "            });\n" +
      "        })()\n" +
      "      </script>");
    while (!routeHandled[0]) {
      page.waitForTimeout(100);
    }
    byte[] body = req.get().postBody;
    assertEquals(0, body.length);
  }

  @Test
  void shouldNotSwallowExceptionsInRoute() throws ExecutionException, InterruptedException {
    context.route("**/empty.html", route -> {
      throw new RuntimeException("My Exception");
    });

    try {
      page.navigate(server.EMPTY_PAGE);
      fail("did not throw");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("My Exception"), e.getMessage());
    }
  }

  @Test
  @Disabled("Conflicts with https://github.com/microsoft/playwright-java/pull/680")
  void shouldNotSwallowExceptionsInFulfill() throws ExecutionException, InterruptedException {
    APIRequestContext request = playwright.request().newContext();
    APIResponse response = request.get(server.EMPTY_PAGE);
    response.dispose();
    page.route("**/*", route -> {
      // Fulfilling with dsiposed response will lead to a server-side exception.
      route.fulfill(new Route.FulfillOptions().setResponse(response));
    });
    try {
      page.navigate(server.EMPTY_PAGE);
      fail("did not throw");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("Fetch response has been disposed"), e.getMessage());
    }
  }

  @Test
  @Disabled("Conflicts with https://github.com/microsoft/playwright-java/pull/680")
  void shouldNotSwallowExceptionsInResume() throws ExecutionException, InterruptedException {
    page.route("**/*", route -> {
      route.resume(new Route.ResumeOptions().setUrl("file:///tmp"));
    });
    try {
      page.navigate(server.EMPTY_PAGE);
      fail("did not throw");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("New URL must have same protocol as overridden URL"), e.getMessage());
    }
  }
}
