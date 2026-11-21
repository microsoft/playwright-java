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

// Port of network.ts.
package com.microsoft.playwright.impl;

import com.microsoft.playwright.WebSocketFrame;

import java.util.Base64;

// java: the { payload: string | Buffer } of the WebSocket frame events and the messages of WebSocketRoute.
class WebSocketFrameImpl implements WebSocketFrame {
  private final byte[] bytes;
  private final String text;

  WebSocketFrameImpl(String payload, boolean isBase64) {
    if (isBase64) {
      bytes = Base64.getDecoder().decode(payload);
      text = null;
    } else {
      bytes = null;
      text = payload;
    }
  }

  @Override
  public byte[] binary() {
    return bytes;
  }

  @Override
  public String text() {
    return text;
  }
}
