package com.microsoft.playwright;

import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.HashMap;
import java.util.Map;

import static com.microsoft.playwright.JUnitUtils.getDefaultExecutionMode;
import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;
import static org.junit.platform.commons.support.AnnotationSupport.isAnnotated;

public class PlaywrightExtension implements ParameterResolver, AfterAllCallback, AfterEachCallback {
  private static final ThreadLocal<Playwright> playwrightThreadLocal;
  private static final ThreadLocal<Map<Class<? extends BrowserFactory>, Browser>> threadLocalBrowserMap;

  static {
    playwrightThreadLocal = new ThreadLocal<>();
    threadLocalBrowserMap = ThreadLocal.withInitial(HashMap::new);
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    if (hasProperAnnotations(extensionContext)) {
      if (getDefaultExecutionMode(extensionContext) == ExecutionMode.CONCURRENT) {
        return extensionContext.getTestMethod().isPresent();
      }
      Class<?> clazz = parameterContext.getParameter().getType();
      return Playwright.class.equals(clazz) || Browser.class.equals(clazz);
    }
    return false;
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    Class<?> clazz = parameterContext.getParameter().getType();
    if (Playwright.class.equals(clazz)) {
      return getOrCreatePlaywright();
    }

    if (Browser.class.equals(clazz)) {
      return getOrCreateBrowser(extensionContext);
    }

    throw new ParameterResolutionException("Unable to resolve Playwright-related parameter");
  }

  @Override
  public void afterAll(ExtensionContext extensionContext) {
    if (getDefaultExecutionMode(extensionContext) == ExecutionMode.SAME_THREAD) {
      threadLocalBrowserMap.get().keySet().forEach(PlaywrightExtension::closeBrowser);
      threadLocalBrowserMap.remove();
      closePlaywright();
    }
  }

  @Override
  public void afterEach(ExtensionContext extensionContext) {
    if (getDefaultExecutionMode(extensionContext) == ExecutionMode.CONCURRENT) {
      threadLocalBrowserMap.get().keySet().forEach(PlaywrightExtension::closeBrowser);
      threadLocalBrowserMap.remove();
      closePlaywright();
    }
  }

  public static Playwright getOrCreatePlaywright() {
    if (playwrightThreadLocal.get() == null) {
      playwrightThreadLocal.set(Playwright.create());
      System.out.println("Created Playwright " + getPlaywright());
    }
    return getPlaywright();
  }

  public static Playwright getPlaywright() {
    return playwrightThreadLocal.get();
  }

  public static void closePlaywright() {
    if (getPlaywright() != null) {
      System.out.println("Closing Playwright " + getPlaywright());
      getPlaywright().close();
      playwrightThreadLocal.remove();
    }
  }

  public static void closeBrowser(Class<? extends BrowserFactory> browserFactoryClass) {
    Browser browser = getBrowser(browserFactoryClass);
    if (browser != null) {
      System.out.println("Closing Browser " + browser);
      browser.close();
    }
  }

  public static Browser getOrCreateBrowser(ExtensionContext extensionContext) {
    BrowserFactory factory = getBrowserFactoryInstance(extensionContext);
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

  public static Browser getBrowser(Class<? extends BrowserFactory> browserFactoryClass) {
    return threadLocalBrowserMap.get().get(browserFactoryClass);
  }

  private boolean hasProperAnnotations(ExtensionContext extensionContext) {
    return isAnnotated(extensionContext.getTestMethod(), UseBrowserFactory.class) ||
      isAnnotated(extensionContext.getTestClass(), UseBrowserFactory.class);
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
