package com.microsoft.playwright.junit;

import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestFixtureWithBaseClass extends FixtureAbstractClass {

  @Test
  void worksWithBaseClassProvidingFixtures(Page page) {
    assertNotNull(page);
  }

  @Nested
  public class NestedClass {
    @Test
    void worksWithNestedClassInsideClassThatExtendsBaseClassThatProvidesFixtures(Page page) {
      assertNotNull(page);
    }
  }

}
