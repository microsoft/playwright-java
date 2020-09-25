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
import java.util.stream.Collectors;

public class ApiGenerator {
  private List<String> output;
  private Set<String> innerTypes;

  private static Map<String, String> tsToJavaMethodName = new HashMap<>();
  static {
    tsToJavaMethodName.put("continue", "continue_");
    tsToJavaMethodName.put("$eval", "evalOnSelector");
    tsToJavaMethodName.put("$$eval", "evalOnSelectorAll");
    tsToJavaMethodName.put("$", "querySelector");
    tsToJavaMethodName.put("$$", "querySelectorAll");
    tsToJavaMethodName.put("goto", "navigate");
  }

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

  ApiGenerator(Reader reader) throws IOException {
    JsonObject api = new Gson().fromJson(reader, JsonObject.class);
    File cwd = FileSystems.getDefault().getPath(".").toFile();
    File dir = new File(cwd, "../lib/src/main/java/com/microsoft/playwright");
    System.out.println("Writing files to: " + dir.getCanonicalPath());
    dir.mkdirs();
    for (Map.Entry<String, JsonElement> entry: api.entrySet()) {
      innerTypes = new HashSet<>();
      output = new ArrayList<>();
      String name = entry.getKey();
      output.add(header);
      output.add("import java.util.*;");
      output.add("import java.util.function.BiConsumer;");
      output.add("");
      output.add("interface " + name + "{");
      generateInterface(entry.getValue().getAsJsonObject(), "  ");
      output.add("}");
      output.add("\n");

      String text = String.join("\n", output);
      FileWriter writer = new FileWriter(new File(dir, name + ".java"));
      writer.write(text);
      writer.close();
    }
  }

  private void generateInterface(JsonObject docClass, String offset) {
    JsonObject members = docClass.get("members").getAsJsonObject();
    for (Map.Entry<String, JsonElement> m : members.entrySet())
      generateMember(m.getValue().getAsJsonObject(), offset);
  }

  private void generateMember(JsonObject docMember, String offset) {
    String kind = docMember.get("kind").getAsString();
    String name = docMember.get("name").getAsString();
    if ("method".equals(kind)) {
      String type = convertReturnType(docMember.get("type"));

      StringBuilder args = new StringBuilder();
      if (docMember.get("args") != null) {
        for (Map.Entry<String, JsonElement> arg : docMember.get("args").getAsJsonObject().entrySet()) {
          String argName = arg.getKey();
          String argType = arg.getValue().getAsJsonObject().get("type").getAsJsonObject().get("name").getAsString();
          argType = convertBuiltinType(argType);
          if (argType.equals("Object")) {
            argType = generateParamClass(name, argName, arg.getValue().getAsJsonObject().get("type").getAsJsonObject(), offset);
          }

          if (argType.equals("function(Route, Request)")) {
            argType = "BiConsumer<Route, Request>";
          } else if (argType.equals("EvaluationArgument")) {
            argType = "Object";
          } else if (argType.equals("number")) {
            argType = "int";
          } else if (argType.contains("|\"")) {
            String enumName = enumName(name, argName);
            generateEnum(enumName, argType, "", offset);
            argType = enumName;
          } else if (argType.contains("|")) {
            argType = "String";
          } else if (argType.contains("function")) {
            // js functions are always passed as text in java.
            if (argName.startsWith("playwright") || argName.startsWith("page")) {
              argType = "String";
            }
          }

          if (args.length() > 0) {
            args.append(", ");
          }
          args.append(argType).append(" ").append(argName);
        }
      }

      if (tsToJavaMethodName.containsKey(name))
        name = tsToJavaMethodName.get(name);
      output.add(offset + type + " " + name + "(" + args + ");");
    }
  }

