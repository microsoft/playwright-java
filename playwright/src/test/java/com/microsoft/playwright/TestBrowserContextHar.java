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

import com.microsoft.playwright.options.HarContentPolicy;
import com.microsoft.playwright.options.HarMode;
import com.microsoft.playwright.options.HarNotFound;
import com.microsoft.playwright.options.RouteFromHarUpdateContentPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;

import static com.microsoft.playwright.Utils.copy;
import static com.microsoft.playwright.Utils.extractZip;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.microsoft.playwright.options.HarContentPolicy.ATTACH;
import static com.microsoft.playwright.options.HarContentPolicy.EMBED;
import static org.junit.jupiter.api.Assertions.*;

public class TestBrowserContextHar extends TestBase {
  @Test
  void shouldContextRouteFromHARMatchingTheMethodAndFollowingRedirects() {
    Path path = Paths.get("src/test/resources/har-fulfill.har");
    context.routeFromHAR(path);
    Page page = context.newPage();
    page.navigate("http://no.playwright/");
    // HAR contains a redirect for the script that should be followed automatically.
    assertEquals("foo", page.evaluate("window.value"));
    // HAR contains a POST for the css file that should not be used.
    assertThat(page.locator("body")).hasCSS("background-color", "rgb(255, 0, 0)");
  }

  @Test
  void shouldPageRouteFromHARMatchingTheMethodAndFollowingRedirects() {
    Path path = Paths.get("src/test/resources/har-fulfill.har");
    Page page = context.newPage();
    page.routeFromHAR(path);
    page.navigate("http://no.playwright/");
    // HAR contains a redirect for the script that should be followed automatically.
    assertEquals("foo", page.evaluate("window.value"));
    // HAR contains a POST for the css file that should not be used.
    assertThat(page.locator("body")).hasCSS("background-color", "rgb(255, 0, 0)");
  }

  @Test
  void fallbackContinueShouldContinueWhenNotFoundInHar() {
    Path path = Paths.get("src/test/resources/har-fulfill.har");
    context.routeFromHAR(path, new BrowserContext.RouteFromHAROptions().setNotFound(HarNotFound.FALLBACK));
    Page page = context.newPage();
    page.navigate(server.PREFIX + "/one-style.html");
    assertThat(page.locator("body")).hasCSS("background-color", "rgb(255, 192, 203)");
  }

  @Test
  void byDefaultShouldAbortRequestsNotFoundInHar() {
    Path path = Paths.get("src/test/resources/har-fulfill.har");
    context.routeFromHAR(path);
    Page page = context.newPage();
    assertThrows(PlaywrightException.class, () -> page.navigate(server.EMPTY_PAGE));
  }

  @Test
  void fallbackContinueShouldContinueRequestsOnBadHar(@TempDir Path tmpDir) throws IOException {
    Path path = tmpDir.resolve("test.har");
    try (Writer stream = new OutputStreamWriter(Files.newOutputStream(path))) {
      stream.write("{ \"log\" : {} }");
    }
    context.routeFromHAR(path, new BrowserContext.RouteFromHAROptions().setNotFound(HarNotFound.FALLBACK));
    Page page = context.newPage();
    page.navigate(server.PREFIX + "/one-style.html");
    assertThat(page.locator("body")).hasCSS("background-color", "rgb(255, 192, 203)");
  }

  @Test
  void shouldOnlyHandleRequestsMatchingUrlFilter() {
    Path path = Paths.get("src/test/resources/har-fulfill.har");
    context.routeFromHAR(path, new BrowserContext.RouteFromHAROptions().setNotFound(HarNotFound.FALLBACK).setUrl("**/*.js"));
    Page page = context.newPage();
    context.route("http://no.playwright/", route -> {
      assertEquals("http://no.playwright/", route.request().url());
      route.fulfill(new Route.FulfillOptions()
        .setStatus(200)
        .setContentType("text/html")
        .setBody("<script src='./script.js'></script><div>hello</div>"));
    });
    page.navigate("http://no.playwright/");
    // HAR contains a redirect for the script that should be followed automatically.
    assertEquals("foo", page.evaluate("window.value"));
    assertThat(page.locator("body")).hasCSS("background-color", "rgba(0, 0, 0, 0)");
  }

