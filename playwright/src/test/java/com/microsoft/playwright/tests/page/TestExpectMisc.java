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

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.assertions.PageAssertions;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.options.PseudoElement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.opentest4j.AssertionFailedError;

import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.microsoft.playwright.fixtures.Utils.async;
import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static com.microsoft.playwright.fixtures.Utils.stripAnsi;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/expect-misc.spec.ts. */
public class TestExpectMisc {
  @Nested
  class ToHaveCount {
    @PlaywrightTest
    @DisplayName("EX-181 toHaveCount pass")
    void toHaveCountPass(Page page) throws Exception {
      page.setContent("<select><option>One</option></select>");
      Locator locator = page.locator("option");
      AtomicBoolean done = new AtomicBoolean(false);
      CompletableFuture<Void> promise = async(() -> {
        assertThat(locator).hasCount(2);
        done.set(true);
        return null;
      });
      page.waitForTimeout(1000);
      assertEquals(false, done.get());
      page.setContent("<select><option>One</option><option>Two</option></select>");
      promise.get();
      assertEquals(true, done.get());
    }

    @PlaywrightTest
    @DisplayName("EX-182 pass zero")
    void passZero(Page page) {
      page.setContent("<div></div>");
      Locator locator = page.locator("span");
      assertThat(locator).hasCount(0);
      assertThat(locator).not().hasCount(1);
    }

    @PlaywrightTest
    @DisplayName("EX-183 eventually pass zero")
    void eventuallyPassZero(Page page) {
      page.setContent("<div><span>hello</span></div>");
      Locator locator = page.locator("span");
      // setTimeout
      async(() -> {
        page.waitForTimeout(200);
        try {
          page.evaluate("() => document.querySelector('div').textContent = ''");
        } catch (PlaywrightException e) {
          // .catch(() => {})
        }
        return null;
      });
      assertThat(locator).hasCount(0);
      assertThat(locator).not().hasCount(1);
    }

    @PlaywrightTest
    @DisplayName("EX-184 eventually pass non-zero")
    void eventuallyPassNonZero(Page page) {
      page.setContent("<ul></ul>");
      // setTimeout
      async(() -> {
        page.waitForTimeout(500);
        page.setContent("<ul><li>one</li><li>two</li></ul>");
        return null;
      });
      Locator locator = page.locator("li");
      assertThat(locator).hasCount(2);
    }

    @PlaywrightTest
    @DisplayName("EX-185 eventually pass not non-zero")
    void eventuallyPassNotNonZero(Page page) {
      page.setContent("<ul><li>one</li><li>two</li></ul>");
      // setTimeout
      async(() -> {
        page.waitForTimeout(500);
        page.setContent("<ul></ul>");
        return null;
      });
      Locator locator = page.locator("li");
      assertThat(locator).not().hasCount(2);
    }

    @PlaywrightTest
    @DisplayName("EX-186 fail zero")
    @Fixme("java: the assertion error has another format: 'Locator expected to have count\nExpected: 0\nReceived: 1\n\nCall log:\n- Assert \"hasCount\" locator(\"span\") with timeout 1000ms ...' (no 'expect(locator)... failed' header, no 'Locator:' line, unquoted values, double quoted locators)")
    void failZero(Page page) {
      page.setContent("<div><span></span></div>");
      Locator locator = page.locator("span");
      AssertionFailedError error = assertThrows(AssertionFailedError.class,
        () -> assertThat(locator).hasCount(0, new LocatorAssertions.HasCountOptions().setTimeout(1000)));
      assertTrue(stripAnsi(error.getMessage()).contains("expect(locator).toHaveCount(expected) failed\n" +
        "\n" +
        "Locator:  locator('span')\n" +
        "Expected: 0\n" +
        "Received: 1\n" +
        "Timeout:  1000ms"));
      assertTrue(stripAnsi(error.getMessage()).contains("- Expect \"toHaveCount\" locator('span') with timeout 1000ms"));
    }

    @PlaywrightTest
    @DisplayName("EX-187 fail zero 2")
    @Fixme("java: the assertion error has another format: 'Locator expected not to have count\nExpected: 1\nReceived: 1\n\nCall log:\n- Assert \"hasCount\" locator(\"span\") with timeout 1000ms ...' (no 'expect(locator)... failed' header, no 'Locator:' line, unquoted values, double quoted locators)")
    void failZero2(Page page) {
      page.setContent("<div><span></span></div>");
      Locator locator = page.locator("span");
      AssertionFailedError error = assertThrows(AssertionFailedError.class,
        () -> assertThat(locator).not().hasCount(1, new LocatorAssertions.HasCountOptions().setTimeout(1000)));
      assertTrue(stripAnsi(error.getMessage()).contains("expect(locator).not.toHaveCount(expected) failed\n" +
        "\n" +
        "Locator:  locator('span')\n" +
        "Expected: not 1\n" +
        "Received: 1\n" +
        "Timeout:  1000ms"));
      assertTrue(stripAnsi(error.getMessage()).contains("- Expect \"not toHaveCount\" locator('span') with timeout 1000ms"));
    }
  }

