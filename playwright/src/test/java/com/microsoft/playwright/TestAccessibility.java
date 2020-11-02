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

import com.google.gson.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.*;

public class TestAccessibility extends TestBase {
  private static class AccessibilityNodeSerializer implements JsonSerializer<AccessibilityNode> {
    @Override
    public JsonElement serialize(AccessibilityNode src, Type typeOfSrc, JsonSerializationContext context) {
      JsonObject json = new JsonObject();
      if (src.role() != null) {
        json.addProperty("role", src.role());
      }
      if (src.name() != null) {
        json.addProperty("name", src.name());
      }
      if (src.valueString() != null) {
        json.addProperty("valueString", src.valueString());
      }
      if (src.valueNumber() != null) {
        json.addProperty("valueNumber", src.valueNumber());
      }
      if (src.description() != null) {
        json.addProperty("description", src.description());
      }
      if (src.keyshortcuts() != null) {
        json.addProperty("keyshortcuts", src.keyshortcuts());
      }
      if (src.roledescription() != null) {
        json.addProperty("roledescription", src.roledescription());
      }
      if (src.valuetext() != null) {
        json.addProperty("valuetext", src.valuetext());
      }
      if (src.disabled() != null) {
        json.addProperty("disabled", src.disabled());
      }
      if (src.expanded() != null) {
        json.addProperty("expanded", src.expanded());
      }
      if (src.focused() != null) {
        json.addProperty("focused", src.focused());
      }
      if (src.modal() != null) {
        json.addProperty("modal", src.modal());
      }
      if (src.multiline() != null) {
        json.addProperty("multiline", src.multiline());
      }
      if (src.multiselectable() != null) {
        json.addProperty("multiselectable", src.multiselectable());
      }
      if (src.readonly() != null) {
        json.addProperty("readonly", src.readonly());
      }
      if (src.required() != null) {
        json.addProperty("required", src.required());
      }
      if (src.selected() != null) {
        json.addProperty("selected", src.selected());
      }
      if (src.level() != null) {
        json.addProperty("level", src.level());
      }
      if (src.valuemin() != null) {
        json.addProperty("valuemin", src.valuemin());
      }
      if (src.valuemax() != null) {
        json.addProperty("valuemax", src.valuemax());
      }
      if (src.autocomplete() != null) {
        json.addProperty("autocomplete", src.autocomplete());
      }
      if (src.haspopup() != null) {
        json.addProperty("haspopup", src.haspopup());
      }
      if (src.invalid() != null) {
        json.addProperty("invalid", src.invalid());
      }
      if (src.orientation() != null) {
        json.addProperty("orientation", src.orientation());
      }
      if (src.checked() != null) {
        json.addProperty("checked", src.checked().toString().toLowerCase());
      }
      if (src.pressed() != null) {
        json.addProperty("pressed", src.pressed().toString().toLowerCase());
      }
      if (src.children() != null) {
        JsonArray children = new JsonArray();
        for (AccessibilityNode child : src.children()) {
          children.add(context.serialize(child));
        }
        json.add("children", children);
      }
      return json;
    }
  }

  private static Gson gson = new GsonBuilder()
    .registerTypeHierarchyAdapter(AccessibilityNode.class, new AccessibilityNodeSerializer()).create();


  static void assertNodeEquals(String expected, AccessibilityNode actual) {
    JsonElement actualJson = gson.toJsonTree(actual);
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

    String golden = isFirefox ? "{\n" +
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
      "}" : isChromium ? "{\n" +
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
    AccessibilityNode snapshot = page.accessibility().snapshot();
    AccessibilityNode node = snapshot.children().get(0);
    assertEquals(isFirefox ? "text leaf" : "text", node.role());
    assertEquals("Hello World", node.name());
  }

  @Test
  void roledescription() {
    page.setContent("<div tabIndex=-1 aria-roledescription='foo'>Hi</div>");
    AccessibilityNode snapshot = page.accessibility().snapshot();
    assertEquals("foo", snapshot.children().get(0).roledescription());
  }

  @Test
  void orientation() {
    page.setContent("<a href='' role='slider' aria-orientation='vertical'>11</a>");
    AccessibilityNode snapshot = page.accessibility().snapshot();
    assertEquals("vertical", snapshot.children().get(0).orientation());
  }

  @Test
  void autocomplete() {
    page.setContent("<div role='textbox' aria-autocomplete='list'>hi</div>");
    AccessibilityNode snapshot = page.accessibility().snapshot();
    assertEquals("list", snapshot.children().get(0).autocomplete());
  }

  @Test
  void multiselectable() {
    page.setContent("<div role='grid' tabIndex=-1 aria-multiselectable=true>hey</div>");
    AccessibilityNode snapshot = page.accessibility().snapshot();
    assertEquals(true, snapshot.children().get(0).multiselectable());
  }

  @Test
  void keyshortcuts() {
    page.setContent("<div role='grid' tabIndex=-1 aria-keyshortcuts='foo'>hey</div>");
    AccessibilityNode snapshot = page.accessibility().snapshot();
    assertEquals("foo", snapshot.children().get(0).keyshortcuts());
  }

