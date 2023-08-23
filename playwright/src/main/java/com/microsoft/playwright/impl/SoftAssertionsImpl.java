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
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.APIResponseAssertions;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.assertions.PageAssertions;
import com.microsoft.playwright.assertions.SoftAssertions;
import org.opentest4j.AssertionFailedError;

import java.util.ArrayList;
import java.util.List;

public class SoftAssertionsImpl implements SoftAssertions {
  final List<Throwable> results;

  public SoftAssertionsImpl() {
    this.results = new ArrayList<>();
  }

  @Override
  public LocatorAssertions assertThat(Locator locator) {
    return new LocatorAssertionsImplProxy(locator, results);
  }

  @Override
  public PageAssertions assertThat(Page page) {
    return new PageAssertionsImplProxy(page, results);
  }

  @Override
  public APIResponseAssertions assertThat(APIResponse response) {
    return new APIResponseAssertionsImplProxy(response, results);
  }

  @Override
  public void assertAll() {
    if (!results.isEmpty()) {
      throw new AssertionFailedError(getFormattedErrorMessage());
    }
  }

  private String getFormattedErrorMessage() {
    StringBuilder message = new StringBuilder();
    message
      .append(results.size())
      .append(" assertion(s) failed:");

    for (Throwable t : results) {
      message.append("\n");
      message.append("----------------------------------------\n");
      message.append(t.getMessage());
    }

    return message.toString();
  }
}
