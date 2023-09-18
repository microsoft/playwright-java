package com.microsoft.playwright.junit;

import com.microsoft.playwright.Playwright;

public interface PlaywrightFactory {
  Playwright newPlaywright();

}