  @Test
  void shouldOnlyContextRouteFromHARRequestsMatchingUrlFilter() {
    Path path = Paths.get("src/test/resources/har-fulfill.har");
    context.routeFromHAR(path, new BrowserContext.RouteFromHAROptions().setUrl("**/*.js"));
    Page page = context.newPage();
    context.route("http://no.playwright/", route -> {
      assertEquals("http://no.playwright/", route.request().url());
      route.fulfill(new Route.FulfillOptions()
        .setStatus(200)
        .setContentType("text/html")
        .setBody("<script src='./script.js'></script><div>hello</div>"));
    });
    page.navigate("http://no.playwright/");
    // HAR contains a redirect for the script that should be followed automatically.
    assertEquals("foo", page.evaluate("window.value"));
    assertThat(page.locator("body")).hasCSS("background-color", "rgba(0, 0, 0, 0)");
  }

  @Test
  void shouldOnlyPageRouteFromHARRequestsMatchingUrlFilter() {
    Path path = Paths.get("src/test/resources/har-fulfill.har");
    Page page = context.newPage();
    page.routeFromHAR(path, new Page.RouteFromHAROptions().setUrl("**/*.js"));
    context.route("http://no.playwright/", route -> {
      assertEquals("http://no.playwright/", route.request().url());
      route.fulfill(new Route.FulfillOptions()
        .setStatus(200)
        .setContentType("text/html")
        .setBody("<script src='./script.js'></script><div>hello</div>"));
    });
    page.navigate("http://no.playwright/");
    // HAR contains a redirect for the script that should be followed automatically.
    assertEquals("foo", page.evaluate("window.value"));
    assertThat(page.locator("body")).hasCSS("background-color", "rgba(0, 0, 0, 0)");
  }

  @Test
  void shouldSupportRegexFilter() {
    Path path = Paths.get("src/test/resources/har-fulfill.har");
    context.routeFromHAR(path, new BrowserContext.RouteFromHAROptions().setUrl(Pattern.compile(".*(\\.js|.*\\.css|no.playwright\\/)$")));
    Page page = context.newPage();
    page.navigate("http://no.playwright/");
    assertEquals("foo", page.evaluate("window.value"));
    assertThat(page.locator("body")).hasCSS("background-color", "rgb(255, 0, 0)");
  }

  @Test
  void newPageShouldFulfillFromHarMatchingTheMethodAndFollowingRedirects() {
    Path path = Paths.get("src/test/resources/har-fulfill.har");
    Page page = browser.newPage();
    page.routeFromHAR(path);
    page.navigate("http://no.playwright/");
    // HAR contains a redirect for the script that should be followed automatically.
    assertEquals("foo", page.evaluate("window.value"));
    // HAR contains a POST for the css file that should not be used.
    assertThat(page.locator("body")).hasCSS("background-color", "rgb(255, 0, 0)");
    page.close();
  }

  @Test
  void shouldChangeDocumentURLAfterRedirectedNavigation() {
    Path path = Paths.get("src/test/resources/har-redirect.har");
    context.routeFromHAR(path);
    Page page = context.newPage();
    Response response = page.waitForNavigation(() -> {
      page.navigate("https://theverge.com/");
      page.waitForURL("https://www.theverge.com/");
    });
    assertThat(page).hasURL("https://www.theverge.com/");
    assertEquals("https://www.theverge.com/", response.request().url());
    assertEquals("https://www.theverge.com/", page.evaluate("location.href"));
  }

  @Test
  void shouldChangeDocumentURLAfterRedirectedNavigationOnClick() {
    Path path = Paths.get("src/test/resources/har-redirect.har");
    context.routeFromHAR(path, new BrowserContext.RouteFromHAROptions().setUrl(Pattern.compile(".*theverge.*")));
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<a href='https://theverge.com/'>click me</a>");
    Response response = page.waitForNavigation(() -> page.click("text=click me"));
    assertThat(page).hasURL("https://www.theverge.com/");
    assertEquals("https://www.theverge.com/", response.request().url());
    assertEquals("https://www.theverge.com/", page.evaluate("location.href"));
  }

