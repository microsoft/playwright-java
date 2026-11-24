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

// Port of expect.ts.
package com.microsoft.playwright.impl;

import com.microsoft.playwright.impl.MatcherHint.MatcherResult;
import org.opentest4j.AssertionFailedError;
import org.opentest4j.ValueWrapper;

import java.util.Collection;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

// java: only the call of a matcher is ported: callMatcherAsStep and invokeMatcher. The expect function, its
// configuration, the custom, soft, poll and promise matchers and the steps belong to the test runner.
abstract class AssertionsBase {
  final boolean isNot;

  AssertionsBase(boolean isNot) {
    this.isNot = isNot;
  }

  // java: the fields of ExpectMatcherStateInternal that the matchers read, utils is MatcherUtils.
  static class ExpectMatcherState {
    boolean isNot;
    String promise;
    double timeout;
    String title;
  }

  // java: the type of a matcher bound to its receiver and arguments.
  interface RawMatcherFn {
    MatcherResult call(ExpectMatcherState state);
  }

  // java: the type of the query argument of toBeTruthy, toEqual and toMatchText.
  interface Query {
    FrameExpectResult query(boolean isNot, double timeout);
  }

  void callMatcherAsStep(String matcherName, RawMatcherFn matcher) {
    // java: no custom message and no title suffix, escapeWithQuotes has nothing to escape in a matcher name.
    String defaultTitle = (isNot ? "not " : "") + matcherName;
    // java: the title of the Java client is Assert, with the Java name of the matcher.
    String title = "Assert \"" + defaultTitle + "\"";

    MatcherResult result = invokeMatcher(matcherName, matcher, title);
    if (result.pass == isNot) {
      // java: ExpectError is an AssertionFailedError with the expected and the actual value of the result.
      String message = result.message.get();
      if (result.expected == null && (result.actual == null || result.actual == MatcherUtils.UNDEFINED))
        throw new AssertionFailedError(message);
      throw new AssertionFailedError(message, formatValue(result.expected), formatValue(result.actual));
    }
  }

  private MatcherResult invokeMatcher(String matcherName, RawMatcherFn matcher, String title) {
    double timeout = AssertionsTimeout.defaultTimeout;
    ExpectMatcherState matcherContext = new ExpectMatcherState();
    matcherContext.isNot = isNot;
    matcherContext.promise = "";
    matcherContext.timeout = timeout;
    matcherContext.title = title;

    return matcher.call(matcherContext);
  }

  // java: a helper with no counterpart upstream, the value of an AssertionFailedError.
  protected static ValueWrapper formatValue(Object value) {
    if (value == MatcherUtils.UNDEFINED) {
      return ValueWrapper.create(null);
    }
    if (value == null || !value.getClass().isArray()) {
      return ValueWrapper.create(value);
    }
    Collection<String> values = asList((Object[]) value).stream().map(e -> e.toString()).collect(Collectors.toList());
    String stringRepresentation = "[" + String.join(", ", values) + "]";
    return ValueWrapper.create(value, stringRepresentation);
  }
}
