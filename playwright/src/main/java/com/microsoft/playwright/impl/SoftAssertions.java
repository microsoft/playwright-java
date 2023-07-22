package com.microsoft.playwright.impl;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.PageAssertions;
import org.opentest4j.AssertionFailedError;

import java.util.ArrayList;
import java.util.List;

public class SoftAssertions {
  final List<Throwable> results;

  public SoftAssertions() {
    this.results = new ArrayList<>();
  }

  public PageAssertions assertThat(Page page) {
    return new PageAssertionsProxy(page, results);
  }

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