  @Nested
  class ToHaveJSProperty {
    @PlaywrightTest
    @DisplayName("EX-188 pass")
    void pass(Page page) {
      page.setContent("<div></div>");
      page.evalOnSelector("div", "e => e.foo = { a: 1, b: 'string', c: new Date(1627503992000) }");
      Locator locator = page.locator("div");
      assertThat(locator).hasJSProperty("foo", mapOf("a", 1, "b", "string", "c", new Date(1627503992000L)));
    }

    @PlaywrightTest
    @DisplayName("EX-189 fail")
    @Fixme("java: the assertion error has another format: 'Locator expected to have JavaScript property 'foo'\nExpected: {a=1, b=string, c=Wed Jul 28 13:26:32 PDT 2021}\nReceived: {a=1, b=string, c=Wed Jul 28 13:26:32 PDT 2021}\n\nCall log:\n- Assert \"hasJSProperty\" locator(\"div\") with timeout 1000ms ...' (no 'expect(locator)... failed' header, no 'Locator:' line, unquoted values, double quoted locators)")
    void fail(Page page) {
      page.setContent("<div></div>");
      page.evalOnSelector("div", "e => e.foo = { a: 1, b: 'string', c: new Date(1627503992000) }");
      Locator locator = page.locator("div");
      AssertionFailedError error = assertThrows(AssertionFailedError.class,
        () -> assertThat(locator).hasJSProperty("foo", mapOf("a", 1, "b", "string", "c", new Date(1627503992001L)), new LocatorAssertions.HasJSPropertyOptions().setTimeout(1000)));
      assertTrue(error.getMessage().contains("-   \"c\""));
    }

    @PlaywrightTest
    @DisplayName("EX-190 pass string")
    void passString(Page page) {
      page.setContent("<div></div>");
      page.evalOnSelector("div", "e => e.foo = 'string'");
      Locator locator = page.locator("div");
      assertThat(locator).hasJSProperty("foo", "string");
    }

    @PlaywrightTest
    @DisplayName("EX-191 fail string")
    @Fixme("java: the assertion error has another format: 'Locator expected to have JavaScript property 'foo'\nExpected: error\nReceived: string\n\nCall log:\n- Assert \"hasJSProperty\" locator(\"div\") with timeout 200ms ...' (no 'expect(locator)... failed' header, no 'Locator:' line, unquoted values, double quoted locators)")
    void failString(Page page) {
      page.setContent("<div></div>");
      page.evalOnSelector("div", "e => e.foo = 'string'");
      Locator locator = page.locator("div");
      AssertionFailedError error = assertThrows(AssertionFailedError.class,
        () -> assertThat(locator).hasJSProperty("foo", "error", new LocatorAssertions.HasJSPropertyOptions().setTimeout(200)));
      assertTrue(stripAnsi(error.getMessage()).contains("expect(locator).toHaveJSProperty(expected) failed\n" +
        "\n" +
        "Locator:  locator('div')\n" +
        "Expected: \"error\"\n" +
        "Received: \"string\"\n" +
        "Timeout:  200ms"));
      assertTrue(stripAnsi(error.getMessage()).contains("- Expect \"toHaveJSProperty\" locator('div') with timeout 200ms"));
    }

    @PlaywrightTest
    @DisplayName("EX-192 pass number")
    void passNumber(Page page) {
      page.setContent("<div></div>");
      page.evalOnSelector("div", "e => e.foo = 2021");
      Locator locator = page.locator("div");
      assertThat(locator).hasJSProperty("foo", 2021);
    }

    @PlaywrightTest
    @DisplayName("EX-193 fail number")
    @Fixme("java: the assertion error has another format: 'Locator expected to have JavaScript property 'foo'\nExpected: 1\nReceived: 2021\n\nCall log:\n- Assert \"hasJSProperty\" locator(\"div\") with timeout 200ms ...' (no 'expect(locator)... failed' header, no 'Locator:' line, unquoted values, double quoted locators)")
    void failNumber(Page page) {
      page.setContent("<div></div>");
      page.evalOnSelector("div", "e => e.foo = 2021");
      Locator locator = page.locator("div");
      AssertionFailedError error = assertThrows(AssertionFailedError.class,
        () -> assertThat(locator).hasJSProperty("foo", 1, new LocatorAssertions.HasJSPropertyOptions().setTimeout(200)));
      assertTrue(stripAnsi(error.getMessage()).contains("expect(locator).toHaveJSProperty(expected) failed\n" +
        "\n" +
        "Locator:  locator('div')\n" +
        "Expected: 1\n" +
        "Received: 2021\n" +
        "Timeout:  200ms"));
      assertTrue(stripAnsi(error.getMessage()).contains("- Expect \"toHaveJSProperty\" locator('div') with timeout 200ms"));
    }

    @PlaywrightTest
    @DisplayName("EX-194 pass boolean")
    void passBoolean(Page page) {
      page.setContent("<div></div>");
      page.evalOnSelector("div", "e => e.foo = true");
      Locator locator = page.locator("div");
      assertThat(locator).hasJSProperty("foo", true);
    }

