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

// Port of toEqual.ts.
package com.microsoft.playwright.impl;

import com.microsoft.playwright.impl.AssertionsBase.ExpectMatcherState;
import com.microsoft.playwright.impl.AssertionsBase.Query;
import com.microsoft.playwright.impl.MatcherHint.MatcherMessageDetails;
import com.microsoft.playwright.impl.MatcherHint.MatcherResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static com.microsoft.playwright.impl.MatcherUtils.printExpectedAndReceived;
import static com.microsoft.playwright.impl.MatcherUtils.printExpected;
import static com.microsoft.playwright.impl.MatcherUtils.printReceived;
import static com.microsoft.playwright.impl.MatcherHint.formatMatcherMessage;

class ToEqual {
  private static final String EXPECTED_LABEL = "Expected";
  private static final String RECEIVED_LABEL = "Received";

  // java: the options are their timeout, contains is not read upstream and there is no AbortSignal.
  static MatcherResult toEqual(
    ExpectMatcherState state,
    String matcherName,
    LocatorImpl locator,
    String receiverType,
    Query query,
    Object expected,
    Double optionsTimeout) {
    double timeout = optionsTimeout != null ? optionsTimeout : state.timeout;

    FrameExpectResult queryResult = query.query(state.isNot, timeout);
    boolean pass = queryResult.matches;
    FrameExpectResult.ExpectReceived received = queryResult.received;
    List<String> log = queryResult.log;
    Boolean timedOut = queryResult.timedOut;
    String errorMessage = queryResult.errorMessage;
    Object receivedValue = received == null ? MatcherUtils.UNDEFINED : received.value;

    if (pass == !state.isNot) {
      MatcherResult result = new MatcherResult();
      result.name = matcherName;
      result.message = () -> "";
      result.pass = pass;
      result.expected = expected;
      return result;
    }

    String printedReceived = null;
    String printedExpected = null;
    String printedDiff = null;
    if (pass) {
      printedExpected = "Expected: not " + printExpected(expected);
      printedReceived = errorMessage != null ? "" : "Received: " + printReceived(receivedValue);
    } else if (errorMessage != null) {
      printedExpected = "Expected: " + printExpected(expected);
    } else if (isArray(expected) && isArray(receivedValue)) {
      List<Object> expectedList = toList(expected);
      List<Object> receivedList = toList(receivedValue);
      List<Object> normalizedExpected = new ArrayList<>();
      for (int index = 0; index < expectedList.size(); index++) {
        Object exp = expectedList.get(index);
        Object rec = index < receivedList.size() ? receivedList.get(index) : null;
        if (exp instanceof Pattern) {
          // java: RegExp.test() converts its argument to a string, a missing element is "undefined".
          normalizedExpected.add(((Pattern) exp).matcher(rec == null ? "undefined" : String.valueOf(rec)).find() ? rec : exp);
          continue;
        }

        normalizedExpected.add(exp);
      }
      String[] printed = printExpectedAndReceived(
        normalizedExpected,
        receivedValue,
        EXPECTED_LABEL,
        RECEIVED_LABEL);
      printedExpected = printed[0];
      printedReceived = printed[1];
    } else {
      String[] printed = printExpectedAndReceived(
        expected,
        receivedValue,
        EXPECTED_LABEL,
        RECEIVED_LABEL);
      printedExpected = printed[0];
      printedReceived = printed[1];
    }
    String finalPrintedExpected = printedExpected;
    String finalPrintedReceived = printedReceived;
    String finalPrintedDiff = printedDiff;
    MatcherResult result = new MatcherResult();
    result.message = () -> {
      MatcherMessageDetails details = new MatcherMessageDetails();
      details.isNot = state.isNot;
      details.promise = state.promise;
      details.matcherName = matcherName;
      details.expectation = "expected";
      details.locator = locator.toString();
      details.timeout = timeout;
      details.timedOut = timedOut;
      details.printedExpected = finalPrintedExpected;
      details.printedReceived = finalPrintedReceived;
      details.printedDiff = finalPrintedDiff;
      details.errorMessage = errorMessage;
      details.log = log;
      return formatMatcherMessage(details);
    };

    result.actual = receivedValue;
    result.expected = expected;
    result.name = matcherName;
    result.pass = pass;
    result.log = log;
    result.timeout = timedOut != null && timedOut ? timeout : null;
    result.ariaSnapshot = received == null ? null : received.ariaSnapshot;
    return result;
  }

  // java: helpers with no counterpart upstream: a JS array is a List or a Java array.
  private static boolean isArray(Object value) {
    return value instanceof List || value instanceof Object[];
  }

  @SuppressWarnings("unchecked")
  private static List<Object> toList(Object value) {
    return value instanceof List ? (List<Object>) value : Arrays.asList((Object[]) value);
  }
}
