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

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.junit.impl.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <strong>NOTE:</strong> this API is experimental and is subject to changes.
 *
 * Use {@code @UsePlaywright} annotation to automatically manage Playwright objects
 * used in your test. Custom configuration can be provided by implementing
 * {@link OptionsFactory} and passing the class as a parameter.
 *
 * <p> When a test class is annotated with {@code @UsePlaywright} each test method can
 * use any of the following arguments that will be automatically created at run time:
 * <ul>
 * <li> {@link com.microsoft.playwright.Page Page page}</li>
 * <li> {@link com.microsoft.playwright.BrowserContext BrowserContext context}</li>
 * <li> {@link com.microsoft.playwright.Browser Browser browser}</li>
 * <li> {@link com.microsoft.playwright.APIRequestContext APIRequestContext request}</li>
 * <li> {@link com.microsoft.playwright.Playwright Playwright playwright}</li>
 * </ul>
 * {@code Page} and {@code BrowserContext} are created before each test and closed
 * after the test has finished. {@code Browser} and {@code Playwright} are reused
 * between tests for better efficiency.
 *
 * <p> An example of using {@code @UsePlaywright} annotation:
 * <pre>{@code
 * import com.microsoft.playwright.Browser;
 * import com.microsoft.playwright.BrowserContext;
 * import com.microsoft.playwright.Page;
 * import org.junit.jupiter.api.Test;
 *
 * import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
 * import static org.junit.jupiter.api.Assertions.assertEquals;
 * import static org.junit.jupiter.api.Assertions.assertNotNull;
 *
 * @UsePlaywright
 * public class TestExample {
 *   @Test
 *   void shouldProvidePage(Page page) {
 *     page.navigate("https://playwright.dev");
 *     assertThat(page).hasURL("https://playwright.dev/");
 *   }
 *
 *   @Test
 *   void shouldResolvePlaywrightObjects(Page page, BrowserContext context, Browser browser) {
 *     assertEquals(context, page.context());
 *     assertEquals(browser, context.browser());
 *     assertNotNull(browser.version());
 *   }
 * }
 * }</pre>
 *
 * <p> For more details and usage examples see our
 * <a href="https://playwright.dev/java/docs/junit">JUnit guide</a>.
 */
@ExtendWith({OptionsExtension.class, PlaywrightExtension.class, BrowserExtension.class, BrowserContextExtension.class,
             PageExtension.class, APIRequestContextExtension.class})
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UsePlaywright {
  Class<? extends OptionsFactory> value() default DefaultOptions.class;
}