    @PlaywrightTest
    @DisplayName("EX-195 fail boolean")
    @Fixme("java: the assertion error has another format: 'Locator expected to have JavaScript property 'foo'\nExpected: true\nReceived: false\n\nCall log:\n- Assert \"hasJSProperty\" locator(\"div\") with timeout 200ms ...' (no 'expect(locator)... failed' header, no 'Locator:' line, unquoted values, double quoted locators)")
    void failBoolean(Page page) {
      page.setContent("<div></div>");
      page.evalOnSelector("div", "e => e.foo = false");
      Locator locator = page.locator("div");
      AssertionFailedError error = assertThrows(AssertionFailedError.class,
        () -> assertThat(locator).hasJSProperty("foo", true, new LocatorAssertions.HasJSPropertyOptions().setTimeout(200)));
      assertTrue(stripAnsi(error.getMessage()).contains("expect(locator).toHaveJSProperty(expected) failed\n" +
        "\n" +
        "Locator:  locator('div')\n" +
        "Expected: true\n" +
        "Received: false\n" +
        "Timeout:  200ms"));
      assertTrue(stripAnsi(error.getMessage()).contains("- Expect \"toHaveJSProperty\" locator('div') with timeout 200ms"));
    }

    @PlaywrightTest
    @DisplayName("EX-196 pass boolean 2")
    void passBoolean2(Page page) {
      page.setContent("<div></div>");
      page.evalOnSelector("div", "e => e.foo = false");
      Locator locator = page.locator("div");
      assertThat(locator).hasJSProperty("foo", false);
    }

    @PlaywrightTest
    @DisplayName("EX-197 fail boolean 2")
    @Fixme("java: the assertion error has another format: 'Locator expected to have JavaScript property 'foo'\nExpected: true\nReceived: false\n\nCall log:\n- Assert \"hasJSProperty\" locator(\"div\") with timeout 200ms ...' (no 'expect(locator)... failed' header, no 'Locator:' line, unquoted values, double quoted locators)")
    void failBoolean2(Page page) {
      page.setContent("<div></div>");
      page.evalOnSelector("div", "e => e.foo = false");
      Locator locator = page.locator("div");
      AssertionFailedError error = assertThrows(AssertionFailedError.class,
        () -> assertThat(locator).hasJSProperty("foo", true, new LocatorAssertions.HasJSPropertyOptions().setTimeout(200)));
      assertTrue(stripAnsi(error.getMessage()).contains("expect(locator).toHaveJSProperty(expected) failed\n" +
        "\n" +
        "Locator:  locator('div')\n" +
        "Expected: true\n" +
        "Received: false\n" +
        "Timeout:  200ms"));
      assertTrue(stripAnsi(error.getMessage()).contains("- Expect \"toHaveJSProperty\" locator('div') with timeout 200ms"));
    }

    @PlaywrightTest
    @DisplayName("EX-198 pass undefined")
    void passUndefined(Page page) {
      page.setContent("<div></div>");
      Locator locator = page.locator("div");
      // Java: undefined is null.
      assertThat(locator).hasJSProperty("foo", null);
    }

    @PlaywrightTest
    @DisplayName("EX-199 pass null")
    @Fixme("java: hasJSProperty(\"foo\", null) does not match a property whose value is null (the matching of null as undefined, EX-198, passes): the log says 'unexpected value \"null\"'")
    void passNull(Page page) {
      page.setContent("<div></div>");
      page.evalOnSelector("div", "e => e.foo = null");
      Locator locator = page.locator("div");
      assertThat(locator).hasJSProperty("foo", null);
    }

    @PlaywrightTest
    @DisplayName("EX-200 pass nested")
    void passNested(Page page) {
      page.setContent("<div></div>");
      page.evalOnSelector("div", "e => e.foo = { nested: { a: 1, b: 'string', c: new Date(1627503992000) } }");
      Locator locator = page.locator("div");
      assertThat(locator).hasJSProperty("foo.nested", mapOf("a", 1, "b", "string", "c", new Date(1627503992000L)));
      assertThat(locator).hasJSProperty("foo.nested.a", 1);
      assertThat(locator).hasJSProperty("foo.nested.b", "string");
      assertThat(locator).hasJSProperty("foo.nested.c", new Date(1627503992000L));
    }

    @PlaywrightTest
    @DisplayName("EX-201 fail nested")
    @Fixme("java: the assertion error has another format: 'Locator expected to have JavaScript property 'foo.bar'\nExpected: {a=1, b=string, c=Wed Jul 28 13:26:32 PDT 2021}\nReceived: null\n\nCall log:\n- Assert \"hasJSProperty\" locator(\"div\") with timeout 1000ms ...' (no 'expect(locator)... failed' header, no 'Locator:' line, unquoted values, double quoted locators)")
    void failNested(Page page) {
      page.setContent("<div></div>");
      page.evalOnSelector("div", "e => e.foo = { nested: { a: 1, b: 'string', c: new Date(1627503992000) } }");
      Locator locator = page.locator("div");
      AssertionFailedError error1 = assertThrows(AssertionFailedError.class,
        () -> assertThat(locator).hasJSProperty("foo.bar", mapOf("a", 1, "b", "string", "c", new Date(1627503992001L)), new LocatorAssertions.HasJSPropertyOptions().setTimeout(1000)));
      // expect.soft
      assertTrue(stripAnsi(error1.getMessage()).contains("Received: undefined"));
      AssertionFailedError error2 = assertThrows(AssertionFailedError.class,
        () -> assertThat(locator).hasJSProperty("foo.nested.a", 2, new LocatorAssertions.HasJSPropertyOptions().setTimeout(1000)));
      // expect.soft
      assertTrue(stripAnsi(error2.getMessage()).contains("Received: 1"));
    }
  }

