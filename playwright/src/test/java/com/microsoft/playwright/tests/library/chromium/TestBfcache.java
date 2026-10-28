/**
 * Copyright Microsoft Corporation. All rights reserved.
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

import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.EnabledOnBrowser;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.Fixture;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.Scope;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.WaitUntilState;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Port of tests/library/chromium/bfcache.spec.ts. */
@EnabledOnBrowser(BrowserName.CHROMIUM)
public class TestBfcache {
  @Fixture(scope = Scope.WORKER)
  static BrowserType.LaunchOptions launchOptions() {
    return Env.launchOptions().setIgnoreDefaultArgs(Arrays.asList("--disable-back-forward-cache"));
  }

  @PlaywrightTest
  @DisplayName("CR-042 bindings should work after restoring from bfcache")
  void bindingsShouldWorkAfterRestoringFromBfcache(Page page, TestServer server) {
    page.exposeFunction("add", args -> (Integer) args[0] + (Integer) args[1]);

    page.navigate(server.PREFIX + "/cached/bfcached.html");
    assertEquals(3, page.evaluate("window.add(1, 2)"));

    page.setContent("<a href='about:blank'}>click me</a>");
    page.click("a");

    page.goBack(new Page.GoBackOptions().setWaitUntil(WaitUntilState.COMMIT));
    page.evaluate("window.didShow");
    assertEquals(5, page.evaluate("window.add(2, 3)"));
  }
}
