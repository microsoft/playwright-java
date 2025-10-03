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

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestPageEventPageError extends TestBase {
  @Test
  void pageErrorsShouldWork() {
    page.navigate(server.EMPTY_PAGE);
    page.evaluate("async () => {\n" +
      "    for (let i = 0; i < 301; i++)\n" +
      "      window.setTimeout(() => { throw new Error('error' + i); }, 0);\n" +
      "    await new Promise(f => window.setTimeout(f, 100));\n" +
      "  }");

    List<String> errors = page.pageErrors();
    assertTrue(errors.size() >= 100, "should be at least 100 errors");

    // Check the last 100 errors (indices 201-300)
    int firstIndex = errors.size() - 100;
    for (int i = 0; i < 100; i++) {
      String error = errors.get(firstIndex + i);
      assertTrue(error.startsWith("Error: error" + (201 + i)), error);
    }
  }
}
