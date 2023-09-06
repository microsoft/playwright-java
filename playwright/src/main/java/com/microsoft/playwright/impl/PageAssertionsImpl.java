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

import java.util.regex.Pattern;

import static com.microsoft.playwright.impl.UrlMatcher.resolveUrl;
import static com.microsoft.playwright.impl.Utils.convertType;

public class PageAssertionsImpl extends AssertionsBase implements PageAssertions {
  private final PageImpl actualPage;

  public PageAssertionsImpl(Page page) {
    this(page, false);
  }

  private PageAssertionsImpl(Page page, boolean isNot) {
    super((LocatorImpl) page.locator(":root"), isNot);
    this.actualPage = (PageImpl) page;
  }

  @Override
  public void hasTitle(String title, HasTitleOptions options) {
    ExpectedTextValue expected = new ExpectedTextValue();
    expected.string = title;
    expected.normalizeWhiteSpace = true;
    expectImpl("to.have.title", expected, title, "Page title expected to be", convertType(options, FrameExpectOptions.class));
  }

  @Override
  public void hasTitle(Pattern pattern, HasTitleOptions options) {
    ExpectedTextValue expected = expectedRegex(pattern);
    expectImpl("to.have.title", expected, pattern, "Page title expected to match regex", convertType(options, FrameExpectOptions.class));
  }

  @Override
  public void hasURL(String url, HasURLOptions options) {
    ExpectedTextValue expected = new ExpectedTextValue();
    if (actualPage.context().baseUrl != null) {
      url = resolveUrl(actualPage.context().baseUrl, url);
    }
    expected.string = url;
    expectImpl("to.have.url", expected, url, "Page URL expected to be", convertType(options, FrameExpectOptions.class));
  }

  @Override
  public void hasURL(Pattern pattern, HasURLOptions options) {
    ExpectedTextValue expected = expectedRegex(pattern);
    expectImpl("to.have.url", expected, pattern, "Page URL expected to match regex", convertType(options, FrameExpectOptions.class));
  }

  @Override
  public PageAssertions not() {
    return new PageAssertionsImpl(actualPage, !isNot);
  }
}

