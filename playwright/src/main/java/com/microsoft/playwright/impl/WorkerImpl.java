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

// Port of worker.ts.
package com.microsoft.playwright.impl;

import com.microsoft.playwright.impl.protocol.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.playwright.ConsoleMessage;
import com.microsoft.playwright.JSHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Worker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.microsoft.playwright.impl.Serialization.*;

public class WorkerImpl extends ChannelOwner implements Worker {
  PageImpl page;  // Set for web workers.
  BrowserContextImpl context;  // Set for service workers.
  final LongStandingScope closedScope = new LongStandingScope(this);
  private String closeReason;

  final WorkerChannel channel = new WorkerChannel(this);

  // java: fromNullable and from are not ported, generated results already hold the impl object.

  WorkerImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
    Map<String, String> mapping = new HashMap<>();
    mapping.put(Events.Worker.CONSOLE, "console");
    setEventToSubscriptionMapping(mapping);
    on(Events.Worker.CLOSE, new Consumer<Worker>() {
      @Override
      public void accept(Worker worker) {
        off(Events.Worker.CLOSE, this);
        closedScope.close(closeErrorWithReason());
      }
    });
  }

  // java: upstream subscribes in the constructor with this._channel.on(...); the Java channel dispatches events here.
  @Override
  protected void handleEvent(String event, JsonObject params) {
    switch (event) {
      case "console": {
        WorkerConsoleEvent e = connection.protocolGson().fromJson(params, WorkerConsoleEvent.class);
        // Note: we only receive console events here for workers from "chromium._connectToWorker".
        emit(Events.Worker.CONSOLE, new ConsoleMessageImpl(e, null, this));
        break;
      }
      case "close": {
        if (page != null) {
          page.workers.remove(this);
        }
        if (context != null) {
          context.serviceWorkers.remove(this);
        }
        emit(Events.Worker.CLOSE, this);
        break;
      }
    }
  }

  TargetClosedError closeErrorWithReason() {
    if (closeReason != null) {
      return new TargetClosedError(closeReason);
    }
    if (page != null) {
      return page.closeErrorWithReason();
    }
    return new TargetClosedError();
  }

  @Override
  public String url() {
    return initializer.get("url").getAsString();
  }

  // java: assertMaxArguments is not ported, a JS typing check; isFunction is not sent, the Java pageFunction is a string.
  @Override
  public Object evaluate(String pageFunction, Object arg) {
    WorkerEvaluateExpressionParams params = new WorkerEvaluateExpressionParams();
    params.expression = pageFunction;
    params.arg = serializeArgument(arg);
    return deserialize(channel.evaluateExpression(params, NO_TIMEOUT).value);
  }

  @Override
  public JSHandle evaluateHandle(String pageFunction, Object arg) {
    WorkerEvaluateExpressionHandleParams params = new WorkerEvaluateExpressionHandleParams();
    params.expression = pageFunction;
    params.arg = serializeArgument(arg);
    return channel.evaluateExpressionHandle(params, NO_TIMEOUT).handle;
  }

  // java: the predicate and timeout are passed separately and the code to run is a Runnable; @apiCall() is not
  // applied, the Runnable runs inside the wait and its calls would join the api zone.
  private <T> T waitForEvent(String event, Predicate<T> predicate, Double timeout, Runnable code) {
    TimeoutSettings timeoutSettings = page != null ? page.timeoutSettings : context != null ? context.timeoutSettings : new TimeoutSettings();
    double timeoutMs = timeoutSettings.timeout(timeout);
    Waiter waiter = Waiter.createForEvent(this, event);
    waiter.rejectOnTimeout(timeoutMs, "Timeout " + Waiter.formatTimeout(timeoutMs) + "ms exceeded while waiting for event \"" + event + "\"");
    if (!Events.Worker.CLOSE.equals(event)) {
      waiter.rejectOnEvent(this, Events.Worker.CLOSE, () -> closeErrorWithReason());
    }
    T result = waiter.waitForEvent(this, event, predicate, code);
    waiter.dispose();
    return result;
  }

  // java: the { reason } options are the protocol params.
  void disconnect(WorkerDisconnectParams options) {
    if (options == null) {
      options = new WorkerDisconnectParams();
    }
    closeReason = options.reason;
    try {
      channel.disconnect(options, NO_TIMEOUT);
    } catch (TargetClosedError e) {
      return;
    }
  }

  // java: typed on/off/waitFor overloads of the generated Worker interface.
  @Override
  public void onClose(Consumer<Worker> handler) {
    on(Events.Worker.CLOSE, handler);
  }

  @Override
  public void offClose(Consumer<Worker> handler) {
    off(Events.Worker.CLOSE, handler);
  }

  @Override
  public void onConsole(Consumer<ConsoleMessage> handler) {
    on(Events.Worker.CONSOLE, handler);
  }

  @Override
  public void offConsole(Consumer<ConsoleMessage> handler) {
    off(Events.Worker.CONSOLE, handler);
  }

  @Override
  public Worker waitForClose(WaitForCloseOptions options, Runnable code) {
    if (options == null) {
      options = new WaitForCloseOptions();
    }
    return waitForEvent(Events.Worker.CLOSE, null, options.timeout, code);
  }

  @Override
  public ConsoleMessage waitForConsoleMessage(WaitForConsoleMessageOptions options, Runnable code) {
    if (options == null) {
      options = new WaitForConsoleMessageOptions();
    }
    return waitForEvent(Events.Worker.CONSOLE, options.predicate, options.timeout, code);
  }
}
