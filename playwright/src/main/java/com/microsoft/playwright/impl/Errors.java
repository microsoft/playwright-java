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

// Port of errors.ts.
package com.microsoft.playwright.impl;

import com.microsoft.playwright.impl.protocol.*;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.TimeoutError;

import java.io.PrintWriter;
import java.io.StringWriter;

import static com.microsoft.playwright.impl.Serialization.serializeValue;

// java: errors.ts declares the error classes and the functions next to them; this class holds the functions.
class Errors {
  private Errors() {
  }

  // java: class PlaywrightError is DriverException.java (its log and details are ServerErrorWithDetails.java),
  // class TimeoutError is the public com.microsoft.playwright.TimeoutError, class TargetClosedError is TargetClosedError.java.
  // java: class AbortError is not ported, there is no AbortSignal.

  static boolean isTargetClosedError(Throwable error) {
    return error instanceof TargetClosedError;
  }

  // java: isError(e) is the Throwable check; e.name is the simple class name and e.stack the printed
  // stack trace; a Java exception has no system error code.
  static SerializedError serializeError(Object e) {
    if (e instanceof Throwable) {
      Throwable error = (Throwable) e;
      SerializedError result = new SerializedError();
      result.error = new SerializedError.SerializedErrorError();
      result.error.message = error.getMessage();
      StringWriter stack = new StringWriter();
      error.printStackTrace(new PrintWriter(stack));
      result.error.stack = stack.toString();
      result.error.name = error.getClass().getSimpleName();
      return result;
    }
    SerializedError result = new SerializedError();
    result.value = serializeValue(e, value -> {
      Serialization.HandleOrValue handle = new Serialization.HandleOrValue();
      handle.hasFallThrough = true;
      handle.fallThrough = value;
      return handle;
    });
    return result;
  }

  // java: the message is built the way the Java client always did, see errorToString, because the
  // tests match on it. Upstream uses error.error.message and appends the call log in Connection.dispatch.
  static PlaywrightException parseError(SerializedError error) {
    return parseError(error, "");
  }

  // java: exceptions are immutable here, so the formatted call log is appended while the message
  // is built; upstream rewrites the message afterwards (rewriteErrorMessage in Connection.dispatch).
  static PlaywrightException parseError(SerializedError error, String callLog) {
    if (error.error == null) {
      if (error.value == null) {
        throw new PlaywrightException("Serialized error must have either an error or a value");
      }
      // java: upstream returns parseSerializedValue(error.value), a thrown non-Error value; a Java
      // exception cannot be a value, so the structure is only printed.
      return new PlaywrightException(errorToString(error) + callLog);
    }
    String message = errorToString(error.error) + callLog;
    if ("TimeoutError".equals(error.error.name)) {
      return new TimeoutError(message);
    }
    if ("TargetClosedError".equals(error.error.name)) {
      return new TargetClosedError(message);
    }
    // java: the AbortError branch is not ported, there is no AbortSignal; it falls through to the generic error.
    // java: e.stack (the server stack) and e.code are not kept, the Java exception has its own stack and no code.
    return new DriverException(message);
  }

  // java: helpers with no counterpart in errors.ts.
  // java: the "has been closed" family that close paths swallow.
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
