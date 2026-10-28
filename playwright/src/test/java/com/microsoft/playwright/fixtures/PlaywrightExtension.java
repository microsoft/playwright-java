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

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.junit.platform.commons.support.AnnotationSupport;

import java.util.stream.Stream;

/**
 * Runs every {@link PlaywrightTest} once per browser from {@link Env#browserNames()}.
 */
public final class PlaywrightExtension implements TestTemplateInvocationContextProvider {
  private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(PlaywrightExtension.class);

  /** Closes the idle workers when the whole test run is over. */
  @SuppressWarnings("deprecation")
  private static final class PoolCloser implements AutoCloseable, ExtensionContext.Store.CloseableResource {
    @Override
    public void close() throws Exception {
      WorkerPool.INSTANCE.closeAll();
    }
  }

  @Override
  public boolean supportsTestTemplate(ExtensionContext context) {
    return context.getTestMethod().filter(method -> AnnotationSupport.isAnnotated(method, PlaywrightTest.class)).isPresent();
  }

  @Override
  public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
    context.getRoot().getStore(NAMESPACE).getOrComputeIfAbsent(PoolCloser.class, key -> new PoolCloser(), PoolCloser.class);
    Fixme[] fixmes = context.getRequiredTestMethod().getAnnotationsByType(Fixme.class);
    return Env.browserNames().stream().map(browserName -> new BrowserInvocation(browserName, fixmes));
  }
}
