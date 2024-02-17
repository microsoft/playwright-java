package com.microsoft.playwright.junit.impl;

import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.junit.Options;
import org.junit.jupiter.api.extension.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.microsoft.playwright.junit.impl.ExtensionUtils.isParameterSupported;
import static com.microsoft.playwright.junit.impl.ExtensionUtils.setTestIdAttribute;

public class PlaywrightExtension implements ParameterResolver, BeforeAllCallback, ExtensionContext.Store.CloseableResource {
  private static final ThreadLocal<Playwright> threadLocalPlaywright;
  private static final List<Playwright> playwrightList;
  private final static Lock beforeLock;
  private static boolean isTestRunStarted;
  private static final ExtensionContext.Namespace namespace = ExtensionContext.Namespace.create(PlaywrightExtension.class);

  static {
    isTestRunStarted = false;
    beforeLock = new ReentrantLock();
    threadLocalPlaywright = new ThreadLocal<>();
    playwrightList = Collections.synchronizedList(new ArrayList<>());
  }

  // Before a Test class starts, we register a closeable resource
  // We use a lock + boolean to ensure this only gets run once for the entire test run
  @Override
  public void beforeAll(final ExtensionContext context) {
    try {
      beforeLock.lock();
      if (!isTestRunStarted) {
        isTestRunStarted = true;
        context.getRoot().getStore(namespace).put(context.getUniqueId(), this);
      }
    } finally {
      beforeLock.unlock();
    }
  }

  // This is a workaround for JUnit's lack of an "AfterTestRun" hook
  // This will be called once after all tests have completed.
  @Override
  public void close() throws Throwable {
    for (Playwright playwright : playwrightList) {
      playwright.close();
    }
    playwrightList.clear();
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    return isParameterSupported(parameterContext, extensionContext, Playwright.class);

  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    return getOrCreatePlaywright(extensionContext);
  }

  static Playwright getOrCreatePlaywright(ExtensionContext extensionContext) {
    Playwright playwright = threadLocalPlaywright.get();
    if (playwright != null) {
      return playwright;
    }

    Options options = OptionsExtension.getOptions(extensionContext);
    playwright = Playwright.create(options.playwrightCreateOptions);
    threadLocalPlaywright.set(playwright);
    playwrightList.add(playwright);
    setTestIdAttribute(playwright, options);
    return playwright;
  }
}
