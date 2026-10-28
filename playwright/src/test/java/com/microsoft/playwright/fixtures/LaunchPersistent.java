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

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;

/**
 * The {@code launchPersistent} fixture: launches one persistent context per test with the
 * configured headless mode and channel, and closes it when the test ends.
 */
public final class LaunchPersistent implements AutoCloseable {
  /** What upstream returns: the persistent context and its first page. */
  public static final class Result {
    public final BrowserContext context;
    public final Page page;

    Result(BrowserContext context, Page page) {
      this.context = context;
      this.page = page;
    }
  }

  private final BrowserType browserType;
  private final CreateUserDataDir createUserDataDir;
  private BrowserContext persistentContext;

  LaunchPersistent(BrowserType browserType, CreateUserDataDir createUserDataDir) {
    this.browserType = browserType;
    this.createUserDataDir = createUserDataDir;
  }

  public Result launch() {
    return launch(new BrowserType.LaunchPersistentContextOptions());
  }

  public synchronized Result launch(BrowserType.LaunchPersistentContextOptions options) {
    if (persistentContext != null) {
      throw new IllegalStateException("can only launch one persistent context");
    }
    if (options.headless == null) {
      options.headless = !Env.headed();
    }
    if (options.channel == null) {
      options.channel = Env.channel();
    }
    persistentContext = browserType.launchPersistentContext(createUserDataDir.create(), options);
    Page page = persistentContext.pages().get(0);
    return new Result(persistentContext, page);
  }

  @Override
  public synchronized void close() {
    if (persistentContext != null) {
      persistentContext.close();
      persistentContext = null;
    }
  }
}
