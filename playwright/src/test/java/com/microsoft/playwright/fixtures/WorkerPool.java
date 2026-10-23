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

/**
 * Hands out {@link Worker}s to tests. Idle workers are kept for the next test with the same key;
 * when the pool is full, the least recently used idle worker with a different key is closed to
 * make room, as Playwright Test tears a worker down when the next test needs different worker
 * fixtures. Workers are not bound to threads, so the pool is safe with JUnit's fork-join
 * executor which may add compensation threads or retire idle ones.
 */
final class WorkerPool {
  static final WorkerPool INSTANCE = new WorkerPool();

  // Least recently released first.
  private final List<Worker> idle = new ArrayList<>();
  private int busy;

  private WorkerPool() {
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
      if (!idle.isEmpty() && idle.size() + busy >= maxWorkers()) {
        evicted = idle.remove(0);
      }
      busy++;
    }
    if (evicted != null) {
      evicted.close();
    }
    return new Worker(key);
  }

  void release(Worker worker, boolean discard) {
    boolean close = discard || !worker.isHealthy();
    synchronized (this) {
      busy--;
      if (!close) {
        idle.add(worker);
      }
    }
    if (close) {
      worker.close();
    }
  }

  void closeAll() {
    List<Worker> workers;
    synchronized (this) {
      workers = new ArrayList<>(idle);
      idle.clear();
    }
    for (Worker worker : workers) {
      worker.close();
    }
  }

  /** As many workers as there are test threads; one when tests do not run in parallel. */
  private static int maxWorkers() {
    Thread thread = Thread.currentThread();
    if (thread instanceof ForkJoinWorkerThread) {
      return ((ForkJoinWorkerThread) thread).getPool().getParallelism();
    }
    return 1;
  }
}
