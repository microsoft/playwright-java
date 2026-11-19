#!/usr/bin/env node
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

// Generates the public Java API (the interfaces, the options classes and the enums) from the
// upstream API docs, docs/src/api/*.md. The docs and the modules that parse them stay in
// microsoft/playwright: the script reads both from the same checkout.
//
// Node.js runs this file as is, it needs no build step and no dependencies.
//
// Usage: node scripts/generate_java_api.mts <upstream-checkout-dir>

import fs from 'node:fs';
import { createRequire } from 'node:module';
import path from 'node:path';

// The subset of the upstream model (utils/doclint/documentation.js) that the generator reads.

type MarkdownNode = {
  type: string,
  text?: string,
  liType?: string,
  codeLang?: string,
  lines?: string[],
  children?: MarkdownNode[],
};

type Langs = {
  only?: string[],
  aliases?: Record<string, string>,
  types?: Record<string, DocType>,
};

type DocType = {
  name: string,
  union?: DocType[],
  templates?: DocType[],
  args?: DocType[],
  returnType?: DocType,
  properties?: DocMember[],
  langAliases?: Record<string, string>,
};

type DocMember = {
  kind: 'method' | 'property' | 'event',
  name: string,
  // The name in the docs, when `name` is the Java alias.
  originalName?: string,
  type: DocType | null,
  argsArray: DocMember[],
  required: boolean,
  langs: Langs,
  spec?: MarkdownNode[],
  since?: string,
  deprecated?: string,
};

type DocClass = {
  name: string,
  extends: string | null,
  membersArray: DocMember[],
  langs: Langs,
  spec?: MarkdownNode[],
};

type LinkRenderer = (item: { clazz?: DocClass, param?: { alias: string }, option?: { alias: string } }) => string | undefined;

type Documentation = {
  classesArray: DocClass[],
  setLinkRenderer(renderer: LinkRenderer): void,
};

const LANG = 'java';
const ROOT_DIR = path.join(import.meta.dirname, '..');
const API_DIR = path.join(ROOT_DIR, 'playwright', 'src', 'main', 'java', 'com', 'microsoft', 'playwright');
const API_PACKAGE = 'com.microsoft.playwright';

const LICENSE_HEADER = `/*
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
`;

// These classes are written by hand.
const HANDWRITTEN_CLASSES = ['PlaywrightException', 'TimeoutError'];
// These interfaces go to the options package.
const OPTIONS_PACKAGE_INTERFACES = ['RequestOptions', 'FormData'];
const BASE_INTERFACES = ['Browser', 'JSHandle', 'BrowserContext'];
const AUTO_CLOSEABLE_INTERFACES = ['Playwright', 'Browser', 'BrowserContext', 'Page'];
// The interfaces with a static create() method that calls the implementation class.
const FACTORY_INTERFACES = ['Playwright', 'FormData', 'RequestOptions'];
const BROWSER_CHANNEL_OPTIONS = ['BrowserType.launch.options.channel', 'BrowserType.launchPersistentContext.options.channel'];

// The imports of a generated interface, in the order of the output. When a roll brings in an
// interface that uses one of these types, add the interface to the list.
const INTERFACE_IMPORTS: [imports: string[], interfaces: string[]][] = [
  [['com.microsoft.playwright.options.*'], ['Page', 'Request', 'Response', 'APIRequestContext', 'APIRequest', 'APIResponse', 'FileChooser', 'Frame', 'FrameLocator', 'ElementHandle', 'Locator', 'Browser', 'BrowserContext', 'BrowserType', 'Mouse', 'Keyboard', 'Tracing', 'Video', 'Debugger', 'Screencast', 'WebError', 'Credentials', 'WebStorage', 'WebMCP']],
  [['java.io.InputStream'], ['Download']],
  [['java.nio.file.Path'], ['Page', 'Frame', 'ElementHandle', 'Locator', 'FormData', 'APIRequest', 'APIRequestContext', 'FileChooser', 'Browser', 'BrowserContext', 'BrowserType', 'Download', 'Route', 'Selectors', 'Tracing', 'Video', 'Screencast']],
  [['java.util.Date'], ['Clock']],
  [['java.util.*'], ['Page', 'Frame', 'ElementHandle', 'Locator', 'LocatorAssertions', 'APIRequest', 'APIRequestContext', 'Browser', 'BrowserContext', 'BrowserType', 'Route', 'Request', 'Response', 'JSHandle', 'ConsoleMessage', 'APIResponse', 'Playwright', 'Debugger', 'Screencast', 'WebSocketRoute', 'Credentials', 'WebStorage', 'WebMCP']],
  [['java.util.function.BiConsumer'], ['WebSocketRoute']],
  [['java.util.function.Consumer'], ['Page', 'Browser', 'BrowserContext', 'WebSocket', 'Worker', 'CDPSession', 'WebSocketRoute', 'Screencast']],
  [['java.util.function.BooleanSupplier'], ['Page', 'BrowserContext']],
  [['java.util.function.Predicate'], ['Page', 'Frame', 'BrowserContext', 'WebSocket', 'Worker']],
  [['java.util.regex.Pattern'], ['Page', 'Frame', 'FrameLocator', 'Locator', 'APIRequestContext', 'Browser', 'BrowserType', 'BrowserContext', 'PageAssertions', 'LocatorAssertions', 'Tracing']],
  [['com.google.gson.JsonObject'], ['CDPSession']],
  [['com.microsoft.playwright.options.AriaRole', 'com.microsoft.playwright.options.PseudoElement'], ['LocatorAssertions']],
  [[
    'com.microsoft.playwright.APIResponse',
    'com.microsoft.playwright.Locator',
    'com.microsoft.playwright.Page',
    'com.microsoft.playwright.impl.APIResponseAssertionsImpl',
    'com.microsoft.playwright.impl.AssertionsTimeout',
    'com.microsoft.playwright.impl.LocatorAssertionsImpl',
    'com.microsoft.playwright.impl.PageAssertionsImpl',
  ], ['PlaywrightAssertions']],
];
const CUSTOM_CLASS_IMPORTS: Record<string, string> = {
  ClientCertificate: 'java.nio.file.Path',
  DropPayload: 'java.util.Map',
};

