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

import java.util.function.Consumer;
import com.google.gson.JsonObject;

/**
 * The {@code CDPSession} instances are used to talk raw Chrome Devtools Protocol:
 * <ul>
 * <li> protocol methods can be called with {@code session.send} method.</li>
 * <li> protocol events can be subscribed to with {@code session.on} method.</li>
 * </ul>
 *
 * <p> Useful links:
 * <ul>
 * <li> Documentation on DevTools Protocol can be found here: <a
 * href="https://chromedevtools.github.io/devtools-protocol/">DevTools Protocol Viewer</a>.</li>
 * <li> Getting Started with DevTools Protocol: https://github.com/aslushnikov/getting-started-with-cdp/blob/master/README.md</li>
 * <pre>{@code
 * CDPSession client = page.context().newCDPSession(page);
 * client.send("Runtime.enable");
 *
 * client.on("Animation.animationCreated", (event) -> System.out.println("Animation created!"));
 *
 * JsonObject response = client.send("Animation.getPlaybackRate");
 * double playbackRate = response.get("playbackRate").getAsDouble();
 * System.out.println("playback rate is " + playbackRate);
 *
 * JsonObject params = new JsonObject();
 * params.addProperty("playbackRate", playbackRate / 2);
 * client.send("Animation.setPlaybackRate", params);
 * }</pre>
 * </ul>
 */
public interface CDPSession {
  /**
   * Detaches the CDPSession from the target. Once detached, the CDPSession object won't emit any events and can't be used to
   * send messages.
   *
   * @since v1.8
   */
  void detach();
  /**
   *
   *
   * @param method Protocol method name.
   * @since v1.8
   */
  default JsonObject send(String method) {
    return send(method, null);
  }
  /**
   *
   *
   * @param method Protocol method name.
   * @param args Optional method parameters.
   * @since v1.8
   */
  JsonObject send(String method, JsonObject args);
  /**
   * Register an event handler for events with the specified event name. The given handler will be called for every event
   * with the given name.
   *
   * @param eventName CDP event name.
   * @param handler Event handler.
   * @since v1.37
   */
  void on(String eventName, Consumer<JsonObject> handler);
  /**
   * Unregister an event handler for events with the specified event name. The given handler will not be called anymore for
   * events with the given name.
   *
   * @param eventName CDP event name.
   * @param handler Event handler.
   * @since v1.37
   */
  void off(String eventName, Consumer<JsonObject> handler);
}

