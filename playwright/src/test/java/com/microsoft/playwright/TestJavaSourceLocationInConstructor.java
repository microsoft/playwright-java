package com.microsoft.playwright;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;

import static com.microsoft.playwright.Utils.mapOf;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestJavaSourceLocationInConstructor extends TestBase {
  private static final String SRC_DIRS = System.getenv("PLAYWRIGHT_JAVA_SRC") == null ? "src/test/java" : System.getenv("PLAYWRIGHT_JAVA_SRC");

  @Override
  Playwright.CreateOptions playwrightOptions() {
    return new Playwright.CreateOptions().setEnv(mapOf("PLAYWRIGHT_JAVA_SRC", SRC_DIRS));
  }

  @Test
  void shouldSupportSourcesLocationPassedToPlaywrightCreate(@TempDir Path tmpDir) throws IOException {
    context.tracing().start(new Tracing.StartOptions().setSources(true));
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<button>Click</button>");
    page.click("'Click'");
    Path trace = tmpDir.resolve("trace1.zip");
    context.tracing().stop(new Tracing.StopOptions().setPath(trace));

    Map<String, byte[]> entries = Utils.parseZip(trace);
    Map<String, byte[]> sources = entries.entrySet().stream().filter(e -> e.getKey().endsWith(".txt")).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    assertEquals(1, sources.size());

    String path = getClass().getName().replace('.', File.separatorChar);
    String[] srcRoots = SRC_DIRS.split(File.pathSeparator);
    // Resolve in the last specified source dir.
    Path sourceFile = Paths.get(srcRoots[srcRoots.length - 1], path + ".java");
    byte[] thisFile = Files.readAllBytes(sourceFile);
    assertEquals(new String(thisFile, UTF_8), new String(sources.values().iterator().next(), UTF_8));
  }
}