// The static methods with a body. The key is the path of the method. A method with overloads
// has an entry for the name of each overload in the docs.
const STATIC_METHODS: Record<string, Record<string, string[]>> = {
  'FormData.create': {
    create: [
      'static FormData create() {',
      '  return new FormDataImpl();',
      '}',
    ],
  },
  'RequestOptions.create': {
    create: [
      'static RequestOptions create() {',
      '  return new RequestOptionsImpl();',
      '}',
    ],
  },
  'PlaywrightAssertions.assertThat': {
    expectPage: [
      'static PageAssertions assertThat(Page page) {',
      '  return new PageAssertionsImpl(page);',
      '}',
      '',
    ],
    expectLocator: [
      'static LocatorAssertions assertThat(Locator locator) {',
      '  return new LocatorAssertionsImpl(locator);',
      '}',
      '',
    ],
    expectAPIResponse: [
      'static APIResponseAssertions assertThat(APIResponse response) {',
      '  return new APIResponseAssertionsImpl(response);',
      '}',
      '',
    ],
  },
  'PlaywrightAssertions.setDefaultAssertionTimeout': {
    setDefaultAssertionTimeout: [
      'static void setDefaultAssertionTimeout(double timeout) {',
      '  AssertionsTimeout.setDefaultTimeout(timeout);',
      '}',
      '',
    ],
  },
};

const BUILTIN_TYPES: Record<string, string> = {
  'int': 'int',
  'float': 'double',
  'string': 'String',
  'void': 'void',
  'path': 'Path',
  'EvaluationArgument': 'Object',
  'Serializable': 'Object',
  'any': 'Object',
  'Readable': 'InputStream',
  'Buffer': 'byte[]',
  'Disposable': 'AutoCloseable',
  'URL': 'String',
  'RegExp': 'Pattern',
};

const BOXED_TYPES: Record<string, string> = {
  'int': 'Integer',
  'double': 'Double',
  'boolean': 'Boolean',
};

// `Class.member` in the docs => `Class.alias`, for the members with a Java alias.
const memberAliases = new Map<string, string>();

// ---------------------------------------------------------------------------------------------
// Filtering the docs for Java.

function isSupported(item: { langs?: Langs }): boolean {
  return !item.langs?.only || item.langs.only.includes(LANG);
}

// Renames the member to its Java alias and replaces its type with the Java one. Removes
// everything that is for other languages, at any depth.
function filterMember(member: DocMember) {
  const alias = member.langs?.aliases?.[LANG];
  if (alias) {
    member.originalName = member.name;
    member.name = alias;
  }
  const type = member.langs?.types?.[LANG];
  if (type)
    member.type = type;
  if (member.type)
    filterType(member.type);
  member.argsArray = member.argsArray.filter(isSupported);
  member.argsArray.forEach(filterMember);
}

function filterType(type: DocType) {
  if (type.properties) {
    type.properties = type.properties.filter(isSupported);
    type.properties.forEach(filterMember);
  }
  for (const nested of [...type.union || [], ...type.templates || [], ...type.args || []])
    filterType(nested);
  if (type.returnType)
    filterType(type.returnType);
}

function filterForJava(documentation: Documentation): DocClass[] {
  const classes = documentation.classesArray.filter(isSupported);
  for (const clazz of classes) {
    const className = clazz.name;
    const alias = clazz.langs?.aliases?.[LANG];
    if (alias)
      clazz.name = alias;
    clazz.membersArray = clazz.membersArray.filter(isSupported);
    for (const member of clazz.membersArray) {
      const memberName = member.name;
      filterMember(member);
      if (member.name !== memberName)
        memberAliases.set(`${className}.${memberName}`, `${className}.${member.name}`);
    }
  }
  return classes;
}

// ---------------------------------------------------------------------------------------------
// Javadoc.

function toTitle(name: string): string {
  return name[0].toUpperCase() + name.substring(1);
}

function isAssertionClass(className: string): boolean {
  return className.toLowerCase().includes('assert');
}

