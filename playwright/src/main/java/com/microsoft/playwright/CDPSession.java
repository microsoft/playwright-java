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

import java.util.*;

/**
 * The {@code CDPSession} instances are used to talk raw Chrome Devtools Protocol:
 * <p>
 * protocol methods can be called with {@code session.send} method.
 * <p>
 * protocol events can be subscribed to with {@code session.on} method.
 * <p>
 * Useful links:
 * <p>
 * Documentation on DevTools Protocol can be found here: DevTools Protocol Viewer.
 * <p>
 * Getting Started with DevTools Protocol: https://github.com/aslushnikov/getting-started-with-cdp/blob/master/README.md
 * <p>
 */
public interface CDPSession {
  /**
   * Detaches the CDPSession from the target. Once detached, the CDPSession object won't emit any events and can't be used
   * <p>
   * to send messages.
   */
  void detach();
  default Object send(String method) {
    return send(method, null);
  }
  /**
   * 
   * @param method protocol method name
   * @param params Optional method parameters
   */
  Object send(String method, Object params);
}