  @Test
  void shouldGoBackToRedirectedNavigation() {
    Path path = Paths.get("src/test/resources/har-redirect.har");
    context.routeFromHAR(path, new BrowserContext.RouteFromHAROptions().setUrl(Pattern.compile(".*theverge.*")));
    Page page = context.newPage();
    page.navigate("https://theverge.com/");
    page.navigate(server.EMPTY_PAGE);
    assertThat(page).hasURL(server.EMPTY_PAGE);
    Response response = page.goBack();
    assertThat(page).hasURL("https://www.theverge.com/");
    assertEquals("https://www.theverge.com/", response.request().url());
    assertEquals("https://www.theverge.com/", page.evaluate("location.href"));
  }

  @Test
  @DisabledIf(value="isFirefox", disabledReason="Flaky in Firefox, upstream as well")
  void shouldGoForwardToRedirectedNavigation() {
    Path path = Paths.get("src/test/resources/har-redirect.har");
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

  @Test
  void shouldReloadRedirectedNavigation() {
    Path path = Paths.get("src/test/resources/har-redirect.har");
    context.routeFromHAR(path, new BrowserContext.RouteFromHAROptions().setUrl(Pattern.compile(".*theverge.*")));
    Page page = context.newPage();
    page.navigate("https://theverge.com/");
    assertThat(page).hasURL("https://www.theverge.com/");
    Response response = page.reload();
    assertThat(page).hasURL("https://www.theverge.com/");
    assertEquals("https://www.theverge.com/", response.request().url());
    assertEquals("https://www.theverge.com/", page.evaluate("() => location.href"));
  }

  @Test
  void shouldFulfillFromHarWithContentInAFile() {
    Path path = Paths.get("src/test/resources/har-sha1.har");
    context.routeFromHAR(path);
    Page page = context.newPage();
    page.navigate("http://no.playwright/");
    assertEquals("<html><head></head><body>Hello, world</body></html>", page.content());
  }

  @Test
  void shouldRoundTripHarZip(@TempDir Path tmpDir) {
    Path harPath = tmpDir.resolve("har.zip");
    try (BrowserContext context1 = browser.newContext(new Browser.NewContextOptions()
      .setRecordHarPath(harPath)
      .setRecordHarMode(HarMode.MINIMAL))) {
      Page page1 = context1.newPage();
      page1.navigate(server.PREFIX + "/one-style.html");
    }
    try (BrowserContext context2 = browser.newContext()) {
      context2.routeFromHAR(harPath, new BrowserContext.RouteFromHAROptions().setNotFound(HarNotFound.ABORT));
      Page page2 = context2.newPage();
      page2.navigate(server.PREFIX + "/one-style.html");
      assertTrue(page2.content().contains("hello, world!"));
      assertThat(page2.locator("body")).hasCSS("background-color", "rgb(255, 192, 203)");
    }
  }

  @Test
  void shouldProduceExtractedZip(@TempDir Path tmpDir) throws IOException {
    Path harPath = tmpDir.resolve("har.har");
    try (BrowserContext context1 = browser.newContext(new Browser.NewContextOptions()
      .setRecordHarPath(harPath)
      .setRecordHarMode(HarMode.MINIMAL)
      .setRecordHarContent(ATTACH))) {
      Page page1 = context1.newPage();
      page1.navigate(server.PREFIX + "/one-style.html");
    }
    assertTrue(Files.exists(harPath));
    String har = new String(Files.readAllBytes(harPath), StandardCharsets.UTF_8);
    assertFalse(har.contains("background-color"));
    try (BrowserContext context2 = browser.newContext()) {
      context2.routeFromHAR(harPath, new BrowserContext.RouteFromHAROptions().setNotFound(HarNotFound.ABORT));
      Page page2 = context2.newPage();
      page2.navigate(server.PREFIX + "/one-style.html");
      assertTrue(page2.content().contains("hello, world!"));
      assertThat(page2.locator("body")).hasCSS("background-color", "rgb(255, 192, 203)");
    }
  }

  @Test
  void shouldRoundTripExtractedHarZip(@TempDir Path tmpDir) throws IOException {
    Path harPath = tmpDir.resolve("har.zip");
    try (BrowserContext context1 = browser.newContext(new Browser.NewContextOptions()
      .setRecordHarPath(harPath)
      .setRecordHarMode(HarMode.MINIMAL))) {
      Page page1 = context1.newPage();
      page1.navigate(server.PREFIX + "/one-style.html");
    }

    Path harDir = tmpDir.resolve("hardir");
    extractZip(harPath, harDir);

    try (BrowserContext context2 = browser.newContext()) {
      context2.routeFromHAR(harDir.resolve("har.har"));
      Page page2 = context2.newPage();
      page2.navigate(server.PREFIX + "/one-style.html");
      assertTrue(page2.content().contains("hello, world!"));
      assertThat(page2.locator("body")).hasCSS("background-color", "rgb(255, 192, 203)");
    }
  }

  @Test
  void shouldRoundTripHarWithPostData(@TempDir Path tmpDir) {
    server.setRoute("/echo", exchange -> {
      exchange.sendResponseHeaders(200, 0);
      try (OutputStream out = exchange.getResponseBody()) {
        copy(exchange.getRequestBody(), out);
      }
    });

    String fetchFunction = "async body => {\n" +
      "    const response = await fetch('/echo', { method: 'POST', body });\n" +
      "    return await response.text();\n" +
      "  }\n";
    Path harPath = tmpDir.resolve("har.zip");
    try (BrowserContext context1 = browser.newContext(new Browser.NewContextOptions()
      .setRecordHarPath(harPath)
      .setRecordHarMode(HarMode.MINIMAL))) {
      Page page1 = context1.newPage();
      page1.navigate(server.EMPTY_PAGE);
      assertEquals("1", page1.evaluate(fetchFunction, "1"));
      assertEquals("2", page1.evaluate(fetchFunction, "2"));
      assertEquals("3", page1.evaluate(fetchFunction, "3"));
    }
    server.reset();
    try (BrowserContext context2 = browser.newContext()) {
      context2.routeFromHAR(harPath);
      Page page2 = context2.newPage();
      page2.navigate(server.EMPTY_PAGE);
      assertEquals("1", page2.evaluate(fetchFunction, "1"));
      assertEquals("2", page2.evaluate(fetchFunction, "2"));
      assertEquals("3", page2.evaluate(fetchFunction, "3"));
      assertEquals("3", page2.evaluate(fetchFunction, "3"));
      assertThrows(PlaywrightException.class, () -> page2.evaluate(fetchFunction, "4"));
    }
  }

  @Test
  void shouldDisambiguateByHeader(@TempDir Path tmpDir) {
    server.setRoute("/echo", exchange -> {
      exchange.sendResponseHeaders(200, 0);
      try (OutputStream out = exchange.getResponseBody()) {
        List<String> values = exchange.getRequestHeaders().get("baz");
        try (OutputStreamWriter writer = new OutputStreamWriter(exchange.getResponseBody())) {
          writer.write(values == null ? "<no header>" : String.join(", ", values));
        }
      }
    });

    String fetchFunction = "async bazValue => {\n" +
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
      "  }\n";
    Path harPath = tmpDir.resolve("har.zip");
    try (BrowserContext context1 = browser.newContext(new Browser.NewContextOptions()
      .setRecordHarPath(harPath)
      .setRecordHarMode(HarMode.MINIMAL))) {
      Page page1 = context1.newPage();
      page1.navigate(server.EMPTY_PAGE);
      assertEquals("baz1", page1.evaluate(fetchFunction, "baz1"));
      assertEquals("baz2", page1.evaluate(fetchFunction, "baz2"));
      assertEquals("baz3", page1.evaluate(fetchFunction, "baz3"));
    }
    server.reset();
    try (BrowserContext context2 = browser.newContext()) {
      context2.routeFromHAR(harPath);
      Page page2 = context2.newPage();
      page2.navigate(server.EMPTY_PAGE);
      assertEquals("baz1", page2.evaluate(fetchFunction, "baz1"));
      assertEquals("baz2", page2.evaluate(fetchFunction, "baz2"));
      assertEquals("baz3", page2.evaluate(fetchFunction, "baz3"));
      assertEquals("baz1", page2.evaluate(fetchFunction, "baz4"));
    }
  }

  @Test
  void shouldUpdateHarZipForContext(@TempDir Path tmpDir) {
    Path harPath = tmpDir.resolve("har.zip");
    try (BrowserContext context1 = browser.newContext()) {
      context1.routeFromHAR(harPath, new BrowserContext.RouteFromHAROptions().setUpdate(true));
      Page page1 = context1.newPage();
      page1.navigate(server.PREFIX + "/one-style.html");
    }
    try (BrowserContext context2 = browser.newContext()) {
      context2.routeFromHAR(harPath, new BrowserContext.RouteFromHAROptions().setNotFound(HarNotFound.ABORT));
      Page page2 = context2.newPage();
      page2.navigate(server.PREFIX + "/one-style.html");
      assertTrue(page2.content().contains("hello, world!"));
      assertThat(page2.locator("body")).hasCSS("background-color", "rgb(255, 192, 203)");
    }
  }

  @Test
  void shouldUpdateHarZipForPage(@TempDir Path tmpDir) {
    Path harPath = tmpDir.resolve("har.zip");
    try (BrowserContext context1 = browser.newContext()) {
      Page page1 = context1.newPage();
      page1.routeFromHAR(harPath, new Page.RouteFromHAROptions().setUpdate(true));
      page1.navigate(server.PREFIX + "/one-style.html");
    }
    try (BrowserContext context2 = browser.newContext()) {
      Page page2 = context2.newPage();
      page2.routeFromHAR(harPath, new Page.RouteFromHAROptions().setNotFound(HarNotFound.ABORT));
      page2.navigate(server.PREFIX + "/one-style.html");
      assertTrue(page2.content().contains("hello, world!"));
      assertThat(page2.locator("body")).hasCSS("background-color", "rgb(255, 192, 203)");
    }
  }

  @Test
  void shouldUpdateHarZipForPageWithDifferentOptions(@TempDir Path tmpDir) {
    Path harPath = tmpDir.resolve("har.zip");
    try (BrowserContext context1 = browser.newContext()) {
      Page page1 = context1.newPage();
      page1.routeFromHAR(harPath, new Page.RouteFromHAROptions()
        .setUpdate(true)
        .setUpdateContent(RouteFromHarUpdateContentPolicy.EMBED)
        .setUpdateMode(HarMode.FULL));
      page1.navigate(server.PREFIX + "/one-style.html");
    }

    try (BrowserContext context2 = browser.newContext()) {
      Page page2 = context2.newPage();
      page2.routeFromHAR(harPath, new Page.RouteFromHAROptions().setNotFound(HarNotFound.ABORT));
      page2.navigate(server.PREFIX + "/one-style.html");
      assertTrue(page2.content().contains("hello, world!"));
      assertThat(page2.locator("body")).hasCSS("background-color", "rgb(255, 192, 203)");
    }
  }

  @Test
  void shouldUpdateExtractedHarZipForPage(@TempDir Path tmpDir) {
    Path harPath = tmpDir.resolve("har.har");
    try (BrowserContext context1 = browser.newContext()) {
      Page page1 = context1.newPage();
      page1.routeFromHAR(harPath, new Page.RouteFromHAROptions().setUpdate(true));
      page1.navigate(server.PREFIX + "/one-style.html");
    }
    try (BrowserContext context2 = browser.newContext()) {
      Page page2 = context2.newPage();
      page2.routeFromHAR(harPath, new Page.RouteFromHAROptions().setNotFound(HarNotFound.ABORT));
      page2.navigate(server.PREFIX + "/one-style.html");
      assertTrue(page2.content().contains("hello, world!"));
      assertThat(page2.locator("body")).hasCSS("background-color", "rgb(255, 192, 203)");
    }
  }
}
