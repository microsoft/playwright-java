package com.microsoft.playwright.junit;

import org.junit.jupiter.api.extension.ExtensionContext;

import static org.junit.platform.commons.support.AnnotationSupport.isAnnotated;

class ExtensionUtils {

  static boolean hasProperAnnotation(ExtensionContext extensionContext) {
    return isAnnotated(extensionContext.getTestMethod(), UseBrowserFactory.class) ||
      isAnnotated(extensionContext.getTestClass(), UseBrowserFactory.class);
  }
}
