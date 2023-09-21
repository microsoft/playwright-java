package com.microsoft.playwright.junit;

import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.junit.PlaywrightFactory;

class DefaultPlaywrightFactory implements PlaywrightFactory {
  @Override
  public Playwright newPlaywright() {
    return Playwright.create();
  }
}
