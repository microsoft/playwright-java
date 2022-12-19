package com.microsoft.playwright;

import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;

public class TestSelectorsRole extends TestBase {
  @Test
  void shouldDetectRoles() {
    page.setContent("<button>Hello</button>\n" +
      "    <select multiple=\"\" size=\"2\"></select>\n" +
      "    <select></select>\n" +
      "    <h3>Heading</h3>\n" +
      "    <details><summary>Hello</summary></details>\n" +
      "    <div role='dialog'>I am a dialog</div>");
    assertEquals(asList("<button>Hello</button>"), page.locator("role=button").evaluateAll("els => els.map(e => e.outerHTML)"));

    assertEquals(asList("<select multiple=\"\" size=\"2\"></select>"), page.locator("role=listbox").evaluateAll("els => els.map(e => e.outerHTML)"));

    assertEquals(asList("<select></select>"), page.locator("role=combobox").evaluateAll("els => els.map(e => e.outerHTML)"));

    assertEquals(asList("<h3>Heading</h3>"), page.locator("role=heading").evaluateAll("els => els.map(e => e.outerHTML)"));

    assertEquals(asList("<details><summary>Hello</summary></details>"), page.locator("role=group").evaluateAll("els => els.map(e => e.outerHTML)"));

    assertEquals(asList("<div role=\"dialog\">I am a dialog</div>"), page.locator("role=dialog").evaluateAll("els => els.map(e => e.outerHTML)"));

    assertEquals(emptyList(), page.locator("role=menuitem").evaluateAll("els => els.map(e => e.outerHTML)"));

    assertEquals(emptyList(), page.getByRole(AriaRole.MENUITEM).evaluateAll("els => els.map(e => e.outerHTML)"));
  }

  @Test
  void shouldSupportSelected() {
    page.setContent("<select>\n" +
      "      <option>Hi</option>\n" +
      "      <option selected>Hello</option>\n" +
      "    </select>\n" +
      "    <div>\n" +
      "      <div role=\"option\" aria-selected=\"true\">Hi</div>\n" +
      "      <div role=\"option\" aria-selected=\"false\">Hello</div>\n" +
      "    </div>");
    assertEquals(asList(
      "<option selected=\"\">Hello</option>",
      "<div role=\"option\" aria-selected=\"true\">Hi</div>"
    ), page.locator("role=option[selected]").evaluateAll("els => els.map(e => e.outerHTML)"));

    assertEquals(asList(
      "<option selected=\"\">Hello</option>",
      "<div role=\"option\" aria-selected=\"true\">Hi</div>"
    ), page.locator("role=option[selected=true]").evaluateAll("els => els.map(e => e.outerHTML)"));

    assertEquals(asList(
      "<option selected=\"\">Hello</option>",
      "<div role=\"option\" aria-selected=\"true\">Hi</div>"
    ), page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setSelected(true)).evaluateAll("els => els.map(e => e.outerHTML)"));

