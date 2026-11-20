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

// Port of clientHelper.ts.
package com.microsoft.playwright.impl;

import com.microsoft.playwright.PlaywrightException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;

// java: no envObjectToArray, the env map is serialized as a name/value array by Serialization; no
// initScriptSourceWithExposedFunctions, there is no exposeFunctions option.
class ClientHelper {
  // java: fun is a script String or a file Path; the Java API has no function, { content } or arg.
  static String evaluationScript(Object fun, boolean addSourceUrl) {
    if (fun instanceof String)
      return (String) fun;
    if (fun instanceof Path) {
      Path path = (Path) fun;
      String source;
      try {
        source = new String(Files.readAllBytes(path), UTF_8);
      } catch (IOException e) {
        throw new PlaywrightException("Failed to read script from file", e);
      }
      if (addSourceUrl)
        source = addSourceUrlToScript(source, path);
      return source;
    }
    throw new PlaywrightException("Either path or content property must be present");
  }

  static String addSourceUrlToScript(String source, Path path) {
    return source + "\n//# sourceURL=" + path.toString().replace("\n", "");
  }
}
