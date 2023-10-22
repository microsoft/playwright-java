package com.microsoft.playwright.junit.impl;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.junit.Options;
import org.junit.jupiter.api.extension.*;

import static com.microsoft.playwright.junit.impl.ExtensionUtils.hasUsePlaywrightAnnotation;

public class BrowserExtension implements ParameterResolver, AfterAllCallback {
  private static final ThreadLocal<Browser> threadLocalBrowser = new ThreadLocal<>();

  @Override
  public void afterAll(ExtensionContext extensionContext) {
    try {
      Browser browser = threadLocalBrowser.get();
      if (browser != null) {
        browser.close();
      }
    } finally {
      threadLocalBrowser.remove();
    }
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

  static Browser getOrCreateBrowser(ExtensionContext extensionContext) {
    Browser browser = threadLocalBrowser.get();
    if (browser != null) {
      return browser;
    }

    Options options = OptionsExtension.getOptions(extensionContext);
    Playwright playwright = PlaywrightExtension.getOrCreatePlaywright(extensionContext);
    BrowserType.LaunchOptions launchOptions = getLaunchOptions(options);


    switch (options.getBrowserName()) {
      case "webkit":
        browser =  playwright.webkit().launch(launchOptions);
        break;
      case "firefox":
        browser =  playwright.firefox().launch(launchOptions);
        break;
      case "chromium":
        browser =  playwright.chromium().launch(launchOptions);
        break;
      default:
        throw new PlaywrightException("Invalid browser name.");
    }

    threadLocalBrowser.set(browser);
    return browser;
  }

  private static BrowserType.LaunchOptions getLaunchOptions(Options options) {
    BrowserType.LaunchOptions launchOptions = options.getLaunchOptions();
    if(launchOptions == null) {
      launchOptions = new BrowserType.LaunchOptions();
    }

    launchOptions.setHeadless(options.isHeadless());

    if(options.getChannel() != null) {
      options.setChannel(options.getChannel());
    }

    return launchOptions;
  }
}
