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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

  private static final class Entry {
    final FixtureDefinition definition;
    final Object value;

    Entry(FixtureDefinition definition, Object value) {
      this.definition = definition;
      this.value = value;
    }
  }

  final Key key;
  private final Map<Object, Object> values = new HashMap<>();
  private final List<Entry> created = new ArrayList<>();

  Worker(Key key) {
    this.key = key;
    values.put(BuiltinFixture.BROWSER_NAME.identity(), key.browserName);
  }

  Object get(FixtureDefinition definition, FixtureResolver resolver) throws Exception {
    Object value = values.get(definition.identity());
    if (value == null) {
      value = definition.create(resolver);
      if (value == null) {
        throw new IllegalStateException(definition.describe() + " returned null");
      }
      values.put(definition.identity(), value);
      created.add(new Entry(definition, value));
    }
    return value;
  }

  /** A worker whose browser got disconnected cannot serve another test. */
  boolean isHealthy() {
    Object browser = values.get(BuiltinFixture.BROWSER.identity());
    return browser == null || ((Browser) browser).isConnected();
  }

  @Override
  public void close() {
    for (int i = created.size() - 1; i >= 0; i--) {
      Entry entry = created.get(i);
      try {
        entry.definition.close(entry.value);
      } catch (Throwable e) {
        System.err.println("Failed to close " + entry.definition.describe() + ": " + e);
      }
    }
    created.clear();
    values.clear();
  }

  @Override
  public String toString() {
    return "Worker[" + key + "]";
  }
}
