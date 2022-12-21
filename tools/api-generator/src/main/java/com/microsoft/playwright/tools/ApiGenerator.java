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

package com.microsoft.playwright.tools;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.*;
import java.nio.file.FileSystems;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.reverse;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

abstract class Element {
  final String jsonName;
  final String jsonPath;
  final JsonElement jsonElement;
  final Element parent;

  Element(Element parent, JsonElement jsonElement) {
    this(parent, false, jsonElement);
  }

  Element(Element parent, boolean useParentJsonPath, JsonElement jsonElement) {
    this.parent = parent;
    if (jsonElement != null && jsonElement.isJsonObject()) {
      this.jsonName = jsonElement.getAsJsonObject().get("name").getAsString();
    } else {
      this.jsonName = "";
    }
    if (useParentJsonPath) {
      this.jsonPath = parent.jsonPath;
    } else {
      this.jsonPath = parent == null ? jsonName : parent.jsonPath + "." + jsonName ;
    }
    this.jsonElement = jsonElement;
  }


  TypeDefinition typeScope() {
    return parent.typeScope();
  }

  Map<String, TypeDefinition> topLevelTypes() {
    return parent.topLevelTypes();
  }

  static String toTitle(String name) {
    return Character.toUpperCase(name.charAt(0)) + name.substring(1);
  }

  void writeJavadoc(List<String> output, String offset, String text) {
    if (text.isEmpty()) {
      return;
    }
    output.add(offset + "/**");
    String[] lines = text.split("\\n");
    for (String line : lines) {
      output.add((offset + " *" + (line.isEmpty() ? "" : " ") + line)
        .replace("*/", "*\\/"));
    }
    output.add(offset + " */");
  }

  String comment() {
    JsonObject json = jsonElement.getAsJsonObject();
    if (!json.has("spec")) {
      return "";
    }
    if (json.has("deprecated")) {
      return "@deprecated " + beautify(json.get("deprecated").getAsString());
    }
    return formatSpec(json.getAsJsonArray("spec"));
  }

  private static String formatSpec(JsonArray spec) {
    List<String> out = new ArrayList<>();
    String currentItemList = null;
    for (JsonElement item : spec) {
      JsonObject node = item.getAsJsonObject();
      String type = node.get("type").getAsString();
      if ("code".equals(type)) {
        if (!node.get("codeLang").getAsString().contains("java")) {
          continue;
        }
        out.add("<pre>{@code");
        for (JsonElement line : node.getAsJsonArray("lines")) {
          out.add(line.getAsString());
        }
        out.add("}</pre>");
      } else if ("li".equals(type)) {
        String text = node.get("text").getAsString();
        if (text.startsWith("extends: ")) {
          continue;
        }
        if (currentItemList == null) {
          String liType = node.get("liType").getAsString();
          if ("ordinal".equals(liType)) {
            currentItemList = "ol";
            out.add("<ol>");
          } else {
            currentItemList = "ul";
            out.add("<ul>");
          }
        }
        out.add("<li> " + beautify(text) + "</li>");
      } else {
        if (currentItemList != null) {
          out.add("</" + currentItemList + ">");
          currentItemList = null;
        }
        String paragraph = node.get("text").getAsString();
        paragraph = beautify(paragraph);
        if ("note".equals(type)) {
          paragraph = "<strong>NOTE:</strong> " + paragraph;
        }
        if (!out.isEmpty())
          paragraph = "\n<p> " + paragraph;
        out.add(paragraph);
      }
    }
    if (currentItemList != null) {
      out.add("</" + currentItemList + ">");
      currentItemList = null;
    }

    return String.join("\n", out);
  }

  private static String beautify(String paragraph) {
    String linkified = linkifyMemberRefs(paragraph);
    linkified = updateExternalLinks(linkified)
      .replaceAll("↵", " ")
      .replaceAll("`'([^`]+)'`", "{@code \"$1\"}")
      .replaceAll("`([^`]+)`", "{@code $1}");
    return wrapText(linkified, 120, "");
  }

  private static String linkifyMemberRefs(String paragraph) {
    Matcher matcher = Pattern.compile("\\[`(event|property|method): ([^`]+)`]").matcher(paragraph);
    String linkified = "";
    int start = 0;
    while (matcher.find()) {
      linkified += paragraph.substring(start, matcher.start());
      String name = matcher.group(2);
      if (ApiGenerator.aliases.containsKey(name)) {
        name = ApiGenerator.aliases.get(name);
      }
      if ("event".equals(matcher.group(1))) {
        String[] parts = name.split("\\.");
        name = parts[0] + ".on" + toTitle(parts[1]);
      }
      linkified += "{@link " + name.replace(".", "#") + " " + name + "()}";
      start = matcher.end();
    }
    linkified += paragraph.substring(start);
    return linkified;
  }

  private static String updateExternalLinks(String paragraph) {
    Matcher matcher = Pattern.compile("\\[([^\\]]+)\\]\\(([^\\)]+)\\)").matcher(paragraph);
    String linkified = "";
    int start = 0;
    while (matcher.find()) {
      String url = matcher.group(2);
      if (url.startsWith("../")) {
        // ../actionability.md#editable => https://playwright.dev/java/docs/actionability/#editable
        url = url.replace(".md", "");
        url = url.replace("../", "https://playwright.dev/java/docs/");
      }
      if (url.startsWith("./")) {
        // ./class-tracing.md => https://playwright.dev/java/docs/api/class-tracing
        url = url.replace(".md", "");
        url = url.replace("./", "https://playwright.dev/java/docs/api/");
      }
      String link = "<a href=\"" + url + "\">" + matcher.group(1) + "</a>";
      linkified += paragraph.substring(start, matcher.start());
      linkified += link;
      start = matcher.end();
    }
    linkified += paragraph.substring(start);
    linkified = linkified.replaceAll("\\[Promise\\]",
      "<a href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>");
    return linkified;
  }