  @Nested
  class ToHaveClass {
    @PlaywrightTest
    @DisplayName("EX-202 pass")
    void pass(Page page) {
      page.setContent("<div class=\"foo bar baz\"></div>");
      Locator locator = page.locator("div");
      assertThat(locator).hasClass("foo bar baz");
    }

    @PlaywrightTest
    @DisplayName("EX-203 pass with SVGs")
    void passWithSVGs(Page page) {
      page.setContent("<svg class=\"c1 c2\" role=\"img\" xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 512 512\"></svg>");
      assertThat(page.locator("svg")).hasClass(Pattern.compile("c1"));
    }

    @PlaywrightTest
    @DisplayName("EX-204 fail")
    @Fixme("java: the assertion error has another format: 'Locator expected to have class\nExpected: foo bar baz\nReceived: bar baz\n\nCall log:\n- Assert \"hasClass\" locator(\"div\") with timeout 1000ms ...' (no 'expect(locator)... failed' header, no 'Locator:' line, unquoted values, double quoted locators)")
    void fail(Page page) {
      page.setContent("<div class=\"bar baz\"></div>");
      Locator locator = page.locator("div");
      AssertionFailedError error = assertThrows(AssertionFailedError.class,
        () -> assertThat(locator).hasClass("foo bar baz", new LocatorAssertions.HasClassOptions().setTimeout(1000)));
      assertTrue(stripAnsi(error.getMessage()).contains("expect(locator).toHaveClass(expected) failed\n" +
        "\n" +
        "Locator:  locator('div')\n" +
        "Expected: \"foo bar baz\"\n" +
        "Received: \"bar baz\"\n" +
        "Timeout:  1000ms\n" +
        "\n" +
        "Call log:\n"));
      assertTrue(stripAnsi(error.getMessage()).contains("- Expect \"toHaveClass\" locator('div') with timeout 1000ms"));
    }

    // EX-205 pass with array: not ported, see upstream_not_ported.txt.

    // EX-206 fail with array: not ported, see upstream_not_ported.txt.
  }

  @Nested
  class ToContainClass {
    // EX-207 pass: not ported, see upstream_not_ported.txt.

    @PlaywrightTest
    @DisplayName("EX-208 pass with SVGs")
    void passWithSVGs(Page page) {
      page.setContent("<svg class=\"c1 c2\" role=\"img\" xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 512 512\"></svg>");
      assertThat(page.locator("svg")).containsClass("c1");
    }

    @PlaywrightTest
    @DisplayName("EX-209 fail")
    @Fixme("java: the assertion error has another format: 'Locator expected to contain class\nExpected: does-not-exist\nReceived: bar baz\n\nCall log:\n- Assert \"containsClass\" locator(\"div\") with timeout 1000ms ...' (no 'expect(locator)... failed' header, no 'Locator:' line, unquoted values, double quoted locators)")
    void fail(Page page) {
      page.setContent("<div class=\"bar baz\"></div>");
      Locator locator = page.locator("div");
      AssertionFailedError error = assertThrows(AssertionFailedError.class,
        () -> assertThat(locator).containsClass("does-not-exist", new LocatorAssertions.ContainsClassOptions().setTimeout(1000)));
      assertTrue(stripAnsi(error.getMessage()).contains("expect(locator).toContainClass(expected) failed\n" +
        "\n" +
        "Locator:  locator('div')\n" +
        "Expected: \"does-not-exist\"\n" +
        "Received: \"bar baz\"\n" +
        "Timeout:  1000ms\n" +
        "\n" +
        "Call log:\n"));
      assertTrue(stripAnsi(error.getMessage()).contains("- Expect \"toContainClass\" locator('div') with timeout 1000ms"));
    }

    // EX-210 pass with array: not ported, see upstream_not_ported.txt.

    @PlaywrightTest
    @DisplayName("EX-211 fail with array")
    @Fixme("java: the assertion error has another format: 'Locator expected to contain classes\nExpected: [foo, bar, baz]\nReceived: [foo, bar, bar]\n\nCall log:\n- Assert \"containsClass\" locator(\"div\") with timeout 1000ms ...' (no 'expect(locator)... failed' header, no 'Locator:' line, unquoted values, double quoted locators)")
    void failWithArray(Page page) {
      page.setContent("<div class=\"foo\"></div><div class=\"bar\"></div><div class=\"bar\"></div>");
      Locator locator = page.locator("div");
      AssertionFailedError error = assertThrows(AssertionFailedError.class,
        () -> assertThat(locator).containsClass(Arrays.asList("foo", "bar", "baz"), new LocatorAssertions.ContainsClassOptions().setTimeout(1000)));
      assertTrue(stripAnsi(error.getMessage()).contains("expect(locator).toContainClass(expected) failed"));
      assertTrue(stripAnsi(error.getMessage()).contains("Timeout: 1000ms"));
      assertTrue(stripAnsi(error.getMessage()).contains("- Expect \"toContainClass\" locator('div') with timeout 1000ms"));
    }
  }

  @Nested
  class ToHaveTitle {
    @PlaywrightTest
    @DisplayName("EX-212 pass")
    void pass(Page page) {
      page.setContent("<title>  Hello     world</title>");
      assertThat(page).hasTitle("Hello  world");
    }

