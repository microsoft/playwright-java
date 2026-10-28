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

/**
 * JUnit 5 fixtures for the Playwright tests, a port of the upstream Playwright Test fixtures in
 * {@code tests/config/browserTest.ts}.
 *
 * <h2>Mapping to Playwright Test</h2>
 * <ul>
 *   <li>Projects (one per browser) are the invocations of a {@link com.microsoft.playwright.fixtures.PlaywrightTest},
 *       selected with the {@code BROWSER} environment variable, see {@link com.microsoft.playwright.fixtures.Env}.</li>
 *   <li>Destructured fixtures {@code ({ page, server })} are method parameters resolved by type.</li>
 *   <li>{@code test.extend({...})} and {@code test.use({...})} are {@link com.microsoft.playwright.fixtures.Fixture}
 *       methods on the test class, its supertypes, interfaces or enclosing classes; a method may depend on
 *       the fixture it overrides by declaring a parameter of its own return type.</li>
 *   <li>Worker fixtures ({@code scope: 'worker'}) are {@link com.microsoft.playwright.fixtures.Scope#WORKER}
 *       fixtures. A worker is a bundle of such values that one test uses at a time; the pool hands it to
 *       the next test with the same worker fixtures and closes it after a failure, as Playwright Test
 *       restarts a worker process. Contexts that a test leaves open in a worker's browsers are closed
 *       when the test ends; contexts created by worker fixtures stay open.</li>
 *   <li>{@code test.skip(({ browserName }) => ...)} is {@link com.microsoft.playwright.fixtures.DisabledOnBrowser}
 *       or {@link com.microsoft.playwright.fixtures.EnabledOnBrowser}; dynamic conditions use JUnit assumptions.</li>
 *   <li>Tests generated in a {@code for} loop are a {@code @Nested @ParameterizedClass} whose
 *       {@code @Parameter} field is read by a {@code @Fixture} method.</li>
 *   <li>The test server ({@code tests/config/testserver}) is {@link com.microsoft.playwright.fixtures.TestServer}
 *       and {@link com.microsoft.playwright.fixtures.HttpsTestServer}, serving {@code src/test/assets};
 *       {@code testInfo} is {@link com.microsoft.playwright.fixtures.TestInfo}; the function fixtures
 *       {@code contextFactory}, {@code launchPersistent}, {@code createUserDataDir} and {@code pageWithHar}
 *       are the classes of those names; {@code tests/config/utils.ts} is {@link com.microsoft.playwright.fixtures.Utils};
 *       {@code tests/config/commonFixtures.ts} is {@link com.microsoft.playwright.fixtures.TestChildProcess} and
 *       {@link com.microsoft.playwright.fixtures.ChildProcessFactory}; {@code tests/config/proxy.ts} and
 *       {@code tests/config/mockSocksServer.ts} are {@link com.microsoft.playwright.fixtures.ProxyServer} and
 *       {@link com.microsoft.playwright.fixtures.MockSocksServer}; {@code tests/config/remoteServer.ts} is
 *       {@link com.microsoft.playwright.fixtures.StartRemoteServer} with {@link com.microsoft.playwright.fixtures.PlaywrightServer}
 *       and {@link com.microsoft.playwright.fixtures.RunServer}; the image comparator of
 *       {@code toMatchSnapshot} is {@link com.microsoft.playwright.fixtures.ImageComparator}.</li>
 *   <li>A port that fails because of a known gap of the Java client carries
 *       {@link com.microsoft.playwright.fixtures.Fixme}, a strict expected failure.</li>
 * </ul>
 *
 * <p>The rules for porting the upstream tests with these fixtures are in {@code src/test/AGENTS.md}.
 *
 * <h2>Threading</h2>
 * Playwright objects are not thread-safe, so every worker is used by a single test at a time, tests
 * only touch the fixtures they were given, and a test calls Playwright from its own thread only (event
 * and route handlers run on it, inside the call that pumps the messages); the test servers run on
 * threads of their own and never call Playwright. Workers are pooled rather than bound to threads because
 * JUnit's fork-join executor may add or retire threads; sequential use from different threads is safe.
 * Each test thread runs a test in every browser one after another, so the pool keeps a worker per
 * browser for every thread. Errors while closing fixtures fail the test that used them, or the whole
 * run for idle workers that no test owns.
 *
 * <h2>Other extensions</h2>
 * The resolver only claims parameters whose type is a known fixture, so it coexists with the parameter
 * resolvers of Spring, Quarkus, Micronaut or {@code @TempDir}. Test-scoped fixture methods may be
 * instance methods and read fields those extensions injected.
 */
package com.microsoft.playwright.fixtures;
