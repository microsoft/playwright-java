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

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.assertions.APIResponseAssertions;
import org.opentest4j.AssertionFailedError;

import java.util.List;

public class APIResponseAssertionsImpl implements APIResponseAssertions {
  private final APIResponse actual;
  private final boolean isNot;

  APIResponseAssertionsImpl(APIResponse response, boolean isNot) {
    this.actual = response;
    this.isNot = isNot;
  }

  public APIResponseAssertionsImpl(APIResponse response) {
    this(response, false);
  }

  @Override
  public APIResponseAssertions not() {
    return new APIResponseAssertionsImpl(actual, !isNot);
  }

  @Override
  public void isOK() {
    if (actual.ok() == !isNot) {
      return;
    }
    String message = "Response status expected to be within [200..299] range, was " + actual.status();
    if (isNot) {
      message = message.replace("expected to", "expected not to");
    }
    List<String> logList = ((APIResponseImpl) actual).fetchLog();
    String log = String.join("\n", logList);
    if (!log.isEmpty()) {
      log = "\nCall log:\n" + log;
    }
    throw new AssertionFailedError(message + log);
  }
}
