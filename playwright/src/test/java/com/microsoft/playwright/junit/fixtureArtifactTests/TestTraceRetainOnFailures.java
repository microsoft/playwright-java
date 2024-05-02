package com.microsoft.playwright.junit.fixtureArtifactTests;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.junit.FixtureTestCase;
import com.microsoft.playwright.junit.Options;
import com.microsoft.playwright.junit.OptionsFactory;
import com.microsoft.playwright.junit.UsePlaywright;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.abort;

@UsePlaywright(TestTraceRetainOnFailures.TestOptions.class)
@FixtureTestCase
public class TestTraceRetainOnFailures {
  public static class TestOptions implements OptionsFactory {
    @Override
    public Options getOptions() {
      return new Options().setTrace(Options.Trace.RETAIN_ON_FAILURE);
    }
  }

  @Test
  void traceShouldNotExistWhenTestPasses(Page ignored) {
    // force pass
  }

  @Test
  void traceShouldNotExistWhenTestIsAborted(Page ignored) {
    abort();
  }

  @Test
  void traceShouldExistWhenTestFails(Page ignored) {
    fail();
  }
}
