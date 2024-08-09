package com.microsoft.playwright.junit.fixtureArtifactTests;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.junit.FixtureTestCase;
import com.microsoft.playwright.junit.Options;
import com.microsoft.playwright.junit.OptionsFactory;
import com.microsoft.playwright.junit.UsePlaywright;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.junit.TestTracesAndScreenshots.getCustomOutputPath;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.abort;

@FixtureTestCase
@UsePlaywright(TestScreenshotWithCustomOutputPath.TestOptions.class)
public class TestScreenshotWithCustomOutputPath {
  public static class TestOptions implements OptionsFactory {
    @Override
    public Options getOptions() {
      return new Options().setScreenshot(Options.Screenshot.ON).setOutputDir(getCustomOutputPath());
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
