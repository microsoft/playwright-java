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

import java.util.List;
import java.util.regex.Pattern;

public class PageAssertionsImplProxy extends SoftAssertionsBase implements PageAssertions {
  private final PageAssertionsImpl pageAssertionsImpl;

  PageAssertionsImplProxy(Page page, List<Throwable> results) {
    this(results, new PageAssertionsImpl(page));
  }

  public PageAssertionsImplProxy(List<Throwable> results, PageAssertionsImpl pageAssertionsImpl) {
    super(results);
    this.pageAssertionsImpl = pageAssertionsImpl;
  }

  @Override
  public PageAssertions not() {
    return new PageAssertionsImplProxy(super.results, pageAssertionsImpl.not());
  }

  @Override
  public void hasTitle(String titleOrRegExp, HasTitleOptions options) {
    assertAndCaptureResult(() -> pageAssertionsImpl.hasTitle(titleOrRegExp, options));
  }

  @Override
  public void hasTitle(Pattern titleOrRegExp, HasTitleOptions options) {
    assertAndCaptureResult(() -> pageAssertionsImpl.hasTitle(titleOrRegExp, options));
  }

  @Override
  public void hasURL(String urlOrRegExp, HasURLOptions options) {
    assertAndCaptureResult(() -> pageAssertionsImpl.hasURL(urlOrRegExp, options));
  }

  @Override
  public void hasURL(Pattern urlOrRegExp, HasURLOptions options) {
    assertAndCaptureResult(() -> pageAssertionsImpl.hasURL(urlOrRegExp, options));
  }
}
