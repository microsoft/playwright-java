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

package com.microsoft.playwright.junit;

import java.lang.annotation.*;

/**
 * <strong>NOTE:</strong> this API is experimental and is subject to changes.
 *
 * <p> Marks the field or method of a test class annotated with {@link UsePlaywright} that
 * provides the {@link Options} for the Playwright fixtures. A field must be of type
 * {@code Options}, a method must return {@code Options} and take no parameters.
 *
 * <p> A static field or method is read once per test class:
 * <pre>{@code
 * @UsePlaywright
 * public class MyTest {
 *   @PlaywrightOptions
 *   static final Options options = new Options()
 *       .setHeadless(false)
 *       .setContextOptions(new Browser.NewContextOptions()
 *           .setBaseURL("https://github.com"));
 *
 *   @Test
 *   void testWithCustomOptions(Page page) {
 *     page.navigate("/");
 *     assertThat(page).hasURL(Pattern.compile("github"));
 *   }
 * }
 * }</pre>
 *
 * <p> A non-static field or method is read for each test instance, so the options can depend
 * on the state of the instance, e.g. on a server injected by a dependency injection framework:
 * <pre>{@code
 * @UsePlaywright
 * @MicronautTest
 * public class MyTest {
 *   @Inject
 *   EmbeddedServer server;
 *
 *   @PlaywrightOptions
 *   Options options() {
 *     return new Options().setBaseUrl(server.getURL().toString());
 *   }
 *
 *   @Test
 *   void testWithEmbeddedServer(Page page) {
 *     page.navigate("/");
 *   }
 * }
 * }</pre>
 * {@code Playwright} and {@code Browser} are shared between tests, so they are created with the
 * options of the first test instance; only the options of {@code BrowserContext} and
 * {@code APIRequestContext} take effect for every test instance. Unless the test class uses
 * {@code @TestInstance(TestInstance.Lifecycle.PER_CLASS)}, a non-static member cannot be used
 * when Playwright fixtures are requested in {@code @BeforeAll} methods or in the constructor,
 * because there is no test instance yet.
 *
 * <p> The annotated member may be declared in a superclass or in an enclosing class of the test
 * class.
 *
 * <p> For more details and usage examples see our
 * <a href="https://playwright.dev/java/docs/junit">JUnit guide</a>.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
@Documented
public @interface PlaywrightOptions {
}
