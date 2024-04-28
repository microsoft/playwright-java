package com.microsoft.playwright.junit;

import org.junit.jupiter.api.Tag;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Tests tagged with @Transient should be excluded in normal circumstances because they are run indirectly by the test engine
// in order to verify behavior that would normally fail the test run (failed tests).
@Tag("transient")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Transient {
}
