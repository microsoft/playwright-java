package com.microsoft.playwright;

import com.microsoft.playwright.assertions.PageAssertions;
import com.microsoft.playwright.impl.PageAssertionsImpl;
import com.microsoft.playwright.impl.PageAssertionsImplProxy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.regex.Pattern;

import static com.microsoft.playwright.Utils.createProxy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

// The only thing we want to verify in these tests is that the correct method was called
@Tag("mockito")
@ExtendWith(MockitoExtension.class)
public class TestSoftPageAssertions {
  @Mock
  private PageAssertionsImpl pageAssertionsMock;
  private PageAssertionsImplProxy proxy;
  private final static Pattern pattern = Pattern.compile("");

  @BeforeEach
  void beforeEach() {
    proxy = createProxy(PageAssertionsImplProxy.class, pageAssertionsMock);
  }

  @Test
  void proxyImplementsPageAssertions() {
    assertTrue(PageAssertions.class.isAssignableFrom(proxy.getClass()));
  }

  @Test
  void not() {
    proxy.not();
    verify(pageAssertionsMock).not();
  }

  @Test
  void hasTitleString() {
    assertDoesNotThrow(() -> proxy.hasTitle("", null));
    verify(pageAssertionsMock).hasTitle("", null);
  }

  @Test
  void hasTitlePattern() {
    assertDoesNotThrow(() -> proxy.hasTitle(pattern, null));
    verify(pageAssertionsMock).hasTitle(pattern, null);
  }

  @Test
  void hasURLString() {
    assertDoesNotThrow(() -> proxy.hasURL("", null));
    verify(pageAssertionsMock).hasURL("", null);
  }

  @Test
  void hasURLPattern() {
    assertDoesNotThrow(() -> proxy.hasURL(pattern, null));
    verify(pageAssertionsMock).hasURL(pattern, null);
  }
}
