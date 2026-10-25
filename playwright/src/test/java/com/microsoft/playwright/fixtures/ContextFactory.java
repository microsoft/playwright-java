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

package com.microsoft.playwright.fixtures;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@code contextFactory} fixture: creates contexts in the test's browser and closes the ones
 * still open when the test ends. Without options, the context gets the test's
 * {@link Browser.NewContextOptions} fixture, like {@code test.use()} options upstream.
 */
public final class ContextFactory implements AutoCloseable {
  private final Browser browser;
  private final Browser.NewContextOptions defaultOptions;
  private final List<BrowserContext> contexts = new ArrayList<>();

  ContextFactory(Browser browser, Browser.NewContextOptions defaultOptions) {
    this.browser = browser;
    this.defaultOptions = defaultOptions;
  }

  public BrowserContext newContext() {
    return newContext(defaultOptions);
  }

  public BrowserContext newContext(Browser.NewContextOptions options) {
    BrowserContext context = browser.newContext(options);
    synchronized (contexts) {
      contexts.add(context);
    }
    return context;
  }

  @Override
  public void close() {
    List<BrowserContext> toClose;
    synchronized (contexts) {
      toClose = new ArrayList<>(contexts);
      contexts.clear();
    }
    for (BrowserContext context : toClose) {
      context.close();
    }
  }
}