    @PlaywrightTest
    @DisplayName("EX-213 fail")
    @Fixme("java: the assertion error has another format: 'Page title expected to be\nExpected: Hello\nReceived: Bye\n\nCall log:\n- Assert \"hasTitle\" with timeout 1000ms ...' (no 'expect(locator)... failed' header, no 'Locator:' line, unquoted values, double quoted locators)")
    void fail(Page page) {
      page.setContent("<title>Bye</title>");
      AssertionFailedError error = assertThrows(AssertionFailedError.class,
        () -> assertThat(page).hasTitle("Hello", new PageAssertions.HasTitleOptions().setTimeout(1000)));
      assertTrue(stripAnsi(error.getMessage()).contains("expect(page).toHaveTitle(expected) failed\n" +
        "\n" +
        "Expected: \"Hello\"\n" +
        "Received: \"Bye\"\n" +
        "Timeout:  1000ms\n" +
        "\n" +
        "Call log:\n"));
      assertTrue(stripAnsi(error.getMessage()).contains("- Expect \"toHaveTitle\" with timeout 1000ms"));
    }
  }

  @Nested
  class ToHaveURL {
    @PlaywrightTest
    @DisplayName("EX-214 pass")
    void pass(Page page) {
      page.navigate("data:text/html,<div>A</div>");
      assertThat(page).hasURL("data:text/html,<div>A</div>");
    }

    @PlaywrightTest
    @DisplayName("EX-215 fail string")
    @Fixme("java: the assertion error has another format: 'Page URL expected to be\nExpected: wrong\nReceived: data:text/html,<div>A</div>\n\nCall log:\n- Assert \"hasURL\" with timeout 1000ms ...' (no 'expect(locator)... failed' header, no 'Locator:' line, unquoted values, double quoted locators)")
    void failString(Page page) {
      page.navigate("data:text/html,<div>A</div>");
      AssertionFailedError error = assertThrows(AssertionFailedError.class,
        () -> assertThat(page).hasURL("wrong", new PageAssertions.HasURLOptions().setTimeout(1000)));
      assertTrue(stripAnsi(error.getMessage()).contains("expect(page).toHaveURL(expected) failed\n" +
        "\n" +
        "Expected: \"wrong\"\n" +
        "Received: \"data:text/html,<div>A</div>\"\n" +
        "Timeout:  1000ms\n" +
        "\n" +
        "Call log:\n"));
    }

    // EX-216 fail with invalid argument: not ported, see upstream_not_ported.txt.

    // EX-217 fail with positive predicate: not ported, see upstream_not_ported.txt.

    // EX-218 fail with negative predicate: not ported, see upstream_not_ported.txt.

    // EX-219 resolve predicate on initial call: not ported, see upstream_not_ported.txt.

    // EX-220 resolve predicate after retries: not ported, see upstream_not_ported.txt.

    @PlaywrightTest
    @DisplayName("EX-221 support ignoreCase")
    void supportIgnoreCase(Page page) {
      page.navigate("data:text/html,<div>A</div>");
      assertThat(page).hasURL("DATA:teXT/HTml,<div>a</div>", new PageAssertions.HasURLOptions().setIgnoreCase(true));
    }

    // EX-222 support URLPattern: not ported, see upstream_not_ported.txt.
  }

  @Nested
  class ToHaveAttribute {
    @PlaywrightTest
    @DisplayName("EX-223 pass")
    void pass(Page page) {
      page.setContent("<div id=node>Text content</div>");
      Locator locator = page.locator("#node");
      assertThat(locator).hasAttribute("id", "node");
    }

    @PlaywrightTest
    @DisplayName("EX-224 should not match missing attribute")
    @Fixme("java: the assertion error has another format: 'Locator expected to have attribute 'disabled'\nExpected:\nReceived: null\n\nCall log:\n- Assert \"hasAttribute\" locator(\"#node\") with timeout 1000ms ...' (no 'expect(locator)... failed' header, no 'Locator:' line, unquoted values, double quoted locators)")
    void shouldNotMatchMissingAttribute(Page page) {
      page.setContent("<div checked id=node>Text content</div>");
      Locator locator = page.locator("#node");
      {
        AssertionFailedError error = assertThrows(AssertionFailedError.class,
          () -> assertThat(locator).hasAttribute("disabled", "", new LocatorAssertions.HasAttributeOptions().setTimeout(1000)));
        assertTrue(stripAnsi(error.getMessage()).contains("expect(locator).toHaveAttribute(expected) failed\n" +
          "\n" +
          "Locator:  locator('#node')\n" +
          "Expected: \"\"\n" +
          "Received: serializes to the same string\n" +
          "Timeout:  1000ms\n" +
          "\n" +
          "Call log:\n"));
        assertTrue(stripAnsi(error.getMessage()).contains("- Expect \"toHaveAttribute\" locator('#node') with timeout 1000ms"));
      }
      {
        AssertionFailedError error = assertThrows(AssertionFailedError.class,
          () -> assertThat(locator).hasAttribute("disabled", Pattern.compile(".*"), new LocatorAssertions.HasAttributeOptions().setTimeout(1000)));
        assertTrue(stripAnsi(error.getMessage()).contains("expect(locator).toHaveAttribute(expected) failed\n" +
          "\n" +
          "Locator: locator('#node')\n" +
          "Expected pattern: /.*/\n" +
          "Received string:  \"\"\n" +
          "Timeout: 1000ms\n" +
          "\n" +
          "Call log:\n"));
        assertTrue(stripAnsi(error.getMessage()).contains("- Expect \"toHaveAttribute\" locator('#node') with timeout 1000ms"));
      }
      assertThat(locator).not().hasAttribute("disabled", "");
      assertThat(locator).not().hasAttribute("disabled", Pattern.compile(".*"));
    }

