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

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestUnrouteBehavior extends TestBase {
  @Test
  void contextUnrouteAllRemovesAllHandlers() {
    context.route("**/*", route -> {
      route.abort();
    });
    context.route("**/empty.html", route -> {
      route.abort();
    });
    context.unrouteAll();
    page.navigate(server.EMPTY_PAGE);
  }

  @Test
  void pageUnrouteAllRemovesAllRoutes() {
    page.route("**/*", route -> {
      route.abort();
    });
    page.route("**/empty.html", route -> {
      route.abort();
    });
    page.unrouteAll();
    Response response = page.navigate(server.EMPTY_PAGE);
    assertTrue(response.ok());
  }

}
