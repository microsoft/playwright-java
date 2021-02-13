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
        .replace("*/", "*\\/")
        .replace("NOTE: ", "<strong>NOTE:</strong> ")
        .replaceAll("`([^`]+)`", "{@code $1}"));
    }
    output.add(offset + " */");
  }

  String formattedComment() {
    return comment()
      // Remove any code snippets between ``` and ```.
      .replaceAll("\\n```((?<!`)`(?!`)|[^`])+```\\n", "")
      .replaceAll("\\nAn example of[^\\n]+\\n", "")
      .replaceAll("\\nThis example [^\\n]+\\n", "")
      .replaceAll("\\nExamples:\\n", "")
      .replaceAll("\\nSee ChromiumBrowser[^\\n]+", "\n")
      // > **NOTE** ... => **NOTE** ...
      .replaceAll("\\n> ", "\n")
      .replaceAll("\\n\\n", "\n\n<p> ");
  }

  String comment() {
    JsonObject json = jsonElement.getAsJsonObject();
    if (!json.has("comment")) {
      return "";
    }
    return json.get("comment").getAsString();
  }
}

// Represents return type of a method, type of a method param or type of a field.
class TypeRef extends Element {
  String customType;
  boolean isNestedClass;

  TypeRef(Element parent, JsonElement jsonElement) {
    super(parent, true, jsonElement);

    createCustomType();
  }

  private static String typeExpression(JsonObject jsonType) {
    String typeName = jsonType.get("name").getAsString();
    if (jsonType.has("union")) {
      List<String> values = new ArrayList<>();
      for (JsonElement item : jsonType.getAsJsonArray("union")) {
        values.add(typeExpression(item.getAsJsonObject()));
      }
      values.sort(String::compareTo);
      String enumValues = String.join("|", values);
      return typeName.isEmpty() ? enumValues : typeName + "<" + enumValues + ">";
    }
    if ("function".equals(typeName)) {
      if (!jsonType.has("args")) {
        return typeName;
      }
      List<String> args = new ArrayList<>();
      for (JsonElement item : jsonType.getAsJsonArray("args")) {
        args.add(typeExpression(item.getAsJsonObject()));
      }
      String returnType = "";
      if (jsonType.has("returnType") && jsonType.get("returnType").isJsonObject()) {
        returnType = ":" + typeExpression(jsonType.getAsJsonObject("returnType"));
      }
      return typeName + "(" + String.join(", ", args) + ")" + returnType;
    }
    List<String> templateArgs = new ArrayList<>();
    if (jsonType.has("templates")) {
      for (JsonElement item : jsonType.getAsJsonArray("templates")) {
        templateArgs.add(typeExpression(item.getAsJsonObject()));
      }
    }
    if (templateArgs.isEmpty()) {
      return typeName;
    }
    return typeName + "<" + String.join(", ", templateArgs) + ">";
  }

