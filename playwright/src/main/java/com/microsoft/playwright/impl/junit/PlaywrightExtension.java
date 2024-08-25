/*
 * Copyright (c) Microsoft Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.playwright.impl.junit;

import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.junit.Options;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.microsoft.playwright.impl.junit.ExtensionUtils.*;

public class PlaywrightExtension implements ParameterResolver {
  private static final ThreadLocal<Playwright> threadLocalPlaywright = new ThreadLocal<>();
  static final ExtensionContext.Namespace namespace = ExtensionContext.Namespace.create(PlaywrightExtension.class);

  // There should be at most one instance of PlaywrightRegistry per test run, it keeps
  // track of all created Playwright instances and calls `close()` on each of them after
  // the tests finished.
  static class PlaywrightRegistry implements ExtensionContext.Store.CloseableResource {
    private final List<Playwright> playwrightList = Collections.synchronizedList(new ArrayList<>());

    static synchronized PlaywrightRegistry getOrCreateFor(ExtensionContext extensionContext) {
      ExtensionContext.Store rootStore = extensionContext.getRoot().getStore(namespace);
      PlaywrightRegistry instance = (PlaywrightRegistry) rootStore.get(PlaywrightRegistry.class);
      if (instance == null) {
        instance = new PlaywrightRegistry();
        rootStore.put(PlaywrightRegistry.class, instance);
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

  /**
   * Returns the Playwright that belongs to the current test.  Will be created if it doesn't already exist.
   * <strong>NOTE:</strong> this method is subject to change.
   * @param extensionContext the context in which the current test or container is being executed.
   * @return The Playwright that belongs to the current test.
   */
  public static Playwright getOrCreatePlaywright(ExtensionContext extensionContext) {
    Playwright playwright = threadLocalPlaywright.get();
    if (playwright != null) {
      return playwright;
    }

    Options options = OptionsExtension.getOptions(extensionContext);
    PlaywrightRegistry registry = PlaywrightRegistry.getOrCreateFor(extensionContext);
    playwright = registry.createPlaywright(options.playwrightCreateOptions);
    threadLocalPlaywright.set(playwright);

    setTestIdAttribute(playwright, options);
    return playwright;
  }
}
