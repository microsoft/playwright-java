/**
 * Copyright 2017 Google Inc. All rights reserved.
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

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.LaunchPersistent;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.Cookie;
import com.microsoft.playwright.options.SameSiteAttribute;
import org.junit.jupiter.api.DisplayName;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static com.microsoft.playwright.fixtures.Utils.verifyViewport;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/library/defaultbrowsercontext-1.spec.ts. */
public class TestDefaultbrowsercontext1 {
  private static List<Cookie> maybeFilterCookies(String channel, List<Cookie> cookies) {
    if (channel != null && channel.startsWith("msedge")) {
      List<Cookie> result = new ArrayList<>();
      for (Cookie c : cookies) {
        if (!c.domain.endsWith("microsoft.com")) {
          result.add(c);
        }
      }
      return result;
    }
    return cookies;
  }

  // Java: Cookie has no equals(), cookies are compared as maps.
  private static List<Map<String, Object>> cookieMaps(List<Cookie> cookies) {
    List<Map<String, Object>> result = new ArrayList<>();
    for (Cookie c : cookies) {
      result.add(mapOf("name", c.name, "value", c.value, "domain", c.domain, "path", c.path, "expires", c.expires,
        "httpOnly", c.httpOnly, "secure", c.secure, "sameSite", sameSite(c.sameSite)));
    }
    return result;
  }

  private static String sameSite(SameSiteAttribute attribute) {
    String name = attribute.name();
    return name.charAt(0) + name.substring(1).toLowerCase();
  }

  // Java: the defaultSameSiteCookieValue fixture of tests/config/browserTest.ts.
  private static String defaultSameSiteCookieValue(BrowserName browserName) {
    if (browserName == BrowserName.CHROMIUM || Env.isBidi()) {
      return "Lax";
    } else if (browserName == BrowserName.WEBKIT && (Env.platform().equals("linux") || "webkit-wsl".equals(Env.channel()))) {
      return "Lax";
    } else {
      return "None";
    }
  }

  @PlaywrightTest
  @DisplayName("DC-001 context.cookies() should work @smoke")
  void contextCookiesShouldWork(TestServer server, LaunchPersistent launchPersistent, BrowserName browserName) {
    Page page = launchPersistent.launch().page;
    page.navigate(server.EMPTY_PAGE);
    Object documentCookie = page.evaluate("() => {\n" +
      "  document.cookie = 'username=John Doe';\n" +
      "  return document.cookie;\n" +
      "}");
    assertEquals("username=John Doe", documentCookie);
    assertEquals(Arrays.asList(mapOf(
      "name", "username",
      "value", "John Doe",
      "domain", server.HOSTNAME,
      "path", "/",
      "expires", -1.0,
      "httpOnly", false,
      "secure", false,
      "sameSite", defaultSameSiteCookieValue(browserName))),
      cookieMaps(maybeFilterCookies(Env.channel(), page.context().cookies())));
  }

  @PlaywrightTest
  @DisplayName("DC-002 context.addCookies() should work")
  void contextAddCookiesShouldWork(TestServer server, LaunchPersistent launchPersistent, BrowserName browserName) {
    Page page = launchPersistent.launch().page;
    page.navigate(server.EMPTY_PAGE);
    page.context().addCookies(Arrays.asList(new Cookie("username", "John Doe")
      .setUrl(server.EMPTY_PAGE)
      .setSameSite(SameSiteAttribute.LAX)));
    assertEquals("username=John Doe", page.evaluate("() => document.cookie"));
    assertEquals(Arrays.asList(mapOf(
      "name", "username",
      "value", "John Doe",
      "domain", server.HOSTNAME,
      "path", "/",
      "expires", -1.0,
      "httpOnly", false,
      "secure", false,
      "sameSite", (browserName == BrowserName.WEBKIT && Env.isWindows && !"webkit-wsl".equals(Env.channel())) ? "None" : "Lax")),
      cookieMaps(maybeFilterCookies(Env.channel(), page.context().cookies())));
  }

  @PlaywrightTest
  @DisplayName("DC-003 context.clearCookies() should work")
  void contextClearCookiesShouldWork(TestServer server, LaunchPersistent launchPersistent) {
    Page page = launchPersistent.launch().page;
    page.navigate(server.EMPTY_PAGE);
    page.context().addCookies(Arrays.asList(
      new Cookie("cookie1", "1").setUrl(server.EMPTY_PAGE),
      new Cookie("cookie2", "2").setUrl(server.EMPTY_PAGE)));
    assertEquals("cookie1=1; cookie2=2", page.evaluate("document.cookie"));
    page.context().clearCookies();
    page.reload();
    assertEquals(Collections.emptyList(), maybeFilterCookies(Env.channel(), page.context().cookies(Collections.<String>emptyList())));
    assertEquals("", page.evaluate("document.cookie"));
  }

