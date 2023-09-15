package com.microsoft.playwright.junit;

import com.microsoft.playwright.Page;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@UsePlaywright
public class PageTests {
  private static Page pageFromBeforeAll;
  private Page pageFromBeforeEach;

  @BeforeAll
  static void beforeAll(Page page) {
    assert page != null;
    pageFromBeforeAll = page;
  }

  @BeforeEach
  void beforeEach(Page page) {
    assert page != null;
    pageFromBeforeEach = page;
    assertNotEquals(page, pageFromBeforeAll);
  }

  @Test
  void test1(Page page) {
    assert page != null;
    assertEquals(page, pageFromBeforeEach);
  }
}
