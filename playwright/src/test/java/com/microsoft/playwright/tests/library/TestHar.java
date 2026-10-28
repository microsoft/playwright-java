/**
 * Copyright 2018 Google Inc. All rights reserved.
 * Modifications copyright (c) Microsoft Corporation.
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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.Tracing;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.ContextFactory;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.EnabledOnBrowser;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.HttpsTestServer;
import com.microsoft.playwright.fixtures.LaunchPersistent;
import com.microsoft.playwright.fixtures.PageWithHar;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestInfo;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.impl.driver.Driver;
import com.microsoft.playwright.options.HarContentPolicy;
import com.microsoft.playwright.options.HarMode;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.SecurityDetails;
import com.microsoft.playwright.options.ServerAddr;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.microsoft.playwright.fixtures.Utils.asset;
import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/** Port of tests/library/har.spec.ts. */
public class TestHar {
  @PlaywrightTest
  @DisplayName("HA-001 should have version and creator")
  void shouldHaveVersionAndCreator(PageWithHar pageWithHar, TestServer server) throws Exception {
    PageWithHar.Result harSession = pageWithHar.create();
    harSession.page.navigate(server.EMPTY_PAGE);
    JsonObject log = harSession.getLog();
    assertEquals("1.2", log.get("version").getAsString());
    assertEquals("Playwright", log.getAsJsonObject("creator").get("name").getAsString());
    String version = System.getenv("PW_VERSION_OVERRIDE") != null ? System.getenv("PW_VERSION_OVERRIDE") : driverVersion();
    assertTrue(log.getAsJsonObject("creator").get("version").getAsString().contains(version));
  }

  @PlaywrightTest
  @DisplayName("HA-002 should have browser")
  void shouldHaveBrowser(BrowserName browserName, Browser browser, PageWithHar pageWithHar, TestServer server) {
    PageWithHar.Result harSession = pageWithHar.create();
    harSession.page.navigate(server.EMPTY_PAGE);
    JsonObject log = harSession.getLog();

    assertEquals(browserName.toString(), log.getAsJsonObject("browser").get("name").getAsString());
    assertEquals(browser.version(), log.getAsJsonObject("browser").get("version").getAsString());
  }

  @PlaywrightTest
  @DisplayName("HA-003 should have pages")
  void shouldHavePages(PageWithHar pageWithHar, TestServer server) {
    PageWithHar.Result harSession = pageWithHar.create();
    harSession.page.navigate("data:text/html,<title>Hello</title>");
    // For data: load comes before domcontentloaded...
    harSession.page.waitForLoadState(LoadState.DOMCONTENTLOADED);
    JsonObject log = harSession.getLog();
    assertEquals(1, log.getAsJsonArray("pages").size());
    JsonObject pageEntry = log.getAsJsonArray("pages").get(0).getAsJsonObject();
    assertTrue(truthy(pageEntry.get("id")));
    assertEquals("Hello", pageEntry.get("title").getAsString());
    assertTrue(dateValue(pageEntry.get("startedDateTime").getAsString()) > System.currentTimeMillis() - 3600 * 1000);
    assertTrue(pageEntry.getAsJsonObject("pageTimings").get("onContentLoad").getAsDouble() > 0);
    assertTrue(pageEntry.getAsJsonObject("pageTimings").get("onLoad").getAsDouble() > 0);
  }

  @PlaywrightTest
  @DisplayName("HA-004 should have pages in persistent context")
  void shouldHavePagesInPersistentContext(LaunchPersistent launchPersistent, BrowserName browserName, TestInfo testInfo) throws IOException {
    Path harPath = testInfo.outputPath("test.har");
    LaunchPersistent.Result persistent = launchPersistent.launch(Env.launchPersistentContextOptions().setRecordHarPath(harPath));
    BrowserContext context = persistent.context;
    Page page = persistent.page;
    page.navigate("data:text/html,<title>Hello</title>");
    // For data: load comes before domcontentloaded...
    page.waitForLoadState(LoadState.DOMCONTENTLOADED);
    context.close();
    JsonObject log = readLog(harPath);
    assertEquals(1, log.getAsJsonArray("pages").size());
    JsonObject pageEntry = log.getAsJsonArray("pages").get(0).getAsJsonObject();
    assertTrue(truthy(pageEntry.get("id")));
    assertEquals("Hello", pageEntry.get("title").getAsString());
  }

  @PlaywrightTest
  @DisplayName("HA-005 should include request")
  void shouldIncludeRequest(PageWithHar pageWithHar, TestServer server) {
    PageWithHar.Result harSession = pageWithHar.create();
    harSession.page.navigate(server.EMPTY_PAGE);
    JsonObject log = harSession.getLog();
    assertEquals(1, log.getAsJsonArray("entries").size());
    JsonObject entry = entry(log, 0);
    assertEquals(log.getAsJsonArray("pages").get(0).getAsJsonObject().get("id"), entry.get("pageref"));
    assertEquals(server.EMPTY_PAGE, entry.getAsJsonObject("request").get("url").getAsString());
    assertEquals("GET", entry.getAsJsonObject("request").get("method").getAsString());
    assertEquals("HTTP/1.1", entry.getAsJsonObject("request").get("httpVersion").getAsString());
    assertTrue(entry.getAsJsonObject("request").getAsJsonArray("headers").size() > 1);
    assertTrue(truthy(findHeader(entry.getAsJsonObject("request").getAsJsonArray("headers"), "user-agent")));
    assertEquals(0, entry.getAsJsonObject("request").get("bodySize").getAsInt());
  }

  @PlaywrightTest
  @DisplayName("HA-006 should populate entry startedDateTime from the browser")
  @Fixme("java: page.waitForResponse() matches the glob through a round trip to the driver before it registers its listener, so the response that arrived while the test thread was busy is dispatched before the listener exists; the call fails with \"Timeout 30000ms exceeded\"")
  void shouldPopulateEntryStartedDateTimeFromTheBrowser(PageWithHar pageWithHar, TestServer server) {
    PageWithHar.Result harSession = pageWithHar.create();
    harSession.page.navigate(server.EMPTY_PAGE);

    // The browser issues a request after a short delay, then we deliberately
    // block Node's event loop. The protocol event for the request therefore
    // queues up while Node is busy and is only processed by the harTracer after
    // the block ends. If `startedDateTime` is populated from Node's clock at
    // observation time it will land inside the busy-loop window (i.e. close to
    // `unblockedAt`); if it comes from the browser via the debugging protocol
    // it will be tied to when the browser actually sent the request.
    harSession.page.evaluate("() => {\n" +
      "      window.builtins.setTimeout(() => { void fetch('/delayed-fetch'); }, 50);\n" +
      "    }");

    long blockUntil = System.currentTimeMillis() + 300;
    while (System.currentTimeMillis() < blockUntil) {
      // Busy loop to prevent Node from processing protocol events.
    }
    long unblockedAt = System.currentTimeMillis();

    harSession.page.waitForResponse("**/delayed-fetch", () -> {});
    JsonObject log = harSession.getLog();

    JsonObject entry = findEntry(log, "/delayed-fetch");
    long startedAt = dateValue(entry.get("startedDateTime").getAsString());
    assertTrue(startedAt != Long.MIN_VALUE);
    // The recorded time should be tied to when the browser actually sent the
    // request (during the busy loop), not to when Node observed the protocol
    // event (after the busy loop).
    assertTrue(startedAt < unblockedAt - 100);
  }

  @PlaywrightTest
  @DisplayName("HA-007 should include response")
  void shouldIncludeResponse(PageWithHar pageWithHar, TestServer server) {
    PageWithHar.Result harSession = pageWithHar.create();
    harSession.page.navigate(server.EMPTY_PAGE);
    JsonObject log = harSession.getLog();
    JsonObject entry = entry(log, 0);
    assertEquals(200, entry.getAsJsonObject("response").get("status").getAsInt());
    assertEquals("OK", entry.getAsJsonObject("response").get("statusText").getAsString());
    assertEquals("HTTP/1.1", entry.getAsJsonObject("response").get("httpVersion").getAsString());
    assertTrue(entry.getAsJsonObject("response").getAsJsonArray("headers").size() > 1);
    assertTrue(findHeader(entry.getAsJsonObject("response").getAsJsonArray("headers"), "content-type").get("value").getAsString().contains("text/html"));
  }

  @PlaywrightTest
  @DisplayName("HA-008 should include redirectURL")
  void shouldIncludeRedirectURL(PageWithHar pageWithHar, TestServer server) {
    server.setRedirect("/foo.html", "/empty.html");
    PageWithHar.Result harSession = pageWithHar.create();
    harSession.page.navigate(server.PREFIX + "/foo.html");
    JsonObject log = harSession.getLog();
    assertEquals(2, log.getAsJsonArray("entries").size());
    JsonObject entry = entry(log, 0);
    assertEquals(302, entry.getAsJsonObject("response").get("status").getAsInt());
    assertEquals(server.EMPTY_PAGE, entry.getAsJsonObject("response").get("redirectURL").getAsString());
  }

  @PlaywrightTest
  @DisplayName("HA-009 should include query params")
  void shouldIncludeQueryParams(PageWithHar pageWithHar, TestServer server) {
    PageWithHar.Result harSession = pageWithHar.create();
    harSession.page.navigate(server.PREFIX + "/har.html?name=value");
    JsonObject log = harSession.getLog();
    assertEquals(json("[{\"name\":\"name\",\"value\":\"value\"}]"), entry(log, 0).getAsJsonObject("request").get("queryString"));
  }

