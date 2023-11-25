package com.microsoft.playwright.junit.impl;

import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.junit.Options;
import org.junit.jupiter.api.extension.*;

import static com.microsoft.playwright.junit.impl.ExtensionUtils.isParameterSupported;

public class PlaywrightExtension implements ParameterResolver, AfterAllCallback {
  private static final ThreadLocal<Playwright> threadLocalPlaywright = new ThreadLocal<>();

  @Override
  public void afterAll(ExtensionContext extensionContext) {
    Playwright playwright = threadLocalPlaywright.get();
    threadLocalPlaywright.remove();
    if (playwright != null) {
      playwright.close();
    }
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    return isParameterSupported(parameterContext, extensionContext, Playwright.class);

  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    return getOrCreatePlaywright(extensionContext);
  }

  static Playwright getOrCreatePlaywright(ExtensionContext extensionContext) {
    Playwright playwright = threadLocalPlaywright.get();
    if (playwright != null) {
      return playwright;
    }

    Options options = OptionsExtension.getOptions(extensionContext);
    playwright = Playwright.create(options.getPlaywrightCreateOptions());
    threadLocalPlaywright.set(playwright);
    return playwright;
  }
}
