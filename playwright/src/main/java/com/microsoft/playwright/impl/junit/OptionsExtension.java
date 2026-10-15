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
import com.microsoft.playwright.junit.OptionsFactory;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static com.microsoft.playwright.impl.junit.ExtensionUtils.*;

public class OptionsExtension implements AfterAllCallback {
  private static final ThreadLocal<Options> threadLocalOptions = new ThreadLocal<>();
  // Test instance that the options were created for, when the factory is an inner class of the test class.
  private static final ThreadLocal<Object> threadLocalEnclosingInstance = new ThreadLocal<>();

  @Override
  public void afterAll(ExtensionContext extensionContext) {
    threadLocalOptions.remove();
    threadLocalEnclosingInstance.remove();
  }

  static Options getOptions(ExtensionContext extensionContext) {
    Class<? extends OptionsFactory> factoryClass = getUsePlaywrightAnnotation(extensionContext).value();
    // Inner class factories can access fields of the test instance, so the options are
    // re-created for each test instance.
    Object enclosingInstance = findEnclosingInstance(factoryClass, extensionContext);
    Options options = threadLocalOptions.get();
    if (options != null && (enclosingInstance == null || enclosingInstance == threadLocalEnclosingInstance.get())) {
      return options;
    }
    options = createFactory(factoryClass, enclosingInstance).getOptions();
    threadLocalOptions.set(options);
    threadLocalEnclosingInstance.set(enclosingInstance);
    return options;
  }

  private static boolean isInnerClass(Class<?> clazz) {
    return clazz.isMemberClass() && !Modifier.isStatic(clazz.getModifiers());
  }

  private static Object findEnclosingInstance(Class<?> factoryClass, ExtensionContext extensionContext) {
    if (!isInnerClass(factoryClass)) {
      return null;
    }
    return extensionContext.getTestInstances()
      .flatMap(instances -> instances.findInstance(factoryClass.getDeclaringClass()))
      .orElse(null);
  }

  private static OptionsFactory createFactory(Class<? extends OptionsFactory> factoryClass, Object enclosingInstance) {
    if (enclosingInstance == null && isInnerClass(factoryClass)) {
      throw new PlaywrightException("Cannot create " + factoryClass.getName() + " without an instance of " +
        factoryClass.getDeclaringClass().getName() + ". Make the options factory a static class or use " +
        "@TestInstance(TestInstance.Lifecycle.PER_CLASS) to request Playwright fixtures in @BeforeAll methods.");
    }
    try {
      if (enclosingInstance == null) {
        return factoryClass.newInstance();
      }
      // Use the declaring class rather than enclosingInstance.getClass(), which may be a subclass.
      Constructor<? extends OptionsFactory> constructor = factoryClass.getDeclaredConstructor(factoryClass.getDeclaringClass());
      constructor.setAccessible(true);
      return constructor.newInstance(enclosingInstance);
    } catch (ReflectiveOperationException e) {
      throw new PlaywrightException("Failed to create options", e);
    }
  }
}
