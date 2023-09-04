package com.microsoft.playwright;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.Locale;

import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;

class JUnitUtils {
  // Necessary because of https://github.com/junit-team/junit5/issues/3447
  static ExecutionMode getDefaultExecutionMode(ExtensionContext extensionContext) {
    Execution execution = findAnnotation(extensionContext.getTestClass(), Execution.class).orElse(null);
    if(execution == null) {
      String param = extensionContext.getConfigurationParameter("junit.jupiter.execution.parallel.mode.default").orElse(null);
      assert param != null;
      return ExecutionMode.valueOf(param.toUpperCase(Locale.ROOT));
    }
    return execution.value();
  }
}
