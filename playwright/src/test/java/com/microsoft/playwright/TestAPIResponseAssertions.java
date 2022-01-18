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
import org.opentest4j.AssertionFailedError;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestAPIResponseAssertions extends TestBase {
  @Test
  void passWithResponse() {
    APIResponse res = page.request().get(server.EMPTY_PAGE);
    assertThat(res).isOK();
  }

  @Test
  void passWithNot() {
    APIResponse res = page.request().get(server.PREFIX + "/unknown");
    assertThat(res).not().isOK();
  }

  @Test
  void fail() {
    APIResponse res = page.request().get(server.PREFIX + "/unknown");
    boolean didThrow = false;
    try {
      assertThat(res).isOK();
    } catch (AssertionFailedError e) {
      didThrow = true;
      assertTrue(e.getMessage().contains("→ GET " + server.PREFIX + "/unknown"), "Actual error: " + e.toString());
      assertTrue(e.getMessage().contains("← 404 Not Found"), "Actual error: " + e.toString());
    }
    assertTrue(didThrow);
  }
}