    @PlaywrightTest
    @DisplayName("EX-225 should match boolean attribute")
    @Fixme("java: the assertion error has another format: 'Locator expected not to have attribute 'checked'\nExpected:\nReceived:\n\nCall log:\n- Assert \"hasAttribute\" locator(\"#node\") with timeout 1000ms ...' (no 'expect(locator)... failed' header, no 'Locator:' line, unquoted values, double quoted locators)")
    void shouldMatchBooleanAttribute(Page page) {
      page.setContent("<div checked id=node>Text content</div>");
      Locator locator = page.locator("#node");
      assertThat(locator).hasAttribute("checked", "");
      assertThat(locator).hasAttribute("checked", Pattern.compile(".*"));
      {
        AssertionFailedError error = assertThrows(AssertionFailedError.class,
          () -> assertThat(locator).not().hasAttribute("checked", "", new LocatorAssertions.HasAttributeOptions().setTimeout(1000)));
        assertTrue(stripAnsi(error.getMessage()).contains("expect(locator).not.toHaveAttribute(expected) failed\n" +
          "\n" +
          "Locator:  locator('#node')\n" +
          "Expected: not \"\"\n" +
          "Received: \"\"\n" +
          "Timeout:  1000ms\n" +
          "\n" +
          "Call log:\n"));
        assertTrue(stripAnsi(error.getMessage()).contains("- Expect \"not toHaveAttribute\" locator('#node') with timeout 1000ms"));
      }
      {
        AssertionFailedError error = assertThrows(AssertionFailedError.class,
          () -> assertThat(locator).not().hasAttribute("checked", Pattern.compile(".*"), new LocatorAssertions.HasAttributeOptions().setTimeout(1000)));
        assertTrue(stripAnsi(error.getMessage()).contains("expect(locator).not.toHaveAttribute(expected) failed\n" +
          "\n" +
          "Locator: locator('#node')\n" +
          "Expected pattern: not /.*/\n" +
          "Received string: \"\"\n" +
          "Timeout: 1000ms"));
        assertTrue(stripAnsi(error.getMessage()).contains("- Expect \"not toHaveAttribute\" locator('#node') with timeout 1000ms"));
      }
    }

    // EX-226 should match attribute without value: not ported, see upstream_not_ported.txt.

    // EX-227 should support boolean attribute with options: not ported, see upstream_not_ported.txt.

    @PlaywrightTest
    @DisplayName("EX-228 support ignoreCase")
    void supportIgnoreCase(Page page) {
      page.setContent("<div id=NoDe>Text content</div>");
      Locator locator = page.locator("#NoDe");
      assertThat(locator).hasAttribute("id", "node", new LocatorAssertions.HasAttributeOptions().setIgnoreCase(true));
      assertThat(locator).not().hasAttribute("id", "node");
    }
  }

  @Nested
  class ToHaveCSS {
    @PlaywrightTest
    @DisplayName("EX-229 pass")
    void pass(Page page) {
      page.setContent("<div id=node style=\"color: rgb(255, 0, 0)\">Text content</div>");
      Locator locator = page.locator("#node");
      assertThat(locator).hasCSS("color", "rgb(255, 0, 0)");
    }

    @PlaywrightTest
    @DisplayName("EX-230 should support pseudo element")
    @Fixme("java: the pseudo option of hasCSS is not applied: 'Locator expected to have CSS property 'color'\nExpected: rgb(255, 0, 0)\nReceived: rgb(0, 0, 0)'")
    void shouldSupportPseudoElement(Page page) {
      page.setContent("\n" +
        "      <style>\n" +
        "        #node::before {\n" +
        "          color: rgb(255, 0, 0);\n" +
        "          content: \"Text content\";\n" +
        "        }\n" +
        "      </style>\n" +
        "      <div id=node></div>\n" +
        "    ");
      Locator locator = page.locator("#node");
      assertThat(locator).hasCSS("color", "rgb(255, 0, 0)", new LocatorAssertions.HasCSSOptions().setPseudo(PseudoElement.BEFORE));
    }

    @PlaywrightTest
    @DisplayName("EX-231 custom css properties")
    void customCssProperties(Page page) {
      page.setContent("<div id=node style=\"--custom-color-property:#FF00FF;\">Text content</div>");
      Locator locator = page.locator("#node");
      assertThat(locator).hasCSS("--custom-color-property", "#FF00FF");
    }
  }

  @Nested
  class ToHaveId {
    @PlaywrightTest
    @DisplayName("EX-232 pass")
    void pass(Page page) {
      page.setContent("<div id=node>Text content</div>");
      Locator locator = page.locator("#node");
      assertThat(locator).hasId("node");
    }
  }

