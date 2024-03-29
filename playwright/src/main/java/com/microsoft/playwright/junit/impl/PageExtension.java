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

package com.microsoft.playwright.junit.impl;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.extension.*;

import static com.microsoft.playwright.junit.impl.ExtensionUtils.isClassHook;
import static com.microsoft.playwright.junit.impl.ExtensionUtils.isParameterSupported;

public class PageExtension implements ParameterResolver, AfterEachCallback {
  private static final ThreadLocal<Page> threadLocalPage = new ThreadLocal<>();

  @Override
  public void afterEach(ExtensionContext extensionContext) {
    Page page = threadLocalPage.get();
    threadLocalPage.remove();
    if (page != null) {
      page.close();
    }
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    return !isClassHook(extensionContext) && isParameterSupported(parameterContext, extensionContext, Page.class);
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    return getOrCreatePage(extensionContext);
  }

  /**
   * Returns the Page that belongs to the current test.  Will be created if it doesn't already exist.
   * <strong>NOTE:</strong> this method is subject to change.
   * @param extensionContext the context in which the current test or container is being executed.
   * @return The Page that belongs to the current test.
   */
  public static Page getOrCreatePage(ExtensionContext extensionContext) {
    Page page = threadLocalPage.get();
    if (page != null) {
      return page;
    }

    BrowserContext browserContext = BrowserContextExtension.getOrCreateBrowserContext(extensionContext);
    page = browserContext.newPage();
    threadLocalPage.set(page);
    return page;
  }
}
