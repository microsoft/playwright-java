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

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/page-click-timeout-3.spec.ts. */
public class TestPageClickTimeout3 {
  @PlaywrightTest
  @DisplayName("CL-117 should timeout waiting for hit target")
  @Fixme("java: the error message has no 'elementHandle.click: ' prefix (it starts with 'Error {' and the serialized error)")
  void shouldTimeoutWaitingForHitTarget(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/button.html");
    ElementHandle button = page.querySelector("button");
    page.evaluate("() => {\n" +
      "  document.body.style.position = 'relative';\n" +
      "  const blocker = document.createElement('div');\n" +
      "  blocker.id = 'blocker';\n" +
      "  blocker.style.position = 'absolute';\n" +
      "  blocker.style.width = '400px';\n" +
      "  blocker.style.height = '20px';\n" +
      "  blocker.style.left = '0';\n" +
      "  blocker.style.top = '0';\n" +
      "  document.body.appendChild(blocker);\n" +
      "}");
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> button.click(new ElementHandle.ClickOptions().setTimeout(5000)));
    assertTrue(error.getMessage().contains("elementHandle.click: Timeout 5000ms exceeded."));
    assertTrue(error.getMessage().contains("<div id=\"blocker\"></div> intercepts pointer events"));
    assertTrue(error.getMessage().contains("retrying click action"));
    assertTrue(error.getMessage().contains("waiting 500ms"));
  }

  @PlaywrightTest
  @DisplayName("CL-118 should still click when force but hit target is obscured")
  void shouldStillClickWhenForceButHitTargetIsObscured(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/button.html");
    ElementHandle button = page.querySelector("button");
    page.evaluate("() => {\n" +
      "  document.body.style.position = 'relative';\n" +
      "  const blocker = document.createElement('div');\n" +
      "  blocker.id = 'blocker';\n" +
      "  blocker.style.position = 'absolute';\n" +
      "  blocker.style.width = '400px';\n" +
      "  blocker.style.height = '200px';\n" +
      "  blocker.style.left = '0';\n" +
      "  blocker.style.top = '0';\n" +
      "  document.body.appendChild(blocker);\n" +
      "}");
    button.click(new ElementHandle.ClickOptions().setForce(true));
  }

  @PlaywrightTest
  @DisplayName("CL-119 should report wrong hit target subtree")
  @Fixme("java: the error message has no 'elementHandle.click: ' prefix (it starts with 'Error {' and the serialized error)")
  void shouldReportWrongHitTargetSubtree(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/button.html");
    ElementHandle button = page.querySelector("button");
    page.evaluate("() => {\n" +
      "  document.body.style.position = 'relative';\n" +
      "\n" +
      "  const blocker = document.createElement('div');\n" +
      "  blocker.id = 'blocker';\n" +
      "  blocker.style.position = 'absolute';\n" +
      "  blocker.style.width = '400px';\n" +
      "  blocker.style.height = '20px';\n" +
      "  blocker.style.left = '0';\n" +
      "  blocker.style.top = '0';\n" +
      "  document.body.appendChild(blocker);\n" +
      "\n" +
      "  const inner = document.createElement('div');\n" +
      "  inner.id = 'inner';\n" +
      "  inner.style.position = 'absolute';\n" +
      "  inner.style.left = '0';\n" +
      "  inner.style.top = '0';\n" +
      "  inner.style.right = '0';\n" +
      "  inner.style.bottom = '0';\n" +
      "  blocker.appendChild(inner);\n" +
      "}");
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> button.click(new ElementHandle.ClickOptions().setTimeout(5000)));
    assertTrue(error.getMessage().contains("elementHandle.click: Timeout 5000ms exceeded."));
    assertTrue(error.getMessage().contains("<div id=\"inner\"></div> from <div id=\"blocker\">…</div> subtree intercepts pointer events"));
    assertTrue(error.getMessage().contains("retrying click action"));
  }
}
