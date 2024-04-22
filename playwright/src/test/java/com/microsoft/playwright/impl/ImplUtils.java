package com.microsoft.playwright.impl;

import com.microsoft.playwright.Browser;

public class ImplUtils {
  public static boolean isRemoteBrowser(Browser browser) {
    return ((BrowserImpl) browser).isConnectedOverWebSocket;
  }
}
