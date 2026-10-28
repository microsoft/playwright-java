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
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/page/page-event-load.spec.ts. */
public class TestPageEventLoad {
  @PlaywrightTest
  @DisplayName("NA-087 should fire once")
  void shouldFireOnce(Page page, TestServer server, BrowserName browserName) {
    assumeFalse(browserName == BrowserName.FIREFOX && !Env.isBidi(), "fixme: Firefox sometimes double fires.");

    AtomicInteger count = new AtomicInteger(0);
    page.onLoad(p -> count.incrementAndGet());
    page.navigate(server.PREFIX + "/one-style.html");
    page.waitForTimeout(1000);
    assertEquals(1, count.get());
  }

  @PlaywrightTest
  @DisplayName("NA-088 should fire once with iframe navigation")
  void shouldFireOnceWithIframeNavigation(Page page, TestServer server, BrowserName browserName) throws Exception {
    assumeFalse(browserName == BrowserName.FIREFOX && !Env.isBidi(), "fixme: Firefox sometimes double fires.");

    AtomicInteger requestCount = new AtomicInteger(0);
    server.setRoute("/tracker", (req, res) -> {
      res.write("request count: " + requestCount.incrementAndGet());
      res.end();
    });
    server.setRoute("/home", (req, res) -> {
      res.write("\n" +
        "      <!DOCTYPE html>\n" +
        "      <html>\n" +
        "        <head>\n" +
        "        </head>\n" +
        "        <body>\n" +
        "          <script>\n" +
        "            window.eventLog = [];\n" +
        "            window.addEventListener('load', () => {\n" +
        "              window.eventLog.push('load');\n" +
        "            });\n" +
        "          </script>\n" +
        "          <form id=\"trackerForm\" action=\"/tracker\" method=\"post\" target=\"tracker\">\n" +
        "            <input type=\"submit\">\n" +
        "          </form>\n" +
        "          <iframe name=\"tracker\" src=\"/tracker\">\n" +
        "        </body>\n" +
        "      </html>\n" +
        "    ");
      res.end();
    });
    AtomicInteger count = new AtomicInteger(0);
    page.onLoad(p -> count.incrementAndGet());
    page.navigate(server.PREFIX + "/home");
    com.microsoft.playwright.FrameLocator trackingFrame = page.frameLocator("[name=tracker]");
    assertThat(trackingFrame.locator(":scope")).containsText("request count: 1");
    // Java: Promise.race([waitForEvent('load'), waitForTimeout(1000)]) is a load handler and a timeout after the click.
    CompletableFuture<String> loadFired = new CompletableFuture<>();
    page.onLoad(new Consumer<Page>() {
      @Override
      public void accept(Page p) {
        page.offLoad(this);
        loadFired.complete("loadfired");
      }
    });
    page.locator("input[type=submit]").click();
    page.waitForTimeout(1000);
    loadFired.complete("timeout");
    assertEquals("timeout", loadFired.get());
    assertEquals(1, count.get());
    assertEquals(Arrays.asList("load"), page.evaluate("() => window['eventLog']"));
  }
}
