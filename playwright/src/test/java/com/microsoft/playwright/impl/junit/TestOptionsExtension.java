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

import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.junit.Options;
import com.microsoft.playwright.junit.PlaywrightOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.TestInstances;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class TestOptionsExtension {
  static class AllTypes {
    @PlaywrightOptions
    static final Options options = new Options()
      .setBaseUrl("http://localhost:1234/")
      .setContextOptions(new Browser.NewContextOptions().setLocale("fr-FR"));

    @PlaywrightOptions
    static final BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions().setHeadless(false);

    @PlaywrightOptions
    static Browser.NewContextOptions contextOptions() {
      return new Browser.NewContextOptions().setLocale("de-DE");
    }

    @PlaywrightOptions
    static final APIRequest.NewContextOptions apiRequestOptions = new APIRequest.NewContextOptions().setBaseURL("http://localhost:1234/api/");
  }

  static class Base {
    @PlaywrightOptions
    static final Options options = new Options().setBaseUrl("http://localhost:1234/base/");
  }

  static class Subclass extends Base {
  }

  static class Outer {
    @PlaywrightOptions
    static final Options options = new Options().setBaseUrl("http://localhost:1234/outer/").setIgnoreHTTPSErrors(true);

    class Nested {
      @PlaywrightOptions
      Browser.NewContextOptions contextOptions = new Browser.NewContextOptions().setLocale("de-DE");
    }
  }

  static class InstanceMethod {
    @PlaywrightOptions
    Options options() {
      return new Options();
    }
  }

  static class TwoOfSameType {
    @PlaywrightOptions
    static final Options options = new Options();

    @PlaywrightOptions
    static Options moreOptions() {
      return new Options();
    }
  }

  static class WrongType {
    @PlaywrightOptions
    static final String options = "";
  }

  static class MethodWithParameters {
    @PlaywrightOptions
    static Options options(String name) {
      return new Options();
    }
  }

  static class NullOptions {
    @PlaywrightOptions
    static final Options options = null;
  }

  static class NoOptions {
  }

  private static TestInstances testInstances(Object... instances) {
    return new TestInstances() {
      @Override
      public Object getInnermostInstance() {
        return instances[instances.length - 1];
      }

      @Override
      public List<Object> getEnclosingInstances() {
        return Arrays.asList(instances).subList(0, instances.length - 1);
      }

      @Override
      public List<Object> getAllInstances() {
        return Arrays.asList(instances);
      }

      @Override
      public <T> Optional<T> findInstance(Class<T> requiredType) {
        return getAllInstances().stream().filter(requiredType::isInstance).map(requiredType::cast).findFirst();
      }
    };
  }

  @Test
  void shouldCombineMembersOfAllTypes() {
    Options options = OptionsExtension.createOptions(AllTypes.class, Optional.empty());
    assertEquals("http://localhost:1234/", options.baseUrl);
    assertEquals(false, options.launchOptions.headless);
    assertEquals("de-DE", options.contextOptions.locale);
    assertEquals("http://localhost:1234/api/", options.apiRequestOptions.baseURL);
  }

  @Test
  void shouldNotModifyOptionsOfTheTestClass() {
    OptionsExtension.createOptions(AllTypes.class, Optional.empty());
    assertEquals("fr-FR", AllTypes.options.contextOptions.locale);
    assertNull(AllTypes.options.launchOptions);
    assertNull(AllTypes.options.apiRequestOptions);
  }

  @Test
  void shouldFindMemberInSuperclass() {
    Options options = OptionsExtension.createOptions(Subclass.class, Optional.empty());
    assertEquals("http://localhost:1234/base/", options.baseUrl);
  }

  @Test
  void shouldOverrideOnlyMembersOfTheSameTypeInNestedClass() {
    Outer outer = new Outer();
    Outer.Nested nested = outer.new Nested();
    Options options = OptionsExtension.createOptions(Outer.Nested.class, Optional.of(testInstances(outer, nested)));
    assertEquals("de-DE", options.contextOptions.locale);
    assertEquals("http://localhost:1234/outer/", options.baseUrl);
    assertEquals(true, options.ignoreHTTPSErrors);
  }

  @Test
  void shouldReturnDefaultOptionsWhenThereAreNoMembers() {
    Options options = OptionsExtension.createOptions(NoOptions.class, Optional.empty());
    assertNull(options.baseUrl);
    assertEquals(Options.Trace.OFF, options.trace);
  }

  @Test
  void shouldThrowWithoutTestInstance() {
    PlaywrightException e = assertThrows(PlaywrightException.class,
      () -> OptionsExtension.createOptions(InstanceMethod.class, Optional.empty()));
    assertEquals("@PlaywrightOptions method " + InstanceMethod.class.getName() + ".options() requires a test instance. " +
      "Make it static, or use @TestInstance(TestInstance.Lifecycle.PER_CLASS) to request Playwright fixtures in @BeforeAll methods or constructors.",
      e.getMessage());
  }

  @Test
  void shouldThrowWhenThereAreSeveralMembersOfTheSameType() {
    PlaywrightException e = assertThrows(PlaywrightException.class,
      () -> OptionsExtension.createOptions(TwoOfSameType.class, Optional.empty()));
    assertEquals("Only one @PlaywrightOptions field or method of type com.microsoft.playwright.junit.Options is allowed, found: " +
      "@PlaywrightOptions field " + TwoOfSameType.class.getName() + ".options, " +
      "@PlaywrightOptions method " + TwoOfSameType.class.getName() + ".moreOptions()", e.getMessage());
  }

  @Test
  void shouldThrowOnWrongType() {
    PlaywrightException e = assertThrows(PlaywrightException.class,
      () -> OptionsExtension.createOptions(WrongType.class, Optional.empty()));
    assertEquals("@PlaywrightOptions field " + WrongType.class.getName() + ".options must be of type " +
      "com.microsoft.playwright.junit.Options, com.microsoft.playwright.BrowserType.LaunchOptions, " +
      "com.microsoft.playwright.Browser.NewContextOptions, com.microsoft.playwright.APIRequest.NewContextOptions.", e.getMessage());
  }

  @Test
  void shouldThrowOnMethodWithParameters() {
    PlaywrightException e = assertThrows(PlaywrightException.class,
      () -> OptionsExtension.createOptions(MethodWithParameters.class, Optional.empty()));
    assertEquals("@PlaywrightOptions method " + MethodWithParameters.class.getName() + ".options() must not have parameters.", e.getMessage());
  }

  @Test
  void shouldThrowOnNullOptions() {
    PlaywrightException e = assertThrows(PlaywrightException.class,
      () -> OptionsExtension.createOptions(NullOptions.class, Optional.empty()));
    assertEquals("@PlaywrightOptions field " + NullOptions.class.getName() + ".options must not be null.", e.getMessage());
  }
}
