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
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

// Java-side implementation of `hasScreenshot()`. The pixel-level comparison
// itself is done by the driver via "Page.expectScreenshot"; this class only
// resolves the baseline file and decides whether to create/update/compare it.
//
// There is no test runner here to derive a snapshot name from, so a name is
// always required explicitly. Baselines are stored under
// "src/test/resources/__screenshots__/<name>", overridable via the
// "playwright.snapshotDir" system property; "-Dplaywright.updateSnapshots=true"
// (re-)generates baselines.
class ScreenshotAssertionsHelper {
  private static final String SNAPSHOT_DIR_PROPERTY = "playwright.snapshotDir";
  private static final String DEFAULT_SNAPSHOT_DIR = "src/test/resources/__screenshots__";
  private static final String UPDATE_SNAPSHOTS_PROPERTY = "playwright.updateSnapshots";

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

    PageExpectScreenshotOptions protocolOptions = toProtocolOptions(options);
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
      writeFile(result.actual, expectedPath);
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

  private PageExpectScreenshotOptions toProtocolOptions(ScreenshotAssertionsOptions options) {
    PageExpectScreenshotOptions result = new PageExpectScreenshotOptions();
    result.locator = locator;
    result.animations = options.animations == null ? ScreenshotAnimations.DISABLED : options.animations;
    result.caret = options.caret == null ? ScreenshotCaret.HIDE : options.caret;
    result.clip = options.clip;
    result.fullPage = options.fullPage;
    result.omitBackground = options.omitBackground;
    result.scale = options.scale == null ? ScreenshotScale.CSS : options.scale;
    result.maxDiffPixels = options.maxDiffPixels;
    result.maxDiffPixelRatio = options.maxDiffPixelRatio;
    result.threshold = options.threshold;
    result.maskColor = options.maskColor;
    result.style = options.style;
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
    String[] segments = toSegments(nameOrNames);
    String lastSegment = segments[segments.length - 1];
    // The driver's comparator only supports PNG ("Only PNG screenshots are supported").
    if (!lastSegment.toLowerCase(Locale.ROOT).endsWith(".png")) {
      throw new PlaywrightException("Screenshot name \"" + lastSegment + "\" must have a '.png' extension");
    }
    String baseDir = System.getProperty(SNAPSHOT_DIR_PROPERTY, DEFAULT_SNAPSHOT_DIR);
    Path snapshotDir = Paths.get(baseDir).toAbsolutePath().normalize();
    Path path = snapshotDir;
    for (String segment : segments) {
      Path segmentPath = Paths.get(segment);
      if (segmentPath.isAbsolute()) {
        throw new PlaywrightException("Screenshot name must be relative: " + segment);
      }
      path = path.resolve(segmentPath).normalize();
    }

    if (!path.startsWith(snapshotDir)) {
      throw new PlaywrightException("Screenshot name resolves outside the snapshot directory: " + path);
    }
    return path;
  }

  private static String[] toSegments(Object nameOrNames) {
    if (nameOrNames instanceof String[]) {
      String[] segments = (String[]) nameOrNames;
      if (segments.length == 0) {
        throw new PlaywrightException("Screenshot name segments must not be empty");
      }
      for (String segment : segments) {
        if (segment == null || segment.isEmpty()) {
          throw new PlaywrightException("Screenshot name segments must not be null or empty");
        }
      }
      return segments;
    }
    if (nameOrNames instanceof String && !((String) nameOrNames).isEmpty()) {
      return new String[] { (String) nameOrNames };
    }
    throw new PlaywrightException(
      "A screenshot name is required, for example: assertThat(page).hasScreenshot(\"example.png\")");
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
      writeFile(result.actual, actualDebugPath(expectedPath));
    }
    if (result.diff != null) {
      writeFile(result.diff, diffDebugPath(expectedPath));
    }
  }

  private static void writeFile(byte[] bytes, Path path) {
    Path temp = null;
    try {
      temp = Files.createTempFile(path.getParent(), path.getFileName().toString(), ".tmp");
      Utils.writeToFile(bytes, temp);
      try {
        Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
      } catch (AtomicMoveNotSupportedException e) {
        Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING);
      }
    } catch (IOException e) {
      try {
        if (temp != null) {
          Files.deleteIfExists(temp);
        }
      } catch (IOException ignored) {
        // Preserve the original write failure.
      }
      throw new PlaywrightException("Failed to write screenshot file: " + path, e);
    }
  }

  private static String callLog(List<String> log) {
    if (log == null || log.isEmpty()) {
      return "";
    }
    return "\nCall log:\n" + String.join("\n", log);
  }
}
