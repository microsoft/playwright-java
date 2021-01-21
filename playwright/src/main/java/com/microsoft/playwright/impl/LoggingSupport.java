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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.function.Supplier;

class LoggingSupport {
  private static final boolean isEnabled;
  static {
    String debug = System.getenv("DEBUG");
    isEnabled = (debug != null) && debug.contains("pw:api");
  }

  private static final SimpleDateFormat timestampFormat;
  static {
    timestampFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    timestampFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
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

  private void logApi(String message) {
    // This matches log format produced by the server.
    String timestamp = timestampFormat.format(new Date());
    System.err.println(timestamp + " pw:api " + message);
  }
}
