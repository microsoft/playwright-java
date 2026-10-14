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

import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

// Java's \Q...\E quoting (Pattern.quote) is not supported by JavaScript regular expressions.
public class TestRegexQuote extends TestBase {
  @Test
  void shouldFilterByQuotedPattern() {
    page.setContent("<div>Hello Playwright</div><div>Hello world</div>");
    Locator locator = page.locator("div").filter(new Locator.FilterOptions().setHasText(Pattern.compile(Pattern.quote("Playwright"))));
    assertThat(locator).hasCount(1);
    assertThat(locator).hasText("Hello Playwright");
  }

  @Test
  void shouldEscapeRegexSpecialCharactersInQuotedPattern() {
    page.setContent("<div>a.b(c)</div><div>aXbc</div>");
    Locator locator = page.locator("div").filter(new Locator.FilterOptions().setHasText(Pattern.compile(Pattern.quote("a.b(c)"))));
    assertThat(locator).hasCount(1);
    assertThat(locator).hasText("a.b(c)");
    assertThat(page.getByText(Pattern.compile("^" + Pattern.quote("a.b(c)") + "$"))).hasCount(1);
  }

  @Test
  void shouldHandleQuotedPatternContainingQuoteEnd() {
    page.setContent("<div>x\\Ey[1+1]/</div><div>xEy</div>");
    Pattern pattern = Pattern.compile(Pattern.quote("x\\Ey[1+1]/"));
    assertThat(page.locator("div").filter(new Locator.FilterOptions().setHasText(pattern))).hasCount(1);
    assertThat(page.locator("div").first()).hasText(pattern);
    assertThat(page.locator("div").last()).not().hasText(pattern);
  }

  @Test
  void shouldAssertTextWithQuotedPattern() {
    page.setContent("<div>Price: $9.99 (USD)</div>");
    assertThat(page.locator("div")).hasText(Pattern.compile(Pattern.quote("$9.99 (USD)")));
    assertThat(page.locator("div")).hasText(Pattern.compile("^Price: " + Pattern.quote("$9.99") + "\\s+\\(USD\\)$"));
    assertThat(page.locator("div")).not().hasText(Pattern.compile(Pattern.quote("$9x99")));
  }

  @Test
  void shouldNotTreatEscapedBackslashFollowedByQAsQuote() {
    page.setContent("<div>a\\Qb</div>");
    assertThat(page.locator("div")).hasText(Pattern.compile("a\\\\Qb"));
  }

  @Test
  void shouldSerializeQuotedPatternAsEvaluateArgument() {
    Object result = page.evaluate("r => [r.source, r.test('a.b(c)'), r.test('aXbc')]", Pattern.compile(Pattern.quote("a.b(c)")));
    assertEquals(asList("a\\.b\\(c\\)", true, false), result);
  }

  @Test
  void shouldRouteWithQuotedPattern() {
    boolean[] intercepted = {false};
    page.route(Pattern.compile(Pattern.quote(server.EMPTY_PAGE)), route -> {
      intercepted[0] = true;
      route.fulfill(new Route.FulfillOptions().setBody("intercepted"));
    });
    page.navigate(server.EMPTY_PAGE);
    assertTrue(intercepted[0]);
    assertThat(page.locator("body")).hasText("intercepted");
  }
}