  private static List<String> tokenizeNoBreakLinks(String text) {
     List<String> links = new ArrayList<>();
    // Don't wrap simple links with spaces.
    Matcher mather = Pattern.compile("\\[[^\\]]+\\]").matcher(text);
    String sanitized = "";
    int start = 0;
    while (mather.find()) {
      sanitized += text.substring(start, mather.start());
      sanitized += "[" + links.size() + "]";
      start = mather.end();
      links.add(mather.group());
    }
    sanitized += text.substring(start, text.length());
    List<String> tokens = Arrays.asList(sanitized.split(" "));
    for (int i = 0; i < links.size(); i++) {
      int index = i;
      tokens = tokens.stream().map(s -> s.replace("[" + index + "]", links.get(index))).collect(Collectors.toList());
    }
    return tokens;
  }

  private static String wrapText(String text, int maxColumns, String prefix) {
    if (maxColumns == 0)
      return prefix + text;
    if (text.trim().startsWith("|"))
      return prefix + text;
    String indent = prefix;
    List<String> lines = new ArrayList<>();
    maxColumns -= indent.length();
    List<String> words = tokenizeNoBreakLinks(text);
    String line = "";
    for (String word : words) {
      if (!line.isEmpty() && line.length() + word.length() < maxColumns) {
        line += " " + word;
      } else {
        if (!line.isEmpty())
          lines.add(line);
        line = (!lines.isEmpty() ? indent : prefix) + word;
      }
    }
    if (!line.isEmpty())
      lines.add(line);
    return String.join("\n", lines);
  }
}

// Represents return type of a method, type of a method param or type of a field.
class TypeRef extends Element {
  String customType;

  private static final Map<String, String> customTypeNames = new HashMap<>();
  static {
    customTypeNames.put("BrowserContext.addCookies.cookies", "Cookie");
    customTypeNames.put("BrowserContext.cookies", "Cookie");

    customTypeNames.put("Request.headersArray", "HttpHeader");
    customTypeNames.put("Response.headersArray", "HttpHeader");
    customTypeNames.put("APIResponse.headersArray", "HttpHeader");

    customTypeNames.put("Locator.selectOption.values", "SelectOption");
    customTypeNames.put("ElementHandle.selectOption.values", "SelectOption");
    customTypeNames.put("Frame.selectOption.values", "SelectOption");
    customTypeNames.put("Page.selectOption.values", "SelectOption");

    customTypeNames.put("Locator.setInputFiles.files", "FilePayload");
    customTypeNames.put("ElementHandle.setInputFiles.files", "FilePayload");
    customTypeNames.put("FileChooser.setFiles.files", "FilePayload");
    customTypeNames.put("Frame.setInputFiles.files", "FilePayload");
    customTypeNames.put("Page.setInputFiles.files", "FilePayload");
    customTypeNames.put("Page.setInputFiles.files", "FilePayload");
    customTypeNames.put("FormData.set.value", "FilePayload");

    customTypeNames.put("Locator.dragTo.options.sourcePosition", "Position");
    customTypeNames.put("Page.dragAndDrop.options.sourcePosition", "Position");
    customTypeNames.put("Frame.dragAndDrop.options.sourcePosition", "Position");
    customTypeNames.put("Locator.dragTo.options.targetPosition", "Position");
    customTypeNames.put("Page.dragAndDrop.options.targetPosition", "Position");
    customTypeNames.put("Frame.dragAndDrop.options.targetPosition", "Position");
  }

  TypeRef(Element parent, JsonElement jsonElement) {
    super(parent, true, jsonElement);

    createClassesAndEnums(jsonElement.getAsJsonObject());
  }

  private void createClassesAndEnums(JsonObject jsonObject) {
    if (jsonObject.has("union")) {
      if (jsonObject.get("name").getAsString().isEmpty()) {
        for (JsonElement item : jsonObject.getAsJsonArray("union")) {
          if (item.isJsonObject()) {
            createClassesAndEnums(item.getAsJsonObject());
          }
        }
      } else {
        typeScope().createEnum(jsonObject);
      }
      return;
    }
    if (jsonObject.has("templates")) {
      for (JsonElement item : jsonObject.getAsJsonArray("templates")) {
        if (item.isJsonObject()) {
          createClassesAndEnums(item.getAsJsonObject());
        }
      }
      return;
    }
    if ("Object".equals(jsonObject.get("name").getAsString())) {
      if (customType != null) {
        // Same type maybe referenced as 'Object' in several union values, e.g. Object|Array<Object>
        return;
      }
      if (parent instanceof Param && "options".equals(parent.jsonName)) {
        customType = toTitle(parent.parent.jsonName) + toTitle(parent.jsonName);
        typeScope().createNestedClass(customType, this, jsonObject);
      } else {
        if (customTypeNames.containsKey(jsonPath)) {
          customType = customTypeNames.get(jsonPath);
        } else {
          customType = toTitle(parent.jsonName);
        }
        typeScope().createTopLevelClass(customType, this, jsonObject);
      }
    }
  }

  String toJava() {
    if (jsonElement.isJsonNull()) {
      return "void";
    }
    return convertBuiltinType(stripNullable());
  }

  boolean isCustomClass() {
    JsonObject jsonObject = stripNullable();
    if (!"Object".equals(jsonObject.get("name").getAsString())) {
      return false;
    }
    return !jsonElement.getAsJsonObject().has("templates");
  }