  @Nested
  class ToBeInViewport {
    @PlaywrightTest
    @DisplayName("EX-233 should work")
    void shouldWork(Page page) {
      page.setContent("\n" +
        "      <div id=big style=\"height: 10000px;\"></div>\n" +
        "      <div id=small>foo</div>\n" +
        "    ");
      assertThat(page.locator("#big")).isInViewport();
      assertThat(page.locator("#small")).not().isInViewport();
      page.locator("#small").scrollIntoViewIfNeeded();
      assertThat(page.locator("#small")).isInViewport();
      assertThat(page.locator("#small")).isInViewport(new LocatorAssertions.IsInViewportOptions().setRatio(1));
    }

    @PlaywrightTest
    @DisplayName("EX-234 should respect ratio option")
    void shouldRespectRatioOption(Page page) {
      page.setContent("\n" +
        "      <style>body, div, html { padding: 0; margin: 0; }</style>\n" +
        "      <div id=big style=\"height: 400vh;\"></div>\n" +
        "    ");
      assertThat(page.locator("div")).isInViewport();
      assertThat(page.locator("div")).isInViewport(new LocatorAssertions.IsInViewportOptions().setRatio(0.1));
      assertThat(page.locator("div")).isInViewport(new LocatorAssertions.IsInViewportOptions().setRatio(0.2));

      assertThat(page.locator("div")).isInViewport(new LocatorAssertions.IsInViewportOptions().setRatio(0.24));
      // In this test, element's ratio is 0.25.
      assertThat(page.locator("div")).isInViewport(new LocatorAssertions.IsInViewportOptions().setRatio(0.25));
      assertThat(page.locator("div")).not().isInViewport(new LocatorAssertions.IsInViewportOptions().setRatio(0.26));

      assertThat(page.locator("div")).not().isInViewport(new LocatorAssertions.IsInViewportOptions().setRatio(0.3));
      assertThat(page.locator("div")).not().isInViewport(new LocatorAssertions.IsInViewportOptions().setRatio(0.7));
      assertThat(page.locator("div")).not().isInViewport(new LocatorAssertions.IsInViewportOptions().setRatio(0.8));
    }

    // EX-235 should have good stack: not ported, see upstream_not_ported.txt.

    @PlaywrightTest
    @DisplayName("EX-236 should report intersection even if fully covered by other element")
    void shouldReportIntersectionEvenIfFullyCoveredByOtherElement(Page page) {
      page.setContent("\n" +
        "      <h1>hello</h1>\n" +
        "      <div style=\"position: relative; height: 10000px; top: -5000px;></div>\n" +
        "    ");
      assertThat(page.locator("h1")).isInViewport();
    }
  }

  @PlaywrightTest
  @DisplayName("EX-237 toHaveCount should not produce logs twice")
  @Fixme("java: the assertion error has another format: 'Locator expected to have count\nExpected: 2\nReceived: 1\n\nCall log:\n- Assert \"hasCount\" locator(\"option\") with timeout 2000ms ...' (no 'expect(locator)... failed' header, no 'Locator:' line, unquoted values, double quoted locators)")
  void toHaveCountShouldNotProduceLogsTwice(Page page) {
    page.setContent("<select><option>One</option></select>");
    AssertionFailedError error = assertThrows(AssertionFailedError.class,
      () -> assertThat(page.locator("option")).hasCount(2, new LocatorAssertions.HasCountOptions().setTimeout(2000)));
    String waitingForMessage = "waiting for locator('option')";
    assertTrue(error.getMessage().contains(waitingForMessage));
    assertTrue(error.getMessage().contains("locator resolved to 1 element"));
    assertTrue(error.getMessage().contains("unexpected value \"1\""));
    assertFalse(error.getMessage().replace(waitingForMessage, "<redacted>").contains(waitingForMessage));
  }

  @PlaywrightTest
  @DisplayName("EX-238 toHaveText should not produce logs twice")
  @Fixme("java: the assertion error has another format: 'Locator expected to have text\nExpected: world\nReceived: hello\n\nCall log:\n- Assert \"hasText\" locator(\"div\") with timeout 2000ms ...' (no 'expect(locator)... failed' header, no 'Locator:' line, unquoted values, double quoted locators)")
  void toHaveTextShouldNotProduceLogsTwice(Page page) {
    page.setContent("<div>hello</div>");
    AssertionFailedError error = assertThrows(AssertionFailedError.class,
      () -> assertThat(page.locator("div")).hasText("world", new LocatorAssertions.HasTextOptions().setTimeout(2000)));
    String waitingForMessage = "waiting for locator('div')";
    assertTrue(error.getMessage().contains(waitingForMessage));
    assertTrue(error.getMessage().contains("locator resolved to <div>hello</div>"));
    assertTrue(error.getMessage().contains("unexpected value \"hello\""));
    assertFalse(error.getMessage().replace(waitingForMessage, "<redacted>").contains(waitingForMessage));
  }

  @PlaywrightTest
  @DisplayName("EX-239 toHaveText that does not match should not produce logs twice")
  @Fixme("java: the assertion error has another format: 'Locator expected to have text\nError: element(s) not found\nExpected: world\nReceived: - text: hello\n\nCall log:\n- Assert \"hasText\" locator(\"span\") with timeout 2000ms ...' (no 'expect(locator)... failed' header, no 'Locator:' line, unquoted values, double quoted locators)")
  void toHaveTextThatDoesNotMatchShouldNotProduceLogsTwice(Page page) {
    page.setContent("<div>hello</div>");
    AssertionFailedError error = assertThrows(AssertionFailedError.class,
      () -> assertThat(page.locator("span")).hasText("world", new LocatorAssertions.HasTextOptions().setTimeout(2000)));
    String waitingForMessage = "waiting for locator('span')";
    assertTrue(error.getMessage().contains(waitingForMessage));
    assertFalse(error.getMessage().contains("locator resolved to"));
    assertFalse(error.getMessage().replace(waitingForMessage, "<redacted>").contains(waitingForMessage));
  }

