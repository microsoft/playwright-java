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

package com.microsoft.playwright.tests.library;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import java.util.concurrent.CompletableFuture;

import static com.microsoft.playwright.fixtures.Utils.asset;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/library/favicon.spec.ts. */
public class TestFavicon {
  @PlaywrightTest
  @DisplayName("FA-001 should load svg favicon with prefer-color-scheme")
  void shouldLoadSvgFaviconWithPreferColorScheme(Page page, TestServer server, BrowserName browserName) throws Exception {
    assumeFalse(!Env.headed() && browserName != BrowserName.FIREFOX, "headless browsers, except firefox, do not request favicons");
    assumeFalse(Env.headed() && browserName == BrowserName.WEBKIT, "headed webkit does not have a favicon feature");

    // Browsers aggressively cache favicons, so force bust with the
    // `d` parameter to make iterating on this test more predictable and isolated.
    String favicon = "/favicon.svg?d=" + System.currentTimeMillis();
    server.setRoute(favicon, (req, res) -> {
      server.serveFile(req, res, asset("media-query-prefers-color-scheme.svg"));
    });

    server.setRoute("/page.html", (_req, res) -> {
      res.end("\n" +
        "      <!DOCTYPE html>\n" +
        "      <html>\n" +
        "        <head>\n" +
        "          <meta charset=\"utf-8\">\n" +
        "          <link rel=\"icon\" type=\"image/svg+xml\" href=\"" + favicon + "\">\n" +
        "          <title>SVG Favicon Test</title>\n" +
        "        </head>\n" +
        "        <body>\n" +
        "          favicons\n" +
        "        </body>\n" +
        "      </html>\n");
    });

    CompletableFuture<TestServer.Request> faviconRequest = server.waitForRequest(favicon);
    page.navigate(server.PREFIX + "/page.html");
    faviconRequest.get();

    // Add artificial delay since, just because we saw the request for the favicon,
    // it does not mean the browser has processed it. There's not a great way to
    // hook into something like "favicon is fully displayed" event, so hopefully
    // 500ms is enough, but not too much!
    page.waitForTimeout(500);
    // Text still being around ensures we haven't actually lost our browser to a crash.
    page.waitForSelector("text=favicons");
  }
}
