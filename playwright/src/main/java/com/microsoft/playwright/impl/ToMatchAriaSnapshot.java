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

// Port of toMatchAriaSnapshot.ts.
package com.microsoft.playwright.impl;

import com.microsoft.playwright.impl.AssertionsBase.ExpectMatcherState;
import com.microsoft.playwright.impl.MatcherHint.MatcherMessageDetails;
import com.microsoft.playwright.impl.MatcherHint.MatcherResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.microsoft.playwright.impl.MatcherUtils.printExpectedAndReceived;
import static com.microsoft.playwright.impl.MatcherUtils.printExpected;
import static com.microsoft.playwright.impl.MatcherHint.formatMatcherMessage;
import static com.microsoft.playwright.impl.MatcherHint.printReceivedStringContainExpectedSubstring;

class ToMatchAriaSnapshot {
  // java: the receiver is a LocatorImpl or a PageImpl, expected is the snapshot text, the options are their timeout.
  @SuppressWarnings("unchecked")
  static MatcherResult toMatchAriaSnapshot(
    ExpectMatcherState state,
    Object receiver,
    String expectedParam,
    Double optionsTimeout) {
    String matcherName = "matchesAriaSnapshot";
    LocatorImpl locator = receiver instanceof PageImpl ? null : (LocatorImpl) receiver;

    // java: there is no testInfo: no ignoreSnapshots, no updateSnapshots, no snapshot file, no baseline
    // generation and no global children option.
    String expected;
    double timeout;
    expected = expectedParam;
    timeout = optionsTimeout != null ? optionsTimeout : state.timeout;

    expected = unshift(expected);

    FrameExpectOptions expectParams = new FrameExpectOptions();
    expectParams.expectedValue = expected;
    expectParams.isNot = state.isNot;
    expectParams.timeout = timeout;
    expectParams.title = state.title;
    FrameExpectResult queryResult = locator != null ?
      locator.expect("to.match.aria", expectParams) :
      ((PageImpl) receiver).mainFrame().expect("to.match.aria", expectParams);
    boolean pass = queryResult.matches;
    FrameExpectResult.ExpectReceived received = queryResult.received;
    List<String> log = queryResult.log;
    Boolean timedOut = queryResult.timedOut;
    String errorMessage = queryResult.errorMessage;
    // java: MatcherReceived is the parsed object, a Map with raw and regex.
    Map<String, Object> typedReceived = received == null || received.value == MatcherUtils.UNDEFINED ? null : (Map<String, Object>) received.value;

    String finalExpected = expected;
    MatcherResult result = new MatcherResult();
    result.message = () -> {
      String printedExpected = null;
      String printedReceived = null;
      String printedDiff = null;
      if (errorMessage != null) {
        printedExpected = "Expected: " + (state.isNot ? "not " : "") + printExpected(finalExpected);
      } else if (pass) {
        String raw = (String) typedReceived.get("raw");
        String receivedString = printReceivedStringContainExpectedSubstring(raw, raw.indexOf(finalExpected), finalExpected.length());
        printedExpected = "Expected: not " + printExpected(finalExpected);
        printedReceived = "Received: " + receivedString;
      } else {
        String[] printed = printExpectedAndReceived(finalExpected, typedReceived.get("raw"), "Expected", "Received");
        printedExpected = printed[0];
        printedReceived = printed[1];
      }
      MatcherMessageDetails details = new MatcherMessageDetails();
      details.isNot = state.isNot;
      details.promise = state.promise;
      details.matcherName = matcherName;
      details.expectation = "expected";
      details.locator = locator == null ? null : locator.toString();
      details.timeout = timeout;
      details.timedOut = timedOut;
      details.printedExpected = printedExpected;
      details.printedReceived = printedReceived;
      details.printedDiff = printedDiff;
      details.errorMessage = errorMessage;
      details.log = log;
      return formatMatcherMessage(details);
    };

    if (errorMessage != null) {
      result.pass = state.isNot;
      result.name = "matchesAriaSnapshot";
      result.expected = expected;
      return result;
    }

    result.name = matcherName;
    result.expected = expected;
    result.pass = pass;
    result.actual = typedReceived == null ? null : typedReceived.get("raw");
    result.log = log;
    result.timeout = timedOut != null && timedOut ? timeout : null;
    return result;
  }

  private static String unshift(String snapshot) {
    String[] lines = snapshot.split("\n", -1);
    int whitespacePrefixLength = 100;
    for (String line : lines) {
      if (line.trim().isEmpty())
        continue;
      int length = 0;
      while (length < line.length() && Character.isWhitespace(line.charAt(length)))
        length++;
      if (length < whitespacePrefixLength)
        whitespacePrefixLength = length;
    }
    List<String> result = new ArrayList<>();
    for (String line : lines) {
      if (line.trim().isEmpty())
        continue;
      result.add(line.substring(Math.min(whitespacePrefixLength, line.length())));
    }
    return String.join("\n", result);
  }

  // java: no indent, it formats the suggested rebaseline.
}
