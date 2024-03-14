package com.microsoft.playwright.junit;

import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@UsePlaywright
public class TestFixturesWithNestedClass {

  @Test
  void worksWithOuterClass(Page page) {
    assertNotNull(page);
  }

  @Nested
  class NestedClass {
    @Test
    void worksWithNestedClasses(Page page) {
      assertNotNull(page);
    }

    @Nested
    class DeeplyNestedClass {
      @Test
      void worksWithDeeplyNestedClass(Page page) {
        assertNotNull(page);
      }
    }
  }
}
