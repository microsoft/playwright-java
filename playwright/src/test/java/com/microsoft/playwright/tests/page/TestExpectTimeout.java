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

package com.microsoft.playwright.tests.page;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;
import org.opentest4j.AssertionFailedError;


import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.microsoft.playwright.fixtures.Utils.stripAnsi;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/expect-timeout.spec.ts. */
public class TestExpectTimeout {
  @PlaywrightTest
  @DisplayName("EX-244 should print element not found")
  @Fixme("java: the assertion error has another format: no 'expect(locator).toHaveText(expected) failed' header, no 'Locator:' and 'Expected:' lines")
  void shouldPrintElementNotFound(Page page) {
    page.setContent("<div id=node>Text content</div>");
    AssertionFailedError error = assertThrows(AssertionFailedError.class,
      () -> assertThat(page.locator("no-such-thing")).hasText("hey", new LocatorAssertions.HasTextOptions().setTimeout(1000)));
    assertTrue(stripAnsi(error.getMessage()).contains("expect(locator).toHaveText(expected) failed\n" +
      "\n" +
      "Locator: locator('no-such-thing')\n" +
      "Expected: \"hey\"\n" +
      "Timeout: 1000ms\n" +
      "Error: element(s) not found\n" +
      "\n" +
      "Call log:\n"));
  }

  @PlaywrightTest
  @DisplayName("EX-245 should print timed out error message when value does not match")
  @Fixme("java: the assertion error has another format: no 'expect(locator).toHaveText(expected) failed' header, no 'Locator:' and 'Expected:' lines")
  void shouldPrintTimedOutErrorMessageWhenValueDoesNotMatch(Page page) {
    page.setContent("<div id=node>Text content</div>");
    AssertionFailedError error = assertThrows(AssertionFailedError.class,
      () -> assertThat(page.locator("div")).hasText("hey", new LocatorAssertions.HasTextOptions().setTimeout(1000)));
    assertTrue(stripAnsi(error.getMessage()).contains("expect(locator).toHaveText(expected) failed\n" +
      "\n" +
      "Locator:  locator('div')\n" +
      "Expected: \"hey\"\n" +
      "Received: \"Text content\"\n" +
      "Timeout:  1000ms\n" +
      "\n" +
      "Call log:\n"));
  }

  @PlaywrightTest
  @DisplayName("EX-246 should print timed out error message with impossible timeout")
  @Fixme("java: the assertion error has another format: no 'expect(locator).toHaveText(expected) failed' header, no 'Locator:' and 'Expected:' lines")
  void shouldPrintTimedOutErrorMessageWithImpossibleTimeout(Page page) {
    page.setContent("<div id=node>Text content</div>");
    AssertionFailedError error = assertThrows(AssertionFailedError.class,
      () -> assertThat(page.locator("no-such-thing")).hasText("hey", new LocatorAssertions.HasTextOptions().setTimeout(1)));
    assertTrue(stripAnsi(error.getMessage()).contains("expect(locator).toHaveText(expected) failed\n" +
      "\n" +
      "Locator: locator('no-such-thing')\n" +
      "Expected: \"hey\"\n" +
      "Timeout: 1ms\n" +
      "Error: element(s) not found\n" +
      "\n" +
      "Call log:"));
  }

  @PlaywrightTest
  @DisplayName("EX-247 should print timed out error message when value does not match with impossible timeout")
  @Fixme("java: the assertion error has another format: no 'expect(locator).toHaveText(expected) failed' header, no 'Locator:' and 'Expected:' lines")
  void shouldPrintTimedOutErrorMessageWhenValueDoesNotMatchWithImpossibleTimeout(Page page) {
    page.setContent("<div id=node>Text content</div>");
    AssertionFailedError error = assertThrows(AssertionFailedError.class,
      () -> assertThat(page.locator("div")).hasText("hey", new LocatorAssertions.HasTextOptions().setTimeout(1)));
    assertTrue(stripAnsi(error.getMessage()).contains("expect(locator).toHaveText(expected) failed\n" +
      "\n" +
      "Locator:  locator('div')\n" +
      "Expected: \"hey\"\n" +
      "Received: \"Text content\"\n" +
      "Timeout:  1ms\n" +
      "\n" +
      "Call log:\n"));
  }

  @PlaywrightTest
  @DisplayName("EX-248 should have timeout error name")
  void shouldHaveTimeoutErrorName(Page page) {
    assertThrows(TimeoutError.class, () -> page.waitForSelector("#not-found", new Page.WaitForSelectorOptions().setTimeout(1)));
  }

  // EX-249 should not throw when navigating during one-shot check: not ported, see upstream_not_ported.txt.

  // EX-250 should not throw when navigating during first locator handler check: not ported, see upstream_not_ported.txt.

  @PlaywrightTest
  @DisplayName("EX-251 should timeout during first locator handler check")
  @Fixme("java: the assertion error has another format: no 'expect(locator).toHaveText(expected) failed' header, no 'Locator:' and 'Expected:' lines")
  void shouldTimeoutDuringFirstLocatorHandlerCheck(Page page, TestServer server) {
    page.addLocatorHandler(page.locator("div"), locator -> {});
    page.setContent("<div>hello</div><span>bye</span>");
    AssertionFailedError error = assertThrows(AssertionFailedError.class,
      () -> assertThat(page.locator("span")).hasText("bye", new LocatorAssertions.HasTextOptions().setTimeout(3000)));
    assertTrue(stripAnsi(error.getMessage()).contains("expect(locator).toHaveText(expected) failed\n" +
      "\n" +
      "Locator:  locator('span')\n" +
      "Expected: \"bye\"\n" +
      "Received: \"\"\n" +
      "Timeout:  3000ms\n" +
      "\n" +
      "Call log:\n"));
    assertTrue(error.getMessage().contains("locator handler has finished, waiting for locator('div') to be hidden"));
    assertTrue(error.getMessage().contains("locator resolved to visible <div>hello</div>"));
  }

  @PlaywrightTest
  @DisplayName("EX-252 should not miss element that appears between retries before the deadline")
  void shouldNotMissElementThatAppearsBetweenRetriesBeforeTheDeadline(Page page) {
    page.setContent("<div id=\"target\" style=\"display:none\">content</div>");
    page.evaluate("() => {\n" +
      "    window.builtins.setTimeout(() => {\n" +
      "      document.getElementById('target').style.display = 'block';\n" +
      "    }, 1500);\n" +
      "  }");
    assertThat(page.locator("#target")).isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(1800));
  }

  // EX-253 .. EX-258 (the signal option): not ported, see upstream_not_ported.txt.
}
