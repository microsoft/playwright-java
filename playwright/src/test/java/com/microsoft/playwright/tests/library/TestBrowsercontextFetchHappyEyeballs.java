/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
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

package com.microsoft.playwright.tests.library;

import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/library/browsercontext-fetch-happy-eyeballs.spec.ts. */
public class TestBrowsercontextFetchHappyEyeballs {
  // FE-185 get should work: not ported, see upstream_not_ported.txt.
  // FE-186 get should work on request fixture: not ported, see upstream_not_ported.txt.
  // FE-187 https post should work with ignoreHTTPSErrors option: not ported, see upstream_not_ported.txt.

  @PlaywrightTest
  @DisplayName("FE-188 should work with ip6 and port as the host")
  void shouldWorkWithIp6AndPortAsTheHost(Playwright playwright, TestServer server) {
    // Java: the `request` fixture is a context of playwright.request(), disposed at the end of the test.
    APIRequestContext request = playwright.request().newContext();
    try {
      String insideDocker = System.getenv("INSIDE_DOCKER");
      assumeFalse(insideDocker != null && !insideDocker.isEmpty(), "docker does not support IPv6 by default");
      APIResponse response = request.get("http://[::1]:" + server.PORT + "/simple.json");
      assertEquals("http://[::1]:" + server.PORT + "/simple.json", response.url());
      assertThat(response).isOK();
    } finally {
      request.dispose();
    }
  }
}
