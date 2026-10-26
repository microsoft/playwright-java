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

import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.DisplayName;

import java.net.URI;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/locator-frame.spec.ts. */
public class TestLocatorFrame {
  private static Map<String, String> stringMap(String... keysAndValues) {
    Map<String, String> map = new LinkedHashMap<>();
    for (int i = 0; i < keysAndValues.length; i += 2) {
      map.put(keysAndValues[i], keysAndValues[i + 1]);
    }
    return map;
  }

  private static void fulfill(Route route, String body) {
    try {
      route.fulfill(new Route.FulfillOptions().setBody(body).setContentType("text/html"));
    } catch (PlaywrightException e) {
      // .catch(() => {})
    }
  }

  private static void routeIframe(Page page) {
    page.route("**/empty.html", route -> {
      fulfill(route, "<iframe src=\"iframe.html\" name=\"frame1\"></iframe>");
    });
    page.route("**/iframe.html", route -> {
      fulfill(route, "\n" +
        "        <html>\n" +
        "          <div>\n" +
        "            <button data-testid=\"buttonId\">Hello iframe</button>\n" +
        "            <iframe src=\"iframe-2.html\"></iframe>\n" +
        "          </div>\n" +
        "          <span>1</span>\n" +
        "          <span>2</span>\n" +
        "          <label for=target>Name</label><input id=target type=text placeholder=Placeholder title=Title alt=Alternative>\n" +
        "        </html>");
    });
    page.route("**/iframe-2.html", route -> {
      fulfill(route, "<html><button>Hello nested iframe</button></html>");
    });
  }

  private static void routeAmbiguous(Page page) {
    page.route("**/empty.html", route -> {
      fulfill(route, "<iframe src=\"iframe-1.html\"></iframe>\n" +
        "             <iframe src=\"iframe-2.html\"></iframe>\n" +
        "             <iframe src=\"iframe-3.html\"></iframe>");
    });
    page.route("**/iframe-*", route -> {
      String path = URI.create(route.request().url()).getPath().substring(1);
      fulfill(route, "<html><button>Hello from " + path + "</button></html>");
    });
  }

  @PlaywrightTest
  @DisplayName("LO-089 should work for iframe @smoke")
  void shouldWorkForIframe(Page page, TestServer server) {
    routeIframe(page);
    page.navigate(server.EMPTY_PAGE);
    Locator button = page.frameLocator("iframe").locator("button");
    button.waitFor();
    assertEquals("Hello iframe", button.innerText());
    assertThat(button).hasText("Hello iframe");
    button.click();
  }

  @PlaywrightTest
  @DisplayName("LO-090 should work for nested iframe")
  void shouldWorkForNestedIframe(Page page, TestServer server) {
    routeIframe(page);
    page.navigate(server.EMPTY_PAGE);
    Locator button = page.frameLocator("iframe").frameLocator("iframe").locator("button");
    button.waitFor();
    assertEquals("Hello nested iframe", button.innerText());
    assertThat(button).hasText("Hello nested iframe");
    button.click();
  }

  @PlaywrightTest
  @DisplayName("LO-091 should work for $ and $$")
  void shouldWorkForAnd(Page page, TestServer server) {
    routeIframe(page);
    page.navigate(server.EMPTY_PAGE);
    Locator locator = page.frameLocator("iframe").locator("button");
    assertThat(locator).hasText("Hello iframe");
    Locator spans = page.frameLocator("iframe").locator("span");
    assertThat(spans).hasCount(2);
  }