  private String generateParamClass(String methodName, String argName, JsonObject json, String offset) {
    String className = toTitle(methodName) + toTitle(argName);
    output.add("");
    output.add(offset + "class " + className + " {");
    String memberOffset = offset + "  ";
    for (Map.Entry<String, JsonElement> e : json.get("properties").getAsJsonObject().entrySet()) {
      String name = e.getKey();
      String type = e.getValue().getAsJsonObject().get("type").getAsJsonObject().get("name").getAsString();
      if ("modifiers".equals(name)) {
        if (!type.equals("Array<\"Alt\"|\"Control\"|\"Meta\"|\"Shift\">"))
          throw new RuntimeException("Unexpected type of modifiers: " + type);
        generateEnum("Modifier", "\"Alt\"|\"Control\"|\"Meta\"|\"Shift\"", className, memberOffset);
        type = "Set<Modifier>";
      }
      if ("media".equals(name)) {
        if (!type.equals("null|\"print\"|\"screen\""))
          throw new RuntimeException("Unexpected type of media: " + type);
        type = "Media";
        generateEnum(type, "\"print\"|\"screen\"", className, memberOffset);
      }
      if ("pdf".equals(methodName) && (name.equals("width") || name.equals("height"))) {
        if (!type.equals("string|number"))
          throw new RuntimeException("Unexpected type of pdf dimensions: " + type);
        type = "String";
      }
      if ("continue".equals(methodName) && name.equals("postData")) {
        if (!type.equals("string|Buffer"))
          throw new RuntimeException("Unexpected type of pdf dimensions: " + type);
        type = "String";
      }
      if ("fulfill".equals(methodName) && name.equals("body")) {
        if (!type.equals("string|Buffer"))
          throw new RuntimeException("Unexpected type of pdf dimensions: " + type);
        type = "String";
      }
      if ("waitForNavigation".equals(methodName) && argName.equals("options") && name.equals("url")) {
        if (!type.equals("string|RegExp|Function"))
          throw new RuntimeException("Unexpected type of pdf dimensions: " + type);
        type = "String";
      }
      if (type.contains("|\"") && type.endsWith("\"")) {
        type = type.replace("null|", "");
        String enumName = toTitle(name);
        generateEnum(enumName, type, className, memberOffset);
        type = enumName;
      }
      if ("ignoreDefaultArgs".equals(name)) {
        type = "Boolean";
      }
      type = convertBuiltinType(type);
      type = replacePrimitiveWithBoxedType(type);
      output.add(memberOffset + type + " " + name + ";");
    }
    output.add(offset + "}");
    return className;
  }

  private static String toTitle(String name) {
    return Character.toUpperCase(name.charAt(0)) + name.substring(1);
  }

  private static String enumName(String methodName, String argName) {
    if (methodName.startsWith("waitFor")) {
      return methodName.substring("waitFor".length());
    }
    return toTitle(argName);
  }

  private void generateEnum(String name, String values, String scope, String offset) {
    if (!innerTypes.add(scope + values)) {
      return;
    }
    String[] split = values.split("\\|");
    List<String> enumValues = Arrays.stream(split).map(s -> s.substring(1, s.length() - 1).replace("-", "_").toUpperCase()).collect(Collectors.toList());
    output.add(offset + "enum " + name + " { " + String.join(", ", enumValues) + " }");
  }

  private static String convertReturnType(JsonElement jsonType) {
    String type = jsonType.isJsonNull() ? "void" : jsonType.getAsJsonObject().get("name").getAsString();
    if ("Promise".equals(type)) {
      type = "void";
    }
    // Java API is sync just strip Promise<>
    if (type.startsWith("Promise<")) {
      type = type.substring("Promise<".length(), type.length() - 1);
    }
    return convertBuiltinType(type);
  }

  private static String replacePrimitiveWithBoxedType(String type) {
    return type.replace("int", "Integer")
      .replace("boolean", "Boolean")
      .replace("double", "Double");
  }

  private static String convertBuiltinType(String type) {
    return type.replace("string|number|boolean", "String")
      .replace("Array<", "List<")
      .replace("string", "String")
      .replace("number", "int")
      .replace("Serializable", "Object")
      .replace("Buffer", "byte[]")
      .replace("ChildProcess", "Object")
      .replace("Object<", "Map<")
      .replace("null|", "");
  }

  public static void main(String[] args) throws IOException {
    File cwd = FileSystems.getDefault().getPath(".").toFile();
    System.out.println(cwd.getCanonicalPath());
    File file = new File(cwd, "src/main/resources/api.json");
    System.out.println("Reading from: " + file.getCanonicalPath());
    new ApiGenerator(new FileReader(file));
  }

}
