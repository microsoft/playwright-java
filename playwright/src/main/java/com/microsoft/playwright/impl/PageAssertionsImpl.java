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

// Port of matchers.ts.
package com.microsoft.playwright.impl;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.PageAssertions;
import com.microsoft.playwright.impl.protocol.ExpectedTextValue;

import java.util.List;
import java.util.regex.Pattern;

import static com.microsoft.playwright.impl.LocatorAssertionsImpl.serializeExpectedTextValues;
import static com.microsoft.playwright.impl.ToMatchAriaSnapshot.toMatchAriaSnapshot;
import static com.microsoft.playwright.impl.ToMatchText.toMatchText;
import static com.microsoft.playwright.impl.UrlMatcher.constructURLBasedOnBaseURL;

// java: the matchers of a Page, see LocatorAssertionsImpl.
public class PageAssertionsImpl extends AssertionsBase implements PageAssertions {
  private final PageImpl actualPage;

  public PageAssertionsImpl(Page page) {
    this(page, false);
  }

  private PageAssertionsImpl(Page page, boolean isNot) {
    super(isNot);
    this.actualPage = (PageImpl) page;
  }

  @Override
  public PageAssertions not() {
    return new PageAssertionsImpl(actualPage, !isNot);
  }

  @Override
  public void hasTitle(String expected, HasTitleOptions options) {
    toHaveTitle(expected, options);
  }

  @Override
  public void hasTitle(Pattern expected, HasTitleOptions options) {
    toHaveTitle(expected, options);
  }

  private void toHaveTitle(Object expected, HasTitleOptions options) {
    callMatcherAsStep("hasTitle", state -> {
      return toMatchText(state, "hasTitle", actualPage, "Page", (isNot, timeout) -> {
        List<ExpectedTextValue> expectedText = serializeExpectedTextValues(new Object[] {expected}, null, true, null);
        FrameExpectOptions expectOptions = new FrameExpectOptions();
        expectOptions.expectedText = expectedText;
        expectOptions.isNot = isNot;
        expectOptions.timeout = timeout;
        expectOptions.title = state.title;
        return actualPage.mainFrame().expect("to.have.title", expectOptions);
      }, expected, options == null ? null : options.timeout, false);
    });
  }

  @Override
  public void hasURL(String expected, HasURLOptions options) {
    toHaveURL(expected, options);
  }

  @Override
  public void hasURL(Pattern expected, HasURLOptions options) {
    toHaveURL(expected, options);
  }

  // java: the public API has no URLPattern and no predicate, so toHaveURLWithPredicate of toHaveURL.ts is not ported.
  private void toHaveURL(Object expectedParam, HasURLOptions options) {
    callMatcherAsStep("hasURL", state -> {
      String baseURL = actualPage.context().options.baseURL;
      Object expected = expectedParam instanceof String ? constructURLBasedOnBaseURL(baseURL, (String) expectedParam) : expectedParam;
      return toMatchText(state, "hasURL", actualPage, "Page", (isNot, timeout) -> {
        List<ExpectedTextValue> expectedText = serializeExpectedTextValues(new Object[] {expected}, null, null, options == null ? null : options.ignoreCase);
        FrameExpectOptions expectOptions = new FrameExpectOptions();
        expectOptions.expectedText = expectedText;
        expectOptions.isNot = isNot;
        expectOptions.timeout = timeout;
        expectOptions.title = state.title;
        return actualPage.mainFrame().expect("to.have.url", expectOptions);
      }, expected, options == null ? null : options.timeout, false);
    });
  }

  // java: toMatchAriaSnapshot of toMatchAriaSnapshot.ts.
  @Override
  public void matchesAriaSnapshot(String expected, MatchesAriaSnapshotOptions options) {
    callMatcherAsStep("matchesAriaSnapshot", state -> toMatchAriaSnapshot(state, actualPage, expected, options == null ? null : options.timeout));
  }
}
