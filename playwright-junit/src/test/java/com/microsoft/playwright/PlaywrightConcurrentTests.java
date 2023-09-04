package com.microsoft.playwright;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Execution(ExecutionMode.CONCURRENT)
@UseBrowserFactory
public class PlaywrightConcurrentTests {
  private Playwright p;

  @BeforeEach
  void beforeEach(Playwright playwright) {
    assert playwright != null;
    System.out.println("BeforeEach " + playwright);
    p = playwright;
  }

  @AfterEach
  void afterEach(Playwright playwright) {
    assert playwright != null;
    System.out.println("AfterEach " + playwright);
    assertEquals(p, playwright, "Playwright parameter is not equal to the one created in the hooks");
  }

  @Test
  void test1(Playwright playwright) {
    assert playwright != null;
    System.out.println("Test1" + playwright);
    assertEquals(p, playwright, "Playwright parameter is not equal to the one created in the hooks");
  }

  @Test
  void test2(Playwright playwright) {
    assert playwright != null;
    System.out.println("Test2" + playwright);
    assertEquals(p, playwright, "Playwright parameter is not equal to the one created in the hooks");
  }
}