  boolean isTypeUnion() {
    if (isNullable()) {
      return false;
    }
    if (!jsonElement.getAsJsonObject().has("union")) {
      return false;
    }
    return jsonElement.getAsJsonObject().get("name").getAsString().isEmpty();
  }

  private List<JsonObject> supportedUnionTypes() {
    List<JsonObject> result = new ArrayList<>();
    for (JsonElement item : jsonElement.getAsJsonObject().getAsJsonArray("union")) {
      JsonObject o = item.getAsJsonObject();
      if (o.get("name").getAsString().equals("function") && !o.has("args")) {
        continue;
      }
      if (o.get("name").getAsString().equals("null")) {
        continue;
      }
      result.add(o);
    }
    return result;
  }

  int unionSize() {
    return supportedUnionTypes().size();
  }

  String formatTypeFromUnion(int i) {
    JsonElement overloadedType = supportedUnionTypes().get(i);
    return convertBuiltinType(overloadedType.getAsJsonObject());
  }

  boolean isNullable() {
    JsonObject jsonType = jsonElement.getAsJsonObject();
    if (!jsonType.has("union")) {
      return false;
    }
    if (!jsonType.get("name").getAsString().isEmpty()) {
      return false;
    }
    JsonArray values = jsonType.getAsJsonArray("union");
    if (values.size() != 2) {
      return false;
    }
    for (JsonElement item : values) {
      JsonObject o = item.getAsJsonObject();
      if ("null".equals(o.get("name").getAsString())) {
        return true;
      }
    }
    return false;
  }

  private JsonObject stripNullable() {
    JsonObject jsonType = jsonElement.getAsJsonObject();
    if (!isNullable()) {
      return jsonType;
    }
    if (!jsonType.has("union")) {
      return jsonType;
    }
    if (!jsonType.get("name").getAsString().isEmpty()) {
      return jsonType;
    }
    JsonArray values = jsonType.getAsJsonArray("union");
    if (values.size() != 2) {
      return jsonType;
    }
    for (JsonElement item : values) {
      JsonObject o = item.getAsJsonObject();
      if (!"null".equals(o.get("name").getAsString())) {
        return o;
      }
    }
    throw new RuntimeException("Unexpected union " + jsonPath + ": " + jsonType);
  }

  private String convertBuiltinType(JsonObject jsonType) {
    String name = jsonType.get("name").getAsString();
    if (jsonType.has("union")) {
      if (name.isEmpty()) {
        if (parent instanceof Field) {
          return "Object";
        }
        throw new RuntimeException("Unexpected union without name: " + jsonType);
      }
      return name;
    }
    if ("int".equals(name)) {
      return "int";
    }
    if ("float".equals(name)) {
      return "double";
    }
    if ("string".equals(name)) {
      return "String";
    }
    if ("void".equals(name)) {
      return "void";
    }
    if ("path".equals(name)) {
      return "Path";
    }
    if ("EvaluationArgument".equals(name)) {
      return "Object";
    }
    if ("Serializable".equals(name)) {
      return "Object";
    }
    if ("any".equals(name)) {
      return "Object";
    }
    if ("Readable".equals(name)) {
      return "InputStream";
    }
    if ("Buffer".equals(name)) {
      return "byte[]";
    }
    if ("URL".equals(name)) {
      return "String";
    }
    if ("RegExp".equals(name)) {
      return "Pattern";
    }
    if ("Array".equals(name)) {
      String elementType = convertTemplateParams(jsonType);
      if (parent instanceof Param && isTypeUnion()) {
        long numArrayOverloads = supportedUnionTypes().stream().filter(
          jsonObject -> "Array".equals(jsonObject.get("name").getAsString())).count();
        if (numArrayOverloads > 1) {
          // Use array instead of List as after type erasure all lists are indistinguishable and wouldn't allow overloads.
          return elementType + "[]";
        }
      }
      return "List<" + elementType + ">";
    }
    if ("Object".equals(name)) {
      if (customType != null) {
        return customType;
      }
      return "Map<" + convertTemplateParams(jsonType) + ">";
    }
    if ("Map".equals(name)) {
      return "Map<" + convertTemplateParams(jsonType) + ">";
    }
    if ("Promise".equals(name)) {
      return convertTemplateParams(jsonType);
    }
    if ("function".equals(name)) {
      if (!jsonType.has("args")) {
        switch (jsonPath) {
          case "BrowserContext.exposeBinding.callback": return "BindingCallback";
          case "BrowserContext.exposeFunction.callback": return "FunctionCallback";
          case "Page.exposeBinding.callback": return "BindingCallback";
          case "Page.exposeFunction.callback": return "FunctionCallback";
          default:
            throw new RuntimeException("Missing mapping for " + jsonPath);
        }
      }
      if (jsonType.getAsJsonArray("args").size() == 1) {
        String paramType = convertBuiltinType(jsonType.getAsJsonArray("args").get(0).getAsJsonObject());
        if (!jsonType.has("returnType") || jsonType.get("returnType").isJsonNull()) {
          return "Consumer<" + paramType + ">";
        }
        if (jsonType.has("returnType")
          && "boolean".equals(jsonType.getAsJsonObject("returnType").get("name").getAsString())) {
          return "Predicate<" + paramType + ">";
        }
        throw new RuntimeException("Missing mapping for " + jsonType);
      }
    }
    return name;
  }

  private String convertTemplateParams(JsonObject jsonType) {
    if (!jsonType.has("templates")) {
      return "";
    }
    List<String> params = new ArrayList<>();
    for (JsonElement item : jsonType.getAsJsonArray("templates")) {
      params.add(convertBuiltinType(item.getAsJsonObject()));
    }
    return String.join(", ", params);
  }
}

