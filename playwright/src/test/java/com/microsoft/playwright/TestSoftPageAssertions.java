package com.microsoft.playwright;

import com.microsoft.playwright.assertions.PageAssertions;
import com.microsoft.playwright.assertions.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.util.regex.Pattern;

import static com.microsoft.playwright.Utils.assertFailureCount;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestSoftPageAssertions extends TestBase {
  private SoftAssertions softly;

  @BeforeEach
  void beforeEach() {
    softly = SoftAssertions.create();
  }

  @Test
  void hasUrlTextPass() {
    page.navigate("data:text/html,<div>A</div>");
    softly.assertThat(page).hasURL("data:text/html,<div>A</div>");
    softly.assertAll();
    assertFailureCount(softly, 0);
  }

  @Test
  void hasURLTextFail() {
    page.navigate("data:text/html,<div>B</div>");
    softly.assertThat(page).hasURL("foo", new PageAssertions.HasURLOptions().setTimeout(1_000));
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> softly.assertAll());
    assertTrue(e.getMessage().contains("1 assertion(s) failed"), e.getMessage());
    assertTrue(e.getMessage().contains("Page URL expected to be"), e.getMessage());
    assertFailureCount(softly, 1);
  }

  @Test
  void shouldSupportHasUrlWithBaseUrl() {
    try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setBaseURL(server.PREFIX))) {
      Page page = context.newPage();
      page.navigate(server.EMPTY_PAGE);
      softly.assertThat(page).hasURL("/empty.html", new PageAssertions.HasURLOptions().setTimeout(1_000));
      softly.assertAll();
      assertFailureCount(softly, 0);
    }
  }

  @Test
  void notHasUrlText() {
    page.navigate("data:text/html,<div>B</div>");
    softly.assertThat(page).not().hasURL("about:blank", new PageAssertions.HasURLOptions().setTimeout(1000));
    softly.assertAll();
    assertFailureCount(softly, 0);
  }

  @Test
  void hasURLRegexPass() {
    page.navigate("data:text/html,<div>A</div>");
    softly.assertThat(page).hasURL(Pattern.compile("text"));
    softly.assertAll();
    assertFailureCount(softly, 0);
  }

  @Test
  void hasURLRegexFail() {
    page.navigate(server.EMPTY_PAGE);
    softly.assertThat(page).hasURL(Pattern.compile(".*foo.*"), new PageAssertions.HasURLOptions().setTimeout(1_000));
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> softly.assertAll());
    assertTrue(e.getMessage().contains("1 assertion(s) failed"), e.getMessage());
    assertTrue(e.getMessage().contains("Page URL expected to match regex"), e.getMessage());
    assertFailureCount(softly, 1);
  }

  @Test
  void notHasUrlRegEx() {
    page.navigate("data:text/html,<div>B</div>");
    softly.assertThat(page).not().hasURL(Pattern.compile("about"), new PageAssertions.HasURLOptions().setTimeout(1000));
    softly.assertAll();
    assertFailureCount(softly, 0);
  }

  @Test
  void hasTitleTextPass() {
    page.navigate(server.PREFIX + "/title.html");
    softly.assertThat(page).hasTitle("Woof-Woof", new PageAssertions.HasTitleOptions().setTimeout(1_000));
    softly.assertAll();
    assertFailureCount(softly, 0);
  }

  @Test
  void hasTitleTextNormalizeWhitespaces() {
    page.setContent("<title>     Foo     Bar    </title>");
    softly.assertThat(page).hasTitle("  Foo  Bar", new PageAssertions.HasTitleOptions().setTimeout(1_000));
    softly.assertAll();
    assertFailureCount(softly, 0);
  }

  @Test
  void hasTitleTextFail() {
    page.navigate(server.PREFIX + "/title.html");
    softly.assertThat(page).hasTitle("foo", new PageAssertions.HasTitleOptions().setTimeout(1_000));
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> softly.assertAll());
    assertTrue(e.getMessage().contains("1 assertion(s) failed"), e.getMessage());
    assertTrue(e.getMessage().contains("Page title expected to be: foo\nReceived: Woof-Woof"), e.getMessage());
    assertFailureCount(softly, 1);
  }

  @Test
  void hasTitleRegexPass() {
    page.navigate(server.PREFIX + "/title.html");
    softly.assertThat(page).hasTitle(Pattern.compile("^.oof.+oof$"));
    softly.assertAll();
    assertFailureCount(softly, 0);
  }

  @Test
  void hasTitleRegexFail() {
    page.navigate(server.PREFIX + "/title.html");
    softly.assertThat(page).hasTitle(Pattern.compile("^foo[AB]"), new PageAssertions.HasTitleOptions().setTimeout(1_000));
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> softly.assertAll());
    assertTrue(e.getMessage().contains("1 assertion(s) failed"), e.getMessage());
    assertTrue(e.getMessage().contains("Page title expected to match regex: ^foo[AB]\nReceived: Woof-Woof"), e.getMessage());
    assertFailureCount(softly, 1);
  }

  @Test
  void notHasTitleRegEx() {
    page.navigate(server.PREFIX + "/title.html");
    softly.assertThat(page).not().hasTitle(Pattern.compile("ab.ut"));
    softly.assertAll();
    assertFailureCount(softly, 0);
  }

  @Test
  void hasTitleRegExCaseInsensitivePass() {
    page.navigate(server.PREFIX + "/title.html");
    softly.assertThat(page).hasTitle(Pattern.compile("woof-woof", Pattern.CASE_INSENSITIVE));
    softly.assertAll();
    assertFailureCount(softly, 0);
  }
}
