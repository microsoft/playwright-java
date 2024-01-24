package com.microsoft.playwright.junit.impl;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.impl.Utils;
import com.microsoft.playwright.junit.Options;
import org.junit.jupiter.api.extension.*;

import static com.microsoft.playwright.junit.impl.ExtensionUtils.isParameterSupported;

public class BrowserExtension implements ParameterResolver, AfterAllCallback {
  private static final ThreadLocal<Browser> threadLocalBrowser = new ThreadLocal<>();

  @Override
  public void afterAll(ExtensionContext extensionContext) {
    Browser browser = threadLocalBrowser.get();
    threadLocalBrowser.remove();
    if (browser != null) {
      browser.close();
    }
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    return isParameterSupported(parameterContext, extensionContext, Browser.class);
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
        browser = playwright.webkit().launch(launchOptions);
        break;
      case "firefox":
        browser = playwright.firefox().launch(launchOptions);
        break;
      case "chromium":
        browser = playwright.chromium().launch(launchOptions);
        break;
      default:
        throw new PlaywrightException("Invalid browser name.");
    }

    threadLocalBrowser.set(browser);
    return browser;
  }

  private static BrowserType.LaunchOptions getLaunchOptions(Options options) {
    BrowserType.LaunchOptions launchOptions = Utils.clone(options.getLaunchOptions());
    if (launchOptions == null) {
      launchOptions = new BrowserType.LaunchOptions();
    }

    if (options.isHeadless() != null) {
      launchOptions.setHeadless(options.isHeadless());
    }

    if (options.getChannel() != null) {
      launchOptions.setChannel(options.getChannel());
    }

    return launchOptions;
  }
}
