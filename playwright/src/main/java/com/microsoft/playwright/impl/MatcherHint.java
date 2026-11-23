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

// Port of matcherHint.ts.
package com.microsoft.playwright.impl;

import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Matcher;

import static com.microsoft.playwright.impl.MatcherUtils.numberToString;
import static com.microsoft.playwright.impl.MatcherUtils.printReceived;

class MatcherHint {
  // java: no attachments, suggestedRebaseline, softError and shouldNotRetryTest, they belong to the test runner.
  static class MatcherResult {
    String name;
    Object expected;
    Supplier<String> message;
    boolean pass;
    Object actual;
    List<String> log;
    Double timeout;
    String ariaSnapshot;
  }

  // java: no ExpectError, AssertionsBase throws an AssertionFailedError of opentest4j.

  // java: no expectTypes, the receiver of a matcher is typed.

  // java: InternalMatcherUtils is MatcherUtils, its methods are static; there are no colors.

  private static String printSubstring(String val) {
    return val.replaceAll("\"|\\\\", "\\\\$0");
  }

  static String printReceivedStringContainExpectedSubstring(String received, int start, int length) {
    return "\"" +
      printSubstring(slice(received, 0, start)) +
      printSubstring(slice(received, start, start + length)) +
      printSubstring(slice(received, start + length, received.length())) +
      "\"";
  }

  // java: result is the Matcher after a successful find(), or null.
  static String printReceivedStringContainExpectedResult(String received, Matcher result) {
    return result == null
      ? printReceived(received)
      : printReceivedStringContainExpectedSubstring(
        received,
        result.start(),
        result.group().length());
  }

  static class MatcherMessageDetails {
    String promise;
    boolean isNot;
    String receiver;
    String matcherName;
    String expectation;
    String locator;
    String printedExpected;
    String printedReceived;
    String printedDiff;
    Boolean timedOut;
    Double timeout;
    String errorMessage;
    List<String> log;
  }

  static String formatMatcherMessage(MatcherMessageDetails details) {
    String receiver = details.receiver != null ? details.receiver : (details.locator != null && !details.locator.isEmpty() ? "locator" : "page");
    // java: the call is the Java one: assertThat(locator).not().hasText(expected).
    String message = "assertThat(" + receiver
      + ")" + (details.promise != null && !details.promise.isEmpty() ? "." + details.promise : "") + (details.isNot ? ".not()" : "") + "."
      + details.matcherName
      + "(" + details.expectation + ")"
      + " failed\n\n";

    String[] diffLines = details.printedDiff == null ? null : details.printedDiff.split("\n", -1);
    if (diffLines != null && diffLines.length == 2) {
      details.printedExpected = diffLines[0];
      details.printedReceived = diffLines[1];
      details.printedDiff = null;
    }

    boolean align = isEmpty(details.errorMessage) && details.printedExpected != null && details.printedExpected.startsWith("Expected:")
      && (isEmpty(details.printedReceived) || details.printedReceived.startsWith("Received:"));
    if (!isEmpty(details.locator))
      message += "Locator: " + (align ? " " : "") + details.locator + "\n";
    if (!isEmpty(details.printedExpected))
      message += details.printedExpected + "\n";
    if (!isEmpty(details.printedReceived))
      message += details.printedReceived + "\n";
    if (details.timedOut != null && details.timedOut && details.timeout != null && details.timeout != 0)
      message += "Timeout: " + (align ? " " : "") + numberToString(details.timeout) + "ms\n";
    if (!isEmpty(details.printedDiff))
      message += details.printedDiff + "\n";
    if (!isEmpty(details.errorMessage)) {
      message += details.errorMessage;
      if (!details.errorMessage.endsWith("\n"))
        message += "\n";
    }
    message += callLogText(details.log);
    return message;
  }

  static String callLogText(List<String> log) {
    if (log == null || !log.stream().anyMatch(l -> !isEmpty(l)))
      return "";
    return "\nCall log:\n" + (String.join("\n", log)) + "\n";
  }

  // java: helpers with no counterpart upstream: String.prototype.slice, and the truthiness of a string.
  private static String slice(String string, int start, int end) {
    int length = string.length();
    int from = start < 0 ? Math.max(length + start, 0) : Math.min(start, length);
    int to = end < 0 ? Math.max(length + end, 0) : Math.min(end, length);
    return from < to ? string.substring(from, to) : "";
  }

  private static boolean isEmpty(String string) {
    return string == null || string.isEmpty();
  }
}
