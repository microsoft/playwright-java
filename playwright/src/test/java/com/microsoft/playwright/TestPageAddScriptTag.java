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
import org.junit.jupiter.api.condition.DisabledIf;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestPageAddScriptTag extends TestBase {
  @Test
  @DisabledIf(value="com.microsoft.playwright.TestBase#isWebKit", disabledReason="Upstream behavior")
  void shouldIncludeSourceURLWhenPathIsProvided() {
    page.navigate(server.EMPTY_PAGE);
    Path path = Paths.get("src/test/resources/injectedfile.js");
    page.addScriptTag(new Page.AddScriptTagOptions().setPath(path));
    String result = (String) page.evaluate("() => window['__injectedError'].stack");
    assertTrue(result.contains("resources" + File.separator + "injectedfile.js"), result);
  }
}
