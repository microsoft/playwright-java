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
 * <p>For more details and usage examples see our
 * <a href="https://playwright.dev/java/docs/junit">JUnit guide</a>.
 */
public interface OptionsFactory {
  Options getOptions();
}
