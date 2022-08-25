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
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class TestPageAssertions extends TestBase {
  @Test
  void hasURLTextPass() {
    page.navigate("data:text/html,<div>A</div>");
    assertThat(page).hasURL("data:text/html,<div>A</div>");
  }

  @Test
  void hasURLTextFail() {
    page.navigate("data:text/html,<div>B</div>");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(page).hasURL("foo", new PageAssertions.HasURLOptions().setTimeout(1_000));
    });
    assertEquals("foo", e.getExpected().getValue());
    assertEquals("data:text/html,<div>B</div>", e.getActual().getValue());
    assertTrue(e.getMessage().contains("Page URL expected to be"), e.getMessage());
  }

  @Test
  void shouldSupportHasUrlWithBaseUrl() {
    try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setBaseURL(server.PREFIX))) {
      Page page = context.newPage();
      page.navigate(server.EMPTY_PAGE);
      assertThat(page).hasURL("/empty.html", new PageAssertions.HasURLOptions().setTimeout(1_000));
    }
  }

  @Test
  void notHasUrlText() {
    page.navigate("data:text/html,<div>B</div>");
    assertThat(page).not().hasURL("about:blank", new PageAssertions.HasURLOptions().setTimeout(1000));
  }

  @Test
  void hasURLRegexPass() {
    page.navigate("data:text/html,<div>A</div>");
    assertThat(page).hasURL(Pattern.compile("text"));
  }

  @Test
  void hasURLRegexFail() {
    page.navigate(server.EMPTY_PAGE);
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(page).hasURL(Pattern.compile(".*foo.*"), new PageAssertions.HasURLOptions().setTimeout(1_000));
    });
    assertEquals(".*foo.*", e.getExpected().getStringRepresentation());
    assertEquals(server.EMPTY_PAGE, e.getActual().getValue());
    assertTrue(e.getMessage().contains("Page URL expected to match regex"), e.getMessage());
  }

  @Test
  void notHasUrlRegEx() {
    page.navigate("data:text/html,<div>B</div>");
    assertThat(page).not().hasURL(Pattern.compile("about"), new PageAssertions.HasURLOptions().setTimeout(1000));
  }

  @Test
  void hasTitleTextPass() {
    page.navigate(server.PREFIX + "/title.html");
    assertThat(page).hasTitle("Woof-Woof", new PageAssertions.HasTitleOptions().setTimeout(1_000));
  }

  @Test
  void hasTitleTextNormalizeWhitespaces() {
    page.setContent("<title>     Foo     Bar    </title>");
    assertThat(page).hasTitle("  Foo  Bar", new PageAssertions.HasTitleOptions().setTimeout(1_000));
  }

  @Test
  void hasTitleTextFail() {
    page.navigate(server.PREFIX + "/title.html");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(page).hasTitle("foo", new PageAssertions.HasTitleOptions().setTimeout(1_000));
    });
    assertEquals("foo", e.getExpected().getValue());
    assertEquals("Woof-Woof", e.getActual().getValue());
    assertTrue(e.getMessage().contains("Page title expected to be: foo\nReceived: Woof-Woof"), e.getMessage());
  }

  @Test
  void hasTitleRegexPass() {
    page.navigate(server.PREFIX + "/title.html");
    assertThat(page).hasTitle(Pattern.compile("^.oof.+oof$"));
  }

  @Test
  void hasTitleRegexFail() {
    page.navigate(server.PREFIX + "/title.html");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(page).hasTitle(Pattern.compile("^foo[AB]"), new PageAssertions.HasTitleOptions().setTimeout(1_000));
    });
    assertEquals("^foo[AB]", e.getExpected().getStringRepresentation());
    assertEquals("Woof-Woof", e.getActual().getValue());
    assertTrue(e.getMessage().contains("Page title expected to match regex: ^foo[AB]\nReceived: Woof-Woof"), e.getMessage());
  }

  @Test
  void notHasTitleRegEx() {
    page.navigate(server.PREFIX + "/title.html");
    assertThat(page).not().hasTitle(Pattern.compile("ab.ut"));
  }

  @Test
  void hasTitleRegExCaseInsensitivePass() {
    page.navigate(server.PREFIX + "/title.html");
    assertThat(page).hasTitle(Pattern.compile("woof-woof", Pattern.CASE_INSENSITIVE));
  }
}
