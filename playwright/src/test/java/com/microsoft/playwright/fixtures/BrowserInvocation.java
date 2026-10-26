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

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.io.TempDir;
import org.opentest4j.AssertionFailedError;
import org.opentest4j.TestAbortedException;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * One invocation of a {@link PlaywrightTest}: binds the browser, resolves fixture parameters and
 * applies the {@link EnabledOnBrowser}/{@link DisabledOnBrowser} conditions.
 */
final class BrowserInvocation implements TestTemplateInvocationContext, ParameterResolver, ExecutionCondition, InvocationInterceptor {
  private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(BrowserInvocation.class);

  private final BrowserName browserName;

  BrowserInvocation(BrowserName browserName) {
    this.browserName = browserName;
  }

  @Override
  public String getDisplayName(int invocationIndex) {
    return "[" + browserName + "]";
  }

  @Override
  public List<Extension> getAdditionalExtensions() {
    return Collections.singletonList(this);
  }

  @Override
  public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
    List<AnnotatedElement> elements = new ArrayList<>();
    context.getTestMethod().ifPresent(elements::add);
    for (Class<?> clazz = context.getRequiredTestClass(); clazz != null; clazz = clazz.getEnclosingClass()) {
      for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
        elements.add(c);
      }
    }
    for (AnnotatedElement element : elements) {
      for (DisabledOnBrowser disabled : element.getAnnotationsByType(DisabledOnBrowser.class)) {
        if (Arrays.asList(disabled.value()).contains(browserName) && matchesOs(disabled.os()) && (!disabled.headed() || Env.headed())) {
          return ConditionEvaluationResult.disabled("Disabled on " + browserName + reason(disabled.reason()));
        }
      }
      EnabledOnBrowser enabled = element.getAnnotation(EnabledOnBrowser.class);
      if (enabled != null && !Arrays.asList(enabled.value()).contains(browserName)) {
        return ConditionEvaluationResult.disabled("Enabled only on " + Arrays.toString(enabled.value()) + reason(enabled.reason()));
      }
    }
    return ConditionEvaluationResult.enabled("Running in " + browserName);
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

  private static boolean matchesOs(OS[] os) {
    if (os.length == 0) {
      return true;
    }
    for (OS candidate : os) {
      if (candidate.isCurrentOs()) {
        return true;
      }
    }
    return false;
  }

  private static String reason(String reason) {
    return reason.isEmpty() ? "" : ": " + reason;
  }

  @Override
  public void interceptTestTemplateMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
    Fixme fixme = invocationContext.getExecutable().getAnnotation(Fixme.class);
    if (fixme == null || fixme.browsers().length > 0 && !Arrays.asList(fixme.browsers()).contains(browserName)
      || fixme.parameters().length > 0 && !matchesParameters(extensionContext, fixme.parameters())) {
      invocation.proceed();
      return;
    }
    try {
      invocation.proceed();
    } catch (TestAbortedException e) {
      throw e;
    } catch (Throwable e) {
      throw new ExpectedFailure(fixme.value(), e);
    }
    throw new AssertionFailedError("Test marked @Fixme passed, remove the annotation: " + fixme.value());
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    // Constructors are resolved against the class context, which cannot hold per-invocation fixtures.
    if (parameterContext.getDeclaringExecutable() instanceof Constructor) {
      return false;
    }
    if (parameterContext.isAnnotated(TempDir.class)) {
      return false;
    }
    return registry(extensionContext).supports(parameterContext.getParameter().getType());
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    return scope(extensionContext).get(parameterContext.getParameter().getType());
  }

  private static FixtureRegistry registry(ExtensionContext context) {
    return FixtureRegistry.forTestClass(context.getRequiredTestClass());
  }

  private TestScope scope(ExtensionContext context) {
    return context.getStore(NAMESPACE).getOrComputeIfAbsent(TestScope.class,
      key -> new TestScope(context, registry(context), browserName), TestScope.class);
  }
}
