package com.microsoft.playwright;

import com.microsoft.playwright.assertions.LocatorAssertions;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class TestLocatorAssertions2 extends TestBase {
  @Test
  void isAttachedDefault() {
    page.setContent("<input></input>");
    Locator locator = page.locator("input");
    assertThat(locator).isAttached();
  }

  @Test
  void isAttachedWithHiddenElement() {
    page.setContent("<button style='display:none'>hello</button>");
    Locator locator = page.locator("button");
    assertThat(locator).isAttached();
  }

  @Test
  void isAttachedWithNot() {
    page.setContent("<button>hello</button>");
    Locator locator = page.locator("input");
    assertThat(locator).not().isAttached();
  }

  @Test
  void isAttachedWithAttachedTrue() {
    page.setContent("<button>hello</button>");
    Locator locator = page.locator("button");
    assertThat(locator).isAttached(new LocatorAssertions.IsAttachedOptions().setAttached(true));
  }

  @Test
  void isAttachedWithAttachedFalse() {
    page.setContent("<button>hello</button>");
    Locator locator = page.locator("input");
    assertThat(locator).isAttached(new LocatorAssertions.IsAttachedOptions().setAttached(false));
  }

  @Test
  void isAttachedWithNotAndAttachedFalse() {
    page.setContent("<button>hello</button>");
    Locator locator = page.locator("button");
    assertThat(locator).not().isAttached(new LocatorAssertions.IsAttachedOptions().setAttached(false));
  }

  @Test
  void isAttachedEventually() {
    page.setContent("<div></div>");
    Locator locator = page.locator("span");
      page.evalOnSelector("div", "div => setTimeout(() => {\n" +
        "      div.innerHTML = '<span>Hello</span>'\n" +
        "    }, 100)");
    assertThat(locator).isAttached();
  }

  @Test
  void isAttachedEventuallyWithNot() {
    page.setContent("<div><span>Hello</span></div>");
    Locator locator = page.locator("span");
      page.evalOnSelector("div",  "div => setTimeout(() => {\n" +
        "      div.textContent = '';\n" +
        "    }, 0)");
    assertThat(locator).not().isAttached();
  }

  @Test
  void isAttachedFail() {
    page.setContent("<button>Hello</button>");
    Locator locator = page.locator("input");
    AssertionFailedError error = assertThrows(AssertionFailedError.class,
      () -> assertThat(locator).isAttached(new LocatorAssertions.IsAttachedOptions().setTimeout(1000)));
    assertFalse(error.getMessage().contains("locator resolved to"), error.getMessage());
  }

  @Test
  void isAttachedFailWithNot() {
    page.setContent("<input></input>");
    Locator locator = page.locator("input");
    AssertionFailedError error = assertThrows(AssertionFailedError.class,
      () -> assertThat(locator).not().isAttached(new LocatorAssertions.IsAttachedOptions().setTimeout(1000)));
    assertTrue(error.getMessage().contains("locator resolved to <input/>"), error.getMessage());
  }

  @Test
  void isAttachedWithImpossibleTimeout() {
    page.setContent("<div id=node>Text content</div>");
    assertThat(page.locator("#node")).isAttached(new LocatorAssertions.IsAttachedOptions().setTimeout(1));
  }

  @Test
  void isAttachedWithImpossibleTimeoutNot() {
    page.setContent("<div id=node>Text content</div>");
    assertThat(page.locator("no-such-thing")).not().isAttached(new LocatorAssertions.IsAttachedOptions().setTimeout(1));
  }
}
