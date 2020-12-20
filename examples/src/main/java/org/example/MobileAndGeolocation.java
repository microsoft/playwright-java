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

import java.nio.file.Paths;

import static java.util.Arrays.asList;

public class MobileAndGeolocation {
  public static void main(String[] args) throws Exception {
    Playwright playwright = Playwright.create();
    BrowserType browserType = playwright.chromium();
    Browser browser = browserType.launch(new BrowserType.LaunchOptions().withHeadless(false));
    DeviceDescriptor pixel2 = playwright.devices().get("Pixel 2");
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
        .withDevice(pixel2)
        .withLocale("en-US")
        .withGeolocation(new Geolocation(41.889938, 12.492507))
        .withPermissions(asList("geolocation")));
    Page page = context.newPage();
    page.navigate("https://www.openstreetmap.org/");
    page.click("a[data-original-title=\"Show My Location\"]");
    page.screenshot(new Page.ScreenshotOptions().withPath(Paths.get("colosseum-pixel2.png")));
    browser.close();
    playwright.close();
  }
}
