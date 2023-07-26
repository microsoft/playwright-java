package com.microsoft.playwright.assertions;

import com.microsoft.playwright.Page;
import org.opentest4j.AssertionFailedError;

import java.util.ArrayList;
import java.util.List;

class SoftAssertionsImpl implements SoftAssertions {
  final List<Throwable> results;

  SoftAssertionsImpl() {
    this.results = new ArrayList<>();
  }

  @Override
  public PageAssertions assertThat(Page page) {
    return new PageAssertionsProxy(page, results);
  }

  @Override
  public void assertAll() {
    if (!results.isEmpty()) {
      throw new AssertionFailedError(getFormattedErrorMessage());
    }
  }

  private String getFormattedErrorMessage() {
    StringBuilder message = new StringBuilder();
    message
      .append(results.size())
      .append(" assertion(s) failed:");

    for (Throwable t : results) {
      message.append("\n");
      message.append("----------------------------------------\n");
      message.append(t.getMessage());
    }

    return message.toString();
  }

}
