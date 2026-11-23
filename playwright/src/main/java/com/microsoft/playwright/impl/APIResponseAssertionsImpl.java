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

// Port of matchers.ts.
package com.microsoft.playwright.impl;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.assertions.APIResponseAssertions;
import com.microsoft.playwright.impl.MatcherHint.MatcherMessageDetails;
import com.microsoft.playwright.impl.MatcherHint.MatcherResult;

import java.util.List;
import java.util.regex.Pattern;

import static com.microsoft.playwright.impl.MatcherHint.formatMatcherMessage;

// java: the matcher of an APIResponse, see LocatorAssertionsImpl.
public class APIResponseAssertionsImpl extends AssertionsBase implements APIResponseAssertions {
  private final APIResponseImpl actual;

  APIResponseAssertionsImpl(APIResponse response, boolean isNot) {
    super(isNot);
    this.actual = (APIResponseImpl) response;
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
    callMatcherAsStep("isOK", state -> {
      APIResponseImpl response = actual;
      String matcherName = "isOK";

      String contentType = response.headers().get("content-type");
      boolean isTextEncoding = contentType != null && !contentType.isEmpty() && isTextualMimeType(contentType);
      List<String> log = null;
      String text = null;
      if (state.isNot == response.ok()) {
        log = response.fetchLog();
        text = isTextEncoding ? response.text() : null;
      }

      List<String> finalLog = log;
      String finalText = text;
      MatcherResult result = new MatcherResult();
      result.message = () -> {
        MatcherMessageDetails details = new MatcherMessageDetails();
        details.isNot = state.isNot;
        details.promise = state.promise;
        details.matcherName = matcherName;
        details.receiver = "response";
        details.expectation = "";
        details.log = finalLog;
        return formatMatcherMessage(details) + (finalText == null ? "" : "\nResponse text:\n" + (finalText.length() > 1000 ? finalText.substring(0, 1000) : finalText));
      };

      result.pass = response.ok();
      return result;
    });
  }

  // java: isTextualMimeType of isomorphic/mimeType.ts.
  static boolean isTextualMimeType(String mimeType) {
    return Pattern.matches("^(text/.*?|application/(json|(x-)?javascript|xml.*?|ecmascript|graphql|x-www-form-urlencoded)|image/svg(\\+xml)?|application/.*?(\\+json|\\+xml))(;\\s*charset=.*)?$", mimeType);
  }
}
