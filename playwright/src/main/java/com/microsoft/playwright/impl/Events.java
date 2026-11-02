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
// Port of packages/playwright-core/src/client/events.ts
package com.microsoft.playwright.impl;

final class Events {
  private Events() {
  }

  static final class Browser {
    static final String CONTEXT = "context";
    static final String DISCONNECTED = "disconnected";
  }

  static final class Debugger {
    static final String PAUSED_STATE_CHANGED = "pausedstatechanged";
    static final String API_CALLS_UPDATED = "apicallsupdated";
  }

  static final class BrowserContext {
    static final String CONSOLE = "console";
    static final String CLOSE = "close";
    static final String DIALOG = "dialog";
    static final String DIALOG_CLOSED = "dialogclosed";
    static final String DOWNLOAD = "download";
    static final String FRAME_ATTACHED = "frameattached";
    static final String FRAME_DETACHED = "framedetached";
    static final String FRAME_NAVIGATED = "framenavigated";
    static final String PAGE = "page";
    static final String PAGE_CLOSE = "pageclose";
    static final String PAGE_LOAD = "pageload";
    // Can't use just 'error' due to node.js special treatment of error events.
    static final String WEB_ERROR = "weberror";
    static final String BACKGROUND_PAGE = "backgroundpage";  // Deprecated in v1.56, never emitted anymore.
    static final String SERVICE_WORKER = "serviceworker";
    static final String REQUEST = "request";
    static final String RESPONSE = "response";
    static final String REQUEST_FAILED = "requestfailed";
    static final String REQUEST_FINISHED = "requestfinished";
  }

  static final class Page {
    static final String CLOSE = "close";
    static final String CRASH = "crash";
    static final String CONSOLE = "console";
    static final String DIALOG = "dialog";
    static final String DIALOG_CLOSED = "dialogclosed";
    static final String DOWNLOAD = "download";
    static final String FILE_CHOOSER = "filechooser";
    static final String DOM_CONTENT_LOADED = "domcontentloaded";
    // Can't use just 'error' due to node.js special treatment of error events.
    static final String PAGE_ERROR = "pageerror";
    static final String REQUEST = "request";
    static final String RESPONSE = "response";
    static final String REQUEST_FAILED = "requestfailed";
    static final String REQUEST_FINISHED = "requestfinished";
    static final String FRAME_ATTACHED = "frameattached";
    static final String FRAME_DETACHED = "framedetached";
    static final String FRAME_NAVIGATED = "framenavigated";
    static final String LOAD = "load";
    static final String POPUP = "popup";
    static final String WEB_SOCKET = "websocket";
    static final String WORKER = "worker";
  }

  static final class WebSocket {
    static final String CLOSE = "close";
    static final String ERROR = "socketerror";
    static final String FRAME_RECEIVED = "framereceived";
    static final String FRAME_SENT = "framesent";
  }

  static final class Worker {
    static final String CLOSE = "close";
    static final String CONSOLE = "console";
  }

  // java: events that upstream emits as string literals (cdpSession.ts) or does not have (the Java
  // frame waits that upstream implements with promises, the json pipe close that the transport observes).
  static final class CDPSession {
    static final String CLOSE = "close";
  }

  static final class Frame {
    static final String LOAD_STATE = "loadstate";
    static final String NAVIGATED = "navigated";
  }

  static final class JsonPipe {
    static final String CLOSE = "close";
  }
}