  @PlaywrightTest
  @DisplayName("HA-010 should include postData")
  void shouldIncludePostData(PageWithHar pageWithHar, TestServer server) {
    PageWithHar.Result harSession = pageWithHar.create();
    harSession.page.navigate(server.EMPTY_PAGE);
    harSession.page.evaluate("() => fetch('./post', { method: 'POST', body: 'Hello' })");
    JsonObject log = harSession.getLog();
    assertEquals(json("{\"mimeType\":\"text/plain;charset=UTF-8\",\"params\":[],\"text\":\"Hello\"}"),
      entry(log, 1).getAsJsonObject("request").get("postData"));
  }

  @PlaywrightTest
  @DisplayName("HA-011 should include binary postData")
  void shouldIncludeBinaryPostData(PageWithHar pageWithHar, TestServer server) {
    PageWithHar.Result harSession = pageWithHar.create();
    harSession.page.navigate(server.EMPTY_PAGE);
    harSession.page.evaluate("async () => {\n" +
      "      await fetch('./post', { method: 'POST', body: new Uint8Array(Array.from(Array(16).keys())) });\n" +
      "    }");
    JsonObject log = harSession.getLog();
    assertEquals(json("{\"mimeType\":\"application/octet-stream\",\"params\":[],\"text\":\"\"}"),
      entry(log, 1).getAsJsonObject("request").get("postData"));
  }

  @PlaywrightTest
  @DisplayName("HA-012 should include form params")
  void shouldIncludeFormParams(PageWithHar pageWithHar, TestServer server) {
    PageWithHar.Result harSession = pageWithHar.create();
    harSession.page.navigate(server.EMPTY_PAGE);
    harSession.page.setContent("<form method='POST' action='/post'><input type='text' name='foo' value='bar'><input type='number' name='baz' value='123'><input type='submit'></form>");
    harSession.page.click("input[type=submit]");
    JsonObject log = harSession.getLog();
    assertEquals(json("{\"mimeType\":\"application/x-www-form-urlencoded\",\"params\":[{\"name\":\"foo\",\"value\":\"bar\"},{\"name\":\"baz\",\"value\":\"123\"}],\"text\":\"foo=bar&baz=123\"}"),
      entry(log, 1).getAsJsonObject("request").get("postData"));
  }

  @PlaywrightTest
  @DisplayName("HA-013 should include cookies")
  void shouldIncludeCookies(PageWithHar pageWithHar, TestServer server) {
    PageWithHar.Result harSession = pageWithHar.create();
    BrowserContext context = harSession.page.context();
    context.addCookies(Arrays.asList(
      new com.microsoft.playwright.options.Cookie("name1", "\"value1\"").setDomain("localhost").setPath("/").setHttpOnly(true),
      new com.microsoft.playwright.options.Cookie("name2", "val\"ue2").setDomain("localhost").setPath("/").setSameSite(com.microsoft.playwright.options.SameSiteAttribute.LAX),
      new com.microsoft.playwright.options.Cookie("name3", "val=ue3").setDomain("localhost").setPath("/"),
      new com.microsoft.playwright.options.Cookie("name4", "val,ue4").setDomain("localhost").setPath("/")));
    harSession.page.navigate(server.EMPTY_PAGE);
    JsonObject log = harSession.getLog();
    assertEquals(json("[{\"name\":\"name1\",\"value\":\"\\\"value1\\\"\"},{\"name\":\"name2\",\"value\":\"val\\\"ue2\"},{\"name\":\"name3\",\"value\":\"val=ue3\"},{\"name\":\"name4\",\"value\":\"val,ue4\"}]"),
      entry(log, 0).getAsJsonObject("request").get("cookies"));
  }

  @PlaywrightTest
  @DisplayName("HA-014 should include set-cookies")
  void shouldIncludeSetCookies(PageWithHar pageWithHar, TestServer server) {
    PageWithHar.Result harSession = pageWithHar.create();
    server.setRoute("/empty.html", (req, res) -> {
      res.setHeader("Set-Cookie", Arrays.asList(
        "name1=value1; HttpOnly",
        "name2=\"value2\"",
        "name3=value4; Path=/; Domain=example.com; Max-Age=1500"));
      res.end();
    });
    harSession.page.navigate(server.EMPTY_PAGE);
    JsonObject log = harSession.getLog();
    JsonArray cookies = entry(log, 0).getAsJsonObject("response").getAsJsonArray("cookies");
    assertEquals(json("{\"name\":\"name1\",\"value\":\"value1\",\"httpOnly\":true}"), cookies.get(0));
    assertEquals(json("{\"name\":\"name2\",\"value\":\"\\\"value2\\\"\"}"), cookies.get(1));
    assertTrue(dateValue(cookies.get(2).getAsJsonObject().get("expires").getAsString()) > System.currentTimeMillis());
  }

  @PlaywrightTest
  @DisplayName("HA-015 should include set-cookies without equals sign as nameless")
  @Fixme("driver: the HAR has {\"name\":\"token\",\"value\":\"\"} for the cookie \"token; HttpOnly\"; the fix for https://github.com/microsoft/playwright/issues/43155 is newer than the bundled driver 1.64.1")
  void shouldIncludeSetCookiesWithoutEqualsSignAsNameless(PageWithHar pageWithHar, TestServer server) {
    PageWithHar.Result harSession = pageWithHar.create();
    server.setRoute("/empty.html", (req, res) -> {
      res.setHeader("Set-Cookie", Arrays.asList("token; HttpOnly"));
      res.end();
    });
    harSession.page.navigate(server.EMPTY_PAGE);
    JsonObject log = harSession.getLog();
    assertEquals(json("[{\"name\":\"\",\"value\":\"token\",\"httpOnly\":true}]"), entry(log, 0).getAsJsonObject("response").get("cookies"));
  }

  @PlaywrightTest
  @DisplayName("HA-016 should include set-cookies with lowercase attributes")
  void shouldIncludeSetCookiesWithLowercaseAttributes(PageWithHar pageWithHar, TestServer server) {
    PageWithHar.Result harSession = pageWithHar.create();
    server.setRoute("/empty.html", (req, res) -> {
      res.setHeader("Set-Cookie", Arrays.asList("name=value; path=/; httponly; secure; samesite=Lax"));
      res.end();
    });
    harSession.page.navigate(server.EMPTY_PAGE);
    JsonObject log = harSession.getLog();
    JsonArray cookies = entry(log, 0).getAsJsonObject("response").getAsJsonArray("cookies");
    assertEquals(json("{\"name\":\"name\",\"value\":\"value\",\"path\":\"/\",\"httpOnly\":true,\"secure\":true,\"sameSite\":\"Lax\"}"), cookies.get(0));
  }

  @PlaywrightTest
  @DisplayName("HA-017 should skip invalid Expires")
  void shouldSkipInvalidExpires(PageWithHar pageWithHar, TestServer server) {
    PageWithHar.Result harSession = pageWithHar.create();
    server.setRoute("/empty.html", (req, res) -> {
      res.setHeader("Set-Cookie", Arrays.asList("name=value;Expires=Sat Sep 14 01:02:27 CET 2024"));
      res.end();
    });
    harSession.page.navigate(server.EMPTY_PAGE);
    JsonObject log = harSession.getLog();
    JsonArray cookies = entry(log, 0).getAsJsonObject("response").getAsJsonArray("cookies");
    assertEquals(json("{\"name\":\"name\",\"value\":\"value\"}"), cookies.get(0));
  }

  @PlaywrightTest
  @DisplayName("HA-018 should include set-cookies with comma")
  @DisabledOnBrowser(value = BrowserName.WEBKIT, reason = "fixme: We get \"name1=val, ue1, name2=val, ue2\" as a header value")
  void shouldIncludeSetCookiesWithComma(PageWithHar pageWithHar, TestServer server, BrowserName browserName) {
    PageWithHar.Result harSession = pageWithHar.create();
    server.setRoute("/empty.html", (req, res) -> {
      res.setHeader("Set-Cookie", Arrays.asList("name1=val, ue1", "name2=val, ue2"));
      res.end();
    });
    harSession.page.navigate(server.EMPTY_PAGE);
    JsonObject log = harSession.getLog();
    JsonArray cookies = entry(log, 0).getAsJsonObject("response").getAsJsonArray("cookies");
    assertEquals(json("{\"name\":\"name1\",\"value\":\"val, ue1\"}"), cookies.get(0));
    assertEquals(json("{\"name\":\"name2\",\"value\":\"val, ue2\"}"), cookies.get(1));
  }

  @PlaywrightTest
  @DisplayName("HA-019 should include secure set-cookies")
  void shouldIncludeSecureSetCookies(PageWithHar pageWithHar, HttpsTestServer httpsServer) {
    PageWithHar.Result harSession = pageWithHar.create();
    httpsServer.setRoute("/empty.html", (req, res) -> {
      res.setHeader("Set-Cookie", Arrays.asList("name1=value1; Secure"));
      res.end();
    });
    harSession.page.navigate(httpsServer.EMPTY_PAGE);
    JsonObject log = harSession.getLog();
    JsonArray cookies = entry(log, 0).getAsJsonObject("response").getAsJsonArray("cookies");
    assertEquals(json("{\"name\":\"name1\",\"value\":\"value1\",\"secure\":true}"), cookies.get(0));
  }

  @PlaywrightTest
  @DisplayName("HA-020 should record request overrides")
  void shouldRecordRequestOverrides(PageWithHar pageWithHar, TestServer server) {
    PageWithHar.Result harSession = pageWithHar.create();
    harSession.page.route("**/foo", route -> {
      Map<String, String> headers = new HashMap<>(route.request().headers());
      headers.put("content-type", "text/plain");
      headers.put("cookie", "foo=bar");
      headers.put("custom", "value");
      route.fallback(new Route.FallbackOptions()
        .setUrl(server.EMPTY_PAGE)
        .setMethod("POST")
        .setHeaders(headers)
        .setPostData("Hi!"));
    });

    harSession.page.navigate(server.PREFIX + "/foo");
    JsonObject log = harSession.getLog();
    JsonObject request = entry(log, 0).getAsJsonObject("request");
    assertEquals(server.EMPTY_PAGE, request.get("url").getAsString());
    assertEquals("POST", request.get("method").getAsString());
    assertTrue(request.getAsJsonArray("headers").contains(json("{\"name\":\"custom\",\"value\":\"value\"}")));
    assertEquals(json("[]"), request.get("cookies"));
    assertEquals(json("{\"mimeType\":\"text/plain\",\"params\":[],\"text\":\"Hi!\"}"), request.get("postData"));
  }

