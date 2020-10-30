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

import java.io.OutputStreamWriter;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static com.microsoft.playwright.Page.EventType.REQUEST;
import static com.microsoft.playwright.Page.EventType.REQUESTFAILED;
import static com.microsoft.playwright.Utils.mapOf;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class TestPageRoute extends TestBase {

  @Test
  void shouldIntercept() {
    boolean[] intercepted = {false};
    page.route("**/empty.html", route -> {
      Request request = route.request();
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
    Consumer<Route> handler1 = route -> {
      intercepted.add(1);
      route.continue_();
    };
    page.route("**/empty.html", handler1);
    page.route("**/empty.html", route -> {
      intercepted.add(2);
      route.continue_();
    });
    page.route("**/empty.html", route -> {
      intercepted.add(3);
      route.continue_();
    });
    page.route("**/*", route -> {
      intercepted.add(4);
      route.continue_();
    });
    page.navigate(server.EMPTY_PAGE);
    assertEquals(asList(1), intercepted);

    intercepted.clear();
    page.unroute("**/empty.html", handler1);
    page.navigate(server.EMPTY_PAGE);
    assertEquals(asList(2), intercepted);

    intercepted.clear();
    page.unroute("**/empty.html");
    page.navigate(server.EMPTY_PAGE);
    assertEquals(asList(4), intercepted);
  }

  @Test
  void shouldWorkWhenPOSTIsRedirectedWith302() {
    server.setRedirect("/rredirect", "/empty.html");
    page.navigate(server.EMPTY_PAGE);
    page.route("**/*", route -> route.continue_());
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
    page.route("**/*", route -> {
      Map<String, String> headers = new HashMap<>(route.request().headers());
      headers.put("foo", "bar");
      route.continue_(new Route.ContinueOverrides().withHeaders(headers));
    });
    page.navigate(server.PREFIX + "/rrredirect");
  }

  // @see https://github.com/GoogleChrome/puppeteer/issues/4743
  @Test
  void shouldBeAbleToRemoveHeaders() throws ExecutionException, InterruptedException {
    page.route("**/*", route -> {
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
    page.route("**/*", route -> {
      requests.add(route.request());
      route.continue_();
    });
    page.navigate(server.PREFIX + "/one-style.html");
    assertTrue(requests.get(1).url().contains("/one-style.css"));
    assertTrue(requests.get(1).headers().containsKey("referer"));
    assertTrue(requests.get(1).headers().get("referer").contains("/one-style.html"));
  }

  @Test
  void shouldProperlyReturnNavigationResponseWhenURLHasCookies() {
    // Setup cookie.
    page.navigate(server.EMPTY_PAGE);
    context.addCookies(asList(new BrowserContext.AddCookie()
      .withUrl(server.EMPTY_PAGE).withName("foo").withValue("bar")));
    // Setup request interception.
    page.route("**/*", route -> route.continue_());
    Response response = page.reload();
    assertEquals(200, response.status());
  }

  @Test
  void shouldShowCustomHTTPHeaders() {
    page.setExtraHTTPHeaders(mapOf("foo", "bar"));
    page.route("**/*", route -> {
      assertEquals("bar", route.request().headers().get("foo"));
      route.continue_();
    });
    Response response = page.navigate(server.EMPTY_PAGE);
    assertTrue(response.ok());
  }

  // @see https://github.com/GoogleChrome/puppeteer/issues/4337
  @Test
  void shouldWorkWithRedirectInsideSyncXHR() {
    page.navigate(server.EMPTY_PAGE);
    server.setRedirect("/logo.png", "/pptr.png");
    page.route("**/*", route -> route.continue_());
    Object status = page.evaluate("async () => {\n" +
      "  const request = new XMLHttpRequest();\n" +
      "  request.open('GET', '/logo.png', false);  // `false` makes the request synchronous\n" +
      "  request.send(null);\n" +
      "  return request.status;\n" +
      "}");
    assertEquals(200, status);
  }

  @Test
  void shouldWorkWithCustomRefererHeaders() {
    page.setExtraHTTPHeaders(mapOf("referer", server.EMPTY_PAGE));
    page.route("**/*", route -> {
      assertEquals(server.EMPTY_PAGE, route.request().headers().get("referer"));
      route.continue_();
    });
    Response response = page.navigate(server.EMPTY_PAGE);
    assertTrue(response.ok());
  }

  @Test
  void shouldBeAbortable() {
    page.route(Pattern.compile(".*\\.css$"), route -> route.abort());
    boolean[] failed = {false};
    page.addListener(REQUESTFAILED, event -> {
      Request request = (Request) event.data();
      if (request.url().contains(".css"))
        failed[0] = true;
    });
    Response response = page.navigate(server.PREFIX + "/one-style.html");
    assertTrue(response.ok());
    assertNull(response.request().failure());
    assertTrue(failed[0]);
  }

  @Test
  void shouldBeAbortableWithCustomErrorCodes() {
    page.route("**/*", route -> route.abort("internetdisconnected"));
    Request[] failedRequest = {null};
    page.addListener(REQUESTFAILED, event -> failedRequest[0] = (Request) event.data());
    try {
      page.navigate(server.EMPTY_PAGE);
    } catch (PlaywrightException e) {
    }
    assertNotNull(failedRequest[0]);
    if (isWebKit)
      assertEquals("Request intercepted", failedRequest[0].failure().errorText());
    else if (isFirefox)
      assertEquals("NS_ERROR_OFFLINE", failedRequest[0].failure().errorText());
    else
      assertEquals("net::ERR_INTERNET_DISCONNECTED", failedRequest[0].failure().errorText());
  }

  @Test
  void shouldSendReferer() throws ExecutionException, InterruptedException {
    page.setExtraHTTPHeaders(mapOf("referer", "http://google.com/"));
    page.route("**/*", route -> route.continue_());
    Future<Server.Request> request = server.waitForRequest("/grid.html");
    page.navigate(server.PREFIX + "/grid.html");
    assertEquals(asList("http://google.com/"), request.get().headers.get("referer"));
  }

  @Test
  void shouldFailNavigationWhenAbortingMainResource() {
    page.route("**/*", route -> route.abort());
    try {
      page.navigate(server.EMPTY_PAGE);
      fail("did not throw");
    } catch (PlaywrightException e) {
      if (isWebKit)
        assertTrue(e.getMessage().contains("Request intercepted"));
      else if (isFirefox)
        assertTrue(e.getMessage().contains("NS_ERROR_FAILURE"));
      else
        assertTrue(e.getMessage().contains("net::ERR_FAILED"));
    }
  }


  @Test
  void shouldNotWorkWithRedirects() {
    List<Request> intercepted = new ArrayList<>();
    page.route("**/*", route -> {
      route.continue_();
      intercepted.add(route.request());
    });
    server.setRedirect("/non-existing-page.html", "/non-existing-page-2.html");
    server.setRedirect("/non-existing-page-2.html", "/non-existing-page-3.html");
    server.setRedirect("/non-existing-page-3.html", "/non-existing-page-4.html");
    server.setRedirect("/non-existing-page-4.html", "/empty.html");

    Response response = page.navigate(server.PREFIX + "/non-existing-page.html");
    assertEquals(200, response.status());
    assertTrue(response.url().contains("empty.html"));

    assertEquals(1, intercepted.size());
    assertEquals("document", intercepted.get(0).resourceType());
    assertTrue(intercepted.get(0).isNavigationRequest());
    assertTrue(intercepted.get(0).url().contains("/non-existing-page.html"));

    List<Request> chain = new ArrayList<>();
    for (Request r = response.request(); r != null; r = r.redirectedFrom()) {
      chain.add(r);
      assertTrue(r.isNavigationRequest());
    }
    assertEquals(5, chain.size());
    assertTrue(chain.get(0).url().contains("/empty.html"));
    assertTrue(chain.get(1).url().contains("/non-existing-page-4.html"));
    assertTrue(chain.get(2).url().contains("/non-existing-page-3.html"));
    assertTrue(chain.get(3).url().contains("/non-existing-page-2.html"));
    assertTrue(chain.get(4).url().contains("/non-existing-page.html"));
    for (int i = 0; i < chain.size(); i++) {
      assertEquals(i != 0 ? chain.get(i - 1) : null, chain.get(i).redirectedTo());
    }
  }

  @Test
  void shouldWorkWithRedirectsForSubresources() {
    List<Request> intercepted = new ArrayList<>();
    page.route("**/*", route -> {
      route.continue_();
      intercepted.add(route.request());
    });
    server.setRedirect("/one-style.css", "/two-style.css");
    server.setRedirect("/two-style.css", "/three-style.css");
    server.setRedirect("/three-style.css", "/four-style.css");
    server.setRoute("/four-style.css", exchange -> {
      exchange.sendResponseHeaders(200, 0);
      try (OutputStreamWriter writer = new OutputStreamWriter(exchange.getResponseBody())) {
        writer.write("body {box-sizing: border-box; } ");
      }
    });
    Response response = page.navigate(server.PREFIX + "/one-style.html");
    assertEquals(200, response.status());
    assertTrue(response.url().contains("one-style.html"));

    assertEquals(2, intercepted.size());
    assertEquals("document", intercepted.get(0).resourceType());
    assertTrue(intercepted.get(0).url().contains("one-style.html"));

    Request r = intercepted.get(1);
    for (String url : asList("/one-style.css", "/two-style.css", "/three-style.css", "/four-style.css")) {
      assertEquals("stylesheet", r.resourceType());
      assertTrue(r.url().contains(url));
      r = r.redirectedTo();
    }
    assertNull(r);
  }

  @Test
  void shouldWorkWithEqualRequests() {
    page.navigate(server.EMPTY_PAGE);
    AtomicInteger responseCount = new AtomicInteger(1);
    server.setRoute("/zzz", exchange -> {
      exchange.sendResponseHeaders(200, 0);
      try (OutputStreamWriter writer = new OutputStreamWriter(exchange.getResponseBody())) {
        writer.write((responseCount.getAndIncrement()) * 11 + "");
      }
    });

    boolean[] spinner = {false};
    // Cancel 2nd request.
    page.route("**/*", route -> {
      if (spinner[0]) {
        route.abort();
      } else {
        route.continue_();
      }
      spinner[0] = !spinner[0];
    });
    List<Object> results = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      results.add(page.evaluate("() => fetch('/zzz').then(response => response.text()).catch(e => 'FAILED')"));
    }
    assertEquals(asList("11", "FAILED", "22"), results);
  }

  @Test
  void shouldNavigateToDataURLAndNotFireDataURLRequests() {
    List<Request> requests = new ArrayList<>();
    page.route("**/*", route -> {
      requests.add(route.request());
      route.continue_();
    });
    String dataURL = "data:text/html,<div>yo</div>";
    Response response = page.navigate(dataURL);
    assertNull(response);
    assertEquals(0, requests.size());
  }

  @Test
  void shouldBeAbleToFetchDataURLAndNotFireDataURLRequests() {
    page.navigate(server.EMPTY_PAGE);
    List<Request> requests = new ArrayList<>();
    page.route("**/*", route -> {
      requests.add(route.request());
      route.continue_();
    });
    String dataURL = "data:text/html,<div>yo</div>";
    Object text = page.evaluate("url => fetch(url).then(r => r.text())", dataURL);
    assertEquals("<div>yo</div>", text);
    assertEquals(0, requests.size());
  }

  @Test
  void shouldNavigateToURLWithHashAndAndFireRequestsWithoutHash() {
    List<Request> requests = new ArrayList<>();
    page.route("**/*", route -> {
      requests.add(route.request());
      route.continue_();
    });
    Response response = page.navigate(server.EMPTY_PAGE + "#hash");
    assertEquals(200, response.status());
    assertEquals(server.EMPTY_PAGE, response.url());
    assertEquals(1, requests.size());
    assertEquals(server.EMPTY_PAGE, requests.get(0).url());
  }

  @Test
  void shouldWorkWithEncodedServer() throws InterruptedException {
    // The requestWillBeSent will report encoded URL, whereas interception will
    // report URL as-is. @see crbug.com/759388
    page.route("**/*", route -> route.continue_());
    Response response = page.navigate(server.PREFIX + "/some nonexisting page");
    assertEquals(404, response.status());
  }

  @Test
  void shouldWorkWithBadlyEncodedServer() {
    server.setRoute("/malformed", exchange -> {
      exchange.sendResponseHeaders(200, 0);
      exchange.getResponseBody().close();
    });
    page.route("**/*", route -> route.continue_());
    Response response = page.navigate(server.PREFIX + "/malformed?rnd=%911");
    assertEquals(200, response.status());
  }

  @Test
  void shouldWorkWithEncodedServer2() {
    // The requestWillBeSent will report URL as-is, whereas interception will
    // report encoded URL for stylesheet. @see crbug.com/759388
    List<Request> requests = new ArrayList<>();
    page.route("**/*", route -> {
      route.continue_();
      requests.add(route.request());
    });
    Response response = page.navigate("data:text/html,<link rel='stylesheet' href='" + server.PREFIX + "/fonts?helvetica|arial'/>");
    assertNull(response);
    assertEquals(1, requests.size());
    assertEquals(400, (requests.get(0).response()).status());
  }

  @Test
  void shouldNotThrowInvalidInterceptionIdIfTheRequestWasCancelled() {
    page.setContent("<iframe></iframe>");
    Route[] route = {null};
    page.route("**/*", r -> route[0] = r);
    // Wait for request interception.
    Deferred<Event<Page.EventType>> event = page.waitForEvent(REQUEST);
    page.evalOnSelector("iframe", "(frame, url) => frame.src = url", server.EMPTY_PAGE);
    event.get();
    // Delete frame to cause request to be canceled.
    page.evalOnSelector("iframe", "frame => frame.remove()");
    try {
      route[0].continue_();
    } catch (PlaywrightException e) {
      fail("Should not throw");
    }
  }

  @Test
  void shouldInterceptMainResourceDuringCrossProcessNavigation() {
    page.navigate(server.EMPTY_PAGE);
    boolean[] intercepted = {false};
    page.route(server.CROSS_PROCESS_PREFIX + "/empty.html", route -> {
      intercepted[0] = true;
      route.continue_();
    });
    Response response = page.navigate(server.CROSS_PROCESS_PREFIX + "/empty.html");
    assertTrue(response.ok());
    assertTrue(intercepted[0]);
  }

  @Test
  void shouldCreateARedirect() {
    page.navigate(server.PREFIX + "/empty.html");
    page.route("**/*", route -> {
      if (!route.request().url().equals(server.PREFIX + "/redirect_this")) {
        route.continue_();
        return;
      }
      route.fulfill(new Route.FulfillResponse()
        .withStatus(301)
        .withHeaders(mapOf("location", "/empty.html")));
    });
    Object text = page.evaluate("async url => {\n" +
      "  const data = await fetch(url);\n" +
      "  return data.text();\n" +
      "}", server.PREFIX + "/redirect_this");
    assertEquals("", text);
  }

  @Test
  void shouldSupportCorsWithGET() {
    page.navigate(server.EMPTY_PAGE);
    page.route("**/cars*", route -> {
      Map<String, String> headers = new HashMap<>();
      if (route.request().url().endsWith("allow")) {
        headers.put("access-control-allow-origin", "*");
      }
      route.fulfill(new Route.FulfillResponse()
        .withStatus(200)
        .withContentType("application/json")
        .withHeaders(headers)
        .withBody("[\"electric\",\"gas\"]"));
    });
    {
      // Should succeed
      Object resp = page.evaluate("async () => {\n" +
        "  const response = await fetch('https://example.com/cars?allow', { mode: 'cors' });\n" +
        "  return response.json();\n" +
        "}");
      assertEquals(asList("electric", "gas"), resp);
    }
    {
      // Should be rejected
      try {
        page.evaluate("async () => {\n" +
          "  const response = await fetch('https://example.com/cars?reject', { mode: 'cors' });\n" +
          "  return response.json();\n" +
          "}");
        fail("did not throw");
      } catch (PlaywrightException e) {
        assertTrue(e.getMessage().contains("failed"));
      }
    }
  }

  @Test
  void shouldSupportCorsWithPOST() {
    page.navigate(server.EMPTY_PAGE);
    page.route("**/cars", route -> {
      route.fulfill(new Route.FulfillResponse()
        .withStatus(200)
        .withContentType("application/json")
        .withHeaders(mapOf("Access-Control-Allow-Origin", "*"))
        .withBody("[\"electric\",\"gas\"]"));
    });
    Object resp = page.evaluate("async () => {\n" +
      "  const response = await fetch('https://example.com/cars', {\n" +
      "    method: 'POST',\n" +
      "    headers: { 'Content-Type': 'application/json' },\n" +
      "    mode: 'cors',\n" +
      "    body: JSON.stringify({ 'number': 1 })\n" +
      "  });\n" +
      "  return response.json();\n" +
      "}");
    assertEquals(asList("electric", "gas"), resp);
  }

  @Test
  void shouldSupportCorsWithCredentials() {
    page.navigate(server.EMPTY_PAGE);
    page.route("**/cars", route -> {
      route.fulfill(new Route.FulfillResponse()
        .withStatus(200)
        .withContentType("application/json")
        .withHeaders(mapOf("Access-Control-Allow-Origin", server.PREFIX,
                                   "Access-Control-Allow-Credentials", "true"))
        .withBody("[\"electric\",\"gas\"]"));
    });
    Object resp = page.evaluate("async () => {\n" +
      "  const response = await fetch('https://example.com/cars', {\n" +
      "    method: 'POST',\n" +
      "    headers: { 'Content-Type': 'application/json' },\n" +
      "    mode: 'cors',\n" +
      "    body: JSON.stringify({ 'number': 1 }),\n" +
      "    credentials: 'include'\n" +
      "  });\n" +
      "  return response.json();\n" +
      "}");
    assertEquals(asList("electric", "gas"), resp);
  }

  @Test
  void shouldRejectCorsWithDisallowedCredentials() {
    page.navigate(server.EMPTY_PAGE);
    page.route("**/cars", route -> {
      route.fulfill(new Route.FulfillResponse()
        .withStatus(200)
        .withContentType("application/json")
        // Should fail without this line below!
        // "Access-Control-Allow-Credentials": "true"
        .withHeaders(mapOf("Access-Control-Allow-Origin", server.PREFIX))
        .withBody("[\"electric\",\"gas\"]"));
    });
    try {
      page.evaluate("async () => {\n" +
        "  const response = await fetch('https://example.com/cars', {\n" +
        "    method: 'POST',\n" +
        "    headers: { 'Content-Type': 'application/json' },\n" +
        "    mode: 'cors',\n" +
        "    body: JSON.stringify({ 'number': 1 }),\n" +
        "    credentials: 'include'\n" +
        "  });\n" +
        "  return response.json();\n" +
        "}");
      fail("did not throw");
    } catch (PlaywrightException e) {
    }
  }

  @Test
  void shouldSupportCorsForDifferentMethods() {
    page.navigate(server.EMPTY_PAGE);
    page.route("**/cars", route -> {
      route.fulfill(new Route.FulfillResponse()
        .withStatus(200)
        .withContentType("application/json")
        .withHeaders(mapOf("Access-Control-Allow-Origin", "*"))
        .withBody("[\"" + route.request().method() + "\",\"electric\",\"gas\"]"));
    });
    // First POST
    {
      Object resp = page.evaluate("async () => {\n" +
        "  const response = await fetch('https://example.com/cars', {\n" +
        "    method: 'POST',\n" +
        "    headers: { 'Content-Type': 'application/json' },\n" +
        "    mode: 'cors',\n" +
        "    body: JSON.stringify({ 'number': 1 })\n" +
        "  });\n" +
        "  return response.json();\n" +
        "}");
      assertEquals(asList("POST", "electric", "gas"), resp);
    }
    // Then DELETE
    {
      Object resp = page.evaluate("async () => {\n" +
        "  const response = await fetch('https://example.com/cars', {\n" +
        "    method: 'DELETE',\n" +
        "    headers: {},\n" +
        "    mode: 'cors',\n" +
        "    body: ''\n" +
        "  });\n" +
        "  return response.json();\n" +
        "}");
      assertEquals(asList("DELETE", "electric", "gas"), resp);
    }
  }

}
