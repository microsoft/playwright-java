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

package com.microsoft.playwright.impl;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.ScreenshotAnimations;
import com.microsoft.playwright.options.ScreenshotCaret;
import com.microsoft.playwright.options.ScreenshotScale;
import org.opentest4j.AssertionFailedError;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

// Implements the Java counterpart of `toHaveScreenshot()`, ported from the
// `SnapshotHelper` logic in `packages/playwright/src/matchers/toMatchSnapshot.ts`
// of the upstream Playwright repository. Since the Java bindings do not have a
// dedicated test-runner (unlike @playwright/test), the snapshot storage/update
// conventions below are a Java-specific adaptation:
//  - Snapshots are stored under "src/test/resources/__screenshots__/<TestClassSimpleName>/<name>"
//    by default. Override the root with the "playwright.snapshotDir" system property.
//  - Pass "-Dplaywright.updateSnapshots=true" to (re-)generate/update baseline snapshots,
//    analogous to "--update-snapshots" for @playwright/test.
// The actual pixel-level image comparison (pixelmatch/SSIM) is performed by the
// Playwright driver (Node.js) via the "Page.expectScreenshot" protocol method -
// this class only handles the Java-side snapshot lifecycle (resolving the file,
// reading/writing baselines) and error formatting.
class ScreenshotAssertionsHelper {
  private static final String SNAPSHOT_DIR_PROPERTY = "playwright.snapshotDir";
  private static final String DEFAULT_SNAPSHOT_DIR = "src/test/resources/__screenshots__";
  private static final String UPDATE_SNAPSHOTS_PROPERTY = "playwright.updateSnapshots";
  private static final ConcurrentHashMap<String, AtomicInteger> anonymousNameCounters = new ConcurrentHashMap<>();

  private final PageImpl page;
  private final LocatorImpl locator;
  private final boolean isNot;

  ScreenshotAssertionsHelper(PageImpl page, LocatorImpl locator, boolean isNot) {
    this.page = page;
    this.locator = locator;
    this.isNot = isNot;
  }

  void assertScreenshot(Object nameOrNames, ScreenshotAssertionsOptions options, String title) {
    if (options == null) {
      options = new ScreenshotAssertionsOptions();
    }
    Path expectedPath = resolveSnapshotPath(nameOrNames);
    String extension = expectedPath.getFileName().toString().toLowerCase().endsWith(".webp") ? "webp" : "png";

    PageExpectScreenshotOptions protocolOptions = toProtocolOptions(options, extension);
    protocolOptions.timeout = options.timeout == null ? AssertionsTimeout.defaultTimeout : options.timeout;
    protocolOptions.isNot = isNot;

    boolean hasSnapshot = Files.exists(expectedPath);

    if (isNot) {
      if (!hasSnapshot) {
        // Nothing to compare against - matchers using ".not()" won't write baselines automatically.
        return;
      }
      protocolOptions.expected = encode(readFile(expectedPath));
      PageImpl.ExpectScreenshotResult result = page.expectScreenshot(protocolOptions, title);
      if (result.errorMessage == null) {
        // Screenshots differ, exactly as ".not()" expects.
        return;
      }
      throw new AssertionFailedError(title + "\nScreenshot comparison failed:\n  Expected result should be different from the actual one." + callLog(result.log));
    }

    boolean updateAll = isUpdateSnapshotsAll();

    if (!hasSnapshot) {
      protocolOptions.expected = null;
      PageImpl.ExpectScreenshotResult result = page.expectScreenshot(protocolOptions, title);
      if (result.errorMessage != null) {
        writeDebugArtifacts(expectedPath, result);
        throw new AssertionFailedError(title + "\n" + result.errorMessage + callLog(result.log));
      }
      Utils.writeToFile(result.actual, expectedPath);
      return;
    }

    byte[] expectedBytes = readFile(expectedPath);
    if (updateAll) {
      protocolOptions.expected = null;
      PageImpl.ExpectScreenshotResult result = page.expectScreenshot(protocolOptions, title);
      if (result.errorMessage != null) {
        writeDebugArtifacts(expectedPath, result);
        throw new AssertionFailedError(title + "\n  Failed to re-generate expected.\n" + result.errorMessage + callLog(result.log));
      }
      if (!Arrays.equals(result.actual, expectedBytes)) {
        Utils.writeToFile(result.actual, expectedPath);
      }
      return;
    }

    protocolOptions.expected = encode(expectedBytes);
    PageImpl.ExpectScreenshotResult result = page.expectScreenshot(protocolOptions, title);
    if (result.errorMessage == null) {
      return;
    }
    writeDebugArtifacts(expectedPath, result);
    throw new AssertionFailedError(title + "\nScreenshot comparison failed:\n  " + result.errorMessage +
      callLog(result.log) + "\n\n  Expected: " + expectedPath +
      "\n  Actual: " + actualDebugPath(expectedPath) +
      (result.diff != null ? "\n  Diff: " + diffDebugPath(expectedPath) : ""));
  }

