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

// Port of jest-matcher-utils.
// java: the original is MIT licensed, Copyright (c) Meta Platforms, Inc. and affiliates.
package com.microsoft.playwright.impl;

import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.microsoft.playwright.impl.JestDiff.DIFF_DELETE;
import static com.microsoft.playwright.impl.JestDiff.DIFF_EQUAL;
import static com.microsoft.playwright.impl.JestDiff.DIFF_INSERT;
import static com.microsoft.playwright.impl.JestDiff.diffStringsRaw;
import static com.microsoft.playwright.impl.JestDiff.diffStringsUnified;
import static com.microsoft.playwright.impl.JestGetType.getType;
import static com.microsoft.playwright.impl.JestGetType.isPrimitive;

// java: the ensure functions, matcherHint, matcherErrorMessage, pluralize, highlightTrailingWhitespace,
// diff and shouldPrintDiff are not ported, the assertions do not call them.
class JestMatcherUtils {
  // java: no ANSI colors, the color functions return the text as is.
  static String EXPECTED_COLOR(String text) {
    return JestDiff.Chalk.green(text);
  }

  static String RECEIVED_COLOR(String text) {
    return JestDiff.Chalk.red(text);
  }

  static String INVERTED_COLOR(String text) {
    return JestDiff.Chalk.inverse(text);
  }

  static String BOLD_WEIGHT(String text) {
    return JestDiff.Chalk.bold(text);
  }

  static String DIM_COLOR(String text) {
    return JestDiff.Chalk.dim(text);
  }

  private static final Pattern MULTILINE_REGEXP = Pattern.compile("\\n");
  private static final String SPACE_SYMBOL = "·";

  static String stringify(Object object) {
    return stringify(object, 10, 10);
  }

  static String stringify(Object object, int maxDepth, int maxWidth) {
    int MAX_LENGTH = 10_000;
    String result;
    try {
      PrettyFormat.Options options = new PrettyFormat.Options();
      options.maxDepth = maxDepth;
      options.maxWidth = maxWidth;
      options.min = true;
      result = PrettyFormat.format(object, options);
    } catch (RuntimeException e) {
      PrettyFormat.Options options = new PrettyFormat.Options();
      options.callToJSON = false;
      options.maxDepth = maxDepth;
      options.maxWidth = maxWidth;
      options.min = true;
      result = PrettyFormat.format(object, options);
    }
    if (result.length() >= MAX_LENGTH && maxDepth > 1) {
      return stringify(object, maxDepth / 2, maxWidth);
    } else if (result.length() >= MAX_LENGTH && maxWidth > 1) {
      return stringify(object, maxDepth, maxWidth / 2);
    } else {
      return result;
    }
  }

  // java: $ with the m flag in JavaScript is the position before \n, \r,  ,   or the
  // end of the string. $ with MULTILINE in Java does not match between \r and \n, and it
  // matches before \u0085.
  private static final Pattern trailingSpacesRegex = Pattern.compile(JestDiff.WHITESPACE + "+(?=[\\n\\r\\u2028\\u2029]|\\z)");

  private static String replaceTrailingSpaces(String text) {
    // java: replaceAll with a callback is a loop over the matches.
    Matcher matcher = trailingSpacesRegex.matcher(text);
    StringBuffer result = new StringBuffer();
    while (matcher.find()) {
      String spaces = matcher.group();
      matcher.appendReplacement(result, Matcher.quoteReplacement(repeat(SPACE_SYMBOL, spaces.length())));
    }
    matcher.appendTail(result);
    return result.toString();
  }

