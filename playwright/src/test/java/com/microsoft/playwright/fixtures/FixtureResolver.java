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

/**
 * Resolves the dependencies of a fixture while it is being created.
 */
interface FixtureResolver {
  /**
   * Returns the fixture of the given type. Asking for the type of the fixture being created
   * returns the definition it overrides, the way an upstream fixture can depend on the fixture
   * of the same name from the base test.
   */
  <T> T get(Class<T> type);

  /** The test instance a non-static {@link Fixture} method is invoked on. */
  Object testInstance(Class<?> declaringClass);

  /** The JUnit context of the test invocation; only test-scoped fixtures have one. */
  ExtensionContext extensionContext();
}
