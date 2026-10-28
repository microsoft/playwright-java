/**
 * Copyright 2018 Google Inc. All rights reserved.
 * Modifications copyright (c) Microsoft Corporation.
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

package com.microsoft.playwright.tests.page;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/elementhandle-eval-on-selector.spec.ts. */
public class TestElementhandleEvalOnSelector {
  @PlaywrightTest
  @DisplayName("EH-039 should work")
  void shouldWork(Page page, TestServer server) {
    page.setContent("<html><body><div class=\"tweet\"><div class=\"like\">100</div><div class=\"retweets\">10</div></div></body></html>");
    ElementHandle tweet = page.querySelector(".tweet");
    Object content = tweet.evalOnSelector(".like", "node => node.innerText");
    assertEquals("100", content);
  }

  @PlaywrightTest
  @DisplayName("EH-040 should retrieve content from subtree")
  void shouldRetrieveContentFromSubtree(Page page, TestServer server) {
    String htmlContent = "<div class=\"a\">not-a-child-div</div><div id=\"myId\"><div class=\"a\">a-child-div</div></div>";
    page.setContent(htmlContent);
    ElementHandle elementHandle = page.querySelector("#myId");
    Object content = elementHandle.evalOnSelector(".a", "node => node.innerText");
    assertEquals("a-child-div", content);
  }

  @PlaywrightTest
  @DisplayName("EH-041 should throw in case of missing selector")
  @Fixme("java: the message is \"Error { message='Failed to find element matching selector \".a\" name='Error ...\" without the 'elementHandle.$eval: ' prefix")
  void shouldThrowInCaseOfMissingSelector(Page page, TestServer server) {
    String htmlContent = "<div class=\"a\">not-a-child-div</div><div id=\"myId\"></div>";
    page.setContent(htmlContent);
    ElementHandle elementHandle = page.querySelector("#myId");
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> elementHandle.evalOnSelector(".a", "node => node.innerText"));
    assertTrue(error.getMessage().contains("elementHandle.$eval: Failed to find element matching selector \".a\""));
  }

  @PlaywrightTest
  @DisplayName("EH-042 should work for all")
  void shouldWorkForAll(Page page, TestServer server) {
    page.setContent("<html><body><div class=\"tweet\"><div class=\"like\">100</div><div class=\"like\">10</div></div></body></html>");
    ElementHandle tweet = page.querySelector(".tweet");
    Object content = tweet.evalOnSelectorAll(".like", "nodes => nodes.map(n => n.innerText)");
    assertEquals(Arrays.asList("100", "10"), content);
  }

  @PlaywrightTest
  @DisplayName("EH-043 should retrieve content from subtree for all")
  void shouldRetrieveContentFromSubtreeForAll(Page page, TestServer server) {
    String htmlContent = "<div class=\"a\">not-a-child-div</div><div id=\"myId\"><div class=\"a\">a1-child-div</div><div class=\"a\">a2-child-div</div></div>";
    page.setContent(htmlContent);
    ElementHandle elementHandle = page.querySelector("#myId");
    Object content = elementHandle.evalOnSelectorAll(".a", "nodes => nodes.map(n => n.innerText)");
    assertEquals(Arrays.asList("a1-child-div", "a2-child-div"), content);
  }

  @PlaywrightTest
  @DisplayName("EH-044 should not throw in case of missing selector for all")
  void shouldNotThrowInCaseOfMissingSelectorForAll(Page page, TestServer server) {
    String htmlContent = "<div class=\"a\">not-a-child-div</div><div id=\"myId\"></div>";
    page.setContent(htmlContent);
    ElementHandle elementHandle = page.querySelector("#myId");
    Object nodesLength = elementHandle.evalOnSelectorAll(".a", "nodes => nodes.length");
    assertEquals(0, nodesLength);
  }
}
