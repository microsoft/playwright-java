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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.playwright.PlaywrightException;

import java.util.ArrayList;
import java.util.List;

class ServerErrorWithDetails extends PlaywrightException {
  private final JsonObject errorDetails;
  private final JsonArray log;

  ServerErrorWithDetails(PlaywrightException cause, JsonObject errorDetails, JsonArray log) {
    super(cause.getMessage(), cause);
    this.errorDetails = errorDetails;
    this.log = log;
  }

  // Rethrown with the calling thread's stack trace, see WaitableResult.get().
  ServerErrorWithDetails(ServerErrorWithDetails cause) {
    super(cause.getMessage(), cause);
    this.errorDetails = cause.errorDetails;
    this.log = cause.log;
  }

  JsonObject errorDetails() {
    return errorDetails;
  }

  List<String> log() {
    List<String> result = new ArrayList<>();
    if (log != null) {
      for (JsonElement e : log) {
        result.add(e.getAsString());
      }
    }
    return result;
  }
}
