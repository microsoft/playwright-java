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

package com.microsoft.playwright.impl;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.options.AriaRole;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.microsoft.playwright.impl.Serialization.serializeArgument;
import static com.microsoft.playwright.impl.Utils.convertType;

public class LocatorAssertionsImpl extends AssertionsBase implements LocatorAssertions {
  public LocatorAssertionsImpl(Locator locator) {
    this(locator, false);
  }

  private LocatorAssertionsImpl(Locator locator, boolean isNot) {
    super((LocatorImpl) locator, isNot);
  }


  @Override
  public void containsClass(String classname, ContainsClassOptions options) {
    ExpectedTextValue expected = new ExpectedTextValue();
    expected.string = classname;
    expectImpl("to.contain.class", expected, classname, "Locator expected to contain class", convertType(options, FrameExpectOptions.class), "Assert \"containsClass\"");
  }

  @Override
  public void containsClass(List<String> classnames, ContainsClassOptions options) {
    List<ExpectedTextValue> list = new ArrayList<>();
    for (String text : classnames) {
      ExpectedTextValue expected = new ExpectedTextValue();
      expected.string = text;
      list.add(expected);
    }
    expectImpl("to.contain.class.array", list, classnames, "Locator expected to contain classes", convertType(options, FrameExpectOptions.class), "Assert \"containsClass\"");
  }

  @Override
  public void containsText(String text, ContainsTextOptions options) {
    ExpectedTextValue expected = new ExpectedTextValue();
    expected.string = text;
    expected.ignoreCase = shouldIgnoreCase(options);
    expected.matchSubstring = true;
    expected.normalizeWhiteSpace = true;
    expectImpl("to.have.text", expected, text, "Locator expected to contain text", convertType(options, FrameExpectOptions.class), "Assert \"containsText\"");
  }

  @Override
  public void containsText(Pattern pattern, ContainsTextOptions options) {
    ExpectedTextValue expected = expectedRegex(pattern);
    expected.ignoreCase = shouldIgnoreCase(options);
    expected.matchSubstring = true;
    expected.normalizeWhiteSpace = true;
    expectImpl("to.have.text", expected, pattern, "Locator expected to contain regex", convertType(options, FrameExpectOptions.class), "Assert \"containsText\"");
  }

  @Override
  public void containsText(String[] strings, ContainsTextOptions options) {
    List<ExpectedTextValue> list = new ArrayList<>();
    for (String text : strings) {
      ExpectedTextValue expected = new ExpectedTextValue();
      expected.string = text;
      expected.ignoreCase = shouldIgnoreCase(options);
      expected.matchSubstring = true;
      expected.normalizeWhiteSpace = true;
      list.add(expected);
    }
    expectImpl("to.contain.text.array", list, strings, "Locator expected to contain text", convertType(options, FrameExpectOptions.class), "Assert \"containsText\"");
  }

  @Override
  public void containsText(Pattern[] patterns, ContainsTextOptions options) {
    List<ExpectedTextValue> list = new ArrayList<>();
    for (Pattern pattern : patterns) {
      ExpectedTextValue expected = expectedRegex(pattern);
      expected.ignoreCase = shouldIgnoreCase(options);
      expected.matchSubstring = true;
      expected.normalizeWhiteSpace = true;
      list.add(expected);
    }
    expectImpl("to.contain.text.array", list, patterns, "Locator expected to contain text", convertType(options, FrameExpectOptions.class), "Assert \"containsText\"");
  }

  @Override
  public void hasAccessibleDescription(String description, HasAccessibleDescriptionOptions options) {
    ExpectedTextValue expected = new ExpectedTextValue();
    expected.string = description;
    expected.ignoreCase = shouldIgnoreCase(options);
    expected.normalizeWhiteSpace = true;
    expectImpl("to.have.accessible.description", expected, description, "Locator expected to have accessible description", convertType(options, FrameExpectOptions.class), "Assert \"hasAccessibleDescription\"");
  }

