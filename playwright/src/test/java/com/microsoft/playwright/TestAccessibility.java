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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.condition.EnabledIf;

import static org.junit.jupiter.api.Assertions.*;

public class TestAccessibility extends TestBase {
  static void assertNodeEquals(String expected, String actual) {
    JsonElement actualJson = new Gson().fromJson(actual, JsonElement.class);
    assertNodeEquals(expected, actualJson);
  }

  static void assertNodeEquals(String expected, JsonElement actualJson) {
    assertEquals(JsonParser.parseString(expected), actualJson);
  }

  @Test
  void shouldWork() {
    page.setContent("<head>\n" +
      "  <title>Accessibility Test</title>\n" +
      "</head>\n" +
      "<body>\n" +
      "  <h1>Inputs</h1>\n" +
      "  <input placeholder='Empty input' autofocus />\n" +
      "  <input placeholder='readonly input' readonly />\n" +
      "  <input placeholder='disabled input' disabled />\n" +
      "  <input aria-label='Input with whitespace' value='  ' />\n" +
      "  <input value='value only' />\n" +
      "  <input aria-placeholder='placeholder' value='and a value' />\n" +
      "  <div aria-hidden='true' id='desc'>This is a description!</div>\n" +
      "  <input aria-placeholder='placeholder' value='and a value' aria-describedby='desc' />\n" +
      "</body>");
    // autofocus happens after a delay in chrome these days
    page.waitForFunction("() => document.activeElement.hasAttribute('autofocus')");

    String golden = isFirefox() ? "{\n" +
      "  role: 'document',\n" +
      "  name: 'Accessibility Test',\n" +
      "  children: [\n" +
      "    {role: 'heading', name: 'Inputs', level: 1},\n" +
      "    {role: 'textbox', name: 'Empty input', focused: true},\n" +
      "    {role: 'textbox', name: 'readonly input', readonly: true},\n" +
      "    {role: 'textbox', name: 'disabled input', disabled: true},\n" +
      "    {role: 'textbox', name: 'Input with whitespace', valueString: '  '},\n" +
      "    {role: 'textbox', name: '', valueString: 'value only'},\n" +
      "    {role: 'textbox', name: '', valueString: 'and a value'}, // firefox doesn't use aria-placeholder for the name\n" +
      "    {role: 'textbox', name: '', valueString: 'and a value', description: 'This is a description!'} // and here\n" +
      "  ]\n" +
      "}" : isChromium() ? "{\n" +
      "  role: 'WebArea',\n" +
      "  name: 'Accessibility Test',\n" +
      "  children: [\n" +
      "    {role: 'heading', name: 'Inputs', level: 1},\n" +
      "    {role: 'textbox', name: 'Empty input', focused: true},\n" +
      "    {role: 'textbox', name: 'readonly input', readonly: true},\n" +
      "    {role: 'textbox', name: 'disabled input', disabled: true},\n" +
      "    {role: 'textbox', name: 'Input with whitespace', valueString: '  '},\n" +
      "    {role: 'textbox', name: '', valueString: 'value only'},\n" +
      "    {role: 'textbox', name: 'placeholder', valueString: 'and a value'},\n" +
      "    {role: 'textbox', name: 'placeholder', valueString: 'and a value', description: 'This is a description!'}\n" +
      "  ]\n" +
      "}" : "{\n" +
      "  role: 'WebArea',\n" +
      "  name: 'Accessibility Test',\n" +
      "  children: [\n" +
      "    {role: 'heading', name: 'Inputs', level: 1},\n" +
      "    {role: 'textbox', name: 'Empty input', focused: true},\n" +
      "    {role: 'textbox', name: 'readonly input', readonly: true},\n" +
      "    {role: 'textbox', name: 'disabled input', disabled: true},\n" +
      "    {role: 'textbox', name: 'Input with whitespace', valueString: '  ' },\n" +
      "    {role: 'textbox', name: '', valueString: 'value only' },\n" +
      "    {role: 'textbox', name: 'placeholder', valueString: 'and a value'},\n" +
      "    {role: 'textbox', name: 'This is a description!',valueString: 'and a value'} // webkit uses the description over placeholder for the name\n" +
      "  ]\n" +
      "}";
    assertNodeEquals(golden, page.accessibility().snapshot());
  }

  @Test
  void shouldWorkWithRegularText() {
    page.setContent("<div>Hello World</div>");
    JsonObject snapshot = new Gson().fromJson(page.accessibility().snapshot(), JsonObject.class);
    JsonObject node = snapshot.getAsJsonArray("children").get(0).getAsJsonObject();
    assertEquals(isFirefox() ? "text leaf" : "text", node.get("role").getAsString());
    assertEquals("Hello World", node.get("name").getAsString());
  }

