package com.microsoft.playwright.junit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.platform.testkit.engine.EngineTestKit;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;

import static com.microsoft.playwright.TestOptionsFactories.getBrowserName;
import static com.microsoft.playwright.junit.FixtureTestUtils.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

@FixtureTest
public class TestScreenshots {
  // Select all the test classes that need to be executed as part of this test
  private static final EngineTestKit.Builder engineTestKitBuilder = EngineTestKit.engine("junit-jupiter")
    .selectors(selectClass(TestScreenshotOn.class), selectClass(TestScreenshotOff.class),
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
      File screenshotFile = getScreenshotPathForCustomLocation(getCustomOutputPath().resolve("screenshotTest"), testClass, test, 1).toFile();
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
}
