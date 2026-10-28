/**
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

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;


import static com.microsoft.playwright.fixtures.Utils.browserMajorVersion;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/page/elementhandle-scroll-into-view.spec.ts. */
public class TestElementhandleScrollIntoView {
  @PlaywrightTest
  @DisplayName("EH-093 should work @smoke")
  void shouldWork(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/offscreenbuttons.html");
    for (int i = 0; i < 11; ++i) {
      ElementHandle button = page.querySelector("#btn" + i);
      Object before = button.evaluate("button => {\n" +
        "      return button.getBoundingClientRect().right - window.innerWidth;\n" +
        "    }");
      assertEquals(10 * i, before);
      button.scrollIntoViewIfNeeded();
      Object after = button.evaluate("button => {\n" +
        "      return button.getBoundingClientRect().right - window.innerWidth;\n" +
        "    }");
      assertEquals(true, ((Number) after).doubleValue() <= 0);
      page.evaluate("() => window.scrollTo(0, 0)");
    }
  }

  @PlaywrightTest
  @DisplayName("EH-094 should throw for detached element")
  void shouldThrowForDetachedElement(Page page, TestServer server) {
    page.setContent("<div>Hello</div>");
    ElementHandle div = page.querySelector("div");
    div.evaluate("div => div.remove()");
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> div.scrollIntoViewIfNeeded());
    assertTrue(error.getMessage().contains("Element is not attached to the DOM"));
  }

  @PlaywrightTest
  @DisplayName("EH-096 should scroll display:contents into view")
  void shouldScrollDisplayContentsIntoView(Page page, BrowserName browserName, Browser browser) {
    assumeFalse(browserName == BrowserName.CHROMIUM && browserMajorVersion(browser) < 105, "Needs https://chromium-review.googlesource.com/c/chromium/src/+/3758670");

    page.setContent("\n" +
      "    <style>\n" +
      "      html, body { margin: 0; padding: 0; width: 100%; height: 100%; }\n" +
      "      ::-webkit-scrollbar { display: none; }\n" +
      "      * { scrollbar-width: none; }\n" +
      "    </style>\n" +
      "    <div id=container style=\"width:200px;height:200px;overflow:scroll;border:1px solid black;\">\n" +
      "      <div style=\"margin-top:500px;background:red;\">\n" +
      "        <div style=\"height:50px;width:100px;background:cyan;\">\n" +
      "          <div id=target style=\"display:contents\">Hello</div>\n" +
      "        </div>\n" +
      "      <div>\n" +
      "    </div>\n" +
      "  ");
    ElementHandle div = page.querySelector("#target");
    div.scrollIntoViewIfNeeded();
    Object scrollTop = page.evalOnSelector("#container", "e => e.scrollTop");
    // On Android the value is not exact due to various scale conversions.
    assertTrue(Math.abs(((Number) scrollTop).doubleValue() - 350) < 1);
  }

  @PlaywrightTest
  @DisplayName("EH-097 should work for visibility:hidden element")
  void shouldWorkForVisibilityHiddenElement(Page page) {
    page.setContent("<div style=\"visibility:hidden\">Hello</div>");
    ElementHandle div = page.querySelector("div");
    div.scrollIntoViewIfNeeded();
  }

  @PlaywrightTest
  @DisplayName("EH-098 should work for zero-sized element")
  void shouldWorkForZeroSizedElement(Page page) {
    page.setContent("<div style=\"height:0\">Hello</div>");
    ElementHandle div = page.querySelector("div");
    div.scrollIntoViewIfNeeded();
  }

  @PlaywrightTest
  @DisplayName("EH-101 should timeout waiting for visible")
  void shouldTimeoutWaitingForVisible(Page page, TestServer server) {
    page.setContent("<div style=\"display:none\">Hello</div>");
    ElementHandle div = page.querySelector("div");
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> div.scrollIntoViewIfNeeded(new ElementHandle.ScrollIntoViewIfNeededOptions().setTimeout(3000)));
    assertTrue(error.getMessage().contains("element is not visible"));
    assertTrue(error.getMessage().contains("retrying scroll into view action"));
  }
}
