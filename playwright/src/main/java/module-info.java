module playwright {
  requires playwright.driver;
  requires jdk.httpserver;
  requires java.desktop;
  requires com.google.gson;
  requires org.opentest4j;

  exports com.microsoft.playwright.assertions;
  exports com.microsoft.playwright.impl;
  exports com.microsoft.playwright.options;
  exports com.microsoft.playwright;

  opens com.microsoft.playwright;
  opens com.microsoft.playwright.impl;
}