abstract class TypeDefinition extends Element {
  final List<CustomClass> classes = new ArrayList<>();

  TypeDefinition(Element parent, JsonObject jsonElement) {
    super(parent, jsonElement);
  }

  TypeDefinition(Element parent, boolean useParentJsonPath, JsonObject jsonElement) {
    super(parent, useParentJsonPath, jsonElement);
  }

  String name() {
    return jsonName;
  }

  @Override
  TypeDefinition typeScope() {
    return this;
  }

  void createEnum(JsonObject jsonObject) {
    Enum newEnum = new Enum(this, jsonObject);
    if (newEnum.jsonName == null) {
      throw new RuntimeException("Enum without name: " + jsonObject);
    }
    Map<String, TypeDefinition> enumMap = topLevelTypes();
    TypeDefinition existing = enumMap.putIfAbsent(newEnum.jsonName, newEnum);
    if (existing != null && (!(existing instanceof Enum) || !((Enum) existing).hasSameValues(newEnum))) {
      throw new RuntimeException("Two enums with same name have different values:\n" + jsonObject + "\n" + existing.jsonElement);
    }
  }

  void createTopLevelClass(String name, Element parent, JsonObject jsonObject) {
    Map<String, TypeDefinition> map = topLevelTypes();
    TypeDefinition existing = map.putIfAbsent(name, new CustomClass(parent, name, jsonObject));
    if (existing != null && (!(existing instanceof CustomClass))) {
      throw new RuntimeException("Two classes with same name have different values:\n" + jsonObject + "\n" + existing.jsonElement);
    }
  }

  void createNestedClass(String name, Element parent, JsonObject jsonObject) {
    for (CustomClass c : classes) {
      if (c.name.equals(name)) {
        return;
      }
    }
    classes.add(new CustomClass(parent, name, jsonObject));
  }

  void writeTo(List<String> output, String offset) {
    for (CustomClass c : classes) {
      c.writeTo(output, offset);
    }
  }
}

class Event extends Element {
  private final TypeRef type;

  Event(Element parent, JsonObject jsonElement) {
    super(parent, jsonElement);
    type = new TypeRef(this, jsonElement.get("type"));
  }

  void writeListenerMethods(List<String> output, String offset) {
    writeJavadoc(output, offset, comment());
    String name = toTitle(jsonName);
    String paramType = type.toJava();
    String listenerType = "Consumer<" + paramType + ">";
    output.add(offset + "void on" + name + "(" + listenerType + " handler);");
    writeJavadoc(output, offset, "Removes handler that was previously added with {@link #on" + name + " on" + name + "(handler)}.");
    output.add(offset + "void off" + name + "(" + listenerType + " handler);");
  }
}

class Method extends Element {
  final TypeRef returnType;
  final List<Param> params = new ArrayList<>();

  Method(TypeDefinition parent, JsonObject jsonElement) {
    super(parent, jsonElement);
    returnType = new TypeRef(this, jsonElement.get("type"));
    if (jsonElement.has("args")) {
      for (JsonElement arg : jsonElement.getAsJsonArray("args")) {
        JsonObject paramObj = arg.getAsJsonObject();
        if (paramObj.get("name").getAsString().equals("options") &&
          paramObj.getAsJsonObject("type").getAsJsonArray("properties").size() == 0) {
          continue;
        }
        params.add(new Param(this, arg.getAsJsonObject()));
      }
    }
  }

  void writeTo(List<String> output, String offset) {
    if ("Playwright.create".equals(jsonPath)) {
      writeJavadoc(params, output, offset);
      output.add(offset + "static Playwright create(CreateOptions options) {");
      output.add(offset + "  return PlaywrightImpl.create(options);");
      output.add(offset + "}");
      output.add("");
      output.add(offset + "static Playwright create() {");
      output.add(offset + "  return create(null);");
      output.add(offset + "}");
      return;
    }
    if ("FormData.create".equals(jsonPath)) {
      writeJavadoc(params, output, offset);
      output.add(offset + "static FormData create() {");
      output.add(offset + "  return new FormDataImpl();");
      output.add(offset + "}");
      return;
    }
    if ("RequestOptions.create".equals(jsonPath)) {
      writeJavadoc(params, output, offset);
      output.add(offset + "static RequestOptions create() {");
      output.add(offset + "  return new RequestOptionsImpl();");
      output.add(offset + "}");
      return;
    }
    if ("PlaywrightAssertions.assertThat".equals(jsonPath)) {
      writeJavadoc(params, output, offset);
      String originalName = jsonElement.getAsJsonObject().get("originalName").getAsString();
      if ("expectPage".equals(originalName)) {
        output.add(offset + "static PageAssertions assertThat(Page page) {");
        output.add(offset + "  return new PageAssertionsImpl(page);");
        output.add(offset + "}");
        output.add("");
      } else if ("expectLocator".equals(originalName)) {
        output.add(offset + "static LocatorAssertions assertThat(Locator locator) {");
        output.add(offset + "  return new LocatorAssertionsImpl(locator);");
        output.add(offset + "}");
        output.add("");
      } else if ("expectAPIResponse".equals(originalName)) {
        output.add(offset + "static APIResponseAssertions assertThat(APIResponse response) {");
        output.add(offset + "  return new APIResponseAssertionsImpl(response);");
        output.add(offset + "}");
        output.add("");
      } else {
        throw new RuntimeException("Unexpected originalName: " + originalName);
      }
      return;
    }
    if ("PlaywrightAssertions.setDefaultAssertionTimeout".equals(jsonPath)) {
      writeJavadoc(params, output, offset);
      output.add(offset + "static void setDefaultAssertionTimeout(double milliseconds) {");
      output.add(offset + "  AssertionsTimeout.setDefaultTimeout(milliseconds);");
      output.add(offset + "}");
      output.add("");
      return;
    }
    int numOverloads = 1;
    for (int i = 0; i < params.size(); i++) {
      if (params.get(i).type.isTypeUnion()) {
        numOverloads = params.get(i).type.unionSize();
        break;
      }
    }

    for (int i = 0; i < numOverloads; i++) {
      writeOverloadedMethods(i, output, offset);
    }
  }

