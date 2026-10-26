/*
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

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.Fixture;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.HttpCredentials;
import org.junit.jupiter.api.DisplayName;

import java.net.URI;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/library/browsercontext-credentials.spec.ts. */
public class TestBrowsercontextCredentials {
  @Fixture
  Boolean failsOn401(BrowserName browserName) {
    return browserName == BrowserName.CHROMIUM && !Env.isHeadlessShell(browserName);
  }

  // Java: stands for page.goto(url).catch(e => e); the result is the Response or the PlaywrightException.
  private static Object gotoOrError(Page page, String url) {
    try {
      return page.navigate(url);
    } catch (PlaywrightException e) {
      return e;
    }
  }

  @PlaywrightTest
  @DisplayName("AU-001 should fail without credentials")
  void shouldFailWithoutCredentials(Browser browser, TestServer server, Boolean failsOn401) {
    server.setAuth("/empty.html", "user", "pass");
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    Object responseOrError = gotoOrError(page, server.EMPTY_PAGE);
    if (failsOn401) {
      assertTrue(((PlaywrightException) responseOrError).getMessage().contains("net::ERR_INVALID_AUTH_CREDENTIALS"));
    } else {
      assertEquals(401, ((Response) responseOrError).status());
    }
  }

  @PlaywrightTest
  @DisplayName("AU-004 should work with correct credentials @smoke")
  void shouldWorkWithCorrectCredentials(Browser browser, TestServer server) {
    server.setAuth("/empty.html", "user", "pass");
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setHttpCredentials(new HttpCredentials("user", "pass")));
    Page page = context.newPage();
    Response response = page.navigate(server.EMPTY_PAGE);
    assertEquals(200, response.status());
    context.close();
  }

  @PlaywrightTest
  @DisplayName("AU-005 should fail with wrong credentials")
  void shouldFailWithWrongCredentials(Browser browser, TestServer server) {
    server.setAuth("/empty.html", "user", "pass");
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setHttpCredentials(new HttpCredentials("foo", "bar")));
    Page page = context.newPage();
    Response response = page.navigate(server.EMPTY_PAGE);
    assertEquals(401, response.status());
    context.close();
  }

  @PlaywrightTest
  @DisplayName("AU-006 should return resource body")
  void shouldReturnResourceBody(Browser browser, TestServer server) {
    server.setAuth("/playground.html", "user", "pass");
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setHttpCredentials(new HttpCredentials("user", "pass")));
    Page page = context.newPage();
    Response response = page.navigate(server.PREFIX + "/playground.html");
    assertEquals(200, response.status());
    assertEquals("Playground", page.title());
    assertTrue(new String(response.body()).contains("Playground"));
    context.close();
  }

  @PlaywrightTest
  @DisplayName("AU-007 should work with a single credential in an array")
  void shouldWorkWithASingleCredentialInAnArray(Browser browser, TestServer server) {
    server.setAuth("/empty.html", "user", "pass");
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setHttpCredentials(Arrays.asList(new HttpCredentials("user", "pass"))));
    Page page = context.newPage();
    Response response = page.navigate(server.EMPTY_PAGE);
    assertEquals(200, response.status());
    context.close();
  }

  @PlaywrightTest
  @DisplayName("AU-008 should work with multiple credentials for different origins")
  void shouldWorkWithMultipleCredentialsForDifferentOrigins(Browser browser, TestServer server) {
    server.setAuth("/empty.html", "user1", "pass1");
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setHttpCredentials(Arrays.asList(
        new HttpCredentials("user1", "pass1").setOrigin(server.PREFIX),
        new HttpCredentials("user2", "pass2").setOrigin(server.CROSS_PROCESS_PREFIX))));
    Page page = context.newPage();
    Response response1 = page.navigate(server.EMPTY_PAGE);
    assertEquals(200, response1.status());
    // Wrong credentials are picked for the other origin.
    Response response2 = page.navigate(server.CROSS_PROCESS_PREFIX + "/empty.html");
    assertEquals(401, response2.status());
    context.close();
  }

  @PlaywrightTest
  @DisplayName("AU-009 should fall back to credentials without origin")
  void shouldFallBackToCredentialsWithoutOrigin(Browser browser, TestServer server) {
    server.setAuth("/empty.html", "user", "pass");
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setHttpCredentials(Arrays.asList(
        new HttpCredentials("user2", "pass2").setOrigin(server.CROSS_PROCESS_PREFIX),
        new HttpCredentials("user", "pass"))));
    Page page = context.newPage();
    Response response1 = page.navigate(server.EMPTY_PAGE);
    assertEquals(200, response1.status());
    // First matching entry has wrong credentials for this origin.
    Response response2 = page.navigate(server.CROSS_PROCESS_PREFIX + "/empty.html");
    assertEquals(401, response2.status());
    context.close();
  }

  @PlaywrightTest
  @DisplayName("AU-010 should use the first matching credential")
  void shouldUseTheFirstMatchingCredential(Browser browser, TestServer server) {
    server.setAuth("/empty.html", "user", "pass");
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setHttpCredentials(Arrays.asList(
        new HttpCredentials("wrong", "wrong"),
        new HttpCredentials("user", "pass").setOrigin(server.PREFIX))));
    Page page = context.newPage();
    Response response = page.navigate(server.EMPTY_PAGE);
    assertEquals(401, response.status());
    context.close();
  }

  @PlaywrightTest
  @DisplayName("AU-011 should work with correct credentials and matching origin")
  void shouldWorkWithCorrectCredentialsAndMatchingOrigin(Browser browser, TestServer server) {
    server.setAuth("/empty.html", "user", "pass");
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setHttpCredentials(new HttpCredentials("user", "pass").setOrigin(server.PREFIX)));
    Page page = context.newPage();
    Response response = page.navigate(server.EMPTY_PAGE);
    assertEquals(200, response.status());
    context.close();
  }

  @PlaywrightTest
  @DisplayName("AU-012 should work with correct credentials and matching origin case insensitive")
  void shouldWorkWithCorrectCredentialsAndMatchingOriginCaseInsensitive(Browser browser, TestServer server) {
    server.setAuth("/empty.html", "user", "pass");
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setHttpCredentials(new HttpCredentials("user", "pass").setOrigin(server.PREFIX.toUpperCase())));
    Page page = context.newPage();
    Response response = page.navigate(server.EMPTY_PAGE);
    assertEquals(200, response.status());
    context.close();
  }

  @PlaywrightTest
  @DisplayName("AU-013 should fail with correct credentials and mismatching scheme")
  void shouldFailWithCorrectCredentialsAndMismatchingScheme(Browser browser, TestServer server, Boolean failsOn401) {
    server.setAuth("/empty.html", "user", "pass");
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setHttpCredentials(new HttpCredentials("user", "pass").setOrigin(server.PREFIX.replace("http://", "https://"))));
    Page page = context.newPage();
    Object responseOrError = gotoOrError(page, server.EMPTY_PAGE);
    if (failsOn401) {
      assertTrue(((PlaywrightException) responseOrError).getMessage().contains("net::ERR_INVALID_AUTH_CREDENTIALS"));
    } else {
      assertEquals(401, ((Response) responseOrError).status());
    }
    context.close();
  }

  @PlaywrightTest
  @DisplayName("AU-014 should fail with correct credentials and mismatching hostname")
  void shouldFailWithCorrectCredentialsAndMismatchingHostname(Browser browser, TestServer server, Boolean failsOn401) {
    server.setAuth("/empty.html", "user", "pass");
    String hostname = URI.create(server.PREFIX).getHost();
    String origin = server.PREFIX.replace(hostname, "mismatching-hostname");
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setHttpCredentials(new HttpCredentials("user", "pass").setOrigin(origin)));
    Page page = context.newPage();
    Object responseOrError = gotoOrError(page, server.EMPTY_PAGE);
    if (failsOn401) {
      assertTrue(((PlaywrightException) responseOrError).getMessage().contains("net::ERR_INVALID_AUTH_CREDENTIALS"));
    } else {
      assertEquals(401, ((Response) responseOrError).status());
    }
    context.close();
  }

  @PlaywrightTest
  @DisplayName("AU-015 should fail with correct credentials and mismatching port")
  void shouldFailWithCorrectCredentialsAndMismatchingPort(Browser browser, TestServer server, Boolean failsOn401) {
    server.setAuth("/empty.html", "user", "pass");
    String origin = server.PREFIX.replace(String.valueOf(server.PORT), String.valueOf(server.PORT + 1));
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setHttpCredentials(new HttpCredentials("user", "pass").setOrigin(origin)));
    Page page = context.newPage();
    Object responseOrError = gotoOrError(page, server.EMPTY_PAGE);
    if (failsOn401) {
      assertTrue(((PlaywrightException) responseOrError).getMessage().contains("net::ERR_INVALID_AUTH_CREDENTIALS"));
    } else {
      assertEquals(401, ((Response) responseOrError).status());
    }
    context.close();
  }

  @PlaywrightTest
  @DisplayName("AU-016 should not override Authorization header set by the page")
  void shouldNotOverrideAuthorizationHeaderSetByThePage(Browser browser, TestServer server) {
    server.setAuth("/empty.html", "user", "pass");
    server.setRoute("/echo-auth", (req, res) -> {
      res.end(req.headers.get("authorization") != null ? req.headers.get("authorization") : "<none>");
    });
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setHttpCredentials(new HttpCredentials("user", "pass")));
    Page page = context.newPage();
    Response response = page.navigate(server.EMPTY_PAGE);
    assertEquals(200, response.status());
    Object received = page.evaluate("async () => {\n" +
      "  const response = await fetch('/echo-auth', { headers: { 'Authorization': 'Bearer my-own-app-token' } });\n" +
      "  return await response.text();\n" +
      "}");
    assertEquals("Bearer my-own-app-token", received);
    context.close();
  }
}
