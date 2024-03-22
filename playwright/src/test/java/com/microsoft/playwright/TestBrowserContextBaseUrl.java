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

package com.microsoft.playwright;

import com.microsoft.playwright.junit.FixtureTest;
import com.microsoft.playwright.junit.UsePlaywright;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@FixtureTest
@UsePlaywright(TestOptionsFactories.BasicOptionsFactory.class)
public class TestBrowserContextBaseUrl {
  @Test
  void shouldConstructANewURLWhenABaseURLInBrowserNewContextIsPassedToPageGoto(Browser browser, Server server) {
    try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setBaseURL(server.PREFIX))) {
      Page page = context.newPage();
      assertEquals(server.EMPTY_PAGE, page.navigate("/empty.html").url());
    }
  }

  @Test
  void shouldConstructANewURLWhenABaseURLInBrowserNewPageIsPassedToPageGoto(Browser browser, Server server) {
    try (Page page = browser.newPage(new Browser.NewPageOptions().setBaseURL(server.PREFIX))) {
      assertEquals(server.EMPTY_PAGE, page.navigate("/empty.html").url());
    }
  }
  @Test
  void shouldConstructTheURLsCorrectlyWhenABaseURLWithoutATrailingSlashInBrowserNewPageIsPassedToPageGoto(Browser browser, Server server) {
    try (Page page = browser.newPage(new Browser.NewPageOptions().setBaseURL(server.PREFIX + "/url-construction"))) {
      assertEquals(server.PREFIX + "/mypage.html", page.navigate("mypage.html").url());
      assertEquals(server.PREFIX + "/mypage.html", page.navigate("./mypage.html").url());
      assertEquals(server.PREFIX + "/mypage.html", page.navigate("/mypage.html").url());
    }
  }

  @Test
  void shouldConstructTheURLsCorrectlyWhenABaseURLWithATrailingSlashInBrowserNewPageIsPassedToPageGoto(Browser browser, Server server) {
    try (Page page = browser.newPage(new Browser.NewPageOptions().setBaseURL(server.PREFIX + "/url-construction/"))) {
      assertEquals(server.PREFIX + "/url-construction/mypage.html", page.navigate("mypage.html").url());
      assertEquals(server.PREFIX + "/url-construction/mypage.html", page.navigate("./mypage.html").url());
      assertEquals(server.PREFIX + "/mypage.html", page.navigate("/mypage.html").url());
      assertEquals(server.PREFIX + "/url-construction/", page.navigate(".").url());
      assertEquals(server.PREFIX + "/", page.navigate("/").url());
    }
  }

  @Test
  void shouldNotConstructANewURLWhenValidURLsArePassed(Browser browser, Server server) {
    try (Page page = browser.newPage(new Browser.NewPageOptions().setBaseURL("http://microsoft.com"))) {
      assertEquals(server.EMPTY_PAGE, page.navigate(server.EMPTY_PAGE).url());

      page.navigate("data:text/html,Hello world");
      assertEquals("data:text/html,Hello world", page.evaluate("window.location.href"));

      page.navigate("about:blank");
      assertEquals("about:blank", page.evaluate("window.location.href"));
    }
  }

  @Test
  void shouldBeAbleToMatchAURLRelativeToItsGivenURLWithUrlMatcher(Browser browser, Server server) {
    try (Page page = browser.newPage(new Browser.NewPageOptions().setBaseURL(server.PREFIX + "/foobar/"))) {
      page.navigate("/kek/index.html");
      page.waitForURL("/kek/index.html");
      assertEquals(server.PREFIX + "/kek/index.html", page.url());

      page.route("./kek/index.html", route -> route.fulfill(new Route.FulfillOptions().setBody("base-url-matched-route")));
      Request[] request = {null};
      Response response = page.waitForResponse("./kek/index.html", () -> {
        request[0] = page.waitForRequest("./kek/index.html", () -> {
          page.navigate("./kek/index.html");
        });
      });
      assertNotNull(request[0]);
      assertNotNull(response);
      assertEquals(server.PREFIX + "/foobar/kek/index.html", request[0].url());
      assertEquals(server.PREFIX + "/foobar/kek/index.html", response.url());
      assertEquals("base-url-matched-route", response.text());
    }
  }
}
