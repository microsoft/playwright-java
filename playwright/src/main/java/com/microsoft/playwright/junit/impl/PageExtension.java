package com.microsoft.playwright.junit.impl;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.extension.*;

import static com.microsoft.playwright.junit.impl.ExtensionUtils.hasUsePlaywrightAnnotation;
import static com.microsoft.playwright.junit.impl.ExtensionUtils.isClassHook;

public class PageExtension implements ParameterResolver, AfterEachCallback {
  private final static ThreadLocal<Page> threadLocalPage;

  static {
    threadLocalPage = new ThreadLocal<>();
  }

  @Override
  public void afterEach(ExtensionContext extensionContext) {
    cleanupPage();
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    if (isClassHook(extensionContext) || !hasUsePlaywrightAnnotation(extensionContext)) {
      return false;
    }
    Class<?> clazz = parameterContext.getParameter().getType();
    return Page.class.equals(clazz);
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    return getOrCreatePage(extensionContext);
  }

  private Page getOrCreatePage(ExtensionContext extensionContext) {
    Page page = threadLocalPage.get();
    if (page != null) {
      return page;
    }

    BrowserContext browserContext = BrowserContextExtension.getOrCreateBrowserContext(extensionContext);
    page = browserContext.newPage();
    threadLocalPage.set(page);
    return page;
  }

  private void cleanupPage() {
    try {
      Page page = threadLocalPage.get();
      if (page != null) {
        if (!page.isClosed()) {
          page.close();
        }
      }
    } finally {
      threadLocalPage.remove();
    }

  }
}
