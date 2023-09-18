package com.microsoft.playwright.junit;

import org.junit.jupiter.api.extension.ExtensionContext;

import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;
import static org.junit.platform.commons.support.AnnotationSupport.isAnnotated;

class ExtensionUtils {
  static boolean hasUsePlaywrightAnnotation(ExtensionContext extensionContext) {
    return isAnnotated(extensionContext.getTestClass(), UsePlaywright.class);
  }

  static UsePlaywright getUsePlaywrightAnnotation(ExtensionContext extensionContext) {
    return findAnnotation(extensionContext.getTestClass(), UsePlaywright.class).get();
  }
}
