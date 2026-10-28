/**
 * Copyright 2019 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.playwright.tests.library.chromium;

import com.google.gson.JsonObject;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.CDPSession;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.EnabledOnBrowser;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Port of tests/library/chromium/launcher.spec.ts. */
@EnabledOnBrowser(BrowserName.CHROMIUM)
public class TestLauncher {
  @PlaywrightTest
  @DisplayName("CR-035 should not create pages automatically")
  void shouldNotCreatePagesAutomatically(BrowserType browserType) {
    Browser browser = browserType.launch(Env.launchOptions());
    CDPSession browserSession = browser.newBrowserCDPSession();
    List<JsonObject> targets = new ArrayList<>();
    browserSession.on("Target.targetCreated", params -> {
      JsonObject targetInfo = params.getAsJsonObject("targetInfo");
      if (!targetInfo.get("type").getAsString().equals("browser"))
        targets.add(targetInfo);
    });
    JsonObject discover = new JsonObject();
    discover.addProperty("discover", true);
    browserSession.send("Target.setDiscoverTargets", discover);
    browser.newContext();
    browser.close();
    assertEquals(0, targets.size());
  }
}
