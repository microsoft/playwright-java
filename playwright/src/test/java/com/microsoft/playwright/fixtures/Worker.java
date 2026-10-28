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
import com.microsoft.playwright.Playwright;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A bundle of worker-scoped fixture values, the counterpart of a Playwright Test worker process.
 * Playwright Java objects are not thread-safe, so a worker is only ever used by one test at a
 * time; tests that run one after another on a thread reuse it through the {@link WorkerPool}.
 */
final class Worker implements AutoCloseable {
  /** Workers with equal keys are interchangeable. */
  static final class Key {
    final BrowserName browserName;
    final List<Method> workerMethods;

    Key(BrowserName browserName, List<Method> workerMethods) {
      this.browserName = browserName;
      this.workerMethods = workerMethods;
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Key)) {
        return false;
      }
      Key other = (Key) o;
      return browserName == other.browserName && workerMethods.equals(other.workerMethods);
    }

    @Override
    public int hashCode() {
      return 31 * browserName.hashCode() + workerMethods.hashCode();
    }

    @Override
    public String toString() {
      return browserName + (workerMethods.isEmpty() ? "" : " " + workerMethods);
    }
  }

  final Key key;
  private final FixtureValues values = new FixtureValues();
  // Contexts created by worker fixtures. They belong to the worker and outlive tests.
  private final Set<BrowserContext> workerContexts = new HashSet<>();

  Worker(Key key) {
    this.key = key;
    values.add(BuiltinFixture.BROWSER_NAME, key.browserName);
  }

  Object get(FixtureDefinition definition, FixtureResolver resolver) throws Exception {
    Object value = values.get(definition);
    if (value == null) {
      Set<BrowserContext> before = contexts();
      value = definition.create(resolver);
      if (value == null) {
        throw new IllegalStateException(definition.describe() + " returned null");
      }
      values.add(definition, value);
      Set<BrowserContext> created = contexts();
      created.removeAll(before);
      workerContexts.addAll(created);
    }
    return value;
  }

  /**
   * Undoes what the last test changed in this worker: closes the contexts that it opened in this
   * worker's browsers and left behind, so that the next test starts with only the contexts of the
   * worker fixtures, and restores the default test id attribute, which Playwright Test sets before
   * every test.
   */
  void resetForNextTest(Teardown teardown) {
    Set<BrowserContext> leaked = contexts();
    leaked.removeAll(workerContexts);
    for (BrowserContext context : leaked) {
      teardown.run(context::close);
    }
    for (Playwright playwright : values.ofType(Playwright.class)) {
      teardown.run(() -> playwright.selectors().setTestIdAttribute("data-testid"));
    }
  }

  private Set<BrowserContext> contexts() {
    Set<BrowserContext> result = new HashSet<>();
    for (Browser browser : values.ofType(Browser.class)) {
      if (browser.isConnected()) {
        result.addAll(browser.contexts());
      }
    }
    return result;
  }

  /** A worker whose browser got disconnected cannot serve another test. */
  boolean isHealthy() {
    return values.ofType(Browser.class).stream().allMatch(Browser::isConnected);
  }

  @Override
  public void close() throws Exception {
    Teardown teardown = new Teardown();
    values.close(teardown);
    teardown.rethrow();
  }

  @Override
  public String toString() {
    return "Worker[" + key + "]";
  }
}
