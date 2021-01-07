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

import com.microsoft.playwright.Deferred;

import java.util.function.Supplier;

class LoggingSupport {
  private static boolean isEnabled;
  {
    String debug = System.getenv("DEBUG");
    isEnabled = (debug != null) && debug.contains("pw:api");
  }

  void withLogging(String apiName, Runnable code) {
    withLogging(apiName, () -> {
      code.run();
      return null;
    });
  }

  <T> T withLogging(String apiName, Supplier<T> code) {
    if (isEnabled) {
      logApi("=> " + apiName + " started");
    }
    boolean success = false;
    try {
      T result = code.get();
      success = true;
      return result;
    } finally {
      if (isEnabled) {
        logApi("<= " + apiName + (success ? " succeeded" : " failed"));
      }
    }
  }

  <T> Deferred<T> withLoggingDeferred(String apiName, Supplier<Deferred<T>> code) {
    if (isEnabled) {
      logApi("=> " + apiName + " started");
    }
    Deferred<T> deferred = code.get();
    return () -> {
      boolean success = false;
      try {
        T result = deferred.get();
        success = true;
        return result;
      } finally {
        if (isEnabled) {
          logApi("<= " + apiName + (success ? " succeeded" : " failed"));
        }
      }
    };
  }

  private void logApi(String message) {
    System.err.println(message);
  }
}
