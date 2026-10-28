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

package com.microsoft.playwright.fixtures;

import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * The {@code testInfo} of the running test: a per-test output directory and the golden
 * screenshots, the parts of Playwright Test's {@code TestInfo} the upstream tests use.
 */
public final class TestInfo implements AutoCloseable {
  private final ExtensionContext context;
  private final BrowserName browserName;
  private Path outputDir;

  TestInfo(ExtensionContext context, BrowserName browserName) {
    this.context = context;
    this.browserName = browserName;
  }

  /** The upstream title, from the {@code @DisplayName} of the test method. */
  public String title() {
    return context.getParent().orElse(context).getDisplayName();
  }

  public Class<?> testClass() {
    return context.getRequiredTestClass();
  }

  public BrowserName browserName() {
    return browserName;
  }

  /** A temporary directory of this test, deleted when the test ends. */
  public synchronized Path outputDir() {
    if (outputDir == null) {
      try {
        outputDir = Files.createTempDirectory("playwright-test-");
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }
    return outputDir;
  }

  /** {@code testInfo.outputPath('test.har')}: a path in {@link #outputDir()}, its parent created. */
  public Path outputPath(String... segments) {
    Path path = outputDir();
    for (String segment : segments) {
      path = path.resolve(segment);
    }
    try {
      Files.createDirectories(path.getParent());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return path;
  }

  /**
   * The golden file of {@code expect(buffer).toMatchSnapshot(name)}: under
   * {@code src/test/snapshots/<group>/<spec>.spec.ts-snapshots/<name>-<browser>.<ext>}, where
   * group and spec come from the test class ({@code tests.page.TestPageScreenshot} is
   * {@code page/page-screenshot}).
   */
  public Path snapshotPath(String name) {
    Class<?> outer = testClass();
    while (outer.getEnclosingClass() != null) {
      outer = outer.getEnclosingClass();
    }
    String packageName = outer.getPackage().getName();
    String marker = ".tests.";
    int index = packageName.indexOf(marker);
    String group = index == -1 ? "" : packageName.substring(index + marker.length()).replace('.', '/');
    String spec = kebabCase(outer.getSimpleName().replaceFirst("^Test", ""));
    int dot = name.lastIndexOf('.');
    String base = dot == -1 ? name : name.substring(0, dot);
    String extension = dot == -1 ? "" : name.substring(dot);
    return Env.srcTestDir().resolve("snapshots").resolve(group).resolve(spec + ".spec.ts-snapshots")
      .resolve(base + "-" + browserName + extension);
  }

  static String kebabCase(String camel) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < camel.length(); i++) {
      char c = camel.charAt(i);
      boolean boundary = i > 0 && (Character.isUpperCase(c) || Character.isDigit(c) && !Character.isDigit(camel.charAt(i - 1)));
      if (boundary) {
        result.append('-');
      }
      result.append(Character.toLowerCase(c));
    }
    return result.toString();
  }

  /** {@code expect(screenshot).toMatchSnapshot(name)}: pixelmatch at threshold 0.2 with no different pixel allowed. */
  public void expectToMatchSnapshot(byte[] actual, String name) {
    expectToMatchSnapshot(actual, name, 0);
  }

  /** {@code expect(screenshot).toMatchSnapshot(name, { maxDiffPixels })}. Set {@code PW_UPDATE_SNAPSHOTS=1} to write the golden files. */
  public void expectToMatchSnapshot(byte[] actual, String name, int maxDiffPixels) {
    Path golden = snapshotPath(name);
    try {
      if (System.getenv("PW_UPDATE_SNAPSHOTS") != null || !Files.exists(golden)) {
        Files.createDirectories(golden.getParent());
        Files.write(golden, actual);
        if (System.getenv("PW_UPDATE_SNAPSHOTS") == null) {
          fail("A snapshot doesn't exist at " + golden + ", writing actual.");
        }
        return;
      }
      byte[] expected = Files.readAllBytes(golden);
      String error = ImageComparator.compare(expected, actual, maxDiffPixels);
      if (error != null) {
        Path actualPath = outputPath(name.replaceFirst("(\\.[^.]+)?$", "-actual$1"));
        Files.write(actualPath, actual);
        fail("Screenshot comparison failed for " + golden + ": " + error + " Actual written to " + actualPath);
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void close() throws IOException {
    if (outputDir != null) {
      try (Stream<Path> paths = Files.walk(outputDir)) {
        paths.sorted(Comparator.reverseOrder()).forEach(path -> {
          try {
            Files.deleteIfExists(path);
          } catch (IOException e) {
            // Best effort, like removeFolders upstream.
          }
        });
      }
    }
  }
}
