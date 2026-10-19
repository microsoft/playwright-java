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

package com.microsoft.playwright.impl.junit;

import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.junit.Options;
import com.microsoft.playwright.junit.PlaywrightOptions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Member;

import static org.junit.jupiter.api.Assertions.*;

public class TestOptionsExtension {
  static class StaticField {
    @PlaywrightOptions
    static Options options = new Options();
  }

  static class Subclass extends StaticField {
  }

  static class InstanceMethod {
    @PlaywrightOptions
    Options options() {
      return new Options();
    }

    class Nested {
    }
  }

  static class FieldAndMethod {
    @PlaywrightOptions
    static Options options = new Options();

    @PlaywrightOptions
    static Options moreOptions() {
      return new Options();
    }
  }

  static class WrongType {
    @PlaywrightOptions
    static String options = "";
  }

  static class MethodWithParameters {
    @PlaywrightOptions
    static Options options(String name) {
      return new Options();
    }
  }

  static class NoOptions {
  }

  @Test
  void shouldFindStaticField() {
    Member member = OptionsExtension.findOptionsMember(StaticField.class);
    assertEquals("options", member.getName());
    assertEquals(StaticField.class, member.getDeclaringClass());
  }

  @Test
  void shouldFindMemberInSuperclass() {
    Member member = OptionsExtension.findOptionsMember(Subclass.class);
    assertEquals(StaticField.class, member.getDeclaringClass());
  }

  @Test
  void shouldFindMemberInEnclosingClass() {
    Member member = OptionsExtension.findOptionsMember(InstanceMethod.Nested.class);
    assertEquals("options", member.getName());
    assertEquals(InstanceMethod.class, member.getDeclaringClass());
  }

  @Test
  void shouldReturnNullWhenThereIsNoMember() {
    assertNull(OptionsExtension.findOptionsMember(NoOptions.class));
  }

  @Test
  void shouldThrowWhenThereAreSeveralMembers() {
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> OptionsExtension.findOptionsMember(FieldAndMethod.class));
    assertEquals("Only one @PlaywrightOptions field or method is allowed, found: " +
      "@PlaywrightOptions field " + FieldAndMethod.class.getName() + ".options, " +
      "@PlaywrightOptions method " + FieldAndMethod.class.getName() + ".moreOptions()", e.getMessage());
  }

  @Test
  void shouldThrowOnWrongType() {
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> OptionsExtension.findOptionsMember(WrongType.class));
    assertEquals("@PlaywrightOptions field " + WrongType.class.getName() + ".options must be of type " +
      Options.class.getName() + ".", e.getMessage());
  }

  @Test
  void shouldThrowOnMethodWithParameters() {
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> OptionsExtension.findOptionsMember(MethodWithParameters.class));
    assertEquals("@PlaywrightOptions method " + MethodWithParameters.class.getName() + ".options() must not have parameters.",
      e.getMessage());
  }
}
