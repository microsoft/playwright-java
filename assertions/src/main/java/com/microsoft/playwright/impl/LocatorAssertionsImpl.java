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
import org.opentest4j.AssertionFailedError;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.microsoft.playwright.impl.AssertionUtils.formatValue;
import static com.microsoft.playwright.impl.Serialization.serializeArgument;
import static com.microsoft.playwright.impl.Utils.convertViaJson;
import static java.util.Arrays.asList;

public class LocatorAssertionsImpl implements LocatorAssertions {
  private final LocatorImpl actual;
  private final boolean isNot;

  static class HasTextCommonOptions {
    public Object expressionArg;
    public SerializedArgument expectedValue;
    public Double timeout;
    public Boolean useInnerText;
    public Integer expectedNumber;
  }

  public LocatorAssertionsImpl(Locator locator) {
    this(locator, false);
  }

  private LocatorAssertionsImpl(Locator locator, boolean isNot) {
    this.actual = (LocatorImpl) locator;
    this.isNot = isNot;
  }

  @Override
  public void containsText(String text, ContainsTextOptions options) {
    ExpectedTextValue expected = new ExpectedTextValue();
    expected.string = text;
    expected.matchSubstring = true;
    expected.normalizeWhiteSpace = true;
    expectImpl("to.have.text", expected, text, "Locator expected to contain text", convertViaJson(options, HasTextCommonOptions.class));
  }

  @Override
  public void containsText(Pattern pattern, ContainsTextOptions options) {
    ExpectedTextValue expected = new ExpectedTextValue();
    expected.regexSource = pattern.pattern();
    // expected.regexFlags =
    expected.matchSubstring = true;
    expected.normalizeWhiteSpace = true;
    expectImpl("to.have.text", expected, pattern, "Locator expected to contain text", convertViaJson(options, HasTextCommonOptions.class));
  }

  @Override
  public void containsText(String[] strings, ContainsTextOptions options) {
    List<ExpectedTextValue> list = new ArrayList<>();
    for (String text : strings) {
      ExpectedTextValue expected = new ExpectedTextValue();
      expected.string = text;
      expected.matchSubstring = true;
      expected.normalizeWhiteSpace = true;
      list.add(expected);
    }
    expectImpl("to.contain.text.array", list, strings, "Locator expected to contain text", convertViaJson(options, HasTextCommonOptions.class));
  }

  @Override
  public void containsText(Pattern[] patterns, ContainsTextOptions options) {
    List<ExpectedTextValue> list = new ArrayList<>();
    for (Pattern pattern : patterns) {
      ExpectedTextValue expected = new ExpectedTextValue();
      expected.regexSource = pattern.pattern();
      expected.matchSubstring = true;
      expected.normalizeWhiteSpace = true;
      list.add(expected);
    }
    expectImpl("to.contain.text.array", list, patterns, "Locator expected to contain text", convertViaJson(options, HasTextCommonOptions.class));
  }

  @Override
  public void hasAttribute(String name, String text, HasAttributeOptions options) {
    ExpectedTextValue expected = new ExpectedTextValue();
    expected.string = text;
    hasAttribute(name, expected, text, options);
  }

  @Override
  public void hasAttribute(String name, Pattern pattern, HasAttributeOptions options) {
    ExpectedTextValue expected = new ExpectedTextValue();
    expected.regexSource = pattern.pattern();
    hasAttribute(name, expected, pattern, options);
  }

  private void hasAttribute(String name, ExpectedTextValue expectedText, Object expectedValue, HasAttributeOptions options) {
    if (options == null) {
      options = new HasAttributeOptions();
    }
    HasTextCommonOptions commonOptions = convertViaJson(options, HasTextCommonOptions.class);
    commonOptions.expressionArg = name;
    expectImpl("to.have.attribute", expectedText, expectedValue, "Locator expected to have attribute", commonOptions);
  }

  @Override
  public void hasClass(String text, HasClassOptions options) {
    ExpectedTextValue expected = new ExpectedTextValue();
    expected.string = text;
    expectImpl("to.have.class", expected, text, "Locator expected to have class", convertViaJson(options, HasTextCommonOptions.class));
  }

  @Override
  public void hasClass(Pattern pattern, HasClassOptions options) {
    ExpectedTextValue expected = new ExpectedTextValue();
    expected.regexSource = pattern.pattern();
    expectImpl("to.have.class", expected, pattern, "Locator expected to have class", convertViaJson(options, HasTextCommonOptions.class));
  }

