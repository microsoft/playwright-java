package com.microsoft.playwright.junit;

import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.impl.junit.PlaywrightExtension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import static com.microsoft.playwright.Utils.getBrowserTypeFromEnv;

public class BrowserTypeParameterResolver implements ParameterResolver {
  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    return BrowserType.class.equals(parameterContext.getParameter().getType());
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    Playwright playwright = PlaywrightExtension.getOrCreatePlaywright(extensionContext);
    return getBrowserTypeFromEnv(playwright);
  }
}
