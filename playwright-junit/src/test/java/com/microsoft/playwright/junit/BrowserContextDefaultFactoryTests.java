package com.microsoft.playwright.junit;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.PlaywrightException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@UsePlaywright
public class BrowserContextDefaultFactoryTests {
  private static BrowserContext browserContextFromBeforeAll;
  private BrowserContext browserContextFromBeforeEach;

  @BeforeAll
  public static void beforeAll(BrowserContext browserContext) {
    assert browserContext != null;
    PlaywrightException pe = assertThrows(PlaywrightException.class, () -> browserContext.newPage().navigate("/"));
    assertTrue(pe.getMessage().contains("Cannot navigate to invalid URL"));
    browserContextFromBeforeAll = browserContext;
  }

  @BeforeEach
  public void beforeEach(BrowserContext browserContext) {
    assert browserContext != null;
    browserContextFromBeforeEach = browserContext;
    assertNotEquals(browserContext, browserContextFromBeforeAll);
  }

  @Test
  public void test1(BrowserContext browserContext) {
    assert browserContext != null;
    assertEquals(browserContext, browserContextFromBeforeEach);
  }
}