  @Override
  public void hasAccessibleDescription(Pattern pattern, HasAccessibleDescriptionOptions options) {
    ExpectedTextValue expected = expectedRegex(pattern);
    expected.ignoreCase = shouldIgnoreCase(options);
    expected.normalizeWhiteSpace = true;
    expectImpl("to.have.accessible.description", expected, pattern, "Locator expected to have accessible description", convertType(options, FrameExpectOptions.class), "Assert \"hasAccessibleDescription\"");
  }

  @Override
  public void hasAccessibleErrorMessage(String errorMessage, HasAccessibleErrorMessageOptions options) {
    ExpectedTextValue expected = new ExpectedTextValue();
    expected.string = errorMessage;
    expected.ignoreCase = shouldIgnoreCase(options);
    expected.normalizeWhiteSpace = true;
    expectImpl("to.have.accessible.error.message", expected, errorMessage, "Locator expected to have accessible error message", convertType(options, FrameExpectOptions.class), "Assert \"hasAccessibleErrorMessage\"");
  }

  @Override
  public void hasAccessibleErrorMessage(Pattern pattern, HasAccessibleErrorMessageOptions options) {
    ExpectedTextValue expected = expectedRegex(pattern);
    expected.ignoreCase = shouldIgnoreCase(options);
    expected.normalizeWhiteSpace = true;
    expectImpl("to.have.accessible.error.message", expected, pattern, "Locator expected to have accessible error message", convertType(options, FrameExpectOptions.class), "Assert \"hasAccessibleErrorMessage\"");
  }

  @Override
  public void hasAccessibleName(String name, HasAccessibleNameOptions options) {
    ExpectedTextValue expected = new ExpectedTextValue();
    expected.string = name;
    expected.ignoreCase = shouldIgnoreCase(options);
    expected.normalizeWhiteSpace = true;
    expectImpl("to.have.accessible.name", expected, name, "Locator expected to have accessible name", convertType(options, FrameExpectOptions.class), "Assert \"hasAccessibleName\"");
  }

  @Override
  public void hasAccessibleName(Pattern pattern, HasAccessibleNameOptions options) {
    ExpectedTextValue expected = expectedRegex(pattern);
    expected.ignoreCase = shouldIgnoreCase(options);
    expected.normalizeWhiteSpace = true;
    expectImpl("to.have.accessible.name", expected, pattern, "Locator expected to have accessible name", convertType(options, FrameExpectOptions.class), "Assert \"hasAccessibleName\"");
  }

  @Override
  public void hasAttribute(String name, String text, HasAttributeOptions options) {
    ExpectedTextValue expected = new ExpectedTextValue();
    expected.string = text;
    expected.ignoreCase = shouldIgnoreCase(options);
    hasAttribute(name, expected, text, options);
  }

  @Override
  public void hasAttribute(String name, Pattern pattern, HasAttributeOptions options) {
    ExpectedTextValue expected = expectedRegex(pattern);
    expected.ignoreCase = shouldIgnoreCase(options);
    hasAttribute(name, expected, pattern, options);
  }

  private void hasAttribute(String name, ExpectedTextValue expectedText, Object expectedValue, HasAttributeOptions options) {
    if (options == null) {
      options = new HasAttributeOptions();
    }
    FrameExpectOptions commonOptions = convertType(options, FrameExpectOptions.class);
    commonOptions.expressionArg = name;
    String message = "Locator expected to have attribute '" + name + "'";
    if (expectedValue instanceof Pattern) {
      message += " matching regex";
    }
    expectImpl("to.have.attribute.value", expectedText, expectedValue, message, commonOptions, "Assert \"hasAttribute\"");
  }

  @Override
  public void hasClass(String text, HasClassOptions options) {
    ExpectedTextValue expected = new ExpectedTextValue();
    expected.string = text;
    expectImpl("to.have.class", expected, text, "Locator expected to have class", convertType(options, FrameExpectOptions.class), "Assert \"hasClass\"");
  }

  @Override
  public void hasClass(Pattern pattern, HasClassOptions options) {
    ExpectedTextValue expected = expectedRegex(pattern);
    expectImpl("to.have.class", expected, pattern, "Locator expected to have class matching regex", convertType(options, FrameExpectOptions.class), "Assert \"hasClass\"");
  }

