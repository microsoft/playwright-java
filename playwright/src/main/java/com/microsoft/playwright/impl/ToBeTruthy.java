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

// Port of toBeTruthy.ts.
package com.microsoft.playwright.impl;

import com.microsoft.playwright.impl.AssertionsBase.ExpectMatcherState;
import com.microsoft.playwright.impl.AssertionsBase.Query;
import com.microsoft.playwright.impl.MatcherHint.MatcherMessageDetails;
import com.microsoft.playwright.impl.MatcherHint.MatcherResult;

import java.util.List;

import static com.microsoft.playwright.impl.MatcherHint.formatMatcherMessage;

class ToBeTruthy {
  // java: the options are their timeout, there is no AbortSignal.
  static MatcherResult toBeTruthy(
    ExpectMatcherState state,
    String matcherName,
    LocatorImpl locator,
    String receiverType,
    String expected,
    String arg,
    Query query,
    Double optionsTimeout) {
    double timeout = optionsTimeout != null ? optionsTimeout : state.timeout;

    FrameExpectResult queryResult = query.query(state.isNot, timeout);
    boolean pass = queryResult.matches;
    List<String> log = queryResult.log;
    Boolean timedOut = queryResult.timedOut;
    FrameExpectResult.ExpectReceived received = queryResult.received;
    String errorMessage = queryResult.errorMessage;
    Object receivedValue = received == null ? JestGetType.UNDEFINED : received.value;

    if (pass == !state.isNot) {
      MatcherResult result = new MatcherResult();
      result.name = matcherName;
      result.message = () -> "";
      result.pass = pass;
      result.expected = expected;
      return result;
    }

    String printedReceived;
    String printedExpected;
    if (pass) {
      printedExpected = "Expected: not " + expected;
      printedReceived = errorMessage != null ? "" : "Received: " + expected;
    } else {
      printedExpected = "Expected: " + expected;
      printedReceived = errorMessage != null ? "" : "Received: " + receivedValue;
    }
    MatcherResult result = new MatcherResult();
    result.message = () -> {
      MatcherMessageDetails details = new MatcherMessageDetails();
      details.isNot = state.isNot;
      details.promise = state.promise;
      details.matcherName = matcherName;
      details.expectation = arg;
      details.locator = locator.toString();
      details.timeout = timeout;
      details.timedOut = timedOut;
      details.printedExpected = printedExpected;
      details.printedReceived = printedReceived;
      details.errorMessage = errorMessage;
      details.log = log;
      return formatMatcherMessage(details);
    };
    result.pass = pass;
    result.actual = receivedValue;
    result.name = matcherName;
    result.expected = expected;
    result.log = log;
    result.timeout = timedOut != null && timedOut ? timeout : null;
    result.ariaSnapshot = received == null ? null : received.ariaSnapshot;
    return result;
  }
}