  private void writeOverloadedMethods(int overloadIndex, List<String> output, String offset) {
    for (int i = params.size() - 1; i >= 0; i--) {
      Param p = params.get(i);
      if (!p.isOptional()) {
        continue;
      }
      // For optional overloaded params generate only overload without the param.
      if (p.type.isTypeUnion() && overloadIndex != 0) {
        continue;
      }
      writeDefaultOverloadedMethod(overloadIndex, i, output, offset);
    }

    List<String> paramList = params.stream().map(p -> p.type.isTypeUnion() ? p.toJavaOverload(overloadIndex) : p.toJava()).collect(toList());
    writeJavadoc(params, output, offset);
    output.add(offset + returnType.toJava() + " " + jsonName + "(" + String.join(", ", paramList) + ");");
  }


  private void writeDefaultOverloadedMethod(int overloadIndex, int firstNullOptional, List<String> output, String offset) {
    List<Param> paramList = new ArrayList<>();
    List<String> argList = new ArrayList<>();
    for (int i = 0; i < params.size(); i++) {
      Param p = params.get(i);
      if (i == firstNullOptional) {
        if (p.type.isTypeUnion()) {
          String type = p.type.formatTypeFromUnion(overloadIndex);
          argList.add("int".equals(type) ? "0" : "(" + type + ") null");
        } else {
          String defaultValue = "null";
          // TODO: it should probably be done for all methods that have name#1, name#2 etc overloads.
          if ("LocatorAssertions.hasAttribute".equals(jsonPath)) {
            defaultValue = "(" + p.type.toJava() + ") " + defaultValue;
          }
          argList.add(defaultValue);
        }
        continue;
      }
      if (p.isOptional() && i > firstNullOptional) {
        continue;
      }
      paramList.add(p);
      argList.add(p.jsonName);
    }
    String paramsStr = paramList.stream().map(p -> p.type.isTypeUnion() ? p.toJavaOverload(overloadIndex) : p.toJava())
      .collect(joining(", "));
    String returns = returnType.toJava().equals("void") ? "" : "return ";
    writeJavadoc(paramList, output, offset);
    output.add(offset + "default " + returnType.toJava() + " " + jsonName + "(" + paramsStr + ") {");
    output.add(offset + "  " + returns + jsonName + "(" + String.join(", ", argList) + ");");
    output.add(offset + "}");
  }

  private void writeJavadoc(List<Param> paramList, List<String> output, String offset) {
    List<String> sections = new ArrayList<>();
    sections.add(comment());
    boolean hasBlankLine = false;
    if (!paramList.isEmpty()) {
      for (Param p : paramList) {
        String comment = p.comment();
        if (comment.isEmpty()) {
          continue;
        }
        if (!hasBlankLine) {
          sections.add("");
          hasBlankLine = true;
        }
        sections.add("@param " + p.jsonName + " " + comment);
      }
    }
    if (jsonElement.getAsJsonObject().has("returnComment")) {
      if (!hasBlankLine) {
        sections.add("");
        hasBlankLine = true;
      }
      String returnComment = jsonElement.getAsJsonObject().get("returnComment").getAsString();
      sections.add("@return " + returnComment);
    }
    if (jsonElement.getAsJsonObject().has("since")) {
      if (!hasBlankLine) {
        sections.add("");
        hasBlankLine = true;
      }
      String since = jsonElement.getAsJsonObject().get("since").getAsString();
      sections.add("@since " + since);
    }
    writeJavadoc(output, offset, String.join("\n", sections));
  }
}

class Param extends Element {
  final TypeRef type;

  Param(Method method, JsonObject jsonElement) {
    super(method, jsonElement);
    type = new TypeRef(this, jsonElement.get("type").getAsJsonObject());
  }

  boolean isOptional() {
    return !jsonElement.getAsJsonObject().get("required").getAsBoolean();
  }

  String toJavaOverload(int overoadIndex) {
    return type.formatTypeFromUnion(overoadIndex) + " " + jsonName;
  }

  String toJava() {
    return type.toJava() + " " + jsonName;
  }
}

class Field extends Element {
  final String name;
  final TypeRef type;

  Field(CustomClass parent, String name, JsonObject jsonElement) {
    super(parent, jsonElement);
    this.name = name;
    this.type = new TypeRef(this, jsonElement.getAsJsonObject().get("type"));
  }

  boolean isRequired() {
    return jsonElement.getAsJsonObject().has("required") &&
      jsonElement.getAsJsonObject().get("required").getAsBoolean();
  }

  void writeTo(List<String> output, String offset) {
    writeJavadoc(output, offset, comment());
    String typeStr = type.toJava();
    if (type.isNullable()) {
      typeStr = "Optional<" + typeStr + ">";
    }
    // Convert optional fields to boxed types.
    if (!isRequired()) {
      if (typeStr.equals("int")) {
        typeStr = "Integer";
      } else if (typeStr.equals("double")) {
        typeStr = "Double";
      } else if (typeStr.equals("boolean")) {
        typeStr = "Boolean";
      }
    }
    if (isBrowserChannelOption()) {
      typeStr = "Object";
    }
    output.add(offset + "public " + typeStr + " " + name + ";");
  }

