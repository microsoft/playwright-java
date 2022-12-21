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


/**
 * The {@code WebSocketFrame} class represents frames sent over {@code WebSocket} connections in the page. Frame payload is
 * returned by either {@link WebSocketFrame#text WebSocketFrame.text()} or {@link WebSocketFrame#binary
 * WebSocketFrame.binary()} method depending on the its type.
 */
public interface WebSocketFrame {
  /**
   * Returns binary payload.
   *
   * @since v1.9
   */
  byte[] binary();
  /**
   * Returns text payload.
   *
   * @since v1.9
   */
  String text();
}

