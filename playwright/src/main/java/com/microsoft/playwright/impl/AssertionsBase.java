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

import com.microsoft.playwright.PlaywrightException;
import org.opentest4j.AssertionFailedError;
import org.opentest4j.ValueWrapper;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.microsoft.playwright.impl.Utils.toJsRegexFlags;
import static java.util.Arrays.asList;

class AssertionsBase {
  final LocatorImpl actualLocator;
  final boolean isNot;

  AssertionsBase(LocatorImpl actual, boolean isNot) {
    this.actualLocator = actual;
    this.isNot = isNot;
  }

  void expectImpl(String expression, ExpectedTextValue textValue, Object expected, String message, FrameExpectOptions options) {
    expectImpl(expression, asList(textValue), expected, message, options);
  }

  void expectImpl(String expression, List<ExpectedTextValue> expectedText, Object expected, String message, FrameExpectOptions options) {
    if (options == null) {
      options = new FrameExpectOptions();
    }
    options.expectedText = expectedText;
    options.isNot = isNot;
    expectImpl(expression, options, expected, message);
  }

  void expectImpl(String expression, FrameExpectOptions expectOptions, Object expected, String message) {
    if (expectOptions.timeout == null) {
      expectOptions.timeout = AssertionsTimeout.defaultTimeout;
    }
    if (expectOptions.isNot) {
      message = message.replace("expected to", "expected not to");
    }
    FrameExpectResult result = actualLocator.expect(expression, expectOptions);
    if (result.matches == isNot) {
      Object actual = result.received == null ? null : Serialization.deserialize(result.received);
      String log = String.join("\n", result.log);
      if (!log.isEmpty()) {
        log = "\nCall log:\n" + log;
      }
      if (expected == null) {
        throw new AssertionFailedError(message + log);
      }
      ValueWrapper expectedValue = formatValue(expected);
      ValueWrapper actualValue = formatValue(actual);
      message += ": " + expectedValue.getStringRepresentation() + "\nReceived: " + actualValue.getStringRepresentation() + "\n";
      throw new AssertionFailedError(message + log, expectedValue, actualValue);
    }
  }

  private static ValueWrapper formatValue(Object value) {
    if (value == null || !value.getClass().isArray()) {
      return ValueWrapper.create(value);
    }
    Collection<String> values = asList((Object[]) value).stream().map(e -> e.toString()).collect(Collectors.toList());
    String stringRepresentation = "[" + String.join(", ", values) + "]";
    return ValueWrapper.create(value, stringRepresentation);
  }

  static ExpectedTextValue expectedRegex(Pattern pattern) {
    ExpectedTextValue expected = new ExpectedTextValue();
    expected.regexSource = pattern.pattern();
    if (pattern.flags() != 0) {
      expected.regexFlags = toJsRegexFlags(pattern);
    }
    return expected;
  }
}
