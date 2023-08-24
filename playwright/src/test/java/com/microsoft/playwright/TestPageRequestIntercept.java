package com.microsoft.playwright;

import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class TestPageRequestIntercept extends TestBase {
  @Test
  void shouldFulfillPopupMainRequestUsingAlias() {
    page.context().route("**/*", route -> {
      APIResponse response = route.fetch();
      route.fulfill(new Route.FulfillOptions().setResponse(response).setBody("hello" ));
    });
    page.setContent("<a target=_blank href='" + server.EMPTY_PAGE + "'>click me</a>");
    Page popup = page.waitForPopup(() -> page.getByText("click me").click());
    assertThat(popup.locator("body")).hasText("hello");
  }
}
