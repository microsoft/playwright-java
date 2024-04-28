package com.microsoft.playwright.junit;

import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.abort;

@Transient
@UsePlaywright(TestTraceOff.TestOptions.class)
public class TestTraceOff {
  public static class TestOptions implements OptionsFactory {
    @Override
    public Options getOptions() {
      return new Options().setTrace(Options.Trace.OFF);
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
  void traceShouldNotExistWhenTestFails(Page ignored) {
    fail();
  }
}
