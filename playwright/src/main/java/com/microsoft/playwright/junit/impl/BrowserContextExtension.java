package com.microsoft.playwright.junit.impl;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Tracing;
import com.microsoft.playwright.junit.BrowserContextFactory;
import com.microsoft.playwright.junit.Config;
import com.microsoft.playwright.junit.UsePlaywright;
import org.junit.jupiter.api.extension.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.microsoft.playwright.junit.impl.ExtensionUtils.*;

public class BrowserContextExtension implements ParameterResolver, AfterEachCallback, AfterAllCallback {
  private final static ThreadLocal<BrowserContext> threadLocalBrowserContext;
  private final static ThreadLocal<BrowserContextFactory> threadLocalBrowserContextFactory;

  static {
    threadLocalBrowserContext = new ThreadLocal<>();
    threadLocalBrowserContextFactory = new ThreadLocal<>();
  }

  @Override
  public void afterEach(ExtensionContext extensionContext) {
    cleanupBrowserContext(extensionContext);
  }

  @Override
  public void afterAll(ExtensionContext extensionContext) {
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

    Config config = ConfigExtension.getConfig(extensionContext);
    if (config == null) {
      BrowserContextFactory browserContextFactory = getBrowserContextFactoryInstance(extensionContext);
      browserContext = browserContextFactory.newBrowserContext(browser);
    } else {
      Browser.NewContextOptions options = config.contextOptions();
      browserContext = browser.newContext(options);

      if (config.trace()) {
        // TODO: setSources
        browserContext.tracing().start(new Tracing.StartOptions().setSnapshots(true).setScreenshots(true));
      }
    }

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

  private void cleanupBrowserContext(ExtensionContext extensionContext) {
    try {
      BrowserContext browserContext = threadLocalBrowserContext.get();
      if (browserContext != null) {
        Config config = ConfigExtension.getConfig(extensionContext);
        if (config != null) {
          Path dir = ConfigExtension.outputDirForTest(config, extensionContext);
          try {
            Files.createDirectories(dir);
          } catch (IOException e) {
            throw new PlaywrightException("Failed to create ouptut dir", e);
          }
          browserContext.tracing().stop(new Tracing.StopOptions().setPath(dir.resolve("trace.zip")));
        }
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
