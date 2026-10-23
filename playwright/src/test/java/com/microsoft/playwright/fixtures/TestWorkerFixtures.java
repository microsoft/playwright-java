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
import com.microsoft.playwright.BrowserType;
import org.junit.jupiter.api.AfterAll;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class TestWorkerFixtures {
  private static final AtomicInteger created = new AtomicInteger();
  private static final AtomicInteger closed = new AtomicInteger();

  static final class WorkerResource implements AutoCloseable {
    final Browser browser;

    WorkerResource(Browser browser) {
      this.browser = browser;
      created.incrementAndGet();
    }

    @Override
    public void close() {
      closed.incrementAndGet();
    }
  }

  @Fixture(scope = Scope.WORKER)
  static BrowserType.LaunchOptions launchOptions(BrowserType.LaunchOptions defaults) {
    assertEquals(!Env.headed(), defaults.headless);
    return defaults.setTimeout(123456);
  }

  @Fixture(scope = Scope.WORKER)
  static WorkerResource workerResource(Browser browser) {
    return new WorkerResource(browser);
  }

  @AfterAll
  static void workerResourcesOutliveTests() {
    // Worker fixtures are closed with their worker, which the pool may evict while other classes
    // run, never once per test.
    assertTrue(created.get() > 0);
    assertTrue(closed.get() <= created.get(), "created " + created.get() + ", closed " + closed.get());
  }

  @PlaywrightTest
  void shouldLaunchWithOverriddenOptions(BrowserType.LaunchOptions launchOptions, Browser browser, WorkerResource resource) {
    assertEquals(123456, launchOptions.timeout);
    assertTrue(browser.isConnected());
    assertSame(browser, resource.browser);
  }

  @PlaywrightTest
  void shouldReuseWorkerFixtures(Browser browser, WorkerResource resource) {
    assertSame(browser, resource.browser);
  }
}
