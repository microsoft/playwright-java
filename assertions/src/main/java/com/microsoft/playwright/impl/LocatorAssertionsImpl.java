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
import static com.microsoft.playwright.impl.Utils.convertViaJson;
import static java.util.Arrays.asList;

public class LocatorAssertionsImpl implements LocatorAssertions {
  private final LocatorImpl actual;
  private final boolean isNot;

  static class HasTextCommonOptions {
    public Object expressionArg;
    public Double timeout;
    public Boolean useInnerText;
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
  public void hasClass(String expected, HasClassOptions options) {

  }

  @Override
  public void hasClass(Pattern expected, HasClassOptions options) {

  }

  @Override
  public void hasClass(String[] expected, HasClassOptions options) {

  }

  @Override
  public void hasClass(Pattern[] expected, HasClassOptions options) {

  }

  @Override
  public void hasCount(int count, HasCountOptions options) {

  }

  @Override
  public void hasCSS(String name, String value, HasCSSOptions options) {

  }

  @Override
  public void hasCSS(String name, Pattern value, HasCSSOptions options) {

  }

  @Override
  public void hasId(String id, HasIdOptions options) {

  }

  @Override
  public void hasJSProperty(String name, Object value, HasJSPropertyOptions options) {

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

  }

  @Override
  public void hasValue(Pattern value, HasValueOptions options) {

  }

  @Override
  public void isChecked(IsCheckedOptions options) {

  }

  @Override
  public void isDisabled(IsDisabledOptions options) {

  }

  @Override
  public void isEditable(IsEditableOptions options) {

  }

  @Override
  public void isEmpty(IsEmptyOptions options) {

  }

  @Override
  public void isEnabled(IsEnabledOptions options) {

  }

  @Override
  public void isFocused(IsFocusedOptions options) {

  }

  @Override
  public void isHidden(IsHiddenOptions options) {

  }

  @Override
  public void isVisible(IsVisibleOptions options) {

  }

  private void expectImpl(String expression, ExpectedTextValue textValue, Object expected, String message, HasTextCommonOptions options) {
    expectImpl(expression, asList(textValue), expected, message, options);
  }

  private void expectImpl(String expression, List<ExpectedTextValue> expectedText, Object expected, String message, HasTextCommonOptions options) {
    if (options == null) {
      options = new HasTextCommonOptions();
    }
    FrameExpectOptions expectOptions = new FrameExpectOptions();
    expectOptions.expressionArg = options.expressionArg;
    expectOptions.expectedText = expectedText;
    expectOptions.isNot = isNot;
    expectOptions.timeout = options.timeout;
    expectOptions.useInnerText = options.useInnerText;
    expectImpl(expression, expectOptions, expected, message);
  }

  private void expectImpl(String expression, ExpectedTextValue textValue, Object expected, String message, Double timeout) {
  }

  private void expectImpl(String expression, FrameExpectOptions expectOptions, Object expected, String message) {
    FrameExpectResult result = actual.expect(expression, expectOptions);
    if (result.matches == isNot) {
      Object actual = result.received == null ? null : Serialization.deserialize(result.received);
      String log = String.join("\n", result.log);
      if (!log.isEmpty()) {
        log = "\nCall log:\n" + log;
      }
      throw new AssertionFailedError(message + log, formatValue(expected), formatValue(actual));
    }
  }

  @Override
  public LocatorAssertions not() {
    return new LocatorAssertionsImpl(actual, !isNot);
  }
}

