package com.microsoft.playwright;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestPageEventPopup extends TestBase {
  @Test
  void shouldWorkWithClickingTarget_blank() {
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<a target=_blank rel='opener' href='/one-style.html'>yo</a>");
    Page popup = page.waitForPopup(() -> page.click("a"));
    assertEquals(false, page.evaluate("() => !!window.opener"));
    assertEquals(true, popup.evaluate("() => !!window.opener"));
    assertEquals(popup, popup.mainFrame().page());
  }
}
