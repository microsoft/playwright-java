package com.microsoft.playwright.junit;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FixtureTestUtils {

  public static void cleanUpDirs() {
    cleanUpDirs(getDefaultOutputPath());
    cleanUpDirs(getCustomOutputPath());
  }

  public static void cleanUpDirs(Path path) {
    try (Stream<Path> stream = Files.walk(path)) {
      stream.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    } catch (IOException ignored) {
      // swallow
    }
  }

  public static List<Method> getTestMethods(Class<?> testClass) {
    return Arrays.stream(testClass.getDeclaredMethods()).filter(m -> m.isAnnotationPresent(Test.class)).collect(Collectors.toList());
  }

  public static Path getDefaultOutputPath() {
    return Paths.get(System.getProperty("user.dir")).resolve("test-results");
  }

  public static Path getCustomOutputPath() {
    return Paths.get(System.getProperty("java.io.tmpdir")).resolve("playwright-custom-output");
  }
}
