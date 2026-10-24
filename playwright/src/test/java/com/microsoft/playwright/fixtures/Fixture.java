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
 * Declares a fixture factory on a test class, a superclass, an implemented interface (default
 * method) or an enclosing class of a {@code @Nested} test. The Java counterpart of
 * {@code test.extend({ name: async ({ deps }, use) => ... })} upstream.
 *
 * <p>The method's return type is the fixture type that test parameters are resolved by. Its
 * parameters are the fixtures it depends on. A parameter of the method's own return type resolves
 * to the fixture being overridden, so defaults can be adjusted rather than rebuilt:
 * <pre>{@code
 * @Fixture
 * Browser.NewContextOptions contextOptions() {
 *   return new Browser.NewContextOptions().setHasTouch(true);
 * }
 *
 * @Fixture(scope = Scope.WORKER)
 * static BrowserType.LaunchOptions launchOptions(BrowserType.LaunchOptions defaults) {
 *   return defaults.setArgs(asList("--enable-features=WebMCP"));
 * }
 * }</pre>
 *
 * <p>A value that implements {@link AutoCloseable} is closed when its scope ends, in reverse
 * creation order. Worker-scoped fixture methods must be static and may only depend on other
 * worker-scoped fixtures; test-scoped ones may be instance methods and so can use fields injected
 * by other extensions (Spring, Micronaut, {@code @TempDir}, {@code @Parameter}).
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Fixture {
  Scope scope() default Scope.TEST;
}
