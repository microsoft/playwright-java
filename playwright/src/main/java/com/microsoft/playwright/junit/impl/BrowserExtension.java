package com.microsoft.playwright.junit.impl;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.junit.*;
import org.junit.jupiter.api.extension.*;

import static com.microsoft.playwright.junit.impl.ExtensionUtils.getUsePlaywrightAnnotation;
import static com.microsoft.playwright.junit.impl.ExtensionUtils.hasUsePlaywrightAnnotation;

public class BrowserExtension implements ParameterResolver, AfterAllCallback {
  private static final ThreadLocal<Browser> threadLocalBrowser;
  private static final ThreadLocal<BrowserFactory> threadLocalBrowserFactory;

  static {
    threadLocalBrowser = new ThreadLocal<>();
    threadLocalBrowserFactory = new ThreadLocal<>();
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    if (!hasUsePlaywrightAnnotation(extensionContext)) {
      return false;
    }
    Class<?> clazz = parameterContext.getParameter().getType();
    return Browser.class.equals(clazz);
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    return getOrCreateBrowser(extensionContext);
  }

  @Override
  public void afterAll(ExtensionContext extensionContext) {
    try {
      Browser browser = threadLocalBrowser.get();
      if (browser != null) {
        browser.close();
      }
    } finally {
      threadLocalBrowser.remove();
      threadLocalBrowserFactory.remove();
    }
  }

  static Browser getOrCreateBrowser(ExtensionContext extensionContext) {
    Browser browser = threadLocalBrowser.get();
    if (browser != null) {
      return browser;
    }

    Playwright playwright = PlaywrightExtension.getOrCreatePlaywright(extensionContext);
    BrowserFactory browserFactory = getBrowserFactoryInstance(extensionContext);

    Config config = ConfigExtension.getConfig(extensionContext);
    if (config == null) {
      browser = browserFactory.newBrowser(playwright);
    } else {
      browser = newBrowser(playwright, config);
    }
    threadLocalBrowser.set(browser);
    return browser;
  }

  private static Browser newBrowser(Playwright playwright, Config config) {
    BrowserType.LaunchOptions launchOptions = config.launchOptions();
    switch (config.browserName()) {
      case "webkit":
        return playwright.webkit().launch(launchOptions);
      case "firefox":
        return playwright.firefox().launch(launchOptions);
      case "chromium":
        return playwright.chromium().launch(launchOptions);
      default:
        throw new PlaywrightException("Invalid value set for browserName.  Must be one of: chromium, firefox, webkit");
    }
  }


  private static BrowserFactory getBrowserFactoryInstance(ExtensionContext extensionContext) {
    if (threadLocalBrowserFactory.get() != null) {
      return threadLocalBrowserFactory.get();
    }

    UsePlaywright usePlaywrightAnnotation = getUsePlaywrightAnnotation(extensionContext);

    try {
      BrowserFactory browserFactory = usePlaywrightAnnotation.browserFactory().newInstance();
      threadLocalBrowserFactory.set(browserFactory);
      return browserFactory;
    } catch (InstantiationException | IllegalAccessException e) {
      throw new PlaywrightException("Unable to create an instance of the supplied BrowserFactory", e);
    }
  }
}
