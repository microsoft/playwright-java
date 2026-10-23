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

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.opentest4j.TestAbortedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The fixtures of one test invocation: resolves and caches test-scoped values, borrows a
 * {@link Worker} for the worker-scoped ones, and tears everything down when the test is over.
 */
final class TestScope implements AutoCloseable {
  private static final class Entry {
    final FixtureDefinition definition;
    final Object value;

    Entry(FixtureDefinition definition, Object value) {
      this.definition = definition;
      this.value = value;
    }
  }

  private final ExtensionContext context;
  private final FixtureRegistry registry;
  private final BrowserName browserName;
  private final Map<Object, Object> values = new HashMap<>();
  private final List<Entry> created = new ArrayList<>();
  private final Set<FixtureDefinition> resolving = new LinkedHashSet<>();
  private Worker worker;

  TestScope(ExtensionContext context, FixtureRegistry registry, BrowserName browserName) {
    this.context = context;
    this.registry = registry;
    this.browserName = browserName;
  }

  <T> T get(Class<T> type) {
    List<FixtureDefinition> chain = registry.chain(type);
    if (chain.isEmpty()) {
      throw new ParameterResolutionException("No fixture of type " + type.getName());
    }
    return type.cast(resolve(chain.get(chain.size() - 1)));
  }

  private Object resolve(FixtureDefinition definition) {
    try {
      if (definition.scope() == Scope.WORKER) {
        return worker().get(definition, new Resolver(definition));
      }
      Object value = values.get(definition.identity());
      if (value == null) {
        value = create(definition);
        values.put(definition.identity(), value);
        created.add(new Entry(definition, value));
      }
      return value;
    } catch (RuntimeException | Error e) {
      throw e;
    } catch (Exception e) {
      throw new IllegalStateException(definition.describe() + " failed", e);
    }
  }

  private Object create(FixtureDefinition definition) throws Exception {
    if (!resolving.add(definition)) {
      throw new IllegalStateException("Fixture dependency cycle: " + resolving + " -> " + definition.describe());
    }
    try {
      Object value = definition.create(new Resolver(definition));
      if (value == null) {
        throw new IllegalStateException(definition.describe() + " returned null");
      }
      return value;
    } finally {
      resolving.remove(definition);
    }
  }

  private Worker worker() {
    if (worker == null) {
      worker = WorkerPool.INSTANCE.acquire(new Worker.Key(browserName, registry.workerMethods()));
    }
    return worker;
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
    if (worker != null) {
      // Like Playwright Test, do not reuse a worker after a failure: the test may have left the
      // browser or the Playwright instance in an unknown state.
      boolean failed = context.getExecutionException().filter(e -> !(e instanceof TestAbortedException)).isPresent();
      WorkerPool.INSTANCE.release(worker, failed);
      worker = null;
    }
  }

  private final class Resolver implements FixtureResolver {
    private final FixtureDefinition from;

    Resolver(FixtureDefinition from) {
      this.from = from;
    }

    @Override
    public <T> T get(Class<T> type) {
      List<FixtureDefinition> chain = registry.chain(type);
      FixtureDefinition target;
      if (type.equals(from.type())) {
        int index = chain.indexOf(from);
        if (index <= 0) {
          throw new IllegalStateException(from.describe() + " does not override another fixture of type " + type.getName());
        }
        target = chain.get(index - 1);
      } else {
        if (chain.isEmpty()) {
          throw new IllegalStateException(from.describe() + " depends on unknown fixture type " + type.getName());
        }
        target = chain.get(chain.size() - 1);
      }
      if (from.scope() == Scope.WORKER && target.scope() == Scope.TEST) {
        throw new IllegalStateException("Worker-scoped " + from.describe() + " cannot depend on test-scoped " + target.describe());
      }
      return type.cast(resolve(target));
    }

    @Override
    public Object testInstance(Class<?> declaringClass) {
      if (from.scope() == Scope.WORKER) {
        throw new IllegalStateException("Worker-scoped " + from.describe() + " must be static");
      }
      return context.getRequiredTestInstances().findInstance(declaringClass)
        .orElseThrow(() -> new IllegalStateException("No test instance of " + declaringClass.getName() + " to invoke " + from.describe()));
    }
  }
}