    assertEquals(asList(
      "<option>Hi</option>",
      "<div role=\"option\" aria-selected=\"false\">Hello</div>"
    ), page.locator("role=option[selected=false]").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(asList(
      "<option>Hi</option>",
      "<div role=\"option\" aria-selected=\"false\">Hello</div>"
    ), page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setSelected(false)).evaluateAll("els => els.map(e => e.outerHTML)"));
  }

  @Test
  void shouldSupportChecked() {
    page.setContent("<input type=checkbox>\n" +
      "    <input type=checkbox checked>\n" +
      "    <input type=checkbox indeterminate>\n" +
      "    <div role=checkbox aria-checked=\"true\">Hi</div>\n" +
      "    <div role=checkbox aria-checked=\"false\">Hello</div>\n" +
      "    <div role=checkbox>Unknown</div>");
    page.evalOnSelector("[indeterminate]", "input => input.indeterminate = true");

    assertEquals(asList(
      "<input type=\"checkbox\" checked=\"\">",
      "<div role=\"checkbox\" aria-checked=\"true\">Hi</div>"
    ), page.locator("role=checkbox[checked]").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(asList(
      "<input type=\"checkbox\" checked=\"\">",
      "<div role=\"checkbox\" aria-checked=\"true\">Hi</div>"
    ), page.locator("role=checkbox[checked=true]").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(asList(
      "<input type=\"checkbox\" checked=\"\">",
      "<div role=\"checkbox\" aria-checked=\"true\">Hi</div>"
    ), page.getByRole(AriaRole.CHECKBOX, new Page.GetByRoleOptions().setChecked(true)).evaluateAll("els => els.map(e => e.outerHTML)"));

    assertEquals(asList(
      "<input type=\"checkbox\">",
      "<div role=\"checkbox\" aria-checked=\"false\">Hello</div>",
      "<div role=\"checkbox\">Unknown</div>"
    ), page.locator("role=checkbox[checked=false]").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(asList(
      "<input type=\"checkbox\">",
      "<div role=\"checkbox\" aria-checked=\"false\">Hello</div>",
      "<div role=\"checkbox\">Unknown</div>"
    ), page.getByRole(AriaRole.CHECKBOX, new Page.GetByRoleOptions().setChecked(false)).evaluateAll("els => els.map(e => e.outerHTML)"));

    assertEquals(asList(
      "<input type=\"checkbox\" indeterminate=\"\">"
    ), page.locator("role=checkbox[checked='mixed']").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(asList(
      "<input type=\"checkbox\">",
      "<input type=\"checkbox\" checked=\"\">",
      "<input type=\"checkbox\" indeterminate=\"\">",
      "<div role=\"checkbox\" aria-checked=\"true\">Hi</div>",
      "<div role=\"checkbox\" aria-checked=\"false\">Hello</div>",
      "<div role=\"checkbox\">Unknown</div>"
    ), page.locator("role=checkbox").evaluateAll("els => els.map(e => e.outerHTML)"));
  }

  @Test
  void shouldSupportPressed() {
    page.setContent("<button>Hi</button>\n" +
      "    <button aria-pressed=\"true\">Hello</button>\n" +
      "    <button aria-pressed=\"false\">Bye</button>\n" +
      "    <button aria-pressed=\"mixed\">Mixed</button>");
    assertEquals(asList(
      "<button aria-pressed=\"true\">Hello</button>"
    ), page.locator("role=button[pressed]").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(asList(
      "<button aria-pressed=\"true\">Hello</button>"
    ), page.locator("role=button[pressed=true]").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(asList(
      "<button aria-pressed=\"true\">Hello</button>"
    ), page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setPressed(true)).evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(asList(
      "<button>Hi</button>",
      "<button aria-pressed=\"false\">Bye</button>"
    ), page.locator("role=button[pressed=false]").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(asList(
      "<button>Hi</button>",
      "<button aria-pressed=\"false\">Bye</button>"
    ), page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setPressed(false)).evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(asList(
      "<button aria-pressed=\"mixed\">Mixed</button>"
    ), page.locator("role=button[pressed='mixed']").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(asList(
      "<button>Hi</button>",
      "<button aria-pressed=\"true\">Hello</button>",
      "<button aria-pressed=\"false\">Bye</button>",
      "<button aria-pressed=\"mixed\">Mixed</button>"
    ), page.locator("role=button").evaluateAll("els => els.map(e => e.outerHTML)"));
  }

  @Test
  void shouldSupportExpanded() {
    page.setContent("<div role=\"treeitem\">Hi</div>\n" +
      "    <div role=\"treeitem\" aria-expanded=\"true\">Hello</div>\n" +
      "    <div role=\"treeitem\" aria-expanded=\"false\">Bye</div>");
    assertEquals(asList(
      "<div role=\"treeitem\">Hi</div>",
      "<div role=\"treeitem\" aria-expanded=\"true\">Hello</div>",
      "<div role=\"treeitem\" aria-expanded=\"false\">Bye</div>"
    ), page.locator("role=treeitem").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(asList(
      "<div role=\"treeitem\">Hi</div>",
      "<div role=\"treeitem\" aria-expanded=\"true\">Hello</div>",
      "<div role=\"treeitem\" aria-expanded=\"false\">Bye</div>"
    ), page.getByRole(AriaRole.TREEITEM).evaluateAll("els => els.map(e => e.outerHTML)"));


    assertEquals(asList(
      "<div role=\"treeitem\" aria-expanded=\"true\">Hello</div>"
    ), page.locator("role=treeitem[expanded]").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(asList(
      "<div role=\"treeitem\" aria-expanded=\"true\">Hello</div>"
    ), page.locator("role=treeitem[expanded=true]").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(asList(
      "<div role=\"treeitem\" aria-expanded=\"true\">Hello</div>"
    ), page.getByRole(AriaRole.TREEITEM, new Page.GetByRoleOptions().setExpanded(true)).evaluateAll("els => els.map(e => e.outerHTML)"));

    assertEquals(asList(
      "<div role=\"treeitem\" aria-expanded=\"false\">Bye</div>"
    ), page.locator("role=treeitem[expanded=false]").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(asList(
      "<div role=\"treeitem\" aria-expanded=\"false\">Bye</div>"
    ), page.getByRole(AriaRole.TREEITEM, new Page.GetByRoleOptions().setExpanded(false)).evaluateAll("els => els.map(e => e.outerHTML)"));

    // Workaround for expanded='none'.
    assertEquals(asList(
      "<div role=\"treeitem\">Hi</div>"
    ), page.locator("[role=treeitem]:not([aria-expanded])").evaluateAll("els => els.map(e => e.outerHTML)"));
  }

  @Test
  void shouldSupportDisabled() {
    page.setContent("<button>Hi</button>\n" +
      "    <button disabled>Bye</button>\n" +
      "    <button aria-disabled=\"true\">Hello</button>\n" +
      "    <button aria-disabled=\"false\">Oh</button>\n" +
      "    <fieldset disabled>\n" +
      "      <button>Yay</button>\n" +
      "    </fieldset>");
    assertEquals(asList(
      "<button disabled=\"\">Bye</button>",
      "<button aria-disabled=\"true\">Hello</button>",
      "<button>Yay</button>"
    ), page.locator("role=button[disabled]").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(asList(
      "<button disabled=\"\">Bye</button>",
      "<button aria-disabled=\"true\">Hello</button>",
      "<button>Yay</button>"
    ), page.locator("role=button[disabled=true]").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(asList(
      "<button disabled=\"\">Bye</button>",
      "<button aria-disabled=\"true\">Hello</button>",
      "<button>Yay</button>"
    ), page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setDisabled(true)).evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(asList(
      "<button>Hi</button>",
      "<button aria-disabled=\"false\">Oh</button>"
      ), page.locator("role=button[disabled=false]").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(asList(
      "<button>Hi</button>",
      "<button aria-disabled=\"false\">Oh</button>"
      ), page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setDisabled(false)).evaluateAll("els => els.map(e => e.outerHTML)"));
  }

  @Test
  void shouldSupportLevel() {
    page.setContent("<h1>Hello</h1>\n" +
      "    <h3>Hi</h3>\n" +
      "    <div role=\"heading\" aria-level=\"5\">Bye</div>");
    assertEquals(asList(
      "<h1>Hello</h1>"
    ), page.locator("role=heading[level=1]").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(asList(
      "<h1>Hello</h1>"
    ), page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setLevel(1)).evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(asList(
      "<h3>Hi</h3>"
    ), page.locator("role=heading[level=3]").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(asList(
      "<h3>Hi</h3>"
    ), page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setLevel(3)).evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(asList(
      "<div role=\"heading\" aria-level=\"5\">Bye</div>"
      ), page.locator("role=heading[level=5]").evaluateAll("els => els.map(e => e.outerHTML)"));
  }

  @Test
  void shouldFilterHiddenUnlessExplicitlyAskedFor() {
    page.setContent("<button>Hi</button>\n" +
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
      "    <script>\n" +
      "      function addButton(host, text) {\n" +
      "        const root = host.attachShadow({ mode: 'open' });\n" +
      "        const button = document.createElement('button');\n" +
      "        button.textContent = text;\n" +
      "        root.appendChild(button);\n" +
      "      }\n" +
      "      addButton(document.getElementById('host1'), 'Shadow1');\n" +
      "      addButton(document.getElementById('host2'), 'Shadow2');\n" +
      "    </script>");
    assertEquals(asList(
      "<button>Hi</button>",
      "<button aria-hidden=\"false\">Nay</button>",
      "<button style=\"visibility:visible\">Still here</button>",
      "<button>Shadow1</button>"
    ), page.locator("role=button").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(asList(
      "<button>Hi</button>",
      "<button hidden=\"\">Hello</button>",
      "<button aria-hidden=\"true\">Yay</button>",
      "<button aria-hidden=\"false\">Nay</button>",
      "<button style=\"visibility:hidden\">Bye</button>",
      "<button>Oh</button>",
      "<button style=\"visibility:visible\">Still here</button>",
      "<button style=\"display:none\">Never</button>",
      "<button>Shadow1</button>",
      "<button>Shadow2</button>"
    ), page.locator("role=button[include-hidden]").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(asList(
      "<button>Hi</button>",
      "<button hidden=\"\">Hello</button>",
      "<button aria-hidden=\"true\">Yay</button>",
      "<button aria-hidden=\"false\">Nay</button>",
      "<button style=\"visibility:hidden\">Bye</button>",
      "<button>Oh</button>",
      "<button style=\"visibility:visible\">Still here</button>",
      "<button style=\"display:none\">Never</button>",
      "<button>Shadow1</button>",
      "<button>Shadow2</button>"
    ), page.locator("role=button[include-hidden=true]").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(asList(
      "<button>Hi</button>",
      "<button aria-hidden=\"false\">Nay</button>",
      "<button style=\"visibility:visible\">Still here</button>",
      "<button>Shadow1</button>"
    ), page.locator("role=button[include-hidden=false]").evaluateAll("els => els.map(e => e.outerHTML)"));
  }

  @Test
  void shouldSupportName() {
    page.setContent("<div role=\"button\" aria-label=\" Hello \"></div>\n" +
      "    <div role=\"button\" aria-label=\"Hallo\"></div>\n" +
      "    <div role=\"button\" aria-label=\"Hello\" aria-hidden=\"true\"></div>\n" +
      "    <div role=\"button\" aria-label=\"123\" aria-hidden=\"true\"></div>\n" +
      "    <div role=\"button\" aria-label='foo\"bar' aria-hidden=\"true\"></div>");
    assertEquals(asList(
      "<div role=\"button\" aria-label=\" Hello \"></div>"
      ), page.locator("role=button[name='Hello']").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(asList(
      "<div role=\"button\" aria-label=\" Hello \"></div>"
    ), page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(" \n Hello ")).evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(asList(
      "<div role=\"button\" aria-label=\" Hello \"></div>"
    ), page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Hello")).evaluateAll("els => els.map(e => e.outerHTML)"));

    assertEquals(asList(
      "<div role=\"button\" aria-label=\"Hallo\"></div>"
    ), page.locator("role=button[name*='all']").evaluateAll("els => els.map(e => e.outerHTML)"));

    assertEquals(asList(
      "<div role=\"button\" aria-label=\" Hello \"></div>",
      "<div role=\"button\" aria-label=\"Hallo\"></div>"
    ), page.locator("role=button[name=/^H[ae]llo$/]").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(asList(
      "<div role=\"button\" aria-label=\" Hello \"></div>",
      "<div role=\"button\" aria-label=\"Hallo\"></div>"
    ), page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("^H[ae]llo$"))).evaluateAll("els => els.map(e => e.outerHTML)"));

    assertEquals(asList(
      "<div role=\"button\" aria-label=\" Hello \"></div>",
      "<div role=\"button\" aria-label=\"Hallo\"></div>"
    ), page.locator("role=button[name=/h.*o/i]").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(asList(
      "<div role=\"button\" aria-label=\" Hello \"></div>",
      "<div role=\"button\" aria-label=\"Hallo\"></div>"
    ), page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("h.*o", Pattern.CASE_INSENSITIVE))).evaluateAll("els => els.map(e => e.outerHTML)"));

    assertEquals(asList(
      "<div role=\"button\" aria-label=\" Hello \"></div>",
      "<div role=\"button\" aria-label=\"Hello\" aria-hidden=\"true\"></div>"
      ), page.locator("role=button[name='Hello'][include-hidden]").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(asList(
      "<div role=\"button\" aria-label=\" Hello \"></div>",
      "<div role=\"button\" aria-label=\"Hello\" aria-hidden=\"true\"></div>"
      ), page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Hello").setIncludeHidden(true)).evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(asList(
      "<div role=\"button\" aria-label=\" Hello \"></div>",
      "<div role=\"button\" aria-label=\"Hello\" aria-hidden=\"true\"></div>"
      ), page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("hello").setIncludeHidden(true)).evaluateAll("els => els.map(e => e.outerHTML)"));

    assertEquals(asList(
      "<div role=\"button\" aria-label=\" Hello \"></div>"
      ), page.locator("role=button[name=Hello]").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(asList(
      "<div role=\"button\" aria-label=\"123\" aria-hidden=\"true\"></div>"
      ), page.locator("role=button[name=123][include-hidden]").evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(asList(
      "<div role=\"button\" aria-label=\"123\" aria-hidden=\"true\"></div>"
      ), page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("123").setIncludeHidden(true)).evaluateAll("els => els.map(e => e.outerHTML)"));
  }

  @Test
  void errors() {
    PlaywrightException e0 = assertThrows(PlaywrightException.class, () -> page.querySelector("role=[bar]"));
    assertTrue(e0.getMessage().contains("Role must not be empty"), e0.getMessage());

    PlaywrightException e1 = assertThrows(PlaywrightException.class, () -> page.querySelector("role=foo[sElected]"));
    assertTrue(e1.getMessage().contains("Unknown attribute \"sElected\", must be one of \"checked\", \"disabled\", \"expanded\", \"include-hidden\", \"level\", \"name\", \"pressed\", \"selected\""), e1.getMessage());

    PlaywrightException e2 = assertThrows(PlaywrightException.class, () -> page.querySelector("role=foo[bar . qux=true]"));
    assertTrue(e2.getMessage().contains("Unknown attribute \"bar.qux\""), e2.getMessage());

    PlaywrightException e3 = assertThrows(PlaywrightException.class, () -> page.querySelector("role=heading[level='bar']"));
    assertTrue(e3.getMessage().contains("\"level\" attribute must be compared to a number"), e3.getMessage());

    PlaywrightException e4 = assertThrows(PlaywrightException.class, () -> page.querySelector("role=checkbox[checked='bar']"));
    assertTrue(e4.getMessage().contains("\"checked\" must be one of true, false, \"mixed\""), e4.getMessage());

    PlaywrightException e5 = assertThrows(PlaywrightException.class, () -> page.querySelector("role=checkbox[checked~=true]"));
    assertTrue(e5.getMessage().contains("cannot use ~= in attribute with non-string matching value"), e5.getMessage());

    PlaywrightException e6 = assertThrows(PlaywrightException.class, () -> page.querySelector("role=button[level=3]"));
    assertTrue(e6.getMessage().contains("\"level\" attribute is only supported for roles: \"heading\", \"listitem\", \"row\", \"treeitem\""), e6.getMessage());

    PlaywrightException e7 = assertThrows(PlaywrightException.class, () -> page.querySelector("role=button[name]"));
    assertTrue(e7.getMessage().contains("\"name\" attribute must have a value"), e7.getMessage());
  }

}
