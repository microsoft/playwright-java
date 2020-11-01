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

package com.microsoft.playwright.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.playwright.AccessibilityNode;

import java.util.ArrayList;
import java.util.List;

class AccessibilityNodeImpl implements AccessibilityNode {
  private final JsonObject json;

  AccessibilityNodeImpl(JsonObject json) {
    this.json = json;
  }

  @Override
  public String role() {
    return json.get("role").getAsString();
  }

  @Override
  public String name() {
    return json.get("name").getAsString();
  }

  @Override
  public String valueString() {
    if (!json.has("valueString")) {
      return null;
    }
    return json.get("valueString").getAsString();
  }

  @Override
  public Double valueNumber() {
    if (!json.has("valueNumber")) {
      return null;
    }
    return json.get("valueNumber").getAsDouble();
  }

  @Override
  public String description() {
    if (!json.has("description")) {
      return null;
    }
    return json.get("description").getAsString();
  }

  @Override
  public String keyshortcuts() {
    if (!json.has("keyshortcuts")) {
      return null;
    }
    return json.get("keyshortcuts").getAsString();
  }

  @Override
  public String roledescription() {
    if (!json.has("roledescription")) {
      return null;
    }
    return json.get("roledescription").getAsString();
  }

  @Override
  public String valuetext() {
    if (!json.has("valuetext")) {
      return null;
    }
    return json.get("valuetext").getAsString();
  }

  @Override
  public Boolean disabled() {
    if (!json.has("disabled")) {
      return null;
    }
    return json.get("disabled").getAsBoolean();
  }

  @Override
  public Boolean expanded() {
    if (!json.has("expanded")) {
      return null;
    }
    return json.get("expanded").getAsBoolean();
  }

  @Override
  public Boolean focused() {
    if (!json.has("focused")) {
      return null;
    }
    return json.get("focused").getAsBoolean();
  }

  @Override
  public Boolean modal() {
    if (!json.has("modal")) {
      return null;
    }
    return json.get("modal").getAsBoolean();
  }

  @Override
  public Boolean multiline() {
    if (!json.has("multiline")) {
      return null;
    }
    return json.get("multiline").getAsBoolean();
  }

  @Override
  public Boolean multiselectable() {
    if (!json.has("multiselectable")) {
      return null;
    }
    return json.get("multiselectable").getAsBoolean();
  }

  @Override
  public Boolean readonly() {
    if (!json.has("readonly")) {
      return null;
    }
    return json.get("readonly").getAsBoolean();
  }

  @Override
  public Boolean required() {
    if (!json.has("required")) {
      return null;
    }
    return json.get("required").getAsBoolean();
  }

  @Override
  public Boolean selected() {
    if (!json.has("selected")) {
      return null;
    }
    return json.get("selected").getAsBoolean();
  }

  @Override
  public CheckedState checked() {
    if (!json.has("checked")) {
      return null;
    }
    String value = json.get("checked").getAsString();
    switch (value) {
      case "checked": return CheckedState.CHECKED;
      case "unchecked": return CheckedState.UNCHECKED;
      case "mixed": return CheckedState.MIXED;
      default: throw new IllegalStateException("Unexpected value: " + value);
    }
  }

  @Override
  public PressedState pressed() {
    if (!json.has("pressed")) {
      return null;
    }
    String value = json.get("pressed").getAsString();
    switch (value) {
      case "pressed": return PressedState.PRESSED;
      case "released": return PressedState.RELEASED;
      case "mixed": return PressedState.MIXED;
      default: throw new IllegalStateException("Unexpected value: " + value);
    }
  }

  @Override
  public Integer level() {
    if (!json.has("level")) {
      return null;
    }
    return json.get("level").getAsInt();
  }

  @Override
  public Double valuemin() {
    if (!json.has("valuemin")) {
      return null;
    }
    return json.get("valuemin").getAsDouble();
  }

  @Override
  public Double valuemax() {
    if (!json.has("valuemax")) {
      return null;
    }
    return json.get("valuemax").getAsDouble();
  }

  @Override
  public String autocomplete() {
    if (!json.has("autocomplete")) {
      return null;
    }
    return json.get("autocomplete").getAsString();
  }

  @Override
  public String haspopup() {
    if (!json.has("haspopup")) {
      return null;
    }
    return json.get("haspopup").getAsString();
  }

  @Override
  public String invalid() {
    if (!json.has("invalid")) {
      return null;
    }
    return json.get("invalid").getAsString();
  }

  @Override
  public String orientation() {
    if (!json.has("orientation")) {
      return null;
    }
    return json.get("orientation").getAsString();
  }

  @Override
  public List<AccessibilityNode> children() {
    if (!json.has("children")) {
      return null;
    }
    List<AccessibilityNode> result = new ArrayList<>();
    for (JsonElement e : json.getAsJsonArray("children")) {
      result.add(new AccessibilityNodeImpl(e.getAsJsonObject()));
    }
    return result;
  }
}
