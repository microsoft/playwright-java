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
 * A fixture factory: either one of the built-in fixtures or a {@link Fixture} method.
 */
interface FixtureDefinition {
  Class<?> type();

  Scope scope();

  /** Stable key for cached values; equal for the same definition seen through different test classes. */
  Object identity();

  Object create(FixtureResolver resolver) throws Exception;

  void close(Object value) throws Exception;

  String describe();
}
