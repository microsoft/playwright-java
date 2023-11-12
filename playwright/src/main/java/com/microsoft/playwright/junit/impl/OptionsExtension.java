package com.microsoft.playwright.junit.impl;

import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.junit.Options;
import com.microsoft.playwright.junit.UsePlaywright;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import static com.microsoft.playwright.junit.impl.ExtensionUtils.getUsePlaywrightAnnotation;

public class OptionsExtension implements AfterAllCallback {
  private static final ThreadLocal<Options> threadLocalOptions = new ThreadLocal<>();

  @Override
  public void afterAll(ExtensionContext extensionContext) {
    threadLocalOptions.remove();
  }

  static Options getOptions(ExtensionContext extensionContext) {
    Options options = threadLocalOptions.get();
    if (options != null) {
      return options;
    }

    UsePlaywright usePlaywrightAnnotation = getUsePlaywrightAnnotation(extensionContext);
    try {
      options = usePlaywrightAnnotation.options().newInstance();
      threadLocalOptions.set(options);
    } catch (InstantiationException | IllegalAccessException e) {
      throw new PlaywrightException("Failed to create options", e);
    }
    return options;
  }
}
