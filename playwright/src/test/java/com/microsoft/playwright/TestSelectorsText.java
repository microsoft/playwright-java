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

import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestSelectorsText extends TestBase {

  @Test
  void shouldWorkSmoke() {
    page.setContent("<div>Hi&gt;&gt;<span></span></div>");
    assertEquals("<span></span>", page.evalOnSelector("text=\"Hi>>\">>span", "e => e.outerHTML"));
    assertEquals("<span></span>", page.evalOnSelector("text=/Hi\\>\\>/ >> span", "e => e.outerHTML"));

    page.setContent("<div>let's<span>hello</span></div>");
    assertEquals("<span>hello</span>", page.evalOnSelector("text=/let's/i >> span", "e => e.outerHTML"));
    assertEquals("<span>hello</span>", page.evalOnSelector("text=/let\'s/i >> span", "e => e.outerHTML"));
  }

  @Test
  void hasTextAndInternalTextShouldMatchFullNodeTextInStrictMode() {
    page.setContent("<div id=div1>hello<span>world</span></div>\n" +
      "    <div id=div2>hello</div>");
    assertThat(page.getByText("helloworld", new Page.GetByTextOptions().setExact(true))).hasId("div1");
    assertThat(page.getByText("hello", new Page.GetByTextOptions().setExact(true))).hasId("div2");
    assertThat(page.locator("div", new Page.LocatorOptions().setHasText(Pattern.compile("^helloworld$")))).hasId("div1");
    assertThat(page.locator("div", new Page.LocatorOptions().setHasText(Pattern.compile("^hello$")))).hasId("div2");

    page.setContent("<div id=div1><span id=span1>hello</span>world</div>\n" +
      "    <div id=div2><span id=span2>hello</span></div>");
    assertThat(page.getByText("helloworld", new Page.GetByTextOptions().setExact(true))).hasId("div1");
    assertEquals(asList("span1", "span2"), page.getByText("hello", new Page.GetByTextOptions().setExact(true)).evaluateAll("els => els.map(e => e.id)"));
    assertThat(page.locator("div", new Page.LocatorOptions().setHasText(Pattern.compile("^helloworld$")))).hasId("div1");
    assertThat(page.locator("div", new Page.LocatorOptions().setHasText(Pattern.compile("^hello$")))).hasId("div2");
  }
}
