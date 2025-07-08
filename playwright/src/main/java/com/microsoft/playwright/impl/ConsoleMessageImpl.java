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

package com.microsoft.playwright.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.playwright.ConsoleMessage;
import com.microsoft.playwright.JSHandle;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.ConsoleMessageType;

import java.util.ArrayList;
import java.util.List;

public class ConsoleMessageImpl implements ConsoleMessage {
  private final Connection connection;
  private PageImpl page;
  private final JsonObject initializer;

  public ConsoleMessageImpl(Connection connection, JsonObject initializer) {
    this.connection = connection;
    // Note: currently, we only report console messages for pages and they always have a page.
    // However, in the future we might report console messages for service workers or something else,
    // where page() would be null.
    if (initializer.has("page")) {
      page = connection.getExistingObject(initializer.getAsJsonObject("page").get("guid").getAsString());
    }
    this.initializer = initializer;
  }

  public ConsoleMessageType type() {
    String value = initializer.get("type").getAsString();
    switch (value) {
      case "log": return ConsoleMessageType.LOG;
      case "debug": return ConsoleMessageType.DEBUG;
      case "info": return ConsoleMessageType.INFO;
      case "error": return ConsoleMessageType.ERROR;
      case "warning": return ConsoleMessageType.WARNING;
      case "dir": return ConsoleMessageType.DIR;
      case "dirxml": return ConsoleMessageType.DIRXML;
      case "table": return ConsoleMessageType.TABLE;
      case "trace": return ConsoleMessageType.TRACE;
      case "clear": return ConsoleMessageType.CLEAR;
      case "startGroup": return ConsoleMessageType.STARTGROUP;
      case "startGroupCollapsed": return ConsoleMessageType.STARTGROUPCOLLAPSED;
      case "endGroup": return ConsoleMessageType.ENDGROUP;
      case "assert": return ConsoleMessageType.ASSERT;
      case "profile": return ConsoleMessageType.PROFILE;
      case "profileEnd": return ConsoleMessageType.PROFILEEND;
      case "count": return ConsoleMessageType.COUNT;
      case "timeEnd": return ConsoleMessageType.TIMEEND;
      default: throw new PlaywrightException("Unexpected console message type: " + value);
    }
  }

  public String text() {
    return initializer.get("text").getAsString();
  }

  @Override
  public List<JSHandle> args() {
    List<JSHandle> result = new ArrayList<>();
    for (JsonElement item : initializer.getAsJsonArray("args")) {
      result.add(connection.getExistingObject(item.getAsJsonObject().get("guid").getAsString()));
    }
    return result;
  }

  @Override
  public String location() {
    JsonObject location = initializer.getAsJsonObject("location");
    return location.get("url").getAsString() + ":" +
      location.get("lineNumber").getAsNumber() + ":" +
      location.get("columnNumber").getAsNumber();
  }

  @Override
  public PageImpl page() {
    return page;
  }
}
