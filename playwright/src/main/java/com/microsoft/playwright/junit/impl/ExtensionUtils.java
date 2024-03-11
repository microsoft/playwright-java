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

package com.microsoft.playwright.junit.impl;

import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.junit.Options;
import com.microsoft.playwright.junit.UsePlaywright;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.platform.commons.support.AnnotationSupport;

import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;

class ExtensionUtils {
  static boolean hasUsePlaywrightAnnotation(ExtensionContext extensionContext) {
    return AnnotationSupport.isAnnotated(extensionContext.getTestClass(), UsePlaywright.class);
  }

  static UsePlaywright getUsePlaywrightAnnotation(ExtensionContext extensionContext) {
    return findAnnotation(extensionContext.getTestClass(), UsePlaywright.class).get();
  }

  static boolean isClassHook(ExtensionContext extensionContext) {
    return !extensionContext.getTestMethod().isPresent();
  }

  static boolean isParameterSupported(ParameterContext parameterContext, ExtensionContext extensionContext, Class<?> subject) {
    if (!hasUsePlaywrightAnnotation(extensionContext)) {
      return false;
    }
    Class<?> clazz = parameterContext.getParameter().getType();
    return subject.equals(clazz);
  }

  static void setTestIdAttribute(Playwright playwright, Options options) {
    String testIdAttribute = options.testIdAttribute == null ? "data-testid" : options.testIdAttribute;
    playwright.selectors().setTestIdAttribute(testIdAttribute);

  }
}
