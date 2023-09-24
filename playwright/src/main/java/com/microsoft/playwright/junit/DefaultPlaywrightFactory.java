package com.microsoft.playwright.junit;

import com.microsoft.playwright.Playwright;

public class DefaultPlaywrightFactory implements PlaywrightFactory {
  @Override
  public Playwright newPlaywright() {
    return Playwright.create();
  }
}
