/**
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

package com.microsoft.playwright.tests.library;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/library/browsercontext-set-extra-http-headers.spec.ts. */
public class TestBrowsercontextSetExtraHttpHeaders {
  @PlaywrightTest
  @DisplayName("NE-119 should override extra headers from browser context")
  void shouldOverrideExtraHeadersFromBrowserContext(Browser browser, TestServer server) throws Exception {
    Map<String, String> extraHTTPHeaders = new HashMap<>();
    extraHTTPHeaders.put("fOo", "bAr");
    extraHTTPHeaders.put("baR", "foO");
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setExtraHTTPHeaders(extraHTTPHeaders));
    Page page = context.newPage();
    Map<String, String> pageHeaders = new HashMap<>();
    pageHeaders.put("Foo", "Bar");
    page.setExtraHTTPHeaders(pageHeaders);
    CompletableFuture<TestServer.Request> requestFuture = server.waitForRequest("/empty.html");
    page.navigate(server.EMPTY_PAGE);
    TestServer.Request request = requestFuture.get();
    context.close();
    assertEquals("Bar", request.headers.get("foo"));
    assertEquals("foO", request.headers.get("bar"));
  }

  @PlaywrightTest
  @DisplayName("NE-120 should throw for non-string header values")
  @Fixme("java: newContext() with a null header value fails with 'Value cannot be null', the Node.js client checks 'Expected value of header \"foo\" to be String, but \"object\" is found.'")
  void shouldThrowForNonStringHeaderValues(Browser browser) {
    Map<String, String> extraHTTPHeaders = new HashMap<>();
    extraHTTPHeaders.put("foo", null);
    PlaywrightException error3 = assertThrows(PlaywrightException.class,
      () -> browser.newContext(new Browser.NewContextOptions().setExtraHTTPHeaders(extraHTTPHeaders)));
    assertTrue(error3.getMessage().contains("Expected value of header \"foo\" to be String, but \"object\" is found."));
  }
}
