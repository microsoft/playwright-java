package com.microsoft.playwright.junit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
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

// Test both traces and screenshots in the same class as they both have the same default directory
// and cleanup is easier
@Tag("fixtureTracing") // These tests should be run in isolation as there are threading issues when run with others
@FixtureTest
public class TestTracesAndScreenshots {
  // Select all the test classes that need to be executed as part of this test
  private static final EngineTestKit.Builder engineTestKitBuilder = EngineTestKit.engine("junit-jupiter")
    .selectors(selectClass(TestTraceWithCustomOutputPath.class), selectClass(TestTraceOn.class), selectClass(TestTraceOff.class),
      selectClass(TestTraceRetainOnFailures.class), selectClass(TestScreenshotOn.class), selectClass(TestScreenshotOff.class),
      selectClass(TestScreenshotOnlyOnFailures.class), selectClass(TestScreenshotWithCustomOutputPath.class));

  @BeforeAll
  static void beforeAll() {
    cleanUpDirs();
    // run tests
    engineTestKitBuilder.execute();
  }

  @AfterAll
  static void afterAll() {
    cleanUpDirs();
  }

  @Test
  void testTraceWithCustomOutputPath() {
    Class<?> testClass = TestTraceWithCustomOutputPath.class;
    List<Method> testMethods = getTestMethods(testClass);

    for (Method test : testMethods) {
      File traceFile = getTracePathForCustomLocation(getCustomOutputPath(), testClass, test).toFile();
      assertTrue(traceFile.exists());
      assertTrue(traceFile.length() > 0);
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

  @Test
  void testScreenshotOn() {
    Class<?> testClass = TestScreenshotOn.class;
    List<Method> testMethods = getTestMethods(testClass);

    for (Method test : testMethods) {
      if (test.getName().contains("BrowserContest")) {
        File screenshotFile1 = getScreenshotPathForCustomLocation(getDefaultOutputPath(), testClass, test, 1).toFile();
        File screenshotFile2 = getScreenshotPathForCustomLocation(getDefaultOutputPath(), testClass, test, 1).toFile();
        assertTrue(screenshotFile1.exists());
        assertTrue(screenshotFile1.length() > 0);
        assertTrue(screenshotFile2.exists());
        assertTrue(screenshotFile2.length() > 0);
      } else {
        File screenshotFile = getScreenshotPathForDefaultLocation(testClass, test).toFile();
        assertTrue(screenshotFile.exists());
        assertTrue(screenshotFile.length() > 0);
      }
    }
  }

  @Test
  void testScreenshotOff() {
    Class<?> testClass = TestScreenshotOff.class;
    List<Method> testMethods = getTestMethods(testClass);

    for (Method test : testMethods) {
      File screenshotFile = getScreenshotPathForDefaultLocation(testClass, test).toFile();
      assertFalse(screenshotFile.exists());

    }
  }

  @Test
  void testScreenshotOnlyOnFailures() {
    Class<?> testClass = TestScreenshotOnlyOnFailures.class;
    List<Method> testMethods = getTestMethods(testClass);

    for (Method test : testMethods) {
      File screenshotFile = getScreenshotPathForDefaultLocation(testClass, test).toFile();
      if (test.getName().contains("ShouldNot")) {
        assertFalse(screenshotFile.exists());
      } else {
        assertTrue(screenshotFile.exists());
        assertTrue(screenshotFile.length() > 0);
      }
    }
  }

  @Test
  void testScreenshotCustomOutputPath() {
    Class<?> testClass = TestScreenshotWithCustomOutputPath.class;
    List<Method> testMethods = getTestMethods(testClass);

    for (Method test : testMethods) {
      File screenshotFile = getScreenshotPathForCustomLocation(getCustomOutputPath(), testClass, test, 1).toFile();
      assertTrue(screenshotFile.exists());
      assertTrue(screenshotFile.length() > 0);
    }
  }


  private Path getScreenshotPathForDefaultLocation(Class<?> testClass, Method test) {
    return getScreenshotPathForCustomLocation(getDefaultOutputPath(), testClass, test, 1);
  }

  private Path getScreenshotPathForCustomLocation(Path outputPath, Class<?> testClass, Method test, int pageNumber) {
    return outputPath.resolve(testClass.getName() + "." + test.getName() + "-" + getBrowserName())
      .resolve("test-finished-" + pageNumber + ".png");
  }

  private Path getTracePathForDefaultLocation(Class<?> testClass, Method test) {
    return getTracePathForCustomLocation(getDefaultOutputPath(), testClass, test);
  }

  private Path getTracePathForCustomLocation(Path outputPath, Class<?> testClass, Method test) {
    return outputPath.resolve(testClass.getName() + "." + test.getName() + "-" + getBrowserName()).resolve("trace.zip");
  }

  private static void cleanUpDirs() {
    cleanUpDirs(getDefaultOutputPath());
    cleanUpDirs(getCustomOutputPath());
  }

  private static void cleanUpDirs(Path path) {
    try (Stream<Path> stream = Files.walk(path)) {
      stream.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    } catch (IOException ignored) {
      // swallow
    }
  }

  private List<Method> getTestMethods(Class<?> testClass) {
    return Arrays.stream(testClass.getDeclaredMethods()).filter(m -> m.isAnnotationPresent(Test.class)).collect(Collectors.toList());
  }

  private static Path getDefaultOutputPath() {
    return Paths.get(System.getProperty("user.dir")).resolve("test-results");
  }

  public static Path getCustomOutputPath() {
    return Paths.get(System.getProperty("java.io.tmpdir")).resolve("playwright-custom-output");
  }
}