  private PageExpectScreenshotOptions toProtocolOptions(ScreenshotAssertionsOptions options, String extension) {
    PageExpectScreenshotOptions result = new PageExpectScreenshotOptions();
    result.locator = locator;
    result.animations = options.animations == null ? ScreenshotAnimations.DISABLED : options.animations;
    result.caret = options.caret == null ? ScreenshotCaret.HIDE : options.caret;
    result.clip = options.clip;
    result.fullPage = options.fullPage;
    result.omitBackground = options.omitBackground;
    result.scale = options.scale == null ? ScreenshotScale.CSS : options.scale;
    result.maxDiffPixels = options.maxDiffPixels == null ? null : options.maxDiffPixels.doubleValue();
    result.maxDiffPixelRatio = options.maxDiffPixelRatio;
    result.threshold = options.threshold;
    result.maskColor = options.maskColor;
    result.style = options.style;
    result.type = extension;
    if (options.mask != null) {
      List<LocatorImpl> mask = new ArrayList<>();
      for (Locator l : options.mask) {
        mask.add((LocatorImpl) l);
      }
      result.mask = mask;
    }
    return result;
  }

  private static boolean isUpdateSnapshotsAll() {
    return Boolean.parseBoolean(System.getProperty(UPDATE_SNAPSHOTS_PROPERTY, "false"));
  }

  private static Path resolveSnapshotPath(Object nameOrNames) {
    String name;
    if (nameOrNames instanceof String[]) {
      String[] segments = (String[]) nameOrNames;
      name = String.join("-", segments);
    } else if (nameOrNames instanceof String) {
      name = (String) nameOrNames;
    } else {
      name = null;
    }
    StackTraceElement caller = callerFrame();
    if (name == null || name.isEmpty()) {
      String key = caller.getClassName() + "#" + caller.getMethodName();
      int index = anonymousNameCounters.computeIfAbsent(key, k -> new AtomicInteger(0)).incrementAndGet();
      name = caller.getMethodName() + "-" + index + ".png";
    }
    if (!name.toLowerCase().endsWith(".png") && !name.toLowerCase().endsWith(".webp")) {
      throw new PlaywrightException("Screenshot name \"" + name + "\" must have a '.png' or '.webp' extension");
    }
    String baseDir = System.getProperty(SNAPSHOT_DIR_PROPERTY, DEFAULT_SNAPSHOT_DIR);
    String simpleClassName = simpleClassName(caller.getClassName());
    return Paths.get(baseDir, simpleClassName, name);
  }

  private static String simpleClassName(String className) {
    // Strip package name and any enclosing-class '$' qualifiers (e.g. anonymous/nested classes).
    int dot = className.lastIndexOf('.');
    String simple = dot == -1 ? className : className.substring(dot + 1);
    int dollar = simple.indexOf('$');
    return dollar == -1 ? simple : simple.substring(0, dollar);
  }

  private static StackTraceElement callerFrame() {
    for (StackTraceElement frame : Thread.currentThread().getStackTrace()) {
      String className = frame.getClassName();
      if (className.startsWith("java.") || className.startsWith("jdk.")) {
        continue;
      }
      if (className.startsWith("com.microsoft.playwright.impl.") || className.startsWith("com.microsoft.playwright.assertions.")) {
        continue;
      }
      return frame;
    }
    throw new PlaywrightException("Could not determine the caller of the screenshot assertion to infer the snapshot name");
  }

  private static byte[] readFile(Path path) {
    try {
      return Files.readAllBytes(path);
    } catch (IOException e) {
      throw new PlaywrightException("Failed to read snapshot file: " + path, e);
    }
  }

  private static String encode(byte[] bytes) {
    return Base64.getEncoder().encodeToString(bytes);
  }

  private static Path actualDebugPath(Path expectedPath) {
    return withSuffix(expectedPath, "-actual");
  }

  private static Path diffDebugPath(Path expectedPath) {
    return withSuffix(expectedPath, "-diff");
  }

  private static Path withSuffix(Path expectedPath, String suffix) {
    String fileName = expectedPath.getFileName().toString();
    int dot = fileName.lastIndexOf('.');
    String newFileName = dot == -1 ? fileName + suffix : fileName.substring(0, dot) + suffix + fileName.substring(dot);
    Path parent = expectedPath.getParent();
    return parent == null ? Paths.get(newFileName) : parent.resolve(newFileName);
  }

  private static void writeDebugArtifacts(Path expectedPath, PageImpl.ExpectScreenshotResult result) {
    if (result.actual != null) {
      Utils.writeToFile(result.actual, actualDebugPath(expectedPath));
    }
    if (result.diff != null) {
      Utils.writeToFile(result.diff, diffDebugPath(expectedPath));
    }
  }

  private static String callLog(List<String> log) {
    if (log == null || log.isEmpty()) {
      return "";
    }
    return "\nCall log:\n" + String.join("\n", log);
  }
}
