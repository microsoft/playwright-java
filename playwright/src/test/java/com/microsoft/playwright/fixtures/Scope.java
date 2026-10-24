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

/**
 * Lifetime of a {@link Fixture}.
 */
public enum Scope {
  /** Created for every test that uses it and torn down when the test finishes. */
  TEST,
  /**
   * Shared by all the tests that run on the same worker, like a Playwright Test worker fixture.
   * A worker is a thread-bound bundle of Playwright, Browser and test servers; it is kept alive
   * across tests and classes as long as they request the same worker-scoped fixtures.
   */
  WORKER
}
