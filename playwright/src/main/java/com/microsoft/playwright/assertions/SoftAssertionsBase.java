package com.microsoft.playwright.assertions;

import com.microsoft.playwright.PlaywrightException;
import org.opentest4j.AssertionFailedError;

import java.util.List;

class SoftAssertionsBase {
  final List<Throwable> results;

  public SoftAssertionsBase(List<Throwable> results) {
    this.results = results;
  }

  void assertAndCaptureResult(Runnable assertion) {
    try {
      assertion.run();
    } catch (AssertionFailedError | PlaywrightException failure) {
      results.add(failure);
    }
  }
}
