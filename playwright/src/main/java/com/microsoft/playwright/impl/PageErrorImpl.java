package com.microsoft.playwright.impl;

import com.microsoft.playwright.PageError;

public class PageErrorImpl implements PageError {
  private final PageImpl page;
  private final String error;

  PageErrorImpl(PageImpl page, String error) {
    this.page = page;
    this.error = error;
  }

  @Override
  public PageImpl page() {
    return page;
  }

  @Override
  public String error() {
    return error;
  }
}
