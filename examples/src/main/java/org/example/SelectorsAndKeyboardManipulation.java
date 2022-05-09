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

public class SelectorsAndKeyboardManipulation {
  public class void main(String[] args) {
    try(Playwright playwright = Playwright.create()) {
      Browser browser = playwright.firefox().launch();
      BrowserContext context = browser.newContext();
      Page page = context.newPage();
      page.navigate("https://playwright.dev/");
      page.click("#__docusaurus > nav > div.navbar__inner > div.navbar__items.navbar__items--right > div.searchBox_qEbK > button");
      page.waitForSelector("#docsearch-input");
      page.type("#docsearch-input", "getting started");
      page.waitForSelector("#docsearch-item-0 > a > div");
      page.click("#docsearch-item-0 > a > div");
      page.waitForSelector("#__docusaurus > div.main-wrapper > div > main > div > div > div.col.docItemCol_DM6M > div > article > div.theme-doc-markdown.markdown > header > h1");
      page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("screenshot.png")));
    }
  }
}
