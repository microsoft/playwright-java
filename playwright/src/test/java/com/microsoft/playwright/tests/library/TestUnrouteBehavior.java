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
import com.microsoft.playwright.Route;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static com.microsoft.playwright.fixtures.Utils.async;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/library/unroute-behavior.spec.ts. */
public class TestUnrouteBehavior {
  @PlaywrightTest
  @DisplayName("RO-186 context.unroute should not wait for pending handlers to complete")
  @Fixme("java: route.fallback() called after the handler returned resumes the request (RouteImpl.shouldResumeIfFallbackIsCalled) instead of running the remaining handlers; secondHandlerCalled stays false")
  void contextUnrouteShouldNotWaitForPendingHandlersToComplete(Page page, BrowserContext context, TestServer server) throws Exception {
    AtomicBoolean secondHandlerCalled = new AtomicBoolean(false);
    context.route(Pattern.compile(".*"), route -> {
      secondHandlerCalled.set(true);
      route.resume();
    });
    CompletableFuture<Void> routePromise = new CompletableFuture<>();
    AtomicReference<Route> pendingRoute = new AtomicReference<>();
    // Java: a handler can not wait for routeBarrier, it returns and the test continues the route
    // (after `await routeBarrier`) by calling route.fallback() itself.
    Consumer<Route> handler = route -> {
      pendingRoute.set(route);
      routePromise.complete(null);
    };
    context.route(Pattern.compile(".*"), handler);
    // Java: concurrent
    CompletableFuture<Response> navigationPromise = async(() -> page.navigate(server.EMPTY_PAGE));
    page.waitForCondition(routePromise::isDone);
    context.unroute(Pattern.compile(".*"), handler);
    // continueRouteCallback();
    pendingRoute.get().fallback();
    navigationPromise.get();
    assertEquals(true, secondHandlerCalled.get());
  }

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
    CompletableFuture<Void> routePromise = new CompletableFuture<>();
    context.route(Pattern.compile(".*"), route -> {
      routePromise.complete(null);
    });
    page.route(Pattern.compile(".*"), route -> {
      route.fallback();
    });
    // Java: concurrent
    async(() -> {
      try {
        page.navigate(server.EMPTY_PAGE);
      } catch (PlaywrightException e) {
      }
      return null;
    });
    page.waitForCondition(routePromise::isDone);
    page.close();
  }

  @PlaywrightTest
  @DisplayName("RO-191 context.close should not wait for active route handlers on the owned pages")
  void contextCloseShouldNotWaitForActiveRouteHandlersOnTheOwnedPages(Page page, BrowserContext context, TestServer server) {
    CompletableFuture<Void> routePromise = new CompletableFuture<>();
    page.route(Pattern.compile(".*"), route -> {
      routePromise.complete(null);
    });
    page.route(Pattern.compile(".*"), route -> {
      route.fallback();
    });
    // Java: concurrent
    async(() -> {
      try {
        page.navigate(server.EMPTY_PAGE);
      } catch (PlaywrightException e) {
      }
      return null;
    });
    page.waitForCondition(routePromise::isDone);
    context.close();
  }

  @PlaywrightTest
  @DisplayName("RO-192 page.unroute should not wait for pending handlers to complete")
  @Fixme("java: route.fallback() called after the handler returned resumes the request (RouteImpl.shouldResumeIfFallbackIsCalled) instead of running the remaining handlers; secondHandlerCalled stays false")
  void pageUnrouteShouldNotWaitForPendingHandlersToComplete(Page page, TestServer server) throws Exception {
    AtomicBoolean secondHandlerCalled = new AtomicBoolean(false);
    page.route(Pattern.compile(".*"), route -> {
      secondHandlerCalled.set(true);
      route.resume();
    });
    CompletableFuture<Void> routePromise = new CompletableFuture<>();
    AtomicReference<Route> pendingRoute = new AtomicReference<>();
    // Java: a handler can not wait for routeBarrier, it returns and the test continues the route
    // (after `await routeBarrier`) by calling route.fallback() itself.
    Consumer<Route> handler = route -> {
      pendingRoute.set(route);
      routePromise.complete(null);
    };
    page.route(Pattern.compile(".*"), handler);
    // Java: concurrent
    CompletableFuture<Response> navigationPromise = async(() -> page.navigate(server.EMPTY_PAGE));
    page.waitForCondition(routePromise::isDone);
    page.unroute(Pattern.compile(".*"), handler);
    // continueRouteCallback();
    pendingRoute.get().fallback();
    navigationPromise.get();
    assertEquals(true, secondHandlerCalled.get());
  }

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
  void pageCloseDoesNotWaitForActiveRouteHandlers(Page page, TestServer server) throws Exception {
    AtomicBoolean secondHandlerCalled = new AtomicBoolean(false);
    page.route(Pattern.compile(".*"), route -> secondHandlerCalled.set(true));
    CompletableFuture<Void> routePromise = new CompletableFuture<>();
    page.route(Pattern.compile(".*"), route -> {
      routePromise.complete(null);
      // await new Promise(() => {}) never settles, the route stays pending.
    });
    // Java: concurrent
    async(() -> {
      try {
        page.navigate(server.EMPTY_PAGE);
      } catch (PlaywrightException e) {
      }
      return null;
    });
    page.waitForCondition(routePromise::isDone);
    page.close();
    Thread.sleep(500);
    assertEquals(false, secondHandlerCalled.get());
  }

  @PlaywrightTest
  @DisplayName("RO-197 route.continue should not throw if page has been closed")
  void routeContinueShouldNotThrowIfPageHasBeenClosed(Page page, TestServer server) throws Exception {
    CompletableFuture<Route> routePromise = new CompletableFuture<>();
    page.route(Pattern.compile(".*"), route -> {
      routePromise.complete(route);
    });
    // Java: concurrent
    async(() -> {
      try {
        page.navigate(server.EMPTY_PAGE);
      } catch (PlaywrightException e) {
      }
      return null;
    });
    page.waitForCondition(routePromise::isDone);
    Route route = routePromise.get();
    page.close();
    // Should not throw.
    route.resume();
  }

  @PlaywrightTest
  @DisplayName("RO-198 route.fallback should not throw if page has been closed")
  void routeFallbackShouldNotThrowIfPageHasBeenClosed(Page page, TestServer server) throws Exception {
    CompletableFuture<Route> routePromise = new CompletableFuture<>();
    page.route(Pattern.compile(".*"), route -> {
      routePromise.complete(route);
    });
    // Java: concurrent
    async(() -> {
      try {
        page.navigate(server.EMPTY_PAGE);
      } catch (PlaywrightException e) {
      }
      return null;
    });
    page.waitForCondition(routePromise::isDone);
    Route route = routePromise.get();
    page.close();
    // Should not throw.
    route.fallback();
  }

  @PlaywrightTest
  @DisplayName("RO-199 route.fulfill should not throw if page has been closed")
  void routeFulfillShouldNotThrowIfPageHasBeenClosed(Page page, TestServer server) throws Exception {
    CompletableFuture<Route> routePromise = new CompletableFuture<>();
    page.route(Pattern.compile(".*"), route -> {
      routePromise.complete(route);
    });
    // Java: concurrent
    async(() -> {
      try {
        page.navigate(server.EMPTY_PAGE);
      } catch (PlaywrightException e) {
      }
      return null;
    });
    page.waitForCondition(routePromise::isDone);
    Route route = routePromise.get();
    page.close();
    // Should not throw.
    route.fulfill();
  }

  // RO-200 should not continue requests in flight (page): not ported, see upstream_not_ported.txt.

  // RO-201 should not continue requests in flight (context): not ported, see upstream_not_ported.txt.
}
