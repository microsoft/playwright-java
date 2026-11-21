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

import java.util.ArrayList;
import java.util.List;

// java: the channel { type, method, params } and the { keepTestTimeout } options are plain parameters;
// BrowserContextOptions is Browser.NewContextOptions or BrowserType.LaunchPersistentContextOptions and
// NewContextOptions is APIRequest.NewContextOptions in Java, which share no type, so the options are Object.
interface ClientInstrumentation {
  void addListener(ClientInstrumentationListener listener);

  void removeListener(ClientInstrumentationListener listener);

  void removeAllListeners();

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

  static ClientInstrumentation createInstrumentation() {
    List<ClientInstrumentationListener> listeners = new ArrayList<>();
    // java: upstream returns a Proxy that forwards every on*/run* hook to the listeners; this class forwards each hook.
    return new ClientInstrumentation() {
      @Override
      public void addListener(ClientInstrumentationListener listener) {
        listeners.add(listener);
      }

      @Override
      public void removeListener(ClientInstrumentationListener listener) {
        // java: splice(indexOf(listener), 1) removes the last listener for an unknown one; remove(Object) removes nothing.
        listeners.remove(listener);
      }

      @Override
      public void removeAllListeners() {
        listeners.clear();
      }

      @Override
      public void onApiCallBegin(ApiCallData apiCall, String type, String method, JsonObject params) {
        for (ClientInstrumentationListener listener : listeners)
          listener.onApiCallBegin(apiCall, type, method, params);
      }

      @Override
      public void onApiCallEnd(ApiCallData apiCall) {
        for (ClientInstrumentationListener listener : listeners)
          listener.onApiCallEnd(apiCall);
      }

      @Override
      public void onWillPause(boolean keepTestTimeout) {
        for (ClientInstrumentationListener listener : listeners)
          listener.onWillPause(keepTestTimeout);
      }

      @Override
      public void onPage(PageImpl page) {
        for (ClientInstrumentationListener listener : listeners)
          listener.onPage(page);
      }

      @Override
      public void runBeforeCreateBrowserContext(Object options) {
        for (ClientInstrumentationListener listener : listeners)
          listener.runBeforeCreateBrowserContext(options);
      }

      @Override
      public void runBeforeCreateRequestContext(Object options) {
        for (ClientInstrumentationListener listener : listeners)
          listener.runBeforeCreateRequestContext(options);
      }

      @Override
      public void runAfterCreateBrowserContext(BrowserContextImpl context) {
        for (ClientInstrumentationListener listener : listeners)
          listener.runAfterCreateBrowserContext(context);
      }

      @Override
      public void runAfterCreateRequestContext(APIRequestContextImpl context) {
        for (ClientInstrumentationListener listener : listeners)
          listener.runAfterCreateRequestContext(context);
      }

      @Override
      public void runBeforeCloseBrowserContext(BrowserContextImpl context) {
        for (ClientInstrumentationListener listener : listeners)
          listener.runBeforeCloseBrowserContext(context);
      }

      @Override
      public void runBeforeCloseRequestContext(APIRequestContextImpl context) {
        for (ClientInstrumentationListener listener : listeners)
          listener.runBeforeCloseRequestContext(context);
      }
    };
  }
}
