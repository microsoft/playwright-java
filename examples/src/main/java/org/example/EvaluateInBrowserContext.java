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

package org.example;

import com.microsoft.playwright.*;

public class EvaluateInBrowserContext {
  public static void main(String[] args) {
    try (Playwright playwright = Playwright.create()) {
      Browser browser = playwright.firefox().launch();
      BrowserContext context = browser.newContext();
      Page page = context.newPage();
      page.navigate("https://www.example.com/");
      Object dimensions = page.evaluate("() => {\n" +
          "  return {\n" +
          "      width: document.documentElement.clientWidth,\n" +
          "      height: document.documentElement.clientHeight,\n" +
          "      deviceScaleFactor: window.devicePixelRatio\n" +
          "  }\n" +
          "}");
      System.out.println(dimensions);
    }
  }
}
