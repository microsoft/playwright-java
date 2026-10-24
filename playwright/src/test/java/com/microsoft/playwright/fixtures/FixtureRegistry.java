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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The fixtures visible to a test class: the built-in ones followed by the {@link Fixture} methods
 * found on its enclosing classes, supertypes and the class itself. For every fixture type the
 * registry keeps the chain of definitions in that order, the last one being the most specific
 * and the one a test parameter resolves to.
 */
final class FixtureRegistry {
  private static final Map<Class<?>, FixtureRegistry> CACHE = new ConcurrentHashMap<>();

  private final Map<Class<?>, List<FixtureDefinition>> chains = new HashMap<>();
  private final List<Method> workerMethods = new ArrayList<>();

  static FixtureRegistry forTestClass(Class<?> testClass) {
    return CACHE.computeIfAbsent(testClass, FixtureRegistry::new);
  }

  private FixtureRegistry(Class<?> testClass) {
    for (FixtureDefinition definition : BuiltinFixture.ALL) {
      add(definition);
    }
    for (Class<?> clazz : lookupOrder(testClass)) {
      List<Method> methods = new ArrayList<>();
      for (Method method : clazz.getDeclaredMethods()) {
        if (method.isAnnotationPresent(Fixture.class)) {
          methods.add(method);
        }
      }
      // Declaration order is not guaranteed by reflection.
      methods.sort(Comparator.comparing(Method::getName));
      List<Class<?>> typesInClass = new ArrayList<>();
      for (Method method : methods) {
        MethodFixture definition = new MethodFixture(method);
        if (typesInClass.contains(definition.type())) {
          throw new IllegalStateException(clazz.getName() + " declares more than one @Fixture of type " + definition.type().getName());
        }
        typesInClass.add(definition.type());
        add(definition);
      }
    }
  }

  /** Outermost enclosing class first, each preceded by its supertypes; the test class last. */
  private static List<Class<?>> lookupOrder(Class<?> testClass) {
    List<Class<?>> nesting = new ArrayList<>();
    for (Class<?> clazz = testClass; clazz != null; clazz = clazz.getEnclosingClass()) {
      nesting.add(0, clazz);
    }
    List<Class<?>> result = new ArrayList<>();
    for (Class<?> clazz : nesting) {
      addHierarchy(clazz, result);
    }
    return result;
  }

  private static void addHierarchy(Class<?> clazz, List<Class<?>> out) {
    if (clazz == null || clazz == Object.class || out.contains(clazz)) {
      return;
    }
    addHierarchy(clazz.getSuperclass(), out);
    for (Class<?> iface : clazz.getInterfaces()) {
      addHierarchy(iface, out);
    }
    out.add(clazz);
  }

  private void add(FixtureDefinition definition) {
    chains.computeIfAbsent(definition.type(), k -> new ArrayList<>()).add(definition);
    if (definition.scope() == Scope.WORKER && definition instanceof MethodFixture) {
      workerMethods.add(((MethodFixture) definition).method());
    }
  }

  boolean supports(Class<?> type) {
    return chains.containsKey(type);
  }

  List<FixtureDefinition> chain(Class<?> type) {
    List<FixtureDefinition> chain = chains.get(type);
    return chain == null ? Collections.emptyList() : chain;
  }

  /** The worker-scoped overrides; tests with equal lists can share a worker. */
  List<Method> workerMethods() {
    return Collections.unmodifiableList(workerMethods);
  }

  @Override
  public String toString() {
    return "FixtureRegistry" + Arrays.toString(chains.keySet().toArray());
  }
}
