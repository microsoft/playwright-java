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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a ported test that fails because of a known gap of the Java client or of the test
 * harness, the counterpart of a strict {@code xfail}. The test still runs: a failure is reported
 * as skipped with the reason, and a pass fails the test so that the annotation gets removed once
 * the gap is closed.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Fixme {
  /**
   * Why the test fails, starting with {@code java:} for a client gap, {@code harness:} for a
   * missing test utility or {@code driver:} for an upstream change the bundled driver lacks.
   */
  String value();

  /** Only expect the failure in these browsers; empty means all. */
  BrowserName[] browsers() default {};

  /**
   * In a {@code @ParameterizedClass}, only expect the failure for the invocations whose display
   * name (for example {@code [3] type=null, value=null}) contains one of these fragments; empty
   * means all.
   */
  String[] parameters() default {};

  /**
   * The expected failure is a hang: the test runs on a thread of its own and is reported as the
   * expected failure when it has not finished after this many milliseconds; 0 means no limit.
   */
  long timeoutMillis() default 0;
}
