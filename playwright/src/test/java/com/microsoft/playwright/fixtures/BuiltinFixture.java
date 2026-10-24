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

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.HttpsServer;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Server;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The fixtures every test can use without declaring anything, the counterpart of the
 * {@code browserTest} fixtures upstream.
 */
final class BuiltinFixture<T> implements FixtureDefinition {
  interface Factory<T> {
    T create(FixtureResolver resolver) throws Exception;
  }

  interface Closer<T> {
    void close(T value) throws Exception;
  }

  /** Bound by the worker to the browser the test invocation runs in. */
  static final BuiltinFixture<BrowserName> BROWSER_NAME = worker(BrowserName.class, resolver -> {
    throw new IllegalStateException("BrowserName is bound by the worker");
  }, null);

  static final BuiltinFixture<Browser> BROWSER = worker(Browser.class,
    resolver -> resolver.get(BrowserType.class).launch(resolver.get(BrowserType.LaunchOptions.class)),
    Browser::close);

  static final List<FixtureDefinition> ALL = Collections.unmodifiableList(Arrays.asList(
    BROWSER_NAME,
    worker(Playwright.CreateOptions.class, resolver -> new Playwright.CreateOptions(), null),
    worker(Playwright.class, resolver -> Playwright.create(resolver.get(Playwright.CreateOptions.class)), Playwright::close),
    worker(BrowserType.class, resolver -> resolver.get(BrowserName.class).browserType(resolver.get(Playwright.class)), null),
    worker(BrowserType.LaunchOptions.class, resolver -> Env.launchOptions(), null),
    BROWSER,
    worker(Server.class, resolver -> Server.createHttp(), Server::stop),
    worker(HttpsServer.class, resolver -> Server.createHttps(), Server::stop),
    // The test-scoped servers are the worker ones, reset before every test.
    test(Server.class, resolver -> reset(resolver.get(Server.class)), null),
    test(HttpsServer.class, resolver -> reset(resolver.get(HttpsServer.class)), null),
    test(Browser.NewContextOptions.class, resolver -> new Browser.NewContextOptions(), null),
    test(BrowserContext.class,
      resolver -> resolver.get(Browser.class).newContext(resolver.get(Browser.NewContextOptions.class)),
      BrowserContext::close),
    test(Page.class, resolver -> resolver.get(BrowserContext.class).newPage(), null)));

  private final Class<T> type;
  private final Scope scope;
  private final Factory<T> factory;
  private final Closer<T> closer;

  private static <S extends Server> S reset(S server) {
    server.reset();
    return server;
  }

  private static <T> BuiltinFixture<T> worker(Class<T> type, Factory<T> factory, Closer<T> closer) {
    return new BuiltinFixture<>(type, Scope.WORKER, factory, closer);
  }

  private static <T> BuiltinFixture<T> test(Class<T> type, Factory<T> factory, Closer<T> closer) {
    return new BuiltinFixture<>(type, Scope.TEST, factory, closer);
  }

  private BuiltinFixture(Class<T> type, Scope scope, Factory<T> factory, Closer<T> closer) {
    this.type = type;
    this.scope = scope;
    this.factory = factory;
    this.closer = closer;
  }

  @Override
  public Class<?> type() {
    return type;
  }

  @Override
  public Scope scope() {
    return scope;
  }

  @Override
  public Object identity() {
    return this;
  }

  @Override
  public Object create(FixtureResolver resolver) throws Exception {
    return factory.create(resolver);
  }

  @Override
  public void close(Object value) throws Exception {
    if (closer != null) {
      closer.close(type.cast(value));
    }
  }

  @Override
  public String describe() {
    return "built-in " + scope.name().toLowerCase() + " fixture " + type.getSimpleName();
  }
}
