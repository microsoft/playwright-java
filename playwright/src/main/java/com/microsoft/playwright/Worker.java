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

  void onClose(Consumer<Worker> handler);
  void offClose(Consumer<Worker> handler);

  class WaitForCloseOptions {
    /**
     * Maximum time to wait for in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the [{@code method: BrowserContext.setDefaultTimeout}].
     */
    public Double timeout;

    public WaitForCloseOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  default Object evaluate(String expression) {
    return evaluate(expression, null);
  }
  /**
   * Returns the return value of {@code expression}.
   *
   * <p> If the function passed to the [{@code method: Worker.evaluate}] returns a [Promise], then [{@code method: Worker.evaluate}] would
   * wait for the promise to resolve and return its value.
   *
   * <p> If the function passed to the [{@code method: Worker.evaluate}] returns a non-[Serializable] value, then
   * [{@code method: Worker.evaluate}] returns {@code undefined}. Playwright also supports transferring some  additional values that are
   * not serializable by {@code JSON}: {@code -0}, {@code NaN}, {@code Infinity}, {@code -Infinity}.
   *
   * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
   * as a function. Otherwise, evaluated as an expression.
   * @param arg Optional argument to pass to {@code expression}.
   */
  Object evaluate(String expression, Object arg);
  default JSHandle evaluateHandle(String expression) {
    return evaluateHandle(expression, null);
  }
  /**
   * Returns the return value of {@code expression} as a {@code JSHandle}.
   *
   * <p> The only difference between [{@code method: Worker.evaluate}] and [{@code method: Worker.evaluateHandle}] is that
   * [{@code method: Worker.evaluateHandle}] returns {@code JSHandle}.
   *
   * <p> If the function passed to the [{@code method: Worker.evaluateHandle}] returns a [Promise], then
   * [{@code method: Worker.evaluateHandle}] would wait for the promise to resolve and return its value.
   *
   * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
   * as a function. Otherwise, evaluated as an expression.
   * @param arg Optional argument to pass to {@code expression}.
   */
  JSHandle evaluateHandle(String expression, Object arg);
  String url();
  default Worker waitForClose(Runnable callback) {
    return waitForClose(null, callback);
  }
  /**
   * Performs action and waits for the Worker to close.
   *
   * @param callback Callback that performs the action triggering the event.
   */
  Worker waitForClose(WaitForCloseOptions options, Runnable callback);
}

