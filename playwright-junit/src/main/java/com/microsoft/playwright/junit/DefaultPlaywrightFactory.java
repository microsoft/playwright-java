package com.microsoft.playwright.junit;

import com.microsoft.playwright.Playwright;

class DefaultPlaywrightFactory implements PlaywrightFactory {
  @Override
  public Playwright newPlaywright() {
    return Playwright.create();
  }
}