  void writeBuilderMethod(List<String> output, String offset, String parentClass) {
    if (type.customType == null && type.isTypeUnion()) {
      for (int i = 0; i < type.unionSize(); i++) {
        writeGenericBuilderMethod(output, offset, parentClass, type.formatTypeFromUnion(i));
      }
      return;
    }
    if (type.isCustomClass()) {
      TypeDefinition customType = topLevelTypes().get(type.customType);
      if (customType instanceof CustomClass) {
        CustomClass clazz = (CustomClass) customType;
        List<String> params = new ArrayList<>();
        List<String> args = new ArrayList<>();
        for (Field f : clazz.fields) {
          if (!f.isRequired()) {
            continue;
          }
          params.add(f.type.toJava() + " " + f.name);
          args.add(f.name);
        }
        if (!params.isEmpty()) {
          writeJavadoc(output, offset, comment());
          output.add(offset + "public " + parentClass + " set" + toTitle(name) + "(" + String.join(", ", params) + ") {");
          output.add(offset + "  return set" + toTitle(name) + "(new " + type.toJava() + "(" + String.join(", ", args) + "));");
          output.add(offset + "}");
        }
      }
    }

    if (isBrowserChannelOption()) {
      output.add(offset + "@Deprecated");
      writeGenericBuilderMethod(output, offset, parentClass, "BrowserChannel");
    }

    writeGenericBuilderMethod(output, offset, parentClass, type.toJava());
  }

  private void writeGenericBuilderMethod(List<String> output, String offset, String parentClass, String paramType) {
    writeJavadoc(output, offset, comment());
    output.add(offset + "public " + parentClass + " set" + toTitle(name) + "(" + paramType + " " + name + ") {");
    String rvalue = type.isNullable() ? "Optional.ofNullable(" + name + ")" : name;
    output.add(offset + "  this." + name + " = " + rvalue + ";");
    output.add(offset + "  return this;");
    output.add(offset + "}");
  }

  private boolean isBrowserChannelOption() {
    return asList("BrowserType.launch.options.channel", "BrowserType.launchPersistentContext.options.channel").contains(jsonPath);
  }
}

class Interface extends TypeDefinition {
  private final List<Method> methods = new ArrayList<>();
  private final List<Event> events = new ArrayList<>();
  private final Map<String, TypeDefinition> topLevelTypes;
  static final String header = "/*\n" +
    " * Copyright (c) Microsoft Corporation.\n" +
    " *\n" +
    " * Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
    " * you may not use this file except in compliance with the License.\n" +
    " * You may obtain a copy of the License at\n" +
    " *\n" +
    " * http://www.apache.org/licenses/LICENSE-2.0\n" +
    " *\n" +
    " * Unless required by applicable law or agreed to in writing, software\n" +
    " * distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
    " * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
    " * See the License for the specific language governing permissions and\n" +
    " * limitations under the License.\n" +
    " */\n";

  private static Set<String> allowedBaseInterfaces = new HashSet<>(asList("Browser", "JSHandle", "BrowserContext"));
  private static Set<String> autoCloseableInterfaces = new HashSet<>(asList("Playwright", "Browser", "BrowserContext", "Page"));

  Interface(JsonObject jsonElement, Map<String, TypeDefinition> topLevelTypes) {
    super(null, jsonElement);
    this.topLevelTypes = topLevelTypes;
    for (JsonElement item : jsonElement.getAsJsonArray("members")) {
      JsonObject memberJson = item.getAsJsonObject();
      switch (memberJson.get("kind").getAsString()) {
        case "method":
        // All properties are converted to methods in Java.
        case "property":
          methods.add(new Method(this, memberJson));
          break;
        case "event":
          events.add(new Event(this, memberJson));
          break;
        default:
          throw new RuntimeException("Unexpected member kind: " + memberJson.toString());
      }
    }
  }

  @Override
  Map<String, TypeDefinition> topLevelTypes() {
    return topLevelTypes;
  }

  void writeTo(List<String> output, String offset) {
    if (methods.stream().anyMatch(m -> "create".equals(m.jsonName))) {
      output.add("import com.microsoft.playwright.impl." + jsonName + "Impl;");
    }
    if (asList("Page", "Request", "Response", "APIRequestContext", "APIRequest", "APIResponse", "FileChooser", "Frame", "FrameLocator", "ElementHandle", "Locator", "Browser", "BrowserContext", "BrowserType", "Mouse", "Keyboard").contains(jsonName)) {
      output.add("import com.microsoft.playwright.options.*;");
    }
    if ("Download".equals(jsonName)) {
      output.add("import java.io.InputStream;");
    }
    if (asList("Page", "Frame", "ElementHandle", "Locator", "FormData", "APIRequest", "APIRequestContext", "FileChooser", "Browser", "BrowserContext", "BrowserType", "Download", "Route", "Selectors", "Tracing", "Video").contains(jsonName)) {
      output.add("import java.nio.file.Path;");
    }
    if (asList("Page", "Frame", "ElementHandle", "Locator", "APIRequest", "Browser", "BrowserContext", "BrowserType", "Route", "Request", "Response", "JSHandle", "ConsoleMessage", "APIResponse", "Playwright").contains(jsonName)) {
      output.add("import java.util.*;");
    }
    if (asList("Page", "Browser", "BrowserContext", "WebSocket", "Worker").contains(jsonName)) {
      output.add("import java.util.function.Consumer;");
    }
    if (asList("Page", "Frame", "BrowserContext", "WebSocket").contains(jsonName)) {
      output.add("import java.util.function.Predicate;");
    }
    if (asList("Page", "Frame", "FrameLocator", "Locator", "Browser", "BrowserType", "BrowserContext", "PageAssertions", "LocatorAssertions").contains(jsonName)) {
      output.add("import java.util.regex.Pattern;");
    }
    if ("PlaywrightAssertions".equals(jsonName)) {
      output.add("import com.microsoft.playwright.APIResponse;");
      output.add("import com.microsoft.playwright.Locator;");
      output.add("import com.microsoft.playwright.Page;");
      output.add("import com.microsoft.playwright.impl.APIResponseAssertionsImpl;");
      output.add("import com.microsoft.playwright.impl.AssertionsTimeout;");
      output.add("import com.microsoft.playwright.impl.LocatorAssertionsImpl;");
      output.add("import com.microsoft.playwright.impl.PageAssertionsImpl;");
    }
    output.add("");

    List<String> superInterfaces = new ArrayList<>();
    if (jsonElement.getAsJsonObject().has("extends")) {
      String base = jsonElement.getAsJsonObject().get("extends").getAsString();
      if (allowedBaseInterfaces.contains(base)) {
        superInterfaces.add(base);
      }
    }
    if (autoCloseableInterfaces.contains(jsonName)) {
      superInterfaces.add("AutoCloseable");
    }
    String implementsClause = superInterfaces.isEmpty() ? "" : " extends " + String.join(", ", superInterfaces);

    writeJavadoc(output, offset, comment());
    output.add("public interface " + jsonName + implementsClause + " {");
    offset = "  ";
    writeEvents(output, offset);
    super.writeTo(output, offset);
    for (Method m : methods) {
      m.writeTo(output, offset);
    }
    output.add("}");
    output.add("\n");
  }

