package com.microsoft.playwright;

import com.microsoft.playwright.fixtures.PlaywrightTest;

import static com.microsoft.playwright.Utils.mapOf;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TestRequestGC {

  @PlaywrightTest
  void shouldWork(Page page) {
      page.evaluate("() => {\n" +
        "    globalThis.objectToDestroy = { hello: 'world' };\n" +
        "    globalThis.weakRef = new WeakRef(globalThis.objectToDestroy);\n" +
        "  }");

      page.requestGC();
      assertEquals(mapOf("hello", "world"), page.evaluate("() => globalThis.weakRef.deref()"));

      page.requestGC();
      assertEquals(mapOf("hello", "world"), page.evaluate("() => globalThis.weakRef.deref()"));

      page.evaluate("() => globalThis.objectToDestroy = null");
      page.requestGC();
      assertNull(page.evaluate("() => globalThis.weakRef.deref()"));
  }
}
