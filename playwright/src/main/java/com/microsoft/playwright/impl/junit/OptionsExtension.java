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

package com.microsoft.playwright.impl.junit;

import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.junit.Options;
import com.microsoft.playwright.junit.PlaywrightOptions;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ModifierSupport;
import org.junit.platform.commons.support.ReflectionSupport;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.joining;

public class OptionsExtension implements AfterAllCallback {
  private static final ThreadLocal<CachedOptions> threadLocalOptions = new ThreadLocal<>();

  private static class CachedOptions {
    final Options options;
    // @PlaywrightOptions member the options were read from, null when the test class has none.
    final Member source;
    // Test instance the options were read from, null for static members.
    final Object testInstance;

    CachedOptions(Options options, Member source, Object testInstance) {
      this.options = options;
      this.source = source;
      this.testInstance = testInstance;
    }
  }

  @Override
  public void afterAll(ExtensionContext extensionContext) {
    threadLocalOptions.remove();
  }

  static Options getOptions(ExtensionContext extensionContext) {
    Member member = findOptionsMember(extensionContext.getRequiredTestClass());
    Object testInstance = member == null || ModifierSupport.isStatic(member) ? null : getTestInstance(extensionContext, member);

    CachedOptions cached = threadLocalOptions.get();
    if (cached != null && Objects.equals(cached.source, member) && cached.testInstance == testInstance) {
      return cached.options;
    }
    Options options = member == null ? new Options() : readOptions(member, testInstance);
    threadLocalOptions.set(new CachedOptions(options, member, testInstance));
    return options;
  }

  private static Object getTestInstance(ExtensionContext extensionContext, Member member) {
    Class<?> declaringClass = member.getDeclaringClass();
    return extensionContext.getTestInstances()
      .flatMap(instances -> instances.findInstance(declaringClass))
      .orElseThrow(() -> new PlaywrightException(describe(member) + " requires a test instance. Make it static, or use " +
        "@TestInstance(TestInstance.Lifecycle.PER_CLASS) to request Playwright fixtures in @BeforeAll methods or constructors."));
  }

  // Looks for a @PlaywrightOptions field or method in the test class, its superclasses and its enclosing classes.
  static Member findOptionsMember(Class<?> testClass) {
    for (Class<?> clazz = testClass; clazz != null; clazz = clazz.getDeclaringClass()) {
      List<Member> members = new ArrayList<>();
      members.addAll(AnnotationSupport.findAnnotatedFields(clazz, PlaywrightOptions.class));
      members.addAll(AnnotationSupport.findAnnotatedMethods(clazz, PlaywrightOptions.class, HierarchyTraversalMode.BOTTOM_UP));
      if (members.size() > 1) {
        throw new PlaywrightException("Only one @PlaywrightOptions field or method is allowed, found: " +
          members.stream().map(OptionsExtension::describe).collect(joining(", ")));
      }
      if (!members.isEmpty()) {
        return validateOptionsMember(members.get(0));
      }
    }
    return null;
  }

  private static Member validateOptionsMember(Member member) {
    Class<?> type;
    if (member instanceof Field) {
      type = ((Field) member).getType();
    } else {
      Method method = (Method) member;
      if (method.getParameterCount() != 0) {
        throw new PlaywrightException(describe(member) + " must not have parameters.");
      }
      type = method.getReturnType();
    }
    if (!Options.class.isAssignableFrom(type)) {
      throw new PlaywrightException(describe(member) + " must be of type " + Options.class.getName() + ".");
    }
    return member;
  }

  private static Options readOptions(Member member, Object testInstance) {
    Object options;
    if (member instanceof Field) {
      options = ReflectionSupport.tryToReadFieldValue((Field) member, testInstance)
        .getOrThrow(e -> new PlaywrightException("Failed to read " + describe(member), e));
    } else {
      options = ReflectionSupport.invokeMethod((Method) member, testInstance);
    }
    if (options == null) {
      throw new PlaywrightException(describe(member) + " must not be null.");
    }
    return (Options) options;
  }

  private static String describe(Member member) {
    String name = member.getDeclaringClass().getName() + "." + member.getName();
    return member instanceof Field ? "@PlaywrightOptions field " + name : "@PlaywrightOptions method " + name + "()";
  }
}
