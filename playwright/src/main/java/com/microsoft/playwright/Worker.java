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

/**
 * The Worker class represents a <a href="https://developer.mozilla.org/en-US/docs/Web/API/Web_Workers_API">WebWorker</a>.
 * {@code worker} event is emitted on the page object to signal a worker creation. {@code close} event is emitted on the
 * worker object when the worker is gone.
 * <pre>{@code
 * page.onWorker(worker -> {
 *   System.out.println("Worker created: " + worker.url());
 *   worker.onClose(worker1 -> System.out.println("Worker destroyed: " + worker1.url()));
 * });
 * System.out.println("Current workers:");
 * for (Worker worker : page.workers())
 *   System.out.println("  " + worker.url());
 * }</pre>
 */
public interface Worker {

  /**
   * Emitted when this dedicated <a href="https://developer.mozilla.org/en-US/docs/Web/API/Web_Workers_API">WebWorker</a> is
   * terminated.
   */
  void onClose(Consumer<Worker> handler);
  /**
   * Removes handler that was previously added with {@link #onClose onClose(handler)}.
   */
  void offClose(Consumer<Worker> handler);

  class WaitForCloseOptions {
    /**
     * Maximum time to wait for in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The
     * default value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}.
     */
    public Double timeout;

    /**
     * Maximum time to wait for in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The
     * default value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}.
     */
    public WaitForCloseOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  /**
   * Returns the return value of {@code expression}.
   *
   * <p> If the function passed to the {@link Worker#evaluate Worker.evaluate()} returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
   * Worker#evaluate Worker.evaluate()} would wait for the promise to resolve and return its value.
   *
   * <p> If the function passed to the {@link Worker#evaluate Worker.evaluate()} returns a non-[Serializable] value, then {@link
   * Worker#evaluate Worker.evaluate()} returns {@code undefined}. Playwright also supports transferring some additional
   * values that are not serializable by {@code JSON}: {@code -0}, {@code NaN}, {@code Infinity}, {@code -Infinity}.
   *
   * @param expression JavaScript expression to be evaluated in the browser context. If the expression evaluates to a function, the function is
   * automatically invoked.
   * @since v1.8
   */
  default Object evaluate(String expression) {
    return evaluate(expression, null);
  }
  /**
   * Returns the return value of {@code expression}.
   *
   * <p> If the function passed to the {@link Worker#evaluate Worker.evaluate()} returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
   * Worker#evaluate Worker.evaluate()} would wait for the promise to resolve and return its value.
   *
   * <p> If the function passed to the {@link Worker#evaluate Worker.evaluate()} returns a non-[Serializable] value, then {@link
   * Worker#evaluate Worker.evaluate()} returns {@code undefined}. Playwright also supports transferring some additional
   * values that are not serializable by {@code JSON}: {@code -0}, {@code NaN}, {@code Infinity}, {@code -Infinity}.
   *
   * @param expression JavaScript expression to be evaluated in the browser context. If the expression evaluates to a function, the function is
   * automatically invoked.
   * @param arg Optional argument to pass to {@code expression}.
   * @since v1.8
   */
  Object evaluate(String expression, Object arg);
  /**
   * Returns the return value of {@code expression} as a {@code JSHandle}.
   *
   * <p> The only difference between {@link Worker#evaluate Worker.evaluate()} and {@link Worker#evaluateHandle
   * Worker.evaluateHandle()} is that {@link Worker#evaluateHandle Worker.evaluateHandle()} returns {@code JSHandle}.
   *
   * <p> If the function passed to the {@link Worker#evaluateHandle Worker.evaluateHandle()} returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
   * Worker#evaluateHandle Worker.evaluateHandle()} would wait for the promise to resolve and return its value.
   *
   * @param expression JavaScript expression to be evaluated in the browser context. If the expression evaluates to a function, the function is
   * automatically invoked.
   * @since v1.8
   */
  default JSHandle evaluateHandle(String expression) {
    return evaluateHandle(expression, null);
  }
  /**
   * Returns the return value of {@code expression} as a {@code JSHandle}.
   *
   * <p> The only difference between {@link Worker#evaluate Worker.evaluate()} and {@link Worker#evaluateHandle
   * Worker.evaluateHandle()} is that {@link Worker#evaluateHandle Worker.evaluateHandle()} returns {@code JSHandle}.
   *
   * <p> If the function passed to the {@link Worker#evaluateHandle Worker.evaluateHandle()} returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
   * Worker#evaluateHandle Worker.evaluateHandle()} would wait for the promise to resolve and return its value.
   *
   * @param expression JavaScript expression to be evaluated in the browser context. If the expression evaluates to a function, the function is
   * automatically invoked.
   * @param arg Optional argument to pass to {@code expression}.
   * @since v1.8
   */
  JSHandle evaluateHandle(String expression, Object arg);
  /**
   *
   *
   * @since v1.8
   */
  String url();
  /**
   * Performs action and waits for the Worker to close.
   *
   * @param callback Callback that performs the action triggering the event.
   * @since v1.10
   */
  default Worker waitForClose(Runnable callback) {
    return waitForClose(null, callback);
  }
  /**
   * Performs action and waits for the Worker to close.
   *
   * @param callback Callback that performs the action triggering the event.
   * @since v1.10
   */
  Worker waitForClose(WaitForCloseOptions options, Runnable callback);
}

