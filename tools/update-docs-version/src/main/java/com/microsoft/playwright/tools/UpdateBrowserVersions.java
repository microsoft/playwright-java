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

package com.microsoft.playwright.tools;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class UpdateBrowserVersions {
  public static void main(String[] args) throws Exception {
    Path path = Paths.get("README.md");
    String readme = new String(Files.readAllBytes(path), UTF_8);

    Playwright playwright = Playwright.create();
    List<BrowserType> browserTypes = Arrays.asList(
      playwright.chromium(),
      playwright.webkit(),
      playwright.firefox()
    );
    for (BrowserType browserType : browserTypes) {
      Browser browser = browserType.launch();
      String version = browser.version();
      browser.close();
      readme = readme.replaceAll(
        "<!-- GEN:" + browserType.name() + "-version -->([^<]+)<!-- GEN:stop -->",
        "<!-- GEN:" + browserType.name() + "-version -->" + version + "<!-- GEN:stop -->");
      try (FileWriter writer = new FileWriter(path.toFile())) {
        writer.write(readme);
      }
    }
    playwright.close();
  }
}
