package com.microsoft.playwright;

import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class TestPageStrict extends TestBase {
  @Test
  void shouldFailPageTextContentInStrictMode() {
    page.setContent("<span>span1</span><div><span>target</span></div>");
    try {
      page.textContent("span", new Page.TextContentOptions().setStrict(true));
      fail("did not throw");
    } catch (PlaywrightException e) {
      assertTrue(e.getMessage().contains("strict mode violation"));
    }
  }

  @Test
  void shouldFailPageGetAttributeInStrictMode() {
    page.setContent("<span>span1</span><div><span>target</span></div>");
    try {
      page.getAttribute("span", "id", new Page.GetAttributeOptions().setStrict(true));
      fail("did not throw");
    } catch (PlaywrightException e) {
      assertTrue(e.getMessage().contains("strict mode violation"));
    }
  }

  @Test
  void shouldFailPageFillInStrictMode() {
    page.setContent("<input></input><div><input></input></div>");
    try {
      page.fill("input", "text", new Page.FillOptions().setStrict(true));
      fail("did not throw");
    } catch (PlaywrightException e) {
      assertTrue(e.getMessage().contains("strict mode violation"));
    }
  }


  @Test
  void shouldFailPageInStrictMode() {
    page.setContent("<span>span1</span><div><span>target</span></div>");
    try {
      ElementHandle error = page.querySelector("span", new Page.QuerySelectorOptions().setStrict(true));
      fail("did not throw");
    } catch (PlaywrightException e) {
      assertTrue(e.getMessage().contains("strict mode violation"));
    }
  }

  @Test
  void shouldFailPageWaitForSelectorInStrictMode() {
    page.setContent("<span>span1</span><div><span>target</span></div>");
    try {
      page.waitForSelector("span", new Page.WaitForSelectorOptions().setStrict(true));
      fail("did not throw");
    } catch (PlaywrightException e) {
      assertTrue(e.getMessage().contains("strict mode violation"));
    }
  }

  @Test
  void shouldFailPageDispatchEventInStrictMode() {
    page.setContent("<span></span><div><span></span></div>");
    try {
      page.dispatchEvent("span", "click", new HashMap<>(), new Page.DispatchEventOptions().setStrict(true));
      fail("did not throw");
    } catch (PlaywrightException e) {
      assertTrue(e.getMessage().contains("strict mode violation"));
    }
  }
}
