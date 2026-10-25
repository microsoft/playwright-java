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

import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/** Port of tests/page/page-event-popup.spec.ts. */
public class TestPageEventPopup {
  @PlaywrightTest
  @DisplayName("PO-001 should work @smoke")
  void shouldWork(Page page) {
    Page popup = page.waitForPopup(() -> {
      page.evaluate("() => window['__popup'] = window.open('about:blank')");
    });
    assertEquals(false, page.evaluate("() => !!window.opener"));
    assertEquals(true, popup.evaluate("() => !!window.opener"));
  }

  @PlaywrightTest
  @DisplayName("PO-002 should work with window features")
  void shouldWorkWithWindowFeatures(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    Page popup = page.waitForPopup(() -> {
      page.evaluate("() => window['__popup'] = window.open(window.location.href, 'Title', 'toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=yes,width=780,height=200,top=0,left=0')");
    });
    assertEquals(false, page.evaluate("() => !!window.opener"));
    assertEquals(true, popup.evaluate("() => !!window.opener"));
  }

  @PlaywrightTest
  @DisplayName("PO-003 should emit for immediately closed popups")
  void shouldEmitForImmediatelyClosedPopups(Page page) {
    Page popup = page.waitForPopup(() -> {
      page.evaluate("() => {\n" +
        "  const win = window.open('about:blank');\n" +
        "  win.close();\n" +
        "}");
    });
    assertNotNull(popup);
  }

  @PlaywrightTest
  @DisplayName("PO-004 should emit for immediately closed popups 2")
  void shouldEmitForImmediatelyClosedPopups2(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    Page popup = page.waitForPopup(() -> {
      page.evaluate("() => {\n" +
        "  const win = window.open(window.location.href);\n" +
        "  win.close();\n" +
        "}");
    });
    assertNotNull(popup);
  }

  @PlaywrightTest
  @DisplayName("PO-006 should work with empty url")
  void shouldWorkWithEmptyUrl(Page page) {
    Page popup = page.waitForPopup(() -> {
      page.evaluate("() => window['__popup'] = window.open('')");
    });
    assertEquals(false, page.evaluate("() => !!window.opener"));
    assertEquals(true, popup.evaluate("() => !!window.opener"));
  }

  @PlaywrightTest
  @DisplayName("PO-007 should work with noopener and no url")
  void shouldWorkWithNoopenerAndNoUrl(Page page) {
    Page popup = page.waitForPopup(() -> {
      page.evaluate("() => window['__popup'] = window.open(undefined, null, 'noopener')");
    });
    // Chromium reports `about:blank#blocked` here.
    assertEquals("about:blank", popup.url().split("#")[0]);
    assertEquals(false, page.evaluate("() => !!window.opener"));
    assertEquals(false, popup.evaluate("() => !!window.opener"));
  }

  @PlaywrightTest
  @DisplayName("PO-008 should work with noopener and about:blank")
  void shouldWorkWithNoopenerAndAboutBlank(Page page) {
    Page popup = page.waitForPopup(() -> {
      page.evaluate("() => window['__popup'] = window.open('about:blank', null, 'noopener')");
    });
    assertEquals(false, page.evaluate("() => !!window.opener"));
    assertEquals(false, popup.evaluate("() => !!window.opener"));
  }

  @PlaywrightTest
  @DisplayName("PO-009 should work with noopener and url")
  void shouldWorkWithNoopenerAndUrl(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    Page popup = page.waitForPopup(() -> {
      page.evaluate("url => window['__popup'] = window.open(url, null, 'noopener')", server.EMPTY_PAGE);
    });
    assertEquals(false, page.evaluate("() => !!window.opener"));
    assertEquals(false, popup.evaluate("() => !!window.opener"));
  }

  @PlaywrightTest
  @DisplayName("PO-010 should work with clicking target=_blank")
  void shouldWorkWithClickingTargetBlank(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<a target=_blank rel=\"opener\" href=\"/one-style.html\">yo</a>");
    Page popup = page.waitForPopup(() -> {
      page.click("a");
    });
    assertEquals(false, page.evaluate("() => !!window.opener"));
    assertEquals(true, popup.evaluate("() => !!window.opener"));
    assertEquals(popup, popup.mainFrame().page());
  }

  @PlaywrightTest
  @DisplayName("PO-011 should work with fake-clicking target=_blank and rel=noopener")
  void shouldWorkWithFakeClickingTargetBlankAndRelNoopener(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<a target=_blank rel=noopener href=\"/one-style.html\">yo</a>");
    Page popup = page.waitForPopup(() -> {
      page.evalOnSelector("a", "a => a.click()");
    });
    assertEquals(false, page.evaluate("() => !!window.opener"));
    assertEquals(false, popup.evaluate("() => !!window.opener"));
  }

  @PlaywrightTest
  @DisplayName("PO-012 should work with clicking target=_blank and rel=noopener")
  void shouldWorkWithClickingTargetBlankAndRelNoopener(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<a target=_blank rel=noopener href=\"/one-style.html\">yo</a>");
    Page popup = page.waitForPopup(() -> {
      page.click("a");
    });
    assertEquals(false, page.evaluate("() => !!window.opener"));
    assertEquals(false, popup.evaluate("() => !!window.opener"));
  }

  @PlaywrightTest
  @DisplayName("PO-013 should report popup opened from iframes")
  void shouldReportPopupOpenedFromIframes(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/frames/two-frames.html");
    Frame frame = page.frames().get(1);
    assertNotNull(frame);
    Page popup = page.waitForPopup(() -> {
      frame.evaluate("() => window.open('')");
    });
    assertNotNull(popup);
  }
}
