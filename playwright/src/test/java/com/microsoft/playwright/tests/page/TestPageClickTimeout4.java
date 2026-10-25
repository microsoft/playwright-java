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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.microsoft.playwright.fixtures.Utils.rafraf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/page-click-timeout-4.spec.ts. */
public class TestPageClickTimeout4 {
  @PlaywrightTest
  @DisplayName("CL-120 should timeout waiting for stable position")
  @Fixme("java: the error message has no 'elementHandle.click: ' prefix (it starts with 'Error {' and the serialized error)")
  void shouldTimeoutWaitingForStablePosition(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/button.html");
    ElementHandle button = page.querySelector("button");
    button.evaluate("button => {\n" +
      "  button.style.transition = 'margin 5s linear 0s';\n" +
      "  button.style.marginLeft = '200px';\n" +
      "}");
    // rafraf for Firefox to kick in the animation.
    rafraf(page);
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> button.click(new ElementHandle.ClickOptions().setTimeout(3000)));
    assertTrue(error.getMessage().contains("elementHandle.click: Timeout 3000ms exceeded."));
    assertTrue(error.getMessage().contains("waiting for element to be visible, enabled and stable"));
    assertTrue(error.getMessage().contains("element is not stable"));
    assertTrue(error.getMessage().contains("retrying click action"));
  }

  @PlaywrightTest
  @DisplayName("CL-122 should fail to click the button behind a large header after scrolling around")
  void shouldFailToClickTheButtonBehindALargeHeaderAfterScrollingAround(Page page) {
    page.setViewportSize(500, 240);
    page.setContent("\n" +
      "    <style>\n" +
      "    * {\n" +
      "      padding: 0;\n" +
      "      margin: 0;\n" +
      "    }\n" +
      "    li {\n" +
      "      height: 80px;\n" +
      "      border: 1px solid black;\n" +
      "    }\n" +
      "    ol {\n" +
      "      padding-top: 160px;\n" +
      "    }\n" +
      "    div.fixed {\n" +
      "      position: fixed;\n" +
      "      z-index: 1001;\n" +
      "      width: 100%;\n" +
      "      background: rgba(255, 0, 0, 0.2);\n" +
      "      height: 2000px;\n" +
      "    }\n" +
      "    </style>\n" +
      "\n" +
      "    <div class=fixed></div>\n" +
      "\n" +
      "    <ol>\n" +
      "    <li>hi1</li><li>hi2</li><li>hi3</li><li>hi4</li><li>hi5</li><li>hi6</li><li>hi7</li><li>hi8</li>\n" +
      "    <li id=target onclick=\"window.__clicked = true\">hi9</li>\n" +
      "    <li>hi10</li><li>hi11</li><li>hi12</li><li>hi13</li><li id=li14>hi14</li>\n" +
      "    </ol>\n" +
      "\n" +
      "    <script>\n" +
      "      window.scrollTops = [];\n" +
      "      window.addEventListener('scroll', () => {\n" +
      "        window.scrollTops.push(window.scrollY);\n" +
      "      });\n" +
      "    </script>\n" +
      "  ");
    page.evalOnSelector("#li14", "e => e.scrollIntoView()");
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.click("#target", new Page.ClickOptions().setTimeout(3500)));
    assertTrue(error.getMessage().contains("<div class=\"fixed\"></div> intercepts pointer events"));
    assertNull(page.evaluate("() => window['__clicked']"));
    List<?> scrollTops = (List<?>) page.evaluate("() => window['scrollTops']");
    Set<Object> distinct = new HashSet<>(scrollTops);
    assertTrue(distinct.size() > 2);  // At least three different scroll positions.
  }
}
