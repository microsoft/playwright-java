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

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Server;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@FixtureTest
@UsePlaywright
public class TestFixturePlaywrightOptions {
  // Set per instance, similar to fields injected by Spring or Micronaut.
  private final Server server;
  private boolean optionsRead;

  TestFixturePlaywrightOptions(Server server) {
    this.server = server;
  }

  @PlaywrightOptions
  Options options() {
    optionsRead = true;
    return new Options().setBaseUrl(server.PREFIX);
  }

  // Each repetition gets a new test instance.
  @RepeatedTest(2)
  void shouldReadOptionsForEachTestInstance(Page page) {
    assertTrue(optionsRead);
    page.navigate("/empty.html");
    assertThat(page).hasURL(server.EMPTY_PAGE);
  }

  @Nested
  class NestedClass {
    @Test
    void shouldUseOptionsOfEnclosingClass(Page page) {
      assertTrue(optionsRead);
      page.navigate("/empty.html");
      assertThat(page).hasURL(server.EMPTY_PAGE);
    }
  }

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class PerClassLifecycle {
    @BeforeAll
    void beforeAll(Browser.NewContextOptions contextOptions) {
      assertEquals(server.PREFIX, contextOptions.baseURL);
    }

    @Test
    void shouldUseOptionsOfEnclosingClass(Page page) {
      page.navigate("/empty.html");
      assertThat(page).hasURL(server.EMPTY_PAGE);
    }
  }
}
