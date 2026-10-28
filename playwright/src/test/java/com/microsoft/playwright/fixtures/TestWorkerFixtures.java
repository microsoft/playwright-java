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
import com.microsoft.playwright.BrowserType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;

// Runs alone and one test at a time, so that no other test can evict the workers whose reuse it checks.
@Execution(ExecutionMode.SAME_THREAD)
@Isolated
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestWorkerFixtures {
  private static final Map<BrowserName, Set<WorkerResource>> resourcesByBrowser = new ConcurrentHashMap<>();
  private static final AtomicInteger closed = new AtomicInteger();

  static final class WorkerResource implements AutoCloseable {
    final Browser browser;
    // Created by a worker fixture, so it is kept open from one test to the next.
    final BrowserContext context;

    WorkerResource(Browser browser) {
      this.browser = browser;
      this.context = browser.newContext();
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

  private static void record(BrowserName browserName, WorkerResource resource) {
    resourcesByBrowser.computeIfAbsent(browserName, k -> new HashSet<>()).add(resource);
  }

  @AfterAll
  static void workerFixturesAreSharedByTests() {
    resourcesByBrowser.forEach((browserName, resources) ->
      assertEquals(1, resources.size(), browserName + " tests did not share the worker: " + resources));
    assertEquals(0, closed.get(), "Worker fixtures were closed while their worker was idle");
  }

  @PlaywrightTest
  @Order(1)
  void shouldLaunchWithOverriddenOptions(BrowserType.LaunchOptions launchOptions, Browser browser, WorkerResource resource, BrowserName browserName) {
    record(browserName, resource);
    assertEquals(123456, launchOptions.timeout);
    assertTrue(browser.isConnected());
    assertSame(browser, resource.browser);
    // Left open on purpose: it must be closed before the next test gets this worker.
    browser.newContext();
  }

  @PlaywrightTest
  @Order(2)
  void shouldReuseWorkerFixtures(Browser browser, WorkerResource resource, BrowserName browserName) {
    record(browserName, resource);
    assertSame(browser, resource.browser);
    assertEquals(singletonList(resource.context), browser.contexts());
  }
}
