package com.microsoft.playwright.junit.impl;

import com.microsoft.playwright.junit.Options;
import com.microsoft.playwright.junit.OptionsFactory;

public class DefaultOptions implements OptionsFactory {
  @Override
  public Options getOptions() {
    return new Options();
  }
}
