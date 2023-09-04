package com.microsoft.playwright;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Execution(ExecutionMode.SAME_THREAD)
@UseBrowserFactory
public class PlaywrightSameThreadTests {
  private Playwright p;
  private static Playwright ps;

  @BeforeAll
  static void beforeAll(Playwright playwright) {
    assert playwright != null;
    System.out.println("BeforeAll " + playwright);
    ps = playwright;
  }

  @AfterAll
  static void afterAll(Playwright playwright) {
    assert playwright != null;
    System.out.println("AfterAll " + playwright);
    assertEquals(ps, playwright, "Static Playwright is not equal to others that were created");
  }

  @BeforeEach
  void beforeEach(Playwright playwright) {
    assert playwright != null;
    System.out.println("BeforeEach " + playwright);
    p = playwright;
    assertEquals(p, ps);
  }

  @AfterEach
  void afterEach(Playwright playwright) {
    assert playwright != null;
    System.out.println("AfterEach " + playwright);
    assertEquals(p, ps);
    assertEquals(p, playwright, "Playwright parameter is not equal to the one created in the hooks");
    assertEquals(playwright, ps, "Playwright parameter is not equal to static Playwright from Before/AfterAll hooks");
  }

  @Test
  void test1(Playwright playwright) {
    assert playwright != null;
    System.out.println("Test1" + playwright);
    assertEquals(p, playwright, "Playwright parameter is not equal to others that were created");
    assertEquals(playwright, ps, "Playwright parameter is not equal to static Playwright from Before/AfterAll hooks");
  }

  @Test
  void test2(Playwright playwright) {
    assert playwright != null;
    System.out.println("Test2" + playwright);
    assertEquals(p, playwright, "Playwright parameter is not equal to others that were created");
    assertEquals(playwright, ps, "Playwright parameter is not equal to static Playwright from Before/AfterAll hooks");
  }
}