  @PlaywrightTest
  @DisplayName("HA-021 should include content @smoke")
  void shouldIncludeContent(PageWithHar pageWithHar, TestServer server) {
    PageWithHar.Result harSession = pageWithHar.create();
    harSession.page.navigate(server.PREFIX + "/har.html");
    harSession.page.evaluate("() => fetch('/pptr.png').then(r => r.arrayBuffer())");
    JsonObject log = harSession.getLog();

    assertNull(content(log, 0).get("encoding"));
    assertEquals("text/html; charset=utf-8", content(log, 0).get("mimeType").getAsString());
    assertTrue(content(log, 0).get("text").getAsString().contains("HAR Page"));
    assertTrue(content(log, 0).get("size").getAsDouble() >= 96);
    assertEquals(0, content(log, 0).get("compression").getAsInt());

    assertNull(content(log, 1).get("encoding"));
    assertEquals("text/css; charset=utf-8", content(log, 1).get("mimeType").getAsString());
    assertTrue(content(log, 1).get("text").getAsString().contains("pink"));
    assertTrue(content(log, 1).get("size").getAsDouble() >= 37);
    assertEquals(0, content(log, 1).get("compression").getAsInt());

    assertEquals("base64", content(log, 2).get("encoding").getAsString());
    assertEquals("image/png", content(log, 2).get("mimeType").getAsString());
    assertTrue(Base64.getDecoder().decode(content(log, 2).get("text").getAsString()).length > 0);
    assertTrue(content(log, 2).get("size").getAsDouble() >= 6000);
    assertEquals(0, content(log, 2).get("compression").getAsInt());
  }

  @PlaywrightTest
  @DisplayName("HA-113 should store binary content with a textual mime type as base64")
  @Fixme("driver: response.content has no \"encoding\": \"base64\" for a binary body with a textual mime type; the fix for https://github.com/microsoft/playwright/issues/43493 is newer than the bundled driver 1.64.1")
  @DisabledOnBrowser(value = BrowserName.WEBKIT, reason = "fixme: WebKit decodes the body of a textual resource before it gets to us")
  void shouldStoreBinaryContentWithATextualMimeTypeAsBase64(PageWithHar pageWithHar, TestServer server, BrowserName browserName) {
    byte[] body = new byte[]{(byte) 0x80, (byte) 0xc3, 0x28, (byte) 0xff, 0x41};
    server.setRoute("/binary.js", (req, res) -> {
      res.setHeader("Content-Type", "application/javascript");
      res.end(body);
    });
    PageWithHar.Result harSession = pageWithHar.create();
    harSession.page.navigate(server.EMPTY_PAGE);
    harSession.page.evaluate("() => fetch('/binary.js').then(r => r.arrayBuffer())");
    JsonObject log = harSession.getLog();
    JsonObject content = findEntry(log, "/binary.js").getAsJsonObject("response").getAsJsonObject("content");
    assertEquals("base64", content.get("encoding").getAsString());
    assertArrayEquals(body, Base64.getDecoder().decode(content.get("text").getAsString()));
  }

  @PlaywrightTest
  @DisplayName("HA-022 should use attach mode for zip extension")
  void shouldUseAttachModeForZipExtension(PageWithHar pageWithHar, TestServer server) {
    PageWithHar.Result harSession = pageWithHar.create(new PageWithHar.Options().setOutputPath("test.har.zip"));
    harSession.page.navigate(server.PREFIX + "/har.html");
    harSession.page.evaluate("() => fetch('/pptr.png').then(r => r.arrayBuffer())");
    Map<String, byte[]> zip = harSession.getZip();
    JsonObject log = zipLog(zip);
    assertNull(content(log, 0).get("text"));
    assertTrue(new String(zip.get("75841480e2606c03389077304342fac2c58ccb1b.html"), StandardCharsets.UTF_8).contains("HAR Page"));
  }

  @PlaywrightTest
  @DisplayName("HA-023 should omit content")
  void shouldOmitContent(PageWithHar pageWithHar, TestServer server) {
    PageWithHar.Result harSession = pageWithHar.create(new PageWithHar.Options().setContent(HarContentPolicy.OMIT).setOutputPath("test.har"));
    harSession.page.navigate(server.PREFIX + "/har.html");
    harSession.page.evaluate("() => fetch('/pptr.png').then(r => r.arrayBuffer())");
    JsonObject log = harSession.getLog();
    assertNull(content(log, 0).get("text"));
    assertNull(content(log, 0).get("_file"));
  }

  @PlaywrightTest
  @DisplayName("HA-024 should omit content legacy")
  void shouldOmitContentLegacy(PageWithHar pageWithHar, TestServer server) {
    PageWithHar.Result harSession = pageWithHar.create(new PageWithHar.Options().setOmitContent(true).setOutputPath("test.har"));
    harSession.page.navigate(server.PREFIX + "/har.html");
    harSession.page.evaluate("() => fetch('/pptr.png').then(r => r.arrayBuffer())");
    JsonObject log = harSession.getLog();
    assertNull(content(log, 0).get("text"));
    assertNull(content(log, 0).get("_file"));
  }

  @PlaywrightTest
  @DisplayName("HA-025 should attach content")
  void shouldAttachContent(PageWithHar pageWithHar, TestServer server) {
    PageWithHar.Result harSession = pageWithHar.create(new PageWithHar.Options().setContent(HarContentPolicy.ATTACH).setOutputPath("test.har.zip"));
    harSession.page.navigate(server.PREFIX + "/har.html");
    harSession.page.evaluate("() => fetch('/pptr.png').then(r => r.arrayBuffer())");
    Map<String, byte[]> zip = harSession.getZip();
    JsonObject log = zipLog(zip);

    assertNull(content(log, 0).get("encoding"));
    assertEquals("text/html; charset=utf-8", content(log, 0).get("mimeType").getAsString());
    assertTrue(content(log, 0).get("_file").getAsString().contains("75841480e2606c03389077304342fac2c58ccb1b"));
    assertTrue(content(log, 0).get("size").getAsDouble() >= 96);
    assertEquals(0, content(log, 0).get("compression").getAsInt());

    assertNull(content(log, 1).get("encoding"));
    assertEquals("text/css; charset=utf-8", content(log, 1).get("mimeType").getAsString());
    assertTrue(content(log, 1).get("_file").getAsString().contains("79f739d7bc88e80f55b9891a22bf13a2b4e18adb"));
    assertTrue(content(log, 1).get("size").getAsDouble() >= 37);
    assertEquals(0, content(log, 1).get("compression").getAsInt());

    assertNull(content(log, 2).get("encoding"));
    assertEquals("image/png", content(log, 2).get("mimeType").getAsString());
    assertTrue(content(log, 2).get("_file").getAsString().contains("a4c3a18f0bb83f5d9fe7ce561e065c36205762fa"));
    assertTrue(content(log, 2).get("size").getAsDouble() >= 6000);
    assertEquals(0, content(log, 2).get("compression").getAsInt());

    assertTrue(new String(zip.get("75841480e2606c03389077304342fac2c58ccb1b.html"), StandardCharsets.UTF_8).contains("HAR Page"));
    assertTrue(new String(zip.get("79f739d7bc88e80f55b9891a22bf13a2b4e18adb.css"), StandardCharsets.UTF_8).contains("pink"));
    assertEquals(content(log, 2).get("size").getAsInt(), zip.get("a4c3a18f0bb83f5d9fe7ce561e065c36205762fa.png").length);
  }

  @PlaywrightTest
  @DisplayName("HA-026 should filter by glob")
  void shouldFilterByGlob(ContextFactory contextFactory, TestServer server, TestInfo testInfo) throws IOException {
    Path harPath = testInfo.outputPath("test.har");
    BrowserContext context = contextFactory.newContext(new Browser.NewContextOptions()
      .setBaseURL(server.PREFIX)
      .setRecordHarPath(harPath)
      .setRecordHarUrlFilter("/*.css")
      .setIgnoreHTTPSErrors(true));
    Page page = context.newPage();
    page.navigate("/har.html");
    context.close();
    JsonObject log = readLog(harPath);
    assertEquals(1, log.getAsJsonArray("entries").size());
    assertTrue(entry(log, 0).getAsJsonObject("request").get("url").getAsString().endsWith("one-style.css"));
  }

  @PlaywrightTest
  @DisplayName("HA-027 should filter by regexp")
  void shouldFilterByRegexp(ContextFactory contextFactory, TestServer server, TestInfo testInfo) throws IOException {
    Path harPath = testInfo.outputPath("test.har");
    BrowserContext context = contextFactory.newContext(new Browser.NewContextOptions()
      .setRecordHarPath(harPath)
      .setRecordHarUrlFilter(Pattern.compile("HAR.X?HTML", Pattern.CASE_INSENSITIVE))
      .setIgnoreHTTPSErrors(true));
    Page page = context.newPage();
    page.navigate(server.PREFIX + "/har.html");
    context.close();
    JsonObject log = readLog(harPath);
    assertEquals(1, log.getAsJsonArray("entries").size());
    assertTrue(entry(log, 0).getAsJsonObject("request").get("url").getAsString().endsWith("har.html"));
  }

