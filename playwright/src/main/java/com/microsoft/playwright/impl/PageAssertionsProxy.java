package com.microsoft.playwright.impl;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.PageAssertions;

import java.util.List;
import java.util.regex.Pattern;

class PageAssertionsProxy extends SoftAssertionsBase implements PageAssertions {
  private final PageAssertionsImpl pageAssertions;

  PageAssertionsProxy(Page page, List<Throwable> results) {
    super(results);
    this.pageAssertions = new PageAssertionsImpl(page);
  }

  @Override
  public PageAssertions not() {
    return pageAssertions.not();
  }

  @Override
  public void hasTitle(String titleOrRegExp, HasTitleOptions options) {
    assertAndCaptureResult(() -> pageAssertions.hasTitle(titleOrRegExp, options));
  }

  @Override
  public void hasTitle(Pattern titleOrRegExp, HasTitleOptions options) {
    assertAndCaptureResult(() -> pageAssertions.hasTitle(titleOrRegExp, options));
  }

  @Override
  public void hasURL(String urlOrRegExp, HasURLOptions options) {
    assertAndCaptureResult(() -> pageAssertions.hasURL(urlOrRegExp, options));
  }

  @Override
  public void hasURL(Pattern urlOrRegExp, HasURLOptions options) {
    assertAndCaptureResult(() -> pageAssertions.hasURL(urlOrRegExp, options));
  }
}
