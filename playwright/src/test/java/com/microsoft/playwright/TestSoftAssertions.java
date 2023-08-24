package com.microsoft.playwright;

import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.assertions.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import static org.junit.jupiter.api.Assertions.*;

public class TestSoftAssertions extends TestBase {
  private SoftAssertions softly;

  @BeforeEach
  void beforeEach() {
    softly = SoftAssertions.create();
  }

  @Test
  void canMakeMultipleAssertionsWithoutFailingImmediately() {
    page.setContent("<div id=node class='foo bar'>Text content</div>");
    Locator locator = page.locator(".foo");
    assertDoesNotThrow(() -> softly.assertThat(locator).hasText("Text content"));
    assertDoesNotThrow(() -> softly.assertThat(locator).hasClass("foo bar"));
    assertDoesNotThrow(() -> softly.assertThat(locator).hasId("node"));
    softly.assertAll();
  }

  @Test
  void failureMessageIncludesMessagesFromAllAssertions() {
    page.setContent("<div id=node class='foo bar'>Text content</div>");
    Locator locator = page.locator(".foo");
    assertDoesNotThrow(() -> softly.assertThat(locator).hasText("some text", new LocatorAssertions.HasTextOptions().setTimeout(1000)));
    assertDoesNotThrow(() -> softly.assertThat(locator).hasClass("abc", new LocatorAssertions.HasClassOptions().setTimeout(1000)));
    assertDoesNotThrow(() -> softly.assertThat(locator).hasId("foo", new LocatorAssertions.HasIdOptions().setTimeout(1000)));
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> softly.assertAll());

    assertTrue(e.getMessage().contains("3 assertion(s) failed"));
    assertTrue(e.getMessage().contains("Locator expected to have text: some text"));
    assertTrue(e.getMessage().contains("Received: Text content"));
    assertTrue(e.getMessage().contains("Locator expected to have class: abc"));
    assertTrue(e.getMessage().contains("Received: foo bar"));
    assertTrue(e.getMessage().contains("Locator expected to have ID: foo"));
    assertTrue(e.getMessage().contains("Received: node"));
  }
}
