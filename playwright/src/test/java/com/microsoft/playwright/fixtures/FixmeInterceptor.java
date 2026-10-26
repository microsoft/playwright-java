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
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.opentest4j.AssertionFailedError;
import org.opentest4j.TestAbortedException;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Runs a {@link Fixme} test as a strict expected failure: a failure is reported as skipped with
 * the reason and a pass fails the test. Registered for {@code @Fixme} methods only, so that the
 * stack traces of the other tests do not go through the harness.
 */
final class FixmeInterceptor implements InvocationInterceptor {
  private final BrowserName browserName;
  private final Fixme fixme;

  FixmeInterceptor(BrowserName browserName, Fixme fixme) {
    this.browserName = browserName;
    this.fixme = fixme;
  }

  @Override
  public void interceptTestTemplateMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
    if (fixme.browsers().length > 0 && !Arrays.asList(fixme.browsers()).contains(browserName)
      || fixme.parameters().length > 0 && !matchesParameters(extensionContext, fixme.parameters())) {
      invocation.proceed();
      return;
    }
    try {
      if (fixme.timeoutMillis() > 0) {
        proceedWithTimeout(invocation, fixme.timeoutMillis());
      } else {
        invocation.proceed();
      }
    } catch (TestAbortedException e) {
      throw e;
    } catch (Throwable e) {
      throw new ExpectedFailure(fixme.value(), e);
    }
    throw new AssertionFailedError("Test marked @Fixme passed, remove the annotation: " + fixme.value());
  }

  /** True when an enclosing context, such as a parameterized class invocation, has one of the fragments in its display name. */
  private static boolean matchesParameters(ExtensionContext context, String[] fragments) {
    for (ExtensionContext current = context; current != null; current = current.getParent().orElse(null)) {
      String name = current.getDisplayName();
      for (String fragment : fragments) {
        if (name.contains(fragment)) {
          return true;
        }
      }
    }
    return false;
  }

  /** Runs the test on a thread of its own and gives up on it after the timeout, like JUnit's separate-thread timeout. */
  private static void proceedWithTimeout(Invocation<Void> invocation, long timeoutMillis) throws Throwable {
    CompletableFuture<Void> done = new CompletableFuture<>();
    Thread thread = new Thread(() -> {
      try {
        invocation.proceed();
        done.complete(null);
      } catch (Throwable e) {
        done.completeExceptionally(e);
      }
    }, "fixme test");
    thread.setDaemon(true);
    thread.start();
    try {
      done.get(timeoutMillis, TimeUnit.MILLISECONDS);
    } catch (ExecutionException e) {
      throw e.getCause();
    } catch (TimeoutException e) {
      throw new TimeoutException("Test did not finish within " + timeoutMillis + " ms");
    }
  }
}
