package com.microsoft.playwright.junit;

import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.extension.*;

import static org.junit.platform.commons.support.AnnotationSupport.isAnnotated;

public class PlaywrightExtension implements ParameterResolver, AfterAllCallback {
  private static final ThreadLocal<Playwright> playwrightThreadLocal;

  static {
    playwrightThreadLocal = new ThreadLocal<>();
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    if (!isAnnotated(extensionContext.getTestClass(), UseBrowserFactory.class)) {
      return false;
    }
    Class<?> clazz = parameterContext.getParameter().getType();
    return Playwright.class.equals(clazz);
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    return getOrCreatePlaywright();
  }

  static Playwright getOrCreatePlaywright() {
    if (playwrightThreadLocal.get() == null) {
      playwrightThreadLocal.set(Playwright.create());
      System.out.println("Created Playwright " + getPlaywright());
    }
    return getPlaywright();
  }

  private static Playwright getPlaywright() {
    return playwrightThreadLocal.get();
  }

  private static void closePlaywright() {
    if (getPlaywright() != null) {
      System.out.println("Closing Playwright " + getPlaywright());
      getPlaywright().close();
      playwrightThreadLocal.remove();
    }
  }

  @Override
  public void afterAll(ExtensionContext extensionContext) {
    closePlaywright();
  }
}