  @Test
  void shouldNotReportTextNodesInsideControls() {
    page.setContent("<div role='tablist'>\n" +
      "  <div role='tab' aria-selected='true'><b>Tab1</b></div>\n" +
      "  <div role='tab'>Tab2</div>\n" +
      "</div>");
    String golden = "{\n" +
      "  role: '" + (isFirefox ? "document" : "WebArea") + "',\n" +
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
  void richTextEditableFieldsShouldHaveChildren() {
// TODO:   test.skip(browserName === "webkit", "WebKit rich text accessibility is iffy");
    page.setContent("<div contenteditable='true'>\n" +
      "  Edit this image: <img src='fakeimage.png' alt='my fake image'>\n" +
      "</div>");
    String golden = isFirefox ? "{\n" +
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
    AccessibilityNode snapshot = page.accessibility().snapshot();
    assertNodeEquals(golden, snapshot.children().get(0));
  }

  @Test
  void richTextEditableFieldsWithRoleShouldHaveChildren() {
// TODO:   test.skip(browserName === "webkit", "WebKit rich text accessibility is iffy");
    page.setContent("<div contenteditable='true' role=\"textbox\">\n" +
      "  Edit this image: <img src='fakeimage.png' alt='my fake image'>\n" +
      "</div>");
    String golden = isFirefox ? "{\n" +
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
    AccessibilityNode snapshot = page.accessibility().snapshot();
    assertNodeEquals(golden, snapshot.children().get(0));
  }

  // TODO:   suite.skip(browserName === "firefox", "Firefox does not support contenteditable='plaintext-only'");
// TODO:   suite.skip(browserName === "webkit", "WebKit rich text accessibility is iffy");
  @Test
  void plainTextFieldWithRoleShouldNotHaveChildren() {
    page.setContent("<div contenteditable='plaintext-only' role='textbox'>Edit this image:<img src='fakeimage.png' alt='my fake image'></div>");
    AccessibilityNode snapshot = page.accessibility().snapshot();
    assertNodeEquals("{\n" +
      "  role: 'textbox',\n" +
      "  name: '',\n" +
      "  valueString: 'Edit this image:'\n" +
      "}", snapshot.children().get(0));
  }

  @Test
  void plainTextFieldWithoutRoleShouldNotHaveContent() {
    page.setContent("<div contenteditable='plaintext-only'>Edit this image:<img src='fakeimage.png' alt='my fake image'></div>");
    AccessibilityNode snapshot = page.accessibility().snapshot();
    assertNodeEquals("{\n" +
      "  role: 'generic',\n" +
      "  name: ''\n" +
      "}", snapshot.children().get(0));
  }

  @Test
  void plainTextFieldWithTabindexAndWithoutRoleShouldNotHaveContent() {
    page.setContent("<div contenteditable='plaintext-only' tabIndex=0>Edit this image:<img src='fakeimage.png' alt='my fake image'></div>");
    AccessibilityNode snapshot = page.accessibility().snapshot();
    assertNodeEquals("{\n" +
      "  role: 'generic',\n" +
      "  name: ''\n" +
      "}", snapshot.children().get(0));
  }

  @Test
  void nonEditableTextboxWithRoleAndTabIndexAndLabelShouldNotHaveChildren() {
    page.setContent("<div role='textbox' tabIndex=0 aria-checked='true' aria-label='my favorite textbox'>\n" +
      "this is the inner content\n" +
      "<img alt='yo' src='fakeimg.png'>\n" +
      "</div>");
    String golden = isFirefox ? "{\n" +
      "  role: 'textbox',\n" +
      "  name: 'my favorite textbox',\n" +
      "  valueString: 'this is the inner content yo'\n" +
      "}" : isChromium ? "{\n" +
      "  role: 'textbox',\n" +
      "  name: 'my favorite textbox',\n" +
      "  valueString: 'this is the inner content '\n" +
      "}" : "{\n" +
      "  role: 'textbox',\n" +
      "  name: 'my favorite textbox',\n" +
      "  valueString: 'this is the inner content  ',\n" +
      "}";
    AccessibilityNode snapshot = page.accessibility().snapshot();
    assertNodeEquals(golden, snapshot.children().get(0));
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
    AccessibilityNode snapshot = page.accessibility().snapshot();
    assertNodeEquals(golden, snapshot.children().get(0));
  }

  @Test
  void checkboxWithoutLabelShouldNotHaveChildren() {
    page.setContent("<div role='checkbox' aria-checked='true'>\n" +
      "this is the inner content\n" +
      "<img alt='yo' src='fakeimg.png'>\n" +
      "</div>");
    String golden = isFirefox ? "{\n" +
      "  role: 'checkbox',\n" +
      "  name: 'this is the inner content yo',\n" +
      "  checked: 'checked'\n" +
      "}" : "{\n" +
      "  role: 'checkbox',\n" +
      "  name: 'this is the inner content yo',\n" +
      "  checked: 'checked'\n" +
      "}";
    AccessibilityNode snapshot = page.accessibility().snapshot();
    assertNodeEquals(golden, snapshot.children().get(0));
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
      (isWebKit ? ", orientation: 'vertical'" : "") +
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
    AccessibilityNode snapshot = page.accessibility().snapshot(
      new Accessibility.SnapshotOptions().withRoot(root).withInterestingOnly(false));
    assertEquals("textbox", snapshot.role());
    assertTrue(snapshot.valueString().contains("hello"));
    assertTrue(snapshot.valueString().contains("world"));
    assertNotNull(snapshot.children());
  }

  @Test
  void shouldWorkWhenThereIsATitle() {
    page.setContent("<title>This is the title</title>\n" +
      "<div>This is the content</div>");
    AccessibilityNode snapshot = page.accessibility().snapshot();
    assertEquals("This is the title", snapshot.name());
    assertEquals("This is the content", snapshot.children().get(0).name());
  }
}