  @PlaywrightTest
  @DisplayName("HA-028 should include sizes")
  void shouldIncludeSizes(PageWithHar pageWithHar, TestServer server) throws IOException {
    PageWithHar.Result harSession = pageWithHar.create();
    harSession.page.navigate(server.PREFIX + "/har.html");
    JsonObject log = harSession.getLog();
    assertEquals(2, log.getAsJsonArray("entries").size());
    assertTrue(entry(log, 0).getAsJsonObject("request").get("url").getAsString().endsWith("har.html"));
    assertTrue(entry(log, 0).getAsJsonObject("request").get("headersSize").getAsDouble() >= 100);
    assertEquals(Files.size(asset("har.html")), entry(log, 0).getAsJsonObject("response").get("bodySize").getAsLong());
    assertTrue(entry(log, 0).getAsJsonObject("response").get("headersSize").getAsDouble() >= 100);
    assertTrue(entry(log, 0).getAsJsonObject("response").get("_transferSize").getAsDouble() >= 250);

    assertTrue(entry(log, 1).getAsJsonObject("request").get("url").getAsString().endsWith("one-style.css"));
    assertEquals(Files.size(asset("one-style.css")), entry(log, 1).getAsJsonObject("response").get("bodySize").getAsLong());
    assertTrue(entry(log, 1).getAsJsonObject("response").get("headersSize").getAsDouble() >= 100);
    assertTrue(entry(log, 1).getAsJsonObject("response").get("_transferSize").getAsDouble() >= 150);
  }

  @PlaywrightTest
  @DisplayName("HA-029 should work with gzip compression")
  void shouldWorkWithGzipCompression(PageWithHar pageWithHar, TestServer server, BrowserName browserName) {
    PageWithHar.Result harSession = pageWithHar.create();
    server.enableGzip("/simplezip.json");
    Response response = harSession.page.navigate(server.PREFIX + "/simplezip.json");
    assertEquals("gzip", response.headers().get("content-encoding"));
    JsonObject log = harSession.getLog();
    assertEquals(1, log.getAsJsonArray("entries").size());
    assertTrue(content(log, 0).get("compression").getAsDouble() > 4000);
  }

  @PlaywrightTest
  @DisplayName("HA-030 should calculate time")
  void shouldCalculateTime(PageWithHar pageWithHar, TestServer server) {
    PageWithHar.Result harSession = pageWithHar.create();
    harSession.page.navigate(server.PREFIX + "/har.html");
    JsonObject log = harSession.getLog();
    assertTrue(entry(log, 0).get("time").getAsDouble() > 0);
  }

  @PlaywrightTest
  @DisplayName("HA-031 should return receive time")
  void shouldReturnReceiveTime(PageWithHar pageWithHar, TestServer server) {
    PageWithHar.Result harSession = pageWithHar.create();
    harSession.page.navigate(server.PREFIX + "/har.html");
    JsonObject log = harSession.getLog();
    assertTrue(entry(log, 0).getAsJsonObject("timings").get("receive").getAsDouble() > 0);
  }

  @PlaywrightTest
  @DisplayName("HA-032 should report the correct _transferSize with PNG files")
  void shouldReportTheCorrectTransferSizeWithPNGFiles(PageWithHar pageWithHar, TestServer server) throws IOException {
    PageWithHar.Result harSession = pageWithHar.create();
    harSession.page.navigate(server.EMPTY_PAGE);
    harSession.page.setContent("\n" +
      "    <img src=\"" + server.PREFIX + "/pptr.png\" />\n" +
      "  ");
    JsonObject log = harSession.getLog();
    assertTrue(entry(log, 1).getAsJsonObject("response").get("_transferSize").getAsDouble() > Files.size(asset("pptr.png")));
  }

  @PlaywrightTest
  @DisplayName("HA-033 should have -1 _transferSize when its a failed request")
  void shouldHaveMinus1TransferSizeWhenItsAFailedRequest(PageWithHar pageWithHar, TestServer server) {
    PageWithHar.Result harSession = pageWithHar.create();
    server.setRoute("/one-style.css", (req, res) -> {
      res.setHeader("Content-Type", "text/css");
      res.socket().destroy();
    });
    List<Request> failedRequests = new ArrayList<>();
    harSession.page.onRequestFailed(request -> failedRequests.add(request));
    harSession.page.navigate(server.PREFIX + "/har.html");
    JsonObject log = harSession.getLog();
    assertTrue(entry(log, 1).getAsJsonObject("request").get("url").getAsString().endsWith("/one-style.css"));
    assertEquals(-1, entry(log, 1).getAsJsonObject("response").get("_transferSize").getAsInt());
  }

  @PlaywrightTest
  @DisplayName("HA-034 should record failed request headers")
  void shouldRecordFailedRequestHeaders(PageWithHar pageWithHar, TestServer server) {
    PageWithHar.Result harSession = pageWithHar.create();
    server.setRoute("/har.html", (req, res) -> {
      res.socket().destroy();
    });
    try {
      harSession.page.navigate(server.PREFIX + "/har.html");
    } catch (PlaywrightException e) {
    }
    JsonObject log = harSession.getLog();
    assertTrue(truthy(entry(log, 0).getAsJsonObject("response").get("_failureText")));
    JsonObject request = entry(log, 0).getAsJsonObject("request");
    assertTrue(request.get("url").getAsString().endsWith("/har.html"));
    assertEquals("GET", request.get("method").getAsString());
    assertTrue(hasHeaderNamed(request.getAsJsonArray("headers"), "User-Agent"));
  }

  @PlaywrightTest
  @DisplayName("HA-035 should record failed request overrides")
  void shouldRecordFailedRequestOverrides(PageWithHar pageWithHar, TestServer server) {
    PageWithHar.Result harSession = pageWithHar.create();
    server.setRoute("/empty.html", (req, res) -> {
      res.socket().destroy();
    });
    harSession.page.route("**/foo", route -> {
      Map<String, String> headers = new HashMap<>(route.request().headers());
      headers.put("content-type", "text/plain");
      headers.put("cookie", "foo=bar");
      headers.put("custom", "value");
      route.fallback(new Route.FallbackOptions()
        .setUrl(server.EMPTY_PAGE)
        .setMethod("POST")
        .setHeaders(headers)
        .setPostData("Hi!"));
    });
    try {
      harSession.page.navigate(server.PREFIX + "/foo");
    } catch (PlaywrightException e) {
    }
    JsonObject log = harSession.getLog();
    assertTrue(truthy(entry(log, 0).getAsJsonObject("response").get("_failureText")));
    JsonObject request = entry(log, 0).getAsJsonObject("request");
    assertEquals(server.EMPTY_PAGE, request.get("url").getAsString());
    assertEquals("POST", request.get("method").getAsString());
    assertTrue(request.getAsJsonArray("headers").contains(json("{\"name\":\"custom\",\"value\":\"value\"}")));
    assertEquals(json("[]"), request.get("cookies"));
    assertEquals(json("{\"mimeType\":\"text/plain\",\"params\":[],\"text\":\"Hi!\"}"), request.get("postData"));
  }

  @PlaywrightTest
  @DisplayName("HA-036 should report the correct request body size")
  void shouldReportTheCorrectRequestBodySize(PageWithHar pageWithHar, TestServer server) {
    server.setRoute("/api", (req, res) -> res.end());
    PageWithHar.Result harSession = pageWithHar.create();
    harSession.page.navigate(server.EMPTY_PAGE);
    harSession.page.waitForResponse(server.PREFIX + "/api1", () -> harSession.page.evaluate("() => {\n" +
      "      void fetch('/api1', {\n" +
      "        method: 'POST',\n" +
      "        body: 'abc123'\n" +
      "      });\n" +
      "    }"));
    JsonObject log = harSession.getLog();
    assertEquals(6, entry(log, 1).getAsJsonObject("request").get("bodySize").getAsInt());
  }

  @PlaywrightTest
  @DisplayName("HA-037 should report the correct request body size when the bodySize is 0")
  void shouldReportTheCorrectRequestBodySizeWhenTheBodySizeIs0(PageWithHar pageWithHar, TestServer server) {
    server.setRoute("/api", (req, res) -> res.end());
    PageWithHar.Result harSession = pageWithHar.create();
    harSession.page.navigate(server.EMPTY_PAGE);
    harSession.page.waitForResponse(server.PREFIX + "/api2", () -> harSession.page.evaluate("() => {\n" +
      "      void fetch('/api2', {\n" +
      "        method: 'POST',\n" +
      "        body: ''\n" +
      "      });\n" +
      "    }"));
    JsonObject log = harSession.getLog();
    assertEquals(0, entry(log, 1).getAsJsonObject("request").get("bodySize").getAsInt());
  }

  @PlaywrightTest
  @DisplayName("HA-038 should report the correct response body size when the bodySize is 0")
  void shouldReportTheCorrectResponseBodySizeWhenTheBodySizeIs0(PageWithHar pageWithHar, TestServer server) {
    PageWithHar.Result harSession = pageWithHar.create();
    Response response = harSession.page.navigate(server.EMPTY_PAGE);
    response.finished();
    JsonObject log = harSession.getLog();
    assertEquals(0, entry(log, 0).getAsJsonObject("response").get("bodySize").getAsInt());
  }

  @PlaywrightTest
  @DisplayName("HA-039 should have popup requests")
  void shouldHavePopupRequests(PageWithHar pageWithHar, TestServer server) {
    PageWithHar.Result harSession = pageWithHar.create();
    harSession.page.navigate(server.EMPTY_PAGE);
    harSession.page.setContent("<a target=_blank rel=noopener href=\"/one-style.html\">yo</a>");
    Page popup = harSession.page.waitForPopup(() -> harSession.page.click("a"));
    popup.waitForLoadState();
    JsonObject log = harSession.getLog();

    assertEquals(2, log.getAsJsonArray("pages").size());

    List<JsonObject> entries = new ArrayList<>();
    for (JsonElement e : log.getAsJsonArray("entries")) {
      if (e.getAsJsonObject().get("pageref").equals(log.getAsJsonArray("pages").get(1).getAsJsonObject().get("id"))) {
        entries.add(e.getAsJsonObject());
      }
    }
    assertEquals(2, entries.size());
    assertEquals(server.PREFIX + "/one-style.html", entries.get(0).getAsJsonObject("request").get("url").getAsString());
    assertEquals(200, entries.get(0).getAsJsonObject("response").get("status").getAsInt());
    assertEquals(server.PREFIX + "/one-style.css", entries.get(1).getAsJsonObject("request").get("url").getAsString());
    assertEquals(200, entries.get(1).getAsJsonObject("response").get("status").getAsInt());
  }

