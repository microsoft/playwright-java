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
@UsePlaywright(TestTraceWithCustomOutputPath.TestOptions.class)
public class TestTraceWithCustomOutputPath {
  public static class TestOptions implements OptionsFactory {
    @Override
    public Options getOptions() {
      return new Options().setTrace(Options.Trace.ON).setOutputDir(getCustomOutputPath());
    }
  }

  @Test
  void traceShouldExistInCustomOutputPathWhenTestPasses(Page ignored) {
    // force pass
  }

  @Test
  void traceShouldExistInCustomOutputPathWhenTestIsAborted(Page ignored) {
    abort();
  }

  @Test
  void traceShouldExistInCustomOutputPathWhenTestFails(Page ignored) {
    fail();
  }
}