  @Test
  void roledescription() {
    page.setContent("<div tabIndex=-1 aria-roledescription='foo'>Hi</div>");
    JsonObject snapshot = new Gson().fromJson(page.accessibility().snapshot(), JsonObject.class);
    assertEquals("foo", snapshot.getAsJsonArray("children")
      .get(0).getAsJsonObject()
      .get("roledescription").getAsString());
  }

  @Test
  void orientation() {
    page.setContent("<a href='' role='slider' aria-orientation='vertical'>11</a>");
    JsonObject snapshot = new Gson().fromJson(page.accessibility().snapshot(), JsonObject.class);
    assertEquals("vertical", snapshot.getAsJsonArray("children")
      .get(0).getAsJsonObject()
      .get("orientation").getAsString());
  }

  @Test
  void autocomplete() {
    page.setContent("<div role='textbox' aria-autocomplete='list'>hi</div>");
    JsonObject snapshot = new Gson().fromJson(page.accessibility().snapshot(), JsonObject.class);
    assertEquals("list", snapshot.getAsJsonArray("children")
      .get(0).getAsJsonObject()
      .get("autocomplete").getAsString());
  }

  @Test
  void multiselectable() {
    page.setContent("<div role='grid' tabIndex=-1 aria-multiselectable=true>hey</div>");
    JsonObject snapshot = new Gson().fromJson(page.accessibility().snapshot(), JsonObject.class);
    assertEquals(true, snapshot.getAsJsonArray("children")
      .get(0).getAsJsonObject()
      .get("multiselectable").getAsBoolean());
  }

  @Test
  void keyshortcuts() {
    page.setContent("<div role='grid' tabIndex=-1 aria-keyshortcuts='foo'>hey</div>");
    JsonObject snapshot = new Gson().fromJson(page.accessibility().snapshot(), JsonObject.class);
    assertEquals("foo", snapshot.getAsJsonArray("children")
      .get(0).getAsJsonObject()
      .get("keyshortcuts").getAsString());
  }

  @Test
  void shouldNotReportTextNodesInsideControls() {
    page.setContent("<div role='tablist'>\n" +
      "  <div role='tab' aria-selected='true'><b>Tab1</b></div>\n" +
      "  <div role='tab'>Tab2</div>\n" +
      "</div>");
    String golden = "{\n" +
      "  role: '" + (isFirefox() ? "document" : "WebArea") + "',\n" +
      "  name: '',\n" +
      "  children: [{\n" +
      "    role: 'tab',\n" +
      "    name: 'Tab1',\n" +
      "    selected: true\n" +
      "  }, {\n" +
      "    role: 'tab',\n" +
      "    name: 'Tab2'\n" +
      "  }]\n" +
      "}";
    assertNodeEquals(golden, page.accessibility().snapshot());
  }

  @Test
  @DisabledIf(value="com.microsoft.playwright.TestBase#isWebKit", disabledReason="skip")
  void richTextEditableFieldsShouldHaveChildren() {
    page.setContent("<div contenteditable='true'>\n" +
      "  Edit this image: <img src='fakeimage.png' alt='my fake image'>\n" +
      "</div>");
    String golden = isFirefox() ? "{\n" +
      "  role: 'section',\n" +
      "  name: '',\n" +
      "  children: [{\n" +
      "    role: 'text leaf',\n" +
      "    name: 'Edit this image: '\n" +
      "  }, {\n" +
      "    role: 'text',\n" +
      "    name: 'my fake image'\n" +
      "  }]\n" +
      "}" : "{\n" +
      "  role: 'generic',\n" +
      "  name: '',\n" +
      "  valueString: 'Edit this image: ',\n" +
      "  children: [{\n" +
      "    role: 'text',\n" +
      "    name: 'Edit this image:'\n" +
      "  }, {\n" +
      "    role: 'img',\n" +
      "    name: 'my fake image'\n" +
      "  }]\n" +
      "}";
    JsonObject snapshot = new Gson().fromJson(page.accessibility().snapshot(), JsonObject.class);
    assertNodeEquals(golden, snapshot.getAsJsonArray("children").get(0));
  }