  @Override
  public void hasClass(String[] strings, HasClassOptions options) {
    List<ExpectedTextValue> list = new ArrayList<>();
    for (String text : strings) {
      ExpectedTextValue expected = new ExpectedTextValue();
      expected.string = text;
      list.add(expected);
    }
    expectImpl("to.have.class.array", list, strings, "Locator expected to have class", convertType(options, FrameExpectOptions.class), "Assert \"hasClass\"");
  }

  @Override
  public void hasClass(Pattern[] patterns, HasClassOptions options) {
    List<ExpectedTextValue> list = new ArrayList<>();
    for (Pattern pattern : patterns) {
      ExpectedTextValue expected = expectedRegex(pattern);
      list.add(expected);
    }
    expectImpl("to.have.class.array", list, patterns, "Locator expected to have class matching regex", convertType(options, FrameExpectOptions.class), "Assert \"hasClass\"");
  }

  @Override
  public void hasCount(int count, HasCountOptions options) {
    if (options == null) {
      options = new HasCountOptions();
    }
    FrameExpectOptions commonOptions = convertType(options, FrameExpectOptions.class);
    commonOptions.expectedNumber = (double) count;
    List<ExpectedTextValue> expectedText = null;
    expectImpl("to.have.count", expectedText, count, "Locator expected to have count", commonOptions, "Assert \"hasCount\"");
  }

  @Override
  public void hasCSS(String name, String value, HasCSSOptions options) {
    ExpectedTextValue expected = new ExpectedTextValue();
    expected.string = value;
    hasCSS(name, expected, value, options);
  }

  @Override
  public void hasCSS(String name, Pattern pattern, HasCSSOptions options) {
    ExpectedTextValue expected = expectedRegex(pattern);
    hasCSS(name, expected, pattern, options);
  }

  private void hasCSS(String name, ExpectedTextValue expectedText, Object expectedValue, HasCSSOptions options) {
    if (options == null) {
      options = new HasCSSOptions();
    }
    FrameExpectOptions commonOptions = convertType(options, FrameExpectOptions.class);
    commonOptions.expressionArg = name;
    String message = "Locator expected to have CSS property '" + name + "'";
    if (expectedValue instanceof Pattern) {
      message += " matching regex";
    }
    expectImpl("to.have.css", expectedText, expectedValue, message, commonOptions, "Assert \"hasCSS\"");
  }

  @Override
  public void hasId(String id, HasIdOptions options) {
    ExpectedTextValue expected = new ExpectedTextValue();
    expected.string = id;
    expectImpl("to.have.id", expected, id, "Locator expected to have ID", convertType(options, FrameExpectOptions.class), "Assert \"hasId\"");
  }

  @Override
  public void hasId(Pattern pattern, HasIdOptions options) {
    ExpectedTextValue expected = expectedRegex(pattern);
    expectImpl("to.have.id", expected, pattern, "Locator expected to have ID matching regex", convertType(options, FrameExpectOptions.class), "Assert \"hasId\"");
  }

  @Override
  public void hasJSProperty(String name, Object value, HasJSPropertyOptions options) {
    if (options == null) {
      options = new HasJSPropertyOptions();
    }
    FrameExpectOptions commonOptions = convertType(options, FrameExpectOptions.class);
    commonOptions.expressionArg = name;
    commonOptions.expectedValue = serializeArgument(value);
    List<ExpectedTextValue> list = null;
    expectImpl("to.have.property", list, value, "Locator expected to have JavaScript property '" + name + "'", commonOptions, "Assert \"hasJSProperty\"");
  }

  @Override
  public void hasRole(AriaRole role, HasRoleOptions options) {
    ExpectedTextValue expected = new ExpectedTextValue();
    expected.string = role.toString().toLowerCase();
    expectImpl("to.have.role", expected, expected.string, "Locator expected to have role", convertType(options, FrameExpectOptions.class), "Assert \"hasRole\"");
  }

  @Override
  public void hasText(String text, HasTextOptions options) {
    ExpectedTextValue expected = new ExpectedTextValue();
    expected.string = text;
    expected.ignoreCase = shouldIgnoreCase(options);
    expected.matchSubstring = false;
    expected.normalizeWhiteSpace = true;
    expectImpl("to.have.text", expected, text, "Locator expected to have text", convertType(options, FrameExpectOptions.class), "Assert \"hasText\"");
  }

