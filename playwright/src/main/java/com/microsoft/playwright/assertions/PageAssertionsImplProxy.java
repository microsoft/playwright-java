package com.microsoft.playwright.assertions;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.impl.PageAssertionsImpl;

import java.util.List;
import java.util.regex.Pattern;

class PageAssertionsImplProxy extends SoftAssertionsBase implements PageAssertions {
  private final PageAssertionsImpl pageAssertions;

  PageAssertionsImplProxy(Page page, List<Throwable> results) {
    super(results);
    this.pageAssertions = new PageAssertionsImpl(page);
  }

  private PageAssertionsImplProxy(List<Throwable> results, PageAssertionsImpl pageAssertions) {
    super(results);
    this.pageAssertions = pageAssertions;
  }

  @Override
  public PageAssertions not() {
    return new PageAssertionsImplProxy(super.results, (PageAssertionsImpl) pageAssertions.not());
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
