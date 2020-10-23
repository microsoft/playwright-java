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
package com.microsoft.playwright.example;

import com.microsoft.playwright.*;

import java.io.File;

public class Main {
  public static void main(String[] args) {
    Playwright playwright = Playwright.create();
    Browser browser = playwright.chromium().launch();
    BrowserContext context = browser.newContext(
      new Browser.NewContextOptions().withViewport(800, 600));
    Page page = context.newPage();
    page.navigate("https://webkit.org");
    page.click("text=check feature status");
    page.screenshot(new Page.ScreenshotOptions().withPath(new File("s.png")));
    browser.close();
  }
}
