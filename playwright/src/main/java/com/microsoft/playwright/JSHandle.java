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

/**
 * JSHandle represents an in-page JavaScript object. JSHandles can be created with the [{@code method: Page.evaluateHandle}]
 * method.
 *
 * <p> JSHandle prevents the referenced JavaScript object being garbage collected unless the handle is exposed with
 * [{@code method: JSHandle.dispose}]. JSHandles are auto-disposed when their origin frame gets navigated or the parent context
 * gets destroyed.
 *
 * <p> JSHandle instances can be used as an argument in [{@code method: Page.evalOnSelector}], [{@code method: Page.evaluate}] and
 * [{@code method: Page.evaluateHandle}] methods.
 */
public interface JSHandle {
  /**
   * Returns either {@code null} or the object handle itself, if the object handle is an instance of {@code ElementHandle}.
   */
  ElementHandle asElement();
  /**
   * The {@code jsHandle.dispose} method stops referencing the element handle.
   */
  void dispose();
  default Object evaluate(String expression) {
    return evaluate(expression, null);
  }
  /**
   * Returns the return value of {@code expression}.
   *
   * <p> This method passes this handle as the first argument to {@code expression}.
   *
   * <p> If {@code expression} returns a [Promise], then {@code handle.evaluate} would wait for the promise to resolve and return its value.
   *
   *
   * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
   * as a function. Otherwise, evaluated as an expression.
   * @param arg Optional argument to pass to {@code expression}
   */
  Object evaluate(String expression, Object arg);
  default JSHandle evaluateHandle(String expression) {
    return evaluateHandle(expression, null);
  }
  /**
   * Returns the return value of {@code expression} as a {@code JSHandle}.
   *
   * <p> This method passes this handle as the first argument to {@code expression}.
   *
   * <p> The only difference between {@code jsHandle.evaluate} and {@code jsHandle.evaluateHandle} is that {@code jsHandle.evaluateHandle} returns
   * {@code JSHandle}.
   *
   * <p> If the function passed to the {@code jsHandle.evaluateHandle} returns a [Promise], then {@code jsHandle.evaluateHandle} would wait
   * for the promise to resolve and return its value.
   *
   * <p> See [{@code method: Page.evaluateHandle}] for more details.
   *
   * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
   * as a function. Otherwise, evaluated as an expression.
   * @param arg Optional argument to pass to {@code expression}
   */
  JSHandle evaluateHandle(String expression, Object arg);
  /**
   * The method returns a map with **own property names** as keys and JSHandle instances for the property values.
   */
  Map<String, JSHandle> getProperties();
  /**
   * Fetches a single property from the referenced object.
   *
   * @param propertyName property to get
   */
  JSHandle getProperty(String propertyName);
  /**
   * Returns a JSON representation of the object. If the object has a {@code toJSON} function, it **will not be called**.
   *
   * <p> <strong>NOTE:</strong> The method will return an empty JSON object if the referenced object is not stringifiable. It will throw an
   * error if the object has circular references.
   */
  Object jsonValue();
}

