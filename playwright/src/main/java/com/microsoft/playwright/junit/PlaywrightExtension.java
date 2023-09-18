package com.microsoft.playwright.junit;

import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import org.junit.jupiter.api.extension.*;

import static com.microsoft.playwright.junit.ExtensionUtils.getUsePlaywrightAnnotation;
import static com.microsoft.playwright.junit.ExtensionUtils.hasUsePlaywrightAnnotation;

class PlaywrightExtension implements ParameterResolver, AfterAllCallback {
  private static final ThreadLocal<Playwright> threadLocalPlaywright;
  private static final ThreadLocal<PlaywrightFactory> threadLocalPlaywrightFactory;

  static {
    threadLocalPlaywright = new ThreadLocal<>();
    threadLocalPlaywrightFactory = new ThreadLocal<>();
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    if (!hasUsePlaywrightAnnotation(extensionContext)) {
      return false;
    }
    Class<?> clazz = parameterContext.getParameter().getType();
    return Playwright.class.equals(clazz);
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    return getOrCreatePlaywright(extensionContext);
  }

  @Override
  public void afterAll(ExtensionContext extensionContext) {
    closePlaywright();
  }

  static Playwright getOrCreatePlaywright(ExtensionContext extensionContext) {
    Playwright playwright = threadLocalPlaywright.get();
    if (playwright != null) {
      return playwright;
    }

    PlaywrightFactory playwrightFactory = getPlaywrightFactoryInstance(extensionContext);
    playwright = playwrightFactory.newPlaywright();
    threadLocalPlaywright.set(playwright);
    return playwright;
  }

  private static PlaywrightFactory getPlaywrightFactoryInstance(ExtensionContext extensionContext) {
    if (threadLocalPlaywrightFactory.get() != null) {
      return threadLocalPlaywrightFactory.get();
    }

    UsePlaywright usePlaywrightAnnotation = getUsePlaywrightAnnotation(extensionContext);

    try {
      PlaywrightFactory playwrightFactory = usePlaywrightAnnotation.playwrightFactory().newInstance();
      threadLocalPlaywrightFactory.set(playwrightFactory);
      return playwrightFactory;
    } catch (InstantiationException | IllegalAccessException e) {
      throw new PlaywrightException("Unable to create an instance of the supplied PlaywrightFactory", e);
    }
  }

  private static void closePlaywright() {
    if (threadLocalPlaywright.get() != null) {
      threadLocalPlaywright.get().close();
      threadLocalPlaywright.remove();
    }
  }
}
