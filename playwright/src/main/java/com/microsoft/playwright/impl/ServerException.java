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

import com.microsoft.playwright.PlaywrightException;

import java.io.PrintStream;
import java.io.PrintWriter;

class ServerException extends PlaywrightException {
  private final SerializedError.Error error;

  ServerException(SerializedError.Error error) {
    super(error.name + ": " + error.message);
    this.error = error;
  }

  @Override
  public void printStackTrace(PrintWriter s) {
    super.printStackTrace(s);
    s.println("Caused by Playwright server error:");
    s.println(getMessage());
    s.println(error.stack);
  }

  @Override
  public void printStackTrace(PrintStream s) {
    super.printStackTrace(s);
    s.println("Caused by Playwright server error:");
    s.println(getMessage());
    s.println(error.stack);
  }
}
