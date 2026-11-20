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
  PageImpl page;
  // java: no service worker or connectToWorker API, so there is no _context, _closeReason or _disconnect.
  final LongStandingScope closedScope = new LongStandingScope(this);

  final WorkerChannel channel = new WorkerChannel(this);

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
    if ("close".equals(event)) {
      if (page != null) {
        page.workers.remove(this);
      }
      emit(Events.Worker.CLOSE, this);
    }
  }

  TargetClosedError closeErrorWithReason() {
    if (page != null) {
      return page.closeErrorWithReason();
    }
    return new TargetClosedError();
  }

  @Override
  public String url() {
    return initializer.get("url").getAsString();
  }

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

  // java: the predicate and timeout are passed separately and the code to run is a Runnable.
  private <T> T waitForEvent(String event, Predicate<T> predicate, Double timeout, Runnable code) {
    TimeoutSettings timeoutSettings = page != null ? page.timeoutSettings : new TimeoutSettings();
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