  @Override
  public void hasText(Pattern pattern, HasTextOptions options) {
    ExpectedTextValue expected = expectedRegex(pattern);
    expected.ignoreCase = shouldIgnoreCase(options);
    // Just match substring, same as containsText.
    expected.matchSubstring = true;
    expected.normalizeWhiteSpace = true;
    expectImpl("to.have.text", expected, pattern, "Locator expected to have text matching regex", convertType(options, FrameExpectOptions.class), "Assert \"hasText\"");
  }

  @Override
  public void hasText(String[] strings, HasTextOptions options) {
    List<ExpectedTextValue> list = new ArrayList<>();
    for (String text : strings) {
      ExpectedTextValue expected = new ExpectedTextValue();
      expected.string = text;
      expected.ignoreCase = shouldIgnoreCase(options);
      expected.matchSubstring = false;
      expected.normalizeWhiteSpace = true;
      list.add(expected);
    }
    expectImpl("to.have.text.array", list, strings, "Locator expected to have text", convertType(options, FrameExpectOptions.class), "Assert \"hasText\"");
  }

  @Override
  public void hasText(Pattern[] patterns, HasTextOptions options) {
    List<ExpectedTextValue> list = new ArrayList<>();
    for (Pattern pattern : patterns) {
      ExpectedTextValue expected = expectedRegex(pattern);
      expected.ignoreCase = shouldIgnoreCase(options);
      expected.matchSubstring = true;
      expected.normalizeWhiteSpace = true;
      list.add(expected);
    }
    expectImpl("to.have.text.array", list, patterns, "Locator expected to have text matching regex", convertType(options, FrameExpectOptions.class), "Assert \"hasText\"");
  }

  @Override
  public void hasValue(String value, HasValueOptions options) {
    ExpectedTextValue expected = new ExpectedTextValue();
    expected.string = value;
    expectImpl("to.have.value", expected, value, "Locator expected to have value", convertType(options, FrameExpectOptions.class), "Assert \"hasValue\"");
  }

  @Override
  public void hasValue(Pattern pattern, HasValueOptions options) {
    ExpectedTextValue expected = expectedRegex(pattern);
    expectImpl("to.have.value", expected, pattern, "Locator expected to have value matching regex", convertType(options, FrameExpectOptions.class), "Assert \"hasValue\"");
  }

  @Override
  public void hasValues(String[] values, HasValuesOptions options) {
    List<ExpectedTextValue> list = new ArrayList<>();
    for (String text : values) {
      ExpectedTextValue expected = new ExpectedTextValue();
      expected.string = text;
      list.add(expected);
    }
    expectImpl("to.have.values", list, values, "Locator expected to have values", convertType(options, FrameExpectOptions.class), "Assert \"hasValues\"");
  }

  @Override
  public void hasValues(Pattern[] patterns, HasValuesOptions options) {
    List<ExpectedTextValue> list = new ArrayList<>();
    for (Pattern pattern : patterns) {
      ExpectedTextValue expected = expectedRegex(pattern);
      expected.matchSubstring = true;
      list.add(expected);
    }
    expectImpl("to.have.values", list, patterns, "Locator expected to have values matching regex", convertType(options, FrameExpectOptions.class), "Assert \"hasValues\"");
  }

  @Override
  public void matchesAriaSnapshot(String expected, MatchesAriaSnapshotOptions snapshotOptions) {
    if (snapshotOptions == null) {
      snapshotOptions = new MatchesAriaSnapshotOptions();
    }
    FrameExpectOptions options = convertType(snapshotOptions, FrameExpectOptions.class);
    options.expectedValue = serializeArgument(expected);
    expectImpl("to.match.aria", options, expected,"Locator expected to match Aria snapshot", "Assert \"matchesAriaSnapshot\"");
  }