  @PlaywrightTest
  @DisplayName("DC-004 should support viewport option")
  void shouldSupportViewportOption(LaunchPersistent launchPersistent) {
    LaunchPersistent.Result result = launchPersistent.launch(new BrowserType.LaunchPersistentContextOptions().setViewportSize(456, 789));
    Page page = result.page;
    BrowserContext context = result.context;
    verifyViewport(page, 456, 789);
    Page page2 = context.newPage();
    verifyViewport(page2, 456, 789);
  }

  @PlaywrightTest
  @DisplayName("DC-005 should support deviceScaleFactor option")
  void shouldSupportDeviceScaleFactorOption(LaunchPersistent launchPersistent) {
    Page page = launchPersistent.launch(new BrowserType.LaunchPersistentContextOptions().setDeviceScaleFactor(3)).page;
    assertEquals(3, page.evaluate("window.devicePixelRatio"));
  }

  @PlaywrightTest
  @DisplayName("DC-006 should support userAgent option")
  void shouldSupportUserAgentOption(TestServer server, LaunchPersistent launchPersistent) throws Exception {
    Page page = launchPersistent.launch(new BrowserType.LaunchPersistentContextOptions().setUserAgent("foobar")).page;
    assertEquals("foobar", page.evaluate("() => navigator.userAgent"));
    CompletableFuture<TestServer.Request> requestPromise = server.waitForRequest("/empty.html");
    page.navigate(server.EMPTY_PAGE);
    TestServer.Request request = requestPromise.get();
    assertEquals("foobar", request.headers.get("user-agent"));
  }

  @PlaywrightTest
  @DisplayName("DC-007 should support bypassCSP option")
  void shouldSupportBypassCSPOption(TestServer server, LaunchPersistent launchPersistent) {
    Page page = launchPersistent.launch(new BrowserType.LaunchPersistentContextOptions().setBypassCSP(true)).page;
    page.navigate(server.PREFIX + "/csp.html");
    page.addScriptTag(new Page.AddScriptTagOptions().setContent("window[\"__injected\"] = 42;"));
    assertEquals(42, page.evaluate("__injected"));
    assertEquals(42, page.evaluate("window[\"__inlineScriptValue\"]"));
  }

  @PlaywrightTest
  @DisplayName("DC-008 should support javascriptEnabled option")
  void shouldSupportJavascriptEnabledOption(LaunchPersistent launchPersistent, BrowserName browserName) {
    Page page = launchPersistent.launch(new BrowserType.LaunchPersistentContextOptions().setJavaScriptEnabled(false)).page;
    page.navigate("data:text/html, <script>var something = \"forbidden\"</script>");
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.evaluate("something"));
    if (browserName == BrowserName.WEBKIT) {
      assertTrue(error.getMessage().contains("Can't find variable: something"));
    } else {
      assertTrue(error.getMessage().contains("something is not defined"));
    }
  }

  @PlaywrightTest
  @DisplayName("DC-009 should support httpCredentials option")
  void shouldSupportHttpCredentialsOption(TestServer server, LaunchPersistent launchPersistent) {
    Page page = launchPersistent.launch(new BrowserType.LaunchPersistentContextOptions().setHttpCredentials("user", "pass")).page;
    server.setAuth("/playground.html", "user", "pass");
    Response response = page.navigate(server.PREFIX + "/playground.html");
    assertEquals(200, response.status());
  }

  @PlaywrightTest
  @DisplayName("DC-010 should support offline option")
  void shouldSupportOfflineOption(TestServer server, LaunchPersistent launchPersistent) {
    Page page = launchPersistent.launch(new BrowserType.LaunchPersistentContextOptions().setOffline(true)).page;
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.navigate(server.EMPTY_PAGE));
    assertNotNull(error);
  }

  @PlaywrightTest
  @DisplayName("DC-011 should support acceptDownloads option")
  void shouldSupportAcceptDownloadsOption(TestServer server, LaunchPersistent launchPersistent) throws IOException {
    Page page = launchPersistent.launch().page;
    server.setRoute("/download", (req, res) -> {
      res.setHeader("Content-Type", "application/octet-stream");
      res.setHeader("Content-Disposition", "attachment");
      res.end("Hello world");
    });
    page.setContent("<a href=\"" + server.PREFIX + "/download\">download</a>");
    Download download = page.waitForDownload(() -> page.click("a"));
    Path path = download.path();
    assertTrue(Files.exists(path));
    assertEquals("Hello world", new String(Files.readAllBytes(path), StandardCharsets.UTF_8));
  }
}
