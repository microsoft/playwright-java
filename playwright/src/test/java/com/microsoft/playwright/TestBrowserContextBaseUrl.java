package com.microsoft.playwright;

import com.microsoft.playwright.junit.UsePlaywright;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Tag("fixtures") //temp tag to allow only tests with junit integration to run.  will be removed before merge.
@UsePlaywright(browserFactory = BrowserFromEnv.class)
public class TestBrowserContextBaseUrl extends __TestBaseNew {
  @Test
  void shouldConstructANewURLWhenABaseURLInBrowserNewContextIsPassedToPageGoto(Browser browser) throws MalformedURLException {
    try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setBaseURL(server.PREFIX))) {
      Page page = context.newPage();
      assertEquals(server.EMPTY_PAGE, page.navigate("/empty.html").url());
    }
  }

  @Test
  void shouldConstructANewURLWhenABaseURLInBrowserNewPageIsPassedToPageGoto(Browser browser) {
    try (Page page = browser.newPage(new Browser.NewPageOptions().setBaseURL(server.PREFIX))) {
      assertEquals(server.EMPTY_PAGE, page.navigate("/empty.html").url());
    }
  }
  @Test
  void shouldConstructTheURLsCorrectlyWhenABaseURLWithoutATrailingSlashInBrowserNewPageIsPassedToPageGoto(Browser browser) {
    try (Page page = browser.newPage(new Browser.NewPageOptions().setBaseURL(server.PREFIX + "/url-construction"))) {
      assertEquals(server.PREFIX + "/mypage.html", page.navigate("mypage.html").url());
      assertEquals(server.PREFIX + "/mypage.html", page.navigate("./mypage.html").url());
      assertEquals(server.PREFIX + "/mypage.html", page.navigate("/mypage.html").url());
    }
  }

  @Test
  void shouldConstructTheURLsCorrectlyWhenABaseURLWithATrailingSlashInBrowserNewPageIsPassedToPageGoto(Browser browser) {
    try (Page page = browser.newPage(new Browser.NewPageOptions().setBaseURL(server.PREFIX + "/url-construction/"))) {
      assertEquals(server.PREFIX + "/url-construction/mypage.html", page.navigate("mypage.html").url());
      assertEquals(server.PREFIX + "/url-construction/mypage.html", page.navigate("./mypage.html").url());
      assertEquals(server.PREFIX + "/mypage.html", page.navigate("/mypage.html").url());
      assertEquals(server.PREFIX + "/url-construction/", page.navigate(".").url());
      assertEquals(server.PREFIX + "/", page.navigate("/").url());
    }
  }

  @Test
  void shouldNotConstructANewURLWhenValidURLsArePassed(Browser browser) {
    try (Page page = browser.newPage(new Browser.NewPageOptions().setBaseURL("http://microsoft.com"))) {
      assertEquals(server.EMPTY_PAGE, page.navigate(server.EMPTY_PAGE).url());

      page.navigate("data:text/html,Hello world");
      assertEquals("data:text/html,Hello world", page.evaluate("window.location.href"));

      page.navigate("about:blank");
      assertEquals("about:blank", page.evaluate("window.location.href"));
    }
  }

  @Test
  void shouldBeAbleToMatchAURLRelativeToItsGivenURLWithUrlMatcher(Browser browser) {
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
