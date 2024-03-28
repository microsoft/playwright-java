/*
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

package com.microsoft.playwright;

import com.microsoft.playwright.junit.FixtureTest;
import com.microsoft.playwright.junit.UsePlaywright;
import com.microsoft.playwright.options.Cookie;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;

@FixtureTest
@UsePlaywright(TestOptionsFactories.BasicOptionsFactory.class)
public class TestBrowserContextClearCookies {
  @Test
  void shouldClearCookies(Page page, BrowserContext context, Server server) {
    page.navigate(server.EMPTY_PAGE);
    context.addCookies(asList(
      new Cookie("cookie1", "1").setUrl(server.EMPTY_PAGE)));
    assertEquals("cookie1=1", page.evaluate("document.cookie"));
    context.clearCookies();
    assertEquals(emptyList(), context.cookies());
    page.reload();
    assertEquals("", page.evaluate("document.cookie"));
  }

  @Test
  void shouldIsolateCookiesWhenClearing(BrowserContext context, Browser browser, Server server) {
    BrowserContext anotherContext = browser.newContext();
    context.addCookies(asList(
      new Cookie("page1cookie", "page1value").setUrl(server.EMPTY_PAGE)));
    anotherContext.addCookies(asList(
      new Cookie("page2cookie", "page2value").setUrl(server.EMPTY_PAGE)));

    assertEquals(1, (context.cookies()).size());
    assertEquals(1, (anotherContext.cookies()).size());

    context.clearCookies();
    assertEquals(0, (context.cookies()).size());
    assertEquals(1, (anotherContext.cookies()).size());

    anotherContext.clearCookies();
    assertEquals(0, (context.cookies()).size());
    assertEquals(0, (anotherContext.cookies()).size());
    anotherContext.close();
  }

  @Test
  void shouldRemoveCookiesByName(Page page, BrowserContext context, Server server) throws MalformedURLException {
    context.addCookies(Arrays.asList(
      new Cookie("cookie1", "1").setDomain(new URL(server.PREFIX).getHost()).setPath("/"),
      new Cookie("cookie2", "2").setDomain(new URL(server.PREFIX).getHost()).setPath("/")
    ));

    page.navigate(server.PREFIX);
    assertEquals("cookie1=1; cookie2=2", page.evaluate("document.cookie"));
    context.clearCookies(new BrowserContext.ClearCookiesOptions().setName("cookie1"));
    assertEquals("cookie2=2", page.evaluate("document.cookie"));
  }

  @Test
  public void shouldRemoveCookiesByNameRegex(Page page, BrowserContext context, Server server) throws MalformedURLException {
    context.addCookies(Arrays.asList(
      new Cookie("cookie1", "1").setDomain(new URL(server.PREFIX).getHost()).setPath("/"),
      new Cookie("cookie2", "2").setDomain(new URL(server.PREFIX).getHost()).setPath("/")
    ));

    page.navigate(server.PREFIX);
    assertEquals("cookie1=1; cookie2=2", page.evaluate("document.cookie"));
    context.clearCookies(new BrowserContext.ClearCookiesOptions().setName(Pattern.compile("coo.*1")));
    assertEquals("cookie2=2", page.evaluate("document.cookie"));
  }

  @Test
  public void shouldRemoveCookiesByDomain(Page page, BrowserContext context, Server server) throws MalformedURLException {
    context.addCookies(Arrays.asList(
      new Cookie("cookie1", "1").setDomain(new URL(server.PREFIX).getHost()).setPath("/"),
      new Cookie("cookie2", "2").setDomain(new URL(server.CROSS_PROCESS_PREFIX).getHost()).setPath("/")
    ));
    page.navigate(server.PREFIX);
    assertEquals("cookie1=1", page.evaluate("document.cookie"));
    page.navigate(server.CROSS_PROCESS_PREFIX);
    assertEquals("cookie2=2", page.evaluate("document.cookie"));
    context.clearCookies(new BrowserContext.ClearCookiesOptions().setDomain(new URL(server.CROSS_PROCESS_PREFIX).getHost()));
    assertEquals("", page.evaluate("document.cookie"));
    page.navigate(server.PREFIX);
    assertEquals("cookie1=1", page.evaluate("document.cookie"));
  }

  @Test
  public void shouldRemoveCookiesByPath(Page page, BrowserContext context, Server server) throws MalformedURLException {
    context.addCookies(Arrays.asList(
      new Cookie("cookie1", "1").setDomain(new URL(server.PREFIX).getHost()).setPath("/api/v1"),
      new Cookie("cookie2", "2").setDomain(new URL(server.PREFIX).getHost()).setPath("/api/v2"),
      new Cookie("cookie3", "3").setDomain(new URL(server.PREFIX).getHost()).setPath("/")
    ));
    page.navigate(server.PREFIX + "/api/v1");
    assertEquals("cookie1=1; cookie3=3", page.evaluate("document.cookie"));
    context.clearCookies(new BrowserContext.ClearCookiesOptions().setPath("/api/v1"));
    assertEquals("cookie3=3", page.evaluate("document.cookie"));
    page.navigate(server.PREFIX + "/api/v2");
    assertEquals("cookie2=2; cookie3=3", page.evaluate("document.cookie"));
    page.navigate(server.PREFIX + "/");
    assertEquals("cookie3=3", page.evaluate("document.cookie"));
  }

  @Test
  public void shouldRemoveCookiesByNameAndDomain(Page page, BrowserContext context, Server server) throws MalformedURLException {
    context.addCookies(Arrays.asList(
      new Cookie("cookie1", "1").setDomain(new URL(server.PREFIX).getHost()).setPath("/"),
      new Cookie("cookie1", "1").setDomain(new URL(server.CROSS_PROCESS_PREFIX).getHost()).setPath("/")
    ));
    page.navigate(server.PREFIX);
    assertEquals("cookie1=1", page.evaluate("document.cookie"));
    context.clearCookies(new BrowserContext.ClearCookiesOptions().setName("cookie1").setDomain(new URL(server.PREFIX).getHost()));
    assertEquals("", page.evaluate("document.cookie"));
    page.navigate(server.CROSS_PROCESS_PREFIX);
    assertEquals("cookie1=1", page.evaluate("document.cookie"));
  }
}
