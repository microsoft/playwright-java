package com.microsoft.playwright.junit;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.PlaywrightException;
import org.junit.jupiter.api.extension.*;

import static com.microsoft.playwright.junit.ExtensionUtils.*;

class BrowserContextExtension implements ParameterResolver, AfterEachCallback {
  private final static ThreadLocal<BrowserContext> threadLocalBrowserContext;
  private final static ThreadLocal<BrowserContextFactory> threadLocalBrowserContextFactory;

  static {
    threadLocalBrowserContext = new ThreadLocal<>();
    threadLocalBrowserContextFactory = new ThreadLocal<>();
  }

  @Override
  public void afterEach(ExtensionContext extensionContext) {
    cleanupBrowserContext();
    cleanupBrowserContextFactory();
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
    if (browserContext != null) {
      return browserContext;
    }

    Browser browser = BrowserExtension.getOrCreateBrowser(extensionContext);
    BrowserContextFactory browserContextFactory = getBrowserContextFactoryInstance(extensionContext);
    browserContext = browserContextFactory.newBrowserContext(browser);
    threadLocalBrowserContext.set(browserContext);
    return browserContext;
  }

  private static BrowserContextFactory getBrowserContextFactoryInstance(ExtensionContext extensionContext) {
    if (threadLocalBrowserContextFactory.get() != null) {
      return threadLocalBrowserContextFactory.get();
    }

    UsePlaywright usePlaywrightAnnotation = getUsePlaywrightAnnotation(extensionContext);

    try {
      BrowserContextFactory browserContextFactory = usePlaywrightAnnotation.browserContextFactory().newInstance();
      threadLocalBrowserContextFactory.set(browserContextFactory);
      return browserContextFactory;
    } catch (InstantiationException | IllegalAccessException e) {
      throw new PlaywrightException("Unable to create an instance of the supplied BrowserContextFactory", e);
    }
  }

  private void cleanupBrowserContext() {
    try {
      BrowserContext browserContext = threadLocalBrowserContext.get();
      if (browserContext != null) {
        browserContext.close();
      }
    } finally {
      threadLocalBrowserContext.remove();
    }
  }

  private void cleanupBrowserContextFactory() {
    BrowserContextFactory browserContextFactory = threadLocalBrowserContextFactory.get();
    if (browserContextFactory != null) {
      threadLocalBrowserContextFactory.remove();
    }
  }
}
