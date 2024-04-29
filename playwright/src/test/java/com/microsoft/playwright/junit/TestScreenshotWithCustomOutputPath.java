package com.microsoft.playwright.junit;

import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.junit.FixtureTestUtils.getCustomOutputPath;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.abort;

@Transient
@UsePlaywright(TestScreenshotWithCustomOutputPath.TestOptions.class)
public class TestScreenshotWithCustomOutputPath {
  public static class TestOptions implements OptionsFactory {
    @Override
    public Options getOptions() {
      return new Options().setScreenshot(Options.Screenshot.ON).setOutputDir(getCustomOutputPath().resolve("screenshotTest"));
    }
  }

  @Test
  void screenshotShouldExistInCustomOutputPathWhenTestPasses(Page ignored) {
    // force pass
  }

  @Test
  void screenshotShouldExistInCustomOutputPathWhenTestIsAborted(Page ignored) {
    abort();
  }

  @Test
  void screenshotShouldExistInCustomOutputPathWhenTestFails(Page ignored) {
    fail();
  }
}
