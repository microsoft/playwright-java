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

// Port of toMatchText.ts.
package com.microsoft.playwright.impl;

import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.impl.AssertionsBase.ExpectMatcherState;
import com.microsoft.playwright.impl.AssertionsBase.Query;
import com.microsoft.playwright.impl.MatcherHint.MatcherMessageDetails;
import com.microsoft.playwright.impl.MatcherHint.MatcherResult;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.microsoft.playwright.impl.JestMatcherUtils.EXPECTED_COLOR;
import static com.microsoft.playwright.impl.JestMatcherUtils.printDiffOrStringify;
import static com.microsoft.playwright.impl.JestMatcherUtils.printExpected;
import static com.microsoft.playwright.impl.JestMatcherUtils.printWithType;
import static com.microsoft.playwright.impl.MatcherHint.formatMatcherMessage;
import static com.microsoft.playwright.impl.MatcherHint.printReceivedStringContainExpectedResult;
import static com.microsoft.playwright.impl.MatcherHint.printReceivedStringContainExpectedSubstring;

class ToMatchText {
  // java: the receiver is a LocatorImpl or a PageImpl, expected is a String or a Pattern; the options are their
  // timeout and matchSubstring, there is no AbortSignal.
  static MatcherResult toMatchText(
    ExpectMatcherState state,
    String matcherName,
    Object receiver,
    String receiverType,
    Query query,
    Object expected,
    Double optionsTimeout,
    boolean matchSubstring) {
    LocatorImpl locator = "Locator".equals(receiverType) ? (LocatorImpl) receiver : null;

    if (
      !(expected instanceof String) &&
      !(expected instanceof Pattern)
    ) {
      String errorMessage = "Error: " + EXPECTED_COLOR("expected") + " value must be a string or regular expression\n" + printWithType("Expected", expected, JestMatcherUtils::printExpected);
      MatcherMessageDetails details = new MatcherMessageDetails();
      details.promise = state.promise;
      details.isNot = state.isNot;
      details.locator = locator == null ? null : locator.toString();
      details.matcherName = matcherName;
      details.expectation = "expected";
      details.errorMessage = errorMessage;
      throw new PlaywrightException(formatMatcherMessage(details));
    }

    double timeout = optionsTimeout != null ? optionsTimeout : state.timeout;

    FrameExpectResult queryResult = query.query(state.isNot, timeout);
    boolean pass = queryResult.matches;
    FrameExpectResult.ExpectReceived received = queryResult.received;
    List<String> log = queryResult.log;
    Boolean timedOut = queryResult.timedOut;
    String errorMessage = queryResult.errorMessage;

    if (pass == !state.isNot) {
      MatcherResult result = new MatcherResult();
      result.name = matcherName;
      result.message = () -> "";
      result.pass = pass;
      result.expected = expected;
      return result;
    }

    String expectedSuffix = expected instanceof String ? (matchSubstring ? " substring" : "") : " pattern";
    String receivedSuffix = expected instanceof String ? (matchSubstring ? " string" : "") : " string";
    // java: a received value that is not a string keeps its text.
    String receivedValue = received == null || received.value == null || received.value == JestGetType.UNDEFINED ? null : String.valueOf(received.value);
    String receivedString = receivedValue == null ? "" : receivedValue;
    String printedReceived = null;
    String printedExpected = null;
    String printedDiff = null;
    if (pass) {
      if (expected instanceof String) {
        printedExpected = "Expected" + expectedSuffix + ": not " + printExpected(expected);
        if (errorMessage == null) {
          String formattedReceived = printReceivedStringContainExpectedSubstring(receivedString, receivedString.indexOf((String) expected), ((String) expected).length());
          printedReceived = "Received" + receivedSuffix + ": " + formattedReceived;
        }
      } else {
        printedExpected = "Expected" + expectedSuffix + ": not " + printExpected(expected);
        if (errorMessage == null) {
          Matcher matcher = ((Pattern) expected).matcher(receivedString);
          String formattedReceived = printReceivedStringContainExpectedResult(receivedString, matcher.find() ? matcher : null);
          printedReceived = "Received" + receivedSuffix + ": " + formattedReceived;
        }
      }
    } else {
      if (errorMessage != null)
        printedExpected = "Expected" + expectedSuffix + ": " + printExpected(expected);
      else
        printedDiff = printDiffOrStringify(expected, receivedString, "Expected" + expectedSuffix, "Received" + receivedSuffix, false);
    }

    String finalPrintedExpected = printedExpected;
    String finalPrintedReceived = printedReceived;
    String finalPrintedDiff = printedDiff;
    MatcherResult result = new MatcherResult();
    result.message = () -> {
      MatcherMessageDetails details = new MatcherMessageDetails();
      details.promise = state.promise;
      details.isNot = state.isNot;
      details.matcherName = matcherName;
      details.expectation = "expected";
      details.locator = locator == null ? null : locator.toString();
      details.timeout = timeout;
      details.timedOut = timedOut;
      details.printedExpected = finalPrintedExpected;
      details.printedReceived = finalPrintedReceived;
      details.printedDiff = finalPrintedDiff;
      details.log = log;
      details.errorMessage = errorMessage;
      return formatMatcherMessage(details);
    };

    result.name = matcherName;
    result.expected = expected;
    result.pass = pass;
    result.actual = receivedValue;
    result.log = log;
    result.timeout = timedOut != null && timedOut ? timeout : null;
    result.ariaSnapshot = received == null ? null : received.ariaSnapshot;
    return result;
  }
}