  private void writeEvents(List<String> output, String offset) {
    if (events.isEmpty()) {
      return;
    }
    for (Event e : events) {
      output.add("");
      e.writeListenerMethods(output, offset);
    }
    output.add("");
  }
}

class CustomClass extends TypeDefinition {
  final String name;
  final List<Field> fields = new ArrayList<>();

  CustomClass(Element parent, String name, JsonObject jsonElement) {
    super(parent, true, jsonElement);
    this.name = name;

    JsonObject jsonType = jsonElement;
    if (jsonType.has("union")) {
      if (!jsonName.isEmpty()) {
        throw new RuntimeException("Unexpected named union: " + jsonElement);
      }
      for (JsonElement item : jsonType.getAsJsonArray("union")) {
        if (!"null".equals(item.getAsJsonObject().get("name").getAsString())) {
          jsonType = item.getAsJsonObject();
          break;
        }
      }
    }

    while (jsonType.has("templates")) {
      JsonArray params = jsonType.getAsJsonArray("templates");
      if (params.size() != 1) {
        throw new RuntimeException("Unexpected number of parameters for " + jsonPath + ": " + jsonElement);
      }
      jsonType = params.get(0).getAsJsonObject();
    }

    if (jsonType.has("properties")) {
      for (JsonElement item : jsonType.getAsJsonArray("properties")) {
        JsonObject propertyJson = item.getAsJsonObject();
        String propertyName = propertyJson.get("name").getAsString();
        fields.add(new Field(this, propertyName, propertyJson));
      }
    }
  }

  @Override
  String name() {
    return name;
  }

  @Override
  void writeTo(List<String> output, String offset) {
    if (asList("RecordHar", "RecordVideo").contains(name)) {
      output.add("import java.nio.file.Path;");
    }
    String access = (parent.typeScope() instanceof CustomClass) || topLevelTypes().containsKey(name) ? "public " : "";
    output.add(offset + access + "class " + name + " {");
    String bodyOffset = offset + "  ";
    super.writeTo(output, bodyOffset);

    boolean isReturnType = parent.parent instanceof Method;
    for (Field f : fields) {
      f.writeTo(output, bodyOffset);
    }
    output.add("");
    if (!isReturnType) {
      writeConstructor(output, bodyOffset);
      writeBuilderMethods(output, bodyOffset);
    }
    output.add(offset + "}");
  }

  private void writeBuilderMethods(List<String> output, String bodyOffset) {
    for (Field f : fields) {
      if (!f.isRequired()) {
        f.writeBuilderMethod(output, bodyOffset, name);
      }
    }
  }

  private void writeConstructor(List<String> output, String bodyOffset) {
    List<Field> requiredFields = fields.stream().filter(f -> f.isRequired()).collect(toList());
    if (requiredFields.isEmpty()) {
      return;
    }
    List<String> args = requiredFields.stream().map(f -> f.type.toJava() + " " + f.name).collect(toList());
    output.add(bodyOffset + "public " + name + "(" + String.join(", ", args) + ") {");
    requiredFields.forEach(f -> output.add(bodyOffset + "  this." + f.name + " = " + f.name + ";"));
    output.add(bodyOffset + "}");
  }
}

class Enum extends TypeDefinition {
  final List<String> enumValues;

  Enum(TypeDefinition parent, JsonObject jsonObject) {
    super(parent, jsonObject);
    enumValues = new ArrayList<>();
    for (JsonElement item : jsonObject.getAsJsonArray("union")) {
      String value = item.getAsJsonObject().get("name").getAsString();
      if ("null".equals(value)) {
        throw new RuntimeException("Unexpected null: " + jsonObject);
      }
      enumValues.add(value.substring(1, value.length() - 1).replace("-", "_").toUpperCase());
    }
    if ("BrowserChannel".equals(jsonName)) {
      // Firefox stable 'channel' was removed in 1.12.0
      enumValues.add("@Deprecated FIREFOX_STABLE");
    }
  }

  @Override
  void writeTo(List<String> output, String offset) {
    output.add("public enum " + jsonName + " {\n  " + String.join(",\n  ", enumValues) + "\n}");
  }