  // java: helper with no counterpart upstream, the port of String.prototype.repeat.
  private static String repeat(String string, int count) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < count; i++) {
      result.append(string);
    }
    return result.toString();
  }

  static String printReceived(Object object) {
    return RECEIVED_COLOR(replaceTrailingSpaces(stringify(object)));
  }

  static String printExpected(Object value) {
    return EXPECTED_COLOR(replaceTrailingSpaces(stringify(value)));
  }

  static String printWithType(String name, Object value, Function<Object, String> print) {
    String type = getType(value);
    String hasType = !type.equals("null") && !type.equals("undefined") ? name + " has type:  " + type + "\n" : "";
    String hasValue = name + " has value: " + print.apply(value);
    return hasType + hasValue;
  }

  private static String getCommonAndChangedSubstrings(List<JestDiff.Diff> diffs, int op, boolean hasCommonDiff) {
    String reduced = "";
    for (JestDiff.Diff diff : diffs) {
      reduced = reduced + (diff.op == DIFF_EQUAL ? diff.text : diff.op == op ? hasCommonDiff ? INVERTED_COLOR(diff.text) : diff.text : "");
    }
    return reduced;
  }

  private static boolean isLineDiffable(Object expected, Object received) {
    String expectedType = getType(expected);
    String receivedType = getType(received);
    if (!expectedType.equals(receivedType)) {
      return false;
    }
    if (isPrimitive(expected)) {
      return expected instanceof String && received instanceof String && ((String) expected).length() > 0 && ((String) received).length() > 0 && (MULTILINE_REGEXP.matcher((String) expected).find() || MULTILINE_REGEXP.matcher((String) received).find());
    }
    if (expectedType.equals("date") || expectedType.equals("function") || expectedType.equals("regexp")) {
      return false;
    }
    // java: the value model has no Error values and no asymmetric matchers, these two
    // checks are not ported.
    return true;
  }

  private static final int MAX_DIFF_STRING_LENGTH = 20_000;

  static String printDiffOrStringify(Object expected, Object received, String expectedLabel, String receivedLabel, boolean expand) {
    if (expected instanceof String && received instanceof String && ((String) expected).length() > 0 && ((String) received).length() > 0 && ((String) expected).length() <= MAX_DIFF_STRING_LENGTH && ((String) received).length() <= MAX_DIFF_STRING_LENGTH && !expected.equals(received)) {
      if (((String) expected).contains("\n") || ((String) received).contains("\n")) {
        JestDiff.DiffOptions options = new JestDiff.DiffOptions();
        options.aAnnotation = expectedLabel;
        options.bAnnotation = receivedLabel;
        options.changeLineTrailingSpaceColor = JestDiff.Chalk::bgYellow;
        options.commonLineTrailingSpaceColor = JestDiff.Chalk::bgYellow;
        options.emptyFirstOrLastLinePlaceholder = "↵";
        options.expand = expand;
        options.includeChangeCounts = true;
        return diffStringsUnified((String) expected, (String) received, options);
      }
      List<JestDiff.Diff> diffs = diffStringsRaw((String) expected, (String) received, true);
      boolean hasCommonDiff = false;
      for (JestDiff.Diff diff : diffs) {
        if (diff.op == DIFF_EQUAL) {
          hasCommonDiff = true;
        }
      }
      Function<String, String> printLabel = getLabelPrinter(expectedLabel, receivedLabel);
      String expectedLine = printLabel.apply(expectedLabel) + printExpected(getCommonAndChangedSubstrings(diffs, DIFF_DELETE, hasCommonDiff));
      String receivedLine = printLabel.apply(receivedLabel) + printReceived(getCommonAndChangedSubstrings(diffs, DIFF_INSERT, hasCommonDiff));
      return expectedLine + "\n" + receivedLine;
    }
    if (isLineDiffable(expected, received)) {
      // java: there are no asymmetric matchers, replaceMatchedToAsymmetricMatcher returns
      // the values as they are and is not ported.
      Object replacedExpected = expected;
      Object replacedReceived = received;
      JestDiff.DiffOptions options = new JestDiff.DiffOptions();
      options.aAnnotation = expectedLabel;
      options.bAnnotation = receivedLabel;
      options.expand = expand;
      options.includeChangeCounts = true;
      String difference = JestDiff.diff(replacedExpected, replacedReceived, options);
      if (difference != null && difference.contains("- " + expectedLabel) && difference.contains("+ " + receivedLabel)) {
        return difference;
      }
    }
    Function<String, String> printLabel = getLabelPrinter(expectedLabel, receivedLabel);
    String expectedLine = printLabel.apply(expectedLabel) + printExpected(expected);
    String receivedLine = printLabel.apply(receivedLabel) + (stringify(expected).equals(stringify(received)) ? "serializes to the same string" : printReceived(received));
    return expectedLine + "\n" + receivedLine;
  }

  static Function<String, String> getLabelPrinter(String... strings) {
    int maxLength = 0;
    for (String string : strings) {
      maxLength = Math.max(string.length(), maxLength);
    }
    int finalMaxLength = maxLength;
    return string -> string + ": " + repeat(" ", finalMaxLength - string.length());
  }
}
