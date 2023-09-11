package com.microsoft.playwright.impl;

import com.microsoft.playwright.WebError;

public class WebErrorImpl implements WebError {
  private final PageImpl page;
  private final String error;

  WebErrorImpl(PageImpl page, String error) {
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
