package com.microsoft.playwright.junit;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@ExtendWith(ServerLifecycle.class)
@Tag("fixtures")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FixtureTest {
}
