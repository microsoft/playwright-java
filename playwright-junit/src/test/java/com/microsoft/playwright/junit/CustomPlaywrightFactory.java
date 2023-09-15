package com.microsoft.playwright.junit;

import com.microsoft.playwright.Playwright;

import java.util.HashMap;
import java.util.Map;

public class CustomPlaywrightFactory implements PlaywrightFactory {
  @Override
  public Playwright newPlaywright() {
    Map<String, String> options = new HashMap<>();
    options.put("foo", "bar");
    return Playwright.create(new Playwright.CreateOptions().setEnv(options));
  }
}
