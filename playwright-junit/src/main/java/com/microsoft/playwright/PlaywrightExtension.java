package com.microsoft.playwright;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import static com.microsoft.playwright.ExtensionUtils.hasProperAnnotation;

public class PlaywrightExtension implements ParameterResolver {
  private static final ThreadLocal<Playwright> playwrightThreadLocal;

  static {
    playwrightThreadLocal = new ThreadLocal<>();
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    if (!hasProperAnnotation(extensionContext)) {
      return false;
    }
    Class<?> clazz = parameterContext.getParameter().getType();
    return Playwright.class.equals(clazz);
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    return getOrCreatePlaywright();
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
}
