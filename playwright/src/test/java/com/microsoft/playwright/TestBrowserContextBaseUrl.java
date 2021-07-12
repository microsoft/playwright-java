package com.microsoft.playwright;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestBrowserContextBaseUrl extends TestBase {
  @Test
  void shouldConstructANewURLWhenABaseURLInBrowserNewContextIsPassedToPageGoto() {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setBaseURL(server.PREFIX));
    Page page = context.newPage();
    assertEquals(server.EMPTY_PAGE, (page.navigate("/empty.html")).url());
    context.close();
  }

  @Test
  void shouldConstructANewURLWhenABaseURLInBrowserNewPageIsPassedToPageGoto() {
    Page page = browser.newPage(new Browser.NewPageOptions().setBaseURL(server.PREFIX));
    assertEquals(server.EMPTY_PAGE, (page.navigate("/empty.html")).url());
    page.close();
  }
}
