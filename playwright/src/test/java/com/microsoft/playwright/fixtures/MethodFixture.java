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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;

/**
 * A fixture declared with a {@link Fixture} method on a test class.
 */
final class MethodFixture implements FixtureDefinition {
  private final Method method;
  private final Scope scope;

  MethodFixture(Method method) {
    this.method = method;
    this.scope = method.getAnnotation(Fixture.class).scope();
    if (method.getReturnType() == void.class) {
      throw new IllegalStateException(describe() + " must return the fixture value");
    }
    if (scope == Scope.WORKER && !Modifier.isStatic(method.getModifiers())) {
      throw new IllegalStateException(describe() + " is worker-scoped and must be static");
    }
  }

  Method method() {
    return method;
  }

  @Override
  public Class<?> type() {
    return method.getReturnType();
  }

  @Override
  public Scope scope() {
    return scope;
  }

  @Override
  public Object identity() {
    return method;
  }

  @Override
  public Object create(FixtureResolver resolver) throws Exception {
    Parameter[] parameters = method.getParameters();
    Object[] args = new Object[parameters.length];
    for (int i = 0; i < parameters.length; i++) {
      args[i] = resolver.get(parameters[i].getType());
    }
    Object target = Modifier.isStatic(method.getModifiers()) ? null : resolver.testInstance(method.getDeclaringClass());
    method.setAccessible(true);
    try {
      return method.invoke(target, args);
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();
      if (cause instanceof Exception) {
        throw (Exception) cause;
      }
      throw (Error) cause;
    }
  }

  @Override
  public void close(Object value) throws Exception {
    if (value instanceof AutoCloseable) {
      ((AutoCloseable) value).close();
    }
  }

  @Override
  public String describe() {
    return "@Fixture " + method.getDeclaringClass().getSimpleName() + "." + method.getName() + "()";
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof MethodFixture && method.equals(((MethodFixture) o).method);
  }

  @Override
  public int hashCode() {
    return method.hashCode();
  }
}
