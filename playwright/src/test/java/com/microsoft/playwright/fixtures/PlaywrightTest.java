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

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a test method that runs once per browser selected by the {@code BROWSER} environment
 * variable (all three browsers by default), the Java counterpart of a Playwright Test project.
 * Invocations are named after the browser ({@code [chromium]}, {@code [firefox]}, {@code [webkit]});
 * reports that only show the invocation index number them in the order of {@link Env#browserNames()}.
 *
 * <p>Fixtures are injected as method parameters, mirroring the destructured fixtures of
 * {@code test('...', async ({ page, server }) => ...)} upstream:
 * <pre>{@code
 * @PlaywrightTest
 * void shouldWork(Page page, Server server) {
 *   page.navigate(server.EMPTY_PAGE);
 * }
 * }</pre>
 *
 * <p>Built-in fixtures: {@link com.microsoft.playwright.Playwright}, {@link BrowserName},
 * {@link com.microsoft.playwright.BrowserType}, {@link com.microsoft.playwright.BrowserType.LaunchOptions},
 * {@link com.microsoft.playwright.Browser}, {@link com.microsoft.playwright.Browser.NewContextOptions},
 * {@link com.microsoft.playwright.BrowserContext}, {@link com.microsoft.playwright.Page},
 * {@link com.microsoft.playwright.Server} and {@link com.microsoft.playwright.HttpsServer}.
 * Worker-scoped fixtures (Playwright, Browser, servers) are reused by the tests that run
 * sequentially on the same thread; test-scoped ones (context, page) are created for every test
 * that asks for them and closed afterwards. A test class overrides or adds fixtures with
 * {@link Fixture} methods.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@TestTemplate
@ExtendWith(PlaywrightExtension.class)
public @interface PlaywrightTest {
}
