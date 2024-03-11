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

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestLocatorEvaluate extends TestBase {
  @Test
  void shouldWork() {
    page.setContent("<html><body><div class='tweet'><div class='like'>100</div><div class='retweets'>10</div></div></body></html>");
    Locator tweet = page.locator(".tweet .like");
    Object content = tweet.evaluate("node => node.innerText");
    assertEquals("100", content);
  }

  @Test
  void shouldRetrieveContentFromSubtree() {
    String htmlContent = "<div class='a'>not-a-child-div</div><div id='myId'><div class='a'>a-child-div</div></div>";
    page.setContent(htmlContent);
    Locator locator = page.locator("#myId .a");
    Object content = locator.evaluate("node => node.innerText");
    assertEquals("a-child-div", content);
  }

  @Test
  void shouldWorkForAll() {
    page.setContent("<html><body><div class='tweet'><div class='like'>100</div><div class='like'>10</div></div></body></html>");
    Locator tweet = page.locator(".tweet .like");
    Object content = tweet.evaluateAll("nodes => nodes.map(n => n.innerText)");
    assertEquals(asList("100", "10"), content);
  }

  @Test
  void shouldRetrieveContentFromSubtreeForAll() {
    String htmlContent = "<div class='a'>not-a-child-div</div><div id='myId'><div class='a'>a1-child-div</div><div class='a'>a2-child-div</div></div>";
    page.setContent(htmlContent);
    Locator element = page.locator("#myId .a");
    Object content = element.evaluateAll("nodes => nodes.map(n => n.innerText)");
    assertEquals(asList("a1-child-div", "a2-child-div"), content);
  }

  @Test
  void shouldNotThrowInCaseOfMissingSelectorForAll() {
    String htmlContent = "<div class='a'>not-a-child-div</div><div id='myId'></div>";
    page.setContent(htmlContent);
    Locator element = page.locator("#myId .a");
    Object nodesLength = element.evaluateAll("nodes => nodes.length");
    assertEquals(0, nodesLength);
  }
}
