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

package com.microsoft.playwright;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class TestCoverage extends TestBase {
  @BeforeEach
  void onlyChromium() {
    assumeTrue(isChromium());
  }

  @Test
  void shouldCollectJavaScriptCoverage() {
    page.coverage().startJSCoverage();
    page.evaluate("() => eval('function foo() { return 42; } foo(); //# sourceURL=nice-name.js')");
    List<Coverage.ScriptCoverage> coverage = page.coverage().stopJSCoverage();

    assertEquals(1, coverage.size());
    assertEquals("nice-name.js", coverage.get(0).url);
    Coverage.FunctionCoverage function = coverage.get(0).functions.stream()
      .filter(entry -> "foo".equals(entry.functionName))
      .findFirst()
      .orElseThrow(AssertionError::new);
    assertEquals(1, function.ranges.get(0).count);
  }

  @Test
  void shouldReportAnonymousScriptsWhenEnabled() {
    page.coverage().startJSCoverage(new Coverage.StartJSCoverageOptions().setReportAnonymousScripts(true));
    page.evaluate("() => eval('2 + 2')");
    List<Coverage.ScriptCoverage> coverage = page.coverage().stopJSCoverage();

    assertTrue(coverage.stream().anyMatch(entry -> "2 + 2".equals(entry.source)));
  }

  @Test
  void shouldResetJavaScriptCoverageOnNavigation() {
    page.coverage().startJSCoverage();
    page.evaluate("() => eval('2 + 2 //# sourceURL=before-navigation.js')");
    page.navigate(server.EMPTY_PAGE);

    assertTrue(page.coverage().stopJSCoverage().isEmpty());
  }

  @Test
  void shouldCollectCSSCoverage() {
    page.coverage().startCSSCoverage();
    page.setContent("<style>div { color: green; } span { color: red; }</style><div>hello</div>");
    List<Coverage.StyleSheetCoverage> coverage = page.coverage().stopCSSCoverage();

    assertEquals(1, coverage.size());
    assertFalse(coverage.get(0).ranges.isEmpty());
    Coverage.StyleSheetCoverageRange range = coverage.get(0).ranges.get(0);
    assertEquals("div { color: green; }", coverage.get(0).text.substring(range.start, range.end));
  }
}