  @Override
  public void hasClass(String[] strings, HasClassOptions options) {
    List<ExpectedTextValue> list = new ArrayList<>();
    for (String text : strings) {
      ExpectedTextValue expected = new ExpectedTextValue();
      expected.string = text;
      list.add(expected);
    }
    expectImpl("to.have.class.array", list, strings, "Locator expected to have class", convertViaJson(options, HasTextCommonOptions.class));
  }

  @Override
  public void hasClass(Pattern[] patterns, HasClassOptions options) {
    List<ExpectedTextValue> list = new ArrayList<>();
    for (Pattern pattern : patterns) {
      ExpectedTextValue expected = new ExpectedTextValue();
      expected.regexSource = pattern.pattern();
      list.add(expected);
    }
    expectImpl("to.have.class.array", list, patterns, "Locator expected to have class", convertViaJson(options, HasTextCommonOptions.class));
  }

  @Override
  public void hasCount(int count, HasCountOptions options) {
    if (options == null) {
      options = new HasCountOptions();
    }
    HasTextCommonOptions commonOptions = convertViaJson(options, HasTextCommonOptions.class);
    commonOptions.expectedNumber = count;
    List<ExpectedTextValue> expectedText = null;
    expectImpl("to.have.count", expectedText, count, "Locator expected to have count", commonOptions);
  }

  @Override
  public void hasCSS(String name, String value, HasCSSOptions options) {
    ExpectedTextValue expected = new ExpectedTextValue();
    expected.string = value;
    hasCSS(name, expected, value, options);
  }

  @Override
  public void hasCSS(String name, Pattern value, HasCSSOptions options) {
    ExpectedTextValue expected = new ExpectedTextValue();
    expected.regexSource = value.pattern();
    hasCSS(name, expected, value, options);
  }

  private void hasCSS(String name, ExpectedTextValue expectedText, Object expectedValue, HasCSSOptions options) {
    if (options == null) {
      options = new HasCSSOptions();
    }
    HasTextCommonOptions commonOptions = convertViaJson(options, HasTextCommonOptions.class);
    commonOptions.expressionArg = name;
    expectImpl("to.have.css", expectedText, expectedValue, "Locator expected to have CSS", commonOptions);
  }

  @Override
  public void hasId(String id, HasIdOptions options) {
    ExpectedTextValue expected = new ExpectedTextValue();
    expected.string = id;
    expectImpl("to.have.id", expected, id, "Locator expected to have ID", convertViaJson(options, HasTextCommonOptions.class));
  }

  @Override
  public void hasJSProperty(String name, Object value, HasJSPropertyOptions options) {
    if (options == null) {
      options = new HasJSPropertyOptions();
    }
    HasTextCommonOptions commonOptions = convertViaJson(options, HasTextCommonOptions.class);
    commonOptions.expressionArg = name;
    commonOptions.expectedValue = serializeArgument(value);
    List<ExpectedTextValue> list = null;
    expectImpl("to.have.property", list, value, "Locator expected to have js property", commonOptions);
  }

  @Override
  public void hasText(String text, HasTextOptions options) {
    ExpectedTextValue expected = new ExpectedTextValue();
    expected.string = text;
    expected.matchSubstring = false;
    expected.normalizeWhiteSpace = true;
    expectImpl("to.have.text", expected, text, "Locator expected to have text", convertViaJson(options, HasTextCommonOptions.class));
  }

  @Override
  public void hasText(Pattern pattern, HasTextOptions options) {
    ExpectedTextValue expected = new ExpectedTextValue();
    expected.string = pattern.pattern();
    // Just match substring, same as containsText.
    expected.matchSubstring = true;
    expected.normalizeWhiteSpace = true;
    expectImpl("to.have.text", expected, pattern, "Locator expected to have text", convertViaJson(options, HasTextCommonOptions.class));
  }

  @Override
  public void hasText(String[] strings, HasTextOptions options) {
    List<ExpectedTextValue> list = new ArrayList<>();
    for (String text : strings) {
      ExpectedTextValue expected = new ExpectedTextValue();
      expected.string = text;
      expected.matchSubstring = false;
      expected.normalizeWhiteSpace = true;
      list.add(expected);
    }
    expectImpl("to.have.text.array", list, strings, "Locator expected to have text", convertViaJson(options, HasTextCommonOptions.class));
  }

