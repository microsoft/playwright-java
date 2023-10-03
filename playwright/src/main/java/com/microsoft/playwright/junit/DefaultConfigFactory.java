package com.microsoft.playwright.junit;

public class DefaultConfigFactory implements ConfigFactory {
  @Override
  public Config newConfig() {
    return new Config();
  }
}
