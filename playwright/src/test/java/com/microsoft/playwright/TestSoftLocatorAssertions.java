package com.microsoft.playwright;

import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.impl.LocatorAssertionsImpl;
import com.microsoft.playwright.impl.LocatorAssertionsImplProxy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.microsoft.playwright.Utils.createProxy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

// The only thing we want to verify in these tests is that the correct method was called
@ExtendWith(MockitoExtension.class)
public class TestSoftLocatorAssertions {
  @Mock
  private LocatorAssertionsImpl locatorAssertionsMock;
  private final static Pattern pattern = Pattern.compile("");
  private final static Pattern[] patternArray = new Pattern[]{};
  private final static String[] stringArray = new String[]{};
  private LocatorAssertionsImplProxy proxy;

  @BeforeEach
  void beforeEach() {
    proxy = createProxy(LocatorAssertionsImplProxy.class, locatorAssertionsMock);
  }

  @Test
  void proxyImplementsLocatorAssertions() {
    assertTrue(LocatorAssertions.class.isAssignableFrom(proxy.getClass()));
  }

  @Test
  void not() {
    proxy.not();
    verify(locatorAssertionsMock).not();
  }

  @Test
  void isAttached() {
    assertDoesNotThrow(() -> proxy.isAttached(null));
    verify(locatorAssertionsMock).isAttached(null);
  }

  @Test
  void isChecked() {
    assertDoesNotThrow(() -> proxy.isChecked(null));
    verify(locatorAssertionsMock).isChecked(null);
  }

  @Test
  void isDisabled() {
    assertDoesNotThrow(() -> proxy.isDisabled(null));
    verify(locatorAssertionsMock).isDisabled(null);
  }

  @Test
  void isEditable() {
    assertDoesNotThrow(() -> proxy.isEditable(null));
    verify(locatorAssertionsMock).isEditable(null);
  }

  @Test
  void isEmpty() {
    assertDoesNotThrow(() -> proxy.isEmpty(null));
    verify(locatorAssertionsMock).isEmpty(null);
  }

  @Test
  void isEnabled() {
    assertDoesNotThrow(() -> proxy.isEnabled(null));
    verify(locatorAssertionsMock).isEnabled(null);
  }

  @Test
  void isFocused() {
    assertDoesNotThrow(() -> proxy.isFocused(null));
    verify(locatorAssertionsMock).isFocused(null);
  }

  @Test
  void isHidden() {
    assertDoesNotThrow(() -> proxy.isHidden(null));
    verify(locatorAssertionsMock).isHidden(null);
  }

  @Test
  void isInViewPort() {
    assertDoesNotThrow(() -> proxy.isInViewport(null));
    verify(locatorAssertionsMock).isInViewport(null);
  }

  @Test
  void isVisible() {
    assertDoesNotThrow(() -> proxy.isVisible(null));
    verify(locatorAssertionsMock).isVisible(null);
  }

  @Test
  void containsTextString() {
    assertDoesNotThrow(() -> proxy.containsText("", null));
    verify(locatorAssertionsMock).containsText("", null);
  }

  @Test
  void containsTextPattern() {
    assertDoesNotThrow(() -> proxy.containsText(pattern, null));
    verify(locatorAssertionsMock).containsText(pattern, null);
  }

  @Test
  void containsTextStringArray() {
    assertDoesNotThrow(() -> proxy.containsText(stringArray, null));
    verify(locatorAssertionsMock).containsText(stringArray, null);
  }

  @Test
  void containsTextPatternArray() {
    assertDoesNotThrow(() -> proxy.containsText(patternArray, null));
    verify(locatorAssertionsMock).containsText(patternArray, null);
  }

  @Test
  void hasAttributeStringValue() {
    assertDoesNotThrow(() -> proxy.hasAttribute("", "", null));
    verify(locatorAssertionsMock).hasAttribute("", "", null);
  }

  @Test
  void hasAttributePatternValue() {
    assertDoesNotThrow(() -> proxy.hasAttribute("", pattern, null));
    verify(locatorAssertionsMock).hasAttribute("", pattern, null);
  }

  @Test
  void hasClassString() {
    assertDoesNotThrow(() -> proxy.hasClass("", null));
    verify(locatorAssertionsMock).hasClass("", null);
  }

  @Test
  void hasClassPattern() {
    assertDoesNotThrow(() -> proxy.hasClass(pattern, null));
    verify(locatorAssertionsMock).hasClass(pattern, null);
  }

  @Test
  void hasClassStringArray() {
    assertDoesNotThrow(() -> proxy.hasClass(stringArray, null));
    verify(locatorAssertionsMock).hasClass(stringArray, null);
  }

  @Test
  void hasClassPatternArray() {
    assertDoesNotThrow(() -> proxy.hasClass(patternArray, null));
    verify(locatorAssertionsMock).hasClass(patternArray, null);
  }

  @Test
  void hasCount() {
    assertDoesNotThrow(() -> proxy.hasCount(0, null));
    verify(locatorAssertionsMock).hasCount(0, null);
  }

  @Test
  void hasCSSString() {
    assertDoesNotThrow(() -> proxy.hasCSS("", "", null));
    verify(locatorAssertionsMock).hasCSS("", "", null);
  }

  @Test
  void hasCSSPattern() {
    assertDoesNotThrow(() -> proxy.hasCSS("", pattern, null));
    verify(locatorAssertionsMock).hasCSS("", pattern, null);
  }

  @Test
  void hasIdString() {
    assertDoesNotThrow(() -> proxy.hasId("", null));
    verify(locatorAssertionsMock).hasId("", null);
  }

  @Test
  void hasIdPattern() {
    assertDoesNotThrow(() -> proxy.hasId(pattern, null));
    verify(locatorAssertionsMock).hasId(pattern, null);
  }

  @Test
  void hasJSProperty() {
    assertDoesNotThrow(() -> proxy.hasJSProperty("", null, null));
    verify(locatorAssertionsMock).hasJSProperty("", null, null);
  }

  @Test
  void hasTextString() {
    assertDoesNotThrow(() -> proxy.hasText("", null));
    verify(locatorAssertionsMock).hasText("", null);
  }

  @Test
  void hasTextPattern() {
    assertDoesNotThrow(() -> proxy.hasText(pattern, null));
    verify(locatorAssertionsMock).hasText(pattern, null);
  }

  @Test
  void hasTextStringArray() {
    assertDoesNotThrow(() -> proxy.hasText(stringArray, null));
    verify(locatorAssertionsMock).hasText(stringArray, null);
  }

  @Test
  void hasTextPatternArray() {
    assertDoesNotThrow(() -> proxy.hasText(patternArray, null));
    verify(locatorAssertionsMock).hasText(patternArray, null);
  }

  @Test
  void hasValueString() {
    assertDoesNotThrow(() -> proxy.hasValue("", null));
    verify(locatorAssertionsMock).hasValue("", null);
  }

  @Test
  void hasValuePattern() {
    assertDoesNotThrow(() -> proxy.hasValue(pattern, null));
    verify(locatorAssertionsMock).hasValue(pattern, null);
  }

  @Test
  void hasValuesStringArray() {
    assertDoesNotThrow(() -> proxy.hasValues(stringArray, null));
    verify(locatorAssertionsMock).hasValues(stringArray, null);
  }

  @Test
  void hasValuesPatternArray() {
    assertDoesNotThrow(() -> proxy.hasValues(patternArray, null));
    verify(locatorAssertionsMock).hasValues(patternArray, null);
  }
}
