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
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

public class TestPageEventRequest extends TestBase {
  @Test
  void shouldReturnLastRequests() throws ExecutionException, InterruptedException {
    page.navigate(server.PREFIX + "/title.html");

    // Set up routes for 200 requests
    for (int i = 0; i < 200; ++i) {
      final int index = i;
      server.setRoute("/fetch-" + i, exchange -> {
        exchange.sendResponseHeaders(200, 0);
        exchange.getResponseBody().write(("url:" + server.PREFIX + exchange.getRequestURI().toString()).getBytes());
        exchange.getResponseBody().close();
      });
    }

    // #0 is the navigation request, so start with #1.
    for (int i = 0; i < 99; ++i) {
      page.evaluate("url => fetch(url)", server.PREFIX + "/fetch-" + i);
    }
    List<Request> first99Requests = page.requests();
    first99Requests.remove(0); // Remove the navigation request

    for (int i = 99; i < 199; ++i) {
      page.evaluate("url => fetch(url)", server.PREFIX + "/fetch-" + i);
    }
    List<Request> last100Requests = page.requests();
    List<Request> allRequests = new ArrayList<>();
    allRequests.addAll(first99Requests);
    allRequests.addAll(last100Requests);

    // All 199 requests are fully functional.
    int index = 0;
    for (Request request : allRequests) {
      Response response = request.response();
      assertEquals("url:" + server.PREFIX + "/fetch-" + index, response.text());
      assertEquals(server.PREFIX + "/fetch-" + index, request.url());
      index++;
    }
  }
}
