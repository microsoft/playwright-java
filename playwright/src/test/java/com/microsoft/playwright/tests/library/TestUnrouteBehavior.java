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

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import java.util.regex.Pattern;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/** Port of tests/library/unroute-behavior.spec.ts. */
public class TestUnrouteBehavior {
  // RO-186 context.unroute should not wait for pending handlers to complete: not ported, see upstream_not_ported.txt.

  @PlaywrightTest
  @DisplayName("RO-187 context.unrouteAll removes all handlers")
  void contextUnrouteAllRemovesAllHandlers(Page page, BrowserContext context, TestServer server) {
    context.route("**/*", route -> {
      route.abort();
    });
    context.route("**/empty.html", route -> {
      route.abort();
    });
    context.unrouteAll();
    page.navigate(server.EMPTY_PAGE);
  }

  // RO-188 context.unrouteAll should wait for pending handlers to complete: not ported, see upstream_not_ported.txt.

  // RO-189 context.unrouteAll should not wait for pending handlers to complete if behavior is ignoreErrors: not ported, see upstream_not_ported.txt.

  @PlaywrightTest
  @DisplayName("RO-190 page.close should not wait for active route handlers on the owning context")
  void pageCloseShouldNotWaitForActiveRouteHandlersOnTheOwningContext(Page page, BrowserContext context, TestServer server) {
    // it.info().annotations.push({ type: 'issue', description: 'https://github.com/microsoft/playwright/issues/23781' });
    // Java: the page is closed from the context handler, which does not handle the route, while the
    // navigation is pending; upstream closes it from the test once the handler has been called.
    context.route(Pattern.compile(".*"), route -> {
      page.close();
    });
    page.route(Pattern.compile(".*"), route -> {
      route.fallback();
    });
    try {
      page.navigate(server.EMPTY_PAGE);
    } catch (PlaywrightException e) {
      // .catch(() => {})
    }
  }

  @PlaywrightTest
  @DisplayName("RO-191 context.close should not wait for active route handlers on the owned pages")
  void contextCloseShouldNotWaitForActiveRouteHandlersOnTheOwnedPages(Page page, BrowserContext context, TestServer server) {
    // it.info().annotations.push({ type: 'issue', description: 'https://github.com/microsoft/playwright/issues/23781' });
    // Java: the context is closed from the page handler, which does not handle the route, while the
    // navigation is pending; upstream closes it from the test once the handler has been called.
    page.route(Pattern.compile(".*"), route -> {
      context.close();
    });
    page.route(Pattern.compile(".*"), route -> {
      route.fallback();
    });
    try {
      page.navigate(server.EMPTY_PAGE);
    } catch (PlaywrightException e) {
      // .catch(() => {})
    }
  }

  // RO-192 page.unroute should not wait for pending handlers to complete: not ported, see upstream_not_ported.txt.

  @PlaywrightTest
  @DisplayName("RO-193 page.unrouteAll removes all routes")
  void pageUnrouteAllRemovesAllRoutes(Page page, TestServer server) {
    page.route("**/*", route -> {
      route.abort();
    });
    page.route("**/empty.html", route -> {
      route.abort();
    });
    page.unrouteAll();
    Response response = page.navigate(server.EMPTY_PAGE);
    assertEquals(true, response.ok());
  }

  // RO-194 page.unrouteAll should wait for pending handlers to complete: not ported, see upstream_not_ported.txt.

  // RO-195 page.unrouteAll should not wait for pending handlers to complete if behavior is ignoreErrors: not ported, see upstream_not_ported.txt.

  @PlaywrightTest
  @DisplayName("RO-196 page.close does not wait for active route handlers")
  void pageCloseDoesNotWaitForActiveRouteHandlers(Page page, TestServer server) {
    // it.info().annotations.push({ type: 'issue', description: 'https://github.com/microsoft/playwright/issues/23781' });
    boolean[] secondHandlerCalled = {false};
    page.route(Pattern.compile(".*"), route -> secondHandlerCalled[0] = true);
    // Java: the page is closed from the handler, which never handles the route, while the navigation is
    // pending; upstream closes it from the test once the handler has been called.
    page.route(Pattern.compile(".*"), route -> {
      page.close();
    });
    try {
      page.navigate(server.EMPTY_PAGE);
    } catch (PlaywrightException e) {
      // .catch(() => {})
    }
    // Java: nothing is dispatched once the navigation has returned, the page is closed.
    assertFalse(secondHandlerCalled[0]);
  }

  @PlaywrightTest
  @DisplayName("RO-197 route.continue should not throw if page has been closed")
  void routeContinueShouldNotThrowIfPageHasBeenClosed(Page page, TestServer server) {
    // it.info().annotations.push({ type: 'issue', description: 'https://github.com/microsoft/playwright/issues/23781' });
    // Java: the page is closed from the route handler, while the navigation is pending; upstream closes it
    // from the test once the handler has been called.
    page.route(Pattern.compile(".*"), route -> {
      page.close();
      // Should not throw.
      route.resume();
    });
    try {
      page.navigate(server.EMPTY_PAGE);
    } catch (PlaywrightException e) {
      // .catch(() => {})
    }
  }

  @PlaywrightTest
  @DisplayName("RO-198 route.fallback should not throw if page has been closed")
  void routeFallbackShouldNotThrowIfPageHasBeenClosed(Page page, TestServer server) {
    // it.info().annotations.push({ type: 'issue', description: 'https://github.com/microsoft/playwright/issues/23781' });
    // Java: the page is closed from the route handler, while the navigation is pending; upstream closes it
    // from the test once the handler has been called.
    page.route(Pattern.compile(".*"), route -> {
      page.close();
      // Should not throw.
      route.fallback();
    });
    try {
      page.navigate(server.EMPTY_PAGE);
    } catch (PlaywrightException e) {
      // .catch(() => {})
    }
  }

  @PlaywrightTest
  @DisplayName("RO-199 route.fulfill should not throw if page has been closed")
  void routeFulfillShouldNotThrowIfPageHasBeenClosed(Page page, TestServer server) {
    // it.info().annotations.push({ type: 'issue', description: 'https://github.com/microsoft/playwright/issues/23781' });
    // Java: the page is closed from the route handler, while the navigation is pending; upstream closes it
    // from the test once the handler has been called.
    page.route(Pattern.compile(".*"), route -> {
      page.close();
      // Should not throw.
      route.fulfill();
    });
    try {
      page.navigate(server.EMPTY_PAGE);
    } catch (PlaywrightException e) {
      // .catch(() => {})
    }
  }

  // RO-200 should not continue requests in flight (page): not ported, see upstream_not_ported.txt.

  // RO-201 should not continue requests in flight (context): not ported, see upstream_not_ported.txt.
}
