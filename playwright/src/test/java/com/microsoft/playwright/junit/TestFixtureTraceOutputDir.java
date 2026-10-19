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

package com.microsoft.playwright.junit;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@FixtureTest
@UsePlaywright
public class TestFixtureTraceOutputDir {
  @TempDir
  static Path outputDir;
  private static String browserName;

  // A method rather than a field because outputDir is injected after the class is initialized.
  @PlaywrightOptions
  static Options options() {
    return new Options().setTrace(Options.Trace.ON).setOutputDir(outputDir);
  }

  @Test
  void shouldSaveTrace(Browser browser, Page page) {
    browserName = browser.browserType().name();
    page.setContent("<div>test</div>");
  }

  @ParameterizedTest(name = "[{index}] {0}")
  @ValueSource(strings = {"foo", "bar"})
  void shouldSaveTracePerParameterizedInvocation(String value, Page page) {
    page.setContent("<div>" + value + "</div>");
  }

  @RepeatedTest(2)
  void shouldSaveTracePerRepetition(Page page) {
    page.setContent("<div>repeated</div>");
  }

  @AfterAll
  static void shouldWriteTraceOfEachInvocationToSeparateDir() throws IOException {
    String prefix = TestFixtureTraceOutputDir.class.getName() + ".";
    List<String> expected = Stream.of(
      "shouldSaveTrace-" + browserName,
      "shouldSaveTracePerParameterizedInvocation-" + browserName + "--1-foo",
      "shouldSaveTracePerParameterizedInvocation-" + browserName + "--2-bar",
      "shouldSaveTracePerRepetition-" + browserName + "-repetition-1-of-2",
      "shouldSaveTracePerRepetition-" + browserName + "-repetition-2-of-2"
    ).map(name -> prefix + name).sorted().collect(Collectors.toList());
    List<String> actual;
    try (Stream<Path> dirs = Files.list(outputDir)) {
      actual = dirs.map(dir -> dir.getFileName().toString()).sorted().collect(Collectors.toList());
    }
    assertEquals(expected, actual);
    for (String dir : actual) {
      assertTrue(Files.exists(outputDir.resolve(dir).resolve("trace.zip")), dir);
    }
  }
}
