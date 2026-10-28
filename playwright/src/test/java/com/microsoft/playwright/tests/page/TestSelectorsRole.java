/**
 * Copyright 2018 Google Inc. All rights reserved.
 * Modifications copyright (c) Microsoft Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.playwright.tests.page;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.function.Executable;

import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/selectors-role.spec.ts. */
public class TestSelectorsRole {

  @PlaywrightTest
  @DisplayName("SE-050 should detect roles")
  void shouldDetectRoles(Page page) {
    page.setContent("\n" +
      "    <button>Hello</button>\n" +
      "    <select multiple=\"\" size=\"2\"></select>\n" +
      "    <select></select>\n" +
      "    <h3>Heading</h3>\n" +
      "    <details><summary>Hello</summary></details>\n" +
      "    <div role=\"dialog\">I am a dialog</div>\n" +
      "  ");
    assertEquals(Arrays.asList("<button>Hello</button>"), page.locator("role=button").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(Arrays.asList("<select multiple=\"\" size=\"2\"></select>"), page.locator("role=listbox").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(Arrays.asList("<select></select>"), page.locator("role=combobox").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(Arrays.asList("<h3>Heading</h3>"), page.locator("role=heading").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(Arrays.asList("<details><summary>Hello</summary></details>"), page.locator("role=group").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(Arrays.asList("<div role=\"dialog\">I am a dialog</div>"), page.locator("role=dialog").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(Arrays.asList(), page.locator("role=menuitem").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(Arrays.asList(), page.getByRole(AriaRole.MENUITEM).evaluateAll("els => els.map(e => e.outerHTML)"));
  }

  @PlaywrightTest
  @DisplayName("SE-051 should support selected")
  void shouldSupportSelected(Page page) {
    page.setContent("\n" +
      "    <select>\n" +
      "      <option>Hi</option>\n" +
      "      <option selected>Hello</option>\n" +
      "    </select>\n" +
      "    <div>\n" +
      "      <div role=\"option\" aria-selected=\"true\">Hi</div>\n" +
      "      <div role=\"option\" aria-selected=\"false\">Hello</div>\n" +
      "    </div>\n" +
      "  ");
    assertEquals(Arrays.asList("<option selected=\"\">Hello</option>", "<div role=\"option\" aria-selected=\"true\">Hi</div>"), page.locator("role=option[selected]").evaluateAll("els => els.map(e => e.outerHTML)"));

    assertEquals(Arrays.asList("<option selected=\"\">Hello</option>", "<div role=\"option\" aria-selected=\"true\">Hi</div>"), page.locator("role=option[selected=true]").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(Arrays.asList("<option selected=\"\">Hello</option>", "<div role=\"option\" aria-selected=\"true\">Hi</div>"), page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setSelected(true)).evaluateAll("els => els.map(e => e.outerHTML)"));

    assertEquals(Arrays.asList("<option>Hi</option>", "<div role=\"option\" aria-selected=\"false\">Hello</div>"), page.locator("role=option[selected=false]").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(Arrays.asList("<option>Hi</option>", "<div role=\"option\" aria-selected=\"false\">Hello</div>"), page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setSelected(false)).evaluateAll("els => els.map(e => e.outerHTML)"));
  }

  @PlaywrightTest
  @DisplayName("SE-052 should support checked")
  void shouldSupportChecked(Page page) {
    page.setContent("\n" +
      "    <input type=checkbox>\n" +
      "    <input type=checkbox checked>\n" +
      "    <input type=checkbox indeterminate>\n" +
      "    <div role=checkbox aria-checked=\"true\">Hi</div>\n" +
      "    <div role=checkbox aria-checked=\"false\">Hello</div>\n" +
      "    <div role=checkbox>Unknown</div>\n" +
      "  ");
    page.evalOnSelector("[indeterminate]", "input => input.indeterminate = true");

    assertEquals(Arrays.asList("<input type=\"checkbox\" checked=\"\">", "<div role=\"checkbox\" aria-checked=\"true\">Hi</div>"), page.locator("role=checkbox[checked]").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(Arrays.asList("<input type=\"checkbox\" checked=\"\">", "<div role=\"checkbox\" aria-checked=\"true\">Hi</div>"), page.locator("role=checkbox[checked=true]").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(Arrays.asList("<input type=\"checkbox\" checked=\"\">", "<div role=\"checkbox\" aria-checked=\"true\">Hi</div>"), page.getByRole(AriaRole.CHECKBOX, new Page.GetByRoleOptions().setChecked(true)).evaluateAll("els => els.map(e => e.outerHTML)"));

    assertEquals(Arrays.asList("<input type=\"checkbox\">", "<div role=\"checkbox\" aria-checked=\"false\">Hello</div>", "<div role=\"checkbox\">Unknown</div>"), page.locator("role=checkbox[checked=false]").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(Arrays.asList("<input type=\"checkbox\">", "<div role=\"checkbox\" aria-checked=\"false\">Hello</div>", "<div role=\"checkbox\">Unknown</div>"), page.getByRole(AriaRole.CHECKBOX, new Page.GetByRoleOptions().setChecked(false)).evaluateAll("els => els.map(e => e.outerHTML)"));

    assertEquals(Arrays.asList("<input type=\"checkbox\" indeterminate=\"\">"), page.locator("role=checkbox[checked=\"mixed\"]").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(Arrays.asList("<input type=\"checkbox\">", "<input type=\"checkbox\" checked=\"\">", "<input type=\"checkbox\" indeterminate=\"\">", "<div role=\"checkbox\" aria-checked=\"true\">Hi</div>", "<div role=\"checkbox\" aria-checked=\"false\">Hello</div>", "<div role=\"checkbox\">Unknown</div>"), page.locator("role=checkbox").evaluateAll("els => els.map(e => e.outerHTML)"));
  }

  @PlaywrightTest
  @DisplayName("SE-053 should support pressed")
  void shouldSupportPressed(Page page) {
    page.setContent("\n" +
      "    <button>Hi</button>\n" +
      "    <button aria-pressed=\"true\">Hello</button>\n" +
      "    <button aria-pressed=\"false\">Bye</button>\n" +
      "    <button aria-pressed=\"mixed\">Mixed</button>\n" +
      "  ");
    assertEquals(Arrays.asList("<button aria-pressed=\"true\">Hello</button>"), page.locator("role=button[pressed]").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(Arrays.asList("<button aria-pressed=\"true\">Hello</button>"), page.locator("role=button[pressed=true]").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(Arrays.asList("<button aria-pressed=\"true\">Hello</button>"), page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setPressed(true)).evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(Arrays.asList("<button>Hi</button>", "<button aria-pressed=\"false\">Bye</button>"), page.locator("role=button[pressed=false]").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(Arrays.asList("<button>Hi</button>", "<button aria-pressed=\"false\">Bye</button>"), page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setPressed(false)).evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(Arrays.asList("<button aria-pressed=\"mixed\">Mixed</button>"), page.locator("role=button[pressed=\"mixed\"]").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(Arrays.asList("<button>Hi</button>", "<button aria-pressed=\"true\">Hello</button>", "<button aria-pressed=\"false\">Bye</button>", "<button aria-pressed=\"mixed\">Mixed</button>"), page.locator("role=button").evaluateAll("els => els.map(e => e.outerHTML)"));
  }

  @PlaywrightTest
  @DisplayName("SE-054 should support expanded")
  void shouldSupportExpanded(Page page) {
    page.setContent("\n" +
      "    <div role=\"treeitem\">Hi</div>\n" +
      "    <div role=\"treeitem\" aria-expanded=\"true\">Hello</div>\n" +
      "    <div role=\"treeitem\" aria-expanded=\"false\">Bye</div>\n" +
      "  ");

    assertEquals(Arrays.asList("<div role=\"treeitem\">Hi</div>", "<div role=\"treeitem\" aria-expanded=\"true\">Hello</div>", "<div role=\"treeitem\" aria-expanded=\"false\">Bye</div>"), page.locator("role=treeitem").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(Arrays.asList("<div role=\"treeitem\">Hi</div>", "<div role=\"treeitem\" aria-expanded=\"true\">Hello</div>", "<div role=\"treeitem\" aria-expanded=\"false\">Bye</div>"), page.getByRole(AriaRole.TREEITEM).evaluateAll("els => els.map(e => e.outerHTML)"));

    assertEquals(Arrays.asList("<div role=\"treeitem\" aria-expanded=\"true\">Hello</div>"), page.locator("role=treeitem[expanded]").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(Arrays.asList("<div role=\"treeitem\" aria-expanded=\"true\">Hello</div>"), page.locator("role=treeitem[expanded=true]").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(Arrays.asList("<div role=\"treeitem\" aria-expanded=\"true\">Hello</div>"), page.getByRole(AriaRole.TREEITEM, new Page.GetByRoleOptions().setExpanded(true)).evaluateAll("els => els.map(e => e.outerHTML)"));

    assertEquals(Arrays.asList("<div role=\"treeitem\" aria-expanded=\"false\">Bye</div>"), page.locator("role=treeitem[expanded=false]").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(Arrays.asList("<div role=\"treeitem\" aria-expanded=\"false\">Bye</div>"), page.getByRole(AriaRole.TREEITEM, new Page.GetByRoleOptions().setExpanded(false)).evaluateAll("els => els.map(e => e.outerHTML)"));

    // Workaround for expanded="none".
    assertEquals(Arrays.asList("<div role=\"treeitem\">Hi</div>"), page.locator("[role=treeitem]:not([aria-expanded])").evaluateAll("els => els.map(e => e.outerHTML)"));
  }

  @PlaywrightTest
  @DisplayName("SE-055 should support disabled")
  void shouldSupportDisabled(Page page) {
    page.setContent("\n" +
      "    <button>Hi</button>\n" +
      "    <button disabled>Bye</button>\n" +
      "    <button aria-disabled=\"true\">Hello</button>\n" +
      "    <button aria-disabled=\"false\">Oh</button>\n" +
      "    <fieldset disabled>\n" +
      "      <button>Yay</button>\n" +
      "    </fieldset>\n" +
      "    <select>\n" +
      "      <optgroup disabled>\n" +
      "        <option>one</option>\n" +
      "      </optgroup>\n" +
      "      <optgroup>\n" +
      "        <option>two</option>\n" +
      "      </optgroup>\n" +
      "      <option disabled>three</option>\n" +
      "    </select>\n" +
      "  ");
    assertEquals(Arrays.asList("<button disabled=\"\">Bye</button>", "<button aria-disabled=\"true\">Hello</button>", "<button>Yay</button>"), page.locator("role=button[disabled]").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(Arrays.asList("<button disabled=\"\">Bye</button>", "<button aria-disabled=\"true\">Hello</button>", "<button>Yay</button>"), page.locator("role=button[disabled=true]").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(Arrays.asList("<button disabled=\"\">Bye</button>", "<button aria-disabled=\"true\">Hello</button>", "<button>Yay</button>"), page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setDisabled(true)).evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(Arrays.asList("<button>Hi</button>", "<button aria-disabled=\"false\">Oh</button>"), page.locator("role=button[disabled=false]").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(Arrays.asList("<button>Hi</button>", "<button aria-disabled=\"false\">Oh</button>"), page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setDisabled(false)).evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(Arrays.asList("<option>one</option>", "<option disabled=\"\">three</option>"), page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setDisabled(true)).evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(Arrays.asList("<option>two</option>"), page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setDisabled(false)).evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(Arrays.asList("<option>one</option>", "<option>two</option>", "<option disabled=\"\">three</option>"), page.getByRole(AriaRole.OPTION).evaluateAll("els => els.map(e => e.outerHTML)"));
  }

  @PlaywrightTest
  @DisplayName("SE-056 should inherit disabled from the ancestor")
  void shouldInheritDisabledFromTheAncestor(Page page) {
    page.setContent("\n" +
      "    <span aria-disabled=\"true\">\n" +
      "      <button>Click me!</button>\n" +
      "    </span>\n" +
      "  ");
    assertThat(page.locator("button")).isDisabled();

    page.setContent("\n" +
      "    <span aria-disabled=\"true\">\n" +
      "      <h1>Heading</h1>\n" +
      "    </span>\n" +
      "  ");
    assertThat(page.locator("h1")).not().isDisabled();
  }

  @PlaywrightTest
  @DisplayName("SE-057 should support disabled fieldset")
  void shouldSupportDisabledFieldset(Page page) {
    page.setContent("\n" +
      "    <fieldset disabled>\n" +
      "      <input></input>\n" +
      "      <button data-testid=\"inside-fieldset-element\">x</button>\n" +
      "      <legend>\n" +
      "        <button data-testid=\"inside-legend-element\">legend</button>\n" +
      "      </legend>\n" +
      "    </fieldset>\n" +
      "\n" +
      "    <fieldset disabled>\n" +
      "      <legend>\n" +
      "        <div>\n" +
      "          <button data-testid=\"nested-inside-legend-element\">x</button>\n" +
      "        </div>\n" +
      "      </legend>\n" +
      "    </fieldset>\n" +
      "\n" +
      "    <fieldset disabled>\n" +
      "      <div></div>\n" +
      "      <legend>\n" +
      "        <button data-testid=\"first-legend-element\">x</button>\n" +
      "      </legend>\n" +
      "      <legend>\n" +
      "        <button data-testid=\"second-legend-element\">x</button>\n" +
      "      </legend>\n" +
      "    </fieldset>\n" +
      "\n" +
      "    <fieldset disabled>\n" +
      "      <fieldset>\n" +
      "        <button data-testid=\"deep-button\">x</button>\n" +
      "      </fieldset>\n" +
      "    </fieldset>\n" +
      "  ");

    // expect.soft
    assertAll(
      () -> assertThat(page.getByTestId("inside-legend-element")).isEnabled(),
      () -> assertThat(page.getByTestId("nested-inside-legend-element")).isEnabled(),
      () -> assertThat(page.getByTestId("first-legend-element")).isEnabled(),
      () -> assertThat(page.getByTestId("second-legend-element")).isDisabled(),
      () -> assertThat(page.getByTestId("deep-button")).isDisabled());
  }

  @PlaywrightTest
  @DisplayName("SE-058 should support level")
  void shouldSupportLevel(Page page) {
    page.setContent("\n" +
      "    <h1>Hello</h1>\n" +
      "    <h3>Hi</h3>\n" +
      "    <div role=\"heading\" aria-level=\"5\">Bye</div>\n" +
      "  ");
    assertEquals(Arrays.asList("<h1>Hello</h1>"), page.locator("role=heading[level=1]").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(Arrays.asList("<h1>Hello</h1>"), page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setLevel(1)).evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(Arrays.asList("<h3>Hi</h3>"), page.locator("role=heading[level=3]").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(Arrays.asList("<h3>Hi</h3>"), page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setLevel(3)).evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(Arrays.asList("<div role=\"heading\" aria-level=\"5\">Bye</div>"), page.locator("role=heading[level=5]").evaluateAll("els => els.map(e => e.outerHTML)"));
  }

  @PlaywrightTest
  @DisplayName("SE-059 should filter hidden, unless explicitly asked for")
  void shouldFilterHiddenUnlessExplicitlyAskedFor(Page page) {
    page.setContent("\n" +
      "    <button>Hi</button>\n" +
      "    <button hidden>Hello</button>\n" +
      "    <button aria-hidden=\"true\">Yay</button>\n" +
      "    <button aria-hidden=\"false\">Nay</button>\n" +
      "    <button style=\"visibility:hidden\">Bye</button>\n" +
      "    <div style=\"visibility:hidden\">\n" +
      "      <button>Oh</button>\n" +
      "    </div>\n" +
      "    <div style=\"visibility:hidden\">\n" +
      "      <button style=\"visibility:visible\">Still here</button>\n" +
      "    </div>\n" +
      "    <button style=\"display:none\">Never</button>\n" +
      "    <div id=host1></div>\n" +
      "    <div id=host2 style=\"display:none\"></div>\n" +
      "\n" +
      "    <input name=\"one\">\n" +
      "    <details>\n" +
      "      <summary>Open form</summary>\n" +
      "      <label>\n" +
      "         Label\n" +
      "         <input name=\"two\">\n" +
      "      </label>\n" +
      "    </details>\n" +
      "\n" +
      "    <select>\n" +
      "      <option style=\"visibility:hidden\">One</option>\n" +
      "      <option style=\"display:none\">Two</option>\n" +
      "      <option>Three</option>\n" +
      "    </select>\n" +
      "\n" +
      "    <script>\n" +
      "      function addButton(host, text) {\n" +
      "        const root = host.attachShadow({ mode: 'open' });\n" +
      "        const button = document.createElement('button');\n" +
      "        button.textContent = text;\n" +
      "        root.appendChild(button);\n" +
      "      }\n" +
      "      addButton(document.getElementById('host1'), 'Shadow1');\n" +
      "      addButton(document.getElementById('host2'), 'Shadow2');\n" +
      "    </script>\n" +
      "  ");
    assertEquals(Arrays.asList("<button>Hi</button>", "<button aria-hidden=\"false\">Nay</button>", "<button style=\"visibility:visible\">Still here</button>", "<button>Shadow1</button>"), page.locator("role=button").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(Arrays.asList("<button>Hi</button>", "<button hidden=\"\">Hello</button>", "<button aria-hidden=\"true\">Yay</button>", "<button aria-hidden=\"false\">Nay</button>", "<button style=\"visibility:hidden\">Bye</button>", "<button>Oh</button>", "<button style=\"visibility:visible\">Still here</button>", "<button style=\"display:none\">Never</button>", "<button>Shadow1</button>", "<button>Shadow2</button>"), page.locator("role=button[include-hidden]").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(Arrays.asList("<button>Hi</button>", "<button hidden=\"\">Hello</button>", "<button aria-hidden=\"true\">Yay</button>", "<button aria-hidden=\"false\">Nay</button>", "<button style=\"visibility:hidden\">Bye</button>", "<button>Oh</button>", "<button style=\"visibility:visible\">Still here</button>", "<button style=\"display:none\">Never</button>", "<button>Shadow1</button>", "<button>Shadow2</button>"), page.locator("role=button[include-hidden=true]").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(Arrays.asList("<button>Hi</button>", "<button aria-hidden=\"false\">Nay</button>", "<button style=\"visibility:visible\">Still here</button>", "<button>Shadow1</button>"), page.locator("role=button[include-hidden=false]").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(Arrays.asList("<input name=\"one\">"), page.locator("role=textbox").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(Arrays.asList("<option style=\"visibility:hidden\">One</option>", "<option>Three</option>"), page.locator("role=option").evaluateAll("els => els.map(e => e.outerHTML)"));
  }

  @PlaywrightTest
  @DisplayName("SE-060 should support name")
  void shouldSupportName(Page page) {
    page.setContent("\n" +
      "    <div role=\"button\" aria-label=\" Hello \"></div>\n" +
      "    <div role=\"button\" aria-label=\"Hallo\"></div>\n" +
      "    <div role=\"button\" aria-label=\"Hello\" aria-hidden=\"true\"></div>\n" +
      "    <div role=\"button\" aria-label=\"123\" aria-hidden=\"true\"></div>\n" +
      "    <div role=\"button\" aria-label='foo\"bar' aria-hidden=\"true\"></div>\n" +
      "  ");
    assertEquals(Arrays.asList("<div role=\"button\" aria-label=\" Hello \"></div>"), page.locator("role=button[name=\"Hello\"]").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(Arrays.asList("<div role=\"button\" aria-label=\" Hello \"></div>"), page.locator("role=button[name=\" \n Hello \"]").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(Arrays.asList("<div role=\"button\" aria-label=\" Hello \"></div>"), page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Hello")).evaluateAll("els => els.map(e => e.outerHTML)"));

    assertEquals(Arrays.asList("<div role=\"button\" aria-label=\"Hallo\"></div>"), page.locator("role=button[name*=\"all\"]").evaluateAll("els => els.map(e => e.outerHTML)"));

    assertEquals(Arrays.asList("<div role=\"button\" aria-label=\" Hello \"></div>", "<div role=\"button\" aria-label=\"Hallo\"></div>"), page.locator("role=button[name=/^H[ae]llo$/]").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(Arrays.asList("<div role=\"button\" aria-label=\" Hello \"></div>", "<div role=\"button\" aria-label=\"Hallo\"></div>"), page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("^H[ae]llo$"))).evaluateAll("els => els.map(e => e.outerHTML)"));

    assertEquals(Arrays.asList("<div role=\"button\" aria-label=\" Hello \"></div>", "<div role=\"button\" aria-label=\"Hallo\"></div>"), page.locator("role=button[name=/h.*o/i]").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(Arrays.asList("<div role=\"button\" aria-label=\" Hello \"></div>", "<div role=\"button\" aria-label=\"Hallo\"></div>"), page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("h.*o", Pattern.CASE_INSENSITIVE))).evaluateAll("els => els.map(e => e.outerHTML)"));

    assertEquals(Arrays.asList("<div role=\"button\" aria-label=\" Hello \"></div>", "<div role=\"button\" aria-label=\"Hello\" aria-hidden=\"true\"></div>"), page.locator("role=button[name=\"Hello\"][include-hidden]").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(Arrays.asList("<div role=\"button\" aria-label=\" Hello \"></div>", "<div role=\"button\" aria-label=\"Hello\" aria-hidden=\"true\"></div>"), page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Hello").setIncludeHidden(true)).evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(Arrays.asList("<div role=\"button\" aria-label=\" Hello \"></div>", "<div role=\"button\" aria-label=\"Hello\" aria-hidden=\"true\"></div>"), page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("hello").setIncludeHidden(true)).evaluateAll("els => els.map(e => e.outerHTML)"));

    assertEquals(Arrays.asList("<div role=\"button\" aria-label=\" Hello \"></div>"), page.locator("role=button[name=Hello]").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(Arrays.asList("<div role=\"button\" aria-label=\"123\" aria-hidden=\"true\"></div>"), page.locator("role=button[name=123][include-hidden]").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(Arrays.asList("<div role=\"button\" aria-label=\"123\" aria-hidden=\"true\"></div>"), page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("123").setIncludeHidden(true)).evaluateAll("els => els.map(e => e.outerHTML)"));
  }

  @PlaywrightTest
  @DisplayName("SE-061 should support option name with html whitespace")
  void shouldSupportOptionNameWithHtmlWhitespace(Page page) {
    page.setContent("\n" +
      "    <select>\n" +
      "      <option value=\"html\">&nbsp;HTML</option>\n" +
      "    </select>\n" +
      "  ");
    assertThat(page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName("HTML"))).hasCount(1);
  }

  @PlaywrightTest
  @DisplayName("SE-062 errors")
  void errors(Page page) {
    PlaywrightException e0 = assertThrows(PlaywrightException.class, () -> page.querySelector("role=[bar]"));
    assertTrue(e0.getMessage().contains("Role must not be empty"));

    PlaywrightException e1 = assertThrows(PlaywrightException.class, () -> page.querySelector("role=foo[sElected]"));
    assertTrue(e1.getMessage().contains("Unknown attribute \"sElected\", must be one of \"checked\", \"description\", \"disabled\", \"expanded\", \"include-hidden\", \"level\", \"name\", \"pressed\", \"selected\""));

    PlaywrightException e2 = assertThrows(PlaywrightException.class, () -> page.querySelector("role=foo[bar . qux=true]"));
    assertTrue(e2.getMessage().contains("Unknown attribute \"bar.qux\""));

    PlaywrightException e3 = assertThrows(PlaywrightException.class, () -> page.querySelector("role=heading[level=\"bar\"]"));
    assertTrue(e3.getMessage().contains("\"level\" attribute must be compared to a number"));

    PlaywrightException e4 = assertThrows(PlaywrightException.class, () -> page.querySelector("role=checkbox[checked=\"bar\"]"));
    assertTrue(e4.getMessage().contains("\"checked\" must be one of true, false, \"mixed\""));

    PlaywrightException e5 = assertThrows(PlaywrightException.class, () -> page.querySelector("role=checkbox[checked~=true]"));
    assertTrue(e5.getMessage().contains("cannot use ~= in attribute with non-string matching value"));

    PlaywrightException e6 = assertThrows(PlaywrightException.class, () -> page.querySelector("role=button[level=3]"));
    assertTrue(e6.getMessage().contains("\"level\" attribute is only supported for roles: \"heading\", \"listitem\", \"row\", \"treeitem\""));

    PlaywrightException e7 = assertThrows(PlaywrightException.class, () -> page.querySelector("role=button[name]"));
    assertTrue(e7.getMessage().contains("\"name\" attribute must have a value"));

    PlaywrightException e8 = assertThrows(PlaywrightException.class, () -> page.querySelector("role=treeitem[expanded=\"none\"]"));
    assertTrue(e8.getMessage().contains("\"expanded\" must be one of true, false"));
  }

  @PlaywrightTest
  @DisplayName("SE-063 hidden with shadow dom slots")
  void hiddenWithShadowDomSlots(Page page) {
    page.setContent("\n" +
      "    <div make-hidden>\n" +
      "      <button>hidden1</button>\n" +
      "    </div>\n" +
      "    <div make-hidden>\n" +
      "      <span><button>hidden2</button></v>\n" +
      "    </div>\n" +
      "    <div>\n" +
      "      <button>visible1</button>\n" +
      "    </div>\n" +
      "    <div>\n" +
      "      <span><button>visible2</button></span>\n" +
      "    </div>\n" +
      "    <script>\n" +
      "      for (const div of document.querySelectorAll('div')) {\n" +
      "        const hidden = div.hasAttribute('make-hidden');\n" +
      "        div.attachShadow({ mode: 'open' }).innerHTML = hidden ? 'nothing to see here' : '<slot></slot>';\n" +
      "      }\n" +
      "    </script>\n" +
      "  ");
    assertEquals(Arrays.asList("<button>visible1</button>", "<button>visible2</button>"), page.locator("role=button").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(Arrays.asList("<button>hidden1</button>", "<button>hidden2</button>", "<button>visible1</button>", "<button>visible2</button>"), page.locator("role=button[include-hidden]").evaluateAll("els => els.map(e => e.outerHTML)"));
  }

  @PlaywrightTest
  @DisplayName("SE-064 should support output accessible name")
  void shouldSupportOutputAccessibleName(Page page) {
    page.setContent("<label>Output1<output>output</output></label>");
    assertThat(page.getByRole(AriaRole.STATUS, new Page.GetByRoleOptions().setName("Output1"))).isVisible();
  }

  @PlaywrightTest
  @DisplayName("SE-065 should not match scope by default")
  void shouldNotMatchScopeByDefault(Page page) {
    page.setContent("\n" +
      "    <ul>\n" +
      "      <li aria-label=\"Parent list\">\n" +
      "        Parent list\n" +
      "        <ul>\n" +
      "          <li>child 1</li>\n" +
      "          <li>child 2</li>\n" +
      "        </ul>\n" +
      "      </li>\n" +
      "    </ul>\n" +
      "  ");
    Locator children = page.getByRole(AriaRole.LISTITEM, new Page.GetByRoleOptions().setName("Parent list")).getByRole(AriaRole.LISTITEM);
    assertThat(children).hasCount(2);
    assertThat(children).hasText(new String[] {"child 1", "child 2"});
  }

  @PlaywrightTest
  @DisplayName("SE-066 should not match inert elements, unless explicitly asked for")
  @Fixme("driver: the driver 1.64.1 does not skip inert elements in role selectors: getByRole(BUTTON) returns <button>Hi</button>, <button inert=\"\">Hello</button>, ... instead of the three visible buttons")
  void shouldNotMatchInertElementsUnlessExplicitlyAskedFor(Page page) {
    page.setContent("\n" +
      "    <button>Hi</button>\n" +
      "    <button inert>Hello</button>\n" +
      "    <div inert id=container>\n" +
      "      <button>Yay</button>\n" +
      "      <div><button>Nay</button></div>\n" +
      "    </div>\n" +
      "    <button><span inert>Save</span></button>\n" +
      "    <div id=host></div>\n" +
      "    <div inert id=inert-host></div>\n" +
      "    <script>\n" +
      "      function addButton(host, text) {\n" +
      "        const root = host.attachShadow({ mode: 'open' });\n" +
      "        const button = document.createElement('button');\n" +
      "        button.textContent = text;\n" +
      "        root.appendChild(button);\n" +
      "      }\n" +
      "      addButton(document.getElementById('host'), 'Shadow1');\n" +
      "      addButton(document.getElementById('inert-host'), 'Shadow2');\n" +
      "    </script>\n" +
      "  ");
    assertEquals(Arrays.asList("<button>Hi</button>", "<button><span inert=\"\">Save</span></button>", "<button>Shadow1</button>"), page.getByRole(AriaRole.BUTTON).evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(Arrays.asList("<button>Hi</button>", "<button inert=\"\">Hello</button>", "<button>Yay</button>", "<button>Nay</button>", "<button><span inert=\"\">Save</span></button>", "<button>Shadow1</button>", "<button>Shadow2</button>"), page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setIncludeHidden(true)).evaluateAll("els => els.map(e => e.outerHTML)"));
    assertThat(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Yay"))).hasCount(0);
    assertThat(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Yay").setIncludeHidden(true))).hasCount(1);

    // Inert content does not contribute to the accessible name.
    assertThat(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save"))).hasCount(0);
    assertThat(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save").setIncludeHidden(true))).hasCount(1);

    page.locator("#container").evaluate("e => e.removeAttribute('inert')");
    assertEquals(Arrays.asList("<button>Hi</button>", "<button>Yay</button>", "<button>Nay</button>", "<button><span inert=\"\">Save</span></button>", "<button>Shadow1</button>"), page.getByRole(AriaRole.BUTTON).evaluateAll("els => els.map(e => e.outerHTML)"));
  }

  @PlaywrightTest
  @DisplayName("SE-067 should not match elements outside of a modal dialog")
  @Fixme("driver: the driver 1.64.1 does not skip elements outside of a modal dialog in role selectors: expected <[<button>Inside</button>]> but was <[<button>Outside</button>, <button>Inside</button>, <button>Inert inside</button>, <button>Non-modal</button>]>")
  void shouldNotMatchElementsOutsideOfAModalDialog(Page page) {
    page.setContent("\n" +
      "    <button>Outside</button>\n" +
      "    <dialog id=dialog>\n" +
      "      <button>Inside</button>\n" +
      "      <div inert><button>Inert inside</button></div>\n" +
      "    </dialog>\n" +
      "    <dialog id=non-modal><button>Non-modal</button></dialog>\n" +
      "    <script>\n" +
      "      document.getElementById('dialog').showModal();\n" +
      "      document.getElementById('non-modal').show();\n" +
      "    </script>\n" +
      "  ");
    assertEquals(Arrays.asList("<button>Inside</button>"), page.getByRole(AriaRole.BUTTON).evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(Arrays.asList("<button>Outside</button>", "<button>Inside</button>", "<button>Inert inside</button>", "<button>Non-modal</button>"), page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setIncludeHidden(true)).evaluateAll("els => els.map(e => e.outerHTML)"));
    assertThat(page.getByRole(AriaRole.DIALOG)).hasCount(1);
    assertThat(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Outside"))).hasCount(0);

    page.locator("#dialog").evaluate("e => e.close()");
    assertEquals(Arrays.asList("<button>Outside</button>", "<button>Non-modal</button>"), page.getByRole(AriaRole.BUTTON).evaluateAll("els => els.map(e => e.outerHTML)"));
  }

  @PlaywrightTest
  @DisplayName("SE-068 should match the topmost modal dialog")
  @Fixme("driver: the driver 1.64.1 does not skip elements outside of the topmost modal dialog in role selectors: expected <[<button>Inner</button>]> but was <[<button>Outside</button>, <button>Outer</button>, <button>Inner</button>]>")
  void shouldMatchTheTopmostModalDialog(Page page) {
    page.setContent("\n" +
      "    <button>Outside</button>\n" +
      "    <dialog id=outer>\n" +
      "      <button>Outer</button>\n" +
      "      <dialog id=inner><button>Inner</button></dialog>\n" +
      "    </dialog>\n" +
      "    <div inert>\n" +
      "      <dialog id=escaping><button>Escaping</button></dialog>\n" +
      "    </div>\n" +
      "    <script>\n" +
      "      document.getElementById('outer').showModal();\n" +
      "      document.getElementById('inner').showModal();\n" +
      "    </script>\n" +
      "  ");
    assertEquals(Arrays.asList("<button>Inner</button>"), page.getByRole(AriaRole.BUTTON).evaluateAll("els => els.map(e => e.outerHTML)"));

    page.locator("#inner").evaluate("e => e.close()");
    assertEquals(Arrays.asList("<button>Outer</button>"), page.getByRole(AriaRole.BUTTON).evaluateAll("els => els.map(e => e.outerHTML)"));

    // Modal dialog inside an inert subtree is not inert.
    page.locator("#outer").evaluate("e => e.close()");
    page.locator("#escaping").evaluate("e => e.showModal()");
    assertEquals(Arrays.asList("<button>Escaping</button>"), page.getByRole(AriaRole.BUTTON).evaluateAll("els => els.map(e => e.outerHTML)"));
  }

  @PlaywrightTest
  @DisplayName("SE-069 should respect PLAYWRIGHT_SKIP_INERT_CHECK")
  void shouldRespectPLAYWRIGHTSKIPINERTCHECK(TestServer server, BrowserName browserName) {
    // Java: process.env of the in-process driver is the env of the driver process that a new Playwright starts.
    try (Playwright playwright = Playwright.create(new Playwright.CreateOptions().setEnv(Collections.singletonMap("PLAYWRIGHT_SKIP_INERT_CHECK", "1")));
         Browser browser = browserName.browserType(playwright).launch(Env.launchOptions())) {
      Page page = browser.newPage();
      // Navigation creates a fresh injected script that reads the env variable.
      page.navigate(server.EMPTY_PAGE);
      page.setContent("\n" +
        "      <button>Outside</button>\n" +
        "      <div inert><button>Inert</button></div>\n" +
        "      <dialog id=dialog><button>Inside</button></dialog>\n" +
        "      <script>\n" +
        "        document.getElementById('dialog').showModal();\n" +
        "      </script>\n" +
        "    ");
      assertEquals(Arrays.asList("<button>Outside</button>", "<button>Inert</button>", "<button>Inside</button>"), page.getByRole(AriaRole.BUTTON).evaluateAll("els => els.map(e => e.outerHTML)"));
    }
  }

  @PlaywrightTest
  @DisplayName("SE-070 should respect PLAYWRIGHT_SKIP_DIALOG_CHECK")
  @Fixme("driver: the driver 1.64.1 does not skip inert elements in role selectors, so <button>Inert</button> is returned: expected <[<button>Outside</button>, <button>Inside</button>]> but was <[<button>Outside</button>, <button>Inert</button>, <button>Inside</button>]>")
  void shouldRespectPLAYWRIGHTSKIPDIALOGCHECK(TestServer server, BrowserName browserName) {
    // Java: process.env of the in-process driver is the env of the driver process that a new Playwright starts.
    try (Playwright playwright = Playwright.create(new Playwright.CreateOptions().setEnv(Collections.singletonMap("PLAYWRIGHT_SKIP_DIALOG_CHECK", "1")));
         Browser browser = browserName.browserType(playwright).launch(Env.launchOptions())) {
      Page page = browser.newPage();
      // Navigation creates a fresh injected script that reads the env variable.
      page.navigate(server.EMPTY_PAGE);
      page.setContent("\n" +
        "      <button>Outside</button>\n" +
        "      <div inert><button>Inert</button></div>\n" +
        "      <dialog id=dialog><button>Inside</button></dialog>\n" +
        "      <script>\n" +
        "        document.getElementById('dialog').showModal();\n" +
        "      </script>\n" +
        "    ");
      assertEquals(Arrays.asList("<button>Outside</button>", "<button>Inside</button>"), page.getByRole(AriaRole.BUTTON).evaluateAll("els => els.map(e => e.outerHTML)"));
    }
  }

}
