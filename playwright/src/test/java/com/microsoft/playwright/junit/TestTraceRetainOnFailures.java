package com.microsoft.playwright.junit;

import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.abort;

@UsePlaywright(TestTraceRetainOnFailures.TestOptions.class)
@Transient
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
