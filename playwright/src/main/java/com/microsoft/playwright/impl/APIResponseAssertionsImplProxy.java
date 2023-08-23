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

import java.util.List;

public class APIResponseAssertionsImplProxy extends SoftAssertionsBase implements APIResponseAssertions {
  private final APIResponseAssertionsImpl apiResponseAssertionsImpl;

  APIResponseAssertionsImplProxy(APIResponse response, List<Throwable> results) {
    this(results, new APIResponseAssertionsImpl(response));
  }

  private APIResponseAssertionsImplProxy(List<Throwable> results, APIResponseAssertionsImpl apiResponseAssertionsImpl) {
    super(results);
    this.apiResponseAssertionsImpl = apiResponseAssertionsImpl;
  }

  @Override
  public APIResponseAssertions not() {
    return new APIResponseAssertionsImplProxy(super.results, apiResponseAssertionsImpl.not());
  }

  @Override
  public void isOK() {
    assertAndCaptureResult(apiResponseAssertionsImpl::isOK);
  }
}
