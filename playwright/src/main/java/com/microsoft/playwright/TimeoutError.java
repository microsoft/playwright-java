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

package com.microsoft.playwright;

/**
 * TimeoutError is emitted whenever certain operations are terminated due to timeout, e.g. {@link Page#waitForSelector
 * Page.waitForSelector()} or {@link BrowserType#launch BrowserType.launch()}.
 */
public class TimeoutError extends PlaywrightException {
  public TimeoutError(String message) {
    super(message);
  }

  public TimeoutError(String message, Throwable exception) {
    super(message, exception);
  }
}

