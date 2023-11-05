package com.microsoft.playwright.junit.impl;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.junit.Options;
import org.junit.jupiter.api.extension.*;

import static com.microsoft.playwright.junit.impl.ExtensionUtils.hasUsePlaywrightAnnotation;
import static com.microsoft.playwright.junit.impl.ExtensionUtils.isClassHook;

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
    if (isClassHook(extensionContext) || !hasUsePlaywrightAnnotation(extensionContext)) {
      return false;
    }
    Class<?> clazz = parameterContext.getParameter().getType();
    return BrowserContext.class.equals(clazz);
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    return getOrCreateBrowserContext(extensionContext);
  }

  static BrowserContext getOrCreateBrowserContext(ExtensionContext extensionContext) {
    BrowserContext browserContext = threadLocalBrowserContext.get();
    if(browserContext != null) {
      return browserContext;
    }

    Options options = OptionsExtension.getOptions(extensionContext);
    Browser browser = BrowserExtension.getOrCreateBrowser(extensionContext);
    Browser.NewContextOptions contextOptions = getContextOptions(options);
    browserContext = browser.newContext(contextOptions);
    threadLocalBrowserContext.set(browserContext);
    return browserContext;
  }

  private static Browser.NewContextOptions getContextOptions(Options options) {
    Browser.NewContextOptions contextOptions = options.getContextOption();
    if(contextOptions == null) {
      contextOptions = new Browser.NewContextOptions();
    }

    if(options.getBaseUrl() != null) {
      contextOptions.setBaseURL(options.getBaseUrl());
    }

    if(options.getStorageStatePath() != null) {
      contextOptions.setStorageStatePath(options.getStorageStatePath());
    }

    if(options.getViewportSize() != null) {
      contextOptions.setViewportSize(options.getViewportSize());
    }

    return contextOptions;
  }
}
