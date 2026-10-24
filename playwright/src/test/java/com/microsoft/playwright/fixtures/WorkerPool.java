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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.function.IntSupplier;

/**
 * Hands out {@link Worker}s to tests. Idle workers are kept for the next test with the same key;
 * when the pool is full, the least recently used idle worker is closed to make room, as Playwright
 * Test tears a worker down when the next test needs different worker fixtures. Workers are not
 * bound to threads, so the pool is safe with JUnit's fork-join executor which may add compensation
 * threads or retire idle ones.
 */
final class WorkerPool {
  /**
   * Every test thread runs each test once per browser, one invocation after another, so it needs
   * a worker per browser to reuse them from one test to the next.
   */
  static final WorkerPool INSTANCE = new WorkerPool(() -> testThreads() * Env.browserNames().size());

  private final IntSupplier capacity;
  // Least recently released first.
  private final List<Worker> idle = new ArrayList<>();
  private int busy;
  // Failures to close workers that no test owns: evicted ones and the idle ones at the end of the run.
  // Like Playwright Test's worker teardown errors, they are reported for the whole run.
  private final Teardown unownedTeardown = new Teardown();

  WorkerPool(IntSupplier capacity) {
    this.capacity = capacity;
  }

  Worker acquire(Worker.Key key) {
    Worker evicted = null;
    synchronized (this) {
      for (int i = idle.size() - 1; i >= 0; i--) {
        if (idle.get(i).key.equals(key)) {
          busy++;
          return idle.remove(i);
        }
      }
      if (!idle.isEmpty() && idle.size() + busy >= capacity.getAsInt()) {
        evicted = idle.remove(0);
      }
      busy++;
    }
    if (evicted != null) {
      closeUnowned(evicted);
    }
    return new Worker(key);
  }

  /**
   * Returns the worker to the pool once it is ready for the next test, or closes it when it must
   * not serve another one; failures to clean it up are reported to the test that used it.
   */
  void release(Worker worker, boolean discard) throws Exception {
    Teardown teardown = new Teardown();
    boolean reuse = !discard && worker.isHealthy();
    if (reuse) {
      worker.closeTestContexts(teardown);
      reuse = !teardown.failed();
    }
    synchronized (this) {
      busy--;
      if (reuse) {
        idle.add(worker);
      }
    }
    if (!reuse) {
      teardown.run(worker::close);
    }
    teardown.rethrow();
  }

  void closeAll() throws Exception {
    List<Worker> workers;
    synchronized (this) {
      workers = new ArrayList<>(idle);
      idle.clear();
    }
    // Workers share nothing, so they are closed concurrently.
    workers.parallelStream().forEach(this::closeUnowned);
    unownedTeardown.rethrow();
  }

  private void closeUnowned(Worker worker) {
    unownedTeardown.run(() -> {
      try {
        worker.close();
      } catch (Exception e) {
        throw new IllegalStateException("Failed to close " + worker, e);
      }
    });
  }

  /** As many as JUnit runs tests in parallel; one when tests do not run in parallel. */
  private static int testThreads() {
    Thread thread = Thread.currentThread();
    if (thread instanceof ForkJoinWorkerThread) {
      return ((ForkJoinWorkerThread) thread).getPool().getParallelism();
    }
    return 1;
  }
}
