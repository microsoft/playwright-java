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

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Whenever a <a href="https://developer.mozilla.org/en-US/docs/Web/API/WebSocket">{@code WebSocket}</a> route is set up
 * with {@link com.microsoft.playwright.Page#routeWebSocket Page.routeWebSocket()} or {@link
 * com.microsoft.playwright.BrowserContext#routeWebSocket BrowserContext.routeWebSocket()}, the {@code WebSocketRoute}
 * object allows to handle the WebSocket, like an actual server would do.
 *
 * <p> <strong>Mocking</strong>
 *
 * <p> By default, the routed WebSocket will not connect to the server. This way, you can mock entire communcation over the
 * WebSocket. Here is an example that responds to a {@code "request"} with a {@code "response"}.
 * <pre>{@code
 * page.routeWebSocket("wss://example.com/ws", ws -> {
 *   ws.onMessage(frame -> {
 *     if ("request".equals(frame.text()))
 *       ws.send("response");
 *   });
 * });
 * }</pre>
 *
 * <p> Since we do not call {@link com.microsoft.playwright.WebSocketRoute#connectToServer WebSocketRoute.connectToServer()}
 * inside the WebSocket route handler, Playwright assumes that WebSocket will be mocked, and opens the WebSocket inside the
 * page automatically.
 *
 * <p> Here is another example that handles JSON messages:
 * <pre>{@code
 * page.routeWebSocket("wss://example.com/ws", ws -> {
 *   ws.onMessage(frame -> {
 *     JsonObject json = new JsonParser().parse(frame.text()).getAsJsonObject();
 *     if ("question".equals(json.get("request").getAsString())) {
 *       Map<String, String> result = new HashMap();
 *       result.put("response", "answer");
 *       ws.send(gson.toJson(result));
 *     }
 *   });
 * });
 * }</pre>
 *
 * <p> <strong>Intercepting</strong>
 *
 * <p> Alternatively, you may want to connect to the actual server, but intercept messages in-between and modify or block them.
 * Calling {@link com.microsoft.playwright.WebSocketRoute#connectToServer WebSocketRoute.connectToServer()} returns a
 * server-side {@code WebSocketRoute} instance that you can send messages to, or handle incoming messages.
 *
 * <p> Below is an example that modifies some messages sent by the page to the server. Messages sent from the server to the
 * page are left intact, relying on the default forwarding.
 * <pre>{@code
 * page.routeWebSocket("/ws", ws -> {
 *   WebSocketRoute server = ws.connectToServer();
 *   ws.onMessage(frame -> {
 *     if ("request".equals(frame.text()))
 *       server.send("request2");
 *     else
 *       server.send(frame.text());
 *   });
 * });
 * }</pre>
 *
 * <p> After connecting to the server, all **messages are forwarded** between the page and the server by default.
 *
 * <p> However, if you call {@link com.microsoft.playwright.WebSocketRoute#onMessage WebSocketRoute.onMessage()} on the
 * original route, messages from the page to the server **will not be forwarded** anymore, but should instead be handled by
 * the {@code handler}.
 *
 * <p> Similarly, calling {@link com.microsoft.playwright.WebSocketRoute#onMessage WebSocketRoute.onMessage()} on the
 * server-side WebSocket will **stop forwarding messages** from the server to the page, and {@code handler} should take
 * care of them.
 *
 * <p> The following example blocks some messages in both directions. Since it calls {@link
 * com.microsoft.playwright.WebSocketRoute#onMessage WebSocketRoute.onMessage()} in both directions, there is no automatic
 * forwarding at all.
 * <pre>{@code
 * page.routeWebSocket("/ws", ws -> {
 *   WebSocketRoute server = ws.connectToServer();
 *   ws.onMessage(frame -> {
 *     if (!"blocked-from-the-page".equals(frame.text()))
 *       server.send(frame.text());
 *   });
 *   server.onMessage(frame -> {
 *     if (!"blocked-from-the-server".equals(frame.text()))
 *       ws.send(frame.text());
 *   });
 * });
 * }</pre>
 */
public interface WebSocketRoute {
  class CloseOptions {
    /**
     * Optional <a href="https://developer.mozilla.org/en-US/docs/Web/API/WebSocket/close#code">close code</a>.
     */
    public Integer code;
    /**
     * Optional <a href="https://developer.mozilla.org/en-US/docs/Web/API/WebSocket/close#reason">close reason</a>.
     */
    public String reason;

    /**
     * Optional <a href="https://developer.mozilla.org/en-US/docs/Web/API/WebSocket/close#code">close code</a>.
     */
    public CloseOptions setCode(int code) {
      this.code = code;
      return this;
    }
    /**
     * Optional <a href="https://developer.mozilla.org/en-US/docs/Web/API/WebSocket/close#reason">close reason</a>.
     */
    public CloseOptions setReason(String reason) {
      this.reason = reason;
      return this;
    }
  }
  /**
   * Closes one side of the WebSocket connection.
   *
   * @since v1.48
   */
  default void close() {
    close(null);
  }
  /**
   * Closes one side of the WebSocket connection.
   *
   * @since v1.48
   */
  void close(CloseOptions options);
  /**
   * By default, routed WebSocket does not connect to the server, so you can mock entire WebSocket communication. This method
   * connects to the actual WebSocket server, and returns the server-side {@code WebSocketRoute} instance, giving the ability
   * to send and receive messages from the server.
   *
   * <p> Once connected to the server:
   * <ul>
   * <li> Messages received from the server will be **automatically forwarded** to the WebSocket in the page, unless {@link
   * com.microsoft.playwright.WebSocketRoute#onMessage WebSocketRoute.onMessage()} is called on the server-side {@code
   * WebSocketRoute}.</li>
   * <li> Messages sent by the <a href="https://developer.mozilla.org/en-US/docs/Web/API/WebSocket/send">{@code
   * WebSocket.send()}</a> call in the page will be **automatically forwarded** to the server, unless {@link
   * com.microsoft.playwright.WebSocketRoute#onMessage WebSocketRoute.onMessage()} is called on the original {@code
   * WebSocketRoute}.</li>
   * </ul>
   *
   * <p> See examples at the top for more details.
   *
   * @since v1.48
   */
  WebSocketRoute connectToServer();
  /**
   * Allows to handle <a href="https://developer.mozilla.org/en-US/docs/Web/API/WebSocket/close">{@code WebSocket.close}</a>.
   *
   * <p> By default, closing one side of the connection, either in the page or on the server, will close the other side. However,
   * when {@link com.microsoft.playwright.WebSocketRoute#onClose WebSocketRoute.onClose()} handler is set up, the default
   * forwarding of closure is disabled, and handler should take care of it.
   *
   * @param handler Function that will handle WebSocket closure. Received an optional <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/WebSocket/close#code">close code</a> and an optional <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/WebSocket/close#reason">close reason</a>.
   * @since v1.48
   */
  void onClose(BiConsumer<Integer, String> handler);
  /**
   * This method allows to handle messages that are sent by the WebSocket, either from the page or from the server.
   *
   * <p> When called on the original WebSocket route, this method handles messages sent from the page. You can handle this
   * messages by responding to them with {@link com.microsoft.playwright.WebSocketRoute#send WebSocketRoute.send()},
   * forwarding them to the server-side connection returned by {@link com.microsoft.playwright.WebSocketRoute#connectToServer
   * WebSocketRoute.connectToServer()} or do something else.
   *
   * <p> Once this method is called, messages are not automatically forwarded to the server or to the page - you should do that
   * manually by calling {@link com.microsoft.playwright.WebSocketRoute#send WebSocketRoute.send()}. See examples at the top
   * for more details.
   *
   * <p> Calling this method again will override the handler with a new one.
   *
   * @param handler Function that will handle messages.
   * @since v1.48
   */
  void onMessage(Consumer<WebSocketFrame> handler);
  /**
   * Sends a message to the WebSocket. When called on the original WebSocket, sends the message to the page. When called on
   * the result of {@link com.microsoft.playwright.WebSocketRoute#connectToServer WebSocketRoute.connectToServer()}, sends
   * the message to the server. See examples at the top for more details.
   *
   * @param message Message to send.
   * @since v1.48
   */
  void send(String message);
  /**
   * Sends a message to the WebSocket. When called on the original WebSocket, sends the message to the page. When called on
   * the result of {@link com.microsoft.playwright.WebSocketRoute#connectToServer WebSocketRoute.connectToServer()}, sends
   * the message to the server. See examples at the top for more details.
   *
   * @param message Message to send.
   * @since v1.48
   */
  void send(byte[] message);
  /**
   * URL of the WebSocket created in the page.
   *
   * @since v1.48
   */
  String url();
}

