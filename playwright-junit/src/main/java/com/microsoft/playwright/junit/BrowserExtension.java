package com.microsoft.playwright.junit;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import org.junit.jupiter.api.extension.*;

import java.util.HashMap;
import java.util.Map;

import static com.microsoft.playwright.junit.ExtensionUtils.hasProperAnnotation;
import static com.microsoft.playwright.junit.PlaywrightExtension.getOrCreatePlaywright;
import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;

public class BrowserExtension implements ParameterResolver, AfterAllCallback {
  private static final ThreadLocal<Map<Class<? extends BrowserFactory>, Browser>> threadLocalBrowserMap;
  private static final ThreadLocal<Map<Class<? extends BrowserFactory>, BrowserFactory>> threadLocalBrowserFactoryMap;

  static {
    threadLocalBrowserMap = ThreadLocal.withInitial(HashMap::new);
    threadLocalBrowserFactoryMap = ThreadLocal.withInitial(HashMap::new);
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

    Browser browser = getBrowser(browserFactoryClass);
    if (browser != null) {
      return browser;
    }

    Playwright playwright = getOrCreatePlaywright();
    browser = factory.newBrowser(playwright);
    System.out.println("Created Browser " + browser);
    threadLocalBrowserMap.get().put(browserFactoryClass, browser);
    return browser;
  }

  private static void closeAllBrowsers() {
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
    UseBrowserFactory useBrowserFactory = findAnnotation(extensionContext.getTestClass(), UseBrowserFactory.class)
      .orElseThrow(() -> new PlaywrightException("UseBrowserFactory annotation not found."));

    if(threadLocalBrowserFactoryMap.get().containsKey(useBrowserFactory.value())) {
      return threadLocalBrowserFactoryMap.get().get(useBrowserFactory.value());
    }

    try {
      BrowserFactory browserFactory =  useBrowserFactory.value().newInstance();
      threadLocalBrowserFactoryMap.get().put(useBrowserFactory.value(), browserFactory);
      return browserFactory;
    } catch (InstantiationException | IllegalAccessException e) {
      throw new PlaywrightException("Unable to create an instance of the supplied BrowserFactory", e);
    }
  }

  @Override
  public void afterAll(ExtensionContext extensionContext) {
    closeAllBrowsers();
  }
}
