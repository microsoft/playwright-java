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

package com.microsoft.playwright.tests.page;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static com.microsoft.playwright.fixtures.Utils.browserMajorVersion;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/page/page-set-extra-http-headers.spec.ts. */
public class TestPageSetExtraHttpHeaders {
  @PlaywrightTest
  @DisplayName("NE-114 should work @smoke")
  void shouldWork(Page page, TestServer server) throws Exception {
    page.setExtraHTTPHeaders(Collections.singletonMap("foo", "bar"));
    CompletableFuture<TestServer.Request> requestPromise = server.waitForRequest("/empty.html");
    page.navigate(server.EMPTY_PAGE);
    TestServer.Request request = requestPromise.get();
    assertEquals("bar", request.headers.get("foo"));
    assertNull(request.headers.get("baz"));
  }

  @PlaywrightTest
  @DisplayName("NE-115 should work with redirects")
  void shouldWorkWithRedirects(Page page, TestServer server) throws Exception {
    server.setRedirect("/foo.html", "/empty.html");
    page.setExtraHTTPHeaders(Collections.singletonMap("foo", "bar"));
    CompletableFuture<TestServer.Request> requestPromise = server.waitForRequest("/empty.html");
    page.navigate(server.PREFIX + "/foo.html");
    TestServer.Request request = requestPromise.get();
    assertEquals("bar", request.headers.get("foo"));
  }

  @PlaywrightTest
  @DisplayName("NE-116 should work with extra headers from browser context")
  void shouldWorkWithExtraHeadersFromBrowserContext(Page page, TestServer server) throws Exception {
    page.context().setExtraHTTPHeaders(Collections.singletonMap("foo", "bar"));
    CompletableFuture<TestServer.Request> requestPromise = server.waitForRequest("/empty.html");
    page.navigate(server.EMPTY_PAGE);
    TestServer.Request request = requestPromise.get();
    assertEquals("bar", request.headers.get("foo"));
  }

  @PlaywrightTest
  @DisplayName("NE-118 should not duplicate referer header")
  void shouldNotDuplicateRefererHeader(Page page, TestServer server, BrowserName browserName, Browser browser) {
    assumeFalse(browserName == BrowserName.CHROMIUM && browserMajorVersion(browser) < 154, "fail: Request has referer and Referer");
    page.setExtraHTTPHeaders(Collections.singletonMap("referer", server.EMPTY_PAGE));
    Response response = page.navigate(server.EMPTY_PAGE);
    assertEquals(true, response.ok());
    assertEquals(server.EMPTY_PAGE, response.request().headers().get("referer"));
  }
}
