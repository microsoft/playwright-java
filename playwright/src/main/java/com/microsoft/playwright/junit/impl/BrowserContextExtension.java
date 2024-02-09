package com.microsoft.playwright.junit.impl;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.impl.Utils;
import com.microsoft.playwright.junit.Options;
import org.junit.jupiter.api.extension.*;

import static com.microsoft.playwright.junit.impl.ExtensionUtils.isClassHook;
import static com.microsoft.playwright.junit.impl.ExtensionUtils.isParameterSupported;

public class BrowserContextExtension implements ParameterResolver, AfterEachCallback {
  private static final ThreadLocal<BrowserContext> threadLocalBrowserContext = new ThreadLocal<>();

  @Override
  public void afterEach(ExtensionContext extensionContext) {
    BrowserContext browserContext = threadLocalBrowserContext.get();
    threadLocalBrowserContext.remove();
    if (browserContext != null) {
      browserContext.close();
    }
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    return !isClassHook(extensionContext) && isParameterSupported(parameterContext, extensionContext, BrowserContext.class);
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    return getOrCreateBrowserContext(extensionContext);
  }

  static BrowserContext getOrCreateBrowserContext(ExtensionContext extensionContext) {
    BrowserContext browserContext = threadLocalBrowserContext.get();
    if (browserContext != null) {
      return browserContext;
    }

    Options options = OptionsExtension.getOptions(extensionContext);
    Playwright playwright = PlaywrightExtension.getOrCreatePlaywright(extensionContext);
    Browser browser = BrowserExtension.getOrCreateBrowser(extensionContext);
    Browser.NewContextOptions contextOptions = getContextOptions(playwright, options);
    browserContext = browser.newContext(contextOptions);
    threadLocalBrowserContext.set(browserContext);
    return browserContext;
  }

  private static Browser.NewContextOptions getContextOptions(Playwright playwright, Options options) {
    Browser.NewContextOptions contextOptions = Utils.clone(options.getContextOptions());
    if (contextOptions == null) {
      contextOptions = new Browser.NewContextOptions();
    }

    if (options.getBaseUrl() != null) {
      contextOptions.setBaseURL(options.getBaseUrl());
    }

    if (options.getStorageStatePath() != null) {
      contextOptions.setStorageStatePath(options.getStorageStatePath());
    }

    if (options.getDeviceName() != null) {
      DeviceDescriptor deviceDescriptor = DeviceDescriptor.findByName(playwright, options.getDeviceName());
      if (deviceDescriptor == null) {
        throw new PlaywrightException("Unknown device name: " + options.getDeviceName());
      }
      contextOptions.userAgent = deviceDescriptor.userAgent;
      if (deviceDescriptor.viewport != null) {
        contextOptions.setViewportSize(deviceDescriptor.viewport.width, deviceDescriptor.viewport.height);
      }
      contextOptions.deviceScaleFactor = deviceDescriptor.deviceScaleFactor;
      contextOptions.isMobile = deviceDescriptor.isMobile;
      contextOptions.hasTouch = deviceDescriptor.hasTouch;
    }

    if (options.getViewportSize() != null) {
      contextOptions.setViewportSize(options.getViewportSize());
    }

    return contextOptions;
  }
}
