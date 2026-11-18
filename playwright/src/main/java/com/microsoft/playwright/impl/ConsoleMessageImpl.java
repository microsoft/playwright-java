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

// Port of consoleMessage.ts.
package com.microsoft.playwright.impl;

import com.microsoft.playwright.impl.protocol.*;
import com.microsoft.playwright.ConsoleMessage;
import com.microsoft.playwright.JSHandle;
import com.microsoft.playwright.Worker;

import java.util.ArrayList;
import java.util.List;

public class ConsoleMessageImpl implements ConsoleMessage {
  private final PageImpl page;
  private final WorkerImpl worker;
  private final String type;
  private final String text;
  private final List<JSHandleImpl> args;
  private final String url;
  private final int lineNumber;
  private final int columnNumber;
  private final double timestamp;

  ConsoleMessageImpl(BrowserContextConsoleEvent event) {
    this(event.page, event.worker, event.type, event.text, event.args,
      event.location.url, event.location.lineNumber, event.location.columnNumber, event.timestamp);
  }

  ConsoleMessageImpl(PageImpl page, PageConsoleMessagesResult.PageConsoleMessagesResultMessages message) {
    this(page, null, message.type, message.text, message.args,
      message.location.url, message.location.lineNumber, message.location.columnNumber, message.timestamp);
  }

  private ConsoleMessageImpl(PageImpl page, WorkerImpl worker, String type, String text, List<JSHandleImpl> args,
                             String url, int lineNumber, int columnNumber, double timestamp) {
    this.page = page;
    this.worker = worker;
    this.type = type;
    this.text = text;
    this.args = args;
    this.url = url;
    this.lineNumber = lineNumber;
    this.columnNumber = columnNumber;
    this.timestamp = timestamp;
  }

  @Override
  public Worker worker() {
    return worker;
  }

  @Override
  public PageImpl page() {
    return page;
  }

  @Override
  public String type() {
    return type;
  }

  @Override
  public String text() {
    return text;
  }

  @Override
  public List<JSHandle> args() {
    return new ArrayList<JSHandle>(args);
  }

  @Override
  public String location() {
    // java: the public API returns a formatted string, upstream returns { url, line, column }.
    return url + ":" + lineNumber + ":" + columnNumber;
  }

  @Override
  public double timestamp() {
    return timestamp;
  }
}
