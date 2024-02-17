package com.microsoft.playwright.junit.impl;

import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.junit.Options;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.microsoft.playwright.junit.impl.ExtensionUtils.isParameterSupported;
import static com.microsoft.playwright.junit.impl.ExtensionUtils.setTestIdAttribute;

public class PlaywrightExtension implements ParameterResolver {
  private static final ThreadLocal<Playwright> threadLocalPlaywright = new ThreadLocal<>();
  private static final ExtensionContext.Namespace namespace = ExtensionContext.Namespace.create(PlaywrightExtension.class);

  static
  class PlaywrgihtRegistry implements ExtensionContext.Store.CloseableResource {
    private final List<Playwright> playwrightList = Collections.synchronizedList(new ArrayList<>());

    static synchronized PlaywrgihtRegistry getOrCreateFor(ExtensionContext extensionContext) {
      ExtensionContext.Store rootStore = extensionContext.getRoot().getStore(namespace);
      PlaywrgihtRegistry instance = (PlaywrgihtRegistry) rootStore.get(PlaywrgihtRegistry.class);
      if (instance == null) {
        instance = new PlaywrgihtRegistry();
        rootStore.put(PlaywrgihtRegistry.class, instance);
      }
      return instance;
    }

    Playwright createPlaywright(Playwright.CreateOptions options) {
      Playwright playwright = Playwright.create(options);
      playwrightList.add(playwright);
      return playwright;
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
    PlaywrgihtRegistry registry = PlaywrgihtRegistry.getOrCreateFor(extensionContext);
    playwright = registry.createPlaywright(options.playwrightCreateOptions);
    threadLocalPlaywright.set(playwright);

    setTestIdAttribute(playwright, options);
    return playwright;
  }
}
