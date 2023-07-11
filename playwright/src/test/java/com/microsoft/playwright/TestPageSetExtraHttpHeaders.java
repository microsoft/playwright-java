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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.microsoft.playwright.Utils.mapOf;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class TestPageSetExtraHttpHeaders extends TestBase {
  @Test
  void shouldWork() throws ExecutionException, InterruptedException {
    page.setExtraHTTPHeaders(mapOf("foo", "bar"));
    Future<Server.Request> request = server.futureRequest("/empty.html");
    page.navigate(server.EMPTY_PAGE);
    assertEquals(asList("bar"), request.get().headers.get("foo"));
    assertNull(request.get().headers.get("baz"));
  }

  @Test
  void shouldWorkWithRedirects() throws ExecutionException, InterruptedException {
    server.setRedirect("/foo.html", "/empty.html");
    page.setExtraHTTPHeaders(mapOf("foo", "bar"));
    Future<Server.Request> request = server.futureRequest("/empty.html");
    page.navigate(server.PREFIX + "/foo.html");
    assertEquals(asList("bar"), request.get().headers.get("foo"));
  }

  @Test
  void shouldWorkWithExtraHeadersFromBrowserContext() throws ExecutionException, InterruptedException {
    BrowserContext context = browser.newContext();
    context.setExtraHTTPHeaders(mapOf("foo", "bar"));
    Page page = context.newPage();
    Future<Server.Request> request = server.futureRequest("/empty.html");
    page.navigate(server.EMPTY_PAGE);
    context.close();
    assertEquals(asList("bar"), request.get().headers.get("foo"));
  }

  @Test
  void shouldOverrideExtraHeadersFromBrowserContext() throws ExecutionException, InterruptedException {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setExtraHTTPHeaders(mapOf("fOo", "bAr", "baR", "foO")));
    Page page = context.newPage();
    page.setExtraHTTPHeaders(mapOf("Foo", "Bar"));
    Future<Server.Request> request = server.futureRequest("/empty.html");
    page.navigate(server.EMPTY_PAGE);
    context.close();
    assertEquals(asList("Bar"), request.get().headers.get("foo"));
    assertEquals(asList("foO"), request.get().headers.get("bar"));
  }

  @Test
  void shouldThrowForNonStringHeaderValues() {
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      browser.newContext(new Browser.NewContextOptions().setExtraHTTPHeaders(mapOf("foo", null)));
    });
    assertTrue(e.getMessage().contains("Value cannot be null"));
  }
}
