module playwright.driver.bundle {
  requires playwright.driver;

  exports com.microsoft.playwright.impl.driver.jar;

  opens com.microsoft.playwright.impl.driver.jar;
}
