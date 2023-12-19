package com.microsoft.playwright;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestUnrouteBehavior extends TestBase {
  @Test
  void contextUnrouteAllRemovesAllHandlers() {
    context.route("**/*", route -> {
      route.abort();
    });
    context.route("**/empty.html", route -> {
      route.abort();
    });
    context.unrouteAll();
    page.navigate(server.EMPTY_PAGE);
  }

  @Test
  void pageUnrouteAllRemovesAllRoutes() {
    page.route("**/*", route -> {
      route.abort();
    });
    page.route("**/empty.html", route -> {
      route.abort();
    });
    page.unrouteAll();
    Response response = page.navigate(server.EMPTY_PAGE);
    assertTrue(response.ok());
  }

}
