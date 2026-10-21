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

import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.junit.Options;
import com.microsoft.playwright.junit.PlaywrightOptions;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstances;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ModifierSupport;
import org.junit.platform.commons.support.ReflectionSupport;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.*;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;

public class OptionsExtension implements AfterAllCallback {
  // Types a @PlaywrightOptions member may have. The typed options override the corresponding field of Options.
  private static final List<Class<?>> OPTIONS_TYPES = Arrays.asList(
    Options.class, BrowserType.LaunchOptions.class, Browser.NewContextOptions.class, APIRequest.NewContextOptions.class);

  private static final ThreadLocal<CachedOptions> threadLocalOptions = new ThreadLocal<>();

  // A @PlaywrightOptions member and the test instance to read it from, null for static members.
  private static class Source {
    final Member member;
    final Object testInstance;

    Source(Member member, Object testInstance) {
      this.member = member;
      this.testInstance = testInstance;
    }

    Object read() {
      Object value;
      if (member instanceof Field) {
        value = ReflectionSupport.tryToReadFieldValue((Field) member, testInstance)
          .getOrThrow(e -> new PlaywrightException("Failed to read " + describe(member), e));
      } else {
        value = ReflectionSupport.invokeMethod((Method) member, testInstance);
      }
      if (value == null) {
        throw new PlaywrightException(describe(member) + " must not be null.");
      }
      return value;
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Source)) {
        return false;
      }
      Source other = (Source) o;
      return member.equals(other.member) && testInstance == other.testInstance;
    }

    @Override
    public int hashCode() {
      return member.hashCode();
    }
  }

  private static class CachedOptions {
    final Options options;
    final Map<Class<?>, Source> sources;

    CachedOptions(Options options, Map<Class<?>, Source> sources) {
      this.options = options;
      this.sources = sources;
    }
  }

  @Override
  public void afterAll(ExtensionContext extensionContext) {
    threadLocalOptions.remove();
  }

  static Options getOptions(ExtensionContext extensionContext) {
    Map<Class<?>, Source> sources = findSources(extensionContext.getRequiredTestClass(), extensionContext.getTestInstances());
    CachedOptions cached = threadLocalOptions.get();
    if (cached != null && cached.sources.equals(sources)) {
      return cached.options;
    }
    Options options = createOptions(sources);
    threadLocalOptions.set(new CachedOptions(options, sources));
    return options;
  }

  // Visible for testing.
  static Options createOptions(Class<?> testClass, Optional<TestInstances> testInstances) {
    return createOptions(findSources(testClass, testInstances));
  }

  private static Options createOptions(Map<Class<?>, Source> sources) {
    Options base = read(sources, Options.class);
    // Copy, so that the typed options below do not modify the user's object, which may be shared between tests.
    Options options = base == null ? new Options() : copyOf(base);
    BrowserType.LaunchOptions launchOptions = read(sources, BrowserType.LaunchOptions.class);
    if (launchOptions != null) {
      options.launchOptions = launchOptions;
    }
    Browser.NewContextOptions contextOptions = read(sources, Browser.NewContextOptions.class);
    if (contextOptions != null) {
      options.contextOptions = contextOptions;
    }
    APIRequest.NewContextOptions apiRequestOptions = read(sources, APIRequest.NewContextOptions.class);
    if (apiRequestOptions != null) {
      options.apiRequestOptions = apiRequestOptions;
    }
    return options;
  }

  private static <T> T read(Map<Class<?>, Source> sources, Class<T> type) {
    Source source = sources.get(type);
    return source == null ? null : type.cast(source.read());
  }

  // Shallow copy, the extensions clone the nested options before modifying them.
  private static Options copyOf(Options options) {
    Options copy = new Options();
    for (Field field : Options.class.getFields()) {
      try {
        field.set(copy, field.get(options));
      } catch (IllegalAccessException e) {
        throw new PlaywrightException("Failed to copy options", e);
      }
    }
    return copy;
  }

  // Finds the @PlaywrightOptions members of the test class, its superclasses and its enclosing classes, one per type.
  // A member of a nested class takes precedence over a member of the same type in an enclosing class.
  private static Map<Class<?>, Source> findSources(Class<?> testClass, Optional<TestInstances> testInstances) {
    Map<Class<?>, Source> sources = new HashMap<>();
    for (Class<?> clazz = testClass; clazz != null; clazz = clazz.getDeclaringClass()) {
      for (Map.Entry<Class<?>, List<Member>> entry : findMembersByType(clazz).entrySet()) {
        List<Member> members = entry.getValue();
        if (members.size() > 1) {
          throw new PlaywrightException("Only one @PlaywrightOptions field or method of type " + entry.getKey().getCanonicalName() +
            " is allowed, found: " + members.stream().map(OptionsExtension::describe).collect(joining(", ")));
        }
        if (!sources.containsKey(entry.getKey())) {
          sources.put(entry.getKey(), toSource(members.get(0), testInstances));
        }
      }
    }
    return sources;
  }

  private static Map<Class<?>, List<Member>> findMembersByType(Class<?> clazz) {
    List<Member> members = new ArrayList<>();
    members.addAll(AnnotationSupport.findAnnotatedFields(clazz, PlaywrightOptions.class));
    members.addAll(AnnotationSupport.findAnnotatedMethods(clazz, PlaywrightOptions.class, HierarchyTraversalMode.BOTTOM_UP));
    return members.stream().collect(groupingBy(OptionsExtension::optionsType));
  }

  // Returns which of OPTIONS_TYPES the member provides, validating the member on the way.
  private static Class<?> optionsType(Member member) {
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
    return OPTIONS_TYPES.stream().filter(optionsType -> optionsType.isAssignableFrom(type)).findFirst()
      .orElseThrow(() -> new PlaywrightException(describe(member) + " must be of type " +
        OPTIONS_TYPES.stream().map(Class::getCanonicalName).collect(joining(", ")) + "."));
  }

  private static Source toSource(Member member, Optional<TestInstances> testInstances) {
    if (ModifierSupport.isStatic(member)) {
      return new Source(member, null);
    }
    Class<?> declaringClass = member.getDeclaringClass();
    Object testInstance = testInstances.flatMap(instances -> instances.findInstance(declaringClass))
      .orElseThrow(() -> new PlaywrightException(describe(member) + " requires a test instance. Make it static, or use " +
        "@TestInstance(TestInstance.Lifecycle.PER_CLASS) to request Playwright fixtures in @BeforeAll methods or constructors."));
    return new Source(member, testInstance);
  }

  private static String describe(Member member) {
    String name = member.getDeclaringClass().getName() + "." + member.getName();
    return member instanceof Field ? "@PlaywrightOptions field " + name : "@PlaywrightOptions method " + name + "()";
  }
}
