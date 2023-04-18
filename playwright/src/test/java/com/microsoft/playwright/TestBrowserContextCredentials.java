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

import com.microsoft.playwright.options.HttpCredentials;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestBrowserContextCredentials extends TestBase {

  static boolean isChromiumHeadful() {
    return isChromium() && isHeadful();
  }

  @Test
  @DisabledIf(value="isChromiumHeadful", disabledReason="fail")
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
}
