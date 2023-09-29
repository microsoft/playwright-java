package com.microsoft.playwright;

import com.microsoft.playwright.junit.BrowserContextFactory;
import com.microsoft.playwright.options.Cookie;

import java.util.Collections;

public class CustomBrowserContextFactory implements BrowserContextFactory {
  @Override
  public BrowserContext newBrowserContext(Browser browser) {
    BrowserContext context = browser.newContext();
    Cookie cookie = new Cookie("foo", "bar").setUrl("https://microsoft.com");
    context.addCookies(Collections.singletonList(cookie));
    return context;
  }
}
