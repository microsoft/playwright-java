package com.microsoft.playwright.junit.impl;

import com.microsoft.playwright.junit.UsePlaywright;
import org.junit.jupiter.api.extension.ExtensionContext;
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
}
