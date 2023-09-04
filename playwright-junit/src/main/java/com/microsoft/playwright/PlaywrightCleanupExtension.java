package com.microsoft.playwright;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static com.microsoft.playwright.BrowserExtension.closeAllBrowsers;
import static com.microsoft.playwright.ExtensionUtils.getExecutionMode;
import static com.microsoft.playwright.PlaywrightExtension.closePlaywright;

public class PlaywrightCleanupExtension implements AfterEachCallback, AfterAllCallback {
  @Override
  public void afterAll(ExtensionContext extensionContext) {
    if (getExecutionMode(extensionContext) == ExecutionMode.SAME_THREAD) {
      cleanup();
    }
  }

  @Override
  public void afterEach(ExtensionContext extensionContext) {
    if (getExecutionMode(extensionContext) == ExecutionMode.CONCURRENT) {
      cleanup();
    }
  }

  private void cleanup() {
    closeAllBrowsers();
    closePlaywright();
  }
}
