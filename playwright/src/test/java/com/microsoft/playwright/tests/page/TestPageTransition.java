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

package com.microsoft.playwright.tests.page;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import org.junit.jupiter.api.DisplayName;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Port of tests/page/page-transition.spec.ts. */
public class TestPageTransition {
  @PlaywrightTest
  @DisplayName("PA-038 should not crash when filter transition completes")
  @DisabledOnBrowser(value = BrowserName.WEBKIT, reason = "fixme: Web process crashes on the compositor thread when a filter transitions to none")
  void shouldNotCrashWhenFilterTransitionCompletes(Page page) {
    page.setContent("\n" +
      "    <style>\n" +
      "      div { transition: filter 0.1s; }\n" +
      "      .dim { filter: brightness(0.95); }\n" +
      "    </style>\n" +
      "    " + String.join("", Collections.nCopies(10, "<div>hello</div>")) + "\n" +
      "  ");
    boolean[] crashed = {false};
    page.onCrash(p -> crashed[0] = true);
    for (int i = 0; i < 10 && !crashed[0]; i++) {
      try {
        page.evaluate("async () => {\n" +
          "  const divs = [...document.querySelectorAll('div')];\n" +
          "  const settled = () => Promise.all(divs.map(div => new Promise(f => div.addEventListener('transitionend', f, { once: true }))));\n" +
          "  let promise = settled();\n" +
          "  divs.forEach(div => div.classList.add('dim'));\n" +
          "  await promise;\n" +
          "  promise = settled();\n" +
          "  divs.forEach(div => div.classList.remove('dim'));\n" +
          "  await promise;\n" +
          "}");
      } catch (PlaywrightException e) {
        // .catch(() => {})
      }
    }
    assertEquals(false, crashed[0]);
  }
}
