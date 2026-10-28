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
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.Fixture;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** Port of tests/library/locator-dispatchevent-touch.spec.ts. */
public class TestLocatorDispatcheventTouch {
  @Fixture
  Browser.NewContextOptions contextOptions(Browser.NewContextOptions contextOptions) {
    return contextOptions.setHasTouch(true);
  }

  @PlaywrightTest
  @DisplayName("TA-010 should support touch points in touch event arguments")
  void shouldSupportTouchPointsInTouchEventArguments(Page page, TestServer server, BrowserName browserName) {
    page.navigate(server.EMPTY_PAGE);
    page.setContent("\n" +
      "      <div data-testid='outer' style=\"position: absolute; width: 120px; height: 120px; background-color: red;\">\n" +
      "        <div data-testid='inner' style=\"position: absolute; width: 100px; height: 100px; top: 10px; left: 10px; background-color: green; z-index: 3;\">inner</div>\n" +
      "      </div>");
    Locator outer = page.getByTestId("outer");
    outer.evaluate("(el) => {\n" +
      "  const events = [];\n" +
      "  window.events = events;\n" +
      "  el.addEventListener('touchstart', (e) => events.push('touchstart: ' + [...e.touches].map(t => `${t.constructor.name}(id: ${t.identifier}, clientX: ${t.clientX}, clientY: ${t.clientY})`)));\n" +
      "  el.addEventListener('touchmove', (e) => events.push('touchmove: ' + [...e.touches].map(t => `${t.constructor.name}(id: ${t.identifier}, clientX: ${t.clientX}, clientY: ${t.clientY})`)));\n" +
      "  el.addEventListener('touchend', (e) => events.push('touchend: ' + [...e.touches].map(t => `${t.constructor.name}(id: ${t.identifier}, clientX: ${t.clientX}, clientY: ${t.clientY})`)));\n" +
      "}");

    List<Map<String, Object>> touches = asList(
      mapOf("identifier", 0, "clientX", 61, "clientY", 60),
      mapOf("identifier", 1, "clientX", 59, "clientY", 60));
    Locator inner = page.getByTestId("inner");
    inner.dispatchEvent("touchstart", mapOf(
      "touches", touches,
      "changedTouches", touches,
      "targetTouches", touches));
    inner.dispatchEvent("touchmove", mapOf(
      "touches", touches,
      "changedTouches", touches,
      "targetTouches", touches));
    inner.dispatchEvent("touchend", mapOf(
      "touches", new ArrayList<Object>(),
      "changedTouches", touches,
      "targetTouches", new ArrayList<Object>()));
    assertEquals(asList(
      "touchstart: Touch(id: 0, clientX: 61, clientY: 60),Touch(id: 1, clientX: 59, clientY: 60)",
      "touchmove: Touch(id: 0, clientX: 61, clientY: 60),Touch(id: 1, clientX: 59, clientY: 60)",
      "touchend: "), page.evaluate("() => window.events"));
  }
}