  @PlaywrightTest
  @DisplayName("HA-040 should not contain internal pages")
  void shouldNotContainInternalPages(BrowserName browserName, PageWithHar pageWithHar, TestServer server) {
    server.setRoute("/empty.html", (req, res) -> {
      res.setHeader("Set-Cookie", "name=value");
      res.end();
    });

    PageWithHar.Result harSession = pageWithHar.create();
    harSession.page.navigate(server.EMPTY_PAGE);

    List<com.microsoft.playwright.options.Cookie> cookies = harSession.context.cookies();
    assertEquals(1, cookies.size());
    // Get storage state, this create internal page.
    harSession.context.storageState();

    JsonObject log = harSession.getLog();
    assertEquals(1, log.getAsJsonArray("pages").size());
  }

  @PlaywrightTest
  @DisplayName("HA-041 should have connection details")
  void shouldHaveConnectionDetails(PageWithHar pageWithHar, TestServer server, BrowserName browserName) {
    PageWithHar.Result harSession = pageWithHar.create();
    harSession.page.navigate(server.EMPTY_PAGE);
    JsonObject log = harSession.getLog();
    JsonObject entry = entry(log, 0);
    String serverIPAddress = entry.get("serverIPAddress").getAsString();
    int port = entry.get("_serverPort").getAsInt();
    JsonElement securityDetails = entry.get("_securityDetails");
    assertTrue(Pattern.compile("^127\\.0\\.0\\.1|\\[::1\\]").matcher(serverIPAddress).find());
    assertEquals(server.PORT, port);
    assertEquals(json("{}"), securityDetails);
  }

  @PlaywrightTest
  @DisplayName("HA-042 should have security details")
  void shouldHaveSecurityDetails(PageWithHar pageWithHar, HttpsTestServer httpsServer, BrowserName browserName, TestInfo testInfo) throws IOException {
    assumeFalse(browserName == BrowserName.WEBKIT && Env.platform().equals("win32") && !"webkit-wsl".equals(Env.channel()), "fail: ");

    PageWithHar.Result harSession = pageWithHar.create();
    Path apiHarPath = testInfo.outputPath("api.har");
    harSession.context.request().tracing().startHar(apiHarPath);
    harSession.page.navigate(httpsServer.EMPTY_PAGE);
    harSession.page.request().get(httpsServer.EMPTY_PAGE);
    harSession.context.request().tracing().stopHar();
    JsonObject log = harSession.getLog();
    assertEquals(1, log.getAsJsonArray("entries").size());
    JsonObject entry = entry(log, 0);
    String serverIPAddress = entry.get("serverIPAddress").getAsString();
    int port = entry.get("_serverPort").getAsInt();
    JsonElement securityDetails = entry.get("_securityDetails");
    assertTrue(Pattern.compile("^127\\.0\\.0\\.1|\\[::1\\]").matcher(serverIPAddress).find());
    assertEquals(httpsServer.PORT, port);
    if (browserName == BrowserName.WEBKIT) {
      assertEquals(json("{\"protocol\":\"TLS 1.3\",\"subjectName\":\"playwright-test\",\"validFrom\":1691708270,\"validTo\":2007068270}"), securityDetails);
    } else {
      assertEquals(json("{\"issuer\":\"playwright-test\",\"protocol\":\"TLS 1.3\",\"subjectName\":\"playwright-test\",\"validFrom\":1691708270,\"validTo\":2007068270}"), securityDetails);
    }

    JsonObject apiLog = readLog(apiHarPath);
    assertEquals(1, apiLog.getAsJsonArray("entries").size());
    assertEquals(json("{\"issuer\":\"playwright-test\",\"protocol\":\"TLSv1.3\",\"subjectName\":\"playwright-test\",\"validFrom\":1691708270,\"validTo\":2007068270}"),
      entry(apiLog, 0).get("_securityDetails"));
  }

  @PlaywrightTest
  @DisplayName("HA-043 should have connection details for redirects")
  void shouldHaveConnectionDetailsForRedirects(PageWithHar pageWithHar, TestServer server, BrowserName browserName) {
    server.setRedirect("/foo.html", "/empty.html");
    PageWithHar.Result harSession = pageWithHar.create();
    harSession.page.navigate(server.PREFIX + "/foo.html");
    JsonObject log = harSession.getLog();
    assertEquals(2, log.getAsJsonArray("entries").size());

    JsonObject detailsFoo = entry(log, 0);

    if (browserName == BrowserName.WEBKIT) {
      assertNull(detailsFoo.get("serverIPAddress"));
      assertNull(detailsFoo.get("_serverPort"));
    } else {
      assertTrue(Pattern.compile("^127\\.0\\.0\\.1|\\[::1\\]").matcher(detailsFoo.get("serverIPAddress").getAsString()).find());
      assertEquals(server.PORT, detailsFoo.get("_serverPort").getAsInt());
    }

    JsonObject detailsEmpty = entry(log, 1);
    assertTrue(Pattern.compile("^127\\.0\\.0\\.1|\\[::1\\]").matcher(detailsEmpty.get("serverIPAddress").getAsString()).find());
    assertEquals(server.PORT, detailsEmpty.get("_serverPort").getAsInt());
  }

  @PlaywrightTest
  @DisplayName("HA-044 should have connection details for failed requests")
  void shouldHaveConnectionDetailsForFailedRequests(PageWithHar pageWithHar, TestServer server, BrowserName browserName) {
    server.setRoute("/one-style.css", (req, res) -> {
      res.setHeader("Content-Type", "text/css");
      res.socket().destroy();
    });
    PageWithHar.Result harSession = pageWithHar.create();
    harSession.page.navigate(server.PREFIX + "/one-style.html");
    JsonObject log = harSession.getLog();
    JsonObject entry = entry(log, 0);
    String serverIPAddress = entry.get("serverIPAddress").getAsString();
    int port = entry.get("_serverPort").getAsInt();
    assertTrue(Pattern.compile("^127\\.0\\.0\\.1|\\[::1\\]").matcher(serverIPAddress).find());
    assertEquals(server.PORT, port);
  }

  @PlaywrightTest
  @DisplayName("HA-045 should return server address directly from response")
  void shouldReturnServerAddressDirectlyFromResponse(Page page, TestServer server) {
    Response response = page.navigate(server.EMPTY_PAGE);
    ServerAddr serverAddr = response.serverAddr();
    assertTrue(Pattern.compile("^127\\.0\\.0\\.1|\\[::1\\]").matcher(serverAddr.ipAddress).find());
    assertEquals(server.PORT, serverAddr.port);
  }

  @PlaywrightTest
  @DisplayName("HA-046 should return security details directly from response")
  void shouldReturnSecurityDetailsDirectlyFromResponse(ContextFactory contextFactory, HttpsTestServer httpsServer, BrowserName browserName) {
    BrowserContext context = contextFactory.newContext(new Browser.NewContextOptions().setIgnoreHTTPSErrors(true));
    Page page = context.newPage();
    Response response = page.navigate(httpsServer.EMPTY_PAGE);
    SecurityDetails securityDetails = response.securityDetails();
    if (browserName == BrowserName.WEBKIT && Env.platform().equals("win32")) {
      // WebKit on Windows does not surface the TLS protocol.
      Map<String, Object> actual = securityDetailsFields(securityDetails);
      actual.remove("protocol");
      assertEquals(mapOf("subjectName", "playwright-test", "validFrom", 1691708270.0, "validTo", 2007068270.0), actual);
    } else if (browserName == BrowserName.WEBKIT) {
      assertEquals(mapOf("protocol", "TLS 1.3", "subjectName", "playwright-test", "validFrom", 1691708270.0, "validTo", 2007068270.0), securityDetailsFields(securityDetails));
    } else {
      assertEquals(mapOf("issuer", "playwright-test", "protocol", "TLS 1.3", "subjectName", "playwright-test", "validFrom", 1691708270.0, "validTo", 2007068270.0), securityDetailsFields(securityDetails));
    }
  }

  // HA-047 should contain http2 for http2 requests: not ported, see upstream_not_ported.txt.

