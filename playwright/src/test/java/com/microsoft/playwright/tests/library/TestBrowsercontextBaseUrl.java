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
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.fixtures.CreateUserDataDir;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Port of tests/library/browsercontext-base-url.spec.ts. */
public class TestBrowsercontextBaseUrl {
  @PlaywrightTest
  @DisplayName("NA-089 should construct a new URL when a baseURL in browser.newContext is passed to page.goto @smoke")
  void shouldConstructANewURLWhenABaseURLInBrowserNewContextIsPassedToPageGoto(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setBaseURL(server.PREFIX));
    Page page = context.newPage();
    assertEquals(server.EMPTY_PAGE, page.navigate("/empty.html").url());
    context.close();
  }

  @PlaywrightTest
  @DisplayName("NA-090 should construct a new URL when a baseURL in browser.newPage is passed to page.goto")
  void shouldConstructANewURLWhenABaseURLInBrowserNewPageIsPassedToPageGoto(Browser browser, TestServer server) {
    Page page = browser.newPage(new Browser.NewPageOptions()
      .setBaseURL(server.PREFIX));
    assertEquals(server.EMPTY_PAGE, page.navigate("/empty.html").url());
    page.close();
  }

  @PlaywrightTest
  @DisplayName("NA-091 should construct a new URL when a baseURL in browserType.launchPersistentContext is passed to page.goto")
  void shouldConstructANewURLWhenABaseURLInBrowserTypeLaunchPersistentContextIsPassedToPageGoto(BrowserType browserType, TestServer server, CreateUserDataDir createUserDataDir) {
    Path userDataDir = createUserDataDir.create();
    BrowserContext context = browserType.launchPersistentContext(userDataDir, Env.launchPersistentContextOptions()
      .setBaseURL(server.PREFIX));
    Page page = context.newPage();
    assertEquals(server.EMPTY_PAGE, page.navigate("/empty.html").url());
    context.close();
  }

  @PlaywrightTest
  @DisplayName("NA-092 should construct the URLs correctly when a baseURL without a trailing slash in browser.newPage is passed to page.goto")
  void shouldConstructTheURLsCorrectlyWhenABaseURLWithoutATrailingSlashInBrowserNewPageIsPassedToPageGoto(Browser browser, TestServer server) {
    Page page = browser.newPage(new Browser.NewPageOptions()
      .setBaseURL(server.PREFIX + "/url-construction"));
    assertEquals(server.PREFIX + "/mypage.html", page.navigate("mypage.html").url());
    assertEquals(server.PREFIX + "/mypage.html", page.navigate("./mypage.html").url());
    assertEquals(server.PREFIX + "/mypage.html", page.navigate("/mypage.html").url());
    page.close();
  }

  @PlaywrightTest
  @DisplayName("NA-093 should construct the URLs correctly when a baseURL with a trailing slash in browser.newPage is passed to page.goto")
  void shouldConstructTheURLsCorrectlyWhenABaseURLWithATrailingSlashInBrowserNewPageIsPassedToPageGoto(Browser browser, TestServer server) {
    Page page = browser.newPage(new Browser.NewPageOptions()
      .setBaseURL(server.PREFIX + "/url-construction/"));
    assertEquals(server.PREFIX + "/url-construction/mypage.html", page.navigate("mypage.html").url());
    assertEquals(server.PREFIX + "/url-construction/mypage.html", page.navigate("./mypage.html").url());
    assertEquals(server.PREFIX + "/mypage.html", page.navigate("/mypage.html").url());
    assertEquals(server.PREFIX + "/url-construction/", page.navigate(".").url());
    assertEquals(server.PREFIX + "/", page.navigate("/").url());
    page.close();
  }

  @PlaywrightTest
  @DisplayName("NA-094 should not construct a new URL when valid URLs are passed")
  void shouldNotConstructANewURLWhenValidURLsArePassed(Browser browser, TestServer server) {
    Page page = browser.newPage(new Browser.NewPageOptions()
      .setBaseURL("http://microsoft.com"));
    assertEquals(server.EMPTY_PAGE, page.navigate(server.EMPTY_PAGE).url());

    page.navigate("data:text/html,Hello world");
    assertEquals("data:text/html,Hello world", page.evaluate("() => window.location.href"));

    page.navigate("about:blank");
    assertEquals("about:blank", page.evaluate("() => window.location.href"));
    page.close();
  }

  @PlaywrightTest
  @DisplayName("NA-095 should be able to match a URL relative to its given URL with urlMatcher")
  void shouldBeAbleToMatchAURLRelativeToItsGivenURLWithUrlMatcher(Browser browser, TestServer server) {
    Page page = browser.newPage(new Browser.NewPageOptions()
      .setBaseURL(server.PREFIX + "/foobar/"));
    page.navigate("/kek/index.html");
    page.waitForURL("/kek/index.html");
    assertEquals(server.PREFIX + "/kek/index.html", page.url());

    page.route("./kek/index.html", route -> route.fulfill(new com.microsoft.playwright.Route.FulfillOptions()
      .setBody("base-url-matched-route")));
    Response[] response = new Response[1];
    Request request = page.waitForRequest("./kek/index.html", () -> {
      response[0] = page.waitForResponse("./kek/index.html", () -> {
        page.navigate("./kek/index.html");
      });
    });
    assertEquals(server.PREFIX + "/foobar/kek/index.html", request.url());
    assertEquals(server.PREFIX + "/foobar/kek/index.html", response[0].url());
    assertEquals("base-url-matched-route", new String(response[0].body(), StandardCharsets.UTF_8));
    page.close();
  }

  @PlaywrightTest
  @DisplayName("NA-096 should not construct a new URL with baseURL when a glob was used")
  void shouldNotConstructANewURLWithBaseURLWhenAGlobWasUsed(Browser browser, TestServer server) {
    Page page = browser.newPage(new Browser.NewPageOptions()
      .setBaseURL(server.PREFIX + "/foobar/"));
    page.navigate("./kek/index.html");
    page.waitForURL("**/foobar/kek/index.html");
    page.close();
  }
}
