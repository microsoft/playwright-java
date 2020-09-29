/**
 * Copyright (c) Microsoft Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.playwright.example;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.microsoft.playwright.*;
import com.microsoft.playwright.impl.*;

import java.io.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;


public class Main {

  public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
    Playwright playwright = Playwright.create();
    BrowserType.LaunchOptions options = new BrowserType.LaunchOptions()
//      .withSlowMo(1000)
      .withHeadless(false);

    System.out.println("options = " + new Gson().toJson(options));
    Browser browser = playwright.chromium().launch(options);
    System.out.println("browser = " + browser);

    Browser.NewContextOptions contextOptions = new Browser.NewContextOptions();
    contextOptions.setViewport().withWidth(800).withHeight(600);
    BrowserContext context = browser.newContext(contextOptions);
    Page page = context.newPage();
//    page.navigate("http://example.com", null);
    page.navigate("https://webkit.org", null);
    page.click("text=web browser engine", new Page.ClickOptions());

//    Supplier<PageImpl> popupSupplier = page.waitForPopup();
//    Supplier<PageImpl> pageSupplier = context.waitForPage();
    page.evaluate("window.open('http://example.com'); 13", null);
    {
      Object r = page.evaluate("function foo(a) { return a + 1; }", 20);
      System.out.println("r = " + new Gson().toJson(r));
    }
//    {
//      List<Integer> r = page.evalTyped("function foo() { return [1,2,3]; }");
//      System.out.println("r = " + new Gson().toJson(r));
//      int p = r.get(0).intValue() + 1;
//    }
//
//    {
//      int r = page.evalTyped("function foo() { return 7; }");
//      System.out.println("int r = " + new Gson().toJson(r));
//    }
//
//    {
//      double r = page.evalTyped("function foo() { return 7.2; }");
//      System.out.println("double r = " + new Gson().toJson(r));
//    }
//
//    PageImpl popup = popupSupplier.get();
//    System.out.println("popup = " + popup);
//    PageImpl page2 = pageSupplier.get();
//    System.out.println(page2 == popup);
//
//
//
//
//    page.addDialogHandler(d -> {
//      System.out.println("Got dialog type: " +  d.type());
//      System.out.println("    message = " +  d.message());
//      d.accept("abc");
//    });
//    page.evaluate("alert('Hi there!')");
//    System.out.println("After alert");
//
//
//
//    page.addConsoleListener(m -> {
//      System.out.println("Got console message type: " +  m.type());
//      System.out.println("    text = " +  m.text());
//      System.out.println("    location = " +  m.location());
//    });
//    page.evaluate("console.log('A message')");

//    Thread.sleep(1000);
    browser.close();

    // Disconnect and terminate the threads?
    // playwright.close();
    System.out.println("\nDONE.");
    System.exit(0);
  }
}
