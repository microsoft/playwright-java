package com.microsoft.playwright.junit.impl;

import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.junit.Options;
import org.junit.jupiter.api.extension.*;

import static com.microsoft.playwright.junit.impl.ExtensionUtils.isParameterSupported;

public class APIRequestContextExtension implements ParameterResolver, BeforeEachCallback, AfterAllCallback {
  private static final ThreadLocal<APIRequestContext> threadLocalAPIRequestContext = new ThreadLocal<>();

  @Override
  public void beforeEach(ExtensionContext extensionContext) {
    threadLocalAPIRequestContext.remove();
  }

  @Override
  public void afterAll(ExtensionContext extensionContext) {
    threadLocalAPIRequestContext.remove();
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    return isParameterSupported(parameterContext, extensionContext, APIRequestContext.class);
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    return getOrCreateAPIRequestContext(extensionContext);
  }

  static APIRequestContext getOrCreateAPIRequestContext(ExtensionContext extensionContext) {
    APIRequestContext apiRequestContext = threadLocalAPIRequestContext.get();
    if (apiRequestContext != null) {
      return apiRequestContext;
    }

    Options options = OptionsExtension.getOptions(extensionContext);
    Playwright playwright = PlaywrightExtension.getOrCreatePlaywright(extensionContext);
    apiRequestContext = playwright.request().newContext(options.apiRequestOptions);
    threadLocalAPIRequestContext.set(apiRequestContext);
    return apiRequestContext;
  }
}