  @PlaywrightTest
  @DisplayName("EX-240 strict mode violation error format")
  @Fixme("java: the assertion error has another format: 'Locator expected to have text\nError: strict mode violation: locator(\"div\") resolved to 2 elements:\n1) <div>a</div> aka getByText(\"a\")\n2) <div>b</div> aka getByText(\"b\")\nExpected: foo\n\nCall log:\n- Assert \"hasText\" locator(\"div\") with timeout 5000ms ...' (no 'expect(locator)... failed' header, no 'Locator:' line, unquoted values, double quoted locators)")
  void strictModeViolationErrorFormat(Page page) {
    page.setContent("<div>a</div><div>b</div>");
    AssertionFailedError error = assertThrows(AssertionFailedError.class,
      () -> assertThat(page.locator("div")).hasText("foo"));
    assertTrue(stripAnsi(error.getMessage()).contains("expect(locator).toHaveText(expected) failed\n" +
      "\n" +
      "Locator: locator('div')\n" +
      "Expected: \"foo\"\n" +
      "Error: strict mode violation: locator('div') resolved to 2 elements:\n" +
      "    1) <div>a</div> aka getByText('a')\n" +
      "    2) <div>b</div> aka getByText('b')\n" +
      "\n" +
      "Call log:\n"));
  }

  @PlaywrightTest
  @DisplayName("EX-241 invalid selector error format")
  @Fixme("java: the assertion error has another format: 'Locator expected to be visible\nError: Unexpected token \"#\" while parsing css selector \"##\". Did you mean to CSS.escape it?\n\nCall log:\n- Assert \"isVisible\" ## with timeout 5000ms ...' (no 'expect(locator)... failed' header, no 'Locator:' line, unquoted values, double quoted locators)")
  void invalidSelectorErrorFormat(Page page) {
    page.setContent("<div>a</div><div>b</div>");
    AssertionFailedError error = assertThrows(AssertionFailedError.class,
      () -> assertThat(page.locator("##")).isVisible());
    assertTrue(stripAnsi(error.getMessage()).contains("expect(locator).toBeVisible() failed\n" +
      "\n" +
      "Locator: ##\n" +
      "Expected: visible\n" +
      "Error: Unexpected token \"#\" while parsing css selector \"##\". Did you mean to CSS.escape it?\n" +
      "\n" +
      "Call log:\n"));
  }

  @PlaywrightTest
  @DisplayName("EX-242 should report expect error details when page closes during expect")
  @Fixme("java: the assertion error has another format: 'Locator expected to have text\nExpected: world\nReceived: hello\n\nCall log:\n- Assert \"hasText\" locator(\"div\") with timeout 10000ms ...' (no 'expect(locator)... failed' header, no 'Locator:' line, unquoted values, double quoted locators)")
  void shouldReportExpectErrorDetailsWhenPageClosesDuringExpect(Page page) throws Exception {
    page.setContent("<div>hello</div>");
    CompletableFuture<Throwable> promise = async(() -> {
      try {
        assertThat(page.locator("div")).hasText("world", new LocatorAssertions.HasTextOptions().setTimeout(10000));
        return null;
      } catch (Throwable e) {
        return e;
      }
    });
    page.waitForTimeout(1000);
    page.close();
    Throwable error = promise.get();
    assertTrue(stripAnsi(error.getMessage()).contains("expect(locator).toHaveText(expected) failed"));
    assertTrue(stripAnsi(error.getMessage()).contains("Expected: \"world\""));
    assertTrue(stripAnsi(error.getMessage()).contains("Received: \"hello\""));
  }

  @PlaywrightTest
  @DisplayName("EX-243 should report page close reason when page closes during expect")
  @Fixme("java: the assertion error has another format: 'Locator expected to have text\nError: Bye bye.\nExpected: world\nReceived: null\n\nCall log:\n- Assert \"hasText\" locator(\"non-existent\") with timeout 10000ms ...' (no 'expect(locator)... failed' header, no 'Locator:' line, unquoted values, double quoted locators)")
  void shouldReportPageCloseReasonWhenPageClosesDuringExpect(Page page) throws Exception {
    page.setContent("<div>hello</div>");
    CompletableFuture<Throwable> promise = async(() -> {
      try {
        assertThat(page.locator("non-existent")).hasText("world", new LocatorAssertions.HasTextOptions().setTimeout(10000));
        return null;
      } catch (Throwable e) {
        return e;
      }
    });
    page.close(new Page.CloseOptions().setReason("Bye bye."));
    Throwable error = promise.get();
    assertTrue(stripAnsi(error.getMessage()).contains("expect(locator).toHaveText(expected) failed"));
    assertTrue(stripAnsi(error.getMessage()).contains("Bye bye."));
    assertFalse(stripAnsi(error.getMessage()).contains("Internal server error"));
  }
}
