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
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

@FixtureTest
public class TestTraces {
  // Select all the test classes that need to be executed as part of this test
  private static final EngineTestKit.Builder engineTestKitBuilder = EngineTestKit.engine("junit-jupiter")
    .selectors(selectClass(TestTraceWithCustomOutputPath.class), selectClass(TestTraceOn.class), selectClass(TestTraceOff.class),
      selectClass(TestTraceRetainOnFailures.class));

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
      File traceFile = getTracePathForCustomLocation(getCustomOutputPath().resolve("traceTest"), testClass, test).toFile();
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

  private Path getTracePathForDefaultLocation(Class<?> testClass, Method test) {
    return getTracePathForCustomLocation(getDefaultOutputPath(), testClass, test);
  }

  private Path getTracePathForCustomLocation(Path outputPath, Class<?> testClass, Method test) {
    return outputPath.resolve(testClass.getName() + "." + test.getName() + "-" + getBrowserName()).resolve("trace.zip");
  }
}
