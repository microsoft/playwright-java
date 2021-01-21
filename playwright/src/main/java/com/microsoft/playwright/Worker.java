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
import java.util.function.Consumer;

/**
 * The Worker class represents a [WebWorker](https://developer.mozilla.org/en-US/docs/Web/API/Web_Workers_API). {@code worker}
 * event is emitted on the page object to signal a worker creation. {@code close} event is emitted on the worker object when the
 * worker is gone.
 */
public interface Worker {
  enum EventType {
    CLOSE,
  }

  void addListener(EventType type, Listener<EventType> listener);
  void removeListener(EventType type, Listener<EventType> listener);

  void onClose(Consumer<Worker> handler);
  void offClose(Consumer<Worker> handler);


  class WaitForCloseOptions {
    public Double timeout;
    public WaitForCloseOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  Worker waitForClose(Runnable code, WaitForCloseOptions options);
  default Worker waitForClose(Runnable code) { return waitForClose(code, null); }

  default Object evaluate(String pageFunction) {
    return evaluate(pageFunction, null);
  }
  /**
   * Returns the return value of {@code pageFunction}
   *
   * <p> If the function passed to the {@code worker.evaluate} returns a [Promise], then {@code worker.evaluate} would wait for the promise
   * to resolve and return its value.
   *
   * <p> If the function passed to the {@code worker.evaluate} returns a non-[Serializable] value, then {@code worker.evaluate} returns
   * {@code undefined}. DevTools Protocol also supports transferring some additional values that are not serializable by {@code JSON}:
   * {@code -0}, {@code NaN}, {@code Infinity}, {@code -Infinity}, and bigint literals.
   *
   * @param pageFunction Function to be evaluated in the worker context
   * @param arg Optional argument to pass to {@code pageFunction}
   */
  Object evaluate(String pageFunction, Object arg);
  default JSHandle evaluateHandle(String pageFunction) {
    return evaluateHandle(pageFunction, null);
  }
  /**
   * Returns the return value of {@code pageFunction} as in-page object (JSHandle).
   *
   * <p> The only difference between {@code worker.evaluate} and {@code worker.evaluateHandle} is that {@code worker.evaluateHandle} returns
   * in-page object (JSHandle).
   *
   * <p> If the function passed to the {@code worker.evaluateHandle} returns a [Promise], then {@code worker.evaluateHandle} would wait for
   * the promise to resolve and return its value.
   *
   * @param pageFunction Function to be evaluated in the page context
   * @param arg Optional argument to pass to {@code pageFunction}
   */
  JSHandle evaluateHandle(String pageFunction, Object arg);
  String url();
}

