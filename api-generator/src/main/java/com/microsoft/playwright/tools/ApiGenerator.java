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

package com.microsoft.playwright.tools;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.*;
import java.nio.file.FileSystems;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

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
      this.jsonName = jsonElement.getAsJsonObject().get("name").getAsString();;
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

  static String toTitle(String name) {
    return Character.toUpperCase(name.charAt(0)) + name.substring(1);
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

  void createCustomType() {
    boolean isEnum = jsonName.contains("|\"");
    boolean isClass = jsonName.equals("Object");
    // Use path to the corresponding method, param of field as the key.
    String parentPath = parent.jsonPath;
    Types.Mapping mapping = TypeDefinition.types.findForPath(parentPath);
    if (mapping == null) {
      if (isEnum) {
        throw new RuntimeException("Cannot create enum, type mapping is missing for: " + parentPath);
      }
      if (!isClass) {
        return;
      }

      if (parent instanceof Field) {
        customType = toTitle(parent.jsonName);
      } else {
        customType = toTitle(parent.parent.jsonName) + toTitle(parent.jsonName);
      }
    } else {
      if (!mapping.from.equals(jsonName)) {
        throw new RuntimeException("Unexpected source type for: " + parentPath);
      }
      customType = mapping.to;
      if (mapping.customMapping != null) {
        mapping.customMapping.defineTypesIn(typeScope());
        return;
      }
    }
    if (isEnum) {
      typeScope().createEnum(customType, jsonName);
    } else if (isClass) {
      typeScope().createNestedClass(customType, this, jsonElement.getAsJsonObject());
      isNestedClass = true;
    }
  }

  String toJava() {
    if (customType != null) {
      return customType;
    }
    if (jsonElement.isJsonNull()) {
      return "void";
    }
    // Convert optional fields to boxed types.
    if (!parent.jsonElement.getAsJsonObject().get("required").getAsBoolean()) {
      if (jsonName.equals("number")) {
        return "Integer";
      }
      if (jsonName.equals("boolean")) {
        return "Boolean";
      }
    }
    if (jsonName.replace("null|", "").contains("|")) {
      throw new RuntimeException("Missing mapping for type union: " + jsonPath + ": " + jsonName);
    }
//    if (jsonPath.contains("geolocation")) {
//      System.out.println(jsonPath + ": " + jsonName);
//    }
    return convertBuiltinType(stripPromise(jsonName));
  }

  private static String stripPromise(String type) {
    if ("Promise".equals(type)) {
      return "void";
    }
    // Java API is sync just strip Promise<>
    if (type.startsWith("Promise<")) {
      return type.substring("Promise<".length(), type.length() - 1);
    }
    return type;
  }

  private static String convertBuiltinType(String type) {
    return type.replace("Array<", "List<")
      .replace("Object<", "Map<")
      .replace("string", "String")
      .replace("number", "int")
      .replace("null|", "");
  }
}

abstract class TypeDefinition extends Element {
  final List<Enum> enums = new ArrayList<>();
  final List<NestedClass> classes = new ArrayList<>();

  static final Types types = new Types();

  TypeDefinition(Element parent, JsonObject jsonElement) {
    super(parent, jsonElement);
  }

  TypeDefinition(Element parent, boolean useParentJsonPath, JsonObject jsonElement) {
    super(parent, useParentJsonPath, jsonElement);
  }

  @Override
  TypeDefinition typeScope() {
    return this;
  }

  void createEnum(String name, String values) {
    addEnum(new Enum(this, name, values));
  }

  void addEnum(Enum newEnum) {
    for (Enum e : enums) {
      if (e.name.equals(newEnum.name)) {
        return;
      }
    }
    enums.add(newEnum);
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
    for (Enum e : enums) {
      e.writeTo(output, offset);
    }
    for (NestedClass c : classes) {
      c.writeTo(output, offset);
    }
  }
}

class Method extends Element {
  final TypeRef returnType;
  final List<Param> params = new ArrayList<>();

  private static Map<String, String> tsToJavaMethodName = new HashMap<>();
  static {
    tsToJavaMethodName.put("continue", "continue_");
    tsToJavaMethodName.put("$eval", "evalOnSelector");
    tsToJavaMethodName.put("$$eval", "evalOnSelectorAll");
    tsToJavaMethodName.put("$", "querySelector");
    tsToJavaMethodName.put("$$", "querySelectorAll");
    tsToJavaMethodName.put("goto", "navigate");
  }

  Method(TypeDefinition parent, JsonObject jsonElement) {
    super(parent, jsonElement);
    returnType = new TypeRef(this, jsonElement.get("type"));
    if (jsonElement.get("args") != null) {
      for (Map.Entry<String, JsonElement> arg : jsonElement.get("args").getAsJsonObject().entrySet()) {
        params.add(new Param(this, arg.getValue().getAsJsonObject()));
      }
    }
  }

  String toJava() {
    String name = jsonName;
    if (tsToJavaMethodName.containsKey(name)) {
      name = tsToJavaMethodName.get(name);
    }

    StringBuilder paramList = new StringBuilder();
    for (Param p : params) {
      if (paramList.length() > 0)
        paramList.append(", ");
      paramList.append(p.toJava());
    }

    return returnType.toJava() + " " + name + "(" + paramList + ");";
  }

