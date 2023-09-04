package com.microsoft.playwright;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.Locale;

import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;

class JUnitUtils {
  // Necessary because of https://github.com/junit-team/junit5/issues/3447
  static ExecutionMode getExecutionMode(ExtensionContext extensionContext) {
    return findAnnotation(extensionContext.getTestMethod(), Execution.class)
      .map(Execution::value)
      .orElseGet(() -> findAnnotation(extensionContext.getTestClass(), Execution.class)
        .map(Execution::value)
        .orElseGet(() -> extensionContext.getConfigurationParameter("junit.jupiter.execution.parallel.mode.default")
          .map(paramValue -> ExecutionMode.valueOf(paramValue.toUpperCase(Locale.ROOT)))
          .orElse(ExecutionMode.SAME_THREAD)));
  }
}
