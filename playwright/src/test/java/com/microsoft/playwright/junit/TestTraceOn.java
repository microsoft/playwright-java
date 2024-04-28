package com.microsoft.playwright.junit;

import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.abort;

@Transient
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
