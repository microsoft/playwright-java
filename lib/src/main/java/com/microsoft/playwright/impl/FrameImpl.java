/**
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

package com.microsoft.playwright.impl;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.playwright.*;

import java.util.*;

import static com.microsoft.playwright.impl.Helpers.isFunctionBody;

public class FrameImpl extends ChannelOwner implements Frame {
  PageImpl page;

  FrameImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
  }

  private static SerializedValue serializeValue(Object value) {
    SerializedValue result = new SerializedValue();
    if (value == null)
      result.v = "undefined";
    else if (value instanceof Double) {
      double d = ((Double) value).doubleValue();
      if (d == Double.POSITIVE_INFINITY)
        result.v = "Infinity";
      else if (d == Double.NEGATIVE_INFINITY)
        result.v = "-Infinity";
      else if (d == -0)
        result.v = "-0";
      else if (Double.isNaN(d))
        result.v="NaN";
      else
        result.n = d;
    }
//    if (value instanceof Date)
    else if (value instanceof Boolean)
      result.b = (Boolean) value;
    else if (value instanceof Integer)
      result.n = (Integer) value;
    else if (value instanceof String)
      result.s = (String) value;
    else if (value instanceof List) {
      List<SerializedValue> list = new ArrayList<>();
      for (Object o : (List) value)
        list.add(serializeValue(o));
      result.a = list.toArray(new SerializedValue[0]);
    } else if (value instanceof Map) {
      List<SerializedValue.O> list = new ArrayList<>();
      Map<String, Object> map = (Map<String, Object>) value;
      for (Map.Entry<String, Object> e : map.entrySet()) {
        SerializedValue.O o = new SerializedValue.O();
        o.k = e.getKey();
        o.v = serializeValue(e.getValue());
        list.add(o);
      }
      result.o = list.toArray(new SerializedValue.O[0]);
    } else
      throw new RuntimeException("Unsupported type of argument: " + value);
    return result;
  }
  private static SerializedArgument serializeArgument(Object arg) {
    SerializedArgument result = new SerializedArgument();
    result.value = serializeValue(arg);
    result.handles = new Channel[0];
    return result;
  }


  private static <T> T deserialize(SerializedValue value) {
    if (value.n != null) {
      if (value.n.doubleValue() == (double) value.n.intValue())
        return (T) Integer.valueOf(value.n.intValue());
      return (T) Double.valueOf(value.n.doubleValue());
    }
    if (value.b != null)
      return (T) value.b;
    if (value.s != null)
      return (T) value.s;
    if (value.v != null) {
      switch (value.v) {
        case "undefined":
          return null;
        case "Infinity":
          return (T) Double.valueOf(Double.POSITIVE_INFINITY);
        case "-Infinity":
          return (T) Double.valueOf(Double.NEGATIVE_INFINITY);
        case "-0":
          return (T) Double.valueOf(-0);
        case "NaN":
          return (T) Double.valueOf(Double.NaN);
        default:
          throw new RuntimeException("Unexpected value: " + value.v);
      }
    }
    if (value.a != null) {
      List list = new ArrayList();
      for (SerializedValue v : value.a)
        list.add(deserialize(v));
      return (T) list;
    }
    if (value.o != null) {
      Map map = new LinkedHashMap<>();
      for (SerializedValue.O o : value.o)
        map.put(o.k, deserialize(o.v));
      return (T) map;
    }
    throw new RuntimeException("Unexpected result: " + new Gson().toJson(value));
  }

  public <T> T evalTyped(String expression) {
    return (T) evaluate(expression, null, false);
  }

  private Object evaluate(String expression, Object arg, boolean forceExpression) {
    JsonObject params = new JsonObject();
    params.addProperty("expression", expression);
    params.addProperty("world", "main");
    if (!isFunctionBody(expression))
      forceExpression = true;
    params.addProperty("isFunction", !forceExpression);
    params.add("arg", new Gson().toJsonTree(serializeArgument(arg)));
    JsonElement json = sendMessage("evaluateExpression", params);
//    System.out.println("json = " + new Gson().toJson(json));
    SerializedValue value = new Gson().fromJson(json.getAsJsonObject().get("value"), SerializedValue.class);
    return deserialize(value);
  }

  @Override
  public ElementHandle querySelector(String selector) {
    return null;
  }

  @Override
  public List<ElementHandle> querySelectorAll(String selector) {
    return null;
  }

  @Override
  public Object evalOnSelector(String selector, String pageFunction, Object arg) {
    return null;
  }

  @Override
  public Object evalOnSelectorAll(String selector, String pageFunction, Object arg) {
    return null;
  }

  @Override
  public ElementHandle addScriptTag(AddScriptTagOptions options) {
    return null;
  }

  @Override
  public ElementHandle addStyleTag(AddStyleTagOptions options) {
    return null;
  }

  @Override
  public void check(String selector, CheckOptions options) {

  }

  @Override
  public List<Frame> childFrames() {
    return null;
  }

  @Override
  public void click(String selector, ClickOptions options) {
    if (options == null) {
      options = new ClickOptions();
    }
    JsonObject params = new JsonObject();
    params.addProperty("selector", selector);
    JsonElement result = sendMessage("click", params);
  }

  @Override
  public String content() {
    return null;
  }

  @Override
  public void dblclick(String selector, DblclickOptions options) {

  }

  @Override
  public void dispatchEvent(String selector, String type, Object eventInit, DispatchEventOptions options) {

  }

  @Override
  public Object evaluate(String pageFunction, Object arg) {
    return evaluate(pageFunction, arg, false);
  }

  @Override
  public JSHandle evaluateHandle(String pageFunction, Object arg) {
    return null;
  }

  @Override
  public void fill(String selector, String value, FillOptions options) {

  }

  @Override
  public void focus(String selector, FocusOptions options) {

  }

  @Override
  public ElementHandle frameElement() {
    return null;
  }

  @Override
  public String getAttribute(String selector, String name, GetAttributeOptions options) {
    return null;
  }

  @Override
  public ResponseImpl navigate(String url, NavigateOptions options) {
    if (options == null) {
      options = new NavigateOptions();
    }
    JsonObject params = new Gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("url", url);
    JsonElement result = sendMessage("goto", params);
    System.out.println("result = " + new Gson().toJson(result));
    return connection.getExistingObject(result.getAsJsonObject().getAsJsonObject("response").get("guid").getAsString());
  }

  @Override
  public void hover(String selector, HoverOptions options) {

  }

  @Override
  public String innerHTML(String selector, InnerHTMLOptions options) {
    return null;
  }

  @Override
  public String innerText(String selector, InnerTextOptions options) {
    return null;
  }

  @Override
  public boolean isDetached() {
    return false;
  }

  @Override
  public String name() {
    return null;
  }

  @Override
  public Page page() {
    return null;
  }

  @Override
  public Frame parentFrame() {
    return null;
  }

  @Override
  public void press(String selector, String key, PressOptions options) {

  }

  @Override
  public List<String> selectOption(String selector, String values, SelectOptionOptions options) {
    return null;
  }

  @Override
  public void setContent(String html, SetContentOptions options) {

  }

  @Override
  public void setInputFiles(String selector, String files, SetInputFilesOptions options) {

  }

  @Override
  public String textContent(String selector, TextContentOptions options) {
    return null;
  }

  @Override
  public String title() {
    return null;
  }

  @Override
  public void type(String selector, String text, TypeOptions options) {

  }

  @Override
  public void uncheck(String selector, UncheckOptions options) {

  }

  @Override
  public String url() {
    return null;
  }

  @Override
  public JSHandle waitForFunction(String pageFunction, Object arg, WaitForFunctionOptions options) {
    return null;
  }

  @Override
  public void waitForLoadState(LoadState state, WaitForLoadStateOptions options) {

  }

  @Override
  public Response waitForNavigation(WaitForNavigationOptions options) {
    return null;
  }

  @Override
  public ElementHandle waitForSelector(String selector, WaitForSelectorOptions options) {
    return null;
  }

  @Override
  public void waitForTimeout(int timeout) {

  }
}
