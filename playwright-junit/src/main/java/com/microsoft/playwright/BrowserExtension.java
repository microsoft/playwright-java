package com.microsoft.playwright;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.util.HashMap;
import java.util.Map;

import static com.microsoft.playwright.ExtensionUtils.hasProperAnnotation;
import static com.microsoft.playwright.PlaywrightExtension.getOrCreatePlaywright;
import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;

public class BrowserExtension implements ParameterResolver {
  private static final ThreadLocal<Map<Class<? extends BrowserFactory>, Browser>> threadLocalBrowserMap;

  static {
    threadLocalBrowserMap = ThreadLocal.withInitial(HashMap::new);
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    if (!hasProperAnnotation(extensionContext)) {
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
    Class<? extends BrowserFactory> browserFactoryClass = factory.getClass();

    if (getBrowser(browserFactoryClass) != null) {
      return getBrowser(factory.getClass());
    }

    Playwright playwright = getOrCreatePlaywright();
    Browser browser = factory.newBrowser(playwright);
    System.out.println("Created Browser " + browser);
    threadLocalBrowserMap.get().put(browserFactoryClass, browser);
    return browser;
  }

  static void closeAllBrowsers() {
    threadLocalBrowserMap.get().keySet().forEach(browserFactoryClass -> {
      Browser browser = getBrowser(browserFactoryClass);
      if (browser != null) {
        System.out.println("Closing Browser " + browser);
        browser.close();
      }
    });
    threadLocalBrowserMap.remove();
  }

  private static Browser getBrowser(Class<? extends BrowserFactory> browserFactoryClass) {
    return threadLocalBrowserMap.get().get(browserFactoryClass);
  }

  private static BrowserFactory getBrowserFactoryInstance(ExtensionContext extensionContext) {
    UseBrowserFactory useBrowserFactory =
      findAnnotation(extensionContext.getTestMethod(), UseBrowserFactory.class)
        .orElse(findAnnotation(extensionContext.getTestClass(), UseBrowserFactory.class)
          .orElseThrow(() -> new PlaywrightException("UseBrowserFactory annotation not found.")));

    try {
      return useBrowserFactory.value().newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw new PlaywrightException("Unable to create an instance of the supplied BrowserFactory", e);
    }
  }
}