  @PlaywrightTest
  @DisplayName("LO-092 should wait for frame")
  @Fixme("java: the log says 'waiting for locator(\"body\").locator(\"iframe\").contentFrame().locator(\"span\")', with double quotes")
  void shouldWaitForFrame(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.locator("body").frameLocator("iframe").locator("span").click(new Locator.ClickOptions().setTimeout(1000)));
    assertTrue(error.getMessage().contains("waiting for locator('body').locator('iframe').contentFrame()"));
  }

  // LO-093 should wait for frame 2: not ported, see upstream_not_ported.txt.

  @PlaywrightTest
  @DisplayName("LO-094 should wait for frame to go")
  void shouldWaitForFrameToGo(Page page, TestServer server) throws Exception {
    routeIframe(page);
    page.navigate(server.EMPTY_PAGE);
    page.waitForTimeout(300);
    try {
      page.evalOnSelector("iframe", "e => e.remove()");
    } catch (PlaywrightException e) {
      // .catch(() => {})
    }
    // Java: the assertion stays satisfied once the iframe is gone, so it runs after the removal.
    assertThat(page.frameLocator("iframe").locator("button")).isHidden();
  }

  @PlaywrightTest
  @DisplayName("LO-095 should not wait for frame")
  void shouldNotWaitForFrame(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    assertThat(page.frameLocator("iframe").locator("span")).isHidden();
  }

  @PlaywrightTest
  @DisplayName("LO-096 should not wait for frame 2")
  void shouldNotWaitForFrame2(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    assertThat(page.frameLocator("iframe").locator("span")).not().isVisible();
  }

  @PlaywrightTest
  @DisplayName("LO-097 should not wait for frame 3")
  void shouldNotWaitForFrame3(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    assertThat(page.frameLocator("iframe").locator("span")).hasCount(0);
  }

  // LO-098 should click in lazy iframe: not ported, see upstream_not_ported.txt.

  @PlaywrightTest
  @DisplayName("LO-099 waitFor should survive frame reattach")
  void waitForShouldSurviveFrameReattach(Page page, TestServer server) throws Exception {
    routeIframe(page);
    page.navigate(server.EMPTY_PAGE);
    Locator button = page.frameLocator("iframe").locator("button:has-text(\"Hello nested iframe\")");
    page.locator("iframe").evaluate("e => e.remove()");
    page.evaluate("() => {\n" +
      "  const iframe = document.createElement('iframe');\n" +
      "  iframe.src = 'iframe-2.html';\n" +
      "  document.body.appendChild(iframe);\n" +
      "}");
    // Java: the wait stays satisfied once the button is there, so it runs after the reattach.
    button.waitFor();
  }

  // LO-100 click should survive frame reattach: not ported, see upstream_not_ported.txt.

  // LO-101 click should survive iframe navigation: not ported, see upstream_not_ported.txt.

  @PlaywrightTest
  @DisplayName("LO-102 should non work for non-frame")
  void shouldNonWorkForNonFrame(Page page, TestServer server) {
    routeIframe(page);
    page.setContent("<div></div>");
    Locator button = page.frameLocator("div").locator("button");
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> button.waitFor());
    assertTrue(error.getMessage().contains("<div></div>"));
    assertTrue(error.getMessage().contains("<iframe> was expected"));
  }

  @PlaywrightTest
  @DisplayName("LO-103 locator.frameLocator should work for iframe")
  void locatorFrameLocatorShouldWorkForIframe(Page page, TestServer server) {
    routeIframe(page);
    page.navigate(server.EMPTY_PAGE);
    Locator button = page.locator("body").frameLocator("iframe").locator("button");
    button.waitFor();
    assertEquals("Hello iframe", button.innerText());
    assertThat(button).hasText("Hello iframe");
    button.click();
  }

  @PlaywrightTest
  @DisplayName("LO-104 locator.frameLocator should throw on ambiguity")
  @Fixme("java: the message says 'strict mode violation: locator(\"body\").locator(\"iframe\") resolved to 3 elements', with double quotes")
  void locatorFrameLocatorShouldThrowOnAmbiguity(Page page, TestServer server) {
    routeAmbiguous(page);
    page.navigate(server.EMPTY_PAGE);
    Locator button = page.locator("body").frameLocator("iframe").locator("button");
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> button.waitFor());
    assertTrue(error.getMessage().contains("Error: strict mode violation: locator('body').locator('iframe') resolved to 3 elements"));
  }

  @PlaywrightTest
  @DisplayName("LO-105 locator.frameLocator should not throw on first/last/nth")
  void locatorFrameLocatorShouldNotThrowOnFirstLastNth(Page page, TestServer server) {
    routeAmbiguous(page);
    page.navigate(server.EMPTY_PAGE);
    Locator button1 = page.locator("body").frameLocator("iframe").first().locator("button");
    assertThat(button1).hasText("Hello from iframe-1.html");
    Locator button2 = page.locator("body").frameLocator("iframe").nth(1).locator("button");
    assertThat(button2).hasText("Hello from iframe-2.html");
    Locator button3 = page.locator("body").frameLocator("iframe").last().locator("button");
    assertThat(button3).hasText("Hello from iframe-3.html");
  }

  @PlaywrightTest
  @DisplayName("LO-106 getBy coverage")
  void getByCoverage(Page page, TestServer server) {
    routeIframe(page);
    page.navigate(server.EMPTY_PAGE);
    Locator button1 = page.frameLocator("iframe").getByRole(AriaRole.BUTTON);
    Locator button2 = page.frameLocator("iframe").getByText("Hello");
    Locator button3 = page.frameLocator("iframe").getByTestId("buttonId");
    assertThat(button1).hasText("Hello iframe");
    assertThat(button2).hasText("Hello iframe");
    assertThat(button3).hasText("Hello iframe");
    Locator input1 = page.frameLocator("iframe").getByLabel("Name");
    assertThat(input1).hasValue("");
    Locator input2 = page.frameLocator("iframe").getByPlaceholder("Placeholder");
    assertThat(input2).hasValue("");
    Locator input3 = page.frameLocator("iframe").getByAltText("Alternative");
    assertThat(input3).hasValue("");
    Locator input4 = page.frameLocator("iframe").getByTitle("Title");
    assertThat(input4).hasValue("");
  }

  @PlaywrightTest
  @DisplayName("LO-107 wait for hidden should succeed when frame is not in dom")
  void waitForHiddenShouldSucceedWhenFrameIsNotInDom(Page page) {
    page.navigate("about:blank");
    Locator button = page.frameLocator("iframe1").locator("button");
    assertTrue(button.isHidden());
    button.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN).setTimeout(1000));
    button.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.DETACHED).setTimeout(1000));
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> button.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED).setTimeout(1000)));
    assertTrue(error.getMessage().contains("Timeout 1000ms exceeded"));
  }

  @PlaywrightTest
  @DisplayName("LO-108 should work with COEP/COOP/CORP isolated iframe")
  @DisabledOnBrowser(value = BrowserName.FIREFOX, reason = "fixme")
  void shouldWorkWithCOEPCOOPCORPIsolatedIframe(Page page, TestServer server) {
    page.route("**/empty.html", route -> {
      route.fulfill(new Route.FulfillOptions()
        .setBody("<iframe src=\"https://" + server.CROSS_PROCESS_PREFIX + "/btn.html\" allow=\"cross-origin-isolated; fullscreen\" sandbox=\"allow-same-origin allow-scripts allow-popups\" ></iframe>")
        .setContentType("text/html")
        .setHeaders(stringMap(
          "cross-origin-embedder-policy", "require-corp",
          "cross-origin-opener-policy", "same-origin",
          "cross-origin-resource-policy", "cross-origin")));
    });
    page.route("**/btn.html", route -> {
      route.fulfill(new Route.FulfillOptions()
        .setBody("<button onclick=\"window.__clicked=true\">Click target</button>")
        .setContentType("text/html")
        .setHeaders(stringMap(
          "cross-origin-embedder-policy", "require-corp",
          "cross-origin-opener-policy", "same-origin",
          "cross-origin-resource-policy", "cross-origin")));
    });
    page.navigate(server.EMPTY_PAGE);
    page.frameLocator("iframe").getByRole(AriaRole.BUTTON).click();
    assertEquals(true, page.frames().get(1).evaluate("() => window['__clicked']"));
  }

  @PlaywrightTest
  @DisplayName("LO-109 locator.contentFrame should work")
  void locatorContentFrameShouldWork(Page page, TestServer server) {
    routeIframe(page);
    page.navigate(server.EMPTY_PAGE);
    Locator locator = page.locator("iframe");
    FrameLocator frameLocator = locator.contentFrame();
    Locator button = frameLocator.locator("button");
    assertEquals("Hello iframe", button.innerText());
    assertThat(button).hasText("Hello iframe");
    button.click();
  }

  @PlaywrightTest
  @DisplayName("LO-110 frameLocator.owner should work")
  void frameLocatorOwnerShouldWork(Page page, TestServer server) {
    routeIframe(page);
    page.navigate(server.EMPTY_PAGE);
    FrameLocator frameLocator = page.frameLocator("iframe");
    Locator locator = frameLocator.owner();
    assertThat(locator).isVisible();
    assertEquals("frame1", locator.getAttribute("name"));
  }

  @PlaywrightTest
  @DisplayName("LO-111 should treat elements inside hidden iframe as hidden")
  void shouldTreatElementsInsideHiddenIframeAsHidden(Page page) {
    page.setContent("\n" +
      "    <iframe name=\"hidden\" style=\"visibility: hidden\" srcdoc=\"<button>Hidden</button>\"></iframe>\n" +
      "    <div style=\"visibility: hidden\">\n" +
      "      <iframe name=\"hidden-parent\" srcdoc=\"<button>Hidden parent</button>\"></iframe>\n" +
      "      <iframe name=\"visible-override\" style=\"visibility: visible\" srcdoc=\"<button>Visible override</button>\"></iframe>\n" +
      "    </div>\n" +
      "    <iframe name=\"outer\" style=\"visibility: hidden\" srcdoc=\"<iframe name='inner' srcdoc='<button>Nested</button>'></iframe>\"></iframe>\n" +
      "  ");

    for (Locator button : Arrays.asList(
      page.frameLocator("[name=hidden]").locator("button"),
      page.frameLocator("[name=hidden-parent]").locator("button"),
      page.frameLocator("[name=outer]").frameLocator("[name=inner]").locator("button"))) {
      assertThat(button).isAttached();
      assertEquals(false, button.isVisible());
      assertEquals(true, button.isHidden());
      assertThat(button).isHidden();
      assertThat(button).not().isVisible();
      button.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
    }

    Locator button = page.frameLocator("[name=visible-override]").locator("button");
    assertEquals(true, button.isVisible());
    assertThat(button).isVisible();
    button.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
  }

  // LO-112 should wait for hidden iframe to become visible: not ported, see upstream_not_ported.txt.

  @PlaywrightTest
  @DisplayName("LO-113 should treat elements inside hidden cross-origin iframe as hidden")
  void shouldTreatElementsInsideHiddenCrossOriginIframeAsHidden(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<iframe style=\"visibility: hidden\" src=\"" + server.CROSS_PROCESS_PREFIX + "/frames/frame.html\"></iframe>");
    Locator div = page.frameLocator("iframe").locator("div");
    assertThat(div).hasText("Hi, I'm frame");
    assertEquals(false, div.isVisible());
    assertThat(div).isHidden();

    page.evaluate("() => document.querySelector('iframe').style.visibility = 'visible'");
    assertThat(div).isVisible();
    assertEquals(true, div.isVisible());
  }
}
