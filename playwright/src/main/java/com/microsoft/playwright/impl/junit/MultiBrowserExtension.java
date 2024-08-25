package com.microsoft.playwright.impl.junit;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.platform.commons.util.AnnotationUtils.*;

public class MultiBrowserExtension implements TestTemplateInvocationContextProvider {

  private static final String SELECTED_BROWSERS = "browsers";

  @Override
  public boolean supportsTestTemplate(ExtensionContext context) {
    if (!context.getTestMethod().isPresent()) {
      return false;
    }

    Method testMethod = context.getTestMethod().get();
    Optional<MultiBrowser> annotation = findAnnotation(testMethod, MultiBrowser.class);

    if (!annotation.isPresent()) {
      return false;
    }

    context.getStore(PlaywrightExtension.namespace).put(SELECTED_BROWSERS, annotation.get().browsers());
    return true;
  }

  @Override
  public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext extensionContext) {
    return Arrays.stream(extensionContext.getStore(PlaywrightExtension.namespace).get(SELECTED_BROWSERS, String[].class)).map(MultiBrowserTestTemplateInvocationContext::new);
  }

}
