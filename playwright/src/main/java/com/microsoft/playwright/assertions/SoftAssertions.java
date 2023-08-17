package com.microsoft.playwright.assertions;

import com.microsoft.playwright.Page;

public interface SoftAssertions {
  PageAssertions assertThat(Page page);

  void assertAll();

  static SoftAssertions create() {
    return new SoftAssertionsImpl();
  }
}
