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
 * <p> Marks a field or method of a test class annotated with {@link UsePlaywright} that provides
 * options for the Playwright fixtures. A method must take no parameters. The member must be of one
 * of the following types, and a test class may have one member of each type:
 * <ul>
 * <li> {@link Options} - all options;</li>
 * <li> {@link com.microsoft.playwright.BrowserType.LaunchOptions BrowserType.LaunchOptions} -
 *      options for launching the browser, overrides {@link Options#setLaunchOptions};</li>
 * <li> {@link com.microsoft.playwright.Browser.NewContextOptions Browser.NewContextOptions} -
 *      options for creating the browser context, overrides {@link Options#setContextOptions};</li>
 * <li> {@link com.microsoft.playwright.APIRequest.NewContextOptions APIRequest.NewContextOptions} -
 *      options for creating the API request context, overrides {@link Options#setApiRequestOptions}.</li>
 * </ul>
 *
 * <p> A static field or method is read once per test class:
 * <pre>{@code
 * @UsePlaywright
 * public class MyTest {
 *   @PlaywrightOptions
 *   static final Options options = new Options().setHeadless(false).setDeviceName("iPhone 14");
 *
 *   @PlaywrightOptions
 *   static final Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
 *       .setBaseURL("https://github.com")
 *       .setLocale("de-DE");
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
 *   Browser.NewContextOptions contextOptions() {
 *     return new Browser.NewContextOptions().setBaseURL(server.getURL().toString());
 *   }
 *
 *   @Test
 *   void testWithEmbeddedServer(Page page) {
 *     page.navigate("/");
 *   }
 * }
 * }</pre>
 * Unless the test class uses {@code @TestInstance(TestInstance.Lifecycle.PER_CLASS)}, a non-static
 * member cannot be used when Playwright fixtures are requested in {@code @BeforeAll} methods or in
 * the constructor, because there is no test instance yet.
 *
 * <p> The annotated members may be declared in a superclass or in an enclosing class of the test
 * class. A {@code @Nested} class can declare its own members to run its tests with different options,
 * the members of other types declared in the enclosing class still apply:
 * <pre>{@code
 * @UsePlaywright
 * public class MyTest {
 *   @PlaywrightOptions
 *   static final Options options = new Options().setBaseUrl("https://example.com");
 *
 *   @Nested
 *   class InGerman {
 *     @PlaywrightOptions
 *     Browser.NewContextOptions contextOptions = new Browser.NewContextOptions().setLocale("de-DE");
 *
 *     @Test
 *     void testInGerman(Page page) {
 *       page.navigate("/");
 *     }
 *   }
 * }
 * }</pre>
 *
 * <p> {@code Playwright} and {@code Browser} are shared between the tests of a class, so they are
 * created with the options in effect for the first test; only the options of {@code BrowserContext}
 * and {@code APIRequestContext} (base URL, device, context and API request options, timeout, tracing)
 * take effect for every test instance and nested class.
 *
 * <p> For more details and usage examples see our
 * <a href="https://playwright.dev/java/docs/junit">JUnit guide</a>.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
@Documented
public @interface PlaywrightOptions {
}
