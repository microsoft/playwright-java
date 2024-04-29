package com.microsoft.playwright.junit;

import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.abort;

@Transient
@UsePlaywright(TestScreenshotOnlyOnFailures.TestOptions.class)
public class TestScreenshotOnlyOnFailures {
  public static class TestOptions implements OptionsFactory {
    @Override
    public Options getOptions() {
      return new Options().setScreenshot(Options.Screenshot.ONLY_ON_FAILURE);
    }
  }

  @Test
  void screenshotShouldNotExistWhenTestPasses(Page ignored) {
    // force pass
  }

  @Test
  void screenshotShouldNotExistWhenTestIsAborted(Page ignored) {
    abort();
  }

  @Test
  void screenshotShouldExistWhenTestFails(Page ignored) {
    fail();
  }
}
