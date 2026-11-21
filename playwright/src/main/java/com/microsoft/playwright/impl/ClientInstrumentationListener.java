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

// Port of clientInstrumentation.ts.
package com.microsoft.playwright.impl;

import com.google.gson.JsonObject;

// java: the optional methods of upstream's listener are default no-ops; the channel { type, method, params }
// and the { keepTestTimeout } options are plain parameters; the context options are Object, see ClientInstrumentation.
interface ClientInstrumentationListener {
  default void onApiCallBegin(ApiCallData apiCall, String type, String method, JsonObject params) {
  }

  default void onApiCallEnd(ApiCallData apiCall) {
  }

  default void onWillPause(boolean keepTestTimeout) {
  }

  default void onPage(PageImpl page) {
  }

  default void runBeforeCreateBrowserContext(Object options) {
  }

  default void runBeforeCreateRequestContext(Object options) {
  }

  default void runAfterCreateBrowserContext(BrowserContextImpl context) {
  }

  default void runAfterCreateRequestContext(APIRequestContextImpl context) {
  }

  default void runBeforeCloseBrowserContext(BrowserContextImpl context) {
  }

  default void runBeforeCloseRequestContext(APIRequestContextImpl context) {
  }
}
