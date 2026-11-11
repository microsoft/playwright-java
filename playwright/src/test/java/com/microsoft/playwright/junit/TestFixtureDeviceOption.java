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

package com.microsoft.playwright.junit;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Server;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.junit.ServerLifecycle.serverMap;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@FixtureTest
@UsePlaywright(TestFixtureDeviceOption.CustomOptions.class)
public class TestFixtureDeviceOption {

  public static class CustomOptions implements OptionsFactory {
    @Override
    public Options getOptions() {
      return new Options().setDeviceName("iPhone 14");
    }
  }

  @Test
  public void testPredefinedDeviceParameters(Server server, Page page) {
    page.navigate(server.EMPTY_PAGE);
    assertEquals("webkit", page.context().browser().browserType().name());
    // TODO: failing since 1.57 roll.
    // assertEquals(3, page.evaluate("window.devicePixelRatio"));
    assertEquals(980, page.evaluate("window.innerWidth"));
    // The layout height is 980 * 664 / 390 = 1668.5, WebKit rounds it either way depending on the platform.
    assertEquals(1668.5, ((Number) page.evaluate("window.innerHeight")).doubleValue(), 0.5);
  }
}
