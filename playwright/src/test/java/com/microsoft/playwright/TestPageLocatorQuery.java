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
import static org.junit.jupiter.api.Assertions.*;

public class TestPageLocatorQuery extends TestBase {
  @Test
  void shouldRespectFirstAndLast() {
    page.setContent("<section>\n" +
      "    <div><p>A</p></div>\n" +
      "    <div><p>A</p><p>A</p></div>\n" +
      "    <div><p>A</p><p>A</p><p>A</p></div>\n" +
      "  </section>");
    assertEquals(6, page.locator("div >> p").count());
    assertEquals(6, page.locator("div").locator("p").count());
    assertEquals(1, page.locator("div").first().locator("p").count());
    assertEquals(3, page.locator("div").last().locator("p").count());
  }

  @Test
  void shouldRespectNth() {
    page.setContent("<section>\n" +
      "    <div><p>A</p></div>\n" +
      "    <div><p>A</p><p>A</p></div>\n" +
      "    <div><p>A</p><p>A</p><p>A</p></div>\n" +
      "  </section>");
    assertEquals(1, page.locator("div >> p").nth(0).count());
    assertEquals(2, page.locator("div").nth(1).locator("p").count());
    assertEquals(3, page.locator("div").nth(2).locator("p").count());
  }

  @Test
  void shouldThrowOnCaptureWNth() {
    page.setContent("<section><div><p>A</p></div></section>");
    try {
      page.locator("*css=div >> p").nth(1).click();
      fail("did not throw");
    } catch (PlaywrightException e) {
      assertTrue(e.getMessage().contains("Can't query n-th element"), e.getMessage());
    }
  }

  @Test
  void shouldThrowOnDueToStrictness() {
    page.setContent("<div>A</div><div>B</div>");
    try {
      page.locator("div").isVisible();
      fail("did not throw");
    } catch (PlaywrightException e) {
      assertTrue(e.getMessage().contains("strict mode violation"), e.getMessage());
    }
  }

  @Test
  void shouldThrowOnDueToStrictness2() {
    page.setContent("<select><option>One</option><option>Two</option></select>");
    try {
      page.locator("option").evaluate("e => {}");
      fail("did not throw");
    } catch (PlaywrightException e) {
      assertTrue(e.getMessage().contains("strict mode violation"), e.getMessage());
    }
  }

  @Test
  void shouldFilterByText() {
    page.setContent("<div>Foobar</div><div>Bar</div>");
    assertEquals("Foobar", page.locator("div", new Page.LocatorOptions().setHasText("Foo")).textContent());
  }

  @Test
  void shouldFilterByText2() {
    page.setContent("<div>foo <span>hello world</span> bar</div>");
    assertEquals("foo hello world bar", page.locator("div", new Page.LocatorOptions().setHasText("hello world")).textContent());
  }

  @Test
  void shouldFilterByRegex() {
    page.setContent("<div>Foobar</div><div>Bar</div>");
    assertEquals("Foobar", page.locator("div", new Page.LocatorOptions().setHasText(Pattern.compile("Foo.*"))).textContent());
  }

  @Test
  void shouldFilterByTextWithQuotes() {
    page.setContent("<div>Hello \"world\"</div><div>Hello world</div>");
    assertEquals("Hello \"world\"", page.locator("div", new Page.LocatorOptions().setHasText("Hello \"world\"")).textContent());
  }

  @Test
  void shouldFilterByRegexWithQuotes() {
    page.setContent("<div>Hello \"world\"</div><div>Hello world</div>");
    assertEquals("Hello \"world\"", page.locator("div", new Page.LocatorOptions().setHasText(Pattern.compile("Hello \"world\""))).textContent());
  }

  @Test
  void shouldFilterByRegexAndRegexpFlags() {
    page.setContent("<div>Hello \"world\"</div><div>Hello world</div>");
    Pattern pattern = Pattern.compile("hElLo \"world\"", Pattern.CASE_INSENSITIVE);
    assertEquals("Hello \"world\"", page.locator("div", new Page.LocatorOptions().setHasText(pattern)).textContent());
  }

  @Test
  void shouldSupportHasLocator() {
    page.setContent("<div><span>hello</span></div><div><span>world</span></div>");
    assertThat(page.locator("div", new Page.LocatorOptions().setHas(page.locator("text=world")))).hasCount(1);
    assertEquals("<div><span>world</span></div>", page.locator("div", new Page.LocatorOptions().setHas(page.locator("text=world"))).evaluate("e => e.outerHTML"));
    assertThat(page.locator("div", new Page.LocatorOptions().setHas(page.locator("text='hello'")))).hasCount(1);
    assertEquals("<div><span>hello</span></div>", page.locator("div", new Page.LocatorOptions().setHas(page.locator("text='hello'"))).evaluate("e => e.outerHTML"));
    assertThat(page.locator("div", new Page.LocatorOptions().setHas(page.locator("xpath=./span")))).hasCount(2);
    assertThat(page.locator("div", new Page.LocatorOptions().setHas(page.locator("span")))).hasCount(2);
    assertThat(page.locator("div", new Page.LocatorOptions().setHas(page.locator("span", new Page.LocatorOptions().setHasText("wor"))))).hasCount(1);
    assertEquals("<div><span>world</span></div>", page.locator("div", new Page.LocatorOptions().setHas(
      page.locator("span", new Page.LocatorOptions().setHasText("wor")))).evaluate("e => e.outerHTML"));
    assertThat(page.locator("div", new Page.LocatorOptions()
        .setHas(page.locator("span")).setHasText("wor"))).hasCount(1);
  }

  @Test
  void shouldSupportLocatorFilter() {
    page.setContent("<section><div><span>hello</span></div><div><span>world</span></div></section>");
    assertThat(page.locator("div").filter(new Locator.FilterOptions().setHasText("hello"))).hasCount(1);
    assertThat(page.locator("div", new Page.LocatorOptions().setHasText("hello")).filter(new Locator.FilterOptions().setHasText("hello"))).hasCount(1);
    assertThat(page.locator("div", new Page.LocatorOptions().setHasText("hello")).filter(new Locator.FilterOptions().setHasText("world"))).hasCount(0);
    assertThat(page.locator("section", new Page.LocatorOptions().setHasText("hello")).filter(new Locator.FilterOptions().setHasText("world"))).hasCount(1);
    assertThat(page.locator("div").filter(new Locator.FilterOptions().setHasText("hello")).locator("span")).hasCount(1);
    assertThat(page.locator("div").filter(new Locator.FilterOptions().setHas(page.locator("span", new Page.LocatorOptions().setHasText("world"))))).hasCount(1);
    assertThat(page.locator("div").filter(new Locator.FilterOptions().setHas(page.locator("span")))).hasCount(2);
    assertThat(page.locator("div").filter(new Locator.FilterOptions()
      .setHas(page.locator("span"))
      .setHasText("world"))).hasCount(1);
  }
}
