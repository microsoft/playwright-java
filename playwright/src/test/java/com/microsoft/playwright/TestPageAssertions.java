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

import com.microsoft.playwright.assertions.PageAssertions;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import org.opentest4j.AssertionFailedError;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class TestPageAssertions {
  @PlaywrightTest
  void hasURLTextPass(Page page) {
    page.navigate("data:text/html,<div>A</div>");
    assertThat(page).hasURL("data:text/html,<div>A</div>");
  }

  @PlaywrightTest
  void hasURLTextFail(Page page) {
    page.navigate("data:text/html,<div>B</div>");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(page).hasURL("foo", new PageAssertions.HasURLOptions().setTimeout(1_000));
    });
    assertEquals("foo", e.getExpected().getValue());
    assertEquals("data:text/html,<div>B</div>", e.getActual().getValue());
    assertTrue(e.getMessage().contains("Page URL expected to be"), e.getMessage());
  }

  @PlaywrightTest
  void shouldSupportHasUrlWithBaseUrl(Browser browser, Server server) {
    try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setBaseURL(server.PREFIX))) {
      Page page = context.newPage();
      page.navigate(server.EMPTY_PAGE);
      assertThat(page).hasURL("/empty.html", new PageAssertions.HasURLOptions().setTimeout(1_000));
    }
  }

  @PlaywrightTest
  void notHasUrlText(Page page) {
    page.navigate("data:text/html,<div>B</div>");
    assertThat(page).not().hasURL("about:blank", new PageAssertions.HasURLOptions().setTimeout(1000));
  }

  @PlaywrightTest
  public void hasUrlSupportIgnoreCase(Page page) {
    page.navigate("data:text/html,<div>A</div>");
    assertThat(page).hasURL("DATA:teXT/HTml,<div>a</div>", new PageAssertions.HasURLOptions().setIgnoreCase(true));
  }

  @PlaywrightTest
  void hasURLRegexPass(Page page) {
    page.navigate("data:text/html,<div>A</div>");
    assertThat(page).hasURL(Pattern.compile("text"));
  }

  @PlaywrightTest
  void hasURLRegexFail(Page page, Server server) {
    page.navigate(server.EMPTY_PAGE);
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(page).hasURL(Pattern.compile(".*foo.*"), new PageAssertions.HasURLOptions().setTimeout(1_000));
    });
    assertEquals(".*foo.*", e.getExpected().getStringRepresentation());
    assertEquals(server.EMPTY_PAGE, e.getActual().getValue());
    assertTrue(e.getMessage().contains("Page URL expected to match regex"), e.getMessage());
  }

  @PlaywrightTest
  void notHasUrlRegEx(Page page) {
    page.navigate("data:text/html,<div>B</div>");
    assertThat(page).not().hasURL(Pattern.compile("about"), new PageAssertions.HasURLOptions().setTimeout(1000));
  }

  @PlaywrightTest
  void hasTitleTextPass(Page page, Server server) {
    page.navigate(server.PREFIX + "/title.html");
    assertThat(page).hasTitle("Woof-Woof", new PageAssertions.HasTitleOptions().setTimeout(1_000));
  }

  @PlaywrightTest
  void hasTitleTextNormalizeWhitespaces(Page page) {
    page.setContent("<title>     Foo     Bar    </title>");
    assertThat(page).hasTitle("  Foo  Bar", new PageAssertions.HasTitleOptions().setTimeout(1_000));
  }

  @PlaywrightTest
  void hasTitleTextFail(Page page, Server server) {
    page.navigate(server.PREFIX + "/title.html");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(page).hasTitle("foo", new PageAssertions.HasTitleOptions().setTimeout(1_000));
    });
    assertEquals("foo", e.getExpected().getValue());
    assertEquals("Woof-Woof", e.getActual().getValue());
    assertTrue(e.getMessage().contains("Page title expected to be\nExpected: foo\nReceived: Woof-Woof"), e.getMessage());
  }

  @PlaywrightTest
  void hasTitleRegexPass(Page page, Server server) {
    page.navigate(server.PREFIX + "/title.html");
    assertThat(page).hasTitle(Pattern.compile("^.oof.+oof$"));
  }

  @PlaywrightTest
  void hasTitleRegexFail(Page page, Server server) {
    page.navigate(server.PREFIX + "/title.html");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(page).hasTitle(Pattern.compile("^foo[AB]"), new PageAssertions.HasTitleOptions().setTimeout(1_000));
    });
    assertEquals("^foo[AB]", e.getExpected().getStringRepresentation());
    assertEquals("Woof-Woof", e.getActual().getValue());
    assertTrue(e.getMessage().contains("Page title expected to match regex\nExpected: ^foo[AB]\nReceived: Woof-Woof"), e.getMessage());
  }

  @PlaywrightTest
  void notHasTitleRegEx(Page page, Server server) {
    page.navigate(server.PREFIX + "/title.html");
    assertThat(page).not().hasTitle(Pattern.compile("ab.ut"));
  }

  @PlaywrightTest
  void hasTitleRegExCaseInsensitivePass(Page page, Server server) {
    page.navigate(server.PREFIX + "/title.html");
    assertThat(page).hasTitle(Pattern.compile("woof-woof", Pattern.CASE_INSENSITIVE));
  }
}
