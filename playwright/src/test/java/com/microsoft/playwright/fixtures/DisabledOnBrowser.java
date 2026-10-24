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

import org.junit.jupiter.api.condition.OS;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Skips the annotated {@link PlaywrightTest} (or every test of the annotated class) in the listed
 * browsers, the counterpart of {@code test.skip(({ browserName }) => browserName === 'webkit', reason)}.
 * The condition can be narrowed to some operating systems or to headed mode, as in
 * {@code test.skip(({ browserName, isMac }) => browserName === 'webkit' && isMac)}.
 */
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(DisabledOnBrowser.List.class)
public @interface DisabledOnBrowser {
  BrowserName[] value();

  /** Only skip on these operating systems; empty means any. */
  OS[] os() default {};

  /** Only skip when the browser runs headed, see {@link Env#headed()}. */
  boolean headed() default false;

  String reason() default "";

  @Documented
  @Target({ElementType.METHOD, ElementType.TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @interface List {
    DisabledOnBrowser[] value();
  }
}