// Splits as String.split() of Java does: without the trailing empty strings.
function splitDroppingTrailingEmpty(text: string, separator: string): string[] {
  const parts = text.split(separator);
  if (parts.length === 1)
    return parts;
  while (parts.length && !parts[parts.length - 1])
    parts.pop();
  return parts;
}

function writeJavadoc(output: string[], offset: string, text: string) {
  if (!text)
    return;
  output.push(offset + '/**');
  for (const line of splitDroppingTrailingEmpty(text, '\n'))
    output.push((offset + ' *' + (line ? ' ' : '') + line).replaceAll('*/', '*\\/'));
  output.push(offset + ' */');
}

function linkifyMemberRefs(paragraph: string): string {
  return paragraph.replace(/\[`(event|property|method): ([^`]+)`]/g, (match, kind: string, ref: string) => {
    let name = memberAliases.get(ref) ?? ref;
    if (kind === 'event') {
      const parts = name.split('.');
      name = parts[0] + '.on' + toTitle(parts[1]);
    }
    const packagePrefix = isAssertionClass(name.split('.')[0]) ? `${API_PACKAGE}.assertions.` : `${API_PACKAGE}.`;
    return `{@link ${packagePrefix}${name.replaceAll('.', '#')} ${name}()}`;
  });
}

function updateExternalLinks(paragraph: string): string {
  return paragraph.replace(/\[([^\]]+)\]\(([^\)]+)\)/g, (match, text: string, url: string) => {
    // ../actionability.md#editable => https://playwright.dev/java/docs/actionability#editable
    if (url.startsWith('../'))
      url = url.replaceAll('.md', '').replaceAll('../', 'https://playwright.dev/java/docs/');
    // ./class-tracing.md => https://playwright.dev/java/docs/api/class-tracing
    if (url.startsWith('./'))
      url = url.replaceAll('.md', '').replaceAll('./', 'https://playwright.dev/java/docs/api/');
    return `<a href="${url}">${text}</a>`;
  }).replaceAll('[Promise]', `<a href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>`);
}

// Splits the text into words. A link in square brackets is not split.
function tokenizeNoBreakLinks(text: string): string[] {
  const links: string[] = [];
  const sanitized = text.replace(/\[[^\]]+\]/g, link => {
    links.push(link);
    return `[${links.length - 1}]`;
  });
  let tokens = splitDroppingTrailingEmpty(sanitized, ' ');
  links.forEach((link, index) => {
    tokens = tokens.map(token => token.replaceAll(`[${index}]`, () => link));
  });
  return tokens;
}

function wrapText(text: string, maxColumns: number): string {
  // A table row.
  if (/^[\u0000- ]*\|/.test(text))
    return text;
  const lines: string[] = [];
  let line = '';
  for (const word of tokenizeNoBreakLinks(text)) {
    if (line && line.length + word.length < maxColumns) {
      line += ' ' + word;
    } else {
      if (line)
        lines.push(line);
      line = word;
    }
  }
  if (line)
    lines.push(line);
  return lines.join('\n');
}

function beautify(paragraph: string): string {
  const text = updateExternalLinks(linkifyMemberRefs(paragraph))
      .replaceAll('↵', ' ')
      .replace(/`'([^`]+)'`/g, '{@code "$1"}')
      .replace(/`([^`]+)`/g, '{@code $1}');
  return wrapText(text, 120);
}

function formatSpec(spec: MarkdownNode[]): string {
  const out: string[] = [];
  let currentList: string | null = null;
  const closeList = () => {
    if (currentList)
      out.push(`</${currentList}>`);
    currentList = null;
  };
  for (const node of spec) {
    if (node.type === 'li') {
      if (node.text!.startsWith('extends: '))
        continue;
      if (!currentList) {
        currentList = node.liType === 'ordinal' ? 'ol' : 'ul';
        out.push(`<${currentList}>`);
      }
      out.push(`<li> ${beautify(node.text!)}</li>`);
      continue;
    }
    closeList();
    const paragraphPrefix = out.length ? '\n<p> ' : '';
    if (node.type === 'code') {
      // This is true for javascript too: the code of all languages that have 'java' in the name is in the comment.
      if (!node.codeLang?.includes('java'))
        continue;
      out.push('<pre>{@code', ...node.lines!, '}</pre>');
    } else if (node.type === 'note') {
      const text = node.children!.filter(child => child.type === 'text').map(child => beautify(child.text!)).join('');
      out.push(`${paragraphPrefix}<strong>NOTE:</strong> ${text}`);
    } else {
      // **Usage**, **Details** and other titles are bold text.
      const title = node.text!.match(/^\*\*(.+)\*\*$/);
      const paragraph = title ? `<strong>${title[1]}</strong>` : beautify(node.text!);
      out.push(paragraphPrefix + paragraph);
    }
  }
  closeList();
  return out.join('\n');
}

function memberComment(member: DocMember): string {
  if (!member.spec)
    return '';
  if (member.deprecated !== undefined)
    return '@deprecated ' + beautify(member.deprecated);
  return formatSpec(member.spec);
}

// ---------------------------------------------------------------------------------------------
// The model of the generated code.

abstract class Element {
  readonly parent: Element | null;
  readonly name: string;
  // The names from the interface to this element, for example `BrowserType.launch.options.channel`.
  readonly path: string;

  constructor(parent: Element | null, name: string, path?: string) {
    this.parent = parent;
    this.name = name;
    this.path = path ?? (parent ? `${parent.path}.${name}` : name);
  }

  typeScope(): TypeDefinition {
    return this.parent!.typeScope();
  }

  // All types that go to files of their own, by name. The first definition of a type wins.
  topLevelTypes(): Map<string, TypeDefinition> {
    return this.parent!.topLevelTypes();
  }
}

function nullable(javaType: string): string {
  if (javaType.endsWith('[]'))
    return javaType.substring(0, javaType.length - 2) + ' @Nullable []';
  return '@Nullable ' + javaType;
}

// The Java alias of the type in the docs (`alias-java: Cookie`), or the alias for all languages.
function javaAlias(type: DocType): string | undefined {
  return type.langAliases?.[LANG] ?? type.langAliases?.default;
}

// The return type of a method, the type of a method param or the type of a field.
class TypeRef extends Element {
  readonly type: DocType | null;
  // The name of the class generated for the `Object` in this type.
  customType: string | null = null;

  constructor(parent: Element, type: DocType | null) {
    super(parent, type?.name ?? '', parent.path);
    this.type = type;
    if (type)
      this.createClassesAndEnums(type);
  }

  private createClassesAndEnums(type: DocType) {
    if (type.union) {
      if (type.name)
        this.typeScope().createEnum(type);
      else
        type.union.forEach(item => this.createClassesAndEnums(item));
      return;
    }
    if (type.templates) {
      type.templates.forEach(item => this.createClassesAndEnums(item));
      return;
    }
    if (type.name === 'function' && type.args) {
      for (const arg of type.args) {
        const alias = javaAlias(arg);
        if (arg.name === 'Object' && alias)
          this.typeScope().createTopLevelInterface(alias, this, arg);
      }
      return;
    }
    if (type.name !== 'Object')
      return;
    // The same type may be `Object` in several union values, for example Object|Array<Object>.
    if (this.customType)
      return;
    if (this.parent instanceof Param && this.parent.name === 'options') {
      this.customType = toTitle(this.parent.parent!.name) + toTitle(this.parent.name);
      this.typeScope().createNestedClass(this.customType, this, type);
    } else {
      this.customType = javaAlias(type) ?? toTitle(this.parent!.name);
      this.typeScope().createTopLevelClass(this.customType, this, type);
    }
  }

  toJava(): string {
    if (!this.type)
      return 'void';
    return this.convertType(this.stripNullable());
  }

  toJavaWithNullability(): string {
    return this.withNullability(this.toJava(), false);
  }

  // Adds @Nullable when the type admits null, or when it is the type of an optional parameter or field.
  withNullability(javaType: string, optional: boolean): string {
    // Serializable is Object in Java, and the JSON null is a value of it.
    const isNullable = this.isNullable() || this.stripNullable()?.name === 'Serializable';
    return optional || isNullable ? nullable(javaType) : javaType;
  }

  isCustomClass(): boolean {
    return this.stripNullable()?.name === 'Object' && !this.type!.templates;
  }

  // A union of types, that is not an enum and not `null|T`.
  isTypeUnion(): boolean {
    return !!this.type?.union && !this.type.name && !this.isNullable();
  }

  private supportedUnionTypes(): DocType[] {
    return this.type!.union!.filter(type => type.name !== 'null' && (type.name !== 'function' || type.args));
  }

  unionSize(): number {
    return this.supportedUnionTypes().length;
  }

  // True for Object|Array<Object>, where both are the same custom class.
  isCustomClassOrListUnion(): boolean {
    if (!this.customType || !this.isTypeUnion())
      return false;
    const types = this.supportedUnionTypes();
    const isObjectList = (type: DocType) => type.name === 'Array' && type.templates?.length === 1 && type.templates[0].name === 'Object';
    return types.length === 2 && types.some(type => type.name === 'Object') && types.some(isObjectList);
  }

  formatTypeFromUnion(index: number): string {
    return this.convertType(this.supportedUnionTypes()[index]);
  }

  // True for `null|T`.
  isNullable(): boolean {
    const type = this.type;
    return !!type?.union && !type.name && type.union.length === 2 && type.union.some(item => item.name === 'null');
  }

  private stripNullable(): DocType {
    if (!this.isNullable())
      return this.type!;
    return this.type!.union!.find(item => item.name !== 'null')!;
  }

  private convertType(type: DocType): string {
    const name = type.name;
    if (type.union) {
      if (name)
        return name;
      if (this.parent instanceof Field)
        return 'Object';
      throw new Error(`Unexpected union without name in ${this.path}`);
    }
    if (Object.hasOwn(BUILTIN_TYPES, name))
      return BUILTIN_TYPES[name];
    if (name === 'Array') {
      const elementType = this.convertTemplateParams(type);
      // After the type erasure all lists are the same type, so the overloads take arrays.
      if (this.parent instanceof Param && this.isTypeUnion() && this.supportedUnionTypes().filter(item => item.name === 'Array').length > 1)
        return elementType + '[]';
      return `List<${elementType}>`;
    }
    if (name === 'Object') {
      // An `Object` in an argument of a function has no class of this type reference.
      const className = this.customType ?? javaAlias(type);
      if (className)
        return className;
      return `Map<${this.convertTemplateParams(type)}>`;
    }
    if (name === 'Map')
      return `Map<${this.convertTemplateParams(type)}>`;
    if (name === 'Promise')
      return this.convertTemplateParams(type);
    if (name === 'function') {
      const alias = javaAlias(type);
      if (alias)
        return alias;
      if (!type.args)
        throw new Error(`Missing mapping for ${this.path}`);
      if (this.path === 'WebSocketRoute.onClose.handler')
        return 'BiConsumer<Integer, String>';
      if (type.args.length === 1) {
        const paramType = this.convertType(type.args[0]);
        const returnTypeName = type.returnType?.name;
        if (returnTypeName === 'boolean')
          return `Predicate<${paramType}>`;
        if (!returnTypeName || returnTypeName === 'Promise' || returnTypeName === 'void')
          return `Consumer<${paramType}>`;
        throw new Error(`Missing mapping for the return type ${returnTypeName} in ${this.path}`);
      }
    }
    return name;
  }

  private convertTemplateParams(type: DocType): string {
    return (type.templates || []).map(item => this.convertType(item)).join(', ');
  }
}

abstract class TypeDefinition extends Element {
  // The options classes of the methods.
  readonly nestedClasses: CustomClass[] = [];

  override typeScope(): TypeDefinition {
    return this;
  }

  abstract writeTo(output: string[], offset: string): void;

  // Constructs the type in any case: it registers the types of its fields.
  private createTopLevelType<T extends TypeDefinition>(name: string, type: T, kind: Function) {
    const types = this.topLevelTypes();
    const existing = types.get(name);
    if (!existing)
      types.set(name, type);
    else if (!(existing instanceof kind))
      throw new Error(`Two types with the name ${name} are of different kinds, the second one is ${type.path}`);
    return existing as T | undefined;
  }

  createEnum(type: DocType) {
    const newEnum = new Enum(this, type);
    const existing = this.createTopLevelType(newEnum.name, newEnum, Enum);
    if (existing && existing.values.join() !== newEnum.values.join())
      throw new Error(`Two enums with the name ${newEnum.name} have different values: ${existing.values} and ${newEnum.values}`);
  }

  createTopLevelClass(name: string, parent: TypeRef, type: DocType) {
    this.createTopLevelType(name, new CustomClass(parent, name, type), CustomClass);
  }

  createTopLevelInterface(name: string, parent: TypeRef, type: DocType) {
    this.createTopLevelType(name, new CustomInterface(parent, name, type), CustomInterface);
  }

  createNestedClass(name: string, parent: TypeRef, type: DocType) {
    if (!this.nestedClasses.some(c => c.name === name))
      this.nestedClasses.push(new CustomClass(parent, name, type));
  }
}

class Event extends Element {
  private readonly member: DocMember;
  private readonly type: TypeRef;

  constructor(parent: Interface, member: DocMember) {
    super(parent, member.name);
    this.member = member;
    this.type = new TypeRef(this, member.type);
  }

  writeListenerMethods(output: string[], offset: string) {
    writeJavadoc(output, offset, memberComment(this.member));
    const name = toTitle(this.name);
    const paramType = this.type.toJavaWithNullability();
    const listenerType = paramType === 'void' ? 'Runnable' : `Consumer<${paramType}>`;
    output.push(`${offset}void on${name}(${listenerType} handler);`);
    writeJavadoc(output, offset, `Removes handler that was previously added with {@link #on${name} on${name}(handler)}.`);
    output.push(`${offset}void off${name}(${listenerType} handler);`);
  }
}

class Param extends Element {
  readonly member: DocMember;
  readonly type: TypeRef;

  constructor(method: Method, member: DocMember) {
    super(method, member.name);
    this.member = member;
    this.type = new TypeRef(this, member.type);
  }

  isOptional(): boolean {
    return !this.member.required;
  }

  // The declaration of the param. `overloadIndex` picks the type from the union, when the type is a union.
  toJava(overloadIndex: number): string {
    const javaType = this.type.isTypeUnion() ? this.type.formatTypeFromUnion(overloadIndex) : this.type.toJava();
    return `${this.type.withNullability(javaType, this.isOptional())} ${this.name}`;
  }
}

// A method or a property of an interface: all properties are methods in Java.
class Method extends Element {
  private readonly member: DocMember;
  private readonly returnType: TypeRef;
  private readonly params: Param[];

  constructor(parent: Interface, member: DocMember) {
    super(parent, member.name);
    this.member = member;
    this.returnType = new TypeRef(this, member.type);
    this.params = member.argsArray
        .filter(arg => arg.name !== 'options' || arg.type?.properties?.length)
        .map(arg => new Param(this, arg));
  }

  writeTo(output: string[], offset: string) {
    if (this.path === 'Playwright.create') {
      this.writeJavadoc(this.params, output, offset);
      output.push(
          `${offset}static Playwright create(${this.params[0].toJava(0)}) {`,
          `${offset}  return PlaywrightImpl.create(options);`,
          `${offset}}`,
          '',
          `${offset}static Playwright create() {`,
          `${offset}  return create(null);`,
          `${offset}}`);
      return;
    }
    const staticMethod = STATIC_METHODS[this.path];
    if (staticMethod) {
      const originalName = this.member.originalName ?? this.name;
      const body = staticMethod[originalName];
      if (!body)
        throw new Error(`Unexpected overload of ${this.path}: ${originalName}`);
      this.writeJavadoc(this.params, output, offset);
      output.push(...body.map(line => line && offset + line));
      return;
    }
    // Each type of the first param with a union type makes an overload.
    const unionParam = this.params.find(p => p.type.isTypeUnion());
    const numOverloads = unionParam ? unionParam.type.unionSize() : 1;
    for (let i = 0; i < numOverloads; i++)
      this.writeOverloadedMethods(i, output, offset);
  }

  private writeOverloadedMethods(overloadIndex: number, output: string[], offset: string) {
    for (let i = this.params.length - 1; i >= 0; i--) {
      const param = this.params[i];
      if (!param.isOptional())
        continue;
      // An optional param with a union type has one overload without the param.
      if (param.type.isTypeUnion() && overloadIndex !== 0)
        continue;
      this.writeDefaultOverloadedMethod(overloadIndex, i, output, offset);
    }
    const params = this.params.map(p => p.toJava(overloadIndex)).join(', ');
    this.writeJavadoc(this.params, output, offset);
    output.push(`${offset}${this.returnType.toJavaWithNullability()} ${this.name}(${params});`);
  }

  // Writes the default method without the optional params that start at `firstNullOptional`.
  private writeDefaultOverloadedMethod(overloadIndex: number, firstNullOptional: number, output: string[], offset: string) {
    const params: Param[] = [];
    const args: string[] = [];
    this.params.forEach((param, i) => {
      if (i === firstNullOptional) {
        if (param.type.isTypeUnion()) {
          const type = param.type.formatTypeFromUnion(overloadIndex);
          args.push(type === 'int' ? '0' : `(${type}) null`);
        } else {
          // TODO: it should probably be done for all methods that have name#1, name#2 etc overloads.
          args.push(this.path === 'LocatorAssertions.hasAttribute' ? `(${param.type.toJava()}) null` : 'null');
        }
        return;
      }
      if (param.isOptional() && i > firstNullOptional)
        return;
      params.push(param);
      args.push(param.name);
    });
    const returns = this.returnType.toJava() === 'void' ? '' : 'return ';
    this.writeJavadoc(params, output, offset);
    output.push(
        `${offset}default ${this.returnType.toJavaWithNullability()} ${this.name}(${params.map(p => p.toJava(overloadIndex)).join(', ')}) {`,
        `${offset}  ${returns}${this.name}(${args.join(', ')});`,
        `${offset}}`);
  }

  private writeJavadoc(params: Param[], output: string[], offset: string) {
    const tags: string[] = [];
    for (const param of params) {
      const comment = memberComment(param.member);
      if (comment)
        tags.push(`@param ${param.name} ${comment}`);
    }
    if (this.member.since !== undefined)
      tags.push(`@since ${this.member.since}`);
    const sections = [memberComment(this.member)];
    if (tags.length)
      sections.push('', ...tags);
    writeJavadoc(output, offset, sections.join('\n'));
  }
}

class Field extends Element {
  private readonly member: DocMember;
  readonly type: TypeRef;

  constructor(parent: TypeDefinition, member: DocMember) {
    super(parent, member.name);
    this.member = member;
    this.type = new TypeRef(this, member.type);
  }

  isRequired(): boolean {
    return !!this.member.required;
  }

  comment(): string {
    return memberComment(this.member);
  }

  writeTo(output: string[], offset: string) {
    writeJavadoc(output, offset, this.comment());
    let type = this.type.toJava();
    if (this.type.isNullable())
      type = `Optional<${type}>`;
    if (this.isBrowserChannelOption())
      type = 'Object';
    // An optional field has a boxed type: it can be null.
    if (!this.isRequired())
      type = nullable(BOXED_TYPES[type] ?? type);
    output.push(`${offset}public ${type} ${this.name};`);
  }

  writeBuilderMethod(output: string[], offset: string, parentClass: string) {
    if (this.type.isTypeUnion() && (!this.type.customType || this.type.isCustomClassOrListUnion())) {
      // A union of a custom class and its list, for example Object|Array<Object> of httpCredentials,
      // has the setter with the required fields too.
      if (this.type.customType)
        this.writeRequiredFieldsBuilderMethod(output, offset, parentClass);
      for (let i = 0; i < this.type.unionSize(); i++)
        this.writeGenericBuilderMethod(output, offset, parentClass, this.type.formatTypeFromUnion(i));
      return;
    }
    if (this.type.isCustomClass())
      this.writeRequiredFieldsBuilderMethod(output, offset, parentClass);
    if (this.isBrowserChannelOption()) {
      output.push(`${offset}@Deprecated`);
      this.writeGenericBuilderMethod(output, offset, parentClass, 'BrowserChannel');
    }
    this.writeGenericBuilderMethod(output, offset, parentClass, this.type.toJava());
  }

  // The setter that takes the required fields of the custom class, for example setHttpCredentials(username, password).
  private writeRequiredFieldsBuilderMethod(output: string[], offset: string, parentClass: string) {
    const customType = this.topLevelTypes().get(this.type.customType!);
    if (!(customType instanceof CustomClass))
      return;
    const requiredFields = customType.fields.filter(f => f.isRequired());
    if (!requiredFields.length)
      return;
    const params = requiredFields.map(f => `${f.type.toJavaWithNullability()} ${f.name}`).join(', ');
    const args = requiredFields.map(f => f.name).join(', ');
    writeJavadoc(output, offset, this.comment());
    output.push(
        `${offset}public ${parentClass} set${toTitle(this.name)}(${params}) {`,
        `${offset}  return set${toTitle(this.name)}(new ${this.type.customType}(${args}));`,
        `${offset}}`);
  }

  private writeGenericBuilderMethod(output: string[], offset: string, parentClass: string, paramType: string) {
    writeJavadoc(output, offset, this.comment());
    const value = this.type.isNullable() ? `Optional.ofNullable(${this.name})` : this.name;
    output.push(
        `${offset}public ${parentClass} set${toTitle(this.name)}(${this.type.withNullability(paramType, false)} ${this.name}) {`,
        `${offset}  this.${this.name} = ${value};`,
        `${offset}  return this;`,
        `${offset}}`);
  }

  private isBrowserChannelOption(): boolean {
    return BROWSER_CHANNEL_OPTIONS.includes(this.path);
  }
}

// An interface of the API, made from a class of the docs.
class Interface extends TypeDefinition {
  private readonly clazz: DocClass;
  private readonly types: Map<string, TypeDefinition>;
  private readonly methods: Method[] = [];
  private readonly events: Event[] = [];

  constructor(clazz: DocClass, topLevelTypes: Map<string, TypeDefinition>) {
    super(null, clazz.name);
    this.clazz = clazz;
    this.types = topLevelTypes;
    for (const member of clazz.membersArray) {
      if (member.kind === 'event')
        this.events.push(new Event(this, member));
      else
        this.methods.push(new Method(this, member));
    }
  }

  override topLevelTypes(): Map<string, TypeDefinition> {
    return this.types;
  }

  writeTo(output: string[], offset: string) {
    if (FACTORY_INTERFACES.includes(this.name) && this.methods.some(m => m.name === 'create'))
      output.push(`import ${API_PACKAGE}.impl.${this.name}Impl;`);
    for (const [imports, interfaces] of INTERFACE_IMPORTS) {
      if (interfaces.includes(this.name))
        output.push(...imports.map(name => `import ${name};`));
    }
    output.push('');

    const superInterfaces: string[] = [];
    if (this.clazz.extends && BASE_INTERFACES.includes(this.clazz.extends))
      superInterfaces.push(this.clazz.extends);
    if (AUTO_CLOSEABLE_INTERFACES.includes(this.name))
      superInterfaces.push('AutoCloseable');
    const extendsClause = superInterfaces.length ? ` extends ${superInterfaces.join(', ')}` : '';

    writeJavadoc(output, offset, this.clazz.spec ? formatSpec(this.clazz.spec) : '');
    output.push(`public interface ${this.name}${extendsClause} {`);
    const bodyOffset = '  ';
    for (const event of this.events) {
      output.push('');
      event.writeListenerMethods(output, bodyOffset);
    }
    if (this.events.length)
      output.push('');
    for (const nestedClass of this.nestedClasses)
      nestedClass.writeTo(output, bodyOffset);
    for (const method of this.methods)
      method.writeTo(output, bodyOffset);
    output.push('}', '\n');
  }
}

// A class with fields and setters, made from an `Object` type with properties. It is the options
// of a method, nested in the interface, or a type in a file of its own.
class CustomClass extends TypeDefinition {
  readonly fields: Field[];

  constructor(parent: TypeRef, name: string, type: DocType) {
    super(parent, name, parent.path);
    this.fields = (type.properties || []).map(property => new Field(this, property));
  }

  writeTo(output: string[], offset: string) {
    if (CUSTOM_CLASS_IMPORTS[this.name])
      output.push(`import ${CUSTOM_CLASS_IMPORTS[this.name]};`, '');
    const access = this.topLevelTypes().has(this.name) ? 'public ' : '';
    output.push(`${offset}${access}class ${this.name} {`);
    const bodyOffset = offset + '  ';
    for (const field of this.fields)
      field.writeTo(output, bodyOffset);
    output.push('');
    // The return value of a method is not created by the user: it has no constructor and no setters.
    const isReturnType = this.parent!.parent instanceof Method;
    if (!isReturnType) {
      this.writeConstructor(output, bodyOffset);
      for (const field of this.fields) {
        if (!field.isRequired())
          field.writeBuilderMethod(output, bodyOffset, this.name);
      }
    }
    output.push(`${offset}}`);
  }

  private writeConstructor(output: string[], offset: string) {
    const requiredFields = this.fields.filter(f => f.isRequired());
    if (!requiredFields.length)
      return;
    const params = requiredFields.map(f => `${f.type.toJavaWithNullability()} ${f.name}`).join(', ');
    output.push(
        `${offset}public ${this.name}(${params}) {`,
        ...requiredFields.map(f => `${offset}  this.${f.name} = ${f.name};`),
        `${offset}}`);
  }
}

// An interface with getters, made from an `Object` argument of a callback.
class CustomInterface extends TypeDefinition {
  private readonly fields: Field[];

  constructor(parent: TypeRef, name: string, type: DocType) {
    super(parent, name, parent.path);
    this.fields = (type.properties || []).map(property => new Field(this, property));
  }

  writeTo(output: string[], offset: string) {
    output.push(`${offset}public interface ${this.name} {`);
    const bodyOffset = offset + '  ';
    this.fields.forEach((field, index) => {
      if (index)
        output.push('');
      writeJavadoc(output, bodyOffset, field.comment());
      output.push(`${bodyOffset}${field.type.withNullability(field.type.toJava(), !field.isRequired())} ${field.name}();`);
    });
    output.push(`${offset}}`);
  }
}

class Enum extends TypeDefinition {
  readonly values: string[];

  constructor(parent: TypeDefinition, type: DocType) {
    super(parent, type.name);
    // The values are in quotes: "load", "domcontentloaded".
    this.values = type.union!.map(item => item.name.substring(1, item.name.length - 1).replaceAll('-', '_').toUpperCase());
    // Firefox stable 'channel' was removed in 1.12.0
    if (this.name === 'BrowserChannel')
      this.values.push('@Deprecated FIREFOX_STABLE');
  }

  writeTo(output: string[], offset: string) {
    output.push(`public enum ${this.name} {\n  ${this.values.join(',\n  ')}\n}`);
  }
}

// ---------------------------------------------------------------------------------------------
// Writing the files.

function writeFile(dir: string, packageName: string, type: TypeDefinition) {
  const body: string[] = [];
  type.writeTo(body, '');
  const lines = [LICENSE_HEADER, `package ${packageName};`, ''];
  if (body.some(line => line.includes('@Nullable'))) {
    lines.push('import org.jspecify.annotations.Nullable;');
    if (body[0] && !body[0].startsWith('import '))
      lines.push('');
  }
  lines.push(...body);
  fs.writeFileSync(path.join(dir, `${type.name}.java`), lines.join('\n'));
}

function generateInterfaces(classes: DocClass[], dir: string, packageName: string, topLevelTypes: Map<string, TypeDefinition>) {
  for (const clazz of classes) {
    const iface = new Interface(clazz, topLevelTypes);
    // The file is written before the next interface is read: a nested class is `public` when
    // a type with its name is among the top-level types found so far.
    if (OPTIONS_PACKAGE_INTERFACES.includes(clazz.name))
      topLevelTypes.set(clazz.name, iface);
    else
      writeFile(dir, packageName, iface);
  }
}

function parseDocs(upstreamDir: string): Documentation {
  const require = createRequire(import.meta.url);
  const { parseApi } = require(path.join(upstreamDir, 'utils', 'doclint', 'api_parser'));
  const documentation: Documentation = parseApi(path.join(upstreamDir, 'docs', 'src', 'api'));
  // The links to params, options and classes become code. The links to members stay:
  // linkifyMemberRefs() makes {@link} tags of them.
  documentation.setLinkRenderer(({ clazz, param, option }) => {
    if (param)
      return `\`${param.alias}\``;
    if (option)
      return `\`${option.alias}\``;
    if (clazz)
      return `\`${clazz.name}\``;
  });
  return documentation;
}

function main() {
  if (process.argv.length !== 3) {
    console.error(`Usage: node ${path.relative(process.cwd(), process.argv[1])} <upstream-checkout-dir>`);
    console.error('For example: node scripts/generate_java_api.mts .upstream');
    process.exit(1);
  }
  const classes = filterForJava(parseDocs(path.resolve(process.argv[2])))
      .filter(clazz => !HANDWRITTEN_CLASSES.includes(clazz.name));

  const topLevelTypes = new Map<string, TypeDefinition>();
  generateInterfaces(classes.filter(clazz => !isAssertionClass(clazz.name)), API_DIR, API_PACKAGE, topLevelTypes);
  // TODO: generate SoftAssertions once they are implemented.
  generateInterfaces(classes.filter(clazz => isAssertionClass(clazz.name) && !clazz.name.includes('SoftAssertions')),
      path.join(API_DIR, 'assertions'), `${API_PACKAGE}.assertions`, topLevelTypes);
  for (const type of topLevelTypes.values()) {
    if (type instanceof CustomInterface)
      writeFile(API_DIR, API_PACKAGE, type);
    else
      writeFile(path.join(API_DIR, 'options'), `${API_PACKAGE}.options`, type);
  }
  console.log(`Generated the Java API in ${path.relative(process.cwd(), API_DIR)}`);
}

main();