  boolean hasSameValues(Enum other) {
    return enumValues.equals(other.enumValues);
  }
}

public class ApiGenerator {
  // TODO: make it an instance field.
  static final Map<String, String> aliases = new HashMap<>();

  ApiGenerator(Reader reader) throws IOException {
    JsonArray api = new Gson().fromJson(reader, JsonArray.class);
    File cwd = FileSystems.getDefault().getPath(".").toFile();
    filterOtherLangs(api, new Stack<>());

    File dir = new File(cwd, "playwright/src/main/java/com/microsoft/playwright");
    System.out.println("Writing files to: " + dir.getCanonicalPath());
    generate(api, dir, "com.microsoft.playwright", isAssertion().negate());

    File assertionsDir = new File(cwd,"playwright/src/main/java/com/microsoft/playwright/assertions");
    System.out.println("Writing assertion files to: " + dir.getCanonicalPath());
    generate(api, assertionsDir, "com.microsoft.playwright.assertions", isAssertion());
  }

  private static Predicate<String> isAssertion() {
    return className -> className.toLowerCase().contains("assert");
  }

  private void generate(JsonArray api, File dir, String packageName, Predicate<String> classFilter) throws IOException {
    Map<String, TypeDefinition> topLevelTypes = new HashMap<>();
    for (JsonElement entry: api) {
      String name = entry.getAsJsonObject().get("name").getAsString();
      // We write this one manually.
      if (asList("PlaywrightException", "TimeoutError").contains(name)) {
        continue;
      }
      if (!classFilter.test(name)) {
        continue;
      }
      Interface iface = new Interface(entry.getAsJsonObject(), topLevelTypes);
      if (asList("RequestOptions", "FormData").contains(name)) {
        topLevelTypes.put(name, iface);
        continue;
      }
      List<String> lines = new ArrayList<>();
      lines.add(Interface.header);
      lines.add("package " + packageName + ";");
      lines.add("");
      iface.writeTo(lines, "");
      String text = String.join("\n", lines);
      try (FileWriter writer = new FileWriter(new File(dir, name + ".java"))) {
        writer.write(text);
      }
    }

    dir = new File(dir, "options");
    for (TypeDefinition e : topLevelTypes.values()) {
      List<String> lines = new ArrayList<>();
      lines.add(Interface.header);
      lines.add("package " + packageName + ".options;");
      lines.add("");
      e.writeTo(lines, "");
      String text = String.join("\n", lines);
      try (FileWriter writer = new FileWriter(new File(dir, e.name() + ".java"))) {
        writer.write(text);
      }
    }
  }

  private static void filterOtherLangs(JsonElement json, Stack<String> path) {
    if (json.isJsonArray()) {
      List<Integer> toRemove = new ArrayList<>();
      JsonArray array = json.getAsJsonArray();
      for (int i = 0; i < array.size(); i++) {
        JsonElement item = array.get(i);
        if (isSupported(item)) {
          filterOtherLangs(item, path);
        } else {
          toRemove.add(i);
        }
      }
      reverse(toRemove);
      for (int index : toRemove) {
        array.remove(index);
      }
    } else if (json.isJsonObject()) {
      List<String> toRemove = new ArrayList<>();
      JsonObject object = json.getAsJsonObject();
      path.push(object.has("name") ? object.get("name").getAsString() : "<none>");
      String alias = alias(object);
      if (alias != null) {
        List<String> aliasPath = new ArrayList<>(path);
        aliasPath.set(aliasPath.size() - 1, alias);
        aliases.put(String.join(".", path), String.join(".", aliasPath));
        // Save original name.
        object.addProperty("originalName", object.get("name").getAsString());
        // Rename in place.
        object.addProperty("name", alias);
      }
      overrideType(object);

      for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
        if (isSupported(entry.getValue())) {
          filterOtherLangs(entry.getValue(), path);
        } else {
          toRemove.add(entry.getKey());
        }
      }
      path.pop();
      for (String key : toRemove) {
        object.remove(key);
      }
    }
  }

  private static void overrideType(JsonObject jsonObject) {
    if (!jsonObject.has("langs")) {
      return;
    }
    JsonObject langs = jsonObject.getAsJsonObject("langs");
    if (!langs.has("types")) {
      return;
    }
    JsonElement type = langs.getAsJsonObject("types").get("java");
    if (type == null) {
      return;
    }
    jsonObject.add("type", type);
  }

  private static String alias(JsonObject jsonObject) {
    if (!jsonObject.has("langs")) {
      return null;
    }
    JsonObject langs = jsonObject.getAsJsonObject("langs");
    if (!langs.has("aliases")) {
      return null;
    }
    JsonElement javaAlias = langs.getAsJsonObject("aliases").get("java");
    if (javaAlias == null) {
      return null;
    }
    return javaAlias.getAsString();
  }

  private static boolean isSupported(JsonElement json) {
    if (!json.isJsonObject()) {
      return true;
    }
    JsonObject jsonObject = json.getAsJsonObject();
    if (!jsonObject.has("langs")) {
      return true;
    }
    JsonObject langs = jsonObject.getAsJsonObject("langs");
    if (!langs.has("only")) {
      return true;
    }
    JsonArray only = langs.getAsJsonArray("only");
    for (JsonElement lang : only) {
      if ("java".equals(lang.getAsString())) {
        return true;
      }
    }
    return false;
  }

  public static void main(String[] args) throws IOException {
    File cwd = FileSystems.getDefault().getPath(".").toFile();
    File file = new File(cwd, "tools/api-generator/src/main/resources/api.json");
    System.out.println("Reading from: " + file.getCanonicalPath());
    new ApiGenerator(new FileReader(file));
  }
}