  void writeTo(List<String> output, String offset) {
    output.add(offset + toJava());
  }
}

class Param extends Element {
  final TypeRef type;

  Param(Method method, JsonObject jsonElement) {
    super(method, jsonElement);
    type = new TypeRef(this, jsonElement.get("type").getAsJsonObject());
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

  String toJava() {
    return type.toJava() + " " + name;
  }

  void writeTo(List<String> output, String offset) {
    output.add(offset + toJava() + ";");
  }
}

class Interface extends TypeDefinition {
  private final List<Method> methods = new ArrayList<>();
  private static String header = "/**\n" +
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

  Interface(JsonObject jsonElement) {
    super(null, jsonElement);

    JsonObject members = jsonElement.get("members").getAsJsonObject();
    for (Map.Entry<String, JsonElement> m : members.entrySet())
      createMember(m.getValue().getAsJsonObject());
  }

  private void createMember(JsonObject json) {
    String kind = json.get("kind").getAsString();
    if ("method".equals(kind)) {
      methods.add(new Method(this, json));
    }
  }

  void writeTo(List<String> output, String offset) {
    output.add(header);
    output.add("import java.util.*;");
    output.add("import java.util.function.BiConsumer;");
    output.add("");
    output.add("public interface " + jsonName + " {");
    super.writeTo(output, offset + "  ");
    for (Method m : methods) {
      m.writeTo(output, offset + "  ");
    }
    output.add("}");
    output.add("\n");
  }
}

class NestedClass extends TypeDefinition {
  final String name;
  final List<Field> fields = new ArrayList<>();

  NestedClass(Element parent, String name, JsonObject jsonElement) {
    super(parent, true, jsonElement);
    this.name = name;

    JsonObject properties = jsonElement.get("properties").getAsJsonObject();
    for (Map.Entry<String, JsonElement> m : properties.entrySet()) {
      fields.add(new Field(this, m.getKey(), m.getValue().getAsJsonObject()));
    }
  }

  void writeTo(List<String> output, String offset) {
    String access = parent.typeScope() instanceof NestedClass ? "public " : "";
    output.add(offset + access + "class " + name + " {");
    String bodyOffset = offset + "  ";
    super.writeTo(output, bodyOffset);
    for (Field f : fields) {
      f.writeTo(output, bodyOffset);
    }
    output.add("");
    writeBuilderMethods(output, bodyOffset);
    output.add(offset + "}");
  }

  private void writeBuilderMethods(List<String> output, String bodyOffset) {
    if (parent.typeScope() instanceof  NestedClass) {
      NestedClass outer = (NestedClass) parent.typeScope();
      output.add(bodyOffset + name + "() {");
      output.add(bodyOffset + "}");
      output.add(bodyOffset + "public " + outer.name + " done() {");
      output.add(bodyOffset + "  return " + outer.name + ".this;");
      output.add(bodyOffset + "}");
      output.add("");
    }
    for (Field f : fields) {
      if (f.type.isNestedClass) {
        output.add(bodyOffset + "public " + f.type.toJava() + " set" + toTitle(f.name) + "() {");
        output.add(bodyOffset + "  this." + f.name + " = new " + f.type.toJava() + "();");
        output.add(bodyOffset + "  return this." + f.name + ";");
      } else {
        output.add(bodyOffset + "public " + name + " with" + toTitle(f.name) + "(" + f.toJava() + ") {");
        output.add(bodyOffset + "  this." + f.name + " = " + f.name + ";");
        output.add(bodyOffset + "  return this;");
      }
      output.add(bodyOffset + "}");
    }
  }
}

class Enum extends TypeDefinition {
  final String name;
  final List<String> enumValues;

  Enum(TypeDefinition parent, String name, String values) {
    super(parent, null);
    this.name = name;
    String[] split = values.split("\\|");
    enumValues = Arrays.stream(split)
      .filter(s -> !"null".equals(s))
      .map(s -> s.substring(1, s.length() - 1).replace("-", "_").toUpperCase())
      .collect(Collectors.toList());
  }

  void writeTo(List<String> output, String offset) {
    output.add(offset + "enum " + name + " { " + String.join(", ", enumValues) + "}");
  }
}

public class ApiGenerator {
  ApiGenerator(Reader reader) throws IOException {
    JsonObject api = new Gson().fromJson(reader, JsonObject.class);
    File cwd = FileSystems.getDefault().getPath(".").toFile();
    File dir = new File(cwd, "../lib/src/main/java/com/microsoft/playwright");
    System.out.println("Writing files to: " + dir.getCanonicalPath());
    dir.mkdirs();
    for (Map.Entry<String, JsonElement> entry: api.entrySet()) {
      String name = entry.getKey();
      List<String> lines = new ArrayList<>();
      new Interface(entry.getValue().getAsJsonObject()).writeTo(lines, "");
      String text = String.join("\n", lines);
      FileWriter writer = new FileWriter(new File(dir, name + ".java"));
      writer.write(text);
      writer.close();
    }
  }

  public static void main(String[] args) throws IOException {
    File cwd = FileSystems.getDefault().getPath(".").toFile();
    System.out.println(cwd.getCanonicalPath());
    File file = new File(cwd, "src/main/resources/api.json");
    System.out.println("Reading from: " + file.getCanonicalPath());
    new ApiGenerator(new FileReader(file));
  }
}
