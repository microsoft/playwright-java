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

import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.impl.PlaywrightImpl;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import static com.microsoft.playwright.fixtures.BrowserName.FIREFOX;
import static org.junit.jupiter.api.Assertions.*;

public class  TestPageInterception {
  @PlaywrightTest
  void shouldWorkWithNavigationSmoke(Page page, Server server) {
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

  @PlaywrightTest
  void shouldInterceptAfterAServiceWorker(Page page, Server server, BrowserName browserName) {
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
    if (browserName != FIREFOX) {
      // Page route is not applied to service worker initiated fetch with redirect.
      server.setRedirect("/serviceworkers/fetchdummy/passthrough", "/simple.json");
      Object redirectedResponse = page.evaluate("() => window['fetchDummy']('passthrough')");
      assertEquals("{\"foo\": \"bar\"}\n", redirectedResponse);
    }
  }

  @PlaywrightTest
  void shouldFulfillInterceptedResponseUsingAlias(Page page, Server server) {
    page.route("**/*", route -> {
      APIResponse response = route.fetch();
      route.fulfill(new Route.FulfillOptions().setResponse(response));
    });
    Response response = page.navigate(server.PREFIX + "/empty.html");
    assertEquals(200, response.status());
    assertTrue(response.headers().get("content-type").contains("text/html"));
  }

  @PlaywrightTest
  void shouldSupportTimeoutOptionInRouteFetch(Page page, Server server) {
    server.setRoute("/slow", exchange -> {
      exchange.getResponseHeaders().set("Content-type", "text/plain");
      exchange.sendResponseHeaders(200, 4096);
    });

    page.route("**/*", route -> {
      PlaywrightException error = assertThrows(PlaywrightException.class,
        () -> route.fetch(new Route.FetchOptions().setTimeout(1000)));
      assertTrue(error.getMessage().contains("Timeout 1000ms exceeded"), error.getMessage());
    });
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.navigate(server.PREFIX + "/slow", new Page.NavigateOptions().setTimeout(2000)));
    assertTrue(error.getMessage().contains("Timeout 2000ms exceeded"), error.getMessage());
  }

  @PlaywrightTest
  void shouldInterceptWithUrlOverride(Page page, Server server) {
    page.route("**/*.html", route -> {
      APIResponse response = route.fetch(new Route.FetchOptions().setUrl(server.PREFIX + "/one-style.html"));
      route.fulfill(new Route.FulfillOptions().setResponse(response));
    });
    Response response = page.navigate(server.PREFIX + "/empty.html");
    assertEquals(200, response.status());
    assertTrue(response.text().contains("one-style.css"), response.text());
  }

  @PlaywrightTest
  void shouldInterceptWithPostDataOverride(Page page, Server server) throws ExecutionException, InterruptedException {
    Future<Server.Request> request = server.futureRequest("/empty.html");
    page.route("**/*.html", route -> {
      APIResponse response = route.fetch(new Route.FetchOptions().setPostData("{ \"foo\": \"bar\" }"));
      route.fulfill(new Route.FulfillOptions().setResponse(response));
    });
    page.navigate(server.PREFIX + "/empty.html");
    assertEquals("{ \"foo\": \"bar\" }", new String(request.get().postBody));
  }

