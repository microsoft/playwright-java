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

import com.microsoft.playwright.options.BoundingBox;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestPageCheck extends TestBase {
  @Test
  void shouldCheckTheLabelWithPosition() {
    page.setContent("<input id='checkbox' type='checkbox' style='width: 5px; height: 5px;'>\n" +
      "    <label for='checkbox'>\n" +
      "      <a href=" + server.EMPTY_PAGE + ">I am a long link that goes away so that nothing good will happen if you click on me</a>\n" +
      "      Click me\n" +
      "    </label>");
    BoundingBox box = page.querySelector("text=Click me").boundingBox();
    page.check("text=Click me", new Page.CheckOptions().setPosition(box.width - 10, 2));
    assertEquals(true, page.evalOnSelector("input", "input => input.checked"));
  }

  @Test
  void trialRunShouldNotCheck() {
    page.setContent("<input id='checkbox' type='checkbox'></input>");
    page.check("input", new Page.CheckOptions().setTrial(true));
    assertEquals(false, page.evaluate("() => window['checkbox'].checked"));
  }

  @Test
  void trialRunShouldNotUncheck() {
    page.setContent("<input id='checkbox' type='checkbox' checked></input>");
    page.uncheck("input", new Page.UncheckOptions().setTrial(true));
    assertEquals(true, page.evaluate("() => window['checkbox'].checked"));
  }

  @Test
  void shouldCheckTheBoxUsingSetChecked() {
    page.setContent("<input id='checkbox' type='checkbox'></input>");
    page.setChecked("input", true);
    assertEquals(true, page.evaluate("() => window['checkbox'].checked"));
    page.setChecked("input", false);
    assertEquals(false, page.evaluate("() => window['checkbox'].checked"));
  }
}
