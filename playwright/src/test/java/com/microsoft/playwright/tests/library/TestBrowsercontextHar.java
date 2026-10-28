/**
 * Copyright Microsoft Corporation. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.playwright.tests.library;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.JSHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.fixtures.ContextFactory;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestInfo;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.HarContentPolicy;
import com.microsoft.playwright.options.HarMode;
import com.microsoft.playwright.options.HarNotFound;
import org.junit.jupiter.api.DisplayName;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.microsoft.playwright.fixtures.Utils.asset;
import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static com.microsoft.playwright.fixtures.Utils.parseZip;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

/** Port of tests/library/browsercontext-har.spec.ts. */
public class TestBrowsercontextHar {
  @PlaywrightTest
  @DisplayName("HA-068 should context.routeFromHAR, matching the method and following redirects")
  void shouldContextRouteFromHARMatchingTheMethodAndFollowingRedirects(BrowserContext context) {
    Path path = asset("har-fulfill.har");
    context.routeFromHAR(path);
    Page page = context.newPage();
    page.navigate("http://no.playwright/");
    // HAR contains a redirect for the script that should be followed automatically.
    assertEquals("foo", page.evaluate("window.value"));
    // HAR contains a POST for the css file that should not be used.
    assertThat(page.locator("body")).hasCSS("background-color", "rgb(255, 0, 0)");
  }

  @PlaywrightTest
  @DisplayName("HA-069 should page.routeFromHAR, matching the method and following redirects")
  void shouldPageRouteFromHARMatchingTheMethodAndFollowingRedirects(BrowserContext context) {
    Path path = asset("har-fulfill.har");
    Page page = context.newPage();
    page.routeFromHAR(path);
    page.navigate("http://no.playwright/");
    // HAR contains a redirect for the script that should be followed automatically.
    assertEquals("foo", page.evaluate("window.value"));
    // HAR contains a POST for the css file that should not be used.
    assertThat(page.locator("body")).hasCSS("background-color", "rgb(255, 0, 0)");
  }

  @PlaywrightTest
  @DisplayName("HA-070 fallback:continue should continue when not found in har")
  void fallbackContinueShouldContinueWhenNotFoundInHar(BrowserContext context, TestServer server) {
    Path path = asset("har-fulfill.har");
    context.routeFromHAR(path, new BrowserContext.RouteFromHAROptions().setNotFound(HarNotFound.FALLBACK));
    Page page = context.newPage();
    page.navigate(server.PREFIX + "/one-style.html");
    assertThat(page.locator("body")).hasCSS("background-color", "rgb(255, 192, 203)");
  }

