package com.microsoft.playwright;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestPageAddScriptTag extends TestBase {
  @Test
  @DisabledIf(value="com.microsoft.playwright.TestBase#isWebKit", disabledReason="Upstream behavior")
  void shouldIncludeSourceURLWhenPathIsProvided() {
    page.navigate(server.EMPTY_PAGE);
    Path path = Paths.get("src/test/resources/injectedfile.js");
    page.addScriptTag(new Page.AddScriptTagOptions().setPath(path));
    String result = (String) page.evaluate("() => window['__injectedError'].stack");
    assertTrue(result.contains("resources/injectedfile.js"));
  }
}
