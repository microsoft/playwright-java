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

package com.microsoft.playwright.tests.page;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import org.junit.jupiter.api.DisplayName;

import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** Port of tests/page/page-request-gc.spec.ts. */
public class TestPageRequestGc {
  @PlaywrightTest
  @DisplayName("RO-149 should work")
  void shouldWork(Page page) {
    page.evaluate("() => {\n" +
      "  globalThis.objectToDestroy = { hello: 'world' };\n" +
      "  globalThis.weakRef = new WeakRef(globalThis.objectToDestroy);\n" +
      "}");

    page.requestGC();
    assertEquals(mapOf("hello", "world"), page.evaluate("() => globalThis.weakRef.deref()"));

    page.requestGC();
    assertEquals(mapOf("hello", "world"), page.evaluate("() => globalThis.weakRef.deref()"));

    page.evaluate("() => globalThis.objectToDestroy = null");
    page.requestGC();
    assertNull(page.evaluate("() => globalThis.weakRef.deref()"));
  }

  @PlaywrightTest
  @DisplayName("RO-150 should collect element retained by locator hit-target interceptor after detach")
  @DisabledOnBrowser(value = BrowserName.FIREFOX, reason = "fixme: Firefox hit-target interceptor does not release its element reference on detach")
  @DisabledOnBrowser(value = BrowserName.CHROMIUM, reason = "fixme: Chromium retains a native reference to the last hit-tested element; fix pending upstream discussion, see #41575")
  void shouldCollectElementRetainedByLocatorHitTargetInterceptorAfterDetach(Page page, BrowserName browserName) {
    page.setContent("<button id=\"btn\">click me</button>");
    page.locator("#btn").click();
    page.evaluate("() => {\n" +
      "  globalThis.weakRef = new WeakRef(document.getElementById('btn'));\n" +
      "  document.getElementById('btn').remove();\n" +
      "}");
    page.requestGC();
    assertNull(page.evaluate("() => globalThis.weakRef.deref()"));
  }
}