  @PlaywrightTest
  @DisplayName("HA-071 by default should abort requests not found in har")
  void byDefaultShouldAbortRequestsNotFoundInHar(BrowserContext context, TestServer server) {
    Path path = asset("har-fulfill.har");
    context.routeFromHAR(path);
    Page page = context.newPage();
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.navigate(server.EMPTY_PAGE));
    assertTrue(error instanceof PlaywrightException);
  }

  @PlaywrightTest
  @DisplayName("HA-072 fallback:continue should continue requests on bad har")
  void fallbackContinueShouldContinueRequestsOnBadHar(BrowserContext context, TestServer server, TestInfo testInfo) throws IOException {
    Path path = testInfo.outputPath("test.har");
    Files.write(path, "{\"log\":{}}".getBytes(StandardCharsets.UTF_8));
    context.routeFromHAR(path, new BrowserContext.RouteFromHAROptions().setNotFound(HarNotFound.FALLBACK));
    Page page = context.newPage();
    page.navigate(server.PREFIX + "/one-style.html");
    assertThat(page.locator("body")).hasCSS("background-color", "rgb(255, 192, 203)");
  }

  @PlaywrightTest
  @DisplayName("HA-073 should only handle requests matching url filter")
  void shouldOnlyHandleRequestsMatchingUrlFilter(BrowserContext context) {
    Path path = asset("har-fulfill.har");
    context.routeFromHAR(path, new BrowserContext.RouteFromHAROptions().setNotFound(HarNotFound.FALLBACK).setUrl("**/*.js"));
    Page page = context.newPage();
    context.route("http://no.playwright/", route -> {
      assertEquals("http://no.playwright/", route.request().url());
      route.fulfill(new Route.FulfillOptions()
        .setStatus(200)
        .setContentType("text/html")
        .setBody("<script src=\"./script.js\"></script><div>hello</div>"));
    });
    page.navigate("http://no.playwright/");
    // HAR contains a redirect for the script that should be followed automatically.
    assertEquals("foo", page.evaluate("window.value"));
    assertThat(page.locator("body")).hasCSS("background-color", "rgba(0, 0, 0, 0)");
  }

  @PlaywrightTest
  @DisplayName("HA-074 should only context.routeFromHAR requests matching url filter")
  void shouldOnlyContextRouteFromHARRequestsMatchingUrlFilter(BrowserContext context) {
    Path path = asset("har-fulfill.har");
    context.routeFromHAR(path, new BrowserContext.RouteFromHAROptions().setUrl("**/*.js"));
    Page page = context.newPage();
    context.route("http://no.playwright/", route -> {
      assertEquals("http://no.playwright/", route.request().url());
      route.fulfill(new Route.FulfillOptions()
        .setStatus(200)
        .setContentType("text/html")
        .setBody("<script src=\"./script.js\"></script><div>hello</div>"));
    });
    page.navigate("http://no.playwright/");
    // HAR contains a redirect for the script that should be followed automatically.
    assertEquals("foo", page.evaluate("window.value"));
    assertThat(page.locator("body")).hasCSS("background-color", "rgba(0, 0, 0, 0)");
  }

  @PlaywrightTest
  @DisplayName("HA-075 should only page.routeFromHAR requests matching url filter")
  void shouldOnlyPageRouteFromHARRequestsMatchingUrlFilter(BrowserContext context) {
    Path path = asset("har-fulfill.har");
    Page page = context.newPage();
    page.routeFromHAR(path, new Page.RouteFromHAROptions().setUrl("**/*.js"));
    context.route("http://no.playwright/", route -> {
      assertEquals("http://no.playwright/", route.request().url());
      route.fulfill(new Route.FulfillOptions()
        .setStatus(200)
        .setContentType("text/html")
        .setBody("<script src=\"./script.js\"></script><div>hello</div>"));
    });
    page.navigate("http://no.playwright/");
    // HAR contains a redirect for the script that should be followed automatically.
    assertEquals("foo", page.evaluate("window.value"));
    assertThat(page.locator("body")).hasCSS("background-color", "rgba(0, 0, 0, 0)");
  }

  @PlaywrightTest
  @DisplayName("HA-076 should apply overrides before routing from har")
  void shouldApplyOverridesBeforeRoutingFromHar(BrowserContext context) {
    Path path = asset("har-fulfill.har");
    context.routeFromHAR(path, new BrowserContext.RouteFromHAROptions().setUrl("**/*.js"));
    Page page = context.newPage();
    context.route("http://no.playwright/my-script.js", route -> {
      route.fallback(new Route.FallbackOptions().setUrl("http://no.playwright/script2.js"));
    });
    context.route("http://test.example/", route -> {
      route.fulfill(new Route.FulfillOptions()
        .setStatus(200)
        .setContentType("text/html")
        .setBody("<script src=\"http://no.playwright/my-script.js\"></script><div>hello</div>"));
    });
    page.navigate("http://test.example/");
    // HAR contains script2.js that sets the value.
    assertEquals("foo", page.evaluate("window.value"));
  }

  @PlaywrightTest
  @DisplayName("HA-077 should support regex filter")
  void shouldSupportRegexFilter(BrowserContext context) {
    Path path = asset("har-fulfill.har");
    context.routeFromHAR(path, new BrowserContext.RouteFromHAROptions().setUrl(Pattern.compile(".*(\\.js|.*\\.css|no.playwright\\/)$")));
    Page page = context.newPage();
    page.navigate("http://no.playwright/");
    assertEquals("foo", page.evaluate("window.value"));
    assertThat(page.locator("body")).hasCSS("background-color", "rgb(255, 0, 0)");
  }

  @PlaywrightTest
  @DisplayName("HA-078 newPage should fulfill from har, matching the method and following redirects")
  void newPageShouldFulfillFromHarMatchingTheMethodAndFollowingRedirects(Browser browser) {
    Path path = asset("har-fulfill.har");
    Page page = browser.newPage();
    page.routeFromHAR(path);
    page.navigate("http://no.playwright/");
    // HAR contains a redirect for the script that should be followed automatically.
    assertEquals("foo", page.evaluate("window.value"));
    // HAR contains a POST for the css file that should not be used.
    assertThat(page.locator("body")).hasCSS("background-color", "rgb(255, 0, 0)");
    page.close();
  }

  @PlaywrightTest
  @DisplayName("HA-079 should change document URL after redirected navigation")
  void shouldChangeDocumentURLAfterRedirectedNavigation(BrowserContext context) {
    Path path = asset("har-redirect.har");
    context.routeFromHAR(path);
    Page page = context.newPage();
    // Java: page.waitForURL() runs after the navigation, which is the callback of waitForNavigation().
    Response response = page.waitForNavigation(() -> page.navigate("https://theverge.com/"));
    page.waitForURL("https://www.theverge.com/");
    assertThat(page).hasURL("https://www.theverge.com/");
    assertEquals("https://www.theverge.com/", response.request().url());
    assertEquals("https://www.theverge.com/", page.evaluate("() => location.href"));
  }

  @PlaywrightTest
  @DisplayName("HA-080 should change document URL after redirected navigation on click")
  void shouldChangeDocumentURLAfterRedirectedNavigationOnClick(TestServer server, BrowserContext context) {
    Path path = asset("har-redirect.har");
    context.routeFromHAR(path, new BrowserContext.RouteFromHAROptions().setUrl(Pattern.compile(".*theverge.*")));
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<a href=\"https://theverge.com/\">click me</a>");
    Response response = page.waitForNavigation(() -> page.click("text=click me"));
    assertThat(page).hasURL("https://www.theverge.com/");
    assertEquals("https://www.theverge.com/", response.request().url());
    assertEquals("https://www.theverge.com/", page.evaluate("() => location.href"));
  }

  @PlaywrightTest
  @DisplayName("HA-081 should goBack to redirected navigation")
  void shouldGoBackToRedirectedNavigation(BrowserContext context, TestServer server) {
    Path path = asset("har-redirect.har");
    context.routeFromHAR(path, new BrowserContext.RouteFromHAROptions().setUrl(Pattern.compile(".*theverge.*")));
    Page page = context.newPage();
    page.navigate("https://theverge.com/");
    page.navigate(server.EMPTY_PAGE);
    assertThat(page).hasURL(server.EMPTY_PAGE);
    Response response = page.goBack();
    assertThat(page).hasURL("https://www.theverge.com/");
    assertEquals("https://www.theverge.com/", response.request().url());
    assertEquals("https://www.theverge.com/", page.evaluate("() => location.href"));
  }

  @PlaywrightTest
  @DisplayName("HA-082 should goForward to redirected navigation")
  void shouldGoForwardToRedirectedNavigation(BrowserContext context, TestServer server) {
    Path path = asset("har-redirect.har");
    context.routeFromHAR(path, new BrowserContext.RouteFromHAROptions().setUrl(Pattern.compile(".*theverge.*")));
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    assertThat(page).hasURL(server.EMPTY_PAGE);
    page.navigate("https://theverge.com/");
    assertThat(page).hasURL("https://www.theverge.com/");
    page.goBack();
    assertThat(page).hasURL(server.EMPTY_PAGE);
    Response response = page.goForward();
    assertThat(page).hasURL("https://www.theverge.com/");
    assertEquals("https://www.theverge.com/", response.request().url());
    assertEquals("https://www.theverge.com/", page.evaluate("() => location.href"));
  }

  @PlaywrightTest
  @DisplayName("HA-083 should reload redirected navigation")
  void shouldReloadRedirectedNavigation(BrowserContext context, TestServer server) {
    Path path = asset("har-redirect.har");
    context.routeFromHAR(path, new BrowserContext.RouteFromHAROptions().setUrl(Pattern.compile(".*theverge.*")));
    Page page = context.newPage();
    page.navigate("https://theverge.com/");
    assertThat(page).hasURL("https://www.theverge.com/");
    Response response = page.reload();
    assertThat(page).hasURL("https://www.theverge.com/");
    assertEquals("https://www.theverge.com/", response.request().url());
    assertEquals("https://www.theverge.com/", page.evaluate("() => location.href"));
  }

  @PlaywrightTest
  @DisplayName("HA-084 should fulfill from har with content in a file")
  void shouldFulfillFromHarWithContentInAFile(BrowserContext context) {
    Path path = asset("har-sha1.har");
    context.routeFromHAR(path);
    Page page = context.newPage();
    page.navigate("http://no.playwright/");
    assertEquals("<html><head></head><body>Hello, world</body></html>", page.content());
  }

  @PlaywrightTest
  @DisplayName("HA-085 should round-trip har.zip")
  @Fixme("driver: a record HAR path with the upper case extension .ZIP is written as JSON instead of a zip; the fix for https://github.com/microsoft/playwright/issues/43503 is newer than the bundled driver 1.64.1")
  void shouldRoundTripHarZip(ContextFactory contextFactory, TestServer server, TestInfo testInfo) throws IOException {
    Path harPath = testInfo.outputPath("har.ZIP");
    BrowserContext context1 = contextFactory.newContext(new Browser.NewContextOptions().setRecordHarMode(HarMode.MINIMAL).setRecordHarPath(harPath));
    Page page1 = context1.newPage();
    page1.navigate(server.PREFIX + "/one-style.html");
    context1.close();

    Path harDir = testInfo.outputPath("har");
    extractZip(harPath, harDir);
    JsonObject log = JsonParser.parseString(new String(Files.readAllBytes(harDir.resolve("har.har")), StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonObject("log");
    assertTrue(log.getAsJsonArray("entries").get(0).getAsJsonObject().getAsJsonObject("response").getAsJsonObject("content").get("_file").getAsJsonPrimitive().isString());

    BrowserContext context2 = contextFactory.newContext();
    context2.routeFromHAR(harPath, new BrowserContext.RouteFromHAROptions().setNotFound(HarNotFound.ABORT));
    Page page2 = context2.newPage();
    page2.navigate(server.PREFIX + "/one-style.html");
    assertTrue(page2.content().contains("hello, world!"));
    assertThat(page2.locator("body")).hasCSS("background-color", "rgb(255, 192, 203)");
  }

  @PlaywrightTest
  @DisplayName("HA-086 should produce extracted zip")
  void shouldProduceExtractedZip(ContextFactory contextFactory, TestServer server, TestInfo testInfo) throws IOException {
    Path harPath = testInfo.outputPath("har.har");
    BrowserContext context1 = contextFactory.newContext(new Browser.NewContextOptions().setRecordHarMode(HarMode.MINIMAL).setRecordHarPath(harPath).setRecordHarContent(HarContentPolicy.ATTACH));
    Page page1 = context1.newPage();
    page1.navigate(server.PREFIX + "/one-style.html");
    context1.close();

    assertTrue(Files.exists(harPath));
    String har = new String(Files.readAllBytes(harPath), StandardCharsets.UTF_8);
    assertFalse(har.contains("background-color"));

    BrowserContext context2 = contextFactory.newContext();
    context2.routeFromHAR(harPath, new BrowserContext.RouteFromHAROptions().setNotFound(HarNotFound.ABORT));
    Page page2 = context2.newPage();
    page2.navigate(server.PREFIX + "/one-style.html");
    assertTrue(page2.content().contains("hello, world!"));
    assertThat(page2.locator("body")).hasCSS("background-color", "rgb(255, 192, 203)");
  }

  @PlaywrightTest
  @DisplayName("HA-087 should round-trip extracted har.zip")
  void shouldRoundTripExtractedHarZip(ContextFactory contextFactory, TestServer server, TestInfo testInfo) throws IOException {
    Path harPath = testInfo.outputPath("har.zip");
    BrowserContext context1 = contextFactory.newContext(new Browser.NewContextOptions().setRecordHarMode(HarMode.MINIMAL).setRecordHarPath(harPath));
    Page page1 = context1.newPage();
    page1.navigate(server.PREFIX + "/one-style.html");
    context1.close();

    Path harDir = testInfo.outputPath("hardir");
    extractZip(harPath, harDir);

    BrowserContext context2 = contextFactory.newContext();
    context2.routeFromHAR(harDir.resolve("har.har"));
    Page page2 = context2.newPage();
    page2.navigate(server.PREFIX + "/one-style.html");
    assertTrue(page2.content().contains("hello, world!"));
    assertThat(page2.locator("body")).hasCSS("background-color", "rgb(255, 192, 203)");
  }

  private static final String FETCH_FUNCTION = "async (body) => {\n" +
    "    const response = await fetch('/echo', { method: 'POST', body });\n" +
    "    return await response.text();\n" +
    "  }";

  @PlaywrightTest
  @DisplayName("HA-088 should round-trip har with postData")
  void shouldRoundTripHarWithPostData(ContextFactory contextFactory, TestServer server, TestInfo testInfo) {
    server.setRoute("/echo", (req, res) -> {
      String body = req.postBodyText();
      res.end(body);
    });

    Path harPath = testInfo.outputPath("har.zip");
    BrowserContext context1 = contextFactory.newContext(new Browser.NewContextOptions().setRecordHarMode(HarMode.MINIMAL).setRecordHarPath(harPath));
    Page page1 = context1.newPage();
    page1.navigate(server.EMPTY_PAGE);

    assertEquals("1", page1.evaluate(FETCH_FUNCTION, "1"));
    assertEquals("2", page1.evaluate(FETCH_FUNCTION, "2"));
    assertEquals("3", page1.evaluate(FETCH_FUNCTION, "3"));
    context1.close();

    server.reset();
    BrowserContext context2 = contextFactory.newContext();
    context2.routeFromHAR(harPath);
    Page page2 = context2.newPage();
    page2.navigate(server.EMPTY_PAGE);
    assertEquals("1", page2.evaluate(FETCH_FUNCTION, "1"));
    assertEquals("2", page2.evaluate(FETCH_FUNCTION, "2"));
    assertEquals("3", page2.evaluate(FETCH_FUNCTION, "3"));
    assertThrows(PlaywrightException.class, () -> page2.evaluate(FETCH_FUNCTION, "4"));
  }

  @PlaywrightTest
  @DisplayName("HA-089 should record overridden requests to har")
  void shouldRecordOverriddenRequestsToHar(ContextFactory contextFactory, TestServer server, TestInfo testInfo) {
    server.setRoute("/echo", (req, res) -> {
      String body = req.postBodyText();
      res.end(body);
    });

    Path harPath = testInfo.outputPath("har.zip");
    BrowserContext context1 = contextFactory.newContext(new Browser.NewContextOptions().setRecordHarMode(HarMode.MINIMAL).setRecordHarPath(harPath));
    Page page1 = context1.newPage();
    page1.navigate(server.EMPTY_PAGE);
    String fetchFunction = "async (arg) => {\n" +
      "    const response = await fetch(arg.path, { method: 'POST', body: arg.body });\n" +
      "    return await response.text();\n" +
      "  }";
    page1.route("**/echo_redir", route -> {
      route.fallback(new Route.FallbackOptions()
        .setUrl(server.PREFIX + "/echo")
        // Java: +route.request().postData() + 10 is a number in JavaScript, here its text.
        .setPostData(String.valueOf(Integer.parseInt(route.request().postData()) + 10)));
    });
    assertEquals("11", page1.evaluate(fetchFunction, mapOf("path", "/echo_redir", "body", "1")));
    assertEquals("12", page1.evaluate(fetchFunction, mapOf("path", "/echo_redir", "body", "2")));
    context1.close();

    server.reset();
    BrowserContext context2 = contextFactory.newContext();
    context2.routeFromHAR(harPath);
    Page page2 = context2.newPage();
    page2.navigate(server.EMPTY_PAGE);
    assertEquals("11", page2.evaluate(fetchFunction, mapOf("path", "/echo", "body", "11")));
    assertEquals("12", page2.evaluate(fetchFunction, mapOf("path", "/echo", "body", "12")));
  }

  @PlaywrightTest
  @DisplayName("HA-090 should disambiguate by header")
  void shouldDisambiguateByHeader(ContextFactory contextFactory, TestServer server, TestInfo testInfo) {
    server.setRoute("/echo", (req, res) -> {
      res.end(req.headers.get("baz"));
    });

    Path harPath = testInfo.outputPath("har.zip");
    BrowserContext context1 = contextFactory.newContext(new Browser.NewContextOptions().setRecordHarMode(HarMode.MINIMAL).setRecordHarPath(harPath));
    Page page1 = context1.newPage();
    page1.navigate(server.EMPTY_PAGE);

    String fetchFunction = "async (bazValue) => {\n" +
      "    const response = await fetch('/echo', {\n" +
      "      method: 'POST',\n" +
      "      body: '',\n" +
      "      headers: {\n" +
      "        foo: 'foo-value',\n" +
      "        bar: 'bar-value',\n" +
      "        baz: bazValue,\n" +
      "      }\n" +
      "    });\n" +
      "    return await response.text();\n" +
      "  }";

    assertEquals("baz1", page1.evaluate(fetchFunction, "baz1"));
    assertEquals("baz2", page1.evaluate(fetchFunction, "baz2"));
    assertEquals("baz3", page1.evaluate(fetchFunction, "baz3"));
    context1.close();

    BrowserContext context2 = contextFactory.newContext();
    context2.routeFromHAR(harPath);
    Page page2 = context2.newPage();
    page2.navigate(server.EMPTY_PAGE);
    assertEquals("baz1", page2.evaluate(fetchFunction, "baz1"));
    assertEquals("baz2", page2.evaluate(fetchFunction, "baz2"));
    assertEquals("baz3", page2.evaluate(fetchFunction, "baz3"));
    assertEquals("baz1", page2.evaluate(fetchFunction, "baz4"));
  }

  @PlaywrightTest
  @DisplayName("HA-091 should update har.zip for context")
  void shouldUpdateHarZipForContext(ContextFactory contextFactory, TestServer server, TestInfo testInfo) {
    Path harPath = testInfo.outputPath("har.zip");
    BrowserContext context1 = contextFactory.newContext();
    context1.routeFromHAR(harPath, new BrowserContext.RouteFromHAROptions().setUpdate(true));
    Page page1 = context1.newPage();
    page1.navigate(server.PREFIX + "/one-style.html");
    context1.close();

    BrowserContext context2 = contextFactory.newContext();
    context2.routeFromHAR(harPath, new BrowserContext.RouteFromHAROptions().setNotFound(HarNotFound.ABORT));
    Page page2 = context2.newPage();
    page2.navigate(server.PREFIX + "/one-style.html");
    assertTrue(page2.content().contains("hello, world!"));
    assertThat(page2.locator("body")).hasCSS("background-color", "rgb(255, 192, 203)");
  }

  @PlaywrightTest
  @DisplayName("HA-092 should ignore boundary when matching multipart/form-data body")
  void shouldIgnoreBoundaryWhenMatchingMultipartFormDataBody(ContextFactory contextFactory, TestServer server, TestInfo testInfo) throws Exception {
    server.setRoute("/empty.html", (req, res) -> {
      res.setHeader("Content-Type", "text/html");
      res.end("\n" +
        "      <form id=\"form\" action=\"form.html\" enctype=\"multipart/form-data\" method=\"POST\">\n" +
        "      <input id=\"file\" type=\"file\" multiple />\n" +
        "      <button type=\"submit\">Upload</button>\n" +
        "      </form>");
    });
    server.setRoute("/form.html", (req, res) -> {
      res.setHeader("Content-Type", "text/html");
      res.end("<div>done</div>");
    });

    Path harPath = testInfo.outputPath("har.zip");
    BrowserContext context1 = contextFactory.newContext();
    context1.routeFromHAR(harPath, new BrowserContext.RouteFromHAROptions().setUpdate(true));
    Page page1 = context1.newPage();
    page1.navigate(server.PREFIX + "/empty.html");
    CompletableFuture<TestServer.Request> reqPromise = server.waitForRequest("/form.html");
    page1.locator("button").click();
    assertThat(page1.locator("div")).hasText("done");
    TestServer.Request req = reqPromise.get();
    assertTrue(req.postBodyText().contains("---"));
    context1.close();

    BrowserContext context2 = contextFactory.newContext();
    context2.routeFromHAR(harPath, new BrowserContext.RouteFromHAROptions().setNotFound(HarNotFound.ABORT));
    Page page2 = context2.newPage();
    page2.navigate(server.PREFIX + "/empty.html");
    Request request = page2.waitForRequest(Pattern.compile(".*form.html"), () -> page2.locator("button").click());
    // expect.soft
    assertNotNull(request.response());
    assertNull(request.failure());
    assertThat(page2.locator("div")).hasText("done");
  }

  @PlaywrightTest
  @DisplayName("HA-093 should record single set-cookie headers")
  void shouldRecordSingleSetCookieHeaders(ContextFactory contextFactory, TestServer server, TestInfo testInfo) {
    server.setRoute("/empty.html", (req, res) -> {
      res.setHeader("Content-Type", "text/html");
      res.setHeader("set-cookie", Arrays.asList("first=foo"));
      res.end();
    });

    Path harPath = testInfo.outputPath("har.zip");
    BrowserContext context1 = contextFactory.newContext();
    context1.routeFromHAR(harPath, new BrowserContext.RouteFromHAROptions().setUpdate(true));
    Page page1 = context1.newPage();
    page1.navigate(server.EMPTY_PAGE);
    Object cookie1 = page1.evaluate("() => document.cookie");
    assertEquals("first=foo", cookie1);
    context1.close();

    BrowserContext context2 = contextFactory.newContext();
    context2.routeFromHAR(harPath, new BrowserContext.RouteFromHAROptions().setNotFound(HarNotFound.ABORT));
    Page page2 = context2.newPage();
    page2.navigate(server.EMPTY_PAGE);
    Object cookie2 = page2.evaluate("() => document.cookie");
    assertEquals("first=foo", cookie2);
  }

  @PlaywrightTest
  @DisplayName("HA-094 should record multiple set-cookie headers")
  void shouldRecordMultipleSetCookieHeaders(ContextFactory contextFactory, TestServer server, TestInfo testInfo) {
    server.setRoute("/empty.html", (req, res) -> {
      res.setHeader("Content-Type", "text/html");
      res.setHeader("set-cookie", Arrays.asList("first=foo", "second=bar"));
      res.end();
    });

    Path harPath = testInfo.outputPath("har.zip");
    BrowserContext context1 = contextFactory.newContext();
    context1.routeFromHAR(harPath, new BrowserContext.RouteFromHAROptions().setUpdate(true));
    Page page1 = context1.newPage();
    page1.navigate(server.EMPTY_PAGE);
    String cookie1 = (String) page1.evaluate("() => document.cookie");
    assertEquals("first=foo; second=bar", sortedCookies(cookie1));
    context1.close();

    BrowserContext context2 = contextFactory.newContext();
    context2.routeFromHAR(harPath, new BrowserContext.RouteFromHAROptions().setNotFound(HarNotFound.ABORT));
    Page page2 = context2.newPage();
    page2.navigate(server.EMPTY_PAGE);
    String cookie2 = (String) page2.evaluate("() => document.cookie");
    assertEquals("first=foo; second=bar", sortedCookies(cookie2));
  }

  @PlaywrightTest
  @DisplayName("HA-095 should update har.zip for page")
  void shouldUpdateHarZipForPage(ContextFactory contextFactory, TestServer server, TestInfo testInfo) {
    Path harPath = testInfo.outputPath("har.zip");
    BrowserContext context1 = contextFactory.newContext();
    Page page1 = context1.newPage();
    page1.routeFromHAR(harPath, new Page.RouteFromHAROptions().setUpdate(true));
    page1.navigate(server.PREFIX + "/one-style.html");
    context1.close();

    BrowserContext context2 = contextFactory.newContext();
    Page page2 = context2.newPage();
    page2.routeFromHAR(harPath, new Page.RouteFromHAROptions().setNotFound(HarNotFound.ABORT));
    page2.navigate(server.PREFIX + "/one-style.html");
    assertTrue(page2.content().contains("hello, world!"));
    assertThat(page2.locator("body")).hasCSS("background-color", "rgb(255, 192, 203)");
  }

  @PlaywrightTest
  @DisplayName("HA-096 should update har.zip for page with different options")
  void shouldUpdateHarZipForPageWithDifferentOptions(ContextFactory contextFactory, TestServer server, TestInfo testInfo) {
    Path harPath = testInfo.outputPath("har.zip");
    BrowserContext context1 = contextFactory.newContext();
    Page page1 = context1.newPage();
    page1.routeFromHAR(harPath, new Page.RouteFromHAROptions().setUpdate(true)
      .setUpdateContent(com.microsoft.playwright.options.RouteFromHarUpdateContentPolicy.EMBED)
      .setUpdateMode(HarMode.FULL));
    page1.navigate(server.PREFIX + "/one-style.html");
    context1.close();

    BrowserContext context2 = contextFactory.newContext();
    Page page2 = context2.newPage();
    page2.routeFromHAR(harPath, new Page.RouteFromHAROptions().setNotFound(HarNotFound.ABORT));
    page2.navigate(server.PREFIX + "/one-style.html");
    assertTrue(page2.content().contains("hello, world!"));
    assertThat(page2.locator("body")).hasCSS("background-color", "rgb(255, 192, 203)");
  }

  @PlaywrightTest
  @DisplayName("HA-097 should update extracted har.zip for page")
  void shouldUpdateExtractedHarZipForPage(ContextFactory contextFactory, TestServer server, TestInfo testInfo) {
    Path harPath = testInfo.outputPath("har.har");
    BrowserContext context1 = contextFactory.newContext();
    Page page1 = context1.newPage();
    page1.routeFromHAR(harPath, new Page.RouteFromHAROptions().setUpdate(true));
    page1.navigate(server.PREFIX + "/one-style.html");
    context1.close();

    BrowserContext context2 = contextFactory.newContext();
    Page page2 = context2.newPage();
    page2.routeFromHAR(harPath, new Page.RouteFromHAROptions().setNotFound(HarNotFound.ABORT));
    page2.navigate(server.PREFIX + "/one-style.html");
    assertTrue(page2.content().contains("hello, world!"));
    assertThat(page2.locator("body")).hasCSS("background-color", "rgb(255, 192, 203)");
  }

  @PlaywrightTest
  @DisplayName("HA-098 page.unrouteAll should stop page.routeFromHAR")
  void pageUnrouteAllShouldStopPageRouteFromHAR(ContextFactory contextFactory, TestServer server) {
    Path harPath = asset("har-fulfill.har");
    BrowserContext context1 = contextFactory.newContext();
    Page page1 = context1.newPage();
    // The har file contains requests for another domain, so the router
    // is expected to abort all requests.
    page1.routeFromHAR(harPath, new Page.RouteFromHAROptions().setNotFound(HarNotFound.ABORT));
    assertThrows(PlaywrightException.class, () -> page1.navigate(server.EMPTY_PAGE));
    // Java: unrouteAll() has no behavior option.
    page1.unrouteAll();
    Response response = page1.navigate(server.EMPTY_PAGE);
    assertTrue(response.ok());
  }

  @PlaywrightTest
  @DisplayName("HA-099 context.unrouteAll should stop context.routeFromHAR")
  void contextUnrouteAllShouldStopContextRouteFromHAR(ContextFactory contextFactory, TestServer server) {
    Path harPath = asset("har-fulfill.har");
    BrowserContext context1 = contextFactory.newContext();
    Page page1 = context1.newPage();
    // The har file contains requests for another domain, so the router
    // is expected to abort all requests.
    context1.routeFromHAR(harPath, new BrowserContext.RouteFromHAROptions().setNotFound(HarNotFound.ABORT));
    assertThrows(PlaywrightException.class, () -> page1.navigate(server.EMPTY_PAGE));
    // Java: unrouteAll() has no behavior option.
    context1.unrouteAll();
    Response response = page1.navigate(server.EMPTY_PAGE);
    assertTrue(response.ok());
  }

  @PlaywrightTest
  @DisplayName("HA-100 should ignore aborted requests")
  void shouldIgnoreAbortedRequests(ContextFactory contextFactory, TestServer server, TestInfo testInfo) throws Exception {
    Path path = testInfo.outputPath("test.har");
    {
      server.setRoute("/x", (req, res) -> {
        req.socket().destroy();
      });
      BrowserContext context1 = contextFactory.newContext();
      context1.routeFromHAR(path, new BrowserContext.RouteFromHAROptions().setUpdate(true));
      Page page1 = context1.newPage();
      page1.navigate(server.EMPTY_PAGE);
      CompletableFuture<TestServer.Request> reqPromise = server.waitForRequest("/x");
      // Java: the evaluate() blocks until the page settles the fetch, which is after the server got the request.
      Object req = page1.evaluate("url => fetch(url).catch(e => 'cancelled')", server.PREFIX + "/x");
      reqPromise.get();
      assertEquals("cancelled", req);
      context1.close();
    }
    server.reset();
    {
      server.setRoute("/x", (req, res) -> {
        res.writeHead(200, "Content-Type", "text/plain");
        res.end("test");
      });
      BrowserContext context2 = contextFactory.newContext();
      context2.routeFromHAR(path);
      Page page2 = context2.newPage();
      page2.navigate(server.EMPTY_PAGE);
      // Java: the fetch is started in the page and its state is read after the timeout, like Promise.race() with the timeout.
      JSHandle evalPromise = page2.evaluateHandle("url => ({ promise: fetch(url).catch(e => 'cancelled') })", server.PREFIX + "/x");
      page2.waitForTimeout(1000);
      Object result = evalPromise.evaluate("holder => Promise.race([holder.promise, 'timeout'])");
      assertEquals("timeout", result);
    }
  }

  /** The extractZip() of the upstream utils: writes the entries of the zip into the directory. */
  private static void extractZip(Path zip, Path dir) throws IOException {
    for (Map.Entry<String, byte[]> entry : parseZip(zip).entrySet()) {
      Path file = dir.resolve(entry.getKey());
      Files.createDirectories(file.getParent());
      Files.write(file, entry.getValue());
    }
  }

  private static String sortedCookies(String cookie) {
    List<String> parts = Arrays.stream(cookie.split("; ")).sorted().collect(Collectors.toList());
    return String.join("; ", parts);
  }
}