  @Override
  public void isChecked(IsCheckedOptions options) {
    if (options == null) {
      options = new IsCheckedOptions();
    }

    Map<String, Boolean> expectedValue = new HashMap<>();
    if (options.indeterminate != null) {
      expectedValue.put("indeterminate", options.indeterminate);
    }
    if (options.checked != null) {
      expectedValue.put("checked", options.checked);
    }

    String expected;
    if (options.indeterminate != null && options.indeterminate) {
      expected = "indeterminate";
    } else {
      boolean unchecked = options.checked != null && !options.checked;
      expected = unchecked ? "unchecked" : "checked";
    }

    String message = "Locator expected to be";
    FrameExpectOptions expectOptions = convertType(options, FrameExpectOptions.class);
    expectOptions.expectedValue = serializeArgument(expectedValue);
    expectImpl("to.be.checked", expectOptions, expected, message, "Assert \"isChecked\"");
  }

  @Override
  public void isDisabled(IsDisabledOptions options) {
    expectTrue("to.be.disabled", "Locator expected to be disabled", convertType(options, FrameExpectOptions.class), "Assert \"isDisabled\"");
  }

  @Override
  public void isEditable(IsEditableOptions options) {
    FrameExpectOptions frameOptions = convertType(options, FrameExpectOptions.class);
    boolean editable = options == null || options.editable == null || options.editable == true;
    String message = "Locator expected to be " + (editable ? "editable" : "readonly");
    expectTrue(editable ? "to.be.editable" : "to.be.readonly", message, frameOptions, "Assert \"isEditable\"");
  }

  @Override
  public void isEmpty(IsEmptyOptions options) {
    expectTrue("to.be.empty", "Locator expected to be empty", convertType(options, FrameExpectOptions.class), "Assert \"isEmpty\"");
  }

  @Override
  public void isEnabled(IsEnabledOptions options) {
    FrameExpectOptions frameOptions = convertType(options, FrameExpectOptions.class);
    boolean enabled = options == null || options.enabled == null || options.enabled == true;
    String message = "Locator expected to be " + (enabled ? "enabled" : "disabled");
    expectTrue(enabled ? "to.be.enabled" : "to.be.disabled", message, frameOptions, "Assert \"isEnabled\"");
  }

  @Override
  public void isFocused(IsFocusedOptions options) {
    expectTrue("to.be.focused", "Locator expected to be focused", convertType(options, FrameExpectOptions.class), "Assert \"isFocused\"");
  }

  @Override
  public void isHidden(IsHiddenOptions options) {
    expectTrue("to.be.hidden", "Locator expected to be hidden", convertType(options, FrameExpectOptions.class), "Assert \"isHidden\"");
  }

  @Override
  public void isInViewport(IsInViewportOptions options) {
    FrameExpectOptions expectOptions = convertType(options, FrameExpectOptions.class);
    if (options != null && options.ratio != null) {
      expectOptions.expectedNumber = options.ratio;
    }
    expectTrue("to.be.in.viewport", "Locator expected to be in viewport",  expectOptions, "Assert \"isInViewport\"");
  }

  @Override
  public void isVisible(IsVisibleOptions options) {
    FrameExpectOptions frameOptions = convertType(options, FrameExpectOptions.class);
    boolean visible = options == null || options.visible == null || options.visible == true;
    String message = "Locator expected to be " + (visible ? "visible" : "hidden");
    expectTrue(visible ? "to.be.visible" : "to.be.hidden", message, frameOptions, "Assert \"isVisible\"");
  }

  private void expectTrue(String expression, String message, FrameExpectOptions options, String title) {
    List<ExpectedTextValue> expectedText = null;
    expectImpl(expression, expectedText, null, message, options, title);
  }

  @Override
  public LocatorAssertions not() {
    return new LocatorAssertionsImpl(actualLocator, !isNot);
  }

  @Override
  public void isAttached(IsAttachedOptions options) {
    FrameExpectOptions frameOptions = convertType(options, FrameExpectOptions.class);
    boolean attached = options == null || options.attached == null || options.attached == true;
    String message = "Locator expected to be " + (attached ? "attached" : "detached");
    expectTrue(attached ? "to.be.attached" : "to.be.detached", message, frameOptions, "Assert \"isAttached\"");
  }
}