  @PlaywrightTest
  @DisplayName("HA-048 should filter favicon and favicon redirects")
  void shouldFilterFaviconAndFaviconRedirects(TestServer server, BrowserName browserName, ContextFactory contextFactory, PageWithHar pageWithHar) throws Exception {
    boolean headless = !Env.headed();
    assumeFalse(headless && browserName != BrowserName.FIREFOX, "headless browsers, except firefox, do not request favicons");
    assumeFalse(!headless && browserName == BrowserName.WEBKIT, "headed webkit does not have a favicon feature");

    PageWithHar.Result harSession = pageWithHar.create();

    // Browsers aggressively cache favicons, so force bust with the
    // `d` parameter to make iterating on this test more predictable and isolated.
    String favicon = "/no-cache-2/favicon.ico";
    String hashedFaviconUrl = "/favicon-hashed.ico";
    server.setRedirect(favicon, hashedFaviconUrl);
    server.setRoute(hashedFaviconUrl, (req, res) -> {
      server.serveFile(req, res, asset("media-query-prefers-color-scheme.svg"));
    });

    server.setRoute("/page.html", (req, res) -> {
      res.end("\n" +
        "      <!DOCTYPE html>\n" +
        "      <html>\n" +
        "        <head>\n" +
        "          <meta charset=\"utf-8\">\n" +
        "          <link rel=\"icon\" type=\"image/svg+xml\" href=\"" + favicon + "\">\n" +
        "          <title>SVG Favicon Test</title>\n" +
        "        </head>\n" +
        "        <body>\n" +
        "          favicons\n" +
        "        </body>\n" +
        "      </html>\n");
    });

    CompletableFuture<TestServer.Request> faviconRequest = server.waitForRequest(favicon);
    CompletableFuture<TestServer.Request> hashedFaviconRequest = server.waitForRequest(hashedFaviconUrl);
    harSession.page.navigate(server.PREFIX + "/page.html");
    faviconRequest.get();
    hashedFaviconRequest.get();

    harSession.page.waitForTimeout(500);
    // Text still being around ensures we haven't actually lost our browser to a crash.
    harSession.page.waitForSelector("text=favicons");

    // favicon and 302 redirects to favicons should be filtered out of request logs
    JsonObject log = harSession.getLog();
    assertEquals(1, log.getAsJsonArray("entries").size());
    JsonObject entry = entry(log, 0);
    assertEquals(server.PREFIX + "/page.html", entry.getAsJsonObject("request").get("url").getAsString());
  }

  @PlaywrightTest
  @DisplayName("HA-049 should have different hars for concurrent contexts")
  void shouldHaveDifferentHarsForConcurrentContexts(PageWithHar pageWithHar) {
    PageWithHar.Result session0 = pageWithHar.create(new PageWithHar.Options().setOutputPath("test-0.har"));
    session0.page.navigate("data:text/html,<title>Zero</title>");
    session0.page.waitForLoadState(LoadState.DOMCONTENTLOADED);

    PageWithHar.Result session1 = pageWithHar.create(new PageWithHar.Options().setOutputPath("test-1.har"));
    session1.page.navigate("data:text/html,<title>One</title>");
    session1.page.waitForLoadState(LoadState.DOMCONTENTLOADED);

    // Trigger flushing on the server and ensure they are not racing to same
    // location. NB: Run this test with --repeat-each 10.
    // Java: the two getLog() calls run one after the other, like the Promise.all of the calls.
    JsonObject log0 = session0.getLog();
    JsonObject log1 = session1.getLog();

    {
      assertEquals(1, log0.getAsJsonArray("pages").size());
      JsonObject pageEntry = log0.getAsJsonArray("pages").get(0).getAsJsonObject();
      assertEquals("Zero", pageEntry.get("title").getAsString());
    }

    {
      assertEquals(1, log1.getAsJsonArray("pages").size());
      JsonObject pageEntry = log1.getAsJsonArray("pages").get(0).getAsJsonObject();
      assertNotEquals(log0.getAsJsonArray("pages").get(0).getAsJsonObject().get("id"), pageEntry.get("id"));
      assertEquals("One", pageEntry.get("title").getAsString());
    }
  }

  @PlaywrightTest
  @DisplayName("HA-050 should exclude API request")
  void shouldExcludeAPIRequest(PageWithHar pageWithHar, TestServer server) {
    PageWithHar.Result harSession = pageWithHar.create();
    harSession.page.navigate(server.EMPTY_PAGE);
    harSession.page.request().get(server.PREFIX + "/simple.json");
    JsonObject log = harSession.getLog();
    List<String> urls = new ArrayList<>();
    for (JsonElement e : log.getAsJsonArray("entries")) {
      urls.add(e.getAsJsonObject().getAsJsonObject("request").get("url").getAsString());
    }
    assertEquals(Arrays.asList(server.EMPTY_PAGE), urls);
  }

  @PlaywrightTest
  @DisplayName("HA-051 should correctly record API request cookies with equals sign in value")
  void shouldCorrectlyRecordAPIRequestCookiesWithEqualsSignInValue(ContextFactory contextFactory, TestServer server, TestInfo testInfo) throws IOException {
    BrowserContext context = contextFactory.newContext();
    Path harPath = testInfo.outputPath("request.har");
    context.request().tracing().startHar(harPath);
    String url = server.PREFIX + "/simple.json";
    context.request().get(url, com.microsoft.playwright.options.RequestOptions.create()
      .setHeader("cookie", "token=abc=xyz; other=val"));
    context.request().tracing().stopHar();
    context.close();
    JsonObject log = readLog(harPath);
    assertEquals(json("[{\"name\":\"token\",\"value\":\"abc=xyz\"},{\"name\":\"other\",\"value\":\"val\"}]"),
      entry(log, 0).getAsJsonObject("request").get("cookies"));
  }

  @PlaywrightTest
  @DisplayName("HA-052 should record nameless API request cookies")
  @Fixme("driver: the API request HAR has {\"name\":\"nameless\",\"value\":\"\"} for the cookie header \"nameless; other=val\"; the fix for https://github.com/microsoft/playwright/issues/43155 is newer than the bundled driver 1.64.1")
  void shouldRecordNamelessAPIRequestCookies(ContextFactory contextFactory, TestServer server, TestInfo testInfo) throws IOException {
    BrowserContext context = contextFactory.newContext();
    Path harPath = testInfo.outputPath("request.har");
    context.request().tracing().startHar(harPath);
    context.request().get(server.PREFIX + "/simple.json", com.microsoft.playwright.options.RequestOptions.create()
      .setHeader("cookie", "nameless; other=val"));
    context.request().tracing().stopHar();
    context.close();
    JsonObject log = readLog(harPath);
    assertEquals(json("[{\"name\":\"\",\"value\":\"nameless\"},{\"name\":\"other\",\"value\":\"val\"}]"),
      entry(log, 0).getAsJsonObject("request").get("cookies"));
  }

  @PlaywrightTest
  @DisplayName("HA-053 should respect minimal mode for API Requests")
  void shouldRespectMinimalModeForAPIRequests(ContextFactory contextFactory, TestServer server, TestInfo testInfo) throws IOException {
    BrowserContext context = contextFactory.newContext();
    Path harPath = testInfo.outputPath("request.har");
    context.request().tracing().startHar(harPath, new Tracing.StartHarOptions().setMode(HarMode.MINIMAL));
    String url = server.PREFIX + "/simple.json";
    context.request().post(url, com.microsoft.playwright.options.RequestOptions.create()
      .setHeader("cookie", "a=b; c=d")
      .setData(mapOf("foo", "bar")));
    context.request().tracing().stopHar();
    context.close();
    JsonArray entries = readLog(harPath).getAsJsonArray("entries");
    assertEquals(1, entries.size());
    JsonObject entry = entries.get(0).getAsJsonObject();
    assertEquals(json("{\"receive\":-1,\"send\":-1,\"wait\":-1}"), entry.get("timings"));
    assertNull(entry.get("serverIPAddress"));
    assertNull(entry.get("_serverPort"));
    assertEquals(json("[]"), entry.getAsJsonObject("request").get("cookies"));
    assertEquals(-1, entry.getAsJsonObject("request").get("bodySize").getAsInt());
    assertEquals(-1, entry.getAsJsonObject("response").get("bodySize").getAsInt());
  }

  @PlaywrightTest
  @DisplayName("HA-054 should include redirects from API request")
  void shouldIncludeRedirectsFromAPIRequest(ContextFactory contextFactory, TestServer server, TestInfo testInfo) throws IOException {
    server.setRedirect("/redirect-me", "/simple.json");
    BrowserContext context = contextFactory.newContext();
    Path harPath = testInfo.outputPath("request.har");
    context.request().tracing().startHar(harPath);
    context.request().post(server.PREFIX + "/redirect-me", com.microsoft.playwright.options.RequestOptions.create()
      .setHeader("cookie", "a=b; c=d")
      .setData(mapOf("foo", "bar")));
    context.request().tracing().stopHar();
    context.close();
    JsonObject log = readLog(harPath);
    assertEquals(2, log.getAsJsonArray("entries").size());
    JsonObject redirect = entry(log, 0);
    JsonObject json = entry(log, 1);
    assertEquals(server.PREFIX + "/redirect-me", redirect.getAsJsonObject("request").get("url").getAsString());
    assertEquals(server.PREFIX + "/simple.json", json.getAsJsonObject("request").get("url").getAsString());
    assertEquals(-1, redirect.getAsJsonObject("response").get("bodySize").getAsInt());
    assertEquals(-1, redirect.getAsJsonObject("response").getAsJsonObject("content").get("size").getAsInt());
    assertNotNull(redirect.get("timings"));
    assertNotNull(json.get("timings"));
  }

  @PlaywrightTest
  @DisplayName("HA-055 should not hang on resources served from cache")
  void shouldNotHangOnResourcesServedFromCache(ContextFactory contextFactory, TestServer server, BrowserName browserName, PageWithHar pageWithHar) {
    server.setRoute("/one-style.css", (req, res) -> {
      res.writeHead(200, "Content-Type", "text/css", "Cache-Control", "public, max-age=10031518");
      res.end("body { background: red }");
    });
    PageWithHar.Result harSession = pageWithHar.create();
    harSession.page.navigate(server.PREFIX + "/har.html");
    harSession.page.navigate(server.PREFIX + "/har.html");
    JsonObject log = harSession.getLog();
    List<JsonObject> entries = new ArrayList<>();
    for (JsonElement e : log.getAsJsonArray("entries")) {
      if (e.getAsJsonObject().getAsJsonObject("request").get("url").getAsString().endsWith("one-style.css")) {
        entries.add(e.getAsJsonObject());
      }
    }
    // In firefox no request events are fired for cached resources.
    if (browserName == BrowserName.FIREFOX && !Env.isBidi()) {
      assertEquals(1, entries.size());
    } else {
      assertEquals(2, entries.size());
    }
  }

