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

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.ContextFactory;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import java.util.concurrent.CompletableFuture;

import static com.microsoft.playwright.fixtures.Utils.attachFrame;
import static com.microsoft.playwright.fixtures.Utils.chromiumVersionLessThan;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/library/browsercontext-user-agent.spec.ts. */
public class TestBrowsercontextUserAgent {
  @PlaywrightTest
  @DisplayName("UA-001 should work")
  void shouldWork(Browser browser, TestServer server) throws Exception {
    {
      BrowserContext context = browser.newContext();
      Page page = context.newPage();
      assertTrue(((String) page.evaluate("() => navigator.userAgent")).contains("Mozilla"));
      context.close();
    }
    {
      BrowserContext context = browser.newContext(new Browser.NewContextOptions().setUserAgent("foobar"));
      Page page = context.newPage();
      CompletableFuture<TestServer.Request> requestFuture = server.waitForRequest("/empty.html");
      page.navigate(server.EMPTY_PAGE);
      TestServer.Request request = requestFuture.get();
      assertEquals("foobar", request.headers.get("user-agent"));
      context.close();
    }
  }

  @PlaywrightTest
  @DisplayName("UA-002 should work for subframes")
  void shouldWorkForSubframes(Browser browser, TestServer server) throws Exception {
    {
      BrowserContext context = browser.newContext();
      Page page = context.newPage();
      assertTrue(((String) page.evaluate("() => navigator.userAgent")).contains("Mozilla"));
      context.close();
    }
    {
      BrowserContext context = browser.newContext(new Browser.NewContextOptions().setUserAgent("foobar"));
      Page page = context.newPage();
      CompletableFuture<TestServer.Request> requestFuture = server.waitForRequest("/empty.html");
      attachFrame(page, "frame1", server.EMPTY_PAGE);
      TestServer.Request request = requestFuture.get();
      assertEquals("foobar", request.headers.get("user-agent"));
      context.close();
    }
  }

  @PlaywrightTest
  @DisplayName("UA-004 should make a copy of default options")
  void shouldMakeACopyOfDefaultOptions(Browser browser, TestServer server) throws Exception {
    Browser.NewContextOptions options = new Browser.NewContextOptions().setUserAgent("foobar");
    BrowserContext context = browser.newContext(options);
    options.setUserAgent("wrong");
    Page page = context.newPage();
    CompletableFuture<TestServer.Request> requestFuture = server.waitForRequest("/empty.html");
    page.navigate(server.EMPTY_PAGE);
    TestServer.Request request = requestFuture.get();
    assertEquals("foobar", request.headers.get("user-agent"));
    context.close();
  }

  @PlaywrightTest
  @DisplayName("UA-005 custom user agent for download")
  void customUserAgentForDownload(TestServer server, ContextFactory contextFactory, Browser browser, BrowserName browserName) throws Exception {
    // it.info().annotations.push({ type: 'issue', description: 'https://github.com/microsoft/playwright/issues/22843' });
    assumeFalse(browserName == BrowserName.CHROMIUM && chromiumVersionLessThan(browser.version(), "116.0.0.0"), "https://chromium-review.googlesource.com/c/chromium/src/+/4554578");

    server.setRoute("/download", (req, res) -> {
      res.setHeader("Content-Type", "application/octet-stream");
      res.setHeader("Content-Disposition", "attachment");
      res.end("Hello world");
    });

    BrowserContext context = contextFactory.newContext(new Browser.NewContextOptions().setUserAgent("MyCustomUA"));
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<a id=\"download\" download=\"name\" href=\"/download\">Download</a>");
    CompletableFuture<TestServer.Request> serverRequest = server.waitForRequest("/download");
    try {
      page.click("#download");
    } catch (PlaywrightException e) {
    }
    TestServer.Request req = serverRequest.get();
    assertEquals("MyCustomUA", req.headers.get("user-agent"));
  }
}
