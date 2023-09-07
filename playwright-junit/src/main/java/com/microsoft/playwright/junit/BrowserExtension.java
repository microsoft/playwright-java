package com.microsoft.playwright.junit;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import org.junit.jupiter.api.extension.*;

import static com.microsoft.playwright.junit.PlaywrightExtension.getOrCreatePlaywright;
import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;
import static org.junit.platform.commons.support.AnnotationSupport.isAnnotated;

public class BrowserExtension implements ParameterResolver, AfterAllCallback {
  private static final ThreadLocal<Browser> threadLocalBrowser;
  private static final ThreadLocal<BrowserFactory> threadLocalBrowserFactory;

  static {
    threadLocalBrowser = new ThreadLocal<>();
    threadLocalBrowserFactory = new ThreadLocal<>();
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    if (!isAnnotated(extensionContext.getTestClass(), UseBrowserFactory.class)) {
      return false;
    }
    Class<?> clazz = parameterContext.getParameter().getType();
    return Browser.class.equals(clazz);
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    BrowserFactory factory = getBrowserFactoryInstance(extensionContext);
    return getOrCreateBrowser(factory);
  }

  public static Browser getOrCreateBrowser(BrowserFactory factory) {
    Browser browser = getBrowser();
    if (browser != null) {
      return browser;
    }

    Playwright playwright = getOrCreatePlaywright();
    browser = factory.newBrowser(playwright);
    System.out.println("Created Browser " + browser);
    threadLocalBrowser.set(browser);
    return browser;
  }

  private static Browser getBrowser() {
    return threadLocalBrowser.get();
  }

  private static BrowserFactory getBrowserFactoryInstance(ExtensionContext extensionContext) {
    if (threadLocalBrowserFactory.get() != null) {
      return threadLocalBrowserFactory.get();
    }

    UseBrowserFactory useBrowserFactory = findAnnotation(extensionContext.getTestClass(), UseBrowserFactory.class).get();

    try {
      BrowserFactory browserFactory = useBrowserFactory.value().newInstance();
      threadLocalBrowserFactory.set(browserFactory);
      return browserFactory;
    } catch (InstantiationException | IllegalAccessException e) {
      throw new PlaywrightException("Unable to create an instance of the supplied BrowserFactory", e);
    }
  }

  @Override
  public void afterAll(ExtensionContext extensionContext) {
    Browser browser = getBrowser();
    if (browser != null) {
      System.out.println("Closing Browser " + browser);
      browser.close();
    }
    threadLocalBrowser.remove();
    threadLocalBrowserFactory.remove();
  }
}
