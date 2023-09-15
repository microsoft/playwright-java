package com.microsoft.playwright.junit;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.extension.*;

import static com.microsoft.playwright.junit.ExtensionUtils.hasUsePlaywrightAnnotation;

public class PageExtension implements ParameterResolver, BeforeEachCallback, AfterEachCallback, AfterAllCallback {
  private final static ThreadLocal<Page> threadLocalPage;

  static {
    threadLocalPage = new ThreadLocal<>();
  }

  @Override
  public void afterAll(ExtensionContext extensionContext) {
    cleanupPage();
  }

  @Override
  public void afterEach(ExtensionContext extensionContext) {
    cleanupPage();
  }

  @Override
  public void beforeEach(ExtensionContext extensionContext) {
    // Cleanup class-level Page (for example, if one was requested in a BeforeAll callback)
    // This will clean up the ThreadLocal Page for a new Page for the test
    cleanupPage();
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    if (!hasUsePlaywrightAnnotation(extensionContext)) {
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
    System.out.println("Creating Page " + page);
    threadLocalPage.set(page);
    return page;
  }

  private void cleanupPage() {
    Page page = threadLocalPage.get();
    if (page != null) {
      if (!page.isClosed()) {
        System.out.println("Closing Page " + page);
        page.close();
      }
      threadLocalPage.remove();
    }
  }
}
