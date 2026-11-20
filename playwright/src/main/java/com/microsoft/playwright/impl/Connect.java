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

// Port of connect.ts.
package com.microsoft.playwright.impl;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.impl.protocol.*;

import java.io.IOException;
import java.util.Map;
import java.util.function.Consumer;

import static com.microsoft.playwright.impl.Time.monotonicTime;

final class Connect {
  private Connect() {
  }

  // java: upstream's params is one ConnectOptions object with browserName and endpoint in it.
  static BrowserImpl connectToBrowser(PlaywrightImpl playwright, String browserName, String endpoint, BrowserType.ConnectOptions params) {
    double timeout = params.timeout == null ? 0 : params.timeout;
    double deadline = timeout != 0 ? monotonicTime() + timeout : 0;
    JsonObject headers = new JsonObject();
    if (browserName != null) {
      headers.addProperty("x-playwright-browser", browserName);
    }
    if (params.headers != null) {
      for (Map.Entry<String, String> entry : params.headers.entrySet()) {
        headers.addProperty(entry.getKey(), entry.getValue());
      }
    }
    LocalUtilsConnectParams connectParams = new LocalUtilsConnectParams();
    connectParams.endpoint = endpoint;
    connectParams.headers = headers;
    connectParams.exposeNetwork = params.exposeNetwork;
    connectParams.slowMo = params.slowMo;
    // java: __testHookRedirectPortForwarding is a socksProxyRedirectPortForTest field of a ConnectOptions subclass in the tests.
    JsonElement redirectPort = new Gson().toJsonTree(params).getAsJsonObject().get("socksProxyRedirectPortForTest");
    if (redirectPort != null && !redirectPort.isJsonNull()) {
      connectParams.socksProxyRedirectPortForTest = redirectPort.getAsInt();
    }
    BrowserImpl[] browser = {null};
    Connection connection = connectToEndpoint(playwright.connection, connectParams, timeout, () -> {
      // Emulate all pages, contexts and the browser closing upon disconnect.
      if (browser[0] != null) {
        for (BrowserContext context : browser[0].contexts()) {
          for (Page page : context.pages()) {
            ((PageImpl) page).onClose();
          }
          ((BrowserContextImpl) context).onClose();
        }
        // java: no setTimeout, the browser is closed right after its contexts.
        browser[0].didClose();
      }
    });

    // java: raceAgainstDeadline is the deadline of Connection.initializePlaywright, which throws a TimeoutError
    // with the message "Timeout <timeout>ms exceeded".
    try {
      PlaywrightImpl remotePlaywright = deadline == 0 ? connection.initializePlaywright() : connection.initializePlaywright(deadline, timeout);
      BrowserImpl preLaunchedBrowser = connection.protocolGson().fromJson(remotePlaywright.initializer, PlaywrightInitializer.class).preLaunchedBrowser;
      if (preLaunchedBrowser == null) {
        closeConnection(connection);
        throw new PlaywrightException("Malformed endpoint. Did you use BrowserType.launchServer method?");
      }
      // java: upstream assigns the inner `playwright` to itself; here the remote Playwright shares the selectors of
      // this one, because getByTestId reads the test id attribute through connection.playwright.
      remotePlaywright.selectors = playwright.selectors;
      browser[0] = preLaunchedBrowser;
      browser[0].shouldCloseConnectionOnClose = true;
      browser[0].on(Events.Browser.DISCONNECTED, (Consumer<Browser>) b -> closeConnection(connection));
      return browser[0];
    } catch (TimeoutError e) {
      closeConnection(connection);
      throw e;
    }
  }

  // java: the Connection emits no 'close' event, onClose is the 'close' listener of connectToBrowser; it runs when the
  // pipe closes, which happens both when the server goes away and when Connection.close closes the pipe.
  static Connection connectToEndpoint(Connection parentConnection, LocalUtilsConnectParams params, double timeout, Runnable onClose) {
    LocalUtils localUtils = parentConnection.localUtils();
    // java: the JsonPipe is the transport (JsonPipeTransport upstream), there is no WebSocketTransport. The headers
    // of the connect result are not used by the Java Connection.
    JsonPipe transport = localUtils.channel.connect(params, timeout).pipe;
    Connection connection = new Connection(transport, parentConnection.env, localUtils);
    connection.markAsRemote();
    // java: Connection.close closes the transport itself, there is no connection.on('close', () => transport.close()).

    transport.onClose(pipe -> {
      // java: JsonPipe.closeReason() defaults to "Browser has been closed" when the server gives no reason.
      closeConnection(connection, pipe.closeReason());
      onClose.run();
    });
    // java: there is no onmessage/onMessage wiring, the Connection sends to the JsonPipe and polls it for messages,
    // and an error of dispatch propagates out of the call that pumped the message.
    return connection;
  }

  // java: Connection.close declares IOException.
  private static void closeConnection(Connection connection) {
    closeConnection(connection, null);
  }

  private static void closeConnection(Connection connection, String reason) {
    try {
      connection.close(reason);
    } catch (IOException e) {
      throw new PlaywrightException("Failed to close the connection", e);
    }
  }
}
