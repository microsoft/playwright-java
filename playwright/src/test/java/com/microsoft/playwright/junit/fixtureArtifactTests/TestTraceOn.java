package com.microsoft.playwright.junit.fixtureArtifactTests;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.junit.FixtureTestCase;
import com.microsoft.playwright.junit.Options;
import com.microsoft.playwright.junit.OptionsFactory;
import com.microsoft.playwright.junit.UsePlaywright;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.abort;

@FixtureTestCase
@UsePlaywright(TestTraceOn.TestOptions.class)
public class TestTraceOn {
  public static class TestOptions implements OptionsFactory {
    @Override
    public Options getOptions() {
      return new Options().setTrace(Options.Trace.ON);
    }
  }

  @Test
  void traceShouldExistWhenTestPasses(Page ignored) {
    // force pass
  }

  @Test
  void traceShouldExistWhenTestIsAborted(Page ignored) {
    abort();
  }

  @Test
  void traceShouldExistWhenTestFails(Page ignored) {
    fail();
  }

}