  @Test
  @DisabledIf(value="com.microsoft.playwright.TestBase#isWebKit", disabledReason="skip")
  void richTextEditableFieldsWithRoleShouldHaveChildren() {
    page.setContent("<div contenteditable='true' role=\"textbox\">\n" +
      "  Edit this image: <img src='fakeimage.png' alt='my fake image'>\n" +
      "</div>");
    String golden = isFirefox() ? "{\n" +
      "  role: 'textbox',\n" +
      "  name: '',\n" +
      "  valueString: 'Edit this image: my fake image',\n" +
      "  children: [{\n" +
      "    role: 'text',\n" +
      "    name: 'my fake image'\n" +
      "  }]\n" +
      "}" : "{\n" +
      "  role: 'textbox',\n" +
      "  name: '',\n" +
      "  valueString: 'Edit this image: ',\n" +
      "  children: [{\n" +
      "    role: 'text',\n" +
      "    name: 'Edit this image:'\n" +
      "  }, {\n" +
      "    role: 'img',\n" +
      "    name: 'my fake image'\n" +
      "  }]\n" +
      "}";
    JsonObject snapshot = new Gson().fromJson(page.accessibility().snapshot(), JsonObject.class);
    assertNodeEquals(golden, snapshot.getAsJsonArray("children").get(0));
  }

  @Test
  @EnabledIf(value="com.microsoft.playwright.TestBase#isChromium", disabledReason="skip")
  void plainTextFieldWithRoleShouldNotHaveChildren() {
    page.setContent("<div contenteditable='plaintext-only' role='textbox'>Edit this image:<img src='fakeimage.png' alt='my fake image'></div>");
    JsonObject snapshot = new Gson().fromJson(page.accessibility().snapshot(), JsonObject.class);
    assertNodeEquals("{\n" +
      "  role: 'textbox',\n" +
      "  name: '',\n" +
      "  valueString: 'Edit this image:'\n" +
      "}", snapshot.getAsJsonArray("children").get(0));
  }

  @Test
  @EnabledIf(value="com.microsoft.playwright.TestBase#isChromium", disabledReason="skip")
  void plainTextFieldWithoutRoleShouldNotHaveContent() {
    page.setContent("<div contenteditable='plaintext-only'>Edit this image:<img src='fakeimage.png' alt='my fake image'></div>");
    JsonObject snapshot = new Gson().fromJson(page.accessibility().snapshot(), JsonObject.class);
    assertNodeEquals("{\n" +
      "  role: 'generic',\n" +
      "  name: ''\n" +
      "}", snapshot.getAsJsonArray("children").get(0));
  }

  @Test
  @EnabledIf(value="com.microsoft.playwright.TestBase#isChromium", disabledReason="skip")
  void plainTextFieldWithTabindexAndWithoutRoleShouldNotHaveContent() {
    page.setContent("<div contenteditable='plaintext-only' tabIndex=0>Edit this image:<img src='fakeimage.png' alt='my fake image'></div>");
    JsonObject snapshot = new Gson().fromJson(page.accessibility().snapshot(), JsonObject.class);
    assertNodeEquals("{\n" +
      "  role: 'generic',\n" +
      "  name: ''\n" +
      "}", snapshot.getAsJsonArray("children").get(0));
  }

  @Test
  void nonEditableTextboxWithRoleAndTabIndexAndLabelShouldNotHaveChildren() {
    page.setContent("<div role='textbox' tabIndex=0 aria-checked='true' aria-label='my favorite textbox'>\n" +
      "this is the inner content\n" +
      "<img alt='yo' src='fakeimg.png'>\n" +
      "</div>");
    String golden = isFirefox() ? "{\n" +
      "  role: 'textbox',\n" +
      "  name: 'my favorite textbox',\n" +
      "  valueString: 'this is the inner content yo'\n" +
      "}" : isChromium() ? "{\n" +
      "  role: 'textbox',\n" +
      "  name: 'my favorite textbox',\n" +
      "  valueString: 'this is the inner content '\n" +
      "}" : "{\n" +
      "  role: 'textbox',\n" +
      "  name: 'my favorite textbox',\n" +
      "  valueString: 'this is the inner content '\n" +
      "}";
    JsonObject snapshot = new Gson().fromJson(page.accessibility().snapshot(), JsonObject.class);
    assertNodeEquals(golden, snapshot.getAsJsonArray("children").get(0));
  }

  @Test
  void checkboxWithAndTabIndexAndLabelShouldNotHaveChildren() {
    page.setContent("<div role='checkbox' tabIndex=0 aria-checked='true' aria-label='my favorite checkbox'>\n" +
      "this is the inner content\n" +
      "<img alt='yo' src='fakeimg.png'>\n" +
      "</div>");
    String golden = "{\n" +
      "  role: 'checkbox',\n" +
      "  name: 'my favorite checkbox',\n" +
      "  checked: 'checked'\n" +
      "}";
    JsonObject snapshot = new Gson().fromJson(page.accessibility().snapshot(), JsonObject.class);
    assertNodeEquals(golden, snapshot.getAsJsonArray("children").get(0));
  }