  void createCustomType() {
    // Use path to the corresponding method, param of field as the key.
    String parentPath = parent.jsonPath;
    Types.Mapping mapping = TypeDefinition.types.findForPath(parentPath);
    if (mapping != null) {
      String typeExpression = typeExpression(jsonElement.getAsJsonObject());
      if (!mapping.from.equals(typeExpression)) {
        throw new RuntimeException("Unexpected source type for: " + parentPath +". Expected: " + mapping.from + "; found: " + typeExpression);
      }
      customType = mapping.to;
      return;
    }
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
        throw new RuntimeException("Custom type has already been created: " + customType);
      }
      if (parent.jsonName.equals("cookies")) {
        customType = "Cookie";
        typeScope().createTopLevelClass(customType, this, jsonElement.getAsJsonObject());
      } else if (parent instanceof Method || parent instanceof Field || (parent instanceof Param && !"options".equals(parent.jsonName))) {
        customType = toTitle(parent.jsonName);
        typeScope().createTopLevelClass(customType, this, jsonElement.getAsJsonObject());
      } else {
        customType = toTitle(parent.parent.jsonName) + toTitle(parent.jsonName);
        typeScope().createNestedClass(customType, this, jsonElement.getAsJsonObject());
        isNestedClass = true;
      }
    }
  }

  String toJava() {
    if (customType != null) {
      return customType;
    }
    if (jsonElement.isJsonNull()) {
      return "void";
    }
    return convertBuiltinType(stripNullable());
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
      try {
        if (o.get("name").getAsString().equals("function") && !o.has("args")) {
          continue;
        }
      } catch (NullPointerException e) {
        System.out.println(o);
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

  private static String convertBuiltinType(JsonObject jsonType) {
    String name = jsonType.get("name").getAsString();
    if (jsonType.has("union")) {
      if (name.isEmpty()) {
        throw new RuntimeException("Unexpected enum without name: " + jsonType);
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
      return "List<" + convertTemplateParams(jsonType) + ">";
    }
    if ("Object".equals(name)) {
      String expression = typeExpression(jsonType);
      if (!"Object<string, string>".equals(expression) && !"Object<string, any>".equals(expression)) {
        throw new RuntimeException("Unexpected object type: " + typeExpression(jsonType));
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

  private static String convertTemplateParams(JsonObject jsonType) {
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
  final List<NestedClass> classes = new ArrayList<>();

  static final Types types = new Types();

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
    TypeDefinition existing = map.putIfAbsent(name, new NestedClass(parent, name, jsonObject));
    if (existing != null && (!(existing instanceof NestedClass))) {
      throw new RuntimeException("Two classes with same name have different values:\n" + jsonObject + "\n" + existing.jsonElement);
    }
  }

  void createNestedClass(String name, Element parent, JsonObject jsonObject) {
    for (NestedClass c : classes) {
      if (c.name.equals(name)) {
        return;
      }
    }
    classes.add(new NestedClass(parent, name, jsonObject));
  }

  void writeTo(List<String> output, String offset) {
    for (NestedClass c : classes) {
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
    String name = toTitle(jsonName);
    String paramType = type.toJava();
    String listenerType = "Consumer<" + paramType + ">";
    output.add(offset + "void on" + name + "(" + listenerType + " handler);");
    output.add(offset + "void off" + name + "(" + listenerType + " handler);");
  }
}

class Method extends Element {
  final TypeRef returnType;
  final List<Param> params = new ArrayList<>();

  private static Map<String, String[]> customSignature = new HashMap<>();
  static {
    customSignature.put("Page.setViewportSize", new String[]{"void setViewportSize(int width, int height);"});
    customSignature.put("BrowserContext.cookies", new String[]{
      "default List<Cookie> cookies() { return cookies((List<String>) null); }",
      "default List<Cookie> cookies(String url) { return cookies(Arrays.asList(url)); }",
      "List<Cookie> cookies(List<String> urls);",
    });
    customSignature.put("BrowserContext.addCookies", new String[]{
      "void addCookies(List<Cookie> cookies);"
    });
    customSignature.put("FileChooser.setFiles", new String[]{
      "default void setFiles(Path file) { setFiles(file, null); }",
      "default void setFiles(Path file, SetFilesOptions options) { setFiles(new Path[]{ file }, options); }",
      "default void setFiles(Path[] files) { setFiles(files, null); }",
      "void setFiles(Path[] files, SetFilesOptions options);",
      "default void setFiles(FileChooser.FilePayload file) { setFiles(file, null); }",
      "default void setFiles(FileChooser.FilePayload file, SetFilesOptions options)  { setFiles(new FileChooser.FilePayload[]{ file }, options); }",
      "default void setFiles(FileChooser.FilePayload[] files) { setFiles(files, null); }",
      "void setFiles(FileChooser.FilePayload[] files, SetFilesOptions options);",
    });
    customSignature.put("ElementHandle.setInputFiles", new String[]{
      "default void setInputFiles(Path file) { setInputFiles(file, null); }",
      "default void setInputFiles(Path file, SetInputFilesOptions options) { setInputFiles(new Path[]{ file }, options); }",
      "default void setInputFiles(Path[] files) { setInputFiles(files, null); }",
      "void setInputFiles(Path[] files, SetInputFilesOptions options);",
      "default void setInputFiles(FileChooser.FilePayload file) { setInputFiles(file, null); }",
      "default void setInputFiles(FileChooser.FilePayload file, SetInputFilesOptions options)  { setInputFiles(new FileChooser.FilePayload[]{ file }, options); }",
      "default void setInputFiles(FileChooser.FilePayload[] files) { setInputFiles(files, null); }",
      "void setInputFiles(FileChooser.FilePayload[] files, SetInputFilesOptions options);",
    });
    String[] setInputFilesWithSelector = {
      "default void setInputFiles(String selector, Path file) { setInputFiles(selector, file, null); }",
      "default void setInputFiles(String selector, Path file, SetInputFilesOptions options) { setInputFiles(selector, new Path[]{ file }, options); }",
      "default void setInputFiles(String selector, Path[] files) { setInputFiles(selector, files, null); }",
      "void setInputFiles(String selector, Path[] files, SetInputFilesOptions options);",
      "default void setInputFiles(String selector, FileChooser.FilePayload file) { setInputFiles(selector, file, null); }",
      "default void setInputFiles(String selector, FileChooser.FilePayload file, SetInputFilesOptions options)  { setInputFiles(selector, new FileChooser.FilePayload[]{ file }, options); }",
      "default void setInputFiles(String selector, FileChooser.FilePayload[] files) { setInputFiles(selector, files, null); }",
      "void setInputFiles(String selector, FileChooser.FilePayload[] files, SetInputFilesOptions options);",
    };
    customSignature.put("Page.setInputFiles", setInputFilesWithSelector);
    customSignature.put("Frame.setInputFiles", setInputFilesWithSelector);

    String[] selectOption = {
      "default List<String> selectOption(String selector, String value) {",
      "  return selectOption(selector, value, null);",
      "}",
      "default List<String> selectOption(String selector, String value, SelectOptionOptions options) {",
      "  String[] values = value == null ? null : new String[]{ value };",
      "  return selectOption(selector, values, options);",
      "}",
      "default List<String> selectOption(String selector, String[] values) {",
      "  return selectOption(selector, values, null);",
      "}",
      "default List<String> selectOption(String selector, String[] values, SelectOptionOptions options) {",
      "  if (values == null) {",
      "    return selectOption(selector, new ElementHandle.SelectOption[0], options);",
      "  }",
      "  return selectOption(selector, Arrays.asList(values).stream().map(",
      "    v -> new ElementHandle.SelectOption().withValue(v)).toArray(ElementHandle.SelectOption[]::new), options);",
      "}",
      "default List<String> selectOption(String selector, ElementHandle.SelectOption value) {",
      "  return selectOption(selector, value, null);",
      "}",
      "default List<String> selectOption(String selector, ElementHandle.SelectOption value, SelectOptionOptions options) {",
      "  ElementHandle.SelectOption[] values = value == null ? null : new ElementHandle.SelectOption[]{value};",
      "  return selectOption(selector, values, options);",
      "}",
      "default List<String> selectOption(String selector, ElementHandle.SelectOption[] values) {",
      "  return selectOption(selector, values, null);",
      "}",
      "List<String> selectOption(String selector, ElementHandle.SelectOption[] values, SelectOptionOptions options);",
      "default List<String> selectOption(String selector, ElementHandle value) {",
      "  return selectOption(selector, value, null);",
      "}",
      "default List<String> selectOption(String selector, ElementHandle value, SelectOptionOptions options) {",
      "  ElementHandle[] values = value == null ? null : new ElementHandle[]{value};",
      "  return selectOption(selector, values, options);",
      "}",
      "default List<String> selectOption(String selector, ElementHandle[] values) {",
      "  return selectOption(selector, values, null);",
      "}",
      "List<String> selectOption(String selector, ElementHandle[] values, SelectOptionOptions options);",
    };
    customSignature.put("Page.selectOption", selectOption);
    customSignature.put("Frame.selectOption", selectOption);
    customSignature.put("ElementHandle.selectOption", Arrays.stream(selectOption).map(s -> s
      .replace("String selector, ", "")
      .replace("(selector, ", "(")
      .replace("ElementHandle.", "")).toArray(String[]::new));
  }

  Method(TypeDefinition parent, JsonObject jsonElement) {
    super(parent, jsonElement);
    if (customSignature.containsKey(jsonPath) && customSignature.get(jsonPath).length == 0) {
      returnType = null;
    } else {
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
  }

  void writeTo(List<String> output, String offset) {
    if (customSignature.containsKey(jsonPath)) {
      String[] signatures = customSignature.get(jsonPath);
      for (int i = 0; i < signatures.length; i++) {
        if (i == signatures.length - 1) {
          writeJavadoc(params, output, offset);
        }
        output.add(offset + signatures[i]);
      }
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
        argList.add("int".equals(params.get(firstNullOptional).type.toJava()) ? "0" : "null");
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
    sections.add(formattedComment());
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

  Field(NestedClass parent, String name, JsonObject jsonElement) {
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
    if (asList("Frame.waitForNavigation.options.url",
               "Page.waitForNavigation.options.url").contains(jsonPath)) {
      output.add(offset + "public String glob;");
      output.add(offset + "public Pattern pattern;");
      output.add(offset + "public Predicate<String> predicate;");
      return;
    }
    if (asList("Frame.waitForFunction.options.polling",
               "Page.waitForFunction.options.polling").contains(jsonPath)) {
      output.add(offset + "public Integer pollingInterval;");
      return;
    }
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
    output.add(offset + "public " + typeStr + " " + name + ";");
  }

  void writeBuilderMethod(List<String> output, String offset, String parentClass) {
    if (asList("Frame.waitForNavigation.options.url",
               "Page.waitForNavigation.options.url").contains(jsonPath)) {
      output.add(offset + "public WaitForNavigationOptions withUrl(String glob) {");
      output.add(offset + "  this.glob = glob;");
      output.add(offset + "  return this;");
      output.add(offset + "}");
      output.add(offset + "public WaitForNavigationOptions withUrl(Pattern pattern) {");
      output.add(offset + "  this.pattern = pattern;");
      output.add(offset + "  return this;");
      output.add(offset + "}");
      output.add(offset + "public WaitForNavigationOptions withUrl(Predicate<String> predicate) {");
      output.add(offset + "  this.predicate = predicate;");
      output.add(offset + "  return this;");
      output.add(offset + "}");
      return;
    }
    if (asList("Frame.waitForFunction.options.polling",
               "Page.waitForFunction.options.polling").contains(jsonPath)) {
      output.add(offset + "public WaitForFunctionOptions withRequestAnimationFrame() {");
      output.add(offset + "  this.pollingInterval = null;");
      output.add(offset + "  return this;");
      output.add(offset + "}");
      output.add(offset + "public WaitForFunctionOptions withPollingInterval(int millis) {");
      output.add(offset + "  this.pollingInterval = millis;");
      output.add(offset + "  return this;");
      output.add(offset + "}");
      return;
    }
    if (asList("Page.click.options.position",
      "Page.dblclick.options.position",
      "Page.hover.options.position",
      "Frame.click.options.position",
      "Frame.dblclick.options.position",
      "Frame.hover.options.position",
      "ElementHandle.click.options.position",
      "ElementHandle.dblclick.options.position",
      "ElementHandle.hover.options.position").contains(jsonPath)) {
      output.add(offset + "public " + parentClass + " withPosition(Position position) {");
      output.add(offset + "  this.position = position;");
      output.add(offset + "  return this;");
      output.add(offset + "}");
      output.add(offset + "public " + parentClass + " withPosition(double x, double y) {");
      output.add(offset + "  return withPosition(new Position(x, y));");
      output.add(offset + "}");
      return;
    }
    if ("Route.resume.options.postData".equals(jsonPath)) {
      output.add(offset + "public " + parentClass + " withPostData(String postData) {");
      output.add(offset + "  this.postData = postData.getBytes(StandardCharsets.UTF_8);");
      output.add(offset + "  return this;");
      output.add(offset + "}");
    }
    if ("ViewportSize".equals(type.toJava())) {
      output.add(offset + "public " + parentClass + " with" + toTitle(name) + "(int width, int height) {");
      output.add(offset + "  return with" + toTitle(name) + "(new " + type.toJava() + "(width, height));");
      output.add(offset + "}");
    }
    if (name.equals("httpCredentials")) {
      output.add(offset + "public " + parentClass + " with" + toTitle(name) + "(String username, String password) {");
      output.add(offset + "  this." + name + " = new " + type.toJava() + "(username, password);");
      output.add(offset + "  return this;");
    } else if (type.isNestedClass) {
      output.add(offset + "public " + type.toJava() + " set" + toTitle(name) + "() {");
      output.add(offset + "  this." + name + " = new " + type.toJava() + "();");
      output.add(offset + "  return this." + name + ";");
    } else if ("Browser.VideoSize".equals(type.toJava()) || "VideoSize".equals(type.toJava())) {
      output.add(offset + "public " + parentClass + " with" + toTitle(name) + "(int width, int height) {");
      output.add(offset + "  this." + name + " = new " + type.toJava() + "(width, height);");
      output.add(offset + "  return this;");
    } else {
      String paramType = type.toJava();
      if ("Boolean".equals(paramType)) {
        paramType = "boolean";
      } else if ("Integer".equals(paramType)) {
        paramType = "int";
      } else if ("Double".equals(paramType)) {
        paramType = "double";
      }
      output.add(offset + "public " + parentClass + " with" + toTitle(name) + "(" + paramType + " " + name + ") {");
      String rvalue = type.isNullable() ? "Optional.ofNullable(" + name + ")" : name;
      output.add(offset + "  this." + name + " = " + rvalue + ";");
      output.add(offset + "  return this;");
    }
    output.add(offset + "}");
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
    " */\n" +
    "\n" +
    "package com.microsoft.playwright;\n";

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
    output.add(header);
    if ("Playwright".equals(jsonName)) {
      output.add("import com.microsoft.playwright.impl.PlaywrightImpl;");
    }
    if (asList("Page", "Request", "Frame", "ElementHandle", "Browser", "BrowserContext", "BrowserType", "Mouse", "Keyboard").contains(jsonName)) {
      output.add("import com.microsoft.playwright.options.*;");
    }
    if (jsonName.equals("Route")) {
      output.add("import java.nio.charset.StandardCharsets;");
    }
    if ("Download".equals(jsonName)) {
      output.add("import java.io.InputStream;");
    }
    if (asList("Page", "Frame", "ElementHandle", "FileChooser", "Browser", "BrowserContext", "BrowserType", "Download", "Route", "Selectors", "Video").contains(jsonName)) {
      output.add("import java.nio.file.Path;");
    }
    output.add("import java.util.*;");
    if (asList("Page", "Browser", "BrowserContext", "WebSocket", "Worker").contains(jsonName)) {
      output.add("import java.util.function.Consumer;");
    }
    if (asList("Page", "Frame", "BrowserContext", "WebSocket").contains(jsonName)) {
      output.add("import java.util.function.Predicate;");
    }
    if (asList("Page", "Frame", "BrowserContext").contains(jsonName)) {
      output.add("import java.util.regex.Pattern;");
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

    writeJavadoc(output, offset, formattedComment());
    output.add("public interface " + jsonName + implementsClause + " {");
    offset = "  ";
    writeSharedTypes(output, offset);
    writeEvents(output, offset);
    super.writeTo(output, offset);
    for (Method m : methods) {
      m.writeTo(output, offset);
    }
    if ("Playwright".equals(jsonName)) {
      output.add("");
      output.add(offset + "static Playwright create() {");
      output.add(offset + "  return PlaywrightImpl.create();");
      output.add(offset + "}");
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

  private void writeSharedTypes(List<String> output, String offset) {
    switch (jsonName) {
      case "ElementHandle": {
        output.add(offset + "class SelectOption {");
        output.add(offset + "  public String value;");
        output.add(offset + "  public String label;");
        output.add(offset + "  public Integer index;");
        output.add("");
        output.add(offset + "  public SelectOption withValue(String value) {");
        output.add(offset + "    this.value = value;");
        output.add(offset + "    return this;");
        output.add(offset + "  }");
        output.add(offset + "  public SelectOption withLabel(String label) {");
        output.add(offset + "    this.label = label;");
        output.add(offset + "    return this;");
        output.add(offset + "  }");
        output.add(offset + "  public SelectOption withIndex(int index) {");
        output.add(offset + "    this.index = index;");
        output.add(offset + "    return this;");
        output.add(offset + "  }");
        output.add(offset + "}");
        output.add("");
        break;
      }
      case "FileChooser": {
        output.add(offset + "class FilePayload {");
        output.add(offset + "  public final String name;");
        output.add(offset + "  public final String mimeType;");
        output.add(offset + "  public final byte[] buffer;");
        output.add("");
        output.add(offset + "  public FilePayload(String name, String mimeType, byte[] buffer) {");
        output.add(offset + "    this.name = name;");
        output.add(offset + "    this.mimeType = mimeType;");
        output.add(offset + "    this.buffer = buffer;");
        output.add(offset + "  }");
        output.add(offset + "}");
        output.add("");
        break;
      }
    }
  }
}

class NestedClass extends TypeDefinition {
  final String name;
  final List<Field> fields = new ArrayList<>();

  NestedClass(Element parent, String name, JsonObject jsonElement) {
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
    String access = (parent.typeScope() instanceof NestedClass) || topLevelTypes().containsKey(name) ? "public " : "";
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
  ApiGenerator(Reader reader) throws IOException {
    JsonArray api = new Gson().fromJson(reader, JsonArray.class);
    File cwd = FileSystems.getDefault().getPath(".").toFile();
    File dir = new File(cwd, "playwright/src/main/java/com/microsoft/playwright");
    System.out.println("Writing files to: " + dir.getCanonicalPath());
    filterOtherLangs(api);
    Map<String, TypeDefinition> topLevelTypes = new HashMap<>();
    for (JsonElement entry: api) {
      String name = entry.getAsJsonObject().get("name").getAsString();
      List<String> lines = new ArrayList<>();
      new Interface(entry.getAsJsonObject(), topLevelTypes).writeTo(lines, "");
      String text = String.join("\n", lines);
      try (FileWriter writer = new FileWriter(new File(dir, name + ".java"))) {
        writer.write(text);
      }
    }
    dir = new File(dir, "options");
    for (TypeDefinition e : topLevelTypes.values()) {
      List<String> lines = new ArrayList<>();
      lines.add(Interface.header.replace("package com.microsoft.playwright;", "package com.microsoft.playwright.options;"));
      e.writeTo(lines, "");
      String text = String.join("\n", lines);
      try (FileWriter writer = new FileWriter(new File(dir, e.name() + ".java"))) {
        writer.write(text);
      }
    }
  }

  private static void filterOtherLangs(JsonElement json) {
    if (json.isJsonArray()) {
      List<Integer> toRemove = new ArrayList<>();
      JsonArray array = json.getAsJsonArray();
      for (int i = 0; i < array.size(); i++) {
        JsonElement item = array.get(i);
        if (isSupported(item)) {
          filterOtherLangs(item);
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
      String alias = alias(object);
      if (alias != null) {
        // Rename in place.
        object.addProperty("name", alias);
      }
      overrideType(object);
      for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
        if (isSupported(entry.getValue())) {
          filterOtherLangs(entry.getValue());
        } else {
          toRemove.add(entry.getKey());
        }
      }
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
    System.out.println(cwd.getCanonicalPath());
    File file = new File(cwd, "tools/api-generator/src/main/resources/api.json");
    System.out.println("Reading from: " + file.getCanonicalPath());
    new ApiGenerator(new FileReader(file));
  }
}
