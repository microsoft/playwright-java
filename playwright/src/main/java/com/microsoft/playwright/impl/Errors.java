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

// Port of packages/playwright-core/src/client/errors.ts
package com.microsoft.playwright.impl;

import com.microsoft.playwright.impl.protocol.*;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.TimeoutError;

// java: errors.ts declares the error classes (PlaywrightError, TimeoutError, TargetClosedError)
// and the functions next to them. Here the classes stay in their own files (TimeoutError is
// public API, TargetClosedError is public, DriverException is upstream's generic PlaywrightError)
// and this class holds the functions.
class Errors {
  private Errors() {
  }

  static boolean isTargetClosedError(Throwable error) {
    return error instanceof TargetClosedError;
  }

  // serializeError is not needed: the client never sends errors to the server.

  // java: the message is built the way the Java client always did, see errorToString, because the
  // tests match on it. Upstream uses error.error.message and appends the call log in Connection.dispatch.
  static PlaywrightException parseError(SerializedError error) {
    return parseError(error, "");
  }

  // java: exceptions are immutable here, so the formatted call log is appended while the message
  // is built; upstream rewrites the message afterwards (rewriteErrorMessage in Connection.dispatch).
  static PlaywrightException parseError(SerializedError error, String callLog) {
    if (error.error == null) {
      // java: upstream parses error.value back into a value; here the structure is only printed.
      return new PlaywrightException(errorToString(error) + callLog);
    }
    String message = errorToString(error.error) + callLog;
    if ("TimeoutError".equals(error.error.name)) {
      return new TimeoutError(message);
    }
    if ("TargetClosedError".equals(error.error.name)) {
      return new TargetClosedError(message);
    }
    return new DriverException(message);
  }

  // java: the "has been closed" family that close paths swallow; not in errors.ts.
  static boolean isSafeCloseError(PlaywrightException exception) {
    return isSafeCloseError(exception.getMessage());
  }

  static boolean isSafeCloseError(String error) {
    return error.contains("Browser has been closed") || error.contains("Target page, context or browser has been closed");
  }

  // java: format of the driver error message; tests match on it.
  static String errorToString(SerializedError.SerializedErrorError error) {
    return "Error {\n" +
      "  message='" + error.message + '\n' +
      "  name='" + error.name + '\n' +
      "  stack='" + error.stack + '\n' +
      '}';
  }

  static String errorToString(SerializedError error) {
    if (error.error != null) {
      return errorToString(error.error);
    }
    return "SerializedError{" +
      "value=" + error.value +
      '}';
  }
}
