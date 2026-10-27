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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * The fixtures of one test invocation: resolves and caches test-scoped values, borrows a
 * {@link Worker} for the worker-scoped ones, and tears everything down when the test is over.
 */
final class TestScope implements AutoCloseable {
  // The method timeout does not cover the fixtures, and a hang there stops the whole run without a trace.
  private static final long HANG_DUMP_MINUTES = 5;
  private static final ScheduledThreadPoolExecutor hangWatchdog = new ScheduledThreadPoolExecutor(1, runnable -> {
    Thread thread = new Thread(runnable, "test hang watchdog");
    thread.setDaemon(true);
    return thread;
  });

  static {
    hangWatchdog.setRemoveOnCancelPolicy(true);
  }

  private final ExtensionContext context;
  private final FixtureRegistry registry;
  private final BrowserName browserName;
  private final FixtureValues values = new FixtureValues();
  private final Set<FixtureDefinition> resolving = new LinkedHashSet<>();
  private Worker worker;
  private final ScheduledFuture<?> hangDump;

  TestScope(ExtensionContext context, FixtureRegistry registry, BrowserName browserName) {
    this.context = context;
    this.registry = registry;
    this.browserName = browserName;
    hangDump = hangWatchdog.schedule(this::dumpThreads, HANG_DUMP_MINUTES, TimeUnit.MINUTES);
  }

  private void dumpThreads() {
    StringBuilder dump = new StringBuilder("\n" + context.getRequiredTestClass().getName() + "." + context.getRequiredTestMethod().getName() + " " + context.getDisplayName()
      + " is still running after " + HANG_DUMP_MINUTES + " minutes, the threads:\n");
    for (Map.Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet()) {
      dump.append('"').append(entry.getKey().getName()).append("\" ").append(entry.getKey().getState()).append('\n');
      for (StackTraceElement frame : entry.getValue()) {
        dump.append("\tat ").append(frame).append('\n');
      }
    }
    System.out.println(dump);
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
      Object value = values.get(definition);
      if (value == null) {
        value = create(definition);
        values.add(definition, value);
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

  /** Closes the test fixtures and returns the worker; a failure to close any of them fails the test. */
  @Override
  public void close() throws Exception {
    Teardown teardown = new Teardown();
    values.close(teardown);
    if (worker != null) {
      // Like Playwright Test, do not reuse a worker after a failure: the test may have left the
      // browser or the Playwright instance in an unknown state.
      boolean discard = teardown.failed()
        || context.getExecutionException().filter(e -> !(e instanceof TestAbortedException) || e instanceof ExpectedFailure).isPresent();
      teardown.run(() -> WorkerPool.INSTANCE.release(worker, discard));
      worker = null;
    }
    hangDump.cancel(false);
    teardown.rethrow();
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

    @Override
    public ExtensionContext extensionContext() {
      if (from.scope() == Scope.WORKER) {
        throw new IllegalStateException("Worker-scoped " + from.describe() + " cannot depend on the test context");
      }
      return context;
    }
  }
}
