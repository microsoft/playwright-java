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
import com.microsoft.playwright.Response;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;


import static org.junit.jupiter.api.Assertions.assertEquals;

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

  // RO-190 page.close should not wait for active route handlers on the owning context: not ported, see upstream_not_ported.txt.

  // RO-191 context.close should not wait for active route handlers on the owned pages: not ported, see upstream_not_ported.txt.

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

  // RO-196 page.close does not wait for active route handlers: not ported, see upstream_not_ported.txt.

  // RO-197 route.continue should not throw if page has been closed: not ported, see upstream_not_ported.txt.

  // RO-198 route.fallback should not throw if page has been closed: not ported, see upstream_not_ported.txt.

  // RO-199 route.fulfill should not throw if page has been closed: not ported, see upstream_not_ported.txt.

  // RO-200 should not continue requests in flight (page): not ported, see upstream_not_ported.txt.

  // RO-201 should not continue requests in flight (context): not ported, see upstream_not_ported.txt.
}
