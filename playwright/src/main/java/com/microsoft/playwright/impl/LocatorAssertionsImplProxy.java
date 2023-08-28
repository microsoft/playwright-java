package com.microsoft.playwright.impl;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.assertions.LocatorAssertions;

import java.util.List;
import java.util.regex.Pattern;

public class LocatorAssertionsImplProxy extends SoftAssertionsBase implements LocatorAssertions {
  private final LocatorAssertionsImpl locatorAssertionsImpl;

  LocatorAssertionsImplProxy(Locator locator, List<Throwable> results) {
    this(results, new LocatorAssertionsImpl(locator));
  }

  private LocatorAssertionsImplProxy(List<Throwable> results, LocatorAssertionsImpl locatorAssertionsImpl) {
    super(results);
    this.locatorAssertionsImpl = locatorAssertionsImpl;
  }

  @Override
  public LocatorAssertions not() {
    return new LocatorAssertionsImplProxy(super.results, locatorAssertionsImpl.not());
  }

  @Override
  public void isAttached(IsAttachedOptions options) {
    assertAndCaptureResult(() -> locatorAssertionsImpl.isAttached(options));
  }

  @Override
  public void isChecked(IsCheckedOptions options) {
    assertAndCaptureResult(() -> locatorAssertionsImpl.isChecked(options));
  }

  @Override
  public void isDisabled(IsDisabledOptions options) {
    assertAndCaptureResult(() -> locatorAssertionsImpl.isDisabled(options));
  }

  @Override
  public void isEditable(IsEditableOptions options) {
    assertAndCaptureResult(() -> locatorAssertionsImpl.isEditable(options));
  }

  @Override
  public void isEmpty(IsEmptyOptions options) {
    assertAndCaptureResult(() -> locatorAssertionsImpl.isEmpty(options));
  }

  @Override
  public void isEnabled(IsEnabledOptions options) {
    assertAndCaptureResult(() -> locatorAssertionsImpl.isEnabled(options));
  }

  @Override
  public void isFocused(IsFocusedOptions options) {
    assertAndCaptureResult(() -> locatorAssertionsImpl.isFocused(options));
  }

  @Override
  public void isHidden(IsHiddenOptions options) {
    assertAndCaptureResult(() -> locatorAssertionsImpl.isHidden(options));
  }

  @Override
  public void isInViewport(IsInViewportOptions options) {
    assertAndCaptureResult(() -> locatorAssertionsImpl.isInViewport(options));
  }

  @Override
  public void isVisible(IsVisibleOptions options) {
    assertAndCaptureResult(() -> locatorAssertionsImpl.isVisible(options));
  }

  @Override
  public void containsText(String expected, ContainsTextOptions options) {
    assertAndCaptureResult(() -> locatorAssertionsImpl.containsText(expected, options));
  }

  @Override
  public void containsText(Pattern expected, ContainsTextOptions options) {
    assertAndCaptureResult(() -> locatorAssertionsImpl.containsText(expected, options));
  }

  @Override
  public void containsText(String[] expected, ContainsTextOptions options) {
    assertAndCaptureResult(() -> locatorAssertionsImpl.containsText(expected, options));
  }

  @Override
  public void containsText(Pattern[] expected, ContainsTextOptions options) {
    assertAndCaptureResult(() -> locatorAssertionsImpl.containsText(expected, options));
  }

  @Override
  public void hasAttribute(String name, String value, HasAttributeOptions options) {
    assertAndCaptureResult(() -> locatorAssertionsImpl.hasAttribute(name, value, options));
  }

  @Override
  public void hasAttribute(String name, Pattern value, HasAttributeOptions options) {
    assertAndCaptureResult(() -> locatorAssertionsImpl.hasAttribute(name, value, options));
  }

  @Override
  public void hasClass(String expected, HasClassOptions options) {
    assertAndCaptureResult(() -> locatorAssertionsImpl.hasClass(expected, options));
  }

  @Override
  public void hasClass(Pattern expected, HasClassOptions options) {
    assertAndCaptureResult(() -> locatorAssertionsImpl.hasClass(expected, options));
  }

  @Override
  public void hasClass(String[] expected, HasClassOptions options) {
    assertAndCaptureResult(() -> locatorAssertionsImpl.hasClass(expected, options));
  }

  @Override
  public void hasClass(Pattern[] expected, HasClassOptions options) {
    assertAndCaptureResult(() -> locatorAssertionsImpl.hasClass(expected, options));
  }

  @Override
  public void hasCount(int count, HasCountOptions options) {
    assertAndCaptureResult(() -> locatorAssertionsImpl.hasCount(count, options));
  }

  @Override
  public void hasCSS(String name, String value, HasCSSOptions options) {
    assertAndCaptureResult(() -> locatorAssertionsImpl.hasCSS(name, value, options));
  }

  @Override
  public void hasCSS(String name, Pattern value, HasCSSOptions options) {
    assertAndCaptureResult(() -> locatorAssertionsImpl.hasCSS(name, value, options));
  }

  @Override
  public void hasId(String id, HasIdOptions options) {
    assertAndCaptureResult(() -> locatorAssertionsImpl.hasId(id, options));
  }

  @Override
  public void hasId(Pattern id, HasIdOptions options) {
    assertAndCaptureResult(() -> locatorAssertionsImpl.hasId(id, options));
  }

  @Override
  public void hasJSProperty(String name, Object value, HasJSPropertyOptions options) {
    assertAndCaptureResult(() -> locatorAssertionsImpl.hasJSProperty(name, value, options));
  }

  @Override
  public void hasText(String expected, HasTextOptions options) {
    assertAndCaptureResult(() -> locatorAssertionsImpl.hasText(expected, options));
  }

  @Override
  public void hasText(Pattern expected, HasTextOptions options) {
    assertAndCaptureResult(() -> locatorAssertionsImpl.hasText(expected, options));
  }

  @Override
  public void hasText(String[] expected, HasTextOptions options) {
    assertAndCaptureResult(() -> locatorAssertionsImpl.hasText(expected, options));
  }

  @Override
  public void hasText(Pattern[] expected, HasTextOptions options) {
    assertAndCaptureResult(() -> locatorAssertionsImpl.hasText(expected, options));
  }

  @Override
  public void hasValue(String value, HasValueOptions options) {
    assertAndCaptureResult(() -> locatorAssertionsImpl.hasValue(value, options));
  }

  @Override
  public void hasValue(Pattern value, HasValueOptions options) {
    assertAndCaptureResult(() -> locatorAssertionsImpl.hasValue(value, options));
  }

  @Override
  public void hasValues(String[] values, HasValuesOptions options) {
    assertAndCaptureResult(() -> locatorAssertionsImpl.hasValues(values, options));
  }

  @Override
  public void hasValues(Pattern[] values, HasValuesOptions options) {
    assertAndCaptureResult(() -> locatorAssertionsImpl.hasValues(values, options));
  }
}
