package com.microsoft.playwright.impl.junit;

import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;

import java.util.Collections;
import java.util.List;

public class MultiBrowserTestTemplateInvocationContext implements TestTemplateInvocationContext {

  private final String selectedBrowser;

  public MultiBrowserTestTemplateInvocationContext(String selectedBrowser) {
    this.selectedBrowser = selectedBrowser;
  }

  @Override
  public String getDisplayName(int invocationIndex) {
    return selectedBrowser;
  }

  @Override
  public List<Extension> getAdditionalExtensions() {
    return Collections.singletonList(new SelectedBrowserExtension(selectedBrowser));
  }
}
