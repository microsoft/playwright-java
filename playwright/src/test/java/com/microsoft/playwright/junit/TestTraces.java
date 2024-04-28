package com.microsoft.playwright.junit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.platform.testkit.engine.EngineTestKit;

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

import static com.microsoft.playwright.TestOptionsFactories.getBrowserName;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

// Test traces and screenshots together because the artifacts go in the same directory
@FixtureTest
public class TestTraces {
  private static final Path baseDir = Paths.get(System.getProperty("user.dir")).resolve("test-results");
  private static final EngineTestKit.Builder engineTestKitBuilder = EngineTestKit.engine("junit-jupiter")
    .selectors(selectClass(TestTraceWithCustomOutputPath.class), selectClass(TestTraceOn.class), selectClass(TestTraceOff.class),
      selectClass(TestTraceRetainOnFailures.class));

  @BeforeAll
  static void beforeAll() {
    cleanUpDirs();
    engineTestKitBuilder.execute();
  }

  @AfterAll
  static void afterAll() {
    cleanUpDirs();
  }

  @Test
  void testTraceWithCustomOutput() {
    Class<?> testClass = TestTraceWithCustomOutputPath.class;
    List<Method> testMethods = getTestMethods(testClass);
    Path outputPath = null;
    try {
      outputPath = (Path) testClass.getDeclaredField("outputPath").get(null);
      for (Method test : testMethods) {
        File traceFile = getTracePathForCustomLocation(outputPath, testClass, test).toFile();
        assertTrue(traceFile.exists());
        assertTrue(traceFile.length() > 0);
      }
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException(e);
    } finally {
      cleanUpDirs(outputPath);
    }
  }

  @Test
  void testTraceOn() {
    Class<?> testClass = TestTraceOn.class;
    List<Method> testMethods = getTestMethods(testClass);

    for (Method test : testMethods) {
      File traceFile = getTracePathForDefaultLocation(testClass, test).toFile();
      assertTrue(traceFile.exists());
      assertTrue(traceFile.length() > 0);
    }
  }

  @Test
  void testTraceOff() {
    Class<?> testClass = TestTraceOff.class;
    List<Method> testMethods = getTestMethods(testClass);

    for (Method test : testMethods) {
      File traceFile = getTracePathForDefaultLocation(testClass, test).toFile();
      assertFalse(traceFile.exists());
    }
  }

  @Test
  void testTraceRetainOnFailure() {
    Class<?> testClass = TestTraceRetainOnFailures.class;
    List<Method> testMethods = getTestMethods(testClass);

    for (Method test : testMethods) {
      File traceFile = getTracePathForDefaultLocation(testClass, test).toFile();
      if (test.getName().contains("ShouldNot")) {
        assertFalse(traceFile.exists());
      } else {
        assertTrue(traceFile.exists());
      }
    }
  }

  private List<Method> getTestMethods(Class<?> testClass) {
    return Arrays.stream(testClass.getDeclaredMethods()).filter(m -> m.isAnnotationPresent(Test.class)).collect(Collectors.toList());
  }

  private Path getTracePathForDefaultLocation(Class<?> testClass, Method test) {
    return getTracePathForCustomLocation(baseDir, testClass, test);
  }

  private Path getTracePathForCustomLocation(Path customBaseDir, Class<?> testClass, Method test) {
    return customBaseDir.resolve(testClass.getName() + "." + test.getName() + "-" + getBrowserName()).resolve("trace.zip");
  }

  private static void cleanUpDirs() {
    cleanUpDirs(baseDir);
  }

  private static void cleanUpDirs(Path path) {
    try (Stream<Path> stream = Files.walk(path)) {
      stream.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    } catch (IOException ignored) {
      // swallow
    }
  }
}
