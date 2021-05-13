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
    getServer().setAuth("/empty.html", "user", "pass");
    BrowserContext context = getBrowser().newContext();
    Page page = context.newPage();
    Response response = page.navigate(getServer().EMPTY_PAGE);
    assertEquals(401, response.status());
    context.close();
  }

  void shouldWorkWithSetHTTPCredentials() {
    // The method is not exposed in Java.
  }

  @Test
  void shouldWorkWithCorrectCredentials() {
    getServer().setAuth("/empty.html", "user", "pass");
    BrowserContext context = getBrowser().newContext(new Browser.NewContextOptions()
      .setHttpCredentials("user", "pass"));
    Page page = context.newPage();
    Response response = page.navigate(getServer().EMPTY_PAGE);
    assertEquals(200, response.status());
    context.close();
  }

  @Test
  void shouldFailWithWrongCredentials() {
    getServer().setAuth("/empty.html", "user", "pass");
    BrowserContext context = getBrowser().newContext(new Browser.NewContextOptions().setHttpCredentials("foo", "bar"));
    Page page = context.newPage();
    Response response = page.navigate(getServer().EMPTY_PAGE);
    assertEquals(401, response.status());
    context.close();
  }

  @Test
  void shouldReturnResourceBody() {
    getServer().setAuth("/playground.html", "user", "pass");
    BrowserContext context = getBrowser().newContext(new Browser.NewContextOptions()
      .setHttpCredentials("user", "pass"));
    Page page = context.newPage();
    Response response = page.navigate(getServer().PREFIX + "/playground.html");
    assertEquals(200, response.status());
    assertEquals("Playground", page.title());
    assertTrue(new String(response.body()).contains("Playground"));
    context.close();
  }
}
