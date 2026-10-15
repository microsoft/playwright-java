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

import com.microsoft.playwright.*;
import com.microsoft.playwright.junit.Options;
import org.junit.jupiter.api.extension.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static com.microsoft.playwright.impl.junit.ExtensionUtils.*;
import static com.microsoft.playwright.impl.junit.PageExtension.cleanUpPage;

public class BrowserContextExtension implements ParameterResolver, TestWatcher {
  private static final ThreadLocal<BrowserContext> threadLocalBrowserContext = new ThreadLocal<>();

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    return !isClassHook(extensionContext) && isParameterSupported(parameterContext, extensionContext, BrowserContext.class);
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    return getOrCreateBrowserContext(extensionContext);
  }

  /**
   * Returns the BrowserContext that belongs to the current test.  Will be created if it doesn't already exist.
   * <strong>NOTE:</strong> this method is subject to change.
   *
   * @param extensionContext the context in which the current test or container is being executed.
   * @return The BrowserContext that belongs to the current test.
   */
  public static BrowserContext getOrCreateBrowserContext(ExtensionContext extensionContext) {
    BrowserContext browserContext = threadLocalBrowserContext.get();
    if (browserContext != null) {
      return browserContext;
    }

    Options options = OptionsExtension.getOptions(extensionContext);
    Playwright playwright = PlaywrightExtension.getOrCreatePlaywright(extensionContext);
    setTestIdAttribute(playwright, options);
    Browser browser = BrowserExtension.getOrCreateBrowser(extensionContext);
    Browser.NewContextOptions contextOptions = BrowserContextOptionsExtension.getContextOptions(playwright, options);
    browserContext = browser.newContext(contextOptions);
    if (options.defaultTimeout != null) {
      browserContext.setDefaultTimeout(options.defaultTimeout);
    }
    if (shouldRecordTrace(options)) {
      Tracing.StartOptions startOptions = new Tracing.StartOptions().setSnapshots(true).setScreenshots(true).setTitle(extensionContext.getDisplayName());
      if (System.getenv("PLAYWRIGHT_JAVA_SRC") != null) {
        startOptions.setSources(true);
      }
      browserContext.tracing().start(startOptions);
    }
    threadLocalBrowserContext.set(browserContext);
    return browserContext;
  }

  @Override
  public void testSuccessful(ExtensionContext extensionContext) {
    saveTraceWhenOn(extensionContext);
    closeBrowserContext();
  }

  @Override
  public void testAborted(ExtensionContext extensionContext, Throwable cause) {
    saveTraceWhenOn(extensionContext);
    closeBrowserContext();
  }

  @Override
  public void testFailed(ExtensionContext extensionContext, Throwable cause) {
    Options options = OptionsExtension.getOptions(extensionContext);
    if (shouldRecordTrace(options)) {
      saveTrace(extensionContext);
    }
    closeBrowserContext();
  }

  private static void saveTraceWhenOn(ExtensionContext extensionContext) {
    Options options = OptionsExtension.getOptions(extensionContext);
    if (options.trace.equals(Options.Trace.ON)) {
      saveTrace(extensionContext);
    }
  }

  private static void saveTrace(ExtensionContext extensionContext) {
    BrowserContext browserContext = threadLocalBrowserContext.get();
    if (browserContext == null) {
      return;
    }
    Path outputPath = getOutputPath(extensionContext);
    createOutputPath(outputPath);
    Tracing.StopOptions stopOptions = new Tracing.StopOptions().setPath(outputPath.resolve("trace.zip"));
    browserContext.tracing().stop(stopOptions);
  }

  private static void createOutputPath(Path outputPath) {
    if (!Files.exists(outputPath)) {
      try {
        Files.createDirectories(outputPath);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private static Path getOutputPath(ExtensionContext extensionContext) {
    BrowserType browserType = BrowserExtension.getBrowser().browserType();
    Path defaultOutputPath = getDefaultOutputPath(extensionContext);
    String outputDirName = extensionContext.getRequiredTestClass().getName() + "." +
      extensionContext.getRequiredTestMethod().getName() + "-" +
      browserType.name();
    if (isTestTemplateInvocation(extensionContext)) {
      // Each invocation of a parameterized or repeated test gets its own directory.
      outputDirName += "-" + trimLongString(sanitizeForFilePath(extensionContext.getDisplayName()), 60);
    }
    return defaultOutputPath.resolve(outputDirName);
  }

  private static boolean isTestTemplateInvocation(ExtensionContext extensionContext) {
    return extensionContext.getParent().flatMap(ExtensionContext::getTestMethod).isPresent();
  }

  private static String sanitizeForFilePath(String s) {
    return s.replaceAll("[\\x00-\\x2C\\x2E-\\x2F\\x3A-\\x40\\x5B-\\x60\\x7B-\\x7F]+", "-");
  }

  private static String trimLongString(String s, int length) {
    if (s.length() <= length) {
      return s;
    }
    String middle = "-" + calculateSha1(s).substring(0, 5) + "-";
    int start = (length - middle.length()) / 2;
    int end = length - middle.length() - start;
    return s.substring(0, start) + middle + s.substring(s.length() - end);
  }

  private static String calculateSha1(String s) {
    try {
      byte[] digest = MessageDigest.getInstance("SHA-1").digest(s.getBytes(StandardCharsets.UTF_8));
      StringBuilder hex = new StringBuilder();
      for (byte b : digest) {
        hex.append(String.format("%02x", b));
      }
      return hex.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  private static Path getDefaultOutputPath(ExtensionContext extensionContext) {
    Options options = OptionsExtension.getOptions(extensionContext);
    Path outputPath = options.outputDir;
    if (outputPath == null) {
      outputPath = Paths.get(System.getProperty("user.dir")).resolve("test-results");
    }
    return outputPath;
  }

  private void closeBrowserContext() {
    cleanUpPage();
    BrowserContext browserContext = threadLocalBrowserContext.get();
    threadLocalBrowserContext.remove();
    if (browserContext != null) {
      browserContext.close();
    }
  }

  private static boolean shouldRecordTrace(Options options) {
    return options.trace.equals(Options.Trace.ON) || options.trace.equals(Options.Trace.RETAIN_ON_FAILURE);
  }
}