  @PlaywrightTest
  @DisplayName("HA-056 should not hang on slow chunked response")
  void shouldNotHangOnSlowChunkedResponse(BrowserName browserName, Browser browser, ContextFactory contextFactory, TestServer server, PageWithHar pageWithHar) {
    server.setRoute("/empty.html", (req, res) -> {
      res.writeHead(200, "Content-Type", "text/html");
      res.end("<script>\n" +
        "    let receivedFirstData = new Promise(f => {\n" +
        "      setTimeout(() =>  {\n" +
        "        var x = new XMLHttpRequest();\n" +
        "        x.open(\"GET\", \"slow.txt\");\n" +
        "        x.onprogress = () => f();\n" +
        "        x.send();\n" +
        "      }, 0);\n" +
        "    });\n" +
        "    </script>");
    });
    server.setRoute("/slow.txt", (req, res) -> {
      res.writeHead(200, "Content-Type", "text/plain", "Transfer-Encoding", "chunked", "Content-length", "2023");
      res.write("begin");
    });
    PageWithHar.Result harSession = pageWithHar.create();
    harSession.page.navigate(server.EMPTY_PAGE);
    harSession.page.evaluate("() => window.receivedFirstData");
    JsonObject log = harSession.getLog();

    assertEquals(browserName.toString(), log.getAsJsonObject("browser").get("name").getAsString());
    assertEquals(browser.version(), log.getAsJsonObject("browser").get("version").getAsString());
  }

