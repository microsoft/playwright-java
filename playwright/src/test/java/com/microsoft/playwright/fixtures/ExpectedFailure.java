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

import org.opentest4j.TestAbortedException;

/** The failure of a {@link Fixme} test, reported as skipped; the worker is still discarded. */
final class ExpectedFailure extends TestAbortedException {
  ExpectedFailure(String reason, Throwable cause) {
    super("Expected failure: " + reason, cause);
  }
}
