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
@UsePlaywright(TestFixtureInnerOptionsFactory.CustomOptions.class)
public class TestFixtureInnerOptionsFactory {
  // Set per instance, similar to fields injected by Spring or Micronaut.
  private final Server server;
  private boolean optionsCreated;

  TestFixtureInnerOptionsFactory(Server server) {
    this.server = server;
  }

  class CustomOptions implements OptionsFactory {
    @Override
    public Options getOptions() {
      optionsCreated = true;
      return new Options().setBaseUrl(server.PREFIX);
    }
  }

  // Each repetition gets a new test instance.
  @RepeatedTest(2)
  void shouldCreateOptionsForEachTestInstance(Page page) {
    assertTrue(optionsCreated);
    page.navigate("/empty.html");
    assertThat(page).hasURL(server.EMPTY_PAGE);
  }

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class PerClassLifecycle {
    @BeforeAll
    void beforeAll(Browser.NewContextOptions contextOptions) {
      assertEquals(server.PREFIX, contextOptions.baseURL);
    }

    @Test
    void shouldUseOuterInstance(Page page) {
      page.navigate("/empty.html");
      assertThat(page).hasURL(server.EMPTY_PAGE);
    }
  }
}