  @PlaywrightTest
  void shouldNotFollowRedirectsWhenMaxRedirectsIsSetTo0InRouteFetch(Page page, Server server) {
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

  @PlaywrightTest
  void shouldWorkWithGlob(Playwright playwright) {
    assertTrue(globToRegex(playwright, "**/*.js").matcher("https://localhost:8080/foo.js").find());
    assertFalse(globToRegex(playwright, "**/*.css").matcher("https://localhost:8080/foo.js").find());
    assertFalse(globToRegex(playwright, "*.js").matcher("https://localhost:8080/foo.js").find());
    assertTrue(globToRegex(playwright, "https://**/*.js").matcher("https://localhost:8080/foo.js").find());
    assertTrue(globToRegex(playwright, "http://localhost:8080/simple/path.js").matcher("http://localhost:8080/simple/path.js").find());
    assertTrue(globToRegex(playwright, "**/{a,b}.js").matcher("https://localhost:8080/a.js").find());
    assertTrue(globToRegex(playwright, "**/{a,b}.js").matcher("https://localhost:8080/b.js").find());
    assertFalse(globToRegex(playwright, "**/{a,b}.js").matcher("https://localhost:8080/c.js").find());

    assertTrue(globToRegex(playwright, "**/*.{png,jpg,jpeg}").matcher("https://localhost:8080/c.jpg").find());
    assertTrue(globToRegex(playwright, "**/*.{png,jpg,jpeg}").matcher("https://localhost:8080/c.jpeg").find());
    assertTrue(globToRegex(playwright, "**/*.{png,jpg,jpeg}").matcher("https://localhost:8080/c.png").find());
    assertFalse(globToRegex(playwright, "**/*.{png,jpg,jpeg}").matcher("https://localhost:8080/c.css").find());
    assertTrue(globToRegex(playwright, "foo*").matcher("foo.js").find());
    assertFalse(globToRegex(playwright, "foo*").matcher("foo/bar.js").find());
    assertFalse(globToRegex(playwright, "http://localhost:3000/signin-oidc*").matcher("http://localhost:3000/signin-oidc/foo").find());
    assertTrue(globToRegex(playwright, "http://localhost:3000/signin-oidc*").matcher("http://localhost:3000/signin-oidcnice").find());

    assertTrue(globToRegex(playwright, "**/*.js").matcher("/foo.js").find());
    assertFalse(globToRegex(playwright, "asd/**.js").matcher("/foo.js").find());
    assertFalse(globToRegex(playwright, "**/*.js").matcher("bar_foo.js").find());

    // custom protocols
    assertTrue(globToRegex(playwright, "my.custom.protocol://**").matcher("my.custom.protocol://foo").find());
    assertFalse(globToRegex(playwright, "my.{p,y}://**").matcher("my.p://foo").find());
    assertTrue(globToRegex(playwright, "my.{p,y}://**").matcher("my.p://foo/").find());
    assertTrue(globToRegex(playwright, "f*e://**").matcher("file:///foo/").find());

    // range [] is NOT supported
    assertTrue(globToRegex(playwright, "**/api/v[0-9]").matcher("http://example.com/api/v[0-9]").find());
    assertFalse(globToRegex(playwright, "**/api/v[0-9]").matcher("http://example.com/api/version").find());

    // query params
    assertTrue(globToRegex(playwright, "**/api\\?param").matcher("http://example.com/api?param").find());
    assertFalse(globToRegex(playwright, "**/api\\?param").matcher("http://example.com/api-param").find());
    assertTrue(globToRegex(playwright, "**/three-columns/settings.html\\?**id=settings-**").matcher("http://mydomain:8080/blah/blah/three-columns/settings.html?id=settings-e3c58efe-02e9-44b0-97ac-dd138100cf7c&blah").find());

    assertEquals("^\\?$", globToRegex(playwright, "\\?").pattern());
    assertEquals("^\\\\$", globToRegex(playwright, "\\").pattern());
    assertEquals("^\\\\$", globToRegex(playwright, "\\\\").pattern());
    assertEquals("^\\[$", globToRegex(playwright, "\\[").pattern());
    assertEquals("^\\[a-z\\]$", globToRegex(playwright, "[a-z]").pattern());
    assertEquals("^\\$\\^\\+\\.\\*\\(\\)\\|\\?\\{\\}\\[\\]$", globToRegex(playwright, "$^+.\\*()|\\?\\{\\}\\[\\]").pattern());


    assertTrue(urlMatches(playwright, null, "http://playwright.dev/", "http://playwright.dev"));
    assertTrue(urlMatches(playwright, null, "http://playwright.dev/?a=b", "http://playwright.dev?a=b"));
    assertTrue(urlMatches(playwright, null, "http://playwright.dev/", "h*://playwright.dev"));
    assertTrue(urlMatches(playwright, null, "http://api.playwright.dev/?x=y", "http://*.playwright.dev?x=y"));
    assertTrue(urlMatches(playwright, null, "http://playwright.dev/foo/bar", "**/foo/**"));
    assertTrue(urlMatches(playwright, "http://playwright.dev", "http://playwright.dev/?x=y", "?x=y"));
    assertTrue(urlMatches(playwright, "http://playwright.dev/foo/", "http://playwright.dev/foo/bar?x=y", "./bar?x=y"));

    // /**/ should match /.
    assertTrue(urlMatches(playwright, null, "https://foo/bar.js", "https://foo/**/bar.js"));
    assertTrue(urlMatches(playwright, null, "https://foo/bar.js", "https://foo/**/**/bar.js"));

    // Case insensitive matching
    assertTrue(urlMatches(playwright, null, "https://playwright.dev/fooBAR", "HtTpS://pLaYwRiGhT.dEv/fooBAR"));
    assertTrue(urlMatches(playwright, "http://ignored", "https://playwright.dev/fooBAR", "HtTpS://pLaYwRiGhT.dEv/fooBAR"));
    // Path and search query are case-sensitive
    assertFalse(urlMatches(playwright, null, "https://playwright.dev/foobar", "https://playwright.dev/fooBAR"));
    assertFalse(urlMatches(playwright, null, "https://playwright.dev/foobar?a=b", "https://playwright.dev/foobar?A=B"));

    assertTrue(urlMatches(playwright, null, "https://localhost:3000/?a=b", "**/?a=b"));
    assertTrue(urlMatches(playwright, null, "https://localhost:3000/?a=b", "**?a=b"));
    assertTrue(urlMatches(playwright, null, "https://localhost:3000/?a=b", "**=b"));

    // This is not supported, we treat ? as a query separator.
    assertFalse(urlMatches(playwright, null, "http://localhost:8080/Simple/path.js", "http://localhost:8080/?imple/path.js"));
    assertFalse(urlMatches(playwright, null, "http://playwright.dev/", "http://playwright.?ev"));
    assertTrue(urlMatches(playwright, null, "http://playwright./?ev", "http://playwright.?ev"));
    assertFalse(urlMatches(playwright, null, "http://playwright.dev/foo", "http://playwright.dev/f??"));
    assertTrue(urlMatches(playwright, null, "http://playwright.dev/f??", "http://playwright.dev/f??"));
    assertTrue(urlMatches(playwright, null, "http://playwright.dev/?x=y", "http://playwright.dev\\\\?x=y"));
    assertTrue(urlMatches(playwright, null, "http://playwright.dev/?x=y", "http://playwright.dev/\\\\?x=y"));
    assertTrue(urlMatches(playwright, "http://playwright.dev/foo", "http://playwright.dev/foo?bar", "?bar"));
    assertTrue(urlMatches(playwright, "http://playwright.dev/foo", "http://playwright.dev/foo?bar", "\\\\?bar"));
    assertTrue(urlMatches(playwright, "http://first.host/", "http://second.host/foo", "**/foo"));
    assertTrue(urlMatches(playwright, "http://playwright.dev/", "http://localhost/", "*//localhost/"));

    String[] customPrefixes = {"about", "data", "chrome", "edge", "file"};
    for (String prefix : customPrefixes) {
      assertTrue(urlMatches(playwright, "http://playwright.dev/", prefix + ":blank", prefix + ":blank"));
      assertFalse(urlMatches(playwright, "http://playwright.dev/", prefix + ":blank", "http://playwright.dev/"));
      assertTrue(urlMatches(playwright, null, prefix + ":blank", prefix + ":blank"));
      assertTrue(urlMatches(playwright, null, prefix + ":blank", prefix + ":*"));
      assertFalse(urlMatches(playwright, null, "not" + prefix + ":blank", prefix + ":*"));
    }
  }

  Pattern globToRegex(Playwright playwright, String glob) {
    return globToRegex(playwright, glob, null, false);
  }

  Pattern globToRegex(Playwright playwright, String glob, String baseURL, boolean webSocketUrl) {
    return ((PlaywrightImpl) playwright).localUtils().globToRegex(glob, baseURL, webSocketUrl);
  }

  boolean urlMatches(Playwright playwright, String baseURL, String urlString, String match) {
    if (match == null) {
      return true;
    }

    String glob = (String) match;
    if (glob.isEmpty()) {
      return true;
    }

    return globToRegex(playwright, glob, baseURL, false).matcher(urlString).find();
  }
}
