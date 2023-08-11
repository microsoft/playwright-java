package com.microsoft.playwright;

import org.junit.jupiter.api.Test;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

public class TestPageInterception extends TestBase {
  @Test
  void shouldWorkWithNavigationSmoke() {
    HashMap<String, Request> requests = new HashMap<>();
    page.route("**/*", route -> {
      String[] parts = route.request().url().split("/");
      requests.put(parts[parts.length - 1], route.request());
      route.resume();
    });
    server.setRedirect("/rrredirect", "/frames/one-frame.html");
    page.navigate(server.PREFIX + "/rrredirect");
    assertTrue(requests.get("rrredirect").isNavigationRequest());
    assertTrue(requests.get("frame.html").isNavigationRequest());
    assertFalse(requests.get("script.js").isNavigationRequest());
    assertFalse(requests.get("style.css").isNavigationRequest());
  }

  @Test
  void shouldInterceptAfterAServiceWorker() {
    page.navigate(server.PREFIX + "/serviceworkers/fetchdummy/sw.html");
    page.evaluate("() => window['activationPromise']");

    // Sanity check.
    Object swResponse = page.evaluate("() => window['fetchDummy']('foo')");
    assertEquals("responseFromServiceWorker:foo", swResponse);

    page.route("**/foo", route -> {
      int slash = route.request().url().lastIndexOf("/");
      String name = route.request().url().substring(slash + 1);
      route.fulfill(new Route.FulfillOptions()
        .setStatus(200)
        .setContentType("text/css").setBody("responseFromInterception:" + name));
    });

    // Page route is applied after service worker fetch event.
    Object swResponse2 = page.evaluate("() => window['fetchDummy']('foo')");
    assertEquals("responseFromServiceWorker:foo", swResponse2);

    // Page route is not applied to service worker initiated fetch.
    Object nonInterceptedResponse = page.evaluate("() => window['fetchDummy']('passthrough')");
    assertEquals("FAILURE: Not Found", nonInterceptedResponse);

    // Firefox does not want to fetch the redirect for some reason.
    if (!isFirefox()) {
      // Page route is not applied to service worker initiated fetch with redirect.
      server.setRedirect("/serviceworkers/fetchdummy/passthrough", "/simple.json");
      Object redirectedResponse = page.evaluate("() => window['fetchDummy']('passthrough')");
      assertEquals("{\"foo\": \"bar\"}\n", redirectedResponse);
    }
  }

  @Test
  void shouldFulfillInterceptedResponseUsingAlias() {
    page.route("**/*", route -> {
      APIResponse response = route.fetch();
      System.out.println(response.headers().get("content-type"));
      route.fulfill(new Route.FulfillOptions().setResponse(response));
    });
    Response response = page.navigate(server.PREFIX + "/empty.html");
    assertEquals(200, response.status());
    assertTrue(response.headers().get("content-type").contains("text/html"));
  }

  @Test
  void shouldSupportTimeoutOptionInRouteFetch() {
    server.setRoute("/slow", exchange -> {
      exchange.getResponseHeaders().set("Content-type", "text/plain");
      exchange.sendResponseHeaders(200, 4096);
    });

    page.route("**/*", route -> {
      PlaywrightException error = assertThrows(PlaywrightException.class,
        () -> route.fetch(new Route.FetchOptions().setTimeout(1000)));
      assertTrue(error.getMessage().contains("Request timed out after 1000ms"), error.getMessage());
    });
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.navigate(server.PREFIX + "/slow", new Page.NavigateOptions().setTimeout(2000)));
    assertTrue(error.getMessage().contains("Timeout 2000ms exceeded"), error.getMessage());
  }

  @Test
  void shouldInterceptWithUrlOverride() {
    page.route("**/*.html", route -> {
      APIResponse response = route.fetch(new Route.FetchOptions().setUrl(server.PREFIX + "/one-style.html"));
      route.fulfill(new Route.FulfillOptions().setResponse(response));
    });
    Response response = page.navigate(server.PREFIX + "/empty.html");
    assertEquals(200, response.status());
    assertTrue(response.text().contains("one-style.css"), response.text());
  }

  @Test
  void shouldInterceptWithPostDataOverride() throws ExecutionException, InterruptedException {
    Future<Server.Request> request = server.futureRequest("/empty.html");
    page.route("**/*.html", route -> {
      APIResponse response = route.fetch(new Route.FetchOptions().setPostData("{ \"foo\": \"bar\" }"));
      route.fulfill(new Route.FulfillOptions().setResponse(response));
    });
    page.navigate(server.PREFIX + "/empty.html");
    assertEquals("{ \"foo\": \"bar\" }", new String(request.get().postBody));
  }

  @Test
  void shouldNotFollowRedirectsWhenMaxRedirectsIsSetTo0InRouteFetch() {
    server.setRedirect("/foo", "/empty.html");
    page.route("**/*", route -> {
      APIResponse response = route.fetch(new Route.FetchOptions().setMaxRedirects(0));
      assertEquals("/empty.html", response.headers().get("location"));
      assertEquals(302, response.status());
      route.fulfill(new Route.FulfillOptions().setBody("hello"));
    });
    page.navigate(server.PREFIX + "/foo");
    assertTrue(page.content().contains("hello"));
  }

  @Test
  void shouldProperlyHandleCharacterSetsInGlobs() {
    page.route("**/[a-z]*.html", route -> {
      APIResponse response = route.fetch(new Route.FetchOptions().setUrl(server.PREFIX + "/one-style.html"));
      route.fulfill(new Route.FulfillOptions().setResponse(response));
    });
    Response response = page.navigate(server.PREFIX + "/empty.html");
    assertEquals(200, response.status());
    assertTrue(response.text().contains("one-style.css"), response.text());
  }
}