  @Test
  void checkboxWithoutLabelShouldNotHaveChildren() {
    page.setContent("<div role='checkbox' aria-checked='true'>\n" +
      "this is the inner content\n" +
      "<img alt='yo' src='fakeimg.png'>\n" +
      "</div>");
    String golden = isFirefox() ? "{\n" +
      "  role: 'checkbox',\n" +
      "  name: 'this is the inner content yo',\n" +
      "  checked: 'checked'\n" +
      "}" : "{\n" +
      "  role: 'checkbox',\n" +
      "  name: 'this is the inner content yo',\n" +
      "  checked: 'checked'\n" +
      "}";
    JsonObject snapshot = new Gson().fromJson(page.accessibility().snapshot(), JsonObject.class);
    assertNodeEquals(golden, snapshot.getAsJsonArray("children").get(0));
  }

  @Test
  void shouldWorkAButton() {
    page.setContent("<button>My Button</button>");

    ElementHandle button = page.querySelector("button");
    assertNodeEquals("{\n" +
      "  role: 'button',\n" +
      "  name: 'My Button'\n" +
      "}", page.accessibility().snapshot(new Accessibility.SnapshotOptions().withRoot(button)));
  }

  @Test
  void shouldWorkAnInput() {
    page.setContent("<input title='My Input' value='My Value'>");

    ElementHandle input = page.querySelector("input");
    assertNodeEquals("{\n" +
      "  role: 'textbox',\n" +
      "  name: 'My Input',\n" +
      "  valueString: 'My Value'\n" +
      "}", page.accessibility().snapshot(new Accessibility.SnapshotOptions().withRoot(input)));
  }

  @Test
  void shouldWorkOnAMenu() {
    page.setContent("<div role='menu' title='My Menu'>\n" +
      "  <div role='menuitem'>First Item</div>\n" +
      "  <div role='menuitem'>Second Item</div>\n" +
      "  <div role='menuitem'>Third Item</div>\n" +
      "</div>");

    ElementHandle menu = page.querySelector("div[role='menu']");
    assertNodeEquals("{\n" +
      "  role: 'menu',\n" +
      "  name: 'My Menu',\n" +
      "  children:\n" +
      "  [ { role: 'menuitem', name: 'First Item' },\n" +
      "    { role: 'menuitem', name: 'Second Item' },\n" +
      "    { role: 'menuitem', name: 'Third Item' } ]\n" +
      (isWebKit() ? ", orientation: 'vertical'" : "") +
      "  }", page.accessibility().snapshot(new Accessibility.SnapshotOptions().withRoot(menu)));
  }

  @Test
  void shouldReturnNullWhenTheElementIsNoLongerInDOM() {
    page.setContent("<button>My Button</button>");
    ElementHandle button = page.querySelector("button");
    page.evalOnSelector("button", "button => button.remove()");
    assertEquals(null, page.accessibility().snapshot(new Accessibility.SnapshotOptions().withRoot(button)));
  }

  @Test
  void shouldShowUninterestingNodes() {
    page.setContent("<div id='root' role='textbox'>\n" +
      "  <div>\n" +
      "  hello\n" +
      "    <div>\n" +
      "    world\n" +
      "    </div>\n" +
      "  </div>\n" +
      "</div>");
    ElementHandle root = page.querySelector("#root");
    JsonObject snapshot = new Gson().fromJson(page.accessibility().snapshot(
      new Accessibility.SnapshotOptions().withRoot(root).withInterestingOnly(false)),
      JsonObject.class);
    assertEquals("textbox", snapshot.get("role").getAsString());
    assertTrue(snapshot.get("valueString").getAsString().contains("hello"));
    assertTrue(snapshot.get("valueString").getAsString().contains("world"));
    assertNotNull(snapshot.get("children"));
  }

  @Test
  void shouldWorkWhenThereIsATitle() {
    page.setContent("<title>This is the title</title>\n" +
      "<div>This is the content</div>");
    JsonObject snapshot = new Gson().fromJson(page.accessibility().snapshot(), JsonObject.class);
    assertEquals("This is the title", snapshot.get("name").getAsString());
    assertEquals("This is the content", snapshot.getAsJsonArray("children")
      .get(0).getAsJsonObject()
      .get("name").getAsString());
  }
}
