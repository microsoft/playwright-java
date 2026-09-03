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

import static java.util.Arrays.asList;

import com.microsoft.playwright.options.HttpCredentials;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestBrowserContextCredentials extends TestBase {

  static boolean isChromiumHeadedLike() {
    // --headless=new, the default in all Chromium channels, is like headless.
    return isChromium() && (isHeaded() || getBrowserChannelFromEnv() != null);
  }

  @Test
  @DisabledIf(value="isChromiumHeadedLike", disabledReason="fail")
  void shouldFailWithoutCredentials() {
    server.setAuth("/empty.html", "user", "pass");
    Response response = page.navigate(server.EMPTY_PAGE);
    assertEquals(401, response.status());
  }

  void shouldWorkWithSetHTTPCredentials() {
    // The method is not exposed in Java.
  }

  @Test
  void shouldWorkWithCorrectCredentials() {
    server.setAuth("/empty.html", "user", "pass");
    try (BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setHttpCredentials("user", "pass"))) {
      Page page = context.newPage();
      Response response = page.navigate(server.EMPTY_PAGE);
      assertEquals(200, response.status());
    }
  }

  @Test
  void shouldFailWithWrongCredentials() {
    server.setAuth("/empty.html", "user", "pass");
    try (BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setHttpCredentials("foo", "bar"))) {
      Page page = context.newPage();
      Response response = page.navigate(server.EMPTY_PAGE);
      assertEquals(401, response.status());
    }
  }

  @Test
  void shouldReturnResourceBody() {
    server.setAuth("/playground.html", "user", "pass");
    try (BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setHttpCredentials("user", "pass"))) {
      Page page = context.newPage();
      Response response = page.navigate(server.PREFIX + "/playground.html");
      assertEquals(200, response.status());
      assertEquals("Playground", page.title());
      assertTrue(new String(response.body()).contains("Playground"));
    }
  }

  @Test
  void shouldWorkWithCorrectCredentialsAndMatchingOrigin() {
    server.setAuth("/empty.html", "user", "pass");
    final HttpCredentials httpCredentials = new HttpCredentials("user", "pass");
    httpCredentials.setOrigin(server.PREFIX);
    try (BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setHttpCredentials(httpCredentials))) {
      Page page = context.newPage();
      Response response = page.navigate(server.EMPTY_PAGE);
      assertEquals(200, response.status());
    }
  }

  @Test
  void shouldWorkWithCorrectCredentialsAndMatchingOriginCaseInsensitive() {
    server.setAuth("/empty.html", "user", "pass");
    final HttpCredentials httpCredentials = new HttpCredentials("user", "pass");
    httpCredentials.setOrigin(server.PREFIX.toUpperCase());
    try (BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setHttpCredentials(httpCredentials))) {
      Page page = context.newPage();
      Response response = page.navigate(server.EMPTY_PAGE);
      assertEquals(200, response.status());
    }
  }

  @Test
  @DisabledIf(value="isChromiumHeadedLike", disabledReason="fail")
  void shouldFailWithCorrectCredentialsAndWrongOriginScheme() {
    server.setAuth("/empty.html", "user", "pass");
    final HttpCredentials httpCredentials = new HttpCredentials("user", "pass");
    httpCredentials.setOrigin(Utils.generateDifferentOriginScheme(server));
    try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setHttpCredentials(httpCredentials))) {
      Page page = context.newPage();
      Response response = page.navigate(server.EMPTY_PAGE);
      assertEquals(401, response.status());
    }
  }

  @Test
  @DisabledIf(value="isChromiumHeadedLike", disabledReason="fail")
  void shouldFailWithCorrectCredentialsAndWrongOriginHostname() {
    server.setAuth("/empty.html", "user", "pass");
    final HttpCredentials httpCredentials = new HttpCredentials("user", "pass");
    httpCredentials.setOrigin(Utils.generateDifferentOriginHostname(server));
    try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setHttpCredentials(httpCredentials))) {
      Page page = context.newPage();
      Response response = page.navigate(server.EMPTY_PAGE);
      assertEquals(401, response.status());
    }
  }

  @Test
  @DisabledIf(value="isChromiumHeadedLike", disabledReason="fail")
  void shouldFailWithCorrectCredentialsAndWrongOriginPort() {
    server.setAuth("/empty.html", "user", "pass");
    final HttpCredentials httpCredentials = new HttpCredentials("user", "pass");
    httpCredentials.setOrigin(Utils.generateDifferentOriginPort(server));
    try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setHttpCredentials(httpCredentials))) {
      Page page = context.newPage();
      Response response = page.navigate(server.EMPTY_PAGE);
      assertEquals(401, response.status());
    }
  }


  @Test
  void shouldWorkWithASingleCredentialInAnArray() {
    server.setAuth("/empty.html", "user", "pass");
    try (BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setHttpCredentials(asList(new HttpCredentials("user", "pass"))))) {
      Page page = context.newPage();
      Response response = page.navigate(server.EMPTY_PAGE);
      assertEquals(200, response.status());
    }
  }

  @Test
  void shouldWorkWithMultipleCredentialsForDifferentOrigins() {
    server.setAuth("/empty.html", "user1", "pass1");
    try (BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setHttpCredentials(asList(
        new HttpCredentials("user1", "pass1").setOrigin(server.PREFIX),
        new HttpCredentials("user2", "pass2").setOrigin(server.CROSS_PROCESS_PREFIX))))) {
      Page page = context.newPage();
      Response response1 = page.navigate(server.EMPTY_PAGE);
      assertEquals(200, response1.status());
      // Wrong credentials are picked for the other origin.
      Response response2 = page.navigate(server.CROSS_PROCESS_PREFIX + "/empty.html");
      assertEquals(401, response2.status());
    }
  }

  @Test
  void shouldFallBackToCredentialsWithoutOrigin() {
    server.setAuth("/empty.html", "user", "pass");
    try (BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setHttpCredentials(asList(
        new HttpCredentials("user2", "pass2").setOrigin(server.CROSS_PROCESS_PREFIX),
        new HttpCredentials("user", "pass"))))) {
      Page page = context.newPage();
      Response response1 = page.navigate(server.EMPTY_PAGE);
      assertEquals(200, response1.status());
      // First matching entry has wrong credentials for this origin.
      Response response2 = page.navigate(server.CROSS_PROCESS_PREFIX + "/empty.html");
      assertEquals(401, response2.status());
    }
  }

  @Test
  void shouldUseTheFirstMatchingCredential() {
    server.setAuth("/empty.html", "user", "pass");
    try (BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setHttpCredentials(asList(
        new HttpCredentials("wrong", "wrong"),
        new HttpCredentials("user", "pass").setOrigin(server.PREFIX))))) {
      Page page = context.newPage();
      Response response = page.navigate(server.EMPTY_PAGE);
      assertEquals(401, response.status());
    }
  }
}
