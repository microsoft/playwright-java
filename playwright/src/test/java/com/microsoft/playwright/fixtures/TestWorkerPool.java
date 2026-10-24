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

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class TestWorkerPool {
  private static final Worker.Key CHROMIUM = new Worker.Key(BrowserName.CHROMIUM, Collections.emptyList());
  private static final Worker.Key FIREFOX = new Worker.Key(BrowserName.FIREFOX, Collections.emptyList());
  private static final Worker.Key WEBKIT = new Worker.Key(BrowserName.WEBKIT, Collections.emptyList());

  // Idle workers are closed concurrently at the end of the run.
  private final List<Object> closed = Collections.synchronizedList(new ArrayList<>());
  private final FixtureDefinition resource = new WorkerFixture("resource", null);

  /** A worker fixture whose values record that they were closed, or fail to close. */
  private final class WorkerFixture implements FixtureDefinition {
    private final String name;
    private final String closeError;

    WorkerFixture(String name, String closeError) {
      this.name = name;
      this.closeError = closeError;
    }

    @Override
    public Class<?> type() {
      return Object.class;
    }

    @Override
    public Scope scope() {
      return Scope.WORKER;
    }

    @Override
    public Object identity() {
      return this;
    }

    @Override
    public Object create(FixtureResolver resolver) {
      return new Object();
    }

    @Override
    public void close(Object value) {
      if (closeError != null) {
        throw new IllegalStateException(closeError);
      }
      closed.add(value);
    }

    @Override
    public String describe() {
      return name;
    }
  }

  @Test
  void shouldKeepWorkerPerBrowserWhenEachTestRunsInAllBrowsers() throws Exception {
    // One test thread, every test runs in three browsers one after another.
    WorkerPool pool = new WorkerPool(() -> 3);
    Map<Worker.Key, Worker> firstWorkers = new HashMap<>();
    for (int test = 0; test < 3; test++) {
      for (Worker.Key key : asList(CHROMIUM, FIREFOX, WEBKIT)) {
        Worker worker = pool.acquire(key);
        worker.get(resource, null);
        firstWorkers.putIfAbsent(key, worker);
        assertSame(firstWorkers.get(key), worker, "test " + test + " in " + key);
        pool.release(worker, false);
      }
    }
    assertEquals(asList(), closed);
  }

  @Test
  void shouldEvictLeastRecentlyReleasedWorkerWhenFull() throws Exception {
    WorkerPool pool = new WorkerPool(() -> 2);
    Worker chromium = pool.acquire(CHROMIUM);
    Object chromiumValue = chromium.get(resource, null);
    pool.release(chromium, false);
    Worker firefox = pool.acquire(FIREFOX);
    pool.release(firefox, false);

    Worker webkit = pool.acquire(WEBKIT);
    assertEquals(asList(chromiumValue), closed);
    pool.release(webkit, false);
    assertSame(firefox, pool.acquire(FIREFOX));
  }

  @Test
  void shouldReportFailureToCloseEvictedWorkerAtEndOfRun() throws Exception {
    WorkerPool pool = new WorkerPool(() -> 1);
    Worker chromium = pool.acquire(CHROMIUM);
    chromium.get(new WorkerFixture("broken", "broken failed"), null);
    pool.release(chromium, false);

    // No test owns the evicted worker, so the test that needs its room still gets a worker.
    Worker firefox = pool.acquire(FIREFOX);
    pool.release(firefox, false);

    IllegalStateException e = assertThrows(IllegalStateException.class, pool::closeAll);
    assertEquals("Failed to close " + chromium, e.getMessage());
    assertEquals("broken failed", e.getCause().getMessage());
  }

  @Test
  void shouldCloseWorkerAfterFailedTest() throws Exception {
    WorkerPool pool = new WorkerPool(() -> 1);
    Worker worker = pool.acquire(CHROMIUM);
    Object value = worker.get(resource, null);
    pool.release(worker, true);

    assertEquals(asList(value), closed);
    assertNotSame(worker, pool.acquire(CHROMIUM));
  }

  @Test
  void shouldCloseEverythingAndReportAllTeardownFailures() throws Exception {
    WorkerPool pool = new WorkerPool(() -> 1);
    Worker worker = pool.acquire(CHROMIUM);
    Object value = worker.get(resource, null);
    worker.get(new WorkerFixture("first", "first failed"), null);
    worker.get(new WorkerFixture("second", "second failed"), null);

    // Closed newest first: both failures are reported and the oldest value is still closed.
    IllegalStateException e = assertThrows(IllegalStateException.class, () -> pool.release(worker, true));
    assertEquals("second failed", e.getMessage());
    assertEquals(1, e.getSuppressed().length);
    assertEquals("first failed", e.getSuppressed()[0].getMessage());
    assertEquals(asList(value), closed);
  }

  @Test
  void shouldCloseIdleWorkersAtEndOfRun() throws Exception {
    WorkerPool pool = new WorkerPool(() -> 3);
    Worker chromium = pool.acquire(CHROMIUM);
    Object chromiumValue = chromium.get(resource, null);
    Worker firefox = pool.acquire(FIREFOX);
    Object firefoxValue = firefox.get(resource, null);
    firefox.get(new WorkerFixture("broken", "broken failed"), null);
    pool.release(chromium, false);
    pool.release(firefox, false);

    IllegalStateException e = assertThrows(IllegalStateException.class, pool::closeAll);
    assertEquals("Failed to close " + firefox, e.getMessage());
    assertEquals("broken failed", e.getCause().getMessage());
    assertEquals(new HashSet<>(asList(chromiumValue, firefoxValue)), new HashSet<>(closed));
  }
}
