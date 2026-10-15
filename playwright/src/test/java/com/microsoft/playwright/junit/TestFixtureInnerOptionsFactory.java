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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;

@FixtureTest
@UsePlaywright(TestFixtureInnerOptionsFactory.CustomOptions.class)
public class TestFixtureInnerOptionsFactory {
  private static final List<Object> factoryInstances = new ArrayList<>();
  // Set per instance, similar to fields injected by Spring or Micronaut.
  private final Server server;

  TestFixtureInnerOptionsFactory(Server server) {
    this.server = server;
  }

  class CustomOptions implements OptionsFactory {
    @Override
    public Options getOptions() {
      factoryInstances.add(TestFixtureInnerOptionsFactory.this);
      return new Options().setBaseUrl(server.PREFIX);
    }
  }

  @Test
  void shouldUseTestInstanceFields(Page page) {
    page.navigate("/empty.html");
    assertThat(page).hasURL(server.EMPTY_PAGE);
  }

  @Test
  void shouldCreateOptionsForEachTestInstance(Page page) {
    page.navigate("/empty.html");
    assertSame(this, factoryInstances.get(factoryInstances.size() - 1));
  }

  @Nested
  class NestedClass {
    @Test
    void shouldUseOuterInstanceInNestedClass(Page page) {
      page.navigate("/empty.html");
      assertThat(page).hasURL(server.EMPTY_PAGE);
      assertSame(TestFixtureInnerOptionsFactory.this, factoryInstances.get(factoryInstances.size() - 1));
    }
  }
}
