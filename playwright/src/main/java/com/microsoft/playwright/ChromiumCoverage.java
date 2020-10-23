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

import java.util.*;

public interface ChromiumCoverage {
  class StartCSSCoverageOptions {
    public Boolean resetOnNavigation;

    public StartCSSCoverageOptions withResetOnNavigation(Boolean resetOnNavigation) {
      this.resetOnNavigation = resetOnNavigation;
      return this;
    }
  }
  class StartJSCoverageOptions {
    public Boolean resetOnNavigation;
    public Boolean reportAnonymousScripts;

    public StartJSCoverageOptions withResetOnNavigation(Boolean resetOnNavigation) {
      this.resetOnNavigation = resetOnNavigation;
      return this;
    }
    public StartJSCoverageOptions withReportAnonymousScripts(Boolean reportAnonymousScripts) {
      this.reportAnonymousScripts = reportAnonymousScripts;
      return this;
    }
  }
  default void startCSSCoverage() {
    startCSSCoverage(null);
  }
  void startCSSCoverage(StartCSSCoverageOptions options);
  default void startJSCoverage() {
    startJSCoverage(null);
  }
  void startJSCoverage(StartJSCoverageOptions options);
  List<Object> stopCSSCoverage();
  List<Object> stopJSCoverage();
}

