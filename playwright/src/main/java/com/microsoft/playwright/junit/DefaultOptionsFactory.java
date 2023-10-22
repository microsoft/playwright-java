package com.microsoft.playwright.junit;

public class DefaultOptionsFactory implements OptionsFactory {
  @Override
  public Options getOptions() {
    return new Options();
  }
}
