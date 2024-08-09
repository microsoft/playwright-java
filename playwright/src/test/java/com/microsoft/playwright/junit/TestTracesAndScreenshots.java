package com.microsoft.playwright.junit;

import com.microsoft.playwright.junit.fixtureArtifactTests.*;
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
@Tag("fixtureArtifacts") // These tests should be run in isolation as there are threading issues when run with others
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
      assertTrue(traceFile.exists(), traceFile::getAbsolutePath);
      assertTrue(traceFile.length() > 0);
    }
  }

  @Test
  void testTraceOn() {
    Class<?> testClass = TestTraceOn.class;
    List<Method> testMethods = getTestMethods(testClass);

    for (Method test : testMethods) {
      File traceFile = getTracePathForDefaultLocation(testClass, test).toFile();
      assertTrue(traceFile.exists(), traceFile::getAbsolutePath);
      assertTrue(traceFile.length() > 0);
    }
  }

  @Test
  void testTraceOff() {
    Class<?> testClass = TestTraceOff.class;
    List<Method> testMethods = getTestMethods(testClass);

    for (Method test : testMethods) {
      File traceFile = getTracePathForDefaultLocation(testClass, test).toFile();
      assertFalse(traceFile.exists(), traceFile::getAbsolutePath);
    }
  }

  @Test
  void testTraceRetainOnFailure() {
    Class<?> testClass = TestTraceRetainOnFailures.class;
    List<Method> testMethods = getTestMethods(testClass);

    for (Method test : testMethods) {
      File traceFile = getTracePathForDefaultLocation(testClass, test).toFile();
      if(test.getName().equals("traceShouldExistWhenTestFails")) {
        assertTrue(traceFile.exists(), traceFile::getAbsolutePath);
      } else {
        assertFalse(traceFile.exists(), traceFile::getAbsolutePath);
      }
    }
  }

  @Test
  void testScreenshotOn() {
    Class<?> testClass = TestScreenshotOn.class;
    List<Method> testMethods = getTestMethods(testClass);

    for (Method test : testMethods) {
      if (test.getName().equals("screenshotsShouldExistForAllPagesInTheBrowserContext")) {
        File screenshotFile1 = getScreenshotPathForCustomLocation(getDefaultOutputPath(), testClass, test, 1, false).toFile();
        File screenshotFile2 = getScreenshotPathForCustomLocation(getDefaultOutputPath(), testClass, test, 2, false).toFile();
        assertTrue(screenshotFile1.exists());
        assertTrue(screenshotFile1.length() > 0);
        assertTrue(screenshotFile2.exists());
        assertTrue(screenshotFile2.length() > 0);
      } else if (test.getName().equals("screenshotShouldExistWhenTestFails")) {
        File screenshotFile = getScreenshotPathForDefaultLocation(testClass, test, true).toFile();
        assertTrue(screenshotFile.exists(), screenshotFile::getAbsolutePath);
        assertTrue(screenshotFile.length() > 0, screenshotFile::getAbsolutePath);
      } else {
        File screenshotFile = getScreenshotPathForDefaultLocation(testClass, test, false).toFile();
        assertTrue(screenshotFile.exists(), screenshotFile::getAbsolutePath);
        assertTrue(screenshotFile.length() > 0, screenshotFile::getAbsolutePath);
      }
    }
  }

  @Test
  void testScreenshotOff() {
    Class<?> testClass = TestScreenshotOff.class;
    List<Method> testMethods = getTestMethods(testClass);

    for (Method test : testMethods) {
      File screenshotFileFail = getScreenshotPathForDefaultLocation(testClass, test, false).toFile();
      File screenshotFilePass = getScreenshotPathForDefaultLocation(testClass, test, true).toFile();
      assertFalse(screenshotFileFail.exists(), screenshotFileFail::getAbsolutePath);
      assertFalse(screenshotFilePass.exists(), screenshotFilePass::getAbsolutePath);
    }
  }

  @Test
  void testScreenshotOnlyOnFailures() {
    Class<?> testClass = TestScreenshotOnlyOnFailures.class;
    List<Method> testMethods = getTestMethods(testClass);

    for (Method test : testMethods) {
      if (test.getName().contains("screenshotShouldExistWhenTestFails")) {
        File screenshotFile = getScreenshotPathForDefaultLocation(testClass, test, true).toFile();
        assertTrue(screenshotFile.exists(), screenshotFile::getAbsolutePath);
      } else {
        File screenshotFile = getScreenshotPathForDefaultLocation(testClass, test, false).toFile();
        assertFalse(screenshotFile.exists(), screenshotFile::getAbsolutePath);
      }
    }
  }

  @Test
  void testScreenshotCustomOutputPath() {
    Class<?> testClass = TestScreenshotWithCustomOutputPath.class;
    List<Method> testMethods = getTestMethods(testClass);

    for (Method test : testMethods) {
      if(test.getName().equals("screenshotShouldExistInCustomOutputPathWhenTestFails")) {
        File screenshotFile = getScreenshotPathForCustomLocation(getCustomOutputPath(), testClass, test, 1, true).toFile();
        assertTrue(screenshotFile.exists(), screenshotFile::getAbsolutePath);
        assertTrue(screenshotFile.length() > 0);
      } else {
        File screenshotFile = getScreenshotPathForCustomLocation(getCustomOutputPath(), testClass, test, 1, false).toFile();
        assertTrue(screenshotFile.exists(), screenshotFile::getAbsolutePath);
        assertTrue(screenshotFile.length() > 0);
      }
    }
  }


  private Path getScreenshotPathForDefaultLocation(Class<?> testClass, Method test, boolean didTestFail) {
    return getScreenshotPathForCustomLocation(getDefaultOutputPath(), testClass, test, 1, didTestFail);
  }

  private Path getScreenshotPathForCustomLocation(Path outputPath, Class<?> testClass, Method test, int pageNumber, boolean didTestFail) {
    String fileNamePrefix = "test-finished-";
    if(didTestFail) {
      fileNamePrefix = "test-failed-";
    }
    return outputPath.resolve(testClass.getName() + "." + test.getName() + "-" + getBrowserName())
      .resolve(fileNamePrefix + pageNumber + ".png");
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