  @PlaywrightTest
  @DisplayName("HA-057 should close the context when saving the har fails")
  void shouldCloseTheContextWhenSavingTheHarFails(ContextFactory contextFactory, TestServer server, TestInfo testInfo) throws Exception {
    Path filePath = testInfo.outputPath("not-a-directory");
    Files.write(filePath, "data".getBytes(StandardCharsets.UTF_8));
    BrowserContext context = contextFactory.newContext(new Browser.NewContextOptions().setRecordHarPath(filePath.resolve("test.har")));
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    CompletableFuture<Void> closed = new CompletableFuture<>();
    context.onClose(c -> closed.complete(null));
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> context.close());
    assertTrue(Pattern.compile("ENOTDIR|ENOENT|EEXIST").matcher(error.getMessage()).find());
    closed.get();
    context.close();
  }

  @PlaywrightTest
  @DisplayName("HA-058 should support HAR larger than 512MB")
  @EnabledOnBrowser(value = BrowserName.CHROMIUM, reason = "serializer is browser-agnostic; one browser is enough")
  void shouldSupportHARLargerThan512MB(ContextFactory contextFactory, TestServer server, BrowserName browserName, TestInfo testInfo) throws Exception {
    Path harPath = testInfo.outputPath("test.har");
    BrowserContext context = contextFactory.newContext();
    context.request().tracing().startHar(harPath);

    // 30 x 20MB textual responses push the HAR JSON past V8's ~512MB max
    // string length. Each body still fits in a single string; only the
    // aggregate would overflow the previous tokens.join('').
    byte[] body = new byte[20 * 1024 * 1024];
    Arrays.fill(body, (byte) 'a');
    server.setRoute("/large", (req, res) -> {
      res.writeHead(200, "Content-Type", "text/plain");
      res.end(body);
    });
    for (int i = 0; i < 30; i++) {
      context.request().get(server.PREFIX + "/large");
    }
    context.request().tracing().stopHar();
    context.close();

    long size = Files.size(harPath);
    assertTrue(size > 512L * 1024 * 1024);

    // Reading the whole file as a string would re-hit V8's limit, so
    // sample the head and tail to verify structural sanity.
    byte[] head = new byte[64];
    byte[] tail = new byte[64];
    try (RandomAccessFile fd = new RandomAccessFile(harPath.toFile(), "r")) {
      fd.seek(0);
      fd.readFully(head);
      fd.seek(size - 64);
      fd.readFully(tail);
    }
    assertTrue(Pattern.compile("^\\{\\s*\"log\"\\s*:\\s*\\{").matcher(new String(head, StandardCharsets.UTF_8)).find());
    assertTrue(Pattern.compile("\\}\\s*\\}\\s*$").matcher(new String(tail, StandardCharsets.UTF_8)).find());
  }

  @PlaywrightTest
  @DisplayName("HA-059 should record resource type")
  void shouldRecordResourceType(PageWithHar pageWithHar, TestServer server) {
    server.setRoute("/resource-types.html", (req, res) -> {
      res.writeHead(200, "Content-Type", "text/html");
      res.end("\n" +
        "      <link rel=\"stylesheet\" href=\"/resource-type-stylesheet.css\">\n" +
        "      <script src=\"/resource-type-script.js\"></script>\n" +
        "      <img src=\"/resource-type-image.png\">\n" +
        "    ");
    });
    server.setRoute("/resource-type-stylesheet.css", (req, res) -> {
      res.writeHead(200, "Content-Type", "text/css");
      res.end("\n" +
        "      @font-face {\n" +
        "        font-family: 'iconfont';\n" +
        "        src: url('/resource-type-font.woff2') format('woff2');\n" +
        "      }\n" +
        "      body { font-family: 'iconfont'; }\n" +
        "    ");
    });
    server.setRoute("/resource-type-font.woff2", (req, res) -> {
      server.serveFile(req, res, asset("webfont/iconfont.woff2"));
    });
    server.setRoute("/resource-type-script.js", (req, res) -> {
      res.writeHead(200, "Content-Type", "application/javascript");
      res.end("window.__loaded = true;");
    });
    server.setRoute("/resource-type-image.png", (req, res) -> {
      server.serveFile(req, res, asset("pptr.png"));
    });

    PageWithHar.Result harSession = pageWithHar.create();
    harSession.page.navigate(server.PREFIX + "/resource-types.html");
    harSession.page.evaluate("() => fetch('/resource-type-fetch').catch(() => {})");
    harSession.page.evaluate("() => new Promise(resolve => {\n" +
      "      const xhr = new XMLHttpRequest();\n" +
      "      xhr.open('GET', '/resource-type-xhr');\n" +
      "      xhr.onloadend = () => resolve();\n" +
      "      xhr.send();\n" +
      "    })");
    JsonObject log = harSession.getLog();

    Map<String, String> typeForURL = new LinkedHashMap<>();
    for (JsonElement e : log.getAsJsonArray("entries")) {
      JsonObject entry = e.getAsJsonObject();
      typeForURL.put(entry.getAsJsonObject("request").get("url").getAsString(), entry.get("_resourceType").getAsString());
    }
    // toMatchObject
    Map<String, String> expected = new LinkedHashMap<>();
    expected.put(server.PREFIX + "/resource-types.html", "document");
    expected.put(server.PREFIX + "/resource-type-stylesheet.css", "stylesheet");
    expected.put(server.PREFIX + "/resource-type-script.js", "script");
    expected.put(server.PREFIX + "/resource-type-image.png", "image");
    expected.put(server.PREFIX + "/resource-type-font.woff2", "font");
    expected.put(server.PREFIX + "/resource-type-fetch", "fetch");
    expected.put(server.PREFIX + "/resource-type-xhr", "xhr");
    for (Map.Entry<String, String> e : expected.entrySet()) {
      assertEquals(e.getValue(), typeForURL.get(e.getKey()));
    }
  }

  @Nested
  class TracingStartHar {
    @PlaywrightTest
    @DisplayName("HA-060 should record a HAR with options")
    void shouldRecordAHARWithOptions(ContextFactory contextFactory, TestServer server, TestInfo testInfo) throws IOException {
      BrowserContext context = contextFactory.newContext();
      Path harPath = testInfo.outputPath("tracing.har");
      context.tracing().startHar(harPath, new Tracing.StartHarOptions().setMode(HarMode.MINIMAL).setUrlFilter("**/one-style.css"));
      Page page = context.newPage();
      page.navigate(server.PREFIX + "/one-style.html");
      context.tracing().stopHar();
      context.close();

      JsonObject log = readLog(harPath);
      List<String> urls = new ArrayList<>();
      for (JsonElement e : log.getAsJsonArray("entries")) {
        urls.add(e.getAsJsonObject().getAsJsonObject("request").get("url").getAsString());
      }
      assertEquals(Arrays.asList(server.PREFIX + "/one-style.css"), urls);
      // Minimal mode drops body sizes.
      assertEquals(-1, entry(log, 0).getAsJsonObject("request").get("bodySize").getAsInt());
    }

    @PlaywrightTest
    @DisplayName("HA-061 should record a HAR for a context APIRequestContext")
    void shouldRecordAHARForAContextAPIRequestContext(ContextFactory contextFactory, TestServer server, TestInfo testInfo) throws IOException {
      BrowserContext context = contextFactory.newContext();
      Path harPath = testInfo.outputPath("request.har");
      context.request().tracing().startHar(harPath);
      Page page = context.newPage();
      page.navigate(server.EMPTY_PAGE);
      String url = server.PREFIX + "/simple.json";
      com.microsoft.playwright.APIResponse response = context.request().post(url, com.microsoft.playwright.options.RequestOptions.create()
        .setHeader("cookie", "a=b; c=d")
        .setData(mapOf("foo", "bar")));
      byte[] responseBody = response.body();
      context.request().tracing().stopHar();
      context.close();

      JsonObject log = readLog(harPath);
      assertEquals(1, log.getAsJsonArray("entries").size());
      JsonObject entry = entry(log, 0);
      assertEquals(url, entry.getAsJsonObject("request").get("url").getAsString());
      assertEquals("POST", entry.getAsJsonObject("request").get("method").getAsString());
      assertEquals("HTTP/1.1", entry.getAsJsonObject("request").get("httpVersion").getAsString());
      assertEquals(json("[{\"name\":\"a\",\"value\":\"b\"},{\"name\":\"c\",\"value\":\"d\"}]"), entry.getAsJsonObject("request").get("cookies"));
      assertTrue(entry.getAsJsonObject("request").getAsJsonArray("headers").size() > 1);
      assertTrue(truthy(findHeader(entry.getAsJsonObject("request").getAsJsonArray("headers"), "user-agent")));
      assertEquals("application/json", findHeader(entry.getAsJsonObject("request").getAsJsonArray("headers"), "content-type").get("value").getAsString());
      assertEquals("13", findHeader(entry.getAsJsonObject("request").getAsJsonArray("headers"), "content-length").get("value").getAsString());
      assertEquals(13, entry.getAsJsonObject("request").get("bodySize").getAsInt());

      assertEquals(200, entry.getAsJsonObject("response").get("status").getAsInt());
      assertTrue(findHeader(entry.getAsJsonObject("response").getAsJsonArray("headers"), "content-type").get("value").getAsString().contains("application/json"));
      assertEquals(15, entry.getAsJsonObject("response").getAsJsonObject("content").get("size").getAsInt());
      assertEquals(new String(responseBody, StandardCharsets.UTF_8), entry.getAsJsonObject("response").getAsJsonObject("content").get("text").getAsString());
      assertEquals(15, entry.getAsJsonObject("response").get("bodySize").getAsInt());

      assertTrue(entry.get("time").getAsDouble() > 0);
      // expect.objectContaining
      JsonObject timings = entry.getAsJsonObject("timings");
      assertEquals(-1, timings.get("blocked").getAsInt());
      for (String name : Arrays.asList("connect", "dns", "receive", "send", "ssl", "wait")) {
        assertTrue(timings.get(name).getAsJsonPrimitive().isNumber());
      }

      assertNotNull(entry.get("serverIPAddress"));
      assertEquals(server.PORT, entry.get("_serverPort").getAsInt());
    }

    @PlaywrightTest
    @DisplayName("HA-062 should include pages")
    void shouldIncludePages(ContextFactory contextFactory, TestServer server, TestInfo testInfo) throws IOException {
      BrowserContext context = contextFactory.newContext();
      Path harPath = testInfo.outputPath("tracing.har");
      context.tracing().startHar(harPath);
      Page page = context.newPage();
      page.navigate(server.PREFIX + "/title.html");
      Page page2 = context.newPage();
      page2.navigate(server.PREFIX + "/empty.html");
      page.close(); // Close the page before stopping - it should still be in the har.
      context.tracing().stopHar();
      context.close();

      JsonObject log = readLog(harPath);
      assertEquals(2, log.getAsJsonArray("pages").size());
      assertEquals("Woof-Woof", log.getAsJsonArray("pages").get(0).getAsJsonObject().get("title").getAsString());
      assertEquals("", log.getAsJsonArray("pages").get(1).getAsJsonObject().get("title").getAsString());
    }

    @PlaywrightTest
    @DisplayName("HA-063 should record a zipped HAR for APIRequestContext")
    void shouldRecordAZippedHARForAPIRequestContext(Playwright playwright, TestServer server, TestInfo testInfo) {
      APIRequestContext request = playwright.request().newContext();
      Path harPath = testInfo.outputPath("tracing.har.zip");
      request.tracing().startHar(harPath, new Tracing.StartHarOptions().setContent(HarContentPolicy.ATTACH));
      request.get(server.PREFIX + "/simple.json");
      request.tracing().stopHar();
      request.dispose();

      Map<String, byte[]> resources = com.microsoft.playwright.fixtures.Utils.parseZip(harPath);
      JsonObject log = zipLog(resources);
      boolean found = false;
      for (JsonElement e : log.getAsJsonArray("entries")) {
        if (e.getAsJsonObject().getAsJsonObject("request").get("url").getAsString().equals(server.PREFIX + "/simple.json")) {
          found = true;
        }
      }
      assertTrue(found);
    }

    @PlaywrightTest
    @DisplayName("HA-064 should record correct cookie expires for APIRequestContext")
    void shouldRecordCorrectCookieExpiresForAPIRequestContext(Playwright playwright, TestServer server, TestInfo testInfo) {
      server.setRoute("/set-cookie", (req, res) -> {
        res.setHeader("Set-Cookie", "name=value; Expires=Tue, 01 Jan 2030 00:00:00 GMT");
        res.end("hello");
      });
      APIRequestContext request = playwright.request().newContext();
      Path harPath = testInfo.outputPath("api.har.zip");
      request.tracing().startHar(harPath, new Tracing.StartHarOptions().setContent(HarContentPolicy.ATTACH));
      request.get(server.PREFIX + "/set-cookie");
      request.tracing().stopHar();
      request.dispose();

      Map<String, byte[]> resources = com.microsoft.playwright.fixtures.Utils.parseZip(harPath);
      JsonObject log = zipLog(resources);
      JsonObject entry = findEntry(log, "/set-cookie");
      JsonObject cookie = null;
      for (JsonElement c : entry.getAsJsonObject("response").getAsJsonArray("cookies")) {
        if (cookie == null && c.getAsJsonObject().get("name").getAsString().equals("name")) {
          cookie = c.getAsJsonObject();
        }
      }
      assertEquals(2030, OffsetDateTime.parse(cookie.get("expires").getAsString()).withOffsetSameInstant(ZoneOffset.UTC).getYear());
    }

    @PlaywrightTest
    @DisplayName("HA-065 should record mixed-case request content-type for APIRequestContext")
    void shouldRecordMixedCaseRequestContentTypeForAPIRequestContext(Playwright playwright, TestServer server, TestInfo testInfo) {
      server.setRoute("/post", (req, res) -> res.end("ok"));
      APIRequestContext request = playwright.request().newContext();
      Path harPath = testInfo.outputPath("api-post.har.zip");
      request.tracing().startHar(harPath, new Tracing.StartHarOptions().setContent(HarContentPolicy.ATTACH));
      request.post(server.PREFIX + "/post", com.microsoft.playwright.options.RequestOptions.create()
        .setHeader("Content-Type", "application/json")
        .setData("{\"a\":1}".getBytes(StandardCharsets.UTF_8)));
      request.tracing().stopHar();
      request.dispose();

      Map<String, byte[]> resources = com.microsoft.playwright.fixtures.Utils.parseZip(harPath);
      JsonObject log = zipLog(resources);
      JsonObject entry = findEntry(log, "/post");
      assertEquals("application/json", entry.getAsJsonObject("request").getAsJsonObject("postData").get("mimeType").getAsString());
    }

    // HA-066 should record a HAR with resourcesDir: not ported, see upstream_not_ported.txt.
    // HA-067 should reject resourcesDir together with a .zip har file: not ported, see upstream_not_ported.txt.
  }

  private static JsonElement json(String text) {
    return JsonParser.parseString(text);
  }

  private static JsonObject readLog(Path harPath) throws IOException {
    return json(new String(Files.readAllBytes(harPath), StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonObject("log");
  }

  private static JsonObject zipLog(Map<String, byte[]> zip) {
    return json(new String(zip.get("har.har"), StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonObject("log");
  }

  private static JsonObject entry(JsonObject log, int index) {
    return log.getAsJsonArray("entries").get(index).getAsJsonObject();
  }

  private static JsonObject content(JsonObject log, int index) {
    return entry(log, index).getAsJsonObject("response").getAsJsonObject("content");
  }

  /** entries.find(e => e.request.url.endsWith(suffix)) */
  private static JsonObject findEntry(JsonObject log, String suffix) {
    for (JsonElement e : log.getAsJsonArray("entries")) {
      if (e.getAsJsonObject().getAsJsonObject("request").get("url").getAsString().endsWith(suffix)) {
        return e.getAsJsonObject();
      }
    }
    return null;
  }

  /** headers.find(h => h.name.toLowerCase() === name) */
  private static JsonObject findHeader(JsonArray headers, String name) {
    for (JsonElement h : headers) {
      if (h.getAsJsonObject().get("name").getAsString().toLowerCase().equals(name)) {
        return h.getAsJsonObject();
      }
    }
    return null;
  }

  private static boolean hasHeaderNamed(JsonArray headers, String name) {
    for (JsonElement h : headers) {
      if (h.getAsJsonObject().get("name").getAsString().equals(name)) {
        return true;
      }
    }
    return false;
  }

  /** The JavaScript truthiness of a value that comes from a HAR file. */
  private static boolean truthy(JsonElement value) {
    if (value == null || value.isJsonNull()) {
      return false;
    }
    if (value.isJsonPrimitive()) {
      if (value.getAsJsonPrimitive().isString()) {
        return !value.getAsString().isEmpty();
      }
      if (value.getAsJsonPrimitive().isBoolean()) {
        return value.getAsBoolean();
      }
      return value.getAsDouble() != 0;
    }
    return true;
  }

  /** new Date(text).valueOf() */
  private static long dateValue(String text) {
    return OffsetDateTime.parse(text).toInstant().toEpochMilli();
  }

  private static Map<String, Object> securityDetailsFields(SecurityDetails details) {
    Map<String, Object> result = new LinkedHashMap<>();
    if (details.issuer != null) {
      result.put("issuer", details.issuer);
    }
    if (details.protocol != null) {
      result.put("protocol", details.protocol);
    }
    if (details.subjectName != null) {
      result.put("subjectName", details.subjectName);
    }
    if (details.validFrom != null) {
      result.put("validFrom", details.validFrom);
    }
    if (details.validTo != null) {
      result.put("validTo", details.validTo);
    }
    return result;
  }

  /** require('../../package.json').version of the driver. */
  private static String driverVersion() throws IOException {
    Path packageJson = Driver.ensureDriverInstalled(Collections.<String, String>emptyMap(), false).driverDir().resolve("package").resolve("package.json");
    return json(new String(Files.readAllBytes(packageJson), StandardCharsets.UTF_8)).getAsJsonObject().get("version").getAsString();
  }
}