  @Override
  public void hasText(Pattern[] patterns, HasTextOptions options) {
    List<ExpectedTextValue> list = new ArrayList<>();
    for (Pattern pattern : patterns) {
      ExpectedTextValue expected = new ExpectedTextValue();
      expected.regexSource = pattern.pattern();
      expected.matchSubstring = true;
      expected.normalizeWhiteSpace = true;
      list.add(expected);
    }
    expectImpl("to.have.text.array", list, patterns, "Locator expected to have text", convertViaJson(options, HasTextCommonOptions.class));
  }

  @Override
  public void hasValue(String value, HasValueOptions options) {
    ExpectedTextValue expected = new ExpectedTextValue();
    expected.string = value;
    expectImpl("to.have.value", expected, value, "Locator expected to have value", convertViaJson(options, HasTextCommonOptions.class));
  }

  @Override
  public void hasValue(Pattern pattern, HasValueOptions options) {
    ExpectedTextValue expected = new ExpectedTextValue();
    expected.regexSource = pattern.pattern();
    expectImpl("to.have.value", expected, pattern, "Locator expected to have value", convertViaJson(options, HasTextCommonOptions.class));
  }

  @Override
  public void isChecked(IsCheckedOptions options) {
    expectTrue("to.be.checked", "Locator expected to be checked", convertViaJson(options, HasTextCommonOptions.class));
  }

  @Override
  public void isDisabled(IsDisabledOptions options) {
    expectTrue("to.be.disabled", "Locator expected to be disabled", convertViaJson(options, HasTextCommonOptions.class));
  }

  @Override
  public void isEditable(IsEditableOptions options) {
    expectTrue("to.be.editable", "Locator expected to be editable", convertViaJson(options, HasTextCommonOptions.class));
  }

  @Override
  public void isEmpty(IsEmptyOptions options) {
    expectTrue("to.be.empty", "Locator expected to be empty", convertViaJson(options, HasTextCommonOptions.class));
  }

  @Override
  public void isEnabled(IsEnabledOptions options) {
    expectTrue("to.be.enabled", "Locator expected to be enabled", convertViaJson(options, HasTextCommonOptions.class));
  }

  @Override
  public void isFocused(IsFocusedOptions options) {
    expectTrue("to.be.focused", "Locator expected to be focused", convertViaJson(options, HasTextCommonOptions.class));
  }

  @Override
  public void isHidden(IsHiddenOptions options) {
    expectTrue("to.be.hidden", "Locator expected to be hidden", convertViaJson(options, HasTextCommonOptions.class));
  }

  @Override
  public void isVisible(IsVisibleOptions options) {
    expectTrue("to.be.visible", "Locator expected to be visible", convertViaJson(options, HasTextCommonOptions.class));
  }

  private void expectImpl(String expression, ExpectedTextValue textValue, Object expected, String message, HasTextCommonOptions options) {
    expectImpl(expression, asList(textValue), expected, message, options);
  }

  private void expectTrue(String expression, String message, HasTextCommonOptions options) {
    List<ExpectedTextValue> expectedText = null;
    expectImpl(expression, expectedText, null, message, options);
  }

  private void expectImpl(String expression, List<ExpectedTextValue> expectedText, Object expected, String message, HasTextCommonOptions options) {
    if (options == null) {
      options = new HasTextCommonOptions();
    }
    FrameExpectOptions expectOptions = new FrameExpectOptions();
    expectOptions.expressionArg = options.expressionArg;
    expectOptions.expectedNumber = options.expectedNumber;
    expectOptions.expectedValue = options.expectedValue;
    expectOptions.expectedText = expectedText;
    expectOptions.isNot = isNot;
    expectOptions.timeout = options.timeout;
    expectOptions.useInnerText = options.useInnerText;
    expectImpl(expression, expectOptions, expected, message);
  }

  private void expectImpl(String expression, FrameExpectOptions expectOptions, Object expected, String message) {
    FrameExpectResult result = actual.expect(expression, expectOptions);
    if (result.matches == isNot) {
      Object actual = result.received == null ? null : Serialization.deserialize(result.received);
      String log = String.join("\n", result.log);
      if (!log.isEmpty()) {
        log = "\nCall log:\n" + log;
      }
      if (expected == null) {
        throw new AssertionFailedError(message + log);
      }
      throw new AssertionFailedError(message + log, formatValue(expected), formatValue(actual));
    }
  }

  @Override
  public LocatorAssertions not() {
    return new LocatorAssertionsImpl(actual, !isNot);
  }
}

