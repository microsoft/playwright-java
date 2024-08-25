package com.microsoft.playwright.impl.junit;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class SelectedBrowserExtension implements BeforeEachCallback {

  private final String selectedBrowser;

  public SelectedBrowserExtension(String selectedBrowser) {
    this.selectedBrowser = selectedBrowser;
  }

  @Override
  public void beforeEach(ExtensionContext context){
    context.getStore(PlaywrightExtension.namespace).put(SelectedBrowserExtension.class, selectedBrowser);
  }
}
