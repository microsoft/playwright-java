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

/**
 * <strong>NOTE:</strong> this API is experimental and is subject to changes.
 *
 * <p> Implement this interface to pass custom options to {@link UsePlaywright}
 * annotation.
 *
 * <p> An example of implementing {@code @OptionsFactory}:
 * <pre>{@code
 * import com.microsoft.playwright.junit.Options;
 * import com.microsoft.playwright.junit.OptionsFactory;
 * import com.microsoft.playwright.junit.UsePlaywright;
 *
 * @UsePlaywright(MyTest.CustomOptions.class)
 * public class MyTest {
 *
 *   public static class CustomOptions implements OptionsFactory {
 *     @Override
 *     public Options getOptions() {
 *       return new Options()
 *           .setHeadless(false)
 *           .setContextOptions(new Browser.NewContextOptions()
 *               .setBaseURL("https://github.com"))
 *           .setApiRequestOptions(new APIRequest.NewContextOptions()
 *               .setBaseURL("https://playwright.dev"));
 *     }
 *   }
 *
 *   @Test
 *   public void testWithCustomOptions(Page page, APIRequestContext request) {
 *     page.navigate("/");
 *     assertThat(page).hasURL(Pattern.compile("github"));
 *
 *     APIResponse response = request.get("/");
 *     assertTrue(response.text().contains("Playwright"));
 *   }
 * }
 * }</pre>
 *
 * <p> If the options depend on the state of the test instance (e.g. a server injected by
 * a dependency injection framework), declare the factory as a non-static inner class of
 * the test class. It will be created for each test instance and can access its fields:
 * <pre>{@code
 * @UsePlaywright(MyTest.CustomOptions.class)
 * public class MyTest {
 *   @Inject
 *   EmbeddedServer server;
 *
 *   class CustomOptions implements OptionsFactory {
 *     @Override
 *     public Options getOptions() {
 *       return new Options().setBaseUrl(server.getURL().toString());
 *     }
 *   }
 * }
 * }</pre>
 * Only context level options (e.g. base URL, context options, trace) are re-evaluated for each
 * test instance; {@code Playwright} and {@code Browser} are created with the options of the first
 * test instance and reused. Unless the test class uses {@code @TestInstance(Lifecycle.PER_CLASS)},
 * Playwright fixtures cannot be requested in {@code @BeforeAll} methods because there is no
 * test instance yet.
 *
 * <p>For more details and usage examples see our
 * <a href="https://playwright.dev/java/docs/junit">JUnit guide</a>.
 */
public interface OptionsFactory {
  Options getOptions();
}
