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
import com.microsoft.playwright.impl.Utils;
import com.microsoft.playwright.junit.Options;
import org.junit.jupiter.api.extension.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.microsoft.playwright.impl.junit.ExtensionUtils.*;

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
    Browser.NewContextOptions contextOptions = getContextOptions(playwright, options);
    browserContext = browser.newContext(contextOptions);
    if (recordTrace(options)) {
      Tracing.StartOptions startOptions = new Tracing.StartOptions().setSnapshots(true).setScreenshots(true).setName("my test name").setTitle("my test title");
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
    if (options.trace.equals(Options.Trace.ON) || options.trace.equals(Options.Trace.RETAIN_ON_FAILURE)) {
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
    if (browserContext != null) {
      Path outputDir = getOutputDir(extensionContext);
      createDirs(outputDir);
      Tracing.StopOptions stopOptions = new Tracing.StopOptions().setPath(outputDir.resolve("trace.zip"));
      browserContext.tracing().stop(stopOptions);
    }
  }

  private static void createDirs(Path outputDir) {
    try {
      Files.createDirectories(outputDir);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static Path getOutputDir(ExtensionContext extensionContext) {
    BrowserType browserType = BrowserExtension.getBrowser().browserType();
    Path baseDir = getBaseDir(extensionContext);
    String outputDirName = extensionContext.getRequiredTestClass().getName() + "." +
      extensionContext.getRequiredTestMethod().getName() + "-" +
      browserType.name();
    return baseDir.resolve(outputDirName);
  }

  private static Path getBaseDir(ExtensionContext extensionContext) {
    Options options = OptionsExtension.getOptions(extensionContext);
    Path baseDir = options.outputDir;
    if (baseDir == null) {
      baseDir = Paths.get(System.getProperty("user.dir")).resolve("test-results");
    }
    return baseDir;
  }

  private void closeBrowserContext() {
    BrowserContext browserContext = threadLocalBrowserContext.get();
    threadLocalBrowserContext.remove();
    if (browserContext != null) {
      browserContext.close();
    }
  }

  private static boolean recordTrace(Options options) {
    return options.trace.equals(Options.Trace.ON) || options.trace.equals(Options.Trace.RETAIN_ON_FAILURE);
  }

  private static Browser.NewContextOptions getContextOptions(Playwright playwright, Options options) {
    Browser.NewContextOptions contextOptions = Utils.clone(options.contextOptions);
    if (contextOptions == null) {
      contextOptions = new Browser.NewContextOptions();
    }

    if (options.baseUrl != null) {
      contextOptions.setBaseURL(options.baseUrl);
    }

    if (options.deviceName != null) {
      DeviceDescriptor deviceDescriptor = DeviceDescriptor.findByName(playwright, options.deviceName);
      if (deviceDescriptor == null) {
        throw new PlaywrightException("Unknown device name: " + options.deviceName);
      }
      contextOptions.userAgent = deviceDescriptor.userAgent;
      if (deviceDescriptor.viewport != null) {
        contextOptions.setViewportSize(deviceDescriptor.viewport.width, deviceDescriptor.viewport.height);
      }
      contextOptions.deviceScaleFactor = deviceDescriptor.deviceScaleFactor;
      contextOptions.isMobile = deviceDescriptor.isMobile;
      contextOptions.hasTouch = deviceDescriptor.hasTouch;
    }

    if (options.ignoreHTTPSErrors != null) {
      contextOptions.setIgnoreHTTPSErrors(options.ignoreHTTPSErrors);
    }

    return contextOptions;
  }
}
