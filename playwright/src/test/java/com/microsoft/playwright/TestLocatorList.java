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

package com.microsoft.playwright;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestLocatorList extends TestBase {
  @Test
  void locatorAllShouldWork() {
    page.setContent("<div><p>A</p><p>B</p><p>C</p></div>");
    List<String> texts = new ArrayList<>();
    for (Locator p : page.locator("div >> p").all()) {
      texts.add(p.textContent());
    }
    assertEquals(asList("A", "B", "C"), texts);
  }

}
