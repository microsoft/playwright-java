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

package com.microsoft.playwright.impl;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.PageAssertions;
import org.opentest4j.AssertionFailedError;

import java.util.regex.Pattern;

import static com.microsoft.playwright.impl.UrlMatcher.resolveUrl;
import static java.util.Arrays.asList;

public class PageAssertionsImpl implements PageAssertions {
  private final PageImpl actual;
  private final boolean isNot;

  public PageAssertionsImpl(Page page) {
    this(page, false);
  }

  private PageAssertionsImpl(Page page, boolean isNot) {
    this.actual = (PageImpl) page;
    this.isNot = isNot;
  }

  @Override
  public void hasTitle(String title, HasTitleOptions options) {
    ExpectedTextValue expected = new ExpectedTextValue();
    expected.string = title;
    expectImpl("to.have.title", expected, title, "Page title expected to be", options == null ? null : options.timeout);
  }

  @Override
  public void hasTitle(Pattern pattern, HasTitleOptions options) {
    //urlOrRegExp.flags();
    ExpectedTextValue expected = new ExpectedTextValue();
    expected.regexSource = pattern.pattern();
    // expected.regexFlags =
    expectImpl("to.have.title", expected, pattern, "Page title expected to match regex", options == null ? null : options.timeout);
  }

  @Override
  public void hasURL(String url, HasURLOptions options) {
    ExpectedTextValue expected = new ExpectedTextValue();
    if (actual.context().baseUrl != null) {
      url = resolveUrl(actual.context().baseUrl, url);
    }
    expected.string = url;
    expectImpl("to.have.url", expected, url, "Page URL expected to be", options == null ? null : options.timeout);
  }

  @Override
  public void hasURL(Pattern pattern, HasURLOptions options) {
    //urlOrRegExp.flags();
    ExpectedTextValue expected = new ExpectedTextValue();
    expected.regexSource = pattern.pattern();
    // expected.regexFlags =
    expectImpl("to.have.url", expected, pattern, "Page URL expected to match regex", options == null ? null : options.timeout);
  }

  private void expectImpl(String expression, ExpectedTextValue textValue, Object expected, String message, Double timeout) {
    LocatorImpl locator = actual.locator(":root");
    FrameExpectOptions expectOptions = new FrameExpectOptions();
    expectOptions.expectedText = asList(textValue);
    expectOptions.isNot = isNot;
    expectOptions.timeout = timeout;
    FrameExpectResult result = locator.expect(expression, expectOptions);
    if (result.matches == isNot) {
      Object actual = result.received == null ? null : Serialization.deserialize(result.received);
      String log = String.join("\n", result.log);
      if (!log.isEmpty()) {
        log = "\nCall log:\n" + log;
      }
      throw new AssertionFailedError(message + log, expected, actual);
    }
  }

  @Override
  public PageAssertions not() {
    return new PageAssertionsImpl(actual, !isNot);
  }


}

