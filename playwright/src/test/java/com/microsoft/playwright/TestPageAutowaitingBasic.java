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

import com.microsoft.playwright.options.LoadState;
import org.junit.jupiter.api.Test;

import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestPageAutowaitingBasic extends TestBase {
  @Test
  void shouldWorkWithNoWaitAfterTrue() {
    server.setRoute("/empty.html", exchange -> {});
    page.setContent("<a id='anchor' href='${server.EMPTY_PAGE}'>empty.html</a>");
    page.click("a", new Page.ClickOptions().setNoWaitAfter(true));
  }

  @Test
  void shouldWorkWithDblclickNoWaitAfterTrue() {
    server.setRoute("/empty.html", exchange -> {});
    page.setContent("<a id='anchor' href='${server.EMPTY_PAGE}'>empty.html</a>");
    page.dblclick("a", new Page.DblclickOptions().setNoWaitAfter(true));
  }

  @Test
  void shouldWorkWithWaitForLoadStateLoad() {
    List<String> messages = new ArrayList<>();
    server.setRoute("/empty.html", exchange -> {
      messages.add("route");
      exchange.getResponseHeaders().add("content-type", "text/html");
      exchange.sendResponseHeaders(200, 0);
      try (OutputStreamWriter writer = new OutputStreamWriter(exchange.getResponseBody())) {
        writer.write("<link rel='stylesheet' href='./one-style.css'>");
      } catch (RuntimeException e) {
        e.printStackTrace();
        throw e;
      }
    });
    page.setContent("<a id='anchor' href='" + server.EMPTY_PAGE + "'>empty.html</a>");

    page.onLoad(p -> messages.add("clickload"));
    page.click("a");
    page.waitForLoadState(LoadState.LOAD);
    messages.add("load");
    assertEquals(asList("route", "clickload", "load"), messages);
  }
}
